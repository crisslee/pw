package com.coomix.app.all.ui.history;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import com.coomix.app.all.R;
import com.coomix.app.all.manager.MapIconManager;
import com.coomix.app.all.map.baidu.OverlayType;
import com.coomix.app.all.model.bean.TrackPoint;
import com.coomix.app.framework.util.CommonUtil;
import com.coomix.app.framework.util.TimeUtil;
import com.tencent.mapsdk.raster.model.BitmapDescriptor;
import com.tencent.mapsdk.raster.model.BitmapDescriptorFactory;
import com.tencent.mapsdk.raster.model.CameraPosition;
import com.tencent.mapsdk.raster.model.LatLng;
import com.tencent.mapsdk.raster.model.LatLngBounds;
import com.tencent.mapsdk.raster.model.Marker;
import com.tencent.mapsdk.raster.model.MarkerOptions;
import com.tencent.mapsdk.raster.model.Polyline;
import com.tencent.mapsdk.raster.model.PolylineOptions;
import com.tencent.tencentmap.mapsdk.map.CameraUpdateFactory;
import com.tencent.tencentmap.mapsdk.map.MapView;
import com.tencent.tencentmap.mapsdk.map.TencentMap;
import com.tencent.tencentmap.mapsdk.map.TencentMap.InfoWindowAdapter;
import com.tencent.tencentmap.mapsdk.map.TencentMap.OnMapCameraChangeListener;
import com.tencent.tencentmap.mapsdk.map.TencentMap.OnMapClickListener;
import com.tencent.tencentmap.mapsdk.map.TencentMap.OnMapLongClickListener;
import com.tencent.tencentmap.mapsdk.map.TencentMap.OnMarkerClickListener;
import com.tencent.tencentmap.mapsdk.map.UiSettings;
import java.util.ArrayList;
import java.util.List;

