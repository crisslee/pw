package com.goome.gpns.utils;

import java.net.UnknownHostException;

import android.content.Context;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

public class NetworkUtil {
    private static ConnectivityManager connectMgr = null;
    private static NetworkInfo networkInfo;
    public static final String CTWAP = "ctwap";
    public static final String CMWAP = "cmwap";
    public static final String WAP_3G = "3gwap";
    public static final String UNIWAP = "uniwap";
    public static final int TYPE_NET_WORK_DISABLED = 0;
    public static final int TYPE_CM_CU_WAP = 4; 
    public static final int TYPE_CT_WAP = 5;
    public static final int TYPE_OTHER_NET = 6;
    public static Uri PREFERRED_APN_URI = Uri.parse("content://telephony/carriers/preferapn");

    public NetworkUtil(Context context) {
        connectMgr = (ConnectivityManager) context.getApplicationContext().getSystemService(
                Context.CONNECTIVITY_SERVICE);
    }
    
    public static String getIP(String host){
        System.out.println("get ip:"+host);
        java.net.InetAddress x;
        try {
                x = java.net.InetAddress.getByName(host);
                String ip_devdiv = x.getHostAddress();//�õ��ַ���ʽ��ip��ַ
                return ip_devdiv;
        } catch (UnknownHostException e) {
        	LogUtil.printException2Console(e);
        	FileOperationUtil.saveErrMsgToFile("getIP() occur an exception:"+e.toString());
        } 
        return null;
    }

    public static NetworkInfo.State getNetWorkStatus() {
        networkInfo = connectMgr.getActiveNetworkInfo();
        if (networkInfo != null) {
            return networkInfo.getState();
        } else {
            return NetworkInfo.State.DISCONNECTED;
        }
    }
    
    public boolean isConnected() {
        networkInfo = connectMgr.getActiveNetworkInfo();
        if (networkInfo != null) {
            return networkInfo.isConnected();
        } else{
        	return false;
        }
    }

    public static boolean isConnected(Context context) {
    	if(connectMgr == null){
    		 connectMgr = (ConnectivityManager) context.getApplicationContext().getSystemService(
    	                Context.CONNECTIVITY_SERVICE);
    	}
        networkInfo = connectMgr.getActiveNetworkInfo();
        if (networkInfo != null) {
            return networkInfo.isConnected();
        } else{
        	return false;
        }
    }
    
    public static boolean isNetWorkTypeAvailable(int networkType) {
        NetworkInfo networkInfo = connectMgr.getNetworkInfo(networkType);
        if (networkInfo != null) {
            return networkInfo.isAvailable();
        }
        return false;
    }

    public static boolean isWifi() {
        NetworkInfo ni = connectMgr.getActiveNetworkInfo();
        if (!ni.getTypeName().equals("WIFI")) {
             
            return true;
        }
        return false;
    }

    public static int checkNetworkType(Context mContext) {
        try {
            final ConnectivityManager connectivityManager = (ConnectivityManager) mContext
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            final NetworkInfo mobNetInfoActivity = connectivityManager.getActiveNetworkInfo();
            if (mobNetInfoActivity == null || !mobNetInfoActivity.isAvailable()) {
                 
                return TYPE_OTHER_NET;
            } else {


                int netType = mobNetInfoActivity.getType();
                if (netType == ConnectivityManager.TYPE_WIFI) {
                    return TYPE_OTHER_NET;
                } else if (netType == ConnectivityManager.TYPE_MOBILE) {

                  
                    final Cursor c = mContext.getContentResolver().query(PREFERRED_APN_URI, null,
                            null, null, null);
                    if (c != null) {
                        c.moveToFirst();
                        final String user = c.getString(c.getColumnIndex("user"));
                        if (!TextUtils.isEmpty(user)) {
                            if (user.startsWith(CTWAP)) {
                                return TYPE_CT_WAP;
                            }
                        }
                        c.close();
                    }

                    String netMode = mobNetInfoActivity.getExtraInfo();
                    Log.i("", "netMode ================== " + netMode);
                    if (netMode != null) {
                        netMode = netMode.toLowerCase();
                        if (netMode.equals(CMWAP) || netMode.equals(WAP_3G)
                                || netMode.equals(UNIWAP)) {
                            return TYPE_CM_CU_WAP;
                        }
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            return TYPE_OTHER_NET;
        }

        return TYPE_OTHER_NET;
    }

    public static NetworkInfo getActiveNetworkInfo() {
        return connectMgr.getActiveNetworkInfo();
    }

}