package com.example.hx.ihanc;

public class member {
    int member_id;
    String member_name;
    String member_sn;
    public member(int member_id,String member_name,String member_sn){
        this.member_id=member_id;
        this.member_name=member_name;
        this.member_sn=member_sn;
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


}
