package com.goome.gpns.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;

import com.goome.gpns.GPNSInterface;

public class PreferenceUtil {
    private static SharedPreferences mSharedPreferences = null;
    private static Editor mEditor = null;

    // 保存appId的key
    public static final String APP_ID = "appIdKey";
    // 通道ID key的前缀（channelId_23 23是appId）
    public static final String CHANNEL_ID = "channelId_long";
    // 保存设备指纹的key
    public static final String DEVICE_FINGERPRINT = "deviceFingerprint";

    public static final String GPNS_PROCESS_NAME = "gpnsProcessName";

    // 保存app当前通知总个数
    public final static String ACTIVE_NOTIFY_NUM = "activeNotifyNum";
    public final static String ACTIVE_NOTIFY_IDS = "activeNotifyIds";

    // 保存app启动和停止的时间
    public final static String APP_ACTIVE_TIME = "app_active_time";
    public final static String APP_DEACTIVE_TIME = "app_deactive_time";
    public final static String APP_ACTIVE_REMINDER_TIME = "app_active_reminder_time";

    static {
        if (GPNSInterface.appContext == null) {
            FileOperationUtil.saveExceptionInfoToFile("PreferenceUtil init: GPNSInterface.appContext is null");
        } else {
            mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(GPNSInterface.appContext);
        }
    }

    public static void init(Context context) {
        if (mSharedPreferences == null) {
            mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(GPNSInterface.appContext);
        }
    }

    public static SharedPreferences getPref() {
        return mSharedPreferences;
    }

    public static void removeKey(String key) {
        mEditor = mSharedPreferences.edit();
        mEditor.remove(key);
        mEditor.commit();
    }

    public static void removeAll() {
        mEditor = mSharedPreferences.edit();
        mEditor.clear();
        mEditor.commit();
    }

    public static boolean saveString(String key, String value) {
        mEditor = mSharedPreferences.edit();
        mEditor.putString(key, value);
        return mEditor.commit();
    }

    public static String getString(String key, String faillValue) {
        return mSharedPreferences.getString(key, faillValue);
    }

    public static String getString(Context context, String key, String faillValue) {
        init(context);
        return mSharedPreferences.getString(key, faillValue);
    }

    public static boolean saveInt(String key, int value) {
        mEditor = mSharedPreferences.edit();
        mEditor.putInt(key, value);
        return mEditor.commit();
    }

    public static int getInt(String key, int failValue) {
        return mSharedPreferences.getInt(key, failValue);
    }

    public static void saveLong(String key, long value) {
        mEditor = mSharedPreferences.edit();
        mEditor.putLong(key, value);
        mEditor.commit();
    }

    public static long getLong(String key, long failValue) {
        return mSharedPreferences.getLong(key, failValue);
    }

    public static void saveBoolean(String key, boolean value) {
        mEditor = mSharedPreferences.edit();
        mEditor.putBoolean(key, value);
        mEditor.commit();
    }

    public static Boolean getBoolean(String key, boolean failValue) {
        return mSharedPreferences.getBoolean(key, failValue);
    }

}
