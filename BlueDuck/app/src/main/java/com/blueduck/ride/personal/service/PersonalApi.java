package com.blueduck.ride.personal.service;

import org.json.JSONObject;

import java.util.Map;

import retrofit2.Call;
import retrofit2.http.FieldMap;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

public interface PersonalApi {

    /**
     * 重置新密码/修改密码
     * Reset new password / change password
     * @param map
     * @return
     */
    @FormUrlEncoded
    @POST("app/user")
    Call<JSONObject> resetNewPassword(@FieldMap Map<String,String> map);

}
