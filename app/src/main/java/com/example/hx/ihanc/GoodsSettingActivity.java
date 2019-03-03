package com.example.hx.ihanc;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;

import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import cz.msebera.android.httpclient.Header;

public class GoodsSettingActivity extends AppCompatActivity {
    private Context mContext;
    private ListView categoryLv;
    private CategoryAdapter mCategoryAdapter;
    public static List mCategoryDataList=new ArrayList<category>();
    private GridView mGridView;
    private GoodsAdapter mGoodsAdapter;
    public static ArrayList mGoodsDataList=new ArrayList<Goods>();
    private View lastCategoryView=null;
    private UpDateCategoryDialog dialog=null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_goods_setting);
        ActivityCollector.addActivity(GoodsSettingActivity.this);
        getCategoryData();
        getGoodsData();
        mContext=GoodsSettingActivity.this;
        categoryLv=(ListView)findViewById(R.id.category);
        mCategoryAdapter=new CategoryAdapter(mContext,R.layout.device_name,mCategoryDataList);
        mCategoryAdapter.setListener(new CategoryAdapter.LongClickListener() {
            @Override
            public void OnLongClick(category item) {
                updateCategory(item);
            }
            @Override
            public void OnClick(category item,View view) {
                GoodsAdapter.MyFilter mFilter=mGoodsAdapter.getFilter();
                mFilter.setMyFilter(Utils.GOODSFILTERCATEGORYID);
                mFilter.filter(String.valueOf(item.getCategory_id()));
                view.setBackgroundColor(Color.argb(100,211,211,211));
                if(lastCategoryView!=null)lastCategoryView.setBackgroundColor(Color.WHITE);
                lastCategoryView=view;
            }
        });
        categoryLv.setAdapter(mCategoryAdapter);
        mGridView=(GridView)findViewById(R.id.goods);
        mGoodsAdapter=new GoodsAdapter(mContext,R.layout.goods,mGoodsDataList);
        mGridView.setAdapter(mGoodsAdapter);
        Button mCategoryButton=findViewById(R.id.addCategoryButton);
        mCategoryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateCategory(null);
            }
        });
        Button mGoodButton=findViewById(R.id.addGoodsButton);
        mGoodButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateGoods(null);
            }
        });
        mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                updateGoods((Goods)adapterView.getAdapter().getItem(i));
            }
        });

        SearchView goodsSearchView=findViewById(R.id.goodsSearchView);
        int id = goodsSearchView.getContext().getResources().getIdentifier("android:id/search_src_text",null,null);
        TextView textView = (TextView) goodsSearchView.findViewById(id);
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        goodsSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                if (!TextUtils.isEmpty(s)){
                    GoodsAdapter.MyFilter mFilter=mGoodsAdapter.getFilter();
                    mFilter.setMyFilter(Utils.GOODSFILTERSEARCHVIEW);
                    mFilter.filter(s);
                }else{
                    mGridView.clearTextFilter();
                }
                return false;
            }
        });
    }
    public void getCategoryData(){
        // Log.d(TAG,"getCategoryData");
        mCategoryDataList.clear();
        RequestParams params = new RequestParams();
        IhancHttpClient.get("/cat", params, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                String res = new String(responseBody);
                try {
                    JSONArray resArray = new JSONArray(res);
                    for(int i=0;i<resArray.length();i++){
                        category mItem=new category();
                        JSONObject myjObject = resArray.getJSONObject(i);
                        mItem.setCategory_id(myjObject.getInt("cat_id"));
                        mItem.setCategory_name(myjObject.getString("cat_name"));
                        mCategoryDataList.add(mItem);
                        //Log.d("goodsSetting",myjObject.getString("cat_name"));
                    }
                    mCategoryAdapter.notifyDataSetChanged();
                } catch (JSONException e) { Log.d("JSONArray",e.toString());} }
            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                String res = new String(responseBody);
                // Log.d("getCategoryData Failed",res.toString());
                for(int i=0;i<headers.length;i++){
                    Log.d("getCategoryData Failed",headers[i].toString());
                }
            }
        });
    }
    public void getGoodsData(){
        mGoodsDataList.clear();
        RequestParams params = new RequestParams();
        IhancHttpClient.get("/index/setting/goods", params, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                String res = new String(responseBody);
                try {
                    JSONArray resArray = new JSONArray(res);
                    for(int i=0;i<resArray.length();i++){
                        JSONObject myjObject = resArray.getJSONObject(i);
                        Goods mItem=new Goods(myjObject.getInt("goods_id"),
                                myjObject.getString("goods_name"),
                                myjObject.getString("unit_id"),
                                myjObject.optDouble("out_price"),
                                myjObject.getString("goods_sn"),
                                myjObject.getInt("cat_id"),
                                myjObject.getString("unit_id"),
                                myjObject.getInt("promote")
                        );
                        mGoodsDataList.add(mItem);
                    }
                    mGoodsAdapter.notifyDataSetChanged();
                } catch (JSONException e)
                {
                    Log.d("JSONArray",e.toString());}
            }
            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                String res = new String(responseBody);
                // Log.d("getCategoryData Failed",res.toString());
                for(int i=0;i<headers.length;i++){
                    Log.d("getGoodsData Failed",headers[i].toString());
                }
            }
        });
    }
    private void updateCategory(category item) {
        if(item==null)
           dialog=UpDateCategoryDialog.newInstance(0,"");
        else
            dialog=UpDateCategoryDialog.newInstance(item.getCategory_id(),item.getCategory_name());
        dialog.setOnUpdateCategorySuccessListener(new UpDateCategoryDialog.OnUpdateCategorySuccess() {
            @Override
            public void UpdateCategorySuccess() {
                dialog.dismiss();
                getCategoryData();
            }
        });
        dialog.show(getSupportFragmentManager(),"UpdateCategoryDialog");
    }
    private void updateGoods(Goods goods){
        UpdateGoodsDialog dialog;
        if(goods!=null)
            dialog=UpdateGoodsDialog.newInstance(goods.toString());
        else
            dialog=UpdateGoodsDialog.newInstance("");
        dialog.setRefreshGoodsSetting(new UpdateGoodsDialog.refreshGoodsInterface() {
            @Override
            public void refresh() {
                getGoodsData();
            }
        });
        dialog.show(getSupportFragmentManager(),"updateGoodDialog");
    }

}
