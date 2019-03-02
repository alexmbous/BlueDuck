package com.blueduck.ride.main.bean;

import java.io.Serializable;

/**
 * Bicycle current usage information (driving end internal entity)
 * 单车当前使用信息(行驶结束内部实体)
 */
public class FinishTradeVo implements Serializable{

    private String amount;
    private FinishBikeUseVo bikeUseVo;
    private String date;
    private String id;
    private String status;
    private String type;
    private String uid;
    private String way;
    private String typeStr;
    private String wayStr;
    private String out_pay_id;
    private String free;

    public String getTypeStr() {
        return typeStr;
    }

    public void setTypeStr(String typeStr) {
        this.typeStr = typeStr;
    }

    public String getWayStr() {
        return wayStr;
    }

    public void setWayStr(String wayStr) {
        this.wayStr = wayStr;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public FinishBikeUseVo getBikeUseVo() {
        return bikeUseVo;
    }

    public void setBikeUseVo(FinishBikeUseVo bikeUseVo) {
        this.bikeUseVo = bikeUseVo;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getWay() {
        return way;
    }

    public void setWay(String way) {
        this.way = way;
    }

    public String getOut_pay_id() {
        return out_pay_id;
    }

    public void setOut_pay_id(String out_pay_id) {
        this.out_pay_id = out_pay_id;
    }

    public String getFree() {
        return free;
    }

    public void setFree(String free) {
        this.free = free;
    }

}
