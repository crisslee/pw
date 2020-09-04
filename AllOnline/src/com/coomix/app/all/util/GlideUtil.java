package com.coomix.app.all.util;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.TextUtils;
import com.bumptech.GlideApp;
import com.bumptech.glide.load.Key;
import com.bumptech.glide.util.Util;
import com.coomix.app.all.AllOnlineApp;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class GlideUtil {

    public static File getGlideCacheFile(String pictureUrl) {
        String filePath = getGlideCachePath(pictureUrl);
        if (filePath != null) {
            File file = new File(filePath);
            if (file.exists() && file.isFile()) {
                return file;
            }
        }
        return null;
    }

    public static String getGlideCachePath(String pictureUrl) {
        if (pictureUrl == null) {
            return null;
        }
        String safeKey = null;
        synchronized (GlideUtil.class) {
            try {
                MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
                messageDigest.update(pictureUrl.getBytes(Key.STRING_CHARSET_NAME));
                safeKey = Util.sha256BytesToHex(messageDigest.digest());
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            }
        }

        return GlideApp.getPhotoCacheDir(AllOnlineApp.mApp.getApplicationContext()) + File.separator + safeKey + ".0";
    }

    public static Bitmap getGlideCacheBitmap(String pictureUrl) {
        String filePath = getGlideCachePath(pictureUrl);
        if (!TextUtils.isEmpty(filePath)) {
            return BitmapFactory.decodeFile(filePath);
        }
        return null;
    }

    /**
     * 根据图片的url路径获得Bitmap对象
     */
    public static Bitmap returnBitmap(String url) {
        URL fileUrl = null;
        Bitmap bitmap = null;

        try {
            fileUrl = new URL(url);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        try {
            HttpURLConnection conn = (HttpURLConnection) fileUrl
                .openConnection();
            conn.setDoInput(true);
            conn.connect();
            InputStream is = conn.getInputStream();
            bitmap = BitmapFactory.decodeStream(is);
            is.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bitmap;
    }

    public static Bitmap getBitmap(String url) {
        Bitmap bm = null;
        try {
            URL iconUrl = new URL(url);
            URLConnection conn = iconUrl.openConnection();
            HttpURLConnection http = (HttpURLConnection) conn;

            int length = http.getContentLength();

            conn.connect();
            // 获得图像的字符流
            InputStream is = conn.getInputStream();
            BufferedInputStream bis = new BufferedInputStream(is, length);
            bm = BitmapFactory.decodeStream(bis);
            bis.close();
            is.close();// 关闭流
        } catch (Exception e) {
            e.printStackTrace();
        }
        return bm;
    }
}
