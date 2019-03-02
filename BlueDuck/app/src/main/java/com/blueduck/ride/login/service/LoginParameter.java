package com.blueduck.ride.login.service;

import com.blueduck.ride.utils.AESUtil;
import com.blueduck.ride.utils.CommonSharedValues;
import com.blueduck.ride.utils.CommonUtils;

import java.util.HashMap;
import java.util.Map;

public class LoginParameter {

    /**
     * 校验账号是否已存在
     * Verify that the account already exists
     * @param account
     * @return
     */
    public static Map<String,String> verifyAccount(String account){
        Map<String,String> map = new HashMap<>();
        map.put("requestType","10005");
        map.put("industryId", CommonSharedValues.industryType);
        map.put("account",account);
        return map;
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
     * @return
     */
    public static Map<String,String> login(String account,String password,double lat,double lng,String pushToken,String deviceUUID){
        Map<String,String> map = new HashMap<>();
        map.put("industryType", CommonSharedValues.industryType);
        map.put("requestType","10001");
        map.put("phone",account);
        map.put("password", CommonUtils.md5(password));
        map.put("deviceType","1");
        map.put("deviceToken",pushToken);
        map.put("deviceUUID",deviceUUID);
        if (lat != 0 || lng != 0){
            map.put("lat",lat+"");
            map.put("lng",lng+"");
        }
        return map;
    }

    /**
     * Mailbox verification code parameters
     * 邮箱验证码参数
     * @param email
     * @param emailType
     * @return
     */
    public static Map<String,String> emailCodeParameter(String email,String emailType){
        Map<String,String> map = new HashMap<>();
        map.put("industryId", CommonSharedValues.industryType);
        map.put("requestType", "50008");
        map.put("email", AESUtil.aesEncrypt(email, AESUtil.PHONE_KEY));
        map.put("emailType", emailType);
        return map;
    }

    /**
     * 注册登录
     * register log in
     * @param account
     * @param code
     * @param lat
     * @param lng
     * @param pushToken
     * @param deviceUUID
     * @return
     */
    public static Map<String,String> registerLoginParameter(String account,String code,double lat,double lng,String pushToken,String deviceUUID){
        Map<String,String> map = new HashMap<>();
        map.put("industryType", CommonSharedValues.industryType);
        map.put("requestType","10002");
        map.put("phone",account);
        map.put("code",code);
        map.put("deviceType","1");
        map.put("deviceToken",pushToken);
        map.put("deviceUUID",deviceUUID);
        if (lat != 0 || lng != 0){
            map.put("lat",lat+"");
            map.put("lng",lng+"");
        }
        return map;
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
     * @return
     */
    public static Map<String,String> uploadUserInfo(String token,String firstName,String lastName,String email,String password,String birthday,String type){
        Map<String,String> map = new HashMap<>();
        map.put("requestType","20005");
        map.put("token",token);
        map.put("firstName",firstName);
        map.put("lastName",lastName);
        map.put("email",email);
        map.put("password",CommonUtils.md5(password));
        map.put("birthday",birthday);
        map.put("type",type);
        return map;
    }

    /**
     * Modify user avatar
     * 修改用户头像
     * @param token
     * @param url
     * @return
     */
    public static Map<String,String> updatePhoto(String token,String url){
        Map<String,String> map = new HashMap<>();
        map.put("requestType","20002");
        map.put("token",token);
        map.put("headUrl",url);
        return map;
    }

    /**
     * 保存银行卡
     * Save bank card
     * @param token
     * @param tokenId
     * @return
     */
    public static Map<String,String> saveCard(String token,String tokenId){
        Map<String,String> map = new HashMap<>();
        map.put("requestType","40010");
        map.put("token",token);
        map.put("tokenId", tokenId);
        map.put("payType","6");
        return map;
    }

    /**
     * 修改密码
     * change password
     * @param account
     * @param code
     * @return
     */
    public static Map<String,String> changePassword(String account,String code){
        Map<String,String> map = new HashMap<>();
        map.put("industryType", CommonSharedValues.industryType);
        map.put("requestType","10004");
        map.put("phone",account);
        map.put("code",code);
        return map;
    }
}
