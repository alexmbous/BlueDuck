package com.blueduck.ride.main.bean;

import com.blueduck.ride.main.bean.UserInfo;

public class UserInfoBean {

    private UserInfo userInfo;
    private String couponCount;//优惠券数量 Number of coupons

    public UserInfo getUserInfo() {
        return userInfo;
    }

    public void setUserInfo(UserInfo userInfo) {
        this.userInfo = userInfo;
    }

    public String getCouponCount() {
        return couponCount;
    }

    public void setCouponCount(String couponCount) {
        this.couponCount = couponCount;
    }
}
