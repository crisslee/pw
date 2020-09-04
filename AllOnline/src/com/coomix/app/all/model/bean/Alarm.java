package com.coomix.app.all.model.bean;

import java.io.Serializable;

public class Alarm implements Serializable {

    private static final long serialVersionUID = 4608812296146664592L;
    private String id;
    private String dev_name;
    private String dev_type;
    private String alarm_type;
    private int alarm_type_id;
    private long alarm_time;
    private long gps_time;
    private int speed;
    private String dir;
    private String gps_status;
    private double lat;
    private double lng;
    private String imei;
    private String address;
    private boolean isSelected;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDev_name() {
        return dev_name;
    }

    public void setDev_name(String dev_name) {
        this.dev_name = dev_name;
    }

    public String getDev_type() {
        return dev_type;
    }

    public void setDev_type(String dev_type) {
        this.dev_type = dev_type;
    }

    public String getAlarm_type() {
        return alarm_type;
    }

    public void setAlarm_type(String alarm_type) {
        this.alarm_type = alarm_type;
    }

    public long getAlarm_time() {
        return alarm_time;
    }

    public void setAlarm_time(long alarm_time) {
        this.alarm_time = alarm_time;
    }

    public long getGps_time() {
        return gps_time;
    }

    public void setGps_time(long gps_time) {
        this.gps_time = gps_time;
    }

    public int getSpeed() {
        return speed;
    }

    public void setSpeed(int speed) {
        this.speed = speed;
    }

    public String getDir() {
        return dir;
    }

    public void setDir(String dir) {
        this.dir = dir;
    }

    public String getGps_status() {
        return gps_status;
    }

    public void setGps_status(String gps_status) {
        this.gps_status = gps_status;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLng() {
        return lng;
    }

    public void setLng(double lng) {
        this.lng = lng;
    }

    public String getImei() {
        return imei;
    }

    public void setImei(String imei) {
        this.imei = imei;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }

    public int getAlarm_type_id() {
        return alarm_type_id;
    }

    public void setAlarm_type_id(int alarm_type_id) {
        this.alarm_type_id = alarm_type_id;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public int getCrashLevel() {
        if (gps_status == null || gps_status.length() < 4) {
            return 0;
        }
        String s = gps_status.substring(2, 4);
        return Integer.parseInt(s, 16);
    }
}
