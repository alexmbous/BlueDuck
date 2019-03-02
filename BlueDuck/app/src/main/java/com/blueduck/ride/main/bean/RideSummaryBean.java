package com.blueduck.ride.main.bean;

import java.io.Serializable;

/**
 * Cycling end multiplayer list entity
 * 骑行结束多人列表实体
 */
public class RideSummaryBean implements Serializable{

    private String amount;
    private String duration;
    private String distance;
    private String rideAmount;
    private String rideUser;
    private String id;
    private String hostId;
    private RunBikeVo bikeVo;

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public String getRideAmount() {
        return rideAmount;
    }

    public void setRideAmount(String rideAmount) {
        this.rideAmount = rideAmount;
    }

    public String getRideUser() {
        return rideUser;
    }

    public void setRideUser(String rideUser) {
        this.rideUser = rideUser;
    }

    public String getDistance() {
        return distance;
    }

    public void setDistance(String distance) {
        this.distance = distance;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getHostId() {
        return hostId;
    }

    public void setHostId(String hostId) {
        this.hostId = hostId;
    }

    public RunBikeVo getBikeVo() {
        return bikeVo;
    }

    public void setBikeVo(RunBikeVo bikeVo) {
        this.bikeVo = bikeVo;
    }
}
