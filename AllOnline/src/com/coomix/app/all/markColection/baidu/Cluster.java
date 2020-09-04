/*
 * Main algorithm
 * www.goome.net
 * baidu map cluster
 */

package com.coomix.app.all.markColection.baidu;

import android.content.Context;
import android.graphics.Point;

import com.amap.api.maps.AMap;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Projection;
import com.coomix.app.all.model.bean.DeviceInfo;
import com.google.android.gms.maps.GoogleMap;

import java.util.ArrayList;
import java.util.List;

public class Cluster
{

    // private MarkerClusterActivity context;
    private MapView mMapView;
    private GoogleMap googleMap;
    private AMap amMap;
    private com.amap.api.maps.MapView mapView_amap;

    private com.tencent.tencentmap.mapsdk.map.MapView mapView_tmap;

    private int mGridSize; // use to set the interval of the screen axis
    private List<ClusterDevice> mDevices;

    ///////////////////////////////////////////////////////////////// --------------------------------------
    public Cluster(Context context, MapView mapView, int mGridSize)
    {
        this.mMapView = mapView;
        this.mGridSize = mGridSize;
        mDevices = new ArrayList<ClusterDevice>();
    }

    public Cluster(Context context, GoogleMap mMap, int mGridSize, com.google.android.gms.maps.MapView mapView)
    {
        this.googleMap = mMap;
        this.mGridSize = mGridSize;
        mDevices = new ArrayList<ClusterDevice>();
    }

    public Cluster(Context context, com.tencent.tencentmap.mapsdk.map.MapView mMap, int mGridSize)
    {
        this.mapView_tmap = mMap;
        this.mGridSize = mGridSize;
        mDevices = new ArrayList<ClusterDevice>();
    }

    public Cluster(Context context, GoogleMap mMap, int mGridSize)
    {
        this.googleMap = mMap;
        this.mGridSize = mGridSize;
        mDevices = new ArrayList<ClusterDevice>();
    }

    public Cluster(Context context, AMap amMap, int mGridSize, com.amap.api.maps.MapView mapView)
    {
        this.amMap = amMap;
        this.mapView_amap = mapView;
        this.mGridSize = mGridSize;
        mDevices = new ArrayList<ClusterDevice>();
    }

    // create Baidu cluster
    public ArrayList<ClusterDevice> createBaiduCluster(List<DeviceInfo> markerList)
    {
        this.mDevices.clear();
        ArrayList<ClusterDevice> itemList = new ArrayList<ClusterDevice>();

        // use n,m to indicate the max coordinate of the x,y
        int n = mMapView.getWidth() / mGridSize;
        int m = mMapView.getHeight() / mGridSize;

        ClusterDevice clusterMarker;
        // 璧嬪垵鍊�
        for (int i = 0; i < n * m; i++)
        {
            clusterMarker = null;
            mDevices.add(clusterMarker);
        }

        DeviceInfo marker;

        for (int i = 0; i < markerList.size(); i++)
        {
            marker = markerList.get(i);
            if (marker != null && marker.getState() != DeviceInfo.STATE_DISABLE && marker.getState() != DeviceInfo.STATE_EXPIRE)
            {
                Projection projectionMark = mMapView.getMap().getProjection();
                if (projectionMark != null)
                {
                    Point pixelMark = projectionMark.toScreenLocation(new com.baidu.mapapi.model.LatLng(marker.getLat(), marker.getLng()));

                    // use x,y to indicate the coordinate of the marker
                    int x = pixelMark.x / mGridSize + (((pixelMark.x % mGridSize) > 0) ? 1 : 0);
                    int y = pixelMark.y / mGridSize + (((pixelMark.y % mGridSize) > 0) ? 1 : 0);

                    // put the marker into the correspond location of the
                    // mMyMarkers
                    if (x >= 0 && x <= n && y >= 0 && y <= m)
                    {
                        int j = x + y * n;
                        if(j >= n * m)
                        {
                            continue;
                        }
                        clusterMarker = mDevices.get(j);
                        if (clusterMarker == null)
                        {
                            clusterMarker = new ClusterDevice();
                            clusterMarker.AddMarker(marker);
                            mDevices.set(j, clusterMarker);
                        }
                        else
                        {
                            clusterMarker.AddMarker(marker);
                        }
                    }
                }
            }
        }

        for (int i = 0; i < mDevices.size(); i++)
        {
            ClusterDevice cd = mDevices.get(i);
            if (cd != null)
            {
                itemList.add(cd);
            }
        }

        return itemList;
    }

    // create TMAP cluster
    public ArrayList<ClusterDevice> createTMAPCluster(List<DeviceInfo> markerList)
    {
        this.mDevices.clear();
        ArrayList<ClusterDevice> itemList = new ArrayList<ClusterDevice>();
        // use n,m to indicate the max coordinate of the x,y
        int n = mapView_tmap.getWidth() / mGridSize;
        int m = mapView_tmap.getHeight() / mGridSize;

        ClusterDevice clusterMarker;
        // 璧嬪垵鍊�
        for (int i = 0; i < n * m; i++)
        {
            clusterMarker = null;
            mDevices.add(clusterMarker);
        }

        DeviceInfo marker;

        for (int i = 0; i < markerList.size(); i++)
        {
            marker = markerList.get(i);
            if (marker != null && marker.getState() != DeviceInfo.STATE_DISABLE && marker.getState() != DeviceInfo.STATE_EXPIRE)
            {
                Point pixelMark = mapView_tmap.getProjection().toScreenLocation(new com.tencent.mapsdk.raster.model.LatLng(marker.getLat(), marker.getLng()));

                // use x,y to indicate the coordinate of the marker
                int x = pixelMark.x / mGridSize + (((pixelMark.x % mGridSize) > 0) ? 1 : 0);
                int y = pixelMark.y / mGridSize + (((pixelMark.y % mGridSize) > 0) ? 1 : 0);

                // put the marker into the correspond location of the mMyMarkers
                if (x >= 0 && x <= n && y >= 0 && y <= m)
                {
                    int j = x + y * n;
                    if(j >= n * m)
                    {
                        continue;
                    }
                    clusterMarker = mDevices.get(j);
                    if (clusterMarker == null)
                    {
                        clusterMarker = new ClusterDevice();
                        clusterMarker.AddMarker(marker);
                        mDevices.set(j, clusterMarker);
                    }
                    else
                    {
                        clusterMarker.AddMarker(marker);
                    }
                }
            }
        }

        for (int i = 0; i < mDevices.size(); i++)
        {
            ClusterDevice cd = mDevices.get(i);
            if (cd != null)
            {
                itemList.add(cd);
            }
        }

        return itemList;
    }

