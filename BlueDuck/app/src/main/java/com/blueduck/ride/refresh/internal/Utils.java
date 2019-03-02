package com.blueduck.ride.refresh.internal;

import com.blueduck.ride.utils.LogUtils;


/**
 * <br />
 */
public class Utils {

    private static final String TAG = "Utils";

    static final String LOG_TAG = "PullToRefresh";

    public static void warnDeprecation(String depreacted, String replacement) {
        LogUtils.w(TAG,"You're using the deprecated " + depreacted + " attr, please switch over to " + replacement);
    }
}
