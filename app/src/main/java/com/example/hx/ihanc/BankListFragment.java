package com.example.hx.ihanc;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.SearchView;
import android.widget.TableLayout;
import android.widget.TextView;


import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import cz.msebera.android.httpclient.Header;

import static android.content.Context.MODE_PRIVATE;

/**
 * A fragment representing a list of Items.
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnListFragmentInteractionListener}
 * interface.
 */
public class BankListFragment extends Fragment {

    // TODO: Customize parameter argument names
    private static final String ARG_COLUMN_COUNT = "column-count";
    // TODO: Customize parameters
    private int mColumnCount = 1;
    private OnListFragmentInteractionListener mListener;
    private SwipeRefreshLayout swipeRefreshLayout;
    private int page;
    private TableLayout bankTableLayout;
    private Context context;
    private String name;
    private List<BankListItem> bankListItemList=new ArrayList<BankListItem>();
    private MyBankListRecyclerViewAdapter adpter;
    private RecyclerView recyclerView;
    private View view;
    private TextView textView;
    private String search="";

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public BankListFragment() {
    }

    // TODO: Customize parameter initialization
    @SuppressWarnings("unused")
    public static BankListFragment newInstance(int columnCount) {
        BankListFragment fragment = new BankListFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_COLUMN_COUNT, columnCount);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mColumnCount = getArguments().getInt(ARG_COLUMN_COUNT);
        }
        adpter=new MyBankListRecyclerViewAdapter(bankListItemList, mListener);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.fragment_banklist_list, container, false);
        context = view.getContext();
        recyclerView=view.findViewById(R.id.bankListRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        recyclerView.setAdapter(adpter);
        swipeRefreshLayout=view.findViewById(R.id.swipeRefreshLayout);
        swipeRefreshLayout.setRefreshing(true);
        bankTableLayout=view.findViewById(R.id.bankTable);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                page=1;
                getBankTable();
                getBankList();
            }
        });
        initData();
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
                        getBankList();

                    }
                }
            }
        });
        final SearchView searchView=view.findViewById(R.id.search);
        int id=searchView.getContext().getResources().getIdentifier("android:id/search_src_text",null,null);
        textView=(TextView) searchView.findViewById(id);
        textView.setTextSize(12);
        Button button=view.findViewById(R.id.search_button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hideKeyboard();
                search=textView.getText().toString();
                page=1;
                getBankList();
            }
        });
        textView.setOnEditorActionListener(new TextView.OnEditorActionListener(){
            @Override
            public boolean onEditorAction(TextView v, int actionId,
                                          KeyEvent event) {
                hideKeyboard();
                if ( actionId == 0 ) {
                    // Log.d("searchView",actionId+":");
                    page=1;
                    search=textView.getText().toString();
                    getBankList();
                    recyclerView.scrollToPosition(0);
                }
                return false;
            }
        });


        return view;
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

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
    public interface OnListFragmentInteractionListener {
        // TODO: Update argument type and name
        void onListFragmentInteraction(SaleListItem item);
    }

   public void getBankTable(){
        int size=bankTableLayout.getChildCount();
       for (int i=1;i<size;i++){
           bankTableLayout.removeViewAt(1);
           Log.d("banklist",bankTableLayout.getChildCount()+"");
       }
       Date currentTime = new Date();
       SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
       String dateString = formatter.format(currentTime);
       RequestParams params=new RequestParams();
       params.put("date",dateString);
       params.put("page",1);
       params.put("user",name);
       IhancHttpClient.get("/index/statistics/cashToday", params, new AsyncHttpResponseHandler() {
           @Override
           public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                String res=new String(responseBody);
                try {
                    JSONObject resJson=new JSONObject(res);
                    JSONArray list=resJson.getJSONArray("lists");
                    NumberFormat format=NumberFormat.getCurrencyInstance(Locale.CHINA);
                    for (int i=0 ;i<list.length();i++){
                        JSONObject item=list.getJSONObject(i);
                        View layout = LayoutInflater.from(context).inflate(R.layout.bank_table,null);
                        TextView bankName = (TextView) layout.findViewById(R.id.bankName);
                        TextView bankSum = (TextView) layout.findViewById(R.id.bankSum);
                        bankName.setText(item.getString("bank_name")+"--"+item.getString("user"));
                        bankSum.setText(format.format(item.getInt("sum")));
                        bankTableLayout.addView(layout);
                    }
                }catch (JSONException e){e.printStackTrace();}
           }
           @Override
           public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
           }
       });
   }

   public void getBankList(){
       RequestParams params=new RequestParams();
       params.put("page",page);
       params.put("user",name);
       params.put("search",search);
       IhancHttpClient.get("/index/finance/bankDetailSearch", params, new AsyncHttpResponseHandler() {
           @Override
           public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
               if(page==1){bankListItemList.clear();adpter.isLast=false;}
               String res=new String(responseBody);
               swipeRefreshLayout.setRefreshing(false);
               try {
                   JSONObject resJson=new JSONObject(res);
                   JSONArray list=resJson.getJSONArray("lists");
                   int size=resJson.getInt("total_count");
                   if (size-list.length()-bankListItemList.size()<=0) adpter.isLast=true;
                   NumberFormat format=NumberFormat.getCurrencyInstance(Locale.CHINA);
                   for (int i = 0; i <list.length() ; i++) {
                       JSONObject itemJSON=list.getJSONObject(i);
                       BankListItem item=new BankListItem(
                               itemJSON.getString("time"),
                               itemJSON.getString("bank_name")+"--"+itemJSON.getString("user"),
                               format.format(itemJSON.getInt("sum")),
                               itemJSON.getString("summary"));
                       bankListItemList.add(item);
                   }
                   page++;
                   adpter.notifyDataSetChanged();
                   recyclerView.setNestedScrollingEnabled(false);
               }catch (JSONException e){e.printStackTrace();}
           }

           @Override
           public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {

           }
       });
   }

   public void initData(){
       page=1;
       SharedPreferences sp=context.getSharedPreferences("pref_ihanc", MODE_PRIVATE);
       name=sp.getString("name","");
       name=name.substring(1,name.length()-1);
       getBankTable();
       getBankList();
   }

    public void hideKeyboard() {
        view.clearFocus();
        InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        MainActivity parentActivity=(MainActivity)getActivity();
        if (imm.isActive() && parentActivity.getCurrentFocus() != null) {
            if (parentActivity.getCurrentFocus().getWindowToken() != null) {
                imm.hideSoftInputFromWindow(parentActivity.getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
            }
        }
    }
}
