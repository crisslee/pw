package com.goome.gpns;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;
import com.android.goome.volley.Response;
import com.android.goome.volley.Response.Listener;
import com.android.goome.volley.TimeoutError;
import com.android.goome.volley.VolleyError;
import com.android.goome.volley.toolbox.StringRequest;
import com.goome.gpns.contentprovider.ProviderConst;
import com.goome.gpns.contentprovider.ProviderDBHelper;
import com.goome.gpns.noti.GPNSNotificationBuilder;
import com.goome.gpns.noti.NotifyManager;
import com.goome.gpns.service.ChannelIdInterface;
import com.goome.gpns.service.GPNSService;
import com.goome.gpns.service.MyBroadcastReceiver;
import com.goome.gpns.service.ResultListener;
import com.goome.gpns.utils.CommonUtil;
import com.goome.gpns.utils.ConnectionUtil;
import com.goome.gpns.utils.DateUtil;
import com.goome.gpns.utils.FileOperationUtil;
import com.goome.gpns.utils.LogUtil;
import com.goome.gpns.utils.MD5;
import com.goome.gpns.utils.PreferenceUtil;
import java.util.List;
import java.util.Locale;
import org.json.JSONException;
import org.json.JSONObject;

public class GPNSInterface {
    public static Context appContext;

    // public static final long RECORD_PERIOD = 20 * 1000; // test测试
    // private static final long CHECK_PERIOD = 30 * 1000; // test测试
    public static final long RECORD_PERIOD = 3 * 60 * 1000;
    public static final long CHECK_PERIOD = 5 * 60 * 1000;

    public final static String ACTION_CHANNEL_ID = "com.goome.all.gpns.intent.CHANNEL_ID";
    public final static String ACTION_MESSAGE_RECEIVED = "com.goome.all.gpns.intent.MESSAGE_RECEIVED";
    public final static String ACTION_SDK_NOTIFICATION_OPENED = "com.goome.all.gpns.intent.NOTIFICATION_SDK_OPENED";
    public final static String ACTION_NOTIFICATION_OPENED = "com.goome.all.gpns.intent.NOTIFICATION_OPENED";

    public final static String CHANNEL_ID = "channelId";
    public final static String RAW_PUSH_MSG = "gnpsPushMsg"; // 传递推送消息内容的key键
    public final static String CONTENT = "content";
    public final static String NOTIFY_ID = "notifyId";

    public final static String PLATFORM_TYPE = "android"; // android平台

    public static String appId; // this parameter use to get channelId
    public static String channelId;

    /**
     * 初始化GPNS,启动GPNS服务
     * 
     * @param context
     *            SDK运行所需要的context
     * @param appId
     *            appId
     */
    private synchronized static void initGPNS(Context context, String appId, GPNSNotificationBuilder notiBuilder,
            String channelUrl) {
        GpnsSDKInitializer.initialize(context);
        setPushNotificationBuilder(notiBuilder);
        GPNSInterface.launchAlarmManager(context);
//        Log.d("gpns","initGPNS appId=" + appId);
        if (TextUtils.isEmpty(appId)) {
            FileOperationUtil.saveExceptionInfoToFile("initGPNS() appId不能为空！！！");
            return;
        }
        GPNSInterface.appId = appId;
        PreferenceUtil.init(context);
        PreferenceUtil.saveString(PreferenceUtil.APP_ID, appId);
        ChannelIdInterface.setBaseUrl(channelUrl);

        // try to launch gpns service
        long channelId = ChannelIdInterface.getChannelIdFromFile();
        Log.e("gpns","channelId : " + channelId);
        if (channelId != -1) { // 从本地缓存文件获取通道ID
            LogUtil.d("从本地缓存文件获取通道ID:" + channelId);
            CommonUtil.broadcastMessage(appContext, GPNSInterface.ACTION_CHANNEL_ID, GPNSInterface.CHANNEL_ID,
                    channelId + "");
            tryToStartPushService(channelId);
            // startNewPushService();
        } else { // 从服务器获取通道ID
            ChannelIdInterface.getChannelIdViaNet(appId, new ResultListener<String>() {

                @Override
                public void onSuccessed(String channelId) {
                    LogUtil.d("从服务器获取通道ID:" + channelId);
                    Log.e("gpns","channelId : " + channelId);
                    tryToStartPushService(CommonUtil.parse2Long(channelId));
                }

                @Override
                public void onFailed(String error) {
                    Log.e("gpns","onFailed, error : " + error);
                    FileOperationUtil.saveExceptionInfoToFile(error);
                    LogUtil.showMsg("从服务器获取通道ID失败:" + error);
                }
            });
        }
    }

