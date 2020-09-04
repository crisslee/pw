package com.coomix.app.all.ui.alarm;

import android.content.Intent;
import android.os.Bundle;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.coomix.app.all.AllOnlineApp;
import com.coomix.app.all.Constant;
import com.coomix.app.all.R;
import com.coomix.app.all.manager.DeviceManager;
import com.coomix.app.all.model.bean.Alarm;
import com.coomix.app.all.model.bean.RespDomainAdd;
import com.coomix.app.all.manager.DomainManager;
import com.coomix.app.all.service.ServiceAdapter;
import com.coomix.app.all.service.ServiceAdapter.ServiceAdapterCallback;
import com.coomix.app.all.ui.base.BaseActivity;
import com.coomix.app.all.util.StringUtil;
import com.coomix.app.all.widget.MyActionbar;
import com.coomix.app.all.widget.ZoomControlView;
import com.coomix.app.framework.app.Result;
import com.coomix.app.framework.util.TimeUtil;
import com.coomix.app.all.manager.SettingDataManager;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.InfoWindowAdapter;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import java.util.Date;

public class GAlarmLocationActivity extends BaseActivity implements OnClickListener,
        ServiceAdapterCallback, OnMarkerClickListener, OnMapReadyCallback {
    private ImageButton navMap;
    protected ServiceAdapter mServiceAdapter;
    private TextView deviceNameTv, alarmTimeTv, alarmTypeTv, addressTv, alarmSpeedTV;
    private LinearLayout speedLL;
    private View popView;
    private Alarm mAlarm;
    private GoogleMap mMap;
    private View lineShowView;
    private Marker mAlarmMarker;
    private int flag_map = Constant.MAP_NORMAL;

    private ZoomControlView zoomControlView;

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
    }

    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the
        // map.
//        if (mMap == null) {
//            // Try to obtain the map from the SupportMapFragment.
//            mMap = ((SupportMapFragment) getSupportFragmentManager()
//                    .findFragmentById(R.id.map_view)).getMap();
            if (mMap != null) {
                setUpMap();
            } else {
                Toast.makeText(getApplicationContext(), "Sorry! unable to create maps",
                        Toast.LENGTH_SHORT).show();
                finish();
                return;
            }
