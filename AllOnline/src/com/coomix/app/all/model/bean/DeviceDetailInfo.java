package com.coomix.app.all.model.bean;

import java.io.Serializable;

public class DeviceDetailInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    private String imei;
    private String name;
    /***车牌号*/
    private String number;
    private String phone;
    /*****1：物联卡号码，不能打电话；0：sim卡，能编辑也能打电话*/
    private int is_iot_card;
    private  String tel;   //设备联系人电话
    private String dev_type;
    public int goome_card;//1：是我们公司的物联卡(这里也叫贴片卡)，属于goome的，不能打电话也不能编辑；0：插卡设备
    private String owner;
    private long in_time;
    private long out_time;
    /***定位时间 GPS定位时间 UTC 秒数(如果设备过期，值为0)*/
    private long gps_time;
    private long heart_time;
    private int is_enable;//设备未启用，则到期时间显示为空
    private boolean efence_support;
    //备注
    private String remark;
    //设备状态
    private int device_info_new;
    //客户端显示的设备类型
    private String client_product_type;
    //是否终身卡
    private boolean is_card_lifelong;

    public String getImei() {
        return imei;
    }

    public void setImei(String imei) {
        this.imei = imei;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public int getIs_iot_card() {
        return is_iot_card;
    }

    public void setIs_iot_card(int is_iot_card) {
        this.is_iot_card = is_iot_card;
    }

    public String getTel() {
        return tel;
    }

    public void setTel(String tel) {
        this.tel = tel;
    }

    public String getDev_type() {
        return dev_type;
    }

    public void setDev_type(String dev_type) {
        this.dev_type = dev_type;
    }

    public int getGoome_card() {
        return goome_card;
    }

    public void setGoome_card(int goome_card) {
        this.goome_card = goome_card;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public long getIn_time() {
        return in_time;
    }

    public void setIn_time(long in_time) {
        this.in_time = in_time;
    }

    public long getOut_time() {
        return out_time;
    }

    public void setOut_time(long out_time) {
        this.out_time = out_time;
    }

    public long getGps_time() {
        return gps_time;
    }

    public void setGps_time(long gps_time) {
        this.gps_time = gps_time;
    }

    public int getIs_enable() {
        return is_enable;
    }

    public void setIs_enable(int is_enable) {
        this.is_enable = is_enable;
    }

    public boolean isEfence_support() {
        return efence_support;
    }

    public void setEfence_support(boolean efence_support) {
        this.efence_support = efence_support;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public long getHeart_time() {
        return heart_time;
    }

    public void setHeart_time(long heart_time) {
        this.heart_time = heart_time;
    }

    public int getState() {
        if (device_info_new == 2) {
            // 过期
            return DeviceInfo.STATE_EXPIRE;
        }
        return DeviceInfo.STATE_STOP;
    }

    public String getClient_product_type() {
        return client_product_type;
    }

    public void setClient_product_type(String client_product_type) {
        this.client_product_type = client_product_type;
    }

    public boolean isIs_card_lifelong() {
        return is_card_lifelong;
    }

    public void setIs_card_lifelong(boolean is_card_lifelong) {
        this.is_card_lifelong = is_card_lifelong;
    }
}