    public static void initGPNS(Context context, String appId, String gpnsProcessName,
            GPNSNotificationBuilder notiBuilder, String channelUrl) {
        initGPNS(context, appId, notiBuilder, channelUrl);
        setGpnsProcessName(gpnsProcessName);
    }

    private static void tryToStartPushService(long channelId) {
        if (GPNSInterface.isServiceReallyRun(appContext)) {
            Log.d("gpns","tryToStartPushService 服务已在运行,取消本次多余启动");
        } else {
            Log.d("gpns","tryToStartPushService 启动gpns服务");
            startGpnsService(channelId);
        }
    }

    public static void reStartPushService(long channelId) {
        try {
            LogUtil.d("reStartPushService 启动服务");
            killGpnsService(appContext);
            startGpnsService(channelId);
        } catch (Exception e) {
            FileOperationUtil.saveErrMsgToFile("reStartPushService occur an exception:" + e.toString());
        }
    }

    // private static Intent serviceIntent;
    private static void startGpnsService(long channelId) {
        Log.d("gpns","startGpnsService");
        Intent serviceIntent = new Intent(appContext, GPNSService.class);
        serviceIntent.putExtra(PreferenceUtil.CHANNEL_ID, channelId);
        appContext.startService(serviceIntent); // 启动service 开启socket连接
    }

    private static void stopGpnsService(Context context) {
        try {
            if (isServiceInRunningList(context)) {
                LogUtil.i("手动停止Gpns服务");
                appContext.stopService(new Intent(context, GPNSService.class));
            } else {
                LogUtil.i("Gpns服务没有在运行");
            }
        } catch (Exception e) {
            FileOperationUtil.saveErrMsgToFile("stopService() occur an exception:" + e.toString());
        }
    }

    public static String getAppId() {
        try {
            if (!TextUtils.isEmpty(appId)) {
                return appId;
            }
            appId = PreferenceUtil.getString(PreferenceUtil.APP_ID, null);
            if (TextUtils.isEmpty(appId)) {
                FileOperationUtil.saveExceptionInfoToFile("获取appId失败");
            }
        } catch (Exception e) {
            FileOperationUtil.saveErrMsgToFile("getAppId() occur an exception:appId=" + appId);
            LogUtil.printException2Console(e);
        }
        return appId;
    }

    // 当前的语言
    public static String getLanguage() {
        String lang = "en";
        String languageCode = Locale.getDefault().getLanguage();
        String countryCode = Locale.getDefault().getCountry();
        if ("zh".equals(languageCode)) {
            lang = languageCode + (TextUtils.isEmpty(countryCode) ? "" : "-" + countryCode);
        }
        return lang;
    }

    /**
     * 获取设备名称
     */
    public static String getDeviceName() {
        String deviceName = Build.MODEL; // 当前手机的机型
        // int curVersion = android.os.Build.VERSION.SDK_INT;
        String d = android.os.Build.VERSION.RELEASE; // 当前系统的版本号
        return deviceName + "&" + d;
    }

