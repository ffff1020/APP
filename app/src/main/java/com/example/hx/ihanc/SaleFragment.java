package com.example.hx.ihanc;

import android.content.Context;
import android.content.DialogInterface;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONString;

import java.io.Serializable;
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

import static android.hardware.usb.UsbManager.ACTION_USB_DEVICE_DETACHED;
import static com.example.hx.ihanc.Constant.ACTION_USB_PERMISSION;
import static com.example.hx.ihanc.DeviceConnFactoryManager.ACTION_QUERY_PRINTER_STATE;

public class SaleFragment extends Fragment {
    private member member;
    private TextView title;
    private Button button;
    private Button saveBtn;
    private Button orderBtn;
    public SharedPreferences sp;
    private boolean printAble=false;
    private ArrayList<SaleDetail> mSaleDetails=new ArrayList<SaleDetail>(){};
    private ListView saleDetailLV;
    private Context mContext;
    private SaleDetailAdapter mSaleDetailAdapter;
    private boolean saved;
    Bundle savedState;
    private boolean delete;
    private int ttl;
    private EditText ttlTV;
    private TextView ttlSum;
    private NumberFormat formatter = NumberFormat.getNumberInstance(Locale.CHINA);
    private BankAdapter mBankAdapter;
    private Spinner bankSpinner;
    private int credit_sum;
    private SaleMainFragment mParentFragment;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
       // saved = false;
        View view = inflater.inflate(R.layout.sale_fragment, container, false);
        title=(TextView)view.findViewById(R.id.title);
        saleDetailLV=(ListView)view.findViewById(R.id.SaleDetailListView);
        List<Fragment>list=(List<Fragment>)SaleFragment.this.getFragmentManager().getFragments();
        for(Fragment f:list){
            if(f!=null&&f instanceof SaleMainFragment){
                mParentFragment=(SaleMainFragment) f;
                break;
            }
        }
        title.setOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View view) {
              CreditDetailDialog creditDetailDialog=CreditDetailDialog.newInstance(member.member_id,
                      member.member_name+"--￥"+formatter.format(credit_sum));
              CreditDetailDialog.OnFreshCredit onFreshCredit=new CreditDetailDialog.OnFreshCredit() {
                  @Override
                  public void freshCredit() {
                      getCredit();
                  }
              };
              creditDetailDialog.setOnFreshCredit(onFreshCredit);
              creditDetailDialog.show(getFragmentManager(),"credit");
              //Log.d("saleFragment","print_credit");
             /* if(printAble){
                  // Log.d("saleFragment","print_credit");
                  if(Utils.mCompanyInfo==null) Utils.getCompanyInfo();
                  Utils.printMemberName=member;
                  MainActivity parentActivity = (MainActivity ) getActivity();
                  parentActivity.printDetails();
              }*/

          }
      });
        button=(Button)view.findViewById(R.id.closeFragmentButton);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mSaleDetails.size()>0){
                    final AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                    builder.setIcon(R.mipmap.ihanc);
                    builder.setTitle("瀚盛水产销售系统");
                    builder.setMessage("关闭会删除销售明细，确定关闭吗？");
                    builder.setPositiveButton("确定", new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialog, int which)
                        {
                            mParentFragment.deleteCurrentSaleTabs();
                        }
                    });
                    builder.setNegativeButton("取消", new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialog, int which)
                        {
                        }
                    });
                    builder.show();
                }else {
                    mParentFragment.deleteCurrentSaleTabs();
                }
            }
        });

        ttlTV=(EditText) view.findViewById(R.id.ttl);
        ttlTV.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if(b) ttlTV.setText("");
            }
        });
        ttlTV.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                if (i == EditorInfo.IME_ACTION_DONE) {
                    save();
                    return true;
                }
                return false;
            }
        });
        ttlSum=(TextView) view.findViewById(R.id.ttlSum);
        bankSpinner=(Spinner)view.findViewById(R.id.bankSpinner);
        //Log.d("saleFragment",MainActivity.mBankList.size()+"");
        mBankAdapter=new BankAdapter(mContext,R.layout.unit,MainActivity.mBankList);
        bankSpinner.setAdapter(mBankAdapter);
        bankSpinner.setSelection(0);
        saveBtn=(Button)view.findViewById(R.id.save);
        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                save();
            }

        });
        ttlTV.setText("");
       /* ttlSum.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Log.d("GPrinter","SaleFragment");
                if(mSaleDetails.size()==0) return;
                if(Utils.mCompanyInfo==null) Utils.getCompanyInfo();
                Utils.printMemberName=member;
                String paidSum=ttlTV.getText().toString();
                int paid_sum=0;
                if(!paidSum.equals("")){
                    paid_sum=Integer.parseInt(paidSum);
                }
                final String receiptType=sp.getString(getString(R.string.receipt_type),getString(R.string.receipt_type_default));
                MainActivity parentActivity = (MainActivity ) getActivity();
                if(receiptType.equals(getString(R.string.receipt_type_default))) {
                    Log.d("GPrinter","SaleFragment");
                    parentActivity.initPrinter(mSaleDetails,paid_sum,credit_sum);
                } else{
                    parentActivity.showProgress(true);
                    ImageTask mImageTask = new ImageTask();
                    mImageTask.execute("");
                }
            }
        });*/
        orderBtn=view.findViewById(R.id.orderButton);
        orderBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                order();
            }
        });
        Button printButton=view.findViewById(R.id.print);
        printButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                print();
            }
        });
        return view;
    }

    public void setMember(com.example.hx.ihanc.member member) {
        this.member = member;
       //this.mContext=context;
    }

    public member getMember(){
        return this.member;
    }

    public void addSaleDetail(SaleDetail detail){
        if(mSaleDetails.size()>0&&detail.toString().equals(mSaleDetails.get(mSaleDetails.size()-1).toString()))return;
        this.mSaleDetails.add(detail);
        this.mSaleDetailAdapter.notifyDataSetChanged();
        ttl = 0;
        for (SaleDetail item:mSaleDetails) {
            ttl+=item.getSum();
        }
       // ttlTV.setText(ttl+"");
        ttlSum.setText("合计金额：￥"+formatter.format(ttl));
        delete=false;
    }

   @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        // Restore State Here
        if (!restoreStateFromArguments()) {
            // First Time running, Initialize something here
            mSaleDetails=new ArrayList<SaleDetail>(){};
            getCredit();
            saved = false;
            ttl=0;
           // ttlTV.setText(ttl+"");
            ttlSum.setText("合计金额：￥"+formatter.format(ttl));
            mSaleDetailAdapter=new SaleDetailAdapter(mContext,R.layout.sale_detail_item,mSaleDetails,mListener);
            saleDetailLV.setAdapter(mSaleDetailAdapter);

        }
       Log.d("saleFragment",mSaleDetails.size()+"");
    }
    @Override
    public void onSaveInstanceState(Bundle outState) {
        //super.onSaveInstanceState(outState);
        // Save State Here
       // if(!saved)saveStateToArguments();
//        saveStateToArguments();
    }
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Save State Here
        if(!saved)saveStateToArguments();
        Log.d("saleFragment","onDestroyView");
    }
    @Override
    public void onAttach(Context context){
        super.onAttach(context);
        mContext=context;
        sp=PreferenceManager.getDefaultSharedPreferences(getActivity());
    }
    @Override
    public void onStart(){
        super.onStart();
        mSaleDetailAdapter.notifyDataSetChanged();
        delete=false;
    }
    private void saveStateToArguments() {
        if(mSaleDetails.size()>0) {
            Log.d("saleFragment", "saveStateToArguments");
            Bundle b = getArguments();
            savedState = saveState();
            if (b == null) {
                saved = true;
                b = new Bundle();
                b.putBundle("saleFragment", savedState);
                b.putString("credit", title.getText().toString());
                //b.putInt("show",title.getVisibility());
                b.putInt("ttl", ttl);
                b.putInt("credit_sum",credit_sum );
                this.setArguments(b);
            } else if (!b.equals(savedState)) {
                saved = true;
                b.putBundle("saleFragment", savedState);
                b.putString("credit", title.getText().toString());
                //b.putInt("show",title.getVisibility());
                b.putInt("ttl", ttl);
                b.putInt("credit_sum",credit_sum );
                this.setArguments(b);
            }
        }
    }

    public String getCreditBundle(){
        if(credit_sum!=0)
        return title.getText().toString();
        else
            return "";
    }

  //  public int getShow(){return title.getVisibility(); }
    public int getTTL(){return ttl;}
    public int getCredit_sum(){return credit_sum;}

    private boolean restoreStateFromArguments() {
        Bundle b = getArguments();
        if(b==null) return false;
        savedState = b.getBundle("saleFragment");
        title.setText(b.getString("credit"));
       // title.setVisibility(b.getInt("show"));
        ttl=b.getInt("ttl");
        credit_sum=b.getInt("credit_sum");
        if(credit_sum!=0){
            printAble=true;
            title.setVisibility(View.VISIBLE);
        }
        //ttlTV.setText(ttl+"");

        if (savedState != null) {
            restoreState();
            ttl=0;
            for (int i = 0; i < mSaleDetails.size(); i++) {
                ttl+=mSaleDetails.get(i).getSum();
            }
            ttlSum.setText("合计金额：￥"+formatter.format(ttl));
            return true;
        }
        return false;
    }
    /////////////////////////////////
