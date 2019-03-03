package com.example.hx.ihanc.purchase;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.example.hx.ihanc.Goods;
import com.example.hx.ihanc.GoodsAdapter;
import com.example.hx.ihanc.IhancHttpClient;
import com.example.hx.ihanc.MainActivity;
import com.example.hx.ihanc.R;
import com.example.hx.ihanc.Unit;
import com.example.hx.ihanc.UnitAdapter;
import com.example.hx.ihanc.Utils;
import com.example.hx.ihanc.store.StoreAdapter;
import com.example.hx.ihanc.store.store;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cz.msebera.android.httpclient.Header;

import static com.example.hx.ihanc.MainActivity.storesArray;
import static com.example.hx.ihanc.SaleMainFragment.mGoodsDataList;
import static com.example.hx.ihanc.purchase.PurchaseDetailDialog.purchasedDetails;
import static com.example.hx.ihanc.purchase.PurchaseFragment.purchaseDetails;
import static java.lang.Integer.parseInt;

public class DialogPurchaseDetail extends DialogFragment {
    private int position;
    private int supply_id;
    private View view;
    private AutoCompleteTextView goodsTV;
    private Goods currentGood;
    private GoodsAdapter goodsAdapter;
    private Spinner unitSpinner;
    private UnitAdapter unitAdapter;
    private ArrayList<Unit> unitList=new ArrayList<Unit>();
    private EditText price;
    private EditText number;
    private EditText sum;
    private boolean input=true;
    private Spinner storeSpinner;
    private Button saveButton;
    private Button delButton;
    private onUpdatePurchaseDetail onUpdatePurchaseDetail=null;
    private int purchase_id=0;
    private int purchase_detail_id=0;

