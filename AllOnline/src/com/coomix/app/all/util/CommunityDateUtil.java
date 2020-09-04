package com.coomix.app.all.util;
/*
 * Copyright (C) 2012 www.amsoft.cn
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import android.content.Context;
import android.text.TextUtils;
import com.coomix.app.all.AllOnlineApp;
import com.coomix.app.all.R;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

/**
 * © 2012 amsoft.cn 名称：AbDateUtil.java 描述：日期处理类.
 *
 * @author 还如一梦中
 * @version v1.0 @date：2013-01-18 下午11:52:13
 */
public class CommunityDateUtil {

    /** 时间日期格式化到年月日时分秒. */
    public static final String dateFormatYMDHMS = "yyyy-MM-dd HH:mm:ss";

    public static final String dateFormatMDHM = "MM-dd HH:mm";

    public static final String dateFormatMDHM_cn = "MM月dd日 HH:mm";
    /** 时间日期格式化到年月日时分. */
    public static final String dateFormatYMDHM_cn = "yyyy年MM月dd日 HH:mm";

    /** 时间日期格式化到年月日. */
    public static final String dateFormatYMD = "yyyy-MM-dd";

    /** 时间日期格式化到年月. */
    public static final String dateFormatYM = "yyyy-MM";

    /** 时间日期格式化到年月日时分. */
    public static final String dateFormatYMDHM = "yyyy-MM-dd HH:mm";

    /** 时间日期格式化到月日. */
    public static final String dateFormatMD = "MM/dd";
    public static final String dateFormatDMYHM = "dd/MM/yy HH:mm";

    /** 时分秒. */
    public static final String dateFormatHMS = "HH:mm:ss";

    /** 时分. */
    public static final String dateFormatHM = "HH:mm";

    /** 上午. */
    public static final String AM = "AM";

    /** 下午. */
    public static final String PM = "PM";

    public static final int SECOND_PER_HOUR = 3600;
    public static final int SECOND_PER_MINUTE = 60;

