package com.goome.gpns.utils;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;
import com.goome.gpns.GPNSInterface;
import java.io.IOException;
import java.util.List;
import java.util.Properties;

public class CommonUtil {

    public static void broadcastMessage(Context ctx, Intent intent) {
        ctx.sendBroadcast(intent);
    }

    /**
     * 发送全局广播
     */
    public static void broadcastMessage(Context ctx, String action, String msgKey, String msg) {
        Intent intent = new Intent();
        intent.putExtra(msgKey, msg);
        intent.setAction(action);
        broadcastMessage(ctx, intent);
    }

    public static void showMsg(final String msg) {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(GPNSInterface.appContext, msg, Toast.LENGTH_SHORT).show();
            }
        });
    }

    public static String readValueFromProperty(Context ctx, String fileName, String key) {
        String value = null;
        try {
            Properties properties = new Properties();
            Resources res = ctx.getResources();
            AssetManager assetMgr = res.getAssets();
            properties.load(assetMgr.open(fileName));
            value = properties.getProperty(key);
        } catch (IOException e) {
            FileOperationUtil.saveErrMsgToFile("key=" + key + "|readValueFromProperty():" + e.toString());
            LogUtil.printException2Console(e);
        } catch (Exception e) {
            FileOperationUtil.saveErrMsgToFile("key=" + key + "|readValueFromProperty():" + e.toString());
            LogUtil.printException2Console(e);
        }
        return value;
    }

    /************************************************************/
    public static double parse2Double(String str) {
        double result = Double.NEGATIVE_INFINITY;
        try {
            result = Double.valueOf(str);
        } catch (Exception e) {
            FileOperationUtil.saveErrMsgToFile("parse2Double(" + str + ")" + " occur an exception:" + e.toString());
            LogUtil.printException2Console(e);
        }
        return result;
    }

    public static long parse2Long(String str) {
        long result = Long.MIN_VALUE;
        try {
            result = Long.valueOf(str);
        } catch (Exception e) {
            FileOperationUtil.saveErrMsgToFile("parse2Long(" + str + ")" + " occur an exception:" + e.toString());
            LogUtil.printException2Console(e);
        }
        return result;
    }

    public static int parse2Integer(String str) {
        int result = Integer.MIN_VALUE;
        try {
            result = Integer.valueOf(str);
        } catch (Exception e) {
            FileOperationUtil.saveErrMsgToFile("parse2Integer(" + str + ")" + " occur an exception:" + e.toString());
            LogUtil.printException2Console(e);
        }
        return result;
    }

    /**
     * 获取当前进程名称
     *
     * @param contex context
     * @return 进程全称
     */
    public static String getCurProcessName(Context contex) {
        int pid = android.os.Process.myPid();
        ActivityManager activityMgr = (ActivityManager) contex.getSystemService(Context.ACTIVITY_SERVICE);
        if (activityMgr != null) {
            List<ActivityManager.RunningAppProcessInfo> runningProcessList = activityMgr.getRunningAppProcesses();
            if (runningProcessList != null) {
                for (ActivityManager.RunningAppProcessInfo runningProcess : runningProcessList) {
                    if (pid == runningProcess.pid) {
                        return runningProcess.processName;
                    }
                }
            }
        }
        return "";
    }

}
