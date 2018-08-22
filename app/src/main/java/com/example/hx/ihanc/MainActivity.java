package com.example.hx.ihanc;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
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
import java.util.List;
import java.util.Vector;


import cz.msebera.android.httpclient.Header;

import static android.hardware.usb.UsbManager.ACTION_USB_DEVICE_DETACHED;
import static com.example.hx.ihanc.Constant.ACTION_USB_PERMISSION;
import static com.example.hx.ihanc.DeviceConnFactoryManager.ACTION_QUERY_PRINTER_STATE;
import static com.example.hx.ihanc.DeviceConnFactoryManager.CONN_STATE_FAILED;

public class MainActivity extends AppCompatActivity {

    private TextView mTextMessage;
    public SharedPreferences sp;
    private GPrinter mGPrinter;
    private ListView categoryLv;
    private CategoryAdapter mCategoryAdapter;
    private GridView mGridView;
    private GoodsAdapter mGoodsAdapter;
    private SearchView mGoodSearchView;
    private AutoCompleteTextView memberTV;
    private MemberAdapter memberAdapter;
    private SalePagerAdapter mSalePagerAdapter;
    private UnitAdapter mUnitAdapter;
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
    // Intent request codes
    private static final int REQUEST_CONNECT_DEVICE = 1;
    private static final int REQUEST_ENABLE_BT = 2;
    private List mCategoryDataList=new ArrayList<category>();
    private List mGoodsDataList=new ArrayList<Goods>();
    private List memberDataList=new ArrayList<member>();
    private List<SaleFragment> fragmentList=new ArrayList<SaleFragment>();
    private List<member> memberTabsList=new ArrayList<>();
    private List mUnitList=new ArrayList<Unit>();
    public static List<bank> mBankList=new ArrayList<bank>();
    private SimpleDateFormat df;
    private ViewPager vp;
    private TabLayout tl;
    private MyNumberEdit mWeight;
    private MyNumberEdit mPrice;
    private MyNumberEdit mSumEdit;
    private Goods currentGood=null;
    private TextView infoGoodsName;
    private Spinner unitSpinner;
    private JSONArray printCreditJSON;
    private Button addToSaleBtn;
    private boolean exit=false;

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    return true;
                case R.id.navigation_dashboard:
                    Intent intent=new Intent(MainActivity.this,SettingsActivity.class);
                    startActivity(intent);
                    return true;
                case R.id.navigation_notifications:
                   // intent=new Intent(MainActivity.this,ListActivity.class);
                   // startActivity(intent);

