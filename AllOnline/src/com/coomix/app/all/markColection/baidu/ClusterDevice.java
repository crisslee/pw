package com.coomix.app.all.markColection.baidu;

import com.coomix.app.all.Constant;
import com.coomix.app.all.model.bean.DeviceInfo;
import com.google.android.gms.maps.model.LatLng;

import java.io.Serializable;
import java.util.ArrayList;

public class ClusterDevice implements Serializable {
    /**
     * 
     */
    private static final long serialVersionUID = -17931524989927293L;
    private ArrayList<DeviceInfo> mMarkers;
    private double mTotalLat = 0, mTotalLng = 0;

    public ClusterDevice() {
        mMarkers = new ArrayList<DeviceInfo>();
    }

    /**
     * 
     * ClusterMarker 涓坊鍔爉arker
     * 
     * @param marker
     * @param isAverageCenter
     */
    /*
     * public void AddMarker(DeviceState marker,Boolean isAverageCenter){
     * mMarkers.add(marker);
     * 
     * if(!isAverageCenter){
     * 
     * if(mCenter==null) this.mCenter = new GeoPoint((int)(mMarkers.get(0).lat *
     * 1e6), (int)(mMarkers.get(0).lng * 1e6)); }else{ this.mCenter =
     * calAverageCenter(); } }
     */

    /**
     * 
     * ClusterMarker 涓坊鍔爉arker
     * 
     * @time 2014-10-27
     * @param marker
     * @param
     */
    public void AddMarker(DeviceInfo marker) {

        if (mMarkers.size() == 0) {
            mTotalLat = 0;
            mTotalLng = 0;
        }

        mTotalLat += marker.getLat();
        mTotalLng += marker.getLng();

        mMarkers.add(marker);
    }

    // //////////////////////////////////////////////////////////////
    // calculate the average point
    private com.baidu.mapapi.model.LatLng calBaiduAverageCenter() {

        int len = mMarkers.size() == 0 ? 1 : mMarkers.size();
        return new com.baidu.mapapi.model.LatLng(mTotalLat / len, mTotalLng / len);
    }

    private com.amap.api.maps.model.LatLng calAMAPAverageCenter() {

        int len = mMarkers.size() == 0 ? 1 : mMarkers.size();
        return new com.amap.api.maps.model.LatLng(mTotalLat / len, mTotalLng / len);

    }

    private com.tencent.mapsdk.raster.model.LatLng calTMAPAverageCenter() {

        int len = mMarkers.size() == 0 ? 1 : mMarkers.size();
        return new com.tencent.mapsdk.raster.model.LatLng(mTotalLat / len, mTotalLng / len);

    }

    private LatLng calGoogleAverageCenter() {

        int len = mMarkers.size() == 0 ? 1 : mMarkers.size();
        return new LatLng(mTotalLat / len, mTotalLng);

    }

    //
    public com.baidu.mapapi.model.LatLng getmCenterBaidu(int showType, int mapType) {

        if (mapType == Constant.MAP_TYPE_BAIDU) {

            if (mMarkers.size() == 0) {
                return new com.baidu.mapapi.model.LatLng(0, 0);
            }

            // return the average center point
            if (showType == 1) {
                return calBaiduAverageCenter();
            }

            // return the first point in the block
            else {
                return new com.baidu.mapapi.model.LatLng(mMarkers.get(0).getLat(), mMarkers.get(0).getLng());
            }

        }
        return new com.baidu.mapapi.model.LatLng(0, 0);

    }

    public LatLng getmCenterGoogle(int showType, int mapType) {
        if (mapType == Constant.MAP_TYPE_GOOGLE) {

            if (mMarkers.size() == 0) {
                return new LatLng(0.0, 0.0);
            }

            // return the average center point
            if (showType == 1) {
                return calGoogleAverageCenter();
            }
            // return the first point in the block
            else {
                return new LatLng(mMarkers.get(0).getLat(), mMarkers.get(0).getLng());
            }

        }
        return new LatLng(0.0, 0.0);
    }

    //
    public com.amap.api.maps.model.LatLng getmCenterAmap(int showType, int mapType) {
        if (mapType == Constant.MAP_TYPE_AMAP) {
            if (mMarkers.size() == 0) {
                return new com.amap.api.maps.model.LatLng(0.0, 0.0);
            }

            // return the average center point
            if (showType == 1) {
                return calAMAPAverageCenter();
            }

            // return the first point in the block
            else {
                return new com.amap.api.maps.model.LatLng(mMarkers.get(0).getLat(), mMarkers.get(0).getLng());
            }
        }
        return new com.amap.api.maps.model.LatLng(0.0, 0.0);

    }

    //
    public com.tencent.mapsdk.raster.model.LatLng getmCenterTmap(int showType, int mapType) {
        if (mapType == Constant.MAP_TYPE_TENCENT) {
            if (mMarkers.size() == 0) {
                return new com.tencent.mapsdk.raster.model.LatLng(0.0, 0.0);
            }

            // return the average center point
            if (showType == 1) {
                return calTMAPAverageCenter();
            }

            // return the first point in the block
            else {
                return new com.tencent.mapsdk.raster.model.LatLng(mMarkers.get(0).getLat(), mMarkers.get(0).getLng());
            }
        }
        return new com.tencent.mapsdk.raster.model.LatLng(0.0, 0.0);

    }

    public ArrayList<DeviceInfo> getmMarkers() {
        return mMarkers;
    }
}
