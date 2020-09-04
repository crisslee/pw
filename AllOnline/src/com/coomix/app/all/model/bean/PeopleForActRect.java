package com.coomix.app.all.model.bean;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by goome on 2017/7/12.
 */
public class PeopleForActRect implements Serializable {
    private static final long serialVersionUID = 1L;

    private ArrayList<PeopleForAct> near = new ArrayList<PeopleForAct>();
    private double minlat = 0;
    private double clat = 0;
    private double maxlat = 0;
    private double minlng = 0;
    private double clng = 0;
    private double maxlng = 0;
    private double radius = 0;
    private String city = "";
    private String districe = "";

    public ArrayList<PeopleForAct> getNear() {
        return near;
    }

    public void setNear(ArrayList<PeopleForAct> near) {
        this.near = near;
    }

    public double getMinlat() {
        return minlat;
    }

    public void setMinlat(double minlat) {
        this.minlat = minlat;
    }

    public double getClat() {
        return clat;
    }

    public void setClat(double clat) {
        this.clat = clat;
    }

    public double getMaxlat() {
        return maxlat;
    }

    public void setMaxlat(double maxlat) {
        this.maxlat = maxlat;
    }

    public double getMinlng() {
        return minlng;
    }

    public void setMinlng(double minlng) {
        this.minlng = minlng;
    }

    public double getClng() {
        return clng;
    }

    public void setClng(double clng) {
        this.clng = clng;
    }

    public double getMaxlng() {
        return maxlng;
    }

    public void setMaxlng(double maxlng) {
        this.maxlng = maxlng;
    }

    public double getRadius() {
        return radius;
    }

    public void setRadius(double radius) {
        this.radius = radius;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getDistrice() {
        return districe;
    }

    public void setDistrice(String districe) {
        this.districe = districe;
    }
}
