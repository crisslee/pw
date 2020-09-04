package com.coomix.app.all.ui.advance;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Toast;

import com.coomix.app.all.R;
import com.coomix.app.all.model.bean.Fence;
import com.tencent.mapsdk.raster.model.BitmapDescriptor;
import com.tencent.mapsdk.raster.model.BitmapDescriptorFactory;
import com.tencent.mapsdk.raster.model.CameraPosition;
import com.tencent.mapsdk.raster.model.CircleOptions;
import com.tencent.mapsdk.raster.model.LatLng;
import com.tencent.mapsdk.raster.model.LatLngBounds;
import com.tencent.mapsdk.raster.model.Marker;
import com.tencent.mapsdk.raster.model.MarkerOptions;
import com.tencent.mapsdk.raster.model.Polygon;
import com.tencent.mapsdk.raster.model.PolygonOptions;
import com.tencent.mapsdk.raster.model.Polyline;
import com.tencent.mapsdk.raster.model.PolylineOptions;
import com.tencent.tencentmap.mapsdk.map.CameraUpdateFactory;
import com.tencent.tencentmap.mapsdk.map.MapView;
import com.tencent.tencentmap.mapsdk.map.Projection;
import com.tencent.tencentmap.mapsdk.map.TencentMap;
import com.tencent.tencentmap.mapsdk.map.TencentMap.OnMapCameraChangeListener;
import com.tencent.tencentmap.mapsdk.map.TencentMap.OnMapClickListener;
import com.tencent.tencentmap.mapsdk.map.TencentMap.OnMapLoadedListener;
import com.tencent.tencentmap.mapsdk.map.TencentMap.OnMarkerClickListener;
import com.tencent.tencentmap.mapsdk.map.TencentMap.OnMarkerDraggedListener;
import com.tencent.tencentmap.mapsdk.map.UiSettings;

import java.util.ArrayList;

