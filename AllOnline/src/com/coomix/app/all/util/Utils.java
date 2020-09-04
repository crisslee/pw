/*
 * Copyright (C) 2012 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.coomix.app.all.util;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;
import android.util.Log;
import java.io.Closeable;
import java.io.File;
import java.io.IOException;

/**
 * Class containing some static utility methods.
 */
public class Utils {
    public static final int IO_BUFFER_SIZE = 8 * 1024;

    private static final Utils INSTANCE = new Utils();
    private Context applicationContext;

    private Utils() {
    }

    public static Utils getInstance() {
        return INSTANCE;
    }

    public Context getApplicationContext() {
        return applicationContext;
    }

    public void setApplicationContext(Context context) {
        this.applicationContext = context.getApplicationContext();
    }

    /**
     * 返回当前程序版本号
     */
    public String getAppVersionCode() {
        String versionCode = "";
        try {
            // ---get the package info---
            PackageManager pm = applicationContext.getPackageManager();
            PackageInfo pi = pm.getPackageInfo(applicationContext.getPackageName(), 0);
            versionCode = pi.versionCode + "";
        } catch (Exception e) {
            Log.e("VersionInfo", "Exception", e);
        }
        return versionCode;
    }

    /**
     * Workaround for bug pre-Froyo, see here for more info:
     * http://android-developers.blogspot.com/2011/09/androids-http-clients.html
     */
    public static void disableConnectionReuseIfNecessary() {
        // HTTP connection reuse which was buggy pre-froyo
        if (hasHttpConnectionBug()) {
            System.setProperty("http.keepAlive", "false");
        }
    }

    /**
     * Get the size in bytes of a bitmap.
     *
     * @return size in bytes
     */
    @SuppressLint("NewApi")
    public static int getBitmapSize(Bitmap bitmap) {
        /*
         * if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR1 12) {
         * return bitmap.getByteCount(); }
         */
        // Pre HC-MR1
        return bitmap.getRowBytes() * bitmap.getHeight();
    }

    /**
     * Check if external storage is built-in or removable.
     *
     * @return True if external storage is removable (like an SD card), false
     * otherwise.
     */
    @SuppressLint("NewApi")
    public static boolean isExternalStorageRemovable() {
        if (Build.VERSION.SDK_INT >= /* Build.VERSION_CODES.GINGERBREAD */ 9) {
            return Environment.isExternalStorageRemovable();
        }
        return true;
    }

    /**
     * Get the external app cache directory.
     *
     * @param context The context to use
     * @return The external cache dir
     */
    @SuppressLint("NewApi")
    public static File getExternalCacheDir(Context context) {
        if (hasExternalCacheDir()) {
            return context.getExternalCacheDir();
        }

        // Before Froyo we need to construct the external cache dir ourselves
        final String cacheDir = "/Android/data/" + context.getPackageName() + "/cache/";
        return new File(Environment.getExternalStorageDirectory().getPath() + cacheDir);
    }

    /**
     * Check how much usable space is available at a given path.
     *
     * @param path The path to check
     * @return The space available in bytes
     */
    @SuppressLint("NewApi")
    public static long getUsableSpace(File path) {
        if (Build.VERSION.SDK_INT >= /* Build.VERSION_CODES.GINGERBREAD */ 9) {
            return path.getUsableSpace();
        }
        final StatFs stats = new StatFs(path.getPath());
        return (long) stats.getBlockSize() * (long) stats.getAvailableBlocks();
    }

    /**
     * Get the memory class of this device (approx. per-app memory limit)
     */
    public static int getMemoryClass(Context context) {
        return ((ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE)).getMemoryClass();
    }

    /**
     * Check if OS version has a http URLConnection bug. See here for more
     * information:
     * http://android-developers.blogspot.com/2011/09/androids-http-clients.html
     */
    public static boolean hasHttpConnectionBug() {
        return Build.VERSION.SDK_INT < /* Build.VERSION_CODES.FROYO */ 8;
    }

    /**
     * Check if OS version has built-in external cache dir method.
     */
    public static boolean hasExternalCacheDir() {
        return Build.VERSION.SDK_INT >= /* Build.VERSION_CODES.FROYO */ 8;
    }

    /**
     * Check if ActionBar is available.
     */
    public static boolean hasActionBar() {
        return Build.VERSION.SDK_INT >= /* Build.VERSION_CODES.HONEYCOMB */ 11;
    }

    // 加密处理
    public String encrypt(String sSrc) {
        StringBuilder sb = new StringBuilder();
        int length = sSrc.length();
        for (int i = 0; i < length; i++) {
            byte c = (byte) (sSrc.charAt(i) ^ 22333);
            sb.append((char) c);
        }
        return sb.toString();
    }

    public static void silentClose(Closeable c) {
        if (c == null) {
            return;
        }
        try {
            c.close();
        } catch (IOException e) {
            //
        }
    }

    /**
     * 判断字符串是否无效
     */
    public static boolean isNull(String s) {
        return s == null || s.trim().length() <= 0;
    }

    //创建通知渠道
    public static void createChannel(NotificationManager nm, String id, String name) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            deleteOldChannels(nm);
            if (nm.getNotificationChannel(id) != null) {
                return;
            }
            NotificationChannel channel = new NotificationChannel(id, name, NotificationManager.IMPORTANCE_HIGH);
            channel.enableLights(true);
            channel.enableVibration(true);
            nm.createNotificationChannel(channel);
        }
    }

    private static void deleteOldChannels(NotificationManager nm) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (nm.getNotificationChannel("coomix.all.gpns") != null) {
                nm.deleteNotificationChannel("coomix.all.gpns");
            }
            if (nm.getNotificationChannel("coomix.all.update") != null) {
                nm.deleteNotificationChannel("coomix.all.update");
            }
            if (nm.getNotificationChannel("coomix.all.message") != null) {
                nm.deleteNotificationChannel("coomix.all.message");
            }
        }
    }
}
