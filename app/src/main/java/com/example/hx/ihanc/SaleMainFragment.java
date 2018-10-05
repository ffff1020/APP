package com.example.hx.ihanc;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
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
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import cz.msebera.android.httpclient.Header;

import static android.hardware.usb.UsbManager.ACTION_USB_DEVICE_DETACHED;
import static com.example.hx.ihanc.Constant.ACTION_USB_PERMISSION;
import static com.example.hx.ihanc.DeviceConnFactoryManager.ACTION_QUERY_PRINTER_STATE;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link SaleMainFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link SaleMainFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SaleMainFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    private Context mContext;
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
    private static final String TAG="SaleMainFragment";
    // Intent request codes
    private static final int REQUEST_CONNECT_DEVICE = 1;
    private static final int REQUEST_ENABLE_BT = 2;
    private List mCategoryDataList=new ArrayList<category>();
    private List mGoodsDataList=new ArrayList<Goods>();
    private List memberDataList=new ArrayList<member>();
    private List<SaleFragment> fragmentList=new ArrayList<SaleFragment>();
    private List<SaleFragment> secondFragmentList=new ArrayList<SaleFragment>();
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
    private Button addToSaleBtn;
    private View view;
    private MainActivity parentActivity ;
    private boolean  first = false;
    public SaleMainFragment() {

        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment SaleMainFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static SaleMainFragment newInstance(String param1, String param2) {
        SaleMainFragment fragment = new SaleMainFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
        parentActivity = (MainActivity ) getActivity();
        Log.d(TAG,"onCreate");

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view=inflater.inflate(R.layout.fragment_sale_main, container, false);
        Log.d("saleMainFragment","onCreateView"+fragmentList.size());
        initView();
        return view;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext=context;
        if(getArguments()==null){
            getMemberData();
            getCategoryData();
            getGoodsData();
            return;
        } ;
        FragmentTransaction fragmentTransaction = parentActivity.getSupportFragmentManager().beginTransaction();
       // Log.d("saleMainFragment","onAttach"+fragmentList.size());
        first=getArguments().getBoolean("first");
        for (int i=0;i<memberTabsList.size();i++){
            SaleFragment fragment =SaleFragment.newInstance(getArguments().getBundle(memberTabsList.get(i).getMember_name()));
            fragment.setMember(memberTabsList.get(i));
            //fragmentTransaction.add(fragment,"");
            fragmentList.add(fragment);
        }
        Log.d("saleMainFragment","onAttach"+fragmentList.size());
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }

    public void initView(){
        Log.d("saleMainFragment","initView");
        mSalePagerAdapter=null;
        infoGoodsName=(TextView)view.findViewById(R.id.infoGoodsName);
        mTextMessage = (TextView) view.findViewById(R.id.goodsTV);
        sp= PreferenceManager.getDefaultSharedPreferences(mContext);
        categoryLv=(ListView)view.findViewById(R.id.category);
        mCategoryAdapter=new CategoryAdapter(mContext,R.layout.device_name,mCategoryDataList);
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
        mCategoryAdapter.notifyDataSetChanged();
        mGridView=(GridView)view.findViewById(R.id.goods);
        mGoodsAdapter=new GoodsAdapter(mContext,R.layout.goods,mGoodsDataList);
        mGridView.setAdapter(mGoodsAdapter);

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
        mGoodSearchView=(SearchView)view.findViewById(R.id.goodsSearchView);
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
        memberTV=(AutoCompleteTextView)view.findViewById(R.id.memberTV);
        memberAdapter=new MemberAdapter(mContext,R.layout.member,memberDataList);
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
        vp = (ViewPager) view.findViewById(R.id.vp);
        tl = (TabLayout) view.findViewById(R.id.tl);
        if(!first) {
            mSalePagerAdapter = new SalePagerAdapter(parentActivity.getSupportFragmentManager(), mContext, fragmentList, memberTabsList);
            vp.setAdapter(mSalePagerAdapter);
            tl.setupWithViewPager(vp);
        }else{
           //mSalePagerAdapter.notifyDataSetChanged();
            mSalePagerAdapter = new SalePagerAdapter(parentActivity.getSupportFragmentManager(), mContext, fragmentList, memberTabsList);
            vp.setAdapter(mSalePagerAdapter);
            tl.setupWithViewPager(vp);
        }

        mWeight=(MyNumberEdit)view.findViewById(R.id.weight);
        mPrice=(MyNumberEdit)view.findViewById(R.id.price);
        mSumEdit=(MyNumberEdit)view.findViewById(R.id.sumEdit);
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
        unitSpinner=(Spinner)view.findViewById(R.id.unitSpinner);
        mUnitAdapter=new UnitAdapter(mContext,R.layout.unit,MainActivity.mUnitList);
        unitSpinner.setAdapter(mUnitAdapter);

        // getUnitData();
        unitSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                UnitAdapter mAdapter=(UnitAdapter) adapterView.getAdapter();
                Unit unitItem=mAdapter.getItem(i);
                if(currentGood==null) return;
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
                            if(res.length()<3){
                                mSumEdit.setNum(0.0);
                            } else {
                                res=res.substring(1,res.length()-1);
                                mSumEdit.setNum(Double.valueOf(res.trim()) * mWeight.getNum());
                            }
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
        addToSaleBtn=(Button)view.findViewById(R.id.addToSale);
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
        Log.d(TAG,"getCategoryData");
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
    public void addSaleTabs(member memberItem){
        hideKeyboard();
        memberTV.setText("");
        if(memberTabsList.size()>5){
            Toast.makeText(mContext,"排列中的客户不能超过5个！",Toast.LENGTH_LONG).show();
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
    public void hideKeyboard() {

        InputMethodManager imm = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm.isActive() && parentActivity.getCurrentFocus() != null) {
            if (parentActivity.getCurrentFocus().getWindowToken() != null) {
                imm.hideSoftInputFromWindow(parentActivity.getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
            }
        }
    }
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Bundle args = new Bundle();
        args.putBoolean("first",true);
        FragmentTransaction fragmentTransaction = parentActivity.getSupportFragmentManager().beginTransaction();
        //int size=fragmentList.size();
        Log.d("saleMainFragment","onDestroyView"+fragmentList.size()+","+memberTabsList.size());
        Bundle b;
        for(int i=0;i<memberTabsList.size();i++){
            SaleFragment mf = fragmentList.get(i);
            b=new Bundle();
            b.putBundle("saleFragment", mf.saveState());
            b.putString("credit", mf.getCreditBundle());
          //  b.putInt("show",mf.getShow());
            b.putInt("ttl", mf.getTTL());
            b.putInt("credit_sum",mf.getCredit_sum() );
            args.putBundle(memberTabsList.get(i).getMember_name(),b);
            fragmentTransaction.remove(fragmentList.get(i));
            //fragmentList.remove(i);
        }
        fragmentList.clear();
        fragmentTransaction.commit();
        setArguments(args);
        Log.d("saleMainFragment","onDestroyView"+fragmentList.size()+"");
    }
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

    }

}
