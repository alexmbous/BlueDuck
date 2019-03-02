package com.blueduck.ride.personal.service;

import android.content.Context;

import com.blueduck.ride.R;
import com.blueduck.ride.base.BaseService;
import com.blueduck.ride.utils.CommonUtils;
import com.blueduck.ride.utils.LogUtils;
import com.blueduck.ride.utils.RequestCallBack;
import com.blueduck.ride.utils.RequestDialog;
import com.blueduck.ride.utils.RetrofitHttp;

import org.json.JSONException;
import org.json.JSONObject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PersonalService extends BaseService {

    public PersonalService(Context context, RequestCallBack callBack, String TAG) {
        super(context, callBack, TAG);
    }

    /**
     * 设置密码
     * set a password
     * @param token
     * @param password
     * @param flag
     */
    public void setPassword(String token, String password, final int flag){
        RequestDialog.show(mContext);
        RetrofitHttp.getRetrofit(0).create(PersonalApi.class)
                .resetNewPassword(PersonalParameter.setPassword(token,password))
                .enqueue(new Callback<JSONObject>() {
                    @Override
                    public void onResponse(Call<JSONObject> call, Response<JSONObject> response) {
                        RequestDialog.dismiss(mContext);
                        JSONObject retJson = response.body();
                        if (retJson != null){
                            LogUtils.i(TAG,"设置密码："+retJson.toString());
                            try {
                                int code = retJson.getInt("code");
                                if (code == 200){
                                    String result = retJson.getString("data");
                                    if ("1".equals(result)){
                                        callBack.onSuccess(result,flag);
                                    }else{
                                        CommonUtils.hintDialog(mContext,mContext.getString(R.string.operation_failed));
                                    }
                                }else{
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
                        RequestDialog.dismiss(mContext);
                        CommonUtils.serviceError(mContext,t);
                    }
                });
    }

}
