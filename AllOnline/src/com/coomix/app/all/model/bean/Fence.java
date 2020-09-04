package com.coomix.app.all.model.bean;

import android.graphics.Color;
import android.text.TextUtils;
import java.io.Serializable;

public class Fence implements Serializable {

    private static final long serialVersionUID = 2621645164403049761L;
    public final static int SHAPE_RECTANGLE = 0;
    public final static int SHAPE_CIRCLE = 1;
    public final static int SHAPE_POLYGON = 2;

    public final static int SWITCH_ON = 1;
    public final static int SWITCH_OFF = 0;

    /****普通类型*/
    public static final int ALARM_TYPE_NORMAL = 0;
    /****围栏报警类型 6 一次性出围栏*/
    public static final int ALARM_TYPE_ONCE = 6;

    /*******绘制marker的外圈颜色******/
    public static final int STROKE_COLOR = Color.argb(255, 34, 38, 45);
    /*******绘制marker的填充颜色******/
    public static final int FILL_COLOR = Color.argb(255 * 15 / 100, 0, 0, 0);
    //高德地图需要的参数
    public static final double RADIUS_OF_EARTH_METERS = 6371009;

    private String id;
    private String user_id;
    private int shape_type;
    private String shape_param;
    private double lat;
    private double lng;
    private int radius;
    private int alarm_type;
    private String remark;
    private int validate_flag;
    private String create_time;
    private String phone_num;
    private String imei;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public int getShape_type() {
        return shape_type;
    }

    public void setShape_type(int shape_type) {
        this.shape_type = shape_type;
    }

    public String getShape_param() {
        return shape_param;
    }

    public void setShape_param(String shape_param) {
        this.shape_param = shape_param;
    }

    public double getLat() {
        if ((int) lat <= 0 && shape_type == SHAPE_CIRCLE) {
            getParamSingleInfo();
        }
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLng() {
        if ((int) lng <= 0 && shape_type == SHAPE_CIRCLE) {
            getParamSingleInfo();
        }
        return lng;
    }

    public void setLng(double lng) {
        this.lng = lng;
    }

    public int getRadius() {
        if (radius <= 0 && shape_type == SHAPE_CIRCLE) {
            getParamSingleInfo();
        }
        return radius;
    }

    public void setRadius(int radius) {
        this.radius = radius;
    }

    public int getAlarm_type() {
        return alarm_type;
    }

    public void setAlarm_type(int alarm_type) {
        this.alarm_type = alarm_type;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public int getValidate_flag() {
        return validate_flag;
    }

    public void setValidate_flag(int validate_flag) {
        this.validate_flag = validate_flag;
    }

    public String getCreate_time() {
        return create_time;
    }

    public void setCreate_time(String create_time) {
        this.create_time = create_time;
    }

    public String getPhone_num() {
        return phone_num;
    }

    public void setPhone_num(String phone_num) {
        this.phone_num = phone_num;
    }

    public String getImei() {
        return imei;
    }

    public void setImei(String imei) {
        this.imei = imei;
    }

    public void getParamSingleInfo() {
        if (shape_type == SHAPE_CIRCLE) {
            if (!TextUtils.isEmpty(shape_param)) {
                String[] paramList = shape_param.split(",");
                if (paramList.length >= 3) {
                    lat = Double.valueOf(paramList[0]);
                    lng = Double.valueOf(paramList[1]);
                    radius = Integer.valueOf(paramList[2]);
                }
            }
        }
    }
}
