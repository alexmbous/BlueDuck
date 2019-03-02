package com.blueduck.ride.main.bean;

/**
 * Unlocking entity
 * 开锁实体
 */
public class LockBean {

    private String data;
    private String mac;
    private String date;
    private RedPackBean redpackRule;
    private BoundaryArea cityVo;
    private String tcpConnected;
    private String power;
    private String bikeType;

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public String getMac() {
        return mac;
    }

    public void setMac(String mac) {
        this.mac = mac;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public RedPackBean getRedpackRule() {
        return redpackRule;
    }

    public void setRedpackRule(RedPackBean redpackRule) {
        this.redpackRule = redpackRule;
    }

    public BoundaryArea getCityVo() {
        return cityVo;
    }

    public void setCityVo(BoundaryArea cityVo) {
        this.cityVo = cityVo;
    }

    public String getTcpConnected() {
        return tcpConnected;
    }

    public void setTcpConnected(String tcpConnected) {
        this.tcpConnected = tcpConnected;
    }

    public String getPower() {
        return power;
    }

    public void setPower(String power) {
        this.power = power;
    }

    public String getBikeType() {
        return bikeType;
    }

    public void setBikeType(String bikeType) {
        this.bikeType = bikeType;
    }

}
