package com.coomix.app.framework.app;

public interface ITabCallback {
    /* 第一次选中 */
    void onTabSelected();

    /* 重复选中 */
    void onTabReselected();

    /*消息通知*/
    void onNotifyArrive(int notifyCount);

    void onGetDevices();
}
