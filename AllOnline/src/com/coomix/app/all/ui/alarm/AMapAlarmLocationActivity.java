package com.coomix.app.all.ui.alarm;

import android.os.Bundle;
import android.view.View;

import com.amap.api.maps.AMap;
import com.amap.api.maps.AMap.InfoWindowAdapter;
import com.amap.api.maps.AMap.OnInfoWindowClickListener;
import com.amap.api.maps.AMap.OnMapClickListener;
import com.amap.api.maps.AMap.OnMarkerClickListener;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.MapView;
import com.amap.api.maps.UiSettings;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.CircleOptions;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.LatLngBounds;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.PolygonOptions;
import com.coomix.app.all.Constant;
import com.coomix.app.all.R;
import com.coomix.app.all.model.bean.Fence;
import com.coomix.app.all.manager.MapIconManager;

import java.util.ArrayList;

public class AMapAlarmLocationActivity extends AlarmLocationParentActivity
    implements OnMarkerClickListener, OnInfoWindowClickListener, OnMapClickListener, AMap.OnMapLoadedListener {
    private AMap aMap;
    private MapView mapView;
    private Marker mAlarmMarker;
    private LatLng mAlarmLatLng;
    private PolygonOptions originalPolygonOptions;

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);

        mapView = new MapView(this);
        mapView.onCreate(bundle);
        layoutMapView.addView(mapView, 0);
        aMap = mapView.getMap();
        mAlarmLatLng = new LatLng(mAlarm.getLat(), mAlarm.getLng());

        setUpMap();
    }

    private void setUpMap() {
        UiSettings mUiSettings = aMap.getUiSettings();
        mUiSettings.setMyLocationButtonEnabled(false);
        aMap.setOnMapLoadedListener(this);
        aMap.setOnMarkerClickListener(this);
        aMap.setOnInfoWindowClickListener(this);
        mUiSettings.setRotateGesturesEnabled(false);
        mUiSettings.setTiltGesturesEnabled(true);
        mUiSettings.setCompassEnabled(true);//设置指南针
        mUiSettings.setZoomControlsEnabled(false);//false不显示缩放控件
        mUiSettings.setScaleControlsEnabled(true); //设置地图默认的比例尺
        mUiSettings.setCompassEnabled(false);//设置指南针
        aMap.setOnMapClickListener(this);//地图点击事件

        aMap.setInfoWindowAdapter(new InfoWindowAdapter() {
            @Override
            public View getInfoWindow(Marker marker) {
                if (marker.equals(mAlarmMarker)) {
                    initPopView();
                    return popView;
                }
                return null;
            }

            @Override
            public View getInfoContents(Marker marker) {
                return null;
            }
        });
        addMarkersToMap();
    }

    private void addMarkersToMap() {
        LatLng current = new LatLng(mAlarm.getLat(), mAlarm.getLng());

        aMap.animateCamera(CameraUpdateFactory.newLatLngZoom(current, 17f));

        mAlarmMarker = aMap.addMarker(new MarkerOptions().anchor(0.5f, 0.5f).position(current).snippet("").icon(MapIconManager.getInstance().getAmapStopIcon()));

        mAlarmMarker.showInfoWindow();
    }

    @Override
    public void onMapLoaded() {
        initNetData();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mapView != null) {
            mapView.onResume();
        }
    }

    /**
     * 方法必须重写
     */
    @Override
    protected void onPause() {
        super.onPause();
        if (mapView != null) {
            mapView.onPause();
        }
    }

    /**
     * 方法必须重写
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mapView != null) {
            mapView.onSaveInstanceState(outState);
        }
    }

    //点击对应的车mark的时候触发
    @Override
    public boolean onMarkerClick(Marker marker) {
        return false;
    }

    @Override
    protected void onDestroy() {
        if (aMap != null) {
            aMap.clear();
        }
        if (mapView != null) {
            mapView.onDestroy();
        }

        super.onDestroy();
    }

    //弹出框的点击事件
    @Override
    public void onInfoWindowClick(Marker arg0) {
        // TODO Auto-generated method stub
        if (arg0.isInfoWindowShown()) {
            arg0.hideInfoWindow();
        }
    }

    //点击地图的响应事件
    @Override
    public void onMapClick(LatLng arg0) {
        // TODO Auto-generated method stub
//        if (mAlarmMarker != null)
//        {
//            onInfoWindowClick(mAlarmMarker);
//        }
    }

    @Override
    protected void initFence() {
        if (mFence == null) {
            return;
        }

        if (mFence.getShape_type() == Fence.SHAPE_CIRCLE) {
            final LatLng latLng = new LatLng(mFence.getLat(), mFence.getLng());
            aMap.addMarker(new MarkerOptions().position(latLng).draggable(false).icon(BitmapDescriptorFactory.fromResource(R.drawable.fence_icon)).anchor(0.5F, 0.5F));
            aMap.addCircle(new CircleOptions().center(latLng).radius(mFence.getRadius()).strokeWidth(2).strokeColor(Fence.STROKE_COLOR).fillColor(Fence.FILL_COLOR));
            zoomToSpan(latLng, mFence.getRadius());
        } else if (mFence.getShape_type() == Fence.SHAPE_POLYGON) {
            ArrayList<LatLng> mLatLngList = new ArrayList<LatLng>();
            String latLngList[] = mFence.getShape_param().split(";");
            for (int i = 0; i < latLngList.length; i++) {
                String latLngString[] = latLngList[i].split(",");
                LatLng mLatLng = new LatLng(Double.valueOf(latLngString[1]), Double.valueOf(latLngString[0]));
                mLatLngList.add(mLatLng);
            }

            zoomToSpanPolygon(mLatLngList);
            originalPolygonOptions = new PolygonOptions().addAll(mLatLngList).fillColor(Fence.FILL_COLOR).strokeColor(Fence.STROKE_COLOR).strokeWidth(3);
            aMap.addPolygon(originalPolygonOptions);
        }
    }

    private void zoomToSpan(LatLng latlng, int radius) {
        double radiusAngle = Math.toDegrees(radius / Fence.RADIUS_OF_EARTH_METERS) / Math.cos(Math.toRadians(latlng.latitude));
        LatLngBounds.Builder builder = new LatLngBounds.Builder();

        getLatLngOfPopupView();
        LatLng l1 = aMap.getProjection().fromScreenLocation(leftTop);
        LatLng l2 = aMap.getProjection().fromScreenLocation(leftBottom);
        LatLng l3 = aMap.getProjection().fromScreenLocation(rightTop);
        LatLng l4 = aMap.getProjection().fromScreenLocation(rightBottom);
        builder.include(l1);
        builder.include(l2);
        builder.include(l3);
        builder.include(l4);

        builder.include(new LatLng(latlng.latitude, latlng.longitude + radiusAngle));
        builder.include(new LatLng(latlng.latitude, latlng.longitude - radiusAngle));
        builder.include(new LatLng(latlng.latitude + radiusAngle, latlng.longitude));
        builder.include(new LatLng(latlng.latitude - radiusAngle, latlng.longitude));

        builder.include(mAlarmLatLng);

        LatLngBounds bounds = builder.build();
        aMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, BOUND_DISTANCE));
    }

    private void zoomToSpanPolygon(ArrayList<LatLng> latLngList) {
        LatLngBounds.Builder builder = LatLngBounds.builder();
        for (LatLng mLatLng : latLngList) {
            builder.include(mLatLng);
        }
        getLatLngOfPopupView();
        LatLng l1 = aMap.getProjection().fromScreenLocation(leftTop);
        LatLng l2 = aMap.getProjection().fromScreenLocation(leftBottom);
        LatLng l3 = aMap.getProjection().fromScreenLocation(rightTop);
        LatLng l4 = aMap.getProjection().fromScreenLocation(rightBottom);
        builder.include(l1);
        builder.include(l2);
        builder.include(l3);
        builder.include(l4);
        builder.include(mAlarmLatLng);

        aMap.animateCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), BOUND_DISTANCE));
    }

    @Override
    protected void showInfoWindow() {
        if (mAlarmMarker != null) {
            mAlarmMarker.showInfoWindow();
        }
    }

    @Override
    protected void changeMapMode(int iMapType) {
        if (iMapType == Constant.MAP_SATELLITE) {
            aMap.setMapType(AMap.MAP_TYPE_SATELLITE);
        } else {
            aMap.setMapType(AMap.MAP_TYPE_NORMAL);
        }
    }
}
