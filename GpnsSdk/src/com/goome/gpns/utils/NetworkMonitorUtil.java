package com.goome.gpns.utils;

import com.goome.gpns.GPNSInterface;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class NetworkMonitorUtil {
    private static NetWorkStatusMonitor netWorkStatusMonitor = null;

    public static void startNetWorkStatusMonitor() {
        LogUtil.i("注册网络监听器");
        IntentFilter filter = new IntentFilter();
        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        netWorkStatusMonitor = new NetWorkStatusMonitor();
        GPNSInterface.appContext.registerReceiver(netWorkStatusMonitor, filter);
    }

    public static void stopNetWorkStatusMonitor() {
        try {
            LogUtil.i("注销网络监听器");
            GPNSInterface.appContext.unregisterReceiver(netWorkStatusMonitor);
            netWorkStatusMonitor = null;
        } catch (Exception e) {
            LogUtil.printException2Console(e);
            FileOperationUtil.saveErrMsgToFile("stopNetWorkStatusMonitor() occur an exception");
        }
    }

    private static class NetWorkStatusMonitor extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (!ConnectivityManager.CONNECTIVITY_ACTION.equals(intent.getAction())) {
                return;
            }
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
    }
}
