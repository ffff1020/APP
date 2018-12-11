package com.example.hx.ihanc;

import android.support.v4.app.DialogFragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;

import cz.msebera.android.httpclient.Header;

public class SaleDetailModifyDialog extends DialogFragment {
    private SaleDetail saleDetail;
    private String member;
    private MyNumberEdit sumEdit;
    private MyNumberEdit priceEdit;
    private MyNumberEdit numberEdit;
    private Spinner unitSpinner;
    private UnitAdapter mUnitAdapter;
    private Button mSaveButton;
    private SaveListener saveListener;

    public  static  SaleDetailModifyDialog newInstance(
            String member,SaleDetail saleDetail){
            SaleDetailModifyDialog fd=new SaleDetailModifyDialog();
            Bundle args = new Bundle();
            args.putString("member",member);
            args.putParcelable("saleDetail",saleDetail);
            fd.setArguments(args);
            return fd;
    };
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        member = getArguments().getString("member");
        saleDetail=getArguments().getParcelable("saleDetail");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.dialog_sale_detail_modify, container, false);
        TextView memberTv = v.findViewById(R.id.title);
        memberTv.setText(member);
        priceEdit=v.findViewById(R.id.price);
        sumEdit=v.findViewById(R.id.sum);
        numberEdit=v.findViewById(R.id.number);
        priceEdit.setNum(saleDetail.getPriceDouble());
        sumEdit.setNum(saleDetail.getSum());
        numberEdit.setNum(saleDetail.getNumberDouble());
        priceEdit.setTitle("价格:");
        sumEdit.setTitle("金额:");
        numberEdit.setTitle("数量:");
        priceEdit.init=true;
        sumEdit.init=true;
        numberEdit.init=true;
        TextView goodTv=v.findViewById(R.id.goods);
        goodTv.setText("产品名称："+saleDetail.getGoods_name());
        sumEdit.addTextChangedListener(new MyNumberEdit.TextChangedListener() {
            @Override
            public void TextChanged() {
                if(numberEdit.getNum()==0.0)
                    return;
                Double sum=sumEdit.getNum()/numberEdit.getNum();
                priceEdit.check=false;
                numberEdit.check=false;
                priceEdit.setNum(sum);
            }
        });
        priceEdit.addTextChangedListener(new MyNumberEdit.TextChangedListener() {
            @Override
            public void TextChanged() {
                Double sum=priceEdit.getNum()*numberEdit.getNum();
                sumEdit.check=false;
                sumEdit.setNum(sum);
            }
        });
        numberEdit.addTextChangedListener(new MyNumberEdit.TextChangedListener() {
            @Override
            public void TextChanged() {
                // Log.d("fragment","mWeight listener");
                Double sum=priceEdit.getNum()*numberEdit.getNum();
                sumEdit.check=false;
                sumEdit.setNum(sum);
            }
        });

        unitSpinner=(Spinner)v.findViewById(R.id.unitSpinner);
        mUnitAdapter=new UnitAdapter(getContext(),R.layout.unit,MainActivity.mUnitList);
        unitSpinner.setAdapter(mUnitAdapter);
        unitSpinner.setSelection(0);
        RequestParams params = new RequestParams();
        params.put("goods_id",saleDetail.getGoods_id());
        IhancHttpClient.get("/index/sale/getUnitApp", params, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                String res =new String(responseBody).trim();
                UnitAdapter.UnitFilter mFilter=mUnitAdapter.getFilter();
                if(res.length()<3){
                    mFilter.filter(String.valueOf(saleDetail.good.getUnit_id_0()));
                    unitSpinner.setSelection(0);
                }else{
                    mFilter.filter(saleDetail.good.getUnit_id_0()+"and"+res.substring(2,res.length()-1));
                    String[] str=(saleDetail.good.getUnit_id_0()+"and"+res.substring(2,res.length()-1)).split("and");
                    int index=0;
                    for (int i=0;i<str.length;i++){
                        if(Integer.parseInt(str[i])==saleDetail.getUnit_id()){
                            index=i;
                            break;
                        }
                    }
                    unitSpinner.setSelection(index);
                }
            }
            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                String res =new String(responseBody).trim();
               // Log.d("unitFail",res);
            }
        });

        mSaveButton=v.findViewById(R.id.saveButton);
        mSaveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    JSONObject params = new JSONObject();
                    params.put("sale_detail_id", saleDetail.sale_detail_id);
                    params.put("price", priceEdit.getNum());
                    params.put("number", numberEdit.getNum());
                    params.put("sum", sumEdit.getNum());
                    params.put("sale_id", saleDetail.sale_id);
                   // params.put("goods_id", saleDetail.good.getGoods_id());
                    int selectedUnitId = mUnitAdapter.getItem(unitSpinner.getSelectedItemPosition()).getUnit_id();
                    params.put("unit_check", selectedUnitId==saleDetail.good.getUnit_id_0());
                    params.put("unit_id",selectedUnitId);
                     System.out.print(params);
                    IhancHttpClient.postJson(getContext(),"/index/sale/saleDetailUpdate", params, new AsyncHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                        String res =new String(responseBody).trim();
                        Log.d("modifySuccess",res);
                        try {
                            JSONObject obj=new JSONObject(res);
                            if(obj.getInt("result")==1) {
                                Utils.toast(getContext(), "保存成功！");
                                saveListener.saved();
                                dismiss();
                            }

                        }catch (JSONException e){e.printStackTrace();}
                    }
                    @Override
                    public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                        Log.d("modifyFailure",new String(responseBody));
                    }
                    });
                }catch (JSONException e){e.printStackTrace();}
            }
        });
        return v;
    }
    public interface SaveListener{
        void saved();
    }
    public void SaveListener(SaveListener saveListener){
        this.saveListener=saveListener;
    }

}
