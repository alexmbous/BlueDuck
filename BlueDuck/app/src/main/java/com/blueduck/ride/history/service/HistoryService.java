package com.blueduck.ride.history.service;

import android.content.Context;

import com.blueduck.ride.base.BaseService;
import com.blueduck.ride.utils.CommonUtils;
import com.blueduck.ride.utils.LogUtils;
import com.blueduck.ride.utils.RequestCallBack;
import com.blueduck.ride.utils.RequestDialog;
import com.blueduck.ride.utils.RetrofitHttp;

import org.json.JSONObject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HistoryService extends BaseService {

    public HistoryService(Context context, RequestCallBack callBack, String TAG) {
        super(context, callBack, TAG);
    }

    /**
     * 获得历史数据
     * Get historical data
     * @param token
     * @param pageNo
     * @param flag
     */
    public void getHistory(String token, int pageNo, final int flag){
        RequestDialog.show(mContext);
        RetrofitHttp.getRetrofit(0).create(HistoryApi.class)
                .getHistory(HistoryParameter.getHistory(token,pageNo))
                .enqueue(new Callback<JSONObject>() {
                    @Override
                    public void onResponse(Call<JSONObject> call, Response<JSONObject> response) {
                        RequestDialog.dismiss(mContext);
                        JSONObject retJson = response.body();
                        if (retJson != null){
                            LogUtils.i(TAG,"获得历史数据: "+retJson.toString());
                            callBack.onSuccess(retJson,flag);
                        }else{
                            CommonUtils.onFailure(mContext, 500, TAG);
                        }
                    }

                    @Override
                    public void onFailure(Call<JSONObject> call, Throwable t) {
                        RequestDialog.dismiss(mContext);
                        CommonUtils.serviceError(mContext,t);
                    }
                });
    }
}
