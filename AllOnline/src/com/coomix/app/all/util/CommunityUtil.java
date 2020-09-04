package com.coomix.app.all.util;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Build.VERSION;
import android.os.Handler;
import android.os.Looper;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.ForegroundColorSpan;
import android.util.Base64;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;
import com.amap.api.maps.model.LatLng;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.core.PoiItem;
import com.amap.api.services.geocoder.GeocodeResult;
import com.amap.api.services.geocoder.GeocodeSearch;
import com.amap.api.services.geocoder.RegeocodeQuery;
import com.amap.api.services.geocoder.RegeocodeResult;
import com.coomix.app.all.AllOnlineApp;
import com.coomix.app.all.Constant;
import com.coomix.app.all.R;
import com.coomix.app.all.log.GoomeLog;
import com.coomix.app.all.model.bean.Adver;
import com.coomix.app.all.model.bean.PriceRule;
import com.coomix.app.all.model.response.CommunityUser;
import com.coomix.app.all.model.response.RespBindWechat;
import com.coomix.app.all.ui.activity.CommActDetailActivity;
import com.coomix.app.all.ui.activity.CommActListActivity;
import com.coomix.app.all.ui.web.BusAdverActivity;
import com.coomix.app.all.ui.web.DisclaimerActivity;
import com.coomix.app.framework.util.PreferenceUtil;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.io.Serializable;
import java.io.StringWriter;
import java.io.Writer;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;

/**
 * 社区相关工具类
 *
 * @author think
 */
public class CommunityUtil {
    private final static String TAG = CommunityUtil.class.getSimpleName();

    public static int count = 0;

    /**
     * TextView中显示图片的位置：左边
     */
    public static final int DRAWABLE_LEFT = 0;
    /**
     * TextView中显示图片的位置：上方
     */
    public static final int DRAWABLE_TOP = 1;
    /**
     * TextView中显示图片的位置：右侧
     */
    public static final int DRAWABLE_RIGHT = 2;
    /**
     * TextView中显示图片的位置：下方
     */
    public static final int DRAWABLE_BOTTOM = 3;
    /**
     * TextView中不显示图片
     */
    public static final int DRAWABLE_NONE = 4;

    /**
     * 上班不迟到最大可显示的点赞数量，大于这个用10k+显示
     */
    private final static int MAX_SPUR_LIMIT = 9999;
    /**
     * 上班不迟到最大可显示的排名数量，大于这个用10w+显示
     */
    private final static int MAX_RANK_LIMIT = 99999;

    private static String versionNameAll = null;

    public static boolean isSameUser(CommunityUser user1, CommunityUser user2) {
        if (user1 != null && user2 != null) {
            return user1.getUid().equals(user2.getUid());
        } else {
            return false;
        }
    }

