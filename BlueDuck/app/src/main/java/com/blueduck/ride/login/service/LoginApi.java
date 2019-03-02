package com.blueduck.ride.login.service;

import org.json.JSONObject;

import java.util.Map;

import retrofit2.Call;
import retrofit2.http.FieldMap;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.QueryMap;

public interface LoginApi {
    /**
     * 刷新token
     * Refresh token
     * @param map
     * @return
     */
    @GET("app/user")
    Call<JSONObject> refreshToken(@QueryMap Map<String,String> map);

    /**
     * 校验账号是否已存在
     * Verify that the account already exists
     * @param map
     * @return
     */
    @FormUrlEncoded
    @POST("app/login")
    Call<JSONObject> verifyAccount(@FieldMap Map<String,String> map);

    /**
     * 注册/登录/忘记密码
     * Register / login / forget password
     * @param map
     * @return
     */
    @FormUrlEncoded
    @POST("app/login")
    Call<JSONObject> login(@FieldMap Map<String,String> map);

    /**
     * 获得邮箱验证码
     * Get the mailbox verification code
     * @param map
     * @return
     */
    @POST("other")
    Call<JSONObject> getEmailCode(@QueryMap Map<String,String> map);

    /**
     * 上传用户信息
     * Upload user information
     * @param map
     * @return
     */
    @FormUrlEncoded
    @POST("app/user")
    Call<JSONObject> uploadUserInfo(@FieldMap Map<String,String> map);

    /**
     * 修改用户基础信息
     * Modify user basic information
     * @param map
     * @return
     */
    @FormUrlEncoded
    @POST("app/user")
    Call<JSONObject> updateUserInfo(@FieldMap Map<String,String> map);

    /**
     * 保存银行卡
     * Save bank card
     * @param map
     * @return
     */
    @FormUrlEncoded
    @POST("app/pay")
    Call<JSONObject> saveCard(@FieldMap Map<String,String> map);
}
