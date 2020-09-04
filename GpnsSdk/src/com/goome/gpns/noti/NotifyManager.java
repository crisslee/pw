package com.goome.gpns.noti;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Process;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;
import android.util.Log;
import com.goome.gpns.GPNSInterface;
import com.goome.gpns.utils.CommonUtil;
import com.goome.gpns.utils.FileOperationUtil;
import com.goome.gpns.utils.LogUtil;
import com.goome.gpns.utils.PreferenceUtil;
import org.json.JSONException;
import org.json.JSONObject;

public class NotifyManager {
    private static NotificationManager notiMgr;

    private static Notification mMediaNotification;

    public static GPNSNotificationBuilder notiBuilder;

    public static final int MAX_NOTIFY_ID = 100000;
    public static final int NOTIFICATION_MAX_NUM = 10;

    public static final String PREFIX = "I";
    private static final String SEPARATOR = ",";

    /**
     * msgType: MSG_TYPE_LOCALE, MSG_TYPE_REMOTE;
     */
    public static int MSG_TYPE_LOCALE = 0;
    public static int MSG_TYPE_REMOTE = 1;

    public static final String channelId = "coomix.all.gpns.high";
    public static final String channelName = "报警通知";

    public static void showNotify(Context context, String rawMsg, int msgType, boolean hasSource) {
        if (null == notiMgr) {
            notiMgr = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        }

        if (null == notiBuilder) {
            notiBuilder = GPNSNotificationBuilder.getDefaultBuilder(context);
        }

        if (!TextUtils.isEmpty(rawMsg)) {
            try {
                JSONObject rawMsgObj = new JSONObject(rawMsg);
                JSONObject extrasObj = rawMsgObj.getJSONObject("extras");
                boolean shake = extrasObj.has("shake");
                boolean sound = extrasObj.has("sound");
                notiBuilder.showMsg = rawMsgObj.getString("content");// 用于显示在状态栏的内容
                if (!TextUtils.isEmpty(notiBuilder.showMsg)) {
                    if (shake && sound) {
                        notiBuilder.notificationDefaults = Notification.DEFAULT_ALL;
                    } else if (shake) {
                        notiBuilder.notificationDefaults = Notification.DEFAULT_VIBRATE;
                    } else if (sound) {
                        notiBuilder.notificationDefaults = Notification.DEFAULT_SOUND;
                    } else {
                        notiBuilder.notificationDefaults = Notification.DEFAULT_LIGHTS;
                    }

                    notiBuilder.wholeMsg = rawMsg;// 用于解析
                    followMSgToVib(context, notiBuilder.notificationDefaults, notiBuilder.statusBarIcon,
                        notiBuilder.appName, notiBuilder.showMsg, notiBuilder.wholeMsg, msgType, hasSource);
                }
            } catch (JSONException e) {
                FileOperationUtil.saveExceptionInfoToFile("showNotify occur an JSONException:" + e.toString());
                e.printStackTrace();
            } catch (Exception e) {
                FileOperationUtil.saveExceptionInfoToFile("showNotify occur an exception:" + e.toString());
                e.printStackTrace();
            }
        }
    }

    private static int getNotifyId(Context context) {
        int notifyId = PreferenceUtil.getInt(GPNSInterface.NOTIFY_ID, 2);
        if (notifyId >= MAX_NOTIFY_ID) {
            notifyId = 0;
        }
        ++notifyId;
        boolean b = PreferenceUtil.saveInt(GPNSInterface.NOTIFY_ID, notifyId);
        if (!b) {
            FileOperationUtil.saveExceptionInfoToFile("保存notification ID 失败");
            PreferenceUtil.init(context);
            PreferenceUtil.saveInt(GPNSInterface.NOTIFY_ID, notifyId);
        }
        return notifyId;
    }

