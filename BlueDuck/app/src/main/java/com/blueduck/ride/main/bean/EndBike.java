package com.blueduck.ride.main.bean;

import java.io.Serializable;
import java.util.List;

/**
 * Cycling end
 * 单车骑行结束
 */
public class EndBike implements Serializable{

    private RedPackVo redpackVo;
    private MyRewardsBean couponVo;
    private TradeVo tradeVo;
    private String balancePay;
    private String balance;
    private List<RideSummaryBean> rideList;//费用清单 list of fees

    public TradeVo getTradeVo() {
        return tradeVo;
    }

    public void setTradeVo(TradeVo tradeVo) {
        this.tradeVo = tradeVo;
    }

    public RedPackVo getRedpackVo() {
        return redpackVo;
    }

    public void setRedpackVo(RedPackVo redpackVo) {
        this.redpackVo = redpackVo;
    }

    public MyRewardsBean getCouponVo() {
        return couponVo;
    }

    public void setCouponVo(MyRewardsBean couponVo) {
        this.couponVo = couponVo;
    }

    public String getBalancePay() {
        return balancePay;
    }

    public void setBalancePay(String balancePay) {
        this.balancePay = balancePay;
    }

    public String getBalance() {
        return balance;
    }

    public void setBalance(String balance) {
        this.balance = balance;
    }

    public List<RideSummaryBean> getRideList() {
        return rideList;
    }

    public void setRideList(List<RideSummaryBean> rideList) {
        this.rideList = rideList;
    }
}
