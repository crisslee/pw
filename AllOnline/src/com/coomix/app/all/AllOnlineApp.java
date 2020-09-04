package com.coomix.app.all;

import android.annotation.TargetApi;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.StrictMode;
import android.support.multidex.MultiDex;
import android.util.Log;
import com.amap.api.location.AMapLocation;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.bumptech.glide.request.target.ViewTarget;
import com.coomix.app.all.common.Keys;
import com.coomix.app.all.gpns.GPNSReceiver;
import com.coomix.app.all.log.AppConfigs;
import com.coomix.app.all.log.GLog;
import com.coomix.app.all.log.GoomeLog;
import com.coomix.app.all.log.GoomeLogErrorCode;
import com.coomix.app.all.manager.DomainManager;
import com.coomix.app.all.manager.SettingDataManager;
import com.coomix.app.all.manager.ThemeManager;
import com.coomix.app.all.model.bean.LocationInfo;
import com.coomix.app.all.model.bean.Token;
import com.coomix.app.all.model.db.SQLHelper;
import com.coomix.app.all.model.response.RespServiceProvider;
import com.coomix.app.all.service.AllOnlineApiClient;
import com.coomix.app.all.service.AllOnlineDatabase;
import com.coomix.app.all.service.LocationService;
import com.coomix.app.all.tinker.TinkerManager;
import com.coomix.app.all.ui.update.GoomeUpdateInfo;
import com.coomix.app.all.util.FileUtil;
import com.coomix.app.all.util.ResizeUtil;
import com.coomix.app.framework.app.BaseApiClient;
import com.coomix.app.framework.util.CommonUtil;
import com.coomix.app.framework.util.OSUtil;
import com.coomix.app.framework.util.PreferenceUtil;
import com.coomix.security.Security;
import com.goome.gpns.GPNSInterface;
import com.goome.gpns.crashhandler.CrashHandler;
import com.goomeim.controller.GMImManager;
import com.meituan.android.walle.WalleChannelReader;
import com.tencent.bugly.Bugly;
import com.tencent.bugly.BuglyStrategy;
import com.tencent.bugly.crashreport.CrashReport;
import com.tencent.mars.BaseEvent;
import com.tencent.mars.Mars;
import com.tencent.tinker.anno.DefaultLifeCycle;
import com.tencent.tinker.entry.DefaultApplicationLike;
import com.tencent.tinker.loader.shareutil.ShareConstants;
import com.umeng.analytics.MobclickAgent;
import com.umeng.commonsdk.UMConfigure;
import com.umeng.socialize.PlatformConfig;
import java.util.HashMap;
import java.util.List;
import net.goome.im.GMCallBack;
import net.goome.im.GMError;
import net.goome.im.chat.GMClient;

/**
 * ApplicationLike
 */
@SuppressWarnings("unused")
@DefaultLifeCycle(application = "com.coomix.app.all.AllApplication", flags = ShareConstants.TINKER_ENABLE_ALL,
                  loadVerifyFlag = false)
public class AllOnlineApp extends DefaultApplicationLike {
    private static final String TAG = AllOnlineApp.class.getSimpleName();

    //初始化
    static {
        PlatformConfig.setWeixin(Keys.WEIXIN_APP_ID, Keys.WEIXIN_APP_SECRET);
        PlatformConfig.setQQZone(Keys.QQ_ID, Keys.QQ_KEY);
    }

    // 坐标
    public static AMapLocation currentLocation;
    // app升级信息
    public static GoomeUpdateInfo gUpdateInfo;
    public static Token sToken;
    public static String sAccount;
    public static String sPassword;
    // 用于获取子账号设备
    public static String sTarget;
    public static AllOnlineDatabase mDb;
    private static SQLHelper serviceSqlHelper;
    /**
     * app配置信息
     */
    private static AppConfigs config;
    /**
     * application context 对象
     */
    public static Context mApp;
    // gpns通道id
    public static String sChannelID = "";
    private static AllOnlineApp mInstance;
    //离线地图是否有更新
    public static boolean sHasUpdate;
    //public static boolean isBeyondLimit = false;
    public static int customerId;
    public static boolean bMainHasChild = true;
    /**
     * 屏幕宽高
     */
    public static int screenWidth;
    public static int screenHeight;
    /**
     * 弹窗广告宽度，高度
     */
    public static int widthAdWindow;
    public static int  heightAdWindow;

