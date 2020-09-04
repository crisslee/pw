package com.coomix.app.all.ui.panorama;

import android.Manifest;
import android.content.DialogInterface;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.MeasureSpec;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.coomix.app.all.Constant;
import com.coomix.app.all.R;
import com.coomix.app.all.model.bean.DeviceInfo;
import com.coomix.app.all.manager.MapIconManager;
import com.coomix.app.all.util.StringUtil;
import com.coomix.app.all.widget.MyActionbar;
import com.coomix.app.framework.util.CommonUtil;
import com.coomix.app.all.util.PermissionUtil;
import com.tencent.mapsdk.raster.model.BitmapDescriptor;
import com.tencent.mapsdk.raster.model.LatLng;
import com.tencent.mapsdk.raster.model.Marker;
import com.tencent.mapsdk.raster.model.MarkerOptions;
import com.tencent.tencentmap.mapsdk.map.TencentMap.OnMapClickListener;
import com.tencent.tencentmap.mapsdk.map.TencentMap.OnMapLongClickListener;
import com.tencent.tencentmap.mapsdk.map.TencentMap.OnMarkerClickListener;
import com.tencent.tencentmap.streetviewsdk.StreetViewPanorama;
import com.tencent.tencentmap.streetviewsdk.StreetViewPanorama.OnStreetViewPanoramaFinishListner;
import com.tencent.tencentmap.streetviewsdk.StreetViewPanoramaInfo;
import com.tencent.tencentmap.streetviewsdk.StreetViewPanoramaView;
import com.umeng.analytics.MobclickAgent;
import java.lang.ref.WeakReference;
import java.util.HashMap;

