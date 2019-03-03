package com.example.hx.ihanc.purchase;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import com.example.hx.ihanc.IhancHttpClient;
import com.example.hx.ihanc.R;
import com.example.hx.ihanc.Utils;
import com.loopj.android.http.AsyncHttpResponseHandler;

import net.sourceforge.pinyin4j.PinyinHelper;
import net.sourceforge.pinyin4j.format.HanyuPinyinCaseType;
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType;

import org.json.JSONException;
import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;

public class DialogAddSupply extends DialogFragment {
    private View view;
    public static StringBuffer sb = new StringBuffer();
    private EditText supplyNameEdit;
    private EditText supplySnEdit;
    private EditText supplyTelEdit;
    private DialogAddSupply.OnAddSupplySucceed onAddSupplySucceed;
    private Button saveBtn;

    public static DialogAddSupply newInstance(DialogAddSupply.OnAddSupplySucceed onAddSupplySucceed){
        DialogAddSupply dialog=new DialogAddSupply();
        dialog.onAddSupplySucceed=onAddSupplySucceed;
        return dialog;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.dialog_add_supply, container, false);
        initView();
        return view;
    }

    public interface OnAddSupplySucceed{
        void AddSupplySucceed();
    }

    private void initView(){
        supplyNameEdit=view.findViewById(R.id.supply_name);
        supplySnEdit=view.findViewById(R.id.supply_sn);
        supplyTelEdit=view.findViewById(R.id.supply_tel);
        supplyNameEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if(!TextUtils.isEmpty(supplyNameEdit.getText())){
                    supplySnEdit.setText(getPinYinHeadChar(supplyNameEdit.getText().toString()));
                }
            }
        });
        saveBtn=view.findViewById(R.id.saveButton);

        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(TextUtils.isEmpty(supplyNameEdit.getText())) return;
                JSONObject params=new JSONObject();
                saveBtn.setClickable(false);
                saveBtn.setText("保存中...");
                try {
                    params.put("supply_id","");
                    params.put("supply_name", supplyNameEdit.getText().toString());
                    params.put("supply_sn", supplySnEdit.getText().toString());
                    params.put("supply_phone", supplyTelEdit.getText().toString());
                    IhancHttpClient.postJson(getContext(), "/index/purchase/supplyUpdate", params, new AsyncHttpResponseHandler() {
                        @Override
                        public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                            String res=new String(responseBody);
                            try {
                                JSONObject obj=new JSONObject(res);
                                if(obj.getInt("result")==3){
                                    Toast.makeText(getContext(),"该供应商名已存在！",Toast.LENGTH_LONG).show();
                                    //return;
                                }
                                else if(obj.getInt("result")==1){
                                    Toast.makeText(getContext(),"保存成功！",Toast.LENGTH_LONG).show();
                                    dismiss();
                                    onAddSupplySucceed.AddSupplySucceed();
                                    return;
                                }
                            }catch (JSONException e){e.printStackTrace();}
                            saveBtn.setText("保存");
                            saveBtn.setClickable(true);
                        }

                        @Override
                        public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                            Log.d("addSupply",new String(responseBody));
                        }
                    });

                }catch (JSONException e){
                    e.printStackTrace();
                }
            }
        });
    }

    public static String getPinYinHeadChar(String chines) {
        sb.setLength(0);
        char[] chars = chines.toCharArray();
        HanyuPinyinOutputFormat defaultFormat = new HanyuPinyinOutputFormat();
        defaultFormat.setCaseType(HanyuPinyinCaseType.LOWERCASE);
        defaultFormat.setToneType(HanyuPinyinToneType.WITHOUT_TONE);
        for (int i = 0; i < chars.length; i++) {
            if (chars[i] > 128) {
                try {
                    sb.append(PinyinHelper.toHanyuPinyinStringArray(chars[i], defaultFormat)[0].charAt(0));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                sb.append(chars[i]);
            }
        }
        return sb.toString();
    }
}
