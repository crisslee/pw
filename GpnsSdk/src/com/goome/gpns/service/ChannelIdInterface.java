package com.goome.gpns.service;

import java.util.Timer;
import java.util.TimerTask;

import org.json.JSONException;
import org.json.JSONObject;

import com.android.goome.volley.Response.ErrorListener;
import com.android.goome.volley.Response.Listener;
import com.android.goome.volley.VolleyError;
import com.android.goome.volley.toolbox.StringRequest;
import com.goome.gpns.GPNSInterface;
import com.goome.gpns.utils.CommonUtil;
import com.goome.gpns.utils.ConnectionUtil;
import com.goome.gpns.utils.FileOperationUtil;
import com.goome.gpns.utils.FingerprintUtil;
import com.goome.gpns.utils.LogUtil;
import com.goome.gpns.utils.NetworkUtil;
import com.goome.gpns.utils.PreferenceUtil;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.text.TextUtils;

/**
 * 通道ID接口：使用appId和fingerprint向服务器申请channelId,并保存本地； 如果本地已缓存cid,直接读取即可。
 * 
 * @author Administrator
 *
 */
public class ChannelIdInterface {

    // 注册channelId的接口地址
    // private static String baseUrl = "http://wx-test.gpsoo.net";//开发环境(以前的)
    /* 开放平台 */
    // public static String baseUrl = "http://open-dev.gpsoo.net";// 开发环境(现在的)
    // private static String baseUrl = "http://open.goome.net";//生产环境
    // public static String baseUrl = "api.gpsoo.net";// 生产环境

    /* 汽车在线生产环境 */
    public static String baseUrl = "";

    private static String channelUri = "/1/push/getchannelid?";

    public static void setBaseUrl(String url) {
        baseUrl = url;
    }

    private static String getChannelUrl(String appId, String fingerprint) {
        StringBuilder params = new StringBuilder();
        params.append("appid=").append(appId);
        params.append("&devId=").append(fingerprint);
        // params.append("&fingerprint=").append(fingerprint);
        params.append("&platform=").append("android");
        String url = baseUrl + channelUri + params.toString();
        return url;
    }

    private static String channelUrl = null;

    private static ResultListener<String> listener;
    private static ErrorListener channelIdErrListener;

    private static synchronized ErrorListener getErrorListener(final ResultListener<String> listener) {
        if (channelIdErrListener == null) {
            channelIdErrListener = new ErrorListener() {

                @Override
                public void onErrorResponse(VolleyError error) {
                    retry(error.toString(), channelUrl, listener);
                }
            };
        }
        return channelIdErrListener;
    }

    /**
     * 从本地缓存中获取通道ID
     * 
     * @return 通道ID
     */
    public static long getChannelIdFromFile() {
        return PreferenceUtil.getLong(PreferenceUtil.CHANNEL_ID, -1);
    }

    /**
     * 从服务器获取通道ID
     * 
     * @param appId
     * @param listener
     */
    public static void getChannelIdViaNet(final String appId, final ResultListener<String> listener) {
        if (TextUtils.isEmpty(appId)) {
            FileOperationUtil.saveExceptionInfoToFile("getChannelIdViaNet() appId不能为空");
            if (listener != null) {
                listener.onFailed("appId不能为空");
            }
            return;
        }

        String fingerprint = FingerprintUtil.getFingerprint(GPNSInterface.appContext);
        if (TextUtils.isEmpty(fingerprint)) {
            FileOperationUtil.saveErrMsgToFile("getChannelIdViaNet() 设备fingerprint不能为空");
            if (listener != null) {
                listener.onFailed("设备fingerprint不能为空");
            }
            return;
        }
        channelUrl = getChannelUrl(appId, fingerprint);
        LogUtil.i("getChannelId() url=" + channelUrl);
        ChannelIdInterface.listener = listener;
        requestChannelId(channelUrl, listener);
    }

    private static void requestChannelId(String channelUrl, final ResultListener<String> listener) {
        StringRequest request = new StringRequest(channelUrl, new Listener<String>() {

            @Override
            public void onResponse(String response) {
                parseResponse(response, listener);
            }
        }, getErrorListener(listener));
        ConnectionUtil.requestHttpData(request);
    }

