package com.example.hx.ihanc.purchase;


import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.database.DataSetObserver;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;

import com.example.hx.ihanc.BankAdapter;
import com.example.hx.ihanc.BankListItem;
import com.example.hx.ihanc.Goods;
import com.example.hx.ihanc.IhancHttpClient;
import com.example.hx.ihanc.MainActivity;
import com.example.hx.ihanc.R;
import com.example.hx.ihanc.UnitAdapter;
import com.example.hx.ihanc.Utils;
import com.google.android.gms.plus.PlusOneButton;
import com.loopj.android.http.AsyncHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import cz.msebera.android.httpclient.Header;


public class PurchaseFragment extends Fragment {
    public static final ArrayList<Supply> supply=new ArrayList<Supply>();
    private AutoCompleteTextView supplyTV;
    private SupplyAdapter supplyAdapter;
    private View view;
    private Context mContext;
    private MainActivity parentActivity;
    private Supply currentSupply;
    private List<String> inorder = new ArrayList<String>(); ;
    private InorderAdapter inorderAdapter;
    private Spinner inorderSpinner;
    private RecyclerView recyclerView;
    private PurchaseDetailAdapter purchaseDetailAdapter;
    private Button addSupplyBtn;
    private Spinner bankSpinner;
    private Button saveBtn;
    private EditText paid_sum;
    public static final ArrayList<PurchaseDetail> purchaseDetails=new ArrayList<PurchaseDetail>();

