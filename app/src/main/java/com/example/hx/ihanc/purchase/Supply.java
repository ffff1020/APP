package com.example.hx.ihanc.purchase;

import org.json.JSONException;
import org.json.JSONObject;

public class Supply {
    public int supply_id;
    public String supply_name;
    public String supply_sn;
    public Supply(int supply_id,String supply_name,String supply_sn){
        this.supply_id=supply_id;
        this.supply_name=supply_name;
        this.supply_sn=supply_sn;
    }
    public JSONObject toJSONObject(){
        JSONObject obj=new JSONObject();
        try{
            obj.put("supply_id",supply_id);
            obj.put("supply_name",supply_name);
        }catch (JSONException e){e.printStackTrace();}
        return obj;
    }
}
