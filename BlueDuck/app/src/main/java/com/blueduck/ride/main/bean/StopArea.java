package com.blueduck.ride.main.bean;

import java.util.List;

/**
 * Red envelope bicycle and ordinary bicycle area entity
 * 红包单车与普通单车区域实体
 */
public class StopArea {

    //Red envelope bicycle or ordinary bicycle collection returned by the server
    private List<StopAreaBean> list;//服务器返回的红包单车或普通单车集合
    private boolean isRedBike;//是否红包单车 Whether red envelope bicycle

    public List<StopAreaBean> getList() {
        return list;
    }

    public void setList(List<StopAreaBean> list) {
        this.list = list;
    }

    public boolean isRedBike() {
        return isRedBike;
    }

    public void setRedBike(boolean redBike) {
        isRedBike = redBike;
    }
}
