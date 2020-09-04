package com.coomix.app.all.service;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.os.IBinder;
import android.telephony.CellLocation;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.telephony.cdma.CdmaCellLocation;
import android.telephony.gsm.GsmCellLocation;
import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.coomix.app.all.AllOnlineApp;
import com.coomix.app.all.BuildConfig;
import com.coomix.app.all.Constant;
import com.coomix.app.all.service.ServiceAdapter.ServiceAdapterCallback;
import com.coomix.app.all.util.StringUtil;
import com.coomix.app.framework.app.Result;
import com.coomix.app.all.manager.DomainManager;
import com.coomix.app.all.util.PermissionUtil;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

/**
 *  位置上传服务
 */
public class LocationService extends Service implements ServiceAdapterCallback {

    private LocationListener mLocationListener = new LocationListener();
    public AMapLocationClient mLocationClient = null;
    private AMapLocationClientOption mLocationClientOption;
    private ServiceAdapter mServiceAdapter;
    private int mUploadTaskID = -1;
    /** 上传最小间隔 */
    private static final int UPLOAD_INTERVAL = 60 * 1000;

    /** 最后一次上传的时间 */
    private long lastTime = 0;
    /** 最后一次上传的基站id */
    private int lastCid = 0;
    /** 当前基站id */
    private int cid = 0;
    private TelephonyManager tel;

