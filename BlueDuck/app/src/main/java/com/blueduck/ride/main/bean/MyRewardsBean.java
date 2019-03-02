package com.blueduck.ride.main.bean;

import java.io.Serializable;

/**
 * My riding volume entity
 * 我的骑行卷实体
 */
public class MyRewardsBean implements Serializable{

    private String uid;
    private String id;
    private String end_time;
    private CouponVo couponVo;
    private String start_time;
    private String active_date;
    private String used;
    private String date;
    private String cid;

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getEnd_time() {
        return end_time;
    }

    public void setEnd_time(String end_time) {
        this.end_time = end_time;
    }

    public CouponVo getCouponVo() {
        return couponVo;
    }

    public void setCouponVo(CouponVo couponVo) {
        this.couponVo = couponVo;
    }

    public String getStart_time() {
        return start_time;
    }

    public void setStart_time(String start_time) {
        this.start_time = start_time;
    }

    public String getActive_date() {
        return active_date;
    }

    public void setActive_date(String active_date) {
        this.active_date = active_date;
    }

    public String getUsed() {
        return used;
    }

    public void setUsed(String used) {
        this.used = used;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getCid() {
        return cid;
    }

    public void setCid(String cid) {
        this.cid = cid;
    }

}
