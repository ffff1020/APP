package com.example.hx.ihanc.store;

public class StockDetail {
    public String time;
    public String user;
    public Double Dstock;
    public int Dsum;
    public double stock;
    public int sum;
    public String remark;
    public String type;
    public StockDetail(
             String time,
             String user,
             Double Dstock,
             int Dsum,
             double stock,
             int sum,
             String remark,
             String type
    ){
        this.time=time;
        this.user=user;
        this.Dstock=Dstock;
        this.Dsum=Dsum;
        this.stock=stock;
        this.sum=sum;
        this.remark=remark;
        this.type=type;
    }
}
