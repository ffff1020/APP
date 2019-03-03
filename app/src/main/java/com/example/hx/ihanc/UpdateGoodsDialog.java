package com.example.hx.ihanc;

import android.app.DownloadManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;

import static com.example.hx.ihanc.AddMemberDialog.getPinYinHeadChar;
import static com.example.hx.ihanc.GoodsSettingActivity.mCategoryDataList;

public class UpdateGoodsDialog extends DialogFragment {
    private Goods good=null;
    private View v;
    private Spinner unit0;
    private Spinner unit1;
    private Spinner unit2;
    private UnitAdapter mUnitAdapter;
    private EditText goods_nameET;
    private EditText goods_snET;
    private EditText priceET;
    private Spinner catSpinner;
    private CategoryAdapter mCategoryAdapter;
    private TextView unitTv1;
    private TextView unitTv2;
    private TextView unitTextView1;
    private TextView unitTextView2;
    private EditText unit_price1;
    private EditText unit_price2;
    private EditText price1;
    private EditText price2;
    private int unit_price_id1=0;
    private int unit_price_id2=0;
    private   refreshGoodsInterface  refreshGoodsSetting=null;
    private  static refreshGoodsInterface  refreshGoodsSaleMainFragment=null;
    public static UpdateGoodsDialog newInstance(String good) {
        Bundle args = new Bundle();
        UpdateGoodsDialog fragment = new UpdateGoodsDialog();
        args.putString("good",good);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        String str=getArguments().getString("good");
        if(str.length()>0) {
            try {
                JSONObject obj = new JSONObject(str);
                good = new Goods(
                        obj.getInt("goods_id"), obj.getString("goods_name"),
                        obj.getString("goods_unit"), obj.getDouble("goods_price"),
                        obj.getString("goods_sn"), obj.getInt("category_id"),
                        obj.getString("goods_unit_name"), obj.getInt("promote")
                );
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        v=inflater.inflate(R.layout.dialog_update_good, container, false);
        mUnitAdapter=new UnitAdapter(getContext(),R.layout.unit,MainActivity.mUnitList);
        goods_nameET=v.findViewById(R.id.goods_name);
        goods_snET=v.findViewById(R.id.goods_sn);
        initData();
        unit_price1=v.findViewById(R.id.unit_price1);
        unit_price2=v.findViewById(R.id.unit_price2);

        Button save=v.findViewById(R.id.addGoodsButton);
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(good==null)
                for (int i = 0; i <GoodsSettingActivity.mGoodsDataList.size() ; i++) {
                    Goods good=(Goods) GoodsSettingActivity.mGoodsDataList.get(i);
                    if(good.goods_name.equals(goods_nameET.getText().toString().trim())){
                        Utils.toast(getContext(),"改商品已经存在，请核对！");
                        return;
                    }
                }

                JSONObject data=new JSONObject();
                try{
                    int goods_id=0;
                    if(good!=null) goods_id=good.goods_id;
                    data.put("goods_id",goods_id);
                    data.put("goods_name",goods_nameET.getText().toString().trim());
                    data.put("goods_sn",goods_snET.getText().toString().trim());
                    JSONObject cat=new JSONObject();
                    Unit mUnit0=(Unit)unit0.getSelectedItem();
                    category category=(category)catSpinner.getSelectedItem();
                    cat.put("cat_id",category.getCategory_id());
                    cat.put("cat_name",category.getCategory_name());
                    data.put("cat_id",cat);
                    data.put("warn_stock",0);
                    JSONObject unit_id=new JSONObject();
                    unit_id.put("unit_id",mUnit0.getUnit_id());
                    unit_id.put("unit_name",mUnit0.getUnit_name());
                    data.put("unit_id",unit_id);
                    double price=priceET.getText().toString().trim().length()>0?Double.parseDouble(priceET.getText().toString().trim()):0.0;
                    data.put("out_price",price);
                    data.put("promote",false);
                    data.put("is_order",false);
                    data.put("time",null);
                    String fx1=unit_price1.getText().toString().trim();
                    if(fx1.length()>0&&Double.parseDouble(fx1)>0){
                        JSONObject unit_price_1=new JSONObject();
                        unit_price_1.put("fx",Double.parseDouble(fx1));
                        JSONObject unit_id_1=new JSONObject();
                        Unit mUnit1=(Unit)unit1.getSelectedItem();
                        unit_id_1.put("unit_id",mUnit1.getUnit_id());
                        unit_id_1.put("unit_name",mUnit1.getUnit_name());
                        unit_price_1.put("unit_id",unit_id_1);
                        price=price1.getText().toString().trim().length()>0?Double.parseDouble(price1.getText().toString().trim()):0.0;
                        unit_price_1.put("price",price);
                        if(unit_price_id1!=0)unit_price_1.put("unit_price_id",unit_price_id1);
                        data.put("unit_price1",unit_price_1);
                    }
                    String fx2=unit_price2.getText().toString().trim();
                    if(fx2.length()>0&&Double.parseDouble(fx2)>0){
                        JSONObject unit_price_2=new JSONObject();
                        unit_price_2.put("fx",Double.parseDouble(fx2));
                        JSONObject unit_id_2=new JSONObject();
                        Unit mUnit2=(Unit)unit2.getSelectedItem();
                        unit_id_2.put("unit_id",mUnit2.getUnit_id());
                        unit_id_2.put("unit_name",mUnit2.getUnit_name());
                        unit_price_2.put("unit_id",unit_id_2);
                        price=price2.getText().toString().trim().length()>0?Double.parseDouble(price2.getText().toString().trim()):0.0;
                        unit_price_2.put("price",price);
                        if(unit_price_id2!=0)unit_price_2.put("unit_price_id",unit_price_id2);
                        data.put("unit_price2",unit_price_2);
                    }
                    Log.d("goods",data.toString());
                    IhancHttpClient.postJson(getContext(), "/index/setting/goodsUpdate", data, new AsyncHttpResponseHandler() {
                        @Override
                        public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                            String str=new String(responseBody);
                            Log.d("goods",str);
                            try {
                                JSONObject obj = new JSONObject(str);
                                if(obj.getInt("result")==1){
                                    Toast.makeText(getContext(),"产品保存成功！",Toast.LENGTH_LONG).show();
                                    dismiss();
                                    if(refreshGoodsSetting!=null) refreshGoodsSetting.refresh();
                                    if(refreshGoodsSaleMainFragment!=null) refreshGoodsSaleMainFragment.refresh();
                                }
                            }catch (JSONException e){e.printStackTrace();}
                        }

                        @Override
                        public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                            Log.d("goods",new String(responseBody));
                        }
                    });
                }catch (JSONException e){e.printStackTrace();}
            }
        });

        goods_nameET.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if(!TextUtils.isEmpty(goods_nameET.getText())){
                    goods_snET.setText(getPinYinHeadChar(goods_nameET.getText().toString().trim()));
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        return v;
    }

    private void initData(){
        unit0=v.findViewById(R.id.unitSpinner0);
        unit0.setAdapter(mUnitAdapter);
        unit1=v.findViewById(R.id.unitSpinner1);
        unit1.setAdapter(mUnitAdapter);
        unit2=v.findViewById(R.id.unitSpinner2);
        unit2.setAdapter(mUnitAdapter);

        catSpinner=v.findViewById(R.id.cat);
        mCategoryAdapter=new CategoryAdapter(getContext(),R.layout.device_name,mCategoryDataList);
        priceET=v.findViewById(R.id.price);
        catSpinner.setAdapter(mCategoryAdapter);
        unitTv1=v.findViewById(R.id.unitTV1);
        unitTv2=v.findViewById(R.id.unitTV2);
        price1=v.findViewById(R.id.price1);
        price2=v.findViewById(R.id.price2);
        unit0.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String unit=((Unit)adapterView.getAdapter().getItem(i)).getUnit_name();
                unitTv1.setText("1"+unit+" =");
                unitTv2.setText("1"+unit+" =");
            }
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        if(good!=null){
            goods_nameET.setText(good.goods_name);
            goods_snET.setText(good.goods_sn);
            int j = 0;
            for ( int i =0; i <mCategoryDataList.size() ; i++) {
                if(((category)mCategoryDataList.get(i)).getCategory_id()==good.category_id){
                    j=i;
                    break;
                }
            }
            catSpinner.setSelection(j,true);
            priceET.setText(good.goods_price+"");
            j=0;
            String unit="";
            for ( int i =0; i <MainActivity.mUnitList.size() ; i++) {
                if(((Unit)MainActivity.mUnitList.get(i)).getUnit_id()==good.getGoods_unit_id()){
                    j=i;
                    unit=((Unit)MainActivity.mUnitList.get(i)).getUnit_name();
                    break;
                }
            }
            unit0.setSelection(j,true);
            unitTv1.setText("1"+unit+" =");
            unitTv2.setText("1"+unit+" =");

            unitTextView1=v.findViewById(R.id.unitTextView1);
            unitTextView2=v.findViewById(R.id.unitTextView2);
            unit1.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                    String unit=((Unit)adapterView.getAdapter().getItem(i)).getUnit_name();
                    unitTextView1.setText("/"+unit);
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {

                }
            });
            unit2.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                    String unit=((Unit)adapterView.getAdapter().getItem(i)).getUnit_name();
                    unitTextView2.setText("/"+unit);
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {

                }
            });


            RequestParams params=new RequestParams();
            params.put("goods_id",good.goods_id);
            IhancHttpClient.get("/index/setting/getUnitPrice", params, new AsyncHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                    String str=new String(responseBody);
                    if(str.trim().length()>0){
                        try{
                            JSONArray arr=new JSONArray(str);
                            JSONObject item1=arr.getJSONObject(0);
                            unit_price1.setText(item1.getString("fx"));
                            price1.setText(item1.getString("price"));
                            unit_price_id1=item1.getInt("unit_price_id");
                            int j=0;
                            for ( int i =0; i <MainActivity.mUnitList.size() ; i++) {
                                if(((Unit)MainActivity.mUnitList.get(i)).getUnit_id()==item1.getInt("unit_id")){
                                    j=i;
                                    break;
                                }
                            }
                            unit1.setSelection(j,true);
                            if(arr.length()>1){
                                JSONObject item2=arr.getJSONObject(1);
                                unit_price2.setText(item2.getString("fx"));
                                price2.setText(item2.getString("price"));
                                unit_price_id2=item2.getInt("unit_price_id");
                                j=0;
                                for ( int i =0; i <MainActivity.mUnitList.size() ; i++) {
                                    if(((Unit)MainActivity.mUnitList.get(i)).getUnit_id()==item2.getInt("unit_id")){
                                        j=i;
                                        break;
                                    }
                                }
                                unit2.setSelection(j,true);
                            }
                        }catch (JSONException e){e.printStackTrace();}
                    }
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {

                }
            });
        }
    }

    public interface refreshGoodsInterface{
         void refresh();
    }
    public  void setRefreshGoodsSetting(refreshGoodsInterface refreshGoodsInterface){
        this.refreshGoodsSetting=refreshGoodsInterface;
    }

    public static void setRefreshGoodsSaleMainFragment(refreshGoodsInterface refreshGoodsSaleMain){
        refreshGoodsSaleMainFragment=refreshGoodsSaleMain;
    }
}