// 取出状态数据
/////////////////////////////////
    private void restoreState() {
        if (savedState != null) {
            mSaleDetails=savedState.getParcelableArrayList("listParcel");
            mSaleDetailAdapter=new SaleDetailAdapter(mContext,R.layout.sale_detail_item,mSaleDetails,mListener);
            saleDetailLV.setAdapter(mSaleDetailAdapter);

        }
    }
    //////////////////////////////
// 保存状态数据
//////////////////////////////
    public Bundle saveState() {
        Bundle state = new Bundle();
        state.putParcelableArrayList("listParcel",mSaleDetails);
        return state;
    }

    private void getCredit() {
        if (this.member != null) {
            RequestParams params = new RequestParams();
            params.put("member_id", this.member.getMember_id());
            IhancHttpClient.get("/index/sale/getMemberCredit", params, new AsyncHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                    String res = new String(responseBody);
                    //Log.d("saleFragment", res);
                    if(res.contains("\"credit\":null")){
                        title.setText("");
                        title.setVisibility(View.GONE);
                        return;
                    }
                    try {
                        JSONObject object = new JSONObject(res);
                        credit_sum=object.getInt("credit");
                        if (credit_sum != 0) {
                            title.setText("打印欠款金额：￥" + formatter.format(credit_sum ));
                            title.setVisibility(View.VISIBLE);
                            printAble = true;
                        }
                    } catch (JSONException e) {
                        Log.d("JSONArray", e.toString());
                    }
                }
                @Override
                public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                }
            });
        }
    }

    private SaleDetailAdapter.MyClickListener mListener = new SaleDetailAdapter.MyClickListener() {
         @Override
        public void myOnClick(int position, View v) {
             if (!delete) {
                 delete = true;
                 Toast.makeText(mContext, "再按一次删除", Toast.LENGTH_SHORT).show();
                 eHandler.sendEmptyMessageDelayed(0, 2000);
             } else {
                 ttl-=mSaleDetails.get(position).getSum();
                // ttlTV.setText(ttl+"");
                 ttlSum.setText("合计金额：￥"+formatter.format(ttl));
                 mSaleDetails.remove(position);
                 mSaleDetailAdapter.notifyDataSetChanged();
                 delete=false;
             }
                    };

        @Override
        public void myModifyClick(int position, View v) {

        }
    };
    Handler eHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            delete = false;
        }
    };
    private class ImageTask extends AsyncTask<String, Void, Bitmap> {
        @Override
        protected Bitmap doInBackground(String... params) {
            //Log.d("bitmap","start");
            return creatCreditImage();
        }

        @Override
        protected void onPreExecute() {
            //Log.d("bitmap","onPreExecute");
            super.onPreExecute();
            //
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            ((MainActivity ) getActivity()).showProgress(false);
            super.onPostExecute(bitmap);
            ((MainActivity ) getActivity()).initPrinter(bitmap,credit_sum);
        }
    }

    private Bitmap creatCreditImage(){
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        ArrayList<StringBitmapParameter> title = new ArrayList<>();
        title.add(new StringBitmapParameter(Utils.mCompanyInfo.getName(),BitmapUtil.IS_CENTER,BitmapUtil.IS_LARGE));
        title.add(new StringBitmapParameter("销售单\n",BitmapUtil.IS_CENTER));
        title.add(new StringBitmapParameter("客户："+Utils.printMemberName.getMember_name()));
        String date = df.format(new Date());
        title.add(new StringBitmapParameter("打印时间："+date));
        title.add(new StringBitmapParameter(BitmapUtil.PRINT_LINE));
        ArrayList<StringBitmapParameter> foot = new ArrayList<>();
        foot.add(new StringBitmapParameter(BitmapUtil.PRINT_LINE));
        foot.add(new StringBitmapParameter("感谢您的惠顾，欢迎下次光临!\n",BitmapUtil.IS_CENTER));
        foot.add(new StringBitmapParameter("联系电话："+Utils.mCompanyInfo.getTel()+"\n"));
        if(Utils.mCompanyInfo.getAddress().length>1){
            for (int i=0;i<Utils.mCompanyInfo.getAddress().length;i++){
                foot.add(new StringBitmapParameter("地址："+(i+1)+Utils.mCompanyInfo.getAddress()[0]+"\n"));
            }
        }else{
            foot.add(new StringBitmapParameter("地址："+Utils.mCompanyInfo.getAddress()[0]+"\n"));
        }
        Bitmap bitmapTitle=BitmapUtil.StringListtoBitmap(mContext,title);
        Bitmap bitmapFoot=BitmapUtil.StringListtoBitmap(mContext,foot);
        int paid_sum=0;
        String paidSum=ttlTV.getText().toString();
        if(!paidSum.equals("")){
            paid_sum=Integer.parseInt(paidSum);
        }
        Bitmap bitmapBody=BitmapUtil.StringListtoBitmap(mContext,mSaleDetails,paid_sum,credit_sum);
        Bitmap mergeBitmap = BitmapUtil.addBitmapInHead(bitmapTitle, bitmapBody);
        mergeBitmap=BitmapUtil.addBitmapInHead(mergeBitmap,bitmapFoot);
        return mergeBitmap;
    }

    public static SaleFragment newInstance(Bundle args) {
        SaleFragment fragment = new SaleFragment();
        fragment.setArguments(args);
        return fragment;
    }

    public void order(){
        orderBtn.setClickable(false);
        String paidSum=ttlTV.getText().toString();
        int paid_sum=0;
        if(!paidSum.equals("")){
            paid_sum=Integer.parseInt(paidSum);
        }
        Pay pay =new Pay(mBankAdapter.getItem(bankSpinner.getSelectedItemPosition()).getBank(),paid_sum);
        JSONObject params=new JSONObject();
        JSONObject data=new JSONObject();
        try {
            int size=mSaleDetails.size();
            JSONArray details=new JSONArray();
            for (int i=0;i<size;i++){
                details.put(i,mSaleDetails.get(i).getSaleDetailJson());
            }
            data.put("member",member.getMember());
            data.put("ttl_sum",ttl);
            params.put("pay",pay.getPayJson());
            params.put("data",data);
            params.put("detail",details);
            Date now=new Date();
            SimpleDateFormat f=new SimpleDateFormat("yyyy-MM-dd HH:mm");
            params.put("time",f.format(now));
        }catch (JSONException e){
            e.printStackTrace();
        }
        Log.d("order",params.toString());
        IhancHttpClient.postJson(mContext,"/index/sale/order", params, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                String result=new String(responseBody);
                Log.d("order",new String(responseBody));
                try {
                    JSONObject mObject=new JSONObject(result);
                    if(mObject.getInt("result")==1){
                        Utils.toast(mContext,"保存成功！");
                        ttlTV.setText("");
                        mParentFragment.deleteCurrentSaleTabs();
                        // ((SaleMainFragment)(SaleFragment.this.getParentFragment())).deleteCurrentSaleTabs();
                    }else{
                        Utils.toast(mContext,"保存发生错误，请重新保存！");
                    }
                    orderBtn.setClickable(true);
                }catch (JSONException e){e.printStackTrace();}
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                Log.d("order",new String(responseBody));
                Utils.toastLostLink(mContext);
            }
        });
    }

    private void print(){
        //Log.d("GPrinter","SaleFragment");
        if(mSaleDetails.size()==0) return;
        if(Utils.mCompanyInfo==null) Utils.getCompanyInfo();
        Utils.printMemberName=member;
        String paidSum=ttlTV.getText().toString();
        int paid_sum=0;
        if(!paidSum.equals("")){
            paid_sum=Integer.parseInt(paidSum);
        }
        final String receiptType=sp.getString(getString(R.string.receipt_type),getString(R.string.receipt_type_default));
        MainActivity parentActivity = (MainActivity ) getActivity();
        if(receiptType.equals(getString(R.string.receipt_type_default))) {
            Log.d("GPrinter","SaleFragment");
            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String date=df.format(new Date());
            parentActivity.initPrinter(mSaleDetails,paid_sum,credit_sum,date);
        } else{
            parentActivity.showProgress(true);
            ImageTask mImageTask = new ImageTask();
            mImageTask.execute("");
        }
    }

    private void save(){
        saveBtn.setClickable(false);
        MainActivity parentActivity = (MainActivity) getActivity();
        parentActivity.hideKeyboard();
        String paidSum=ttlTV.getText().toString();
        int paid_sum=0;
        if(!paidSum.equals("")){
            paid_sum=Integer.parseInt(paidSum);
        }
        Pay pay =new Pay(mBankAdapter.getItem(bankSpinner.getSelectedItemPosition()).getBank(),paid_sum);
        JSONObject params=new JSONObject();
        JSONObject data=new JSONObject();
        try {
            int size=mSaleDetails.size();
            JSONArray details=new JSONArray();
            for (int i=0;i<size;i++){
                details.put(i,mSaleDetails.get(i).getSaleDetailJson());
            }
            data.put("member",member.getMember());
            data.put("ttl_sum",ttl);
            params.put("back",1);
            params.put("pay",pay.getPayJson());
            params.put("data",data);
            params.put("detail",details);

        }catch (JSONException e){
            e.printStackTrace();
        }
        IhancHttpClient.postJson(mContext,"/index/sale/sale", params, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                String result=new String(responseBody);
                try {
                    JSONObject mObject=new JSONObject(result);
                    if(mObject.getInt("result")==1){
                        Utils.toast(mContext,"保存成功！");
                        ttlTV.setText("");
                        mParentFragment.deleteCurrentSaleTabs();
                        // ((SaleMainFragment)(SaleFragment.this.getParentFragment())).deleteCurrentSaleTabs();
                    }else{
                        Utils.toast(mContext,"保存发生错误，请重新保存！");
                    }
                    saveBtn.setClickable(true);
                }catch (JSONException e){e.printStackTrace();}
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                Log.d("saleError",new String(responseBody));
                Utils.toastLostLink(mContext);
            }
        });

    }
}
