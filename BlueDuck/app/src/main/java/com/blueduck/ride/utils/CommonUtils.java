package com.blueduck.ride.utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.app.Service;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.v4.content.FileProvider;
import android.text.TextUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.blueduck.ride.BuildConfig;
import com.blueduck.ride.R;
import com.blueduck.ride.base.BaseActivity;
import com.blueduck.ride.base.MyApplication;
import com.blueduck.ride.login.activity.LoginActivity;
import com.blueduck.ride.login.bean.LoginBean;
import com.blueduck.ride.login.service.LoginApi;
import com.blueduck.ride.main.bean.UserInfoBean;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


/**
 * Public tool class
 * 公共工具类
 */
public class CommonUtils {

    private static List<Activity> list = new ArrayList<Activity>();

    public static void remove(Activity activity) {
        list.remove(activity);
    }

    public static void add(Activity activity) {
        list.add(activity);
    }

    public static void finishProgram() {
        for (int i = 0; i < list.size(); i++) {
            Activity activity = list.get(i);
            activity.finish();
        }
        list.clear();
    }

    /**
     * Get notification object
     * 获得通知对象
     * @param context
     * @return
     */
    public static NotificationManager getNotificationManager(Context context){
        return (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
    }

    /**
     * sign out
     * 退出登录
     * @param mContext
     */
    public static void logOut(Context mContext){
        MyApplication myApplication = (MyApplication) mContext.getApplicationContext();
        myApplication.isUpdateVersions = true;
        getNotificationManager(mContext).cancel(CommonSharedValues.PUSH_SERVICE_NOTIFICATION);
        SharedPreferences sp = mContext.getSharedPreferences(CommonSharedValues.SP_NAME, mContext.MODE_PRIVATE);
        SharedPreferences shared = mContext.getSharedPreferences(CommonSharedValues.SAVE_LOGIN, mContext.MODE_PRIVATE);
        SharedPreferences.Editor et = shared.edit();
        /**
         * After logging out, only save the mobile phone number, country area code, red envelope area ID and Bluetooth address,
         * email address, login account type to use when logging in.
         * 退出登录后仅此保存手机号码，国家区域号，红包区域ID和蓝牙地址，邮箱，登录账号类型登录时要用到**/
        et.putString(CommonSharedValues.PHONE_LOGIN_PHONE_NUMBER, sp.getString(CommonSharedValues.SP_KEY_PHONE, ""));
        et.putString(CommonSharedValues.PHONE_LOGIN_PHONE_CODE, sp.getString(CommonSharedValues.SP_PHONE_CODE, ""));
        et.putString(CommonSharedValues.PHONE_LOGIN_EMAIL, sp.getString(CommonSharedValues.SP_KEY_INFO_EMAIL, ""));
        et.putString(CommonSharedValues.EXIT_LOGIN_ACCOUNT_TYPE, sp.getString(CommonSharedValues.SP_LOGIN_ACCOUNT_TYPE, ""));
        et.putString(CommonSharedValues.EXIT_MAC_ADDRESS,sp.getString(CommonSharedValues.SP_MAC_ADDRESS,""));
        et.putString(CommonSharedValues.EXIT_AREA_ID,sp.getString(CommonSharedValues.SP_RED_BIKE_AREA_ID,""));
        et.apply();
        SharedPreferences.Editor editor = sp.edit();
        editor.clear();//退出登录将保存的sp值清空 Logout will clear the saved sp value
        editor.apply();
        mContext.startActivity(new Intent(mContext, LoginActivity.class));
        finishProgram();//结束掉所有活动 End all activities
    }

    /**
     * Collapse virtual keyboard
     * 收起虚拟键盘
     * @param activity
     */
    public static void hideSoftinput(Activity activity) {
        InputMethodManager manager = (InputMethodManager) activity
                .getSystemService(Service.INPUT_METHOD_SERVICE);
        if (manager.isActive()) {
            manager.hideSoftInputFromWindow(activity.getWindow().getDecorView()
                    .getWindowToken(), 0);
        }
    }

    /**
     * Delayed pop-up keyboard, time 998
     * 延时弹出键盘，time 998
     * @param edit
     */
    public static void showSoftInput(final EditText edit, int time) {
        edit.setFocusable(true);
        edit.setFocusableInTouchMode(true);
        edit.requestFocus();
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            public void run() {
                InputMethodManager imm = (InputMethodManager) edit.getContext()
                        .getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.showSoftInput(edit, InputMethodManager.RESULT_SHOWN);
            }
        }, time);
    }

    /**
     * Convert from dp units to px (pixels) according to the resolution of the phone
     * 根据手机的分辨率从 dp 的单位 转成为 px(像素)
     */
    public static int dip2px(Context context,float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    /**
     * Get status bar height
     * 获得状态栏高度
     * @param context
     * @return
     */
    public static int getStatusBarHeight(Context context){
        int stateHeight = 0;
        int resourceId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            stateHeight = context.getResources().getDimensionPixelSize(resourceId);
        }
        return stateHeight;
    }

    /**
     * Get Amazon upload image or file instance
     * 获得亚马逊上传图片或文件实例
     * @return
     */
    /*public static AmazonS3Client getS3Client(){
        ClientConfiguration configuration = new ClientConfiguration();
        configuration.setConnectionTimeout(60 * 1000);// 连接超时，默认60秒 Connection timeout, default 60 seconds
        configuration.setSocketTimeout(60 * 1000);// socket超时，默认60秒 Socket timeout, default 60 seconds
        configuration.setMaxConnections(5);// 最大并发请求书，默认5个 Maximum concurrent request, default 5
        configuration.setMaxErrorRetry(2);// 失败后最大重试次数，默认2次 Maximum number of retries after failure, default 2 times
        return new AmazonS3Client(new BasicAWSCredentials(CommonSharedValues.AMAZONS3_ACCESS_KEY,CommonSharedValues.AMAZONS3_SECRET_KEY),configuration);
    }*/

    /*
     * 将时间转换为时间戳
     * Convert time to timestamp
     */
    public static String dateToStamp(String s){
        String res;
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = null;
        try {
            date = simpleDateFormat.parse(s);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        long ts = date.getTime();
        res = String.valueOf(ts);
        return res;
    }
    /*
    * 将时间戳转换为时间
    * Convert timestamps to time
    */
    public static String stampToDate(String s){
        String res;
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        long lt = new Long(s);
        Date date = new Date(lt);
        res = simpleDateFormat.format(date);
        return res;
    }
    /**
     * 获得当前小时
     * Get current hour
     * @return
     */
    public static int getCurrentHour(){
        long currentTime = System.currentTimeMillis();
        SimpleDateFormat sdf = new SimpleDateFormat("HH");
        String date = sdf.format(new Date(currentTime));
        return Integer.parseInt(date);
    }

    /**
     * 根据时间戳获取上午或下午
     * Get morning or afternoon based on timestamp
     * @param timeMillis
     * @return 0：上午 morning  1：下午 afternoon
     */
    public static int getAmOrPm(long timeMillis){
        SimpleDateFormat sdf = new SimpleDateFormat("HH");
        String date = sdf.format(new Date(timeMillis));
        int hour = Integer.parseInt(date);
        if (hour > 12 && hour <= 24){
            return 1;
        }else{
            return 0;
        }
    }

    /**
     * Determine if the string is a number
     * 判断字符串是否为数字
     * @param str
     * @return
     */
    public static boolean isNumber(String str){
        boolean isNum = str.matches("[0-9]+");
        return isNum;
    }

    /**
     * Determines whether the string is the correct password
     * 判断字符串是否为正确密码
     * @param str
     * @return
     */
    public static boolean isPassword(String str){
        boolean isPass = str.matches("^(?=.*[a-zA-Z])(?=.*[0-9]).{8,18}$");
        return isPass;
    }

    /**
     * Network request exception handling
     * 网络请求异常处理
     * @param context
     * @param throwable
     */
    public static void serviceError(Context context,Throwable throwable){
        String temp = throwable.getMessage();
        if (TextUtils.isEmpty(temp) || temp == null) return;
        LogUtils.i("TAG","serviceError : "+temp);
        String errorMessage = context.getString(R.string.request_error_text);
        /*if(throwable instanceof SocketTimeoutException){//服务器响应超时
            errorMessage = context.getString(R.string.socket_timeout_error);
        }else if(throwable instanceof ConnectException){//网络连接异常，请检查网络
            errorMessage = context.getString(R.string.internet_error);
        }else if(throwable instanceof RuntimeException){//运行时错误
            errorMessage = context.getString(R.string.runtime_error);
        }else if(throwable instanceof UnknownHostException){//无法解析主机，请检查网络连接
            errorMessage = context.getString(R.string.unknown_host_please_check_network);
        }else if(throwable instanceof UnknownServiceException){//未知的服务器错误
            errorMessage = context.getString(R.string.unknown_error);
        }*/
        hintDialog(context,errorMessage);
    }
    public static void onFailure(Context context,int code,String TAG){
        if (code == 101 && context != null){
            logOut(context);
            Toast.makeText(context,context.getString(R.string.login_expired),Toast.LENGTH_LONG).show();
        }else if (code == 102 && context != null) {
            logOut(context);
            Toast.makeText(context, context.getString(R.string.login_expired), Toast.LENGTH_LONG).show();
        }else if (code == 104 && context != null){//账号在其他设备上登录 Account is logged in on other devices
            remoteLoginDialog(context);
        }else if (code == 105 && context != null){//token待刷新 Token to be refreshed
            refreshToken(context,TAG);
        }else if (code == 201 && context != null){//缺少参数 Missing parameters
            Toast.makeText(context,context.getString(R.string.missing_parameter),Toast.LENGTH_LONG).show();
        }else if (code == 202 && context != null){//数据不存在 Data does not exist
//            Toast.makeText(context,context.getString(R.string.data_null),Toast.LENGTH_LONG).show();
        }else if (code == 203 && context != null){//数据已存在 Data already exists
            Toast.makeText(context,context.getString(R.string.data_already_existing),Toast.LENGTH_LONG).show();
        }else if (code == 206 && context != null){//请求过于频繁 Request too frequently
            Toast.makeText(context,context.getResources().getString(R.string.request_error),Toast.LENGTH_LONG).show();
        }else if (code == 500 && context != null){//服务器异常 Server exception
            Toast.makeText(context,context.getString(R.string.service_error),Toast.LENGTH_LONG).show();
        }else if (code == 20005 && context != null){//账户已被冻结 Account has been frozen
            hintDialog(context,context.getString(R.string.account_has_been_frozen));
        }
    }

    /**
     * Refresh token
     * 刷新token
     */
    private static void refreshToken(final Context mContext,final String TAG){
        final SharedPreferences sp = mContext.getSharedPreferences(CommonSharedValues.SP_NAME, mContext.MODE_PRIVATE);
        Map<String, String> map = new HashMap<>();
        map.put("requestType", "20026");
        map.put("token", sp.getString(CommonSharedValues.SP_KEY_TOKEN, ""));
        RetrofitHttp.getRetrofit(0).create(LoginApi.class)
                .refreshToken(map)
                .enqueue(new Callback<JSONObject>() {
                    @Override
                    public void onResponse(Call<JSONObject> call, Response<JSONObject> response) {
                        JSONObject jsonObject = response.body();
                        if (jsonObject != null){
                            try {
                                int code = jsonObject.getInt("code");
                                if (code == 200) {
                                    String token = jsonObject.getString("data");
                                    SharedPreferences.Editor et = sp.edit();
                                    et.putString(CommonSharedValues.SP_KEY_TOKEN, token);
                                    et.apply();
                                } else {
                                    CommonUtils.onFailure(mContext, code, TAG);
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }else{
                            CommonUtils.onFailure(mContext, 500, TAG);
                        }
                    }

                    @Override
                    public void onFailure(Call<JSONObject> call, Throwable t) {
                        serviceError(mContext, t);
                    }
                });
    }

    /**
     * Check if the current network is available
     * 检查当前网络是否可用
     * @param context
     * @return
     */
    public static boolean isNetwork(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager == null) {
            return false;
        } else {
            NetworkInfo[] networkInfo = connectivityManager.getAllNetworkInfo();
            if (networkInfo != null && networkInfo.length > 0) {
                for (int i = 0; i < networkInfo.length; i++) {
                    if (networkInfo[i].getState() == NetworkInfo.State.CONNECTED) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * 判断是否启动定位服务
     * Determine whether to start the location service
     * @param context
     * @return
     */
    public static boolean isOpenLocService(final Context context) {
        boolean isGps = false;
        boolean isNetwork = false;
        if (context != null) {
            LocationManager locationManager
                    = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
            if (locationManager != null) {
                isGps = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
                isNetwork = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
            }
            if (isGps || isNetwork) {
                return true;
            }
        }
        return false;
    }

    /**
     * Prompt to open GPS location service
     * 提示打开GPS定位服务
     */
    public static void openGPSLocationHint(final Context context){
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(context.getString(R.string.hint));
        builder.setCancelable(false);
        builder.setMessage(context.getString(R.string.gps_location_hint));
        builder.setNegativeButton(context.getString(R.string.cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int i) {
                Toast.makeText(context,context.getString(R.string.rejection_hint),Toast.LENGTH_LONG).show();
                dialog.dismiss();
            }
        });
        builder.setPositiveButton(context.getString(R.string.confirm), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                gotoLocServiceSettings(context);
                dialog.dismiss();
            }
        });
        builder.create().show();
    }

    /**
     * Jump location service interface
     * 跳转定位服务界面
     * @param context
     */
    private static void gotoLocServiceSettings(Context context) {
        final Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    /**
     * No network connection prompt pops up dialog box and ends the interface
     * 无网络连接提示弹出对话框并且结束界面
     * @param activity
     */
    public static void networkDialog(final Activity activity, final boolean isFinish){
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle(activity.getResources().getString(R.string.hint));
        builder.setMessage(activity.getResources().getString(R.string.network_message));
        builder.setPositiveButton(activity.getResources().getString(R.string.confirm), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                if (isFinish){
                    activity.finish();
                }
            }
        });
        builder.create().show();
    }

    /**
     * Account login prompt dialog on other devices
     * 账号在其他设备上登录提示对话框
     * @param context
     */
    public static void remoteLoginDialog(final Context context){
        if (context instanceof BaseActivity && !((BaseActivity) context).isRunning()){
            return;
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(context.getResources().getString(R.string.hint));
        builder.setMessage(context.getString(R.string.remote_login));
        builder.setCancelable(false);
        builder.setPositiveButton(context.getResources().getString(R.string.confirm), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(BroadCastValues.REMOTE_LOGIN_BROADCAST);
                context.sendBroadcast(intent);
                dialog.dismiss();
                logOut(context);
            }
        });
        builder.create().show();
    }

    /**
     * Prompt dialog
     * 提示对话框
     * @param message
     */
    public static void hintDialog(Context context,String message){
        if (context instanceof BaseActivity && !((BaseActivity) context).isRunning()){
            return;
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(context.getResources().getString(R.string.hint));
        builder.setMessage(message);
        builder.setPositiveButton(context.getResources().getString(R.string.confirm), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.create().show();
    }
    /**
     * Double type retains two decimal places
     * Double类型保留两位小数
     * @param value
     * @return
     */
    public static String DoubleRetainTwo(double value){
        DecimalFormat df = new DecimalFormat("######0.00");
        return df.format(value);
    }
    /**
     * Double type retains one decimal
     * Double类型保留一位小数
     * @param value
     * @return
     */
    public static String DoubleRetainOne(double value){
        DecimalFormat df = new DecimalFormat("######0.0");
        return df.format(value);
    }

    /**
     * Get the riding unit (for calculating the riding amount)
     * 获得骑行单位(计算骑行金额用)
     * @param start
     * @param end
     * @param minMinutes
     * @return
     */
    public static int getUseTimeCount(String start,String end,int minMinutes){
        if(minMinutes == 0){
            minMinutes = 30;
        }
        int minute = getMinutes(Long.parseLong(start), Long.parseLong(end));  // 计算出分钟 / Calculate minutes
        int num = 0;
        num = minute/minMinutes+(minute%minMinutes > 0?1:0);
        if(num == 0){
            num = 1;
        }
        return num;
    }

    /**
     * Calculate minutes based on start and end time
     * 根据开始与结束时间计算分钟
     * @param start
     * @param end
     * @return
     */
    public static int getMinutes(long start,long end){
        if(end == 0){
            end = System.currentTimeMillis() / 1000;
        }
        long rideTime = end - start; // 秒/seconds
        int minute =  (int) (rideTime/60);  // 计算出分钟/calculating minute
        if(minute == 0){
            return 1;
        }
        return minute;
    }

    /**
     * Get the version name and version number
     * 获取版名和版本号
     * @param context
     * @param type 1：版本号 version number 2：版本名 Version name
     * @return
     */
    public static String getVersionInfo(Context context,int type){
        PackageManager packageManager = context.getPackageManager();
        PackageInfo packInfo = null;
        try {
            packInfo = packageManager.getPackageInfo(context.getPackageName(),0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        if (type == 1){
            int version = packInfo.versionCode;
            return version+"";
        }else if (type == 2){
            return packInfo.versionName;
        }
        return null;
    }

    /**
     * Image scale compression method
     * 图片按比例大小压缩方法
     * **/
    public static Bitmap comp(Bitmap image) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        if( baos.toByteArray().length / 1024>1024) {
            baos.reset();
            image.compress(Bitmap.CompressFormat.JPEG, 50, baos);
        }
        ByteArrayInputStream isBm = new ByteArrayInputStream(baos.toByteArray());
        BitmapFactory.Options newOpts = new BitmapFactory.Options();
        newOpts.inJustDecodeBounds = true;
        Bitmap bitmap = BitmapFactory.decodeStream(isBm, null, newOpts);
        newOpts.inJustDecodeBounds = false;
        int w = newOpts.outWidth;
        int h = newOpts.outHeight;
        float hh = 800f;
        float ww = 480f;
        int be = 1;
        if (w > h && w > ww) {
            be = (int) (newOpts.outWidth / ww);
        } else if (w < h && h > hh) {
            be = (int) (newOpts.outHeight / hh);
        }
        if (be <= 0)
            be = 1;
        newOpts.inSampleSize = be;
        newOpts.inPreferredConfig = Bitmap.Config.RGB_565;
        isBm = new ByteArrayInputStream(baos.toByteArray());
        bitmap = BitmapFactory.decodeStream(isBm, null, newOpts);
        return compressImage(bitmap);
    }
    /**
     * Quality compression
     * 质量压缩
     * **/
    private static Bitmap compressImage(Bitmap image) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        int options = 100;
        while ( baos.toByteArray().length / 1024>100) {
            baos.reset();
            options -= 10;
            image.compress(Bitmap.CompressFormat.JPEG, options, baos);
        }
        ByteArrayInputStream isBm = new ByteArrayInputStream(baos.toByteArray());
        Bitmap bitmap = BitmapFactory.decodeStream(isBm, null, null);
        return bitmap;
    }

    //将Bitmap保存到系统图库 Save Bitmap to the system gallery
    public static String saveImageToGallery(Context context, Bitmap bmp) {
        // 首先保存图片 Save the image first
        File appDir = new File(Environment.getExternalStorageDirectory(), "bicycle");
        if (!appDir.exists()) {
            appDir.mkdir();
        }
        String fileName = System.currentTimeMillis() + ".jpg";
        File file = new File(appDir, fileName);
        try {
            FileOutputStream fos = new FileOutputStream(file);
            bmp.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.flush();
            fos.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // 其次把文件插入到系统图库 Second, insert the file into the system gallery.
        try {
            MediaStore.Images.Media.insertImage(context.getContentResolver(),
                    file.getAbsolutePath(), fileName, null);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        // 最后通知图库更新 Final notification gallery update
        context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(file)));
        return file.getAbsolutePath();
    }
    /**MD5加密算法 MD5 encryption algorithm **/
    public static String md5(String string) {
        if (TextUtils.isEmpty(string)) {
            return "";
        }
        MessageDigest md5 = null;
        try {
            md5 = MessageDigest.getInstance("MD5");
            byte[] bytes = md5.digest(string.getBytes());
            String result = "";
            for (byte b : bytes) {
                String temp = Integer.toHexString(b & 0xff);
                if (temp.length() == 1) {
                    temp = "0" + temp;
                }
                result += temp;
            }
            return result;
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return "";
    }

    /**
     * Get device unique identifier
     * 获取设备唯一标示
     * @param context
     * @return
     */
    public static String getUniqueId(Context context){
        String androidID = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
        String id = androidID + Build.SERIAL;
        return md5(id);
    }

    /**
     * Save user information
     * 保存用户信息
     * @param sp
     */
    public static void saveUserInfo(SharedPreferences sp,UserInfoBean infoBean){
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(CommonSharedValues.SP_KEY_INFO_FIRSTNAME, infoBean.getUserInfo().getFirstname());
        editor.putString(CommonSharedValues.SP_KEY_INFO_LASTNAME, infoBean.getUserInfo().getLastname());
        editor.putString(CommonSharedValues.SP_KEY_INFO_EMAIL, infoBean.getUserInfo().getEmail());
        editor.putString(CommonSharedValues.SP_KEY_NICKNAME, infoBean.getUserInfo().getUserVo().getNickName());
        editor.putString(CommonSharedValues.SP_KEY_EMAIL_AUTH, infoBean.getUserInfo().getEmailAuth());
        editor.putString(CommonSharedValues.SP_KEY_GENDER, infoBean.getUserInfo().getGender());
        editor.putString(CommonSharedValues.SP_KEY_PHONE, infoBean.getUserInfo().getUserVo().getPhone());
        editor.putString(CommonSharedValues.SP_INVITATION_CODE, infoBean.getUserInfo().getUserVo().getInvite_code());
        editor.putString(CommonSharedValues.SP_PHONE_CODE, infoBean.getUserInfo().getUserVo().getPhoneCode());
        editor.apply();
    }

    /**
     * Save login information
     * 保存登录信息
     * @param sp
     * @param loginBean
     * @param account
     * @param regions
     * @param accountType
     */
    public static void saveLoginInfo(SharedPreferences sp, LoginBean loginBean, String account, String regions, int accountType){
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(CommonSharedValues.SP_KEY_UID,loginBean.getuId());
        if (accountType == 1) {//短信 SMS
            editor.putString(CommonSharedValues.SP_KEY_PHONE, account);
            editor.putString(CommonSharedValues.SP_PHONE_CODE, regions);
        }else{//邮箱 email
            editor.putString(CommonSharedValues.SP_KEY_INFO_EMAIL, account);
        }
        editor.putString(CommonSharedValues.SP_LOGIN_ACCOUNT_TYPE,accountType+"");
        editor.putString(CommonSharedValues.SP_KEY_NICKNAME,loginBean.getNickName());
        editor.putString(CommonSharedValues.SP_KEY_INDUSTRYID,loginBean.getIndustryId());
        editor.putString(CommonSharedValues.SP_KEY_TOKEN,loginBean.getToken());
        editor.putString(CommonSharedValues.SP_KEY_AUTHSTATUS,loginBean.getAuthStatus());
        editor.apply();
    }

    /**
     * Provide precise addition
     * 提供精确的加法运算。
     * @param v1
     * @param v2
     * @return 两个参数的和 The sum of two parameters
     */
    public static double add(double v1,double v2){
        BigDecimal b1 = new BigDecimal(Double.toString(v1));
        BigDecimal b2 = new BigDecimal(Double.toString(v2));
        return b1.add(b2).doubleValue();
    }
    /**
     * Provide accurate subtraction
     * 提供精确的减法运算。
     * @param v1
     * @param v2
     * @return 两个参数的差 Difference between two parameters
     */
    public static double sub(double v1,double v2){
        BigDecimal b1 = new BigDecimal(Double.toString(v1));
        BigDecimal b2 = new BigDecimal(Double.toString(v2));
        return b1.subtract(b2).doubleValue();
    }
    /**
     * Provide accurate multiplication
     * 提供精确的乘法运算。
     * @param v1
     * @param v2
     * @return 两个参数的积 Product of two parameters
     */
    public static double mul(double v1,double v2){
        BigDecimal b1 = new BigDecimal(Double.toString(v1));
        BigDecimal b2 = new BigDecimal(Double.toString(v2));
        return b1.multiply(b2).doubleValue();
    }

    /**
     * Get new file name
     * 获得新文件名
     * @return
     */
    private static File getFileName(){
        File dir = Environment.getExternalStorageDirectory();//指定照片的储存路径以及文件名 Specify the storage path and file name of the photo
        File mdir = new File(dir, "bicycle");
        if (!mdir.exists()) {
            mdir.mkdir();
        }
        return new File(mdir,getPhotoFileName());
    }

    /**
     * Get the name of the photo
     * 得到照片的名称
     * @return
     */
    private static String getPhotoFileName() {
        Date date = new Date(System.currentTimeMillis());
        SimpleDateFormat sdf = new SimpleDateFormat("'IMG'_yyyyMMdd_HHmmss");
        return sdf.format(date) + ".jpg";
    }

    /**
     * Open photography
     * 开启照相
     **/
    public static File startCameraPicCut(Activity activity,int CAMERA) {
        File inputPhotoFile = getFileName(); //文件夹的名字,文件名 Folder name, file name
        Uri uriForFile = FileProvider.getUriForFile(activity, BuildConfig.APPLICATION_ID + ".provider", inputPhotoFile);
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);//设置拍照意图 Set photo intent
        //Set the photo activity to help store photos and tell the storage path
        intent.putExtra(MediaStore.EXTRA_OUTPUT, uriForFile);//设置让拍照Activity 帮助存储照片并告知存储路径
        //在用intent呼叫camera時，要另外加上臨時權限
        List<ResolveInfo> resInfoList= activity.getPackageManager().queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
        for (ResolveInfo resolveInfo : resInfoList) {
            String packageName = resolveInfo.activityInfo.packageName;
            activity.grantUriPermission(packageName, uriForFile,
                    Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
        }
        activity.startActivityForResult(intent, CAMERA);
        return inputPhotoFile;
    }

    /**
     * Open gallery
     * 开启图库
     **/
    public static void startImageCapture(Activity activity,int PHOTO_ALBUM) {
        Intent intent = new Intent(Intent.ACTION_PICK, null);
        intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
        activity.startActivityForResult(intent, PHOTO_ALBUM);
    }

    /**
     * Convert File Uri to Content Uri (android 7.0 or higher)
     * File Uri转换为 Content Uri(android 7.0 以上)
     * 转换 content:// uri
     * @param imageFile
     * @return
     */
    public static Uri getImageContentUri(Context context, File imageFile) {
        String filePath = imageFile.getAbsolutePath();
        Cursor cursor = context.getContentResolver().query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                new String[] { MediaStore.Images.Media._ID },
                MediaStore.Images.Media.DATA + "=? ",
                new String[] { filePath }, null);

        if (cursor != null && cursor.moveToFirst()) {
            int id = cursor.getInt(cursor
                    .getColumnIndex(MediaStore.MediaColumns._ID));
            Uri baseUri = Uri.parse("content://media/external/images/media");
            return Uri.withAppendedPath(baseUri, "" + id);
        } else {
            if (imageFile.exists()) {
                ContentValues values = new ContentValues();
                values.put(MediaStore.Images.Media.DATA, filePath);
                return context.getContentResolver().insert(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
            } else {
                return null;
            }
        }
    }

    /**
     * 裁剪 Crop
     * @param uri
     * @param size
     */
    public static File startPhotoZoom(Activity activity,Uri uri, int size, int TAILOR) {
        File outputPhotoFile = getFileName();
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(uri, "image/*");
        intent.putExtra("crop", "true");
        //aspectX aspectY 是宽高的比例 Is the ratio of width to height
        intent.putExtra("aspectX", 1);
        intent.putExtra("aspectY", 1);
        //outputX,outputY 是剪裁图片的宽高 Is the width and height of the cropped image
        intent.putExtra("outputX", size);
        intent.putExtra("outputY", size);
        intent.putExtra("scale", true);
        intent.putExtra("return-data", false);
        //Define the output File Uri, and then take the cropped image information according to this Uri
        intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(outputPhotoFile));//定义输出的File Uri，之后根据这个Uri去拿裁剪好的图片信息
        intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());
        intent.putExtra("noFaceDetection", true);
        activity.startActivityForResult(intent, TAILOR);
        return outputPhotoFile;
    }

    /**
     * Set the length of the ride
     * 设置骑行时长
     * @param time
     * @param timeView
     */
    public static void setTime(long time,TextView timeView){
        long useTimestamp = 0;//骑行计数时间 Cycling count time
        if (time < 0){
            useTimestamp = 0;
        }else {
            useTimestamp = time;
        }
        long hour = useTimestamp / 3600;
        long minute = (useTimestamp % 3600) / 60;
        long ss = (useTimestamp % 3600) % 60;
        String timeStr = "";
        if (hour > 0){
            timeStr = String.format("%s:%s:%s", setTimeFormat(hour), setTimeFormat(minute), setTimeFormat(ss));
        }else{
            timeStr = String.format("%s:%s", setTimeFormat(minute), setTimeFormat(ss));
        }
        timeView.setText(timeStr);
    }

    private static String setTimeFormat(long time) {
        String timeStr = "";
        if (time < 10) {
            timeStr = "0" + time;
        } else {
            timeStr = time + "";
        }
        return timeStr;
    }

    /**
     * Get scooter or bicycle lock power
     * 获取滑板车或单车锁电量
     * @param bikeType
     * @param power
     * @return
     */
    public static int getPowerPercent(String bikeType,int power) {
        if("2".equals(bikeType)){
            return power;
        }else{
            if(power >= 420) return 100;
            else if(power<420 && power>=411) return (95+(power-411)*5/9);
            else if(power<411 && power>=395) return 90+(power-395)*5/16;
            else if(power<395 &&power>=386) return 80+(power-386)*10/9;
            else if(power<386 &&power>=379) return 70+(power-379)*10/7;
            else if(power<379 &&power>=373) return 60+(power-373)*10/6;
            else if(power<373 &&power>=369) return 50+(power-369)*10/4;
            else if(power<369 &&power>=365) return 40+(power-365)*10/4;
            else if(power<365 &&power>=363) return 30+(power-363)*10/2;
            else if(power<363 &&power>=359) return 20+(power-359)*10/4;
            else if(power<359 &&power>=354) return 10+(power-354)*10/5;
            else if(power<354 &&power>340) return (power-340)*10/14;
            else if(power<=340) return 0;
            return 0;
        }
    }

}