    public static void createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (notiMgr.getNotificationChannel(channelId) != null) {
                return;
            }
            NotificationChannel channel = new NotificationChannel(channelId, channelName,
                NotificationManager.IMPORTANCE_HIGH);
            channel.enableLights(true);
            channel.enableVibration(true);
            notiMgr.createNotificationChannel(channel);
        }
    }

    /**
     * 根据推送的消息 进行响铃 震动 震动类型 软件图标 软件名称 消息内容
     */
    private static void followMSgToVib(Context context, int notificationType, int iconId, String app_name,
        String showMsg, String rawMsg, int msgType, boolean hasSource) {
        createChannel();
        Log.d("felix",
            "显示通知：" + " notificationType:" + notificationType + "  iconid:" + iconId + "  app_name:" + app_name
            + "  showMsg:" + showMsg + "  rawMsg:" + rawMsg);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channelId);
        builder.setWhen(System.currentTimeMillis()).setAutoCancel(true);
        if (hasSource) {
            builder.setSmallIcon(0).setTicker(null);
        } else {
            builder.setSmallIcon(iconId).setTicker(showMsg);
        }

        // 根据app上的选择 确定使用哪种方式
        switch (notificationType) {
            case Notification.DEFAULT_ALL:
                builder.setDefaults(Notification.DEFAULT_ALL);
                //.setSound(notiBuilder.getLastAudioUri(context), AudioManager.ADJUST_RAISE);
                break;
            case Notification.DEFAULT_VIBRATE:
                // 震动频率
                long[] vir = {0, 200, 400, 600};
                builder.setDefaults(Notification.DEFAULT_VIBRATE).setVibrate(vir);
                break;
            case Notification.DEFAULT_SOUND:
                builder.setDefaults(Notification.DEFAULT_LIGHTS | Notification.DEFAULT_SOUND);
                //.setSound(notiBuilder.getLastAudioUri(context), AudioManager.ADJUST_RAISE);
                break;
            case Notification.DEFAULT_LIGHTS:
                builder.setDefaults(Notification.DEFAULT_LIGHTS);
                break;
            default:
                break;
        }

        int notifyId;
        if (msgType == MSG_TYPE_LOCALE) {
            notifyId = -1;
            PackageManager pm = context.getPackageManager();
            String pkgName = context.getPackageName();
            Intent intent = pm.getLaunchIntentForPackage(pkgName);
            PendingIntent mPendingIntent = PendingIntent.getActivity(context, notifyId, intent,
                    PendingIntent.FLAG_UPDATE_CURRENT);
            builder.setContentTitle(app_name).setContentText(showMsg).setContentIntent(mPendingIntent);
        } else {
            notifyId = getNotifyId(context);
            Intent intent = new Intent(GPNSInterface.ACTION_SDK_NOTIFICATION_OPENED);
            intent.putExtra(GPNSInterface.NOTIFY_ID, notifyId);
            intent.putExtra(GPNSInterface.RAW_PUSH_MSG, rawMsg);
            intent.putExtra(GPNSInterface.CONTENT, showMsg);

            PendingIntent mPendingIntent = PendingIntent.getBroadcast(context, notifyId, intent,
                    PendingIntent.FLAG_UPDATE_CURRENT);
            builder.setContentTitle(app_name).setContentText(showMsg).setContentIntent(mPendingIntent);
        }

        if (PreferenceUtil.getBoolean("long_shake", false)) {
            builder.setDefaults(Notification.DEFAULT_ALL);
            //.setSound(notiBuilder.getLastAudioUri(context),AudioManager.ADJUST_RAISE);
            mMediaNotification = builder.build();
            mMediaNotification.flags |= Notification.FLAG_INSISTENT;
        } else {
            mMediaNotification = builder.build();
        }

        LogUtil.d("notifyId:" + notifyId);

        // int lastNum = PreferenceUtil.getInt(PreferenceUtil.ACTIVE_NOTIFY_NUM, 0);
        String notifyIdsStr = PreferenceUtil.getString(PreferenceUtil.ACTIVE_NOTIFY_IDS, "");
        // LogUtil.i("最初"+notifyIdsStr+",lastNum="+lastNum);

        LogUtil.d("notifyIdsStr:" + notifyIdsStr);

        String[] notifyArray = notifyIdsStr.split(SEPARATOR);
        int lastNum = notifyArray.length;

        LogUtil.d("lastNum:" + lastNum);

        if (lastNum > NOTIFICATION_MAX_NUM) {
            // //去掉“I1,I2,I4,”最后一个分隔符
            // notifyIdsStr = notifyIdsStr.substring(0, notifyIdsStr.length()-1);
            LogUtil.d("lastNum=" + lastNum + ",notifyIdsStr=" + notifyIdsStr);
            for (int i = lastNum; i >= NOTIFICATION_MAX_NUM; i--) {
                notifyIdsStr = cancelNotify(notifyIdsStr);
            }
        } else if (lastNum == NOTIFICATION_MAX_NUM) {
            LogUtil.d("达到上限" + NOTIFICATION_MAX_NUM);
            notifyIdsStr = cancelNotify(notifyIdsStr);
        }

        notifyIdsStr += PREFIX + notifyId + ",";
        PreferenceUtil.saveString(PreferenceUtil.ACTIVE_NOTIFY_IDS, notifyIdsStr);

        LogUtil.d("notifyIdsStr:" + notifyIdsStr);

        notiMgr.notify(notifyId, mMediaNotification); // 显示多个图标在状态栏
        LogUtil.d("通知铃声：" + mMediaNotification.sound + "  通知类型：" + notificationType);
    }

    private static String cancelNotify(String notifyIdsStr) {
        String cancelIdStr = notifyIdsStr.substring(notifyIdsStr.indexOf(PREFIX) + 1, notifyIdsStr.indexOf(","));
        LogUtil.i("cancel前：" + notifyIdsStr);
        int cancelId = CommonUtil.parse2Integer(cancelIdStr);
        if (cancelId != Integer.MIN_VALUE) {
            notiMgr.cancel(cancelId);
            // changeActiveNotifyNum(-1);
        }
        notifyIdsStr = notifyIdsStr.substring(notifyIdsStr.indexOf(SEPARATOR) + 1);
        LogUtil.i("cancel后：" + notifyIdsStr);
        return notifyIdsStr;
    }

    /**
     * 接收消息通知被点击的广播事件。 实现消息通知的数量控制功能。
     *
     * @author Administrator
     */
    public static class NotifyOpenedReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            long startTime = System.currentTimeMillis();
            try {
                // LogUtil.i("NotifyOpenedReceiver onReceiver");
                // 删除本次点击的消息通知id信息
                int notifyId = intent.getIntExtra(GPNSInterface.NOTIFY_ID, -1);
                if (notifyId != -1) {
                    String notifyIdsStr = PreferenceUtil.getString(PreferenceUtil.ACTIVE_NOTIFY_IDS, "");
                    // LogUtil.i("notifyId="+notifyId);
                    String matchStr = PREFIX + notifyId + SEPARATOR;
                    // LogUtil.i("cancel前"+notifyIdsStr);
                    notifyIdsStr = notifyIdsStr.replace(matchStr, "");
                    // LogUtil.i("cancel后"+notifyIdsStr);
                    PreferenceUtil.saveString(PreferenceUtil.ACTIVE_NOTIFY_IDS, notifyIdsStr);
                }
                // changeActiveNotifyNum(-1);
            } catch (Exception e) {
                FileOperationUtil.saveExceptionInfoToFile("NotifyOpenedReceiver onReceive:" + e.getMessage());
            } finally {
                if (intent != null) {
                    Intent targetIntent = new Intent(GPNSInterface.ACTION_NOTIFICATION_OPENED);
                    targetIntent.putExtra(GPNSInterface.RAW_PUSH_MSG,
                            intent.getStringExtra(GPNSInterface.RAW_PUSH_MSG));
                    targetIntent.putExtra(GPNSInterface.CONTENT, intent.getStringExtra(GPNSInterface.CONTENT));
                    CommonUtil.broadcastMessage(context, targetIntent);
                }
            }
            long consumeTime = System.currentTimeMillis() - startTime;
            if (consumeTime > 10000) {
                LogUtil.e("consumeTime=" + consumeTime);
                FileOperationUtil.saveExceptionInfoToFile("NotifyOpenedReceiver onReceive耗时:" + consumeTime);
            }
        }
    }
}
