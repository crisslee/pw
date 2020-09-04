package com.coomix.app.all.model.bean;

import java.io.Serializable;

/**
 * File Description:
 *
 * @author felixqiu
 * @since 2019/1/3.
 */
public class DevMode implements Serializable {
    private static final long serialVersionUID = 5658529888098050867L;
    public static final int MODE_ALARM = 0;
    public static final int MODE_TRACK = 1;
    public static final int MODE_WEEK = 2;
    public static final int MODE_SCHEDULE = 3;

    public static final int SUB_MODE_LOOP = 0;
    public static final int SUB_MODE_CUSTOM = 1;

    public static final int LOC_WIFI = 0; //wifi定位
    public static final int LOC_GPS = 1; //GPS定位
    public static final int LOC_BASE = 2; //基站定位

    public String uid;
    public String imei;
    //设备模式 0-闹钟模式，1-追踪，2-星期，3-计划表（重复、自定义）
    public int mode;
    //设备所处环境 0-全天空，1-半天空，2-地下室
    public int env;
    //下次上报时间点
    public long next_online_utc;
    //区分平台模式，只有mode==3才有意义，0-重复，1-自定义
    public int sub_mode;
    //最大间隔
    public int max_interval;
    //最小间隔
    public int min_interval;
    public String content;
}
