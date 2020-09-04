package com.coomix.app.framework.util;

import android.app.Activity;
import android.app.AppOpsManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Build.VERSION;
import android.os.Environment;
import android.provider.Settings;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.TextView;
import com.amap.api.maps.offlinemap.OfflineMapStatus;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Projection;
import com.baidu.mapapi.model.LatLng;
import com.coomix.app.all.AllOnlineApp;
import com.coomix.app.all.Constant;
import com.coomix.app.all.R;
import com.coomix.app.all.model.bean.OfflineCityItem;
import com.coomix.app.all.ui.login.LoginActivity;
import com.coomix.app.all.util.CommunityDateUtil;
import com.coomix.app.all.util.CommunityUtil;
import com.coomix.app.all.manager.SettingDataManager;
import com.coomix.app.all.util.StringUtil;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;

public class CommonUtil {

    public static int count = 0;
    public static int errorCount = 0;

    public static boolean isEmptyTrimStringOrNull(String str) {
        if (str == null || str.trim().length() == 0 || "null".equalsIgnoreCase(str)) {
            return true;
        }
        return false;
    }

    public static String getTicket() {
        return AllOnlineApp.sToken.ticket;
    }

    public static boolean isEmptyUrl(String str) {
        boolean flag = false;
        if (!StringUtil.isTrimEmpty(str)) {
            if (str.equals("http://") || str.equals("https://")) {
                flag = true;
            }
        }
        return flag;
    }

    private static final String Content_Type_PNG = "image/png";
    private static final String Content_Type_JPG = "image/jpeg";
    private static final String Content_Type_GIF = "image/gif";

