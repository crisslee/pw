package com.coomix.app.all.ui.main;

import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import com.coomix.app.all.Constant;
import com.coomix.app.all.R;
import com.coomix.app.all.manager.MapIconManager;
import com.coomix.app.all.markColection.baidu.Cluster;
import com.coomix.app.all.markColection.baidu.ClusterDevice;
import com.coomix.app.all.model.bean.DeviceInfo;
import com.coomix.app.all.model.bean.Fence;
import com.coomix.app.all.service.MyOrientationListener;
import com.tencent.map.geolocation.TencentLocation;
import com.tencent.map.geolocation.TencentLocationListener;
import com.tencent.map.geolocation.TencentLocationManager;
import com.tencent.map.geolocation.TencentLocationRequest;
import com.tencent.mapsdk.raster.model.BitmapDescriptor;
import com.tencent.mapsdk.raster.model.BitmapDescriptorFactory;
import com.tencent.mapsdk.raster.model.CameraPosition;
import com.tencent.mapsdk.raster.model.Circle;
import com.tencent.mapsdk.raster.model.CircleOptions;
import com.tencent.mapsdk.raster.model.LatLng;
import com.tencent.mapsdk.raster.model.LatLngBounds;
import com.tencent.mapsdk.raster.model.Marker;
import com.tencent.mapsdk.raster.model.MarkerOptions;
import com.tencent.tencentmap.mapsdk.map.CameraUpdateFactory;
import com.tencent.tencentmap.mapsdk.map.MapView;
import com.tencent.tencentmap.mapsdk.map.Projection;
import com.tencent.tencentmap.mapsdk.map.TencentMap;
import com.tencent.tencentmap.mapsdk.map.TencentMap.OnMapCameraChangeListener;
import com.tencent.tencentmap.mapsdk.map.TencentMap.OnMapClickListener;
import com.tencent.tencentmap.mapsdk.map.TencentMap.OnMapLoadedListener;
import com.tencent.tencentmap.mapsdk.map.TencentMap.OnMarkerClickListener;
import com.tencent.tencentmap.mapsdk.map.UiSettings;
import java.util.ArrayList;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class TMainActivity extends MainActivityParent
    implements OnMapLoadedListener, OnMarkerClickListener, TencentLocationListener,
        OnMapClickListener, OnMapCameraChangeListener, TencentMap.InfoWindowAdapter {
    protected MapView mMapView = null;
    protected TencentMap mTencentMap = null;
    private ClusterTask mClusterTask = new ClusterTask();
    private final ReadWriteLock mClusterTaskLock = new ReentrantReadWriteLock();

    private ArrayList<Marker> listMarkers = new ArrayList<Marker>();
    private Marker currentMarker;

    private TencentLocationManager mLocationManager;
    private TencentLocationRequest mLocationRequest;

    private MyOrientationListener myOrientationListener;
    private int mXDirection;

    private Marker mMyLocationMarker = null;
    private TencentLocation myLocData = null;
    private Circle circle;

    @SuppressWarnings("unchecked")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mMapView = new MapView(this);
        layoutMapView.addView(mMapView, 0);
        mTencentMap = mMapView.getMap();

        initLocation();
        initOritationListener();
        //腾讯地图必须在设置完地图参数后再进行icon生成
        //MapIconManager.getInstance().initIcons();

        //addDevicesOnMap();
    }

    public void initOritationListener(){
        myOrientationListener = new MyOrientationListener(getApplicationContext());
        myOrientationListener.setOnOrientationListener(new MyOrientationListener.OnOrientationListener(){
            @Override
            public void onOrientationChanged(float x){
                mXDirection =(int) x - 45;
            }
        });
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
            Cluster cluster = new Cluster(TMainActivity.this, mMapView, mGridSize);
            return cluster.createTMAPCluster(listDevices);
        }

        @Override
        protected void onPostExecute(ArrayList<ClusterDevice> result) {
            renderingClusters(result);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mMapView != null) {
            mMapView.onResume();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mMapView != null) {
            mMapView.onPause();
        }
    }

    @Override
    public void onStart()
    {
        myOrientationListener.start();
        super.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mMapView != null) {
            mMapView.onStop();
        }
        myOrientationListener.stop();
    }

    @Override
    public void onDestroy() {
        if (mMapView != null) {
            mTencentMap.clearAllOverlays();
            mMapView.onDestroy();
        }
        if (mClusterTask != null) {
            mClusterTask.cancel(true);
        }
        clearMarker();
        super.onDestroy();
    }

    @Override
    public void onMapLoaded() {
        addDevicesOnMap();
    }

    private void addMyLocation() {
        if (myLocData != null) {
            if (mMyLocationMarker != null) {
                mMyLocationMarker.remove();
                mMyLocationMarker = null;
            }
            LatLng ll = new LatLng(myLocData.getLatitude(), myLocData.getLongitude());
            MarkerOptions options = new MarkerOptions().position(ll).icon(BitmapDescriptorFactory
                .fromResource(R.drawable.locate_current_icon)).draggable(false).anchor(0.5f, 0.5f).rotation((float)mXDirection);
            mMyLocationMarker = mTencentMap.addMarker(options);
        }
    }

    // 定位选择
    @Override
    protected void startLocation() {
        if (mLocationManager != null && mLocationRequest != null) {
            mLocationManager.requestLocationUpdates(mLocationRequest, this);
        }
    }

    private void stopLocation() {
        if (mLocationManager != null) {
            mLocationManager.removeUpdates(this);
        }
    }

    @Override
    public boolean onMarkerClick(Marker clickMarker) {
        if(currentMarker != null){
            currentMarker.hideInfoWindow();
        }
        currentMarker = clickMarker;
        Object obj = clickMarker.getTag();
        if (obj != null && obj instanceof DeviceInfo) {
            DeviceInfo device = (DeviceInfo) obj;
            iCurrentShowDeviceIndex = deviceManager.getValidDevIndex(device);
            deviceMarkerTap(device, true);
        } else if (obj != null && obj instanceof ClusterDevice) {
            // 聚合物
            ClusterDevice clusterDevice = (ClusterDevice) obj;
            deviceClusterTap(clickMarker.getPosition(), clusterDevice);
        }
        return true;
    }

    private void deviceClusterTap(LatLng latLng, ClusterDevice clusterDevice) {
        if(mTencentMap != null){
            zoomLevel = mTencentMap.getZoomLevel();
        }
        zoomLevel += 1;
        if(zoomLevel >= mTencentMap.getMaxZoomLevel()){
            zoomLevel = mTencentMap.getMaxZoomLevel();
        }
        mTencentMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoomLevel));
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

    @Override
    public void onCameraChange(CameraPosition position) {
    }

    @Override
    public void onCameraChangeFinish(CameraPosition position) {
        final float oldZoomLevel = zoomLevel;
        if (position != null) {
            zoomLevel = position.getZoom();
        }
        if (oldZoomLevel != zoomLevel && listDevices != null && listDevices.size() > Constant.LIMITCARNUM) {
            //如果是聚合显示的，地图缩放变化后要跟着刷新
            addDevicesOnMap();
        }
    }

    @Override
    public void onLocationChanged(TencentLocation location, int error, String reason) {
        if (location == null) {
            return;
        }
        myLocData = location;
        // 更新 location 图层
        addMyLocation();
        //stopLocation();
    }

    @Override
    public void onStatusUpdate(String arg0, int arg1, String arg2) {
    }

    //设置地图的模式--卫星或普通
    @Override
    protected void changeMapMode(int iType) {
        if (iType == Constant.MAP_SATELLITE) {
            mTencentMap.setSatelliteEnabled(true);
        } else {
            mTencentMap.setSatelliteEnabled(false);
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
        mTencentMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(device.getLat(), device.getLng()), zoomLevel));

        //设置窗口显示内容
        showBottomCarInfo();
        updateBottomView(device, true);
    }

    //更新设备在地图上的显示
    @Override
    protected void addDevicesOnMap() {
        if (listMarkers == null) {
            listMarkers = new ArrayList<Marker>();
        }else{
            clearMarker();
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

            ArrayList<DeviceInfo> markers = clusterMarker.getmMarkers();

            if (markers == null) {
                continue;
            }

            int markNum = markers.size();
            if (zoomLevel < Constant.CLUSTER_LEVEL && markNum >= 2) {
                text.setText(String.valueOf(markNum));
                text.setBackgroundResource(getClusterImageResouce(markNum));

                LatLng ll = clusterMarker.getmCenterTmap(1, Constant.MAP_TYPE_TENCENT);
                BitmapDescriptor bd = BitmapDescriptorFactory.fromView(drawableView);
                MarkerOptions options = new MarkerOptions().position(ll).icon(bd).draggable(false).anchor(0.5f, 0.5f).tag(clusterMarker);
                Marker marker = mTencentMap.addMarker(options);
                listMarkers.add(marker);
            } else {
                for (DeviceInfo state : markers) {
                    addMarker(state);
                }
            }
        }
    }

    //移动地图视角
    @Override
    protected void animateCamera(double lat, double lng, float zoomLevel) {
        if (zoomLevel <= 0) {
            mTencentMap.animateCamera(CameraUpdateFactory.newLatLng(new LatLng(lat, lng)));
        } else {
            mTencentMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lat, lng), zoomLevel));
        }
    }

    //移动地图视角
    @Override
    protected void animateCameraInclue(ArrayList<DeviceInfo> list) {
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        for (DeviceInfo device : list) {
            if (device != null) {
                builder.include(new LatLng(device.getLat(), device.getLng()));
            }
        }
        mTencentMap.animateCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), 50));
    }

    //增加marker
    @Override
    protected void addMarker(DeviceInfo device) {
        BitmapDescriptor bd = MapIconManager.getInstance().getTmapIcon(device);
        if (bd != null) {
            LatLng ll = new LatLng(device.getLat(), device.getLng());
            MarkerOptions options = new MarkerOptions().position(ll).icon(bd).draggable(false).rotation(device.getCourse()).anchor(0.5f, 0.5f).tag(device);
            Marker marker = mTencentMap.addMarker(options);
            listMarkers.add(marker);
            if(device.equals(currDevice)){
                if(currentMarker != null){
                    currentMarker.hideInfoWindow();
                }
                currentMarker = marker;
                if(isBottomInfoShow()){
                    showDevTitle();
                }
            }
        }
    }

    //初始化地图数据，一些参数设置、监听等
    @Override
    protected void initLocation() {
        mLocationManager = TencentLocationManager.getInstance(this);
        mLocationRequest = TencentLocationRequest.create();
        mLocationRequest.setInterval(Constant.MY_LOCATION_INTERVAL);
        mTencentMap.setOnMapCameraChangeListener(this);
        mTencentMap.setOnMarkerClickListener(this);
        mTencentMap.setOnMapClickListener(this);
        mTencentMap.setOnMapLoadedListener(this);
        mTencentMap.setInfoWindowAdapter(this);

        mMapView.getUiSettings().setScaleControlsEnabled(true);
        mMapView.getUiSettings().setLogoPosition(UiSettings.LOGO_POSITION_LEFT_BOTTOM);
        mMapView.getUiSettings().setScaleViewPosition(UiSettings.SCALEVIEW_POSITION_CENTER_BOTTOM);

        startLocationWithPermission();
    }

    @Override
    protected void addFenceOnMap(Fence fence, boolean bSetZoom) {
        if (fence == null || mTencentMap == null) {
            return;
        }
        removeFenceOnMap();
        LatLng latLng = new LatLng(fence.getLat(), fence.getLng());
        CircleOptions option = new CircleOptions().center(latLng).radius(fence.getRadius()).fillColor(Fence.FILL_COLOR).strokeWidth(3).strokeDash(true).strokeColor(Fence.STROKE_COLOR).zIndex(9);
        circle = mTencentMap.addCircle(option);
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
        Projection projection = mMapView.getProjection();
        // 任意给一个经纬度计算计算一个纬度差的距离(纬度方向认为是均匀变化的)
        // 一纬度的距离 , 纬度最大90
        double distanceOfLat = projection.distanceBetween(center, new LatLng(center.getLatitude() < 0 ? center.getLatitude() + 1 : center.getLatitude() - 1, center.getLongitude()));
        double latRadius = radius / distanceOfLat;

        // 一经度的距离(同纬度下，一个经度差的距离认为是相同的) 经度最大180
        double distanceOfLng = projection.distanceBetween(center, new LatLng(center.getLatitude(), center.getLongitude() < 0 ? center.getLongitude() + 1 : center.getLongitude() - 1));
        double lngRadius = radius / distanceOfLng;

        if (latRadius > 180 || lngRadius > 360) {
            return;
        }

        double neLat = center.getLatitude() + latRadius;
        double neLng = center.getLongitude() + lngRadius;
        if (neLat > 90) {
            neLat = 180 - neLat;
        }
        if (neLng > 180) {
            neLng = 360 - neLng;
        }

        double swLat = center.getLatitude() - latRadius;
        double swLng = center.getLongitude() - lngRadius;
        if (swLat < -90) {
            swLat = -180 - swLat;
        }
        if (swLng < -180) {
            swLng = -360 - swLng;
        }

        LatLng northeast = new LatLng(neLat, neLng);
        LatLng southwest = new LatLng(swLat, swLng);
        LatLngBounds bounds = new LatLngBounds.Builder().include(northeast).include(southwest).build();

        mTencentMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 10));
    }

    @Override
    protected void showDevTitle() {
        if(currDevice != null && currentMarker != null) {
            currentMarker.showInfoWindow();
            if(textDevTitle != null){
                textDevTitle.setText(currDevice.getName());
                textDevTitle.setTextColor(getTextColorByStatus(currDevice));
            }
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
        initDevTitleView();
        return textDevTitle;
    }

    @Override
    public void onInfoWindowDettached(Marker marker, View view) {

    }
}