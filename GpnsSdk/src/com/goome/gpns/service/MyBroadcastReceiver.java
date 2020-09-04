package com.goome.gpns.service;

import com.goome.gpns.GPNSInterface;
import com.goome.gpns.GpnsSDKInitializer;
import com.goome.gpns.utils.FileOperationUtil;
import com.goome.gpns.utils.LogUtil;
import com.goome.gpns.utils.NetworkUtil;
import com.goome.gpns.utils.PreferenceUtil;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * 通过AlarmManager定时检测服务是否存活； 如果没有存活，并且网络可用，重新启动服务。
 */

public class MyBroadcastReceiver extends BroadcastReceiver {
    private static String TAG = "MyBroadcastReceiver onReceive() ";
    private static int netUnavailableCount = 0;

    private static final int MAX = 3;// 3 * 2;

    @Override
    public void onReceive(Context context, Intent intent) {
        long startTime = System.currentTimeMillis();

        if (GPNSInterface.appContext == null) {
            GpnsSDKInitializer.initialize(context);
        } else {
            LogUtil.i("GPNSInterface.appContext 不为空");
        }

        GPNSInterface.launchAlarmManager(context);

        LogUtil.d(TAG + "intentAction=" + intent.getAction());

        // print network change
        if (ConnectivityManager.CONNECTIVITY_ACTION.equals(intent.getAction())) {
            NetworkInfo networkInfo = (NetworkInfo) intent.getParcelableExtra(ConnectivityManager.EXTRA_NETWORK_INFO);
            if (networkInfo != null) {
                String content = "网络变化：" + networkInfo.toString();
                // "网络变化为：" + networkInfo.getTypeName()
                // + ",available " + networkInfo.isAvailable()
                // + ",connect:" + networkInfo.isConnected()
                // +",status:"+networkInfo.getState().toString();
                LogUtil.d(content);
            }
        }

        String netStatus = null;
        if (NetworkUtil.isConnected(context)) {
            netStatus = TAG + "网络可访问";
            netUnavailableCount = 0;
        } else {
            netStatus = TAG + "网络不可访问";
            netUnavailableCount++;
        }
        LogUtil.d(netStatus + netUnavailableCount);

        // 1.网络不可用
        if (netUnavailableCount > 0) {
            if (netUnavailableCount >= MAX) {
                LogUtil.d(TAG + "检测到连续" + netUnavailableCount + "次网络不可用");
                GPNSInterface.killGpnsService(context);
            }
            return;
        }

        // 2.网络可用
        // 加锁，避免并发同时启动服务
        synchronized (this) {
            try {
                if (GPNSInterface.isServiceReallyRun(context)) {
                    // msg = "running";
                } else {
                    String msg = null;
                    long channelId = PreferenceUtil.getLong(PreferenceUtil.CHANNEL_ID, -1);
                    if (channelId != -1) {
                        msg = TAG + "重启服务";
                        LogUtil.d(msg);
                        GPNSInterface.reStartPushService(channelId);
                    } else {
                        msg = TAG + "获取cid失败，未能重启服务";
                        LogUtil.d(msg);
                    }
                }
            } catch (Exception e) {
                LogUtil.printException2Console(e);
                FileOperationUtil.saveErrMsgToFile(TAG + "occur an exception:\n" + e.toString());
            }
        }

        long consumeTime = System.currentTimeMillis() - startTime;
        if (consumeTime > 1500) {
            FileOperationUtil.saveExceptionInfoToFile("重启服务耗时：" + consumeTime);
        }
    }
}
