package com.example.hx.ihanc;

public  class SaleListItem {
    public final int sale_id;
    public final String time;
    public final String name;
    public final String sum;
    public  boolean paid=false;

    public SaleListItem(int id, String time, String name, String sum,int paid) {
        this.time = time;
        this.name = name;
        this.sum = sum;
        this.sale_id=id;
        this.paid=paid==1;
    }

    @Override
    public String toString() {
        return time+":"+name+":￥"+sum;
    }
}

