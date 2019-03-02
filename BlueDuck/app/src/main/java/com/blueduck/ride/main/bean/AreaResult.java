package com.blueduck.ride.main.bean;

import java.util.List;

/**
 * Request a bicycle parking area result entity
 * (this class is just to facilitate the request to call back and no other use)
 * 请求单车停车区域结果实体(此类只是方便请求结果回调用，并没有其他用途)
 */
public class AreaResult {

    private List<StopAreaBean> list;
    private String ids;
    private double targetLat;
    private double targetLng;

    public List<StopAreaBean> getList() {
        return list;
    }

    public void setList(List<StopAreaBean> list) {
        this.list = list;
    }

    public String getIds() {
        return ids;
    }

    public void setIds(String ids) {
        this.ids = ids;
    }

    public double getTargetLat() {
        return targetLat;
    }

    public void setTargetLat(double targetLat) {
        this.targetLat = targetLat;
    }

    public double getTargetLng() {
        return targetLng;
    }

    public void setTargetLng(double targetLng) {
        this.targetLng = targetLng;
    }
}
