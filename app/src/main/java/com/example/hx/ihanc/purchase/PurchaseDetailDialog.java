package com.example.hx.ihanc.purchase;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
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
import android.widget.TextView;

import com.example.hx.ihanc.Goods;
import com.example.hx.ihanc.IhancHttpClient;
import com.example.hx.ihanc.MainActivity;
import com.example.hx.ihanc.R;
import com.example.hx.ihanc.Utils;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Locale;

import cz.msebera.android.httpclient.Header;


public class PurchaseDetailDialog extends DialogFragment {
    private int purchase_id;
    private boolean paid;
    private String supply_name;
    private int supply_id;
    private int sum;
    private View view;
    private TextView NameTv;
    private NumberFormat f=NumberFormat.getCurrencyInstance(Locale.CHINA);
    private PurchaseDetailAdapter adapter;
    public static final ArrayList<PurchaseDetail> purchasedDetails=new ArrayList<PurchaseDetail>();
    private OnPurchaseDetailUpdated listener=null;

     public static PurchaseDetailDialog newInstance(int purchase_id, String supply_name,int supply_id,String sum, boolean paid){
         PurchaseDetailDialog dialog=new PurchaseDetailDialog();
         Bundle b=new Bundle();
         b.putInt("purchase_id",purchase_id);
         b.putInt("supply_id",supply_id);
         b.putString("supply_name",supply_name);
         b.putBoolean("paid",paid);
         b.putString("sum",sum);
         dialog.setArguments(b);
         return dialog;
     }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.purchase_id=getArguments().getInt("purchase_id");
        this.supply_name=getArguments().getString("supply_name");
        this.paid=getArguments().getBoolean("paid");
        this.sum=Integer.parseInt(getArguments().getString("sum"));
        this.supply_id=getArguments().getInt("supply_id");
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
         view=inflater.inflate(R.layout.purchase_list_detail_dialog,container);
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

