package com.example.hx.ihanc;

public class CompanyInfo {
    private String name;
    private String tel;
    private String[] address;
    public CompanyInfo(String companyName,String companyTel,String[] companyAddress){
        this.name=companyName;
        this.tel=companyTel;
        this.address=companyAddress;
    }

    public String getName() {
        return name;
    }

    public String getTel() {
        return tel;
    }

    public String[] getAddress() {
        return address;
    }
}
