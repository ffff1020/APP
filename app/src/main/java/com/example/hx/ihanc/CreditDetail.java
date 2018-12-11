package com.example.hx.ihanc;

public class CreditDetail {
    private String time;
    private String goods_name;
    private String summary;
    private int sum;
    private boolean group;
    public boolean selected = false;
    private int sale_id;
    public CreditDetail(String time,String goods_name,
                        String summary,int sum,boolean group,int sale_id
                        ){
        this.time=time;
        this.goods_name=goods_name;
        this.summary=summary;
        this.sum=sum;
        this.group=group;
        this.sale_id=sale_id;

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

    public int getSale_id() {
        return sale_id;
    }


}
