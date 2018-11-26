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
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;

public class PaymentDialog extends DialogFragment {
    private String title;
    private int member_id;
    private OnPaymentSucceed onPaymentSucceed;
    private View view;
    private BankAdapter mBankAdapter;
    private Spinner bankSpinner;
    private EditText payment;
    private int selectedNum=0;
    private int selectedSum=0;
    private int creditSum=0;
    public String selected;

    public static PaymentDialog newInstance(int member_id,String title,
                                            int creditSum,int selectedSum,
                                            int selectedNum,String selected) {
        PaymentDialog f = new PaymentDialog();
        Bundle args = new Bundle();
        args.putInt("member_id", member_id);
        args.putString("title",title);
        args.putInt("creditSum", creditSum);
        args.putInt("selectedSum", selectedSum);
        args.putInt("selectedNum", selectedNum);
        args.putString("selected", selected);
        f.setArguments(args);
        return f;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.member_id = getArguments().getInt("member_id");
        this.title = getArguments().getString("title");
        this.creditSum=getArguments().getInt("creditSum");
        this.selectedSum=getArguments().getInt("selectedSum");
        this.selectedNum=getArguments().getInt("selectedNum");
        this.selected = getArguments().getString("selected");
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.dialog_credit_payment, container);
        TextView titleTV=view.findViewById(R.id.title);
        titleTV.setText(title);
        mBankAdapter=new BankAdapter(getContext(),R.layout.unit,MainActivity.mBankList);
        bankSpinner=view.findViewById(R.id.bankSpinner);
        bankSpinner.setAdapter(mBankAdapter);
        payment=view.findViewById(R.id.payment);
        payment.setText((selectedNum>0?selectedSum:creditSum)+"");
        payment.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if(b) payment.setText("");
            }
        });
        final Button save=view.findViewById(R.id.saveButton);
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //save.setText("保存中...");
                //save.setClickable(false);
                //Log.d("payment",MainActivity.mBankList.get(bankSpinner.getSelectedItemPosition()).getName());
                JSONObject params=new JSONObject();
                try {
                    JSONObject pay=new JSONObject();
                    pay.put("paid_sum",Integer.parseInt(payment.getText().toString()));
                    pay.put("bank",MainActivity.mBankList.get(bankSpinner.getSelectedItemPosition()).getBank());
                    params.put("pay",pay);
                    JSONObject member=new JSONObject();
                    member.put("member_id",member_id);
                    String[] titles = title.split("--");
                    member.put("member_name",titles[0]);
                    params.put("member",member);
                    JSONArray JSONids=new JSONArray();
                    if(selectedNum>0){
                        String[] ids=selected.split(",");
                        for (int i=0;i<ids.length;i++)
                            if(ids[i].length()>0)JSONids.put(Integer.parseInt(ids[i]));
                    }
                    params.put("saleID",JSONids);
                    IhancHttpClient.postJson(getContext(), "/index/sale/payment", params, new AsyncHttpResponseHandler() {
                        @Override
                        public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                            String res=new String(responseBody);
                            try {
                                JSONObject result=new JSONObject(res);
                                if (result.getInt("result")==1){
                                    dismiss();
                                    Toast.makeText(getContext(),"保存成功!",Toast.LENGTH_LONG).show();
                                    onPaymentSucceed.PaymentSucceed();
                                }else{
                                    Toast.makeText(getContext(),"保存失败，请重新保存!",Toast.LENGTH_LONG).show();
                                    dismiss();
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



    public void setOnPaymentSucceed(OnPaymentSucceed onPaymentSucceed){
        this.onPaymentSucceed=onPaymentSucceed;
    }

    public interface OnPaymentSucceed{
         void PaymentSucceed();
    }
}