    /**
     * 记录App的启动时间
     */
    private static long iAppStartTime;
    public static AllOnlineApiClient mApiClient;
    private static int sDensity = 0;
    public static RespServiceProvider.ServiceProvider spInfo;
    private Security security = new Security();

    //    /*----------- tinker start ----------------------*/

    public AllOnlineApp(Application application, int tinkerFlags, boolean tinkerLoadVerifyFlag,
        long applicationStartElapsedTime, long applicationStartMillisTime, Intent tinkerResultIntent) {
        super(application, tinkerFlags, tinkerLoadVerifyFlag, applicationStartElapsedTime, applicationStartMillisTime,
            tinkerResultIntent);
    }

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    @Override
    public void onBaseContextAttached(Context base) {
        super.onBaseContextAttached(base);
        // you must install multiDex whatever tinker is installed!
        MultiDex.install(base);

        TinkerManager.setTinkerApplicationLike(this);
        TinkerManager.initFastCrashProtect();
        //should set before tinker is installed
        TinkerManager.setUpgradeRetryEnable(true);

        //installTinker after load multiDex
        //or you can put com.tencent.tinker.** to main dex
        TinkerManager.installTinker(this);
    }

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    public void registerActivityLifecycleCallback(Application.ActivityLifecycleCallbacks callbacks) {
        getApplication().registerActivityLifecycleCallbacks(callbacks);
    }

    //    /*----------- tinker end ------------------------*/

    @Override
    public void onCreate() {
        super.onCreate();
        mApp = getApplication();

        ViewTarget.setTagId(R.id.tag_glide);

        //init configure
        PreferenceUtil.init(mApp);
        AppConfigs config = (AppConfigs) PreferenceUtil.getObj(Constant.PREFERENCE_APP_CONFIG);
        config = config == null ? new AppConfigs() : config;
        AllOnlineApp.setAppConfig(config);

        //百度地图init，必须先于ThemeManager的init.
        SDKInitializer.initialize(mApp);

        //init theme
        ThemeManager.getInstance().init(mApp);

        //初始化域名
        DomainManager.initDomain();

        initSize();

        String curProcessName = getCurProcessName(mApp);
        String packageName = mApp.getPackageName();
        if (packageName.equals(curProcessName)) {
            init();
            initApiClient(curProcessName);
            registerGpnsReceiver();
        } else if ((packageName + ":appWidget").equals(curProcessName)) {
            init();
        } else if ((packageName + ":GpnsService").equals(curProcessName)) {
            // gpnsService进程，什么都不做
        } else {
            init();
        }

        //Mars初始化
        Mars.init(mApp, new Handler(Looper.getMainLooper()));

        // android 7.0系统解决拍照问题
        if(Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN_MR2) {
            StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
            StrictMode.setVmPolicy(builder.build());
            builder.detectFileUriExposure();
        }

    }

    @Override
    public void onTerminate() {
        if (mDb != null) {
            mDb.close();
        }
        if (mInstance != null) {
            mInstance = null;
        }
        if (serviceSqlHelper != null) {
            serviceSqlHelper.close();
        }

        super.onTerminate();
        if (AllOnlineApiClient.httpClient != null) {
            AllOnlineApiClient.httpClient.getConnectionManager().shutdown();
            AllOnlineApiClient.httpClient = null;
        }
        unregisterConn();
        mApp.unregisterReceiver(gpnsReceiver);
    }

