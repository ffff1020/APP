package com.example.hx.ihanc;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.DialogFragment;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Locale;

import cz.msebera.android.httpclient.Header;

public class SaleDetailDialog extends DialogFragment {
    private int sale_id;
    private String member_name;
    private int credit;
    private boolean paid;
    private int member_id;
    private ProgressBar mProgressView;
    private TextView memberNameTv;
    private ListView listView;
    private ArrayList<SaleDetail> mSaleDetails=new ArrayList<SaleDetail>(){};
    private SaleDetailAdapter mSaleDetailAdapter;
    private printListener mListener;
    private SaleDetailAdapter.MyClickListener mModifyListener=null;
    private View view;
    private NumberFormat format=NumberFormat.getCurrencyInstance(Locale.CHINA);
    private SaleDetailModifyDialog.SaveListener saveListener;

    public static SaleDetailDialog newInstance(int sale_id,
                            String member_name,int credit,
                            int member_id,
                            boolean paid){
        SaleDetailDialog df=new SaleDetailDialog();
        Bundle args = new Bundle();
        args.putInt("sale_id", sale_id);
        args.putString("member_name",member_name);
        args.putInt("member_id", member_id);
        args.putBoolean("paid",paid);
        args.putInt("credit", credit);
        df.setArguments(args);
        return df;

    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.sale_id=getArguments().getInt("sale_id");
        this.member_name=getArguments().getString("member_name");
        this.paid=getArguments().getBoolean("paid");
       // this.mListener=mListener;
        this.credit=getArguments().getInt("credit");;
        this.member_id=getArguments().getInt("member_id");;
        saveListener=new SaleDetailModifyDialog.SaveListener() {
            @Override
            public void saved() {
                getData();
                mListener.fresh();
            }
        };
        mModifyListener=new SaleDetailAdapter.MyClickListener() {
            @Override
            public void myOnClick(int position, View v) {
            }
            @Override
            public void myModifyClick(int position, View v) {
               // Log.d("modify","");
                SaleDetailModifyDialog dialog=SaleDetailModifyDialog.newInstance(member_name,mSaleDetails.get(position));
                dialog.SaveListener(saveListener);
                dialog.show(getFragmentManager(),"sale_detail_modify");
            }
        };
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.sale_detail_dialog, container);
        initView();
        getData();
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        Window win = getDialog().getWindow();
        // 一定要设置Background，如果不设置，window属性设置无效
        win.setBackgroundDrawable( new ColorDrawable(Color.WHITE));

        DisplayMetrics dm = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics( dm );

        WindowManager.LayoutParams params = win.getAttributes();
        params.gravity = Gravity.BOTTOM;
        // 使用ViewGroup.LayoutParams，以便Dialog 宽度充满整个屏幕
        params.width =  ViewGroup.LayoutParams.MATCH_PARENT;
        params.height = ViewGroup.LayoutParams.WRAP_CONTENT;

        win.setAttributes(params);
    }

    public void initView(){
        memberNameTv=view.findViewById(R.id.member_name);
       // NumberFormat format=NumberFormat.getCurrencyInstance(Locale.CHINA);
        memberNameTv.setText(member_name+"--"+format.format(credit));
        mProgressView=view.findViewById(R.id.loading_progress);
        listView=view.findViewById(R.id.detail);
        mSaleDetailAdapter=new SaleDetailAdapter(getContext(),R.layout.sale_detail_item_1,mSaleDetails,mModifyListener);
        listView.setAdapter(mSaleDetailAdapter);
        final Button mPrintButton=(Button)view.findViewById(R.id.printButton);
        mPrintButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mSaleDetails.size()==0) return;
                mPrintButton.setClickable(false);
                if(Utils.mCompanyInfo==null) Utils.getCompanyInfo();
                Utils.printMemberName=new member(member_id,member_name,"");
                RequestParams params = new RequestParams();
                params.put("member_id", member_id);
                params.put("sale_id", sale_id);
                IhancHttpClient.get("/index/sale/getMemberCredit", params, new AsyncHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                        String res = new String(responseBody);
                        int payment=0;
                        int credit_sum=0;
                        try {
                            JSONObject object = new JSONObject(res);
                            credit_sum=object.getInt("credit");
                           // mListener.print(mSaleDetails,object.getInt("payment"),credit_sum-credit);
                           // mPrintButton.setClickable(true);
                        } catch (JSONException e) { e.printStackTrace(); }
                        try {
                            JSONObject object = new JSONObject(res);
                            payment=object.getInt("payment");
                           // mListener.print(mSaleDetails,object.getInt("payment"),credit_sum-credit);
                            //mPrintButton.setClickable(true);
                        } catch (JSONException e) { e.printStackTrace(); }
                        mListener.print(mSaleDetails,payment,credit_sum-credit);
                        mPrintButton.setClickable(true);
                    }
                    @Override
                    public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                    }
                });
            }
        });

        showProgress(true);
    }

    public void getData(){
        mSaleDetails.clear();
        RequestParams params=new RequestParams();
        params.put("sale_id",sale_id);
        IhancHttpClient.get("/index/sale/saleDetail", params, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                String res=new String(responseBody);
                try {
                    JSONObject resJSON=new JSONObject(res);
                    JSONArray list=resJSON.getJSONArray("data");
                    credit=0;
                    for (int i = 0; i <list.length() ; i++) {
                         JSONObject item=list.getJSONObject(i);
                         DetailGoods good=new DetailGoods(item.getInt("goods_id"),item.getString("goods_name"),item.getInt("unit_id_0"));
                         SaleDetail detail=new SaleDetail(good,item.getDouble("number"),item.getDouble("price"),item.getInt("unit_id"),item.getString("unit_name"),item.getInt("sum"));
                         detail.paid=paid;
                         detail.sale_detail_id=item.getInt("sale_detail_id");
                         detail.sale_id=item.getInt("sale_id");
                         mSaleDetails.add(detail);
                         credit+=item.getInt("sum");
                    }
                    memberNameTv.setText(member_name+"--"+format.format(credit));
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
    public  interface  printListener{
        void  print(ArrayList<SaleDetail> mdetails,int payment,int credit);
        void  fresh();
    }
    public void setListener(printListener mListener){
        this.mListener=mListener;
    }



}