                    return true;
                case R.id.navigation_exit:
                    if (!exit) {
                        exit = true;
                        Toast.makeText(getApplicationContext(), "再按一次退出程序",
                                Toast.LENGTH_SHORT).show();
                        eHandler.sendEmptyMessageDelayed(0, 2000);
                    } else {
                        IhancHttpClient.setAuth("");
                        LoginActivity.mPrefEditor.putString("name","");
                        LoginActivity.mPrefEditor.putString("token","");
                        LoginActivity.mPrefEditor.commit();
                        finish();
                        System.exit(0);
                    }


            }
            return false;
        }
    };
    @Override
    protected void onStart(){
        super.onStart();
       // Utils.getCompanyInfo();
        df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        /*IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
        filter.addAction(ACTION_USB_DEVICE_DETACHED);
        filter.addAction(ACTION_QUERY_PRINTER_STATE);
        filter.addAction(DeviceConnFactoryManager.ACTION_CONN_STATE);
        registerReceiver(receiver, filter);*/
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ActivityCollector.addActivity(this);
        initView();
        getStore();
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

    public void initView(){
        infoGoodsName=(TextView)findViewById(R.id.infoGoodsName);
        mTextMessage = (TextView) findViewById(R.id.goodsTV);
        mProgressView = findViewById(R.id.wait_progress);
        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        sp=PreferenceManager.getDefaultSharedPreferences(this);
        categoryLv=(ListView)findViewById(R.id.category);
        mCategoryAdapter=new CategoryAdapter(MainActivity.this,R.layout.device_name,mCategoryDataList);
        categoryLv.setAdapter(mCategoryAdapter);
        categoryLv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                category categoryItem=(category) mCategoryAdapter.getItem(i);
                GoodsAdapter.MyFilter mFilter=mGoodsAdapter.getFilter();
                mFilter.setMyFilter(Utils.GOODSFILTERCATEGORYID);
                mFilter.filter(String.valueOf(categoryItem.getCategory_id()));
            }
        });
        getCategoryData();
        mGridView=(GridView)findViewById(R.id.goods);
        mGoodsAdapter=new GoodsAdapter(MainActivity.this,R.layout.goods,mGoodsDataList);
        mGridView.setAdapter(mGoodsAdapter);
        getGoodsData();
        mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                currentGood=(Goods)adapterView.getAdapter().getItem(i);
                infoGoodsName.setText(currentGood.getGoods_name());
                mPrice.setPrice(currentGood.getGoods_price());
                mSumEdit.setNum(currentGood.getGoods_price()*mWeight.getNum());
                RequestParams params = new RequestParams();
                params.put("goods_id",currentGood.getGoods_id());
                IhancHttpClient.get("/index/sale/getUnitApp", params, new AsyncHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                        String res =new String(responseBody).trim();
                        UnitAdapter.UnitFilter mFilter=mUnitAdapter.getFilter();
                        if(res.length()<3){
                            mFilter.filter(currentGood.getGoods_unit());
                        }else
                        mFilter.filter(currentGood.getGoods_unit()+"and"+res.substring(2,res.length()-1));
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                        String res =new String(responseBody).trim();
                        Log.d("unitFail",res);
                    }
                });
                int position=mUnitAdapter.getPosition(Integer.parseInt(currentGood.getGoods_unit()));
                if(position>0)unitSpinner.setSelection(position);
            }
        });
        mGoodSearchView=(SearchView)findViewById(R.id.goodsSearchView);
        mGoodSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
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
        mTextMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                GoodsAdapter.MyFilter mFilter=mGoodsAdapter.getFilter();
                mFilter.setMyFilter(Utils.GOODSFILTERPROMOTE);
                mFilter.filter(":");
            }
        });
        memberTV=(AutoCompleteTextView)findViewById(R.id.memberTV);
        memberAdapter=new MemberAdapter(MainActivity.this,R.layout.member,memberDataList);
        memberTV.setAdapter(memberAdapter);
        memberTV.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                MemberAdapter.MemberFilter memberFilter=memberAdapter.getFilter();
                memberFilter.filter(editable.toString());
            }
        });
        getMemberData();
        memberTV.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                hideKeyboard();
                member memberItem=(member)adapterView.getAdapter().getItem(i);
                memberTV.setText(memberItem.getMember_name());
                memberTV.clearFocus();
                addSaleTabs(memberItem);
                //Log.d("member Click",memberItem.getMember_name());
            }
        });
        memberTV.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if(b) memberTV.setText("");
            }
        });
        vp=(ViewPager)findViewById(R.id.vp);
        tl=(TabLayout)findViewById(R.id.tl);
        mSalePagerAdapter=new SalePagerAdapter(getSupportFragmentManager(),MainActivity.this,fragmentList,memberTabsList);
        vp.setAdapter(mSalePagerAdapter);
        tl.setupWithViewPager(vp);

        mWeight=(MyNumberEdit)findViewById(R.id.weight);
        mPrice=(MyNumberEdit)findViewById(R.id.price);
        mSumEdit=(MyNumberEdit)findViewById(R.id.sumEdit);
        mWeight.setTitle("数量：");
        mPrice.setTitle("价格：");
        mPrice.setD(0.5);
        mSumEdit.setTitle("金额：");
        mSumEdit.setD(1.0);
        mSumEdit.addTextChangedListener(new MyNumberEdit.TextChangedListener() {
            @Override
            public void TextChanged() {
                if(mWeight.getNum()==0.0)
                    return;
                Double sum=mSumEdit.getNum()/mWeight.getNum();
                mPrice.check=false;
                mWeight.check=false;
                mPrice.setNum(sum);
            }
        });
        mPrice.addTextChangedListener(new MyNumberEdit.TextChangedListener() {
            @Override
            public void TextChanged() {
                Double sum=mPrice.getNum()*mWeight.getNum();
                mSumEdit.check=false;
                mSumEdit.setNum(sum);
            }
        });
        mWeight.addTextChangedListener(new MyNumberEdit.TextChangedListener() {
            @Override
            public void TextChanged() {
                Double sum=mPrice.getNum()*mWeight.getNum();
                mSumEdit.check=false;
                mSumEdit.setNum(sum);
            }
        });
        unitSpinner=(Spinner)findViewById(R.id.unitSpinner);
        mUnitAdapter=new UnitAdapter(MainActivity.this,R.layout.unit,mUnitList);
        unitSpinner.setAdapter(mUnitAdapter);
       // getUnitData();
        unitSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                UnitAdapter mAdapter=(UnitAdapter) adapterView.getAdapter();
                Unit unitItem=mAdapter.getItem(i);
                if(String.valueOf(unitItem.getUnit_id()).equals(currentGood.getGoods_unit())){
                    mPrice.setPrice(currentGood.getGoods_price());
                    mSumEdit.setNum(currentGood.getGoods_price()*mWeight.getNum());
                }else{
                    RequestParams params = new RequestParams();
                    params.put("goods_id",currentGood.getGoods_id());
                    params.put("unit_id",unitItem.getUnit_id());
                    IhancHttpClient.get("/index/sale/getPriceApp", params, new AsyncHttpResponseHandler() {
                        @Override
                        public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                            String res=new String(responseBody);
                            mPrice.setPrice(res);
                            mSumEdit.setNum(Double.parseDouble(res)*mWeight.getNum());
                        }
                        @Override
                        public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                        }
                    });
                };
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        addToSaleBtn=(Button)findViewById(R.id.addToSale);
        addToSaleBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("saleFragment","addToSaleBtn");
                SaleFragment mSaleFragment=(SaleFragment) mSalePagerAdapter.getItem(vp.getCurrentItem());
                if(currentGood==null) return;
                if(mSaleFragment!=null) {
                    DetailGoods good = new DetailGoods(currentGood.getGoods_id(), currentGood.getGoods_name(), currentGood.getGoods_unit_id());
                    Unit unit = mUnitAdapter.getItem(unitSpinner.getSelectedItemPosition());
                    SaleDetail detail = new SaleDetail(good, mWeight.getNum(), mPrice.getNum(), unit.getUnit_id(), unit.getUnit_name(),(int)mSumEdit.getNum());
                    Log.d("saleFragment","addToSaleBtn2");
                    mSaleFragment.addSaleDetail(detail);
                }
            }
        });

    }
    public void getCategoryData(){
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
                    }

                } catch (JSONException e)
                {
                    Log.d("JSONArray",e.toString());}
            }
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
    public void getMemberData(){
        RequestParams params = new RequestParams();
        IhancHttpClient.get("/index/sale/memberAll", params, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                String res = new String(responseBody);
                try {
                    JSONArray resArray = new JSONArray(res);
                    for(int i=0;i<resArray.length();i++){
                        JSONObject myjObject = resArray.getJSONObject(i);
                        member mItem=new member(myjObject.getInt("member_id"),
                                myjObject.getString("member_name"),
                                myjObject.getString("member_sn")
                        );
                        memberDataList.add(mItem);
                    }

                } catch (JSONException e)
                {
                    Log.d("JSONArray",e.toString());}
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {

            }
        });

    }
    public void onBackPressed() {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_MAIN);// "android.intent.action.MAIN"
        intent.addCategory(Intent.CATEGORY_HOME); //"android.intent.category.HOME"
        startActivity(intent);
    }
    public void addSaleTabs(member memberItem){
        hideKeyboard();
        memberTV.setText("");
        if(memberTabsList.size()>5){
            Toast.makeText(MainActivity.this,"排列中的客户不能超过5个！",Toast.LENGTH_LONG).show();
            return;
        }
        for(int i=0;i<memberTabsList.size();i++){
            if(memberItem.getMember_id()==memberTabsList.get(i).getMember_id()) {
                vp.setCurrentItem(i);
                return;
            }
        }
          memberTabsList.add(memberItem);
          SaleFragment mFragment=new SaleFragment();
          mFragment.setMember(memberItem);
          fragmentList.add(mFragment);
          mSalePagerAdapter.notifyDataSetChanged();
          vp.setCurrentItem(memberTabsList.size()-1);
    }
    public void deleteCurrentSaleTabs(){
        int position=vp.getCurrentItem();
        memberTabsList.remove(position);
        fragmentList.remove(position);
        mSalePagerAdapter.notifyDataSetChanged();
    }
    public void initPrinter(Object bitmap){
        String receiptType=sp.getString(getString(R.string.receipt_type),null);
        if(sp.getBoolean("bluetooth_printer",false)){
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
        final String receiptType=sp.getString(getString(R.string.receipt_type),null);
        RequestParams params = new RequestParams();
        params.put("member_id",Utils.printMemberName.getMember_id());
        IhancHttpClient.get("/index/sale/creditDetail", params, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                String res = new String(responseBody);
                try{
                    JSONObject myjObject=new JSONObject(res);
                    printCreditJSON=(JSONArray) myjObject.get("credit");
                    if(receiptType.equals(getString(R.string.receipt_type_default)))
                        initPrinter(printCreditJSON);
                    else{
                         showProgress(true);
                         ImageTask mImageTask = new ImageTask();
                         mImageTask.execute("");
                    }
                   // System.out.print(printCreditJSON);


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
            initPrinter(bitmap);

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
                foot.add(new StringBitmapParameter("地址："+(i+1)+Utils.mCompanyInfo.getAddress()[0]+"\n"));
            }
        }else{
            foot.add(new StringBitmapParameter("地址："+Utils.mCompanyInfo.getAddress()[0]+"\n"));
        }
        Bitmap bitmapTitle=BitmapUtil.StringListtoBitmap(MainActivity.this,title);
        Bitmap bitmapFoot=BitmapUtil.StringListtoBitmap(MainActivity.this,foot);
        Bitmap bitmapBody=BitmapUtil.StringListtoBitmap(MainActivity.this,printCreditJSON);
        Bitmap mergeBitmap = BitmapUtil.addBitmapInHead(bitmapTitle, bitmapBody);
        mergeBitmap=BitmapUtil.addBitmapInHead(mergeBitmap,bitmapFoot);
        return mergeBitmap;
    }
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
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
                    if(storeArray.length()>1){
                        final String[] stores=new String[storeArray.length()];
                        final int[] storeId=new int[storeArray.length()];
                       for (int i=0;i< storeArray.length();i++){
                           JSONObject myjObject = storeArray.getJSONObject(i);
                           stores[i]=myjObject.getString("store_name");
                           storeId[i]=myjObject.getInt("store_id");
                       }
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
    Handler eHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            exit = false;
        }
    };

    public void initPrinter(ArrayList<SaleDetail> saleDetails,int paid_sum,int credit_sum){
        Log.d("GPrinter","initPrinter");
        String receiptType=sp.getString(getString(R.string.receipt_type),null);
        if(sp.getBoolean("bluetooth_printer",false)){
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
                    mGPrinter.print(saleDetails,paid_sum,credit_sum);
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
                mGPrinter.print(saleDetails,paid_sum,credit_sum);
         //   else
          //      mGPrinter.print((Bitmap) bitmap);
        }
    }


}
