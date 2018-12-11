package com.example.hx.ihanc;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.DialogFragment;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.FileProvider;
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
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import cz.msebera.android.httpclient.Header;
import jxl.Workbook;
import jxl.format.Border;
import jxl.format.Colour;
import jxl.write.Label;
import jxl.write.WritableCellFormat;
import jxl.write.WritableFont;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;

public class SaleDetailDialog extends DialogFragment {
    private int sale_id;
    private String member_name;
    private int credit;
    private String time;
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
    private Button paymentButton;
    private onFreshList onFreshList;

    public static SaleDetailDialog newInstance(int sale_id,
                            String member_name,int credit,
                            int member_id,
                            boolean paid,String time){
        SaleDetailDialog df=new SaleDetailDialog();
        Bundle args = new Bundle();
        args.putInt("sale_id", sale_id);
        args.putString("member_name",member_name);
        args.putInt("member_id", member_id);
        args.putBoolean("paid",paid);
        args.putInt("credit", credit);
        args.putString("time",time);
        df.setArguments(args);
        return df;

    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.sale_id=getArguments().getInt("sale_id");
        this.member_name=getArguments().getString("member_name");
        this.paid=getArguments().getBoolean("paid");
        this.time=getArguments().getString("time");
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
                        mListener.print(mSaleDetails,payment,credit_sum-credit,time);
                        mPrintButton.setClickable(true);
                    }
                    @Override
                    public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                    }
                });
            }
        });
        final Button sendWxButton=view.findViewById(R.id.wxButton);
        sendWxButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mSaleDetails.size()==0) return;
                sendWxButton.setClickable(false);
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
                        } catch (JSONException e) { e.printStackTrace(); }
                        try {
                            JSONObject object = new JSONObject(res);
                            payment=object.getInt("payment");
                        } catch (JSONException e) { e.printStackTrace(); }
                        sendWx(payment,credit_sum);
                        sendWxButton.setClickable(true);
                    }
                    @Override
                    public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                    }
                });

            }
        });
        paymentButton=view.findViewById(R.id.paymentButton);
        paymentButton.setOnClickListener(getPaymentButtonListener());
        if(paid) paymentButton.setVisibility(View.GONE);
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
        void  print(ArrayList<SaleDetail> mdetails,int payment,int credit,String time);
        void  fresh();
    }
    public void setListener(printListener mListener){
        this.mListener=mListener;
    }

    public void sendWx(int payment,int credit){
        if(Utils.mCompanyInfo==null) Utils.getCompanyInfo();
        String fileName="ihanc--"+Utils.mCompanyInfo.getName()+".xls";
        File file = new File(Environment.getExternalStorageDirectory(), fileName);
        WritableWorkbook wwb;
        int size=mSaleDetails.size();
        try {
            OutputStream os = new FileOutputStream(file);
            try {
                wwb = Workbook.createWorkbook(os);
                WritableSheet sheet = wwb.createSheet(Utils.mCompanyInfo.getName()+"销售单", 0);
                Label label;
                try {
                    label = new Label(0, 0, Utils.mCompanyInfo.getName(), getHeader());
                    sheet.addCell(label);
                    sheet.setRowView(0,500);
                    label = new Label(0, 1, "销售单", getBody());
                    sheet.addCell(label);
                    label = new Label(0, 2, "客户："+member_name);
                    sheet.addCell(label);
                    label = new Label(0, 3, "销售时间："+time);
                    sheet.addCell(label);
                    sheet.mergeCells(0, 0, 3, 0);
                    sheet.mergeCells(0, 1, 3, 0);
                    sheet.mergeCells(0, 2, 3, 0);
                    sheet.mergeCells(0, 3, 3, 0);
                    NumberFormat f=NumberFormat.getCurrencyInstance(Locale.CHINA);
                    WritableCellFormat body=getBody();
                    label = new Label(0, 4, "");
                    sheet.addCell(label);
                    sheet.mergeCells(0, 4, 3, 0);
                    label = new Label(0, 5, "商品名称",body);
                    sheet.addCell(label);
                    label = new Label(1, 5, "数量",body);
                    sheet.addCell(label);
                    label = new Label(2, 5, "价格",body);
                    sheet.addCell(label);
                    label = new Label(3, 5, "金额",body);
                    sheet.addCell(label);
                    int sum=0;
                    int maxLen=12;
                    for (int i = 0; i < size ; i++) {
                        SaleDetail item=mSaleDetails.get(i);
                        label = new Label(0, 6+i, item.getGoods_name(),body);
                        sheet.addCell(label);
                        label = new Label(1, 6+i, item.getNumber(),body);
                        sheet.addCell(label);
                        label = new Label(2, 6+i, item.getPrice(),body);
                        sheet.addCell(label);
                        label = new Label(3, 6+i, f.format(item.getSum()),body);
                        sheet.addCell(label);
                        if(maxLen<item.getGoods_name().length()) maxLen=item.getGoods_name().getBytes().length;
                        sum+=item.getSum();
                    }
                    sheet.setColumnView(0,maxLen);
                    size+=6;
                    label = new Label(0, size, "");
                    sheet.addCell(label);
                    sheet.mergeCells(0, size, 3, 0);
                    size++;
                    label = new Label(0, size, "合计金额:"+f.format(sum));
                    sheet.addCell(label);
                    sheet.mergeCells(0, size, 3, 0);
                    size++;
                    if(payment!=0){
                            label = new Label(0, size, "实收金额:"+f.format(sum));
                            sheet.addCell(label);
                            sheet.mergeCells(0, size, 3, 0);
                            size++;
                    }
                    if(credit!=0){
                        Date now=new Date();
                        SimpleDateFormat ft = new SimpleDateFormat ("yyyy-MM-dd  HH:mm");
                        label = new Label(0, size, ft.format(now)+"累计应收金额:"+f.format(credit));
                        sheet.addCell(label);
                        sheet.mergeCells(0, size, 3, 0);
                        size++;
                    }
                    label = new Label(0, size, "感谢您的惠顾，欢迎下次光临！",getFoot());
                    sheet.addCell(label);
                    sheet.mergeCells(0, size, 3, 0);
                    size++;
                    label = new Label(0, size, "联系电话："+Utils.mCompanyInfo.getTel());
                    sheet.addCell(label);
                    sheet.mergeCells(0, size, 3, 0);
                    size++;
                    if(Utils.mCompanyInfo.getAddress().length==1){
                        label = new Label(0, size, "地址："+Utils.mCompanyInfo.getAddress()[0]);
                        sheet.addCell(label);
                        sheet.mergeCells(0, size, 3, 0);
                        size++;
                    }else {
                        for (int i = 0; i < Utils.mCompanyInfo.getAddress().length; i++) {
                            label = new Label(0, size, "地址"+(i+1)+"：" + Utils.mCompanyInfo.getAddress()[i]);
                            sheet.addCell(label);
                            sheet.mergeCells(0, size, 3, 0);
                            size++;
                        }
                    }

                    wwb.write();
                    wwb.close();
                    shareMsg(getContext(),file);

                }catch (WriteException e){e.printStackTrace();}
            }catch (IOException e){e.printStackTrace();}
        }catch (FileNotFoundException e){e.printStackTrace();}


    }
    public static WritableCellFormat getHeader() {
        WritableFont font = new WritableFont(WritableFont.TIMES, 15,
                WritableFont.BOLD);// 定义字体
        try {
            font.setColour(Colour.BLUE);// 蓝色字体
        } catch (WriteException e1) {
            e1.printStackTrace();
        }
        WritableCellFormat format = new WritableCellFormat(font);
        try {
            format.setAlignment(jxl.format.Alignment.CENTRE);// 左右居中
            format.setVerticalAlignment(jxl.format.VerticalAlignment.CENTRE);// 上下居中
            format.setBorder(jxl.format.Border.ALL,jxl.format.BorderLineStyle.THIN,Colour.WHITE);
        } catch (WriteException e) {
            e.printStackTrace();
        }
        return format;
    }

    public static WritableCellFormat getFoot() {
        WritableFont font = new WritableFont(WritableFont.TIMES);// 定义字体
        try {
            font.setColour(Colour.BLUE);// 蓝色字体
        } catch (WriteException e1) {
            e1.printStackTrace();
        }
        WritableCellFormat format = new WritableCellFormat(font);
        try {
            format.setAlignment(jxl.format.Alignment.CENTRE);// 左右居中
            format.setVerticalAlignment(jxl.format.VerticalAlignment.CENTRE);// 上下居中
        } catch (WriteException e) {
            e.printStackTrace();
        }
        return format;
    }

    public static WritableCellFormat getBody() {
        WritableFont font = new WritableFont(WritableFont.TIMES, 10);// 定义字体
        WritableCellFormat format = new WritableCellFormat(font);
        try {
            format.setAlignment(jxl.format.Alignment.CENTRE);// 左右居中
            format.setVerticalAlignment(jxl.format.VerticalAlignment.CENTRE);// 上下居中
          //  format.setBorder(jxl.format.Border.ALL,jxl.format.BorderLineStyle.THIN,Colour.BLACK);
        } catch (WriteException e) {
            e.printStackTrace();
        }
        return format;
    }

    @SuppressLint("NewApi")
    public static void shareMsg(Context context,File file) {
        Log.d("shareMsg",file.toString());
        String packageName="com.tencent.mm";
        String activityName="com.tencent.mm.ui.tools.ShareImgUI";
        String appname="微信";
        if (!packageName.isEmpty() && !isAvilible(context, packageName)) {// 判断APP是否存在
            Toast.makeText(context, "请先安装" + appname, Toast.LENGTH_SHORT).show();
            return;
        }
        Intent intent = new Intent("android.intent.action.SEND");
        intent.setType("*/*");
        if (file != null) {
            if (file.isFile() && file.exists()) {
                Uri uri;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    uri = FileProvider.getUriForFile(context, "com.example.hx.ihanc.fileProvider", file);
                    //uri = Uri.fromFile(file);
                } else {
                    uri = Uri.fromFile(file);
                }
                intent.putExtra(Intent.EXTRA_STREAM, uri);
            }
        }

        if (!packageName.isEmpty()) {
            intent.setComponent(new ComponentName(packageName, activityName));
            context.startActivity(intent);
        }
    }

    public static boolean isAvilible(Context context, String packageName) {
        PackageManager packageManager = context.getPackageManager();
        List<PackageInfo> pinfo = packageManager.getInstalledPackages(0);
        for (int i = 0; i < pinfo.size(); i++) {
            if (((PackageInfo) pinfo.get(i)).packageName
                    .equalsIgnoreCase(packageName))
                return true;
        }
        return false;
    }

    private View.OnClickListener getPaymentButtonListener(){
        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                paymentButton.setClickable(false);
                RequestParams params = new RequestParams();
                params.put("member_id", member_id);
                IhancHttpClient.get("/index/sale/getMemberCredit", params, new AsyncHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                        String res = new String(responseBody);
                        Log.d("saleFragment", res);
                        try {
                            JSONObject object = new JSONObject(res);
                            int credit_sum=object.getInt("credit");
                            PaymentDialog dialog=PaymentDialog.newInstance(member_id,member_name+"--￥"+credit_sum,credit_sum,credit,1,sale_id+"");
                            PaymentDialog.OnPaymentSucceed onPaymentSucceed=new PaymentDialog.OnPaymentSucceed() {
                                @Override
                                public void PaymentSucceed() {
                                    dismiss();
                                    if(onFreshList!=null)onFreshList.freshList();
                                }
                            };
                            dialog.setOnPaymentSucceed(onPaymentSucceed);
                            dialog.show(getFragmentManager(),"payment Dialog");
                            paymentButton.setClickable(true);
                        } catch (JSONException e) {
                            Log.d("JSONArray", e.toString());
                        }
                    }
                    @Override
                    public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                    }
                });

            }
        };


    }
    public void setonFreshList(onFreshList onFreshList){
        this.onFreshList=onFreshList;
    }
    public interface onFreshList{
        void freshList();
    }
}