    /**
     * 当前应用的app版本号
     * 
     * @return
     */
    public static String getCurrentAppVersionName(Context context) {
        String versionName = "";
        try {
            // ---get the package info---
            PackageManager pm = context.getPackageManager();
            PackageInfo pi = pm.getPackageInfo(context.getPackageName(), 0);
            versionName = pi.versionName;

            /*
             * if (versionName == null || versionName.length() == 0) { return
             * ""; } else { //V_DEV(20140917) return versionName.substring(0,
             * versionName.indexOf("(")); }
             */
        } catch (Exception e) {
            Log.e("VersionInfo", "Exception", e);
        }
        return versionName;
    }

    // 从AndroidManifest.xml下获取<data>下的数据
    // 从AndroidMainfest配置文件下读取文件
    public static String readAppIdFromMainfest(Context mContext) throws Exception {
        ApplicationInfo appInfo;
        String appId;
        try {
            appInfo = mContext.getPackageManager().getApplicationInfo(mContext.getPackageName(),
                    PackageManager.GET_META_DATA);

            appId = appInfo.metaData.getString("GPNS_APPID");
            LogUtil.i("appId=" + appId);

            if (appId.equals("") || appId == null) {
                throw new Exception("Please check your appkey");
            }
            return appId;
        } catch (NameNotFoundException e) {
            FileOperationUtil.saveErrMsgToFile("readAppIdFromMainfest() occur an exception:" + e.toString());
            LogUtil.printException2Console(e);
        }
        return null;
    }

    // /**
    // * 设置别名和标签名 ------暂时取消该功能
    // * @param mCt 上下文
    // * @param tags 标签名
    // * @param alias 别名
    // */
    // public static void setAliasAndTags(Context mCt,String tags,String alias){
    // System.out.println("通过广播设置Tags和Alias");
    // Intent intent = new Intent(GPNS_SET_ALIAS_AND_TAGS_ACTION);
    // intent.putExtra("mtags", tags);
    // intent.putExtra("malias", alias);
    // mCt.sendBroadcast(intent);
    // }

    /*
     * // //采用这种方式判断服务是否运行会导致不同项目只能建立一个服务，暂时返回false 重复start也进入service，但不执行任何操作
     * public static boolean isServiceRun(Context context) {
     * 这段代码有问题：有时候当服务被强杀后，竟然会判断服务还在，做不到自动启动，原因是： 在service
     * onStartCommand返回值为粘性的（START_STICKY），当服务被强杀时，
     * 使用以下代码判断服务是否在运行，有时候竟然判断服务还在运行，为了确保可以正确判断服务是否 在运行，必须把service
     * onStartCommand返回值改为非粘性的（START_NOT_STICKY） ActivityManager manager =
     * (ActivityManager) context .getSystemService(Context.ACTIVITY_SERVICE);
     * boolean isServiceRunning = false; // 检测后台运行的服务 for (RunningServiceInfo
     * serviceInfo : manager .getRunningServices(Integer.MAX_VALUE)) { //
     * LogUtil.i("service list="+manager //
     * .getRunningServices(Integer.MAX_VALUE)); if
     * (GPNSService.class.getName().equals( serviceInfo.service.getClassName()))
     * { // if(GPNSService.isRunning){ // CommonUtil.showMsg("GPNS正在运行");
     * isServiceRunning = true; LogUtil.e("服务正在运行中..."); // } else{ //
     * isServiceRunning = false; // LogUtil.e("Gpns服务实际上并没有在运行"); //
     * LoggerUtil.debug("Gpns服务实际上并没有在运行"); //// CommonUtil.showMsg("GPNS没有运行");
     * // }
     * 
     * break; } else { isServiceRunning = false; continue; } }
     * LogUtil.i("isServiceRunning="+isServiceRunning); return isServiceRunning;
     * // return false; }
     */

