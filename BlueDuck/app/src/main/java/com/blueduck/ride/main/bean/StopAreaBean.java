package com.blueduck.ride.main.bean;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.List;

/**
 * Parking area entity
 * 停车区域实体
 */
public class StopAreaBean {
    private String name;
    private List<LatLng> latLngs;
    private String id;
    private String detail;
    private String lng;
    private String lat;
    private String note;
    private String type;
    private boolean isRed;

    public List<LatLng> getMapLatLng(){
        List<LatLng> list = null;
        if(latLngs != null && latLngs.size() > 0) {
            list = new ArrayList<>( );
            for (LatLng llb : latLngs) {
                list.add(new LatLng(llb.latitude,llb.longitude));
            }
        }
        return list;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<LatLng> getLatLngs() {
        return latLngs;
    }

    public void setLatLngs(List<LatLng> latLngs) {
        this.latLngs = latLngs;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDetail() {
        return detail;
    }

    public void setDetail(String detail) {
        this.detail = detail;
    }

    public String getLng() {
        return lng;
    }

    public void setLng(String lng) {
        this.lng = lng;
    }

    public String getLat() {
        return lat;
    }

    public void setLat(String lat) {
        this.lat = lat;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public boolean isRed() {
        return isRed;
    }

    public void setRed(boolean red) {
        isRed = red;
    }

}
