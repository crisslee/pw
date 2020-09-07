package com.coomix.app.all.ui.main;

import android.content.Intent;
import android.graphics.Point;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.location.LocationClientOption.LocationMode;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BaiduMap.OnMapClickListener;
import com.baidu.mapapi.map.BaiduMap.OnMapLoadedCallback;
import com.baidu.mapapi.map.BaiduMap.OnMapStatusChangeListener;
import com.baidu.mapapi.map.BaiduMap.OnMarkerClickListener;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.CircleOptions;
import com.baidu.mapapi.map.InfoWindow;
import com.baidu.mapapi.map.MapPoi;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.Overlay;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.map.PolylineOptions;
import com.baidu.mapapi.map.Stroke;
import com.baidu.mapapi.map.offline.MKOLUpdateElement;
import com.baidu.mapapi.map.offline.MKOfflineMap;
import com.baidu.mapapi.map.offline.MKOfflineMapListener;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.model.LatLngBounds;
import com.baidu.mapapi.utils.DistanceUtil;
import com.coomix.app.all.AllOnlineApp;
import com.coomix.app.all.Constant;
import com.coomix.app.all.R;
import com.coomix.app.all.manager.MapIconManager;
import com.coomix.app.all.markColection.baidu.Cluster;
import com.coomix.app.all.markColection.baidu.ClusterDevice;
import com.coomix.app.all.model.bean.DeviceInfo;
import com.coomix.app.all.model.bean.Fence;
import com.coomix.app.all.service.MyOrientationListener;
import com.coomix.app.all.widget.ClickableToast;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class BMainActivity extends MainActivityParent implements BDLocationListener, OnMapLoadedCallback,
    OnMarkerClickListener, OnMapClickListener, OnMapStatusChangeListener {

    private final String MARKER_DATA = "data";
    private ClusterTask mClusterTask = new ClusterTask();
    private final ReadWriteLock mClusterTaskLock = new ReentrantReadWriteLock();
    // 定位
    private LocationClientOption mLocationOption;
    private MyOrientationListener myOrientationListener;
    private int mXDirection;
    public LocationClient mLocationClient = null;
    private MyLocationData myLocData = null;
    protected MapView mMapView = null;
    protected BaiduMap mBaiduMap = null;
    private Overlay circle;
    private ArrayList<Overlay> listOverlays;

    @SuppressWarnings("unchecked")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mMapView = new MapView(this);
        layoutMapView.addView(mMapView, 0);
        mBaiduMap = mMapView.getMap();

        initLocation();
        initOritationListener();
        startLocationWithPermission();
    }

    //初始化方向传感器
    private void initOritationListener()
    {
        myOrientationListener = new MyOrientationListener(getApplicationContext());
        myOrientationListener.setOnOrientationListener(new MyOrientationListener.OnOrientationListener(){
            @Override
            public void onOrientationChanged(float x){
                mXDirection =(int) x ;
            }
        });
    }
    @Override
    public void onResume() {
        if (mMapView != null) {
            mMapView.onResume();
        }
        super.onResume();
    }

    @Override
    public void onPause() {
        if (mMapView != null) {
            mMapView.onPause();
        }
        super.onPause();
        stopLocation();
    }

    @Override
    public void onDestroy() {
        if (mBaiduMap != null) {
            mBaiduMap.clear();
        }
        if (mMapView != null) {
            mMapView.onDestroy();
        }

        if (mClusterTask != null) {
            mClusterTask.cancel(true);
        }

        clearOverlay();
        stopLocation();
        destroyLocation();
        super.onDestroy();
    }

    // 百度离线地图
    private void detectOfflineUpdate() {
        boolean hasUpdate = false;
        MKOfflineMap offline = new MKOfflineMap();
        MKOfflineMapListener listener = new MKOfflineMapListener() {
            @Override
            public void onGetOfflineMapState(int type, int state) {
                switch (type) {
                    case MKOfflineMap.TYPE_DOWNLOAD_UPDATE: {
                        // 处理下载进度更新提示
                    }
                    break;
                    case MKOfflineMap.TYPE_NEW_OFFLINE:
                        // 有新离线地图安装
                        break;
                    case MKOfflineMap.TYPE_VER_UPDATE:
                        // 版本更新提示
                        // MKOLUpdateElement e = mOffline.getUpdateInfo(state);
                        break;
                    default:
                        break;
                }
            }
        };
        offline.init(listener);
        ArrayList<MKOLUpdateElement> localMapList = offline.getAllUpdateInfo();

        if (localMapList != null) {
            for (MKOLUpdateElement mkolUpdateElement : localMapList) {
                if (mkolUpdateElement.update) {
                    hasUpdate = true;
                    break;
                }
            }
        }

        if (offline != null) {
            offline.destroy();
        }
        localMapList = null;

        if (hasUpdate) {
            final ClickableToast toast = new ClickableToast(this);
            mHandler.post(new Runnable() {

                @Override
                public void run() {
                    toast.show();
                }
            });

            toast.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent updateIntent = new Intent(BMainActivity.this, OfflineBMapActivity.class);
                    updateIntent.putExtra("FROM", true);
                    startActivity(updateIntent);
                }
            });

            AllOnlineApp.sHasUpdate = true;
        }
    }

    private void addClusters() {
        if (listDevices == null) {
            return;
        }
        mClusterTaskLock.writeLock().lock();
        try {
            // Attempt to cancel the in-flight request.
            mClusterTask.cancel(true);
            mClusterTask = new ClusterTask();
            mClusterTask.execute();
        } finally {
            mClusterTaskLock.writeLock().unlock();
        }
    }

    /**
     * Runs the clustering algorithm in a background thread, then re-paints when
     * results come back.
     */
    private class ClusterTask extends AsyncTask<Void, Void, ArrayList<ClusterDevice>> {
        @Override
        protected ArrayList<ClusterDevice> doInBackground(Void... params) {
            if (listDevices == null) {
                return null;
            }
            Cluster cluster = new Cluster(BMainActivity.this, mMapView, mGridSize);
            return cluster.createBaiduCluster(listDevices);
        }

        @Override
        protected void onPostExecute(ArrayList<ClusterDevice> result) {
            renderingClusters(result);
        }
    }

    /**
     * 是否第一次显示设备， 为true时，会在获取到设备后调用firstShowDevice()初始化地图显示的位置;
     */
    @Override
    public void onMapLoaded() {
        addDevicesOnMap();
    }

    @Override
    protected void startLocation() {
        if (mLocationClient != null && !mLocationClient.isStarted()) {
            // 开始定位
            mLocationClient.start();
        }
    }

    private void stopLocation() {
        try {
            if (mLocationClient != null && mLocationClient.isStarted()) {
                mLocationClient.stop();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 定位选择
    @Override
    protected void initLocation() {
        mBaiduMap.getUiSettings().setRotateGesturesEnabled(false);
        mBaiduMap.getUiSettings().setCompassEnabled(false);
        mBaiduMap.getUiSettings().setOverlookingGesturesEnabled(false);

        mMapView.showScaleControl(true);
        mMapView.showZoomControls(false);

        // 设置地图监听，当地图状态发生改变时，进行点聚合运算
        mBaiduMap.setOnMapStatusChangeListener(this);
        mBaiduMap.setOnMarkerClickListener(this);
        mBaiduMap.setOnMapClickListener(this);
        mBaiduMap.setOnMapLoadedCallback(this);
        // 开启定位图层
        mBaiduMap.setMyLocationEnabled(true);

        mLocationClient = new LocationClient(getApplicationContext());
        myLocData = new MyLocationData.Builder().build();
        mLocationOption = new LocationClientOption();

        mLocationOption.setProdName("coomixLoc");
        mLocationOption.setCoorType("bd09ll");// 百度原始经纬度
        mLocationOption.setLocationMode(LocationMode.Hight_Accuracy);
        mLocationOption.setIsNeedAddress(true);
        mLocationOption.setScanSpan(Constant.MY_LOCATION_INTERVAL); // 原刷新60秒位置刷新慢的问题
        mLocationClient.setLocOption(mLocationOption);
        mLocationClient.registerLocationListener(this);

        mBaiduMap.setMyLocationConfiguration(new MyLocationConfiguration(MyLocationConfiguration.LocationMode.NORMAL,
            true, BitmapDescriptorFactory.fromResource(R.drawable.locate_current_icon)));
    }

    private void destroyLocation() {
        if (mLocationClient != null) {
            mLocationClient.unRegisterLocationListener(this);
        }
    }

    @Override
    public void onReceiveLocation(BDLocation location) {
        if (location == null) {
            return;
        }

        myLocData = new MyLocationData.Builder().accuracy(location.getRadius())
            .direction(mXDirection) //此处设置开发者获取到的方向信息，顺时针0-360
            .latitude(location.getLatitude()).longitude(location.getLongitude())
            .satellitesNum(location.getSatelliteNumber()).build();
        mBaiduMap.setMyLocationData(myLocData);
        //stopLocation();
    }

    @Override
    public boolean onMarkerClick(Marker clickMarker) {
        Bundle extra = clickMarker.getExtraInfo();
        if (extra != null) {
            //setMyLocationImage(false);
            Object obj = extra.getSerializable(MARKER_DATA);
            if (obj != null && obj instanceof DeviceInfo) {
                DeviceInfo device = (DeviceInfo) obj;
                iCurrentShowDeviceIndex = deviceManager.getValidDevIndex(device);
                deviceMarkerTap(device, true);
            } else if (obj != null && obj instanceof ClusterDevice) {
                // 聚合物
                ClusterDevice clusterDevice = (ClusterDevice) obj;
                deviceClusterTap(clickMarker.getPosition(), clusterDevice);
            }
        }
        return false;
    }

    private void deviceClusterTap(LatLng latLng, ClusterDevice clusterDevice) {
        if(mBaiduMap != null && mBaiduMap.getMapStatus() != null){
            zoomLevel = mBaiduMap.getMapStatus().zoom;
        }
        zoomLevel += 1;
        if(zoomLevel >= mBaiduMap.getMaxZoomLevel()){
            zoomLevel = mBaiduMap.getMaxZoomLevel();
        }
        MapStatus ms = new MapStatus.Builder().target(latLng).zoom(zoomLevel).build();
        mBaiduMap.animateMapStatus(MapStatusUpdateFactory.newMapStatus(ms));
        DrawLines(myLocData.latitude,myLocData.longitude,latLng.latitude,latLng.longitude);
    }

    private LatLngBounds calculateBounds(ArrayList<DeviceInfo> markers) {
        if (markers == null || markers.size() == 0) {
            return null;
        }
        // 故宫经纬度
        double minLat = 39.54;
        double minLng = 116.23;
        double maxLat = 39.54;
        double maxLng = 116.23;

        for (int i = 0; i < markers.size(); i++) {
            DeviceInfo state = markers.get(i);
            if (state == null) {
                continue;
            }
            if (i == 0) {
                minLat = state.getLat();
                minLng = state.getLat();
                maxLat = state.getLat();
                maxLng = state.getLng();
            } else {
                if (state.getLat() < minLat) {
                    minLat = state.getLat();
                }
                if (state.getLng() < minLng) {
                    minLng = state.getLng();
                }
                if (state.getLat() > maxLat) {
                    maxLat = state.getLat();
                }
                if (state.getLng() > maxLng) {
                    maxLng = state.getLng();
                }
            }
        }
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        builder.include(new LatLng(minLat, minLng));
        builder.include(new LatLng(maxLat, maxLng));
        return builder.build();
    }

    @Override
    public void onMapClick(LatLng arg0) {
        if (currDevice != null && mHandler != null) {
            mHandler.removeMessages(REFRESH_SINGLE);
            mHandler.removeMessages(OPEN_TEMP_CMD);
            if (needSpecialCmd(currDevice)) {
                closeTempCmd(currDevice);
            }
            currDevice = null;
            lastSelectedDevice = null;
        }
        hideBottomCarInfo();
    }

    @Override
    public void onMapPoiClick(MapPoi arg0) {
    }

    @Override
    public void onMapStatusChange(MapStatus arg0) {
    }

    @Override
    public void onMapStatusChangeFinish(MapStatus arg0) {
        final float oldZoomLevel = zoomLevel;
        if(arg0 != null) {
            zoomLevel = arg0.zoom;
        }

        if (oldZoomLevel != zoomLevel && listDevices != null && listDevices.size() > Constant.LIMITCARNUM) {
            //如果是聚合显示的，地图缩放变化后要跟着刷新
            addDevicesOnMap();
        }
    }

    @Override
    public void onMapStatusChangeStart(MapStatus arg0) {
    }

    //设置地图的模式--卫星或普通
    @Override
    protected void changeMapMode(int iType) {
        if (iType == Constant.MAP_NORMAL) {
            mBaiduMap.setMapType(BaiduMap.MAP_TYPE_NORMAL);// 普通地图模式
        } else {
            mBaiduMap.setMapType(BaiduMap.MAP_TYPE_SATELLITE);// 卫星地图模式
        }
    }

    //点击marker后的动作
    @Override
    protected void deviceMarkerTap(DeviceInfo device, boolean isAnimated) {
        if (device == null) {
            return;
        }
        super.deviceMarkerTap(device, isAnimated);

        if (isAnimated && zoomLevel < 18) {
            zoomLevel = 18f;
        }
        mBaiduMap.setMapStatus(MapStatusUpdateFactory.newLatLngZoom(new LatLng(device.getLat(), device.getLng()),
            zoomLevel));

        //设置窗口显示内容
        showBottomCarInfo();
        DrawLines(myLocData.latitude,myLocData.longitude,device.getLat(),device.getLng());
        updateBottomView(device, true);
    }

    //更新设备在地图上的显示
    @Override
    protected void addDevicesOnMap() {
        if (listOverlays == null) {
            listOverlays = new ArrayList<Overlay>();
        } else {
            clearOverlay();
        }

        //我的位置弹窗
        //showMyLocation();

        if (listDevices != null && listDevices.size() > 0) {
            if (listDevices.size() < Constant.LIMITCARNUM) {
                // 直接显示车辆
                addMarkers();
            } else {
                // 聚合显示
                addClusters();
            }
        }
    }

    private void clearOverlay(){
        if(listOverlays != null){
            for (Overlay overlay : listOverlays) {
                if (overlay != null) {
                    overlay.remove();
                }
            }
            listOverlays.clear();
        }
    }

    //显示聚合
    @Override
    protected synchronized void renderingClusters(ArrayList<ClusterDevice> clusterDatas) {
        if (clusterDatas == null) {
            return;
        }

        for (ClusterDevice clusterMarker : clusterDatas) {
            View drawableView = LayoutInflater.from(this).inflate(R.layout.drawable_mark, null);
            TextView text = (TextView) drawableView.findViewById(R.id.drawble_mark);
            text.setPadding(3, 3, 3, 3);

            ArrayList<DeviceInfo> listDevice = new ArrayList<DeviceInfo>();
            listDevice = clusterMarker.getmMarkers();

            if (listDevice == null) {
                continue;
            }

            int markNum = listDevice.size();
            if (zoomLevel < Constant.CLUSTER_LEVEL && markNum >= 2) {
                text.setText(String.valueOf(markNum));
                text.setBackgroundResource(getClusterImageResouce(markNum));

                LatLng ll = clusterMarker.getmCenterBaidu(1, Constant.MAP_TYPE_BAIDU);
                Bundle extra = new Bundle();
                extra.putSerializable(MARKER_DATA, clusterMarker);
                BitmapDescriptor bd = BitmapDescriptorFactory.fromView(drawableView);
                MarkerOptions options = new MarkerOptions().position(ll).icon(bd).zIndex(9).draggable(false)
                    .extraInfo(extra).anchor(0.5f, 0.5f);
                Overlay overlay = mBaiduMap.addOverlay(options);
                listOverlays.add(overlay);
            } else {
                for (DeviceInfo device : listDevice) {
                    addMarker(device);
                }
            }
        }
    }

    //移动地图视角
    @Override
    protected void animateCamera(double lat, double lng, float zoomLevel) {
        if (zoomLevel <= 3) {
            mBaiduMap.animateMapStatus(MapStatusUpdateFactory.newLatLng(new LatLng(lat, lng)));
        } else {
            mBaiduMap.animateMapStatus(MapStatusUpdateFactory.newLatLngZoom(new LatLng(lat, lng), zoomLevel));
        }
    }

    //移动地图视角
    @Override
    protected void animateCameraInclue(ArrayList<DeviceInfo> listDevices) {
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        for (DeviceInfo deviceInfo : listDevices) {
            if (deviceInfo != null) {
                builder.include(new LatLng(deviceInfo.getLat(), deviceInfo.getLng()));
            }
        }
        mBaiduMap.animateMapStatus(MapStatusUpdateFactory.newLatLngBounds(builder.build()));
    }

    @Override
    protected void addMarker(DeviceInfo device) {
        //增加marker
        if (device == null) {
            return;
        }

        BitmapDescriptor bd = MapIconManager.getInstance().getBaiduIcon(device);
        if (bd != null) {
            LatLng ll = new LatLng(device.getLat(), device.getLng());
            Bundle extra = new Bundle();
            extra.putSerializable(MARKER_DATA, device);
            // 百度地图(3.6.0)角度逆时针，自己的数据角度顺时针
            MarkerOptions options = new MarkerOptions().position(ll).icon(bd).zIndex(9).draggable(false)
                .rotate(-1 * device.getCourse()).extraInfo(extra).anchor(0.5f, 0.5f);
            Overlay overlay = mBaiduMap.addOverlay(options);
            listOverlays.add(overlay);
        }
    }

    @Override
    public void onMapStatusChangeStart(MapStatus mapStatus, int i) {

    }

    @Override
    protected void addFenceOnMap(Fence fence, boolean bSetZoom) {
        if (fence == null || mBaiduMap == null) {
            return;
        }
        removeFenceOnMap();
        LatLng latLng = new LatLng(fence.getLat(), fence.getLng());
        CircleOptions options = new CircleOptions().center(latLng).radius(fence.getRadius())
            .fillColor(Fence.FILL_COLOR).stroke(new Stroke(2, Fence.STROKE_COLOR));
        circle = mBaiduMap.addOverlay(options);
        if (bSetZoom) {
            zoomToSpan(latLng, fence.getRadius());
        }
    }

    @Override
    protected void removeFenceOnMap() {
        if (circle != null) {
            circle.remove();
            circle = null;
        }
    }

    private void zoomToSpan(LatLng center, int radius) {
        // 任意给一个经纬度计算计算一个纬度差的距离(纬度方向认为是均匀变化的)
        // 一纬度的距离 , 纬度最大90
        double distanceOfLat = DistanceUtil.getDistance(center, new LatLng(center.latitude < 89 ? center.latitude + 1
            : center.latitude - 1, center.longitude));
        double latRadius = radius / distanceOfLat;

        // 一经度的距离(同纬度下，一个经度差的距离认为是相同的) 经度最大180
        double distanceOfLng = DistanceUtil.getDistance(center, new LatLng(center.latitude, center.longitude < 179 ?
            center.longitude + 1 : center.longitude - 1));
        double lngRadius = radius / distanceOfLng;

        if (latRadius > 180 || lngRadius > 360) {
            return;
        }

        double neLat = center.latitude + latRadius;
        double neLng = center.longitude + lngRadius;
        if (neLat > 90) {
            neLat = 180 - neLat;
        }
        if (neLng > 180) {
            neLng = 360 - neLng;
        }

        double swLat = center.latitude - latRadius;
        double swLng = center.longitude - lngRadius;
        if (swLat < -90) {
            swLat = -180 - swLat;
        }
        if (swLng < -180) {
            swLng = -360 - swLng;
        }

        LatLng northeast = new LatLng(neLat, neLng);
        LatLng southwest = new LatLng(swLat, swLng);
        LatLngBounds bounds = new LatLngBounds.Builder().include(northeast).include(southwest).build();

        mBaiduMap.animateMapStatus(MapStatusUpdateFactory.newLatLngBounds(bounds));
    }

    @Override
    protected void showDevTitle() {
        if(textDevTitle == null){
            initDevTitleView();
        }
        if(currDevice != null && mBaiduMap != null) {
            InfoWindow infoWindow = new InfoWindow(textDevTitle, new LatLng(currDevice.getLat(), currDevice.getLng()),
                getResources().getDimensionPixelSize(R.dimen.map_infowindow_y_margin));
            mBaiduMap.showInfoWindow(infoWindow);
            textDevTitle.setText(currDevice.getName());
            textDevTitle.setTextColor(getTextColorByStatus(currDevice));
        }else{
            hideDevTitle();
        }
    }


    @Override
    protected void hideDevTitle() {
        if(mBaiduMap != null) {
            mBaiduMap.hideInfoWindow();
        }
    }

    @Override
    public void onStart()
    {
        myOrientationListener.start();
        super.onStart();
    }

    @Override
    public void onStop()
    {
        myOrientationListener.stop();
        super.onStop();
    }

    public Double Distance(double lat1,double lng1,double lat2,double lng2){
        Double R = 6370996.81;
        Double x = (lng2 - lng1)*Math.PI*R*Math.cos(((lat1+lat2)/2)*Math.PI/180)/180;
        Double y = (lat2 - lat1)*Math.PI*R/180;


        Double distance = Math.hypot(x, y);   //得到两点之间的直线距离

        return   distance;

    }

    public void DrawLines(double lat1,double lng1,double lat2,double lng2){
        LatLng p1 = new LatLng(lat1, lng1);
        LatLng p2 = new LatLng(lat2, lng2);
        List<LatLng> points = new ArrayList<LatLng>();
        points.add(p1);
        points.add(p2);
        OverlayOptions ooPolyline = new PolylineOptions().width(10).color(0xAAFF0000).points(points);
        mBaiduMap.addOverlay(ooPolyline);
        /*
         * 调用Distance方法获取两点间x,y轴之间的距离
         */
        double cc= Distance(lat1,  lng1,lat2,lng2);

        int length=(int)cc;

        Toast.makeText(this, "您与终端距离"+length+"米", Toast.LENGTH_SHORT).show();
    }
}
