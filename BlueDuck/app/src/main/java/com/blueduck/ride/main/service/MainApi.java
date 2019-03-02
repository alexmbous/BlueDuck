package com.blueduck.ride.main.service;

import org.json.JSONObject;

import java.util.Map;

import retrofit2.Call;
import retrofit2.http.FieldMap;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.QueryMap;

public interface MainApi {

    @GET("app/user")
    Call<JSONObject> getUserInfo(@QueryMap Map<String,String> map);//获得个人信息 Get personal information
    @GET("app/bike")
    Call<JSONObject> getBikeUseInfo(@QueryMap Map<String, String> map);//获得单车的使用信息 Get information on the use of bicycles
    @GET("app/bike")
    Call<JSONObject> getBike(@QueryMap Map<String, String> map);//获得附近的单车 Get a nearby bicycle
    @GET("app/bike")
    Call<JSONObject> getStopArea(@QueryMap Map<String,String> map);//获取单车停车区域 Get a bicycle parking area
    @GET("app/bike")
    Call<JSONObject> debLocking(@QueryMap Map<String, String> map);//单车解锁 Bicycle unlock
    @POST("app/bike")
    Call<JSONObject> uploadOldData(@QueryMap Map<String, String> map);//骑行结束/上传旧数据（蓝牙版）End of the ride / upload old data (Bluetooth version)
    @POST("app/bike")
    Call<JSONObject> scooterLocking(@QueryMap Map<String, String> map);//滑板车网络关锁 Scooter network lock
    @FormUrlEncoded
    @POST("app/bike")
    Call<JSONObject> updateRideInfo(@FieldMap Map<String, String> map);//更新骑行信息 Update cycling information
    @FormUrlEncoded
    @POST("app/bike")
    Call<JSONObject> rideEndRate(@FieldMap Map<String, String> map);//骑行结束评价 End of cycling evaluation
    @FormUrlEncoded
    @POST("app/bike")
    Call<JSONObject> unLocking(@FieldMap Map<String, String> map);//单车开锁 Bicycle unlock
    @POST("app/bike")
    Call<JSONObject> closeUnlocking(@QueryMap Map<String, String> map);//结束开锁 End unlocking
    @POST("app/bike")
    Call<JSONObject> bleUnLockSuccess(@QueryMap Map<String, String> map);//蓝牙开锁成功（蓝牙版）Bluetooth unlocked successfully (Bluetooth version)
}
