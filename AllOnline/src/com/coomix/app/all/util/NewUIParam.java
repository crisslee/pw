package com.coomix.app.all.util;

import com.coomix.app.all.AllOnlineApp;

/**
 * 以下尺寸是以1080（480dpi）为基准的所以需要根据当前设备换算下
 * BusOnlineApp.DENSITY在WelcomeActivity.class中初始化赋值
 *
 * @author shishenglong
 * @since 2016年7月29日 上午11:36:57
 */
public class NewUIParam {

    /** 用户头像136*136 px */
    //public static final int USER_ICON_SIZE = 136 * AllOnlineApp.getDensity() / 480;
    /** 帖子昵称文字38px */
    public static final int TEXT_SIZE_TOPIC_NICKNAME = 36 * AllOnlineApp.getDensity() / 480;
    /** 帖子头部时间文字30px */
    public static final int TEXT_SIZE_TOPIC_TIME = 33 * AllOnlineApp.getDensity() / 480;
    /** 帖子正文文字48px */
    public static final int TEXT_SIZE_TOPIC_CONTENT = 45 * AllOnlineApp.getDensity() / 480;
    /** 帖子图片间距10px */
    public static final int TOPIC_IMAGE_GRID = 18 * AllOnlineApp.getDensity() / 480;
}
