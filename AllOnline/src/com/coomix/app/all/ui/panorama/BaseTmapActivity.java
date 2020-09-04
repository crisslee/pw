package com.coomix.app.all.ui.panorama;

import android.os.Bundle;

import com.coomix.app.all.R;
import com.coomix.app.framework.util.PreferenceUtil;
import com.coomix.app.all.ui.base.BaseActivity;
import com.tencent.tencentmap.mapsdk.map.MapView;
import com.tencent.tencentmap.mapsdk.map.TencentMap;
import com.tencent.tencentmap.mapsdk.map.UiSettings;

public class BaseTmapActivity extends BaseActivity {
    protected MapView mMapView = null;
    protected TencentMap mTencentMap = null;

    @Override
    protected void onDestroy() {
        if (mMapView != null) {
            mTencentMap.clearAllOverlays();
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
        mTencentMap = mMapView.getMap();
        mMapView.getUiSettings().setScaleControlsEnabled(true);
        mMapView.getUiSettings().setLogoPosition(UiSettings.LOGO_POSITION_LEFT_BOTTOM);
        mMapView.getUiSettings().setScaleViewPosition(UiSettings.SCALEVIEW_POSITION_CENTER_BOTTOM);
        if (mMapView == null) {
            throw new RuntimeException("Your content must have a MapView whose id attribute is " + "'R.id.map_view'");
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

    @Override
    protected void onStop() {
        if (mMapView != null) {
            mMapView.onStop();
        }
        super.onStop();
    }
}