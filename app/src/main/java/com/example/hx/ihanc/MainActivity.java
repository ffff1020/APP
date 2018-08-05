package com.example.hx.ihanc;

import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
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
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SearchView;
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

import java.util.ArrayList;
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
    private List<SaleFragment> fragmentList=new ArrayList<>();
    private List<member> memberTabsList=new ArrayList<>();
    private ViewPager vp;
    private TabLayout tl;

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    //mTextMessage.setText(R.string.title_home);
                    return true;
                case R.id.navigation_dashboard:
                    Intent intent=new Intent(MainActivity.this,SettingsActivity.class);
                    startActivity(intent);
                    return true;
                case R.id.navigation_notifications:
                   // mTextMessage.setText(R.string.title_notifications);

                    return true;
            }
            return false;
        }
    };
    @Override
    protected void onStart(){
        super.onStart();
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
    }
    @Override
    protected void onStop() {
        super.onStop();
        if(mGPrinter!=null){unregisterReceiver(mGPrinter.receiver);}
       // unregisterReceiver(receiver);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        DeviceConnFactoryManager.closeAllPort();
    }

    public void initView(){
        mTextMessage = (TextView) findViewById(R.id.goodsTV);
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
    public void initPrinter(){
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
                mGPrinter.print("韩川");
            }
        }else{
            String IP=sp.getString(getString(R.string.printer_IP),"").trim();
            IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
            filter.addAction(ACTION_USB_DEVICE_DETACHED);
            filter.addAction(ACTION_QUERY_PRINTER_STATE);
            filter.addAction(DeviceConnFactoryManager.ACTION_CONN_STATE);
            mGPrinter=new GPrinter(IP,false);
            registerReceiver(mGPrinter.receiver, filter);
            mGPrinter.print("SAMPLE");
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

}
