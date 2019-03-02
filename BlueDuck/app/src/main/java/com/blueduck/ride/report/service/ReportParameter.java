package com.blueduck.ride.report.service;


import android.text.TextUtils;

import java.util.HashMap;
import java.util.Map;

public class ReportParameter {

    /**
     * 获得故障列表
     * Get a failure list
     * @param token
     * @return
     */
    public static Map<String,String> getReport(String token){
        Map<String,String> map = new HashMap<>();
        map.put("requestType", "30035");
        map.put("token", token);
        return map;
    }

    /**
     * Submit fault feedback
     * 提交故障反馈
     * @param token
     * @param number
     * @param type
     * @param lat
     * @param lng
     * @param content
     * @return
     */
    public static Map<String,String> submitReport(String token, String number, String type, double lat, double lng, String content){
        Map<String,String> map = new HashMap<>();
        map.put("requestType","30016");
        map.put("token",token);
        map.put("number",number);
        map.put("type",type);
        map.put("lat",lat+"");
        map.put("lng",lng+"");
        if (!TextUtils.isEmpty(content)){
            map.put("content",content);
        }
        return map;
    }
}