    /**
     * ��ȡͼƬ��mime����
     *
     * @param fileName
     * @return
     * @throws IllegalArgumentException �����ͼƬ
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

    public static SpannableString getHtmlText(String friendRemark, String userName) {
        SpannableString spanText = new SpannableString(friendRemark + "(" + userName + ")");
        spanText.setSpan(new ForegroundColorSpan(Color.GRAY), friendRemark.length(),
            friendRemark.length() + userName.length() + 2, Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
        return spanText;
    }

    /**
     * ��ݽ�ѹ��
     *
     * @param gzBytes
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
    public static Point getScreentDimention(Context ctx) {
        final DisplayMetrics dm = ctx.getResources().getDisplayMetrics();
        Point size = new Point();
        size.x = dm.widthPixels;
        size.y = dm.heightPixels;
        return size;
    }

    /**
     * Convert Dip to pixel on screen
     *
     * @param context the context
     * @param dip     the dip number to be converted
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

    private static int isOnService(String begin_time, String end_time) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
            Date dateStart = sdf.parse(begin_time);
            Date dateEnd = sdf.parse(end_time);
            Calendar start = Calendar.getInstance();
            Calendar end = Calendar.getInstance();
            Calendar current = Calendar.getInstance();
            start.setTime(dateStart);
            end.setTime(dateEnd);

            int startH = start.get(Calendar.HOUR_OF_DAY);
            int startM = start.get(Calendar.MINUTE);

            long startTimestamp = startH * 3600 + startM * 60;

            int endH = end.get(Calendar.HOUR_OF_DAY);
            int endM = end.get(Calendar.MINUTE);
            long endTimestamp = endH * 3600 + endM * 60 + 600;

            // ������ʱ��С�ڿ�ʼʱ�䣬���һ��
            // ���� 8:00 -- 0:00
            if (endTimestamp < startTimestamp) {
                endTimestamp += 24 * 3600;
            }

            int currentH = current.get(Calendar.HOUR_OF_DAY);
            int currentM = current.get(Calendar.MINUTE);
            long currentTimestamp = currentH * 3600 + currentM * 60;

            if (currentTimestamp >= startTimestamp && currentTimestamp <= endTimestamp) {
                return 0;
            } else if (currentTimestamp < startTimestamp) {
                return -1;
            } else {
                return 1;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    public static int isOnService(String serviceTime) {
        if (!TextUtils.isEmpty(serviceTime)) {
            String[] timeArray = serviceTime.split(",");
            for (String str : timeArray) {
                String[] serviceTimeArray = str.split("-");
                if (serviceTimeArray.length >= 2) {
                    return isOnService(serviceTimeArray[0], serviceTimeArray[1]);
                }
            }
        }
        return 0;
    }

    public static boolean isNetworkConnected(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        if (networkInfo == null || !networkInfo.isConnected()) {
            return true;
        }
        return false;
    }

    public static synchronized void saveLog(String log) {
        File baseDir = Environment.getExternalStorageDirectory();
        FileWriter writer;
        try {
            File dir = new File(baseDir.getAbsolutePath() + "/Coomix");
            if (!dir.exists()) {
                dir.mkdirs(); // create folders where write files
            }

            File file = new File(dir, "Request_Log.txt");
            writer = new FileWriter(file, true);
            if (count == 0) {
                String time = getTimeString();
                writer.append("\n" + time + "\n");
            }
            writer.append(log + "\n");
            writer.flush();
            writer.close();
            count++;
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
        }
    }

    public static synchronized void saveErrorLog(String log) {
        File baseDir = Environment.getExternalStorageDirectory();
        FileWriter writer;
        try {
            File dir = new File(baseDir.getAbsolutePath() + "/Coomix");
            if (!dir.exists()) {
                dir.mkdirs(); // create folders where write files
            }

            File file = new File(dir, "Error_Log.txt");
            writer = new FileWriter(file, true);
            if (errorCount == 0) {
                String time = getTimeString();
                writer.append("\n" + time + "\n");
            }
            writer.append(log + "\n\n");
            writer.flush();
            writer.close();
            errorCount++;
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
        }
    }

    public static String getTimeString() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        java.util.Date dt = new Date();
        return sdf.format(dt);
    }

    public static String getTimeString(long timeMillis) {
        Date dt = new Date(timeMillis);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//yyyy/MM/dd
        return sdf.format(dt);
    }

    public static String getDateString(long timeMillis) {
        Date dt = new Date(timeMillis);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        return sdf.format(dt);
    }

    /**
     * check if the input is numeric
     *
     * @param str
     * @return
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
        if (size <= 0)
            return "0";
        final String[] units = new String[]{"B", "KB", "MB", "GB", "TB"};
        int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
        return new DecimalFormat("#,##0.#").format(size / Math.pow(1024, digitGroups)) + units[digitGroups];
    }

    public static String getDir(Context context, int course) {
        if ((course >= 338 && course <= 360) || (course >= 0 && course < 23)) {
            return context.getString(R.string.north);
        } else if (course >= 23 && course < 68) {
            return context.getString(R.string.east_north);
        } else if (course >= 68 && course < 113) {
            return context.getString(R.string.east);
        } else if (course >= 113 && course < 158) {
            return context.getString(R.string.east_south);
        } else if (course >= 158 && course < 203) {
            return context.getString(R.string.south);
        } else if (course >= 203 && course < 248) {
            return context.getString(R.string.west_south);
        } else if (course >= 248 && course < 293) {
            return context.getString(R.string.west);
        } else {
            return context.getString(R.string.west_north);
        }
    }

    public static String getStatus_amap(Context context, int status) {
        switch (status) {
            case 0: // 正在下载
                return context.getString(R.string.status_downloading);
            case OfflineMapStatus.SUCCESS:// 完成
                return context.getString(R.string.status_downloaded);
            case OfflineMapStatus.PAUSE:// 暂停
                return context.getString(R.string.status_pause);
            case OfflineMapStatus.WAITING:// 等待中
                return context.getString(R.string.status_wait_for_download);
            default:
                return "";
        }
    }

    public static String getStatus(Context context, int status) {
        switch (status) {
            case OfflineCityItem.DOWNLOADING:

                return context.getString(R.string.status_downloading);
            case OfflineCityItem.FINISHED:

                return context.getString(R.string.status_downloaded);
            case OfflineCityItem.SUSPENDED:

                return context.getString(R.string.status_pause);
            case OfflineCityItem.WAITING:

                return context.getString(R.string.status_wait_for_download);
            default:
                return "";
        }
    }

    public static String formatRangeSize(Context context,int meter) {
        String ret = "";
        if (meter < 1000) {
            ret = String.format(context.getResources().getString(R.string.mileage_metre), meter);
            //ret = String.format("%d米", meter);
        } else {
            ret = String.format(context.getResources().getString(R.string.mileage_kilometre), meter / 1000f);
            //ret = String.format("%.1f公里", meter / 1000f);
        }
        return ret;
    }

    public static String getTimeNumStr(Context context,long sec) {
        String ret = "";
        if (sec < 60) {
            //ret = String.format(context.getResources().getString(R.string.time_seconds), sec);
            ret = String.format("%d秒", sec);
        } else if (sec < 60 * 60) {
            //ret = String.format(context.getResources().getString(R.string.time_minutes), sec / 60);
            ret = String.format("%d分", sec / 60);
        }  else {
            //ret = String.format(context.getResources().getString(R.string.time_hour), sec / (60f * 60f));
            ret = String.format("%.1f时", sec / (60f * 60f));
        }
        return ret;
    }

    public static void clearCollection(Collection<?> collection) {
        if (collection != null) {
            collection.clear();
        }
    }

    public static boolean isSeeable(MapView mapView, LatLng geoPoint) {
        if (geoPoint == null || mapView == null || mapView.getWidth() == 0 || mapView.getHeight() == 0) {
            return false;
        }
        Projection mProjection = mapView.getMap().getProjection();
        Point point = mProjection.toScreenLocation(geoPoint);

        if (point.x >= mapView.getWidth() || point.x <= 0 || point.y <= 0 || point.y >= mapView.getHeight()) {
            return false;
        }
        return true;
    }

    // 根据传入的宽高 给出最后的图
    public static Bitmap zoomImage(Bitmap bgimage, double newWidth, double newHeight) {
        // 获取这个图片的宽和高
        float width = bgimage.getWidth();
        float height = bgimage.getHeight();
        // 创建操作图片用的matrix对象
        Matrix matrix = new Matrix();
        // 计算宽高缩放率
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;
        // 缩放图片动作
        matrix.postScale(scaleWidth, scaleHeight);
        Bitmap bitmap = Bitmap.createBitmap(bgimage, 0, 0, (int) width, (int) height, matrix, true);
        return bitmap;
    }

    // 合并图片
    public static Bitmap combineBitmap(Bitmap background, Bitmap foreground) {
        if (background == null) {
            return null;
        }
        int bgWidth = background.getWidth();
        int bgHeight = background.getHeight();
        int fgWidth = foreground.getWidth();
        int fgHeight = foreground.getHeight();
        Bitmap newmap = Bitmap.createBitmap(bgWidth, bgHeight, Config.ARGB_8888);
        Canvas canvas = new Canvas(newmap);
        canvas.drawBitmap(background, 0, 0, null); // 画前面的图

        canvas.drawBitmap(foreground, (bgWidth - fgWidth) / 2, (bgHeight - fgHeight) / 2 - 14, null);

        canvas.save(Canvas.ALL_SAVE_FLAG);
        canvas.restore();

        return newmap;
    }

    public static boolean isCN() {
//        String language = Locale.getDefault().getLanguage();
//        String countryCode = Locale.getDefault().getCountry();
        String lang = SettingDataManager.language;
        if (lang != null && lang.equals("zh-CN")) {// 中文简体
            return true;
        } else {
            return false;
        }

    }

    public static String getDeviceInfo(Context context) {
        try {
            org.json.JSONObject json = new org.json.JSONObject();
            android.telephony.TelephonyManager tm = (android.telephony.TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);

            String device_id = tm.getDeviceId();

            android.net.wifi.WifiManager wifi = (android.net.wifi.WifiManager) context.getSystemService(Context.WIFI_SERVICE);

            String mac = wifi.getConnectionInfo().getMacAddress();
            json.put("mac", mac);

            if (TextUtils.isEmpty(device_id)) {
                device_id = mac;
            }

            if (TextUtils.isEmpty(device_id)) {
                device_id = android.provider.Settings.Secure.getString(context.getContentResolver(), android.provider.Settings.Secure.ANDROID_ID);
            }

            json.put("device_id", device_id);

            return json.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 过滤emoji 或者 其他非文字类型的字符
     *
     * @param source
     * @return
     */
    public static String filterEmoji(String source) {
        if (!containsEmoji(source)) {
            return source;//如果不包含，直接返回
        }
        //到这里铁定包含
        StringBuilder buf = null;
        int len = source.length();
        for (int i = 0; i < len; i++) {
            char codePoint = source.charAt(i);
            if (isEmojiCharacter(codePoint)) {
                if (buf == null) {
                    buf = new StringBuilder(source.length());
                }
                buf.append(codePoint);
            } else {
            }
        }

        if (buf == null) {
            return source;//如果没有找到 emoji表情，则返回源字符串
        } else {
            if (buf.length() == len) {//这里的意义在于尽可能少的toString，因为会重新生成字符串
                buf = null;
                return source;
            } else {
                return buf.toString();
            }
        }
    }

