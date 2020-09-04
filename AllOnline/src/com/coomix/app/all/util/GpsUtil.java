package com.coomix.app.all.util;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.provider.Settings;
import android.util.Log;
import java.util.ArrayList;
import java.util.List;

/**
 * GPS控制工具类<br/>
 * 控制GPS打开/关闭 ,及GPS状态获取
 *
 * @author 刘生健
 * @since 2015-7-24 下午02:54:46
 */
public class GpsUtil {
    /**
     * 控制GPS开启关闭 可配合{@link #isGPSEnable(Context)}进行打开/关闭
     *
     * @deprecated 测试无效，需系统权限
     */
    public static void toggleGPS(Context context) {
        Intent gpsIntent = new Intent();
        gpsIntent.setClassName("com.android.settings", "com.android.settings.widget.SettingsAppWidgetProvider");
        gpsIntent.addCategory("android.intent.category.ALTERNATIVE");
        gpsIntent.setData(Uri.parse("custom:3"));
        try {
            PendingIntent.getBroadcast(context, 0, gpsIntent, 0).send();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取当前GPS的状态 （开启 true 关闭 false
     */
    public static boolean isGPSEnable(Context context) {
        String str = Settings.Secure.getString(context.getContentResolver(),
            Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
        if (str != null) {
            return str.contains("gps");
        } else {
            return false;
        }
    }

    public static boolean isMockSettingOn(Context context) {
        return !("0".equals(Settings.Secure.getString(context.getContentResolver(),
            Settings.Secure.ALLOW_MOCK_LOCATION)));
    }

    public static List<String> areThereMockPermissionApps(Context context) {
        List<String> list = new ArrayList<>();

        PackageManager pm = context.getPackageManager();
        List<ApplicationInfo> packages = pm.getInstalledApplications(PackageManager.GET_META_DATA);

        for (ApplicationInfo applicationInfo : packages) {
            try {
                PackageInfo packageInfo = pm.getPackageInfo(applicationInfo.packageName,
                    PackageManager.GET_PERMISSIONS);

                // Get Permissions
                String[] requestedPermissions = packageInfo.requestedPermissions;

                if (requestedPermissions != null) {
                    for (int i = 0; i < requestedPermissions.length; i++) {
                        if (requestedPermissions[i].equals("android.permission.ACCESS_MOCK_LOCATION")
                            && !applicationInfo.packageName.equals(context.getPackageName())) {
                            list.add(applicationInfo.packageName);
                        }
                    }
                }
            } catch (PackageManager.NameNotFoundException e) {
                Log.e("Got exception ", e.getMessage());
            }
        }
        return list;
    }
}
