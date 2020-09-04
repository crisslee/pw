package com.coomix.app.all.ui.alarm;

import android.os.Bundle;
import android.util.Log;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BaiduMap.OnMapClickListener;
import com.baidu.mapapi.map.BaiduMap.OnMapLoadedCallback;
import com.baidu.mapapi.map.BaiduMap.OnMapStatusChangeListener;
import com.baidu.mapapi.map.BaiduMap.OnMarkerClickListener;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.Circle;
import com.baidu.mapapi.map.CircleOptions;
import com.baidu.mapapi.map.InfoWindow;
import com.baidu.mapapi.map.MapPoi;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.Polygon;
import com.baidu.mapapi.map.PolygonOptions;
import com.baidu.mapapi.map.Stroke;
import com.baidu.mapapi.map.UiSettings;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.model.LatLngBounds;
import com.baidu.mapapi.utils.DistanceUtil;
import com.coomix.app.all.Constant;
import com.coomix.app.all.R;
import com.coomix.app.all.manager.MapIconManager;
import com.coomix.app.all.model.bean.Fence;
import java.util.ArrayList;

public class BAlarmLocationActivity extends AlarmLocationParentActivity implements OnMapClickListener,
    OnMapStatusChangeListener, OnMapLoadedCallback {

    private MapStatus mPreviousCameraPosition;
    private Marker mMarker;
    // 初始化全局 bitmap 信息，不用时及时 recycle
    private InfoWindow mInfoWindow;
    private MapView mMapView;
    private BaiduMap mBaiduMap;
    private LatLng mAlarmLatLng;
    private BitmapDescriptor circleCenterBd;
    private Circle circle;
    private CircleOptions circleOptions;
    private Polygon polygon;
    private Marker circleCenterOverlay;
    private MarkerOptions circleCenterOptions;
    private PolygonOptions polygonOptions;

    @Override
    protected void onCreate(Bundle bundle)
    {
        super.onCreate(bundle);
        mMapView = new MapView(this);
        layoutMapView.addView(mMapView, 0);
        mBaiduMap = mMapView.getMap();
        if(mAlarm != null)
        {
            mAlarmLatLng = new LatLng(mAlarm.getLat(), mAlarm.getLng());
        }

        initMap();

//        initData();
    }

    private void initMap()
    {
        mMapView.showScaleControl(true);
        mMapView.showZoomControls(false);

        UiSettings uiSettings = mBaiduMap.getUiSettings();
        uiSettings.setOverlookingGesturesEnabled(false);
        uiSettings.setRotateGesturesEnabled(false);
        uiSettings.setCompassEnabled(false);

        mBaiduMap.setOnMapLoadedCallback(this);
        mBaiduMap.setOnMapClickListener(this);
        mBaiduMap.setOnMapStatusChangeListener(this);
        mBaiduMap.setOnMarkerClickListener(new OnMarkerClickListener()
        {
            @Override
            public boolean onMarkerClick(Marker marker)
            {
                if (marker == mMarker)
                {
                    showInfoWindow();
                    return true;
                }
                return false;
            }
        });

        if (mAlarm == null)
        {
            return;
        }
        LatLng ll = new LatLng(mAlarm.getLat(), mAlarm.getLng());
        MapStatus ms = new MapStatus.Builder().zoom(18.0f).target(ll).build();
        mBaiduMap.animateMapStatus(MapStatusUpdateFactory.newMapStatus(ms));

        MarkerOptions options = new MarkerOptions().position(ll).icon(MapIconManager.getInstance().getBaiduStopIcon());
        mMarker = (Marker) mBaiduMap.addOverlay(options);
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();

        if (circleCenterBd != null)
        {
            circleCenterBd.recycle();
        }

        if (mBaiduMap != null)
        {
            mBaiduMap.clear();
        }
        if (mMapView != null)
        {
            mMapView.onDestroy();
        }
    }

    @Override
    public void onMapClick(LatLng ll)
    {
        //mBaiduMap.hideInfoWindow();
    }

    @Override
    public void onMapPoiClick(MapPoi arg0) {
    }

    @Override
    public void onMapStatusChange(MapStatus arg0)
    {
    }

    @Override
    public void onMapStatusChangeFinish(MapStatus arg0)
    {
        mPreviousCameraPosition = mBaiduMap.getMapStatus();
    }

    @Override
    public void onMapStatusChangeStart(MapStatus arg0)
    {
    }

    @Override
    public void onMapLoaded()
    {
        initNetData();
        Log.i("testtt","onMapLoaded");
    }

    @Override
    protected void initFence()
    {
        if (mFence == null)
        {
            return;
        }
        if (mFence.getShape_type() == Fence.SHAPE_CIRCLE)
        {
            // 画圆
            final LatLng latLng = new LatLng(mFence.getLat(), mFence.getLng());
            Stroke stroke = new Stroke(2, Fence.STROKE_COLOR);
            circleOptions = new CircleOptions().center(latLng).radius(mFence.getRadius()).fillColor(Fence.FILL_COLOR).stroke(stroke);
            circle = (Circle) mBaiduMap.addOverlay(circleOptions);
            circleCenterBd = BitmapDescriptorFactory.fromResource(R.drawable.fence_icon);
            circleCenterOptions = new MarkerOptions().position(latLng).icon(circleCenterBd)//.zIndex(9)
                    .draggable(false);
            circleCenterOverlay = (Marker) mBaiduMap.addOverlay(circleCenterOptions);
            zoomToSpan(mFence.getRadius(), latLng);
        }
        else if (mFence.getShape_type() == Fence.SHAPE_POLYGON)
        {
            ArrayList<LatLng> latLngList = new ArrayList<LatLng>();
            String latLngStrList[] = mFence.getShape_param().split(";");
            for (int i = 0; i < latLngStrList.length; i++)
            {
                String latLngString[] = latLngStrList[i].split(",");
                LatLng latLng = new LatLng(Double.valueOf(latLngString[1]), Double.valueOf(latLngString[0]));
                latLngList.add(latLng);
            }
            // 画多边形
            Stroke stroke = new Stroke(3, Fence.STROKE_COLOR);
            polygonOptions = new PolygonOptions().points(latLngList).fillColor(Fence.FILL_COLOR).stroke(stroke);
            polygon = (Polygon) mBaiduMap.addOverlay(polygonOptions);
            zoomToSpan(latLngList);
        }
//        new Handler().postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                if(mAlarm != null) {
//                    mBaiduMap.animateMapStatus(MapStatusUpdateFactory.newLatLng(new LatLng(mAlarm.lat, mAlarm.lng)));
//                }
//            }
//        }, 500);

    }

    public void zoomToSpan(ArrayList<LatLng> latLngList)
    {
        if (latLngList != null && latLngList.size() > 1)
        {
            LatLngBounds.Builder builder = new LatLngBounds.Builder();
            for (LatLng latLng : latLngList)
            {
                builder.include(latLng);
            }

            getLatLngOfPopupView();
            if(leftTop != null && mBaiduMap.getProjection() != null)
            {
                LatLng l1 = mBaiduMap.getProjection().fromScreenLocation(leftTop);
                builder.include(l1);
            }

            if(leftBottom != null && mBaiduMap.getProjection() != null)
            {
                LatLng l2 = mBaiduMap.getProjection().fromScreenLocation(leftBottom);
                builder.include(l2);
            }

            if(rightTop != null && mBaiduMap.getProjection() != null)
            {
                LatLng l3 = mBaiduMap.getProjection().fromScreenLocation(rightTop);
                builder.include(l3);
            }

            if(rightBottom != null&& mBaiduMap.getProjection() != null)
            {
                LatLng l4 = mBaiduMap.getProjection().fromScreenLocation(rightBottom);
                builder.include(l4);
            }

            builder.include(mAlarmLatLng);

            LatLngBounds bounds = builder.build();
            mBaiduMap.animateMapStatus(MapStatusUpdateFactory.newLatLngBounds(bounds));
        }
    }

    private void zoomToSpan(int radius, LatLng center)
    {
        // 任意给一个经纬度计算计算一个纬度差的距离(纬度方向认为是均匀变化的)
        // 一纬度的距离 , 纬度最大90
        double distanceOfLat = DistanceUtil.getDistance(center, new LatLng(center.latitude < 89 ? center.latitude + 1 : center.latitude - 1, center.longitude));
        double latRadius = radius / distanceOfLat;

        // 一经度的距离(同纬度下，一个经度差的距离认为是相同的) 经度最大180
        double distanceOfLng = DistanceUtil.getDistance(center, new LatLng(center.latitude, center.longitude < 179 ? center.longitude + 1 : center.longitude - 1));
        double lngRadius = radius / distanceOfLng;

        if (latRadius > 180 || lngRadius > 360)
        {
            return;
        }

        double neLat = center.latitude + latRadius;
        double neLng = center.longitude + lngRadius;
        if (neLat > 90)
        {
            neLat = 180 - neLat;
        }
        if (neLng > 180)
        {
            neLng = 360 - neLng;
        }

        double swLat = center.latitude - latRadius;
        double swLng = center.longitude - lngRadius;
        if (swLat < -90)
        {
            swLat = -180 - swLat;
        }
        if (swLng < -180)
        {
            swLng = -360 - swLng;
        }

        LatLng northeast = new LatLng(neLat, neLng);
        LatLng southwest = new LatLng(swLat, swLng);

        getLatLngOfPopupView();
        LatLng l1 = null,l2 = null,l3 = null,l4 = null;
        if(leftTop != null && mBaiduMap.getProjection() != null) {
            l1 = mBaiduMap.getProjection().fromScreenLocation(leftTop);
        }
        if(leftBottom != null && mBaiduMap.getProjection() != null) {
            l2 = mBaiduMap.getProjection().fromScreenLocation(leftBottom);
        }
        if(rightTop != null && mBaiduMap.getProjection() != null) {
            l3 = mBaiduMap.getProjection().fromScreenLocation(rightTop);
        }
        if(rightBottom != null && mBaiduMap.getProjection() != null) {
            l4 = mBaiduMap.getProjection().fromScreenLocation(rightBottom);
        }

        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        builder.include(northeast);
        builder.include(southwest);
        if(l1 != null){
            builder.include(l1);
        }
        if(l2 != null){
            builder.include(l2);
        }
        if(l3 != null){
            builder.include(l3);
        }
        if(l4 != null){
            builder.include(l4);
        }
        builder.include(mAlarmLatLng);

        mBaiduMap.animateMapStatus(MapStatusUpdateFactory.newLatLngBounds(builder.build()));

    }

    @Override
    protected void showInfoWindow()
    {
        if (mAlarm == null)
        {
            return;
        }
        if (mInfoWindow == null)
        {
            initPopView();
            mInfoWindow = new InfoWindow(popView, new LatLng(mAlarm.getLat(), mAlarm.getLng()), -47);
        }
        mBaiduMap.showInfoWindow(mInfoWindow);
    }

    @Override
    protected void changeMapMode(int iMapType)
    {
        if (iMapType == Constant.MAP_SATELLITE)
        {
            mBaiduMap.setMapType(BaiduMap.MAP_TYPE_SATELLITE);
        }
        else
        {
            mBaiduMap.setMapType(BaiduMap.MAP_TYPE_NORMAL);
        }
    }

    @Override
    public void onMapStatusChangeStart(MapStatus mapStatus, int i)
    {

    }
}
