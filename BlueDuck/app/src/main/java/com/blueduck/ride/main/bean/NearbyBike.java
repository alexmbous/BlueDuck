package com.blueduck.ride.main.bean;

/**
 * Nearby bicycle entity
 * 附近单车实体
 */
public class NearbyBike {

    private int gsm;
    private int gpsNumber;
    private int useStatus;
    private String number;
    private double gLng;
    private double price;
    private double gLat;
    private String typeId;
    private int power;
    private String bid;
    private int status;
    private RedPackBean redpackRuleVo;
    private int readpack;
    private int powerPercent;
    private int typeCount;
    private int bikeType;
    private String areaId;

    public int getGsm() {
        return gsm;
    }

    public void setGsm(int gsm) {
        this.gsm = gsm;
    }

    public int getGpsNumber() {
        return gpsNumber;
    }

    public void setGpsNumber(int gpsNumber) {
        this.gpsNumber = gpsNumber;
    }

    public int getUseStatus() {
        return useStatus;
    }

    public void setUseStatus(int useStatus) {
        this.useStatus = useStatus;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public double getgLng() {
        return gLng;
    }

    public void setgLng(double gLng) {
        this.gLng = gLng;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public double getgLat() {
        return gLat;
    }

    public void setgLat(double gLat) {
        this.gLat = gLat;
    }

    public String getTypeId() {
        return typeId;
    }

    public void setTypeId(String typeId) {
        this.typeId = typeId;
    }

    public int getPower() {
        return power;
    }

    public void setPower(int power) {
        this.power = power;
    }

    public String getBid() {
        return bid;
    }

    public void setBid(String bid) {
        this.bid = bid;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
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

    public int getPowerPercent() {
        return powerPercent;
    }

    public void setPowerPercent(int powerPercent) {
        this.powerPercent = powerPercent;
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
}
