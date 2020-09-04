package com.coomix.app.all.model.bean;

import java.io.Serializable;

/**
 * 广告对象
 *
 * @author goome
 */
public class Adver implements Serializable {

    private static final long serialVersionUID = 1L;

    /** type=1，点击一次 */
    public static final int REPORT_CLICK = 1;
    /** type=2，展现一次 */
    public static final int REPORT_SHOW = 2;

    /**
     * AdTypeLaunch：启动页广告    = 1
     * AdTypeHome：首页广告，banner= 2
     * AdTypeTransfer：换乘       = 3
     * AdTypeWindow：弹窗广告      = 4
     */
    public static final int AD_TYPE_LAUNCH = 1;
    public static final int AD_TYPE_BANNER = 2;
    public static final int AD_TYPE_TRANSFER = 3;
    public static final int AD_TYPE_POPWINDOW = 4;

    public int type;
    public int id;
    public String name;
    public String picurl;//启动页面大图url
    public String jumpurl = "";//启动页面大图跳转url
    public String md5;
    public long tm;
}
