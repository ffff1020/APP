package com.example.hx.ihanc;

import org.json.JSONException;
import org.json.JSONObject;

public class member {
    int member_id;
    String member_name;
    String member_sn;
    String tel;
    public member(int member_id,String member_name,String member_sn,String tel){
        this.member_id=member_id;
        this.member_name=member_name;
        this.member_sn=member_sn;
        this.tel=tel;
    }

    public int getMember_id() {
        return member_id;
    }

    public String getMember_sn() {
        return member_sn;
    }

    public String getMember_name() {
        return member_name;
    }

    public String getTel() {
        return tel;
    }

    public JSONObject getMember(){
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("member_id", member_id);
            jsonObject.put("member_name", member_name);
        }catch (JSONException e){
            e.printStackTrace();
        }
        return jsonObject;
    }

}
