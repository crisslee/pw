package com.goome.gpns.noti;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.media.RingtoneManager;
import android.net.Uri;
import android.text.TextUtils;
import com.goome.gpns.GPNSInterface;
import com.goome.gpns.contentprovider.ProviderConst;

public class GPNSNotificationBuilder {
    public int notificationDefaults;
    public String appName;

    public int statusBarIcon;
    public Uri audioUri;

    public String showMsg;    // 用于显示在状态栏的内容
    public String wholeMsg;

    public GPNSNotificationBuilder() {
    }

    public GPNSNotificationBuilder(String appName, int statusBarIcon) {
        super();
        this.appName = appName;
        this.statusBarIcon = statusBarIcon;
    }

    public GPNSNotificationBuilder(String appName, int statusBarIcon, Uri audioUri) {
        super();
        this.appName = appName;
        this.statusBarIcon = statusBarIcon;
        this.audioUri = audioUri;
    }

    public static GPNSNotificationBuilder getDefaultBuilder(Context context) {
        ApplicationInfo appInfo = context.getApplicationInfo();
        GPNSNotificationBuilder notiBuilder = new GPNSNotificationBuilder(
            getApplicationName(context, appInfo.packageName), appInfo.icon);
        return notiBuilder;
    }

    public static String getApplicationName(Context context, String pkgName) {
        PackageManager packageManager = null;
        ApplicationInfo applicationInfo = null;
        try {
            packageManager = context.getPackageManager();
            applicationInfo = packageManager.getApplicationInfo(pkgName, packageManager.GET_META_DATA);
        } catch (PackageManager.NameNotFoundException e) {
            applicationInfo = null;
        }
        String applicationName = (String) packageManager.getApplicationLabel(applicationInfo);
        return applicationName;
    }

    public Uri getLastAudioUri(Context context) {
        String audioStr = GPNSInterface.getKV(context, ProviderConst.getContentUri(context), ProviderConst.AUDIO_URI);
        Uri audioUri = null;
        if (!TextUtils.isEmpty(audioStr)) {
            audioUri = Uri.parse(audioStr);
        } else {
            audioUri = RingtoneManager.getActualDefaultRingtoneUri(GPNSInterface.appContext,
                RingtoneManager.TYPE_NOTIFICATION);
        }
        return audioUri;
    }

    @Override
    public String toString() {
        return "notiBuilder={appName=" + appName + ",statusBarIcon="
            + statusBarIcon + ",audioUri=" + audioUri + ",showMsg="
            + showMsg + ",wholeMsg=" + wholeMsg;
    }
}
