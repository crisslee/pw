package com.coomix.app.framework.util;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.provider.Settings.Secure;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.TypedValue;
import com.coomix.app.all.Constant;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.UUID;

public class OSUtil {
    private static final String TAG = "OSUtil";

    private static List<String> INVALID_IMEIs = new ArrayList<String>();
    static {
        INVALID_IMEIs.add("358673013795895");
        INVALID_IMEIs.add("004999010640000");
        INVALID_IMEIs.add("00000000000000");
        INVALID_IMEIs.add("000000000000000");
    }

    public static boolean isValidImei(String imei) {
        if (TextUtils.isEmpty(imei))
            return false;
        if (imei.length() < 10)
            return false;
        if (INVALID_IMEIs.contains(imei))
            return false;
        return true;
    }

    /**
     * @see http://code.google.com/p/android/issues/detail?id=10603
     */
    private static final String INVALID_ANDROIDID = "9774d56d682e549c";

    /**
     * get udid</BR>
     * imei -> androidId -> mac address -> uuid
     * 
     * 
     * @param context
     * @return
     */
    public static String getOldUdid(Context context) {
        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        String udid = telephonyManager.getDeviceId();

        if (isValidImei(udid)) {
            return udid;
        }

        udid = Secure.getString(context.getContentResolver(), Secure.ANDROID_ID);
        if (!TextUtils.isEmpty(udid) && !INVALID_ANDROIDID.equals(udid.toLowerCase(Locale.getDefault()))) {
            return udid;
        }

        String macAddress = getWifiMacAddress(context);
        if (!TextUtils.isEmpty(macAddress)) {
            udid = toMD5(macAddress + Build.MODEL + Build.MANUFACTURER + Build.ID + Build.DEVICE);
            return udid;
        }
        String name = String.valueOf(System.currentTimeMillis());
        udid = UUID.nameUUIDFromBytes(name.getBytes()).toString();
        
        return udid;
    }

    public static final String allChar = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
    public static String generateString(int length) {
        StringBuffer sb = new StringBuffer();
        Random random = new Random();
        for (int i = 0; i < length; i++) {
            sb.append(allChar.charAt(random.nextInt(allChar.length())));
        }
        return sb.toString();
    }
    public static String getUdid(Context context){
        PreferenceUtil.init(context);
        String fingerprint = readFingerprintFromFile();
        if (TextUtils.isEmpty(fingerprint)) {
            fingerprint = OSUtil.createFingerprint(context);
            if(!TextUtils.isEmpty(fingerprint)){
                saveFingerprintToFile(fingerprint);
            }
        }
        return fingerprint;
    }

    /**
     * 从SharedPreferences 文件获取设备指纹
     *
     * @return fingerprint 设备指纹
     */
    private static String readFingerprintFromFile() {
        return PreferenceUtil.getString(Constant.PREFERENCE_SINGLE_UDID, "");
    }

    private static void saveFingerprintToFile(String fingerprint) {
        PreferenceUtil.commitString(Constant.PREFERENCE_SINGLE_UDID, fingerprint);
    }

    /**
     * 生成一个设备指纹（耗时一般在50毫秒以内）：
     * 1.IMEI + 设备硬件信息 + ANDROID_ID 拼接成的字符串
     * 2.用MessageDigest将以上字符串处理成32位的16进制字符串
     *
     * @return 设备指纹
     */
    public static String createFingerprint(Context context) {

        // 1.IMEI，需要READ_PHONE_STATE权限，放弃
        //TelephonyManager TelephonyMgr = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        //final String imei = TelephonyMgr.getDeviceId();

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

        /* 3. Android_id 恢复出厂会变
         * A 64-bit number (as a hex string) that is randomly
        * generated when the user first sets up the device and should remain
        * constant for the lifetime of the user's device. The value may
        * change if a factory reset is performed on the device.
        */
        final String androidId = Secure.getString(context.getContentResolver(), Secure.ANDROID_ID);

        // 4. The WLAN MAC Address string,个别测试机有时获取不到
        /*WifiManager wifiMgr = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
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

        // 创建一个 messageDigest 实例
        MessageDigest msgDigest = null;
        try {
            msgDigest = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            //FileOperationUtil.saveErrMsgToFile("createFingerprint() occur an exception:"+e.toString());
            //LogUtil.printException2Console(e);
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

        if (!TextUtils.isEmpty(deviceUniqueId)) {
            return deviceUniqueId;
        } else {
            return "";
        }

        /**
         * Codes bellow may generate multiple sign for a device. Comment out.
         */
        //      String name = String.valueOf(System.currentTimeMillis());
        //        try {
        //          deviceUniqueId = UUID.nameUUIDFromBytes(name.getBytes()).toString();
        //          deviceUniqueId = toMD5(deviceUniqueId).toUpperCase();
        //          return deviceUniqueId;
        //        }catch (Exception e) {
        //          return "";
        //      }
    }

