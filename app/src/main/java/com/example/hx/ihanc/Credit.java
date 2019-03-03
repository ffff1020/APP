package com.example.hx.ihanc;

import java.text.NumberFormat;
import java.util.Locale;

public class Credit {
    private String name;
    private int sum;
    private int id;
    public Credit(String name,int sum,int id){
        this.name=name;
        this.sum=sum;
        this.id=id;
    }
    public String getCreditSum(){
        NumberFormat format = NumberFormat.getCurrencyInstance(Locale.CHINA);
        return format.format(sum);
    }
    public String getName(){
        return name;
    }
    public int getMemberId(){
        return id;
    }
}
