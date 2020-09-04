package com.goome.gpns.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import com.goome.gpns.GPNSInterface;
import com.goome.gpns.GpnsSDKInitializer;
import com.goome.gpns.noti.NotifyManager;
import com.goome.gpns.noti.NotifyManager.NotifyOpenedReceiver;
import com.goome.gpns.utils.CommonUtil;
import com.goome.gpns.utils.FileOperationUtil;
import com.goome.gpns.utils.LogUtil;
import com.goome.gpns.utils.PreferenceUtil;
import com.goome.gpns.utils.ResourceUtils;
import com.goome.gpnsjni.NativePresenter;
import com.goome.gpnsjni.NativePresenter.MsgCallback;
import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Properties;
import java.util.Timer;
import java.util.TimerTask;

public class GPNSService extends Service implements MsgCallback {
    public static final String CONFIG_FILE_NAME = "serverInfo.properties";
    public static final String HOST_KEY = "server_host", PORT_KEY = "server_port", PUSH_MODEL_KEY = "push_model";
    private static final int MSG_APP_ACTIVE_REMINDER = 0X100;
    public static int pushModel = 1;
    private static String host;
    private static int port;
    private static long mDetectAppActiveStateInterval = 10 * 60;// 检测app是否启动时间间隔
    private static long mAppActiveExpire = 60 * 60; // app超过2天未运行，应提醒
    private static long mAppActiveReminderInterval = 60 * 60;// app太久未使用，提醒间隔，1天

    // private static long mDetectAppActiveStateInterval = 10 * 60;//
    // 检测app是否启动时间间隔
    // private static long mAppActiveExpire = 7 * 24 * 60 * 60; //
    // app超过2天未运行，应提醒
    // private static long mAppActiveReminderInterval = 24 * 60 * 60;//
    // app太久未使用，提醒间隔，1天
    private final String mPreferenceFileName = "gns_share_preference_data";
    private MyHandler mHandler;
    private SharedPreferences mSharedPreferences;
    private NativePresenter mPresenter;
    private Thread socketThread;
    private NotifyOpenedReceiver notifyOpenedReceiver;

    @Override
    public void onCreate() {
        super.onCreate();
        GpnsSDKInitializer.initialize(this);
        // Log.d("gpns", "GPNSService onCreate");
        // Log.d("gpns", "服务被启动");
        GPNSInterface.launchAlarmManager(this);
        registerNotifyOpenedReceiver();
        // WakeLockUtil.getWakeLock(this);
        // sendTestMsg();

        mSharedPreferences = getSharedPreferences(mPreferenceFileName, MODE_PRIVATE);

        mHandler = new MyHandler(this);
        mHandler.sendEmptyMessageDelayed(MSG_APP_ACTIVE_REMINDER, 30 * 1000);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // Log.d("gpns", Thread.currentThread().getName() + " onStartCommand");
        if (socketThread == null) {
            long channelId = -1;
            if (intent != null) {
                channelId = intent.getLongExtra(PreferenceUtil.CHANNEL_ID, -1);
            } else {
                channelId = ChannelIdInterface.getChannelIdFromFile();
            }
            readyToBuildTcpConnection(channelId);
        } else {
            // Log.d("gpns", "soketThread已启动，取消本次启动");
        }

        // GPNS Service 运行在单独的进程中，为了避免跨进程造成的SharedPreferences更新不正确的问题
        if (intent != null) {
            int appActive = intent.getIntExtra("APP_ACTIVE", -1);
            if (appActive == 1) {
                long current = System.currentTimeMillis() / 1000;
                mSharedPreferences.edit().putLong(PreferenceUtil.APP_ACTIVE_TIME, current).commit();
            } else if (appActive == 0) {
                long current = System.currentTimeMillis() / 1000;
                mSharedPreferences.edit().putLong(PreferenceUtil.APP_DEACTIVE_TIME, current).commit();
            }
        }

        return START_STICKY;
        // return START_NOT_STICKY;
        /*
         * 在返回值为粘性的（START_STICKY），当服务被强杀时，
         * 使用GPNSInterface.isServiceRun(context)判断服务是否在运行，
         * 有时候竟然判断服务还在运行，为了确保可以正确判断服务是否 在运行，必须把 service
         * onStartCommand返回值改为非粘性的（START_NOT_STICKY）
         */
    }