    /**
     * 检测是否有emoji表情
     *
     * @param source
     * @return
     */
    public static boolean containsEmoji(String source) {
        int len = source.length();
        for (int i = 0; i < len; i++) {
            char codePoint = source.charAt(i);
            if (!isEmojiCharacter(codePoint)) { //如果不能匹配,则该字符是Emoji表情
                return true;
            }
        }
        return false;
    }

    /**
     * 判断是否是Emoji
     *
     * @param codePoint 比较的单个字符
     * @return
     */
    private static boolean isEmojiCharacter(char codePoint) {
        return (codePoint == 0x0) || (codePoint == 0x9) || (codePoint == 0xA) ||
                (codePoint == 0xD) || ((codePoint >= 0x20) && (codePoint <= 0xD7FF)) ||
                ((codePoint >= 0xE000) && (codePoint <= 0xFFFD)) || ((codePoint >= 0x10000)
                && (codePoint <= 0x10FFFF));
    }

    public static String getStackTrace(Throwable aThrowable) {
        final Writer result = new StringWriter();
        final PrintWriter printWriter = new PrintWriter(result);
        aThrowable.printStackTrace(printWriter);
        return result.toString();
    }

    public static boolean isSetEqual(Set set1, Set set2) {
        if (set1 == null && set2 == null) {
            return true; // Both are null
        }

        if (set1 == null || set2 == null || set1.size() != set2.size()
                || set1.size() == 0 || set2.size() == 0) {
            return false;
        }

        Iterator ite1 = set1.iterator();
        Iterator ite2 = set2.iterator();
        boolean isFullEqual = true;
        while (ite2.hasNext()) {
            if (!set1.contains(ite2.next())) {
                isFullEqual = false;
            }
        }
        return isFullEqual;
    }