    private void initSize() {
        sDensity = mApp.getResources().getDisplayMetrics().densityDpi;
        screenWidth = mApp.getResources().getDisplayMetrics().widthPixels;
        screenHeight = mApp.getResources().getDisplayMetrics().heightPixels;
        if (Constant.IS_DEBUG_MODE) {
            Log.d(TAG, "screenH=" + screenHeight + ", screenW=" + screenWidth + ", density=" + sDensity);
        }

        ResizeUtil.initDeviceParams(mApp);
        /*
         * 以1920 * 1080屏幕为标准，图片尺寸为1184 * 920 计算得到 图片与屏幕间距为padding（左右间距之和）
         * 如果减padding后宽高比小于标准宽高比（920 / 1184）则以宽度为标准计算高度，否则，以高度为标准计算宽度
         * 高度计算时需考虑右上角的关闭按钮（减去两倍高度（上下两倍））2 * 73 * BusOnlineApp.sWidth / 1080
         * 以及广告图到关闭按钮的间距（定义为广告图高度的1/7（上下两倍））2 * 1184 / 7 * BusOnlineApp.sWidth /
         * 1080
         */
        int padding = (1080 - 920) * screenWidth / 1080;
        widthAdWindow = screenWidth - padding;
        heightAdWindow = screenHeight - padding;
        if (heightAdWindow != 0 && (double) widthAdWindow / (double) heightAdWindow < 920d / 1184d) {
            heightAdWindow = widthAdWindow * 1184 / 920;
        } else {
            widthAdWindow = heightAdWindow * 920 / 1184;
        }
    }

    /**
     * 获取当前进程名称
     *
     * @param contex context
     * @return 进程全称
     */
    public static String getCurProcessName(Context contex) {
        int pid = android.os.Process.myPid();
        ActivityManager activityMgr = (ActivityManager) contex.getSystemService(Context.ACTIVITY_SERVICE);
        if (activityMgr != null) {
            List<RunningAppProcessInfo> runningProcessList = activityMgr.getRunningAppProcesses();
            if (runningProcessList != null) {
                for (RunningAppProcessInfo runningProcess : runningProcessList) {
                    if (pid == runningProcess.pid) {
                        return runningProcess.processName;
                    }
                }
            }
        }
        return "";
    }

