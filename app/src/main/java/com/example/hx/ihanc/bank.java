package com.example.hx.ihanc;

public class bank {
    private int bank;
    private String name;
    public bank(int id,String name){
        this.bank=id;
        this.name=name;
    }
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getBank() {
        return bank;
    }
}
