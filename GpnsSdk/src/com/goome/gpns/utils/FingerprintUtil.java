package com.goome.gpns.utils;

import android.content.Context;
import android.os.Build;
import android.provider.Settings.Secure;
import android.text.TextUtils;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * 获取设备指纹的工具类
 * @author HZJ
 * @DATE 2015-8-19
 */

public class FingerprintUtil {
    /**
     * 获取设备指纹
     * 如果从SharedPreferences文件中拿不到，那么重新生成一个，
     * 并保存到SharedPreferences文件中。
     *
     * @return fingerprint 设备指纹
     */
    public static String getFingerprint(Context context) {
        String fingerprint = null;
        fingerprint = readFingerprintFromFile();
        if (TextUtils.isEmpty(fingerprint)) {
            fingerprint = createFingerprint(context);
        } else {
            LogUtil.i("从文件中获取设备指纹：" + fingerprint);
        }
        return fingerprint;
    }

    /**
     * 生成一个设备指纹（耗时一般在50毫秒以内）：
     * 1.IMEI + 设备硬件信息 + ANDROID_ID 拼接成的字符串
     * 2.用MessageDigest将以上字符串处理成32位的16进制字符串
     *
     * @return 设备指纹
     */
    private static String createFingerprint(Context context) {

        // 1.IMEI
        //TelephonyManager TelephonyMgr = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        //final String imei = TelephonyMgr.getDeviceId();
        //LogUtil.i("imei=" + imei);
        //FileOperationUtil.saveFingerprintInfoToFile("imei:" + imei);

        /*// 来自网上的方法 2.Pseudo-Unique ID, 这个在任何Android手机中都有效
        String hardwareInfo = "35"
                + // we make this look like a valid IMEI
                Build.BOARD.length() % 10 + Build.BRAND.length() % 10
                + Build.CPU_ABI.length() % 10 + Build.DEVICE.length() % 10
                + Build.DISPLAY.length() % 10 + Build.HOST.length() % 10
                + Build.ID.length() % 10 + Build.MANUFACTURER.length() % 10
                + Build.MODEL.length() % 10 + Build.PRODUCT.length() % 10
                + Build.TAGS.length() % 10 + Build.TYPE.length() % 10
                + Build.USER.length() % 10; // 13 digits
        LogUtil.i(TAG, "hardward info="+hardwareInfo);*/

        //2.android 设备硬件信息 //小米note2上Build.CPU_ABI会在armeabi-v7a和arm64-v8a不同时变化
        final String hardwareInfo = /*Build.ID + Build.DISPLAY +*/ Build.PRODUCT
            + Build.DEVICE + Build.BOARD /*+ Build.CPU_ABI*/
            + Build.MANUFACTURER + Build.BRAND + Build.MODEL
            /*+ Build.BOOTLOADER*/ + Build.HARDWARE
            /*+ Build.TYPE + Build.TAGS*/ + Build.FINGERPRINT /*+ Build.HOST
                + Build.USER*/;
        LogUtil.i("hardward info:" + hardwareInfo);
        FileOperationUtil.saveFingerprintInfoToFile("hardward info:" + hardwareInfo);

        /* 3. Android_id 恢复出厂会变
         * A 64-bit number (as a hex string) that is randomly
        * generated when the user first sets up the device and should remain
        * constant for the lifetime of the user's device. The value may
        * change if a factory reset is performed on the device.
        */
        final String androidId = Secure.getString(context.getContentResolver(), Secure.ANDROID_ID);
        LogUtil.i("android_id:" + androidId);
        FileOperationUtil.saveFingerprintInfoToFile("android_id:" + androidId);

        // 4. The WLAN MAC Address string,个别测试机有时获取不到
        /*WifiManager wifiMgr = (WifiManager) context
                .getSystemService(Context.WIFI_SERVICE);
        final String wifiMAC = wifiMgr.getConnectionInfo().getMacAddress();
        LogUtil.i("wifi Mac="+wifiMAC);*/

        /*
         *  5. get the bluetooth MAC Address
         *  有部分手机(如三星GT-S5660 2.3.4)当蓝牙关闭时，获取不到蓝牙MAC;
         *  所以为了保证 device id 的不变，舍去
         */
        /*BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        String bt_MAC = null;
        if (bluetoothAdapter == null) {
            LogUtil.e(TAG, "bluetoothAdapter is null");
        } else {
            bt_MAC = bluetoothAdapter.getAddress();
        }
        LogUtil.i("m_szBTMAC="+bt_MAC);*/

        // Combined Device ID
        final String deviceId = hardwareInfo + androidId;
        LogUtil.i("deviceId=" + deviceId);
        FileOperationUtil.saveFingerprintInfoToFile("未处理前的设备总信息：" + deviceId);

        // 创建一个 messageDigest 实例
        MessageDigest msgDigest = null;
        try {
            msgDigest = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            FileOperationUtil.saveErrMsgToFile("createFingerprint() occur an exception:" + e.toString());
            LogUtil.printException2Console(e);
        }

        //用 MessageDigest 将 deviceId 处理成32位的16进制字符串
        msgDigest.update(deviceId.getBytes(), 0, deviceId.length());
        // get md5 bytes
        byte md5ArrayData[] = msgDigest.digest();

        // create a hex string
        String deviceUniqueId = new String();
        for (int i = 0; i < md5ArrayData.length; i++) {
            int b = (0xFF & md5ArrayData[i]);
            // if it is a single digit, make sure it have 0 in front (proper
            // padding)
            if (b <= 0xF) deviceUniqueId += "0";
            // add number to string
            deviceUniqueId += Integer.toHexString(b);
        } // hex string to uppercase
        deviceUniqueId = deviceUniqueId.toUpperCase();
        LogUtil.i("生成的设备指纹：" + deviceUniqueId);
        FileOperationUtil.saveFingerprintInfoToFile("MD5处理完后的设备指纹：" + deviceUniqueId);

        PreferenceUtil.saveString(PreferenceUtil.DEVICE_FINGERPRINT, deviceUniqueId);

        return deviceUniqueId;
    }

    /**
     * 从SharedPreferences 文件获取设备指纹
     *
     * @return fingerprint 设备指纹
     */
    private static String readFingerprintFromFile() {
        return PreferenceUtil.getString(PreferenceUtil.DEVICE_FINGERPRINT, null);
    }
}
