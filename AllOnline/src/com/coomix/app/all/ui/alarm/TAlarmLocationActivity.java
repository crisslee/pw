package com.coomix.app.all.ui.alarm;

import android.os.Bundle;
import android.view.View;

import com.coomix.app.all.Constant;
import com.coomix.app.all.R;
import com.coomix.app.all.model.bean.Fence;
import com.coomix.app.all.manager.MapIconManager;
import com.tencent.mapsdk.raster.model.BitmapDescriptor;
import com.tencent.mapsdk.raster.model.BitmapDescriptorFactory;
import com.tencent.mapsdk.raster.model.CameraPosition;
import com.tencent.mapsdk.raster.model.Circle;
import com.tencent.mapsdk.raster.model.CircleOptions;
import com.tencent.mapsdk.raster.model.LatLng;
import com.tencent.mapsdk.raster.model.LatLngBounds;
import com.tencent.mapsdk.raster.model.Marker;
import com.tencent.mapsdk.raster.model.MarkerOptions;
import com.tencent.mapsdk.raster.model.Polygon;
import com.tencent.mapsdk.raster.model.PolygonOptions;
import com.tencent.tencentmap.mapsdk.map.CameraUpdateFactory;
import com.tencent.tencentmap.mapsdk.map.MapView;
import com.tencent.tencentmap.mapsdk.map.Projection;
import com.tencent.tencentmap.mapsdk.map.TencentMap;
import com.tencent.tencentmap.mapsdk.map.TencentMap.InfoWindowAdapter;
import com.tencent.tencentmap.mapsdk.map.TencentMap.OnMapCameraChangeListener;
import com.tencent.tencentmap.mapsdk.map.TencentMap.OnMapClickListener;
import com.tencent.tencentmap.mapsdk.map.TencentMap.OnMapLoadedListener;
import com.tencent.tencentmap.mapsdk.map.TencentMap.OnMarkerClickListener;
import com.tencent.tencentmap.mapsdk.map.UiSettings;

import java.util.ArrayList;

