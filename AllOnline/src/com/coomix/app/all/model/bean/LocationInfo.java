package com.coomix.app.all.model.bean;

import java.io.Serializable;

public class LocationInfo implements Serializable {

    private static final long serialVersionUID = 3736864744331662473L;

    private String uId;

    private String name;

    private String city;

    private String address;

    private double latitude;

    private double longitude;

    private boolean current = false;

    @Override
    public String toString() {
        return "[uId="
            + uId
            + ", name="
            + name
            + ", city="
            + city
            + ", textAddress="
            + address
            + ", latitude="
            + latitude
            + ", longitude="
            + longitude
            + "]";
    }

    public boolean getCurrent() {
        return current;
    }

    public void setCurrent(boolean cur) {
        current = cur;
    }

    public void setUId(String uId) {
        this.uId = uId;
    }

    public String getUId() {
        return uId;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getCity() {
        return city;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getAddress() {
        return address;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getLongitude() {
        return longitude;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        LocationInfo other = (LocationInfo) obj;
        if ((this.getLatitude() != other.getLatitude()) || (this.getLongitude() != other.getLongitude())
            || (!this.getName().equals(other.getName()))) {
            return false;
        }
        return true;
    }
}
