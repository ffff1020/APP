package com.example.hx.ihanc;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.KeyEvent;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.SearchView;
import android.util.Log;
import android.util.TypedValue;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.TextView;

import com.google.gson.JsonArray;
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

/**
 * A fragment representing a list of Items.
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnListFragmentInteractionListener}
 * interface.
 */
public class saleListFragment extends Fragment {

    // TODO: Customize parameter argument names
    private static final String TYPE_ORDER = "order";
    // TODO: Customize parameters
    private boolean order = false;
    private OnListFragmentInteractionListener mListener;
    private MainActivity parentActivity;
    private Context mContext;
    private MysaleListRecyclerViewAdapter adpter;
    private List<SaleListItem> saleListItemList=new ArrayList<SaleListItem>();
    private SwipeRefreshLayout swipeRefreshLayout;
    private int mPage=1;
    private  RecyclerView recyclerView;
    private SaleDetailDialog.printListener mPrintListener=null;
    private TextView textView;
    private String search="";
    private View view;
    private boolean check=true;
    private int paid_sum1=0;
    private int credit1=0;
    private ArrayList<SaleDetail> mdetail1=new ArrayList<SaleDetail>();
    private SaleDetailDialog dialog=null;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public saleListFragment() {
    }

    // TODO: Customize parameter initialization
    @SuppressWarnings("unused")
    public static saleListFragment newInstance(boolean order) {
        saleListFragment fragment = new saleListFragment();
        Bundle args = new Bundle();
        args.putBoolean(TYPE_ORDER, order);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
       // Log.d("saleList","start");
        if (getArguments() != null) {
            order = getArguments().getBoolean(TYPE_ORDER,false);
        }
        this.mPrintListener=new SaleDetailDialog.printListener() {
            @Override
            public void print(ArrayList<SaleDetail> mdetail ,int paid_sum,int credit,String time) {
                SharedPreferences sp=PreferenceManager.getDefaultSharedPreferences(getActivity());
                final String receiptType=sp.getString(getString(R.string.receipt_type),getString(R.string.receipt_type_default));
                if(receiptType.equals(getString(R.string.receipt_type_default))) {
                    Utils.saleTypeOrder=order;
                    parentActivity.initPrinter(mdetail, paid_sum * (-1), credit - paid_sum, time);
                }else{
                     parentActivity.showProgress(true);
                     paid_sum1=paid_sum;
                     credit1=credit;
                     mdetail1=mdetail;
                     ImageTask mImageTask = new ImageTask();
                     mImageTask.execute("");
                }
            }

            @Override
            public void fresh() {
                mPage=1;
                getData();
            }
        };
        this.mListener=new OnListFragmentInteractionListener() {
            @Override
            public void onListFragmentInteraction(SaleListItem item) {
                if (dialog!=null&&dialog.getDialog()!=null&&dialog.getDialog().isShowing()) return;
                dialog =SaleDetailDialog.newInstance(item.sale_id,item.name,Integer.parseInt(item.sum),item.member_id,item.paid,item.time);
                dialog.remark=item.remark;
                if(order)dialog.setType(order);
                dialog.setListener(mPrintListener);
                dialog.setonFreshList(new SaleDetailDialog.onFreshList() {
                    @Override
                    public void freshList() {
                        hideKeyboard();
                        mPage=1;
                        search=textView.getText().toString();
                        getData();
                        recyclerView.scrollToPosition(0);
                        view.clearFocus();
                    }
                });
                dialog.show(getFragmentManager(),"sale_detail");
            }
        };
        adpter=new MysaleListRecyclerViewAdapter(saleListItemList, mListener);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if(!Utils.role&&!Utils.auth.containsKey("sale_list")){
            Utils.toast(getContext(),"sorry啊，您没有查看销售列表的权限！");
            return null;
        }
        view = inflater.inflate(R.layout.fragment_salelist_list, container, false);
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
        SearchView mSearch=view.findViewById(R.id.search);
        int id=mSearch.getContext().getResources().getIdentifier("android:id/search_src_text",null,null);
        textView=(TextView) mSearch.findViewById(id);
        textView.setTextSize(12);
        textView.setOnEditorActionListener(new TextView.OnEditorActionListener(){
            @Override
            public boolean onEditorAction(TextView v, int actionId,
                                          KeyEvent event) {
                if ( actionId == 0 ) {
                    hideKeyboard();
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
                hideKeyboard();
                mPage=1;
                search=textView.getText().toString();
                getData();
                recyclerView.scrollToPosition(0);
                view.clearFocus();
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

    public  void getData(){
        swipeRefreshLayout.setRefreshing(true);
        check=false;
        RequestParams params=new RequestParams();
        params.put("page",mPage);
        params.put("edate","");
        params.put("sdate","");
        params.put("search",search);
        params.put("user","");
        if(order) params.put("type","ORDER");
        IhancHttpClient.get("/index/sale/saleList", params, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                if(mPage==1){saleListItemList.clear();adpter.isLast=false;}
                String res=new String(responseBody);
                swipeRefreshLayout.setRefreshing(false);
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
                                list.getString("time")+"--"+list.getString("user"),
                                list.getString("member_name"),
                                list.getString("sum"),
                                order?0:list.getInt("finish"),
                                list.getInt("member_id")
                                );
                        item.remark=list.getString("summary");
                        saleListItemList.add(item);
                    }
                    mPage++;
                    adpter.notifyDataSetChanged();
                    recyclerView.setNestedScrollingEnabled(true);
                    check=true;
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

    public void hideKeyboard() {
        view.clearFocus();
        InputMethodManager imm = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm.isActive() && parentActivity.getCurrentFocus() != null) {
            if (parentActivity.getCurrentFocus().getWindowToken() != null) {
                imm.hideSoftInputFromWindow(parentActivity.getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
            }
        }
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
            ((MainActivity ) getActivity()).showProgress(false);
            super.onPostExecute(bitmap);
            ((MainActivity ) getActivity()).initPrinter(bitmap,credit1);
        }
    }

    private Bitmap creatCreditImage(){
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        ArrayList<StringBitmapParameter> title = new ArrayList<>();
        title.add(new StringBitmapParameter(Utils.mCompanyInfo.getName(),BitmapUtil.IS_CENTER,BitmapUtil.IS_LARGE));
        String type=order?"订货单":"销售单\n";
        title.add(new StringBitmapParameter(type,BitmapUtil.IS_CENTER));
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
        Bitmap bitmapTitle=BitmapUtil.StringListtoBitmap(mContext,title);
        Bitmap bitmapFoot=BitmapUtil.StringListtoBitmap(mContext,foot);
        Bitmap bitmapBody=BitmapUtil.StringListtoBitmap(mContext,mdetail1,paid_sum1 * (-1), credit1 - paid_sum1);
        Bitmap mergeBitmap = BitmapUtil.addBitmapInHead(bitmapTitle, bitmapBody);
        mergeBitmap=BitmapUtil.addBitmapInHead(mergeBitmap,bitmapFoot);
        return mergeBitmap;
    }
}
