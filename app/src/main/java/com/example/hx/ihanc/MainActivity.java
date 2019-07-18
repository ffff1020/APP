package com.example.hx.ihanc;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.sdk.android.push.CloudPushService;
import com.alibaba.sdk.android.push.CommonCallback;
import com.alibaba.sdk.android.push.noonesdk.PushServiceFactory;
import com.example.hx.ihanc.purchase.PurchaseMainFragment;
import com.example.hx.ihanc.store.StoreFragment;
import com.example.hx.ihanc.store.store;
import com.google.gson.JsonArray;
import com.gprinter.command.EscCommand;
import com.gprinter.command.LabelCommand;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;


import cz.msebera.android.httpclient.Header;

import static android.hardware.usb.UsbManager.ACTION_USB_DEVICE_DETACHED;
import static com.example.hx.ihanc.Constant.ACTION_USB_PERMISSION;
import static com.example.hx.ihanc.DeviceConnFactoryManager.ACTION_QUERY_PRINTER_STATE;
import static com.example.hx.ihanc.DeviceConnFactoryManager.CONN_STATE_FAILED;

public class MainActivity extends AppCompatActivity {

    public static SharedPreferences sp;
    private GPrinter mGPrinter;
    private View mProgressView;
    // Message types sent from the BluetoothChatService Handler
    public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_READ = 2;
    public static final int MESSAGE_WRITE = 3;
    public static final int MESSAGE_DEVICE_NAME = 4;
    public static final int MESSAGE_TOAST = 5;
    // Key names received from the BluetoothChatService Handler
    public static final String DEVICE_NAME = "device_name";
    public static final String TOAST = "toast";
    public static List mUnitList=new ArrayList<Unit>();
    public static List<bank> mBankList=new ArrayList<bank>();
    private SimpleDateFormat df;
    private JSONArray printCreditJSON;
    private boolean exit=false;
    private FragmentTransaction transaction;
    private FragmentManager fragmentManager;
    private boolean SaleMainFragmentCheck=true;
    private SaleMainFragment mSaleMainFragment;
    private boolean ListFragmentCheck=true;
    private ListFragment mListFragment;
    private boolean StockFragmentCheck=true;
    private boolean PurchaseMainFragmentCheck=true;
    private PurchaseMainFragment PurchaseMainFragment;
    private StoreFragment storeFragment;
    private int credit_sum=99999;
    public static ArrayList<store> storesArray=new ArrayList<store>();

