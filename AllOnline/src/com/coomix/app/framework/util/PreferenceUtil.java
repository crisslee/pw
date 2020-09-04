package com.coomix.app.framework.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;
import com.coomix.app.all.ui.alarm.FollowWechatActivity;
import com.coomix.app.all.ui.alarm.FollowWechatActivityKt;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Set;

public class PreferenceUtil {
    private static SharedPreferences mSharedPreferences = null;
    private static Editor mEditor = null;

    public static void init(Context context) {
        if (null == mSharedPreferences) {
            mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        }
    }

    public static void removeKey(String key) {
        mEditor = mSharedPreferences.edit();
        mEditor.remove(key);
        mEditor.commit();
    }

    public static void removeAll() {
        mEditor = mSharedPreferences.edit();
        mEditor.clear();
        mEditor.commit();
    }

    public static void commitString(String key, String value) {
        mEditor = mSharedPreferences.edit();
        mEditor.putString(key, value);
        mEditor.commit();
    }

    public static String getString(String key, String faillValue) {
        return mSharedPreferences.getString(key, faillValue);
    }

    public static void commitInt(String key, int value) {
        mEditor = mSharedPreferences.edit();
        mEditor.putInt(key, value);
        mEditor.commit();
    }

    public static int getInt(String key, int failValue) {
        return mSharedPreferences.getInt(key, failValue);
    }

    public static void commitLong(String key, long value) {
        mEditor = mSharedPreferences.edit();
        mEditor.putLong(key, value);
        mEditor.commit();
    }

    public static long getLong(String key, long failValue) {
        return mSharedPreferences.getLong(key, failValue);
    }

    public static void commitBoolean(String key, boolean value) {
        mEditor = mSharedPreferences.edit();
        mEditor.putBoolean(key, value);
        mEditor.commit();
    }

    public static Boolean getBoolean(String key, boolean failValue) {
        return mSharedPreferences.getBoolean(key, failValue);
    }

    public static void commitSet(String key, Set<String> value) {
        mEditor = mSharedPreferences.edit();
        mEditor.putStringSet(key, value);
        mEditor.commit();
    }

    public static Set<String> getSet(String key, Set<String> faillValue) {
        return mSharedPreferences.getStringSet(key, faillValue);
    }

    public static void commitFloat(String key, float value) {
        mEditor = mSharedPreferences.edit();
        mEditor.putFloat(key, value);
        mEditor.commit();
    }

    public static float getFloat(String key, float faillValue) {
        return mSharedPreferences.getFloat(key, faillValue);
    }

    public static boolean commitObj(String key, Object object) {
        SharedPreferences share = mSharedPreferences;
        if (object == null) {
            Editor editor = share.edit().remove(key);
            return editor.commit();
        }
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = null;
        try {
            oos = new ObjectOutputStream(baos);
            oos.writeObject(object);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        // 将对象放到OutputStream中
        // 将对象转换成byte数组，并将其进行base64编码
        String objectStr = new String(Base64.encode(baos.toByteArray(),
            Base64.DEFAULT));
        try {
            baos.close();
            oos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        SharedPreferences.Editor editor = share.edit();
        // 将编码后的字符串写到base64.xml文件中
        editor.putString(key, objectStr);
        return editor.commit();
    }

    public static Object getObj(String key) {
        SharedPreferences sharePre = mSharedPreferences;
        try {
            String wordBase64 = sharePre.getString(key, "");
            // 将base64格式字符串还原成byte数组
            // 不可少，否则在下面会报java.io.StreamCorruptedException
            if (wordBase64 == null || wordBase64.equals("")) {
                return null;
            }
            byte[] objBytes = Base64.decode(wordBase64.getBytes(),
                Base64.DEFAULT);
            ByteArrayInputStream bais = new ByteArrayInputStream(objBytes);
            ObjectInputStream ois = new ObjectInputStream(bais);
            // 将byte数组转换成product对象
            Object obj = ois.readObject();
            bais.close();
            ois.close();
            return obj;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