public class TMapHistoryActivity extends HistoryParentActivity implements OnMapLongClickListener, OnMarkerClickListener,
    OnMapClickListener, InfoWindowAdapter, OnMapCameraChangeListener {
    private Polyline mPathOverlay;
    private ArrayList<LatLng> mPathPoints = new ArrayList<LatLng>();
    private Marker mCurLocationMarker;
    private Marker mPopInfoWindow; // 专门用作弹窗marker
    private BitmapDescriptor curLocationBd, mStayPointBd, markerNothing, markerLbsPointBd; // nothing为mPopInfoWindow的背景;
    private MapView mMapView = null;
    private TencentMap mTencentMap = null;
    private CameraPosition mPreviousCameraPosition;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mMapView = new MapView(TMapHistoryActivity.this);
        layoutMapView.addView(mMapView);
        mTencentMap = mMapView.getMap();

        initMapLocation();

        // Infowindow
        View nothing = LayoutInflater.from(this).inflate(R.layout.nothing, null);
        markerNothing = BitmapDescriptorFactory.fromView(nothing);
        //DotView
        View dotView = LayoutInflater.from(this).inflate(R.layout.dot_view, null);
        markerLbsPointBd = BitmapDescriptorFactory.fromView(dotView);

        setCurLocation(new LatLng(mDevice.getLat(), mDevice.getLng()));
    }

    private void initMapLocation() {
        LatLng ll = new LatLng(mDevice.getLat(), mDevice.getLng());
        mTencentMap.setOnMapLongClickListener(this);
        mTencentMap.setOnMapClickListener(this);
        mTencentMap.setOnMarkerClickListener(this);
        mTencentMap.setOnMapCameraChangeListener(this);
        mTencentMap.setInfoWindowAdapter(this);
        mMapView.getUiSettings().setScaleControlsEnabled(true);
        mMapView.getUiSettings().setLogoPosition(UiSettings.LOGO_POSITION_LEFT_BOTTOM);
        mMapView.getUiSettings().setScaleViewPosition(UiSettings.SCALEVIEW_POSITION_CENTER_BOTTOM);

        mTencentMap.animateCamera(CameraUpdateFactory.newLatLngZoom(ll, 16.0f));
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
        TrackPoint curPoint = mArrayListTrackPoints.get(mCurrentIndex - 1);
        BitmapDescriptor bd = MapIconManager.getInstance().getTmapHistoryIcon(curPoint);
        MarkerOptions options = new MarkerOptions()
            .position(latLng)
            .icon(bd)
            .draggable(false).rotation(curPoint.course)
            .anchor(0.5f, 0.5f);
        mCurLocationMarker = mTencentMap.addMarker(options);
    }

    @Override
    protected void addStayPoint(TrackPoint trackPoint) {
        if (trackPoint == null) {
            return;
        }
        Bundle extraInfo = new Bundle();
        extraInfo.putSerializable("TrackPoint", trackPoint);
        LatLng ll = new LatLng(trackPoint.lat, trackPoint.lng);
        MarkerOptions options = new MarkerOptions()
            .position(ll)
            .icon(BitmapDescriptorFactory.fromView(getStayPointView()))
            .draggable(false)
            .tag(extraInfo);
        mTencentMap.addMarker(options).set2Top();
    }

    private void drawPathOverlay(List<LatLng> points) {
        if (points != null && points.size() >= 2) {
            setCurLocation(points.get(points.size() - 1));
            if (mPathOverlay != null) {
                mPathOverlay.remove();
            }
            PolylineOptions options = new PolylineOptions().width(12).color(Color.argb(255, 0, 255, 0))
                .addAll(points).zIndex(9);
            mPathOverlay = mTencentMap.addPolyline(options);
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
            PolylineOptions options = new PolylineOptions().width(12).color(Color.argb(255, 0, 255, 0))
                .addAll(mPathPoints).zIndex(9);
            mPathOverlay = mTencentMap.addPolyline(options);
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
            mLastTrackPoint = null;
            mCount = 0;
            stayNum = 0;
            mTencentMap.clearAllOverlays();
            int tmpIndex = (int) ((progress + 0.0) / 100 * mArrayListTrackPoints.size());
            mCurrentIndex = tmpIndex > 0 ? tmpIndex : 1;

            if (mArrayListTrackPoints.size() > 0) {
                addStartPoint(mArrayListTrackPoints.get(0));
            }
            if (mCurrentIndex > 1) {
                TrackPoint point = null;
                for (int i = 0; i < (mCurrentIndex - 1); i++) {
                    point = mArrayListTrackPoints.get(i);
                    LatLng latLng = new LatLng(point.lat, point.lng);
                    if (mLastTrackPoint == null || !isShiftPoint(mLastTrackPoint, point)) {
                        if (!isFilterLbs || !isLbsPoint(point)) {
                            mPathPoints.add(latLng);
                            checkIsStayPoint(mLastTrackPoint, point);
                            if (isLbsPoint(point)) {
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
                drawPathOverlay(mPathPoints);
            }
        }
    }

    private boolean isOutOfMap(LatLng point) {
        LatLngBounds screenBounds = mMapView.getProjection().getVisibleRegion().getLatLngBounds();
        return !screenBounds.contains(point);
    }

    //起点
    @Override
    protected void addStartPoint(TrackPoint point) {
        LatLng ll = new LatLng(point.lat, point.lng);
        MarkerOptions options = new MarkerOptions()
            .position(ll)
            .icon(BitmapDescriptorFactory.fromResource(R.drawable.history_start_point));
        mTencentMap.addMarker(options);
    }

    //终点
    @Override
    protected void addEndPoint(TrackPoint point) {
        LatLng ll = new LatLng(point.lat, point.lng);
        MarkerOptions options = new MarkerOptions()
            .position(ll)
            .icon(BitmapDescriptorFactory.fromResource(R.drawable.history_end_point));
        mTencentMap.addMarker(options);
    }

    @Override
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
                        mCount++;
                        if (isLbsPoint(tPoint)) {
                            addLbsPoint(centerPoint);
                        }
                        addPathPoint(centerPoint);

                        checkIsStayPoint(mLastTrackPoint, tPoint);

                        if (isOutOfMap(centerPoint)) {
                            mTencentMap.animateTo(centerPoint);
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
            mTencentMap.addMarker(options);
        }
    }

    @Override
    protected void resetData() {
        mPathPoints.clear();
        if (mTencentMap != null) {
            mTencentMap.clearAllOverlays();
        }
        super.resetData();
    }

    @Override
    public void onResume() {
        if (mMapView != null) {
            mMapView.onResume();
        }

        super.onResume();
    }

    @Override
    public void onStart() {
        super.onStart();
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
        if (mTencentMap != null) {
            mTencentMap.clearAllOverlays();
        }
        if (mMapView != null) {
            mMapView.onDestroy();
        }
        super.onDestroy();
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
        Bundle extraInfo = (Bundle) marker.getTag();
        if (extraInfo != null) {
            TrackPoint trackPoint = (TrackPoint) extraInfo.getSerializable("TrackPoint");
            if (trackPoint != null) {
                curShowStayPoint = trackPoint;
                showStayPointInfo(trackPoint);
            }
        }
        return false;
    }

    @Override
    public void onMapClick(LatLng arg0) {
        hideInfoWindow();
        curShowStayPoint = null;
    }

    @Override
    public View getInfoWindow(Marker marker) {
        Bundle extra = (Bundle) marker.getTag();
        if (extra != null) {
            String type = extra.getString("type");
            if (type != null && type.equals(OverlayType.OVERLAY_STAYPOINT)) {
                return mPopupView;
            }
        }
        return null;
    }

    @Override
    public void onInfoWindowDettached(Marker arg0, View arg1) {
    }

    @Override
    public void onCameraChange(CameraPosition position) {
    }

    @Override
    public void onCameraChangeFinish(CameraPosition position) {
        mPreviousCameraPosition = position;
    }

    @Override
    protected double getDistance(TrackPoint center, TrackPoint radius) {
        if (center == null || radius == null) {
            return 0f;
        }
        return mMapView.getProjection().distanceBetween(new LatLng(center.lat, center.lng),
            new LatLng(radius.lat, radius.lng));
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
        MarkerOptions options = new MarkerOptions().position(ll).icon(markerNothing).draggable(false)
            .anchor(0.5f, 0.5f).tag(extra);
        mPopInfoWindow = mTencentMap.addMarker(options);
        mPopInfoWindow.showInfoWindow();
    }
}
