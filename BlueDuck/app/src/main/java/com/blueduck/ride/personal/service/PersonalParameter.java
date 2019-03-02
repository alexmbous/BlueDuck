package com.blueduck.ride.personal.service;

import com.blueduck.ride.utils.CommonUtils;

import java.util.HashMap;
import java.util.Map;

public class PersonalParameter {

    /**
     * 设置密码
     * set a password
     * @param token
     * @param password
     * @return
     */
    public static Map<String,String> setPassword(String token, String password){
        Map<String,String> map = new HashMap<>();
        map.put("requestType","20022");
        map.put("token",token);
        map.put("password", CommonUtils.md5(password));
        return map;
    }
}
