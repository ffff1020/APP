package com.example.hx.ihanc;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Dialog;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import cz.msebera.android.httpclient.Header;

public class SaleDetailDialog extends Dialog {
    private int sale_id;
    private String member_name;
    private ProgressBar mProgressView;
    private TextView memberNameTv;
    private ListView listView;
    private ArrayList<SaleDetail> mSaleDetails=new ArrayList<SaleDetail>(){};
    private SaleDetailAdapter mSaleDetailAdapter;
    public SaleDetailDialog(Context context,int sale_id,String member_name){
        super(context);
        this.sale_id=sale_id;
        this.member_name=member_name;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sale_detail_dialog);
        initView();
        getData();
    }

    public void initView(){
        memberNameTv=findViewById(R.id.member_name);
        memberNameTv.setText(member_name);
        mProgressView=findViewById(R.id.loading_progress);
        listView=findViewById(R.id.detail);
        mSaleDetailAdapter=new SaleDetailAdapter(getContext(),R.layout.sale_detail_item_1,mSaleDetails,null);
        listView.setAdapter(mSaleDetailAdapter);
        showProgress(true);

    }

    public void getData(){
        RequestParams params=new RequestParams();
        params.put("sale_id",sale_id);
        IhancHttpClient.get("/index/sale/saleDetail", params, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                String res=new String(responseBody);
                try {
                    JSONObject resJSON=new JSONObject(res);
                    JSONArray list=resJSON.getJSONArray("data");
                    for (int i = 0; i <list.length() ; i++) {
                         JSONObject item=list.getJSONObject(i);
                         DetailGoods good=new DetailGoods(0,item.getString("goods_name"),0);
                         SaleDetail detail=new SaleDetail(good,item.getDouble("number"),item.getDouble("price"),0,item.getString("unit_name"),item.getInt("sum"));
                         mSaleDetails.add(detail);
                    }
                    mSaleDetailAdapter.notifyDataSetChanged();
                    showProgress(false);
                }catch (JSONException e){e.printStackTrace();}

            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {

            }
        });
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getContext().getResources().getInteger(android.R.integer.config_shortAnimTime);
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
        }
    }
}