    private static void parseResponse(String response, ResultListener<String> listener) {
        try {
            LogUtil.i("getChannelId() response：" + response);
            JSONObject object = new JSONObject(response);
            String ret = object.getString("ret");
            if (ret.endsWith("0")) {
                String data = object.getString("data");
                JSONObject dataObj = new JSONObject(data);
                String channelIdStr = dataObj.getString("channelid");
                long channelId = CommonUtil.parse2Long(channelIdStr);
                if (channelId == Long.MIN_VALUE) {
                    FileOperationUtil.saveExceptionInfoToFile("获取到的通道ID格式不正确:" + channelIdStr);
                    startRetryTimer(listener);
                } else {
                    if (noNeedRetry) {
                        noNeedRetry = false;
                        LogUtil.i("成功获取通道ID，取消定时器");
                        stopRetryTimer();
                    }
                    // 从网络获取通道 id 成功后，保存到 SharedPreferences 文件
                    PreferenceUtil.saveLong(PreferenceUtil.CHANNEL_ID, channelId);
                    CommonUtil.broadcastMessage(GPNSInterface.appContext, GPNSInterface.ACTION_CHANNEL_ID,
                            GPNSInterface.CHANNEL_ID, channelIdStr);
                    if (listener != null) {
                        listener.onSuccessed(channelIdStr);
                    }
                }

            } else {
                retry(object.getString("msg"), channelUrl, listener);
            }
        } catch (JSONException e) {
            retry("Json解析错误:" + e.toString(), channelUrl, listener);
        }
    }

    private static final int MAX = 3; // 最大的重试次数
    private static int count = 0; // 重试计数器

    private static boolean noNeedRetry = false;

    private static void retry(String errMsg, final String channelUrl, final ResultListener<String> listener) {
        if (listener != null) {
            listener.onFailed(errMsg);
        }

        if (noNeedRetry)
            return;

        if (count < MAX) {
            requestChannelId(channelUrl, listener);
            count++;
            LogUtil.e("获取通道ID,重试(" + count + ")");
        } else {
            if (new NetworkUtil(GPNSInterface.appContext).isConnected()) {
                // Url或者服务器出了问题，开定时器，每隔比较长的时间重试一次
                startRetryTimer(listener);
            } else {
                startNetWorkStatusMonitor();
            }
        }

    }

    private static final long TRY_INTERVAL_TIME = 5 * 60 * 1000;
    private static Timer retryTimer = null;
    private static TimerTask retryTask = null;

    private static void startRetryTimer(final ResultListener<String> listener) {
        if (retryTimer != null) {
            LogUtil.i("定时器正在运行中…");
            return;
        }

        FileOperationUtil.saveExceptionInfoToFile("Url或者服务器出了问题，开定时器，每隔" + (TRY_INTERVAL_TIME / 1000) + "秒重试一次");

        noNeedRetry = true;
        retryTimer = new Timer();
        retryTask = new TimerTask() {

            @Override
            public void run() {
                requestChannelId(channelUrl, listener);
            }
        };
        retryTimer.schedule(retryTask, TRY_INTERVAL_TIME, TRY_INTERVAL_TIME);
    }

    private static void stopRetryTimer() {
        if (retryTimer != null) {
            retryTimer.cancel();
            retryTimer = null;
        }
        if (retryTask != null) {
            retryTask.cancel();
            retryTask = null;
        }
    }

    private static void startNetWorkStatusMonitor() {
        LogUtil.i("注册网络监听器");
        IntentFilter filter = new IntentFilter();
        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        netWorkStatusMonitor = new NetWorkStatusMonitor();
        GPNSInterface.appContext.registerReceiver(netWorkStatusMonitor, filter);
    }

    private static void stopNetWorkStatusMonitor() {
        try {
            LogUtil.i("注销网络监听器");
            GPNSInterface.appContext.unregisterReceiver(netWorkStatusMonitor);
        } catch (Exception e) {
            LogUtil.printException2Console(e);
        }
    }

    private static NetWorkStatusMonitor netWorkStatusMonitor = null;

    private static class NetWorkStatusMonitor extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (!ConnectivityManager.CONNECTIVITY_ACTION.equals(intent.getAction())) {
                return;
            }
            NetworkInfo networkInfo = (NetworkInfo) intent.getParcelableExtra(ConnectivityManager.EXTRA_NETWORK_INFO);
            LogUtil.i("网络状态发生变化:" + networkInfo);
            if (networkInfo.isConnected()) {
                LogUtil.i("网络可用，重新尝试获取通道ID");
                stopNetWorkStatusMonitor();
                count = 0;
                requestChannelId(channelUrl, listener);
            }
        }
    }

}
