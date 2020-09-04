package com.coomix.app.all;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;
import android.util.Log;
import com.amap.api.location.AMapLocation;
import com.coomix.app.all.manager.SettingDataManager;
import com.coomix.app.framework.app.BaseApiClient;
import com.coomix.app.framework.util.CommonUtil;
import com.coomix.app.framework.util.OSUtil;
import com.coomix.security.Security;
import com.tencent.bugly.crashreport.CrashReport;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

/**
 * 这个类保存所有必要的参数，供网络请求用
 * 其中instance 不能为null。
 * Created by goome on 2017/5/18.
 */
public class GlobalParam {

    private static GlobalParam instance;

    private Context mContext = AllOnlineApp.mApp.getApplicationContext();
    private Security security;
    private GlobalParam() {
        security = new Security();
    }

    public static GlobalParam getInstance() {
        if (instance == null) {
            synchronized (GlobalParam.class) {
                if (instance == null) {
                    instance = new GlobalParam();
                }
            }
        }
        return instance;
    }

    public String get_cid() {
        return AllOnlineApp.channelId(mContext);
    }

    public String get_version() {
        return OSUtil.getAppVersionNameNoV(mContext);
    }

    public String get_n() {
        return OSUtil.getUdid(mContext);
    }

    public String get_Language() {
        return SettingDataManager.language;
    }

    public String get_source() {
        return Constant.NEW_VERSION_TAG;
    }

    public long get_t() {
        return System.currentTimeMillis() / 1000;
    }

    public String get_ver() {
        return BaseApiClient.COMMUNITY_COOMIX_VERSION;
    }

    //社区的签名
    public String get_community_sign() {
        String ver = BaseApiClient.COMMUNITY_COOMIX_VERSION;
        String sign;
        try {
            sign = security.hashProcess(get_t() + Constant.PRIVATE_KEY + get_n() + ver, ver, mContext);
        } catch (Throwable e) {
            CrashReport.postCatchedException(e);
            if (Constant.IS_DEBUG_MODE) {
                e.printStackTrace();
            }
            return "";
        }
        return sign;
    }

    //爱车安的签名
    public String get_caronline_token() {
        return OSUtil.toMD5(get_t() + Constant.PRIVATE_KEY + get_n() + get_version());
    }

    public String getAccessToken() {
        String token = "";
        if (AllOnlineApp.sToken != null) {
            token = AllOnlineApp.sToken.access_token;
        }
        return token;
    }

    public String get_os() {
        return "android";
    }

    public long get_httpSeq() {
        return BaseApiClient.getHttpSeq();
    }

    public String get_apptype() {
        return BaseApiClient.APP_TYPE;
    }

    public double get_lat() {
        double lat = 0;
        double lng = 0;
        AMapLocation location = AllOnlineApp.getCurrentLocation();
        if (location != null) {
            lat = location.getLatitude();
        }
        return lat;
    }

    public double get_lng() {
        double lat = 0;
        double lng = 0;
        AMapLocation location = AllOnlineApp.getCurrentLocation();
        if (location != null) {
            lng = location.getLongitude();
        }
        return lng;
    }

    public int get_versionCode() {
        int versionCode = 1;
        try {
            versionCode = mContext.getPackageManager().getPackageInfo(mContext.getPackageName(), 0).versionCode;
        } catch (Exception e) {
            CrashReport.postCatchedException(e);
            e.printStackTrace();
        }

        return versionCode;
    }

    public int get_appID() {
        return Constant.COOMIX_APP_ID;
    }