    public void readyToBuildTcpConnection(long channelId) {
        if (channelId != -1) {
            mPresenter = new NativePresenter(this);
            mPresenter.register(this);
            startSocket(channelId);
        } else {
            FileOperationUtil.saveErrMsgToFile("获取不到通道ID，无法与服务器连接");
        }
    }

    private synchronized void startSocket(long channelId) {
        // Log.e("gpns", "startSocket============== ");
        if (socketThread != null) {
            // Log.d("gpns", "soketThread已启动，取消本次启动");
            return;
        }
        readServerInfoFromFile(this, CONFIG_FILE_NAME);
        String initResult = mPresenter.init(host, getAppInfo(), port, channelId, pushModel);
        // Log.d("gpns", "socket init=" + initResult + ",channelId=" + channelId);
        if (initResult.equals("OK")) {
            // Log.d("gpns", "初始化成功");
            socketThread = new Thread() {
                @Override
                public void run() {
                    Log.d("gpns", "开始与服务器建立连接:threadId=" + socketThread.getId());
                    if (mPresenter != null) {
                        try {
                            mPresenter.start();
                        } catch (Exception e) {
                            FileOperationUtil.saveErrMsgToFile("mPresenter.start() occur an exception:" + e.toString());
                            LogUtil.printException2Console(e);
                        }
                    } else {
                        FileOperationUtil.saveExceptionInfoToFile("startSocket() mPresenter is null");
                    }
                }
            };
            socketThread.setName("socketThread");
            socketThread.start();
        } else {
            Log.d("gpns", "初始化失败，重试");
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                FileOperationUtil.saveErrMsgToFile("startSocket() occur an exception:" + e.toString());
                LogUtil.printException2Console(e);
            }
            startSocket(channelId);
        }
    }

    private void readServerInfoFromFile(Context ctx, String fileName) {
        try {
            Properties properties = new Properties();
            properties.load(ctx.getResources().getAssets().open(CONFIG_FILE_NAME));
            host = properties.getProperty(HOST_KEY);
            port = CommonUtil.parse2Integer(properties.getProperty(PORT_KEY));
            pushModel = CommonUtil.parse2Integer(properties.getProperty(PUSH_MODEL_KEY));
            LogUtil.i("host=" + host + ",port=" + port + ",pushModel=" + pushModel);
        } catch (Exception e) {
            FileOperationUtil.saveErrMsgToFile("readServerInfoFromFile occur an exception:" + e.toString());
            LogUtil.printException2Console(e);
        }
    }

    /**
     * 启动app的时候 拼接json格式 版本号 当前语言 手机平台 deviceName 机型&当前的手机版本号 deviceModel ==
     * android {"appVersion":"","language":"","deviceName":"","deviceModel":""}
     *
     * @return appInfo
     */
    public String getAppInfo() {
        StringBuffer sb = new StringBuffer("{");
        sb.append("\"appVersion\":\"");
        sb.append(GPNSInterface.getCurrentAppVersionName(this));
        sb.append("\",\"language\":\"");
        sb.append(GPNSInterface.getLanguage());
        sb.append("\",\"deviceName\":\"");
        sb.append(GPNSInterface.getDeviceName());
        sb.append("\",\"deviceModel\":\"");
        sb.append(GPNSInterface.PLATFORM_TYPE);
        sb.append("\"");
        sb.append("}");

        LogUtil.i(sb.toString());
        return sb.toString();
    }

    /**
     * 发送模拟推送消息
     */
    private void sendTestMsg() {
        LogUtil.i("sendTestMsg");
        Timer timer = new Timer("TestTimer");
        final long PERIOD_TIME = 10 * 1000;
        timer.scheduleAtFixedRate(new TimerTask() {

            private int count = 1;

            @Override
            public void run() {
                count++;
                String testMsg = "{'content':'测试" + count
                        + "','extras':{'alarm':'28271879,震动报警,1449580993,22.508862,113.371894,22.514968,113.378377,2','shake':'default','sound':'default','type':1}}";
                broadcastMessage(testMsg);
            }
        }, PERIOD_TIME, PERIOD_TIME);
    }

    /**
     * 将收到的原始推送消息广播出去
     */
    public void broadcastMessage(String rawPushMsg) {
        // Log.e("gpns", "rawPushMsg=" + rawPushMsg);
        try {
            CommonUtil.broadcastMessage(this, GPNSInterface.ACTION_MESSAGE_RECEIVED, GPNSInterface.RAW_PUSH_MSG,
                rawPushMsg);
            // LogUtil.d("SDK广播:" + rawPushMsg);
        } catch (Exception e) {
            FileOperationUtil.saveErrMsgToFile("SDK广播消息出错：" + e.toString());
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        LogUtil.d("onDestroy");
        mHandler.removeMessages(MSG_APP_ACTIVE_REMINDER);
        GPNSInterface.launchAlarmManager(this, 30 * 1000);
        unRegisterNotifyOpenedReceiver();
        if (mPresenter != null) {
            LogUtil.d("onDestroy停止服务");
            mPresenter.stop();
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }

    private void registerNotifyOpenedReceiver() {
        notifyOpenedReceiver = new NotifyOpenedReceiver();
        IntentFilter filter = new IntentFilter(GPNSInterface.ACTION_SDK_NOTIFICATION_OPENED);
        this.registerReceiver(notifyOpenedReceiver, filter);
    }

    private void unRegisterNotifyOpenedReceiver() {
        try {
            this.unregisterReceiver(notifyOpenedReceiver);
        } catch (Exception e) {
            FileOperationUtil.saveExceptionInfoToFile(
                    "GpnsService unRegisterNotifyOpenedReceiver occur an Exception:" + e.getMessage());
        }
    }

    private void appActiveReminder() {
        long active = mSharedPreferences.getLong(PreferenceUtil.APP_ACTIVE_TIME, 0);
        long deactive = mSharedPreferences.getLong(PreferenceUtil.APP_DEACTIVE_TIME, 0);
        long current = System.currentTimeMillis() / 1000;
        Log.v("timereminder", "active:" + active + "   deactive:" + deactive + "   current:" + current);
        if (deactive > active && current - deactive > mAppActiveExpire) {
            Log.v("timereminder", "111111111111111111");
            long reminder = mSharedPreferences.getLong(PreferenceUtil.APP_ACTIVE_REMINDER_TIME, 0);
            int curHour = currentHour(current);
            if (current - reminder > mAppActiveReminderInterval && curHour >= 8 && curHour < 22) {
                Log.v("timereminder", "222222222");
                // 提醒
                String reminderFormat = getString(ResourceUtils.getIdByName(this, "string", "active_reminder"));
                String appName = getApplicationInfo().loadLabel(getPackageManager()).toString();
                String msg = "{'content':'" + String.format(reminderFormat, appName, appName)
                        + "','extras':{'shake':'default','sound':'default'}}";
                NotifyManager.showNotify(getApplicationContext(),msg, NotifyManager.MSG_TYPE_LOCALE,false);
                mSharedPreferences.edit().putLong(PreferenceUtil.APP_ACTIVE_REMINDER_TIME, current).commit();
            }
        }
    }

    private int currentHour(long currentSeconds) {
        /// 截取出小时点
        Date currentDate = new Date(currentSeconds * 1000);
        SimpleDateFormat sdf = new SimpleDateFormat("HH", Locale.getDefault());
        String cuStrDate = sdf.format(currentDate);
        int cuIntDate = Integer.parseInt(cuStrDate);
        return cuIntDate;
    }

    private static class MyHandler extends Handler {
        private WeakReference<GPNSService> mRef;

        public MyHandler(GPNSService service) {
            mRef = new WeakReference<GPNSService>(service);
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_APP_ACTIVE_REMINDER:
                    mRef.get().appActiveReminder();
                    sendEmptyMessageDelayed(MSG_APP_ACTIVE_REMINDER, mDetectAppActiveStateInterval * 1000);
                    break;

                default:
                    super.handleMessage(msg);
                    break;
            }
        }
    }

}
