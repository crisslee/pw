package com.coomix.app.all.service;

import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v4.app.NotificationCompat;
import android.text.format.DateUtils;
import com.coomix.app.all.AllOnlineApp;
import com.coomix.app.all.Constant;
import com.coomix.app.all.R;
import com.coomix.app.all.ui.update.GoomeUpdateAgent;
import com.coomix.app.all.ui.update.GoomeUpdateConstant;
import com.coomix.app.all.ui.update.GoomeUpdateInfo;
import com.coomix.app.all.ui.update.GoomeUpdateListener;
import com.coomix.app.all.ui.update.GoomeUpdateStatus;
import com.coomix.app.framework.util.PreferenceUtil;
import com.coomix.app.all.util.Utils;
import com.tencent.bugly.crashreport.CrashReport;

import static com.coomix.app.all.Constant.IS_DEBUG_MODE;

public class CheckVersionService extends Service {
    private AlarmManager mAlarmManager = null;

    private final String TAG = "CheckVersionService";

    // 通知管理器
    private static NotificationManager mNM;
    private final static int FINA_NEW_VERSION = 110;

    // 通知显示内容
    // private PendingIntent mPendingIntent;
    private static final int NOTIFICATION_ID_MEDIA = 003;
    private Notification mMediaNotification;

    public static void startService(Context mContext) {

        boolean isRun = isCheckVersionServiceRun(mContext);
        if (isRun) {
            // It is just for testing
        } else {
            try {
                mContext.startService(new Intent(mContext, CheckVersionService.class));
            } catch (Exception e) {
                if (IS_DEBUG_MODE) {
                    e.printStackTrace();
                }
                // OPPO低版本5.1及以前手机，省电策略存在bug，开启服务SecurityException user 0 is restricted
                CrashReport.postCatchedException(e);
            }
        }
    }

