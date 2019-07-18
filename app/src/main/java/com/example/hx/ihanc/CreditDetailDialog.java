package com.example.hx.ihanc;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.PermissionChecker;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageButton;
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
import jxl.JXLException;
import jxl.Workbook;
import jxl.write.Label;
import jxl.write.WritableCellFormat;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;
import jxl.write.biff.JxlWriteException;

import static com.example.hx.ihanc.SaleDetailDialog.getBody;
import static com.example.hx.ihanc.SaleDetailDialog.getFoot;
import static com.example.hx.ihanc.SaleDetailDialog.getHeader;
import static com.example.hx.ihanc.SaleDetailDialog.shareMsg;

public class CreditDetailDialog extends DialogFragment {
    private int member_id;
    private String title;
    private TextView titleTV;
    private List<CreditDetail> data=new ArrayList<CreditDetail>();
    private CreditDetailAdpter.checkBoxListener myOnCheckChangeListener;
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
    private ProgressBar mProgressView;
    private TextView selectedSumTV;
    private ImageButton tel;
    private String phoneNumber;

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
        selected="";
        selectedNum=0;
        this.member_id=getArguments().getInt("member_id");
        this.title=getArguments().getString("title");

        }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_credit_detail, container);
        titleTV=view.findViewById(R.id.title_credit_detail);
        titleTV.setText(title);
        credit_detailLT=view.findViewById(R.id.credit_detail);
        selectedSumTV=view.findViewById(R.id.selectedSumTV);
        myOnCheckChangeListener=new CreditDetailAdpter.checkBoxListener() {
            @Override
            public void onCheckboxChange(int position, CompoundButton buttonView, boolean isChecked) {
               // Log.d("creditDialog",position+""+data.get(position).getSum());
                if (isChecked) {
                    CreditDetail item = data.get(position);
                    selectedTotal += item.getSum();
                    item.selected = true;
                    if (selectedNum == 0)
                        selected += item.getSale_id();
                    else
                        selected += "," + item.getSale_id();
                    selectedNum++;
                } else {
                    String str = "," + data.get(position).getSale_id();
                    if (!selected.contains(str))
                        str = data.get(position).getSale_id() + "";
                    //Log.d("creditDialog",str);
                    selected = selected.replace(str, "");
                    selectedTotal -= data.get(position).getSum();
                    selectedNum--;
                }
                if(selectedNum!=0){
                    NumberFormat format = NumberFormat.getCurrencyInstance(Locale.CHINA);
                    selectedSumTV.setText("已选金额合计："+format.format(selectedTotal));
                    selectedSumTV.setVisibility(View.VISIBLE);
                }else {
                    selectedSumTV.setVisibility(View.GONE);
                }
            }
        };
        adapter=new CreditDetailAdpter(getContext(),R.layout.list_credit_detail,data,myOnCheckChangeListener);
        credit_detailLT.setAdapter(adapter);
        wxButton=view.findViewById(R.id.wxButton);
        wxButton.setOnClickListener(getWxButtonListener());
        printButton=view.findViewById(R.id.printButton);
        printButton.setOnClickListener(getPrintButtonListener());
        paymentButton=view.findViewById(R.id.payButton);
        paymentButton.setOnClickListener(getPaymentButtonListener());
        mProgressView=view.findViewById(R.id.loading_progress);
        tel=view.findViewById(R.id.tel);
        initData();

        return view;
    }

    public View.OnClickListener getPrintButtonListener(){
        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (data.size()<1)return;
                if(selectedNum>0) {
                    Utils.printSaleId=selected;
                }
                if (Utils.mCompanyInfo == null) Utils.getCompanyInfo();
                String[] titles = title.split("--");
                Utils.printMemberName = new member(member_id, titles[0], "","");
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
                if(data.size()<1) return;
                if(Utils.mCompanyInfo==null) Utils.getCompanyInfo();
                Date now=new Date();
                SimpleDateFormat f=new SimpleDateFormat("yyyy-MM-dd HH:mm");
                String[] titles=title.split("--");
                String fileName="ihanc--"+Utils.mCompanyInfo.getName()+System.currentTimeMillis()+".xls";
                File file = new File(Environment.getExternalStorageDirectory(), fileName);
                WritableWorkbook wwb;
                int size=data.size();
                try{
                    OutputStream os = new FileOutputStream(file);
                    try {
                        wwb = Workbook.createWorkbook(os);
                        WritableSheet sheet = wwb.createSheet(Utils.mCompanyInfo.getName()+"对账单", 0);
                        Label label;
                        try {
                            label = new Label(0, 0, Utils.mCompanyInfo.getName(), getHeader());
                            sheet.addCell(label);
                            sheet.setRowView(0,500);
                            label = new Label(0, 1, "对账单", getBody());
                            sheet.addCell(label);
                            label = new Label(0, 2, "客户："+titles[0]);
                            sheet.addCell(label);
                            label = new Label(0, 3, "打印时间："+f.format(now));
                            sheet.addCell(label);
                            label = new Label(0, 4, "");
                            sheet.addCell(label);
                            sheet.mergeCells(0, 0, 4, 0);
                            sheet.mergeCells(0, 1, 4, 0);
                            sheet.mergeCells(0, 2, 4, 0);
                            sheet.mergeCells(0, 3, 4, 0);
                            sheet.mergeCells(0, 4, 4, 0);
                            NumberFormat format=NumberFormat.getCurrencyInstance(Locale.CHINA);
                            WritableCellFormat body=getBody();
                            label = new Label(0, 5, "日期",body);
                            sheet.addCell(label);
                            label = new Label(1, 5, "商品名称",body);
                            sheet.addCell(label);
                            label = new Label(2, 5, "数量",body);
                            sheet.addCell(label);
                            label = new Label(3, 5, "价格",body);
                            sheet.addCell(label);
                            label = new Label(4, 5, "金额",body);
                            sheet.addCell(label);
                            int sum=0;
                            int maxLen=12;
                            if(selectedNum>0){
                                boolean check=false;
                                int j=0;
                                int k=0;
                                for (int i = 0; i < size; i++) {
                                    CreditDetail item = data.get(i);
                                    if(!item.isGroup()){
                                        if(item.selected){
                                            check=true;
                                            label = new Label(0, 6+j, item.getTime().substring(5,10),body);
                                            sheet.addCell(label);
                                            label = new Label(1, 6+j, item.getGoods_name(),body);
                                            if(maxLen<item.getGoods_name().getBytes().length) maxLen=item.getGoods_name().getBytes().length;
                                            sheet.addCell(label);
                                            String temp[] = item.getSummary().split("[*]");
                                            if(temp.length>1) {
                                                label = new Label(2, 6 + j, temp[0], body);
                                                sheet.addCell(label);
                                                temp = temp[1].split("=");
                                                label = new Label(3, 6 + j, temp[0], body);
                                                sheet.addCell(label);
                                                label = new Label(4, 6 + j, temp[1], body);
                                                sheet.addCell(label);
                                                sum += item.getSum();
                                            }else{
                                                label = new Label(2, 6 + j, "", body);
                                                sheet.addCell(label);
                                                label = new Label(3, 6 + j, "", body);
                                                sheet.addCell(label);
                                                label = new Label(4, 6 + j, temp[0], body);
                                                sheet.addCell(label);
                                            }
                                            if(j>0){
                                                sheet.mergeCells(0,6+k,0,j+5);
                                            }
                                            k=j;
                                            j++;
                                        }else check=false;
                                    }else if(check){
                                        if(maxLen<item.getGoods_name().getBytes().length) maxLen=item.getGoods_name().getBytes().length;
                                        label = new Label(1, 6+j, item.getGoods_name(),body);
                                        sheet.addCell(label);
                                        String temp[] = item.getSummary().split("[*]");
                                        if(temp.length>1) {
                                            label = new Label(2, 6 + j, temp[0], body);
                                            sheet.addCell(label);
                                            temp = temp[1].split("=");
                                            label = new Label(3, 6 + j, temp[0], body);
                                            sheet.addCell(label);
                                            label = new Label(4, 6 + j, temp[1], body);
                                            sheet.addCell(label);
                                        }else{
                                            label = new Label(4, 6 + j, temp[0], body);
                                            sheet.addCell(label);
                                        }
                                        j++;
                                    }
                                }
                                size=j;
                                if(j!=k)sheet.mergeCells(0,6+k,0,j+5);
                            }else{
                                int j=0;
                                for (int i = 0; i < size+1; i++) {
                                    if(i==size&&j!=i){
                                        sheet.mergeCells(0,6+j,0,i+5);
                                        break;
                                    }
                                    CreditDetail item = data.get(i);
                                    if (!item.isGroup()) {
                                        label = new Label(0, 6+i, item.getTime().substring(5,10),body);
                                        sheet.addCell(label);
                                        sum += item.getSum();
                                        Log.d("shareMsg","i:"+i+"j:"+j);
                                        if(i>0){
                                            sheet.mergeCells(0,6+j,0,i+5);
                                        }
                                        j=i;
                                    }
                                    if(maxLen<item.getGoods_name().getBytes().length) maxLen=item.getGoods_name().getBytes().length;
                                    label = new Label(1, 6+i, item.getGoods_name(),body);
                                    sheet.addCell(label);
                                    String temp[] = item.getSummary().split("[*]");
                                    if(temp.length>1) {
                                        label = new Label(2, 6 + i, temp[0], body);
                                        sheet.addCell(label);
                                        temp = temp[1].split("=");
                                        label = new Label(3, 6 + i, temp[0], body);
                                        sheet.addCell(label);
                                        label = new Label(4, 6 + i, temp[1], body);
                                        sheet.addCell(label);
                                    }else{
                                        label = new Label(4, 6 + i, temp[0], body);
                                        sheet.addCell(label);
                                    }
                                }
                            }
                            sheet.setColumnView(1,maxLen);
                            sheet.setColumnView(0,6);
                            size+=6;
                            label = new Label(0, size, "");
                            sheet.addCell(label);
                            sheet.mergeCells(0, size, 4, 0);
                            size++;
                            label = new Label(0, size, "合计金额:"+format.format(sum));
                            sheet.addCell(label);
                            sheet.mergeCells(0, size, 4, 0);
                            size++;
                            label = new Label(0, size, "累计应收金额:"+titles[1]);
                            sheet.addCell(label);
                            sheet.mergeCells(0, size, 4, 0);
                            size++;
                            label = new Label(0, size, "感谢您的惠顾，欢迎下次光临！",getFoot());
                            sheet.addCell(label);
                            sheet.mergeCells(0, size, 4, 0);
                            size++;
                            label = new Label(0, size, "联系电话："+Utils.mCompanyInfo.getTel());
                            sheet.addCell(label);
                            sheet.mergeCells(0, size, 4, 0);
                            size++;
                            if(Utils.mCompanyInfo.getAddress().length==1){
                                label = new Label(0, size, "地址："+Utils.mCompanyInfo.getAddress()[0]);
                                sheet.addCell(label);
                                sheet.mergeCells(0, size, 4, 0);
                                size++;
                            }else {
                                for (int i = 0; i < Utils.mCompanyInfo.getAddress().length; i++) {
                                    label = new Label(0, size, "地址"+(i+1)+"：" + Utils.mCompanyInfo.getAddress()[i]);
                                    sheet.addCell(label);
                                    sheet.mergeCells(0, size, 4, 0);
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
        showProgress(true);
        RequestParams params=new RequestParams();
        params.put("member_id",member_id);
        IhancHttpClient.get("/index/sale/creditDetail", params, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                String res=new String(responseBody);
                try {
                    JSONObject resJson=new JSONObject(res);
                    try {
                        phoneNumber=resJson.getString("tel");
                        if (phoneNumber.length()>7){
                            tel.setVisibility(View.VISIBLE);
                           // tel.setVisibility(View.GONE);
                            tel.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    if(!hasPermission()){
                                        requestPermissions(new String[]{Manifest.permission.CALL_PHONE},1);
                                    }else{
                                        intentToCall();
                                    }
                                }
                            });
                        }else{
                            tel.setVisibility(View.GONE);
                        }
                    }catch (JSONException e){e.printStackTrace();tel.setVisibility(View.GONE);}
                    JSONArray list=resJson.getJSONArray("credit");
                    creditSum=resJson.getInt("credit_sum");
                    int sale_id=0;
                    for (int i = 0; i <list.length() ; i++) {
                        Log.d("creditDetailDialog",i+"");
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
                    adapter.setIsSelected(data.size());
                    ViewGroup.LayoutParams lp = credit_detailLT.getLayoutParams();
                    if (data.size() > 10)
                    {
                        WindowManager manager = getActivity().getWindowManager();
                        DisplayMetrics outMetrics = new DisplayMetrics();
                        manager.getDefaultDisplay().getMetrics(outMetrics);
                        int height = outMetrics.heightPixels;
                        lp.height = (int)(height * 0.6);
                    }
                    else
                    {
                        lp.height = ViewGroup.LayoutParams.WRAP_CONTENT;
                    }
                    credit_detailLT.setLayoutParams(lp);
                    showProgress(false);
                }catch (JSONException e){e.printStackTrace();}
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {

            }
        });
    }

    public interface OnFreshCredit{
        void freshCredit();
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            if(getContext()==null)return;
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

    private boolean hasPermission() {
        if(Build.VERSION.SDK_INT >=23){
            if (getContext().checkSelfPermission(Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }else{
            if (PermissionChecker.checkSelfPermission(getContext(),Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }

        return true;
    }

    private void intentToCall() {
        Intent intent = new Intent(Intent.ACTION_CALL);
        Uri data = Uri.parse("tel:" + phoneNumber);
        intent.setData(data);
        startActivity(intent);
    }


    /**
     * 动态请求拨打电话权限后，监听用户的点击事件
     */
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 0x11) {
            // If request is cancelled, the result arrays are empty.
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                intentToCall();
            } else {

            }
        }
    }
}