    public static boolean isSameUser(CommunityUser user1, RespBindWechat user2) {
        try {
            return user1.getUid().equals(user2.getData().getUserinfo().getUid());
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean isMe(CommunityUser user) {
        if (user != null && (user.getUid().equals(String.valueOf(AllOnlineApp.sToken.community_id)))) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * 点击首页的广告后跳转，成功返回true
     *
     * @param context context
     * @param adver ad
     * @return boolean
     */
    public static boolean onAdvertisementClick(Context context, Adver adver, boolean isStartedFromBoot) {
        if (adver == null || TextUtils.isEmpty(adver.jumpurl)) {
            return false;
        }
        try {
            String jumpUrl = adver.jumpurl.trim();
            // 去首尾空格，转小写，并将中文"："和全角"："替换为英文":"
            String url = jumpUrl.toLowerCase()
                .replace("：", ":")
                .replace("：", ":")
                .replace("　", "")
                .replace(" ", "");
            if (url.startsWith(Constant.KEY_HTTP) || url.startsWith(Constant.KEY_HTTPS)) {
                if (url.contains("m.tb.cn")) {
                    CommunityUtil.switch2TaobaoActivity(context, url);
                } else {
                    Intent mIntent = new Intent(context, BusAdverActivity.class);
                    mIntent.putExtra(BusAdverActivity.INTENT_ADVER, adver);
                    if (isStartedFromBoot) {
                        mIntent.putExtra(BusAdverActivity.INTENT_FROM, BusAdverActivity.INTENT_FROM_WELCOME);
                    } else {
                        mIntent.putExtra(BusAdverActivity.INTENT_FROM, BusAdverActivity.INTENT_FROM_SQUARE);
                    }
                    context.startActivity(mIntent);
                }
            } else if (url.startsWith(Constant.KEY_ACTIVITY_DETAIL)) {
                String id = url.substring(Constant.KEY_ACTIVITY_DETAIL.length());
                int actId = Integer.parseInt(id);
                switch2CommActDetailActivity(context, actId);
            } else if (url.startsWith(Constant.KEY_ACTIVITY_CATEGORY)) {
                //跳转到活动列表对应的分类
                String catId = url.substring(Constant.KEY_ACTIVITY_CATEGORY.length());
                if (!TextUtils.isEmpty(catId) && isNumeric(catId.trim())) {
                    switch2CommActListActivity(context, false, Integer.parseInt(catId.trim()));
                } else {
                    switch2CommActListActivity(context, false, -1);
                }
            } else if (url.startsWith(Constant.KEY_ACTIVITY)) {
                switch2CommActListActivity(context, false, -1);
            } else if (url.startsWith(Constant.KEY_TAOBAO)) {
                switch2TaobaoActivity(context, url);
            }
            PreferenceUtil.commitBoolean(Constant.IS_STARTED_FROM_BOOT, isStartedFromBoot);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean isNumeric(String str) {
        Pattern pattern = Pattern.compile("[0-9]*");
        Matcher isNum = pattern.matcher(str);
        return isNum.matches();
    }

    public static void switchActivity(Context context, Class<?> activityClass) {
        context.startActivity(new Intent(context, activityClass));
    }

    public static void switchActivity(Context context, Intent intent) {
        context.startActivity(intent);
    }

    public static void showMsg(int resId) {
        showMsg(getString(resId));
    }

    public static void showMsg(final String msg) {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(AllOnlineApp.mApp, msg, Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * @param str string
     * @return double
     */
    public static double parse2Double(String str) {
        double result = Double.NEGATIVE_INFINITY;
        try {
            result = Double.valueOf(str);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * @param str string
     * @return long
     */
    public static long parse2Long(String str) {
        long result = Long.MIN_VALUE;
        try {
            result = Long.valueOf(str);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * @param str string
     * @return int
     */
    public static int parse2Integer(String str) {
        int result = Integer.MIN_VALUE;
        try {
            result = Integer.valueOf(str);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    public static int getColor(int resId) {
        try {
            return AllOnlineApp.mApp.getResources().getColor(resId);
        } catch (Exception e) {
            return Color.BLACK;
        }
    }

    public static String getString(int resId) {
        try {
            return AllOnlineApp.mApp.getResources().getString(resId);
        } catch (Exception e) {
            return "";
        }
    }

    public static String getString(int resId, Object... formatArgs) {
        try {
            return AllOnlineApp.mApp.getString(resId, formatArgs);
        } catch (Exception e) {
            return "";
        }
    }

    /**
     * 获取app的名字
     */
    public static String getAppName(Context context) {
        try {
            PackageManager packageManager = context.getPackageManager();
            String appname = (String) packageManager.getApplicationLabel(context.getApplicationInfo());
            return appname;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 获取build.gradle中完整的versionName
     *
     * @param context context
     * @return build.gradle中定义的完整versionName
     */
    public static String getAppVersionNameAll(Context context) {
        if (CommunityUtil.isEmptyTrimString(versionNameAll)) {
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
                Log.e("VersionInfo", "Exception", e);
            }
            versionNameAll = versionName;
        }
        return versionNameAll;
    }

    private static String sVersionName = null;

    /**
     * 返回当前程序版本名(含V,测试版可能含DEBUG,DEV) 如 V3.1.6，V3.1.6DEV
     */
    public static String getAppVersionNameNoDate(Context context) {
        if (CommunityUtil.isEmptyTrimString(sVersionName)) {
            String versionName = null;
            try {
                // ---get the package info---
                PackageManager pm = context.getPackageManager();
                PackageInfo pi = pm.getPackageInfo(context.getPackageName(), 0);
                versionName = pi.versionName;
                if (versionName == null || versionName.length() <= 0) {
                    versionName = "";
                } else {
                    if (versionName.contains("(")) {
                        versionName = versionName.substring(0, versionName.indexOf("("));
                    }
                }
            } catch (Exception e) {
                Log.e("VersionInfo", "Exception", e);
            }
            if (versionName != null) {
                sVersionName = versionName;
            }
        }
        return sVersionName;
    }

    private static String versionNameNoV = null;

    /**
     * 获取仅含数字和小数点的版本名(如3.1.6)
     *
     * @param context context
     * @return 仅含数字和小数点的versionName
     */
    public static String getAppVersionNameNoV(Context context) {
        if (CommunityUtil.isEmptyString(versionNameNoV)) {
            try {
                versionNameNoV = getAppVersionNameNoDate(context)
                    .replace("IS_DEBUG_MODE", "")
                    .replace("DEV", "")
                    .replace("V", "")
                    .replace("_A", "")
                    .replace("_B", "")
                    .replace("_C", "")
                    .replace("_D", "")
                    .replace("_E", "");
                if (versionNameNoV != null && versionNameNoV.length() > 5) {
                    versionNameNoV = versionNameNoV.substring(0, 5);
                }
            } catch (Exception e) {
                versionNameNoV = "";
            }
        }
        if (versionNameNoV == null) {
            versionNameNoV = "";
        }
        return versionNameNoV;
    }

    public static boolean isEmptyString(String str) {
        if (str == null || str.length() == 0) {
            return true;
        }
        return false;
    }

    public static boolean isEmptyTrimString(String str) {
        if (str == null || str.trim().length() == 0) {
            return true;
        }
        return false;
    }

    public static boolean isEmptyTrimStringOrNull(String str) {
        if (str == null || str.trim().length() == 0 || "null".equalsIgnoreCase(str)) {
            return true;
        }
        return false;
    }

    private static final String Content_Type_PNG = "image/png";
    private static final String Content_Type_JPG = "image/jpeg";
    private static final String Content_Type_GIF = "image/gif";

    /**
     * 获取图片的mime类型
     *
     * @param fileName filename
     * @return type
     * @throws IllegalArgumentException 如果不是图片
     */
    public static String getFileMimeType(String fileName) {
        if (fileName.toLowerCase().endsWith(".jpg") || fileName.toLowerCase().endsWith(".jpeg")) {
            return Content_Type_JPG;
        }
        if (fileName.toLowerCase().endsWith(".png")) {
            return Content_Type_PNG;
        }
        if (fileName.toLowerCase().endsWith(".gif")) {
            return Content_Type_GIF;
        }
        throw new IllegalArgumentException("not a image file");
    }

    public static String encrypt(String str, String key) {
        try {
            DESKeySpec keySpec = new DESKeySpec(key.getBytes("UTF-8"));

            SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
            SecretKey secretKey = keyFactory.generateSecret(keySpec);

            byte[] cleartext = str.getBytes("UTF-8");

            Cipher cipher = Cipher.getInstance("DES"); // cipher is not thread
            // safe
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            return Base64.encodeToString(cipher.doFinal(cleartext), Base64.DEFAULT);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String decrypt(String str, String key) {
        try {
            DESKeySpec keySpec = new DESKeySpec(key.getBytes("UTF-8"));

            SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
            SecretKey secretKey = keyFactory.generateSecret(keySpec);
            Cipher cipher = Cipher.getInstance("DES");// cipher is not thread
            // safe
            cipher.init(Cipher.DECRYPT_MODE, secretKey);

            byte[] plainTextPwdBytes = cipher.doFinal(Base64.decode(str, Base64.DEFAULT));
            return new String(plainTextPwdBytes, "UTF-8");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static synchronized void saveSerializableObject(Serializable obj, Context context, String fileNmae) {
        try {
            FileOutputStream fos = context.openFileOutput(fileNmae, Context.MODE_PRIVATE);
            ObjectOutputStream os = new ObjectOutputStream(fos);
            os.writeObject(obj);
            os.flush();
            os.close();
            fos.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static synchronized Object getSerializableObject(Context context, String fileName) {
        Object o = null;
        try {
            File file = context.getFileStreamPath(fileName);
            if (!file.exists()) {
                return null;
            }
            ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file));
            o = ois.readObject();
            ois.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return o;
    }

    public static String getFileSize(final long size) {

        if (size > 1073741824) {
            return String.format("%.2f", size / 1073741824.0) + " GB";
        } else if (size > 1048576) {
            return String.format("%.2f", size / 1048576.0) + " MB";
        } else if (size > 1024) {
            return String.format("%.2f", size / 1024.0) + " KB";
        } else {
            return size + " B";
        }
    }

    public static SpannableString getHtmlText(String friendRemark, String userName) {
        SpannableString spanText = new SpannableString(friendRemark + "(" + userName + ")");
        spanText.setSpan(new ForegroundColorSpan(Color.GRAY), friendRemark.length(),
            friendRemark.length() + userName.length() + 2, Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
        return spanText;
    }

    /**
     * 数据解压缩
     *
     * @param gzBytes bytes
     * @return byte[]
     */
    public static byte[] byteDecompress(byte[] gzBytes) {
        byte[] data = null;
        try {
            ByteArrayInputStream bais = new ByteArrayInputStream(gzBytes);
            GZIPInputStream gis = new GZIPInputStream(bais);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();

            int len = -1;
            byte[] buffer = new byte[1024];
            while ((len = gis.read(buffer, 0, buffer.length)) != -1) {
                baos.write(buffer, 0, len);
            }
            gis.close();

            data = baos.toByteArray();
            baos.flush();
            baos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return data;
    }

    public static boolean isInstalledOnSdCard(Context context) {

        // check for API level 8 and higher
        if (VERSION.SDK_INT > android.os.Build.VERSION_CODES.ECLAIR_MR1) {
            PackageManager pm = context.getPackageManager();
            try {
                PackageInfo pi = pm.getPackageInfo(context.getPackageName(), 0);
                ApplicationInfo ai = pi.applicationInfo;
                return (ai.flags & ApplicationInfo.FLAG_EXTERNAL_STORAGE) == ApplicationInfo.FLAG_EXTERNAL_STORAGE;
            } catch (NameNotFoundException e) {
                // ignore
            }
        }

        // check for API level 7 - check files dir
        try {
            String filesDir = context.getFilesDir().getAbsolutePath();
            if (filesDir.startsWith("/data/")) {
                return false;
            } else if (filesDir.contains("/mnt/") || filesDir.contains("/sdcard/")) {
                return true;
            }
        } catch (Throwable e) {
            // ignore
        }

        return false;
    }

    /**
     * gets the screen width and height base on the context
     *
     * @param ctx context of activity
     * @return point contains width and height
     */
    @SuppressWarnings("deprecation")
    public static Point getScreentDimention(Activity ctx) {
        WindowManager wm = (WindowManager) ctx.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point size = new Point();
        size.x = display.getWidth();
        size.y = display.getHeight();
        String mess = "width=" + size.x + ", height=" + size.y;

        return size;
    }

    /**
     * Convert Dip to pixel on screen
     *
     * @param context the context
     * @param dip the dip number to be converted
     * @return pixel number converted
     */
    public static int convertDIP2PX(Context context, int dip) {
        try {
            float scale = context.getResources().getDisplayMetrics().density;
            return (int) (dip * scale + 0.5f * (dip >= 0 ? 1 : -1));
        } catch (Exception e) {
            e.printStackTrace();
            return dip;
        }
    }

    public static String getVerticalText(String text) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < text.length(); i++) {
            sb.append(text.charAt(i)).append("\n");
        }
        return sb.toString();
    }

    /**
     * check if the input is numeric
     *
     * @param str str
     * @return boolean
     */
    public static boolean isInteger(String str) {
        int size = str.length();

        for (int i = 0; i < size; i++) {
            if (!Character.isDigit(str.charAt(i))) {
                return false;
            }
        }

        return size > 0;
    }

    /**
     * check if String is alphanumeric without punctuation
     *
     * @param s String input
     * @return true if s matches the pattern
     */
    public static boolean isAlphanumeric(String s) {
        Pattern p = Pattern.compile("[a-zA-Z0-9]+");
        Matcher m = p.matcher(s);
        return m.matches();
    }

    public static String readableFileSize(long size) {
        if (size <= 0) {
            return "0";
        }
        final String[] units = new String[] { "B", "KB", "MB", "GB", "TB" };
        int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
        return new DecimalFormat("#,##0.#").format(size / Math.pow(1024, digitGroups)) + units[digitGroups];
    }

    public static synchronized boolean deleteDataObject(Context context, String fileName) {
        try {
            File file = context.getFileStreamPath(fileName);
            if (file.exists()) {
                return file.delete();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public static synchronized void saveDataObject(Context context, String fileName, Object object) {
        if (context == null) {
            context = AllOnlineApp.mApp;
        }
        try {
            File file = context.getFileStreamPath(fileName);
            if (file.exists()) {
                file.delete();
            }
            FileOutputStream fos = context.openFileOutput(fileName, Context.MODE_PRIVATE);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(object);
            oos.flush();
            oos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static synchronized Object readDataObject(Context context, String fileName) {
        if (TextUtils.isEmpty(fileName) || context == null) {
            return null;
        }
        Object object = null;
        try {
            File file = context.getFileStreamPath(fileName);
            if (file.exists()) {
                ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file));
                object = ois.readObject();
                ois.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return object;
    }

    /**
     * 将assets中的文件拷贝到dirPath中（如果存在，比较md5）
     *
     * @param context context
     * @param fileName file name
     * @param dirPath 保存的路径（不带文件名），默认context.getFileStreamPath(fileName);
     * @return boolean
     */
    public static synchronized boolean copyAssetsFile(Context context, String fileName, String... dirPath) {
        boolean result = false;
        try {
            File file;
            if (dirPath != null && dirPath.length > 0 && CommunityUtil.isEmptyTrimStringOrNull(dirPath[0])) {
                file = new File(dirPath[0]);
            } else {
                file = context.getFileStreamPath(fileName);
            }
            try {
                if (file.exists()) {
                    String md5 = Md5Util.fileMD5(file.getPath());
                    InputStream is = context.getAssets().open(fileName);
                    String newMd5 = Md5Util.fileMD5(is);
                    if (md5 != null && md5.equals(newMd5)) {
                        // md5相同，则不需要复制
                        result = true;
                    }
                }
            } catch (Exception e) {
                // md5生成/比较出错，重新复制
                e.printStackTrace();
            }
            if (!result) { // 不存在或者md5不同，则拷贝文件
                FileOutputStream fos = context.openFileOutput(fileName, Context.MODE_PRIVATE);
                InputStream is = context.getAssets().open(fileName);
                byte[] buffer = new byte[1024];
                int length = is.read(buffer);
                while (length > 0) {
                    fos.write(buffer, 0, length);
                    length = is.read(buffer);
                }
                result = true;
                fos.flush();
                is.close();
                fos.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    public static boolean isBackground(Context context) {
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        if (activityManager == null) {
            return false;
        }
        List<RunningAppProcessInfo> appProcesses = activityManager.getRunningAppProcesses();
        if (appProcesses == null) {
            return false;
        }
        for (RunningAppProcessInfo appProcess : appProcesses) {
            if (appProcess != null && appProcess.processName.equals(context.getPackageName())) {
                Log.i(context.getPackageName(), "此appimportace =" + appProcess.importance
                    + ",context.getClass().getName()=" + context.getClass().getName());
                if (appProcess.importance != RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                    Log.i(context.getPackageName(), "处于后台" + appProcess.processName);
                    return true;
                } else {
                    Log.i(context.getPackageName(), "处于前台" + appProcess.processName);
                    return false;
                }
            }
        }
        return false;
    }

    public static String getStackTrace(Throwable aThrowable) {
        final Writer result = new StringWriter();
        final PrintWriter printWriter = new PrintWriter(result);
        aThrowable.printStackTrace(printWriter);
        return result.toString();
    }

    public static String getResizePicUrl(String srcPicUrl, int width, int height) {
        String result = srcPicUrl;
        try {
            int index = srcPicUrl.lastIndexOf(".");
            String picStr1 = srcPicUrl.substring(0, index);
            String picStr2 = srcPicUrl.substring(index);
            result = picStr1 + "_" + width + "x" + height + picStr2;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * double类型转为string java中double整数位超过7位时， 转string会使用科学计数法
     *
     * @param src double
     * @param num num
     * @return string
     */
    public static String doubleToString(double src, int num) {
        StringBuilder sb = new StringBuilder("#0");
        if (num > 0) {
            sb.append(".");
            for (int i = 0; i < num; i++) {
                sb.append("0");
            }
        }
        BigDecimal bigD = new BigDecimal(src);
        DecimalFormat format = new DecimalFormat(sb.toString());
        String result = format.format(bigD);
        return result;
    }

    public static boolean isMIUIRom() {
        String property = getSystemProperty("ro.miui.ui.version.name");
        return !isEmptyTrimString(property);
    }

    public static String getSystemProperty(String propName) {
        String line;
        BufferedReader input = null;
        try {
            Process p = Runtime.getRuntime().exec("getprop " + propName);
            input = new BufferedReader(new InputStreamReader(p.getInputStream()), 1024);
            line = input.readLine();
            input.close();
        } catch (Exception e) {
            GoomeLog.getInstance().logW(TAG, "getSystemProperty Exception " + e.getMessage());
            return null;
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return line;
    }

    public static String addSpaceAtCharacter(String source, CharSequence cs) {
        if (TextUtils.isEmpty(source)) {
            return source;
        }
        if (source.contains(cs)) {
            if (source.contains(" ")) {
                source.replaceAll(" ", "");
            }
            StringBuilder builder = new StringBuilder(source);
            builder.insert(builder.indexOf(String.valueOf(cs)), " ");
            builder.insert(builder.indexOf(String.valueOf(cs)) + 1, " ");
            return builder.toString();
        }
        return source;
    }

    //切换到活动列表界面
    public static void switch2CommActListActivity(Context context, boolean isFromBoot, int categoryId) {
        Intent intent = new Intent(context, CommActListActivity.class);
        intent.putExtra(Constant.IS_STARTED_FROM_BOOT, isFromBoot);
        intent.putExtra(CommActListActivity.CATEGORY_ID, categoryId);

        context.startActivity(intent);
    }

    //切换到活动详情界面
    public static void switch2CommActDetailActivity(Context context, int id) {
        Intent intent3 = new Intent(context, CommActDetailActivity.class);
        intent3.putExtra(Constant.NATIVE_ACTIVITY_ID, id);
        context.startActivity(intent3);
    }

    //切换到淘宝商城
    public static void switch2TaobaoActivity(Context context, String url) {
        if (checkPackage(context, "com.taobao.taobao")) {
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_VIEW);
            Uri uri = Uri.parse(url);
            intent.setData(uri);
            intent.setClassName("com.taobao.taobao", "com.taobao.tao.detail.activity.DetailActivity");
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        } else {
            Uri uri = Uri.parse(url);
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            context.startActivity(intent);
        }
    }

    public static boolean checkPackage(Context context, String packageName) {
        if (packageName == null || "".equals(packageName)) {
            return false;
        }
        try {
            context.getPackageManager().getApplicationInfo(packageName, PackageManager.GET_UNINSTALLED_PACKAGES);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    public static boolean isMobileNO(String mobiles) {
        if (TextUtils.isEmpty(mobiles)) {
            return false;
        }
        Pattern p = Pattern.compile("^1[3|4|5|7|8][0-9]\\d{8}$");
        Matcher m = p.matcher(mobiles);
        return m.matches();
    }

    public static boolean isQQOrWX(String qqorwx) {
        if (TextUtils.isEmpty(qqorwx)) {
            return false;
        }
        // QQ号最短5位，微信号最短是6位最长20位
        Pattern p = Pattern.compile("^[a-zA-Z0-9_-]{5,19}$");
        Matcher m = p.matcher(qqorwx);
        return m.matches();
    }

    public static boolean isName(String name) {
        if (TextUtils.isEmpty(name)) {
            return false;
        }
        Pattern p = Pattern.compile("^[\u4E00-\u9FA5a-zA-Z\\s]+");
        Matcher m = p.matcher(name);
        return m.matches();
    }

    public static boolean isLengthOk(String source, final int max) {
        if (source == null || TextUtils.isEmpty(source.trim()) || source.length() > max) {
            return false;
        }

        int lengthCount = 0;
        for (int i = 0; i < source.length(); i++) {
            char charAt = source.charAt(i);
            // 判断是否是中文
            if ((charAt >= 0x4e00) && (charAt <= 0x9fbb)) {
                lengthCount += 2;
            } else {
                lengthCount += 1;
            }
            if (lengthCount > max) {
                return false;
            }
        }

        return lengthCount <= max;
    }

    public static void setTextDrawable(Context context, TextView textView, int drawableId, int pos) {
        setTextDrawable(context, textView, drawableId, "", pos, false);
    }

    public static void setTextDrawable(Context context, TextView textView, int drawableId, int strId, int pos) {
        if (strId <= 0) {
            return;
        }
        setTextDrawable(context, textView, drawableId, context.getString(strId), pos, true);
    }

    public static void setTextDrawable(Context context, TextView textView, int drawableId, String text, int pos) {
        setTextDrawable(context, textView, drawableId, text, pos, true);
    }

    private static void setTextDrawable(Context context, TextView textView, int drawableId, String text, int pos,
        boolean hasText) {
        if (textView == null) {
            return;
        }
        if (hasText) {
            textView.setText(text);
        }
        Drawable drawable = null;
        if (drawableId > 0) {
            drawable = context.getResources().getDrawable(drawableId);
            drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
        }
        switch (pos) {
            case DRAWABLE_LEFT:
                textView.setCompoundDrawables(drawable, null, null, null);
                break;

            case DRAWABLE_TOP:
                textView.setCompoundDrawables(null, drawable, null, null);
                break;

            case DRAWABLE_RIGHT:
                textView.setCompoundDrawables(null, null, drawable, null);
                break;

            case DRAWABLE_BOTTOM:
                textView.setCompoundDrawables(null, null, null, drawable);
                break;

            case DRAWABLE_NONE:
                textView.setCompoundDrawables(null, null, null, null);
                break;

            default:
                break;
        }
    }

    public static void setTextDrawable(Context context, TextView textView, int drawableId, SpannableStringBuilder text,
        int pos) {
        setTextDrawable(context, textView, drawableId, text, pos, true);
    }

    private static void setTextDrawable(Context context, TextView textView, int drawableId, SpannableStringBuilder text,
        int pos, boolean hasText) {
        if (textView == null) {
            return;
        }
        if (hasText) {
            textView.setMovementMethod(LinkMovementMethod.getInstance());
            textView.setText(text);
        }
        Drawable drawable = null;
        if (drawableId > 0) {
            drawable = context.getResources().getDrawable(drawableId);
            drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
        }
        switch (pos) {
            case DRAWABLE_LEFT:
                textView.setCompoundDrawables(drawable, null, null, null);
                break;

            case DRAWABLE_TOP:
                textView.setCompoundDrawables(null, drawable, null, null);
                break;

            case DRAWABLE_RIGHT:
                textView.setCompoundDrawables(null, null, drawable, null);
                break;

            case DRAWABLE_BOTTOM:
                textView.setCompoundDrawables(null, null, null, drawable);
                break;

            case DRAWABLE_NONE:
                textView.setCompoundDrawables(null, null, null, null);
                break;

            default:
                break;
        }
    }

    public static String getSpurStr(long spur) {
        if (spur > MAX_SPUR_LIMIT) {
            return "10k+";
        } else {
            return String.valueOf(spur);
        }
    }

    public static String getRankStr(int rank) {
        if (rank > MAX_RANK_LIMIT) {
            return "10w+";
        } else {
            return String.valueOf(rank);
        }
    }

    /**
     * 该设备android版本是否是指定版本及之后的版本
     */
    public static boolean isLaterThanVersion24() {
        return Build.VERSION.SDK_INT >= 24;
    }

    /**
     * 活动报名中获取价格，value是后台返回的以分为单位的整数价格，需要换算成元为单位保留digit个小数位--最多两位
     */
    public static String getDecimalStrByInt(int value, int digit) {
        if (value < 10) {
            return "0.0" + value;
        }

        String data;
        if (value < 100) {
            data = "0." + value;
        } else {
            StringBuffer temp = new StringBuffer(String.valueOf(value));
            data = temp.insert(temp.length() - 2, ".").toString();
        }

        return data.substring(0, data.length() - (2 - digit));
    }

    /**
     * value是后台返回的以分为单位的整数，需要换算成元为单位保留digit个小数位--最多两位
     */
    public static String getDecimalStrByInt(long value, int digit) {
        if (value < 10) {
            return "0.0" + value;
        }

        String data;
        if (value < 100) {
            data = "0." + value;
        } else {
            StringBuffer temp = new StringBuffer(String.valueOf(value));
            data = temp.insert(temp.length() - 2, ".").toString();
        }

        return data.substring(0, data.length() - (2 - digit));
    }

    public static String getDecimalStrByLong(long value, int digit) {
        if (value < 10L) {
            return "0.0" + value;
        }

        String data;
        if (value < 100L) {
            data = "0." + value;
        } else {
            StringBuffer temp = new StringBuffer(String.valueOf(value));
            data = temp.insert(temp.length() - 2, ".").toString();
        }
        return data.substring(0, data.length() - (2 - digit));
    }

    public static String getPackagePriceMin(Context context, ArrayList<PriceRule> listPrices) {
        int iMin = Integer.MAX_VALUE;
        for (PriceRule priceRule : listPrices) {
            if (priceRule.getPrice() < iMin) {
                iMin = priceRule.getPrice();
            }
        }
        return getMoneyStrByIntDecimal(context, iMin, 2) + context.getString(R.string.up);
    }

    public static String getMoneyStrByIntDecimal(Context cxt, int value, int digit) {
        return cxt.getResources().getString(R.string.money_unit) + getDecimalStrByInt(value, digit);
    }

    public static String getTimeHSM(Context context, int timeMillis) {
        String time = "";
        if (timeMillis < 60) {
            time = context.getString(R.string.sms_code_countdown_text, timeMillis);
        } else {
            int m = timeMillis / 60;
            int s = 0;
            if (m < 60) {
                s = timeMillis % 60;
                time = m + context.getString(R.string.unit_minute) + context.getString(R.string.sms_code_countdown_text,
                    s);
            } else {
                int h = m / 60;
                m = m % 60;
                s = timeMillis - h * 3600 - m * 60;
                time = h
                    + context.getString(R.string.unit_hour)
                    + m
                    + context.getString(R.string.unit_minute)
                    + context.getString(R.string.sms_code_countdown_text, s);
            }
        }
        return time;
    }

    public static String getTimeHM(long timeMillis) {
        Date dt = new Date(timeMillis);
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
        return sdf.format(dt);
    }

    public static void switch2WebViewActivity(Context context, String title, String url) {
        Intent mIntent = new Intent(context, DisclaimerActivity.class);
        mIntent.putExtra(DisclaimerActivity.TITLE, title);
        mIntent.putExtra(DisclaimerActivity.URL, url);
        mIntent.addFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
        context.startActivity(mIntent);
    }

    /**
     * url为空或null，则使用默认的url
     */
    public static void switch2WebViewActivity(Context context, String url, boolean bShare, String from) {
        Adver adver = new Adver();
        adver.jumpurl = url;
        Intent intent = new Intent(context, BusAdverActivity.class);
        intent.putExtra(BusAdverActivity.INTENT_ADVER, adver);
        intent.putExtra(BusAdverActivity.INTENT_FROM, from);
        context.startActivity(intent);
    }

    /**
     * 判断GPS是否开启，GPS或者AGPS开启一个就认为是开启的
     *
     * @return true 表示开启
     */
    public static final boolean isGPSOpened(final Context context) {
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        // 通过GPS卫星定位，定位级别可以精确到街（通过24颗卫星定位，在室外和空旷的地方定位准确、速度快）
        boolean gps = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        // 通过WLAN或移动网络(3G/2G)确定的位置（也称作AGPS，辅助GPS定位。主要用于在室内或遮盖物（建筑群或茂密的深林等）密集的地方定位）
        boolean network = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        if (gps || network) {
            return true;
        }
        return false;
    }

    public static boolean isRoot() {
        if (new File("system/bin/su").exists() || new File("system/xbin/su").exists()) {
            return true;
        }
        return false;
    }

    public static boolean isLetterDigitDecline(String content) {
        if (TextUtils.isEmpty(content)) {
            return false;
        }
        Pattern p = Pattern.compile("[a-zA-Z0-9_]");
        Matcher m = p.matcher(content);
        return m.matches();
    }

    public interface OnAddressCallBack {
        public void onAddressBack(PoiItem poiItem);
    }

    public static void getAddressByLatLng(Context context, LatLng latLng, final OnAddressCallBack callBack) {
        if (latLng == null) {
            return;
        }
        GeocodeSearch mGeocoderSearch = new GeocodeSearch(context);
        LatLonPoint latLonPoint = new LatLonPoint(latLng.latitude, latLng.longitude);
        RegeocodeQuery query = new RegeocodeQuery(latLonPoint, 200, GeocodeSearch.AMAP);
        mGeocoderSearch.setOnGeocodeSearchListener(new GeocodeSearch.OnGeocodeSearchListener() {
            @Override
            public void onRegeocodeSearched(RegeocodeResult result, int rCode) {
                if (rCode == 1000/*AMapException.CODE_AMAP_SUCCESS*/) {
                    if (result != null && result.getRegeocodeAddress() != null
                        && result.getRegeocodeAddress().getFormatAddress() != null) {
                        List<PoiItem> localList = result.getRegeocodeAddress().getPois();
                        if (localList != null && localList.size() > 0) {
                            PoiItem poiItem = localList.get(0);
                            if (callBack != null) {
                                callBack.onAddressBack(poiItem);
                            }
                        }
                    }
                }
            }

            @Override
            public void onGeocodeSearched(GeocodeResult geocodeResult, int i) {

            }
        });
        // 设置异步逆地理编码请求
        mGeocoderSearch.getFromLocationAsyn(query);
    }
}