public class TPanoramaActivity extends BaseTmapActivity implements OnStreetViewPanoramaFinishListner,
    OnMapClickListener, OnMapLongClickListener, OnMarkerClickListener {
    private RelativeLayout mapLayout;
    private StreetViewPanoramaView mPanoramaView;
    private StreetViewPanorama mPanorama;
    private StreetViewPanoramaInfo mPanoramaInfo;

    private com.tencent.mapsdk.raster.model.Marker mDeviceInMap;
    com.tencent.mapsdk.raster.model.Marker mPanoramaInMap;
    private com.tencent.tencentmap.streetviewsdk.Marker mDeviceInPanorama;

    private final static int LOAD_PANORAMA_ERROR = 1;
    private Handler mHandler;

    private boolean isMapSmall = true;

    private DeviceInfo mDevice;

    private Point largeBounds;
    private Point smallBounds;

    private boolean isFullScreen = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        HashMap<String, String> map = new HashMap<String, String>();
        map.put("ev_function", "腾讯全景");
        MobclickAgent.onEvent(TPanoramaActivity.this, "ev_function", map);
        mDevice = (DeviceInfo) getIntent().getSerializableExtra(Constant.KEY_DEVICE);
        if (mDevice == null) {
            finish();
            return;
        }
        mHandler = new MyHandler(this);
        requestPermission();
    }

    @SuppressWarnings("CheckResult")
    private void requestPermission() {
        rxPermissions.requestEach(Manifest.permission.READ_PHONE_STATE)
            .subscribe(permission -> {
                if (permission.granted) {
                    setContentView(R.layout.activity_tpanorama);
                    initView();
                } else if (permission.shouldShowRequestPermissionRationale) {
                    showSettingDlg(getString(R.string.per_phone_state_hint), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            PermissionUtil.goIntentSetting(TPanoramaActivity.this);
                        }
                    }, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                        }
                    });
                } else {
                    finish();
                }
            });
    }

    private void getScreenBounds() {
        Point screenBounds = CommonUtil.getScreentDimention(TPanoramaActivity.this);
        largeBounds = new Point();
        largeBounds.x = screenBounds.x;
        largeBounds.y = CommonUtil.convertDIP2PX(TPanoramaActivity.this, 200);
        smallBounds = new Point();
        smallBounds.x = CommonUtil.convertDIP2PX(TPanoramaActivity.this, 100);
        smallBounds.y = smallBounds.x;
    }

    private void initView() {
        MyActionbar actionbar = (MyActionbar) findViewById(R.id.myActionbar);
        actionbar.initActionbar(true, R.string.panorama, 0, 0);

        mapLayout = (RelativeLayout) findViewById(R.id.map_rl);
        if (!StringUtil.isTrimEmpty(mDevice.getAddress())) {
            actionbar.setTitle(mDevice.getAddress());
        }

        initMap();
        initPanorama();

        updateAllView();
    }

    private void initMap() {
        mTencentMap.setOnMapClickListener(this);
        mTencentMap.setOnMapLongClickListener(this);
        mTencentMap.setOnMarkerClickListener(this);
        mTencentMap.setZoom(18); // 缩放级别18
    }

    private void initPanorama() {
        mPanoramaView = (StreetViewPanoramaView) findViewById(R.id.panorama);
        mPanorama = mPanoramaView.getStreetViewPanorama();

        mPanorama.setIndoorGuidanceEnabled(false);
        mPanorama.setZoomLevel(2);
        mPanorama.setStreetNamesEnabled(true);
        // 设置全景图监听
        mPanorama.setOnStreetViewPanoramaFinishListener(this);
    }

    private View getMarkerLayout(String title) {
        LayoutInflater layInflater = getLayoutInflater();
        View markerLayout = layInflater.inflate(R.layout.panorama_device_marker, null);
        TextView tvMarkerTitle = (TextView) markerLayout.findViewById(R.id.device_tv);

        tvMarkerTitle.setText(title);
        return markerLayout;
    }

    private Bitmap convertViewToBitmap(View view) {
        view.measure(MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED), MeasureSpec.makeMeasureSpec(0,
            MeasureSpec.UNSPECIFIED));
        view.layout(0, 0, view.getMeasuredWidth(), view.getMeasuredHeight());
        view.buildDrawingCache();
        Bitmap bitmap = view.getDrawingCache();
        return bitmap;
    }

    private void updateDeviceLocationOnMap() {
        if (mDevice == null) {
            return;
        }
        LatLng latLng = new LatLng(mDevice.getLat(), mDevice.getLng());
        if (mDeviceInMap == null) {
            BitmapDescriptor bd = MapIconManager.getInstance().getTmapIcon(mDevice);
            MarkerOptions options = new MarkerOptions().position(latLng).icon(bd).draggable(false).anchor(.5f, .5f)
                .rotation(mDevice.getCourse());
            mDeviceInMap = mTencentMap.addMarker(options);
        } else {
            mDeviceInMap.setPosition(latLng);
            mDeviceInMap.setRotation(mDevice.getCourse());
        }
        mTencentMap.animateTo(latLng);
    }

    //private void updatePanoramaLocationOnMap()
    //{
    //    if (mPanoramaInfo == null)
    //    {
    //        return;
    //    }
    //    LatLng latLng = new LatLng(mPanoramaInfo.latitude, mPanoramaInfo.longitude);
    //    if (mPanoramaInMap == null)
    //    {
    //        BitmapDescriptor bd = BitmapDescriptorFactory.fromResource(R.drawable.dev_blue);
    //        MarkerOptions options = new MarkerOptions().position(latLng).icon(bd).draggable(false).anchor(.5f, 1.0f);
    //        mPanoramaInMap = mTencentMap.addMarker(options);
    //    }
    //    else
    //    {
    //        mPanoramaInMap.setPosition(latLng);
    //    }
    //    mTencentMap.animateTo(latLng);
    //}

    private void updateDeviceOverlayOnPanorama() {
        if (mDevice == null) {
            return;
        }
        if (mDeviceInPanorama != null) {
            mPanorama.removeMarker(mDeviceInPanorama);
            mDeviceInPanorama = null;
        }
        String show = getString(R.string.my_location);
        if (mDevice != null) {
            /*if (!CommonUtil.isTrimEmpty(mDevice.number))
            {
                show = mDevice.number;
            }
            else */
            if (!StringUtil.isTrimEmpty(mDevice.getName())) {
                show = mDevice.getName();
            }
        }
        String distanceStr = "";
        if (mPanoramaInfo != null) {
            distanceStr = getString(R.string.device_panorama_distance, (int) (mMapView.getProjection()
                .distanceBetween(new LatLng(mDevice.getLat(), mDevice.getLng()), new LatLng(mPanoramaInfo.latitude,
                    mPanoramaInfo.longitude))));
        }

        mDeviceInPanorama = new com.tencent.tencentmap.streetviewsdk.Marker(mDevice.getLat(), mDevice.getLng(),
            convertViewToBitmap(getMarkerLayout(show + distanceStr))) {
            @Override
            public void onClick(float arg0, float arg1) {
                super.onClick(arg0, arg1);
            }
        };

        mPanorama.addMarker(mDeviceInPanorama);
    }

    private void updateAllView() {
        if (mDevice == null) {
            return;
        }
        // 更新全景地图
        mPanorama.setPosition(mDevice.getLat(), mDevice.getLng());
        updateDeviceOverlayOnPanorama();
        // 更新地图
        updateDeviceLocationOnMap();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private void updateMapLayout() {
        getScreenBounds();
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) mapLayout.getLayoutParams();
        if (isMapSmall) {
            params.width = smallBounds.x;
            params.height = smallBounds.y;
            params.leftMargin = CommonUtil.convertDIP2PX(TPanoramaActivity.this, 10);
            params.bottomMargin = CommonUtil.convertDIP2PX(TPanoramaActivity.this, 10);
        } else {
            params.width = largeBounds.x;
            params.height = largeBounds.y;
            params.leftMargin = 0;
            params.bottomMargin = 0;
        }
        mapLayout.setLayoutParams(params);
        if (mPanoramaInfo != null) {
            mTencentMap.animateTo(new LatLng(mPanoramaInfo.latitude, mPanoramaInfo.longitude));
        } else if (mDevice != null) {
            mTencentMap.animateTo(new LatLng(mDevice.getLat(), mDevice.getLng()));
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        updateMapLayout();
    }

    @Override
    public void onMapClick(LatLng arg0) {
        isMapSmall = !isMapSmall;
        updateMapLayout();
    }

    static class MyHandler extends Handler {
        WeakReference<TPanoramaActivity> activityWrf;

        public MyHandler(TPanoramaActivity activity) {
            activityWrf = new WeakReference<TPanoramaActivity>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            TPanoramaActivity activity = activityWrf.get();
            switch (msg.what) {
                case LOAD_PANORAMA_ERROR: {
                    Toast.makeText(activity, activity.getResources().getString(R.string.no_panorama_data),
                        Toast.LENGTH_LONG).show();
                    activity.finish();
                }
                break;
                default:
                    break;
            }
        }
    }

    @Override
    public void onMapLongClick(LatLng arg0) {

    }

    @Override
    public void OnStreetViewPanoramaFinish(boolean isSuccess) {
        if (isSuccess) {
            mPanoramaInfo = mPanorama.getCurrentStreetViewInfo();
            if (mPanoramaInfo != null) {
                updateDeviceOverlayOnPanorama();
                //updatePanoramaLocationOnMap();
            }
        } else {
            mHandler.sendEmptyMessage(LOAD_PANORAMA_ERROR);
        }
    }

    @Override
    public boolean onMarkerClick(Marker arg0) {
        return true;
    }
}