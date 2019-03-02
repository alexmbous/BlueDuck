package com.blueduck.ride.billing.service;

import org.json.JSONObject;

import java.util.Map;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.QueryMap;

public interface BillingApi {

    /**
     * Get a list of bank cards
     * @param map
     * @return
     */
    @GET("app/pay")
    Call<JSONObject> getCardList(@QueryMap Map<String,String> map);//获得银行卡列表

    /**
     * Set default or delete bank card
     * @param map
     * @return
     */
    @POST("app/pay")
    Call<JSONObject> setDefaultAndDeleteCard(@QueryMap Map<String,String> map);//设置默认或者删除银行卡
}
