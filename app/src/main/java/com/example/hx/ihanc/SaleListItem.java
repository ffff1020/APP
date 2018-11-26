package com.example.hx.ihanc;

public  class SaleListItem {
    public final int sale_id;
    public final String time;
    public final String name;
    public final String sum;
    public  int  member_id;
    public boolean paid;

    public SaleListItem(int id, String time, String name, String sum,int paid,int member_id) {
        this.time = time;
        this.name = name;
        this.sum = sum;
        this.sale_id=id;
        this.member_id=member_id;
        this.paid=paid==1;
    }

    @Override
    public String toString() {
        return time+":"+name+":￥"+sum;
    }
}

