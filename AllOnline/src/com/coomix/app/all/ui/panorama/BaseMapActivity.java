package com.coomix.app.all.ui.panorama;

import android.os.Bundle;

import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.MapView;
import com.coomix.app.all.R;
import com.coomix.app.framework.util.PreferenceUtil;
import com.coomix.app.all.ui.base.BaseActivity;


public class BaseMapActivity extends BaseActivity {
    protected MapView mMapView = null;
    protected BaiduMap mBaiduMap = null;

    @Override
    protected void onDestroy() {
        if (mMapView != null) {
            mBaiduMap.clear();
            mMapView.onDestroy();
        }
        super.onDestroy();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        PreferenceUtil.init(this);
    }

    @Override
    public void setContentView(int layoutResID) {
        super.setContentView(layoutResID);
        mMapView = (MapView) findViewById(R.id.map_view);
        if (mMapView == null) {
            throw new RuntimeException("Your content must have a MapView whose id attribute is " + "'R.id.map_view'");
        }
        mBaiduMap = mMapView.getMap();
    }

    @Override
    protected void onPause() {
        if (mMapView != null) {
            mMapView.onPause();
        }
        super.onPause();
    }

    @Override
    protected void onResume() {

        if (mMapView != null) {
            mMapView.onResume();
        }
        super.onResume();
    }
}