package com.example.hx.ihanc;

import android.content.Context;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.SearchView;
import android.widget.TextView;

import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Console;
import java.util.ArrayList;
import java.util.List;

import cz.msebera.android.httpclient.Header;

public class LogActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private LogRecyclerViewAdapter adapter;
    private ArrayList<LogItem> LogLists=new ArrayList<LogItem>();
    private SwipeRefreshLayout swipeRefreshLayout;
    private TextView textView;
    private String search="";
    private int page=1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log);
        recyclerView=findViewById(R.id.logListRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter=new LogRecyclerViewAdapter(LogLists);
        recyclerView.setAdapter(adapter);
        swipeRefreshLayout=findViewById(R.id.swipeRefreshLayout);
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
                        getLogList();
                    }
                }
            }
        });
        getLogList();
        final SearchView searchView=findViewById(R.id.search);
        int id=searchView.getContext().getResources().getIdentifier("android:id/search_src_text",null,null);
        textView=(TextView) searchView.findViewById(id);
        textView.setTextSize(12);
        Button button=findViewById(R.id.search_button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hideKeyboard();
                search=textView.getText().toString();
                page=1;
                getLogList();
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
                    getLogList();
                    recyclerView.scrollToPosition(0);
                }
                return false;
            }
        });
    }

    private void getLogList(){
        //Log.d("logDetail","getLogList");
        RequestParams params=new RequestParams();
        params.put("page",page);
        params.put("search",search);
        params.put("edate","");
        params.put("sdate","");
        IhancHttpClient.get("/index/setting/log", params, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                String res=new String(responseBody);
                if(page==1){LogLists.clear();adapter.isLast=false;}
                swipeRefreshLayout.setRefreshing(false);
                try{
                    JSONObject resJson=new JSONObject(res);
                    JSONArray list=resJson.getJSONArray("details");
                    int size=resJson.getInt("total_count");
                    if (size-list.length()-LogLists.size()<=0) adapter.isLast=true;
                    for (int i = 0; i <list.length() ; i++) {
                        JSONObject itemJSON=list.getJSONObject(i);
                        LogItem item=new LogItem(itemJSON.getString("user"),itemJSON.getString("time"),itemJSON.getString("remark"));
                        LogLists.add(item);
                    }
                    page++;
                    adapter.notifyDataSetChanged();
                    recyclerView.setNestedScrollingEnabled(false);
                }catch (JSONException e){e.printStackTrace();}
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {

            }
        });

    }

    public void hideKeyboard() {
        textView.clearFocus();
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm.isActive() && getCurrentFocus() != null) {
            if (getCurrentFocus().getWindowToken() != null) {
                imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
            }
        }
    }
}
