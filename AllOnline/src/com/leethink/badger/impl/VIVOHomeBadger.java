package com.leethink.badger.impl;

import android.app.Notification;
import android.content.Context;
import android.content.Intent;

import com.leethink.badger.Badger;

import java.util.Arrays;
import java.util.List;

/**
 * Created by goome
 *
 * @since 2016/12/14.
 */

public class VIVOHomeBadger extends Badger
{

    @Override
    public void executeBadge(Context context, Notification notification, int notificationId, int thisNotificationCount,
                             int count)
    {
        setNotification(notification, notificationId, context);
        try
        {
            String launcherClassName = getLauncherClassName(context);
            if (launcherClassName == null)
            {
                return;
            }
            Intent intent = new Intent("launcher.action.CHANGE_APPLICATION_NOTIFICATION_NUM");
            intent.putExtra("notificationNum", count);
            intent.putExtra("packageName", context.getPackageName());
            intent.putExtra("className", launcherClassName);
            context.sendBroadcast(intent);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public List<String> getSupportLaunchers()
    {
        return Arrays.asList("com.bbk.launcher2");
    }
}
