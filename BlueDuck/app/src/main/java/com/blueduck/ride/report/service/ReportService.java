package com.blueduck.ride.report.service;

import android.content.Context;

import com.blueduck.ride.R;
import com.blueduck.ride.base.BaseService;
import com.blueduck.ride.utils.CommonUtils;
import com.blueduck.ride.utils.LogUtils;
import com.blueduck.ride.utils.RequestCallBack;
import com.blueduck.ride.utils.RequestDialog;
import com.blueduck.ride.utils.RetrofitHttp;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ReportService extends BaseService {

    public ReportService(Context context, RequestCallBack callBack, String TAG) {
        super(context, callBack, TAG);
    }

    /**
     * 获得故障列表
     * Get a failure list
     * @param token
     * @param flag
     */
    public void getReport(String token, final int flag){
        RequestDialog.show(mContext);
        RetrofitHttp.getRetrofit(0).create(ReportApi.class)
                .getReport(ReportParameter.getReport(token))
                .enqueue(new Callback<JSONObject>() {
                    @Override
                    public void onResponse(Call<JSONObject> call, Response<JSONObject> response) {
                        RequestDialog.dismiss(mContext);
                        JSONObject retJson = response.body();
                        if (retJson != null){
                            LogUtils.i(TAG,"获得故障列表: "+retJson.toString());
                            try {
                                int code = retJson.getInt("code");
                                if (code == 200){
                                    Gson gson = new Gson();
                                    JSONArray jsonArray = retJson.getJSONArray("data");
                                    List<String> list = gson.fromJson(jsonArray.toString(),new TypeToken<List<String>>(){}.getType());
                                    callBack.onSuccess(list,flag);
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

    /**
     * Submit fault feedback
     * 提交故障反馈
     * @param token
     * @param number
     * @param lat
     * @param lng
     * @param content
     * @param flag
     */
    public void submitReport(String token, String number, String type, double lat, double lng, String content, final int flag){
        RequestDialog.show(mContext);
        RetrofitHttp.getRetrofit(0).create(ReportApi.class)
                .submitReport(ReportParameter.submitReport(token,number,type,lat,lng,content))
                .enqueue(new Callback<JSONObject>() {
                    @Override
                    public void onResponse(Call<JSONObject> call, Response<JSONObject> response) {
                        RequestDialog.dismiss(mContext);
                        JSONObject retJson = response.body();
                        if (retJson != null) {
                            LogUtils.i(TAG,"提交故障反馈 : "+retJson.toString());
                            try {
                                int code = retJson.getInt("code");
                                if (code == 200) {
                                    String result = retJson.getString("data");
                                    if ("1".equals(result)) {
                                        callBack.onSuccess(result,flag);
                                    } else {
                                        CommonUtils.hintDialog(mContext,mContext.getString(R.string.operation_failed));
                                    }
                                    //You have submitted this failure and are awaiting system review!
                                } else if (code == 203) {//您已提交过该故障，正在等待系统审核!
                                    CommonUtils.hintDialog(mContext, mContext.getString(R.string.you_have_submitted));
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
                        RequestDialog.dismiss(mContext);
                        CommonUtils.serviceError(mContext,t);
                    }
                });
    }
}
