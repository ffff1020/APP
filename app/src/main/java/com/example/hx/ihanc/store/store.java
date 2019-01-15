package com.example.hx.ihanc.store;

public class store {
    private int store_id;
    private String store_name;
    public store(int store_id,String store_name){
        this.store_id=store_id;
        this.store_name=store_name;
    }

    public int getStore_id() {
        return store_id;
    }

    public String getStore_name() {
        return store_name;
    }
}
