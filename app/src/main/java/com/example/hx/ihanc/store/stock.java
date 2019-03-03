package com.example.hx.ihanc.store;

import org.json.JSONException;
import org.json.JSONObject;

public class stock {
    public int stock_id;
    public int goods_id;
    public String goods_name;
    public String inorder;
    public double number;
    public int sum;
    public int unit_id;
    public String unit_name;
    public stock(int stock_id,
             int goods_id,
             String goods_name,
             String inorder,
             double number,
             int sum,
             int unit_id,
             String unit_name){
        this.goods_id=goods_id;
        this.stock_id=stock_id;
        this.goods_name=goods_name;
        this.inorder=inorder;
        this.sum=sum;
        this.number=number;
        this.unit_id=unit_id;
        this.unit_name=unit_name;
    }

    public String toString(){
        String rs="";
        try {
            JSONObject res=new JSONObject();
            res.put("stock_id",stock_id);
            res.put("goods_id",goods_id);
            res.put("goods_name",goods_name);
            res.put("inorder",inorder);
            res.put("sum",sum);
            res.put("number",number);
            res.put("unit_id",unit_id);
            res.put("unit_name",unit_name);
            rs=res.toString();
        }catch (JSONException e){e.printStackTrace();}
        return rs;
    }
    public JSONObject toJson(){
        JSONObject res=new JSONObject();
        try {
            res.put("stock_id",stock_id);
            res.put("goods_id",goods_id);
            res.put("goods_name",goods_name);
            res.put("inorder",inorder);
            res.put("sum",sum);
            res.put("number",number);
            res.put("unit_id",unit_id);
            res.put("unit_name",unit_name);
        }catch (JSONException e){e.printStackTrace();}
        return res;
    }
}
