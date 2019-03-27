package com.blueduck.ride.main.bean;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterItem;

public class MyItem implements ClusterItem {
    private final LatLng mPosition;
    private int priceType; // 价格类型 Price type
    private String  price ; // 单价 unit price
    private String number; // 单车编号 Bicycle number
    private int bid;

    private String least_use_time;
    private String free_use_time;
    private int max_amount;
    private RedPackBean redpackRuleVo;//红包实体 Red envelope entity
    private int readpack;//是否红包单车，0普通，1红包 Whether red envelope bicycle, 0 ordinary, 1 red envelope
    private int typeCount;//多少分钟一块钱 How many minutes a dollar
    private int bikeType;//车类型 1：单车 2 ：滑板车 Car type 1: bicycle 2: scooter
    private String areaId;//单车所属区域Id Bicycle area Id
    private int powerPercent;

    public MyItem(double lat, double lng){
        mPosition = new LatLng(lat,lng);
    }

    public LatLng getmPosition() {
        return mPosition;
    }

    public int getPriceType() {
        return priceType;
    }

    public void setPriceType(int priceType) {
        this.priceType = priceType;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public int getBid() {
        return bid;
    }

    public void setBid(int bid) {
        this.bid = bid;
    }

    @Override
    public LatLng getPosition() {
        return mPosition;
    }

    public String getLeast_use_time() {
        return least_use_time;
    }

    public void setLeast_use_time(String least_use_time) {
        this.least_use_time = least_use_time;
    }

    public String getFree_use_time() {
        return free_use_time;
    }

    public void setFree_use_time(String free_use_time) {
        this.free_use_time = free_use_time;
    }

    public int getMax_amount() {
        return max_amount;
    }

    public void setMax_amount(int max_amount) {
        this.max_amount = max_amount;
    }

    public RedPackBean getRedpackRuleVo() {
        return redpackRuleVo;
    }

    public void setRedpackRuleVo(RedPackBean redpackRuleVo) {
        this.redpackRuleVo = redpackRuleVo;
    }

    public int getReadpack() {
        return readpack;
    }

    public void setReadpack(int readpack) {
        this.readpack = readpack;
    }

    public int getTypeCount() {
        return typeCount;
    }

    public void setTypeCount(int typeCount) {
        this.typeCount = typeCount;
    }

    public int getBikeType() {
        return bikeType;
    }

    public void setBikeType(int bikeType) {
        this.bikeType = bikeType;
    }

    public String getAreaId() {
        return areaId;
    }

    public void setAreaId(String areaId) {
        this.areaId = areaId;
    }

    public int getPowerPercent() {
        return powerPercent;
    }

    public void setPowerPercent(int powerPercent) {
        this.powerPercent = powerPercent;
    }

    /* unimplemented - Garrett */
    @Override
    public String getTitle() {
        return null;
    }

    @Override
    public String getSnippet() {
        return null;
    }
}
