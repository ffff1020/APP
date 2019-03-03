package com.example.hx.ihanc;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpResponseHandler;

import org.json.JSONException;
import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;

public class UpDateCategoryDialog extends DialogFragment {
    private int cat_id;
    private String cat_name;
    private OnUpdateCategorySuccess mListener=null;
    public static UpDateCategoryDialog newInstance(int cat_id,String cat_name) {
        Bundle args = new Bundle();
        args.putString("cat_name",cat_name);
        args.putInt("cat_id",cat_id);
        UpDateCategoryDialog fragment = new UpDateCategoryDialog();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        this.cat_id=getArguments().getInt("cat_id");
        this.cat_name=getArguments().getString("cat_name");
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.dialog_update_category, container, false);
        final EditText cat_nameET=v.findViewById(R.id.cat_nameET);
        cat_nameET.setText(cat_name);
        Button saveBtn=v.findViewById(R.id.saveButton);
        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean check=false;
                for (int i = 0; i <GoodsSettingActivity.mCategoryDataList.size() ; i++) {
                    category item=(category) GoodsSettingActivity.mCategoryDataList.get(i);
                    if(item.getCategory_name().trim().equals(cat_nameET.getText().toString().trim())){
                        check=true;
                        break;
                    }
                }
                if(check){
                    Log.d("updateCat","");
                    Toast.makeText(getContext(),"该种类名称已经存在！",Toast.LENGTH_LONG).show();
                }else {
                    if (cat_id > 0) {
                        JSONObject params = new JSONObject();
                        try {
                            params.put("cat_name", cat_nameET.getText().toString());
                            params.put("cat_id", cat_id);
                            IhancHttpClient.postJson(getContext(), "/catUpdate", params, new AsyncHttpResponseHandler() {
                                @Override
                                public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                                    String res = new String(responseBody);
                                    try {
                                        JSONObject obj = new JSONObject(res);
                                        if (obj.getInt("result") == 1) {
                                            Toast.makeText(getContext(), "种类修改成功！", Toast.LENGTH_LONG).show();
                                            if (mListener != null)
                                                mListener.UpdateCategorySuccess();
                                        }
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }

                                @Override
                                public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {

                                }
                            });
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    } else {
                        JSONObject params = new JSONObject();
                        try {
                            params.put("cat_name", cat_nameET.getText().toString());
                            IhancHttpClient.postJson(getContext(), "/catCreate", params, new AsyncHttpResponseHandler() {
                                @Override
                                public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                                    String res = new String(responseBody);
                                    try {
                                        JSONObject obj = new JSONObject(res);
                                        if (obj.getInt("result") == 1) {
                                            Toast.makeText(getContext(), "种类修改成功！", Toast.LENGTH_LONG).show();
                                            if (mListener != null)
                                                mListener.UpdateCategorySuccess();
                                        }
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }

                                @Override
                                public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {

                                }
                            });
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        });
        return v;
    }

    public void setOnUpdateCategorySuccessListener(OnUpdateCategorySuccess mListener) {
        this.mListener = mListener;
    }

    public interface OnUpdateCategorySuccess{
        void UpdateCategorySuccess();
    }
}
