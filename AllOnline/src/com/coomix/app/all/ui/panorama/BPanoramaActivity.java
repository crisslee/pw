package com.coomix.app.all.ui.panorama;

import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.Point;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.widget.RelativeLayout;
import android.widget.Toast;
import com.baidu.lbsapi.BMapManager;
import com.baidu.lbsapi.MKGeneralListener;
import com.baidu.lbsapi.panoramaview.OnTabMarkListener;
import com.baidu.lbsapi.panoramaview.PanoramaView;
import com.baidu.lbsapi.panoramaview.PanoramaViewListener;
import com.baidu.lbsapi.panoramaview.TextMarker;
import com.baidu.mapapi.map.BaiduMap.OnMapClickListener;
import com.baidu.mapapi.map.BaiduMap.OnMapLongClickListener;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.MapPoi;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.model.LatLng;
import com.coomix.app.all.Constant;
import com.coomix.app.all.R;
import com.coomix.app.all.manager.MapIconManager;
import com.coomix.app.all.model.bean.DeviceInfo;
import com.coomix.app.all.model.bean.PanoramaInfo;
import com.coomix.app.all.util.StringUtil;
import com.coomix.app.all.widget.MyActionbar;
import com.coomix.app.framework.util.CommonUtil;
import com.umeng.analytics.MobclickAgent;
import java.lang.ref.WeakReference;
import java.util.HashMap;

