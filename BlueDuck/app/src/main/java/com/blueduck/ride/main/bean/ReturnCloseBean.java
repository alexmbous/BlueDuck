package com.blueduck.ride.main.bean;

import org.json.JSONObject;

/**
 * Upload server lock result entity
 * (the purpose is to facilitate the request result to call back, and no other purpose)
 * 上传服务器关锁结果实体（目的为方便请求结果回调用，并没有其他用途）
 */
public class ReturnCloseBean {

    private JSONObject jsonObject;
    private int uid;

    public JSONObject getJsonObject() {
        return jsonObject;
    }

    public void setJsonObject(JSONObject jsonObject) {
        this.jsonObject = jsonObject;
    }

    public int getUid() {
        return uid;
    }

    public void setUid(int uid) {
        this.uid = uid;
    }
}
