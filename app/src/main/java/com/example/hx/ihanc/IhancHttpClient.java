package com.example.hx.ihanc;

import android.util.Log;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

public class IhancHttpClient {
    //private static final String BASE_URL = "http://www.ihanc.com/ihanc/hx/tp5/public/index.php";
    private static final String BASE_URL = "http://192.168.100.118/hx/tp5/public/index.php";
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
}
