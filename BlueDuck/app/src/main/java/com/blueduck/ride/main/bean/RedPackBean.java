package com.blueduck.ride.main.bean;

/**
 * Nearby bicycle red envelope entity
 * 附近单车内部红包实体
 */
public class RedPackBean {

    private String couponName;
    private String coupon_id;
    private String area_ids;
    private String coupon_num;
    private String type;
    private String date;
    private String id;
    private String end_time;
    private String must_in_area;
    private String start_time;

    public String getCouponName() {
        return couponName;
    }

    public void setCouponName(String couponName) {
        this.couponName = couponName;
    }

    public String getCoupon_id() {
        return coupon_id;
    }

    public void setCoupon_id(String coupon_id) {
        this.coupon_id = coupon_id;
    }

    public String getArea_ids() {
        return area_ids;
    }

    public void setArea_ids(String area_ids) {
        this.area_ids = area_ids;
    }

    public String getCoupon_num() {
        return coupon_num;
    }

    public void setCoupon_num(String coupon_num) {
        this.coupon_num = coupon_num;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
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

    public String getEnd_time() {
        return end_time;
    }

    public void setEnd_time(String end_time) {
        this.end_time = end_time;
    }

    public String getMust_in_area() {
        return must_in_area;
    }

    public void setMust_in_area(String must_in_area) {
        this.must_in_area = must_in_area;
    }

    public String getStart_time() {
        return start_time;
    }

    public void setStart_time(String start_time) {
        this.start_time = start_time;
    }

}
