package com.example.hx.ihanc;

public class category {
    private int category_id=0;
    private String category_name="";
    public int getCategory_id(){
        return category_id;
    }
    public String getCategory_name(){
        return category_name;
    }

    public void setCategory_id(int id) {
        category_id = id;
    }

    public void setCategory_name(String name) {
        category_name = name;
    }
}
