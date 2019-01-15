package com.example.hx.ihanc;

import android.content.Context;
import android.content.Intent;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpResponseHandler;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import cz.msebera.android.httpclient.Header;

public class Utils {
    private static Toast toast;
    public static final String GOODSFILTERCATEGORYID="GOODSFILTERCATEGORYID";
    public static final String GOODSFILTERSEARCHVIEW="GOODSFILTERSEARCHVIEW";
    public static final String GOODSFILTERPROMOTE="GOODSFILTERPROMOTE";
    public static CompanyInfo mCompanyInfo;
    public static member printMemberName;
    public static int storeId;
    public static boolean Link;
    public static List<SaleFragment> fragmentList;
    public static List<Unit> currentUnitList=new ArrayList<Unit>();
    public static String printSaleId="";
    public static boolean saleTypeOrder=false;

    public static UsbDevice getUsbDeviceFromName(Context context, String usbName) {
        UsbManager usbManager = (UsbManager) context.getSystemService(Context.USB_SERVICE);
        HashMap<String,UsbDevice> usbDeviceList = usbManager.getDeviceList();
        return usbDeviceList.get(usbName);
    }

    public static void toast(Context context, String message) {
        if (toast == null) {
            toast = Toast.makeText(context, message, Toast.LENGTH_SHORT);
        } else {
            toast.setText(message);
        }
        toast.show();
    }

    public static void toastLostLink(Context context) {
        if (toast == null) {
            toast = Toast.makeText(context, "不能连接到服务器！", Toast.LENGTH_SHORT);
        } else {
            toast.setText("不能连接到服务器！");
        }
        toast.show();
    }

    public static void getCompanyInfo(){
        IhancHttpClient.get("/index/setting/info", null, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                String res=new String(responseBody);
                Link=true;
                try {
                    JSONObject mObject=new JSONObject(res);
                    String[] address=mObject.getString("cadd").split("\\+");
                    mCompanyInfo=new CompanyInfo(
                            mObject.getString("cname"),
                            mObject.getString("ctel"),
                            address
                    );

                }catch (JSONException e){
                    Log.d("JSONException",e.toString());
                }

            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                 Log.d("getCompanyInfo",responseBody.toString());
            }
        });
    }


}
