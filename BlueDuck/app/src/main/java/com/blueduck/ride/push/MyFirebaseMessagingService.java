package com.blueduck.ride.push;

import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;

import com.blueduck.ride.R;
import com.blueduck.ride.main.activity.MainActivity;
import com.blueduck.ride.utils.BroadCastValues;
import com.blueduck.ride.utils.CommonSharedValues;
import com.blueduck.ride.utils.CommonUtils;
import com.blueduck.ride.utils.LogUtils;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;


/**
 * 处理和接收推送消息service
 */

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = "MyFirebaseMsgService";

    @Override
    public void onNewToken(String token) {
        /**
         * token更新发生在以下几种情况 /Token update occurs in the following situations
         * 1.应用删除实例 ID /Apply delete instance ID
         * 2.应用在新设备上恢复/App recovery on new device
         * 3.用户卸载/重新安装应用/User uninstall/reinstall application
         * 4.用户清除应用数据。/The user clears the application data.
         */
        sendRegistrationToServer(token);
        LogUtils.i(TAG,"onNewToken : 刷新了推送token : "+token);
    }

    /**
     * Save the obtained token (because the token is only generated in several cases in the onNewToken() method) and send it to the project server.
     * 将获取的token保存下来(因为token只有在onNewToken()方法中的几种情况下才会生成)，并发送给项目服务器
     * @param token The new token.
     */
    private void sendRegistrationToServer(String token) {
        SharedPreferences shared = getSharedPreferences(CommonSharedValues.SAVE_LOGIN, MODE_PRIVATE);
        SharedPreferences.Editor editor = shared.edit();
        editor.putString(CommonSharedValues.GOOGLE_PUSH_TOKEN,token);
        editor.apply();//保存本地
    }

    /**
     * 接收推送消息回调
     * @param remoteMessage
     */
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        // TODO(developer): Handle FCM messages here.
        if (remoteMessage.getData().size() > 0) {
            LogUtils.i(TAG,"onMessageReceived: 推送消息内容=" + remoteMessage.getData());
            String messageTitle = remoteMessage.getData().get("title");
            String messageContent = remoteMessage.getData().get("content");
            String pushType = remoteMessage.getData().get("pushType");
            String messageCount = remoteMessage.getData().get("messageCount");
            String rideId = remoteMessage.getData().get("rideId");//多人骑行人员关锁id
            String rideUser = remoteMessage.getData().get("rideUser");//多人骑行人员关锁name
            String rideOutArea = remoteMessage.getData().get("rideOutArea");//多人骑行是否越界 != 0 为越界
            if (TextUtils.isEmpty(pushType)){
                if (!TextUtils.isEmpty(messageTitle) && !TextUtils.isEmpty(messageContent)) {
                    setNotification(messageTitle,messageContent);
                }
            }else {
                if (!TextUtils.isEmpty(messageTitle) && !TextUtils.isEmpty(messageContent) && !TextUtils.isEmpty(pushType)) {
                    if ("1".equals(pushType)) {//异地登录
                        sendNotification(messageTitle, messageContent,CommonSharedValues.PUSH_SERVICE_NOTIFICATION);
                        Intent intent = new Intent();
                        intent.setAction(BroadCastValues.GOOGLE_PUSH_BROADCAST);
                        sendBroadcast(intent);
                    }else if ("4".equals(pushType)){//刷新单车使用信息，针对关锁了未跳转结费界面的情况
                        setNotification(messageTitle,messageContent);
                        Intent intent = new Intent();
                        if (TextUtils.isEmpty(rideId) && TextUtils.isEmpty(rideUser) && TextUtils.isEmpty(rideOutArea)) {
                            intent.setAction(BroadCastValues.REFRESH_BIKE_USE_INFO);
                        }
                        sendBroadcast(intent);
                    }
                }
            }
        }
    }
    /**
     * 设置通知
     * @param title
     * @param content
     */
    private void setNotification(String title,String content){
        String id = System.currentTimeMillis()+"";
        sendNotification(title, content,Integer.parseInt(id.substring(id.length()-5,id.length())));
    }
    /**
     * 推送通知
     * @param title 通知标题
     * @param content 通知内容
     */
    private void sendNotification(String title,String content,int id) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT);
        Uri defaultSoundUri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this,"push")
                .setSmallIcon(R.mipmap.app_icon)
                .setTicker(title)
                .setContentTitle(title)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(content))
                .setContentText(content)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent);
        CommonUtils.getNotificationManager(this)
                .notify(id, notificationBuilder.build());
    }
}
