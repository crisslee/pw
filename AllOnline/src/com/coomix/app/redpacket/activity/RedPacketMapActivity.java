package com.coomix.app.redpacket.activity;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.MapsInitializer;
import com.amap.api.maps.model.BitmapDescriptor;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.Circle;
import com.amap.api.maps.model.CircleOptions;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.LatLngBounds;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.services.core.PoiItem;
import com.coomix.app.all.AllOnlineApp;
import com.coomix.app.all.R;
import com.coomix.app.all.ui.im.ExMapActivity;
import com.coomix.app.all.util.CommunityUtil;
import com.coomix.app.all.widget.MyActionbar;
import com.coomix.app.redpacket.util.RedPacketConstant;
import com.coomix.app.redpacket.util.RedPacketExtendInfo;
import com.coomix.app.redpacket.util.RedPacketInfo;
import com.coomix.app.redpacket.util.RpLocationUtil;

/**
 * Created by ssl on 2017/2/28.
 */
public class RedPacketMapActivity extends ExMapActivity {
    private LatLng redpacketLatLng;
    private Marker myLocationMarker;
    private Marker circleMarker;
    private Marker redpacketMarker;
    private Circle circle;
    private View infoWindowView;
    private String locationName = "";
    private RedPacketInfo redPacketInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_redpacket_map);

        MyActionbar actionbar = (MyActionbar) findViewById(R.id.myActionbar);
        actionbar.initActionbar(true, R.string.redpacket_map_location, 0, 0);

        if (getIntent() != null && getIntent().hasExtra(RedPacketConstant.REDPACKET_DATA)) {
            redPacketInfo = (RedPacketInfo) getIntent().getSerializableExtra(RedPacketConstant.REDPACKET_DATA);
        }

        if (redPacketInfo == null) {
            finish();
        }

        try {
            MapsInitializer.initialize(this);
        } catch (Exception e) {

        }

        mMapView.onCreate(savedInstanceState);
        mAMap = mMapView.getMap();
        mAMap.setInfoWindowAdapter(myInfoWindowAdapter);

        //mAMap.setOnCameraChangeListener(onCameraChangeListener);

        initData();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (myLocationMarker != null) {
            myLocationMarker.destroy();
            myLocationMarker = null;
        }
        if (circleMarker != null) {
            circleMarker.destroy();
            circleMarker = null;
        }
    }

    private void initData() {
        RedPacketExtendInfo extendInfo = redPacketInfo.getExtend_item();
        if (extendInfo == null) {
            finish();
            return;
        }

        //把地图刷新到红包当前的位置
        redpacketLatLng = new LatLng(Double.parseDouble(extendInfo.getLat()), Double.parseDouble(extendInfo.getLng()));
        //mAMap.moveCamera(CameraUpdateFactory.newLatLngZoom(redpacketLatLng, 11));
        mAMap.setOnMapLoadedListener(new AMap.OnMapLoadedListener() {
            @Override
            public void onMapLoaded() {
                try {
                    //把地图刷新到红包当前的位置
                    refreshLocation(redpacketLatLng);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        //绘制我当前的所在位置的icon marker
        drawMyLocation();

        //设置红包所在位置的名字
        if (redPacketInfo.getExtend_item() != null && !TextUtils.isEmpty(
            redPacketInfo.getExtend_item().getLoc_name())) {
            updateLocationName(redPacketInfo.getExtend_item().getLoc_name());
        } else {
            CommunityUtil.getAddressByLatLng(this, redpacketLatLng, new CommunityUtil.OnAddressCallBack() {
                @Override
                public void onAddressBack(PoiItem poiItem) {
                    if (poiItem == null) {
                        return;
                    }
                    updateLocationName(poiItem.getTitle());
                }
            });
        }

        //绘制红包的图标icon marker
        drawRedPacketLocationIcon();

        //绘制红包的可领取范围
        drawRpArangeCircle();
    }

    private void updateLocationName(String name) {
        if (redpacketMarker == null) {
            drawRedPacketLocationIcon();
        }
        this.locationName = name;
        redpacketMarker.setPosition(redpacketLatLng);
        redpacketMarker.setTitle(name);
        myInfoWindowAdapter.getInfoWindow(redpacketMarker);
        redpacketMarker.showInfoWindow();
    }

    /**
     * 刷新地图位置
     */
    private void refreshLocation(LatLng nowLatLng) {
        if (nowLatLng != null && mMapView != null && mAMap != null) {
            double distance = redPacketInfo.getAlloc_range() * 1.1f / 500 * 0.004436229456270282d;
            LatLng latLng1 = new LatLng(nowLatLng.latitude, nowLatLng.longitude - distance);
            LatLng latLng2 = new LatLng(nowLatLng.latitude, nowLatLng.longitude + distance);
            LatLng latLng3 = new LatLng(nowLatLng.latitude - distance, nowLatLng.longitude);
            LatLng latLng4 = new LatLng(nowLatLng.latitude + distance, nowLatLng.longitude);

            //必须最少四个点，确认出一个矩形
            LatLngBounds.Builder builder = new LatLngBounds.Builder();
            builder.include(latLng1);
            builder.include(latLng2);
            builder.include(latLng3);
            builder.include(latLng4);

            mAMap.moveCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), 30));
        }
    }

    public AMapLocationClient mLocationClient = null;
    private AMapLocationClientOption mLocationClientOption;
    private LocationListener mLocationListener;

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

    private class LocationListener implements AMapLocationListener {

        @SuppressLint("SimpleDateFormat")
        @Override
        public void onLocationChanged(AMapLocation location) {
            if (location != null) {
                mLocationClient.stopLocation();
                LatLng myLocLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                if (myLocationMarker == null) {
                    // 图标
                    View view = new View(RedPacketMapActivity.this);
                    view.setBackgroundResource(R.drawable.mylocation);
                    BitmapDescriptor myLocBitmap = BitmapDescriptorFactory.fromView(view);
                    myLocationMarker = mAMap.addMarker(new MarkerOptions().position(myLocLatLng).icon(myLocBitmap)
                        .anchor(0.5f, 0.5f));
                } else {
                    myLocationMarker.setPosition(myLocLatLng);
                }
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
        mLocationClientOption.setNeedAddress(true);
        return mLocationClientOption;
    }

    /**
     * 添加我的位置的标识
     */
    private void drawMyLocation() {
        double lat = AllOnlineApp.getCurrentLocation().getLatitude();
        double lng = AllOnlineApp.getCurrentLocation().getLongitude();
        //        if (lat == 0 || lng == 0)
        //        {
        //            // 定位失败
        ////            lat = CityManager.getInstance().getUserSelectedCity().latitude;
        ////            lng = CityManager.getInstance().getUserSelectedCity().longitude;
        //            startLocation();
        //            return;
        //        }
        LatLng myLocLatLng = new LatLng(lat, lng);
        if (myLocationMarker == null) {
            // 图标
            View view = new View(this);
            view.setBackgroundResource(R.drawable.mylocation);
            BitmapDescriptor myLocBitmap = BitmapDescriptorFactory.fromView(view);
            myLocationMarker = mAMap.addMarker(new MarkerOptions().position(myLocLatLng).icon(myLocBitmap)
                .anchor(0.5f, 0.5f));
        } else {
            myLocationMarker.setPosition(myLocLatLng);
        }
    }

    /**
     * 绘制红包的位置--图片和文字
     */
    private void drawRedPacketLocationIcon() {
        if (redpacketLatLng != null) {
            // 图标
            View view = new View(this);
            view.setBackgroundResource(R.drawable.map_redpacket_icon);
            BitmapDescriptor rpLocBitmap = BitmapDescriptorFactory.fromView(view);
            redpacketMarker = mAMap.addMarker(new MarkerOptions().position(redpacketLatLng).icon(rpLocBitmap)
                .anchor(0.5f, 0.5f));
            redpacketMarker.showInfoWindow();
        }
    }

    /**
     * 刷新距离圆圈
     */
    private void drawRpArangeCircle() {
        if (redpacketLatLng == null || redPacketInfo.getAlloc_range() <= 0) {
            return;
        }

        if (circle == null) {
            circle = mAMap.addCircle(new CircleOptions().center(redpacketLatLng).radius(redPacketInfo.getAlloc_range())
                .fillColor(Color.argb(50, 0, 122, 200)).strokeWidth(4)
                .strokeColor(Color.argb(90, 16, 171, 254)));
        } else {
            circle.setCenter(redpacketLatLng);
            circle.setRadius(redPacketInfo.getAlloc_range());
        }

        LatLng circleLatLng = getDistanceLatlng(redPacketInfo.getAlloc_range(), redpacketLatLng);
        TextView textView = new TextView(this);
        textView.setPadding(getResources().getDimensionPixelOffset(R.dimen.space_5x), 0,
            getResources().getDimensionPixelOffset(R.dimen.space), 0);
        textView.setText(RpLocationUtil.getRangeString(this, redPacketInfo.getAlloc_range()));

        textView.setGravity(Gravity.CENTER);
        textView.setTextColor(getResources().getColor(R.color.redpacket_map_blue));
        textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimensionPixelSize(R.dimen.text));
        textView.setBackgroundResource(R.drawable.rp_distance_bg);
        BitmapDescriptor bitmap = BitmapDescriptorFactory.fromView(textView);
        if (circleMarker == null) {
            circleMarker = mAMap.addMarker(new MarkerOptions().position(circleLatLng).anchor(0.5f, 0.5f).icon(bitmap));
        } else {
            circleMarker.setPosition(circleLatLng);
            circleMarker.setIcon(bitmap);
        }
    }

    /**
     * 根据一个点的经纬度和距离得到另外一个点的经纬度(只改变维度)
     */
    public LatLng getDistanceLatlng(float distance, LatLng latlngA) {
        return new LatLng(latlngA.latitude - distance / 500 * 0.004436229456270282d, latlngA.longitude);
    }

    private AMap.InfoWindowAdapter myInfoWindowAdapter = new AMap.InfoWindowAdapter() {
        TextView textTitle;

        @Override
        public View getInfoWindow(Marker marker) {
            if (infoWindowView == null) {
                infoWindowView =
                    LayoutInflater.from(RedPacketMapActivity.this).inflate(R.layout.rp_custom_info_window, null);
                textTitle = (TextView) infoWindowView.findViewById(R.id.textViewTitle);
            }

            infoWindowView.setBackgroundResource(R.color.transparent);

            if (TextUtils.isEmpty(locationName)) {
                infoWindowView.setVisibility(View.INVISIBLE);
            } else {
                infoWindowView.setVisibility(View.VISIBLE);
                textTitle.setText(locationName);
            }

            return infoWindowView;
        }

        @Override
        public View getInfoContents(Marker marker) {
            return null;
        }
    };
}
