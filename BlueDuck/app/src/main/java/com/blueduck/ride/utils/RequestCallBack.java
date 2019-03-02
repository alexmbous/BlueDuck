package com.blueduck.ride.utils;

/**
 * Public request callback
 * 公共请求回调
 */
public interface RequestCallBack {

    void onSuccess(Object o, int flag);

    void onFail(Throwable t, int flag);
}