    public Map<String, String> getCommonParas() {
        HashMap<String, String> map = new HashMap<>();
        try {
            long t = get_t();
            map.put("t", String.valueOf(t));
            /* map.put("time", String.valueOf(t));*/
            map.put("n", get_n());
            map.put("sign", get_community_sign());
            map.put("appver", get_version());
            map.put("ver", get_ver());
            map.put("os", "android");
            map.put("access_type", "inner");
            map.put("lang", get_Language());
            map.put("source", get_source());
            map.put("http_seq", String.valueOf(get_httpSeq()));
            map.put("apptype", get_apptype());
            map.put("mlat", String.valueOf(get_lat()));
            map.put("mlng", String.valueOf(get_lng()));
            map.put("vercode", String.valueOf(get_versionCode()));
            map.put("appid", String.valueOf(get_appID()));
            map.put("cn", "gm");
            map.put("posmaptype", SettingDataManager.strMapType);
            map.put("maptype", SettingDataManager.strMapType);
            map.put("map_type", SettingDataManager.strMapType);
            if (!TextUtils.isEmpty(CommonUtil.getTicket())) {
                map.put("ticket", CommonUtil.getTicket());
            }
        } catch (Exception e) {
            if (Constant.IS_DEBUG_MODE) {
                Log.e(GlobalParam.class.getCanonicalName(), e.getMessage());
            }
            CrashReport.postCatchedException(e);
        }
        return map;
    }

    public Map<String, String> getCommonParas(String appType) {
        HashMap<String, String> map = new HashMap<>();
        try {
            long t = get_t();
            map.put("t", String.valueOf(t));
           /* map.put("time", String.valueOf(t));*/
            map.put("n", get_n());
            map.put("sign", get_community_sign());
            map.put("appver", get_version());
            map.put("ver", get_ver());
            map.put("os", "android");
            map.put("access_type", "inner");
            map.put("lang", get_Language());
            map.put("source", get_source());
            map.put("http_seq", String.valueOf(get_httpSeq()));
            map.put("apptype", appType);
            map.put("mlat", String.valueOf(get_lat()));
            map.put("mlng", String.valueOf(get_lng()));
            map.put("vercode", String.valueOf(get_versionCode()));
            map.put("appid", String.valueOf(get_appID()));
            map.put("cn", "gm");
            map.put("posmaptype", SettingDataManager.strMapType);
            map.put("maptype", SettingDataManager.strMapType);
            map.put("map_type", SettingDataManager.strMapType);
            if (!TextUtils.isEmpty(CommonUtil.getTicket())) {
                map.put("ticket", CommonUtil.getTicket());
            }
        } catch (Exception e) {
            if (Constant.IS_DEBUG_MODE) {
                Log.e(GlobalParam.class.getCanonicalName(), e.getMessage());
            }
            CrashReport.postCatchedException(e);
        }
        return map;
    }

    public Map<String, String> getLocTicketParas() {
        HashMap<String, String> map = new HashMap<>();
        try {
            long t = get_t();
            map.put("lat", String.valueOf(get_lat()));
            map.put("lng", String.valueOf(get_lng()));
            map.put("ticket", CommonUtil.getTicket());
        } catch (Exception e) {
            if (Constant.IS_DEBUG_MODE) {
                Log.e(GlobalParam.class.getCanonicalName(), e.getMessage());
            }
            CrashReport.postCatchedException(e);
        }

        return map;
    }

    public String getCommonParasString() {
        Map<String, String> map = getCommonParas();
        StringBuilder sb = new StringBuilder();

        for (HashMap.Entry<String, String> e : map.entrySet()) {
            if (sb.length() > 0) {
                sb.append('&');
            }
            try {
                sb.append(URLEncoder
                        .encode(e.getKey(), "UTF-8"))
                        .append('=')
                        .append(URLEncoder.encode(e.getValue(), "UTF-8"));
            } catch (UnsupportedEncodingException ex) {
                CrashReport.postCatchedException(ex);
            }
        }
        return sb.toString();
    }

    public  synchronized String getChannelId()
    {
        SharedPreferences sharedPrefs = AllOnlineApp.mApp.getSharedPreferences(Constant.PREF_UNIQUE_GPNS_REGID,
            Context.MODE_PRIVATE);
        String uniqueID = sharedPrefs.getString(Constant.PREF_UNIQUE_GPNS_REGID, null);

        return uniqueID;
    }
}
