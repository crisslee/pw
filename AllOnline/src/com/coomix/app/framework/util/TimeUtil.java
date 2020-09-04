package com.coomix.app.framework.util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.SystemClock;
import com.coomix.app.all.R;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class TimeUtil {

    private static final String TAG = "TimeUtil";

    public static final long SECONDS_IN_A_DAY = 24 * 60 * 60;

    /**
     * 转换为xx天/2 day 格式. （一秒也算一天)
     */
    public static String getOneUnitDay(Context context, long second) {
        int day = 0;
        day = (int) ((second + SECONDS_IN_A_DAY - 1) / (SECONDS_IN_A_DAY));
        StringBuilder sb = new StringBuilder();
        if (day > 0) {
            sb.append(day + context.getString(R.string.unit_day));
        }

        return sb.toString();
    }

    /**
     * 2 day 3 hour
     */
    public static String getTwoUnits(Context context, long second) {
        int day = 0, hour = 0, min = 0, sec = 0;
        day = (int) (second / (SECONDS_IN_A_DAY));

        int left = (int) (second % (SECONDS_IN_A_DAY));
        hour = left / (60 * 60);
        left = (int) (left % (60 * 60));
        min = left / 60;
        sec = left % 60;

        StringBuilder sb = new StringBuilder();
        int count = 0;
        while (count < 2) {
            if (day > 0) {
                count++;
                sb.append(day + context.getString(R.string.unit_day));
                day = 0;
                continue;
            }
            if (hour > 0) {
                count++;
                sb.append(hour + context.getString(R.string.unit_hour));
                hour = 0;
                continue;
            }
            if (min > 0) {
                count++;
                sb.append(min + context.getString(R.string.unit_minute));
                min = 0;
                continue;
            }
            if (sec > 0) {
                count++;
                sb.append(sec + context.getString(R.string.unit_second));
                break;
            }
            break;
        }
        return sb.toString();
    }

    public static String getTimeDHMSOld(Context context, long second,int keepCount) {
        int curCount = 0;
        int day = 0, hour = 0, min = 0, sec = 0;
        day = (int) (second / (SECONDS_IN_A_DAY));

        int leftOfDay = (int) (second % (SECONDS_IN_A_DAY));
        hour = leftOfDay / (60 * 60);
        int leftOfHour = (int) (leftOfDay % (60 * 60));
        min = leftOfHour / 60;
        sec = leftOfHour % 60;

        StringBuilder sb = new StringBuilder();

        if (day > 0) {
            curCount++;
            if(curCount >= keepCount)
            {
                if(leftOfDay > 0)
                {
                    day = day + 1;
                }
                sb.append(day + context.getString(R.string.unit_day));
                return sb.toString();
            }
            else
            {
                sb.append(day + context.getString(R.string.unit_day));
            }
        }
        if (hour > 0) {
            curCount++;
            if(curCount >= keepCount)
            {
                if(leftOfHour > 0)
                {
                    hour = hour + 1;
                }
                sb.append(hour + context.getString(R.string.unit_hour));
                return sb.toString();
            }
            else
            {
                sb.append(hour + context.getString(R.string.unit_hour));
            }
        }
        if (min > 0) {
            curCount++;
            if(curCount >= keepCount)
            {
                if(sec > 0)
                {
                    min = min + 1;
                }
                sb.append(min + context.getString(R.string.unit_minute));
                return sb.toString();
            }
            else
            {
                sb.append(min + context.getString(R.string.unit_minute));
            }
        }
        if (sec > 0) {
            sb.append(sec + context.getString(R.string.unit_second));
        }
        return sb.toString();
    }

    public static String getTimeDHMS(Context context, long second,int keepCount) {
        int curCount = 0;
        int day = 0, hour = 0, min = 0, sec = 0;
        day = (int) (second / (SECONDS_IN_A_DAY));

        int leftOfDay = (int) (second % (SECONDS_IN_A_DAY));
        hour = leftOfDay / (60 * 60);
        int leftOfHour = (int) (leftOfDay % (60 * 60));
        min = leftOfHour / 60;
        sec = leftOfHour % 60;

        StringBuilder sb = new StringBuilder();

        if (day > 0) {
            curCount++;
            if (curCount >= keepCount) {
                if (leftOfDay > 0) {
                    day = day + 1;
                }
                sb.append(day + context.getString(R.string.unit_day));
                return sb.toString();
            } else {
                sb.append(day + context.getString(R.string.unit_day));
            }
        } else {
            if (keepCount == 1) {
                if (leftOfDay > 0) {
                    day = 1;
                }
                sb.append(day + context.getString(R.string.unit_day));
                return sb.toString();
            }
        }

        if (hour > 0) {
            sb.append(hour + context.getString(R.string.unit_hour));
            curCount++;
            if (curCount >= keepCount) {
                return sb.toString();
            }
        }
        if (min > 0) {
            sb.append(min + context.getString(R.string.unit_minute));
            curCount++;
            if (curCount >= keepCount) {
                return sb.toString();
            }
        }
        if (sec > 0) {
            sb.append(sec + context.getString(R.string.unit_second));
        }

        if (sb.length() == 0) {
            sb.append(0 + context.getString(R.string.unit_minute));
        }

        return sb.toString();
    }

    /**
     * 秒数转成时分秒  xx:xx:xx
     */
    public static String sec2Time(long time) {
        String timeStr = null;
        long hour = 0;
        long minute = 0;
        long second = 0;
        if (time <= 0) {
            return "00:00:00";
        } else {
            minute = time / 60;
            if (minute < 60) {
                second = time % 60;
                timeStr = "00:" + unitFormat(minute) + ":" + unitFormat(second);
            } else {
                hour = minute / 60;
                minute = minute % 60;
                second = time - hour * 3600 - minute * 60;
                timeStr = unitFormat(hour) + ":" + unitFormat(minute) + ":" + unitFormat(second);
            }
        }
        return timeStr;
    }

    private static String unitFormat(long i) {
        String retStr = null;
        if (i >= 0 && i < 10) {
            retStr = "0" + Long.toString(i);
        } else {
            retStr = "" + i;
        }
        return retStr;
    }

    public static String getTimeDHMSToDay(Context context, long second) {
        int day = 0;
        day = (int) (second / (SECONDS_IN_A_DAY));
        StringBuilder sb = new StringBuilder();
        if (day > 0) {
            sb.append(day + context.getString(R.string.unit_day));
        }

        return sb.toString();
    }

    public static String converTime(Date timestamp, Context mContext) {
        if (timestamp == null) {
            return mContext.getResources().getString(R.string.time_diff);
        }
        // Log.d(TAG, "timestamp:"+timestamp.toGMTString());
        long currentmillSeconds = System.currentTimeMillis();
        // Log.d(TAG, "currentmillSeconds:"+currentmillSeconds+", "+ new
        // Date(currentmillSeconds).toGMTString());
        long timeGap = (currentmillSeconds - timestamp.getTime()) / 1000;// 与现在时间相差秒数
        // Log.d(TAG, "timeGap:" + timeGap);
        String timeStr = null;
        if (timeGap > 365 * SECONDS_IN_A_DAY) {// 1年以上
            timeStr = TimeUtil.getStandardTime(timestamp, "yyyy-MM-dd HH:mm");
        } else if (timeGap > SECONDS_IN_A_DAY * 3) {
            timeStr = TimeUtil.getStandardTime(timestamp, "yyyy-MM-dd HH:mm");
        } else if (timeGap > SECONDS_IN_A_DAY * 2) {
            timeStr = mContext.getResources().getString(R.string.before_yesterday)
                    + TimeUtil.getStandardTime(timestamp, " HH:mm");// 前天
        } else if (timeGap > SECONDS_IN_A_DAY * 1) {
            timeStr = mContext.getResources().getString(R.string.yesterday)
                    + TimeUtil.getStandardTime(timestamp, " HH:mm");// 昨天
        } else if (timeGap > 60 * 60) {// 1小时-24小时
            timeStr = timeGap / (60 * 60) + mContext.getResources().getString(R.string.one_hour);// 小時前
        } else if (timeGap > 60) {// 1分钟-59分钟
            timeStr = timeGap / 60 + mContext.getResources().getString(R.string.minute_before);// 分鐘前
        } else {// 1秒钟-59秒钟
            timeStr = mContext.getResources().getString(R.string.just_just);// 剛剛
        }
        return timeStr;
    }

    /**
     * 传入之前的时间
     *
     * @param timestamp
     * @return
     */
    public static long converTimeForHour(long timestamp) {

        // 当前的时间
        long currentmillSeconds = System.currentTimeMillis();
        long timeGap = (currentmillSeconds - timestamp) / 1000;// 与现在时间相差秒数

        return timeGap;
    }

    public static String getStandardTime(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat(FORMAT_DATE_TIME);
        return sdf.format(date);
    }

    public static String getStandardTime(Date date, String format) {
        if (date == null) {
            return "";
        }
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        return sdf.format(date);
    }

    public final static String FORMAT_DATE_TIME_MINUTE = "yyyy-MM-dd HH:mm";

    public final static String FORMAT_DATE = "yyyy-MM-dd";
    public final static String FORMAT_TIME = "HH:mm:ss";
    public final static String FORMAT_DATE_TIME = "yyyy-MM-dd HH:mm:ss";
    public final static String FORMAT_DATE_TIME_DETAIL = "yyyy-MM-dd HH:mm:ss:SSS";
    public final static String FORMAT_MONTH_DAY_TIME = "MM月dd日 HH:mm:ss";
    public static final String FORMAT_DATE_TO_MINUTE = "yyyy.MM.dd HH:mm";
    public static final String FORMAT_12_HOUR = "aa hh:mm";

    public final static String FORMAT_TIME_HOUR = "HH:mm";
    public final static String FORMAT_TIME_HOUR_ONLY = "HH";

    /**
     * 将当前的时间截取到小时：分钟
     */

    @SuppressLint("SimpleDateFormat")
    public static String getCurrentHour(Date cuDate) {
        SimpleDateFormat sdf = new SimpleDateFormat(FORMAT_TIME_HOUR);
        String cuStrDate = sdf.format(cuDate);
        return cuStrDate;
    }

    /**
     * 将当前的时间截取到小时
     */

    @SuppressLint("SimpleDateFormat")
    public static int getCurrentHourOnly() {
        try {

            long currentMillis = System.currentTimeMillis();
            // 截取出小时点
            Date currentDate = new Date(currentMillis);
            SimpleDateFormat sdf = new SimpleDateFormat(FORMAT_TIME_HOUR_ONLY);
            String cuStrDate = sdf.format(currentDate);
            int cuIntDate = Integer.parseInt(cuStrDate);

            return cuIntDate;

        } catch (Exception e) {
            return 10;
        }

    }

    /**
     * 将日期字符串以指定格式转换为Date
     *
     * @param timeStr   日期字符串
     * @param format 指定的日期格式，若为null或""则使用指定的格式"yyyy-MM-dd HH:mm:ss"
     * @return
     */
    public static Date getTimeFromString(String timeStr, String format) {
        // Log.d(TAG, "timeStr:"+timeStr + ", format:"+format);
        SimpleDateFormat sdf;
        if (timeStr == null || timeStr.length() == 0) {
            return null;
        }
        if (format == null || format.trim().equals("")) {
            sdf = new SimpleDateFormat(FORMAT_DATE_TIME);
        } else {
            sdf = new SimpleDateFormat(format);
        }
        try {
            return sdf.parse(timeStr.trim());
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static long syncServerTime(long firstSyncelapsedRealtime, long serverTime) {
        long elapsedRealtime = SystemClock.elapsedRealtime();
        long currentTime = serverTime + (elapsedRealtime - firstSyncelapsedRealtime);
        // Calendar calendar = Calendar.getInstance(TimeZone
        // .getTimeZone("UTC"));
        // calendar.setTimeInMillis(currentTime);
        return currentTime;
    }

    public static long dateString2Long(String timeStr) {
        Date date = null;
        long mills = 0;
        try {
            date = new SimpleDateFormat(FORMAT_DATE_TIME_MINUTE, Locale.ENGLISH).parse(timeStr);
            mills = (date.getTime() / 1000);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return mills;
    }

    @SuppressLint("SimpleDateFormat")
    public static String long2TimeString(long millSecond) {
        DateFormat formatter = new SimpleDateFormat(FORMAT_TIME_HOUR);
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(millSecond);
        return formatter.format(calendar.getTime());
    }

    @SuppressLint("SimpleDateFormat")
    public static String long2DateTimeString(long millSecond) {
        DateFormat formatter = new SimpleDateFormat(FORMAT_DATE_TIME);
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(millSecond);
        return formatter.format(calendar.getTime());
    }

    @SuppressLint("SimpleDateFormat")
    public static String long2MinuteDate(long millSecond) {
        DateFormat formatter = new SimpleDateFormat(FORMAT_DATE_TO_MINUTE);
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(millSecond);
        return formatter.format(calendar.getTime());
    }

    @SuppressLint("SimpleDateFormat")
    public static String long212Hour(long millSecond) {
        DateFormat formatter = new SimpleDateFormat(FORMAT_12_HOUR);
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(millSecond);
        return formatter.format(calendar.getTime());
    }

    @SuppressLint("SimpleDateFormat")
    public static String long2DateString(Calendar c) {
        DateFormat formatter = new SimpleDateFormat(FORMAT_DATE);
        return formatter.format(c.getTime());
    }

    public static String TimeLong2String(int minutes) {
        StringBuilder sb = new StringBuilder();
        int hour = minutes / 60;
        int min = minutes % 60;

        if(hour < 10) {
            sb.append("0");
        }
        sb.append(hour);

        sb.append(":");

        if(min < 10) {
            sb.append("0");
        }
        sb.append(min);

        return sb.toString();
    }
}
