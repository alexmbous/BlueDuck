package com.blueduck.ride.billing.service;

import java.util.HashMap;
import java.util.Map;

public class BillingParameter {

    /**
     * Get card list
     * 获得卡列表
     * @param token
     * @return
     */
    public static Map<String,String> cardListParameter(String token){
        Map<String,String> map = new HashMap<>();
        map.put("requestType","40009");
        map.put("token",token);
        map.put("payType","6");
        return map;
    }

    /**
     * 设置默认或者删除银行卡
     * Set default or delete bank card
     * @param token
     * @param cardId
     * @param type
     * @return
     */
    public static Map<String,String> setDefaultAndDeleteCard(String token,String cardId,int type){
        Map<String,String> map = new HashMap<>();
        if (type == 0) {
            map.put("requestType", "40012");
        }else if (type == 1){
            map.put("requestType", "40014");
        }
        map.put("token",token);
        map.put("customerPaymentProfileId",cardId);
        map.put("payType","6");
        return map;
    }
}
