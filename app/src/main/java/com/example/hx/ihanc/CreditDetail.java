package com.example.hx.ihanc;

public class CreditDetail {
    private String time;
    private String goods_name;
    private String summary;
    private int sum;
    private boolean group;
    public CreditDetail(String time,String goods_name,String summary,int sum,boolean group){
        this.time=time;
        this.goods_name=goods_name;
        this.summary=summary;
        this.sum=sum;
        this.group=group;
    }
    public String getTime(){
        return time;
    }

    public int getSum() {
        return sum;
    }

    public String getGoods_name() {
        return goods_name;
    }

    public String getSummary() {
        return summary;
    }

    public boolean isGroup() {
        return group;
    }
}
