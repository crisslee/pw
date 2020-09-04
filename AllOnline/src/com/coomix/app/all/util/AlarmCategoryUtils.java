package com.coomix.app.all.util;

import android.content.Context;
import android.content.SharedPreferences;
import com.coomix.app.all.AllOnlineApp;
import com.coomix.app.all.R;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Created by herry on 2017/1/12.
 */
public class AlarmCategoryUtils {
    /*报警类型===>报警图标*/
    private static Map<Integer, Integer> mIconMap;

    private static Map<Integer, Integer> mItemIconMap;

    /*报警类型 ====> 存储KEY*/
    private static Map<Integer, String> mPrefKey;

    private static final int ALARM_DEF_ICON_ID = R.drawable.alarm_type_other;
    private static final int ALARM_ITEM_DEF_ICON_ID = R.drawable.alarm_type_item_other;

    public static final String ALARM_TYPE_LIST = "alarm_type_list";
    public static final String ALARM_NOTIFY = "alarm_notify";
    public static final String ALARM_NOTIFY_INGORED = "alarm_notify_ingored";
    private final static String ALARM_KEY_HEADER = "alarm_" + AllOnlineApp.sAccount + "_";

    static {
        mIconMap = new HashMap<Integer, Integer>();
        fillupMap(mIconMap, false);
        mItemIconMap = new HashMap<Integer, Integer>();
        fillupMap(mItemIconMap, true);
        fillupPrefKey();
    }

    private static void fillupMap(Map<Integer, Integer> map, boolean isItem) {
        if (isItem) {
            /*震动*/
            mItemIconMap.put(1, R.drawable.alarm_type_item_vibrate);
            /*断电*/
            mItemIconMap.put(2, R.drawable.alarm_type_item_poweroff);
            /*低电*/
            mItemIconMap.put(3, R.drawable.alarm_type_item_low_battery);
            /*SOS求救*/
            mItemIconMap.put(4, R.drawable.alarm_type_item_sos);
            /*超速*/
            mItemIconMap.put(5, R.drawable.alarm_type_item_speed_exceed);
            /*离线报警*/
            mItemIconMap.put(6, R.drawable.alarm_type_item_offline);
            /*出围栏*/
            mItemIconMap.put(8, R.drawable.alarm_type_item_fencing_exceed);
            /*位移报警*/
            mItemIconMap.put(9, R.drawable.alarm_type_item_shift);
            /*进围栏*/
            mItemIconMap.put(24, R.drawable.alarm_type_item_fencing_enter);
            /*出区域*/
            mItemIconMap.put(31, R.drawable.alarm_type_item_district_exceed);
            /*拆机*/
            mItemIconMap.put(32, R.drawable.alarm_type_item_dismantle);
            /*光感报警*/
            mItemIconMap.put(33, R.drawable.alarm_type_item_light_resistor);
            /*磁感*/
            mItemIconMap.put(34, R.drawable.alarm_type_item_induction);
            /*蓝牙报警*/
            mItemIconMap.put(36, R.drawable.alarm_type_item_bluetooth);
            /*进二押点告警*/
            mItemIconMap.put(40, R.drawable.alarm_type_item_risk_area_enter);
            /*出二押点告警*/
            mItemIconMap.put(41, R.drawable.alarm_type_item_risk_area_leave);
            /*二押点久留告警*/
            mItemIconMap.put(42, R.drawable.alarm_type_item_risk_area_long_stay);
            //碰撞报警
            mItemIconMap.put(44, R.drawable.alarm_type_item_crash);
            //急加速报警
            mItemIconMap.put(45, R.drawable.alarm_type_item_speedup);
            //急减速报警
            mItemIconMap.put(46, R.drawable.alarm_type_item_speeddown);
            //翻转告警
            mItemIconMap.put(47, R.drawable.alarm_type_item_turnover);
            //急转弯告警
            mItemIconMap.put(48, R.drawable.alarm_type_item_sharpturn);
            //拆动报警
            mItemIconMap.put(49, R.drawable.alarm_type_item_pulldown);
        } else {
            /*震动*/
            mIconMap.put(1, R.drawable.alarm_type_vibrate);
            /*断电*/
            mIconMap.put(2, R.drawable.alarm_type_poweroff);
            /*低电*/
            mIconMap.put(3, R.drawable.alarm_type_low_battery);
            /*SOS求救*/
            mIconMap.put(4, R.drawable.alarm_type_sos);
            /*超速*/
            mIconMap.put(5, R.drawable.alarm_type_speed_exceed);
            /*离线报警*/
            mIconMap.put(6, R.drawable.alarm_type_offline);
            /*出围栏*/
            mIconMap.put(8, R.drawable.alarm_type_fencing_exceed);
            /*位移报警*/
            mIconMap.put(9, R.drawable.alarm_type_shift);
            /*进围栏*/
            mIconMap.put(24, R.drawable.alarm_type_fencing_enter);
            /*出区域*/
            mIconMap.put(31, R.drawable.alarm_type_district_exceed);
            /*拆机*/
            mIconMap.put(32, R.drawable.alarm_type_dismantle);
            /*光感报警*/
            mIconMap.put(33, R.drawable.alarm_type_light_resistor);
            /*磁感*/
            mIconMap.put(34, R.drawable.alarm_type_induction);
            /*蓝牙报警*/
            mIconMap.put(36, R.drawable.alarm_type_bluetooth);
            /*进二押点告警*/
            mIconMap.put(40, R.drawable.alarm_type_risk_area_enter);
            /*出二押点告警*/
            mIconMap.put(41, R.drawable.alarm_type_risk_area_leave);
            /*二押点久留告警*/
            mIconMap.put(42, R.drawable.alarm_type_risk_area_long_stay);
            //碰撞报警
            mIconMap.put(44, R.drawable.alarm_type_crash);
            //急加速报警
            mIconMap.put(45, R.drawable.alarm_type_speedup);
            //急减速报警
            mIconMap.put(46, R.drawable.alarm_type_speeddown);
            //翻转告警
            mIconMap.put(47, R.drawable.alarm_type_turnover);
            //急转弯告警
            mIconMap.put(48, R.drawable.alarm_type_sharpturn);
            //拆动报警
            mIconMap.put(49, R.drawable.alarm_type_pulldown);
        }
    }

