package com.coomix.app.all.ui.im;

import android.os.Bundle;
import com.amap.api.maps.AMap;
import com.amap.api.maps.MapView;
import com.coomix.app.all.R;
import com.coomix.app.all.ui.base.BaseActivity;

public class ExMapActivity extends BaseActivity {
    protected MapView mMapView = null;
    protected AMap mAMap = null;

    @Override
    protected void onDestroy() {
        if (mAMap != null) {
            mAMap.clear();
            mAMap = null;
        }
        if (mMapView != null) {
            mMapView.onDestroy();
            mMapView = null;
        }
        super.onDestroy();
    }

    /**
     * 在setContentView(layoutResID) 后请调用 mMapView.onCreate(savedInstanceState);
     * mAMap = mMapView.getMap();
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void setContentView(int layoutResID) {
        super.setContentView(layoutResID);
        mMapView = (MapView) findViewById(R.id.bmapView);
        if (mMapView == null) {
            throw new RuntimeException("Your content must have a MapView whose id attribute is " + "'R.id.bmapView'");
        }
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