package com.example.hx.ihanc;

import android.support.annotation.Nullable;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

public class Goods  {
    int goods_id;
    String goods_name;
    String goods_unit;
    Double goods_price;
    String goods_sn;
    int category_id;
    String goods_unit_name;
    int promote;
    public int is_order=0;
    public Goods(int goods_id,String goods_name,String goods_unit_id,Double goods_price,
                 String goods_sn,int category_id,String goods_unit_name,int promote){
        this.goods_id=goods_id;
        this.goods_name=goods_name;
        this.goods_unit=goods_unit_id;
        this.goods_price=goods_price;
        this.category_id=category_id;
        this.goods_sn=goods_sn;
        this.goods_unit_name=goods_unit_name;
        this.promote=promote;
    };

    public Double getGoods_price() {
        return goods_price;
    }

    public int getGoods_id() {
        return goods_id;
    }

    public String getGoods_unit() {
        return goods_unit;
    }

    public String getGoods_name() {
        return goods_name;
    }

    public String getGoods_sn() {
        return goods_sn;
    }

    public int getCategory_id() {
        return category_id;
    }

    public String getGoods_unit_name() {
        return goods_unit_name;
    }

    public int getPromote() {
        return promote;
    }

    public int getGoods_unit_id(){return Integer.parseInt(goods_unit);};

    public String toString(){
        JSONObject goodObject=new JSONObject();
        String str="";
        try{
            goodObject.put("goods_id",goods_id);
            goodObject.put("goods_name",goods_name);
            goodObject.put("goods_unit",goods_unit);
            goodObject.put("goods_price",goods_price);
            goodObject.put("category_id",category_id);
            goodObject.put("goods_sn",goods_sn);
            goodObject.put("goods_unit_name",goods_unit_name);
            goodObject.put("promote",promote);
            str=goodObject.toString();
        }catch (JSONException e){e.printStackTrace();}
        return str;
    }
    public JSONObject toJSONObject(){
        JSONObject goodObject=new JSONObject();
        try{
            goodObject.put("goods_id",goods_id);
            goodObject.put("goods_name",goods_name);
            goodObject.put("goods_unit",goods_unit);
            goodObject.put("goods_price",goods_price);
            goodObject.put("category_id",category_id);
            goodObject.put("goods_sn",goods_sn);
            goodObject.put("goods_unit_name",goods_unit_name);
            goodObject.put("promote",promote);
        }catch (JSONException e){e.printStackTrace();}
        return goodObject;
    }
    @Nullable
    public static Goods toGoods(JSONObject goodObject){
        try{
        return new Goods(goodObject.getInt("goods_id"),
        goodObject.getString("goods_name"),
        goodObject.getString("goods_unit"),
        goodObject.getDouble("goods_price"),
        goodObject.getString("goods_sn"),
        goodObject.getInt("category_id"),
        goodObject.getString("goods_unit_name"),
        goodObject.getInt("promote"));
        }catch (JSONException e){e.printStackTrace();}
        return null;
    }


}
