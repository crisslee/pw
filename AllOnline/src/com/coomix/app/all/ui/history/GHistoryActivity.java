package com.coomix.app.all.ui.history;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.MeasureSpec;

import com.coomix.app.all.R;
import com.coomix.app.all.model.bean.TrackPoint;
import com.coomix.app.all.markColection.baidu.MapUtils;
import com.coomix.app.framework.util.TimeUtil;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.InfoWindowAdapter;
import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
import com.google.android.gms.maps.GoogleMap.OnMapLongClickListener;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.VisibleRegion;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class GHistoryActivity extends HistoryParentActivity implements OnMapClickListener, OnMapLongClickListener,
        GoogleMap.OnMarkerClickListener,OnMapReadyCallback
{
    protected GoogleMap mMap;
    private MarkerOptions mMyMarkerOption;
    private Marker mMarkerMyLocation;
    private PolylineOptions mOptions;
    private Polyline mPolyLine;
    private List<LatLng> mListPoints = new ArrayList<LatLng>();
    private Set<Marker> mLbsMarkers = new HashSet<Marker>();
    private Marker clickedMarker;

    // Lbs点图片
    private BitmapDescriptor markerLbsPointBd;
    private SupportMapFragment googleMapFragment;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        findViewById(R.id.googleMapFragment).setVisibility(View.VISIBLE);

        googleMapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.googleMapFragment);
        googleMapFragment.getMapAsync(this);

        markerLbsPointBd = BitmapDescriptorFactory.fromResource(R.drawable.point_p);
    }

    @Override
    public void onMapReady(GoogleMap var1)
    {
        initMap(var1);
    }
    public static Bitmap convertViewToBitmap(View view)
    {
        view.measure(MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED), MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
        view.layout(0, 0, view.getMeasuredWidth(), view.getMeasuredHeight());
        view.buildDrawingCache();
        Bitmap bitmap = view.getDrawingCache();

        return bitmap;
    }

    protected void initMap(GoogleMap var1)
    {
        if (mMap == null)
        {
            mMap = var1;//((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.googleMapFragment)).getMap();
        }

        mMap.setMyLocationEnabled(true);
        mMap.setOnMapClickListener(this);

        mMap.getUiSettings().setMyLocationButtonEnabled(false);
        mMap.getUiSettings().setZoomControlsEnabled(false);
        mMap.setOnMarkerClickListener(this);
        mMap.setOnMapLongClickListener(this);

        mMap.getUiSettings().setTiltGesturesEnabled(true);
        mMap.setInfoWindowAdapter(new InfoWindowAdapter()
        {
            @Override
            public View getInfoWindow(Marker marker)
            {
                return mPopupView;
            }

            @Override
            public View getInfoContents(Marker marker)
            {
                return null;
            }
        });
    }

    @Override
    public boolean onMarkerClick(Marker marker)
    {
        if (!TextUtils.isEmpty(marker.getSnippet()))
        {
            int position = Integer.parseInt(marker.getSnippet());
            if (position >= 0 && position < mArrayListStayPoints.size())
            {
                clickedMarker = marker;
                TrackPoint trackPoint = mArrayListStayPoints.get(position);
                curShowStayPoint = trackPoint;
                showStayPointInfo(trackPoint);
                return true;
            }
        }

        return false;
    }

    private void initMarkerLines(TrackPoint tPoint)
    {
        if (tPoint == null)
        {
            return;
        }
        LatLng latLng = new LatLng(tPoint.lat, tPoint.lng);
        if (mMarkerMyLocation == null)
        {
            mMyMarkerOption = new MarkerOptions().position(latLng);
            mMarkerMyLocation = mMap.addMarker(mMyMarkerOption);
            mMarkerMyLocation.setAnchor(0.5f, 0.5f);
        }
        else
        {
            mMarkerMyLocation.setPosition(latLng);
        }

        int iResId = 0;
        if (tPoint.speed > 0)
        {
            if (tPoint.speed <= 80)
            {
                iResId = R.drawable.dev_green;
            }
            else if (tPoint.speed > 80 && tPoint.speed <= 120)
            {
                iResId = R.drawable.dev_yellow;
            }
            else if (tPoint.speed > 120)
            {
                iResId = R.drawable.dev_red;
            }
        }
        else
        {
            iResId = R.drawable.dev_blue;
        }
        mMarkerMyLocation.setIcon(BitmapDescriptorFactory.fromResource(iResId));
        mMarkerMyLocation.setRotation(tPoint.course);

        if (mPolyLine == null)
        {
            mOptions = new PolylineOptions();
            mOptions.color(Color.GREEN);
            mOptions.width(6);
            mPolyLine = mMap.addPolyline(mOptions);
        }
        if (mListPoints == null)
        {
            mListPoints = new ArrayList<LatLng>();
        }
        mListPoints.add(latLng);
        mPolyLine.setPoints(mListPoints);
    }

    @Override
    protected void progressChange(int progress)
    {
        synchronized (mLocker)
        {
            if (mListPoints == null)
            {
                mListPoints = new ArrayList<LatLng>();
            }
            mTotalDistance = 0;
            mTotalStayTime = 0;
            mCount = 0;
            stayNum = 0;
            mMap.clear();
            mMarkerMyLocation = null;
            mPolyLine = null;
            mArrayListStayPoints.clear();
            mLbsMarkers.clear();
            mLastTrackPoint = null;
            mListPoints.clear();
            if (mArrayListTrackPoints.size() > 0)
            {
                addStartPoint(mArrayListTrackPoints.get(0));
            }

            int tmpIndex = (int) ((progress + 0.0) / 100 * mArrayListTrackPoints.size());
            mCurrentIndex = tmpIndex > 0 ? tmpIndex : 1;
            if (mCurrentIndex > 1)
            {
                for (int i = 0; i < (mCurrentIndex - 1); i++)
                {
                    TrackPoint point = mArrayListTrackPoints.get(i);
                    if (mLastTrackPoint == null || !isShiftPoint(mLastTrackPoint, point))
                    {
                        if (!isFilterLbs || !isLbsPoint(point))
                        {
                            mListPoints.add(new LatLng(point.lat, point.lng));
                            checkIsStayPoint(mLastTrackPoint, point);
                            if (isLbsPoint(point))
                            {
                                addLbsPoint(new LatLng(point.lat, point.lng));
                            }
                        }
                        addStayTime(mLastTrackPoint, point);
                        mLastTrackPoint = point;
                    }
                }
                if (mListPoints != null && mListPoints.size() >= 2)
                {
                    initMarkerLines(mLastTrackPoint);
                }
            }

            if (mCurrentIndex > 0 && mCurrentIndex == mArrayListTrackPoints.size())
            {
                addEndPoint(mArrayListTrackPoints.get(mArrayListTrackPoints.size() - 1));
            }
        }
    }

    private boolean isOutOfGoogleMap(double lat, double lng)
    {
        VisibleRegion vr = mMap.getProjection().getVisibleRegion();
        double left = vr.latLngBounds.southwest.longitude;
        double top = vr.latLngBounds.northeast.latitude;
        double right = vr.latLngBounds.northeast.longitude;
        double bottom = vr.latLngBounds.southwest.latitude;
        if (lng < left || lng > right || lat > top || lat < bottom)
        {
            return true;
        }
        return false;
    }

    @Override
    protected void updateView(int index)
    {
        synchronized (mLocker)
        {
            if (mMap != null && mArrayListTrackPoints != null && mArrayListTrackPoints.size() > 0 && index <= mArrayListTrackPoints.size())
            {
                TrackPoint tPoint = mArrayListTrackPoints.get(index - 1);
                int seekBarIndex = (int) ((index + 0.0) / mArrayListTrackPoints.size() * 100);
                mSeekBar.setProgress(seekBarIndex);
                if (index == 1)
                {
                    addStartPoint(tPoint);
                }
                if (mLastTrackPoint == null || !isShiftPoint(mLastTrackPoint, tPoint))
                {
                    if (!isFilterLbs || !isLbsPoint(tPoint))
                    {
                        String time = TimeUtil.long2TimeString(tPoint.gps_time * 1000);
                        //mTextViewTime.setText(time);
                        textDateTime.setText(TimeUtil.long2DateTimeString(tPoint.gps_time * 1000));
                        textSpeed.setText(getHistorySpeed(tPoint.speed));
                        //mTextViewDir.setText(getString(R.string.label_dir) + " " + CommonUtil.getDir(this, tPoint.course));
                        if (isLbsPoint(tPoint))
                        {
                            addLbsPoint(new LatLng(tPoint.lat, tPoint.lng));
                        }

                        initMarkerLines(tPoint);
                        checkIsStayPoint(mLastTrackPoint, tPoint);

                        if (isOutOfGoogleMap(tPoint.lat, tPoint.lng))
                        {
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(tPoint.lat, tPoint.lng), 15.0f));
                        }
                    }
                    addStayTime(mLastTrackPoint, tPoint);
                    mLastTrackPoint = tPoint;
                }
                if (mArrayListTrackPoints != null)
                {
                    int size = mArrayListTrackPoints.size();
                    if (size > 0)
                    {
                        TrackPoint lastTPoint = mArrayListTrackPoints.get(size - 1);
                        if (lastTPoint.gps_time < respEndTime
                            && index >= size / 2 && size < 10000 && LAST_REQUEST_mTraceEndTime < respEndTime)
                        {
                            // 当前已获取数据的最后一个点的时间小于回放的最后一个点的时间 && 当前播放的点超过一半 && 当前少于10000个点 && 不是正在请求数据
                            requestHistoryData(lastTPoint.gps_time);
                            LAST_REQUEST_mTraceEndTime = respEndTime;
                        }
                    }
                    if (index == size)
                    {
                        // 每次最多播放10000个点, 超过10000个点清除当前播放的点与路径再继续播放
                        if (size < 10000)
                        {
                            displayStatistic(mArrayListTrackPoints.get(size - 1));
                        }
                        else
                        {
                            final AlertDialog dlgsDate = new AlertDialog.Builder(GHistoryActivity.this).setInverseBackgroundForced(true).setTitle(R.string.histroy).setMessage(R.string.histroy_tip).setPositiveButton(R.string.ok, new DialogInterface.OnClickListener()
                            {

                                @Override
                                public void onClick(DialogInterface dialog, int which)
                                {
                                    TrackPoint lastTPoint = mArrayListTrackPoints.get(mArrayListTrackPoints.size() - 1);
                                    if (lastTPoint != null)
                                    {
                                        lastPointTimeLong = lastTPoint.gps_time;
                                    }

                                    initNewHistory();
                                }

                            }).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener()
                            {
                                @Override
                                public void onClick(DialogInterface dialog, int which)
                                {
                                    displayStatistic(mArrayListTrackPoints.get(mArrayListTrackPoints.size() - 1));
                                }

                            }).create();
                            dlgsDate.show();
                        }
                    }
                }
            }
        }
    }

    @Override
    public boolean isLbsPoint(TrackPoint point)
    {
        if (point != null && point.speed == -1)
        {
            return true;
        }
        return false;
    }

    private void addLbsPoint(LatLng ll)
    {
        if (ll != null)
        {
            MarkerOptions options = new MarkerOptions().position(ll).icon(markerLbsPointBd).draggable(false).anchor(0.5f, 0.5f);
            Marker lbsMarker = mMap.addMarker(options);
            mLbsMarkers.add(lbsMarker);
        }
    }

    private void gMapAnimateTo(double lat, double lng)
    {
        CameraPosition cameraPosition = new CameraPosition.Builder().target(new LatLng(lat, lng)).zoom(12).build();
        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
    }

    @Override
    protected void resetData()
    {
        mLbsMarkers.clear();
        mListPoints.clear();
        mMap.clear();
        super.resetData();
    }

    @Override
    public void onResume()
    {
        // initilizeMap();
        super.onResume();
    }

    @Override
    public void onStart()
    {
        // TODO Auto-generated method stub
        super.onStart();
    }

    @Override
    public void onPause()
    {
        super.onPause();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState)
    {
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState)
    {
        super.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    public void onDestroy()
    {
        if (mMap != null)
        {
            mMap.clear();
        }
        super.onDestroy();
    }

    @Override
    public void onMapClick(LatLng arg0)
    {
        if (clickedMarker != null)
        {
            clickedMarker.hideInfoWindow();
        }
        curShowStayPoint = null;
    }

    @Override
    public void onMapLongClick(LatLng arg0)
    {
        // TODO Auto-generated method stub

    }

    @Override
    protected void showInfoWindow(TrackPoint trackPoint, Bundle extra)
    {
        if (trackPoint == null || extra == null)
        {
            return;
        }
        if (clickedMarker != null)
        {
            clickedMarker.showInfoWindow();
        }
    }

    @Override
    protected double getDistance(TrackPoint center, TrackPoint radius)
    {
        float[] result = new float[1];
        Location.distanceBetween(center.lat, center.lng, radius.lat, radius.lng, result);
        return result[0];
    }

    @Override
    protected void addStayPoint(TrackPoint trackPoint)
    {
        if (trackPoint == null)
        {
            return;
        }
        LatLng ll = new LatLng(trackPoint.lat, trackPoint.lng);
        Bitmap bmp = MapUtils.convertViewToBitmap(getStayPointView());
        MarkerOptions options = new MarkerOptions().position(ll).icon(BitmapDescriptorFactory.fromBitmap(bmp)).draggable(false);
        Marker marker = mMap.addMarker(options);
        //用于在marker点击的时候获取停留点的信息
        marker.setSnippet(String.valueOf(mArrayListStayPoints.size() - 1));
    }

    @Override
    protected void addStartPoint(TrackPoint tPoint)
    {
        if (mMap == null)
        {
            return;
        }
        LatLng ll = new LatLng(tPoint.lat, tPoint.lng);
        MarkerOptions options = new MarkerOptions().position(ll).title("").icon(BitmapDescriptorFactory.fromResource(R.drawable.history_start_point));
        mMap.addMarker(options);
    }

    @Override
    protected void addEndPoint(TrackPoint tPoint)
    {
        if (mMap == null)
        {
            return;
        }
        LatLng ll = new LatLng(tPoint.lat, tPoint.lng);
        MarkerOptions options = new MarkerOptions().position(ll).title("").icon(BitmapDescriptorFactory.fromResource(R.drawable.history_end_point));
        mMap.addMarker(options);
    }
}