    public PurchaseFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if(!Utils.role && !Utils.auth.containsKey("purchase")){
            Utils.toast(getContext(),"Sorry啊，您没有进货的权限！");
            return null;
        }
        getSupplyData();
        parentActivity = (MainActivity ) getActivity();
        view = inflater.inflate(R.layout.fragment_purchase, container, false);
        mContext=view.getContext();
        supplyTV=(AutoCompleteTextView)view.findViewById(R.id.supply);
        supplyAdapter=new SupplyAdapter(getContext(),R.layout.member,supply);
        supplyTV.setAdapter(supplyAdapter);
        supplyTV.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }
            @Override
            public void afterTextChanged(Editable editable) {
                SupplyAdapter.SupplyFilter supplyFilter=supplyAdapter.getFilter();
                supplyFilter.filter(editable.toString());
            }
        });
        supplyTV.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                hideKeyboard();
                currentSupply=((Supply)adapterView.getAdapter().getItem(i));
                supplyTV.setText(currentSupply.supply_name);
            }
        });
        inorderSpinner=view.findViewById(R.id.inorder);
        inorderAdapter=new InorderAdapter(mContext, R.layout.unit, inorder);
        inorderSpinner.setAdapter(inorderAdapter);
        getInOrder();
        recyclerView = (RecyclerView) view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(mContext));
        purchaseDetailAdapter=new PurchaseDetailAdapter(purchaseDetails);
        purchaseDetailAdapter.setPurchaseDetailListener(getListener());
        recyclerView.setAdapter(purchaseDetailAdapter);
        addSupplyBtn=view.findViewById(R.id.addSupply);
        addSupplyBtn.setOnClickListener(addSupply());
        bankSpinner=view.findViewById(R.id.bankSpinner);
        BankAdapter bankAdapter=new BankAdapter(getContext(),R.layout.unit,MainActivity.mBankList);
        bankSpinner.setAdapter(bankAdapter);
        saveBtn=view.findViewById(R.id.saveButton);
        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                save();
            }
        });
        paid_sum=view.findViewById(R.id.paid_sum);
        paid_sum.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                if (i == EditorInfo.IME_ACTION_DONE) {
                    save();
                    return true;
                }
                return false;
            }
        });
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    private  void getSupplyData(){
        if(supply.size()==0){
            IhancHttpClient.get("/index/purchase/getSupplyAll", null, new AsyncHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                    String res=new String(responseBody);
                    try{
                        JSONArray result=new JSONArray(res);
                        for (int i = 0; i <result.length() ; i++) {
                            JSONObject obj=result.getJSONObject(i);
                            supply.add(new Supply(obj.getInt("supply_id"),obj.getString("supply_name"),obj.getString("supply_sn")));
                        }
                    }catch (JSONException e){e.printStackTrace();}
                    supplyAdapter.notifyDataSetChanged();
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {

                }
            });
        }
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
    private void getInOrder(){
        inorder.clear();
        IhancHttpClient.get("/index/purchase/getInOrder", null, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                try {
                    JSONArray res=new JSONArray(new String(responseBody));
                    for (int i = 0; i <res.length() ; i++) {
                        inorder.add(res.getJSONObject(i).getString("inorder"));
                    }
                    inorderAdapter.notifyDataSetChanged();
                    inorderSpinner.setSelection(0);
                }catch (JSONException e){e.printStackTrace();}
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
               // Log.d("supply",responseBody.toString());
            }
        });

    }
    private PurchaseDetailAdapter.PurchaseDetailListener getListener(){
       PurchaseDetailAdapter.PurchaseDetailListener listener;
       listener=new PurchaseDetailAdapter.PurchaseDetailListener() {
           @Override
           public void purchaseAdd(int position) {
               if (currentSupply==null) {
                   Utils.toast(getContext(),"请输入供应商！");return;}
               DialogPurchaseDetail dialog=DialogPurchaseDetail.NewInstance(position,currentSupply.supply_id);
               dialog.setOnUpdatePurchaseDetail(setOnUpdatePurchaseDetail());
               dialog.show(getFragmentManager(),"DialogPurchaseDetail");
           }
       };
       return listener;
    }
    private DialogPurchaseDetail.onUpdatePurchaseDetail setOnUpdatePurchaseDetail(){
        return new DialogPurchaseDetail.onUpdatePurchaseDetail() {
            @Override
            public void updatePurchaseDetail(int position, PurchaseDetail detail) {
                hideKeyboard();
                if(position==-1) {
                    purchaseDetails.add(detail);
                    purchaseDetailAdapter.ttl_number+=detail.number;
                    purchaseDetailAdapter.ttl_sum+=detail.sum;
                }else if(detail==null){
                    PurchaseDetail oldDetail=purchaseDetails.get(position);
                   purchaseDetails.remove(position);
                    purchaseDetailAdapter.ttl_number-=oldDetail.number;
                    purchaseDetailAdapter.ttl_sum-=oldDetail.sum;
                }else{
                    PurchaseDetail oldDetail=purchaseDetails.get(position);
                    purchaseDetails.set(position,detail);
                    purchaseDetailAdapter.ttl_number+=detail.number-oldDetail.number;
                    purchaseDetailAdapter.ttl_sum+=detail.sum-oldDetail.sum;
                }
                purchaseDetailAdapter.notifyDataSetChanged();
            }
        };
    }
    private boolean restoreStateFromArguments(){
        Bundle b = getArguments();
        if(b==null) {return false;}
        String purchaseDetail=b.getString("purchaseDetail");
        if (purchaseDetail==null||purchaseDetail.length()==0)return false;
        try {
            JSONArray jsonArray = new JSONArray(purchaseDetail);
            for (int i = 0; i <jsonArray.length() ; i++) {
                JSONObject obj=jsonArray.getJSONObject(i);
                PurchaseDetail detail=new PurchaseDetail(
                        obj.getInt("purchase_id") ,
                        Goods.toGoods(obj.getJSONObject("goods")),
                        obj.getInt("unit_id"),
                        obj.getString("unit_name"),
                        obj.getDouble("price"),
                        obj.getDouble("number"),
                        obj.getInt("sum"),
                        obj.getInt("purchase_detail_id"),
                        obj.getString("store_name"),
                        obj.getInt("store_id")
                );
                purchaseDetails.add(detail);
                purchaseDetailAdapter.ttl_sum=b.getInt("ttl_sum");
                purchaseDetailAdapter.ttl_number=b.getDouble("ttl_number");
                currentSupply=new Supply(b.getInt("supply_id"),b.getString("supply_name"),"");
                supplyTV.setFocusable(false);
                supplyTV.setText(b.getString("supply_name"));
                supplyTV.setFocusable(true);
            }
        }catch (JSONException e){e.printStackTrace();}
        return false;
    }
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if(!restoreStateFromArguments()){}
    }
    public static PurchaseFragment newInstance(Bundle args){
        PurchaseFragment f=new PurchaseFragment();
        f.setArguments(args);
        return f;
    }
    public Bundle getBundle(){
        Bundle b=new Bundle();
        if(purchaseDetails.size()>0) {
            b.putInt("ttl_sum", purchaseDetailAdapter.ttl_sum);
            b.putDouble("ttl_number", purchaseDetailAdapter.ttl_number);
            JSONArray jsonArray = new JSONArray();
            for (PurchaseDetail detail : purchaseDetails) {
                JSONObject obj = new JSONObject();
                try {
                    obj.put("purchase_id", detail.purchase_id);
                    obj.put("unit_id", detail.unit_id);
                    obj.put("unit_name", detail.unit_name);
                    obj.put("price", detail.price);
                    obj.put("number", detail.number);
                    obj.put("sum", detail.sum);
                    obj.put("purchase_detail_id", detail.purchase_detail_id);
                    obj.put("store_name", detail.store_name);
                    obj.put("store_id", detail.store_id);
                    obj.put("goods", detail.goods.toJSONObject());
                    jsonArray.put(obj);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            b.putString("purchaseDetail", jsonArray.toString());
            b.putInt("supply_id",currentSupply.supply_id);
            b.putString("supply_name",currentSupply.supply_name);
        }
        return b;
    }
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        purchaseDetails.clear();
        purchaseDetailAdapter.ttl_sum=0;
        purchaseDetailAdapter.ttl_number=0.0;
    }
    private View.OnClickListener addSupply(){
        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                 DialogAddSupply dialogAddSupply=DialogAddSupply.newInstance(new DialogAddSupply.OnAddSupplySucceed() {
                     @Override
                     public void AddSupplySucceed() {
                         hideKeyboard();
                         supply.clear();
                         getSupplyData();
                     }
                 });
                 dialogAddSupply.show(getFragmentManager(),"dialogSupply");
            }
        };
    }
    private void save(){
        Utils.showProgress(getFragmentManager(),true);
        JSONObject params=new JSONObject();
        try{
            JSONObject data=new JSONObject();
            data.put("ttl_sum",purchaseDetailAdapter.ttl_sum);
            data.put("supply",currentSupply.toJSONObject());
            params.put("data",data);
            JSONObject pay=new JSONObject();
            int position=bankSpinner.getSelectedItemPosition();
            pay.put("bank", MainActivity.mBankList.get(position).getBank());
            pay.put("paid_sum",paid_sum.getText());
            params.put("pay",pay);
            params.put("back",1);
            position=inorderSpinner.getSelectedItemPosition();
            params.put("inorder",inorder.get(position));
            JSONArray detail=new JSONArray();
            for (int i = 0; i <purchaseDetails.size() ; i++) {
                detail.put(purchaseDetails.get(i).toJSONObject());
            }
            params.put("detail",detail);
            Log.d("purchase",params.toString());
            IhancHttpClient.postJson(getContext(), "/index/purchase/purchase", params, new AsyncHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                    Utils.showProgress(getFragmentManager(),false);
                    String result=new String(responseBody);
                    try{
                        JSONObject obj=new JSONObject(result);
                        if (obj.getInt("result")==1){
                            Utils.toast(getContext(),"保存成功！");
                            purchaseDetails.clear();
                            purchaseDetailAdapter.ttl_sum=0;
                            purchaseDetailAdapter.ttl_number=0.0;
                            supplyTV.setText("");
                            paid_sum.setText("");
                            getInOrder();
                        }else{
                            Utils.toast(getContext(),"保存失败，请联系管理员！");
                        }
                    }catch (JSONException e){e.printStackTrace();}
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {

                }
            });

        }catch (JSONException e){
            e.printStackTrace();
        }
    }

}
