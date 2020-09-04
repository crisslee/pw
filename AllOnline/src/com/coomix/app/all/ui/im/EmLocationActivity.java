package com.coomix.app.all.ui.im;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.RelativeLayout;
import android.widget.Toast;
import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.model.BitmapDescriptor;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.core.PoiItem;
import com.amap.api.services.geocoder.GeocodeQuery;
import com.amap.api.services.geocoder.GeocodeResult;
import com.amap.api.services.geocoder.GeocodeSearch;
import com.amap.api.services.geocoder.GeocodeSearch.OnGeocodeSearchListener;
import com.amap.api.services.geocoder.RegeocodeQuery;
import com.amap.api.services.geocoder.RegeocodeResult;
import com.coomix.app.all.AllOnlineApp;
import com.coomix.app.all.GlobalParam;
import com.coomix.app.all.R;
import com.coomix.app.all.widget.MyActionbar;
import java.util.List;

/**
 * 聊天消息中的位置消息的展示
 *
 * @author 刘生健
 * @since 2015-12-15 下午06:24:42
 */
public class EmLocationActivity extends ExMapActivity implements OnGeocodeSearchListener, OnClickListener {

    private AMapLocation mLocation;
    private GeocodeSearch mGeocoderSearch;
    private PoiItem mSelectedPoint;
    private boolean isIntent = false;
    private RelativeLayout mMapPlusRL, mMapMinusRL, mLocateCurrentRL;
    private double latitude;
    private double longitude;
    private String address;
    private String from;
    public static final String FROM_CHAT_ACTION = "chat_get_my_location";
    private boolean isMyLocation;
    private MyActionbar actionbar;

