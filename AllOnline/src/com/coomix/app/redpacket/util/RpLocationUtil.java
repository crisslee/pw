package com.coomix.app.redpacket.util;

import android.content.Context;
import com.amap.api.location.AMapLocation;
import com.coomix.app.all.AllOnlineApp;
import com.coomix.app.all.R;
import com.coomix.app.all.util.CommunityUtil;
import com.coomix.app.all.util.GpsUtil;

/**
 * Created by felixqiu
 *
 * @since 2017/3/4.
 */
public class RpLocationUtil {
    //将整数范围转为xxKM (eg:3500->3.5KM)
    //+ 小数点后保留一位
    public static String getRangeString(Context context, int range) {
        context = context.getApplicationContext();
        if (range == 0) {
            return context.getString(R.string.redpacket_range_none);
        }
        if (range < 1000) {
            return context.getString(R.string.redpacket_range_m, String.valueOf(range));
        } else {
            String strRange = "";
            double r = range / 1000.0;
            if (Math.round(r) - r == 0) {
                strRange = String.valueOf((long) r);
            } else {
                strRange = String.valueOf(r);
            }
            int dotIndex = strRange.indexOf(".");
            if (dotIndex > 0 && strRange.length() > dotIndex + 2) {
                strRange = strRange.substring(0, dotIndex + 2);
            }
            return context.getString(R.string.redpacket_range_km, strRange);
        }
    }

    public static boolean isCurrLocationValid() {
        AMapLocation currLocation = AllOnlineApp.getCurrentLocation();
        return !(currLocation.getLatitude() == 0 && currLocation.getLongitude() == 0);
    }

    public static String getExtendInfo(Context context) {
        StringBuilder sb = new StringBuilder();
        sb.append("{isMockOn:");
        sb.append(GpsUtil.isMockSettingOn(context) ? 1 : 0);
        sb.append(", mockApplist:");
        sb.append(GpsUtil.areThereMockPermissionApps(context));
        sb.append(", isRoot:");
        sb.append(CommunityUtil.isRoot() ? 1 : 0);
        sb.append(", isXposed:");
        //sb.append(TinkerUtil.isXposedExists(new Exception()) ? 1 : 0);
        sb.append("}");
        return sb.toString();
    }
}
