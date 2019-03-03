package com.example.hx.ihanc.store;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.hx.ihanc.IhancHttpClient;
import com.example.hx.ihanc.R;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import cz.msebera.android.httpclient.Header;

public class DialogStockDetail extends DialogFragment {
    private ArrayList<StockDetail> data=new ArrayList<StockDetail>();
    private StockDetailListAdapter adapter;
    private int page=1;
    private View view;
    private Context context;
    private RecyclerView recyclerView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private stock stock;
    private int store_id;
    public static DialogStockDetail newInstance(String goods_name,int store_id){
        DialogStockDetail f=new DialogStockDetail();
        Bundle b=new Bundle();
        b.putString("goods_name",goods_name);
        b.putInt("store_id",store_id);
        f.setArguments(b);
        return f;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String res;
        res=getArguments().getString("goods_name");
        store_id=getArguments().getInt("store_id");
        try{
            JSONObject obj=new JSONObject(res);
            stock=new stock(
                    obj.getInt("stock_id"),
                    obj.getInt("goods_id"),
                    obj.getString("goods_name"),
                    obj.getString("inorder"),
                    obj.getDouble("number"),
                    obj.getInt("sum"),
                    obj.getInt("unit_id"),
                    obj.getString("unit_name")

            );
        }catch (JSONException e){e.printStackTrace();}
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view=inflater.inflate(R.layout.dialog_stock_detail, container, false);
        context=view.getContext();
        recyclerView = (RecyclerView) view.findViewById(R.id.stockDetailListRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        adapter=new StockDetailListAdapter(data);
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
        return view;
    }

    private void initData(){
        swipeRefreshLayout.setRefreshing(true);
        if(page==1) data.clear();
        try {
            final JSONObject params=new JSONObject();
            params.put("page",page);
            params.put("search","");
            JSONObject where=new JSONObject();
            where.put("goods_id",stock.goods_id);
            where.put("store_id",store_id);
            where.put("inorder",stock.inorder);
            params.put("where",where);
            IhancHttpClient.postJson(context, "/index/stock/stockDetail", params, new AsyncHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                    String res=new String(responseBody);
                    swipeRefreshLayout.setRefreshing(false);
                    try {
                        JSONObject rsOBJ=new JSONObject(res);
                        JSONArray list=rsOBJ.getJSONArray("stockDetail");
                        int total_count=rsOBJ.getInt("total_count");
                        if (list.length()+ (page-1)*10>=total_count) {
                            adapter.isLast=true;
                        }else adapter.isLast=false;
                        for (int i = 0; i <list.length() ; i++) {
                            JSONObject itemJSON = list.getJSONObject(i);
                            StockDetail item=new StockDetail(
                                    itemJSON.getString("time"),
                                    itemJSON.getString("user"),
                                    itemJSON.getDouble("Dstock"),
                                    itemJSON.getInt("Dsum"),
                                    itemJSON.getDouble("stock"),
                                    itemJSON.getInt("sum"),
                                    itemJSON.getString("remark"),
                                    itemJSON.getString("type"));
                            data.add(item);
                        }
                        page++;
                        adapter.notifyDataSetChanged();
                        recyclerView.setNestedScrollingEnabled(true);
                    }catch (JSONException e){e.printStackTrace();}

                }

                @Override
                public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {

                }
            });

        }catch (JSONException e){e.printStackTrace();}
    }
}
