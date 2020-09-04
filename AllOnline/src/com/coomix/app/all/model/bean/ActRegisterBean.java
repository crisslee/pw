package com.coomix.app.all.model.bean;

import java.io.Serializable;

/**
 * Created by ly on 2017/6/17.
 */
public class ActRegisterBean implements Serializable {

    public static final String TAG = "ActRegisterBean";
    private static final long serialVersionUID = -7960918229121128596L;

    private String name = "";
    private String phone = "";
    private String wechat = "";
    private String addr = "";
    private String sex = "";
    private String id = "";

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getWechat() {
        return wechat;
    }

    public void setWechat(String wechat) {
        this.wechat = wechat;
    }

    public String getAddr() {
        return addr;
    }

    public void setAddr(String addr) {
        this.addr = addr;
    }

    public String getSex() {
        return sex;
    }

    public void setSex(String sex) {
        this.sex = sex;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
