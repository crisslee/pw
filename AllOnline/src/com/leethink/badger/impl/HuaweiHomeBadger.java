package com.leethink.badger.impl;

import android.app.Notification;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import com.leethink.badger.Badger;

import java.util.Arrays;
import java.util.List;

/**
 * @author Jason Ling
 */
public class HuaweiHomeBadger extends Badger
{

    private static final String TAG = HuaweiHomeBadger.class.getSimpleName();

    @Override
    public void executeBadge(Context context, Notification notification, int notificationId, int thisNotificationCount,
                             int count)
    {
        setNotification(notification, notificationId, context);
        if (checkIsSupportedByVersion(context))
        {
            try
            {
                String launcherClassName = getLauncherClassName(context);
                if (launcherClassName == null)
                {
                    return;
                }
                Bundle localBundle = new Bundle();
                localBundle.putString("package", context.getPackageName());
                localBundle.putString("class", launcherClassName);
                localBundle.putInt("badgenumber", count);
                context.getContentResolver().call(Uri.parse("content://com.huawei.android.launcher.settings/badge/"),
                        "change_badge", null, localBundle);
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }

    public boolean checkIsSupportedByVersion(Context context)
    {
        try
        {
            PackageManager manager = context.getPackageManager();
            PackageInfo info = manager.getPackageInfo("com.huawei.android.launcher", 0);
            if (info.versionCode >= 63029)
            {
                return true;
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        Log.i(TAG, "this model not support badge.");
        return false;
    }

    @Override
    public List<String> getSupportLaunchers()
    {
        return Arrays.asList("com.huawei.android.launcher");
    }
}