    private static final String TAG = "AppApplication";

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            fragmentManager = getSupportFragmentManager();
            transaction = fragmentManager.beginTransaction();
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    if(SaleMainFragmentCheck) {
                        mSaleMainFragment=new SaleMainFragment();
                        SaleMainFragmentCheck=false;
                       // transaction.add(R.id.content,mSaleMainFragment);
                    }
                    transaction.replace(R.id.content, mSaleMainFragment);
                    transaction.commit();
                    return true;
                case R.id.navigation_dashboard:
                    Intent intent=new Intent(MainActivity.this,SettingsActivity.class);
                    startActivity(intent);
                    return true;
                case R.id.navigation_notifications:
                    if(ListFragmentCheck){
                        mListFragment=new ListFragment();
                        ListFragmentCheck=false;
                    }
                    transaction.replace(R.id.content, mListFragment);
                    transaction.commit();
                    return true;
                case R.id.navigation_stock:
                    if(StockFragmentCheck){
                        storeFragment=new StoreFragment();
                        StockFragmentCheck=false;
                    }
                    transaction.replace(R.id.content,storeFragment);
                    transaction.commit();
                    return true;
                case R.id.navigation_purchase:
                    if(PurchaseMainFragmentCheck){
                        PurchaseMainFragment=new PurchaseMainFragment();
                        PurchaseMainFragmentCheck=false;
                    }
                    transaction.replace(R.id.content,PurchaseMainFragment);
                    transaction.commit();
                    return true;
            }
            return false;
        }
    };
    @Override
    protected void onStart(){
        super.onStart();
       // Utils.getCompanyInfo();
        df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Log.d("ihanc","MainActivity onCreate");
        setContentView(R.layout.activity_main);
        ActivityCollector.addActivity(this);
        View viewStock=(View)findViewById(R.id.navigation_stock);
        viewStock.setVisibility(View.GONE);
        View viewPurchase=(View)findViewById(R.id.navigation_purchase);
        viewPurchase.setVisibility(View.GONE);
        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        if(Utils.role || Utils.auth.containsKey("stock")){
            viewStock.setVisibility(View.VISIBLE);
        }
        if(Utils.role || Utils.auth.containsKey("purchase") ||Utils.auth.containsKey("purchase_list") ||Utils.auth.containsKey("scredit")){
            viewPurchase.setVisibility(View.VISIBLE);
        }
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        navigation.setSelectedItemId(R.id.navigation_home);
        getStore();
        sp= PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
        mProgressView = findViewById(R.id.wait_progress);
        Intent intent =getIntent();
        String text =intent.getStringExtra("title");
        if(text!=null){
            navigation.setSelectedItemId(R.id.navigation_notifications);
            if(ListFragmentCheck){
                mListFragment=new ListFragment();
                ListFragmentCheck=false;
            }
            mListFragment.setPushInfo("order");
            fragmentManager = getSupportFragmentManager();
            transaction = fragmentManager.beginTransaction();
            transaction.replace(R.id.content, mListFragment);
            transaction.commit();
        }

    }
    @Override
    protected void onStop() {
        super.onStop();
        if(mGPrinter!=null){
            //unregisterReceiver(mGPrinter.receiver);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        DeviceConnFactoryManager.closeAllPort();
    }
    public  void initPrinter(Object bitmap,int credit_sum){
        String receiptType=sp.getString(getString(R.string.receipt_type),null);
        if(sp.getBoolean("bluetooth_printer",false)||receiptType==null){
            String macAddress=sp.getString(getString(R.string.bluetooth_printer_address),"");
            if(macAddress.equals("")){
                Toast.makeText(MainActivity.this,"请配置您的打印机！",Toast.LENGTH_LONG);
            }else{
                IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
                filter.addAction(ACTION_USB_DEVICE_DETACHED);
                filter.addAction(ACTION_QUERY_PRINTER_STATE);
                filter.addAction(DeviceConnFactoryManager.ACTION_CONN_STATE);
                mGPrinter=new GPrinter(macAddress,true);
                mGPrinter.setCredit_sum(credit_sum);
                registerReceiver(mGPrinter.receiver, filter);
                    if (receiptType.equals(getString(R.string.receipt_type_default)))
                        mGPrinter.print((JSONArray) bitmap);
                    else
                        mGPrinter.print((Bitmap) bitmap);

            }
        }else{
            String IP=sp.getString(getString(R.string.printer_IP),"").trim();
            IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
            filter.addAction(ACTION_USB_DEVICE_DETACHED);
            filter.addAction(ACTION_QUERY_PRINTER_STATE);
            filter.addAction(DeviceConnFactoryManager.ACTION_CONN_STATE);
            mGPrinter=new GPrinter(IP,false);
            mGPrinter.setCredit_sum(credit_sum);
            registerReceiver(mGPrinter.receiver, filter);
            if(receiptType.equals(getString(R.string.receipt_type_default)))
                mGPrinter.print((JSONArray) bitmap);
            else
                mGPrinter.print((Bitmap) bitmap);
        }
   }
    public void hideKeyboard() {
      InputMethodManager imm = (InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE);
      if (imm.isActive() && this.getCurrentFocus() != null) {
          if (this.getCurrentFocus().getWindowToken() != null) {
              imm.hideSoftInputFromWindow(this.getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
          }
      }
    }
    public void printDetails(){
        final String receiptType=sp.getString(getString(R.string.receipt_type),getString(R.string.receipt_type_default));
        RequestParams params = new RequestParams();
        params.put("member_id",Utils.printMemberName.getMember_id());
        if(!Utils.printSaleId.equals(""))
            params.put("sale_id",Utils.printSaleId);
        IhancHttpClient.get("/index/sale/creditDetail", params, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                String res = new String(responseBody);
                Log.d("saleFragment",res);
                try{
                    JSONObject myjObject=new JSONObject(res);
                    printCreditJSON=(JSONArray) myjObject.get("credit");
                    Utils.printSaleId="";
                    credit_sum=myjObject.getInt("credit_sum");
                    if(receiptType.equals(getString(R.string.receipt_type_default)))
                        initPrinter(printCreditJSON,credit_sum);
                    else{
                         showProgress(true);
                         ImageTask mImageTask = new ImageTask();
                         mImageTask.execute("");
                    }
                }catch (JSONException e){Log.d("json",e.toString());}
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {

            }
        });
    }
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
            showProgress(false);
            super.onPostExecute(bitmap);
            initPrinter(bitmap,credit_sum);
        }
    }
    private Bitmap creatCreditImage() {
        ArrayList<StringBitmapParameter> title = new ArrayList<>();
        title.add(new StringBitmapParameter(Utils.mCompanyInfo.getName(),BitmapUtil.IS_CENTER,BitmapUtil.IS_LARGE));
        title.add(new StringBitmapParameter("对账单\n",BitmapUtil.IS_CENTER));
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
                foot.add(new StringBitmapParameter("地址："+(i+1)+Utils.mCompanyInfo.getAddress()[i]+"\n"));
            }
        }else{
            foot.add(new StringBitmapParameter("地址："+Utils.mCompanyInfo.getAddress()[0]+"\n"));
        }
        Bitmap bitmapTitle=BitmapUtil.StringListtoBitmap(MainActivity.this,title);
        Bitmap bitmapFoot=BitmapUtil.StringListtoBitmap(MainActivity.this,foot);
        BitmapUtil.credit_sum=credit_sum;
        Bitmap bitmapBody=BitmapUtil.StringListtoBitmap(MainActivity.this,printCreditJSON);
        Bitmap mergeBitmap = BitmapUtil.addBitmapInHead(bitmapTitle, bitmapBody);
        mergeBitmap=BitmapUtil.addBitmapInHead(mergeBitmap,bitmapFoot);
        return mergeBitmap;
    }
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    public  void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
        }
    }
    private void getStore(){
        //Log.d("ihanc","getStore");
        if(mBankList.size()>0) return;
        IhancHttpClient.get("/index/purchase/purchaseInfo/", null, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                String res=new String(responseBody);

                try{
                    JSONObject mObject=new JSONObject(res);
                    JSONArray unitArray=mObject.getJSONArray("units");
                    for(int i=0;i<unitArray.length();i++){
                        JSONObject myjObject = unitArray.getJSONObject(i);
                        Unit mItem=new Unit(myjObject.getInt("unit_id"),
                                myjObject.getString("unit_name")
                        );
                        mUnitList.add(mItem);
                    }
                    JSONArray storeArray=mObject.getJSONArray("store");

                    final String[] stores=new String[storeArray.length()];
                    final int[] storeId=new int[storeArray.length()];
                       for (int i=0;i< storeArray.length();i++){
                           JSONObject myjObject = storeArray.getJSONObject(i);
                           stores[i]=myjObject.getString("store_name");
                           storeId[i]=myjObject.getInt("store_id");
                           store item=new store(myjObject.getInt("store_id"),myjObject.getString("store_name"));
                           storesArray.add(item);
                       }
                    if(storeArray.length()>1){
                        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                        builder.setTitle("请选择仓库");
                        builder.setCancelable(false);
                        builder.setItems(stores, new DialogInterface.OnClickListener()
                        {
                            @Override
                            public void onClick(DialogInterface dialog, int which)
                            {
                                //Toast.makeText(MainActivity.this,  stores[which]+storeId[which], Toast.LENGTH_SHORT).show();
                                Utils.storeId=storeId[which];
                            }
                        });
                        builder.show();
                    }else{
                        Utils.storeId=storeArray.getJSONObject(0).getInt("store_id");
                    }

                    JSONArray bankArray=mObject.getJSONArray("bank");
                    for (int i=0;i< bankArray.length();i++){
                        JSONObject myjObject = bankArray.getJSONObject(i);
                        bank item=new bank(myjObject.getInt("bank_id"),myjObject.getString("bank_name"));
                        mBankList.add(item);
                    }
                }catch (JSONException e){e.printStackTrace();}

            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {

            }
        });

    }
    public  void initPrinter(ArrayList<SaleDetail> saleDetails,int paid_sum,int credit_sum,String time){
        Log.d("GPrinter","initPrinter");
        String receiptType=sp.getString(getString(R.string.receipt_type),"");
        if(receiptType.equals("")){
            Toast.makeText(MainActivity.this,"请配置您的打印机！",Toast.LENGTH_LONG);
            return;
        }
        if(sp.getBoolean("bluetooth_printer",false)){
            blueSerialSetting();
            String macAddress=sp.getString(getString(R.string.bluetooth_printer_address),"");
            if(macAddress.equals("")){
                Toast.makeText(MainActivity.this,"请配置您的打印机！",Toast.LENGTH_LONG);
            }else{
                IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
                filter.addAction(ACTION_USB_DEVICE_DETACHED);
                filter.addAction(ACTION_QUERY_PRINTER_STATE);
                filter.addAction(DeviceConnFactoryManager.ACTION_CONN_STATE);
                mGPrinter=new GPrinter(macAddress,true);
                registerReceiver(mGPrinter.receiver, filter);
                if (receiptType.equals(getString(R.string.receipt_type_default)))
                    mGPrinter.print(saleDetails,paid_sum,credit_sum,time);
               // else
                    //mGPrinter.print((Bitmap) bitmap);

            }
        }else{
            String IP=sp.getString(getString(R.string.printer_IP),"").trim();
            IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
            filter.addAction(ACTION_USB_DEVICE_DETACHED);
            filter.addAction(ACTION_QUERY_PRINTER_STATE);
            filter.addAction(DeviceConnFactoryManager.ACTION_CONN_STATE);
            mGPrinter=new GPrinter(IP,false);
            registerReceiver(mGPrinter.receiver, filter);
            if(receiptType.equals(getString(R.string.receipt_type_default)))
                mGPrinter.print(saleDetails,paid_sum,credit_sum,time);
         //   else
          //      mGPrinter.print((Bitmap) bitmap);
        }
    }
    public void onBackPressed() {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_MAIN);// "android.intent.action.MAIN"
        intent.addCategory(Intent.CATEGORY_HOME); //"android.intent.category.HOME"
        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        IhancHttpClient.get("/index/setting/info", null, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                String res = new String(responseBody);
                if (res.length()<3){
                    Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                    startActivity(intent);
                    MainActivity.this.finish();
                }
            }
            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {

            }
        });
    }

    //检测设置蓝牙
    public  void blueSerialSetting(){
        BluetoothAdapter mBluetoothAdapter= BluetoothAdapter.getDefaultAdapter();
        if(!mBluetoothAdapter.isEnabled()){
            Intent intent=new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(intent,LoginActivity.REQUEST_ENABLE);
        }
    }
    //是否已经打开蓝牙
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==LoginActivity.REQUEST_ENABLE){
            if(resultCode!=RESULT_OK){
                ActivityCollector.finishAll();
            }
        }
    }


}