    public static String getUpdateUdid() {
        String name = String.valueOf(System.currentTimeMillis());
        String udid = null;
        try {
            udid = UUID.nameUUIDFromBytes(name.getBytes()).toString();
            udid = toMD5(udid).toUpperCase();
            return udid;
        }catch (Exception e) {
            return null;
        }
    }

    public static String toMD5(String encTarget) {
        if (encTarget == null) {
            return null;
        }
        byte[] hash;

        try {
            hash = MessageDigest.getInstance("MD5").digest(encTarget.getBytes("UTF-8"));
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Huh, MD5 should be supported?", e);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("Huh, UTF-8 should be supported?", e);
        }
        StringBuilder hex = new StringBuilder(hash.length * 2);
        for (byte b : hash) {
            int i = (b & 0xFF);
            if (i < 0x10)
                hex.append('0');
            hex.append(Integer.toHexString(i));
        }
        return hex.toString();
    }
    
    public static String adtoMD5(String encTarget) {
        MessageDigest mdEnc = null;
        try {
            mdEnc = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            //System.out.println("Exception while encrypting to md5");
            e.printStackTrace();
        }
        mdEnc.update(encTarget.getBytes(), 0, encTarget.length());
        /**
         * 会将开始的0忽略，生成的字符串长度<=32
         */
        // String md5 = new BigInteger(1, mdEnc.digest()).toString(16);
        // return md5;
        byte b[] = mdEnc.digest();
        int i;
        StringBuffer buf = new StringBuffer("");
        for (int offset = 0; offset < b.length; offset++) {
            i = b[offset];
            if (i < 0) {
                i += 256;
            }
            if (i < 16) {
                buf.append("0");
            }
            buf.append(Integer.toHexString(i));
        }
        return buf.toString();
    }

