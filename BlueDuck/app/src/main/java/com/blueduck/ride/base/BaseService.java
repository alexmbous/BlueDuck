package com.blueduck.ride.base;

import android.content.Context;

import com.blueduck.ride.utils.RequestCallBack;

/**
 * Base Service
 * 基类服务
 */
public class BaseService {

    protected Context mContext;
    protected RequestCallBack callBack;
    protected String TAG;

    /**
     * Service base class constructor
     * 服务基类构造方法
     * @param context
     * @param callBack
     * @param TAG
     */
    public BaseService(Context context, RequestCallBack callBack, String TAG){
        this.mContext = context;
        this.callBack = callBack;
        this.TAG = TAG;
    }

}
