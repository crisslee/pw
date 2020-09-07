package com.coomix.app.all.ui.main;

import android.graphics.Color;
import android.graphics.Point;
import android.graphics.PointF;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationClientOption.AMapLocationMode;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.maps.AMap;
import com.amap.api.maps.AMap.OnCameraChangeListener;
import com.amap.api.maps.AMap.OnMapClickListener;
import com.amap.api.maps.AMap.OnMapLoadedListener;
import com.amap.api.maps.AMap.OnMarkerClickListener;
import com.amap.api.maps.CameraUpdate;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.LocationSource;
import com.amap.api.maps.MapView;
import com.amap.api.maps.Projection;
import com.amap.api.maps.UiSettings;
import com.amap.api.maps.model.BitmapDescriptor;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.CameraPosition;
import com.amap.api.maps.model.Circle;
import com.amap.api.maps.model.CircleOptions;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.LatLngBounds;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.MyLocationStyle;
import com.amap.api.maps.model.PolylineOptions;
import com.coomix.app.all.Constant;
import com.coomix.app.all.R;
import com.coomix.app.all.manager.MapIconManager;
import com.coomix.app.all.markColection.baidu.Cluster;
import com.coomix.app.all.markColection.baidu.ClusterDevice;
import com.coomix.app.all.model.bean.DeviceInfo;
import com.coomix.app.all.model.bean.Fence;
import com.coomix.app.all.service.MyOrientationListener;

import java.util.ArrayList;
import java.util.List;

