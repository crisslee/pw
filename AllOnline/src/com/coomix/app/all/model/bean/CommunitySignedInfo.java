package com.coomix.app.all.model.bean;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * @author shishenglong
 */
public class CommunitySignedInfo implements Serializable {

    private static final long serialVersionUID = 35486695321332158L;

    private String name;
    private String tel;
    private String qqorwx;
    private String addr;
    private String lat;
    private String lon;
    private ArrayList<CommitExtendItem> extend_items;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTel() {
        return tel;
    }

    public void setTel(String tel) {
        this.tel = tel;
    }

    public String getQqorwx() {
        return qqorwx == null ? "" : qqorwx;
    }

    public void setQqorwx(String qqorwx) {
        this.qqorwx = qqorwx;
    }

    public String getAddr() {
        return addr == null ? "" : addr;
    }

    public void setAddr(String addr) {
        this.addr = addr;
    }

    public String getLat() {
        return lat;
    }

    public void setLat(String lat) {
        this.lat = lat;
    }

    public String getLon() {
        return lon;
    }

    public void setLon(String lon) {
        this.lon = lon;
    }

    public ArrayList<CommitExtendItem> getExtend_items() {
        return extend_items;
    }

    public void setExtend_items(ArrayList<CommitExtendItem> extend_items) {
        this.extend_items = extend_items;
    }
}
