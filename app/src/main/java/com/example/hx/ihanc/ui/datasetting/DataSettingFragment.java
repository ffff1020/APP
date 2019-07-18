package com.example.hx.ihanc.ui.datasetting;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.hx.ihanc.AddMemberDialog;
import com.example.hx.ihanc.IhancHttpClient;
import com.example.hx.ihanc.MemberAdapter;
import com.example.hx.ihanc.R;
import com.example.hx.ihanc.creditFragment;
import com.example.hx.ihanc.member;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Member;
import java.util.ArrayList;
import java.util.List;

import cz.msebera.android.httpclient.Header;

public class DataSettingFragment extends Fragment {

    private DataSettingViewModel mViewModel;
    private View view;
    private AddMemberDialog dialog;
    private member currentMember;
    private List memberDataList=new ArrayList<member>();
    private MemberAdapter memberAdapter;
    private AutoCompleteTextView memberTV;
    private EditText sumET;
    private Button saveButton;
    private EditText summaryET;

    public static DataSettingFragment newInstance() {
        return new DataSettingFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        getMemberData();
        view=inflater.inflate(R.layout.data_setting_fragment, container, false);
        AddMemberDialog.OnAddMemberSucceed onAddMemberSucceed=new AddMemberDialog.OnAddMemberSucceed() {
            @Override
            public void AddMemberSucceed() {
                dialog.dismiss();
                getMemberData();
            }
        };
        dialog=AddMemberDialog.newInstance(onAddMemberSucceed);
        Button addMember=(Button)view.findViewById(R.id.addMember);
        addMember.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.show(getFragmentManager(),"addMember");
            }
        });
        memberTV=(AutoCompleteTextView)view.findViewById(R.id.memberTV);
        memberAdapter=new MemberAdapter(getContext(),R.layout.member,memberDataList);
        memberTV.setAdapter(memberAdapter);
        memberTV.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                currentMember=(member)adapterView.getAdapter().getItem(i);
                memberTV.setText(currentMember.getMember_name());
                memberTV.clearFocus();
                sumET.setFocusable(true);
                sumET.setFocusableInTouchMode(true);
                sumET.requestFocus();
            }
        });
        sumET=(EditText)view.findViewById(R.id.sum);
        saveButton=(Button)view.findViewById(R.id.saveButton);
        summaryET=((EditText)view.findViewById(R.id.summary));
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                JSONObject params=new JSONObject();
                try {
                    params.put("member_id",currentMember.getMember_id());
                    params.put("sum",sumET.getText().toString().trim());
                    params.put("summary",summaryET.getText().toString().trim());
                    IhancHttpClient.postJson(getContext(), "/index/setting/mCreditAdd", params, new AsyncHttpResponseHandler() {
                        @Override
                        public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                            String str=new String(responseBody);
                            try {
                                JSONObject res=new JSONObject(str);
                                if(res.getInt("result")==1){
                                    Toast.makeText(getContext(),"保存成功！",Toast.LENGTH_LONG).show();
                                    currentMember=null;
                                    memberTV.setText("");
                                    sumET.setText("");
                                    summaryET.setText("");
                                }
                            }catch (JSONException e){e.printStackTrace();}
                        }

                        @Override
                        public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {

                        }
                    });

                }catch (JSONException e){e.printStackTrace();}
            }
        });
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = ViewModelProviders.of(this).get(DataSettingViewModel.class);
        // TODO: Use the ViewModel
    }
    public void getMemberData(){
        memberDataList.clear();
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
                                myjObject.getString("member_sn"),
                                myjObject.getString("member_phone")
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
    public void hideKeyboard() {
        view.clearFocus();
        InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm.isActive() && getActivity().getCurrentFocus() != null) {
            if (getActivity().getCurrentFocus().getWindowToken() != null) {
                imm.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
            }
        }
    }



}
