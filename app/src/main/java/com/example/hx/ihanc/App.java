package com.example.hx.ihanc;

import android.app.Application;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.util.Log;

import com.alibaba.sdk.android.push.CloudPushService;
import com.alibaba.sdk.android.push.CommonCallback;
import com.alibaba.sdk.android.push.noonesdk.PushServiceFactory;

public class App extends Application {
    private static Context mContext;
    public static final String TAG = "AppApplication";

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = getApplicationContext();
        initPushService(this);
    }

    public static Context getContext() {
        return mContext;
    }

    /**
     * 初始化云推送通道
     * @param applicationContext
     */
    private void initPushService(final Context applicationContext) {
        Log.i(TAG, "initPushService");
        this.createNotificationChannel();
        PushServiceFactory.init(applicationContext);
        final CloudPushService pushService = PushServiceFactory.getCloudPushService();
        pushService.register(applicationContext, new CommonCallback() {
            @Override
            public void onSuccess(String response) {
                Log.i(TAG, "init cloudchannel success");
                //setConsoleText("init cloudchannel success");
                if(Utils.mCompanyInfo!=null) bindPushAlias(Utils.mCompanyInfo.getTel());
            }
            @Override
            public void onFailed(String errorCode, String errorMessage) {
                Log.e(TAG, "init cloudchannel failed -- errorcode:" + errorCode + " -- errorMessage:" + errorMessage);
                //setConsoleText("init cloudchannel failed -- errorcode:" + errorCode + " -- errorMessage:" + errorMessage);
            }
        });
    }

    public static void bindPushAlias(String email){
        final CloudPushService pushService = PushServiceFactory.getCloudPushService();
        pushService.addAlias(email, new CommonCallback() {
            @Override
            public void onSuccess(String s) {
                Log.i(App.TAG, "addAlias cloudchannel success"+Utils.mCompanyInfo.getTel());
            }
            @Override
            public void onFailed(String s, String s1) {
                Log.i(App.TAG, "addAlias cloudchannel Failed");
            }
        });
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                // 通知渠道的id
                //String id = Utils.mCompanyInfo.getTel();
                String id="ihanc";
                // 用户可以看到的通知渠道的名字.
                CharSequence name = "iHanc";
                // 用户可以看到的通知渠道的描述
                String description = "iHanc消息提示";
                int importance = NotificationManager.IMPORTANCE_HIGH;
                NotificationChannel mChannel = new NotificationChannel(id, name, importance);
                // 配置通知渠道的属性
                mChannel.setDescription(description);
                // 设置通知出现时的闪灯（如果 android 设备支持的话）
                mChannel.enableLights(true);
                mChannel.setLightColor(Color.RED);
                // 设置通知出现时的震动（如果 android 设备支持的话）
                mChannel.enableVibration(true);
                mChannel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
                //最后在notificationmanager中创建该通知渠道
                mChannel.setShowBadge(true);
                mChannel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
                mNotificationManager.createNotificationChannel(mChannel);
                Log.i(App.TAG, "createNotificationChannel");
        }
    }
}