public class TFenceActivity extends SetFenceParentActivity
    implements OnMapLoadedListener, OnMapClickListener, OnMapCameraChangeListener, OnMarkerDraggedListener, OnMarkerClickListener
{
    private CircleOptions oldCircleOptions;
    private MarkerOptions oldCircleCenterOptions;
    private Polygon oldPolygon;
    private PolygonOptions oldPolygonOptions;
    private Polygon newPolygon;
    private PolygonOptions newPolygonOptions;
    private Polyline newPolyline; // 只有2个点的时候画线
    private PolylineOptions newPolylineOptions;
    private ArrayList<Marker> newPolygonVertexOverlays = new ArrayList<Marker>();
    private BitmapDescriptor newPolygonVertexBd;
    private MarkerOptions deviceLocationOptions;
    protected MapView mMapView = null;
    protected TencentMap mTencentMap = null;

    @Override
    protected void initFence()
    {
        super.initFence();
        if (mFence != null && !TextUtils.isEmpty(mFence.getId()))
        {
            if (mFence.getShape_type() == Fence.SHAPE_CIRCLE)
            {
                // 画圆
                final LatLng latLng = new LatLng(mFence.getLat(), mFence.getLng());
                oldCircleOptions = new CircleOptions().center(latLng).strokeDash(true).radius(mFence.getRadius()).fillColor(Fence.FILL_COLOR).strokeDash(true).strokeWidth(3).strokeColor(Fence.STROKE_COLOR).zIndex(9);
                mTencentMap.addCircle(oldCircleOptions);
                BitmapDescriptor oldCircleCenterBd = BitmapDescriptorFactory.fromResource(R.drawable.pin);
                oldCircleCenterOptions = new MarkerOptions().position(latLng).icon(oldCircleCenterBd).draggable(false).anchor(.5f, .5f);
                mTencentMap.addMarker(oldCircleCenterOptions);
                zoomToSpan(latLng, mRange);
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
                oldPolygonOptions = new PolygonOptions().addAll(latLngList).fillColor(Fence.FILL_COLOR).strokeWidth(3).strokeColor(Fence.STROKE_COLOR).zIndex(9);
                oldPolygon = mTencentMap.addPolygon(oldPolygonOptions);
                zoomToSpan(latLngList);
            }
        }
        else
        {
            final LatLng latLng = new LatLng(mDevice.getLat(), mDevice.getLng());
            mTencentMap.animateTo(latLng);
            zoomToSpan(latLng, mRange);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        if(mDevice == null){
            finish();
            return;
        }

        mMapView = new MapView(this);
        layoutMapView.addView(mMapView, 0);
        mTencentMap = mMapView.getMap();

        initMap();
    }

    private void initMap()
    {
        mMapView.getUiSettings().setScaleControlsEnabled(true);
        mMapView.getUiSettings().setLogoPosition(UiSettings.LOGO_POSITION_LEFT_BOTTOM);
        mMapView.getUiSettings().setScaleViewPosition(UiSettings.SCALEVIEW_POSITION_CENTER_BOTTOM);
        mTencentMap.setOnMapCameraChangeListener(this);
        mTencentMap.setOnMapClickListener(this);
        mTencentMap.setOnMapLoadedListener(this);
        mTencentMap.setOnMarkerDraggedListener(this);
        mTencentMap.setOnMarkerClickListener(this);

        newPolygonVertexBd = BitmapDescriptorFactory.fromResource(R.drawable.icon_marka);
        LatLng locationLatlng = new LatLng(mDevice.getLat(), mDevice.getLng());
        BitmapDescriptor deviceLocationBd = BitmapDescriptorFactory.fromResource(R.drawable.fence_location_marker);
        deviceLocationOptions = new MarkerOptions().position(locationLatlng).icon(deviceLocationBd).draggable(false).anchor(.5f, .5f);
        mTencentMap.addMarker(deviceLocationOptions);
    }

    private void zoomToSpan(LatLng center, int radius)
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
        LatLngBounds bounds = new LatLngBounds.Builder().include(northeast).include(southwest).build();

        mTencentMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 50));
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
            LatLngBounds bounds = builder.build();
            mTencentMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 50));
        }
    }

    @Override
    protected void drawPin(boolean bDeviceLoc)
    {
        LatLng curPosition = null;
        if (bDeviceLoc)
        {
            //以该设备为中心
            if (mDevice != null)
            {
                curPosition = new LatLng(mDevice.getLat(), mDevice.getLng());
            }
        }
        else
        {
            //以屏幕中心为中心
            curPosition = mTencentMap.getMapCenter();
        }
        if (curPosition == null)
        {
            return;
        }
        width = mMapView.getWidth();
        height = mMapView.getHeight();
        Projection projection = mMapView.getProjection();
        pixel = projection.metersToPixels(curPosition.getLatitude(), mRange);
        super.drawPin(bDeviceLoc);
    }

    @Override
    protected void addFence()
    {
        super.addFence();
        if (isCircle)
        {
            final LatLng mapCenter = mTencentMap.getMapCenter();
            String shapeParam = mapCenter.getLatitude() + "," + mapCenter.getLongitude() + "," + mRange;
            sendAddFenceRequest(shapeParam, Fence.SHAPE_CIRCLE);

            // 圆
            mNewFence.setShape_type(Fence.SHAPE_CIRCLE);
            mNewFence.setShape_param(shapeParam);
            mNewFence.setLat(mapCenter.getLatitude());
            mNewFence.setLng(mapCenter.getLongitude());
            mNewFence.setRadius(mRange);
        }
        else
        {
            if (newPolygon != null && newPolygon.getPoints().size() >= 3)
            {
                String shapeParam = "";
                for (LatLng ll : newPolygon.getPoints())
                {
                    String latLngStr = ll.getLongitude() + "," + ll.getLatitude() + ";";
                    shapeParam = shapeParam + latLngStr;
                }
                sendAddFenceRequest(shapeParam,Fence.SHAPE_POLYGON);

                // 多边形
                mNewFence.setShape_type(Fence.SHAPE_POLYGON);
                mNewFence.setShape_param(shapeParam);
            }
            else
            {
                dismissProgressDialog();

                Toast.makeText(TFenceActivity.this, R.string.set_fence_tip, Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void circleClicked()
    {
        if (isCircle)
        {
            return;
        }
        super.circleClicked();

        clearOldPolygonOverlay();
        clearPolygonOverlay();

        LatLng latLng = new LatLng(mDevice.getLat(), mDevice.getLng());
        mTencentMap.animateTo(latLng);
        drawPin(true);
    }

    @Override
    protected void polygonClicked()
    {
        if (!isCircle)
        {
            return;
        }
        super.polygonClicked();

        showOldPolygonOverlay();
        LatLng latLng = new LatLng(mDevice.getLat(), mDevice.getLng());
        mTencentMap.animateTo(latLng);
    }

    private void clearOldPolygonOverlay()
    {
        if (oldPolygon != null)
        {
            oldPolygon.remove();
        }
        oldPolygon = null;
    }

    private void showOldPolygonOverlay()
    {
        if (oldPolygon == null)
        {
            if (oldPolygonOptions != null)
            {
                oldPolygon = mTencentMap.addPolygon(oldPolygonOptions);
            }
        }
    }

    private void clearPolygonOverlay()
    {
        if (newPolygon != null)
        {
            newPolygon.remove();
        }
        if (newPolyline != null)
        {
            newPolyline.remove();
        }
        for (Marker newPolygonVertex : newPolygonVertexOverlays)
        {
            if (newPolygonVertex != null)
            {
                newPolygonVertex.remove();
            }
        }
        newPolygon = null;
        newPolyline = null;
        newPolygonVertexOverlays = new ArrayList<Marker>();
    }

    private void updateZoomButton(float zoomLevel)
    {
        if (zoomLevel < mTencentMap.getMaxZoomLevel())
        {
            mImageButtonZoomIn.setEnabled(true);
        }
        else
        {
            mImageButtonZoomIn.setEnabled(false);
        }
        if (zoomLevel > mTencentMap.getMinZoomLevel())
        {
            mImageButtonZoomOut.setEnabled(true);
        }
        else
        {
            mImageButtonZoomOut.setEnabled(false);
        }
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        if (mMapView != null)
        {
            mMapView.onResume();
        }
    }

    @Override
    protected void onStop()
    {
        super.onStop();
        if (mMapView != null)
        {
            mMapView.onStop();
        }
    }

    @Override
    protected void onPause()
    {
        super.onPause();
        if (mMapView != null)
        {
            mMapView.onPause();
        }
    }

    @Override
    protected void onDestroy()
    {
        if (mTencentMap != null)
        {
            mTencentMap.clearAllOverlays();
        }

        if (mMapView != null)
        {
            mMapView.onDestroy();
        }
        super.onDestroy();
    }

    private void addNewPoly()
    {
        if (newPolygon != null)
        {
            newPolygon.remove();
        }
        if (newPolyline != null)
        {
            newPolyline.remove();
        }

        ArrayList<LatLng> latLngList = new ArrayList<LatLng>();
        for (Marker marker : newPolygonVertexOverlays)
        {
            latLngList.add(marker.getPosition());
        }
        if (latLngList.size() == 2)
        {
            // 只有2个点的时候画线
            newPolylineOptions = new PolylineOptions().width(3).color(0xFF5670BB).addAll(latLngList).zIndex(9);
            newPolyline = mTencentMap.addPolyline(newPolylineOptions);
        }

        if (latLngList.size() >= 3)
        {
            // 画多边形
            newPolygonOptions = new PolygonOptions().addAll(latLngList).fillColor(Fence.FILL_COLOR).strokeWidth(3).strokeColor(Fence.STROKE_COLOR).zIndex(9);
            newPolygon = mTencentMap.addPolygon(newPolygonOptions);
        }
    }

    @Override
    public void onMapLoaded()
    {
        // int y = findViewById(R.id.rl_panel).getTop() - 60;
        // Point p = CommonUtil.getScreentDimention(this);
        // int x = p.x - 100;
        // mMapView.setScaleControlPosition(new Point(x, y));
        if (mFence != null)
        {
            initFence();
        }
    }

    @Override
    public void onMarkerDrag(Marker arg0)
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void onMarkerDragEnd(Marker marker)
    {
        addNewPoly();
    }

    @Override
    public void onMarkerDragStart(Marker arg0)
    {
        // TODO Auto-generated method stub

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
        if (mPreviousZoomLevel == -1f || mPreviousZoomLevel != position.getZoom())
        {
            updateZoomButton(position.getZoom());
            mPreviousZoomLevel = position.getZoom();
        }
        drawPin(false);
    }

    @Override
    public void onMapClick(LatLng latLng)
    {
        // TODO Auto-generated method stub
        if (!isCircle)
        {
            MarkerOptions vertexOptions = new MarkerOptions().position(latLng).icon(newPolygonVertexBd).draggable(true);
            Marker vertexMarker = mTencentMap.addMarker(vertexOptions);
            newPolygonVertexOverlays.add(vertexMarker);
            addNewPoly();
        }
    }

    @Override
    public boolean onMarkerClick(Marker arg0)
    {
        // TODO Auto-generated method stub
        return true;
    }

    @Override
    protected void zoomToSpan(boolean bDeviceLoc)
    {
        LatLng latLng = null;
        if (bDeviceLoc)
        {
            if (mDevice != null)
            {
                latLng = new LatLng(mDevice.getLat(), mDevice.getLng());
            }
        }
        else
        {
            latLng = mTencentMap.getMapCenter();
        }
        if (latLng != null)
        {
            zoomToSpan(latLng, mRange);
        }
    }

    @Override
    protected void animateCamera(double lat, double lng)
    {
        LatLng deviceLocation = new LatLng(lat, lng);
        mTencentMap.animateTo(deviceLocation);
    }

    @Override
    protected void zoomIn()
    {
        mTencentMap.zoomIn();
    }

    @Override
    protected void zoomOut()
    {
        mTencentMap.zoomOut();
    }

    @Override
    protected void polygonFenceClicked()
    {
        clearOldPolygonOverlay();
        clearPolygonOverlay();
        LatLng latLng = new LatLng(mDevice.getLat(), mDevice.getLng());
        mTencentMap.animateTo(latLng);
    }

}
