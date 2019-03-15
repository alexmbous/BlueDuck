package com.blueduck.ride.login.service;

import android.content.Context;
import android.os.AsyncTask;

import com.amazonaws.services.s3.model.PutObjectRequest;
import com.blueduck.ride.R;
import com.blueduck.ride.base.BaseService;
import com.blueduck.ride.login.bean.LoginBean;
import com.blueduck.ride.login.bean.RegionsBean;
import com.blueduck.ride.utils.CommonSharedValues;
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

import java.io.File;
import java.util.List;
import java.util.UUID;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginService extends BaseService {

    public LoginService(Context context, RequestCallBack callBack, String TAG) {
        super(context, callBack, TAG);
    }

    /**
     * 获得国家电话区域号
     * Get the national telephone area number
     * @param flag
     */
    public void getRegions(final int flag){
        RequestDialog.show(mContext);
        RetrofitHttp.getRetrofit(0).create(LoginApi.class)
                .getRegions(LoginParameter.getRegions())
                .enqueue(new Callback<JSONObject>() {
                    @Override
                    public void onResponse(Call<JSONObject> call, Response<JSONObject> response) {
                        RequestDialog.dismiss(mContext);
                        JSONObject jsonObject = response.body();
                        if (jsonObject != null){
                            LogUtils.i(TAG,"获得国家电话区域号："+jsonObject.toString());
                            try {
                                int code = jsonObject.getInt("code");
                                if (code == 200){
                                    Gson gson = new Gson();
                                    JSONArray jsonArray = jsonObject.getJSONArray("data");
                                    List<RegionsBean> list = gson.fromJson(jsonArray.toString(),new TypeToken<List<RegionsBean>>(){}.getType());
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
     * 校验账号是否已存在
     * Verify that the account already exists
     * @param account
     * @param flag
     */
    public void verifyAccount(String account, final int flag){
        RequestDialog.show(mContext);
        RetrofitHttp.getRetrofit(0).create(LoginApi.class)
                .verifyAccount(LoginParameter.verifyAccount(account))
                .enqueue(new Callback<JSONObject>() {
                    @Override
                    public void onResponse(Call<JSONObject> call, Response<JSONObject> response) {
                        RequestDialog.dismiss(mContext);
                        JSONObject jsonObject = response.body();
                        if (jsonObject != null){
                            LogUtils.i(TAG,"校验账号是否已存在 : "+jsonObject.toString());
                            try {
                                int code = jsonObject.getInt("code");
                                callBack.onSuccess(code,flag);
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
     * 登录
     * login
     * @param account
     * @param password
     * @param lat
     * @param lng
     * @param pushToken
     * @param deviceUUID
     * @param flag
     */
    public void login(String account,String password,double lat,double lng,String pushToken,String deviceUUID,final int flag){
        RequestDialog.show(mContext);
        RetrofitHttp.getRetrofit(0).create(LoginApi.class)
                .login(LoginParameter.login(account,password,lat,lng,pushToken,deviceUUID))
                .enqueue(new Callback<JSONObject>() {
                    @Override
                    public void onResponse(Call<JSONObject> call, Response<JSONObject> response) {
                        RequestDialog.dismiss(mContext);
                        JSONObject retJson = response.body();
                        if (retJson != null){
                            LogUtils.i(TAG,"登录："+retJson.toString());
                            Gson gson = new Gson();
                            try {
                                int code = retJson.getInt("code");
                                if (code == 200){
                                    JSONObject jsonObject = retJson.getJSONObject("data");
                                    LoginBean loginBean = gson.fromJson(jsonObject.toString(), LoginBean.class);
                                    callBack.onSuccess(loginBean,flag);
                                }else if (code == 202){//账号或密码错误 Incorrect username or password
                                    CommonUtils.hintDialog(mContext,mContext.getString(R.string.incorrect_username_or_password));
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
     * Get the mailbox verification code
     * 获得邮箱验证码
     */
    public void getEmailCode(String email, String emailType,final int flag){
        RequestDialog.show(mContext);
        RetrofitHttp.getRetrofit(0).create(LoginApi.class)
                .getEmailCode(LoginParameter.emailCodeParameter(email,emailType))
                .enqueue(new Callback<JSONObject>() {
                    @Override
                    public void onResponse(Call<JSONObject> call, Response<JSONObject> response) {
                        analysisData(response,flag);
                    }

                    @Override
                    public void onFailure(Call<JSONObject> call, Throwable t) {
                        RequestDialog.dismiss(mContext);
                        CommonUtils.serviceError(mContext,t);
                    }
                });
    }

    /**
     * 获得短信验证码
     * Get SMS verification code
     * @param phone
     * @param regions
     * @param smsType
     * @param flag
     */
    public void getPhoneCode(String phone, String regions, String smsType,final int flag){
        RequestDialog.show(mContext);
        RetrofitHttp.getRetrofit(0).create(LoginApi.class)
                .getPhoneCode(LoginParameter.phoneCodeParameter(phone,regions,smsType))
                .enqueue(new Callback<JSONObject>() {
                    @Override
                    public void onResponse(Call<JSONObject> call, Response<JSONObject> response) {
                        analysisData(response,flag);
                    }

                    @Override
                    public void onFailure(Call<JSONObject> call, Throwable t) {
                        RequestDialog.dismiss(mContext);
                        CommonUtils.serviceError(mContext,t);
                    }
                });
    }

    /**
     * Analytical data
     * 解析数据
     * @param response
     */
    private void analysisData(Response<JSONObject> response,int flag){
        RequestDialog.dismiss(mContext);
        JSONObject jsonObject = response.body();
        if (jsonObject != null) {
            LogUtils.i(TAG,"获得验证码："+jsonObject.toString());
            try {
                int code = jsonObject.getInt("code");
                if (code == 200) {
                    String data = jsonObject.getString("data");
                    if ("1".equals(data)){
                        int invalidMinute = jsonObject.getInt("invalid_minute");
                        callBack.onSuccess(invalidMinute,flag);
                    }else{
                        CommonUtils.hintDialog(mContext, mContext.getString(R.string.fail_to_get));
                    }
                }else if (code == 202) {//该用户不存在 The user already exists
                    CommonUtils.hintDialog(mContext,mContext.getString(R.string.account_does_not_exist));
                }else if (code == 203) {//该用户已存在 The user already exists
                    CommonUtils.hintDialog(mContext, mContext.getString(R.string.user_exists));
                }else if (code == 205) {//您今日的短信次数已用完 Your text message has been used up today
                    CommonUtils.hintDialog(mContext, mContext.getString(R.string.max_message_count));
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

    /**
     * register log in
     * 注册登录
     * @param account
     * @param code
     * @param lat
     * @param lng
     */
    public void registerLogin(String account, String code, double lat, double lng, String pushToken,String deviceUUID, final int flag){
        RequestDialog.show(mContext);
        RetrofitHttp.getRetrofit(0).create(LoginApi.class)
                .login(LoginParameter.registerLoginParameter(account,code,lat,lng,pushToken,deviceUUID))
                .enqueue(new Callback<JSONObject>() {
                    @Override
                    public void onResponse(Call<JSONObject> call, Response<JSONObject> response) {
                        RequestDialog.dismiss(mContext);
                        JSONObject retJson = response.body();
                        if (retJson != null) {
                            LogUtils.i(TAG,"注册登录："+retJson.toString());
                            Gson gson = new Gson();
                            try {
                                int code = retJson.getInt("code");
                                if (code == 200) {
                                    JSONObject jsonObject = retJson.getJSONObject("data");
                                    LoginBean loginBean = gson.fromJson(jsonObject.toString(), LoginBean.class);
                                    callBack.onSuccess(loginBean,flag);
                                } else if (code == 204) {//短信验证码无效或已过期 SMS verification code is invalid or has expired
                                    CommonUtils.hintDialog(mContext, mContext.getString(R.string.invalid_code));
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

    /**
     * 上传用户信息
     * Upload user information
     * @param token
     * @param firstName
     * @param lastName
     * @param email
     * @param password
     * @param birthday
     * @param type
     * @param flag
     */
    public void uploadUserInfo(String token, String firstName, String lastName, String email, String phone, String password, String birthday, final String type, final int flag){
        RequestDialog.show(mContext);
        RetrofitHttp.getRetrofit(0).create(LoginApi.class)
                .uploadUserInfo(LoginParameter.uploadUserInfo(token,firstName,lastName,email,phone,password,birthday,type))
                .enqueue(new Callback<JSONObject>() {
                    @Override
                    public void onResponse(Call<JSONObject> call, Response<JSONObject> response) {
                        RequestDialog.dismiss(mContext);
                        JSONObject retJson = response.body();
                        if (retJson != null){
                            LogUtils.i(TAG,"上传用户信息："+retJson.toString());
                            try {
                                int code = retJson.getInt("code");
                                if (code == 200){
                                    String result = "";
                                    if ("1".equals(type)) {
                                        JSONObject jsonObject = retJson.getJSONObject("data");
                                        result = jsonObject.getString("result");
                                    }else{
                                        result = retJson.getString("data");
                                    }
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
     * Modify user avatar
     * 修改用户头像
     * @param url
     * @param flag
     */
    public void updatePhoto(String token,String url, final int flag){
        RequestDialog.show(mContext);
        RetrofitHttp.getRetrofit(0).create(LoginApi.class)
                .updateUserInfo(LoginParameter.updatePhoto(token,url))
                .enqueue(new Callback<JSONObject>() {
                    @Override
                    public void onResponse(Call<JSONObject> call, Response<JSONObject> response) {
                        RequestDialog.dismiss(mContext);
                        JSONObject retJson = response.body();
                        if (retJson != null){
                            LogUtils.i(TAG,"修改用户头像 : "+retJson.toString());
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

    /**
     * 保存银行卡
     * Save bank card
     * @param token
     * @param tokenId
     * @param flag
     */
    public void saveCard(String token, String tokenId, final int flag){
        RequestDialog.show(mContext);
        RetrofitHttp.getRetrofit(0).create(LoginApi.class)
                .saveCard(LoginParameter.saveCard(token,tokenId))
                .enqueue(new Callback<JSONObject>() {
                    @Override
                    public void onResponse(Call<JSONObject> call, Response<JSONObject> response) {
                        RequestDialog.dismiss(mContext);
                        JSONObject retJson = response.body();
                        if (retJson != null){
                            LogUtils.i(TAG, "保存银行卡 : " + retJson.toString());
                            try {
                                int code = retJson.getInt("code");
                                if (code == 200){
                                    String result = retJson.getString("data");
                                    if ("1".equals(result)){
                                        callBack.onSuccess(result,flag);
                                    }else{
                                        CommonUtils.hintDialog(mContext,mContext.getString(R.string.operation_failed));
                                    }
                                }else if (code == 40004){//保存银行卡异常 save card error
                                    String error = retJson.getString("error");
                                    CommonUtils.hintDialog(mContext,error);
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
                        RequestDialog.dismiss(mContext);
                        CommonUtils.serviceError(mContext,t);
                    }
                });
    }

    /**
     * 修改密码
     * change password
     * @param account
     * @param code
     * @param flag
     */
    public void changePassword(String account, String code, final int flag){
        RequestDialog.show(mContext);
        RetrofitHttp.getRetrofit(0).create(LoginApi.class)
                .login(LoginParameter.changePassword(account,code))
                .enqueue(new Callback<JSONObject>() {
                    @Override
                    public void onResponse(Call<JSONObject> call, Response<JSONObject> response) {
                        RequestDialog.dismiss(mContext);
                        JSONObject retJson = response.body();
                        if (retJson != null){
                            LogUtils.i(TAG,"修改密码："+retJson.toString());
                            try {
                                int code = retJson.getInt("code");
                                if (code == 200){
                                    String token = retJson.getString("data");
                                    callBack.onSuccess(token,flag);
                                }else if (code == 204) {//短信验证码无效或已过期 SMS verification code is invalid or has expired
                                    CommonUtils.hintDialog(mContext, mContext.getString(R.string.invalid_code));
                                }else if (code == 202){//账号不存在 Account does not exist
                                    CommonUtils.hintDialog(mContext, mContext.getString(R.string.account_does_not_exist));
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
     * Amazon asynchronously uploads user avatars
     * 亚马逊异步上传用户头像
     * @param imagePath
     */
    public void amazonS3Upload(String imagePath,int flag){
        new S3Example(flag).execute(imagePath);
    }

    private class S3Example extends AsyncTask<String, Void, String> {

        String uuid = "";
        int flag = 0;

        public S3Example(int flag) {
            this.flag = flag;
            RequestDialog.show(mContext);
            uuid = UUID.randomUUID().toString().toUpperCase();
        }

        @Override
        protected String doInBackground(String... strings) {
            File file = new File(strings[0]);
            PutObjectRequest putObjectRequest = new PutObjectRequest(CommonSharedValues.AMAZONS3_BUCKET_NAME, uuid + ".jpg", file);
            CommonUtils.getS3Client().putObject(putObjectRequest);
            return "UPLOAD_SUCCESS";
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            RequestDialog.dismiss(mContext);
            if ("UPLOAD_SUCCESS".equals(s)) {
                String url = CommonSharedValues.AMAZONS3_IMAGE_PATH_PREFIX + uuid + ".jpg";
                LogUtils.i(TAG, "onPostExecute: ----------上传ok---------" + url);
                callBack.onSuccess(url,flag);
            }
        }
    }
}
