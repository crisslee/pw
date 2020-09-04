package com.coomix.app.all.model.bean;

import java.io.Serializable;

/**
 * Created by goome on 2017/7/11.
 */
public class PeopleForAct implements Serializable {

    private static final long serialVersionUID = 1L;

    private String uid = "";
    private String addr2lat = "";
    private String addr2lng = "";
    private double lat = 0;
    private double lng = 0;
    private String realname = "";
    private String tel = "";
    private String wxid = "";
    private String city = "";
    private String district = "";
    private double distance = -1;
    private String remark = "";

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getAddr2lat() {
        return addr2lat;
    }

    public void setAddr2lat(String addr2lat) {
        this.addr2lat = addr2lat;
    }

    public String getAddr2lng() {
        return addr2lng;
    }

    public void setAddr2lng(String addr2lng) {
        this.addr2lng = addr2lng;
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

    public String getRealname() {
        return realname;
    }

    public void setRealname(String realname) {
        this.realname = realname;
    }

    public String getTel() {
        return tel;
    }

    public void setTel(String tel) {
        this.tel = tel;
    }

    public String getWxid() {
        return wxid;
    }

    public void setWxid(String wxid) {
        this.wxid = wxid;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getDistrict() {
        return district;
    }

    public void setDistrict(String district) {
        this.district = district;
    }

    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    @Override
    public String toString() {
        return "PeopleForAct [lat=" + lat + ", lng=" + lng + "]";
    }
}