    public static String getWifiMacAddress(final Context context) {
        try {
            WifiManager wifimanager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
            String mac = wifimanager.getConnectionInfo().getMacAddress();
            if (TextUtils.isEmpty(mac))
                return null;
            return mac;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * print key-value pairs in the bundle
     * 
     * @param bundle
     * @return
     */
    public static String printBundle(Bundle bundle) {
        StringBuilder sb = new StringBuilder();
        for (String key : bundle.keySet()) {
            sb.append("\nkey:" + key + ", value:" + bundle.getString(key));
        }
        return sb.toString();
    }

    public static int getCurrentSdkVersion() {
        return Build.VERSION.SDK_INT;
    }

    /**
     * get application version name writed in the manifest
     */
    public static String getAppVersionNameOld(Context context) {
        String versionName = "";
        try {
            // ---get the package info---
            PackageManager pm = context.getPackageManager();
            PackageInfo pi = pm.getPackageInfo(context.getPackageName(), 0);
            versionName = pi.versionName;
            if (TextUtils.isEmpty(versionName)) {
                return "";
            }
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }
        return versionName;
    }
    
    public static String getAppVersionName(Context context) {
//        String versionName = "";
//        try {
//            // ---get the package info---
//            PackageManager pm = context.getPackageManager();
//            PackageInfo pi = pm.getPackageInfo(context.getPackageName(), 0);
//            versionName = pi.versionName;
//            if (versionName == null || versionName.length() <= 0) {
//                return "";
//            } else {
//                return versionName.substring(0, versionName.indexOf("("));
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
        return getAppVersionNameOld(context);
    }

    public static String getAppVersionDate(Context context) {
//        try {
//            // ---get the package info---
//            PackageManager pm = context.getPackageManager();
//            PackageInfo pi = pm.getPackageInfo(context.getPackageName(), 0);
//            String versionName = pi.versionName;
//            if (versionName == null || versionName.length() <= 0) {
//                return "";
//            } else {
//                return versionName.substring(versionName.indexOf("(") + 1, versionName.indexOf(")"));
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return "";
        return getAppVersionNameOld(context);
    }
    
    private static String sVersionName = null;
    /**
     * 返回当前程序版本名(含V,测试版可能含DEBUG,DEV) 如 V3.1.6，V3.1.6DEV
     */
    public static String getAppVersionNameExtend(Context context) {
        return getAppVersionNameOld(context);
    }

    private static String versionNameNoV = null;

    /**
     * 获取仅含数字和小数点的版本名(如3.1.6)
     */
    public static String getAppVersionNameNoV(Context context) {
        if (TextUtils.isEmpty(versionNameNoV)) {
            //			try{
//				versionNameNoV = getAppVersionNameExtend(context).replace("IS_DEBUG_MODE", "").replace("DEV", "").replace("V", "")
//						.replace("_A", "").replace("_B", "").replace("_C", "").replace("_D", "").replace("_E", "");
//				if (versionNameNoV != null && versionNameNoV.length() > 5){
//					versionNameNoV = versionNameNoV.substring(0, 5);
//				}
//			}
//			catch (Exception e){
//				versionNameNoV = "";
//			}
            try{
                versionNameNoV = getAppVersionNameOld(context);
            }
            catch (Exception e){
                versionNameNoV = "";
            }
        }
        if (versionNameNoV == null) {
            versionNameNoV = "";
        }
        return versionNameNoV;
    }

    public static void sendEmail(Context context, String chooserTitle, String mailAddress, String subject,
            String preContent) {
        final Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);
        emailIntent.setType("plain/text");
        emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[] { mailAddress });
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, subject);
        String content = "\n\n=====================\n";
        content += "Device Environment: \n----\n" + preContent;
        emailIntent.putExtra(Intent.EXTRA_TEXT, content);
        context.startActivity(Intent.createChooser(emailIntent, chooserTitle));
    }

    // some apps only show content, some apps show both subject and content
    public static Intent getAndroidShareIntent(CharSequence chooseTitle, CharSequence subject, CharSequence content) {
        Intent shareIntent = new Intent(android.content.Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, subject);
        shareIntent.putExtra(android.content.Intent.EXTRA_TEXT, content);
        return Intent.createChooser(shareIntent, chooseTitle);
    }

    public static Intent getAndroidImageShareIntent(CharSequence chooseTitle, String pathfile) {
        Intent share = new Intent(Intent.ACTION_SEND);
        share.setType("image/*");
        share.putExtra(Intent.EXTRA_STREAM, Uri.parse(pathfile));
        return Intent.createChooser(share, chooseTitle);
    }

    /**
     * 
     * @see android.telephony.TelephonyManager#getDeviceId()
     * @return imei
     */
    public static String getImei(TelephonyManager telephonyManager) {
        try {
            return telephonyManager.getDeviceId();
        } catch (SecurityException e) {
            e.printStackTrace();
        }
        return "";
    }

    /**
     * @see android.telephony.TelephonyManager#getSubscriberId()
     * @return imsi
     */
    public static String getImsi(TelephonyManager telephonyManager) {
        try {
            return telephonyManager.getSubscriberId();
        } catch (SecurityException e) {
            e.printStackTrace();
        }
        return "";
    }

    public static String getSimSerialNumber(TelephonyManager telephonyManager) {
        try {
            return telephonyManager.getSimSerialNumber();
        } catch (SecurityException e) {
            e.printStackTrace();
        }
        return "";
    }

    public static int getMcc(TelephonyManager telephonyManager) {
        String imsi = getImsi(telephonyManager);
        if (!TextUtils.isEmpty(imsi))
            return Integer.valueOf(imsi.substring(0, 3));
        return -1;
    }

    public static int getMnc(TelephonyManager telephonyManager) {
        String imsi = getImsi(telephonyManager);
        if (!TextUtils.isEmpty(imsi))
            return Integer.valueOf(imsi.substring(3, 5));
        return -1;
    }

    /**
     * dip转换成px
     * 
     * @param context
     * @param dip
     * @return
     */
    public static float dip2px(Context context, int dip) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dip,
                context.getApplicationContext().getResources().getDisplayMetrics());
    }

    private OSUtil() {
    };

    @TargetApi(11)
    public static void enableStrictMode() {
        if (OSUtil.hasGingerbread()) {
            StrictMode.ThreadPolicy.Builder threadPolicyBuilder = new StrictMode.ThreadPolicy.Builder().detectAll()
                    .penaltyLog();
            StrictMode.VmPolicy.Builder vmPolicyBuilder = new StrictMode.VmPolicy.Builder().detectAll().penaltyLog();

            /*
             * if (Utils.hasHoneycomb()) {
             * threadPolicyBuilder.penaltyFlashScreen(); vmPolicyBuilder
             * .setClassInstanceLimit(ImageGridActivity.class, 1)
             * .setClassInstanceLimit(ImageDetailActivity.class, 1); }
             */
            StrictMode.setThreadPolicy(threadPolicyBuilder.build());
            StrictMode.setVmPolicy(vmPolicyBuilder.build());
        }
    }

    public static boolean hasFroyo() {
        // Can use static final constants like FROYO, declared in later versions
        // of the OS since they are inlined at compile time. This is guaranteed
        // behavior.
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO;
    }

    public static boolean hasGingerbread() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD;
    }

    public static boolean hasHoneycomb() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB;
    }

    public static boolean hasHoneycombMR1() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR1;
    }

    public static boolean hasJellyBean() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN;
    }
}
