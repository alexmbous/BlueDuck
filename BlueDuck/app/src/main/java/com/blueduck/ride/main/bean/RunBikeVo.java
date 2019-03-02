package com.blueduck.ride.main.bean;

import java.io.Serializable;

/**
 * Bicycle current usage information (internal internal entities)
 * 单车当前使用信息(行驶中内部实体)
 */
public class RunBikeVo implements Serializable{

    private String gsm;
    private String gpsNumber;
    private String useStatus;
    private String number;
    private String gLng;
    private String price;
    private String gLat;
    private String power;
    private String status;
    private String powerPercent;
    private String readpack;
    private String bikeType;
    private String typeCount;

    public String getGsm() {
        return gsm;
    }

    public void setGsm(String gsm) {
        this.gsm = gsm;
    }

    public String getGpsNumber() {
        return gpsNumber;
    }

    public void setGpsNumber(String gpsNumber) {
        this.gpsNumber = gpsNumber;
    }

    public String getUseStatus() {
        return useStatus;
    }

    public void setUseStatus(String useStatus) {
        this.useStatus = useStatus;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getgLng() {
        return gLng;
    }

    public void setgLng(String gLng) {
        this.gLng = gLng;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getgLat() {
        return gLat;
    }

    public void setgLat(String gLat) {
        this.gLat = gLat;
    }

    public String getPower() {
        return power;
    }

    public void setPower(String power) {
        this.power = power;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getPowerPercent() {
        return powerPercent;
    }

    public void setPowerPercent(String powerPercent) {
        this.powerPercent = powerPercent;
    }

    public String getReadpack() {
        return readpack;
    }

    public void setReadpack(String readpack) {
        this.readpack = readpack;
    }

    public String getBikeType() {
        return bikeType;
    }

    public void setBikeType(String bikeType) {
        this.bikeType = bikeType;
    }

    public String getTypeCount() {
        return typeCount;
    }

    public void setTypeCount(String typeCount) {
        this.typeCount = typeCount;
    }
}