    public AMapLocationClient mLocationClient = null;
    private AMapLocationClientOption mLocationClientOption;
    private LocationListener mLocationListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.em_chat_location);
        mMapView.onCreate(savedInstanceState);// 此方法必须重写
        mAMap = mMapView.getMap();

        if (getIntent() != null && getIntent().getExtras() != null) {
            from = getIntent().getExtras().getString("from");
            latitude = getIntent().getExtras().getDouble("latitude", 0);
            longitude = getIntent().getExtras().getDouble("longitude", 0);
            address = getIntent().getExtras().getString("textAddress");
        }

        isMyLocation = (from != null && FROM_CHAT_ACTION.equals(from));
        initView(savedInstanceState, isMyLocation);

        startLocation();
    }

    private void moveToLocation(AMapLocation mLocation, boolean isMyLocation) {
        LatLng latLng;

        if (isMyLocation) {
            // 从聊天中定位我的位置
            actionbar.setRightText("发送");
            // 我的位置
            //mLocation = AllOnlineApp.getCurrentLocation();
            if (mLocation != null) {
                latLng = new LatLng(mLocation.getLatitude(), mLocation.getLongitude());
                mAMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 14));
            } else {
                Toast.makeText(this, "未定位到当前位置", Toast.LENGTH_SHORT).show();
                return;
            }
            mGeocoderSearch = new GeocodeSearch(EmLocationActivity.this);
            mGeocoderSearch.setOnGeocodeSearchListener(EmLocationActivity.this);
            if (latLng != null) {
                LatLonPoint cenpt = new LatLonPoint(latLng.latitude, latLng.longitude);
                getAddress(cenpt);
            }
        } else {
            // 别人发给我的位置信息
            if (latitude != 0 && longitude != 0 && address != null) {
                //
                actionbar.setRightText(0);
                latLng = new LatLng(latitude, longitude);
                mAMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 14));
            } else {
                Toast.makeText(this, "无效的位置信息", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        addLocation(mAMap, latLng);
    }

    public void addLocation(AMap aMap, LatLng latLng) {
        // 图标
        MarkerOptions myLocationOptions = new MarkerOptions().position(latLng)
            .icon(getBitmapDescriptorFromResource(R.drawable.mylocation)).anchor(0.5f, 0.5f);
        Marker myLocation = aMap.addMarker(myLocationOptions);
        myLocation.setZIndex(0.2f);
    }

    private BitmapDescriptor getBitmapDescriptorFromResource(int resId) {
        View v = new View(AllOnlineApp.mApp.getApplicationContext());
        v.setBackgroundResource(resId);
        BitmapDescriptor bitmap = BitmapDescriptorFactory.fromView(v);
        return bitmap;
    }

    private void initView(Bundle savedInstanceState, boolean islocation) {
        actionbar = (MyActionbar) findViewById(R.id.myActionbar);
        actionbar.initActionbar(true, "位置信息", 0, 0);
        mAMap.getUiSettings().setZoomControlsEnabled(false);

        mMapPlusRL = (RelativeLayout) findViewById(R.id.map_plus_rl);
        mMapPlusRL.setOnClickListener(this);
        mMapMinusRL = (RelativeLayout) findViewById(R.id.map_minus_rl);
        mMapMinusRL.setOnClickListener(this);
        mLocateCurrentRL = (RelativeLayout) findViewById(R.id.locate_current_rl);
        mLocateCurrentRL.setOnClickListener(this);
        actionbar.setRightTextClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                // mButtonSubmit.setEnabled(false);
                if (null != mSelectedPoint) {
                    Intent data = new Intent();
                    data.putExtra("latitude", mSelectedPoint.getLatLonPoint().getLatitude());
                    data.putExtra("longitude", mSelectedPoint.getLatLonPoint().getLongitude());
                    data.putExtra("textAddress", mSelectedPoint.getSnippet());
                    setResult(RESULT_OK, data);
                    finish();
                } else if (mLocation != null) {
                    Intent data = new Intent();
                    data.putExtra("latitude", mLocation.getLatitude());
                    data.putExtra("longitude", mLocation.getLongitude());
                    String address = mLocation.getAddress();
                    if (address == null) {
                        address = "未知";
                    }
                    data.putExtra("textAddress", address);
                    setResult(RESULT_OK, data);
                    finish();
                } else {
                    Toast.makeText(EmLocationActivity.this, "未获取到当前位置信息", Toast.LENGTH_SHORT).show();
                }
                if (mLocationClient != null && mLocationClient.isStarted()) {
                    mLocationClient.stopLocation();
                }
            }
        });
    }

    /**
     * 响应地理编码
     *
     * @param name 地址
     * @param city 表示查询城市，中文或者中文全拼，citycode、adcode
     */
    public void getLatlon(String name, String city) {
        GeocodeQuery query = new GeocodeQuery(name, city);// 第一个参数表示地址，第二个参数表示查询城市，中文或者中文全拼，citycode、adcode，
        mGeocoderSearch.getFromLocationNameAsyn(query);// 设置同步地理编码请求
    }

    /**
     * 响应逆地理编码
     * 经纬度Latlng
     */
    public void getAddress(LatLonPoint latLonPoint) {
        RegeocodeQuery query = new RegeocodeQuery(latLonPoint, 200,
            GeocodeSearch.AMAP);// 第一个参数表示一个Latlng，第二参数表示范围多少米，第三个参数表示是火系坐标系还是GPS原生坐标系
        if (mGeocoderSearch != null && query != null) {
            mGeocoderSearch.getFromLocationAsyn(query);// 设置同步逆地理编码请求
        }
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
        if (mGeocoderSearch != null) {
            mGeocoderSearch = null;
        }
    }

    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub
        switch (v.getId()) {
            case R.id.map_minus_rl:
                mAMap.moveCamera(CameraUpdateFactory.zoomOut());
                break;
            case R.id.map_plus_rl:
                mAMap.moveCamera(CameraUpdateFactory.zoomIn());
                break;
            case R.id.locate_current_rl:
                mLocation = AllOnlineApp.getCurrentLocation();
                if (false/*mLocation != null*/) {
                    LatLng cenpt = new LatLng(mLocation.getLatitude(), mLocation.getLongitude());
                    mAMap.moveCamera(CameraUpdateFactory.changeLatLng(cenpt));
                } else {
                    LatLng cenpt = new LatLng(GlobalParam.getInstance().get_lat(), GlobalParam.getInstance().get_lng());
                    mAMap.moveCamera(CameraUpdateFactory.changeLatLng(cenpt));
                }
                break;
            default:
                break;
        }
    }

    @Override
    public void onGeocodeSearched(GeocodeResult result, int rCode) {

    }

    @Override
    public void onRegeocodeSearched(RegeocodeResult result, int rCode) {
        if (rCode == 0) {
            if (result != null && result.getRegeocodeAddress() != null
                && result.getRegeocodeAddress().getFormatAddress() != null) {
                List<PoiItem> localList = result.getRegeocodeAddress().getPois();
                if (localList != null && localList.size() != 0) {
                    mSelectedPoint = localList.get(0);
                }
            }
        } else if (rCode == 27) {
            // 搜索失败,请检查网络连接！
        } else if (rCode == 32) {
            // key验证失败
        } else {
            // 其它错误
        }
    }

    private class LocationListener implements AMapLocationListener {

        @SuppressLint("SimpleDateFormat")
        @Override
        public void onLocationChanged(AMapLocation location) {
            if (location != null) {
                //AllOnlineApp.setCurrentLocation(location);
                mLocation = location;
                moveToLocation(mLocation, isMyLocation);
            }
        }
    }

    /**
     * 获取特定的LocationClientOption
     */
    private AMapLocationClientOption getLocationClientOption() {
        mLocationClientOption = new AMapLocationClientOption();
        mLocationClientOption.setGpsFirst(true);
        mLocationClientOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
        //mLocationClientOption.setInterval(BAIDU_LOCATION_SCANSPAN);// 设置发起定位请求的间隔时间为10000ms
        mLocationClientOption.setNeedAddress(true);
        return mLocationClientOption;
    }

    private void startLocation() {
        mLocationClient = new AMapLocationClient(getApplicationContext());
        if (mLocationClient != null) {
            mLocationListener = new LocationListener();
            if (mLocationListener != null) {
                mLocationClient.setLocationListener(mLocationListener);
            }
            mLocationClient.setLocationOption(getLocationClientOption());
            mLocationClient.startLocation();
        }
    }
}
