package com.coomix.app.all.ui.update;

/**
 * Created by Administrator on 2016/4/19.
 */
public class GoomeUpdateConfig {
    private static boolean isUpdateOnlyWifi = true;
    private static boolean isUpdateAutoPopup = true;
    private static boolean isUpdateForce = false;
    private static boolean isSilentDownload = false;
    private static boolean isUpdateCheck = true;

    public static boolean isUpdateOnlyWifi() {
        return isUpdateOnlyWifi;
    }

    public static void setUpdateOnlyWifi(boolean onlyWifi) {
        isUpdateOnlyWifi = onlyWifi;
    }

    public static boolean isUpdateAutoPopup() {
        return isUpdateAutoPopup;
    }

    public static void setUpdateAutoPopup(boolean autoPopup) {
        isUpdateAutoPopup = autoPopup;
    }

    public static boolean isUpdateForce() {
        return isUpdateForce;
    }

    public static void setUpdateForce(boolean updateForce) {
        isUpdateForce = updateForce;
    }

    public static boolean isSilentDownload() {
        return isSilentDownload;
    }

    public static void setSilentDownload(boolean silent) {
        isSilentDownload = silent;
    }

    public static boolean isUpdateCheck() {
        return isUpdateCheck;
    }

    public static void setUpdateCheck(boolean check) {
        isUpdateCheck = check;
    }
}

