package com.example.hx.ihanc.purchase;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.SearchView;
import android.widget.Spinner;
import android.widget.TextView;

import com.example.hx.ihanc.Credit;
import com.example.hx.ihanc.CreditDetailDialog;
import com.example.hx.ihanc.IhancHttpClient;
import com.example.hx.ihanc.MycreditRecyclerViewAdapter;
import com.example.hx.ihanc.R;
import com.example.hx.ihanc.Utils;
import com.example.hx.ihanc.creditFragment;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import cz.msebera.android.httpclient.Header;

public class PurchaseCreditFragment extends Fragment {
    private SwipeRefreshLayout swipeRefreshLayout;
    private Context context;
    private List<Credit> mCredits=new ArrayList<Credit>();
    private int page=1;
    private MycreditRecyclerViewAdapter adpter;
    private RecyclerView recyclerView;
    private TextView textView;
    private String search="";
    private View view;
    private String order="time";
    private PurchaseCreditDetailDialog creditDetailDialog=null;
    private TextView ttlCredit;
    private creditFragment.OnListFragmentInteractionListener mListener;

    public PurchaseCreditFragment(){
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Log.d("credit","onCreate");
        mListener=new creditFragment.OnListFragmentInteractionListener() {
            @Override
            public void onListFragmentInteraction(Credit item) {
                //Log.d("purchaseCredit",item.getName()+item.getMemberId());
                if(creditDetailDialog!=null&&creditDetailDialog.getDialog()!=null&&creditDetailDialog.getDialog().isShowing()) return;
                creditDetailDialog=PurchaseCreditDetailDialog.newInstance(item.getMemberId(),item.getName()+"--"+item.getCreditSum());
                CreditDetailDialog.OnFreshCredit onFreshCredit=new CreditDetailDialog.OnFreshCredit() {
                    @Override
                    public void freshCredit() {
                        page=1;initCreditData();
                    }
                };
                creditDetailDialog.setOnFreshCredit(onFreshCredit);
                creditDetailDialog.show(getFragmentManager(),"credit");
            }
        };
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if(!Utils.role && !Utils.auth.containsKey("scredit")){
            Utils.toast(getContext(),"Sorry啊，您没有查看应付款的权限！");
            return null;
        }
        view = inflater.inflate(R.layout.fragment_credit_list, container, false);
        context = view.getContext();
        recyclerView = (RecyclerView) view.findViewById(R.id.creditListRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        adpter=new MycreditRecyclerViewAdapter(mCredits, mListener);
        recyclerView.setAdapter(adpter);
        ttlCredit=view.findViewById(R.id.ttlCredit);
        swipeRefreshLayout=view.findViewById(R.id.swipeRefreshLayout);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                page=1;
                initCreditData();
                recyclerView.scrollToPosition(0);
            }
        });
        swipeRefreshLayout.setRefreshing(true);
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (newState==RecyclerView.SCROLL_STATE_IDLE){
                    int lastVisiblePosition;
                    RecyclerView.LayoutManager layoutManager=recyclerView.getLayoutManager();
                    lastVisiblePosition= ((LinearLayoutManager) layoutManager).findLastVisibleItemPosition();
                    if (layoutManager.getChildCount()>0             //当当前显示的item数量>0
                            &&lastVisiblePosition>=layoutManager.getItemCount()-1           //当当前屏幕最后一个加载项位置>=所有item的数量
                            &&layoutManager.getItemCount()>layoutManager.getChildCount()) { // 当当前总Item数大于可见Item数
                        swipeRefreshLayout.setRefreshing(true);
                        recyclerView.setNestedScrollingEnabled(false);
                        initCreditData();
                    }
                }
            }
        });
        final SearchView searchView=view.findViewById(R.id.search);
        int id=searchView.getContext().getResources().getIdentifier("android:id/search_src_text",null,null);
        textView=(TextView) searchView.findViewById(id);
        textView.setTextSize(12);
        searchView.setQueryHint("输入供应商进行搜索");
        Button button=view.findViewById(R.id.search_button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hideKeyboard();
                search=textView.getText().toString();
                page=1;
                initCreditData();
            }
        });
        textView.setOnEditorActionListener(new TextView.OnEditorActionListener(){
            @Override
            public boolean onEditorAction(TextView v, int actionId,
                                          KeyEvent event) {
                hideKeyboard();
                if ( actionId == 0 ) {
                    page=1;
                    search=textView.getText().toString();
                    initCreditData();
                    recyclerView.scrollToPosition(0);
                }
                return false;
            }
        });
        Spinner orderSpinner=view.findViewById(R.id.orderSpinner);
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<String>(getContext(),
                R.layout.unit, getResources().getStringArray(R.array.creditOrder));
        spinnerAdapter
                .setDropDownViewResource(R.layout.unit);
        orderSpinner.setAdapter(spinnerAdapter);
        orderSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                order=getOrder(i);
                page=1;
                search=textView.getText().toString();
                initCreditData();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        //initCreditData();
        return view;
    }

    public void initCreditData(){
        Log.d("purchaseCredit","initCreditData");
        RequestParams params=new RequestParams();
        params.put("page",page);
        params.put("search",search);
        params.put("order",order);
        IhancHttpClient.get("/index/purchase/credit", params, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                if( page== 1){ mCredits.clear(); }
                String res=new String(responseBody);
                swipeRefreshLayout.setRefreshing(false);
                try {
                    JSONObject resJson=new JSONObject(res);
                    JSONArray list=resJson.getJSONArray("credit");
                    int size=resJson.getInt("total_count");
                    if ((list.length()+(page-1)*10)>=size) {
                        adpter.isLast=true;
                    }else adpter.isLast=false;
                    // Log.d("credit","size:"+size+"list.l:"+list.length()+"mCredit:"+mCredits.size());
                    for (int i = 0; i <list.length() ; i++) {
                        JSONObject itemJSON=list.getJSONObject(i);
                        Credit item=new Credit(
                                itemJSON.getString("supply_name"),itemJSON.getInt("sum"),itemJSON.getInt("supply_id"));
                        mCredits.add(item);
                    }
                    page++;
                    adpter.notifyDataSetChanged();
                    recyclerView.setNestedScrollingEnabled(true);
                }catch (JSONException e){e.printStackTrace();}
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {

            }
        });
        IhancHttpClient.get("/index/purchase/ttlCredit", null, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                String rs=new String(responseBody);
                rs=rs.substring(1,rs.length());
                int res=Integer.parseInt(rs);
                NumberFormat f=NumberFormat.getCurrencyInstance(Locale.CHINA);
                ttlCredit.setText("合计应付金额："+f.format(res));
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {

            }
        });

    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        // Log.d("credit","start");
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
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */

    public void hideKeyboard() {
        view.clearFocus();
        InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        Activity parentActivity=(Activity)getActivity();
        if (imm.isActive() && parentActivity.getCurrentFocus() != null) {
            if (parentActivity.getCurrentFocus().getWindowToken() != null) {
                imm.hideSoftInputFromWindow(parentActivity.getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
            }
        }
    }

    public String getOrder(int position){
        String order="time";
        switch (position){
            case 1:
                order="sum desc";
                break;
            case 2:
                order="time desc";
                break;
            case 3:
                order="sum";
                break;
        }
        return order;
    }
}
