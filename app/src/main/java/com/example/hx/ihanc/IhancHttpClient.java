package com.example.hx.ihanc;

import android.content.Context;
import android.util.Log;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONObject;

import java.io.UnsupportedEncodingException;

import cz.msebera.android.httpclient.entity.ByteArrayEntity;
import cz.msebera.android.httpclient.message.BasicHeader;
import cz.msebera.android.httpclient.protocol.HTTP;

public class IhancHttpClient {
    private static final String BASE_URL = "http://www.ihanc.com/ihanc/hx/tp5/public/index.php";
   // public static final String HOST_URL="http://192.168.100.118/hx/";
    public static final String HOST_URL="http://www.ihanc.com/ihanc/hx/";
   // private static final String BASE_URL = "http://192.168.100.118/hx/tp5/public/index.php";
    private static AsyncHttpClient client = new AsyncHttpClient();
    public static String mAuth="";

    public static void get(String url, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        client.addHeader("Authorization",mAuth);
        client.get(getAbsoluteUrl(url), params, responseHandler);
    }

    public static void post(String url, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        client.addHeader("Authorization",mAuth);
        client.post(getAbsoluteUrl(url), params, responseHandler);
    }

    public static String getAbsoluteUrl(String relativeUrl) {
        return BASE_URL + relativeUrl;
    }

    public static void setAuth(String Auth){
        mAuth=Auth;
    }

    public static void postJson(Context mContext, String url, JSONObject mObject, AsyncHttpResponseHandler responseHandler){
        client.addHeader("Authorization",mAuth);
        ByteArrayEntity entity = null;
        try {
            entity = new ByteArrayEntity(mObject.toString().getBytes("UTF-8"));
            entity.setContentType(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        client.post(mContext,getAbsoluteUrl(url),entity,"application/json",responseHandler);
    }
}
