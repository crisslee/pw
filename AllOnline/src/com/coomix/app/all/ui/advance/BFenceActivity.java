package com.coomix.app.all.ui.advance;

import android.graphics.Point;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Toast;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BaiduMap.OnMapClickListener;
import com.baidu.mapapi.map.BaiduMap.OnMapLoadedCallback;
import com.baidu.mapapi.map.BaiduMap.OnMapStatusChangeListener;
import com.baidu.mapapi.map.BaiduMap.OnMarkerDragListener;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.CircleOptions;
import com.baidu.mapapi.map.MapPoi;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.Polygon;
import com.baidu.mapapi.map.PolygonOptions;
import com.baidu.mapapi.map.Polyline;
import com.baidu.mapapi.map.PolylineOptions;
import com.baidu.mapapi.map.Stroke;
import com.baidu.mapapi.map.UiSettings;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.model.LatLngBounds;
import com.baidu.mapapi.utils.DistanceUtil;
import com.coomix.app.all.R;
import com.coomix.app.all.model.bean.Fence;
import com.coomix.app.framework.util.CommonUtil;
import java.util.ArrayList;

public class BFenceActivity extends SetFenceParentActivity implements OnMapLoadedCallback, OnMapClickListener,
    OnMapStatusChangeListener, OnMarkerDragListener {

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
    private MapView mMapView;
    private BaiduMap mBaiduMap;
    private LatLng curLatLng = null;

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
                Stroke stroke = new Stroke(2, Fence.STROKE_COLOR);
                oldCircleOptions = new CircleOptions().center(latLng).radius(mFence.getRadius()).fillColor(Fence.FILL_COLOR).stroke(stroke);
                mBaiduMap.addOverlay(oldCircleOptions);
                BitmapDescriptor oldCircleCenterBd = BitmapDescriptorFactory.fromResource(R.drawable.pin);
                oldCircleCenterOptions = new MarkerOptions().position(latLng).icon(oldCircleCenterBd).zIndex(9).draggable(false);
                mBaiduMap.addOverlay(oldCircleCenterOptions);
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
                Stroke stroke = new Stroke(3, Fence.STROKE_COLOR);
                oldPolygonOptions = new PolygonOptions().points(latLngList).fillColor(Fence.FILL_COLOR).stroke(stroke);
                oldPolygon = (Polygon) mBaiduMap.addOverlay(oldPolygonOptions);

                zoomToSpan(latLngList);
            }
        }
        else
        {
            final LatLng latLng = new LatLng(mDevice.getLat(), mDevice.getLng());
            mBaiduMap.animateMapStatus(MapStatusUpdateFactory.newLatLng(latLng));
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
        mBaiduMap = mMapView.getMap();
        initMap();
    }

    private void initMap()
    {
        UiSettings uiSettings = mBaiduMap.getUiSettings();
        uiSettings.setOverlookingGesturesEnabled(false);
        uiSettings.setRotateGesturesEnabled(false);
        uiSettings.setCompassEnabled(false);

        mBaiduMap.setOnMapClickListener(this);
        mBaiduMap.setOnMapLoadedCallback(this);
        mBaiduMap.setOnMapStatusChangeListener(this);
        mBaiduMap.setOnMarkerDragListener(this);
        mMapView.showScaleControl(true);
        mMapView.showZoomControls(false);

        newPolygonVertexBd = BitmapDescriptorFactory.fromResource(R.drawable.icon_marka);
        LatLng locationLatlng = new LatLng(mDevice.getLat(), mDevice.getLng());
        BitmapDescriptor deviceLocationBd = BitmapDescriptorFactory.fromResource(R.drawable.fence_location_marker);
        deviceLocationOptions = new MarkerOptions().position(locationLatlng).icon(deviceLocationBd).zIndex(9).draggable(false);
        mBaiduMap.addOverlay(deviceLocationOptions);
    }

    private void zoomToSpan(LatLng center, int radius)
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
        LatLngBounds bounds = new LatLngBounds.Builder().include(northeast).include(southwest).build();

        mBaiduMap.animateMapStatus(MapStatusUpdateFactory.newLatLngBounds(bounds));
    }

    @Override
    protected void drawPin(boolean bDeviceLoc)
    {
        LatLng curPosition = null;
        if (bDeviceLoc)
        {
            if(mDevice != null)
            {
                curPosition = new LatLng(mDevice.getLat(), mDevice.getLng());
            }
        }
        else
        {
            curPosition = curLatLng;
        }
        if(curPosition == null || mBaiduMap.getProjection() == null)
        {
            return;
        }
        //mBaiduMap.animateMapStatus(MapStatusUpdateFactory.newLatLng(curPosition));
        width = mMapView.getWidth();
        height = mMapView.getHeight();
        // 一经度的距离(同纬度下，一个经度差的距离认为是相同的) 经度最大180
        double curLatMeter = DistanceUtil.getDistance(curPosition, new LatLng(curPosition.latitude, curPosition.longitude < 0 ? curPosition.longitude + 1 : curPosition.longitude - 1));
        double zeroLatMeter = DistanceUtil.getDistance(new LatLng(0, 0), new LatLng(0, 1));
        pixel = mBaiduMap.getProjection().metersToEquatorPixels((float) (mRange * zeroLatMeter / curLatMeter));

        super.drawPin(bDeviceLoc);
    }

    @Override
    protected void addFence()
    {
        super.addFence();
        if (isCircle)
        {
            final LatLng mapCenter = mBaiduMap.getMapStatus().target;
            String shapeParam = mapCenter.latitude + "," + mapCenter.longitude + "," + mRange;
            sendAddFenceRequest(shapeParam,Fence.SHAPE_CIRCLE);
            // 圆
            mNewFence.setShape_type(Fence.SHAPE_CIRCLE);
            mNewFence.setShape_param(shapeParam);
            mNewFence.setLat(mapCenter.latitude);
            mNewFence.setLng(mapCenter.longitude);
            mNewFence.setRadius(mRange);
        }
        else
        {
            if (newPolygon != null && newPolygon.getPoints().size() >= 3)
            {
                String shapeParam = "";
                for (LatLng ll : newPolygon.getPoints())
                {
                    String latLngStr = ll.longitude + "," + ll.latitude + ";";
                    shapeParam = shapeParam + latLngStr;
                }
                sendAddFenceRequest(shapeParam,Fence.SHAPE_POLYGON);
                // 多边形
                mNewFence.setShape_type(Fence.SHAPE_POLYGON);
                mNewFence.setShape_param(shapeParam);
            }
            else
            {
//                if (dialog != null)
//                {
//                    dialog.dismiss();
//                }
                dismissProgressDialog();

                Toast.makeText(BFenceActivity.this, R.string.set_fence_tip, Toast.LENGTH_SHORT).show();
            }
        }
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
                oldPolygon = (Polygon) mBaiduMap.addOverlay(oldPolygonOptions);
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

    private void updateZoomButton()
    {
        float level = mBaiduMap.getMapStatus().zoom;
        if (level < mBaiduMap.getMaxZoomLevel())
        {
            mImageButtonZoomIn.setEnabled(true);
        }
        else
        {
            mImageButtonZoomIn.setEnabled(false);
        }
        if (level > mBaiduMap.getMinZoomLevel())
        {
            mImageButtonZoomOut.setEnabled(true);
        }
        else
        {
            mImageButtonZoomOut.setEnabled(false);
        }
    }

    @Override
    protected void onDestroy()
    {
        if (newPolygonVertexBd != null)
        {
            newPolygonVertexBd.recycle();
        }

        if (mBaiduMap != null)
        {
            mBaiduMap.clear();
        }
        if (mMapView != null)
        {
            mMapView.onDestroy();
        }
        super.onDestroy();
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
    protected void onResume()
    {
        super.onResume();
        if (mMapView != null)
        {
            mMapView.onResume();
        }
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
            mBaiduMap.animateMapStatus(MapStatusUpdateFactory.newLatLngBounds(bounds));
        }
    }

    @Override
    public void onMapClick(LatLng latLng)
    {
        if (!isCircle)
        {
            MarkerOptions vertexOptions = new MarkerOptions().position(latLng).icon(newPolygonVertexBd).zIndex(9).draggable(true);
            Marker vertexMarker = (Marker) mBaiduMap.addOverlay(vertexOptions);
            newPolygonVertexOverlays.add(vertexMarker);
            addNewPoly();
        }
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
            newPolylineOptions = new PolylineOptions().width(3).color(0xFF5670BB).points(latLngList);
            newPolyline = (Polyline) mBaiduMap.addOverlay(newPolylineOptions);
        }

        if (latLngList.size() >= 3)
        {
            // 画多边形
            Stroke stroke = new Stroke(3, Fence.STROKE_COLOR);
            newPolygonOptions = new PolygonOptions().points(latLngList).fillColor(Fence.FILL_COLOR).stroke(stroke);
            newPolygon = (Polygon) mBaiduMap.addOverlay(newPolygonOptions);
        }
    }

    @Override
    public void onMapPoiClick(MapPoi arg0) {
    }

    @Override
    public void onMapLoaded()
    {
        int y = findViewById(R.id.rl_panel).getTop() - 60;
        Point p = CommonUtil.getScreentDimention(this);
        int x = p.x - 100;
        mMapView.setScaleControlPosition(new Point(x, y));
        if (mFence != null)
        {
            initFence();
        }
    }

    @Override
    public void onMapStatusChange(MapStatus position)
    {
        // MapStatus position = mBaiduMap.getMapStatus();
    }

    @Override
    public void onMapStatusChangeFinish(MapStatus position)
    {
        if (mPreviousZoomLevel == -1f || mPreviousZoomLevel != position.zoom)
        {
            updateZoomButton();
            mPreviousZoomLevel = position.zoom;
        }
        curLatLng = position.target;
        drawPin(false);
    }

    @Override
    public void onMapStatusChangeStart(MapStatus arg0) {
    }

    @Override
    public void onMarkerDrag(Marker arg0) {
    }

    @Override
    public void onMarkerDragEnd(Marker marker)
    {
        addNewPoly();
    }

    @Override
    public void onMarkerDragStart(Marker arg0) {
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
        drawPin(true);
        isCircle = true;
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
        mBaiduMap.animateMapStatus(MapStatusUpdateFactory.newLatLng(latLng));
    }

    protected void zoomToSpan(boolean bDeviceLoc)
    {
        LatLng latLng = null;
        if(bDeviceLoc)
        {
            if(mDevice != null)
            {
                latLng = new LatLng(mDevice.getLat(), mDevice.getLng());
            }
        }
        else
        {
            latLng = mBaiduMap.getMapStatus().target;
        }
        if(latLng != null)
        {
            zoomToSpan(latLng, mRange);
        }
    }

    @Override
    protected void animateCamera(double lat, double lng)
    {
        mBaiduMap.setMapStatus(MapStatusUpdateFactory.newLatLng(new LatLng(lat, lng)));
    }

    protected void zoomIn()
    {
        mBaiduMap.animateMapStatus(MapStatusUpdateFactory.zoomIn());
    }

    protected void zoomOut()
    {
        mBaiduMap.animateMapStatus(MapStatusUpdateFactory.zoomOut());
    }

    protected void polygonFenceClicked()
    {
        clearOldPolygonOverlay();
        clearPolygonOverlay();
        LatLng latLng = new LatLng(mDevice.getLat(), mDevice.getLng());
        mBaiduMap.animateMapStatus(MapStatusUpdateFactory.newLatLng(latLng));
    }

    @Override
    public void onMapStatusChangeStart(MapStatus mapStatus, int i)
    {

    }
}
