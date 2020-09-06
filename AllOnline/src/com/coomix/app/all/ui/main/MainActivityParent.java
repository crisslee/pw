package com.coomix.app.all.ui.main;

import android.Manifest;
import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.text.Spannable;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;
import com.amap.api.location.AMapLocation;
import com.baidu.location.BDLocation;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.geocode.GeoCodeResult;
import com.baidu.mapapi.search.geocode.GeoCoder;
import com.baidu.mapapi.search.geocode.OnGetGeoCoderResultListener;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeOption;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeResult;
import com.baidu.mapapi.utils.CoordinateConverter;
import com.bumptech.GlideApp;
import com.coomix.app.all.AllOnlineApp;
import com.coomix.app.all.Constant;
import com.coomix.app.all.GlobalParam;
import com.coomix.app.all.R;
import com.coomix.app.all.data.BaseSubscriber;
import com.coomix.app.all.data.DataEngine;
import com.coomix.app.all.data.ExceptionHandle;
import com.coomix.app.all.data.RxUtils;
import com.coomix.app.all.log.AppConfigs;
import com.coomix.app.all.log.GoomeLog;
import com.coomix.app.all.manager.CmdManager;
import com.coomix.app.all.manager.DeviceManager;
import com.coomix.app.all.manager.DomainManager;
import com.coomix.app.all.manager.MapIconManager;
import com.coomix.app.all.manager.SettingDataManager;
import com.coomix.app.all.manager.ThemeManager;
import com.coomix.app.all.markColection.baidu.ClusterDevice;
import com.coomix.app.all.model.bean.DevImeiId;
import com.coomix.app.all.model.bean.DevPowerMode;
import com.coomix.app.all.model.bean.DeviceInfo;
import com.coomix.app.all.model.bean.Fence;
import com.coomix.app.all.model.bean.Notice;
import com.coomix.app.all.model.bean.ThemeColor;
import com.coomix.app.all.model.response.Cmd;
import com.coomix.app.all.model.response.RespAddress;
import com.coomix.app.all.model.response.RespAlarmCount;
import com.coomix.app.all.model.response.RespBase;
import com.coomix.app.all.model.response.RespDeviceList;
import com.coomix.app.all.model.response.RespGoogleAddress;
import com.coomix.app.all.model.response.RespNotice;
import com.coomix.app.all.model.response.RespQueryFence;
import com.coomix.app.all.model.response.RespResponse;
import com.coomix.app.all.model.response.RespSendCmd;
import com.coomix.app.all.model.response.RespTypeCmds;
import com.coomix.app.all.model.response.RespUpdateInfo;
import com.coomix.app.all.model.response.SendCmd;
import com.coomix.app.all.model.response.TypeCmd;
import com.coomix.app.all.performReport.PerformanceReportManager;
import com.coomix.app.all.service.AllOnlineAppService;
import com.coomix.app.all.service.CheckVersionService;
import com.coomix.app.all.service.LocationService;
import com.coomix.app.all.service.OfflineMapService;
import com.coomix.app.all.share.PopupWindowUtil;
import com.coomix.app.all.share.TextSet;
import com.coomix.app.all.share.UmengShareUtils;
import com.coomix.app.all.ui.advance.DeviceSettingActivity;
import com.coomix.app.all.ui.alarm.AlarmCategoryListActivity;
import com.coomix.app.all.ui.audioRecord.AudioRecordingActivity;
import com.coomix.app.all.ui.base.BaseActivity;
import com.coomix.app.all.ui.cardRecharge.CardRechargeActivity;
import com.coomix.app.all.ui.cardRecharge.NoticeRechargeActivity;
import com.coomix.app.all.ui.carlist.AllListActivity;
import com.coomix.app.all.ui.detail.DeviceDetailInfoActivity;
import com.coomix.app.all.ui.history.SelectDateHistoryActivity;
import com.coomix.app.all.ui.installer.UploadInfoActivity;
import com.coomix.app.all.ui.mine.MineActivity;
import com.coomix.app.all.ui.platformRecharge.PlatRechargeActivity;
import com.coomix.app.all.ui.update.GoomeUpdateAgent;
import com.coomix.app.all.ui.update.GoomeUpdateInfo;
import com.coomix.app.all.ui.web.BusAdverActivity;
import com.coomix.app.all.util.AlarmCategoryUtils;
import com.coomix.app.all.util.CommunityUtil;
import com.coomix.app.all.util.CoordinateUtil;
import com.coomix.app.all.util.GpnsUtil;
import com.coomix.app.all.util.PermissionUtil;
import com.coomix.app.all.widget.BatteryState;
import com.coomix.app.all.widget.BottomDrawer;
import com.coomix.app.framework.util.CommonUtil;
import com.coomix.app.framework.util.OSUtil;
import com.coomix.app.framework.util.TimeUtil;
import com.google.gson.Gson;
import com.google.zxing.activity.CaptureActivity;
import com.google.zxing.util.ZConstant;
import com.inuker.bluetooth.library.BluetoothClient;
import com.inuker.bluetooth.library.beacon.Beacon;
import com.inuker.bluetooth.library.connect.listener.BleConnectStatusListener;
import com.inuker.bluetooth.library.connect.options.BleConnectOptions;
import com.inuker.bluetooth.library.connect.response.BleConnectResponse;
import com.inuker.bluetooth.library.connect.response.BleNotifyResponse;
import com.inuker.bluetooth.library.connect.response.BleWriteResponse;
import com.inuker.bluetooth.library.model.BleGattCharacter;
import com.inuker.bluetooth.library.model.BleGattProfile;
import com.inuker.bluetooth.library.model.BleGattService;
import com.inuker.bluetooth.library.search.SearchRequest;
import com.inuker.bluetooth.library.search.response.SearchResponse;
import com.inuker.bluetooth.library.utils.BluetoothLog;
import com.leethink.badger.BadgeUtil;
import com.tencent.tinker.lib.tinker.Tinker;
import com.tencent.tinker.lib.tinker.TinkerInstaller;
import com.umeng.analytics.MobclickAgent;
import io.reactivex.disposables.Disposable;
import java.io.File;
import java.lang.ref.WeakReference;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import net.goome.im.GMError;
import net.goome.im.GMValueCallBack;
import net.goome.im.chat.GMChatRoom;
import net.goome.im.chat.GMChatRoomManager;
import net.goome.im.chat.GMClient;
import okhttp3.MediaType;
import okhttp3.RequestBody;

import static com.inuker.bluetooth.library.Code.REQUEST_SUCCESS;
import static com.inuker.bluetooth.library.Constants.STATUS_CONNECTED;
import static com.inuker.bluetooth.library.Constants.STATUS_DEVICE_CONNECTED;

public abstract class MainActivityParent extends BaseActivity implements OnClickListener {
    public static final String IMEI = "imei";
    protected static final int MSG_START_REFRESH = 2;
    protected static final int REFRESH_SINGLE = 3;
    protected static final int OPEN_TEMP_CMD = 4;
    protected static final int PARAM_CMD = 5;
    protected static final int GET_RESPONSE = 6;
    private static final long RESP_DELAY = 6 * 1000;
    private static final int REQ_SCAN_BIND = 10000;
    public static boolean isAlive = false;
    private static BluetoothClient mBlueToothClient;
    //把地图添加到这个layout中
    protected RelativeLayout layoutMapView = null;
    private final int ACTIVITY_LIST_REQUEST_CODE = 10;
    //设备安装后的天线与水平面的夹角临界
    private final int ANGLE_NORMAL_MAX = 45;
    private final int ANGLE_WARNING_MAX = 90;

    private long clickedTime = 0;
    protected Handler mHandler;
    // 聚合
    protected int mGridSize = 210;
    protected final ReadWriteLock mClusterTaskLock = new ReentrantReadWriteLock();
    protected TextView popName, popState, popAddress;
    private TextView textAlarmCount;
    private ToggleButton toggleButtonSet;
    private TextView textOneKey;
    //录音
    private RelativeLayout rlRecord;
    private ImageView ivRecord;
    private TextView tvRecord;

    protected View powerLL;
    private TextView popPower;

    //电动车电量
    private RelativeLayout rlBattery;
    private BatteryState batteryLevel;
    private TextView batteryPercent;
    //设备安装状态
    private RelativeLayout rlInstall;
    private ImageView imgInstall;
    private TextView tvInstall;
    //gsm,gps信号
    private View signalLine;
    private TextView gpsLevel;
    //电池剩余电量
    private TextView tvBattery;
    //耗电模式
    private RelativeLayout rlPower;
    private TextView powerMode;
    //开锁
    private RelativeLayout rlLock;
    private boolean isOpenLock = false;
    //公告
    private RelativeLayout rlNotice;
    private TextView tvNotice;
    private ImageView closeNotice;
    private Notice notice;

    // 地图缩放级别
    protected float zoomLevel = 17f;
    //搜索页面返回的设备，可能不在设备列表中
    protected DeviceInfo searchDevice;
    protected ArrayList<DeviceInfo> listDevices;
    protected int iCurrentShowDeviceIndex;
    private ImageView imageNavMap, imageMapStreet, user, alarm, list;
    private LinearLayout layoutNoDev;
    private ScreenOnOffReceiver mScreenReceiver;
    private int iMapMode = Constant.MAP_NORMAL;
    protected DeviceInfo currDevice, lastSelectedDevice;
    protected SettingDataManager setManager = null;
    protected View rootView;
    protected LinearLayout layoutBottomCarInfo, layoutNextPre;
    protected DeviceManager deviceManager = null;
    protected CmdManager cmdMgr = CmdManager.INSTANCE;
    private Fence mFence;
    private boolean bSetNoDevCamera = false;
    //获取设备后是否显示
    private boolean showAfterGet = true;
    private String bindImei = null;
    protected TextView textDevTitle;
    private String[] dotText = { ".  ", ".. ", "..." };
    private ValueAnimator recordAni;
    //menu 栏
    private BottomDrawer rlMenu;
    private ImageView divMenu;
    private int divMenuHeight;
    private static final int DURATION = 200;
    private boolean setFromBack = false;
    public String MAC;
    public String imei;
    public UUID targetuuid=null;
    public UUID notifyuuid=null;
    public UUID writeuuid=null;
    public static MainActivityParent instance = null;
    int paramCount;
    // imei --> PowerMode
    private HashMap<String, DevPowerMode> oldMode = new HashMap<>();
    // debug
    private ScrollView sv;
    private LinearLayout llDebug;
    private static final char[] hexArray = "0123456789ABCDEF".toCharArray();
    private final BleConnectStatusListener mBleConnectStatusListener = new BleConnectStatusListener() {
        @Override
        public void onConnectStatusChanged(String mac, int status) {
            if(status == STATUS_CONNECTED){

            }else if(status == STATUS_DEVICE_CONNECTED){

            }
        }
    };

    public class ScreenOnOffReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (Intent.ACTION_SCREEN_OFF.equals(intent.getAction())) {
                stopCountdown();
            } else if (Intent.ACTION_SCREEN_ON.equals(intent.getAction())) {
                startCountdown();
            }
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        deviceManager = DeviceManager.getInstance();

        rootView = LayoutInflater.from(this).inflate(R.layout.activity_main_parent, null);
        setContentView(rootView);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        instance = this;
        mGridSize = getResources().getDimensionPixelSize(R.dimen.cluster_grid_size);
        setManager = SettingDataManager.getInstance(this);
        mHandler = new HandlerEx(this);
        mBlueToothClient = new BluetoothClient(this);
        MobclickAgent.onEvent(this, "ev_function", new HashMap<String, String>().put("ev_function",
            setManager.getMapNameByType() + "地图监控"));

        initViews();

        registerReceiver();
        getAppNotice();

        //防止首页没有初始化成功
        GpnsUtil.initGpns(this, DomainManager.getInstance().getInitGpnsHost());

        getLatestVersion();
        //Tinker 本地测试
        loadTestLocalTinkerPatchAuto();
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onResume() {
        isAlive = true;
        super.onResume();
        startCountdown();
        setThemeIcon();
        if (!setFromBack && layoutBottomCarInfo.getVisibility() == View.VISIBLE) {
            setGoomeDev(currDevice);
        }
        if (setFromBack) {
            setFromBack = false;
        }
        SearchRequest searchRequest = new SearchRequest.Builder()
                .searchBluetoothLeDevice(10000,10)
                .searchBluetoothClassicDevice(1000,10)
                .build();
        mBlueToothClient.search(searchRequest, new SearchResponse() {
            @Override
            public void onSearchStarted() {
                BluetoothLog.v("Bluetooths search start");
            }

            @Override
            public void onDeviceFounded(com.inuker.bluetooth.library.search.SearchResult device) {
                Beacon beacon = new Beacon(device.scanRecord);
                String data = bytesToHex(device.scanRecord);
                String targetMacAddress = "02:00:42:4B:34:31";
                if (targetMacAddress.equalsIgnoreCase(device.getAddress())) {
                    onBlueToothConnected(mBlueToothClient, data, beacon, device);
                }
                BluetoothLog.v(String.format("beacon for %s\n%s", device.getAddress(), beacon.toString()));
            }

            @Override
            public void onSearchStopped() {

            }

            @Override
            public void onSearchCanceled() {

            }
        });
    }