    public  DialogPurchaseDetail(){}
    public static DialogPurchaseDetail NewInstance(int position,int supply_id){
        DialogPurchaseDetail f=new DialogPurchaseDetail();
        Bundle b=new Bundle();
        b.putInt("position",position);
        b.putInt("supply_id",supply_id);
        f.setArguments(b);
        return f;
    };

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        position=getArguments().getInt("position");
        supply_id=getArguments().getInt("supply_id");
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view=inflater.inflate(R.layout.purchase_detail_dialog,container,false);
        goodsTV=view.findViewById(R.id.goods);
        goodsAdapter=new GoodsAdapter(getContext(),R.layout.member,mGoodsDataList);
        goodsTV.setAdapter(goodsAdapter);
        goodsTV.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (!TextUtils.isEmpty(editable)){
                    GoodsAdapter.MyFilter mFilter=goodsAdapter.getFilter();
                    mFilter.setMyFilter(Utils.GOODSFILTERSEARCHVIEW);
                    mFilter.filter(editable.toString());
                }
            }
        });
        goodsTV.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                onGoodSelected(adapterView,i);
            }
        });
        unitSpinner=view.findViewById(R.id.unit);
        unitAdapter=new UnitAdapter(getContext(),R.layout.unit,unitList);
        unitSpinner.setAdapter(unitAdapter);
        price=view.findViewById(R.id.price);
        number=view.findViewById(R.id.number);
        sum=view.findViewById(R.id.sum);
        price.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if(number.getText().toString().trim().length()==0||price.getText().toString().trim().length()==0){
                    sum.setText("");
                }
                if (input){
                    input=false;
                    double doubleSum=getNum(price)*getNum(number);
                    sum.setText(Math.round(doubleSum)+"");
                    input=true;
                }
            }
        });
        number.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if(number.getText().toString().trim().length()==0||price.getText().toString().trim().length()==0){
                    sum.setText("");
                }
                if (input){
                    input=false;
                    double doubleSum=getNum(price)*getNum(number);
                    sum.setText(Math.round(doubleSum)+"");
                    input=true;
                }
            }
        });
        sum.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (input&&number.getText().toString().trim().length()>0
                        &&sum.getText().toString().trim().length()>0
                        &&parseInt(number.getText().toString().trim())!=0
                ){
                    input=false;
                    double doublePrice=getNum(sum)/getNum(number);
                    price.setText((double)Math.round(doublePrice*100)/100+"");
                    input=true;
                }
            }
        });
        storeSpinner=view.findViewById(R.id.store);
        StoreAdapter storeAdapter=new StoreAdapter(getContext(),R.layout.unit,storesArray);
        storeSpinner.setAdapter(storeAdapter);
        saveButton=view.findViewById(R.id.save);
        saveButton.setOnClickListener(onSaveButtonClickListener());
        delButton=view.findViewById(R.id.delete);
        if(position>-1){
            input=false;
            final PurchaseDetail detail=purchase_id>0?purchasedDetails.get(position):purchaseDetails.get(position);
            purchase_detail_id=detail.purchase_detail_id;
            price.setText(detail.price+"");
            number.setText(detail.number+"");
            sum.setText(detail.sum+"");
            currentGood=detail.goods;
            input=true;
            goodsTV.setFocusable(purchase_id==0);
            goodsTV.setText(currentGood.getGoods_name());
            unitList.clear();
            RequestParams params=new RequestParams();
            params.put("goods_id",currentGood.getGoods_id());
            params.put("supply_id",supply_id);
            params.put("unit_id",currentGood.getGoods_unit_id());
            Log.d("purchase",params.toString());
            IhancHttpClient.get("/index/purchase/getUnit", params, new AsyncHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                    String res=new String(responseBody);
                    Log.d("purchase",res);
                    try{
                        JSONObject obj=new JSONObject(res);
                        JSONArray units=obj.getJSONArray("unit");
                        int i=0;
                        for (int j = 0; j <units.length() ; j++) {
                            JSONObject item=units.getJSONObject(j);
                            unitList.add(new Unit(item.getInt("unit_id"),item.getString("unit_name")));
                            if(item.getInt("unit_id")==detail.unit_id) i=j;
                        }
                        unitAdapter.notifyDataSetChanged();
                        unitSpinner.setSelection(i,true);
                    }catch (JSONException e){e.printStackTrace();}
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {

                }
            });
            for (int i = 0; i <storesArray.size() ; i++) {
                if(storesArray.get(i).getStore_name().equals(detail.store_name)) {
                    storeSpinner.setSelection(i);
                    break;
                }
            }
            storeSpinner.setEnabled(false);
            goodsTV.setFocusable(true);
            view.clearFocus();
            delButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    AlertDialog.Builder builder=new AlertDialog.Builder(getContext());
                    builder.setPositiveButton("删除", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                            dismiss();
                            onUpdatePurchaseDetail.updatePurchaseDetail(position,null);
                        }
                    });
                    builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                        }
                    });
                   builder.setMessage("确认删除该明细？");
                   builder.show();
                }
            });
        }else{
            delButton.setVisibility(View.GONE);
        }
        return view;
    }

    private void onGoodSelected(AdapterView adapterView,int i){
        currentGood=(Goods)adapterView.getAdapter().getItem(i);
        goodsTV.setText(currentGood.getGoods_name());
        unitList.clear();
        RequestParams params=new RequestParams();
        params.put("goods_id",currentGood.getGoods_id());
        params.put("supply_id",supply_id);
        params.put("unit_id",currentGood.getGoods_unit_id());
        IhancHttpClient.get("/index/purchase/getUnit", params, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                String res=new String(responseBody);
                Log.d("purchaseDialog",res);
                try{
                    JSONObject obj=new JSONObject(res);
                    if(!obj.getString("result").equals("null")){
                        price.setText(obj.getString("result"));
                    };
                    JSONArray units=obj.getJSONArray("unit");
                    int i=0;
                    for (int j = 0; j <units.length() ; j++) {
                        JSONObject item=units.getJSONObject(j);
                        unitList.add(new Unit(item.getInt("unit_id"),item.getString("unit_name")));
                        if(item.getInt("unit_id")==currentGood.getGoods_unit_id()) i=j;
                    }
                    unitAdapter.notifyDataSetChanged();
                    unitSpinner.setSelection(i,true);
                }catch (JSONException e){e.printStackTrace();}
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {

            }
        });
    };

    public double getNum(TextView textView1){
        double number=0.00;
        String str=textView1.getText().toString().trim();
        if(!TextUtils.isEmpty(str)) {
            Pattern pattern = Pattern.compile("-?[^0-9]+(.[0-9]{2})?$");
            Matcher matcher = pattern.matcher((CharSequence) str);
            str = matcher.replaceAll("").trim();
            if (str.length() >= 1) number = Double.parseDouble(str);
        }
        return number;
    }

    public void setOnUpdatePurchaseDetail(onUpdatePurchaseDetail onUpdatePurchaseDetail){
        this.onUpdatePurchaseDetail=onUpdatePurchaseDetail;
    }

    private View.OnClickListener onSaveButtonClickListener(){
        return  new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hideKeyboard();
                if (currentGood==null) return;
                if(TextUtils.isEmpty(sum.getText())||TextUtils.isEmpty(number.getText())||TextUtils.isEmpty(price.getText())){
                    Utils.toast(getContext(),"请完善进货信息！");
                    return;
                }
                    PurchaseDetail detail = new PurchaseDetail(
                            purchase_id,
                            currentGood,
                            ((Unit) unitSpinner.getSelectedItem()).getUnit_id(),
                            ((Unit) unitSpinner.getSelectedItem()).getUnit_name(),
                            getNum(price),
                            getNum(number),
                            (int) getNum(sum),
                            purchase_detail_id,
                            ((store) storeSpinner.getSelectedItem()).getStore_name(),
                            ((store) storeSpinner.getSelectedItem()).getStore_id()
                    );
                    onUpdatePurchaseDetail.updatePurchaseDetail(position, detail);
                    dismiss();
            }
        };
    }

    public void setPurchased(int purchase_id){
     this.purchase_id=purchase_id;
    }

    public interface onUpdatePurchaseDetail{
         void updatePurchaseDetail(int position,PurchaseDetail detail);
    }
    public void hideKeyboard() {
        view.clearFocus();
        InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm.isActive() && ((MainActivity)getActivity()).getCurrentFocus() != null) {
            if (((MainActivity)getActivity()).getCurrentFocus().getWindowToken() != null) {
                imm.hideSoftInputFromWindow(((MainActivity)getActivity()).getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
            }
        }
    }

}
