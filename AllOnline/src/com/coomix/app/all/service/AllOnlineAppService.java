package com.coomix.app.all.service;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.services.core.PoiItem;
import com.amap.api.services.geocoder.GeocodeResult;
import com.amap.api.services.geocoder.GeocodeSearch;
import com.amap.api.services.geocoder.RegeocodeResult;
import com.coomix.app.all.AllOnlineApp;
import com.coomix.app.all.model.bean.LocationInfo;
import com.coomix.app.framework.app.BaseService;
import java.util.List;

public class AllOnlineAppService extends BaseService {

    /**
     * 百度定位间隔 默认值10s
     */
    public static final int BAIDU_LOCATION_SCANSPAN = 10000;
    public static final String BRCR_REQUEST_LOCATION_ACTION = "com.coomix.app.REQUEST_LOCATION_ACTION";
    private static int BAIDU_LOCATION_GPS_AUTOCLOSE = 2 * 60 * 1000;
    private static int BAIDU_LOCATION_GPS_DISTANCE = 2000;
    protected final IBinder mBinder = new LocalBinder();
    public AMapLocationClient mLocationClient = null;
    private AllOnlineApiClient mClient;
    private LocationListener mLocationListener = new LocationListener();
    private AMapLocationClientOption mLocationClientOption;
    private GeocodeSearch mGeocoderSearch;
    private MyGeoCoderResultListener myGeoCoderResultListener = new MyGeoCoderResultListener();
    private Context mContext;
    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (Intent.ACTION_SCREEN_OFF.equals(action)) {
                System.out.println("screen off,stop locate");
            } else if (Intent.ACTION_SCREEN_ON.equals(action)) {
                System.out.println("screen on,start locate");
            } else if (BRCR_REQUEST_LOCATION_ACTION.equals(action)) {
                startLocation();

                if (mGeocoderSearch == null) {
                    mGeocoderSearch = new GeocodeSearch(AllOnlineAppService.this);
                    mGeocoderSearch.setOnGeocodeSearchListener(myGeoCoderResultListener);
                }
                if (mLocationClient != null) {
                    mLocationClient.startLocation();
                }
            }
        }
    };

    public AllOnlineAppService() {
    }

    public AllOnlineAppService(Context context) {
        this.mContext = context;
    }

    public void onCreate() {
        super.onCreate();
        mClient = new AllOnlineApiClient(this);

        // mLocationClient = new AMapLocationClient(getApplicationContext());
        // mLocationClient.setLocationListener(mLocationListener);
        // mLocationClient.setLocationOption(getLocationClientOption());
        //
        // startLocation();
        //
        // registerReceiver();
    }

    /**
     * 获取特定的百度LocationClientOption
     */
    private AMapLocationClientOption getLocationClientOption() {
        mLocationClientOption = new AMapLocationClientOption();
        mLocationClientOption.setGpsFirst(true);
        // option.setOpenGps(true);
        // option.setProdName("CoomixBusUploadGps");
        //mLocationClientOption.setOnceLocation(true);
        mLocationClientOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
        //mLocationClientOption.setLocationMode(AMapLocationMode.Battery_Saving);
        // option.setCoorType("bd09ll");// 返回的定位结果是百度经纬度,默认值gcj02
        // option.setLocationMode(LocationClientOption.LocationMode.Battery_Saving);
        mLocationClientOption.setInterval(BAIDU_LOCATION_SCANSPAN);// 设置发起定位请求的间隔时间为10000ms
        mLocationClientOption.setNeedAddress(true);
        return mLocationClientOption;
    }

    ///**
    // * 百度定位策略，gps控制
    // *
    // * @param location location
    // */
    //private void gpsControl(AMapLocation location)
    //{
    //    if (location.getLocationType() == AMapLocation.LOCATION_TYPE_GPS)
    //    {
    //        // GPS定位成功, 记载当前时间作为最后一次gps定位时间
    //        AllOnlineApp.lastGpsLocationTime = System.currentTimeMillis();
    //    }
    //    else
    //    {
    //        // 定位结果非GPS
    //        if (mLocationClientOption.getLocationMode() == AMapLocationClientOption.AMapLocationMode.Hight_Accuracy)
    //        {
    //            // 如果不为基站定位,则判断是否连续*分钟gps定位失败
    //            if (System.currentTimeMillis() - AllOnlineApp.lastGpsLocationTime >= BAIDU_LOCATION_GPS_AUTOCLOSE)
    //            {
    //                AllOnlineApp.lastGpsLocationTimeGpsIsOpen = false;
    //                // 当连续*分钟未进行GPS定位，关闭gps，并采用基站定位
    //                // if (GpsUtil.isGPSEnable(getApplicationContext())) {
    //                // // 当前gps处于打开状态，则关闭gps
    //                // GpsUtil.toggleGPS(getApplicationContext());
    //                // BusOnlineApp.lastGpsLocationTimeGpsIsOpen = true;
    //                // }
    //                AllOnlineApp.lastGpsLocation = location;
    //                // 定位方式改为基站定位
    //                mLocationClient.stopLocation();
    //                mLocationClientOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Battery_Saving);
    //                mLocationClientOption.setGpsFirst(false);
    //                mLocationClient.setLocationOption(mLocationClientOption);
    //                mLocationClient.startLocation();
    //            }
    //        }
    //        else if (mLocationClientOption.getLocationMode() == AMapLocationClientOption.AMapLocationMode.Battery_Saving)
    //        {
    //            // 定位模式为基站定位，则判断移动距离是否超过*米
    //            LatLng lastGpsLatLng = new LatLng(AllOnlineApp.lastGpsLocation.getLatitude(),
    //                    AllOnlineApp.lastGpsLocation.getLongitude());
    //            LatLng nowGpsLatLng = new LatLng(location.getLatitude(), location.getLongitude());
    //            // 移动距离（即：最后一次高精度定位，与当前位置的距离）
    //            double distance = AMapUtils.calculateLineDistance(lastGpsLatLng, nowGpsLatLng);
    //            if (distance >= BAIDU_LOCATION_GPS_DISTANCE)
    //            {
    //                // 当位置移动超过*米，则判断是否需要打开gps，并采用高精度定位
    //                // if (BusOnlineApp.lastGpsLocationTimeGpsIsOpen
    //                // && !GpsUtil
    //                // .isGPSEnable(getApplicationContext())) {
    //                // // 当前gps处于关闭状态，则打开gps
    //                // GpsUtil.toggleGPS(getApplicationContext());
    //                // }
    //                AllOnlineApp.lastGpsLocationTime = System.currentTimeMillis();
    //                mLocationClient.stopLocation();
    //                // 定位方式改为高精度
    //                mLocationClient.setLocationOption(mLocationClientOption);
    //                mLocationClientOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
    //                mLocationClientOption.setGpsFirst(true);
    //                mLocationClient.startLocation();
    //            }
    //        }
    //    }
    //}

    private void startLocation() {
        if (mGeocoderSearch == null) {
            mGeocoderSearch = new GeocodeSearch(AllOnlineAppService.this);
            mGeocoderSearch.setOnGeocodeSearchListener(myGeoCoderResultListener);
        }
        mLocationClient.startLocation();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    private class LocationListener implements AMapLocationListener {
        @SuppressLint("SimpleDateFormat")
        @Override
        public void onLocationChanged(AMapLocation location) {
            if (location != null) {
                //gpsControl(location);
                AllOnlineApp.setCurrentLocation(location);
            }
        }
    }

    private class MyGeoCoderResultListener implements GeocodeSearch.OnGeocodeSearchListener {
        @Override
        public void onGeocodeSearched(GeocodeResult result, int rCode) {
        }

        @Override
        public void onRegeocodeSearched(RegeocodeResult result, int rCode) {
            if (rCode == 0) {
                if (result != null && result.getRegeocodeAddress() != null &&
                    result.getRegeocodeAddress().getFormatAddress() != null) {
                    List<PoiItem> localList = result.getRegeocodeAddress().getPois();
                    if (localList != null && localList.size() != 0) {
                        PoiItem poiItem = localList.get(0);
                        LocationInfo mCurrentLocationInfo = new LocationInfo();
                        mCurrentLocationInfo.setCurrent(true);
                        mCurrentLocationInfo.setCity(poiItem.getCityName());
                        mCurrentLocationInfo.setName(poiItem.getTitle());
                        mCurrentLocationInfo.setAddress(poiItem.getAdName());
                        mCurrentLocationInfo.setLatitude(poiItem.getLatLonPoint().getLatitude());
                        mCurrentLocationInfo.setLongitude(poiItem.getLatLonPoint().getLongitude());
                        //AllOnlineApp.mBusOnlineApp.setCurrentLocationInfo(mCurrentLocationInfo);
                    } else {
                    }
                } else {
                }
            } else if (rCode == 27) {
                // 搜索失败,请检查网络连接！
            } else if (rCode == 32) {
                // key验证失败
            } else {
                // 其它错误
            }
        }
    }

    public class LocalBinder extends Binder {
        public AllOnlineAppService getService() {
            return AllOnlineAppService.this;
        }
    }
}
