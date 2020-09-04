package com.coomix.app.all.model.bean;

import java.io.Serializable;

public class MyLocationInfo implements Serializable {
    private static final long serialVersionUID = 1L;

    private double lng;
    private double lat;
    private String name;

    public double getLng() {
        return lng;
    }

    public void setLng(double lng) {
        this.lng = lng;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
