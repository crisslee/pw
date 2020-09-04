package com.coomix.app.all.map.baidu;

import android.os.Bundle;

import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.search.core.PoiInfo;
import com.baidu.mapapi.search.poi.PoiResult;
import com.coomix.app.all.R;

import java.util.ArrayList;

public class MyPoiOverlay {
    private static final int MAX_POI_SIZE = 5;
    private BaiduMap mBaiduMap;
    private PoiResult mPoiResult;
    private String mType;
    private BitmapDescriptor bitmap;
    private ArrayList<Marker> mOverlays;
    private ArrayList<MarkerOptions> mOverlayOptions;

    public MyPoiOverlay(BaiduMap baiduMap, String type) {
        mBaiduMap = baiduMap;
        mType = type;
        if (type.equals("停车场")) {
            bitmap = BitmapDescriptorFactory.fromResource(R.drawable.interestpoint_park_baidu);
        } else if (type.equals("加油站")) {
            bitmap = BitmapDescriptorFactory.fromResource(R.drawable.ic_gas_baidu);
        } else if (type.equals("维修厂")) {
            bitmap = BitmapDescriptorFactory.fromResource(R.drawable.ic_car_fix_baidu);
        } else if (type.equals("银行")) {
            bitmap = BitmapDescriptorFactory.fromResource(R.drawable.interestpoint_bank_baidu);
        } else if (type.equals("厕所")) {
            bitmap = BitmapDescriptorFactory.fromResource(R.drawable.interestpoint_toilet_baidu);
        } else if (type.equals("景点")) {
            bitmap = BitmapDescriptorFactory.fromResource(R.drawable.interestpoint_spot_baidu);
        } else if (type.equals("餐饮")) {
            bitmap = BitmapDescriptorFactory.fromResource(R.drawable.interestpoint_catering_baidu);
        } else if (type.equals("酒店")) {
            bitmap = BitmapDescriptorFactory.fromResource(R.drawable.interestpoint_hotel_baidu);
        } else if (type.equals("服务区")) {
            bitmap = BitmapDescriptorFactory.fromResource(R.drawable.interestpoint_service_area_baidu);
        } else {
            bitmap = BitmapDescriptorFactory.fromResource(R.drawable.interestpoint_empty);
        }
    }

    /**
     * 设置POI数据
     *
     * @param poiResult 设置POI数据
     */
    public void setData(PoiResult poiResult) {
        mPoiResult = poiResult;

        if (mPoiResult == null || mPoiResult.getAllPoi() == null) {
            return;
        }

        mOverlayOptions = new ArrayList<MarkerOptions>();
        int markerSize = 0;
        for (int i = 0; i < mPoiResult.getAllPoi().size() && markerSize < MAX_POI_SIZE; i++) {
            PoiInfo info = mPoiResult.getAllPoi().get(i);
            if (info.location == null) {
                continue;
            }
            markerSize++;
            Bundle extra = new Bundle();
            extra.putParcelable("poiInfo", info);
            extra.putString("type", OverlayType.OVERLAY_POI);
            MarkerOptions options = new MarkerOptions().icon(bitmap).extraInfo(extra).position(info.location);
            mOverlayOptions.add(options);
        }
    }

    /**
     * 将所有Overlay 从 地图上消除
     */
    public void removeFromMap() {
        if (mBaiduMap == null) {
            return;
        }
        if (mOverlays != null) {
            for (Marker marker : mOverlays) {
                marker.remove();
            }
            mOverlays.clear();
        }
        if (mOverlayOptions != null) {
            mOverlayOptions.clear();
        }
    }

    /**
     * 将所有Overlay 添加到地图上
     */
    public void addToMap() {
        if (mBaiduMap == null || mOverlayOptions == null) {
            return;
        }
        if (mOverlays != null) {
            for (Marker marker : mOverlays) {
                marker.remove();
            }
            mOverlays.clear();
        }
        mOverlays = new ArrayList<Marker>();
        for (MarkerOptions options : mOverlayOptions) {
            Marker marker = (Marker) mBaiduMap.addOverlay(options);
            mOverlays.add(marker);
        }
    }
}