    // create AMAP cluster
    public ArrayList<ClusterDevice> createAMAPCluster(List<DeviceInfo> markerList)
    {
        this.mDevices.clear();
        ArrayList<ClusterDevice> itemList = new ArrayList<ClusterDevice>();
        // use n,m to indicate the max coordinate of the x,y
        int n = mapView_amap.getWidth() / mGridSize;
        int m = mapView_amap.getHeight() / mGridSize;

        ClusterDevice clusterMarker;
        // 璧嬪垵鍊�
        for (int i = 0; i < n * m; i++)
        {
            clusterMarker = null;
            mDevices.add(clusterMarker);
        }

        DeviceInfo deviceStatae;

        for (int i = 0; i < markerList.size(); i++)
        {
            deviceStatae = markerList.get(i);
            if (deviceStatae != null && deviceStatae.getState() != DeviceInfo.STATE_DISABLE && deviceStatae.getState() != DeviceInfo.STATE_EXPIRE)
            {
                com.amap.api.maps.model.LatLng markGeo = new com.amap.api.maps.model.LatLng(deviceStatae.getLat(), deviceStatae.getLng());
                Point pixelMark = new Point();
                pixelMark = amMap.getProjection().toScreenLocation(markGeo);

                // use x,y to indicate the coordinate of the marker
                int x = pixelMark.x / mGridSize + (((pixelMark.x % mGridSize) > 0) ? 1 : 0);
                int y = pixelMark.y / mGridSize + (((pixelMark.y % mGridSize) > 0) ? 1 : 0);

                // put the marker into the correspond location of the mMyMarkers
                if (x >= 0 && x <= n && y >= 0 && y <= m)
                {
                    int j = x + y * n;
                    if(j >= n * m)
                    {
                        continue;
                    }
                    clusterMarker = mDevices.get(j);
                    if (clusterMarker == null)
                    {
                        clusterMarker = new ClusterDevice();
                        clusterMarker.AddMarker(deviceStatae);
                        mDevices.set(j, clusterMarker);
                    }
                    else
                    {
                        clusterMarker.AddMarker(deviceStatae);
                    }
                }
            }
        }

        for (int i = 0; i < mDevices.size(); i++)
        {
            ClusterDevice cd = mDevices.get(i);
            if (cd != null)
            {
                itemList.add(cd);
            }
        }

        return itemList;
    }

    // create Google cluster
    public ArrayList<ClusterDevice> createGoogleCluster(List<DeviceInfo> markerList, int width, int height)
    {
        this.mDevices.clear();
        ArrayList<ClusterDevice> itemList = new ArrayList<ClusterDevice>();

        // use n,m to indicate the max coordinate of the x,y
        int n = width / mGridSize;
        int m = height / mGridSize;

        ClusterDevice clusterMarker;
        // 璧嬪垵鍊�
        for (int i = 0; i < n * m; i++)
        {
            clusterMarker = null;
            mDevices.add(clusterMarker);
        }

        DeviceInfo marker;

        for (int i = 0; i < markerList.size(); i++)
        {
            marker = markerList.get(i);
            if (marker != null && marker.getState() != DeviceInfo.STATE_DISABLE && marker.getState() != DeviceInfo.STATE_EXPIRE)
            {
                com.google.android.gms.maps.model.LatLng markGeo = new com.google.android.gms.maps.model.LatLng(marker.getLat(), marker.getLng());

                Point pixelMark = new Point();
                pixelMark = googleMap.getProjection().toScreenLocation(markGeo);

                // use x,y to indicate the coordinate of the marker
                int x = pixelMark.x / mGridSize + (((pixelMark.x % mGridSize) > 0) ? 1 : 0);
                int y = pixelMark.y / mGridSize + (((pixelMark.y % mGridSize) > 0) ? 1 : 0);

                // put the marker into the correspond location of the mMyMarkers
                if (x >= 0 && x <= n && y >= 0 && y <= m)
                {
                    int j = x + y * n;
                    if(j >= n * m)
                    {
                        continue;
                    }
                    clusterMarker = mDevices.get(j);
                    if (clusterMarker == null)
                    {
                        clusterMarker = new ClusterDevice();
                        clusterMarker.AddMarker(marker);
                        mDevices.set(j, clusterMarker);
                    }
                    else
                    {
                        clusterMarker.AddMarker(marker);
                    }
                }
            }
        }

        for (int i = 0; i < mDevices.size(); i++)
        {
            ClusterDevice cd = mDevices.get(i);
            if (cd != null)
            {
                itemList.add(cd);
            }
        }

        return itemList;
    }
}