public class TAlarmLocationActivity extends AlarmLocationParentActivity
    implements OnMapClickListener, OnMarkerClickListener, InfoWindowAdapter, OnMapCameraChangeListener,OnMapLoadedListener
{
    private Marker mMarker;
    private CameraPosition mPreviousCameraPosition;
    private MapView mMapView = null;
    private TencentMap mTencentMap = null;
    private LatLng mAlarmLatLng;
    private Circle circle;
    private CircleOptions circleOptions;
    private Marker circleCenterOverlay;
    private MarkerOptions circleCenterOptions;
    private Polygon polygon;
    private PolygonOptions polygonOptions;

    @Override
    protected void onCreate(Bundle bundle)
    {
        super.onCreate(bundle);

        mMapView = new MapView(this);
        mMapView.onCreate(bundle);
        layoutMapView.addView(mMapView, 0);
        mTencentMap = mMapView.getMap();
        if(mAlarm != null)
        {
            mAlarmLatLng = new LatLng(mAlarm.getLat(), mAlarm.getLng());
        }

        initMap();

//        initData();
    }

    private void initMap()
    {
        mTencentMap.setOnMapLoadedListener(this);
        mTencentMap.setOnMapClickListener(this);
        mTencentMap.setOnMarkerClickListener(this);
        mTencentMap.setOnMapCameraChangeListener(this);
        mTencentMap.setInfoWindowAdapter(this);
        mMapView.getUiSettings().setScaleControlsEnabled(true);
        mMapView.getUiSettings().setLogoPosition(UiSettings.LOGO_POSITION_LEFT_BOTTOM);
        mMapView.getUiSettings().setScaleViewPosition(UiSettings.SCALEVIEW_POSITION_CENTER_BOTTOM);

        if (mAlarm == null)
        {
            return;
        }

        LatLng ll = new LatLng(mAlarm.getLat(), mAlarm.getLng());
        mTencentMap.animateCamera(CameraUpdateFactory.newLatLngZoom(ll, 17.0f));
        MarkerOptions options = new MarkerOptions().position(ll).icon(MapIconManager.getInstance().getTmapStopIcon()).draggable(false).anchor(.5f, .5f);
        mMarker = mTencentMap.addMarker(options);
    }

    private void hideInfoWindow()
    {
        if (mMarker != null && mMarker.isInfoWindowShown())
        {
            mMarker.hideInfoWindow();
        }
    }

    @Override
    protected void onPause()
    {
        if (mMapView != null)
        {
            mMapView.onPause();
        }
        super.onPause();
    }

    @Override
    protected void onResume()
    {

        if (mMapView != null)
        {
            mMapView.onResume();
        }
        super.onResume();
    }

    @Override
    protected void onStop()
    {
        if (mMapView != null)
        {
            mMapView.onStop();
        }
        super.onStop();
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();

        if (mTencentMap != null)
        {
            mTencentMap.clearAllOverlays();
        }

        if (mMapView != null)
        {
            mMapView.onDestroy();
        }
    }
    @Override
    public void onMapLoaded()
    {
        initNetData();
    }
    @Override
    public void onMapClick(LatLng ll)
    {
        //hideInfoWindow();
    }

    @Override
    public void onCameraChange(CameraPosition position)
    {
        // TODO Auto-generated method stub
    }

    @Override
    public void onCameraChangeFinish(CameraPosition position)
    {
        // TODO Auto-generated method stub
//        if (mPreviousCameraPosition == null || mPreviousCameraPosition.getZoom() != position.getZoom())
//        {
//            zoomControlView.zoomTo(position.getZoom());
//        }
//        mPreviousCameraPosition = position;
    }

    @Override
    public View getInfoWindow(Marker marker)
    {
        initPopView();
        return popView;
    }

    @Override
    public void onInfoWindowDettached(Marker marker, View arg1)
    {
        // TODO Auto-generated method stub

    }

    @Override
    public boolean onMarkerClick(Marker marker)
    {
        showInfoWindow();
        return true;
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
            circleOptions = new CircleOptions().center(latLng).strokeDash(true).radius(mFence.getRadius()).fillColor(Fence.FILL_COLOR).strokeWidth(3).strokeColor(Fence.STROKE_COLOR);
            circle = mTencentMap.addCircle(circleOptions);
            BitmapDescriptor circleCenterBd = BitmapDescriptorFactory.fromResource(R.drawable.fence_icon);
            circleCenterOptions = new MarkerOptions().position(latLng).icon(circleCenterBd).draggable(false).anchor(.5f, .5f);
            circleCenterOverlay = mTencentMap.addMarker(circleCenterOptions);
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
            polygonOptions = new PolygonOptions().addAll(latLngList).fillColor(Fence.FILL_COLOR).strokeWidth(3).strokeColor(Fence.STROKE_COLOR).zIndex(9);
            polygon = mTencentMap.addPolygon(polygonOptions);
            zoomToSpan(latLngList);
        }
        showInfoWindow();
    }

    private void zoomToSpan(int radius, LatLng center)
    {
        Projection projection = mMapView.getProjection();
        // 任意给一个经纬度计算计算一个纬度差的距离(纬度方向认为是均匀变化的)
        // 一纬度的距离 , 纬度最大90
        double distanceOfLat = projection.distanceBetween(center, new LatLng(center.getLatitude() < 0 ? center.getLatitude() + 1 : center.getLatitude() - 1, center.getLongitude()));
        double latRadius = radius / distanceOfLat;

        // 一经度的距离(同纬度下，一个经度差的距离认为是相同的) 经度最大180
        double distanceOfLng = projection.distanceBetween(center, new LatLng(center.getLatitude(), center.getLongitude() < 0 ? center.getLongitude() + 1 : center.getLongitude() - 1));
        double lngRadius = radius / distanceOfLng;

        if (latRadius > 180 || lngRadius > 360)
        {
            return;
        }

        double neLat = center.getLatitude() + latRadius;
        double neLng = center.getLongitude() + lngRadius;
        if (neLat > 90)
        {
            neLat = 180 - neLat;
        }
        if (neLng > 180)
        {
            neLng = 360 - neLng;
        }

        double swLat = center.getLatitude() - latRadius;
        double swLng = center.getLongitude() - lngRadius;
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
//        LatLng l1 = mMapView.getProjection().fromScreenLocation(leftTop);
//        LatLng l2 = mMapView.getProjection().fromScreenLocation(leftBottom);
//        LatLng l3 = mMapView.getProjection().fromScreenLocation(rightTop);
//        LatLng l4 = mMapView.getProjection().fromScreenLocation(rightBottom);

        LatLng l1 = null,l2 = null,l3 = null,l4 = null;
        if(leftTop != null)
        {
            l1 = mMapView.getProjection().fromScreenLocation(leftTop);
        }
        if(leftBottom != null)
        {
            l2 = mMapView.getProjection().fromScreenLocation(leftBottom);
        }
        if(rightBottom != null)
        {
            l3 = mMapView.getProjection().fromScreenLocation(rightBottom);
        }
        if(rightTop != null)
        {
            l4 = mMapView.getProjection().fromScreenLocation(rightTop);
        }

        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        builder.include(northeast).include(southwest).include(mAlarmLatLng);

        if(l1 != null)
        {
            builder.include(l1);
        }
        if(l2 != null)
        {
            builder.include(l2);
        }
        if(l3 != null)
        {
            builder.include(l3);
        }
        if(l4 != null)
        {
            builder.include(l4);
        }

        mTencentMap.animateCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), BOUND_DISTANCE));
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
            builder.include(mAlarmLatLng);

            getLatLngOfPopupView();
            LatLng l1 = null,l2 = null,l3 = null,l4 = null;
            if(leftTop != null)
            {
                l1 = mMapView.getProjection().fromScreenLocation(leftTop);
            }
            if(leftBottom != null)
            {
                l2 = mMapView.getProjection().fromScreenLocation(leftBottom);
            }
            if(rightBottom != null)
            {
                l3 = mMapView.getProjection().fromScreenLocation(rightBottom);
            }
            if(rightTop != null)
            {
                l4 = mMapView.getProjection().fromScreenLocation(rightTop);
            }

            if(l1 != null)
            {
                builder.include(l1);
            }
            if(l2 != null)
            {
                builder.include(l2);
            }
            if(l3 != null)
            {
                builder.include(l3);
            }
            if(l4 != null)
            {
                builder.include(l4);
            }
//            if(leftTop != null)
//            {
//                LatLng l1 = mMapView.getProjection().fromScreenLocation(leftTop);
//                builder.include(l1);
//            }
//            if(leftBottom != null)
//            {
//                LatLng l2 = mMapView.getProjection().fromScreenLocation(leftBottom);
//                builder.include(l2);
//            }
//            if(rightTop != null)
//            {
//                LatLng l3 = mMapView.getProjection().fromScreenLocation(rightTop);
//                builder.include(l3);
//            }
//            if(rightBottom != null)
//            {
//                LatLng l4 = mMapView.getProjection().fromScreenLocation(rightBottom);
//                builder.include(l4);
//            }

            mTencentMap.animateCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), BOUND_DISTANCE));
        }
    }

    @Override
    protected void showInfoWindow()
    {
        if (mAlarm == null)
        {
            return;
        }
        if (mMarker != null)
        {
            mMarker.showInfoWindow();
        }
    }

    @Override
    protected void changeMapMode(int iMapType)
    {
        if (iMapType == Constant.MAP_SATELLITE)
        {
            mTencentMap.setSatelliteEnabled(true);
        }
        else
        {
            mTencentMap.setSatelliteEnabled(false);
        }
    }
}