public class AMainActivity extends MainActivityParent implements OnMapLoadedListener, OnMarkerClickListener,
    AMapLocationListener, OnMapClickListener, OnCameraChangeListener, LocationSource, AMap.InfoWindowAdapter {
    private ClusterTask mClusterTask = new ClusterTask();

    private AMap aMap;
    private MapView mapView;
    private ArrayList<Marker> listMarkers = new ArrayList<Marker>();
    private Marker currentMarker;

    // private Marker markerGps;// 定位雷达小图标
    private OnLocationChangedListener mLocationChangedListener;
    private AMapLocationClient mlocationClient;
    private AMapLocationClientOption mLocationOption;
    private MyOrientationListener myOrientationListener;
    private int mXDirection;
    private Circle circle;

    @SuppressWarnings("unchecked")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        //开始执行父类onCreate
        super.onCreate(savedInstanceState);

        mapView = new MapView(this);
        layoutMapView.addView(mapView, 0);
        mapView.onCreate(savedInstanceState);
        aMap = mapView.getMap();

        aMap.getUiSettings().setRotateGesturesEnabled(false);
        aMap.getUiSettings().setCompassEnabled(false);

        initLocation();
        initOritationListener();
        startLocationWithPermission();
    }

    public void initOritationListener(){
        myOrientationListener = new MyOrientationListener(getApplicationContext());
        myOrientationListener.setOnOrientationListener(new MyOrientationListener.OnOrientationListener(){
            @Override
            public void onOrientationChanged(float x){
                mXDirection =(int) x;
                aMap.setMyLocationRotateAngle(mXDirection);
            }
        });
    }

    // 车辆显示
    @Override
    protected void addDevicesOnMap() {
        if (listMarkers == null) {
            listMarkers = new ArrayList<Marker>();
        } else {
            for (Marker marker : listMarkers) {
                if(marker != null) {
                    marker.remove();
                }
            }
            listMarkers.clear();
        }

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

    @Override
    protected void addMarker(DeviceInfo device) {
        if (device != null) {
            BitmapDescriptor icon = MapIconManager.getInstance().getAmapIcon(device);
            if (icon != null) {
                LatLng ll = new LatLng(device.getLat(), device.getLng());
                MarkerOptions options = new MarkerOptions().position(ll).icon(icon).draggable(false).zIndex(9.0f).anchor(0.5f, 0.5f);
                Marker marker = aMap.addMarker(options);
                marker.setRotateAngle(-1 * device.getCourse());
                marker.setObject(device);
                listMarkers.add(marker);
                if (device.equals(currDevice)) {
                    currentMarker = marker;
                    if(isBottomInfoShow()){
                        showDevTitle();
                    }
                }
            }
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
            Cluster cluster = new Cluster(AMainActivity.this, aMap, mGridSize, mapView);
            return cluster.createAMAPCluster(listDevices);
        }

        @Override
        protected void onPostExecute(ArrayList<ClusterDevice> result) {
            renderingClusters(result);
        }
    }

    @Override
    synchronized protected void renderingClusters(ArrayList<ClusterDevice> clusterDatas) {
        if (clusterDatas == null) {
            return;
        }

        for (ClusterDevice clusterMarker : clusterDatas) {
            View drawableView = LayoutInflater.from(this).inflate(R.layout.drawable_mark, null);
            TextView text = (TextView) drawableView.findViewById(R.id.drawble_mark);
            text.setPadding(3, 3, 3, 3);

            ArrayList<DeviceInfo> markers = clusterMarker.getmMarkers();

            if (markers == null) {
                continue;
            }

            int markNum = markers.size();
            if (zoomLevel < Constant.CLUSTER_LEVEL && markNum >= 2) {
                text.setText(String.valueOf(markNum));
                text.setBackgroundResource(getClusterImageResouce(markNum));

                LatLng ll = clusterMarker.getmCenterAmap(1, Constant.MAP_TYPE_AMAP);
                BitmapDescriptor bd = BitmapDescriptorFactory.fromView(drawableView);
                MarkerOptions options = new MarkerOptions().position(ll).icon(bd).draggable(false).anchor(0.5f, 0.5f).zIndex(9.0f);
                Marker marker = aMap.addMarker(options);
                marker.setObject(clusterMarker);
                listMarkers.add(marker);
            } else {
                for (DeviceInfo device : markers) {
                    addMarker(device);
                }
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mapView != null) {
            mapView.onResume();
        }

        // 开始定位
        startLocation();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mapView != null) {
            mapView.onPause();
        }
    }

    @Override
    public void onDestroy() {
        if (mClusterTask != null) {
            mClusterTask.cancel(true);
        }

        clearMarker();

        if (aMap != null) {
            aMap.clear();
        }
        if (mapView != null) {
            mapView.onDestroy();
        }
        destroylocation();

        super.onDestroy();
    }


    @Override
    public void onMapLoaded() {
        addDevicesOnMap();
    }

    // 定位相关
    @Override
    protected void startLocation() {
        if (mlocationClient != null) {
            mlocationClient.startLocation();
        }
    }

    private void stopLocation() {
        if (mlocationClient != null) {
            mlocationClient.stopLocation();
        }
    }

    // 定位到当前的位置
    @Override
    protected void initLocation() {
        // 自定义系统定位小蓝点
        MyLocationStyle style = new MyLocationStyle();
        style.myLocationIcon(BitmapDescriptorFactory.fromResource(R.drawable.locate_current_icon));// 设置小蓝点的图标
        style.myLocationType(MyLocationStyle.LOCATION_TYPE_FOLLOW_NO_CENTER);
        style.showMyLocation(true);
        aMap.setMyLocationStyle(style);
        aMap.setMyLocationRotateAngle(180);

        // 设置地图监听，当地图状态发生改变时，进行点聚合运算
        aMap.setOnCameraChangeListener(this);
        aMap.setOnMarkerClickListener(this);
        aMap.setOnMapClickListener(this);
        aMap.setOnMapLoadedListener(this);
        UiSettings mUiSettings = aMap.getUiSettings();
        mUiSettings.setMyLocationButtonEnabled(false);
        mUiSettings.setZoomControlsEnabled(false);// false不显示缩放控件
        mUiSettings.setScaleControlsEnabled(true); // 设置地图默认的比例尺
        //mUiSettings.setCompassEnabled(true);// 设置指南针

        // 设置定位源
        aMap.setLocationSource(this);// 设置定位监听
        // aMap.getUiSettings().setMyLocationButtonEnabled(false);//
        // 设置默认定位按钮是否显示
        aMap.setMyLocationEnabled(true);// 设置为true表示显示定位层并可触发定位，false表示隐藏定位层并不可触发定位，默认是false
        // 设置定位的类型为定位模式 ，可以由定位、跟随或地图根据面向方向旋转几种
        aMap.setMyLocationType(AMap.LOCATION_TYPE_LOCATE);

        mlocationClient = new AMapLocationClient(this.getApplicationContext());
        // 设置定位监听
        mlocationClient.setLocationListener(this);
        mLocationOption = new AMapLocationClientOption();
        // 设置为高精度定位模式
        mLocationOption.setLocationMode(AMapLocationMode.Hight_Accuracy).setInterval(Constant.MY_LOCATION_INTERVAL);
        // 此方法为每隔固定时间会发起一次定位请求，为了减少电量消耗或网络流量消耗，
        // 注意设置合适的定位时间的间隔（最小间隔支持为2000ms），并且在合适时间调用stopLocation()方法来取消定位请求
        // 在定位结束后，在合适的生命周期调用onDestroy()方法
        // 在单次定位情况下，定位无论成功与否，都无需调用stopLocation()方法移除请求，定位sdk内部会移除

        // 设置定位参数
        mlocationClient.setLocationOption(mLocationOption);

        aMap.setInfoWindowAdapter(this);
    }

    private void destroylocation() {
        if (null != mlocationClient) {
            mlocationClient.unRegisterLocationListener(this);
            /**
             * 如果AMapLocationClient是在当前Activity实例化的，
             * 在Activity的onDestroy中一定要执行AMapLocationClient的onDestroy
             */
            mlocationClient.onDestroy();
            mlocationClient = null;
        }

        if (mlocationClient == null) {
            mLocationOption = null;
        }
        deactivate();
    }

    @Override
    public boolean onMarkerClick(Marker clickMarker) {
        if (clickMarker == null) {
            return true;
        }
        currentMarker = clickMarker;
        Object obj = clickMarker.getObject();
        if (obj != null) {
            if (obj instanceof DeviceInfo) {
                // 车辆
                DeviceInfo device = (DeviceInfo) obj;
                iCurrentShowDeviceIndex = deviceManager.getValidDevIndex(device);
                deviceMarkerTap(device, true);
            } else if (obj instanceof ClusterDevice) {
                // 聚合物
                ClusterDevice clusterDevice = (ClusterDevice) obj;
                deviceClusterTap(clickMarker.getPosition(), clusterDevice);
            }
        }

        return true;
    }

    @Override
    protected void deviceMarkerTap(DeviceInfo device, boolean isAnimated) {
        if (device == null) {
            return;
        }
        super.deviceMarkerTap(device, isAnimated);

        if (isAnimated && zoomLevel < 18) {
            zoomLevel = 18f;
        }

        //设置窗口显示内容
        showBottomCarInfo();
        updateBottomView(device, true);
        DrawLines(mlocationClient.getLastKnownLocation().getLatitude(),mlocationClient.getLastKnownLocation().getLongitude(),device.getLat(),device.getLng());
    }

    private void deviceClusterTap(LatLng latLng, ClusterDevice clusterDevice) {
        if(aMap != null && aMap.getCameraPosition() != null){
            zoomLevel = aMap.getCameraPosition().zoom;
        }
        zoomLevel += 1;
        if(zoomLevel >= aMap.getMaxZoomLevel()){
            zoomLevel = aMap.getMaxZoomLevel();
        }
        aMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoomLevel));
        DrawLines(mlocationClient.getLastKnownLocation().getLatitude(),mlocationClient.getLastKnownLocation().getLongitude(),latLng.latitude,latLng.longitude);
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
                minLng = state.getLng();
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

    private float distanceOnScreen(MapView map, LatLng latLng1, LatLng latLng2) {
        if (map == null || latLng1 == null || latLng2 == null) {
            return 0;
        }
        Projection projection = map.getMap().getProjection();
        PointF point1 = projection.toMapLocation(latLng1);
        PointF point2 = projection.toMapLocation(latLng2);
        double distance = Math.sqrt(Math.pow(point1.x - point2.x, 2) + Math.pow(point1.y - point2.y, 2));
        return (int) distance;
    }

    public void DrawLines(double lat1,double lng1,double lat2,double lng2){
        LatLng p1 = new LatLng(lat1, lng1);
        LatLng p2 = new LatLng(lat2, lng2);
        List<LatLng> points = new ArrayList<LatLng>();
        points.add(p1);
        points.add(p2);
        PolylineOptions newPolyLine = new PolylineOptions().width(10).color(0xAAFF0000).addAll(points);
        aMap.addPolyline(newPolyLine);

        double cc= distanceOnScreen(mapView,p1,p2);

        int length=(int)cc;

        Toast.makeText(this, "您与终端距离"+length+"米", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onCameraChange(CameraPosition position) {

    }

    @Override
    public void onCameraChangeFinish(CameraPosition position) {
        final float oldZoomLevel = zoomLevel;
        if (position != null) {
            zoomLevel = position.zoom;
        }
        if (oldZoomLevel != zoomLevel && listDevices != null && listDevices.size() > Constant.LIMITCARNUM) {
            //如果是聚合显示的，地图缩放变化后要跟着刷新
            addDevicesOnMap();
        }
    }

    @Override
    public void onLocationChanged(AMapLocation location) {
        if (mLocationChangedListener != null) {
            mLocationChangedListener.onLocationChanged(location);
        }
        if (location != null && location.getErrorCode() == 0) {
            //不需要一直定位
            //stopLocation();
        }
    }

    @Override
    public void activate(OnLocationChangedListener listener) {
        mLocationChangedListener = listener;
    }

    @Override
    public void deactivate() {
        mLocationChangedListener = null;
    }

    protected void clearMarker() {
        if (listMarkers != null) {
            for (Marker marker : listMarkers) {
                if(marker != null) {
                    marker.remove();
                }
            }
            listMarkers.clear();
        }
    }

    @Override
    protected void animateCamera(double lat, double lng, float zoomLevel) {
        if (aMap == null) {
            return;
        }

        if (zoomLevel <= 0) {
            aMap.animateCamera(CameraUpdateFactory.changeLatLng(new LatLng(lat, lng)));
        } else {
            aMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lat, lng), zoomLevel));
        }
    }

    @Override
    protected void animateCameraInclue(ArrayList<DeviceInfo> listDevices) {
        if (listDevices == null || listDevices.size() <= 0) {
            return;
        }
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        boolean builderflag = false;
        for (DeviceInfo device : listDevices) {
            if (device != null) {
                builderflag = true;
                builder.include(new LatLng(device.getLat(), device.getLng()));
            }
        }
        if (builderflag) {
            aMap.animateCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), 10));
        }
    }

    @Override
    protected void changeMapMode(int iType) {
        if (iType == Constant.MAP_NORMAL) {
            // 普通地图模式
            aMap.setMapType(AMap.MAP_TYPE_NORMAL);
        } else {
            // 卫星地图模式
            aMap.setMapType(AMap.MAP_TYPE_SATELLITE);
        }
    }

    @Override
    protected void addFenceOnMap(Fence fence, boolean bSetZoom) {
        if (fence == null || aMap == null) {
            return;
        }
        LatLng center = new LatLng(fence.getLat(), fence.getLng());
        removeFenceOnMap();
        circle = aMap.addCircle(new CircleOptions().center(center).radius(fence.getRadius()).strokeWidth(2).strokeColor(Fence.STROKE_COLOR).fillColor(Fence.FILL_COLOR));
        if (bSetZoom) {
            //只有第一次设置的时候才移动到合适的视角（整个屏幕可以直观看到围栏）
            zoomToSpan(center, setManager.getFenceRange());
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
        double radiusAngle = Math.toDegrees(radius / Fence.RADIUS_OF_EARTH_METERS) / Math.cos(Math.toRadians(center.latitude));
        LatLngBounds.Builder builder = new LatLngBounds.Builder();

        builder.include(new LatLng(center.latitude, center.longitude + radiusAngle));
        builder.include(new LatLng(center.latitude, center.longitude - radiusAngle));

        LatLngBounds bounds = builder.build();

        CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, 50);

        aMap.animateCamera(cu);
    }

    @Override
    protected void showDevTitle() {
        if(textDevTitle == null){
            initDevTitleView();
        }
        if (currDevice != null && currentMarker != null) {
            currentMarker.showInfoWindow();
            textDevTitle.setText(currDevice.getName());
            textDevTitle.setTextColor(getTextColorByStatus(currDevice));
        }else{
            hideDevTitle();
        }
    }

    @Override
    protected void hideDevTitle() {
        if(currentMarker != null){
            currentMarker.hideInfoWindow();
        }
    }

    @Override
    public View getInfoWindow(Marker marker) {
        return textDevTitle;
    }

    @Override
    public View getInfoContents(Marker marker) {
        return null;
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
}
