package com.coomix.app.all.ui.main;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;

import com.coomix.app.all.Constant;
import com.coomix.app.all.R;
import com.coomix.app.all.map.baidu.ClusterManager;
import com.coomix.app.all.markColection.baidu.Cluster;
import com.coomix.app.all.markColection.baidu.ClusterDevice;
import com.coomix.app.all.model.bean.DeviceInfo;
import com.coomix.app.all.model.bean.Fence;
import com.coomix.app.all.service.MyOrientationListener;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMapLongClickListener;
import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
import com.google.android.gms.maps.LocationSource;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class GMainActivity extends MainActivityParent implements OnMapClickListener, OnMapLongClickListener, GoogleMap.OnMarkerClickListener, OnMapReadyCallback {

    private FusedLocationProviderClient gFusedLocationClient;
    private ClusterTask mClusterTask = new ClusterTask();
    protected MapView gMapView = null;
    protected GoogleMap mGoogleMap = null;
    private final ReadWriteLock mClusterTaskLock = new ReentrantReadWriteLock();

    private ArrayList<Marker> listMakers = new ArrayList<Marker>();
    private Marker currentMarker;

    private MyOrientationListener myOrientationListener;
    private int mXDirection;

    private LocationManager locationManager;
    private LocationRequest locationRequest;

    private Location myLocData = null;
    private Marker mMyLocationMarker = null;
    private Circle circle;
    private LocationListener listener;

    @SuppressWarnings("unchecked")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        gMapView = (MapView) findViewById(R.id.map_view);
        initOritationListener();

    }

    public void initOritationListener() {
        myOrientationListener = new MyOrientationListener(getApplicationContext());
        myOrientationListener.setOnOrientationListener(new MyOrientationListener.OnOrientationListener() {
            @Override
            public void onOrientationChanged(float x) {
                mXDirection = (int) x - 45;
            }
        });
    }

    private void addClusters() {
        if (listDevices == null) {
            return;
        }
        mClusterTaskLock.writeLock().lock();
        try {

        } finally {
            mClusterTaskLock.writeLock().unlock();
        }
    }

    @Override
    public void onMapClick(LatLng arg0) {
        if(currDevice != null && mHandler != null) {
            mHandler.removeMessages(REFRESH_SINGLE);
            mHandler.removeMessages(OPEN_TEMP_CMD);
            if(needSpecialCmd(currDevice)) {
                closeTempCmd(currDevice);
            }
            currDevice = null;
            lastSelectedDevice = null;
        }
        hideBottomCarInfo();
    }

    @Override
    public void onMapLongClick(LatLng latLng) {

    }

    @Override
    public boolean onMarkerClick(Marker clickMarker) {
        if(currentMarker != null) {
            currentMarker.hideInfoWindow();
        }
        currentMarker = clickMarker;
        Object obj = clickMarker.getTag();
        if (obj != null && obj instanceof DeviceInfo) {
            DeviceInfo device = (DeviceInfo) obj;
            iCurrentShowDeviceIndex = deviceManager.getValidDevIndex(device);
            deviceMarkerTap(device, true);
        }else if(obj != null && obj instanceof ClusterDevice) {
            ClusterDevice clusterDevice = (ClusterDevice) obj;
            deviceClusterTap(clickMarker.getPosition(), clusterDevice);
        }
        return true;
    }

    private void deviceClusterTap(LatLng latLng, ClusterDevice clusterDevice) {
        if(mGoogleMap != null) {
            zoomLevel = mGoogleMap.getMaxZoomLevel();
        }
        mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng,zoomLevel));
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mGoogleMap = googleMap;
        addDevicesOnMap();
    }

    private class ClusterTask extends AsyncTask<Void, Void, ArrayList<ClusterDevice>> {
        @Override
        protected ArrayList<ClusterDevice> doInBackground(Void... params) {
            if (listDevices == null) {
                return null;
            }
            Cluster cluster = new Cluster(GMainActivity.this, mGoogleMap, mGridSize);
            return cluster.createGoogleCluster(listDevices, gMapView.getWidth(), gMapView.getHeight());
        }

        @Override
        protected void onPostExecute(ArrayList<ClusterDevice> result) {
            renderingClusters(result);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if(gMapView != null){
            gMapView.onResume();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if(gMapView != null){
            gMapView.onPause();
        }
    }

    @Override
    public void onStart() {
        myOrientationListener.start();
        super.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
        myOrientationListener.stop();
    }

    @Override
    public void onDestroy() {
        if (gMapView != null) {
            mGoogleMap.clear();
            gMapView.onDestroy();
        }
        if (mClusterTask != null) {
            mClusterTask.cancel(true);
        }
        //clearMarker();
        super.onDestroy();
    }

    private void addMyLocation() {
        if (myLocData != null) {
            if (mMyLocationMarker != null) {
                mMyLocationMarker.remove();
                mMyLocationMarker = null;
            }
            LatLng ll = new LatLng(myLocData.getLatitude(), myLocData.getLongitude());
            MarkerOptions options = new MarkerOptions().position(ll).icon(BitmapDescriptorFactory.fromResource(R.drawable.locate_current_icon)).draggable(false).anchor(0.5f, 0.5f).rotation((float) mXDirection);
            mMyLocationMarker = mGoogleMap.addMarker(options);
        }
    }

    @Override
    protected void startLocation() {
        if (locationManager != null && locationRequest != null) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 5, listener);
        }
    }

    private void stopLocation() {
        if(locationManager != null) {
            locationManager.removeUpdates(listener);
        }
    }

    @Override
    protected void changeMapMode(int iType) {

    }

    @Override
    protected void addDevicesOnMap() {
        if (listMakers == null){
            listMakers = new ArrayList<Marker>();
        }else{
            //clearMarker();
        }

        if(listDevices != null && listDevices.size() > 0){
            if(listDevices.size() < Constant.LIMITCARNUM) {
                addMarkers();
            }else{
                addClusters();
            }
        }
    }

    @Override
    protected void renderingClusters(ArrayList<ClusterDevice> clusterDatas) {

    }

    @Override
    protected void animateCamera(double lat, double lng, float zoomLevel) {

    }

    @Override
    protected void animateCameraInclue(ArrayList<DeviceInfo> listDevices) {

    }

    @Override
    protected void addMarker(DeviceInfo device) {

    }

    @Override
    protected void initLocation() {

    }

    @Override
    protected void addFenceOnMap(Fence fence, boolean bSetZoom) {

    }

    @Override
    protected void removeFenceOnMap() {

    }

    @Override
    protected void showDevTitle() {

    }

    @Override
    protected void hideDevTitle() {

    }
}