    private static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }



    public static byte[] hexStringToByte(String hexString){
        if (hexString == null || hexString.equals("")) {
            return null;
        }
        hexString = hexString.toUpperCase();
        int length = hexString.length() / 2;
        char[] hexChars = hexString.toCharArray();
        byte[] d = new byte[length];
        for (int i = 0; i < length; i++) {
            int pos = i * 2;
            d[i] = (byte) (charToByte(hexChars[pos]) << 4 | charToByte(hexChars[pos + 1]));
        }
        return d;
    }

    private static byte charToByte(char c) {
        return (byte) "0123456789ABCDEF".indexOf(c);
    }

    private void checkprotocol(String message){
        String header = message.substring(0,4);
        imei = message.substring(4,20);
        String protocol = message.substring(20,22);
        String length = message.substring(22,26);
        String syn = message.substring(26,30);
        String data = message.substring(30);
        String writeChar = "0000ffb1-0000-1000-8000-00805f9b34fb";

        switch(protocol){
            case "01":
                responseLoginData(header,imei,protocol,length,syn,data);
            case "02":
                responseGPSData(data);
            case "04":
                responseAlertData(data);
            case "07":
                responseExpendBeatData(header,imei,protocol,length,syn,data);
            case "08":
                responseTimeData(header,imei,protocol,length,syn,data);
        }
    }

    private void responseLoginData(String header,String imei,String protocol,String length,String syn,String data){
        if(writeuuid!=null&&writeuuid.toString()!=""&&targetuuid!=null&&targetuuid.toString()!="") {
            byte[] writeData = hexStringToByte(header+imei+protocol+"0002"+syn);

            mBlueToothClient.write(MAC, targetuuid, writeuuid, writeData, new BleWriteResponse() {
                @Override
                public void onResponse(int code) {

                }
            });
        }
    }

    private void responseGPSData(String data){

    }

    private void responseAlertData(String data){

    }

    private void responseExpendBeatData(String header,String imei,String protocol,String length,String syn,String data) {
        if (writeuuid != null && writeuuid.toString() != "" && targetuuid != null && targetuuid.toString() != "") {
            byte[] writeData = hexStringToByte(header + imei + protocol + "0002" + syn);

            mBlueToothClient.write(MAC, targetuuid, writeuuid, writeData, new BleWriteResponse() {
                @Override
                public void onResponse(int code) {

                }
            });
        }
    }

    private void responseTimeData(String header,String imei,String protocol,String length,String syn,String data){
        if (writeuuid != null && writeuuid.toString() != "" && targetuuid != null && targetuuid.toString() != "") {
            byte[] writeData = hexStringToByte(header + imei + protocol + "0002" + syn);//UTC時間

            mBlueToothClient.write(MAC, targetuuid, writeuuid, writeData, new BleWriteResponse() {
                @Override
                public void onResponse(int code) {

                }
            });
        }
    }

    public void onBlueToothConnected(BluetoothClient mClient,String data, Beacon beacon, com.inuker.bluetooth.library.search.SearchResult device){
        System.out.println("data + " + data);
        BluetoothLog.v(String.format("data + beacon for %s\n%s", device.getAddress(), beacon.toString()));
        BleConnectOptions options = new BleConnectOptions.Builder()
                .setConnectRetry(3)
                .setConnectTimeout(30)
                .setServiceDiscoverRetry(3)
                .setServiceDiscoverTimeout(20000)
                .build();
        //解析data，獲得uuid等數據
        //MAC獲取
        String deviceAddress = device.getAddress();
        MAC = device.getAddress();
        String targetService = "0000ffb0-0000-1000-8000-00805f9b34fb";
        String notifyChar = "0000ffb2-0000-1000-8000-00805f9b34fb";
        String writeChar = "0000ffb1-0000-1000-8000-00805f9b34fb";

        System.out.println("ddebug Connect to that device " + deviceAddress);
        mClient.connect(device.getAddress(), options, new BleConnectResponse() {
            @Override
            public void onResponse(int code, BleGattProfile data) {
                List<BleGattService> services = data.getServices();
                for (int i = 0; i < services.size(); i++) {
                    BleGattService service = services.get(i);
                    List<BleGattCharacter> characters = service.getCharacters();
                    if (service.getUUID().toString().equalsIgnoreCase(targetService)) {
                        targetuuid = service.getUUID();
                        for (int j = 0; j < characters.size(); j++) {
                            BleGattCharacter character =  characters.get(j);
                            if (character.getUuid().toString().equalsIgnoreCase(writeChar)){
                                writeuuid=character.getUuid();
                            }
                            if (character.getUuid().toString().equalsIgnoreCase(notifyChar)) {
                                notifyuuid = character.getUuid();

                                System.out.println("ddebug find service:" + targetuuid.toString() + " character+" + notifyuuid.toString());
                                if (code == REQUEST_SUCCESS) {
                                    //mClient.requestMtu(device.getAddress(),40, new BleMtuResponse(){})
                                    mClient.notify(device.getAddress(), targetuuid, notifyuuid, new BleNotifyResponse() {
                                        @Override
                                        public void onNotify(UUID service, UUID character, byte[] value) {
                                            String notfiyMessage = bytesToHex(value);
                                            System.out.println("ddebug notify:" + notfiyMessage);
                                            checkprotocol(notfiyMessage);
//                                            6767086820012121320601000B00200868200121;
//                                            mClient.writeNoRsp(device.getAddress(), serviceUUID, characterUUID, bytes, new BleWriteResponse() {
//                                                @Override
//                                                public void onResponse(int code) {
//                                                    if (code == REQUEST_SUCCESS) {
//
//                                                    }
//                                                }
//                                            });
                                        }
                                        @Override
                                        public void onResponse(int code) {
                                        }
                                    });


                                }
                            }
                        }
                    }
                }

            }
        });

        //監聽連接狀態
        //mClient.registerConnectStatusListener(MAC,mBleConnectStatusListener);

    }

    @Override
    public void onPause() {
        super.onPause();
        stopAnim();
        stopCountdown();
        if (layoutBottomCarInfo.getVisibility() == View.VISIBLE) {
            mHandler.removeMessages(OPEN_TEMP_CMD);
            mHandler.removeMessages(REFRESH_SINGLE);
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        stopCountdown();
    }

    @Override
    public void onDestroy() {
        //停止監聽
        //mBlueToothClient.unregisterConnectStatusListener(MAC,mBleConnectStatusListener);
        if (mScreenReceiver != null) {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(mScreenReceiver);
        }
        stopCountdown();
        if (mHandler != null) {
            mHandler.removeCallbacksAndMessages(null);
        }
        searchDevice = null;
        if (deviceManager != null) {
            deviceManager.clearData();
            deviceManager = null;
        }
        super.onDestroy();
        MapIconManager.getInstance().release();
    }

    private void loadTestLocalTinkerPatchAuto() {
        // 如果本地有patch则自动加载测试本地patch
        String patchPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/patch_signed_7zip.apk";
        File patchFile = new File(patchPath);
        if (patchFile.exists()) {
            TinkerInstaller.onReceiveUpgradePatch(getApplicationContext(), patchPath);
        }
    }

    private void registerReceiver() {
        mScreenReceiver = new ScreenOnOffReceiver();
        IntentFilter screenOnOffFilter = new IntentFilter(Intent.ACTION_SCREEN_ON);
        screenOnOffFilter.addAction(Intent.ACTION_SCREEN_OFF);
        LocalBroadcastManager.getInstance(this).registerReceiver(mScreenReceiver, screenOnOffFilter);
    }

    protected void initViews() {
        layoutMapView = (RelativeLayout) findViewById(R.id.layoutMapView);

        imageNavMap = (ImageView) findViewById(R.id.nav_map_change);
        imageNavMap.setOnClickListener(this);
        imageMapStreet = (ImageView) findViewById(R.id.map_street);
        imageMapStreet.setOnClickListener(this);

        user = (ImageView) findViewById(R.id.user_icon);
        alarm = (ImageView) findViewById(R.id.warning_icon);
        list = (ImageView) findViewById(R.id.imageViewList);

        setThemeIcon();

        layoutBottomCarInfo = (LinearLayout) findViewById(R.id.layoutBottomCarInfo);
        layoutBottomCarInfo.setVisibility(View.GONE);
        layoutBottomCarInfo.setAnimation(AnimationUtils.loadAnimation(this, R.anim.fade_in));
        powerMode = findViewById(R.id.powerMode);
        powerMode.setOnClickListener(this);
        rlPower = findViewById(R.id.rlPower);
        rlLock = findViewById(R.id.rlLock);
        rlLock.setOnClickListener(this);
        popName = (TextView) findViewById(R.id.pop_name);
        popState = (TextView) findViewById(R.id.pop_state);
        textOneKey = (TextView) findViewById(R.id.one_key_set);

        //外电信息
        rlBattery = (RelativeLayout) findViewById(R.id.rlBattery);
        rlBattery.setOnClickListener(this);
        batteryLevel = (BatteryState) findViewById(R.id.batteryLevel);
        batteryPercent = (TextView) findViewById(R.id.batteryPercent);

        //电量分割线
        powerLL = findViewById(R.id.power_line);
        popPower = (TextView) findViewById(R.id.pop_acc_state_power);
        popAddress = (TextView) findViewById(R.id.pop_address);
        textAlarmCount = (TextView) findViewById(R.id.textViewWarnCount);
        textAlarmCount.setOnClickListener(this);
        textAlarmCount.setVisibility(View.GONE);

        toggleButtonSet = (ToggleButton) findViewById(R.id.toggleButtonSet);
        layoutNoDev = (LinearLayout) findViewById(R.id.layoutNoDev);
        layoutNoDev.setOnClickListener(this);

        layoutNextPre = (LinearLayout) findViewById(R.id.layoutNextPre);
        //定位器安装角度
        rlInstall = (RelativeLayout)findViewById(R.id.rlInstall);
        imgInstall = (ImageView)findViewById(R.id.imgInstall);
        tvInstall = (TextView)findViewById(R.id.tvInstall);
        //gsm,gps信号
        signalLine = findViewById(R.id.signalLine);
        gpsLevel = (TextView) findViewById(R.id.gpsLevel);
        //电池剩余电量
        tvBattery = (TextView) findViewById(R.id.tvBattery);
        //公告
        rlNotice = findViewById(R.id.rlNotice);
        tvNotice = findViewById(R.id.notice);
        tvNotice.setOnClickListener(this);
        closeNotice = findViewById(R.id.closeNotice);
        closeNotice.setOnClickListener(this);

        findViewById(R.id.detail_btn).setOnClickListener(this);
        findViewById(R.id.senior_setting).setOnClickListener(this);
        findViewById(R.id.route_playback_btn).setOnClickListener(this);
        findViewById(R.id.home_share_tv).setOnClickListener(this);
        findViewById(R.id.previous_dev).setOnClickListener(this);
        findViewById(R.id.next_dev).setOnClickListener(this);
        rlRecord = findViewById(R.id.rlRecord);
        ivRecord = findViewById(R.id.ivRecord);
        tvRecord = (TextView) findViewById(R.id.tvRecord);
        rlRecord.setOnClickListener(this);
        user.setOnClickListener(this);
        alarm.setOnClickListener(this);
        list.setOnClickListener(this);
        rlInstall.setOnClickListener(view -> {
            Object obj = rlInstall.getTag();
            if (obj instanceof DeviceInfo) {
                clickDevAngle((DeviceInfo) obj);
            }
        });
        rlMenu = findViewById(R.id.rlMenu);
        rlMenu.setOnClickListener(v -> {
        });
        divMenu = findViewById(R.id.divMenu);
        divMenu.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                divMenuHeight = divMenu.getHeight();
                if (divMenuHeight > 0) {
                    rlMenu.setKeep(divMenuHeight);
                    divMenu.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                }
            }
        });
        divMenu.setOnClickListener(v -> {
            float ty = rlMenu.getTranslationY();
            if (ty == 0) {
                return;
            }
            ValueAnimator open = ValueAnimator.ofFloat(ty, 0).setDuration(DURATION);
            open.addUpdateListener((ani) -> {
                float curr = (float) ani.getAnimatedValue();
                rlMenu.setTranslationY(curr);
            });
            open.start();
        });

        findViewById(R.id.realBottom).setOnClickListener(v -> {
        });

        //默认关闭状态
        toggleButtonSet.setClickable(false);
        setSwitchIcon(Fence.SWITCH_OFF);
        toggleButtonSet.setOnClickListener(view1 -> {
            if (mFence != null && mFence.getValidate_flag() == Fence.SWITCH_ON
                && mFence.getAlarm_type() == Fence.ALARM_TYPE_ONCE) {
                //关闭时候只需要调用switch接口
                switchFence(currDevice, mFence.getId(), Fence.SWITCH_OFF);
            } else if (mFence != null) {
                //打开时候直接调用set接口
                setFence(currDevice);
            }
        });

        initDevTitleView();

        TextView textNoDev = (TextView)findViewById(R.id.textViewNoDev);
        if(AllOnlineApp.sAccount != null && AllOnlineApp.sAccount.equals(Constant.ACCOUNT_AN_ZHUANG_GONG)){
            //安装工账户，此处是上传安装信息的入口
            CommonUtil.setTextDrawable(this, textNoDev, 0, getString(R.string.upload_info),
                CommonUtil.DRAWABLE_NONE);
        }else{
            CommonUtil.setTextDrawable(this, textNoDev, R.drawable.scan_small,
                getString(R.string.scan_qr_code_bind_dev), CommonUtil.DRAWABLE_LEFT);
        }

        sv = findViewById(R.id.debug);
        llDebug = findViewById(R.id.debugLoc);
        Button showDebug = findViewById(R.id.showDebug);
        showDebug.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (sv.getVisibility() == View.VISIBLE) {
                    showDebug.setText("显示");
                    sv.setVisibility(View.GONE);
                } else {
                    showDebug.setText("隐藏");
                    sv.setVisibility(View.VISIBLE);
                }
            }
        });
        if (Constant.IS_DEBUG_MODE) {
            showDebug.setVisibility(View.GONE);
        } else {
            showDebug.setVisibility(View.GONE);
        }
    }

    private void getAppNotice() {
        Disposable d = DataEngine.getAllMainApi().getNotice(AllOnlineApp.sToken.access_token,
            GlobalParam.getInstance().getCommonParas())
            .compose(RxUtils.businessTransformer())
            .compose(RxUtils.toMain())
            .subscribeWith(new BaseSubscriber<RespNotice>() {
                @Override
                public void onHttpError(ExceptionHandle.ResponeThrowable e) {

                }

                @Override
                public void onNext(RespNotice respNotice) {
                    notice = respNotice.getData();
                    if (!TextUtils.isEmpty(notice.content)) {
                        rlNotice.setVisibility(View.VISIBLE);
                        tvNotice.setText(notice.content);
                        tvNotice.requestFocus();
                    }
                }
            });
        subscribeRx(d);
    }

    private void clickDevAngle(DeviceInfo deviceInfo) {
        //http://lite.gmiot.net/wx/exception=1&type=GM06A_equipError.html，
        // 其中exception=1代表姿态感知异常，水平角度大于45，exception=2代表姿态感知异常，水平角度大于90，type=GM06A代表当前型号，暂时默认为GM06A
        if (deviceInfo != null && deviceInfo.getInstallAngle() > ANGLE_NORMAL_MAX) {
            AppConfigs config = AllOnlineApp.getAppConfig();
            StringBuilder url = new StringBuilder(config.getAngle_exception_url());
            url.append("?&exception=").append(deviceInfo.getInstallAngle() > ANGLE_WARNING_MAX ? 2 : 1);
            url.append("&type=").append(deviceInfo.getDev_type());
            CommunityUtil.switch2WebViewActivity(this, url.toString(), true, "");
        }
    }

    protected void initDevTitleView() {
        textDevTitle = new TextView(this);
        textDevTitle.setBackgroundResource(R.drawable.popup_infowindow_bg);
        textDevTitle.setTextColor(getResources().getColor(R.color.text_black));
        textDevTitle.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.textsize_xxsmall));
        textDevTitle.setMaxLines(1);
        textDevTitle.setMinWidth(getResources().getDimensionPixelSize(R.dimen.space_24x));
        textDevTitle.setGravity(Gravity.CENTER);
    }

    private void setThemeIcon() {
        ThemeColor theme = ThemeManager.getInstance().getThemeAll().getThemeColor();
        if (!TextUtils.isEmpty(theme.getProfile())) {
            GlideApp.with(this)
                    .load(theme.getProfile())
                    .placeholder(R.drawable.mine_icon)
                    .error(R.drawable.mine_icon)
                    .into(user);
        }
        if (!TextUtils.isEmpty(theme.getAlarm())) {
            GlideApp.with(this)
                    .load(theme.getAlarm())
                    .placeholder(R.drawable.alarm_icon)
                    .error(R.drawable.alarm_icon)
                    .into(alarm);
        }
        if (!TextUtils.isEmpty(theme.getList())) {
            GlideApp.with(this)
                    .load(theme.getList())
                    .placeholder(R.drawable.list_icon_cn)
                    .error(R.drawable.list_icon_cn)
                    .into(list);
        } else {
            list.setImageResource(setManager.getListDrawableId());
        }
        setSatelliteIcon();
        if (!TextUtils.isEmpty(theme.getStreet())) {
            GlideApp.with(this)
                    .load(theme.getStreet())
                    .placeholder(R.drawable.nav_panorama_rcn)
                    .error(R.drawable.nav_panorama_rcn)
                    .into(imageMapStreet);
        } else {
            imageMapStreet.setImageResource(setManager.getStreetDrawableId());
        }
    }

    private void setSatelliteIcon() {
        ThemeColor theme = ThemeManager.getInstance().getThemeAll().getThemeColor();
        if (iMapMode == Constant.MAP_NORMAL) {
            if (!TextUtils.isEmpty(theme.getSatellite())) {
                GlideApp.with(this)
                        .load(theme.getSatellite())
                        .placeholder(R.drawable.nav_more_map_normal_rcn)
                        .error(R.drawable.nav_more_map_normal_rcn)
                        .into(imageNavMap);
            } else {
                imageNavMap.setImageResource(setManager.getMapTypeNormalDrawableId());
            }
        } else {
            if (!TextUtils.isEmpty(theme.getSatelliteTouched())) {
                GlideApp.with(this)
                        .load(theme.getSatelliteTouched())
                        .placeholder(R.drawable.nav_more_map_press_rcn)
                        .error(R.drawable.nav_more_map_press_rcn)
                        .into(imageNavMap);
            } else {
                imageNavMap.setImageResource(setManager.getMapTypeSatDrawableId());
            }
        }
    }

    private void getDeviceList(final boolean bShowWait) {
        if (bShowWait) {
            showLoading(getString(R.string.please_wait));
        }
        Disposable d = DataEngine.getAllMainApi().getDeviceList(GlobalParam.getInstance().getCommonParas(),
            AllOnlineApp.sAccount, AllOnlineApp.sTarget, AllOnlineApp.sToken.access_token)
            .compose(RxUtils.toMain())
            .compose(RxUtils.businessTransformer())
            .subscribeWith(new BaseSubscriber<RespDeviceList>() {
                @Override
                public void onNext(RespDeviceList respDeviceList) {
                    if (bShowWait) {
                        hideLoading();
                    }
                    deviceManager.setCurrentAccountDeviceList(respDeviceList.getData());
                    //过滤一次得到“可用”的设备列表，去掉未启用的设备，在地图上不显示
                    listDevices = deviceManager.getValidDeviceList();
                    if (searchDevice != null && !listDevices.contains(searchDevice)) {
                        listDevices.add(searchDevice);
                    }

                    updateCurrentDeviceView();
                    addDevicesOnMap();

                    if (respDeviceList.getData() == null || respDeviceList.getData().size() <= 0) {
                        mmoveToNowLocation(respDeviceList.getDefaultpos());
                        imageMapStreet.setVisibility(View.GONE);
                        layoutNoDev.setVisibility(View.VISIBLE);
                        hideBottomCarInfo();
                    } else {
                        bSetNoDevCamera = true;
                        layoutNoDev.setVisibility(View.GONE);
                        imageMapStreet.setVisibility(View.VISIBLE);
                    }
                }

                @Override
                public void onHttpError(ExceptionHandle.ResponeThrowable e) {
                    if (bShowWait) {
                        hideLoading();
                    }
                    if (listDevices == null || listDevices.size() <= 0) {
                        layoutNoDev.setVisibility(View.VISIBLE);
                        imageMapStreet.setVisibility(View.GONE);
                        hideBottomCarInfo();
                    }
                }
            });
        subscribeRx(d);
    }

    private void mmoveToNowLocation(final RespDeviceList.DefaultPos defaultPos) {
        if (bSetNoDevCamera) {
            return;
        }
        //没有设备，地图视角切换到当前位置;如果当前位置没有定位到，则视图移动到后台返回的地址;如果后台没有返回，则切换到默认地址
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                double noDevLat;
                double noDevLng;
                bSetNoDevCamera = true;

                if (AllOnlineApp.currentLocation != null && AllOnlineApp.currentLocation.getLatitude() != 0
                    && AllOnlineApp.currentLocation.getLongitude() != 0) {
                    noDevLat = AllOnlineApp.currentLocation.getLatitude();
                    noDevLng = AllOnlineApp.currentLocation.getLongitude();
                } else if (defaultPos != null) {
                    noDevLat = defaultPos.lat;
                    noDevLng = defaultPos.lng;
                } else {
                    noDevLat = Constant.DEFAULT_LAT;
                    noDevLng = Constant.DEFAULT_LNG;
                }
                animateCamera(noDevLat, noDevLng, 17f);
            }
        }, 1000);
    }

    private void queryFence(final DeviceInfo deviceInfo) {
        Disposable disposable = DataEngine.getAllMainApi().queryFence(
            GlobalParam.getInstance().getCommonParas(),
            GlobalParam.getInstance().getAccessToken(),
            AllOnlineApp.sAccount, deviceInfo.getImei(), "")
            .compose(RxUtils.toMain())
            .compose(RxUtils.businessTransformer())
            .subscribeWith(new BaseSubscriber<RespQueryFence>() {
                @Override
                public void onHttpError(ExceptionHandle.ResponeThrowable e) {
                    //showErr(e.getErrCodeMessage());
                    //hideLoading();
                }

                @Override
                public void onNext(RespQueryFence respQueryFence) {
                    if (deviceInfo.equals(currDevice)) {
                        //请求成功则按钮即可点击状态
                        toggleButtonSet.setClickable(true);

                        mFence = respQueryFence.getData();
                        if (mFence != null && mFence.getAlarm_type() == Fence.ALARM_TYPE_ONCE) {
                            if (mFence.getValidate_flag() == Fence.SWITCH_ON) {
                                addFenceOnMap(mFence, false);
                            } else {
                                removeFenceOnMap();
                            }
                            setSwitchIcon(mFence.getValidate_flag());
                        } else {
                            //如果返回的数据是空的或是其他类型的围栏，则默认设置到关闭状态
                            setSwitchIcon(Fence.SWITCH_OFF);
                            removeFenceOnMap();
                        }
                    }
                }
            });
        subscribeRx(disposable);
    }

    private void switchFence(final DeviceInfo deviceInfo, String fenceId, final int switchFlag) {
        if (deviceInfo == null || TextUtils.isEmpty(fenceId)) {
            return;
        }
        showProgressDialog(getString(R.string.please_wait));
        Disposable disposable = DataEngine.getAllMainApi()
            .switchFence(GlobalParam.getInstance().getCommonParas(), AllOnlineApp.sAccount,
                GlobalParam.getInstance().getAccessToken(), deviceInfo.getImei(), fenceId, switchFlag,
                Fence.ALARM_TYPE_ONCE)
            .compose(RxUtils.toMain())
            .compose(RxUtils.businessTransformer())
            .subscribeWith(new BaseSubscriber<RespBase>() {
                @Override
                public void onNext(RespBase respBase) {
                    hideLoading();
                    removeFenceOnMap();
                    if (deviceInfo.equals(currDevice)) {
                        setSwitchIcon(switchFlag);
                        mFence.setValidate_flag(switchFlag);
                        if (switchFlag == Fence.SWITCH_OFF) {
                            showToast(R.string.switch_fence_off_toast, Toast.LENGTH_SHORT);
                        } else {
                            showToast(R.string.switch_fence_on_toast, Toast.LENGTH_SHORT);
                        }
                    }
                }

                @Override
                public void onHttpError(ExceptionHandle.ResponeThrowable e) {
                    showToast(e.getErrCodeMessage());
                    hideLoading();
                }
            });
        subscribeRx(disposable);
    }

    private void setFence(final DeviceInfo deviceInfo) {
        if (deviceInfo == null) {
            return;
        }
        final String shapeParam = deviceInfo.getLat() + "," + deviceInfo.getLng() + "," + setManager.getFenceRange();
        showProgressDialog(getString(R.string.loading_set_fence));
        Disposable disposable = DataEngine.getAllMainApi().addFence(
            GlobalParam.getInstance().getCommonParas(), GlobalParam.getInstance().getAccessToken(),
            deviceInfo.getImei(), Fence.SHAPE_CIRCLE, shapeParam, Fence.SWITCH_ON, AllOnlineApp.sAccount,
            Fence.ALARM_TYPE_ONCE)
            .compose(RxUtils.toMain())
            .compose(RxUtils.businessTransformer())
            .subscribeWith(new BaseSubscriber<RespBase>() {
                @Override
                public void onHttpError(ExceptionHandle.ResponeThrowable e) {
                    showErr(e.getErrCodeMessage());
                    hideLoading();
                }

                @Override
                public void onNext(RespBase respBase) {
                    hideLoading();
                    showToast(getString(R.string.add_fence_success));
                    if (deviceInfo.equals(currDevice)) {
                        setSwitchIcon(Fence.SWITCH_ON);
                        if (mFence == null) {
                            mFence = new Fence();
                        }
                        mFence.setValidate_flag(Fence.SWITCH_ON);
                        mFence.setAlarm_type(Fence.ALARM_TYPE_ONCE);
                        mFence.setShape_type(Fence.SHAPE_CIRCLE);
                        mFence.setShape_param(shapeParam);
                        mFence.setLat(deviceInfo.getLat());
                        mFence.setLng(deviceInfo.getLng());
                        mFence.setRadius(setManager.getFenceRange());
                        addFenceOnMap(mFence, true);
                    }
                }
            });
        subscribeRx(disposable);
    }

    private void getAlarmCount() {
        Disposable disposable = DataEngine.getAllMainApi().getAlarmOverviewCount(
                GlobalParam.getInstance().getCommonParas(),
                AllOnlineApp.sAccount,
            GlobalParam.getInstance().getAccessToken(),
                0)
                .compose(RxUtils.toMain())
                .compose(RxUtils.businessTransformer())
            .subscribeWith(new BaseSubscriber<RespAlarmCount>() {
                    @Override
                    public void onNext(RespAlarmCount respAlarmCount) {
                        setAlarmCount(respAlarmCount);
                    }

                    @Override
                    public void onHttpError(ExceptionHandle.ResponeThrowable e) {

                    }
                });
        subscribeRx(disposable);
    }

    private void setAlarmCount(RespAlarmCount respAlarmCount) {
        int count = 0;
        if (respAlarmCount != null && respAlarmCount.getData() != null) {
            count = respAlarmCount.getData().count;
        }
        int iReaded = AlarmCategoryUtils.getAllAlarmLocalCount(this);
        count = count - iReaded;
        count = count >= 0 ? count : 0;
        if (count > 99) {
            textAlarmCount.setVisibility(View.VISIBLE);
            textAlarmCount.setText("99+");
        } else if (count > 0) {
            textAlarmCount.setVisibility(View.VISIBLE);
            textAlarmCount.setText(String.valueOf(count));
        } else {
            textAlarmCount.setVisibility(View.GONE);
        }

        BadgeUtil.sendBadgeNotification(null, 0, AllOnlineApp.mApp, 0, count);
    }

    private void goToAudioRecord(DeviceInfo device) {
        if (device == null) {
            return;
        }
        long groupId = device.getVoice_gid();
        final GMChatRoomManager chatroomMgr = GMClient.getInstance().chatroomManager();
        if (chatroomMgr == null) {
            GoomeLog.getInstance().logE("MonitorParentFragment", "chatroomMgr is null", 0);
            return;
        }
        if (chatroomMgr.isInChatroom(groupId, AllOnlineApp.sToken.community_id)) {
            Intent intent = new Intent(this, AudioRecordingActivity.class);
            intent.putExtra(AudioRecordingActivity.PARAM_DEVICE, device);
            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            startActivity(intent);
        } else {
            GMClient.getInstance().chatroomManager().joinChatRoom(groupId,
                AllOnlineApp.getCurrentLocation().getLongitude(), AllOnlineApp.getCurrentLocation().getLatitude(),
                AllOnlineApp.sToken.name, "", new GMValueCallBack<GMChatRoom>() {
                    @Override
                    public void onSuccess(GMChatRoom room) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Intent intent = new Intent(MainActivityParent.this, AudioRecordingActivity.class);
                                intent.putExtra(AudioRecordingActivity.PARAM_DEVICE, device);
                                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                                startActivity(intent);
                            }
                        });
                    }

                    @Override
                    public void onError(GMError gmError) {
                        showToast(gmError.errMsg());
                    }
                });
        }
    }

    private void setSwitchIcon(int flag) {
        if (flag == Fence.SWITCH_ON) {
            toggleButtonSet.setChecked(true);
            //textOneKey.setText(R.string.one_key_close);
        } else {
            toggleButtonSet.setChecked(false);
            //textOneKey.setText(R.string.one_key_open);
        }
    }

    protected void hideBottomCarInfo() {
        if (layoutBottomCarInfo != null) {
            RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) layoutBottomCarInfo.getLayoutParams();
            if (lp.bottomMargin < 0) {
                return;
            }
            ValueAnimator hide = ValueAnimator.ofInt(0, layoutBottomCarInfo.getHeight()).setDuration(DURATION);
            hide.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    int va = (int) animation.getAnimatedValue();
                    lp.bottomMargin = -1 * va;
                    layoutBottomCarInfo.setLayoutParams(lp);
                }
            });
            hide.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {

                }

                @Override
                public void onAnimationEnd(Animator animation) {
                }

                @Override
                public void onAnimationCancel(Animator animation) {

                }

                @Override
                public void onAnimationRepeat(Animator animation) {

                }
            });
            hide.start();
        }
        //上一个，下一个
        if (layoutNextPre != null && layoutNextPre.getVisibility() == View.VISIBLE) {
            layoutNextPre.setVisibility(View.GONE);
        }
        //外电信息
        if (rlBattery != null && rlBattery.getVisibility() == View.VISIBLE) {
            rlBattery.setVisibility(View.GONE);
        }
        //定位器角度，姿态信息
        if (rlInstall != null && rlInstall.getVisibility() == View.VISIBLE) {
            rlInstall.setVisibility(View.GONE);
        }
        hideDevTitle();
    }

    protected void showBottomCarInfo() {
        if (layoutBottomCarInfo != null) {
            RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) layoutBottomCarInfo.getLayoutParams();
            if (lp.bottomMargin == 0) {
                layoutBottomCarInfo.setVisibility(View.VISIBLE);
                return;
            }
            ValueAnimator hide = ValueAnimator.ofInt(lp.bottomMargin, 0).setDuration(DURATION);
            hide.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    int va = (int) animation.getAnimatedValue();
                    lp.bottomMargin = va;
                    layoutBottomCarInfo.setLayoutParams(lp);
                }
            });
            hide.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {

                }

                @Override
                public void onAnimationEnd(Animator animation) {
                }

                @Override
                public void onAnimationCancel(Animator animation) {

                }

                @Override
                public void onAnimationRepeat(Animator animation) {

                }
            });
            hide.start();
        }
        if (layoutNextPre != null
            && layoutNextPre.getVisibility() == View.GONE
            && listDevices != null
            && listDevices.size() > 1) {
            layoutNextPre.setVisibility(View.VISIBLE);
        }
    }

    protected boolean isBottomInfoShow() {
        if (layoutBottomCarInfo != null) {
            return layoutBottomCarInfo.getVisibility() == View.VISIBLE;
        }
        return false;
    }

    private void isFromAlarm(Intent intent) {
        if (intent != null && intent.hasExtra(IMEI)) {
            String imei = intent.getStringExtra(IMEI);
            if (!TextUtils.isEmpty(imei) && listDevices != null && listDevices.size() > 0) {
                for (DeviceInfo deviceInfo : listDevices) {
                    if (deviceInfo != null && imei.equals(deviceInfo.getImei())) {
                        deviceMarkerTap(deviceInfo, true);
                        break;
                    }
                }
            }
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        isFromAlarm(intent);
    }

    @SuppressWarnings("HandlerLeak")
    private class HandlerEx extends Handler {
        WeakReference<MainActivityParent> mReffer;

        public HandlerEx(MainActivityParent activity) {
            mReffer = new WeakReference<MainActivityParent>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            MainActivityParent instance = mReffer.get();
            if (instance != null) {
                switch (msg.what) {
                    case MSG_START_REFRESH:
                        getAlarmCount();
                        getDeviceList(false);
                        instance.mHandler.sendEmptyMessageDelayed(MSG_START_REFRESH, 10000);
                        break;
                    case REFRESH_SINGLE:
                        refreshSingleDev(currDevice);
                        sendNextRefresh(currDevice);
                        break;
                    case OPEN_TEMP_CMD:
                        openTempCmd(currDevice);
                        instance.mHandler.sendEmptyMessageDelayed(OPEN_TEMP_CMD, 60 * 1000);
                        break;
                    case PARAM_CMD:
                        instance.paramCount++;
                        if (instance.paramCount > 2) {
                            break;
                        } else {
                            sendParamCmd((DeviceInfo) msg.obj);
                        }
                        break;
                    case GET_RESPONSE:
                        getResponse((DevImeiId) msg.obj);
                        break;
                    default:
                        break;
                }
            }
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.user_icon:
                //我的
                startActivity(new Intent(MainActivityParent.this, MineActivity.class));
                break;

            case R.id.warning_icon:
            case R.id.textViewWarnCount:
                //告警。主页进去展示的是该账户下全部的告警。不需要传入imei
                startActivity(new Intent(MainActivityParent.this, AlarmCategoryListActivity.class));
                break;

            case R.id.imageViewList:
                //切换地图和列表
                startActivityForResult(new Intent(MainActivityParent.this, AllListActivity.class),
                    ACTIVITY_LIST_REQUEST_CODE);
                break;

            case R.id.nav_map_change:
                //地图模式切换--卫星或普通模式
                if (iMapMode == Constant.MAP_NORMAL) {
                    //aMap.setMapType(AMap.MAP_TYPE_SATELLITE);// 卫星地图模式
                    iMapMode = Constant.MAP_SATELLITE;
                    changeMapMode(iMapMode);
                    setSatelliteIcon();
                } else {
                    //aMap.setMapType(AMap.MAP_TYPE_NORMAL);// 普通地图模式
                    iMapMode = Constant.MAP_NORMAL;
                    changeMapMode(iMapMode);
                    setSatelliteIcon();
                }
                break;

            case R.id.powerMode:
                //耗电模式
                showPowerModeMenu();
                break;

            case R.id.map_street:
                //街景
                SettingDataManager.getInstance(MainActivityParent.this).goToPanoramaActivity(MainActivityParent.this,
                    currDevice);
                break;

            case R.id.home_share_tv:
                //分享
                toShare();
                break;

            case R.id.rlRecord:
                //录音
                goToAudioRecord(currDevice);
                break;

            case R.id.detail_btn:
                if (currDevice == null) {
                    return;
                }
                // 设备详情
                Intent intentDetail = new Intent(this, DeviceDetailInfoActivity.class);
                intentDetail.putExtra(DeviceDetailInfoActivity.DEV_IMEI, currDevice.getImei());
                intentDetail.putExtra(DeviceDetailInfoActivity.DEV_ANGLE, currDevice.getInstallAngle());
                startActivity(intentDetail);
                break;

            case R.id.senior_setting:
                if (currDevice == null) {
                    return;
                }
                //高级设置
                Intent intentHigh = new Intent(MainActivityParent.this, DeviceSettingActivity.class);
                intentHigh.putExtra(Constant.KEY_DEVICE, currDevice);
                startActivity(intentHigh);
                break;

            case R.id.route_playback_btn:
                if (currDevice == null) {
                    return;
                }
                // 进入历史回放界面
                Intent intentHistory = new Intent(MainActivityParent.this, SelectDateHistoryActivity.class);
                intentHistory.putExtra(Constant.KEY_DEVICE, currDevice);
                startActivity(intentHistory);
                break;

            case R.id.previous_dev:
                if (listDevices == null || listDevices.size() <= 1) {
                    return;
                }
                //减少index。移动到上一个车辆查看信息
                --iCurrentShowDeviceIndex;
                if (iCurrentShowDeviceIndex < 0 || iCurrentShowDeviceIndex > listDevices.size() - 1) {
                    iCurrentShowDeviceIndex = listDevices.size() - 1;
                }
                deviceMarkerTap(listDevices.get(iCurrentShowDeviceIndex), false);
                break;

            case R.id.next_dev:
                if (listDevices == null || listDevices.size() <= 1) {
                    return;
                }
                //增加index。移动到下一个车辆查看信息
                ++iCurrentShowDeviceIndex;
                if (iCurrentShowDeviceIndex < 0 || iCurrentShowDeviceIndex > listDevices.size() - 1) {
                    iCurrentShowDeviceIndex = 0;
                }
                deviceMarkerTap(listDevices.get(iCurrentShowDeviceIndex), false);
                break;

            case R.id.layoutNoDev:
                if (AllOnlineApp.sAccount != null && AllOnlineApp.sAccount.equals(Constant.ACCOUNT_AN_ZHUANG_GONG)) {
                    startActivity(new Intent(MainActivityParent.this, UploadInfoActivity.class));
                } else {
                    toScan();
                }
                break;

            //外电信息
            case R.id.rlBattery:
                showOutBatteryInfo();
                break;

            //公告
            case R.id.notice:
                jumpNotice();
                break;
            //关闭公告
            case R.id.closeNotice:
                rlNotice.setVisibility(View.GONE);
                break;
            //开锁
            case R.id.rlLock:
                openLock();
                break;

            default:
                break;
        }
    }

    private void openLock() {
        showProgressDialog("正在开锁...");
        isOpenLock = true;
        SendCmd lock = cmdMgr.getLockCmd();
        if (lock != null) {
            lock.getData().get(0).getCmd().get(0).getParam().get(0).setPval("1");
            sendCmd2Dev(currDevice, lock, new OnSendCmdResult() {
                @Override
                public void onResult(String imei, String id) {
                    getRespDelay(CmdManager.RELAY, imei, id);
                }
            });
            return;
        }
        DeviceInfo d = new DeviceInfo();
        d.setDev_type("GS06");
        d.setImei(currDevice.getImei());
        getRelayCmd(d, new OnAllCmdsResult() {
            @Override
            public void onResult(DeviceInfo dev) {
                SendCmd lock = cmdMgr.getLockCmd();
                if (lock == null) {
                    return;
                }
                lock.getData().get(0).getCmd().get(0).getParam().get(0).setPval("1");
                sendCmd2Dev(dev, lock, new OnSendCmdResult() {
                    @Override
                    public void onResult(String imei, String id) {
                        getRespDelay(CmdManager.RELAY, imei, id);
                    }
                });
            }
        });
    }

    private void openLockFail() {
        if (isOpenLock) {
            isOpenLock = false;
            dismissProgressDialog();
            showToast("开锁失败");
        }
    }

    private void openLockSuccess() {
        if (isOpenLock) {
            isOpenLock = false;
            dismissProgressDialog();
            showToast("开锁成功");
        }
    }

    private void getRelayCmd(DeviceInfo dev, OnAllCmdsResult l) {
        String token = GlobalParam.getInstance().getAccessToken();
        Disposable d = DataEngine.getAllMainApi()
            .getCmds(token, dev.getDev_type(), 1, GlobalParam.getInstance().getCommonParas())
            .compose(RxUtils.businessTransformer())
            .compose(RxUtils.toIO())
            .subscribeWith(new BaseSubscriber<RespTypeCmds>() {
                @Override
                public void onHttpError(ExceptionHandle.ResponeThrowable e) {
                    openLockFail();
                }

                @Override
                public void onNext(RespTypeCmds resp) {
                    if (resp == null || resp.getData().size() <= 0) {
                        return;
                    }
                    for (TypeCmd tc : resp.getData()) {
                        if (tc.getCmd() == null || tc.getCmd().size() <= 0) {
                            continue;
                        }
                        for (Cmd c : tc.getCmd()) {
                            if (CmdManager.RELAY.equals(c.getHead())) {
                                cmdMgr.saveSingleCmd(c);
                                break;
                            }
                        }
                        cmdMgr.makeCmds(dev.getDev_type());
                        break;
                    }
                    if (l != null) {
                        l.onResult(dev);
                    }
                }
            });
        subscribeRx(d);
    }

    private void showOutBatteryInfo() {
        if (currDevice == null) {
            return;
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("外部电池信息")
            .setMessage("电量：" + currDevice.getBatteryLevel() + "%, 电压：" + currDevice.getVoltage() + "V")
            .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            })
            .create()
            .show();
    }

    private void jumpNotice() {
        if (notice == null) {
            return;
        }
        switch (notice.id) {
            //平台
            case 0:
                startActivity(new Intent(this, PlatRechargeActivity.class));
                break;
            //物联卡
            case 1:
                startActivity(new Intent(this, CardRechargeActivity.class));
                break;
            //平台 / 物联卡
            case 2:
                startActivity(new Intent(this, NoticeRechargeActivity.class));
                break;
            // url
            case 3:
                CommunityUtil.switch2WebViewActivity(this, notice.url, false, BusAdverActivity.INTENT_FROM_NOTICE);
                break;
            default:
                break;
        }
    }

    private void showPowerModeMenu() {
        AppConfigs config = AllOnlineApp.getAppConfig();
        ArrayList<TextSet> list = new ArrayList<>();
        TextSet accurate = new TextSet(getString(R.string.loc_accurate_title, getTimerInterval(
            config.getHighTimerInterval())), true, new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currDevice == null || !currDevice.isOnline()) {
                    return;
                }
                sendTimerCmd(config.getHighTimerInterval());
                powerMode.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.power_mode_high, 0, 0);
            }
        });
        list.add(accurate);
        TextSet fine = new TextSet(getString(R.string.loc_fine_title, getTimerInterval(
            config.getMiddleTimerInterval())), true, new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currDevice == null || !currDevice.isOnline()) {
                    return;
                }
                sendTimerCmd(config.getMiddleTimerInterval());
                powerMode.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.power_mode_medium, 0, 0);
            }
        });
        list.add(fine);
        TextSet lowPower = new TextSet(getString(R.string.loc_low_power_title, getTimerInterval(
            config.getLowerTimerInterval())), true, new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currDevice == null || !currDevice.isOnline()) {
                    return;
                }
                sendTimerCmd(config.getLowerTimerInterval());
                powerMode.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.power_mode_low, 0, 0);
            }
        });
        list.add(lowPower);
        TextSet off = new TextSet(R.string.loc_off_power_title, true, new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currDevice == null || !currDevice.isOnline()) {
                    return;
                }
                sendTimerCmd(0);
                powerMode.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.power_mode_off, 0, 0);
            }
        });
        list.add(off);
        PopupWindowUtil.showPopWindow(this, rootView, R.string.loc_accuracy_title, list, true);
    }

    private String getTimerInterval(int time) {
        if (time < 60) {
            return time + "秒";
        } else {
            int m = time / 60;
            if (m < 60) {
                return m + "分钟";
            } else {
                int h = m / 60;
                m = m % 60;
                return h + "小时" + m + "分钟";
            }
        }
    }

    private void saveOldMode(int time) {
        if (currDevice == null || TextUtils.isEmpty(currDevice.getImei())) {
            return;
        }
        DevPowerMode old = oldMode.get(currDevice.getImei());
        DevPowerMode mode = cmdMgr.getDevMode(currDevice.getImei());
        if (mode == null) {
            DevPowerMode temp = new DevPowerMode(currDevice.getImei(), getMode(time + " seconds"));
            oldMode.put(temp.getImei(), temp);
            cmdMgr.saveMode(temp, false);
        } else {
            if (old == null) {
                oldMode.put(mode.getImei(), mode.copy(mode.getImei(), mode.getMode()));
            }
            mode.setMode(getMode(time + " seconds"));
        }
    }

    private void sendTimerCmd(int time) {
        saveOldMode(time);
        SendCmd timer = cmdMgr.getTimerCmd();
        if (timer != null) {
            timer.getData().get(0).getCmd().get(0).getParam().get(0).setPval(String.valueOf(time));
            sendCmd2Dev(currDevice, timer, new OnSendCmdResult() {
                @Override
                public void onResult(String imei, String id) {
                    getRespDelay(CmdManager.TIMER, imei, id);
                }
            });
            return;
        }
        getAllCmds(currDevice, new OnAllCmdsResult() {
            @Override
            public void onResult(DeviceInfo dev) {
                SendCmd timer = cmdMgr.getTimerCmd();
                if (timer == null) {
                    return;
                }
                timer.getData().get(0).getCmd().get(0).getParam().get(0).setPval(String.valueOf(time));
                sendCmd2Dev(dev, timer, new OnSendCmdResult() {
                    @Override
                    public void onResult(String imei, String id) {
                        getRespDelay(CmdManager.TIMER, imei, id);
                    }
                });
            }
        });
    }

    private void sendParamCmd(DeviceInfo dev) {
        if (cmdMgr.getParamCmd() == null) {
            Message param = Message.obtain(mHandler, PARAM_CMD, dev);
            mHandler.sendMessageDelayed(param, 1000);
            return;
        }
        paramCount = 0;
        sendCmd2Dev(dev, cmdMgr.getParamCmd(), new OnSendCmdResult() {
            @Override
            public void onResult(String imei, String id) {
                getRespDelay(CmdManager.PARAM, imei, id);
            }
        });
    }

    private void getRespDelay(String type, String imei, String id) {
        if (TextUtils.isEmpty(imei) || TextUtils.isEmpty(id)) {
            return;
        }
        Message resp = Message.obtain();
        resp.what = GET_RESPONSE;
        resp.obj = new DevImeiId(type, imei, id);
        mHandler.sendMessageDelayed(resp, RESP_DELAY);
    }

    private void getResponse(DevImeiId dev) {
        if (dev == null) {
            return;
        }
        String token = GlobalParam.getInstance().getAccessToken();
        Disposable d = DataEngine.getAllMainApi().getResp(dev.getImei(), dev.getId(), token,
            GlobalParam.getInstance().getCommonParas())
            .compose(RxUtils.businessTransformer())
            .compose(RxUtils.toMain())
            .subscribeWith(new BaseSubscriber<RespResponse>() {
                @Override
                public void onHttpError(ExceptionHandle.ResponeThrowable e) {
                    openLockFail();
                }

                @Override
                public void onNext(RespResponse resp) {
                    if (resp == null) {
                        return;
                    }
                    if (!TextUtils.isEmpty(resp.getData().getResponse())) {
                        dev.setResponse(resp.getData().getResponse());
                        processResponse(dev);
                    } else {
                        openLockFail();
                    }
                }
            });
        subscribeRx(d);
    }

    private void processResponse(DevImeiId resp) {
        if (resp == null) {
            return;
        }
        if (CmdManager.PARAM.equals(resp.getType())) {
            if (TextUtils.isEmpty(resp.getResponse())) {
                return;
            }
            String[] resps = resp.getResponse().split(",");
            for (String s : resps) {
                if (s.startsWith("upload interval") || s.startsWith("上传间隔") || s.startsWith("Upload interval")) {
                    String[] ups = s.split(":");
                    if (ups.length < 2) {
                        continue;
                    }
                    int mode = getMode(ups[1]);
                    DevPowerMode pm = new DevPowerMode(resp.getImei(), mode);
                    cmdMgr.saveMode(pm, true);
                    updateCurrentMode(pm);
                    break;
                }
            }
        } else if (CmdManager.TIMER.equals(resp.getType())) {
            String result = resp.getResponse();
            if (TextUtils.isEmpty(result)) {
                return;
            }
            if (result.contains("指令错误") || result.contains("Exec failure")) {
                showToast("设备不支持此指令，请联系客户经理升级");
                //恢复power mode
                DevPowerMode old = oldMode.get(resp.getImei());
                if (old != null) {
                    cmdMgr.saveMode(old, true);
                    oldMode.remove(resp.getImei());
                    updateCurrentMode(old);
                }
            } else if (result.contains("设置成功") || result.contains("Exec Success")) {
                DeviceInfo dev = new DeviceInfo();
                dev.setImei(resp.getImei());
                sendParamCmd(dev);
            }
        } else if (CmdManager.RELAY.equals(resp.getType())) {
            String result = resp.getResponse();
            if (result.contains("Success") || result.contains("success") || result.contains("成功")) {
                openLockSuccess();
            } else {
                openLockFail();
            }
        }
    }

    private int getMode(String str) {
        int seconds = 0;
        String[] ss = new String[2];
        for (int i = 0; i < str.length(); i++) {
            if (!Character.isDigit(str.charAt(i))) {
                ss[0] = str.substring(0, i);
                if (str.charAt(i) == ' ') {
                    ss[1] = str.substring(i + 1);
                } else {
                    ss[1] = str.substring(i);
                }
                break;
            }
        }
        int value = Integer.parseInt(ss[0]);
        if ("秒".equals(ss[1]) || "seconds".equalsIgnoreCase(ss[1])) {
            seconds = value;
        } else if ("分钟".equals(ss[1]) || "minutes".equalsIgnoreCase(ss[1])) {
            seconds = value * 60;
        } else if ("小时".equals(ss[1]) || "hours".equalsIgnoreCase(ss[1])) {
            seconds = value * 60 * 60;
        }
        if (seconds <= 0) {
            return DevPowerMode.OFF;
        } else if (seconds <= 180) {
            return DevPowerMode.HIGH;
        } else if (seconds <= 300) {
            return DevPowerMode.MEDIUM;
        } else {
            return DevPowerMode.LOW;
        }
    }

    private void updateCurrentMode(DevPowerMode mode) {
        if (mode == null) {
            powerMode.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.power_mode_off, 0, 0);
            return;
        }
        if (currDevice != null && currDevice.getImei().equals(mode.getImei())) {
            switch (mode.getMode()) {
                default:
                case DevPowerMode.OFF:
                    powerMode.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.power_mode_off, 0, 0);
                    break;
                case DevPowerMode.LOW:
                    powerMode.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.power_mode_low, 0, 0);
                    break;
                case DevPowerMode.MEDIUM:
                    powerMode.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.power_mode_medium, 0, 0);
                    break;
                case DevPowerMode.HIGH:
                    powerMode.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.power_mode_high, 0, 0);
                    break;
            }
        }
    }

    private void toScan() {
        Intent i = new Intent(this, CaptureActivity.class);
        i.putExtra(CaptureActivity.SHOW_MANUAL, true);
        startActivityForResult(i, REQ_SCAN_BIND);
    }

    private void toShare() {
        String weiboContentFlag = getString(R.string.share_weibo_flag);
        String title = getString(R.string.monitor_share1);
        StringBuffer content = new StringBuffer(getString(R.string.share_hint));
        UmengShareUtils.toShareLocation(this, title, content, null, null, weiboContentFlag,
            currDevice.getImei());
    }

    private void startCountdown() {
        if (mHandler != null) {
            mHandler.removeMessages(MSG_START_REFRESH);
            mHandler.sendEmptyMessage(MSG_START_REFRESH);
        }
    }

    private void stopCountdown() {
        if (mHandler != null) {
            mHandler.removeMessages(MSG_START_REFRESH);
        }
    }

    private void startAnim() {
        if (recordAni == null) {
            recordAni = ValueAnimator.ofInt(0, 3).setDuration(1500);
            recordAni.setRepeatCount(ValueAnimator.INFINITE);
            recordAni.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    int i = (int) animation.getAnimatedValue();
                    String str = getString(R.string.is_recording_no_dot);
                    tvRecord.setText(str + dotText[i % dotText.length]);
                }
            });
        }
        if (recordAni.isRunning()) {
            return;
        }
        recordAni.start();
    }

    private void stopAnim() {
        if (recordAni != null) {
            recordAni.cancel();
        }
    }

    private void updateCurrentDeviceView() {
        if (listDevices == null || listDevices.size() <= 0) {
            removeFenceOnMap();
            return;
        }

        if (!TextUtils.isEmpty(bindImei)) {
            //扫码绑定后需要选中显示弹窗
            layoutBottomCarInfo.setVisibility(View.VISIBLE);
            for (DeviceInfo deviceInfo : listDevices) {
                if (deviceInfo != null && bindImei.equals(deviceInfo.getImei())) {
                    currDevice = deviceInfo;
                    zoomLevel = 18f;
                    bindImei = null;
                    showBottomCarInfo();
                    updateBottomView(currDevice, true);
                    return;
                }
            }
            bindImei = null;
        }

        if (showAfterGet) {
            showAfterGet = false;
            if (currDevice == null) {
                //第一次进入，需要把设备视图全部切换到可见范围内,不进行地图缩放
                currDevice = listDevices.get(0);
                setGoomeDev(currDevice);
                animateCameraInclue(listDevices);
                showBottomCarInfo();
                updateBottomView(currDevice, false);
            }
            return;
        }

        //已经选中过设备，更新当前设备的信息，zoomLevel保持之前的
        if (currDevice != null) {
            boolean bFind = false;
            for (DeviceInfo deviceInfo : listDevices) {
                if (deviceInfo != null && deviceInfo.equals(currDevice)) {
                    currDevice = deviceInfo;
                    bFind = true;
                    break;
                }
            }
            if (!bFind) {
                currDevice = listDevices.get(0);
                showBottomCarInfo();
                removeFenceOnMap();
            }
            updateBottomView(currDevice, true);
        }
    }

    protected void updateBottomView(DeviceInfo device, final boolean bHasZoom) {
        if (device == null) {
            rlBattery.setVisibility(View.GONE);
            return;
        }

        //录音
        switch (device.getVoice_status()) {
            case 0:
                stopAnim();
                rlRecord.setVisibility(View.VISIBLE);
                ivRecord.setImageResource(R.drawable.audio_close);
                tvRecord.setText(getString(R.string.start_recording));
                AllOnlineApp.loginGMIm(this.getApplicationContext());
                break;
            case 1:
                rlRecord.setVisibility(View.VISIBLE);
                startAnim();
                ivRecord.setImageResource(R.drawable.audio_open);
                tvRecord.setText(getString(R.string.is_recording));
                AllOnlineApp.loginGMIm(this.getApplicationContext());
                break;
            case -1:
            default:
                stopAnim();
                rlRecord.setVisibility(View.GONE);
                break;
        }

        //耗电模式
        if (device.isWSerial()) {
            rlPower.setVisibility(View.VISIBLE);
            powerMode.setVisibility(View.VISIBLE);
            DevPowerMode mode = cmdMgr.getDevMode(device.getImei());
            updateCurrentMode(mode);
        } else {
            rlPower.setVisibility(View.GONE);
            powerMode.setVisibility(View.GONE);
        }

        //开锁
        if (DeviceInfo.TYPE_LOCK.equals(device.getIcon_type())) {
            rlLock.setVisibility(View.VISIBLE);
        } else {
            rlLock.setVisibility(View.GONE);
        }

        if (TextUtils.isEmpty(device.getName())) {
            //名称为空，则其次采用imei，最后显示未知
            popName.setText(TextUtils.isEmpty(device.getImei()) ? getString(R.string.speed_unknown) : device.getImei());
        } else {
            popName.setText(device.getName());
        }

        //acc开关或者设备电量
        if (device.getAcc() >= 0) {
            powerLL.setVisibility(View.VISIBLE);
            popPower.setVisibility(View.VISIBLE);
            if (device.getAcc() == 1) {
                //开
                popPower.setText(getString(R.string.acc_time_open, TimeUtil.getTimeDHMS(this, device.getAcc_seconds(),
                    2)));
            } else {
                //关
                popPower.setText(getString(R.string.acc_time_close, TimeUtil.getTimeDHMS(this, device.getAcc_seconds(),
                    2)));
            }
        } else if (!TextUtils.isEmpty(device.getPower())) {
            powerLL.setVisibility(View.VISIBLE);
            popPower.setVisibility(View.VISIBLE);
            popPower.setText(getString(R.string.power, device.getPower()));
        } else {
            powerLL.setVisibility(View.GONE);
            popPower.setVisibility(View.GONE);
        }

        //电动车电量
        if (device.getVoltage() > 0) {
            rlBattery.setVisibility(View.VISIBLE);
            batteryLevel.refreshPower(device.getBatteryLevel() / 100.0f);
            batteryPercent.setText(device.getVoltage() + "V");
        } else {
            rlBattery.setVisibility(View.GONE);
        }

        //GPS定位器的安装角度
        int timeValid = device.getValid();
        if (timeValid == 0) {
            int iAngle = device.getInstallAngle();
            if (iAngle > 0) {
                //所有度数加了1，为了区分0度和不支持，所以此处判断需要加1
                rlInstall.setVisibility(View.VISIBLE);
                if (iAngle > ANGLE_WARNING_MAX) {
                    tvInstall.setText(R.string.dev_install_not_ok);
                    tvInstall.setTextColor(ContextCompat.getColor(this, R.color.dev_install_error));
                    imgInstall.setImageResource(R.drawable.dev_install_error);
                    rlInstall.setTag(device);
                } else if (iAngle > ANGLE_NORMAL_MAX) {
                    tvInstall.setText(R.string.dev_install_not_ok);
                    tvInstall.setTextColor(ContextCompat.getColor(this, R.color.dev_install_warning));
                    imgInstall.setImageResource(R.drawable.dev_install_warning);
                    rlInstall.setTag(device);
                } else {
                    tvInstall.setText(R.string.dev_install_ok);
                    tvInstall.setTextColor(ContextCompat.getColor(this, R.color.dev_install_ok));
                    imgInstall.setImageResource(R.drawable.dev_install_ok);
                    rlInstall.setTag(null);
                }
            } else {
                rlInstall.setVisibility(View.GONE);
                rlInstall.setTag(null);
            }
        } else {
            rlInstall.setVisibility(View.GONE);
            rlInstall.setTag(null);
        }

        //gps信号
        int gps = device.getGpsLevel();
        int speed = device.getSpeed();
        if (gps == 0 || speed < 0) {
            signalLine.setVisibility(View.GONE);
            gpsLevel.setVisibility(View.GONE);
        } else {
            signalLine.setVisibility(View.VISIBLE);
            gpsLevel.setVisibility(View.VISIBLE);
            switch (gps) {
                case 1:
                    gpsLevel.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.signal0, 0);
                    break;
                case 2:
                    gpsLevel.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.signal1, 0);
                    break;
                case 3:
                    gpsLevel.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.signal2, 0);
                    break;
                case 4:
                    gpsLevel.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.signal3, 0);
                    break;
                case 5:
                    gpsLevel.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.signal4, 0);
                    break;
                default:
                    break;
            }
        }
        //电池剩余电量
        if (!TextUtils.isEmpty(device.getBattery_life())) {
            tvBattery.setVisibility(View.VISIBLE);
            tvBattery.setText(device.getBattery_life());
        } else {
            tvBattery.setVisibility(View.GONE);
        }

        setDeviceStatusText(device, popState);

        queryFence(device);

        if (layoutBottomCarInfo != null && layoutBottomCarInfo.getVisibility() == View.VISIBLE) {
            //只有显示浮窗才会更新地址和视角以及title
            reverseGeo(device);

            showDevTitle();

            if (!bHasZoom) {
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        //传一个无效的zoomLevel。该函数会判断，相当于不设置缩放级别，保持当前的级别
                        animateCamera(device.getLat(), device.getLng(), -1f);
                    }
                }, 500);
            } else {
                animateCamera(device.getLat(), device.getLng(), zoomLevel);
            }
        }
    }

    public static void setDeviceStatusText(DeviceInfo device, TextView textView) {
        if (device == null || textView == null) {
            return;
        }
        switch (device.getState()) {
            case DeviceInfo.STATE_OFFLINE:
                // 离线
                textView.setText(AllOnlineApp.mApp.getString(R.string.car_state_offline2) + "  "
                    + TimeUtil.getTimeDHMS(AllOnlineApp.mApp, device.getSeconds(), 2));
                break;

            case DeviceInfo.STATE_RUNNING:
                // 行驶
                String speed;
                if (device.getSpeed() < 0) {
                    speed = AllOnlineApp.mApp.getString(R.string.speed_unknown);
                } else {
                    speed = device.getSpeed() + "km/h";
                }
                textView.setText(AllOnlineApp.mApp.getString(R.string.car_state_runing2) + "  " + speed);
                break;

            case DeviceInfo.STATE_STOP:
                // 静止
                textView.setText(AllOnlineApp.mApp.getString(R.string.car_state_stop2) + "  "
                    + TimeUtil.getTimeDHMS(AllOnlineApp.mApp, device.getSeconds(), 2));
                break;

            case DeviceInfo.STATE_DISABLE:
                // 设备未启用
                textView.setText(R.string.car_state_disable2);
                break;

            case DeviceInfo.STATE_EXPIRE:
                // 过期
                textView.setText(AllOnlineApp.mApp.getString(R.string.car_state_expire2) + "  "
                    + TimeUtil.getTimeDHMS(AllOnlineApp.mApp, device.getSeconds(), 1));
                break;

            default:
                break;
        }
        textView.setTextColor(getTextColorByStatus(device));
    }

    protected static int getTextColorByStatus(DeviceInfo device) {
        int color = ContextCompat.getColor(AllOnlineApp.mApp, R.color.text_black);
        if (device == null) {
            return color;
        }
        switch (device.getState()) {
            case DeviceInfo.STATE_OFFLINE:
                // 离线
                color = ContextCompat.getColor(AllOnlineApp.mApp, R.color.off_line);
                break;

            case DeviceInfo.STATE_RUNNING:
                // 行驶
                int status = device.getSpeedStatus();
                if (status == DeviceInfo.SPEED_OVER80) {
                    color = ContextCompat.getColor(AllOnlineApp.mApp, R.color.fast);
                } else if (status == DeviceInfo.SPEED_OVER120) {
                    color = ContextCompat.getColor(AllOnlineApp.mApp, R.color.over_speed);
                } else {
                    color = ContextCompat.getColor(AllOnlineApp.mApp, R.color.moving);
                }
                break;

            case DeviceInfo.STATE_STOP:
                // 静止
                color = ContextCompat.getColor(AllOnlineApp.mApp, R.color.still);
                break;

            case DeviceInfo.STATE_DISABLE:
                // 设备未启用
                color = AllOnlineApp.mApp.getResources().getColor(R.color.off_line);
                break;

            case DeviceInfo.STATE_EXPIRE:
                // 过期
                color = ContextCompat.getColor(AllOnlineApp.mApp, R.color.out_date);
                break;

            default:
                break;
        }
        return color;
    }

    protected void reverseGeo(final DeviceInfo device) {
        String cacheAddress = deviceManager.getCachedAddress(device.getLat(), device.getLng());
        if (!TextUtils.isEmpty(cacheAddress)) {
            setAddressText(device, cacheAddress);
            return;
        }

        //新请求地址解析
        setAddressText(device, getString(R.string.reverse));
        Disposable d = DataEngine.getAllMainApi()
            .requestAddress(GlobalParam.getInstance().getCommonParas(), AllOnlineApp.sAccount, AllOnlineApp.sTarget,
                AllOnlineApp.sToken.access_token, device.getLat(), device.getLng())
            .compose(RxUtils.toMain())
            .compose(RxUtils.businessTransformer())
            .subscribeWith(new BaseSubscriber<RespAddress>() {
                @Override
                public void onNext(RespAddress respAddress) {
                    if (respAddress != null && respAddress.getData() != null && !TextUtils.isEmpty(
                        respAddress.getData().address)) {
                        updateAddress(device, respAddress.getData().address);
                    } else {
                        //从baidu请求解析地址
                        getBaiduAddress(device);
                    }
                }

                @Override
                public void onHttpError(ExceptionHandle.ResponeThrowable e) {
                    //从baidu请求解析地址
                    getBaiduAddress(device);
                }
            });
        subscribeRx(d);
    }

    private void updateAddress(DeviceInfo device, String addr) {
        setAddressText(device, addr);
        deviceManager.setCachedAddress(device.getLat(), device.getLng(), addr);
    }

    private void getBaiduAddress(DeviceInfo device) {
        GeoCoder coder = GeoCoder.newInstance();
        OnGetGeoCoderResultListener listener = new OnGetGeoCoderResultListener() {
            @Override
            public void onGetGeoCodeResult(GeoCodeResult geoCodeResult) {
            }

            @Override
            public void onGetReverseGeoCodeResult(ReverseGeoCodeResult result) {
                if (result == null || result.error != SearchResult.ERRORNO.NO_ERROR || TextUtils.isEmpty(
                    result.getAddress())) {
                    setAddressText(device, getString(R.string.address_fail));
                    coder.destroy();
                } else {
                    String addr = result.getAddress();
                    updateAddress(device, addr);
                    coder.destroy();
                }
            }
        };
        coder.setOnGetGeoCodeResultListener(listener);
        LatLng dest;
        LatLng source = new LatLng(device.getLat(), device.getLng());
        if (setManager.getMapTypeInt() == Constant.MAP_TYPE_BAIDU) {
            dest = source;
        } else {
            CoordinateConverter converter = new CoordinateConverter().from(CoordinateConverter.CoordType.COMMON)
                .coord(source);
            dest = converter.convert();
        }
        coder.reverseGeoCode(new ReverseGeoCodeOption().location(dest));
    }

    private void getGoogleAddress(final DeviceInfo device) {
        //只有我们服务器的地址解析失败后，才会采用google解析。可能是国外的地址坐标
        setAddressText(device, getString(R.string.reverse));
        String latLng;
        if (setManager.getMapTypeInt() == Constant.MAP_TYPE_BAIDU) {
            BDLocation bd = new BDLocation();
            bd.setLatitude(device.getLat());
            bd.setLongitude(device.getLng());
            BDLocation target = CoordinateUtil.BAIDU_to_WGS84(bd);
            latLng = target.getLongitude() + "," + target.getLatitude();
        } else {
            latLng = device.getLat() + "," + device.getLng();
        }

        Disposable d = DataEngine.getGoogleAddressApi()
            .requestGoogleAddress(SettingDataManager.language, latLng)
            .compose(RxUtils.toMain())
            .compose(RxUtils.businessTransformer())
            .subscribeWith(new BaseSubscriber<RespGoogleAddress>() {
                @Override
                public void onNext(RespGoogleAddress respGoogleAddress) {
                    if (respGoogleAddress != null && respGoogleAddress.getResults() != null
                        && respGoogleAddress.getResults().size() > 0 && respGoogleAddress.getResults().get(0) != null
                        && !TextUtils.isEmpty(respGoogleAddress.getResults().get(0).formatted_address)) {
                        String newAddress = respGoogleAddress.getResults().get(0).formatted_address;
                        setAddressText(device, newAddress);
                        deviceManager.setCachedAddress(device.getLat(), device.getLng(), newAddress);
                    }
                }

                @Override
                public void onHttpError(ExceptionHandle.ResponeThrowable e) {
                    setAddressText(device, getString(R.string.address_fail));
                }
            });
        subscribeRx(d);
    }

    protected int getClusterImageResouce(int num) {
        if (num < 10) {
            return R.drawable.m0;
        } else if (num < 50) {
            return R.drawable.m1;
        } else if (num < 100) {
            return R.drawable.m2;
        } else if (num < 500) {
            return R.drawable.m3;
        } else {
            return R.drawable.m4;
        }
    }

    private void setAddressText(DeviceInfo deviceInfo, String address) {
        if (currDevice != null && address != null && !address.equals(getString(R.string.reverse))
            && !address.equals(getString(R.string.address_fail))) {
            //把地址放到设备信息中
            currDevice.setAddress(address);
        }

        if (currDevice != null && currDevice.equals(deviceInfo)) {
            String goTo = getString(R.string.go_to);
            address = address + "  " + goTo;
            int iEnd = address.length();
            int iStart = iEnd - goTo.length();
            Spannable spannable = Spannable.Factory.getInstance().newSpannable(address);
            spannable.setSpan(new ClickableSpan() {
                @Override
                public void onClick(View widget) {
                    //导航
                    goToMapNavi();
                }

                @Override
                public void updateDrawState(TextPaint ds) {
                    super.updateDrawState(ds);
                    //设置文字的高亮颜色
                    ds.setColor(ContextCompat.getColor(MainActivityParent.this, R.color.text_blue_new));
                    ds.setUnderlineText(false);
                    ds.clearShadowLayer();
                }
            }, iStart, iEnd, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            popAddress.setText(spannable, TextView.BufferType.SPANNABLE);
            popAddress.setMovementMethod(LinkMovementMethod.getInstance());
        }
    }

    protected final String PAC_GAODE = "com.autonavi.minimap";
    protected final String PAC_BAIDU = "com.baidu.BaiduMap";
    protected final String PAC_TENGXUN = "com.tencent.map";
    protected double startLat, startLon, endLat, endLon;

    private void goToMapNavi() {
        if (currDevice == null) {
            showToast(getString(R.string.data_error));
            return;
        }
        ArrayList<TextSet> list = new ArrayList<TextSet>();

        final Intent bd = new Intent();
        bd.setPackage(PAC_BAIDU);
        bd.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if (CommonUtil.isApkInstalled(this, PAC_BAIDU) && bd.resolveActivity(getPackageManager()) != null) {
            TextSet baidu = new TextSet(R.string.baidu_map, false, new OnClickListener() {
                @Override
                public void onClick(View v) {
                    // 百度地图导航
                    startBaiduMapNavi(bd);
                }
            });
            list.add(baidu);
        }

        final Intent gd = new Intent();
        gd.setPackage(PAC_GAODE);
        gd.setAction(Intent.ACTION_VIEW);
        gd.addCategory(Intent.CATEGORY_DEFAULT);
        gd.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if (CommonUtil.isApkInstalled(this, PAC_GAODE) && gd.resolveActivity(getPackageManager()) != null) {
            TextSet amap = new TextSet(R.string.gaode_map, false, new OnClickListener() {
                @Override
                public void onClick(View v) {
                    // 高德地图导航
                    startGaodeMapNavi(gd);
                }
            });
            list.add(amap);
        }

        final Intent tx = new Intent();
        tx.setPackage(PAC_TENGXUN);
        tx.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if (CommonUtil.isApkInstalled(this, PAC_TENGXUN) && tx.resolveActivity(getPackageManager()) != null) {
            TextSet tmap = new TextSet(R.string.tengxun_map, false, new OnClickListener() {
                @Override
                public void onClick(View v) {
                    // 腾讯地图导航
                    startTengxunMapNavi(tx);
                }
            });
            list.add(tmap);
        }

        if (list.size() > 0) {
            PopupWindowUtil.showPopWindow(this, rootView, R.string.pls_select_map, list, true);
        } else {
            showToast("手机没有安装地图APP");
        }
    }

    protected void startBaiduMapNavi(Intent intent) {
        AMapLocation curr = AllOnlineApp.getCurrentLocation();
        switch (setManager.getMapTypeInt()) {
            case Constant.MAP_TYPE_AMAP:
            case Constant.MAP_TYPE_TENCENT:
            default:
                //去baidu地图，当前的地图下需要转换坐标
                double[] start = CommonUtil.huoxingToBaidu(curr.getLatitude(), curr.getLatitude());
                double[] end = CommonUtil.huoxingToBaidu(currDevice.getLat(), currDevice.getLng());
                startLat = start[0];
                startLon = start[1];
                endLat = end[0];
                endLon = end[1];
                break;

            case Constant.MAP_TYPE_BAIDU:
                startLat = curr.getLatitude();
                startLon = curr.getLongitude();
                endLat = currDevice.getLat();
                endLon = currDevice.getLng();
                break;
        }
        //路线规划
        StringBuilder stringBuffer = new StringBuilder("baidumap://map/direction?");
        stringBuffer.append("&origin=name:我的位置|latlng:").append(startLat).append(",").append(startLon);
        try {
            stringBuffer.append("&destination=name:")
                .append(URLEncoder.encode(currDevice != null ? currDevice.getAddress() : "", "utf-8"))
                .append("|latlng:").append(endLat).append(",").append(endLon);
        } catch (Exception e) {
            e.printStackTrace();
        }
        stringBuffer.append("&mode=driving");
        //时间最短
        stringBuffer.append("&sy=5");
        stringBuffer.append("&src=coomix|AllOnline");
        intent.setData(Uri.parse(stringBuffer.toString()));
        startActivity(intent);
    }

    protected void startGaodeMapNavi(Intent intent) {
        AMapLocation curr = AllOnlineApp.getCurrentLocation();
        switch (setManager.getMapTypeInt()) {
            case Constant.MAP_TYPE_AMAP:
            case Constant.MAP_TYPE_TENCENT:
            default:
                startLat = curr.getLatitude();
                startLon = curr.getLongitude();
                endLat = currDevice.getLat();
                endLon = currDevice.getLng();
                break;

            case Constant.MAP_TYPE_BAIDU:
                //从baidu地图去其他地图，当前的地图下需要转换坐标
                double[] start = CommonUtil.baiduToHuoxing(curr.getLatitude(), curr.getLatitude());
                double[] end = CommonUtil.baiduToHuoxing(currDevice.getLat(), currDevice.getLng());
                startLat = start[0];
                startLon = start[1];
                endLat = end[0];
                endLon = end[1];
                break;
        }

        //路径规划
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("amapuri://route/plan/?");
        try {
            //填写应用名称
            stringBuilder.append("sourceApplication=").append(URLEncoder.encode(getString(R.string.app_name), "utf-8"));
            //导航目的地
            stringBuilder.append("&dname=")
                .append(URLEncoder.encode(currDevice != null ? currDevice.getAddress() : "", "utf-8"));
            //目的地经纬度
            stringBuilder.append("&dlat=").append(endLat);
            stringBuilder.append("&dlon=").append(endLon);
            stringBuilder.append("&dev=0&t=0");
        } catch (Exception e) {
            e.printStackTrace();
        }

        //调用高德地图APP
        //传递组装的数据
        intent.setData(Uri.parse(stringBuilder.toString()));
        startActivity(intent);
    }

    protected void startTengxunMapNavi(Intent intent) {
        AMapLocation curr = AllOnlineApp.getCurrentLocation();
        switch (setManager.getMapTypeInt()) {
            case Constant.MAP_TYPE_AMAP:
            case Constant.MAP_TYPE_TENCENT:
            default:
                startLat = curr.getLatitude();
                startLon = curr.getLongitude();
                endLat = currDevice.getLat();
                endLon = currDevice.getLng();
                break;

            case Constant.MAP_TYPE_BAIDU:
                //从baidu地图去其他地图，当前的地图下需要转换坐标
                double[] start = CommonUtil.baiduToHuoxing(curr.getLatitude(), curr.getLatitude());
                double[] end = CommonUtil.baiduToHuoxing(currDevice.getLat(), currDevice.getLng());
                startLat = start[0];
                startLon = start[1];
                endLat = end[0];
                endLon = end[1];
                break;
        }
        StringBuilder stringBuffer = new StringBuilder("qqmap://map/routeplan?type=drive");
        stringBuffer.append("&from=").append("我的位置");
        stringBuffer.append("&fromcoord=").append(startLat).append(",").append(startLon);
        stringBuffer.append("&to=");
        try {
            stringBuffer.append(URLEncoder.encode(currDevice != null ? currDevice.getAddress() : "", "utf-8"));
        } catch (Exception e) {
            e.printStackTrace();
        }
        stringBuffer.append("&tocoord=").append(endLat).append(",").append(endLon);
        stringBuffer.append("&policy=0&referer=AllOnline");

        intent.setData(Uri.parse(stringBuffer.toString()));
        startActivity(intent);
    }

    @Override
    public void onBackPressed() {
        if (System.currentTimeMillis() - clickedTime <= 2000) {
            // 用户主动退出
            stopService(new Intent(this, AllOnlineAppService.class));
            stopService(new Intent(this, OfflineMapService.class));
            stopService(new Intent(this, LocationService.class));
            stopService(new Intent(this, CheckVersionService.class));

            PerformanceReportManager.getInstance(this).release();
            finish();
        } else {
            showToast(R.string.exit_app, Toast.LENGTH_SHORT);
            clickedTime = System.currentTimeMillis();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && data != null) {
            switch (requestCode) {
                case ACTIVITY_LIST_REQUEST_CODE:
                    if (data.hasExtra(Constant.KEY_DEVICE)) {
                        listDevices = deviceManager.getValidDeviceList();
                        searchDevice = (DeviceInfo) data.getSerializableExtra(Constant.KEY_DEVICE);
                        deviceMarkerTap(searchDevice, true);
                        setFromBack = true;
                    }
                    break;
                //绑定设备
                case REQ_SCAN_BIND:
                    Bundle b = data.getExtras();
                    if (b == null) {
                        return;
                    }
                    if (b.containsKey(ZConstant.INTENT_EXTRA_KEY_QR_SCAN)) {
                        String imei = b.getString(ZConstant.INTENT_EXTRA_KEY_QR_SCAN);
                        bindDevice(imei);
                    }
                    break;
                default:
                    break;
            }
        }
    }

    private void bindDevice(final String imei) {
        String token = AllOnlineApp.sToken.access_token;
        String account = AllOnlineApp.sAccount;
        long time = System.currentTimeMillis() / 1000;
        String sig = OSUtil.toMD5(imei + time + "Goome");
        Disposable d = DataEngine.getAllMainApi()
            .bindDevice(token, account, imei, time, sig, GlobalParam.getInstance().getCommonParas())
            .compose(RxUtils.toMain())
            .compose(RxUtils.businessTransformer())
            .subscribeWith(new BaseSubscriber<RespBase>() {
                @Override
                public void onHttpError(ExceptionHandle.ResponeThrowable e) {
                    showToast(e.getErrCode() + "," + e.getErrMessage());
                }

                @Override
                public void onNext(RespBase respBase) {
                    showToast(getString(R.string.bindphone_toast_bind_suc));
                    requestDevListAfterBind(imei);
                }
            });
        subscribeRx(d);
    }

    private void requestDevListAfterBind(final String imei) {
        showLoading(getString(R.string.please_wait));
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                bindImei = imei;
                getDeviceList(true);
            }
        }, 2000);
    }

    protected void deviceMarkerTap(DeviceInfo device, boolean isAnimated) {
        if (device == null) {
            return;
        }
        if (!device.equals(currDevice)) {
            lastSelectedDevice = currDevice;
            llDebug.removeAllViews();
        }
        //消除重复点击
        if (device.equals(currDevice)) {
            mHandler.removeMessages(OPEN_TEMP_CMD);
            mHandler.removeMessages(REFRESH_SINGLE);
        }
        currDevice = device;
        //如果是谷米设备，进行特殊设置
        setGoomeDev(device);
        if (device.getImei() != null && listDevices != null) {
            for (int i = 0; i < listDevices.size(); i++) {
                if (device.getImei().equals(listDevices.get(i).getImei())) {
                    iCurrentShowDeviceIndex = i;
                    break;
                }
            }
        }
    }

    protected boolean needSpecialCmd(DeviceInfo dev) {
        return dev != null && deviceManager.isGoomeType(dev.getDev_type()) && dev.isOnline();
    }

    private void setGoomeDev(DeviceInfo dev) {
        if (dev == null) {
            return;
        }
        if (lastSelectedDevice != null && needSpecialCmd(lastSelectedDevice)) {
            //删除上个设备的命令
            mHandler.removeMessages(OPEN_TEMP_CMD);
            mHandler.removeMessages(REFRESH_SINGLE);
            closeTempCmd(lastSelectedDevice);
        }
        if (needSpecialCmd(dev)) {
            //获得焦点时且为在线设备设置
            //一分钟一条cmd命令
            mHandler.sendEmptyMessage(OPEN_TEMP_CMD);
            //3秒一条更新请求
            mHandler.sendEmptyMessage(REFRESH_SINGLE);
        }
        if (dev.isOnline() && dev.isWSerial()) {
            sendParamCmd(dev);
        }
    }

    private void refreshSingleDev(DeviceInfo dev) {
        String token = GlobalParam.getInstance().getAccessToken();
        String account = AllOnlineApp.sAccount;
        Disposable d = DataEngine.getAllMainApi()
            .refreshByImei(token, account, dev.getImei(), GlobalParam.getInstance().getCommonParas())
            .compose(RxUtils.businessTransformer())
            .compose(RxUtils.toMain())
            .subscribeWith(new BaseSubscriber<RespDeviceList>() {
                @Override
                public void onHttpError(ExceptionHandle.ResponeThrowable e) {

                }

                @Override
                public void onNext(RespDeviceList resp) {
                    if (resp.getData() == null || resp.getData().size() <= 0) {
                        return;
                    }
                    DeviceInfo di = resp.getData().get(0);
                    if (listDevices != null) {
                        for (DeviceInfo i : listDevices) {
                            if (i.equals(di)) {
                                i.update(di);
                                break;
                            }
                        }
                    }
                    showDebug(di);
                    updateCurrentDeviceView();
                    addDevicesOnMap();
                }
            });
        subscribeRx(d);
    }

    private void showDebug(DeviceInfo dev) {
        if (sv.getVisibility() == View.GONE) {
            return;
        }
        TextView tv = new TextView(this);
        DateFormat formatter = new SimpleDateFormat("HH:mm:ss");
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        String curTime = formatter.format(calendar.getTime());
        String sysTime = TimeUtil.long2DateTimeString(dev.getSys_time() * 1000);
        tv.setText(curTime + "," + sysTime + "," + dev.getLat() + "," + dev.getLng());
        if (llDebug.getChildCount() == 20) {
            llDebug.removeViewAt(0);
        }
        llDebug.addView(tv);
        mHandler.post(() -> {
            sv.fullScroll(ScrollView.FOCUS_DOWN);
        });
    }

    private void sendNextRefresh(DeviceInfo di) {
        if (di.getSys_time() > 0) {
            int interval = 3200;
            long delay;
            long next = (di.getSys_time() * 1000 + interval);
            long curr = System.currentTimeMillis();
            if (next <= curr) {
                delay = interval;
            } else {
                delay = next - curr;
            }
            mHandler.sendEmptyMessageDelayed(REFRESH_SINGLE, delay);
        }
    }

    private void openTempCmd(DeviceInfo dev) {
        AppConfigs config = AllOnlineApp.getAppConfig();
        SendCmd tempTimer = cmdMgr.getTempTimerCmd();
        if (tempTimer != null) {
            tempTimer.getData().get(0).getCmd().get(0).getParam().get(0).setPval(
                String.valueOf(config.getTempTimerInterval()));
            sendCmd2Dev(dev, tempTimer);
            return;
        }
        getAllCmds(dev, new OnAllCmdsResult() {
            @Override
            public void onResult(DeviceInfo dev) {
                SendCmd temp = cmdMgr.getTempTimerCmd();
                if (temp == null) {
                    return;
                }
                temp.getData().get(0).getCmd().get(0).getParam().get(0).setPval(
                    String.valueOf(config.getTempTimerInterval()));
                sendCmd2Dev(dev, temp);
            }
        });
    }

    protected void closeTempCmd(DeviceInfo dev) {
        SendCmd tempTimer = cmdMgr.getTempTimerCmd();
        if (tempTimer != null) {
            tempTimer.getData().get(0).getCmd().get(0).getParam().get(0).setPval("0");
            sendCmd2Dev(dev, tempTimer);
            return;
        }
        getAllCmds(dev, new OnAllCmdsResult() {
            @Override
            public void onResult(DeviceInfo dev) {
                SendCmd temp = cmdMgr.getTempTimerCmd();
                if (temp == null) {
                    return;
                }
                temp.getData().get(0).getCmd().get(0).getParam().get(0).setPval("0");
                sendCmd2Dev(dev, temp);
            }
        });
    }

    private void getAllCmds(DeviceInfo dev, OnAllCmdsResult l) {
        String token = GlobalParam.getInstance().getAccessToken();
        Disposable d = DataEngine.getAllMainApi()
            .getCmds(token, dev.getDev_type(), 1, GlobalParam.getInstance().getCommonParas())
            .compose(RxUtils.businessTransformer())
            .compose(RxUtils.toIO())
            .subscribeWith(new BaseSubscriber<RespTypeCmds>() {
                @Override
                public void onHttpError(ExceptionHandle.ResponeThrowable e) {
                }

                @Override
                public void onNext(RespTypeCmds resp) {
                    if (resp == null || resp.getData().size() <= 0) {
                        return;
                    }
                    for (TypeCmd tc : resp.getData()) {
                        if (tc.getCmd() == null || tc.getCmd().size() <= 0) {
                            continue;
                        }
                        cmdMgr.saveCmds(tc);
                        cmdMgr.makeCmds(dev.getDev_type());
                        break;
                    }
                    if (l != null) {
                        l.onResult(dev);
                    }
                }
            });
        subscribeRx(d);
    }

    private void sendCmd2Dev(DeviceInfo dev, SendCmd cmd) {
        sendCmd2Dev(dev, cmd, null);
    }

    private void sendCmd2Dev(DeviceInfo dev, SendCmd cmd, OnSendCmdResult l) {
        String str = new Gson().toJson(cmd);
        RequestBody body = RequestBody.create(MediaType.parse("application/json"), str);
        String token = GlobalParam.getInstance().getAccessToken();
        Disposable d = DataEngine.getAllMainApi()
            .sendCmd(dev.getImei(), token, GlobalParam.getInstance().getCommonParas(), body)
            .compose(RxUtils.businessTransformer())
            .compose(RxUtils.toIO())
            .subscribeWith(new BaseSubscriber<RespSendCmd>() {
                @Override
                public void onHttpError(ExceptionHandle.ResponeThrowable e) {
                    openLockFail();
                }

                @Override
                public void onNext(RespSendCmd resp) {
                    if (resp == null) {
                        return;
                    }
                    String imei = "", id = "";
                    List<HashMap<String, String>> list = resp.getData();
                    for (HashMap<String, String> map : list) {
                        for (Map.Entry<String, String> entry : map.entrySet()) {
                            if ("imei".equals(entry.getKey())) {
                                imei = entry.getValue();
                                break;
                            }
                            if ("id".equals(entry.getKey())) {
                                id = entry.getValue();
                                break;
                            }
                        }
                    }
                    if (l != null) {
                        l.onResult(imei, id);
                    }
                }
            });
        subscribeRx(d);
    }

    private void getLatestVersion() {
        int versionCode = 1;
        String patchCode = "0";
        try {
            versionCode = getPackageManager().getPackageInfo(getPackageName(), 0).versionCode;
            patchCode = Tinker.with(this).getTinkerLoadResultIfPresent().getPackageConfigByName("patchVersion");
        } catch (Exception e) {
            e.printStackTrace();
        }
        Disposable d = DataEngine.getAllMainApi().getLatestVersion(
            GlobalParam.getInstance().getCommonParas("allonline"), Build.VERSION.RELEASE,
            SettingDataManager.language, versionCode, patchCode)
            .compose(RxUtils.toMain())
            .compose(RxUtils.businessTransformer())
            .subscribeWith(new BaseSubscriber<RespUpdateInfo>() {
                @Override
                public void onNext(RespUpdateInfo respUpdateInfo) {
                    GoomeUpdateInfo updateInfo = respUpdateInfo.getData();
                    if (null != updateInfo) {
                        AllOnlineApp.gUpdateInfo = updateInfo;
                        if (updateInfo.update) {
                            GoomeUpdateAgent.showUpdateDialog(MainActivityParent.this, updateInfo);
                        } else {
                            if (updateInfo.patchUpdate) {
                                // app没有更新，但是patch有更新，下载patch
                                GoomeUpdateAgent.startPatchDownload(getApplicationContext(), updateInfo);
                            }
                        }
                    }
                }

                @Override
                public void onHttpError(ExceptionHandle.ResponeThrowable e) {

                }
            });
        subscribeRx(d);
    }

    protected void addMarkers() {
        if (listDevices == null) {
            return;
        }
        for (DeviceInfo device : listDevices) {
            addMarker(device);
        }
    }

    @SuppressWarnings("CheckResult")
    protected void startLocationWithPermission() {
        rxPermissions.requestEach(Manifest.permission.ACCESS_COARSE_LOCATION)
            .subscribe(permission -> {
                if (permission.granted) {
                    startLocation();
                } else if (permission.shouldShowRequestPermissionRationale) {
                    showSettingDlg(getString(R.string.per_loc_hint), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            PermissionUtil.goIntentSetting(MainActivityParent.this);
                        }
                    });
                } else {

                }
            });
    }

    protected abstract void startLocation();

    //设置地图的模式--卫星或普通
    protected abstract void changeMapMode(int iType);

    //更新设备在地图上的显示
    protected abstract void addDevicesOnMap();

    //显示聚合
    protected abstract void renderingClusters(ArrayList<ClusterDevice> clusterDatas);

    //移动地图视角
    protected abstract void animateCamera(double lat, double lng, float zoomLevel);

    //移动地图视角
    protected abstract void animateCameraInclue(ArrayList<DeviceInfo> listDevices);

    //增加marker
    protected abstract void addMarker(DeviceInfo device);

    //初始化地图数据，一些参数设置、监听等
    protected abstract void initLocation();

    protected abstract void addFenceOnMap(Fence fence, boolean bSetZoom);

    protected abstract void removeFenceOnMap();

    protected abstract void showDevTitle();

    protected abstract void hideDevTitle();

    interface OnSendCmdResult {
        void onResult(String imei, String id);
    }

    interface OnAllCmdsResult {
        void onResult(DeviceInfo dev);
    }
}
