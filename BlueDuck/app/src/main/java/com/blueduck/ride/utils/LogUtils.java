package com.blueduck.ride.utils;

import android.util.Log;

import com.blueduck.ride.BuildConfig;

public class LogUtils {

    /**
     * Whether to enable debugging (can turn off debugging when publishing to cancel all printing to optimize app performance)
     * 是否开启调试(发布时可关闭调试功能取消所有打印以优化app性能)
     */
    private static boolean isLog = BuildConfig.IS_LOG;

    public static void i(String TAG,String message){
        if (isLog) Log.i(TAG, message);
    }

    public static void d(String TAG,String message){
        if (isLog) Log.d(TAG, message);
    }

    public static void e(String TAG,String message){
        if (isLog) Log.e(TAG, message);
    }

    public static void w(String TAG,String message){
        if (isLog) Log.w(TAG, message);
    }
}
