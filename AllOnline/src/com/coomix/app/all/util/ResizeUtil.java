package com.coomix.app.all.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import com.coomix.app.all.AllOnlineApp;
import com.coomix.app.all.R;

/**
 * @author HZJ
 */
public class ResizeUtil {

    private static final String TAG = "ResizeUtil";

    private static final String DEVICE_PREF = "devicePref";
    private static final String HAD_CALCULATE_DEVICE_PARAMS = "hadCalculateDeviceParams", SCREEN_WIDTH = "screenWidth",
        SCREEN_HEIGHT = "screenHeight", DENSITY = "density", RESIZE = "resize", RESIZE_FACTOR = "resizeFactor";

    private static int screenWidth, screenHeight;
    // public static int appScreenHeight;
    private static float density;
    private static float resize;

    private static float resizeFactor;

    public static float getDensity() {
        if (density <= 0) {
            density = getValue(DENSITY, 1);
        }
        return density;
    }

    public static float getResize() {
        float defaultVaule = 1f;
        if (resize <= 0) {
            defaultVaule = NewUIParam.TOPIC_IMAGE_GRID;
            resize = getValue(RESIZE, defaultVaule);
        }
        return defaultVaule;
    }

    public static float getResizeFactor() {
        if (resizeFactor <= 0) {
            SharedPreferences pref = AllOnlineApp.mApp.getSharedPreferences(DEVICE_PREF, Context.MODE_PRIVATE);
            if (!pref.contains(RESIZE_FACTOR)) {
                resizeFactor = getDensity() * getResize();
                pref.edit().putFloat(RESIZE_FACTOR, resizeFactor).commit();
            } else {
                resizeFactor = pref.getFloat(RESIZE_FACTOR, 1);
            }
        }
        return resizeFactor;
    }

    private static float getValue(String key, float defaultVaule) {
        SharedPreferences pref = AllOnlineApp.mApp.getSharedPreferences(DEVICE_PREF, Context.MODE_PRIVATE);
        return pref.getFloat(key, defaultVaule);
    }

    public static void initDeviceParams(Context context) {
        SharedPreferences pref = context.getSharedPreferences(DEVICE_PREF, Context.MODE_PRIVATE);
        boolean hadDeviceParams = pref.getBoolean(HAD_CALCULATE_DEVICE_PARAMS, false);
        if (hadDeviceParams) {
            screenWidth = pref.getInt(SCREEN_WIDTH, -1);
            screenHeight = pref.getInt(SCREEN_HEIGHT, -1);
            density = pref.getFloat(DENSITY, 1);
            resize = pref.getFloat(RESIZE, 1);
            resizeFactor = pref.getFloat(RESIZE_FACTOR, 0);
        } else {
            DisplayMetrics dm = context.getResources().getDisplayMetrics();
            screenWidth = dm.widthPixels;
            screenHeight = dm.heightPixels;
            density = dm.density;
            resize = calculateResize(context);
            resizeFactor = resize * density;
            boolean success = pref.edit().putInt(SCREEN_WIDTH, screenWidth).putInt(SCREEN_HEIGHT, screenHeight)
                .putFloat(DENSITY, density).putFloat(RESIZE, resize).putFloat(RESIZE_FACTOR, resizeFactor).commit();
            if (success) {
                pref.edit().putBoolean(HAD_CALCULATE_DEVICE_PARAMS, true).commit();
            }
        }
    }

    /**
     * 使用华为P6-T100(4.7英寸)手机作为参照设备，此设备分辨率为720 * 1184 参照设备相关参数：density=2.0,
     * width=720, height=1184, scaledDensity=2.0, xdpi=315.31, ydpi=315.65
     * 40sp大小的textView1，在P6设备上的大小为80
     *
     * @return resize
     * @author Barry
     */
    private static float calculateResize(Context context) {
        float resize = 1;
        try {
            View view = LayoutInflater.from(context).inflate(R.layout.resize_layout, null);
            TextView resizeView = (TextView) view.findViewById(R.id.resize_view);
            float oldSize = resizeView.getTextSize(); // oldSize是textView1在当前设备上,适配前的大小
            float percent = (float) 80 / 720; // 80是textView1在参照设备的大小,720是参照设备的屏幕宽度大小
            float newSize = (float) (percent * screenWidth); // newSize是textView1在当前设备上，适配后的大小
            resize = (float) newSize / oldSize;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return resize;
    }

    public static void setTextSize(TextView textView, float textSize) {
        textView.setTextSize(textSize * ResizeUtil.resize);
    }

}
