package com.blueduck.ride.history.service;

import org.json.JSONObject;

import java.util.Map;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.QueryMap;

public interface HistoryApi {

    /**
     * Get cycling history
     * @param map
     * @return
     */
    @GET("app/bike")
    Call<JSONObject> getHistory(@QueryMap Map<String,String> map);//获得骑行历史记录
}
