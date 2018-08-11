package com.example.hx.ihanc;

import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;

import static android.hardware.usb.UsbManager.ACTION_USB_DEVICE_DETACHED;
import static com.example.hx.ihanc.Constant.ACTION_USB_PERMISSION;
import static com.example.hx.ihanc.DeviceConnFactoryManager.ACTION_QUERY_PRINTER_STATE;

public class SaleFragment extends Fragment {
    private member member;
    private TextView title;
    private Button button;
    public SharedPreferences sp;
    private GPrinter mGPrinter;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.sale_fragment, container, false);
        title=(TextView)view.findViewById(R.id.title);
        if(this.member!=null) {
            RequestParams params = new RequestParams();
            params.put("member_id",this.member.getMember_id());
            IhancHttpClient.get("/index/sale/getMemberCredit",params , new AsyncHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                    String res=new String(responseBody);
                    try{
                        JSONObject object = new JSONObject(res);
                        title.setText("欠款金额：￥"+object.getString("credit"));
                    }catch (JSONException e){
                        Log.d("JSONArray",e.toString());
                    }
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {

                }
            });
      }
      title.setOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View view) {
              Utils.printMemberName=member.getMember_name();
              MainActivity parentActivity = (MainActivity ) getActivity();
              parentActivity.initPrinter();
          }
      });
        button=(Button)view.findViewById(R.id.closeFragmentButton);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MainActivity parentActivity = (MainActivity ) getActivity();
                parentActivity.deleteCurrentSaleTabs();
            }
        });
        sp= PreferenceManager.getDefaultSharedPreferences((MainActivity ) getActivity());
        return view;
    }

    public void setMember(com.example.hx.ihanc.member member) {
        this.member = member;
    }

    public member getMember(){
        return member;
    }
}