    private static void fillupPrefKey() {
        mPrefKey = new HashMap<Integer, String>();
        /*震动*/
        mPrefKey.put(1, "alarm_category_vibrate");
        /*断电*/
        mPrefKey.put(2, "alarm_category_poweroff");
        /*低电*/
        mPrefKey.put(3, "alarm_category_low_battery");
        /*SOS求救*/
        mPrefKey.put(4, "alarm_category_sos");
        /*超速*/
        mPrefKey.put(5, "alarm_category_speed_exceed");
        /* 离线报警*/
        mPrefKey.put(6, "alarm_category_offline");
        /*出围栏*/
        mPrefKey.put(8, "alarm_category_fencing_exceed");
        /*位移报警*/
        mPrefKey.put(9, "alarm_category_shift");
        /*进围栏*/
        mPrefKey.put(24, "alarm_category_fencing_enter");
        /*出区域*/
        mPrefKey.put(31, "alarm_category_district_exceed");
        /*拆机*/
        mPrefKey.put(32, "alarm_category_dismantle");
        /*光感报警*/
        mPrefKey.put(33, "alarm_category_light_resistor");
        /*磁感*/
        mPrefKey.put(34, "alarm_category_induction");
        /*蓝牙报警*/
        mPrefKey.put(36, "alarm_category_bluetooth");
        /*进二押点告警*/
        mPrefKey.put(40, "alarm_category_risk_area_enter");
        /*出二押点告警*/
        mPrefKey.put(41, "alarm_category_risk_area_leave");
        /*二押点久留告警*/
        mPrefKey.put(42, "alarm_category_risk_area_long_stay");
        //碰撞报警
        mPrefKey.put(44, "alarm_category_crash");
        //急加速报警
        mPrefKey.put(45, "alarm_category_speedup");
        //急减速报警
        mPrefKey.put(46, "alarm_category_speeddown");
        //翻转告警
        mPrefKey.put(47, "alarm_category_turnover");
        //急转弯告警
        mPrefKey.put(48, "alarm_category_sharpturn");
        //拆动报警
        mPrefKey.put(49, "alarm_category_pulldown");
        /*其他*/
        mPrefKey.put(-1, "alarm_category_other");
    }

    public static Set<Integer> getMapKeys() {
        return mIconMap.keySet();
    }

    public static int getAlarmDrawable(int alarmType) {
        Integer resId = mIconMap.get(alarmType);
        if (resId == null) {
            return ALARM_DEF_ICON_ID;
        }
        return resId;
    }

    public static int getAlarmItemDrawable(int alarmType) {
        Integer resId = mItemIconMap.get(alarmType);
        if (resId == null) {
            return ALARM_ITEM_DEF_ICON_ID;
        }
        return resId;
    }

    public static String getAlarmPrefKey(int alarmId) {
        return mPrefKey.get(alarmId);
    }

    public static String formatCount(int count) {
        if (count < 1000) {
            return String.valueOf(count);
        }
        return "999+";
    }

    /**
     * 客户端只提供了12种报警类型的图标，不在此范围内的统一使用其他类型保持
     */
    public static boolean isNormalCategory(int alarmTypeId) {
        if (mIconMap.containsKey(alarmTypeId)) {
            return true;
        }
        return false;
    }

    private static final String DATE_FORMAT = "MM-dd";
    private static final String HOUR_FORMAT = "HH:mm";

    public static String formatAlarmDate(long timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);
        return sdf.format(timestamp * 1000);
    }

    public static String formatAlarmHour(long timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat(HOUR_FORMAT);
        return sdf.format(timestamp * 1000);
    }

    public static int getAllAlarmLocalCount(Context context) {
        int count = 0;
        SharedPreferences sharedPrefs;
        try {
            sharedPrefs = context.getSharedPreferences(AlarmCategoryUtils.ALARM_TYPE_LIST, Context.MODE_PRIVATE);
            count = sharedPrefs.getInt(ALARM_KEY_HEADER + "_all", 0);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return count;
    }

    public static int setAllAlarmLocalCount(Context context, int count) {
        SharedPreferences sharedPrefs;
        try {
            sharedPrefs = context.getSharedPreferences(ALARM_TYPE_LIST, Context.MODE_PRIVATE);
            sharedPrefs.edit().putInt(ALARM_KEY_HEADER + "_all", count).commit();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return count;
    }

    public static int getAlarmLocalCountByType(Context context, int typeId) {
        int count = 0;
        SharedPreferences sharedPrefs;
        try {
            sharedPrefs = context.getSharedPreferences(ALARM_TYPE_LIST, Context.MODE_PRIVATE);
            count = sharedPrefs.getInt(ALARM_KEY_HEADER + typeId, 0);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return count;
    }

    public static void setAlarmLocalCountByType(Context context, int typeId, int iCount) {
        int count = 0;
        SharedPreferences sharedPrefs;
        try {
            sharedPrefs = context.getSharedPreferences(ALARM_TYPE_LIST, Context.MODE_PRIVATE);
            sharedPrefs.edit().putInt(ALARM_KEY_HEADER + typeId, iCount).commit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
