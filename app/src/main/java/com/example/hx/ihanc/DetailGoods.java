package com.example.hx.ihanc;

import java.io.Serializable;

public class DetailGoods implements Serializable {
    private int goods_id;
    private String goods_name;
    private int unit_id_0;
    public DetailGoods(int id,String name,int unit_0){
        this.goods_id=id;
        this.goods_name=name;
        this.unit_id_0=unit_0;
    };

    public String getGoods_name() {
        return goods_name;
    }

    public int getGoods_id() {
        return goods_id;
    }

    public int getUnit_id_0() {
        return unit_id_0;
    }
}