public class BPanoramaActivity extends BaseMapActivity implements PanoramaViewListener,
    OnMapClickListener, OnMapLongClickListener {
    private RelativeLayout mapLayout;
    private GLSurfaceView mMapSurfaceView;
    private PanoramaView mPanoramaView;
    public BMapManager mBMapManager = null;

    private Marker mDeviceInMap;
    private TextMarker mDeviceInPanorama;

    private final static int LOAD_PANORAMA_ERROR = 1;
    private final static int UPDATE_TITLE_ADDRESS = 2;
    private Handler mHandler;

    private boolean isMapSmall = true;

    private DeviceInfo mDevice;

    private Point largeBounds;
    private Point smallBounds;

    private AddressHandler mAddressHandler;

    private boolean isFullScreen = false;
    private MyActionbar actionbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        HashMap<String, String> map = new HashMap<String, String>();
        map.put("ev_function", "百度全景");
        MobclickAgent.onEvent(BPanoramaActivity.this, "ev_function", map);
        initEngineManager();
        setContentView(R.layout.activity_bpanorama);

        mDevice = (DeviceInfo) getIntent().getSerializableExtra(Constant.KEY_DEVICE);
        if (mDevice == null) {
            return;
        }

        mHandler = new MyHandler(this);

        initView();
    }

    private void getScreenBounds() {
        Point screenBounds = CommonUtil.getScreentDimention(BPanoramaActivity.this);
        largeBounds = new Point();
        largeBounds.x = screenBounds.x;
        largeBounds.y = CommonUtil.convertDIP2PX(BPanoramaActivity.this, 200);
        smallBounds = new Point();
        smallBounds.x = CommonUtil.convertDIP2PX(BPanoramaActivity.this, 100);
        smallBounds.y = smallBounds.x;
    }

    public void initEngineManager() {
        if (mBMapManager == null) {
            mBMapManager = new BMapManager(getApplicationContext());
        }

        MKGeneralListener listener = new MKGeneralListener() {
            @Override
            public void onGetPermissionState(int arg0) {
                // 非零值表示key验证未通过
            }
        };

        if (!mBMapManager.init(listener)) {
            // Toast.makeText(getApplicationContext(), "BMapManager 初始化错误!",
            // Toast.LENGTH_LONG).show();
        }
    }

    private void initView() {
        actionbar = (MyActionbar) findViewById(R.id.myActionbar);
        actionbar.initActionbar(true, R.string.panorama, 0, 0);

        mapLayout = (RelativeLayout) findViewById(R.id.map_rl);

        if (!StringUtil.isTrimEmpty(mDevice.getAddress())) {
            actionbar.setTitle(mDevice.getAddress());
        }

        mMapView = (MapView) findViewById(R.id.map_view);
        mMapView.showScaleControl(true);
        mMapView.showZoomControls(false);
        mBaiduMap.animateMapStatus(MapStatusUpdateFactory.zoomTo(18.0f)); // 缩放级别18
        mBaiduMap.setOnMapClickListener(this);
        mBaiduMap.setOnMapLongClickListener(this);
        mMapSurfaceView = (GLSurfaceView) mMapView.getChildAt(0);
        mMapSurfaceView.setZOrderMediaOverlay(true);

        mPanoramaView = (PanoramaView) findViewById(R.id.panorama);
        mPanoramaView.setPanoramaImageLevel(PanoramaView.ImageDefinition.ImageDefinitionMiddle);
        // 设置全景图监听
        mPanoramaView.setPanoramaViewListener(this);

        updateAllView(new LatLng(mDevice.getLat(), mDevice.getLng()));
        // 等待两个地址返回：
        mAddressHandler = new AddressHandler();
        mAddressHandler.latLng = new LatLng(mDevice.getLat(), mDevice.getLng());
        mAddressHandler.GeoReverseName = mDevice.getAddress();
    }

    private void updateDeviceLocationOnMap(LatLng latLng) {
        if (mDevice == null) {
            return;
        }
        if (mDeviceInMap == null) {
            BitmapDescriptor bd = MapIconManager.getInstance().getBaiduIcon(mDevice);
            MarkerOptions options = new MarkerOptions().position(latLng).icon(bd).zIndex(9).draggable(false)
                .anchor(0.5f, 0.5f).rotate(-1 * mDevice.getCourse());
            mDeviceInMap = (Marker) mBaiduMap.addOverlay(options);
        }
        mDeviceInMap.setPosition(latLng);
        mBaiduMap.animateMapStatus(MapStatusUpdateFactory.newLatLng(latLng));
    }

    private void updateDeviceOverlayOnPanorama(LatLng latLng) {
        mPanoramaView.removeAllMarker();

        mDeviceInPanorama = new TextMarker();
        mDeviceInPanorama.setMarkerPosition(new com.baidu.lbsapi.tools.Point(latLng.longitude, latLng.latitude));
        mDeviceInPanorama.setFontColor(Color.RED);
        String show = getString(R.string.my_location);
        if (mDevice != null) {
            //            if (!CommonUtil.isTrimEmpty(mDevice.number)) {
            //                show = mDevice.number;
            //            } else
            if (!StringUtil.isTrimEmpty(mDevice.getName())) {
                show = mDevice.getName();
            }
        }
        mDeviceInPanorama.setText(show);
        mDeviceInPanorama.setFontSize(16);
        mDeviceInPanorama.setBgColor(Color.WHITE);
        mDeviceInPanorama.setPadding(5, 5, 5, 5);
        // mDeviceInPanorama.setMarkerHeight(20.0f);
        mDeviceInPanorama.setOnTabMarkListener(new OnTabMarkListener() {

            @Override
            public void onTab() {
                // Toast.makeText(PanoDemoMain.this, "textMark1标注已被点击",
                // Toast.LENGTH_SHORT).show();
            }
        });

        mPanoramaView.addMarker(mDeviceInPanorama);
    }

    private void updateAllView(LatLng latLng) {
        // 更新全景地图
        mPanoramaView.setPanorama(latLng.longitude, latLng.latitude);
        // 更新百度地图
        updateDeviceLocationOnMap(latLng);
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
        if (mPanoramaView != null) {
            mPanoramaView.destroy();
        }
        super.onDestroy();
    }

    private void updateMapLayout() {
        getScreenBounds();
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) mapLayout.getLayoutParams();
        if (isMapSmall) {
            params.width = smallBounds.x;
            params.height = smallBounds.y;
            params.leftMargin = CommonUtil.convertDIP2PX(BPanoramaActivity.this, 10);
            params.bottomMargin = CommonUtil.convertDIP2PX(BPanoramaActivity.this, 10);
        } else {
            params.width = largeBounds.x;
            params.height = largeBounds.y;
            params.leftMargin = 0;
            params.bottomMargin = 0;
        }
        mapLayout.setLayoutParams(params);
        updateDeviceLocationOnMap(new LatLng(mDevice.getLat(), mDevice.getLng()));
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        updateMapLayout();
    }

    @Override
    public void onDescriptionLoadEnd(String s) {

    }

    @Override
    public void onLoadPanoramaBegin() {

    }

    @Override
    public void onLoadPanoramaEnd(String json) {
        PanoramaInfo p = new PanoramaInfo(json);
        if (p != null && !StringUtil.isTrimEmpty(p.id)) {
            // 怎么转换坐标？？？
            if (mDevice != null) {
                updateDeviceOverlayOnPanorama(new LatLng(mDevice.getLat(), mDevice.getLng()));
            }
        }
        if (p == null || StringUtil.isTrimEmpty(p.rname)) {
            mAddressHandler.BaiduName = "";
        } else {
            mAddressHandler.BaiduName = p.rname;
        }
        if (mAddressHandler.GeoReverseName != null) {
            mHandler.sendEmptyMessage(UPDATE_TITLE_ADDRESS);
        }
    }

    private void updateTitleAddress() {
        if (mAddressHandler == null) {
            return;
        }
        if (!StringUtil.isTrimEmpty(mAddressHandler.BaiduName)
            && !StringUtil.isTrimEmpty(mAddressHandler.GeoReverseName)) {
            actionbar.setTitle(mAddressHandler.BaiduName + ": " + mAddressHandler.GeoReverseName);
        } else if (StringUtil.isTrimEmpty(mAddressHandler.BaiduName)
            && !StringUtil.isTrimEmpty(mAddressHandler.GeoReverseName)) {
            actionbar.setTitle(mAddressHandler.GeoReverseName);
        } else if (!StringUtil.isTrimEmpty(mAddressHandler.BaiduName)
            && StringUtil.isTrimEmpty(mAddressHandler.GeoReverseName)) {
            actionbar.setTitle(mAddressHandler.BaiduName);
        }
    }

    @Override
    public void onLoadPanoramaError(String errorJson) {
        mHandler.sendEmptyMessage(LOAD_PANORAMA_ERROR);
    }

    @Override
    public void onMessage(String s, int i) {

    }

    @Override
    public void onCustomMarkerClick(String s) {

    }

    @Override
    public void onMapClick(LatLng arg0) {
        isMapSmall = !isMapSmall;
        updateMapLayout();
    }

    @Override
    public void onMapPoiClick(MapPoi arg0) {
        isMapSmall = !isMapSmall;
        updateMapLayout();
    }

    class AddressHandler {
        LatLng latLng;
        String BaiduName;
        String GeoReverseName;
    }

    static class MyHandler extends Handler {
        WeakReference<BPanoramaActivity> activityWrf;

        public MyHandler(BPanoramaActivity activity) {
            activityWrf = new WeakReference<BPanoramaActivity>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            BPanoramaActivity activity = activityWrf.get();
            switch (msg.what) {
                case LOAD_PANORAMA_ERROR: {
                    Toast.makeText(activity, activity.getResources().getString(R.string.no_panorama_data),
                        Toast.LENGTH_LONG).show();
                    activity.finish();
                }
                break;
                case UPDATE_TITLE_ADDRESS: {
                    activity.updateTitleAddress();
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
    public void onMoveStart() {

    }

    @Override
    public void onMoveEnd() {

    }
}