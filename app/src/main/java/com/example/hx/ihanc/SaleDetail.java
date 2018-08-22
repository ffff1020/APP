package com.example.hx.ihanc;

import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONException;
import org.json.JSONObject;

public class SaleDetail implements Parcelable {
    private DetailGoods good;
    private double number;
    private double price;
    private int sum;
    private int unit_id;
    private int store_id;
    private String unit_name;

    public SaleDetail(DetailGoods good,double number,double price,int unit_id,String unit_name,int sum){
        this.good=good;
        this.number=number;
        this.price=price;
        this.sum=sum;
        this.unit_id=unit_id;
        this.store_id=Utils.storeId;
        this.unit_name=unit_name;
    }
    protected SaleDetail(Parcel in){
        this.good=(DetailGoods) in.readValue(DetailGoods.class.getClassLoader());
        this.number=in.readDouble();
        this.price=in.readDouble();
        this.sum=in.readInt();
        this.unit_id=in.readInt();
        this.store_id=in.readInt();
        this.unit_name=in.readString();
    }
     public String getGoods_name(){
        return good.getGoods_name();
     }
     public String getNumber(){
        String str=""+number;
        return str.substring(0,str.length())+unit_name;
     }
     public String getPrice(){
        String str="￥"+price;
        return str.substring(0,str.length());
    }
     public int getSum(){
        return sum;
    }

     public String toString(){
        return good.getGoods_id()+getNumber()+getPrice();
     }

    public static final Creator<SaleDetail> CREATOR = new Creator<SaleDetail>() {
         @Override
         public SaleDetail createFromParcel(Parcel in) {
                        return new SaleDetail(in);
                   }
                 @Override
         public SaleDetail[] newArray(int size) {
                         return new SaleDetail[size];
                     }
     };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        if(good!=null) {
            dest.writeValue(good);
            dest.writeDouble(number);
            dest.writeDouble(price);
            dest.writeInt(sum);
            dest.writeInt(unit_id);
            dest.writeInt(store_id);
            dest.writeString(unit_name);
        }
    }
    public JSONObject getSaleDetailJson(){
        JSONObject mObject=new JSONObject();
        JSONObject goodObject=new JSONObject();
        try{
            goodObject.put("goods_id",good.getGoods_id());
            goodObject.put("goods_name",good.getGoods_name());
            goodObject.put("unit_id_0",good.getUnit_id_0());
            mObject.put("good",goodObject);
            mObject.put("number",number);
            mObject.put("price",price);
            mObject.put("sum",sum);
            mObject.put("store_id",store_id);
            mObject.put("unit_id",unit_id);
        }catch (JSONException e){e.printStackTrace();}
        return mObject;
    }

}
