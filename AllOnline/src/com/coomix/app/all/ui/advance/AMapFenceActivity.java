package com.coomix.app.all.ui.advance;

import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import com.amap.api.maps.AMap;
import com.amap.api.maps.AMap.InfoWindowAdapter;
import com.amap.api.maps.AMap.OnCameraChangeListener;
import com.amap.api.maps.AMap.OnMapClickListener;
import com.amap.api.maps.AMap.OnMapLoadedListener;
import com.amap.api.maps.AMap.OnMarkerClickListener;
import com.amap.api.maps.AMap.OnMarkerDragListener;
import com.amap.api.maps.CameraUpdate;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.MapView;
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
import com.amap.api.maps.model.Polygon;
import com.amap.api.maps.model.PolygonOptions;
import com.amap.api.maps.model.Polyline;
import com.amap.api.maps.model.PolylineOptions;
import com.coomix.app.all.R;
import com.coomix.app.all.model.bean.Fence;

import com.coomix.app.all.ui.advance.SetFenceParentActivity;
import java.util.ArrayList;

public class AMapFenceActivity extends SetFenceParentActivity
    implements OnCameraChangeListener, OnMapClickListener, OnMarkerClickListener, OnMarkerDragListener, OnMapLoadedListener
{
    private LatLng mCenterPoint;
    private AMap aMap;
    private MapView mapView;
    private UiSettings mUiSettings;
    private ArrayList<Marker> mMarkersList = new ArrayList<Marker>();
    private Polygon polygon;
    private Polyline polyline1;
    private Polyline polyline2;
    private String indexDrag = "";
    private int indexPolyline1 = -1;
    private int indexPolyline2 = -1;
    private Polygon originalPolygon;
    private Polyline polylineTwoPoints;
    private PolygonOptions originalPolygonOptions;
    private Circle circle = null;

    private void clearOldPolygonOverlay()
    {
        if (originalPolygon != null)
        {
            originalPolygon.remove();
        }
        originalPolygon = null;
    }

    private void showOldPolygonOverlay()
    {
        if (originalPolygon == null)
        {
            if (originalPolygonOptions != null)
            {
                originalPolygon = aMap.addPolygon(originalPolygonOptions);
            }
        }
    }

    @Override
    protected void initFence()
    {
        super.initFence();
        if (mFence != null && !TextUtils.isEmpty(mFence.getId()))
        {
            if (mFence.getShape_type() == Fence.SHAPE_CIRCLE)
            {
                circle = aMap.addCircle(new CircleOptions().center(new LatLng(mFence.getLat(), mFence.getLng())).radius(mFence.getRadius()).strokeWidth(2).strokeColor(Fence.STROKE_COLOR).fillColor(Fence.FILL_COLOR));
                mCenterPoint = new LatLng(mFence.getLat(), mFence.getLng());
                aMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mCenterPoint, 14.0f));
                if (mFence.getRadius() > 0)
                {
                    mRange = mFence.getRadius();
                    mSeekBar.setProgress(mFence.getRadius() / 100 - 2);
                }
                zoomToSpan(mCenterPoint, mRange);
            }
            else if (mFence.getShape_type() == Fence.SHAPE_POLYGON)
            {
                ArrayList<LatLng> mLatLngList = new ArrayList<LatLng>();
                String latLngList[] = mFence.getShape_param().split(";");
                for (int i = 0; i < latLngList.length; i++)
                {
                    String latLngString[] = latLngList[i].split(",");
                    LatLng mLatLng = new LatLng(Double.valueOf(latLngString[1]), Double.valueOf(latLngString[0]));
                    mLatLngList.add(mLatLng);
                }
                zoomToSpanPolygon(mLatLngList);
                originalPolygonOptions = new PolygonOptions().addAll(mLatLngList).fillColor(Fence.FILL_COLOR).strokeColor(Fence.STROKE_COLOR).strokeWidth(3);
                originalPolygon = aMap.addPolygon(originalPolygonOptions);
            }
        }
        else
        {
            mCenterPoint = new LatLng(mDevice.getLat(), mDevice.getLng());
            aMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mCenterPoint, mPreviousZoomLevel));
            zoomToSpan(mCenterPoint, mRange);
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
        mapView = new MapView(this);
        mapView.onCreate(savedInstanceState);
        layoutMapView.addView(mapView, 0);
        aMap = mapView.getMap();

        initilizeMap();
    }

    /**
     * function to load map. If map is not created it will create it for you
     */
    protected void initilizeMap()
    {
        if (aMap == null)
        {
            aMap = mapView.getMap();
            mUiSettings = aMap.getUiSettings();
        }
        mMaxZoomLevel = aMap.getMaxZoomLevel();
        mMinZoomlevel = aMap.getMinZoomLevel();
        aMap.setOnMapLoadedListener(this);
        aMap.setOnCameraChangeListener(this);
        aMap.setOnMarkerDragListener(this);// 设置marker可拖拽事件监听器
        aMap.setOnMarkerClickListener(this);// 设置点击marker事件监听器
        aMap.setOnMapClickListener(this);
        aMap.getUiSettings().setCompassEnabled(false);// 指南针
        aMap.getUiSettings().setRotateGesturesEnabled(false);
        aMap.getUiSettings().setZoomControlsEnabled(false);

        aMap.setInfoWindowAdapter(new InfoWindowAdapter()
        {
            @Override
            public View getInfoWindow(Marker marker)
            {
                return initPopView();
            }

            @Override
            public View getInfoContents(Marker marker)
            {
                return null;
            }
        });
        LatLng locationLatLng = new LatLng(mDevice.getLat(), mDevice.getLng());
        Marker marker = aMap.addMarker(new MarkerOptions().position(locationLatLng).icon(BitmapDescriptorFactory.fromBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.fence_location_marker))).perspective(true).draggable(false));
    }

    private void zoomToSpan(LatLng latlng, int radius)
    {
        double radiusAngle = Math.toDegrees(radius / Fence.RADIUS_OF_EARTH_METERS) / Math.cos(Math.toRadians(latlng.latitude));
        LatLngBounds.Builder builder = new LatLngBounds.Builder();

        builder.include(new LatLng(latlng.latitude, latlng.longitude + radiusAngle));
        builder.include(new LatLng(latlng.latitude, latlng.longitude - radiusAngle));

        LatLngBounds bounds = builder.build();

        int padding = 50; // offset from edges of the map in pixels
        CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, padding);

        aMap.moveCamera(cu);
    }

    private int meter2Pixel(LatLng loc, double radiusInMeters)
    {
        Point p1 = aMap.getProjection().toScreenLocation(loc);
        Point p2 = aMap.getProjection().toScreenLocation(toRadiusLatLng(loc, radiusInMeters));
        return calculateDistance(p1, p2);
    }

    @Override
    protected void drawPin(boolean bDeviceLoc)
    {
        LatLng center = null;
        if(bDeviceLoc)
        {
            //以该设备为中心
            if(mDevice != null)
            {
                center = new LatLng(mDevice.getLat(), mDevice.getLng());
            }
            else
            {
                center = aMap.getCameraPosition().target;
            }
        }
        else
        {
            //以屏幕中心为中心
            center = aMap.getCameraPosition().target;
        }
        int range = (mSeekBar.getProgress() + 2) * 100;

        width = mapView.getMeasuredWidth();
        height = mapView.getMeasuredHeight();
        pixel = meter2Pixel(center, range);
        super.drawPin(bDeviceLoc);
    }

    /**
     * Generate LatLng of radius marker
     */
    private static LatLng toRadiusLatLng(LatLng center, double radius)
    {
        double radiusAngle = Math.toDegrees(radius / Fence.RADIUS_OF_EARTH_METERS) / Math.cos(Math.toRadians(center.latitude));
        return new LatLng(center.latitude, center.longitude + radiusAngle);
    }

    @Override
    protected void addFence()
    {
        super.addFence();
        if (isCircle)
        {
            LatLng point = aMap.getCameraPosition().target;
            String shapeParam = point.latitude + "," + point.longitude + "," + mRange;
            sendAddFenceRequest(shapeParam, Fence.SHAPE_CIRCLE);
            // 圆
            mNewFence.setShape_type(Fence.SHAPE_CIRCLE);
            mNewFence.setShape_param(shapeParam);
            mNewFence.setLat(point.latitude);
            mNewFence.setLng(point.longitude);
            mNewFence.setRadius(mRange);
        }
        else
        {
            if (mMarkersList.size() >= 3)
            {
                String shapeParam = "";
                for (Marker mMarker : mMarkersList)
                {
                    String latLng = mMarker.getPosition().longitude + "," + mMarker.getPosition().latitude + ";";
                    shapeParam = shapeParam + latLng;
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
                Toast.makeText(this, R.string.set_fence_tip, Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onCameraChange(CameraPosition position)
    {
        if (mPreviousZoomLevel == -1f || mPreviousZoomLevel != position.zoom)
        {
            drawPin(false);
            float level = position.zoom;
            if (level > mMinZoomlevel)
            {
                mImageButtonZoomOut.setEnabled(true);
            }
            else
            {
                mImageButtonZoomOut.setEnabled(false);
            }
            if (level < mMaxZoomLevel)
            {
                mImageButtonZoomIn.setEnabled(true);
            }
            else
            {
                mImageButtonZoomIn.setEnabled(false);
            }
            mPreviousZoomLevel = position.zoom;
        }
    }

    @Override
    protected void onDestroy()
    {
        if (aMap != null)
        {
            aMap.clear();
        }
        if (mMarkersList != null)
        {
            mMarkersList.clear();
        }
        if (mapView != null)
        {
            mapView.onDestroy();
        }
        super.onDestroy();
    }

    @Override
    public void onCameraChangeFinish(CameraPosition arg0)
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void onMarkerDrag(Marker arg0)
    {
        // TODO Auto-generated method stub
        if (mMarkersList.size() >= 3)
        {
            ArrayList<LatLng> polyline1List = new ArrayList<LatLng>();
            polyline1List.add(arg0.getPosition());
            polyline1List.add(mMarkersList.get(indexPolyline1).getPosition());

            ArrayList<LatLng> polyline2List = new ArrayList<LatLng>();
            polyline2List.add(arg0.getPosition());
            polyline2List.add(mMarkersList.get(indexPolyline2).getPosition());

            polyline1.setPoints(polyline1List);

            polyline2.setPoints(polyline2List);
        }
    }

    @Override
    public void onMarkerDragEnd(Marker arg0)
    {
        // TODO Auto-generated method stub
        if (mMarkersList.size() >= 3)
        {
            polyline1.remove();
            polyline2.remove();

            if (mMarkersList.size() >= 3)
            {
                polygon.setPoints(createPolygon(mMarkersList));
            }
        }
    }

    @Override
    public void onMarkerDragStart(Marker arg0)
    {
        // TODO Auto-generated method stub
        if (mMarkersList.size() >= 3)
        {
            indexDrag = arg0.getSnippet();
            if (indexDrag.equals("0"))
            {
                indexPolyline1 = mMarkersList.size() - 1;
                indexPolyline2 = 1;
            }
            else if (indexDrag.equals(String.valueOf(mMarkersList.size() - 1)))
            {
                indexPolyline1 = mMarkersList.size() - 2;
                indexPolyline2 = 0;
            }
            else
            {
                indexPolyline1 = Integer.valueOf(indexDrag) - 1;
                indexPolyline2 = Integer.valueOf(indexDrag) + 1;
            }

            polyline1 = aMap.addPolyline((new PolylineOptions()).add(arg0.getPosition(), mMarkersList.get(indexPolyline1).getPosition()).width(3).setDottedLine(true).geodesic(true).color(Fence.STROKE_COLOR));
            polyline2 = aMap.addPolyline((new PolylineOptions()).add(arg0.getPosition(), mMarkersList.get(indexPolyline2).getPosition()).width(3).setDottedLine(true).geodesic(true).color(Fence.STROKE_COLOR));
        }
    }

    @Override
    public void onMapClick(LatLng arg0)
    {
        // TODO Auto-generated method stub
        if (!isCircle)
        {
            BitmapDescriptor newPolygonVertexBd = BitmapDescriptorFactory.fromBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.icon_marka));
            Marker marker = aMap.addMarker(new MarkerOptions().position(arg0).icon(newPolygonVertexBd).perspective(true).draggable(true));//BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)
            marker.setSnippet(String.valueOf(mMarkersList.size()));
            mMarkersList.add(marker);

            if (mMarkersList.size() == 2)
            {
                polylineTwoPoints = aMap.addPolyline((new PolylineOptions()).add(mMarkersList.get(0).getPosition(), mMarkersList.get(1).getPosition()).width(3).color(Fence.STROKE_COLOR));
            }
            else if (mMarkersList.size() == 3)
            {
                if (polylineTwoPoints != null)
                {
                    polylineTwoPoints.remove();
                }
                polygon = aMap.addPolygon(new PolygonOptions().addAll(createPolygon(mMarkersList)).fillColor(Fence.FILL_COLOR).strokeColor(Fence.STROKE_COLOR).strokeWidth(3));
            }
            else if (mMarkersList.size() > 3)
            {
                polygon.setPoints(createPolygon(mMarkersList));
            }
        }
    }

    private ArrayList<LatLng> createPolygon(ArrayList<Marker> markersList)
    {
        ArrayList<LatLng> latLngList = new ArrayList<LatLng>();
        for (Marker mMarker : markersList)
        {
            latLngList.add(mMarker.getPosition());
        }
        return latLngList;
    }

    @Override
    public boolean onMarkerClick(Marker arg0)
    {
        // TODO Auto-generated method stub
        return false;
    }

    private void zoomToSpanPolygon(ArrayList<LatLng> latLngList)
    {
        LatLngBounds.Builder builder = LatLngBounds.builder();
        for (LatLng mLatLng : latLngList)
        {
            builder.include(mLatLng);
        }
        aMap.moveCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), 50));
    }

    @Override
    public void onMapLoaded()
    {
        initFence();
    }

    @Override
    protected void circleClicked()
    {
        if(isCircle)
        {
            return;
        }
        super.circleClicked();
        if (mMarkersList != null && mMarkersList.size() > 0)
        {
            for (Marker mMarker : mMarkersList)
            {
                mMarker.remove();
            }
        }
        if (polygon != null)
        {
            polygon.remove();
        }
        clearOldPolygonOverlay();
        if (originalPolygon != null)
        {
            originalPolygon.setVisible(false);
        }
        if (circle != null)
        {
            circle.setVisible(true);
        }

        mMarkersList.clear();

        mCenterPoint = new LatLng(mDevice.getLat(), mDevice.getLng());
        aMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mCenterPoint, mPreviousZoomLevel));
    }

    @Override
    protected void polygonClicked()
    {
        super.polygonClicked();
        if (mMarkersList != null && mMarkersList.size() > 0)
        {
            for (Marker mMarker : mMarkersList)
            {
                mMarker.remove();
            }
        }
        if (polygon != null)
        {
            polygon.remove();
        }
        if (originalPolygon != null)
        {
            originalPolygon.setVisible(true);
        }
        if (circle != null)
        {
            circle.setVisible(false);
        }

        mMarkersList.clear();
        showOldPolygonOverlay();
        aMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(mDevice.getLat(), mDevice.getLng()), mPreviousZoomLevel));
    }

    @Override
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
            latLng = aMap.getCameraPosition().target;
        }
        if(latLng != null)
        {
            zoomToSpan(latLng, mRange);
        }
    }

    @Override
    protected void animateCamera(double lat, double lng)
    {
        aMap.animateCamera(CameraUpdateFactory.changeLatLng(new LatLng(lat, lng)));
    }

    @Override
    protected void zoomIn()
    {
        float level = aMap.getCameraPosition().zoom;
        aMap.animateCamera(CameraUpdateFactory.zoomTo(level + 1f));
    }

    @Override
    protected void zoomOut()
    {
        float level = aMap.getCameraPosition().zoom;
        aMap.animateCamera(CameraUpdateFactory.zoomTo(level - 1f));
    }

    @Override
    protected void polygonFenceClicked()
    {
        clearOldPolygonOverlay();
        if (mMarkersList != null && mMarkersList.size() > 0)
        {
            for (Marker mMarker : mMarkersList)
            {
                mMarker.remove();
            }
        }
        if (polygon != null)
        {
            polygon.remove();
        }

        mMarkersList.clear();

        mCenterPoint = new LatLng(mDevice.getLat(), mDevice.getLng());
        aMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mCenterPoint, mPreviousZoomLevel));
    }

}
