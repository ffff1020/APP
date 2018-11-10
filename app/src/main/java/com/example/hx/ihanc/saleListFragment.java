package com.example.hx.ihanc;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import com.google.gson.JsonArray;
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

/**
 * A fragment representing a list of Items.
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnListFragmentInteractionListener}
 * interface.
 */
public class saleListFragment extends Fragment {

    // TODO: Customize parameter argument names
    private static final String ARG_COLUMN_COUNT = "column-count";
    // TODO: Customize parameters
    private int mColumnCount = 1;
    private OnListFragmentInteractionListener mListener;
    private MainActivity parentActivity;
    private Context mContext;
    private MysaleListRecyclerViewAdapter adpter;
    private List<SaleListItem> saleListItemList=new ArrayList<SaleListItem>();
    private SwipeRefreshLayout swipeRefreshLayout;
    private int mPage=1;
    private  RecyclerView recyclerView;
    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public saleListFragment() {
    }

    // TODO: Customize parameter initialization
    @SuppressWarnings("unused")
    public static saleListFragment newInstance(int columnCount) {
        saleListFragment fragment = new saleListFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_COLUMN_COUNT, columnCount);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("saleList","start");
        if (getArguments() != null) {
            mColumnCount = getArguments().getInt(ARG_COLUMN_COUNT);
        }

        this.mListener=new OnListFragmentInteractionListener() {
            @Override
            public void onListFragmentInteraction(SaleListItem item) {
                NumberFormat format=NumberFormat.getCurrencyInstance(Locale.CHINA);
                SaleDetailDialog dialog =new SaleDetailDialog(getContext(),item.sale_id,item.name+"--"+format.format(Integer.parseInt(item.sum)));
                dialog.show();
                // 将对话框的大小按屏幕大小的百分比设置
                WindowManager windowManager = parentActivity.getWindowManager();
                Display display = windowManager.getDefaultDisplay();
                WindowManager.LayoutParams lp = dialog.getWindow().getAttributes();
                lp.width = (int)(display.getWidth() * 0.95); //设置宽度
                WindowManager.LayoutParams dialogLP=dialog.getWindow().getAttributes();
                if ((int)(display.getHeight() * 0.8)<dialogLP.height){
                    lp.height = (int)(display.getHeight() * 0.8);
                }
                dialog.getWindow().setAttributes(lp);
            }
        };
        adpter=new MysaleListRecyclerViewAdapter(saleListItemList, mListener);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_salelist_list, container, false);
        Context context = view.getContext();
        recyclerView = (RecyclerView) view.findViewById(R.id.saleListRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(context,LinearLayoutManager.VERTICAL,false));
        recyclerView.setAdapter(adpter);

        swipeRefreshLayout=view.findViewById(R.id.swipeRefreshLayout);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mPage=1;
                getData();
            }
        });
        swipeRefreshLayout.setRefreshing(true);
        getData();
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
                        getData();
                    }
                }
            }
        });


        return view;
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext=context;
        parentActivity=(MainActivity ) getActivity();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onStart() {
        super.onStart();

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

    public void getData(){
        RequestParams params=new RequestParams();
        params.put("page",mPage);
        params.put("edate","");
        params.put("sdate","");
        params.put("search","");
        params.put("user","");
        IhancHttpClient.get("/index/sale/saleList", params, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                if(mPage==1){saleListItemList.clear();adpter.isLast=false;}
                String res=new String(responseBody);
                swipeRefreshLayout.setRefreshing(false);
                //Log.d("saleListFragment",res);
                try {
                    JSONObject resJson=new JSONObject(res);
                    JSONArray lists=resJson.getJSONArray("lists");
                    int size=resJson.getInt("total_count");
                    if (size-lists.length()-saleListItemList.size()<=0) adpter.isLast=true;
                    for (int i = 0; i <lists.length() ; i++) {
                        JSONObject list=lists.getJSONObject(i);
                        //Log.d("saleListFragment",list.getString("member_name"));
                        SaleListItem item=new SaleListItem(
                                list.getInt("sale_id"),
                                list.getString("time"),
                                list.getString("member_name"),
                                list.getString("sum"),
                                list.getInt("finish")
                                );
                        saleListItemList.add(item);
                    }
                    mPage++;
                    adpter.notifyDataSetChanged();
                    recyclerView.setNestedScrollingEnabled(true);
                }catch (JSONException e){
                    e.printStackTrace();
                }

            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                String res=new String(responseBody);
                Log.d("saleListFragment",res);
            }
        });
    }
}