    public static boolean isServiceInRunningList(Context context) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        boolean isServiceRunning = false;
        List<RunningServiceInfo> runningServiceList = manager.getRunningServices(Integer.MAX_VALUE);
        // 检查服务是否存在运行服务列表中
        for (RunningServiceInfo serviceInfo : runningServiceList) {
            if (GPNSService.class.getName().equals(serviceInfo.service.getClassName())) {
                LogUtil.i("serviceInfo.process=" + serviceInfo.process);
                if (serviceInfo.process.equals(getGpnsProcessName(context))) {
                    LogUtil.d("Gpns服务存在运行列表中");
                    isServiceRunning = true;
                    break;
                }
            } else {
                isServiceRunning = false;
                continue;
            }
        }
        return isServiceRunning;
    }

    private static final float FACTOR = 2.5f;

    private static int count = 0;

    public static boolean isServiceReallyRun(Context context) {
        boolean isRunning = false;

        isRunning = isServiceInRunningList(context);

        if (!isRunning) {
            return false;
        }

        String value = getKV(context, ProviderConst.getContentUri(context), ProviderConst.LAST_NOTIFY);
        if (!TextUtils.isEmpty(value)) {
            long lastRunningTime = CommonUtil.parse2Long(value);
            long currentTime = System.currentTimeMillis();
            String compareInfo = "|currentTime=(" + currentTime + ")" + DateUtil.formatTime(currentTime)
                    + "|lastRunningTime=(" + lastRunningTime + ")" + DateUtil.formatTime(lastRunningTime);
            if ((currentTime - lastRunningTime) > (RECORD_PERIOD * FACTOR)) {
                LogUtil.d("时间戳过时，需要重启服务" + compareInfo);
                isRunning = false;
            } else {
                LogUtil.d("服务正在运行中..." + compareInfo);
                isRunning = true;
            }
        } else {
            count++;
            LogUtil.d("获取不到时间戳" + count);
            // 连续三次读取不了时间戳，将准备重启服务
            if (count <= 2) {
                launchAlarmManager(context, 20 * 1000);
                isRunning = true;
            } else {
                count = 0;
                isRunning = false;
            }
        }

        return isRunning;

    }

    public static String gpnsProcessName = null;

    public static String getGpnsProcessName(Context context) {
        if (TextUtils.isEmpty(gpnsProcessName)) {
            gpnsProcessName = PreferenceUtil.getString(context, PreferenceUtil.GPNS_PROCESS_NAME, null);
            LogUtil.d("从文件中获取GPNS进程名称：" + gpnsProcessName);
        }
        return gpnsProcessName;
    }

    public static void setGpnsProcessName(String packageName) {
        gpnsProcessName = packageName;
        PreferenceUtil.saveString(PreferenceUtil.GPNS_PROCESS_NAME, gpnsProcessName);
    }

    public static void killGpnsService(Context context) {
        String gpnsProcessName = getPakageName();
        if (TextUtils.isEmpty(gpnsProcessName)) {
            LogUtil.d("杀进程失败：要杀进程，必须先设置进程名称");
            // throw new RuntimeException(msg);
        } else {
            LogUtil.d("去杀掉进程：" + gpnsProcessName);
            // 清除掉原进程
            ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);

            List<RunningServiceInfo> runningServiceList = manager.getRunningServices(Integer.MAX_VALUE);

            for (RunningServiceInfo serviceInfo : runningServiceList) {
                if (GPNSService.class.getName().equals(serviceInfo.service.getClassName())
                        && serviceInfo.process.equals(getGpnsProcessName(context))) {
                    stopGpnsService(appContext);
                    int pid = serviceInfo.pid;

                    // if(gpnsProcessName.equals(LoggerUtil.getProcessName())){
                    // 20秒后检测服务是否存活，确认重启成功；
                    launchAlarmManager(context, 20000);
                    // }

                    android.os.Process.killProcess(pid);
                    LogUtil.d("杀掉旧进程：" + gpnsProcessName + "(pid=" + pid + ",uid=" + serviceInfo.uid + ")");
                    return;
                } else {
                    continue;
                }
            }
            LogUtil.d("旧进程已经不存在：" + gpnsProcessName);
        }
    }

    /*****************************************************************/

    private static String loginUri = "/1/device/login?";
    public static String baseUrl = "http://open-dev.gpsoo.net";// 开发环境(现在的)

    public static String getLoginUrl(String accoutId, String appId, int secondTime, String cid) {
        StringBuilder params = new StringBuilder();
        params.append("appid=").append(appId);
        params.append("&account=").append(accoutId);
        params.append("&time=").append(secondTime);
        params.append("&cid=").append(cid);
        params.append("&signature=").append(MD5.hexdigest(secondTime + ""));
        params.append("&access_type=inner");
        return baseUrl + loginUri + params.toString();
    }

    /**
     * 登录接口
     * 
     * @param accoutId
     * @param appId
     * @param secondTime
     * @param listener
     */
    @SuppressLint("NewApi")
    public static void login(final String accoutId, final String appId, final int secondTime, final String cid,
            final ResultListener<String> listener) {
        String url = getLoginUrl(accoutId, appId, secondTime, cid);
        LogUtil.i("url=" + url);

        StringRequest request = new StringRequest(url, new Listener<String>() {
            @Override
            public void onResponse(String response) {
                LogUtil.i("login:" + response);
                try {
                    JSONObject object = new JSONObject(response);
                    String suc = object.getString("success");
                    if (suc.equals("true")) {
                        if (listener != null) {
                            listener.onSuccessed("登录成功");
                            // PreferenceUtil.commitString(appIdKey, appId);
                            // PreferenceUtil.commitString(devIdKey, accoutId);
                        }
                    } else {
                        if (listener != null) {
                            listener.onFailed("登录失败");
                        }
                    }
                } catch (JSONException e) {
                    if (listener != null) {
                        listener.onFailed("Json解析错误:" + e.toString());
                    }
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (error instanceof TimeoutError) {
                    LogUtil.showMsg("网络超时");
                }
                FileOperationUtil.saveExceptionInfoToFile("login error:" + error);
                if (listener != null) {
                    listener.onFailed(error.toString());
                }
            }
        });
        ConnectionUtil.requestHttpData(request);
    }

    public static final String MONITOR_ACTION = "monitor";

    @SuppressLint("NewApi")
    /**
     * gpns服务的守护者：保证gpns服务一直在运行。 （为了省电，用alarmManager代替wakeLock）
     * 用AlarmManager定时检测是否存活
     */
    public static void launchAlarmManager(Context context) {
        launchAlarmManager(context, CHECK_PERIOD);
    }

    @SuppressLint("NewApi")
    public static void launchAlarmManager(Context context, long delayMills) {
        LogUtil.d("launch one-time AlarmManager" + delayMills);
        Intent intent = new Intent(context, MyBroadcastReceiver.class);
        intent.setAction(MONITOR_ACTION);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        AlarmManager alarmMgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        // 为了保证准时，弃用setRepeating，改用one-time exact alarms
        long triggerTime = System.currentTimeMillis() + delayMills;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            alarmMgr.setExact(AlarmManager.RTC, triggerTime, pendingIntent);
        } else {
            alarmMgr.set(AlarmManager.RTC, triggerTime, pendingIntent);
        }
    }

    /*
     * public static void registerReceiverAndSendBroadcast(Context context){
     * LogUtil.e("registerReceiverAndSendBroadcast"); MyBroadcastReceiver
     * receiver = new MyBroadcastReceiver(); IntentFilter filter = new
     * IntentFilter(); filter.addAction(MONITOR_ACTION);
     * filter.addAction("android.intent.action.BOOT_COMPLETED");
     * context.registerReceiver(receiver, filter); Intent intent = new
     * Intent(context, GPNSService.class); intent.setAction(MONITOR_ACTION);
     * context.sendBroadcast(intent); }
     */

    public static boolean writeKV(Context context, Uri uri, String key, String value) {
        boolean result = false;
        try {
            long startTime = System.currentTimeMillis();
            String[] projection = new String[] { key };
            ContentResolver cr = context.getContentResolver();
            Cursor c = cr.query(uri, projection, null, null, null);

            if (c != null) {
                ProviderDBHelper dbHelper = new ProviderDBHelper(context);
                if (c.getCount() == 0) {
                    // myProvider.insert(TimeStampCnst.CONTENT_URI, values);
                    result = dbHelper.insert(key, value);
                } else {
                    // values.put(key, timestamp);
                    // myProvider.update(TimeStampCnst.CONTENT_URI, values,
                    // null, null);
                    result = dbHelper.update(key, value);
                }
                dbHelper.close();
                dbHelper = null;
                c.close();
                c = null;
            }
            long consumeTime = System.currentTimeMillis() - startTime;
            if (consumeTime > 1000) {
                FileOperationUtil.saveExceptionInfoToFile("write " + key + "总耗时：" + consumeTime);
            }
        } catch (Exception e) {
            LogUtil.e("write " + key + "失败：" + e.getMessage());
            FileOperationUtil.saveErrMsgToFile("write " + key + " occur an exception:" + e.toString());
        }
        return result;
    }

    public static String getKV(Context context, Uri uri, String key) {
        String value = null;
        try {
            String[] projection = new String[] { key };
            ContentResolver cr = context.getContentResolver();
            Cursor c = cr.query(uri, projection, null, null, null);
            String msg = null;
            if (c != null) {
                if (c.moveToNext()) {
                    int index = c.getColumnIndex(key);
                    if (index == -1) {
                        msg = "读取失败:不存在" + key + "字段";
                    } else {
                        value = c.getString(index);
                        msg = "读取" + key;// :("+value
                                         // +")"+DateUtil.formatTime(value);
                    }
                } else {
                    msg = key + "读取失败：查询结果为空";
                }
                c.close();
                c = null;
            } else {
                msg = key + "读取失败：cursor为Null";
            }
            LogUtil.d(msg);

            return value;
        } catch (Exception e) {
            String errMsg = "getKV occur an exception:" + e.toString();
            FileOperationUtil.saveErrMsgToFile(errMsg);
        }
        return value;
    }

    public static void setPushNotificationBuilder(GPNSNotificationBuilder notiBuilder) {
        // LogUtil.e("notiBuilder="+notiBuilder.toString());
        NotifyManager.notiBuilder = new GPNSNotificationBuilder(notiBuilder.appName, notiBuilder.statusBarIcon);
        setNotificationSound(appContext, notiBuilder.audioUri);
    }

    public static boolean setNotificationSound(Context context, Uri audioUri) {
        if (null == audioUri) {
            LogUtil.e("audioUri is null");
            return false;
        } else {
            return writeKV(context, ProviderConst.getContentUri(context), ProviderConst.AUDIO_URI, audioUri.toString());
        }
    }

    public static String getPakageName() {
        String pkgName = "com.goome.gpns.GpnsSDK";
        try {
            pkgName = appContext.getPackageName();
        } catch (Exception e) {
            // 引起死循环
            // FileOperationUtil.saveExceptionInfoToFile("getPakageName:" +
            // e.getMessage());
            e.printStackTrace();
        }
        return pkgName;
    }

    public static synchronized void onActive() {
        Intent intentService = new Intent(appContext, GPNSService.class);
        intentService.putExtra("APP_ACTIVE", 1);
        appContext.startService(intentService);
    }

    public static synchronized void onDeactive() {
        Intent intentService = new Intent(appContext, GPNSService.class);
        intentService.putExtra("APP_ACTIVE", 0);
        appContext.startService(intentService);
    }

}
