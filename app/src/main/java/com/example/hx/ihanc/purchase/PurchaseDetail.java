package com.example.hx.ihanc.purchase;

import com.example.hx.ihanc.Goods;

import org.json.JSONException;
import org.json.JSONObject;

public class PurchaseDetail {
    public int purchase_id;
    public Goods goods;
    public int unit_id;
    public String unit_name;
    public double price;
    public double number;
    public int sum;
    public int purchase_detail_id;
    public String store_name;
    public int store_id;
    public boolean paid;
    public PurchaseDetail(
             int purchase_id,
             Goods goods,
             int unit_id,
             String unit_name,
             double price,
             double number,
             int sum,
             int purchase_detail_id,
             String store_name,
             int store_id
    )
    {
        this.purchase_id=purchase_id;
        this.goods=goods;
        this.unit_id=unit_id;
        this.unit_name=unit_name;
        this.price=price;
        this.number=number;
        this.sum=sum;
        this.purchase_detail_id=purchase_detail_id;
        this.store_name=store_name;
        this.store_id=store_id;
    }
    public JSONObject toJSONObject(){
        JSONObject obj=new JSONObject();
        try {
            obj.put("goods_id", goods.getGoods_id());
            obj.put("unit_check",goods.getGoods_unit_id()==unit_id);
            obj.put("is_order",goods.is_order);
            obj.put("number",number);
            obj.put("price",price);
            obj.put("sum",sum);
            obj.put("store_id",store_id);
            obj.put("unit_id",unit_id);
        }catch (JSONException e){e.printStackTrace();}
        return obj;
    }
}
