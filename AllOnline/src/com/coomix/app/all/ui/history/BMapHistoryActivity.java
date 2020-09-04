package com.coomix.app.all.ui.history;

import android.graphics.Color;
import android.os.Bundle;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BaiduMap.OnMapClickListener;
import com.baidu.mapapi.map.BaiduMap.OnMapLongClickListener;
import com.baidu.mapapi.map.BaiduMap.OnMapStatusChangeListener;
import com.baidu.mapapi.map.BaiduMap.OnMarkerClickListener;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.DotOptions;
import com.baidu.mapapi.map.InfoWindow;
import com.baidu.mapapi.map.MapPoi;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.map.Polyline;
import com.baidu.mapapi.map.PolylineOptions;
import com.baidu.mapapi.map.UiSettings;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.model.LatLngBounds;
import com.baidu.mapapi.utils.DistanceUtil;
import com.coomix.app.all.R;
import com.coomix.app.all.manager.MapIconManager;
import com.coomix.app.all.model.bean.TrackPoint;
import com.coomix.app.framework.util.CommonUtil;
import com.coomix.app.framework.util.TimeUtil;
import java.util.ArrayList;
import java.util.List;

public class BMapHistoryActivity extends HistoryParentActivity implements OnMapLongClickListener, OnMarkerClickListener,
    OnMapClickListener, OnMapStatusChangeListener {
    private Polyline mPathOverlay;
    private ArrayList<LatLng> mPathPoints = new ArrayList<LatLng>();
    private BitmapDescriptor curLocationBd;
    private Marker mCurLocationMarker;
    private BitmapDescriptor mStayPointBd;
    private InfoWindow mStayPopup;
    private MapView mMapView = null;
    private BaiduMap mBaiduMap = null;
    private MapStatus mPreviousCameraPosition;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mMapView = new MapView(BMapHistoryActivity.this);
        layoutMapView.addView(mMapView);
        mBaiduMap = mMapView.getMap();

        initMapLocation();

        setCurLocation(new LatLng(mDevice.getLat(), mDevice.getLng()));
    }

    private void initMapLocation() {
        LatLng ll = new LatLng(mDevice.getLat(), mDevice.getLng());
        mMapView.showScaleControl(true);
        mMapView.showZoomControls(false);
        MapStatus ms = new MapStatus.Builder().zoom(17.0f).target(ll).build();
        mBaiduMap.animateMapStatus(MapStatusUpdateFactory.newMapStatus(ms));
        mBaiduMap.setOnMarkerClickListener(this);
        mBaiduMap.setOnMapClickListener(this);
        mBaiduMap.setOnMapLongClickListener(this);
        mBaiduMap.setOnMapStatusChangeListener(this);

        UiSettings uiSettings = mBaiduMap.getUiSettings();
        uiSettings.setOverlookingGesturesEnabled(false);
        uiSettings.setRotateGesturesEnabled(false);
        uiSettings.setCompassEnabled(false);
    }

    private void setCurLocation(LatLng latLng) {
        if (mCurLocationMarker != null) {
            mCurLocationMarker.remove();
        }

        if (mArrayListTrackPoints.size() == 0) {
            return;
        }

        if (mCurrentIndex > mArrayListTrackPoints.size()) {
            mCurrentIndex = mArrayListTrackPoints.size();
        }

        TrackPoint curPoint = mArrayListTrackPoints.get(mCurrentIndex - 1);
        if(curPoint == null){
            return;
        }
        BitmapDescriptor bd = MapIconManager.getInstance().getBaiduHistoryIcon(curPoint);
        MarkerOptions options = new MarkerOptions()
            .position(latLng)
            .icon(bd)
            .zIndex(9)
            .draggable(false)
            .rotate(-1 * curPoint.course)
            .anchor(0.5f, 0.5f); // 百度地图(3.6.0)角度逆时针，自己的数据角度顺时针
        mCurLocationMarker = (Marker) mBaiduMap.addOverlay(options);
    }

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
            .zIndex(10)
            .draggable(false)
            .extraInfo(extraInfo);
        mBaiduMap.addOverlay(options);
    }

    private void drawPathOverlay(List<LatLng> points) {
        if (points != null && points.size() >= 2) {
            setCurLocation(points.get(points.size() - 1));
            if (mPathOverlay != null) {
                mPathOverlay.remove();
            }
            OverlayOptions options = new PolylineOptions().width(9).color(Color.argb(255, 0, 255, 0))
                .points(points);
            mPathOverlay = (Polyline) mBaiduMap.addOverlay(options);
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
            OverlayOptions options = new PolylineOptions().width(9).color(Color.argb(255, 0, 255, 0))
                .points(mPathPoints);
            mPathOverlay = (Polyline) mBaiduMap.addOverlay(options);
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
            mBaiduMap.clear();
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
        if (mBaiduMap == null || mBaiduMap.getMapStatus() == null) {
            return false;
        }
        LatLngBounds screenBounds = mBaiduMap.getMapStatus().bound;
        return !screenBounds.contains(point);
    }

    //起点
    protected void addStartPoint(TrackPoint point) {
        LatLng ll = new LatLng(point.lat, point.lng);
        MarkerOptions options = new MarkerOptions()
            .position(ll)
            .icon(BitmapDescriptorFactory.fromResource(R.drawable.history_start_point));
        mBaiduMap.addOverlay(options);
    }

    //终点
    protected void addEndPoint(TrackPoint point) {
        LatLng ll = new LatLng(point.lat, point.lng);
        MarkerOptions options = new MarkerOptions()
            .position(ll)
            .icon(BitmapDescriptorFactory.fromResource(R.drawable.history_end_point));
        mBaiduMap.addOverlay(options);
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
                        addPathPoint(centerPoint);
                        mCount++;
                        checkIsStayPoint(mLastTrackPoint, tPoint);
                        if (isLbsPoint(tPoint)) {
                            addLbsPoint(centerPoint);
                        }

                        if (isOutOfMap(centerPoint)) {
                            mBaiduMap.animateMapStatus(MapStatusUpdateFactory.newLatLng(centerPoint));
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

    protected void addLbsPoint(LatLng ll) {
        if (ll != null) {
            OverlayOptions options = new DotOptions().center(ll).color(Color.argb(255, 128, 0, 128))
                .radius(10).zIndex(10);
            mBaiduMap.addOverlay(options);
        }
    }

    @Override
    protected void resetData() {
        mPathPoints.clear();
        if (mBaiduMap != null) {
            mBaiduMap.clear();
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
        synchronized (mLocker) {
            mLocker.notify();
        }

        if (mBaiduMap != null) {
            mBaiduMap.clear();
            mBaiduMap.setMyLocationEnabled(false);
        }

        if (curLocationBd != null) {
            curLocationBd.recycle();
            curLocationBd = null;
        }

        if (mStayPointBd != null) {
            mStayPointBd.recycle();
            mStayPointBd = null;
        }

        if (mMapView != null) {
            mMapView.onDestroy();
        }

        super.onDestroy();
    }

    @Override
    public void onMapLongClick(LatLng arg0) {
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        Bundle extraInfo = marker.getExtraInfo();
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
        mBaiduMap.hideInfoWindow();
        curShowStayPoint = null;
    }

    @Override
    public void onMapPoiClick(MapPoi arg0) {
    }

    @Override
    public void onMapStatusChange(MapStatus arg0) {
    }

    @Override
    public void onMapStatusChangeFinish(MapStatus arg0) {
        mPreviousCameraPosition = mBaiduMap.getMapStatus();
    }

    @Override
    public void onMapStatusChangeStart(MapStatus arg0) {
    }

    @Override
    protected void showInfoWindow(TrackPoint trackPoint, Bundle extra) {
        LatLng ll = new LatLng(trackPoint.lat, trackPoint.lng);
        mStayPopup = new InfoWindow(mPopupView, ll, -70);
        mBaiduMap.showInfoWindow(mStayPopup);
    }

    @Override
    protected double getDistance(TrackPoint center, TrackPoint radius) {
        if (center == null || radius == null) {
            return 0f;
        }
        return DistanceUtil.getDistance(new LatLng(center.lat, center.lng), new LatLng(radius.lat, radius.lng));
    }

    @Override
    public void onMapStatusChangeStart(MapStatus mapStatus, int i) {

    }
}
