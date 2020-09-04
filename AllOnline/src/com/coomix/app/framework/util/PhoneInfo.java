package com.coomix.app.framework.util;

import android.content.Context;
import android.telephony.TelephonyManager;
import com.coomix.app.all.util.PermissionUtil;

/**
 * 读取手机设备信息测试代码 http://www.souapp.com 搜应用网 song2c@163.com 宋立波
 */
public class PhoneInfo {

    private TelephonyManager tm;
    /**
     * 国际移动用户识别码
     */
    private String IMSI;
    private Context ctx;

    public PhoneInfo(Context context) {
        ctx = context;
        tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
    }

    /**
     * 获取电话号码
     */
    @SuppressWarnings("MissingPermission")
    public String getNativePhoneNumber() {
        String phoneNumber = null;
        if (PermissionUtil.hasPhonePermission(ctx)) {
            try {
                phoneNumber = tm.getLine1Number();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return phoneNumber;
    }

    /**
     * 获取手机服务商信息
     */
    @SuppressWarnings("MissingPermission")
    public String getProvidersName() {
        String providerName = "N/A";
        try {
            if (PermissionUtil.hasPhonePermission(ctx)) {
                IMSI = tm.getSubscriberId();
            } else {
                IMSI = "";
            }
            // IMSI号前面3位460是国家，紧接着后面2位00 02是中国移动，01是中国联通，03是中国电信。
            if (IMSI.startsWith("46000") || IMSI.startsWith("46002")) {
                providerName = "中国移动";
            } else if (IMSI.startsWith("46001")) {
                providerName = "中国联通";
            } else if (IMSI.startsWith("46003")) {
                providerName = "中国电信";
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return providerName;
    }

    public String getPhoneInfo() {
        if (tm == null) {
            tm = (TelephonyManager) ctx.getSystemService(Context.TELEPHONY_SERVICE);
        }
        boolean hasPermission = PermissionUtil.hasPhonePermission(ctx);
        StringBuilder sb = new StringBuilder();

        try {
            sb.append("\nDeviceId(IMEI) = ").append(hasPermission ? tm.getDeviceId() : null);
            sb.append("\nDeviceSoftwareVersion = ").append(hasPermission ? tm.getDeviceSoftwareVersion() : null);
            sb.append("\nLine1Number = ").append(hasPermission ? tm.getLine1Number() : null);
            sb.append("\nNetworkCountryIso = ").append(tm.getNetworkCountryIso());
            sb.append("\nNetworkOperator = ").append(tm.getNetworkOperator());
            sb.append("\nNetworkOperatorName = ").append(tm.getNetworkOperatorName());
            sb.append("\nNetworkType = ").append(tm.getNetworkType());
            sb.append("\nPhoneType = ").append(tm.getPhoneType());
            sb.append("\nSimCountryIso = ").append(tm.getSimCountryIso());
            sb.append("\nSimOperator = ").append(tm.getSimOperator());
            sb.append("\nSimOperatorName = ").append(tm.getSimOperatorName());
            sb.append("\nSimSerialNumber = ").append(hasPermission ? tm.getSimSerialNumber() : null);
            sb.append("\nSimState = ").append(tm.getSimState());
            sb.append("\nSubscriberId(IMSI) = ").append(hasPermission ? tm.getSubscriberId() : null);
            sb.append("\nVoiceMailNumber = ").append(hasPermission ? tm.getVoiceMailNumber() : null);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return sb.toString();
    }
}
