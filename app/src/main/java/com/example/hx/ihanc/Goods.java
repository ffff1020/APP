package com.example.hx.ihanc;

public class Goods {
    int goods_id;
    String goods_name;
    String goods_unit;
    Double goods_price;
    String goods_sn;
    int category_id;
    String goods_unit_name;
    int promote;
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
}