    private static boolean isCheckVersionServiceRun(Context context) {
        boolean isCheckServiceRunning = false;
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        // 检测后台运行的服务
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (CheckVersionService.class.getName().equals(service.service.getClassName())) {
                isCheckServiceRunning = true;
                break;
            } else {
                isCheckServiceRunning = false;
                continue;
            }
        }
        return isCheckServiceRunning;
    }

    public static boolean isDownloadServiceRun(Context context) {
        boolean isDownloadServiceRunning = false;
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        // 检测后台运行的服务
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (DownloadingService.class.getName().equals(service.service.getClassName())) {
                isDownloadServiceRunning = true;
                break;
            } else {
                isDownloadServiceRunning = false;
                continue;
            }
        }
        return isDownloadServiceRunning;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    // AlarmManager.ELAPSED_REALTIME; //时间流逝时间，即相对于系统启动时间，睡眠时间考虑在内，睡眠状态不可用
    // AlarmManager.ELAPSED_REALTIME_WAKEUP;
    // //时间流逝时间，即相对于系统启动时间，睡眠时间考虑在内，睡眠状态下唤醒
    // AlarmManager.RTC; //硬件时间，即系统时间，睡眠状态不可用
    // AlarmManager.RTC_WAKEUP; //硬件时间，即系统时间，睡眠状态下唤醒
    @Override
    public void onCreate() {
        mNM = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        Intent intent = new Intent(getApplicationContext(), CheckVersionService.class);
        mAlarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        PendingIntent mPendingIntentRepeat = PendingIntent.getService(this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT);

        try {
            // 软件启动5分钟后开始 执行
            long intervalDelay = DateUtils.MINUTE_IN_MILLIS * 5;
            long firstWake = System.currentTimeMillis() + intervalDelay; // 第一次启动延迟启动的时间

            // mAlarmManager.setRepeating(AlarmManager.RTC, firstWake, interval, mPendingIntentRepeat);
            mAlarmManager.set(AlarmManager.RTC, firstWake, mPendingIntentRepeat);
            // 硬件时间，即系统时间，睡眠状态不可用
        } catch (Exception e) {
            e.printStackTrace();
        }
        super.onCreate();
    }

    int count = 0;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        count++;
        try {
            boolean isStart = PreferenceUtil.getBoolean(GoomeUpdateConstant.KEY_lIST_PREFERENCE_DOWNLOAD_START, false);
            if (IS_DEBUG_MODE) {
                //System.out.println("service"+count);
            }
            if (count > 1 && !isStart) {
                Message msg = myHandler.obtainMessage();
                msg.what = FINA_NEW_VERSION;
                msg.sendToTarget();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return START_REDELIVER_INTENT;
    }

    @SuppressWarnings("HandlerLeak")
    Handler myHandler = new Handler() {
        @Override
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case FINA_NEW_VERSION:
                    if (AllOnlineApp.gUpdateInfo == null) {
                        GoomeUpdateAgent.setUpdateListener(new GoomeUpdateListener() {
                            @Override
                            public void onUpdateReturned(int updateStatus, GoomeUpdateInfo updateInfo) {
                                if (updateStatus == GoomeUpdateStatus.Yes) {
                                    //AllOnlineApp.gUpdateInfo = updateInfo;
                                    // 如果该版本被忽略掉了就不再提醒
                                    int ignore = PreferenceUtil.getInt(
                                        GoomeUpdateConstant.KEY_lIST_PREFERENCE_IGNORE_VERSION_CODE, -1);
                                    if (updateInfo != null && updateInfo.update
                                        && Integer.parseInt(updateInfo.verCode) != ignore) {
                                        startVib(AllOnlineApp.gUpdateInfo);// 开启提醒功能
                                    }
                                } else if (updateStatus == GoomeUpdateStatus.No) {
                                    //已是最新版本
                                    cancleNotify();
                                }
                            }
                        });
                        GoomeUpdateAgent.update(CheckVersionService.this);
                    } else {
                        GoomeUpdateInfo updateInfo = AllOnlineApp.gUpdateInfo;
                        if (updateInfo.update) {
                            startVib(AllOnlineApp.gUpdateInfo);// 开启提醒功能
                        } else {
                            //已是最新版本
                            cancleNotify();
                        }
                    }
                    break;
                default:
                    break;
            }
        }
    };

    /**
     * 开启震动和响铃
     */
    private void startVib(GoomeUpdateInfo info) {
        Utils.createChannel(mNM, Constant.channelIdUpdate, Constant.channelNameUpdate);
        if (isDownloadServiceRun(getApplicationContext())) {
            cancleNotify();
            return;
        }
        mNM.cancel(NOTIFICATION_ID_MEDIA);

        Intent intent = new Intent();
        // WarmNewVersionActivity
        intent.setAction("com.coomix.app.all.sdialog");
        intent.putExtra("version", "yes");
        intent.putExtra("version_update", info.update);
        intent.putExtra("version_vercode", info.verCode);
        intent.putExtra("version_vername", info.verName);
        intent.putExtra("version_desc", info.desc);
        intent.putExtra("version_url", info.url);
        intent.putExtra("version_md5", info.newMd5);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);//(Intent.FLAG_ACTIVITY_SINGLE_TOP);

        PendingIntent mPendingIntent = PendingIntent.getActivity(this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationCompat.Builder notifyBuilder = new NotificationCompat.Builder(this, Constant.channelIdUpdate);
        notifyBuilder.setContentTitle(getString(R.string.app_name));
        notifyBuilder.setContentText(getString(R.string.update_notify));

        int drawableId = R.drawable.ic_notify;
        // if (android.os.Build.VERSION.SDK_INT >= 21) {
        //     // 5.0以上版本要求仅支持alpha通道，白色
        //     drawableId = R.drawable.ic_notify_sdk21;
        // }
        notifyBuilder.setSmallIcon(drawableId);
        // 如果不设置LargeIcon，那么系统会默认将上面的SmallIcon显示在通知选项的最左侧，右下角的小图标将不再显示
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher);
        notifyBuilder.setLargeIcon(bitmap);
        notifyBuilder.setWhen(System.currentTimeMillis());
        // 将AutoCancel设为true后，当你点击通知栏的notification后，它会自动被取消消失
        notifyBuilder.setAutoCancel(true);
        // 将Ongoing设为true 那么notification将不能滑动删除
        // notifyBuilder.setOngoing(true);
        // 从Android4.1开始，可以通过以下方法，设置notification的优先级，优先级越高的，通知排的越靠前，优先级低的，不会在手机最顶部的状态栏显示图标
        notifyBuilder.setPriority(NotificationCompat.PRIORITY_DEFAULT);
        // notifyBuilder.setPriority(NotificationCompat.PRIORITY_MIN);

        notifyBuilder.setTicker(getString(R.string.update_notify));

        notifyBuilder.setContentIntent(mPendingIntent);

        notifyBuilder.setDefaults(Notification.DEFAULT_ALL);
        mNM.notify(null, NOTIFICATION_ID_MEDIA, notifyBuilder.build());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    public static void cancleNotify() {
        if (null != mNM) {
            mNM.cancel(NOTIFICATION_ID_MEDIA);
        }
    }
}
