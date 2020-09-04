package com.coomix.app.all.model.bean;

import android.content.Context;

public class BasicGPNSNotificationBuilder {
    public int notificationDefaults;
    public String pushMsgInfo;  //消息的内容 用于显示在状态栏
    public String pushMsgInfoToMax;  //消息的内容 用于解析
    public String appName;
    public int iconId;
    private Context mct;

    public BasicGPNSNotificationBuilder() {
    }

    public BasicGPNSNotificationBuilder(Context context) {
        this.mct = context;
    }
}
