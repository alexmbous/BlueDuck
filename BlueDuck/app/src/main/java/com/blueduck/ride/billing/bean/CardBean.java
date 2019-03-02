package com.blueduck.ride.billing.bean;

import java.util.List;

public class CardBean {

    private String defaultPaymentId;
    private List<CardListBean> paymentList;

    public String getDefaultPaymentId() {
        return defaultPaymentId;
    }

    public void setDefaultPaymentId(String defaultPaymentId) {
        this.defaultPaymentId = defaultPaymentId;
    }

    public List<CardListBean> getPaymentList() {
        return paymentList;
    }

    public void setPaymentList(List<CardListBean> paymentList) {
        this.paymentList = paymentList;
    }

}
