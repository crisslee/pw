package com.coomix.app.all.util;

import android.content.Context;
import com.coomix.app.all.AllOnlineApp;
import com.coomix.app.all.Constant;
import com.coomix.app.all.model.bean.RespDomainAdd;
import com.coomix.app.all.model.bean.Token;
import com.coomix.app.framework.util.PreferenceUtil;
import com.tencent.bugly.crashreport.CrashReport;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by think on 2017/1/19.
 */
public class ExperienceUserUtil {
    public static double[] parseExperienceQQUser(Token token) {
        AllOnlineApp.sAccount = token.name;
        AllOnlineApp.sTarget = AllOnlineApp.sAccount; // 登录时sTarget与sAccount相同
        AllOnlineApp.sToken = token;

        double centerLat = Constant.ShenzhenLat;
        double centerLng = Constant.ShenzhenLng;
        if (AllOnlineApp.getCurrentLocation() != null) {
            centerLat = AllOnlineApp.getCurrentLocation().getLatitude();
            centerLng = AllOnlineApp.getCurrentLocation().getLongitude();
            if (Math.abs(centerLat) > 1 || Math.abs(centerLng) > 1) {
                Constant.toprightlat = AllOnlineApp.getCurrentLocation().getLatitude() + Constant.LatSpan / 2.0;
                if (Constant.toprightlat > 90) {
                    Constant.toprightlat = 180 - Constant.toprightlat;
                }
                Constant.toprightlng = AllOnlineApp.getCurrentLocation().getLongitude() + Constant.LngSpan / 2.0;
                if (Constant.toprightlng > 180) {
                    Constant.toprightlng = 360 - Constant.toprightlng;
                }
                Constant.bottomleftlat = AllOnlineApp.getCurrentLocation().getLatitude() - Constant.LatSpan / 2.0;
                if (Constant.bottomleftlat < -90) {
                    Constant.bottomleftlat = -180 - Constant.bottomleftlat;
                }
                Constant.bottomleftlng = AllOnlineApp.getCurrentLocation().getLongitude() - Constant.LngSpan / 2.0;
                if (Constant.bottomleftlng < -180) {
                    Constant.bottomleftlat = -360 - Constant.bottomleftlng;
                }
            }
        }
        return new double[]{centerLat, centerLng};
    }
    public static Token getExperienceQQUserToken() {
        String jsonStr_sp = getUserJsonFromSp(AllOnlineApp.mApp);
        Token token = null;
        if(jsonStr_sp != null) {
            try {
                JSONObject json = new JSONObject(jsonStr_sp);
                token = userJsonToToken(json);
                return token;
            } catch (Exception e) {
                e.printStackTrace();
                CrashReport.postCatchedException(e);
            }
        }
        String jsonStr = "{\"data\":{\"name\":\"体验用户\",\"usertype\":\"7\",\"access_token\":\"200071000003101484877601328a10d9d885b8183ec12e4e26eb686f860000010017010\",\"expires_in\":7200,\"sp_name\":\"体验账号\",\"sp_contact\":\"123\",\"sp_phone\":\"123\",\"sp_addr\":\"\",\"account\":\"体验用户\",\"verify_phone\":\"\"},\"success\":true,\"errcode\":0,\"msg\":\"\"}";
        try {
            JSONObject json = new JSONObject(jsonStr);
            token = userJsonToToken(json);
        } catch (Exception e) {
            e.printStackTrace();
            CrashReport.postCatchedException(e);
        }
        return token;
    }

    public static Token userJsonToToken(JSONObject json) {
        Token token = new Token();
        JSONObject data = json.optJSONObject("data");
        token.access_token = data.optString("access_token");
        token.name = data.optString("name");
        token.expires_in = System.currentTimeMillis() / 1000 + data.optInt("expires_in");
        token.account = data.optString("account");
        token.loginType = data.optInt("login_type");
        token.usertype = data.optInt("usertype", 8);
        return token;
    }

    public static RespDomainAdd jsonToDomainAdd(JSONObject json) throws JSONException {
        RespDomainAdd mRespDomainAdd = null;
        if (json != null) {
            mRespDomainAdd = new RespDomainAdd();
            mRespDomainAdd.domainMain = json.getString("dy");
            mRespDomainAdd.domainWeb = json.getString("web");
            mRespDomainAdd.domainAd = json.getString("ad");
            mRespDomainAdd.domainGps = json.getString("gps");
            mRespDomainAdd.domainLog = json.getString("log");
            mRespDomainAdd.domainCfg = json.getString("cfg");
            mRespDomainAdd.domainGapi = json.getString("gapi");
            mRespDomainAdd.httpsFlag = json.getBoolean("https");
            mRespDomainAdd.backupIp = json.getString("bip");
            mRespDomainAdd.timestamp = json.getLong("timestamp");
        }
        return mRespDomainAdd;
    }

    public static void clearUserJsonFromSp(Context context){
        PreferenceUtil.init(context);
        PreferenceUtil.removeKey(Constant.PREFERENCE_USER_JSON);
    }

    public static void saveUserJsonToSp(Context context, String jsonStr) {
        PreferenceUtil.init(context);
        PreferenceUtil.commitString(Constant.PREFERENCE_USER_JSON, jsonStr);
    }

    public static String getUserJsonFromSp(Context context) {
        PreferenceUtil.init(context);
        return PreferenceUtil.getString(Constant.PREFERENCE_USER_JSON, null);
    }
}
