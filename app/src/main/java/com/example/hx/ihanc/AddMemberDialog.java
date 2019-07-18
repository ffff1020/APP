package com.example.hx.ihanc;

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
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpResponseHandler;

import net.sourceforge.pinyin4j.PinyinHelper;
import net.sourceforge.pinyin4j.format.HanyuPinyinCaseType;
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType;

import org.json.JSONException;
import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;

public class AddMemberDialog extends DialogFragment {

  private View view;
  public static StringBuffer sb = new StringBuffer();
  private EditText memberNameEdit;
  private EditText memberSnEdit;
  private EditText memberTelEdit;
  private OnAddMemberSucceed onAddMemberSucceed;
  private Button saveBtn;
  public member member=null;
  private TextView title;

  public static AddMemberDialog newInstance(OnAddMemberSucceed onAddMemberSucceed){
    AddMemberDialog dialog=new AddMemberDialog();
    dialog.onAddMemberSucceed=onAddMemberSucceed;
    return dialog;
  }

  @Nullable
  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
    view = inflater.inflate(R.layout.dialog_add_ember, container, false);
    initView();
    return view;
  }

  public interface OnAddMemberSucceed{
    void AddMemberSucceed();
  }

    @Override
    public void onStart() {
        super.onStart();
        if(member!=null){
            memberNameEdit.setText(member.member_name);
            memberSnEdit.setText(member.member_sn);
            memberTelEdit.setText(member.getTel());
            Log.d("dialog",member.member_name);
            title.setText("修改客户");
        }else{
            memberNameEdit.setText("");
            memberSnEdit.setText("");
            memberTelEdit.setText("");
            Log.d("dialog","null");
            title.setText("新增客户");
        }
    }

    private void initView(){
    memberNameEdit=view.findViewById(R.id.member_name);
    memberSnEdit=view.findViewById(R.id.member_sn);
    memberTelEdit=view.findViewById(R.id.member_tel);
    title=view.findViewById(R.id.title);
    memberNameEdit.addTextChangedListener(new TextWatcher() {
      @Override
      public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

      }

      @Override
      public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

      }

      @Override
      public void afterTextChanged(Editable editable) {
        if(!TextUtils.isEmpty(memberNameEdit.getText())){
          memberSnEdit.setText(getPinYinHeadChar(memberNameEdit.getText().toString()));
        }
      }
    });
    saveBtn=view.findViewById(R.id.saveButton);


    saveBtn.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        if(TextUtils.isEmpty(memberNameEdit.getText())) return;
        JSONObject params=new JSONObject();
        saveBtn.setClickable(false);
        saveBtn.setText("保存中...");
        try {
          params.put("member_id","");
          params.put("member_name", memberNameEdit.getText().toString());
          params.put("member_sn", memberSnEdit.getText().toString());
          params.put("member_phone", memberTelEdit.getText().toString());
          IhancHttpClient.postJson(getContext(), "/index/sale/memberUpdate", params, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
              String res=new String(responseBody);
              try {
                JSONObject obj=new JSONObject(res);
                if(obj.getInt("result")==3){
                  Toast.makeText(getContext(),"该用户名已存在！",Toast.LENGTH_LONG).show();
                  //return;
                }
                else if(obj.getInt("result")==1){
                  Toast.makeText(getContext(),"保存成功！",Toast.LENGTH_LONG).show();
                  onAddMemberSucceed.AddMemberSucceed();
                  return;
                }
              }catch (JSONException e){e.printStackTrace();}
              saveBtn.setText("保存");
              saveBtn.setClickable(true);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
              Log.d("addMember",new String(responseBody));
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