    /**
     * 描述：String类型的日期时间转化为Date类型.
     *
     * @param strDate String形式的日期时间
     * @param format 格式化字符串，如："yyyy-MM-dd HH:mm:ss"
     * @return Date Date类型日期时间
     */
    public static Date getDateByFormat(String strDate, String format) {
        SimpleDateFormat mSimpleDateFormat = new SimpleDateFormat(format);
        Date date = null;
        try {
            date = mSimpleDateFormat.parse(strDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return date;
    }

    /**
     * 描述：获取偏移之后的Date.
     *
     * @param date 日期时间
     * @param calendarField Calendar属性，对应offset的值，
     * 如(Calendar.DATE,表示+offset天,Calendar.HOUR_OF_DAY,表示＋offset小时)
     * @param offset 偏移(值大于0,表示+,值小于0,表示－)
     * @return Date 偏移之后的日期时间
     */
    public static Date getDateByOffset(Date date, int calendarField, int offset) {
        Calendar c = new GregorianCalendar();
        try {
            c.setTime(date);
            c.add(calendarField, offset);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return c.getTime();
    }

    /**
     * 描述：获取指定日期时间的字符串(可偏移).
     *
     * @param strDate String形式的日期时间
     * @param format 格式化字符串，如："yyyy-MM-dd HH:mm:ss"
     * @param calendarField Calendar属性，对应offset的值，
     * 如(Calendar.DATE,表示+offset天,Calendar.HOUR_OF_DAY,表示＋offset小时)
     * @param offset 偏移(值大于0,表示+,值小于0,表示－)
     * @return String String类型的日期时间
     */
    public static String getStringByOffset(String strDate, String format, int calendarField, int offset) {
        String mDateTime = null;
        try {
            Calendar c = new GregorianCalendar();
            SimpleDateFormat mSimpleDateFormat = new SimpleDateFormat(format);
            c.setTime(mSimpleDateFormat.parse(strDate));
            c.add(calendarField, offset);
            mDateTime = mSimpleDateFormat.format(c.getTime());
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return mDateTime;
    }

    /**
     * 描述：Date类型转化为String类型(可偏移).
     *
     * @param date the date
     * @param format the format
     * @param calendarField the calendar field
     * @param offset the offset
     * @return String String类型日期时间
     */
    public static String getStringByOffset(Date date, String format, int calendarField, int offset) {
        String strDate = null;
        try {
            Calendar c = new GregorianCalendar();
            SimpleDateFormat mSimpleDateFormat = new SimpleDateFormat(format);
            c.setTime(date);
            c.add(calendarField, offset);
            strDate = mSimpleDateFormat.format(c.getTime());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return strDate;
    }

    /**
     * 描述：Date类型转化为String类型.
     *
     * @param date the date
     * @param format the format
     * @return String String类型日期时间
     */
    public static String getStringByFormat(Date date, String format) {
        SimpleDateFormat mSimpleDateFormat = new SimpleDateFormat(format);
        String strDate = null;
        try {
            strDate = mSimpleDateFormat.format(date);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return strDate;
    }

    /**
     * 描述：获取指定日期时间的字符串,用于导出想要的格式.
     *
     * @param strDate String形式的日期时间，必须为yyyy-MM-dd HH:mm:ss格式
     * @param format 输出格式化字符串，如："yyyy-MM-dd HH:mm:ss"
     * @return String 转换后的String类型的日期时间
     */
    public static String getStringByFormat(String strDate, String format) {
        String mDateTime = null;
        try {
            Calendar c = new GregorianCalendar();
            SimpleDateFormat mSimpleDateFormat = new SimpleDateFormat(dateFormatYMDHMS);
            c.setTime(mSimpleDateFormat.parse(strDate));
            SimpleDateFormat mSimpleDateFormat2 = new SimpleDateFormat(format);
            mDateTime = mSimpleDateFormat2.format(c.getTime());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return mDateTime;
    }

    /**
     * 描述：获取milliseconds表示的日期时间的字符串.
     *
     * @param milliseconds the milliseconds
     * @param format 格式化字符串，如："yyyy-MM-dd HH:mm:ss"
     * @return String 日期时间字符串
     */
    public static String getStringByFormat(long milliseconds, String format) {
        String thisDateTime = null;
        try {
            SimpleDateFormat mSimpleDateFormat = new SimpleDateFormat(format);
            thisDateTime = mSimpleDateFormat.format(milliseconds);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return thisDateTime;
    }

    /**
     * 描述：获取表示当前日期时间的字符串.
     *
     * @param format 格式化字符串，如："yyyy-MM-dd HH:mm:ss"
     * @return String String类型的当前日期时间
     */
    public static String getCurrentDate(String format) {
        // AbLogUtil.d(AbDateUtil.class, "getCurrentDate:"+format);
        String curDateTime = null;
        try {
            SimpleDateFormat mSimpleDateFormat = new SimpleDateFormat(format);
            Calendar c = new GregorianCalendar();
            curDateTime = mSimpleDateFormat.format(c.getTime());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return curDateTime;
    }

    public static String getCurrentDate(SimpleDateFormat dateFormatter) {
        Calendar c = new GregorianCalendar();
        return dateFormatter.format(c.getTime());
    }

    public static String getCurrentDate() {
        // AbLogUtil.d(AbDateUtil.class, "getCurrentDate:"+format);
        String curDateTime = null;
        try {
            SimpleDateFormat mSimpleDateFormat = new SimpleDateFormat(dateFormatYMDHMS);
            Calendar c = new GregorianCalendar();
            curDateTime = mSimpleDateFormat.format(c.getTime());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return curDateTime;
    }

    /**
     * 描述：获取表示当前日期时间的字符串(可偏移).
     *
     * @param format 格式化字符串，如："yyyy-MM-dd HH:mm:ss"
     * @param calendarField Calendar属性，对应offset的值，
     * 如(Calendar.DATE,表示+offset天,Calendar.HOUR_OF_DAY,表示＋offset小时)
     * @param offset 偏移(值大于0,表示+,值小于0,表示－)
     * @return String String类型的日期时间
     */
    public static String getCurrentDateByOffset(String format, int calendarField, int offset) {
        String mDateTime = null;
        try {
            SimpleDateFormat mSimpleDateFormat = new SimpleDateFormat(format);
            Calendar c = new GregorianCalendar();
            c.add(calendarField, offset);
            mDateTime = mSimpleDateFormat.format(c.getTime());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return mDateTime;
    }

    /**
     * 描述：计算两个日期所差的天数.
     *
     * @param milliseconds1 the milliseconds1
     * @param milliseconds2 the milliseconds2
     * @return int 所差的天数 （milliseconds1 - milliseconds2）
     */
    public static int getOffectDay(long milliseconds1, long milliseconds2) {
        // 2016/10/14 11.28 lsj修改
        Calendar calendar1 = Calendar.getInstance();
        calendar1.setTimeInMillis(milliseconds1);
        Calendar calendar2 = Calendar.getInstance();
        calendar2.setTimeInMillis(milliseconds2);
        // 将时，分，秒置为相同， 后续的时间差以24小时为一天计算（避免昨天17点到今天10点，相差天数为0的情况出现）
        calendar1.set(Calendar.HOUR_OF_DAY, calendar2.get(Calendar.HOUR_OF_DAY));
        calendar1.set(Calendar.MINUTE, calendar2.get(Calendar.MINUTE));
        calendar1.set(Calendar.SECOND, calendar2.get(Calendar.SECOND));
        calendar1.set(Calendar.MILLISECOND, calendar2.get(Calendar.MILLISECOND));
        int day = (int) ((calendar1.getTimeInMillis() - calendar2.getTimeInMillis()) / (24 * 60 * 60 * 1000l));
        return day;
    }

    /**
     * 描述：计算两个日期所差的小时数.
     *
     * @param date1 第一个时间的毫秒表示
     * @param date2 第二个时间的毫秒表示
     * @return int 所差的小时数
     */
    public static int getOffectHour(long date1, long date2) {
        Calendar calendar1 = Calendar.getInstance();
        calendar1.setTimeInMillis(date1);
        Calendar calendar2 = Calendar.getInstance();
        calendar2.setTimeInMillis(date2);
        int h1 = calendar1.get(Calendar.HOUR_OF_DAY);
        int h2 = calendar2.get(Calendar.HOUR_OF_DAY);
        int h = 0;
        int day = getOffectDay(date1, date2);
        h = h1 - h2 + day * 24;
        return h;
    }

    /**
     * 描述：计算两个日期所差的分钟数.
     *
     * @param date1 第一个时间的毫秒表示
     * @param date2 第二个时间的毫秒表示
     * @return int 所差的分钟数
     */
    public static int getOffectMinutes(long date1, long date2) {
        Calendar calendar1 = Calendar.getInstance();
        calendar1.setTimeInMillis(date1);
        Calendar calendar2 = Calendar.getInstance();
        calendar2.setTimeInMillis(date2);
        int m1 = calendar1.get(Calendar.MINUTE);
        int m2 = calendar2.get(Calendar.MINUTE);
        int h = getOffectHour(date1, date2);
        int m = 0;
        m = m1 - m2 + h * 60;
        return m;
    }

    /**
     * 描述：获取本周一.
     *
     * @param format the format
     * @return String String类型日期时间
     */
    public static String getFirstDayOfWeek(String format) {
        return getDayOfWeek(format, Calendar.MONDAY);
    }

    /**
     * 描述：获取本周日.
     *
     * @param format the format
     * @return String String类型日期时间
     */
    public static String getLastDayOfWeek(String format) {
        return getDayOfWeek(format, Calendar.SUNDAY);
    }

    /**
     * 描述：获取本周的某一天.
     *
     * @param format the format
     * @param calendarField the calendar field
     * @return String String类型日期时间
     */
    private static String getDayOfWeek(String format, int calendarField) {
        String strDate = null;
        try {
            Calendar c = new GregorianCalendar();
            SimpleDateFormat mSimpleDateFormat = new SimpleDateFormat(format);
            int week = c.get(Calendar.DAY_OF_WEEK);
            if (week == calendarField) {
                strDate = mSimpleDateFormat.format(c.getTime());
            } else {
                int offectDay = calendarField - week;
                if (calendarField == Calendar.SUNDAY) {
                    offectDay = 7 - Math.abs(offectDay);
                }
                c.add(Calendar.DATE, offectDay);
                strDate = mSimpleDateFormat.format(c.getTime());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return strDate;
    }

    /**
     * 描述：获取本月第一天.
     *
     * @param format the format
     * @return String String类型日期时间
     */
    public static String getFirstDayOfMonth(String format) {
        String strDate = null;
        try {
            Calendar c = new GregorianCalendar();
            SimpleDateFormat mSimpleDateFormat = new SimpleDateFormat(format);
            // 当前月的第一天
            c.set(GregorianCalendar.DAY_OF_MONTH, 1);
            strDate = mSimpleDateFormat.format(c.getTime());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return strDate;
    }

    /**
     * 描述：获取本月最后一天.
     *
     * @param format the format
     * @return String String类型日期时间
     */
    public static String getLastDayOfMonth(String format) {
        String strDate = null;
        try {
            Calendar c = new GregorianCalendar();
            SimpleDateFormat mSimpleDateFormat = new SimpleDateFormat(format);
            // 当前月的最后一天
            c.set(Calendar.DATE, 1);
            c.roll(Calendar.DATE, -1);
            strDate = mSimpleDateFormat.format(c.getTime());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return strDate;
    }

    /**
     * 描述：获取表示当前日期的0点时间毫秒数.
     *
     * @return the first time of day
     */
    public static long getFirstTimeOfDay() {
        Date date = null;
        try {
            String currentDate = getCurrentDate(dateFormatYMD);
            date = getDateByFormat(currentDate + " 00:00:00", dateFormatYMDHMS);
            return date.getTime();
        } catch (Exception e) {
        }
        return -1;
    }

    public static long getFirstTimeOfTheDay(String dateStr) {
        Date date = null;
        try {
            date = getDateByFormat(dateStr + " 00:00:00", dateFormatYMDHMS);
            return date.getTime();
        } catch (Exception e) {
        }
        return -1;
    }

    public static long getLastTimeOfTheDay(String dateStr) {
        Date date = null;
        try {
            date = getDateByFormat(dateStr + " 24:00:00", dateFormatYMDHMS);
            return date.getTime();
        } catch (Exception e) {
        }
        return -1;
    }

    /**
     * 描述：获取表示当前日期24点时间毫秒数.
     *
     * @return the last time of day
     */
    public static long getLastTimeOfDay() {
        Date date = null;
        try {
            String currentDate = getCurrentDate(dateFormatYMD);
            date = getDateByFormat(currentDate + " 24:00:00", dateFormatYMDHMS);
            return date.getTime();
        } catch (Exception e) {
        }
        return -1;
    }

    /**
     * 将毫秒转化为 yyyy-MM-dd HH:mm:ss 格式的时间输出
     */
    public static String formatTime(long millis) {
        // 假如这个是你已知的时间类型
        // Calendar cal = Calendar.getInstance();
        // cal.setTimeInMillis(milllis);
        // 北京时区GMT+8
        Calendar cal = Calendar.getInstance();
        cal.clear();
        cal.setTimeZone(TimeZone.getTimeZone("GMT+8"));
        cal.setTimeInMillis(millis);
        DateFormat fmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String time = fmt.format(cal.getTime());
        return time;
    }

    private final static long DAY_MILLIS = 24 * 3600 * 1000;

    public static String formatSpecialSecond(String secondStr) {
        long second = CommunityUtil.parse2Long(secondStr);
        if (second <= 0) {
            return secondStr;
        }
        return formatSpecialMills(second * 1000);
    }

    public static String formatSpecialMills(long millis) {

        String prefixStr = "";

        long curMillis = System.currentTimeMillis();

        int offDay = getOffectDay(curMillis, millis);
        String pattern = dateFormatHM;
        if (offDay == 0) {// 今天
            // prefixStr = "今天 ";
            prefixStr = "今天 ";
        } else if (offDay == 1) {// 昨天
            //return AllOnlineApp.getInstantce().getResources().getString(R.string.date_yesterday);
            prefixStr = "昨天 ";
        } else if (offDay == 2) {// 前天
            //return AllOnlineApp.getInstantce().getResources().getString(R.string.before_yesterday);
            prefixStr = "前天 ";
        } else {
            pattern = dateFormatMDHM;
        }

        // 假如这个是你已知的时间类型
        // Calendar cal = Calendar.getInstance();
        // cal.setTimeInMillis(millis);
        // 北京时区GMT+8
        Calendar cal = Calendar.getInstance();
        cal.clear();
        cal.setTimeZone(TimeZone.getTimeZone("GMT+8"));
        cal.setTimeInMillis(millis);
        DateFormat fmt = new SimpleDateFormat(pattern);
        String time = fmt.format(cal.getTime());
        return prefixStr + time;
    }

    public static String format2BeforeTime(String seconds) {
        long secondsLong = CommunityUtil.parse2Long(seconds);
        if (secondsLong < 0) {
            return seconds;
        }
        long millis = secondsLong * 1000;
        long curMillis = System.currentTimeMillis();
        long curSecond = curMillis / 1000;
        long difference = curSecond - secondsLong;
        int difDay = getOffectDay(curMillis, millis);
        if (difference <= 60) {// 刚刚
            return AllOnlineApp.mApp.getResources().getString(R.string.now);
            //return "刚刚";
        } else if (difference < 60 * 60) {// 多少分钟前
            if (difference / 60 == 1) {
                return AllOnlineApp.mApp.getResources().getString(R.string.before_min_single, difference / 60);
            } else {
                return AllOnlineApp.mApp.getResources().getString(R.string.before_min, difference / 60);
            }
            //return difference / 60 + "分钟前";
        } else if (difference < 24 * 3600) {// 多少小时前
            if (difference / 3600 == 1) {
                return AllOnlineApp.mApp.getResources().getString(R.string.before_hour_single, difference / 3600);
            } else {
                return AllOnlineApp.mApp.getResources().getString(R.string.before_hour, difference / 3600);
            }
            //return difference / 3600 + "小时前";
        } else if (difDay == 1) {// 多少天前
            return AllOnlineApp.mApp.getResources().getString(R.string.date_yesterday);
            //return "昨天";
        } else if (difDay == 2) {// 多少天前
            return AllOnlineApp.mApp.getResources().getString(R.string.the_day_before_yesterday);
            //return "前天";
        } else {// 多少天前
            return getStringByFormat(millis, dateFormatYMD);
        }
    }

    /**
     * 聊天功能时间格式化
     */
    public static String formatTime4Chat(String seconds) {
        long secondsLong = CommunityUtil.parse2Long(seconds);
        if (secondsLong < 0) {
            return seconds;
        } else {
            return formatTime4Chat(secondsLong);
        }
    }

    /**
     * 聊天功能时间格式化
     */
    public static String formatTime4Chat(long seconds) {
        long millis = seconds * 1000;
        long curMillis = System.currentTimeMillis();
        int difDay = getOffectDay(curMillis, millis);
        if (difDay < 1) {
            // 09:02
            return getStringByFormat(millis, dateFormatHM);
        } else if (difDay == 1) {
            // 昨天 09:02
            return "昨天 " + getStringByFormat(millis, dateFormatHM);
        } else if (isSameYear(millis, curMillis)) {
            // 同一年
            if (isSameWeek(millis, curMillis)) {
                // 同一周
                return getStringByFormat(millis, formatWeekDay(millis) + " " + dateFormatHM);
            } else {
                return getStringByFormat(millis, dateFormatMDHM_cn);
            }
        } else {
            // 不同年
            return getStringByFormat(millis, dateFormatYMDHM_cn);
        }
    }

    public static String formatWeekDay(long mills) {
        Calendar cal = Calendar.getInstance();
        cal.clear();
        cal.setTimeZone(TimeZone.getTimeZone("GMT+8"));
        cal.setTimeInMillis(mills);
        int intTemp = cal.get(Calendar.DAY_OF_WEEK) - 1;
        String week = "";
        switch (intTemp) {
            case 0:
                week = "周日";
                break;
            case 1:
                week = "周一";
                break;
            case 2:
                week = "周二";
                break;
            case 3:
                week = "周三";
                break;
            case 4:
                week = "周四";
                break;
            case 5:
                week = "周五";
                break;
            case 6:
                week = "周六";
                break;
        }
        return week;
    }

    public static String formatTime(String millisStr) {
        return formatTime(millisStr, dateFormatYMDHMS);
    }

    public static String formatTime(String millisStr, String pattern) {
        long millis = CommunityUtil.parse2Long(millisStr);
        if (CommunityUtil.parse2Long(millisStr) <= 0) {
            return millisStr;
        }
        // 假如这个是你已知的时间类型
        // Calendar cal = Calendar.getInstance();
        // cal.setTimeInMillis(millis);
        // 北京时区GMT+8
        Calendar cal = Calendar.getInstance();
        cal.clear();
        cal.setTimeZone(TimeZone.getTimeZone("GMT+8"));
        cal.setTimeInMillis(millis);
        DateFormat fmt = new SimpleDateFormat(pattern);
        String time = fmt.format(cal.getTime());
        return time;
    }

    public static String formatTime(long millis, String pattern) {
        // 假如这个是你已知的时间类型
        // Calendar cal = Calendar.getInstance();
        // cal.setTimeInMillis(millis);
        // 北京时区GMT+8
        Calendar cal = Calendar.getInstance();
        cal.clear();
        cal.setTimeZone(TimeZone.getTimeZone("GMT+8"));
        cal.setTimeInMillis(millis);
        DateFormat fmt = new SimpleDateFormat(pattern);
        String time = fmt.format(cal.getTime());
        return time;
    }

    /**
     * 描述：判断是否是闰年()
     * <p>
     * (year能被4整除 并且 不能被100整除) 或者 year能被400整除,则该年为闰年.
     *
     * @param year 年代（如2012）
     * @return boolean 是否为闰年
     */
    public static boolean isLeapYear(int year) {
        if ((year % 4 == 0 && year % 400 != 0) || year % 400 == 0) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * 描述：根据时间返回格式化后的时间的描述. 小于1小时显示多少分钟前 大于1小时显示今天＋实际日期，大于今天全部显示实际时间
     *
     * @param strDate the str date
     * @param outFormat the out format
     * @return the string
     */
    public static String formatDateStr2Desc(String strDate, String outFormat) {

        DateFormat df = new SimpleDateFormat(dateFormatYMDHMS);
        Calendar c1 = Calendar.getInstance();
        Calendar c2 = Calendar.getInstance();
        try {
            c2.setTime(df.parse(strDate));
            c1.setTime(new Date());
            int d = getOffectDay(c1.getTimeInMillis(), c2.getTimeInMillis());
            if (d == 0) {
                int h = getOffectHour(c1.getTimeInMillis(), c2.getTimeInMillis());
                if (h > 0) {
                    return "今天" + getStringByFormat(strDate, dateFormatHM);
                    // return h + "小时前";
                } else if (h < 0) {
                    // return Math.abs(h) + "小时后";
                } else if (h == 0) {
                    int m = getOffectMinutes(c1.getTimeInMillis(), c2.getTimeInMillis());
                    if (m > 0) {
                        return m + "分钟前";
                    } else if (m < 0) {
                        // return Math.abs(m) + "分钟后";
                    } else {
                        return "刚刚";
                    }
                }
            } else if (d > 0) {
                if (d == 1) {
                    // return "昨天"+getStringByFormat(strDate,outFormat);
                } else if (d == 2) {
                    // return "前天"+getStringByFormat(strDate,outFormat);
                }
            } else if (d < 0) {
                if (d == -1) {
                    // return "明天"+getStringByFormat(strDate,outFormat);
                } else if (d == -2) {
                    // return "后天"+getStringByFormat(strDate,outFormat);
                } else {
                    // return Math.abs(d) +
                    // "天后"+getStringByFormat(strDate,outFormat);
                }
            }

            String out = getStringByFormat(strDate, outFormat);
            // if(!TextUtils.isEmpty(out)){
            return out;
            // }
        } catch (Exception e) {
        }

        return strDate;
    }

    public static String formatMsgTime(Context context, long seconds) {
        return formatMsgTime(context, seconds, false);
    }

    public static String formatMsgTime(Context context, long seconds, boolean isDigest) {

        long millis = seconds * 1000;

        long curMillis = System.currentTimeMillis();

        int offDay = getOffectDay(curMillis, millis);
        String pattern = null;
        if (offDay == 0) { // 今天
            pattern = dateFormatHM;
            pattern = "mm";
            long iDiff = System.currentTimeMillis() - millis;
            int iMintue = (int) (iDiff / 60000);// 分钟
            if (iMintue < 1) {
                // 刚刚
                return context.getString(R.string.now);
            } else if (iMintue < 60) {
                // “XX分钟前
                if (iMintue == 1) {
                    return String.format(context.getString(R.string.before_min_single), iMintue);
                } else {
                    return String.format(context.getString(R.string.before_min), iMintue);
                }
            } else if (iMintue / 60 < 24) {
                // XX小时前
                if (iMintue / 60 == 1) {
                    return String.format(context.getString(R.string.before_hour), iMintue / 60);
                } else {
                    return String.format(context.getString(R.string.before_hour), iMintue / 60);
                }
            }
            return formatTime(millis, pattern);
        } else if (offDay == 1) { // 昨天
            return context.getString(R.string.date_yesterday);
        } else if (offDay == 2) { // 前天
            return context.getString(R.string.the_day_before_yesterday);
        } else {
            if (isDigest) {
                pattern = dateFormatDMYHM;
            } else {
                pattern = dateFormatYMD;
            }
            return formatTime(millis, pattern);
        }
    }

    /**
     * 根据给定的日期判断是否为上下午.
     *
     * @param strDate the str date
     * @param format the format
     * @return the time quantum
     */
    public static String getTimeQuantum(String strDate, String format) {
        Date mDate = getDateByFormat(strDate, format);
        int hour = mDate.getHours();
        if (hour >= 12) {
            return "PM";
        } else {
            return "AM";
        }
    }

    /**
     * 根据给定的毫秒数算得时间的描述.
     *
     * @param milliseconds the milliseconds
     * @return the time description
     */
    public static String getTimeDescription(long milliseconds) {
        if (milliseconds > 1000) {
            // 大于一分
            if (milliseconds / 1000 / 60 > 1) {
                long minute = milliseconds / 1000 / 60;
                long second = milliseconds / 1000 % 60;
                return minute + "分" + second + "秒";
            } else {
                // 显示秒
                return milliseconds / 1000 + "秒";
            }
        } else {
            return milliseconds + "毫秒";
        }
    }

    public static String convert2Hour(int minute) {
        String hour = minute / 60 + "";
        String minu = minute % 60 + "";
        if (hour.length() == 1) {
            hour = "0" + hour;
        }
        if (minu.length() == 1) {
            minu = "0" + minu;
        }
        return hour + ":" + minu;
    }

    public static int convert2Minute(String time) {
        String[] array = time.split(":");
        return CommunityUtil.parse2Integer(array[0]) * 60 + CommunityUtil.parse2Integer(array[1]);
    }

    public static String formatCommunityActDateTime(Context context, long millis, String dateDesc, boolean bAddTime) {
        if (millis <= 0) {
            return "";
        }
        Calendar cal = Calendar.getInstance();
        cal.clear();
        cal.setTimeZone(TimeZone.getTimeZone("GMT+8"));
        cal.setTimeInMillis(millis * 1000);
        DateFormat fmt = new SimpleDateFormat("MM.dd");
        String time = fmt.format(cal.getTime());

        if (TextUtils.isEmpty(dateDesc)) {
            dateDesc = getWeekStr(context, cal.get(Calendar.DAY_OF_WEEK) - 1);
        }

        String hourMin = "";
        if (bAddTime) {
            fmt = new SimpleDateFormat(dateFormatHM);
            hourMin = fmt.format(cal.getTime());
        }

        return time + "(" + dateDesc + ")" + hourMin;
    }

    public static String formatCommunityActDateTime(Context context, long millis, boolean bAddTime) {
        if (millis <= 0) {
            return "";
        }
        Calendar cal = Calendar.getInstance();
        cal.clear();
        cal.setTimeZone(TimeZone.getTimeZone("GMT+8"));
        cal.setTimeInMillis(millis * 1000);
        DateFormat fmt = new SimpleDateFormat("MM.dd");
        String time = fmt.format(cal.getTime());
        String week = getWeekStr(context, cal.get(Calendar.DAY_OF_WEEK) - 1);

        String hourMin = "";
        if (bAddTime) {
            fmt = new SimpleDateFormat(dateFormatHM);
            hourMin = fmt.format(cal.getTime());
        }

        return time + "(" + week + ")" + hourMin;
    }

    public static String getWeekStr(Context context, int iWeek) {
        String week = "";
        switch (iWeek) {
            case 0:
            default:
                week = context.getString(R.string.sunday);
                break;
            case 1:
                week = context.getString(R.string.monday);
                break;
            case 2:
                week = context.getString(R.string.tuesday);
                break;
            case 3:
                week = context.getString(R.string.wednesday);
                break;
            case 4:
                week = context.getString(R.string.thursday);
                break;
            case 5:
                week = context.getString(R.string.friday);
                break;
            case 6:
                week = context.getString(R.string.saturday);
                break;
        }

        return week;
    }

    private static DateFormat fmtSame = new SimpleDateFormat(dateFormatYMD);

    /** 输入 毫秒 为单位的数据 */
    public static boolean isSameDay(long startMillis, long endMillis) {
        if (startMillis <= 0 || endMillis <= 0) {
            return false;
        }
        Calendar cal = Calendar.getInstance();
        cal.clear();
        cal.setTimeZone(TimeZone.getTimeZone("GMT+8"));
        cal.setTimeInMillis(startMillis);
        if (fmtSame == null) {
            fmtSame = new SimpleDateFormat(dateFormatYMD);
        }
        String md = fmtSame.format(cal.getTime());
        if (TextUtils.isEmpty(md)) {
            return false;
        }

        cal.clear();
        cal.setTimeZone(TimeZone.getTimeZone("GMT+8"));
        cal.setTimeInMillis(endMillis);
        if (md.equals(fmtSame.format(cal.getTime()))) {
            return true;
        }
        return false;
    }

    /**
     * 是否同一年
     */
    public static boolean isSameYear(long startMillis, long endMillis) {
        Calendar cal = Calendar.getInstance();
        cal.clear();
        cal.setTimeZone(TimeZone.getTimeZone("GMT+8"));
        cal.setTimeInMillis(startMillis);
        Calendar cal2 = Calendar.getInstance();
        cal2.clear();
        cal2.setTimeZone(TimeZone.getTimeZone("GMT+8"));
        cal2.setTimeInMillis(endMillis);
        if (cal.get(Calendar.YEAR) == cal2.get(Calendar.YEAR)) {
            return true;
        }
        return false;
    }

    /**
     * 是否同一周
     */
    public static boolean isSameWeek(long startMillis, long endMillis) {
        int diffDays = getOffectDay(startMillis, endMillis);
        Calendar cal = Calendar.getInstance();
        cal.clear();
        cal.setTimeZone(TimeZone.getTimeZone("GMT+8"));
        cal.setTimeInMillis(startMillis);
        Calendar cal2 = Calendar.getInstance();
        cal2.clear();
        cal2.setTimeZone(TimeZone.getTimeZone("GMT+8"));
        cal2.setTimeInMillis(endMillis);
        if (cal.get(Calendar.YEAR) == cal2.get(Calendar.YEAR)
            && cal.get(Calendar.WEEK_OF_YEAR) == cal2.get(Calendar.WEEK_OF_YEAR)) {
            // 同一年，同一周
            return true;
        }
        return false;
    }

    public static String getMonthDayWeekString(Context context, long millis) {
        if (millis <= 0) {
            return "";
        }
        millis = millis * 1000;
        Calendar cal = Calendar.getInstance();
        cal.clear();
        cal.setTimeZone(TimeZone.getTimeZone("GMT+8"));
        cal.setTimeInMillis(millis);
        // DateFormat fmt = new SimpleDateFormat("MM.dd");
        // String time = fmt.format(cal.getTime());
        String weekOrDay = getWeekStr(context, cal.get(Calendar.DAY_OF_WEEK) - 1);
        long curMillis = System.currentTimeMillis();
        int offDay = getOffectDay(curMillis, millis);
        if (offDay == 0) {
            // 今天
            return context.getString(R.string.date_today);
        } else if (offDay == 1) {
            // 昨天
            weekOrDay = context.getString(R.string.date_yesterday);
        } else if (offDay == 2) {
            // 前天
            weekOrDay = context.getString(R.string.the_day_before_yesterday);
        }

        return String.format(context.getString(R.string.checkin_date_string), cal.get(Calendar.MONTH) + 1,
            cal.get(Calendar.DATE), weekOrDay);
    }

    /**
     * 传入时间是毫秒单位
     */
    public static String getDateStrByFormat(long milliSecond, String format) {
        Calendar cal = Calendar.getInstance();
        cal.clear();
        cal.setTimeZone(TimeZone.getTimeZone("GMT+8"));
        cal.setTimeInMillis(milliSecond);
        DateFormat fmt = new SimpleDateFormat(format);
        return fmt.format(cal.getTime());
    }

    private static DateFormat fmt_hh;

    /**
     * 判断时间是否早于某个时间点--整点，oclock
     */
    public static boolean isLaterThanXOclock(long millis, int oclock) {
        if (millis > 0 && oclock >= 0) {
            Calendar cal = Calendar.getInstance();
            cal.clear();
            cal.setTimeZone(TimeZone.getTimeZone("GMT+8"));
            cal.setTimeInMillis(millis);
            if (fmt_hh == null) {
                fmt_hh = new SimpleDateFormat("HH");
            }
            String hour = fmt_hh.format(cal.getTime());
            try {
                return Integer.parseInt(hour) >= oclock;
            } catch (Exception e) {
                // TODO: handle exception
            }
        }
        return false;
    }

    /**
     * 格式化活动时间的显示<br/>
     * 如果是同一天，只显示开始时间，不是同一天，显示开始时间和结束时间
     */
    public static String formatActTime(Context context, long beginTime, long endTime, boolean addTime) {
        if (isSameDay(beginTime * 1000, endTime * 1000)) {
            return formatCommunityActDateTime(context, beginTime, addTime);
        } else {
            return formatCommunityActDateTime(context, beginTime, addTime) + " - " + formatCommunityActDateTime(context,
                endTime, addTime);
        }
    }

    public static String formatActTime(Context context, long beginTime, long endTime, String beginDateDesc,
        String endDateDesc, boolean addTime) {
        if (isSameDay(beginTime * 1000, endTime * 1000)) {
            return formatCommunityActDateTime(context, beginTime, beginDateDesc, addTime);
        } else {
            return formatCommunityActDateTime(context, beginTime, beginDateDesc, addTime)
                + " - "
                + formatCommunityActDateTime(context,
                endTime, endDateDesc, addTime);
        }
    }

    //将秒数转为xx小时xx分钟的样式
    public static String formatSec2UnitString(int seconds, String hourUnit, String minuteUnit) {
        String str = "";
        int hours = seconds / (60 * 60);
        int leftMin = seconds % (60 * 60);
        int minutes = leftMin / 60;
        if (leftMin % 60 > 0) {
            minutes++;
        }
        //进位
        if (minutes >= 60) {
            hours++;
            minutes -= 60;
        }
        //拼接返回值
        if (hours > 0) {
            str = hours + hourUnit;
        }
        if (minutes > 0) {
            str += minutes + minuteUnit;
        }
        return str;
    }
}