    private void init() {
        mInstance = this;

        String channel = WalleChannelReader.getChannel(getApplication());
        initBugly(channel);
        initUmeng(channel);

        if (Constant.IS_DEBUG_MODE) {
            CrashHandler crashHandler = CrashHandler.getInstance();
            crashHandler.init();
        }

        GoomeLog.getInstance().init(mApp);

        // 启动上传位置服务
        try {
            Log.i("LocationService", "start LocationService");
            Intent intent = new Intent(mApp, LocationService.class);
            mApp.startService(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }

        mDb = AllOnlineDatabase.getInstance(mApp);

        //本地存储
        sChannelID = channelId(mApp);
        sAccount = PreferenceUtil.getString(Constant.LOGIN_ACCOUNT, "");
        sTarget = sAccount;
        sPassword = PreferenceUtil.getString(Constant.LOGIN_PWD, "");
        sToken = (Token) FileUtil.readObject(FileUtil.getTokenPath(mApp));
        if (sToken == null) {
            sToken = new Token();
        }
        spInfo = (RespServiceProvider.ServiceProvider) FileUtil.readObject(FileUtil.getSpPath(mApp));

        SettingDataManager setManager = SettingDataManager.getInstance(mApp);
        setManager.init();

        //当做加密库的第一次初始化，否则可能出现加密库中的decode函数的崩溃
        security.hashProcess("init", BaseApiClient.COMMUNITY_COOMIX_VERSION, mApp);
    }

    public Security getSecurity() {
        return security;
    }

    private void initBugly(String channel) {
        //init bugly
        BuglyStrategy strategy = new BuglyStrategy();
        strategy.setAppChannel(channel);
        // 这里实现SDK初始化，appId替换成你的在Bugly平台申请的appId
        // 调试时，将第三个参数改为true
        if (Constant.IS_DEBUG_MODE) {
            Bugly.init(getApplication(), Keys.BUGLY_APP_ID, true, strategy);
        } else {
            Bugly.init(getApplication(), Keys.BUGLY_APP_ID, false, strategy);
        }
        CrashReport.setUserId(OSUtil.getUdid(mApp));
    }

    private void initUmeng(String channel) {
        //common
        UMConfigure.init(mApp, Keys.UMENG_APPKEY, channel, UMConfigure.DEVICE_TYPE_PHONE, null);
        //不用umeng的crash
        MobclickAgent.setCatchUncaughtExceptions(false);
        if (Constant.IS_DEBUG_MODE) {
            UMConfigure.setLogEnabled(true);
        }
        //分享
        //UMShareAPI.get(getApplication());
    }

    public static void setDensity(int density) {
        sDensity = density;
    }

    public static int getDensity() {
        if (sDensity <= 0) {
            if (mApp != null && mApp.getResources() != null && mApp.getResources().getDisplayMetrics() != null) {
                sDensity = mApp.getResources().getDisplayMetrics().densityDpi;
            }
            if(sDensity <= 0) {
                return 480;
            }
        }
        return sDensity;
    }

    public static synchronized String channelId(Context context) {
        SharedPreferences sharedPrefs = context.getSharedPreferences(Constant.PREF_UNIQUE_GPNS_REGID,
            Context.MODE_PRIVATE);
        String uniqueID = sharedPrefs.getString(Constant.PREF_UNIQUE_GPNS_REGID, null);

        return uniqueID;
    }

    public static AllOnlineApp getInstantce() {
        return mInstance;
    }

    /**
     * 获取app配置信息
     */
    public static AppConfigs getAppConfig() {
        if (config == null) {
            try {
                //PreferenceUtil.init(mApp);
                //config = (AppConfigs) PreferenceUtil.getObj(Constant.PREFERENCE_APP_CONFIG);
            } catch (Exception e) {
                if (Constant.IS_DEBUG_MODE) {
                    e.printStackTrace();
                }
            }
            if (config == null) {
                config = new AppConfigs();
            }
        }
        return config;
    }

    /**
     * 设置app配置信息
     */
    public static void setAppConfig(AppConfigs appConfigs) {
        config = appConfigs;
        if (config != null) {
            GoomeLog.getInstance().setLogLevel(config.getLogLevel());
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    private String getFileMethodLine() {
        String str = "";
        str += "File: " + Thread.currentThread().getStackTrace()[2].getFileName();//文件名
        str += ",Method: " + Thread.currentThread().getStackTrace()[2].getMethodName();//函数名
        str += ",Line: " + Thread.currentThread().getStackTrace()[2].getLineNumber();//行号

        return str;
    }

    /**
     * App第一个界面Oncreate的时候调用，设置，表示App开始启动的时间
     */
    public static void setAppStartTime(long time) {
        iAppStartTime = time;
    }

    public static long getAppStartTime() {
        if (iAppStartTime <= 0) {
            iAppStartTime = System.currentTimeMillis();
        }
        return iAppStartTime;
    }

    /** 获取数据库Helper */
    public static SQLHelper getSQLHelper() {
        if (serviceSqlHelper == null) {
            serviceSqlHelper = new SQLHelper(mApp);
        }
        return serviceSqlHelper;
    }

    /**
     * @param currentLocation the currentLocation to set
     */
    public static void setCurrentLocation(AMapLocation currentLocation) {
        if(currentLocation != null && SettingDataManager.getInstance(mApp).getMapTypeInt() == Constant.MAP_TYPE_BAIDU){
            //我得到位置是用高德地图去定位拿到的
            double[] my = CommonUtil.huoxingToBaidu(currentLocation.getLatitude(), currentLocation.getLongitude());
            currentLocation.setLatitude(my[0]);
            currentLocation.setLongitude(my[1]);
        }
        AllOnlineApp.currentLocation = currentLocation;
    }

    /**
     * @return the currentLocation
     */
    public static AMapLocation getCurrentLocation() {
        if (currentLocation == null) {
            AMapLocation loc = new AMapLocation("null");
            loc.setLatitude(0);
            loc.setLongitude(0);
            loc.setLocationType(-1);
            return loc;
        }
        return currentLocation;
    }

    private void initApiClient(String processName) {
        try {
            mApiClient = new AllOnlineApiClient(mApp);
        } catch (Exception e) {
            GoomeLog.getInstance().logE(getFileMethodLine(), "ProcessName " + processName + "exception:"
                + CommonUtil.getStackTrace(e), GoomeLogErrorCode.ERROR_CODE_OTHER_INIT_ERROR);
        }
    }

    /**
     * mars connection receiver
     */
    private static BaseEvent.ConnectionReceiver connReceiver = null;

    public static void registerConn() {
        if (connReceiver == null) {
            connReceiver = new BaseEvent.ConnectionReceiver();
            IntentFilter filter = new IntentFilter();
            filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
            mApp.registerReceiver(connReceiver, filter);
        }
    }

    public static void unregisterConn() {
        if (connReceiver != null) {
            mApp.unregisterReceiver(connReceiver);
        }
    }

    public interface OnLocationChangeListener {
        void onLocationChange(AMapLocation location);
    }

    private static HashMap<Integer, OnLocationChangeListener> locationListeners = new HashMap<>();

    public static void registerLocationChangeListener(Integer hashcode, OnLocationChangeListener listener) {
        locationListeners.put(hashcode, listener);
    }

    public static void unregisterLocationChangeListener(Integer hashcode) {
        locationListeners.remove(hashcode);
    }

    private LocationInfo mSelectLocationInfo;

    public void setSelectLocationInfo(LocationInfo locationInfo) {
        mSelectLocationInfo = locationInfo;
    }

    public LocationInfo getSelectLocationInfo() {
        return mSelectLocationInfo;
    }

    public static void loginGMIm(Context context) {
        //体验账户不登录im，过华为绿标
        if (!CommonUtil.isLogin()) {
            return;
        }
        GLog.i("GMIM", "loginGMIm");
        GMImManager.getInstance().init(context, null, "4.5.9");

        if (GMClient.getInstance().hasLogin()) {
            return;
        }
        GLog.i("GMIM", "im login");
        GMClient.getInstance().login(sToken.community_id,
                CommonUtil.getTicket(), new GMCallBack() {
                @Override
                public void onSuccess() {
                    //登录成功
                    GLog.i("GMIM", "im login success");
                    GMImManager.getInstance().requestGmImDistublist(0);
                }

                @Override
                public void onError(GMError gmError) {
                    GoomeLog.getInstance().logE("GMIM", "im login fail: " + gmError.errCode(), -1);
                }

                @Override
                public void onProgress(int i, String s) {
                }
            });
    }

    public static void logoutGMIM() {
        if(!GMClient.getInstance().hasLogin()) {
            return;
        }
        GMClient.getInstance().logout(true);
    }

    public static void clearLoginInfo() {
        sAccount = "";
        sPassword = "";
        sTarget = "";
        sToken = new Token();
        FileUtil.deleteTokenFile(mApp);
        PreferenceUtil.commitString(Constant.LOGIN_ACCOUNT, "");
        PreferenceUtil.commitString(Constant.LOGIN_PWD, "");
    }

    private GPNSReceiver gpnsReceiver = new GPNSReceiver();

    private void registerGpnsReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(GPNSInterface.ACTION_CHANNEL_ID);
        filter.addAction(GPNSInterface.ACTION_NOTIFICATION_OPENED);
        filter.addAction(GPNSInterface.ACTION_MESSAGE_RECEIVED);
        mApp.registerReceiver(gpnsReceiver, filter);
    }
}