    public class TelLocationListener extends PhoneStateListener {
        @Override
        public void onCellLocationChanged(CellLocation cellLocation) {
            super.onCellLocationChanged(cellLocation);
            if (tel != null) {
                // 获取当前主机站
                if (cellLocation instanceof CdmaCellLocation) {
                    cid = ((CdmaCellLocation) cellLocation).getBaseStationId();
                } else if (cellLocation instanceof GsmCellLocation) {
                    cid = ((GsmCellLocation) cellLocation).getCid();
                }
            }
            startLocation();
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onCreate() {
        super.onCreate();

        mServiceAdapter = ServiceAdapter.getInstance(this);
        mServiceAdapter.registerServiceCallBack(this);

        tel = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);

        try {
            tel.listen(new TelLocationListener(), PhoneStateListener.LISTEN_CELL_LOCATION);
        } catch (Exception e) {
            e.printStackTrace();
        }

        mLocationClient = new AMapLocationClient(getApplicationContext());
        mLocationClient.setLocationListener(mLocationListener);
        mLocationClient.setLocationOption(getLocationClientOption());
        startLocation();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        try {
            if (mServiceAdapter != null) {
                mServiceAdapter.unregisterServiceCallBack(this);
            }

            if (mLocationClient != null) {
                mLocationClient.unRegisterLocationListener(mLocationListener);
                mLocationClient.stopLocation();
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    private void startLocation() {
        if (PermissionUtil.checkLocationPermission(this)) {
            mLocationClient.startLocation();
        }
    }

    /**
     * 获取特定的百度LocationClientOption
     */
    private AMapLocationClientOption getLocationClientOption() {
        mLocationClientOption = new AMapLocationClientOption();
        mLocationClientOption.setGpsFirst(true);
        int accuracy = AllOnlineApp.getAppConfig().getAndroid_location_accuracy();
        AMapLocationClientOption.AMapLocationMode mode;
        if(accuracy == 0)
        {
            mode = AMapLocationClientOption.AMapLocationMode.Battery_Saving;
        }
        else if(accuracy == 1)
        {
            mode = AMapLocationClientOption.AMapLocationMode.Device_Sensors;
        }
        else
        {
            mode = AMapLocationClientOption.AMapLocationMode.Hight_Accuracy;
        }

        mLocationClientOption.setLocationMode(mode);
        // 设置发起定位请求的间隔时间为10000ms
        mLocationClientOption.setInterval(AllOnlineApp.getAppConfig().getAndroid_location_interval());
        mLocationClientOption.setNeedAddress(true);
        return mLocationClientOption;
    }

    private class LocationListener implements AMapLocationListener {

        @Override
        public void onLocationChanged(AMapLocation location) {
            if (null != location) {
                AllOnlineApp.setCurrentLocation(location);
                if (BuildConfig.DEBUG) {
                    gpsInfoLog(location);
                }
                if (cid != 0 && lastCid != cid) {
                    // 基站有变化
                    lastCid = cid;
                    cid = 0;
                    long nowTime = System.currentTimeMillis();
                    if (nowTime - lastTime > UPLOAD_INTERVAL) {
                        // 最小上传位置间隔
                        lastTime = nowTime;
                    }
                }
            }
        }
    }

    /**
     * 上传gps,
     * @param location
     */
    private void uploadGps(AMapLocation location, int cid) {
        long now = 0;
        if(location==null){
            return;
        }        
        try {
            //"yyyy-MM-dd HH:mm:ss"
            now = location.getTime();
        } catch (Exception e) {
            if (BuildConfig.DEBUG) {
                e.printStackTrace();
            }
        }
        if (now == 0) {
            now = System.currentTimeMillis();
        }
        /* begin 将数据保存到sd卡，用于分析每天上传的点（cid基站id变化频率） */
        // String str = "time:"
        // + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(now)
        // + " lng:" + location.getLongitude() + " lat:"
        // + location.getLatitude() + " cid:" + cid;
        // saveGps(str);
        // if (Constant.IS_DEBUG_MODE) {
        // System.out.println("BusOnlineService saveGps:" + str);
        // }
        /* end 将数据保存到sd卡，用于分析每天上传的点（cid基站id变化频率） */

        if(DomainManager.sRespDomainAdd !=null && !StringUtil.isTrimEmpty(DomainManager.sRespDomainAdd.domainGapi)){
            mUploadTaskID = mServiceAdapter.uploadLocation(this.hashCode(), location.getLongitude(),
                location.getLatitude(), now / 1000, "BAIDU");
        }
    }

    private void saveGps(String log) {
        File baseDir = Environment.getExternalStorageDirectory();
        FileWriter writer;
        try {
            File dir = new File(baseDir.getAbsolutePath() + "/Coomix");
            if (!dir.exists()) {
                dir.mkdirs(); // create folders where write files
            }

            File file = new File(dir, "locationInfo.txt");
            writer = new FileWriter(file, true);
            writer.append(log);
            writer.append("\r\n");
            writer.flush();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
        }
    }

    /**
     * 定位信息打印
     *
     * @param location location
     */
    private void gpsInfoLog(AMapLocation location) {

//        // ///////////////////////////////////////////////////////////////////////////
//        StringBuffer sb = new StringBuffer();
//        /*
//         * sb.append("当前时间 : "); sb.append(location.getTime());
//         */
//        sb.append("错误码 : ");
//        sb.append(location.getLocType());
//        sb.append("纬度 : ");
//        sb.append(location.getLatitude());
//        sb.append("经度 : ");
//        sb.append(location.getLongitude());
//        sb.append("半径 : ");
//        sb.append(location.getRadius());
//        sb.append("时间: ");
//        sb.append(location.getTime());
//        if (location.getLocType() == BDLocation.TypeGpsLocation) {
//            sb.append("速度 : ");
//            sb.append(location.getSpeed());
//            sb.append("卫星数 : ");
//            sb.append(location.getSatelliteNumber());
//        } else if (location.getLocType() == BDLocation.TypeNetWorkLocation) {
//            sb.append("地址 : ");
//            sb.append(location.getAddrStr());
//        }
//        try {
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
    }

    /**
     * 判断服务是否处于运行状态.
     * 
     * @param servicename
     * @param context
     * @return
     */
    public static boolean isServiceRunning(String servicename, Context context) {
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<RunningServiceInfo> infos = am.getRunningServices(30);
        for (RunningServiceInfo info : infos) {
            if (servicename.equals(info.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void callback(int messageId, Result result) {
        try {
            if (result.statusCode == Result.OK) {
                if (result.apiCode == Constant.FM_APIID_UPLOAD_LOCATION && mUploadTaskID == messageId) {
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