    private void initView(){
        NameTv=view.findViewById(R.id.supply_name);
        NameTv.setText(supply_name+"--"+f.format(sum));
        adapter=new PurchaseDetailAdapter(purchasedDetails);
        adapter.showAdd=false;
        RecyclerView recyclerView=view.findViewById(R.id.detail);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);
        adapter.setPurchaseDetailListener(new PurchaseDetailAdapter.PurchaseDetailListener() {
            @Override
            public void purchaseAdd(int position) {
                DialogPurchaseDetail dialog=DialogPurchaseDetail.NewInstance(position,supply_id);
                dialog.setPurchased(purchase_id);
                dialog.setOnUpdatePurchaseDetail(setOnUpdatePurchaseDetail());
                dialog.show(getFragmentManager(),"DialogPurchaseDetail");
            }
        });
        Button paymentBtn=view.findViewById(R.id.paymentButton);
        if(paid){
            paymentBtn.setVisibility(View.GONE);
        }else{
            paymentBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    PurchasePaymentDialog dialog=PurchasePaymentDialog.newInstance(supply_id,supply_name+"--"+f.format(sum),0,sum,1,purchase_id+"");
                    PurchasePaymentDialog.OnPaymentSucceed onPaymentSucceed=new PurchasePaymentDialog.OnPaymentSucceed() {
                        @Override
                        public void PaymentSucceed() {
                            dismiss();
                            listener.OnPurchaseDetailUpdated();
                        }
                    };
                    dialog.setOnPaymentSucceed(onPaymentSucceed);
                    dialog.show(getFragmentManager(),"payment Dialog");
                }
            });
        }
    }
    private void getData(){
        purchasedDetails.clear();
        Utils.showProgress(getFragmentManager(),true);
        RequestParams params=new RequestParams();
        params.put("purchase_id",purchase_id);
        IhancHttpClient.get("/index/purchase/purchaseDetail", params, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                Utils.showProgress(getFragmentManager(),false);
                String str=new String(responseBody);
                Log.d("purchaseDetail",str);
                try {
                    JSONArray res=new JSONArray(str);
                    for (int i = 0; i <res.length() ; i++) {
                        JSONObject obj=res.getJSONObject(i);
                        Goods goods=new Goods(obj.getInt("goods_id"),
                                obj.getString("goods_name"),
                                obj.getString("unit_id_0"),0.0,"",0,"",0);
                        PurchaseDetail item=new PurchaseDetail(purchase_id, goods,
                                            obj.getInt("unit_id"),
                                            obj.getString("unit_name"),obj.getDouble("price"),
                                            obj.getDouble("number"),obj.getInt("sum"),
                                            obj.getInt("purchase_detail_id"),obj.getString("store_name"),0);
                        item.paid=paid;
                        purchasedDetails.add(item);
                    }
                    adapter.notifyDataSetChanged();
                }catch (JSONException e){e.printStackTrace();}
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                Log.d("purchaseDetail","");
            }
        });
    }

    private DialogPurchaseDetail.onUpdatePurchaseDetail setOnUpdatePurchaseDetail(){
         return new DialogPurchaseDetail.onUpdatePurchaseDetail() {
             @Override
             public void updatePurchaseDetail(int position, PurchaseDetail detail) {
                 Utils.hideKeyboard(view,(MainActivity)getActivity());
                 if(detail==null){
                     PurchaseDetail old=purchasedDetails.get(position);
                     purchasedDetails.remove(position);
                     adapter.notifyDataSetChanged();
                     try{
                         JSONObject params=new JSONObject();
                         params.put("purchase_detail_id",old.purchase_detail_id);
                         params.put("purchase_id",old.purchase_id);
                         params.put("goods_unit_id",old.goods.getGoods_unit_id());
                         IhancHttpClient.postJson(getContext(), "/index/purchase/purchaseDetailDelete", params, new AsyncHttpResponseHandler() {
                             @Override
                             public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                                 try{
                                     JSONObject rs=new JSONObject(new String(responseBody));
                                     if (rs.getInt("result")==1){
                                         Utils.toast(getContext(),"保存成功！");
                                     }

                                 }catch (JSONException e){e.printStackTrace();}
                                 listener.OnPurchaseDetailUpdated();
                             }

                             @Override
                             public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                                 Utils.toast(getContext(),"不能连接到服务器！");
                             }
                         });

                     }catch (JSONException e){e.printStackTrace();}

                 }else{
                     PurchaseDetail old=purchasedDetails.get(position);
                     adapter.notifyDataSetChanged();
                     purchasedDetails.set(position,detail);
                     int sum=0;
                     for (int i = 0; i <purchasedDetails.size() ; i++) {
                         sum+=purchasedDetails.get(i).sum;
                     }
                     NameTv.setText(supply_name+"--"+f.format(sum));
                     JSONObject params=new JSONObject();
                     try {
                         params.put("purchase_detail_id",detail.purchase_detail_id);
                         params.put("number",detail.number);
                         params.put("price",detail.price);
                         params.put("sum",detail.sum);
                         params.put("purchase_id",detail.purchase_id);
                         params.put("goods_unit_id",detail.goods.getGoods_unit_id());
                         if (old.unit_id!=detail.unit_id) {
                             params.put("unit_id", detail.unit_id);
                         }
                     }catch (JSONException e){e.printStackTrace();}
                     IhancHttpClient.postJson(getContext(), "/index/purchase/purchaseDetailUpdate", params, new AsyncHttpResponseHandler() {
                         @Override
                         public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                             try{
                                 JSONObject rs=new JSONObject(new String(responseBody));
                                 if (rs.getInt("result")==1){
                                     Utils.toast(getContext(),"保存成功！");
                                 }
                             }catch (JSONException e){e.printStackTrace();}
                             listener.OnPurchaseDetailUpdated();
                         }

                         @Override
                         public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                               Utils.toast(getContext(),"不能连接到服务器！");
                         }
                     });
                 }
             }
         };
    }

    public  interface OnPurchaseDetailUpdated{
         void OnPurchaseDetailUpdated();
    }

    public void setListener(OnPurchaseDetailUpdated listener) {
        this.listener = listener;
    }
}
