package com.example.hx.ihanc.purchase;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.SearchView;
import android.widget.TextView;

import com.example.hx.ihanc.IhancHttpClient;
import com.example.hx.ihanc.R;
import com.example.hx.ihanc.SaleListItem;
import com.example.hx.ihanc.Utils;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import cz.msebera.android.httpclient.Header;

public class PurchaseListFragment extends Fragment {
    private View view;
    private RecyclerView recyclerView;
    private PurchaseListAdapter adapter;
    private ArrayList<SaleListItem> data=new ArrayList<SaleListItem>();
    private SwipeRefreshLayout swipeRefreshLayout;
    private int mPage=1;
    private boolean check=true;
    private TextView textView;
    private String search="";
    private PurchaseDetailDialog dialog=null;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.d("purchaseList","onCreateView");
        view = inflater.inflate(R.layout.fragment_salelist_list, container, false);
        Context context = view.getContext();
        recyclerView = (RecyclerView) view.findViewById(R.id.saleListRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(context,LinearLayoutManager.VERTICAL,false));
        adapter=new PurchaseListAdapter(data);
        adapter.setOnPurchaseListener(new PurchaseListAdapter.OnPurchaseListFragmentListener() {
            @Override
            public void OnPurchaseListFragmentListener(SaleListItem item) {
                if(dialog!=null&&dialog.getDialog()!=null&&dialog.getDialog().isShowing()) return;
                dialog=PurchaseDetailDialog.newInstance(item.sale_id,item.name,item.member_id,item.sum,item.paid);
                dialog.setListener(new PurchaseDetailDialog.OnPurchaseDetailUpdated() {
                    @Override
                    public void OnPurchaseDetailUpdated() {
                        mPage=1;getData();
                    }
                });
                dialog.show(getFragmentManager(),"purchaseDetailDialog");
            }
        });
        recyclerView.setAdapter(adapter);
        swipeRefreshLayout=view.findViewById(R.id.swipeRefreshLayout);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mPage=1;
                getData();
            }
        });

        SearchView mSearch=view.findViewById(R.id.search);
        int id=mSearch.getContext().getResources().getIdentifier("android:id/search_src_text",null,null);
        textView=(TextView) mSearch.findViewById(id);
        textView.setTextSize(12);
        textView.setOnEditorActionListener(new TextView.OnEditorActionListener(){
            @Override
            public boolean onEditorAction(TextView v, int actionId,
                                          KeyEvent event) {
                if ( actionId == 0 ) {
                    // Log.d("searchView",actionId+":");
                    Utils.hideKeyboard(view,getActivity());
                    mPage=1;
                    search=textView.getText().toString();
                    getData();
                    recyclerView.scrollToPosition(0);
                }
                return false;
            }
        });
        Button button=view.findViewById(R.id.search_button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Utils.hideKeyboard(view,getActivity());
                mPage=1;
                search=textView.getText().toString();
                getData();
                recyclerView.scrollToPosition(0);
                view.clearFocus();
            }
        });

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if(!check) return;
                if (newState==RecyclerView.SCROLL_STATE_IDLE){
                    int lastVisiblePosition;
                    RecyclerView.LayoutManager layoutManager=recyclerView.getLayoutManager();
                    lastVisiblePosition= ((LinearLayoutManager) layoutManager).findLastVisibleItemPosition();
                    if (layoutManager.getChildCount()>0             //当当前显示的item数量>0
                            &&lastVisiblePosition>=layoutManager.getItemCount()-1           //当当前屏幕最后一个加载项位置>=所有item的数量
                            &&layoutManager.getItemCount()>layoutManager.getChildCount()) { // 当当前总Item数大于可见Item数
                        //recyclerView.scrollToPosition(adpter.getItemCount()-1);
                        swipeRefreshLayout.setRefreshing(true);
                        recyclerView.setNestedScrollingEnabled(false);
                        getData();
                    }
                }
            }
        });
        return view;
    }


    @Override
    public void onResume() {
        super.onResume();
        Log.d("purchaseList","onResume");
        swipeRefreshLayout.setRefreshing(true);
        getData();
    }

    private void getData(){
        if(!check) return;
        swipeRefreshLayout.setRefreshing(true);
        check=false;
        RequestParams params=new RequestParams();
        params.put("page",mPage);
        params.put("edate","");
        params.put("sdate","");
        params.put("search",search);
        IhancHttpClient.get("/index/purchase/purchaseList", params, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                if(mPage==1){data.clear();adapter.isLast=false;}
                String res=new String(responseBody);
                swipeRefreshLayout.setRefreshing(false);
                try {
                    JSONObject resJson=new JSONObject(res);
                    JSONArray lists=resJson.getJSONArray("lists");
                    int size=resJson.getInt("total_count");
                    if (size-lists.length()-data.size()<=0) adapter.isLast=true;
                    for (int i = 0; i <lists.length() ; i++) {
                        JSONObject list=lists.getJSONObject(i);
                        //Log.d("saleListFragment",list.getString("member_name"));
                        SaleListItem item=new SaleListItem(
                                list.getInt("purchase_id"),
                                list.getString("time")+"  批次："+list.getString("inorder"),
                                list.getString("supply_name"),
                                list.getString("sum"),
                                list.getInt("finish"),
                                list.getInt("supply_id")
                        );
                        data.add(item);
                    }
                    mPage++;
                    adapter.notifyDataSetChanged();
                    recyclerView.setNestedScrollingEnabled(true);
                    check=true;
                }catch (JSONException e){
                    e.printStackTrace();
                }

            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                String res=new String(responseBody);
                Log.d("purchaseListFragment",res);
            }
        });

    }
}
