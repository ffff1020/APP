package com.example.hx.ihanc;

import android.util.Log;

import com.epson.EpsonCom.EpsonCom;
import com.epson.EpsonCom.EpsonComDevice;
import com.epson.EpsonCom.EpsonComDeviceParameters;

public class Print {
    EpsonComDevice dev;
    EpsonComDeviceParameters devParams;
    String IP;
    public Print(){
        dev=new EpsonComDevice();
        devParams=new EpsonComDeviceParameters();
        //SettingEthParameters();
    }
    private EpsonCom.ERROR_CODE SettingEthParameters(){
        devParams.PortType= EpsonCom.PORT_TYPE.ETHERNET;
        devParams.IPAddress=this.IP;
        devParams.PortNumber=9100;
        return dev.setDeviceParameters(devParams);
    }
    public void setIP(String IP){
        this.IP=IP;
    }
    public void doPrint(String content){
        EpsonCom.ERROR_CODE err = EpsonCom.ERROR_CODE.SUCCESS;
        err=SettingEthParameters();
        if(err== EpsonCom.ERROR_CODE.SUCCESS) {
            err=dev.openDevice();
            if(err!= EpsonCom.ERROR_CODE.SUCCESS){
                String errorString = EpsonCom.getErrorText(err);
                Log.d("iHanc", "Error from openDevice: " + errorString);
            }else{
                dev.selectReceiptPaper();
                dev.selectAlignment(EpsonCom.ALIGNMENT.LEFT);
                dev.printString("Hello HANCHUAN", EpsonCom.FONT.FONT_A,true,false,false,false);
                dev.cutPaper();
                dev.closeDevice();
            }
        }else{
            String errorString = EpsonCom.getErrorText(err);
            Log.d("iHanc", "setDeviceParameters Error: " + errorString);
        }
    }
}
