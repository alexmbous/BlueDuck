package com.blueduck.ride.report.service;

import org.json.JSONObject;

import java.util.Map;

import retrofit2.Call;
import retrofit2.http.FieldMap;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.QueryMap;

public interface ReportApi {

    /**
     * 获得故障列表
     * Get a failure list
     * @param map
     * @return
     */
    @GET("app/bike")
    Call<JSONObject> getReport(@QueryMap Map<String,String> map);//获得故障列表

    /**
     * Submit fault feedback
     * @param map
     * @return
     */
    @FormUrlEncoded
    @POST("app/bike")
    Call<JSONObject> submitReport(@FieldMap Map<String,String> map);//提交故障意见
}
