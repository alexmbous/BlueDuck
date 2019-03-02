package com.blueduck.ride.main.service;

import android.content.Context;

import com.blueduck.ride.R;
import com.blueduck.ride.base.BaseService;
import com.blueduck.ride.main.bean.AreaResult;
import com.blueduck.ride.main.bean.ReturnCloseBean;
import com.blueduck.ride.main.bean.StopAreaBean;
import com.blueduck.ride.main.bean.UserInfoBean;
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

public class MainService extends BaseService {

    public MainService(Context context, RequestCallBack callBack, String TAG) {
        super(context, callBack, TAG);
    }

    /**
     * Get user information
     * 获得用户信息
     */
    public void getUserInfo(String token,final boolean isShow,final int flag){
        if (isShow)RequestDialog.show(mContext);
        RetrofitHttp.getRetrofit(0).create(MainApi.class)
                .getUserInfo(MainParameter.getUserInfoParameter(token))
                .enqueue(new Callback<JSONObject>() {
                    @Override
                    public void onResponse(Call<JSONObject> call, Response<JSONObject> response) {
                        if (isShow)RequestDialog.dismiss(mContext);
                        JSONObject jsonObject = response.body();
                        if (jsonObject != null){
                            Gson gson = new Gson();
                            LogUtils.i(TAG,"获得用户信息："+jsonObject.toString());
                            try {
                                int code = jsonObject.getInt("code");
                                if (code == 200){
                                    JSONObject json = jsonObject.getJSONObject("data");
                                    UserInfoBean userInfoBean = gson.fromJson(json.toString(),UserInfoBean.class);
                                    callBack.onSuccess(userInfoBean,flag);
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
                        if (isShow)RequestDialog.dismiss(mContext);
                        CommonUtils.serviceError(mContext,t);
                    }
                });
    }

    /**
     * Get bicycle use information
     * 获得单车使用信息
     * The lock time stamp passed from the unlock interface is used to lock the
     * interface immediately after the lock is unlocked.
     * @param closeTimestamp 开锁界面传过来的关锁时间戳，针对刚开锁还未跳转界面就立马关锁
     * @param flag
     */
    public void getBikeUseInfo(String token,String closeTimestamp, final int flag){
        RetrofitHttp.getRetrofit(0).create(MainApi.class)
                .getBikeUseInfo(MainParameter.getBikeUseInfoParameter(token,closeTimestamp,CommonUtils.getVersionInfo(mContext, 1)))
                .enqueue(new Callback<JSONObject>() {
                    @Override
                    public void onResponse(Call<JSONObject> call, Response<JSONObject> response) {
                        JSONObject jsonObject = response.body();
                        if (jsonObject != null){
                            LogUtils.i(TAG,"获得单车使用信息："+jsonObject.toString());
                            callBack.onSuccess(jsonObject,flag);
                        }else{
                            CommonUtils.onFailure(mContext, 500, TAG);
                        }
                    }

                    @Override
                    public void onFailure(Call<JSONObject> call, Throwable t) {
                        CommonUtils.serviceError(mContext,t);
                    }
                });
    }

    /**
     * Get a nearby bicycle
     * 获得附近单车
     * @param curLat
     * @param curLng
     * @param targetLat
     * @param targetLng
     * @param flag
     */
    public void getBike(double curLat, double curLng, double targetLat, double targetLng, final int flag){
        RetrofitHttp.getRetrofit(0).create(MainApi.class)
                .getBike(MainParameter.getBikeParameter(curLat,curLng,targetLat,targetLng))
                .enqueue(new Callback<JSONObject>() {
                    @Override
                    public void onResponse(Call<JSONObject> call, Response<JSONObject> response) {
                        JSONObject jsonObject = response.body();
                        if (jsonObject != null){
                            LogUtils.i(TAG,"获得附近单车 : "+jsonObject.toString());
                            try {
                                int code = jsonObject.getInt("code");
                                if (code == 200){
                                    JSONArray jsonArray = jsonObject.getJSONArray("data");
                                    callBack.onSuccess(jsonArray,flag);
                                }else{
                                    CommonUtils.onFailure(mContext,code,TAG);
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
                        CommonUtils.serviceError(mContext,t);
                    }
                });
    }

    /**
     * Get parking area
     * 获得停车区域
     * @param curLat
     * @param curLng
     * @param targetLat
     * @param targetLng
     * @param ids
     * @param flag
     */
    public void getStopArea(String token,double curLat, double curLng,
                            final double targetLat, final double targetLng, final String ids, final int flag){
        RetrofitHttp.getRetrofit(0).create(MainApi.class)
                .getStopArea(MainParameter.getStopAreaParameter(token,curLat,curLng,targetLat,targetLng,ids))
                .enqueue(new Callback<JSONObject>() {
                    @Override
                    public void onResponse(Call<JSONObject> call, Response<JSONObject> response) {
                        JSONObject jsonObject = response.body();
                        if (jsonObject != null){
                            LogUtils.i(TAG,"获得停车区域 : "+jsonObject.toString());
                            Gson gson = new Gson();
                            try {
                                int code = jsonObject.getInt("code");
                                if (code == 200){
                                    JSONArray jsonArray = jsonObject.getJSONArray("data");
                                    List<StopAreaBean> list = gson.fromJson(jsonArray.toString(), new TypeToken<List<StopAreaBean>>() {}.getType());
                                    AreaResult areaResult = new AreaResult();
                                    areaResult.setList(list);
                                    areaResult.setIds(ids);
                                    areaResult.setTargetLat(targetLat);
                                    areaResult.setTargetLng(targetLng);
                                    callBack.onSuccess(areaResult,flag);
                                }else{
                                    CommonUtils.onFailure(mContext,code,TAG);
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
                        CommonUtils.serviceError(mContext,t);
                    }
                });
    }

    /**
     * Bicycle unlock
     * 单车解锁
     * @param number
     * @param flag
     */
    public void debLocking(String token,String number,final int flag){
        RetrofitHttp.getRetrofit(0).create(MainApi.class)
                .debLocking(MainParameter.debLockingParameter(token,number))
                .enqueue(new Callback<JSONObject>() {
                    @Override
                    public void onResponse(Call<JSONObject> call, Response<JSONObject> response) {
                        JSONObject jsonObject = response.body();
                        if (jsonObject != null){
                            LogUtils.i(TAG,"单车解锁---GET："+jsonObject.toString());
                            callBack.onSuccess(jsonObject,flag);
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
     * Notify the server to lock or upload old data
     * 通知服务器关锁或上传旧数据
     * @param number
     * If uid > 0 is to upload old data, otherwise it will notify the server to lock with the following userId
     * @param uid 如果uid > 0 就是上传旧数据,否则就是通知服务器关锁用下面的userId
     * @param userId
     * @param runTime
     * @param timestamp
     * @param power
     * @param flag
     */
    public void setUnLockClose(String token,String number, final int uid, String userId, int runTime, long timestamp, String power, final int flag){
        int usId = 0;
        if (uid > 0){
            usId = uid;
        }else{
            usId = Integer.parseInt(userId);
        }
        RetrofitHttp.getRetrofit(0).create(MainApi.class)
                .uploadOldData(MainParameter.uploadOldDataParameter(token,number,power,usId,timestamp,runTime))
                .enqueue(new Callback<JSONObject>() {
                    @Override
                    public void onResponse(Call<JSONObject> call, Response<JSONObject> response) {
                        JSONObject jsonObject = response.body();
                        if (jsonObject != null){
                            LogUtils.i(TAG, "通知服务器关锁或上传旧数据 : " + jsonObject.toString());
                            ReturnCloseBean bean = new ReturnCloseBean();
                            bean.setJsonObject(jsonObject);
                            bean.setUid(uid);
                            callBack.onSuccess(bean,flag);
                        }else{
                            CommonUtils.onFailure(mContext, 500, TAG);
                        }
                    }

                    @Override
                    public void onFailure(Call<JSONObject> call, Throwable t) {
                        CommonUtils.serviceError(mContext,t);
                    }
                });
    }

    /**
     * Scooter network lock
     * 滑板车网络关锁
     * @param date
     */
    public void scooterLocking(String token,String date, final int flag){
        RequestDialog.show(mContext);
        RetrofitHttp.getRetrofit(0).create(MainApi.class)
                .scooterLocking(MainParameter.scooterLockingParameter(token,date))
                .enqueue(new Callback<JSONObject>() {
                    @Override
                    public void onResponse(Call<JSONObject> call, Response<JSONObject> response) {
                        RequestDialog.dismiss(mContext);
                        JSONObject jsonObject = response.body();
                        if (jsonObject != null){
                            LogUtils.i(TAG,"滑板车网络关锁 : "+jsonObject.toString());
                            try {
                                int code = jsonObject.getInt("code");
                                if (code == 200) {
                                    String result = jsonObject.getString("data");
                                    if ("1".equals(result)){
                                        callBack.onSuccess(result,flag);
                                    }
                                }else if (code == 202){
                                    // TODO: 2018/12/12 屏蔽此状态，因为蓝牙与网络同时进行关锁，蓝牙关锁快的话网络就会202，会造成"data null"提示
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
     * Upload riding path and riding distance
     * 上传骑行路径与骑行距离
     * @param rideId
     * @param outArea
     * @param orbit
     * @param distance
     * @param lat
     * @param lng
     * @param flag
     */
    public void updateRideInfo(String token,String rideId, String outArea, String orbit, double distance, double lat, double lng, final int flag){
        RetrofitHttp.getRetrofit(0).create(MainApi.class)
                .updateRideInfo(MainParameter.updateRideInfoParameter(token,rideId,outArea,orbit,distance,lat,lng))
                .enqueue(new Callback<JSONObject>() {
                    @Override
                    public void onResponse(Call<JSONObject> call, Response<JSONObject> response) {
                        JSONObject jsonObject = response.body();
                        if (jsonObject != null){
                            LogUtils.i(TAG,"上传骑行路径与骑行距离 : "+jsonObject.toString());
                            callBack.onSuccess(jsonObject,flag);
                        }else{
                            CommonUtils.onFailure(mContext, 500, TAG);
                        }
                    }

                    @Override
                    public void onFailure(Call<JSONObject> call, Throwable t) {
                        CommonUtils.serviceError(mContext,t);
                    }
                });
    }

    /**
     * End of cycling evaluation
     * 骑行结束评价
     * @param rideId 骑行ID Riding ID
     * @param star 星级 Star
     * @param content 内容 content
     * @param flag
     */
    public void rideEndRate(String token,String rideId,String star,String content,String number,String issueType,final int flag){
        RequestDialog.show(mContext);
        RetrofitHttp.getRetrofit(0).create(MainApi.class)
                .rideEndRate(MainParameter.rideEndRateParameter(token,rideId,star,content,number,issueType))
                .enqueue(new Callback<JSONObject>() {
                    @Override
                    public void onResponse(Call<JSONObject> call, Response<JSONObject> response) {
                        RequestDialog.dismiss(mContext);
                        JSONObject jsonObject = response.body();
                        if (jsonObject != null){
                            LogUtils.i(TAG,"骑行结束评价 : "+jsonObject.toString());
                            try {
                                int code = jsonObject.getInt("code");
                                if (code == 200){
                                    String result = jsonObject.getString("data");
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

    /**
     * Bicycle unlock
     * 单车开锁
     * @param number
     * @param lat
     * @param lng
     * @param inputNumber
     * @param rideUser
     * @param flag
     */
    public void unLocking(String token,String number,String lat,String lng,String inputNumber,String rideUser,final int flag){
        RetrofitHttp.getRetrofit(0).create(MainApi.class)
                .unLocking(MainParameter.unLockingParameter(token,number,lat,lng,inputNumber,rideUser))
                .enqueue(new Callback<JSONObject>() {
                    @Override
                    public void onResponse(Call<JSONObject> call, Response<JSONObject> response) {
                        JSONObject jsonObject = response.body();
                        if (jsonObject != null){
                            LogUtils.i(TAG,"单车开锁---POST："+jsonObject.toString());
                            callBack.onSuccess(jsonObject,flag);
                        }else{
                            CommonUtils.onFailure(mContext, 500, TAG);
                        }
                    }

                    @Override
                    public void onFailure(Call<JSONObject> call, Throwable t) {
                        CommonUtils.serviceError(mContext,t);
                    }
                });
    }

    /**
     * Unlocking failed to end unlocking
     * 开锁失败结束开锁
     * @param date
     */
    public void unLockFail(String token,String date,final int flag){
        RetrofitHttp.getRetrofit(0).create(MainApi.class)
                .closeUnlocking(MainParameter.unLockFailParameter(token,date))
                .enqueue(new Callback<JSONObject>() {
                    @Override
                    public void onResponse(Call<JSONObject> call, Response<JSONObject> response) {
                        JSONObject retJson = response.body();
                        if (retJson != null) {
                            LogUtils.i(TAG, "调用开锁失败接口成功: " + retJson.toString());
                            try {
                                int code = retJson.getInt("code");
                                if (code == 200){
                                    String result = retJson.getString("data");
                                    callBack.onSuccess(result,flag);
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
                        CommonUtils.serviceError(mContext,t);
                    }
                });
    }

    /**
     * Upload old data
     * 上传旧数据
     * @param number
     * @param power
     * @param uid
     * @param timestamp
     * @param runTime
     * @param flag
     */
    public void uploadOldData(String token,String number, String power, int uid, long timestamp, int runTime, final int flag){
        RetrofitHttp.getRetrofit(0).create(MainApi.class)
                .uploadOldData(MainParameter.uploadOldDataParameter(token,number,power,uid,timestamp,runTime))
                .enqueue(new Callback<JSONObject>() {
                    @Override
                    public void onResponse(Call<JSONObject> call, Response<JSONObject> response) {
                        JSONObject retJson = response.body();
                        if (retJson != null){
                            LogUtils.i(TAG, "上传旧数据 : " + retJson.toString());
                            callBack.onSuccess(retJson,flag);
                        }else{
                            CommonUtils.onFailure(mContext, 500, TAG);
                        }
                    }

                    @Override
                    public void onFailure(Call<JSONObject> call, Throwable t) {
                        CommonUtils.serviceError(mContext,t);
                    }
                });
    }

    /**
     * Notify the server that Bluetooth unlocked successfully
     * 通知服务器蓝牙开锁成功
     * @param timestamp
     * @param number
     * @param power
     * @param flag
     */
    public void bleUnlockSuccess(String token,long timestamp,String number,String power,final int flag){
        RetrofitHttp.getRetrofit(0).create(MainApi.class)
                .bleUnLockSuccess(MainParameter.bleUnlockSuccessParameter(token,timestamp,number,power))
                .enqueue(new Callback<JSONObject>() {
                    @Override
                    public void onResponse(Call<JSONObject> call, Response<JSONObject> response) {
                        JSONObject retJson = response.body();
                        if (retJson != null){
                            LogUtils.i(TAG, "通知服务器蓝牙开锁成功 : " + retJson.toString());
                            try {
                                int code = retJson.getInt("code");
                                if (code == 200){
                                    String result = retJson.getString("data");
                                    callBack.onSuccess(result,flag);
                                }else{
                                    CommonUtils.onFailure(mContext,code,TAG);
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
                        CommonUtils.serviceError(mContext,t);
                    }
                });
    }
}
