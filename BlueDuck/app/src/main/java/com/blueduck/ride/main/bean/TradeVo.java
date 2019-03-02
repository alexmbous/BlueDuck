package com.blueduck.ride.main.bean;

import java.io.Serializable;

/**
 * Bicycle current use information (riding the end of the internal entity)
 * 单车当前使用信息(骑行结束内部实体)
 */
public class TradeVo implements Serializable {

    private String uid;
    private String amount;
    private BikeUseVo bikeUseVo;
    private String id;
    private String type;
    private String way;
    private String status;
    private String date;
    private String typeStr;
    private String wayStr;
    private String out_pay_id;
    private String free;

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

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

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public BikeUseVo getBikeUseVo() {
        return bikeUseVo;
    }

    public void setBikeUseVo(BikeUseVo bikeUseVo) {
        this.bikeUseVo = bikeUseVo;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getWay() {
        return way;
    }

    public void setWay(String way) {
        this.way = way;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
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