    public static boolean isStringEqual(String s1, String s2) {
        if (StringUtil.isTrimEmpty(s1) && StringUtil.isTrimEmpty(s2)) {
            return true;
        } else if (!StringUtil.isTrimEmpty(s1) && !StringUtil.isTrimEmpty(s2)) {
            if (s1.equals(s2)) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    //获取系统语言，zh-CN,zh-HK,zh-TW,en
    public static String initLang() {
        String defaultLang = "en";
        String lang = Locale.getDefault().getLanguage();
        String countryCode = Locale.getDefault().getCountry();
        if (lang != null && lang.equals(Locale.CHINESE.toString()) || lang.equals(Locale.CHINA.toString())) {
            if (countryCode != null && countryCode.equals("CN")) {
                defaultLang = "zh-CN";
            } else if (countryCode != null && countryCode.equals("TW")) {
                defaultLang = "zh-TW";
            } else if (countryCode != null && countryCode.equals("HK") || countryCode.equals("MO")) {
                defaultLang = "zh-HK";
            } else {
                defaultLang = "zh-CN";
            }
        } else if (lang.equals("en")) {
            defaultLang = "en";
        }
        return defaultLang;
    }

    public static Locale initLangExtend() {
        String lang = Locale.getDefault().getLanguage();
        String countryCode = Locale.getDefault().getCountry();
        if (lang != null && lang.equals(Locale.CHINESE.toString()) || lang.equals(Locale.CHINA.toString())) {
            if (countryCode != null && countryCode.equals("CN")) {
                return Locale.CHINA;
            } else if (countryCode != null && countryCode.equals("TW")) {
                return Locale.TAIWAN;
            } else if (countryCode != null && countryCode.equals("HK") || countryCode.equals("MO")) {
                return Locale.TAIWAN;
            } else {
                return Locale.CHINA;
            }
        } else if (lang.equals("en")) {
            return Locale.ENGLISH;
        }
        return Locale.ENGLISH;
    }

    //获取系统语言的索引，0-3
    public static int initLangNew()
    {
        int defaultLangValue = 3;
        String lang = Locale.getDefault().getLanguage();
        String countryCode = Locale.getDefault().getCountry();

        if (lang != null && lang.equals(Locale.CHINESE.toString()) || lang.equals(Locale.CHINA.toString()))
        {
            if (countryCode != null && countryCode.equals("CN"))
            {
                defaultLangValue = 0;
            }
            else if (countryCode != null && countryCode.equals("TW"))
            {
                defaultLangValue = 1;
            }
            else if (countryCode != null && countryCode.equals("HK") || countryCode.equals("MO"))
            {
                defaultLangValue = 2;
            }
            else
            {
                defaultLangValue = 0;
            }
        }
        else if (lang.equals("en"))
        {
            defaultLangValue = 3;
        }
        return defaultLangValue;
    }

    /**
     * 判断是否在大陆，用于地址解析,
     * 规则暂定为app语言为主
     * <p>
     * 如果app语言与系统语言都是中文简体Locale.CHINA，选择中文解析；
     * 否则，选择外文解析；
     *
     * @return true表示中文简体
     */
    public static boolean isChina() {
        String lang = Locale.getDefault().getLanguage();
        String county = Locale.getDefault().getCountry();
        if (!StringUtil.isTrimEmpty(lang) && lang.equals(Locale.CHINESE.toString()) || lang.equals(Locale.CHINA.toString())) {
            if (!StringUtil.isTrimEmpty(county) && county.equals("CN")) {
                return true;
            }
        }
        if (Locale.getDefault().toString().contains(Locale.CHINA.toString())) {
            return true;
        }
        return false;
    }

    public static String getLangCodeBySetting(int slang) {
        String lang = "en";
        switch (slang) {
            case 0:
                lang = "zh-CN";
                break;
            case 1:
                lang = "zh-TW";
                break;
            case 2:
                lang = "zh-HK";
                break;
            case 3:
                lang = "en";
                break;
        }
        return lang;
    }

    public static String getGender(Context context, int genderIndex) {
        String[] genders = context.getResources().getStringArray(R.array.gender_list);
        if (genderIndex < 0 || genderIndex >= genders.length) {
            genderIndex = 0;
        }
        return genders[genderIndex];
    }

    public static String getPhone(Context context, String phone) {
        if (CommunityUtil.isEmptyTrimStringOrNull(phone)) {
            return context.getResources().getString(R.string.bindphone_unbind);
        }
        phone = phone.trim();
        if(phone.length() != 11) {
            return phone;
        }
        return phone.substring(0, 3) + "****" + phone.substring(7);
    }

    public static Activity getActivity(View view) {
        Activity activity = null;
        if (view.getContext().getClass().getName().contains("com.android.internal.policy.DecorContext")) {
            try {
                Field field = view.getContext().getClass().getDeclaredField("mPhoneWindow");
                field.setAccessible(true);
                Object obj = field.get(view.getContext());
                java.lang.reflect.Method m1 = obj.getClass().getMethod("getContext");
                activity = (Activity) (m1.invoke(obj));
            } catch (Exception e) {
                e.printStackTrace();
                activity = (Activity) view.getContext();
            }
        } else {
            activity = (Activity) view.getContext();
        }
        return activity;
    }

    public static boolean isShowDailyRedPacket()
    {
        long iOpenTime = PreferenceUtil.getLong(Constant.PREFERENCE_DAILY_RP_OPENED_TIME, 0L);
        if (CommunityDateUtil.isSameDay(iOpenTime, System.currentTimeMillis()))
        {
            //和当前时间是同一天，表示已经领取过
            return false;
        }

        return true;
    }
    public static boolean isMatched(String matchs, String content)
    {
        if (TextUtils.isEmpty(content))
        {
            return false;
        }
        if (TextUtils.isEmpty(matchs))
        {
            return true;
        }
        Pattern p = Pattern.compile(matchs);
        Matcher m = p.matcher(content);
        return m.matches();
    }

    private final static int PLATFORM_SUPPORT_WX = 1 << 0; // 支持微信支付
    private final static int PLATFORM_SUPPORT_ALI = 1 << 1; // 支持支付宝支付
    private final static int PLATFORM_SUPPORT_GOOME = 1 << 2; // 支付零钱支付
    public static boolean isSupportWechatPay()
    {
        int support = AllOnlineApp.getAppConfig().getPay_platform_support();
        if((support & PLATFORM_SUPPORT_WX) != 0)
        {
            return true;
        }
        return false;
    }
    public static boolean isSupportAliPay()
    {
        int support = AllOnlineApp.getAppConfig().getPay_platform_support();
        if((support & PLATFORM_SUPPORT_ALI) != 0)
        {
            return true;
        }
        return false;
    }

    public static boolean isSupportGoomePay()
    {
        int support = AllOnlineApp.getAppConfig().getPay_platform_support();
        if((support & PLATFORM_SUPPORT_GOOME) != 0)
        {
            return true;
        }
        return false;
    }

    public static boolean checkAndGoToLogin(Context context) {
        if (isLogin()) {
            return true;
        } else {
            goToLogin(context);
            return false;
        }
    }

    public static boolean checkAndGoToLogin(Context context, boolean fromDailyRp) {
        if (isLogin()) {
            return true;
        } else {
            goToLogin(context, fromDailyRp);
            return false;
        }
    }

    public static boolean isLogin() {
        return (AllOnlineApp.sToken != null && !TextUtils.isEmpty(AllOnlineApp.sToken.access_token));
    }

    public static void goToLogin(Context context) {
        context.startActivity(new Intent(context, LoginActivity.class));
    }

    public static void goToLogin(Context context, boolean fromDailyRp) {
        Intent toLogin = new Intent(context, LoginActivity.class);
        if (fromDailyRp) {
            toLogin.putExtra(LoginActivity.FROM_WHERE, LoginActivity.FROM_DAILY_REDPACKET);
        }
        context.startActivity(toLogin);
    }

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

    public static void setTextDrawable(Context context, TextView textView, int drawableId, int pos)
    {
        setTextDrawable(context, textView, drawableId, "", pos, false);
    }

    public static void setTextDrawable(Context context, TextView textView, int drawableId, int strId, int pos)
    {
        if (strId <= 0)
        {
            return;
        }
        setTextDrawable(context, textView, drawableId, context.getString(strId), pos, true);
    }

    public static void setTextDrawable(Context context, TextView textView, int drawableId, String text, int pos)
    {
        setTextDrawable(context, textView, drawableId, text, pos, true);
    }

    private static void setTextDrawable(Context context, TextView textView, int drawableId, String text, int pos,
                                        boolean hasText)
    {
        if (textView == null)
        {
            return;
        }
        if (hasText)
        {
            textView.setText(text);
        }
        Drawable drawable = null;
        if (drawableId > 0)
        {
            drawable = context.getResources().getDrawable(drawableId);
            drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
        }
        switch (pos)
        {
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

    public static boolean isLetterDigitDecline(String content)
    {
        if (TextUtils.isEmpty(content))
        {
            return false;
        }
        Pattern p = Pattern.compile("[a-zA-Z0-9_]");
        Matcher m = p.matcher(content);
        return m.matches();
    }

    /**
     * 判断GPS是否开启，GPS或者AGPS开启一个就认为是开启的
     *
     * @param context
     * @return true 表示开启
     */
    public static final boolean isGPSOpened(final Context context)
    {
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        // 通过GPS卫星定位，定位级别可以精确到街（通过24颗卫星定位，在室外和空旷的地方定位准确、速度快）
        boolean gps = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        // 通过WLAN或移动网络(3G/2G)确定的位置（也称作AGPS，辅助GPS定位。主要用于在室内或遮盖物（建筑群或茂密的深林等）密集的地方定位）
        boolean network = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        if (gps || network)
        {
            return true;
        }
        return false;
    }
    /**
     * 项目中用到日程提醒功能，如果应用的通知栏没有打开，则需要提示用户前去打开通知栏,判断通知栏是否打开
     * */
    public static boolean isSystemNotificationEnabled(Context context) {

        String CHECK_OP_NO_THROW = "checkOpNoThrow";
        String OP_POST_NOTIFICATION = "OP_POST_NOTIFICATION";


        /* Context.APP_OPS_MANAGER */
        try {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
                return true;
            }
            AppOpsManager mAppOps = (AppOpsManager) context.getSystemService(Context.APP_OPS_SERVICE);
            ApplicationInfo appInfo = context.getApplicationInfo();
            String pkg = context.getApplicationContext().getPackageName();
            int uid = appInfo.uid;

            Class appOpsClass = null;
            appOpsClass = Class.forName(AppOpsManager.class.getName());
            Method checkOpNoThrowMethod = appOpsClass.getMethod(CHECK_OP_NO_THROW, Integer.TYPE, Integer.TYPE,
                    String.class);
            Field opPostNotificationValue = appOpsClass.getDeclaredField(OP_POST_NOTIFICATION);

            int value = (Integer) opPostNotificationValue.get(Integer.class);
            return ((Integer) checkOpNoThrowMethod.invoke(mAppOps, value, uid, pkg) == AppOpsManager.MODE_ALLOWED);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    public static void goToSet(Context context){
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.BASE) {
//            // 进入设置系统应用权限界面
//            Intent intent = new Intent(Settings.ACTION_SETTINGS);
//            context.startActivity(intent);
//            return;
//        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {// 运行系统在5.x环境使用
//            // 进入设置系统应用权限界面
//            Intent intent = new Intent(Settings.ACTION_SETTINGS);
//            context.startActivity(intent);
//            return;
//        }
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Intent intent = new Intent();
            intent.setAction("android.settings.APP_NOTIFICATION_SETTINGS");
            intent.putExtra("app_package", context.getPackageName());
            intent.putExtra("app_uid", context.getApplicationInfo().uid);
            context.startActivity(intent);
        } else if (android.os.Build.VERSION.SDK_INT == Build.VERSION_CODES.KITKAT) {
            Intent intent = new Intent();
            intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            intent.addCategory(Intent.CATEGORY_DEFAULT);
            intent.setData(Uri.parse("package:" + context.getPackageName()));
            context.startActivity(intent);
        }
    }

    public static boolean isTelNumber(String data)
    {
        if(TextUtils.isEmpty(data))
        {
            return false;
        }
        String MOBILE = "^1[3456789]\\d{9}";
        String PHS = "^0(10|2[0-5789]|\\d{3})\\d{7,8}$";

        Pattern p = Pattern.compile(MOBILE);
        Matcher m = p.matcher(data);

        Pattern p1 = Pattern.compile(PHS);
        Matcher m1 = p1.matcher(data);

        return (m.matches() || m1.matches());
    }

    public static boolean isCoupletCard(String data)
    {
        //物联卡13到20位数字
        if(TextUtils.isEmpty(data))
        {
            return false;
        }
        String MOBILE = "^\\d{13,20}$";
        Pattern p = Pattern.compile(MOBILE);
        Matcher m = p.matcher(data);

        return m.matches();
    }

    public static boolean isApkInstalled(Context context, String packageName) {
        if (TextUtils.isEmpty(packageName)) {
            return false;
        }

        try {
            ApplicationInfo info = context.getPackageManager().getApplicationInfo(packageName,
                PackageManager.GET_UNINSTALLED_PACKAGES);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 将火星坐标转变成百度坐标
     * @ 火星坐标（高德、腾讯地图坐标等）
     * @return 百度坐标
     */

    public static double[] huoxingToBaidu(double lantitude, double longitude)
    {
        double x_pi = 3.14159265358979324 * 3000.0 / 180.0;
        double x = longitude, y = lantitude;
        double z = Math.sqrt(x * x + y * y) + 0.00002 * Math.sin(y * x_pi);
        double theta = Math.atan2(y, x) + 0.000003 * Math.cos(x *  x_pi);
        double[] data = new double[2];
        data[0] = dataDigit(6,z * Math.sin(theta) + 0.006);
        data[1] = dataDigit(6,z * Math.cos(theta) + 0.0065);
        return data;
    }
    /**
     * 将百度坐标转变成火星坐标
     * @  百度坐标（百度地图坐标）
     * @return 火星坐标(高德、腾讯地图等)
     */
    public static double[] baiduToHuoxing(double lantitude, double longitude)
    {
        double x_pi = 3.14159265358979324 * 3000.0 / 180.0;
        double x = longitude - 0.0065, y = lantitude - 0.006;
        double z = Math.sqrt(x * x + y * y) - 0.00002 * Math.sin(y * x_pi);
        double theta = Math.atan2(y, x) - 0.000003 * Math.cos(x * x_pi);
        double[] data = new double[2];
        data[0] = dataDigit(6,z * Math.sin(theta));
        data[1] = dataDigit(6, z * Math.cos(theta));
        return data;
    }

    static double dataDigit(int digit, double in)
    {
        return new BigDecimal(in).setScale(6, BigDecimal.ROUND_HALF_UP).doubleValue();
    }

    /**
     * URLEncoder.encode(param, "UTF-8").replace("+", "%20")
     */
    public static String encodeTicket(String param) {
        if (param != null) {
            try {
                param = URLEncoder.encode(param, "UTF-8").replace("+", "%20");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }

        return param;
    }

}
