package com.blueduck.ride.login.bean;

import java.io.Serializable;

/**
 * 国家区域实体
 * National regional entity
 */
public class RegionsBean implements Serializable {

    private String chinese_name;
    private String country_code;
    private String english_name;
    private String phone_code;
    private String local_name;

    public String getChinese_name() {
        return chinese_name;
    }

    public void setChinese_name(String chinese_name) {
        this.chinese_name = chinese_name;
    }

    public String getCountry_code() {
        return country_code;
    }

    public void setCountry_code(String country_code) {
        this.country_code = country_code;
    }

    public String getEnglish_name() {
        return english_name;
    }

    public void setEnglish_name(String english_name) {
        this.english_name = english_name;
    }

    public String getPhone_code() {
        return phone_code;
    }

    public void setPhone_code(String phone_code) {
        this.phone_code = phone_code;
    }

    public String getLocal_name() {
        return local_name;
    }

    public void setLocal_name(String local_name) {
        this.local_name = local_name;
    }

}