//        }
    }

    private void setUpMap() {
        mMap.getUiSettings().setMyLocationButtonEnabled(false);
        mMap.getUiSettings().setZoomControlsEnabled(false);
        mMap.setOnMarkerClickListener(this);
        mMap.getUiSettings().setTiltGesturesEnabled(true);
        zoomControlView.setMap(mMap);

        mMap.setInfoWindowAdapter(new InfoWindowAdapter() {

            @Override
            public View getInfoWindow(Marker marker) {
                if (marker.equals(mAlarmMarker)) {
                    return popView;
                }
                return null;
            }

            @Override
            public View getInfoContents(Marker marker) {
                return null;
            }
        });
        addMarkersToMap();
    }

    private void addMarkersToMap() {
        LatLng current = new LatLng(mAlarm.getLat(), mAlarm.getLng());

        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(current, 15));

        mAlarmMarker = mMap.addMarker(new MarkerOptions().anchor(0.5f, 0.5f).position(current)
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.dev_blue)));
        mAlarmMarker.showInfoWindow();
    }

    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_galarm_location);

        mAlarm = (Alarm) getIntent().getSerializableExtra("ALARM");
        initView();
        mServiceAdapter = ServiceAdapter.getInstance(this);
        mServiceAdapter.registerServiceCallBack(this);
        reverseGeo();
    }

    protected void initView() {
        MyActionbar actionbar = (MyActionbar) findViewById(R.id.myActionbar);
        actionbar.initActionbar(true, R.string.alarm, 0, 0);

        popView = LayoutInflater.from(this).inflate(R.layout.popup_layout_alarm, null);
        deviceNameTv = (TextView) popView.findViewById(R.id.device_name);
        alarmTypeTv = (TextView) popView.findViewById(R.id.alarm_type);
        alarmTimeTv = (TextView) popView.findViewById(R.id.alarm_time);
        addressTv = (TextView) popView.findViewById(R.id.alarm_address);
        alarmSpeedTV = (TextView) popView.findViewById(R.id.alarm_speed);
        speedLL = (LinearLayout) popView.findViewById(R.id.alarm_speed_ll);
        lineShowView = popView.findViewById(R.id.line_show2);

        deviceNameTv.setText(mAlarm.getDev_name());
        alarmTypeTv.setText(mAlarm.getAlarm_type());
        alarmTimeTv.setText(TimeUtil.getStandardTime(new Date(mAlarm.getAlarm_time() * 1000)));
        if (mAlarm.getAlarm_type() != null && mAlarm.getAlarm_type().equals("超速报警")) {
            speedLL.setVisibility(View.VISIBLE);
            lineShowView.setVisibility(View.VISIBLE);
            alarmSpeedTV.setText(mAlarm.getSpeed() + "km/h");
        } else {
            speedLL.setVisibility(View.GONE);
            lineShowView.setVisibility(View.GONE);
        }

        navMap = (ImageButton) findViewById(R.id.nav_map);
        navMap.setOnClickListener(this);

        zoomControlView = (ZoomControlView) findViewById(R.id.zoomControl);
    }

    @Override
    public void onClick(View v) {
        if (v == navMap) {
            if (flag_map == Constant.MAP_NORMAL) {
                mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
                flag_map = Constant.MAP_SATELLITE;
                navMap.setBackgroundResource(R.drawable.nav_more_map_press);
            } else {
                mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                flag_map = Constant.MAP_NORMAL;
                navMap.setBackgroundResource(R.drawable.nav_more_map_normal);
            }
        }
    }

    private int reverseTaskID = -1;
    private SparseArray<LatLng> latlngValue = new SparseArray<LatLng>();
    private int mFirstHostTaskId = -1;

    protected void reverseGeo() {
        if (mAlarm == null) {
            return;
        }
        String cacheAddress = DeviceManager.getInstance().getCachedAddress(mAlarm.getLat(), mAlarm.getLng());
        if (StringUtil.isTrimEmpty(cacheAddress)) {
            if (DomainManager.sRespDomainAdd.timestamp == 0 || StringUtil.isTrimEmpty(DomainManager.sRespDomainAdd.domainMain)) {
                mFirstHostTaskId = mServiceAdapter.readFirstHost(this.hashCode(), Constant.DOMAIN_ADDRESS_FIRST);//Constant.firstUrl
                return;
            }
            reverseTaskID = mServiceAdapter.reverseGeo(this.hashCode(), AllOnlineApp.sToken.access_token, mAlarm.getLng(), mAlarm.getLat(), AllOnlineApp.sAccount,
                    SettingDataManager.strMapType);
            latlngValue.put(reverseTaskID, new LatLng(mAlarm.getLat(), mAlarm.getLng()));
        } else {
            addressTv.setText(cacheAddress);
            if (mAlarm == null) {
                return;
            }
            if (mAlarmMarker != null) {
                mAlarmMarker.showInfoWindow();
            }
        }
    }

    @Override
    public void callback(int messageId, Result result) {
        if (result.statusCode == Result.ERROR_NETWORK) {
            Toast.makeText(this, getString(R.string.network_error), Toast.LENGTH_SHORT).show();
            return;
        }
        if (result.statusCode == Result.OK) {
            if (result.apiCode == Constant.FM_APIID_REVERSE_GEO && reverseTaskID == messageId) {
                String address = result.mResult.toString();
                LatLng ll = latlngValue.get(reverseTaskID);
                if (null != address) {
                    DeviceManager.getInstance().setCachedAddress(ll.longitude, ll.latitude, address);
                    latlngValue.remove(reverseTaskID);
                }
                addressTv.setText(address);
                mAlarmMarker.showInfoWindow();
            } else if (result.apiCode == Constant.FM_APIID_FIRST_HOST && mFirstHostTaskId == messageId) {
                RespDomainAdd mAdd = (RespDomainAdd) result.mResult;
                if (!StringUtil.isTrimEmpty(mAdd.domainMain) && (DomainManager.sRespDomainAdd.timestamp == 0 || StringUtil
                    .isTrimEmpty(DomainManager.sRespDomainAdd.domainMain))) {
                    DomainManager.sRespDomainAdd.domainMain = mAdd.domainMain;
                    DomainManager.sRespDomainAdd.timestamp = mAdd.timestamp;
                    DomainManager.sRespDomainAdd.httpsFlag = mAdd.httpsFlag;
                    DomainManager.sRespDomainAdd = mAdd;
                    reverseGeo();
                }
            }
        }

    }

    @Override
    protected void onDestroy() {
        if (mServiceAdapter != null) {
            mServiceAdapter.unregisterServiceCallBack(this);
        }
        super.onDestroy();
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        if (marker.isInfoWindowShown()) {
            marker.hideInfoWindow();
        } else {
            marker.showInfoWindow();
        }
        return false;
    }

    @Override
    public void startActivityForResult(Intent intent, int requestCode) {
        try {
            // 当手机不包含谷歌服务框架时，点击谷歌地图内的确认按钮， 会抛出NullPointException异常
            super.startActivityForResult(intent, requestCode);
        } catch (Exception e) {
            // fixes Google Maps bug: http://stackoverflow.com/a/20905954/2075875
        }
    }
}
