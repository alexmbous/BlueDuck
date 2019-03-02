package com.blueduck.ride.base;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;
import android.support.annotation.RequiresApi;


public class MyApplication extends Application{

    public boolean isUpdateVersions = false;//是否提示版本更新 Whether to prompt version update

    @Override
    public void onCreate() {
        super.onCreate();
        isUpdateVersions = true;
        //To make the version judgment, the SDK is greater than 26 to create a notification channel, otherwise the low version will crash.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){//要做版本判断，SDK大于26就创建通知渠道，不然低版本会崩溃
            //单车骑行service渠道通知 Cycling service channel notification
            String channelId = "use";
            String channelName = "use";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            createNotificationChannel(channelId,channelName,importance);
            //应用推送渠道通知 App push channel notifications
            channelId = "push";
            channelName = "push";
            importance = NotificationManager.IMPORTANCE_DEFAULT;
            createNotificationChannel(channelId,channelName,importance);
        }
    }

    /**
     * Create notification channels (after Android 8.0, official notifications must be categorized,
     * otherwise notifications cannot be issued (lower versions do not affect))
     * 创建通知渠道(Android8.0之后官方要求通知必须分类，不然无法发出通知(低版本不影响))
     * @param channelId 渠道ID（随意定义，唯一性就行，发通知时用到，用于标记哪个渠道） Channel ID
     * @param channelName 渠道名称 （随意，是给用户看的） Channel name
     * @param importance 渠道等级 （跟通知的等级差不多） Channel level
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    private void createNotificationChannel(String channelId, String channelName, int importance){
        NotificationChannel channel = new NotificationChannel(channelId,channelName,importance);
        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        manager.createNotificationChannel(channel);
    }
}
