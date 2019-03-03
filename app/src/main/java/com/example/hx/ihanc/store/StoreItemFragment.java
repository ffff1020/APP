package com.example.hx.ihanc.store;

import android.app.Activity;
import android.arch.lifecycle.Lifecycle;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
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
import android.widget.Toast;

import com.example.hx.ihanc.Credit;
import com.example.hx.ihanc.IhancHttpClient;
import com.example.hx.ihanc.R;
import com.example.hx.ihanc.Utils;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.SaxAsyncHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import cz.msebera.android.httpclient.Header;

public class StoreItemFragment extends Fragment {
    private int store_id;
    private int page=1;
    private Context context;
    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView recyclerView;
    private ArrayList<stock> data =new ArrayList<stock>();
    private StockListAdapter adapter;
    private String search="";
    private TextView textView;
    private String[] mStringsItem;
    private Spinner orderSpinner;
    private String order="";
    private View view;
    private int position;
    private String store_name;
    public static StoreItemFragment newInstance(int store_id,String store_name){
        StoreItemFragment f=new StoreItemFragment();
        Bundle args = new Bundle();
        args.putInt("store_id", store_id);
        args.putString("store_name",store_name);
        f.setArguments(args);
        return f;
    };

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.store_id=getArguments().getInt("store_id");
        this.store_name=getArguments().getString("store_name");
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view=inflater.inflate(R.layout.fragment_store_item, container, false);
        final SearchView searchView=view.findViewById(R.id.search);
        int id=searchView.getContext().getResources().getIdentifier("android:id/search_src_text",null,null);
        textView=(TextView) searchView.findViewById(id);
        textView.setTextSize(12);
        context = view.getContext();
        recyclerView = (RecyclerView) view.findViewById(R.id.stockListRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        adapter=new StockListAdapter(data);
        adapter.setItemClickListener(new StockListAdapter.OnStockItemClickListener() {
            @Override
            public void onItemClick(View view) {
                position=(int)view.getTag();
                DialogMenu dialogMenu=DialogMenu.newInstance(data.get(position).toString(),store_id);
                dialogMenu.setOnSelectActions(getDialogListener());
                dialogMenu.show(getFragmentManager(),"storeItem");
            }
        });
        recyclerView.setAdapter(adapter);
        swipeRefreshLayout=view.findViewById(R.id.swipeRefreshLayout);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                page=1;
                initData();
                recyclerView.scrollToPosition(0);
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
                        initData();
                    }
                }
            }
        });

        Button SearchBtn=view.findViewById(R.id.search_button);
        SearchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hideKeyboard();
                search=textView.getText().toString().trim();
                page=1;
                initData();

            }
        });
        orderSpinner=view.findViewById(R.id.orderSpinner);
        mStringsItem = getResources().getStringArray(R.array.stockOrder);
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<String>(getContext(),
                R.layout.unit, mStringsItem);
        spinnerAdapter
                .setDropDownViewResource(R.layout.unit);
        orderSpinner.setAdapter(spinnerAdapter);
        orderSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                order=getOrder(i);
                page=1;
                search=textView.getText().toString().trim();
                initData();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        return view;
    }

    public void initData(){
        swipeRefreshLayout.setRefreshing(true);
        if(page==1) data.clear();
        RequestParams params=new RequestParams();
        params.put("inorder",2);
        params.put("orderby",order);
        params.put("page",page);
        params.put("search",search);
        params.put("store_id",store_id);
        System.out.println(params);
        IhancHttpClient.get("/index/stock/stockList", params, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                String res=new String(responseBody);
                swipeRefreshLayout.setRefreshing(false);
                try {
                    JSONObject resJson = new JSONObject(res);
                    JSONArray list = resJson.getJSONArray("stock");
                    int size=resJson.getInt("total_count");
                    if (list.length()+ (page-1)*10>=size) {
                        adapter.isLast=true;
                    }else adapter.isLast=false;
                    for (int i = 0; i <list.length() ; i++) {
                        JSONObject itemJSON=list.getJSONObject(i);
                        stock item=new stock(
                                itemJSON.getInt("stock_id"),
                                itemJSON.getInt("goods_id"),
                                itemJSON.getString("goods_name"),
                                itemJSON.getString("inorder"),
                                itemJSON.getDouble("number"),
                                itemJSON.getInt("sum"),
                                itemJSON.getInt("unit_id"),
                                itemJSON.getString("unit_name"));
                        data.add(item);
                    }
                    page++;
                    adapter.notifyDataSetChanged();
                    recyclerView.setNestedScrollingEnabled(true);
                }catch (JSONException e){e.printStackTrace();}
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                Log.d("stock",new String(responseBody));
            }
        });
    }

    public String getOrder(int position){
        String order="";
        switch (position){
            case 1:
                order= "number";
                break;
            case 2:
                order= "number desc";
                break;
            case 3:
                order= "sum";
                break;
            case 4:
                order= "sum desc";
                break;
        }
        return order;
    }

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

    private DialogMenu.OnSelectActions getDialogListener(){
        return new DialogMenu.OnSelectActions() {
            @Override
            public void OnSelectUpdate(boolean loss) {
               DialogUpdateStock dialogUpdateStock=DialogUpdateStock.newInstance(loss,data.get(position).goods_name);
               dialogUpdateStock.setOnUpDateStock(OnUpdateStockCallBack());
               dialogUpdateStock.show(getFragmentManager(),"dialogUpdateStock");
            }

            @Override
            public void OnTransfer() {

            }

            @Override
            public void OnExchange() {

            }

            @Override
            public void OnDelete() {
                final AlertDialog.Builder alertDialogBuilder=new AlertDialog.Builder(getContext());
                alertDialogBuilder.setTitle(data.get(position).goods_name);
                alertDialogBuilder.setMessage("确定删除该库存？");
                alertDialogBuilder.setCancelable(false);
                alertDialogBuilder.setNegativeButton(R.string.negative_button, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                    }
                });
                alertDialogBuilder.setPositiveButton("确定删除", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        stock stock=data.get(position);
                        RequestParams params=new RequestParams();
                        params.put("goods_id",stock.goods_id);
                        params.put("store_id",store_id);
                        params.put("inorder",stock.inorder);
                        String info="删除商品发生"+(stock.number>0?"报损:":"报溢:");
                        info+=store_name+"的"+stock.goods_name+",";
                        info+="数量："+stock.number+stock.unit_name+",金额："+stock.sum;
                        params.put("info",info);
                        IhancHttpClient.get("/index/stock/delStock", params, new AsyncHttpResponseHandler() {
                            @Override
                            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                                String res=new String(responseBody);
                                try{
                                    JSONObject obj=new JSONObject(res);
                                    if(obj.getInt("result")==1){
                                        Utils.toast(getContext(),"删除库存成功!");
                                    }else
                                    {
                                        Utils.toast(getContext(),"删除库存失败，请重试！");
                                    }
                                    page=1;
                                    initData();
                                }catch (JSONException e){e.printStackTrace();}
                            }

                            @Override
                            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {

                            }
                        });
                    }
                });
                alertDialogBuilder.create().show();
            }

            @Override
            public void OnCarry() {

            }

            @Override
            public void OnDetail() {
                DialogStockDetail dialogStockDetail=DialogStockDetail.newInstance(data.get(position).toString(),store_id);
                dialogStockDetail.show(getFragmentManager(),"dialogStockDetail");
            }
        };
    }
    private DialogUpdateStock.OnUpdateStock OnUpdateStockCallBack(){
        return new DialogUpdateStock.OnUpdateStock() {
            @Override
            public void OnUpdateStock(double number) {
                try {
                    JSONObject obj = new JSONObject();
                    obj.put("stock", data.get(position).toJson());
                    obj.put("Dstock",number);
                    obj.put("store_id",store_id);
                    Log.d("stock",obj.toString());
                    IhancHttpClient.postJson(getContext(), "/index/stock/updateStock", obj, new AsyncHttpResponseHandler() {
                        @Override
                        public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                            String res=new String(responseBody);
                            Log.d("stock",res);
                            try{
                                JSONObject result=new JSONObject(res);
                                if(result.getInt("result")==1){
                                    Utils.toast(getContext(),"保存成功！");
                                    page=1;
                                    initData();
                                }
                            }catch (JSONException e){e.printStackTrace();}
                        }

                        @Override
                        public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                            String res=new String(responseBody);
                            Log.d("stock",res);
                        }
                    });
                }catch (JSONException e){e.printStackTrace();}
            }
        };
    }
}
