package com.example.hx.ihanc;

public class Unit {
    private int unit_id;
    private String unit_name;
    public Unit(int id,String name){
        this.unit_id=id;
        this.unit_name=name;
    }

    public int getUnit_id() {
        return unit_id;
    }

    public String getUnit_name() {
        return unit_name;
    }
}
