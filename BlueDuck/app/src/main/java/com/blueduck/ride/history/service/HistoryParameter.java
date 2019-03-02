package com.blueduck.ride.history.service;

import com.blueduck.ride.utils.CommonSharedValues;

import java.util.HashMap;
import java.util.Map;

public class HistoryParameter {

    /**
     * 获得历史数据
     * Get historical data
     * @param token
     * @param pageNo
     * @return
     */
    public static Map<String,String> getHistory(String token, int pageNo){
        Map<String,String> map = new HashMap<>();
        map.put("requestType", "30023");
        map.put("industryType", CommonSharedValues.industryType);
        map.put("pageNo", pageNo + "");
        map.put("token", token);
        return map;
    }
}
