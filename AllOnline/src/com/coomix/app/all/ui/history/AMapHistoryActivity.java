package com.coomix.app.all.ui.history;

import android.graphics.Color;
import android.graphics.PointF;
import android.location.Location;
import android.os.Bundle;
import android.os.RemoteException;
import android.view.LayoutInflater;
import android.view.View;
import com.amap.api.maps.AMap;
import com.amap.api.maps.AMap.InfoWindowAdapter;
import com.amap.api.maps.AMap.OnMapClickListener;
import com.amap.api.maps.AMap.OnMapLongClickListener;
import com.amap.api.maps.AMap.OnMarkerClickListener;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.CustomRenderer;
import com.amap.api.maps.MapView;
import com.amap.api.maps.Projection;
import com.amap.api.maps.UiSettings;
import com.amap.api.maps.model.BitmapDescriptor;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.Polyline;
import com.amap.api.maps.model.PolylineOptions;
import com.amap.api.maps.model.VisibleRegion;
import com.coomix.app.all.R;
import com.coomix.app.all.manager.MapIconManager;
import com.coomix.app.all.map.baidu.OverlayType;
import com.coomix.app.all.model.bean.TrackPoint;
import com.coomix.app.framework.util.CommonUtil;
import com.coomix.app.framework.util.TimeUtil;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class AMapHistoryActivity extends HistoryParentActivity implements OnMapLongClickListener, OnMarkerClickListener,
    OnMapClickListener, InfoWindowAdapter, CustomRenderer {
    private Polyline mPathOverlay;
    private ArrayList<LatLng> mPathPoints = new ArrayList<LatLng>();
    private CopyOnWriteArrayList<LatLng> mLbsPoints = new CopyOnWriteArrayList<LatLng>();
    private Marker mCurLocationMarker;
    private Marker mPopInfoWindow; // 专门用作弹窗marker
    private BitmapDescriptor curLocationBd, mStayPointBd, markerNothing, markerLbsPointBd; // nothing为mPopInfoWindow的背景;
    private MapView mMapView = null;
    private AMap mAMap = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (layoutMapView == null) {
            finish();
            return;
        }
        mMapView = new MapView(this);
        mMapView.onCreate(savedInstanceState);
        layoutMapView.addView(mMapView);
        mAMap = mMapView.getMap();

        initMapLocation();

        View nothing = LayoutInflater.from(this).inflate(R.layout.nothing, null);
        markerNothing = BitmapDescriptorFactory.fromView(nothing);
        // DotView
        View dotView = LayoutInflater.from(this).inflate(R.layout.dot_view, null);
        markerLbsPointBd = BitmapDescriptorFactory.fromView(dotView);

        LatLng ll = new LatLng(mDevice.getLat(), mDevice.getLng());
        setCurLocation(ll);
    }

    @Override
    public void onStart() {
        super.onStart();
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
    }

    @Override
    public void onDestroy() {
        if (mAMap != null) {
            mAMap.clear();
        }
        if (mMapView != null) {
            mMapView.onDestroy();
        }

        super.onDestroy();
    }

    private void setCurLocation(LatLng latLng) {
        if (mCurLocationMarker != null) {
            mCurLocationMarker.remove();
        }

        if (mArrayListTrackPoints.size() <= 0) {
            return;
        }

        if (mCurrentIndex > mArrayListTrackPoints.size()) {
            mCurrentIndex = mArrayListTrackPoints.size();
        }

        TrackPoint curPoint = null;
        if (mCurrentIndex > 0) {
            curPoint = mArrayListTrackPoints.get(mCurrentIndex - 1);
        }
        if (curPoint == null) {
            return;
        }
        BitmapDescriptor bd = MapIconManager.getInstance().getAmapHistoryIcon(curPoint);
        MarkerOptions options = new MarkerOptions().position(latLng).icon(bd).draggable(false).zIndex(9.0f)
            .anchor(0.5f, 0.5f);
        mCurLocationMarker = mAMap.addMarker(options);
        mCurLocationMarker.setRotateAngle(-1 * curPoint.course);
    }

    private void drawPathOverlay(List<LatLng> points) {
        if (points != null && points.size() >= 2) {
            setCurLocation(points.get(points.size() - 1));
            if (mPathOverlay != null) {
                mPathOverlay.remove();
            }
            PolylineOptions options = new PolylineOptions().width(12)
                .color(Color.argb(255, 0, 255, 0))
                .addAll(points).zIndex(9);
            mPathOverlay = mAMap.addPolyline(options);
        }
    }

    private void addPathPoint(LatLng latLng) {
        if (latLng == null) {
            return;
        }
        if (mPathPoints == null) {
            mPathPoints = new ArrayList<LatLng>();
        }
        mPathPoints.add(latLng);
        if (mPathPoints.size() >= 2) {
            setCurLocation(mPathPoints.get(mPathPoints.size() - 1));
            if (mPathOverlay != null) {
                mPathOverlay.remove();
            }
            PolylineOptions options = new PolylineOptions().width(12)
                .color(Color.argb(255, 0, 255, 0)).addAll(mPathPoints).zIndex(9);
            mPathOverlay = mAMap.addPolyline(options);
        }
    }

    @Override
    protected void progressChange(int progress) {
        synchronized (mLocker) {
            mTotalDistance = 0;
            mTotalStayTime = 0;
            currentRunTime = 0;
            mArrayListStayPoints.clear();
            mPathPoints.clear();
            mLbsPoints.clear();
            mLastTrackPoint = null;
            mCount = 0;
            stayNum = 0;
            mAMap.clear();
            int tmpIndex = (int) ((progress + 0.0) / 100 * mArrayListTrackPoints.size());
            mCurrentIndex = tmpIndex > 0 ? tmpIndex : 1;

            if (mArrayListTrackPoints.size() > 0) {
                addStartPoint(mArrayListTrackPoints.get(0));
            }
            if (mCurrentIndex > 1) {
                ArrayList<LatLng> tempLbsPoints = new ArrayList<LatLng>();
                TrackPoint point = null;
                for (int i = 0; i < (mCurrentIndex - 1); i++) {
                    point = mArrayListTrackPoints.get(i);
                    LatLng latLng = new LatLng(point.lat, point.lng);
                    if (mLastTrackPoint == null || !isShiftPoint(mLastTrackPoint, point)) {
                        if (!isFilterLbs || !isLbsPoint(point)) {
                            mPathPoints.add(latLng);
                            checkIsStayPoint(mLastTrackPoint, point);
                            if (isLbsPoint(point)) {
                                tempLbsPoints.add(latLng);
                                addLbsPoint(latLng);
                            }
                        }
                        addStayTime(mLastTrackPoint, point);
                        mLastTrackPoint = point;
                    }
                }
                if (point != null) {
                    textDistance.setText(getString(R.string.label_mileage) + CommonUtil.formatRangeSize(this,
                        mTotalDistance));
                    textDateTime.setText(TimeUtil.long2DateTimeString(point.gps_time * 1000));
                    textSpeed.setText(getString(R.string.label_speed) + getHistorySpeed(point.speed));
                }
                mLbsPoints.addAll(tempLbsPoints);
                drawPathOverlay(mPathPoints);
            }
        }
    }

    private boolean isOutOfMap(LatLng point) {
        if (point == null) {
            return false;
        }
        VisibleRegion vr = mAMap.getProjection().getVisibleRegion();
        double left = vr.latLngBounds.southwest.longitude;
        double top = vr.latLngBounds.northeast.latitude;
        double right = vr.latLngBounds.northeast.longitude;
        double bottom = vr.latLngBounds.southwest.latitude;
        if (point.longitude < left || point.longitude > right || point.latitude > top || point.latitude < bottom) {
            return true;
        }
        return false;
    }

    //起点
    @Override
    protected void addStartPoint(TrackPoint point) {
        LatLng ll = new LatLng(point.lat, point.lng);
        MarkerOptions options = new MarkerOptions().position(ll)
            .icon(BitmapDescriptorFactory.fromResource(R.drawable.history_start_point));
        mAMap.addMarker(options);
    }

    //终点
    protected void addEndPoint(TrackPoint point) {
        LatLng ll = new LatLng(point.lat, point.lng);
        MarkerOptions options = new MarkerOptions().position(ll)
            .icon(BitmapDescriptorFactory.fromResource(R.drawable.history_end_point));
        mAMap.addMarker(options);
    }

    protected void updateView(int index) {
        synchronized (mLocker) {
            if (mMapView != null && mArrayListTrackPoints != null && mArrayListTrackPoints.size() > 0
                && index <= mArrayListTrackPoints.size() && index > 0) {
                TrackPoint tPoint = mArrayListTrackPoints.get(index - 1);
                int seekBarIndex = (int) ((index + 0.0) / mArrayListTrackPoints.size() * 100);
                mSeekBar.setProgress(seekBarIndex);
                if (index == 1) {
                    addStartPoint(tPoint);
                }
                if (mLastTrackPoint == null || !isShiftPoint(mLastTrackPoint, tPoint)) {
                    if (!isFilterLbs || !isLbsPoint(tPoint)) {
                        textDistance.setText(getString(R.string.label_mileage) + CommonUtil.formatRangeSize(this,
                            mTotalDistance));
                        textDateTime.setText(TimeUtil.long2DateTimeString(tPoint.gps_time * 1000));
                        textSpeed.setText(getString(R.string.label_speed) + getHistorySpeed(tPoint.speed));
                        LatLng centerPoint = new LatLng(tPoint.lat, tPoint.lng);
                        if (mCount >= 14) {
                            mCount = 0;
                            mPathOverlay.remove();
                        }
                        if (isLbsPoint(tPoint)) {
                            addLbsPoint(centerPoint);
                        }
                        addPathPoint(centerPoint);
                        mCount++;
                        checkIsStayPoint(mLastTrackPoint, tPoint);

                        if (isOutOfMap(centerPoint)) {
                            mAMap.animateCamera(CameraUpdateFactory.changeLatLng(new LatLng(tPoint.lat, tPoint.lng)));
                        }
                    }
                    addStayTime(mLastTrackPoint, tPoint);
                    String runTime = TimeUtil.sec2Time(currentRunTime);
                    textStartTime.setText(runTime);
                    mLastTrackPoint = tPoint;
                }
                if (mArrayListTrackPoints != null) {
                    int size = mArrayListTrackPoints.size();
                    if (index == size) {
                        addEndPoint(mArrayListTrackPoints.get(size - 1));
                    }
                }
            }
        }
    }

    private void addLbsPoint(LatLng ll) {
        if (ll != null) {
            MarkerOptions options = new MarkerOptions().position(ll).icon(markerLbsPointBd).draggable(false)
                .anchor(0.5f, 0.5f);
            mAMap.addMarker(options);
        }
    }

    @Override
    protected void resetData() {
        mPathPoints.clear();
        mLbsPoints.clear();
        if (mAMap != null) {
            mAMap.clear();
        }
        super.resetData();
    }

    private void hideInfoWindow() {
        if (mPopInfoWindow != null) {
            if (mPopInfoWindow.isInfoWindowShown()) {
                mPopInfoWindow.hideInfoWindow();
            }
            mPopInfoWindow.remove();
            mPopInfoWindow = null;
        }
    }

    @Override
    public void onMapLongClick(LatLng arg0) {
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        Bundle extraInfo = (Bundle) marker.getObject();
        if (extraInfo != null) {
            TrackPoint trackPoint = (TrackPoint) extraInfo.getSerializable("TrackPoint");
            if (trackPoint != null) {
                curShowStayPoint = trackPoint;
                showStayPointInfo(trackPoint);
            }
        }
        return true;
    }

    @Override
    public void onMapClick(LatLng arg0) {
        hideInfoWindow();
        curShowStayPoint = null;
    }

    @Override
    public View getInfoWindow(Marker marker) {
        Bundle extra = (Bundle) marker.getObject();
        if (extra != null) {
            String type = extra.getString("type");
            if (type != null && type.equals(OverlayType.OVERLAY_STAYPOINT)) {
                return mPopupView;
            }
        }
        return null;
    }

    @Override
    public View getInfoContents(Marker arg0) {
        return null;
    }

    // 使用OpenGl的方法绘制LBS点

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        try {
            drawCircles(gl);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void OnMapReferencechanged() {
    }

    public void drawCircles(GL10 gl) throws RemoteException {
        if (mLbsPoints != null && mAMap != null) {
            Projection projection = mAMap.getProjection();
            // 10个像素宽度对应的opengl宽度
            float ratio = projection.toOpenGLWidth(10);
            for (LatLng ll : mLbsPoints) {
                PointF point = projection.toOpenGLLocation(ll);
                drawCircle(gl, Color.argb(255, 128, 0, 128), point, ratio);
            }
        }
    }

    private void initMapLocation() {
        mAMap.setOnMapLongClickListener(this);
        mAMap.setOnMapClickListener(this);
        mAMap.setOnMarkerClickListener(this);
        mAMap.setInfoWindowAdapter(this);
        mAMap.setCustomRenderer(this);
        UiSettings uiSettings = mAMap.getUiSettings();
        uiSettings.setZoomControlsEnabled(false);// false不显示缩放控件 默认不显示
        uiSettings.setScaleControlsEnabled(true); // 设置地图默认的比例尺
        uiSettings.setCompassEnabled(false);// 设置指南针
        uiSettings.setScrollGesturesEnabled(true);// 设置地图是否可以手势滑动
        uiSettings.setZoomGesturesEnabled(true);
        uiSettings.setTiltGesturesEnabled(true);
        uiSettings.setRotateGesturesEnabled(false);

        LatLng ll = new LatLng(mDevice.getLat(), mDevice.getLng());
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mAMap.animateCamera(CameraUpdateFactory.newLatLngZoom(ll, 16.0f));
            }
        }, 300);
    }

    @Override
    protected void addStayPoint(TrackPoint trackPoint) {
        if (trackPoint == null) {
            return;
        }
        Bundle extraInfo = new Bundle();
        extraInfo.putSerializable("TrackPoint", trackPoint);
        LatLng ll = new LatLng(trackPoint.lat, trackPoint.lng);
        MarkerOptions options = new MarkerOptions().position(ll)
            .icon(BitmapDescriptorFactory.fromView(getStayPointView())).draggable(false);
        mAMap.addMarker(options).setObject(extraInfo);
    }

    @Override
    protected void showInfoWindow(TrackPoint trackPoint, Bundle extra) {
        if (trackPoint == null || extra == null) {
            return;
        }
        if (mPopInfoWindow != null) {
            if (mPopInfoWindow.isInfoWindowShown()) {
                mPopInfoWindow.hideInfoWindow();
            }
            mPopInfoWindow.remove();
            mPopInfoWindow = null;
        }
        LatLng ll = new LatLng(trackPoint.lat, trackPoint.lng);
        MarkerOptions options = new MarkerOptions().position(ll).icon(markerNothing)
            .draggable(false).anchor(0.5f, 0.5f).title("infoWindow");
        options.setInfoWindowOffset(0, -70);
        mPopInfoWindow = mAMap.addMarker(options);
        mPopInfoWindow.setObject(extra);
        mPopInfoWindow.showInfoWindow();
    }

    @Override
    protected double getDistance(TrackPoint center, TrackPoint radius) {
        float[] result = new float[1];
        Location.distanceBetween(center.lat, center.lng, radius.lat, radius.lng, result);
        return result[0];
    }
}
