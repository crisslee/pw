package com.coomix.app.all.tinker;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Environment;
import android.os.StatFs;
import android.util.Log;

import com.tencent.tinker.lib.util.TinkerLog;
import com.tencent.tinker.loader.shareutil.ShareConstants;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.List;

/**
 * Utils for tinker
 *
 * @author qiufei
 * @since 2016/11/5
 */

public class TinkerUtil {
    private static final String TAG = TinkerUtil.class.getSimpleName();

    /**
     * the error code define by myself
     * should after {@code ShareConstants.ERROR_PATCH_INSERVICE
     */
    public static final int ERROR_PATCH_GOOGLEPLAY_CHANNEL = -20;
    public static final int ERROR_PATCH_ROM_SPACE = -21;
    public static final int ERROR_PATCH_MEMORY_LIMIT = -22;
    public static final int ERROR_PATCH_CRASH_LIMIT = -23;
    public static final int ERROR_PATCH_CONDITION_NOT_SATISFIED = -24;

    public static final String PLATFORM = "platform";

    public static final int MIN_MEMORY_HEAP_SIZE = 45;

    private static boolean background = false;

    public static boolean isGooglePlay() {
        return false;
    }

    public static boolean isBackground() {
        return background;
    }

    public static void setBackground(boolean back) {
        background = back;
    }

    public static int checkForPatchRecover(long roomSize, int maxMemory) {
        if (TinkerUtil.isGooglePlay()) {
            return TinkerUtil.ERROR_PATCH_GOOGLEPLAY_CHANNEL;
        }
        if (maxMemory < MIN_MEMORY_HEAP_SIZE) {
            return TinkerUtil.ERROR_PATCH_MEMORY_LIMIT;
        }
        //or you can mention user to clean their rom space!
        if (!checkRomSpaceEnough(roomSize)) {
            return TinkerUtil.ERROR_PATCH_ROM_SPACE;
        }

        return ShareConstants.ERROR_PATCH_OK;
    }

    /**
     * 判断设备上是否安装了 Xposed
     * 这会影响 tinker 的 class 加载
     *
     * @param thr Exception
     * @return boolean
     */
    public static boolean isXposedExists(Throwable thr) {
        StackTraceElement[] stackTraces = thr.getStackTrace();
        for (StackTraceElement stackTrace : stackTraces) {
            final String clazzName = stackTrace.getClassName();
            if (clazzName != null && clazzName.contains("de.robv.android.xposed.XposedBridge")) {
                return true;
            }
        }
        return false;
    }

    public static boolean checkRomSpaceEnough(long limitSize) {
        long allSize;
        long availableSize = 0;
        try {
            File data = Environment.getDataDirectory();
            StatFs sf = new StatFs(data.getPath());
            availableSize = (long) sf.getAvailableBlocks() * (long) sf.getBlockSize();
            allSize = (long) sf.getBlockCount() * (long) sf.getBlockSize();
        }
        catch (Exception e) {
            allSize = 0;
        }

        return allSize != 0 && availableSize > limitSize;
    }

    public static String getExceptionCauseString(final Throwable ex) {
        final ByteArrayOutputStream bos = new ByteArrayOutputStream();
        final PrintStream ps = new PrintStream(bos);

        try {
            // print directly
            Throwable t = ex;
            while (t.getCause() != null) {
                t = t.getCause();
            }
            t.printStackTrace(ps);
            return toVisualString(bos.toString());
        }
        finally {
            try {
                bos.close();
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static String toVisualString(String src) {
        boolean cutFlg = false;

        if (null == src) {
            return null;
        }

        char[] chr = src.toCharArray();
        if (chr == null) {
            return null;
        }

        int i = 0;
        for (; i < chr.length; i++) {
            if (chr[i] > 127) {
                chr[i] = 0;
                cutFlg = true;
                break;
            }
        }

        if (cutFlg) {
            return new String(chr, 0, i);
        } else {
            return src;
        }
    }

    /**
     * 判断当前程序是否在后台运行
     *
     * @param context Context
     * @return boolean
     */
    public static boolean isBackground(Context context) {
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> appProcesses = activityManager.getRunningAppProcesses();
        for (ActivityManager.RunningAppProcessInfo appProcess : appProcesses) {
            if (appProcess.processName.equals(context.getPackageName())) {
                if (appProcess.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_BACKGROUND) {
                    Log.i("后台:", appProcess.processName);
                    return true;
                } else {
                    Log.i("前台:", appProcess.processName);
                    return false;
                }
            }
        }
        return false;
    }

    public static class ScreenState {
        public interface IOnScreenOff {
            void onScreenOff();
        }

        public ScreenState(final Context context, final IOnScreenOff onScreenOffInterface) {
            IntentFilter filter = new IntentFilter();
            filter.addAction(Intent.ACTION_SCREEN_OFF);

            context.registerReceiver(new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent in) {
                    String action = in == null ? "" : in.getAction();
                    TinkerLog.i(TAG, "ScreenReceiver action [%s] ", action);
                    if (Intent.ACTION_SCREEN_OFF.equals(action)) {
                        if (onScreenOffInterface != null) {
                            onScreenOffInterface.onScreenOff();
                        }
                    }
                    context.unregisterReceiver(this);
                }
            }, filter);
        }
    }

}
