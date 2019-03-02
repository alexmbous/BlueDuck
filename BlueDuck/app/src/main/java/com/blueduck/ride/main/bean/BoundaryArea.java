package com.blueduck.ride.main.bean;

import java.io.Serializable;

/**
 * Boundary area entity
 * 边界区域实体
 */

public class BoundaryArea implements Serializable{

    private String area_detail;
    private String area_lat;
    private String area_lng;
    private String id;
    private String name;
    private String note;

    public String getArea_detail() {
        return area_detail;
    }

    public void setArea_detail(String area_detail) {
        this.area_detail = area_detail;
    }

    public String getArea_lat() {
        return area_lat;
    }

    public void setArea_lat(String area_lat) {
        this.area_lat = area_lat;
    }

    public String getArea_lng() {
        return area_lng;
    }

    public void setArea_lng(String area_lng) {
        this.area_lng = area_lng;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

}
