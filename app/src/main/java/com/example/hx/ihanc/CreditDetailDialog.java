package com.example.hx.ihanc;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Array;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Locale;

import cz.msebera.android.httpclient.Header;

public class CreditDetailDialog extends DialogFragment {
    private int member_id;
    private String title;
    private TextView titleTV;
    private List<CreditDetail> data=new ArrayList<CreditDetail>();
    private CreditDetailAdpter.MyOnCheckChangeListener myOnCheckChangeListener;
    private ListView credit_detailLT;
    private CreditDetailAdpter adapter;
    private Button wxButton;
    private Button printButton;
    private Button paymentButton;
    private int selectedTotal=0;
    private int selectedNum=0;
    private String selected="";
    private int creditSum=0;
    private OnFreshCredit onFreshCredit;

    public static CreditDetailDialog newInstance(int member_id,String title) {
        CreditDetailDialog f = new CreditDetailDialog();
        Bundle args = new Bundle();
        args.putInt("member_id", member_id);
        args.putString("title",title);
        f.setArguments(args);
        return f;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.member_id=getArguments().getInt("member_id");
        this.title=getArguments().getString("title");
        myOnCheckChangeListener=new CreditDetailAdpter.MyOnCheckChangeListener() {
            @Override
            public void myOnCheckChange(int position, CompoundButton buttonView, boolean isChecked) {
                //Log.d("creditDialog",position+":"+data.get(position).getSum()+"");
                if(isChecked) {
                    CreditDetail item=data.get(position);
                    selectedTotal += item.getSum();
                    item.selected=true;
                    selectedNum++;
                    if(selectedNum==0)
                        selected+=item.getSale_id();
                    else
                        selected+=","+item.getSale_id();
                }
                else {
                    selectedTotal -= data.get(position).getSum();
                    selectedNum--;
                }

            }
        };
        initData();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_credit_detail, container);
        titleTV=view.findViewById(R.id.title_credit_detail);
        titleTV.setText(title);
        credit_detailLT=view.findViewById(R.id.credit_detail);
        adapter=new CreditDetailAdpter(getContext(),R.layout.list_credit_detail,data,myOnCheckChangeListener);
        credit_detailLT.setAdapter(adapter);
        wxButton=view.findViewById(R.id.wxButton);
        wxButton.setOnClickListener(getWxButtonListener());
        printButton=view.findViewById(R.id.printButton);
        printButton.setOnClickListener(getPrintButtonListener());
        paymentButton=view.findViewById(R.id.payButton);
        paymentButton.setOnClickListener(getPaymentButtonListener());
        return view;
    }

    public View.OnClickListener getPrintButtonListener(){
        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(selectedNum>0) {
                    Utils.printSaleId=selected;
                }
                if (Utils.mCompanyInfo == null) Utils.getCompanyInfo();
                String[] titles = title.split("--");
                Utils.printMemberName = new member(member_id, titles[0], "");
                MainActivity parentActivity = (MainActivity) getActivity();
                parentActivity.printDetails();
                SharedPreferences sp=PreferenceManager.getDefaultSharedPreferences(getActivity());
                final String receiptType=sp.getString(getString(R.string.receipt_type),getString(R.string.receipt_type_default));
                if(!receiptType.equals(getString(R.string.receipt_type_default))) {
                    dismiss();
                }
            }
        };
    };

    public View.OnClickListener getWxButtonListener(){
        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String[] titles=title.split("--");
                String n="\n";
                String line="--------------------------------------------\n";
                Date now=new Date();
                SimpleDateFormat f=new SimpleDateFormat("yyyy-MM-dd HH:mm");
                String tab="       ";
                int sum=0;
                if(Utils.mCompanyInfo==null) Utils.getCompanyInfo();
                String msg=tab+Utils.mCompanyInfo.getName()+n
                        +tab+tab+"对账单\n"
                        +"客户："+titles[0]+n
                        +"打印时间："+f.format(now)+n
                        +line;
                if(selectedNum>0){
                    boolean check=false;
                    boolean first=true;
                    for (int i = 0; i < data.size(); i++) {
                        CreditDetail item = data.get(i);
                        if(!item.isGroup()){
                            if(item.selected){
                                check=true;
                                if (first){ msg += n;first=false;}
                                msg += item.getTime() + n;
                                sum += item.getSum();
                                msg += item.getGoods_name() + n;
                                String temp = item.getSummary();
                                for (int j = 0; j < 36 - temp.getBytes().length; j++) msg += " ";
                                msg += item.getSummary() + n;
                            }else check=false;
                        }else if(check){
                            msg += item.getGoods_name() + n;
                            String temp = item.getSummary();
                            for (int j = 0; j < 36 - temp.getBytes().length; j++) msg += " ";
                            msg += item.getSummary() + n;
                        }
                    }
                }else{
                    for (int i = 0; i < data.size(); i++) {
                        CreditDetail item = data.get(i);
                        if (!item.isGroup()) {
                            if (i > 0) msg += n;
                            msg += item.getTime() + n;
                            sum += item.getSum();
                        }
                        msg += item.getGoods_name() + n;
                        String temp = item.getSummary();
                        for (int j = 0; j < 36 - temp.getBytes().length; j++) msg += " ";
                        msg += item.getSummary() + n;
                    }
                }
                NumberFormat nf=NumberFormat.getCurrencyInstance(Locale.CHINA);
                msg+=line+"合计金额："+nf.format(sum)+n
                    +"现累计欠款："+titles[1]+n;
                msg+=line;
                msg+="    "+"谢谢惠顾，欢迎再次光临！"+n;
                msg+="联系电话："+Utils.mCompanyInfo.getTel()+n;
                if (Utils.mCompanyInfo.getAddress().length>1){
                    for (int k=0;k<Utils.mCompanyInfo.getAddress().length;k++)
                        msg+="地址"+(k+1)+"："+Utils.mCompanyInfo.getAddress()[k]+n;
                }else
                    msg+="地址："+Utils.mCompanyInfo.getAddress()[0]+n;
                shareMsg(msg);
            }
        };
    }

    public View.OnClickListener getPaymentButtonListener(){
      return new View.OnClickListener() {
          @Override
          public void onClick(View view) {
              PaymentDialog dialog=PaymentDialog.newInstance(member_id,title,creditSum,selectedTotal,selectedNum,selected);
              PaymentDialog.OnPaymentSucceed onPaymentSucceed=new PaymentDialog.OnPaymentSucceed() {
                  @Override
                  public void PaymentSucceed() {
                      dismiss();
                      if(onFreshCredit!=null)onFreshCredit.freshCredit();
                  }
              };
              dialog.setOnPaymentSucceed(onPaymentSucceed);
              dialog.show(getFragmentManager(),"payment Dialog");
          }
      }  ;
    };

    public void setOnFreshCredit(OnFreshCredit onFreshCredit){
        this.onFreshCredit=onFreshCredit;
       // Log.d("onCreditDialog","setOnFreshCredit");
    }

    public void initData(){
        RequestParams params=new RequestParams();
        params.put("member_id",member_id);
        IhancHttpClient.get("/index/sale/creditDetail", params, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                String res=new String(responseBody);
                try {
                    JSONObject resJson=new JSONObject(res);
                    JSONArray list=resJson.getJSONArray("credit");
                    creditSum=resJson.getInt("credit_sum");
                    int sale_id=0;
                    for (int i = 0; i <list.length() ; i++) {
                        JSONObject itemJSON=list.getJSONObject(i);
                        String type=itemJSON.getString("type");
                        String goods_name="";
                        String time=itemJSON.getString("time");
                        int sum=0;
                        String summary="";
                        boolean group=false;
                        if(i>0){
                            group=sale_id==itemJSON.getInt("sale_id");
                        }
                        switch (type) {
                            case "Init":
                                goods_name="初期录入应收款";
                                summary="￥"+itemJSON.getString("ttl");
                                sum=itemJSON.getInt("ttl");
                                break;
                            case "P":
                                goods_name="客户付款";
                                summary="￥"+itemJSON.getString("ttl");
                                sum=itemJSON.getInt("ttl");
                                break;
                            case "income":
                                String income=itemJSON.getString("summary");
                                String[] info=income.split("-");
                                goods_name=info[info.length-1];
                                summary="￥"+itemJSON.getString("ttl");
                                sum=itemJSON.getInt("ttl");
                                break;
                            case "cost":
                                income=itemJSON.getString("summary");
                                info=income.split("-");
                                goods_name=info[info.length-1];
                                summary="￥"+itemJSON.getString("ttl");
                                sum=itemJSON.getInt("ttl");
                                break;
                            case "CF":
                                goods_name="结转余额";
                                summary="￥"+itemJSON.getString("ttl");
                                sum=itemJSON.getInt("ttl");
                                break;
                                default:
                                    goods_name=itemJSON.getString("goods_name");
                                    String number=itemJSON.getString("number");
                                    String price=itemJSON.getString("price");
                                    summary=number.substring(0,number.length()-1)+itemJSON.getString("unit_name") + " * ￥" + price.substring(0,price.length()-1) + "= ￥" + itemJSON.getString("sum");
                                    sum=itemJSON.getInt("ttl");
                                    break;
                        }
                        CreditDetail item = new CreditDetail(time,goods_name,summary,sum,group,itemJSON.getInt("sale_id"));
                        data.add(item);
                        sale_id=itemJSON.getInt("sale_id");
                    }
                    adapter.notifyDataSetChanged();
                }catch (JSONException e){e.printStackTrace();}
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {

            }
        });
    }

    @SuppressLint("NewApi")
    private void shareMsg(String msgText) {
        String packageName="com.tencent.mm";
        String activityName="com.tencent.mm.ui.tools.ShareImgUI";
        String appname="微信";
        Context context=getContext();
        if (!packageName.isEmpty() && !isAvilible(context, packageName)) {// 判断APP是否存在
            Toast.makeText(context, "请先安装" + appname, Toast.LENGTH_SHORT).show();
            return;
        }
        Intent intent = new Intent("android.intent.action.SEND");
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT, msgText);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if (!packageName.isEmpty()) {
            intent.setComponent(new ComponentName(packageName, activityName));
            context.startActivity(intent);
        }
    }

    public boolean isAvilible(Context context, String packageName) {
        PackageManager packageManager = context.getPackageManager();
        List<PackageInfo> pinfo = packageManager.getInstalledPackages(0);
        for (int i = 0; i < pinfo.size(); i++) {
            if (((PackageInfo) pinfo.get(i)).packageName
                    .equalsIgnoreCase(packageName))
                return true;
        }
        return false;
    }

    public interface OnFreshCredit{
        void freshCredit();
    }
}
