package com.example.hx.ihanc;

import org.json.JSONException;
import org.json.JSONObject;

public class Pay {
    private int bank;
    private int paid_sum;
    public Pay(int bank,int paid_sum){
        this.bank=bank;
        this.paid_sum=paid_sum;
    }

    public JSONObject getPayJson(){
        JSONObject pay=new JSONObject();
        try{
            pay.put("bank",bank);
            pay.put("paid_sum",paid_sum);
        }catch (JSONException e){e.printStackTrace();}
        return  pay;
    }
}
