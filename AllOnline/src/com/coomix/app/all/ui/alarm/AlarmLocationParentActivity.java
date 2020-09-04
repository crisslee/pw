package com.coomix.app.all.ui.alarm;

import android.graphics.Point;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.coomix.app.all.AllOnlineApp;
import com.coomix.app.all.Constant;
import com.coomix.app.all.GlobalParam;
import com.coomix.app.all.R;
import com.coomix.app.all.data.BaseSubscriber;
import com.coomix.app.all.data.DataEngine;
import com.coomix.app.all.data.ExceptionHandle;
import com.coomix.app.all.data.RxUtils;
import com.coomix.app.all.manager.DeviceManager;
import com.coomix.app.all.manager.SettingDataManager;
import com.coomix.app.all.model.bean.Alarm;
import com.coomix.app.all.model.bean.AreaFence;
import com.coomix.app.all.model.bean.Fence;
import com.coomix.app.all.model.response.RespAreaFence;
import com.coomix.app.all.model.response.RespQueryFence;
import com.coomix.app.all.ui.base.BaseActivity;
import com.coomix.app.all.util.StringUtil;
import com.coomix.app.all.widget.MyActionbar;
import com.coomix.app.framework.util.TimeUtil;
import io.reactivex.disposables.Disposable;
import java.util.Date;

public abstract class AlarmLocationParentActivity extends BaseActivity implements OnClickListener {
    public static final String ALARM_DATA = "ALARM";
    private ImageButton navMap;
    private LinearLayout alarmInfoLayout;
    protected TextView deviceNameTv, alarmTimeTitleTv, alarmTimeTv, alarmTypeTv, addressTitleTv, addressTv, alarmSpeedTV, alarmAreaTV;
    protected TextView gotoMonitorTv, seperatorTv, popDeviceNameTv;
    private LinearLayout speedLL, areaLL;
    protected View popView;
    protected Alarm mAlarm;
    private int flag_map = Constant.MAP_NORMAL;
    protected RelativeLayout layoutMapView;
    private SparseArray<Alarm> listIdsAlarms = new SparseArray<Alarm>();
    protected Fence mFence;
    protected AreaFence mAreaFence;
    protected Point leftTop, leftBottom, rightTop, rightBottom;
    //地图中显示弹窗或marker距离边框的距离
    protected final int BOUND_DISTANCE = 50;

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.activity_map_alarm_location);

        mAlarm = (Alarm) getIntent().getSerializableExtra(ALARM_DATA);

        initView();
    }

    protected void initView() {
        MyActionbar actionbar = (MyActionbar) findViewById(R.id.myActionbar);
        actionbar.initActionbar(true, "", 0, 0);

        layoutMapView = (RelativeLayout) findViewById(R.id.layoutAlarmMapView);
        navMap = (ImageButton) findViewById(R.id.nav_map);
        navMap.setOnClickListener(this);

        if (mAlarm != null) {
            actionbar.setTitle(mAlarm.getAlarm_type());
        } else {
            actionbar.setTitle(R.string.alarm);
        }

        alarmInfoLayout = (LinearLayout) findViewById(R.id.layoutBottomAlarmInfo);
        deviceNameTv = (TextView) findViewById(R.id.device_name);
        alarmTimeTitleTv = (TextView) findViewById(R.id.alarmTimeTitle);
        alarmTimeTv = (TextView) findViewById(R.id.alarm_time);
        addressTitleTv = (TextView) findViewById(R.id.addressTitle);
        addressTv = (TextView) findViewById(R.id.alarm_address);
        alarmSpeedTV = (TextView) findViewById(R.id.alarm_speed);
        alarmAreaTV = (TextView) findViewById(R.id.alarm_area);
        speedLL = (LinearLayout) findViewById(R.id.alarm_speed_ll);
        areaLL = (LinearLayout) findViewById(R.id.alarm_area_ll);

        if (mAlarm != null) {
            if (TextUtils.isEmpty(mAlarm.getDev_name())) {
                deviceNameTv.setText(R.string.car_name_empty);
            } else {
                deviceNameTv.setText(mAlarm.getDev_name());
            }
            alarmTimeTv.setText(TimeUtil.getStandardTime(new Date(mAlarm.getAlarm_time() * 1000)));
            if (mAlarm.getAlarm_type() != null) {
                if (mAlarm.getAlarm_type().compareToIgnoreCase(getString(R.string.alarm_overspeed)) == 0) {
                    speedLL.setVisibility(View.VISIBLE);
                    alarmSpeedTV.setText(mAlarm.getSpeed() + "km/h");
                } else {
                    speedLL.setVisibility(View.GONE);
                }

                if (mAlarm.getAlarm_type().compareToIgnoreCase(getString(R.string.alarm_geoareafence)) == 0) {
                    areaLL.setVisibility(View.VISIBLE);
                } else {
                    areaLL.setVisibility(View.GONE);
                }
            }
        }
        navMap.setBackgroundResource(SettingDataManager.getInstance(AlarmLocationParentActivity.this).getMapTypeNormalDrawableId());

        setAddressText();
    }

    protected void initPopView() {
        popView = LayoutInflater.from(this).inflate(R.layout.alarm_popup_layout, null);
        popDeviceNameTv = (TextView) popView.findViewById(R.id.car_name);
        seperatorTv = (TextView) popView.findViewById(R.id.seperator);
        gotoMonitorTv = (TextView) popView.findViewById(R.id.gotoMonitor);

        if (mAlarm != null) {
            if (TextUtils.isEmpty(mAlarm.getDev_name())) {
                popDeviceNameTv.setText(R.string.car_name_empty);
            } else {
                popDeviceNameTv.setText(mAlarm.getDev_name());
            }
        }

        seperatorTv.setVisibility(View.VISIBLE);
        gotoMonitorTv.setVisibility(View.VISIBLE);
        gotoMonitorTv.setOnClickListener(view -> gotoMonitor());
    }

    protected void initNetData() {
        showInfoWindow();
        if (mAlarm != null && mAlarm.getAlarm_type() != null) {
            if (mAlarm.getAlarm_type().compareToIgnoreCase(getString(R.string.alarm_geofence)) == 0 ||//出围栏报警
                    mAlarm.getAlarm_type().compareToIgnoreCase(getString(R.string.alarm_geofencein)) == 0)//进围栏报警
            {
                queryFence();
            } else if (mAlarm.getAlarm_type().compareToIgnoreCase(getString(R.string.alarm_geoareafence)) == 0)//出省市报警
            {
                getAreaFence();
            }
        }
    }

    protected void setAddressText() {
//        if (isDisable)
//        {
//            alarmTimeTv.setVisibility(View.GONE);
//            addressTv.setVisibility(View.GONE);
//            alarmTimeTitleTv.setText(R.string.device_is_expire);
//            addressTv.setText(getExpireDes());
//            return;
//        }

        if (mAlarm == null) {
            return;
        }
        String cacheAddress = null;
        if (!TextUtils.isEmpty(mAlarm.getAddress())) {
            addressTv.setText(mAlarm.getAddress());
        } else if (!TextUtils.isEmpty(DeviceManager.getInstance().getCachedAddress(mAlarm.getLat(), mAlarm.getLng()))) {
            addressTv.setText(DeviceManager.getInstance().getCachedAddress(mAlarm.getLat(), mAlarm.getLng()));
        } else {
            addressTv.setText(R.string.empty_addr);
        }
    }

    private void gotoMonitor() {
        if(mAlarm == null){
            return;
        }
        SettingDataManager.getInstance(this).goToMainByMap(this, mAlarm.getImei());
        finish();
    }

    protected void getLatLngOfPopupView() {
        if (popView == null || !popView.isShown()) {
            return;
        }
        int[] location = new int[2];

        int topBarHeight = 0;
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            topBarHeight = getResources().getDimensionPixelSize(resourceId);
        }
        popView.getLocationOnScreen(location);
        leftTop = new Point(location[0] - 600, location[1] - topBarHeight - 100);
        leftBottom = new Point(location[0] - 600, location[1] + popView.getHeight() - topBarHeight - 100);
        rightTop = new Point(location[0] + popView.getWidth() + 600, location[1] - topBarHeight - 100);
        rightBottom = new Point(location[0] + popView.getWidth() + 600, location[1] + popView.getHeight() - topBarHeight - 100);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.nav_map:
                if (flag_map == Constant.MAP_NORMAL) {
                    flag_map = Constant.MAP_SATELLITE;
                    navMap.setBackgroundResource(SettingDataManager.getInstance(AlarmLocationParentActivity.this).getMapTypeSatDrawableId());
                    changeMapMode(flag_map);
                } else {
                    flag_map = Constant.MAP_NORMAL;
                    navMap.setBackgroundResource(SettingDataManager.getInstance(AlarmLocationParentActivity.this).getMapTypeNormalDrawableId());
                    changeMapMode(flag_map);
                }
                break;

//            case R.id.right_tv:
//                Intent intent = new Intent(mContext, DeviceSettingActivity.class);
//                intent.putExtra(Constant.KEY_DEVICE, device);
//                startActivity(intent);
//                break;

            default:
                break;
        }
    }

    private void queryFence() {
        if (mAlarm == null || TextUtils.isEmpty(mAlarm.getImei())) {
            return;
        }
        showLoading(getString(R.string.please_wait));
        Disposable d = DataEngine.getAllMainApi().queryFence(
                GlobalParam.getInstance().getCommonParas(),
            GlobalParam.getInstance().getAccessToken(),
                AllOnlineApp.sAccount, mAlarm.getImei(), mAlarm.getId())
                .compose(RxUtils.toMain())
                .compose(RxUtils.businessTransformer())
                .subscribeWith(new BaseSubscriber<RespQueryFence>() {
                    @Override
                    public void onHttpError(ExceptionHandle.ResponeThrowable e) {
                        showErr(e.getErrCodeMessage());
                        hideLoading();
                    }

                    @Override
                    public void onNext(RespQueryFence respQueryFence) {
                        mFence = respQueryFence.getData();
                        initFence();
                        hideLoading();
                    }
                });
        subscribeRx(d);
    }

    private void getAreaFence() {
        if (mAlarm == null || StringUtil.isTrimEmpty(mAlarm.getImei())) {
            return;
        }
        showLoading(getString(R.string.please_wait));
        Disposable d = DataEngine.getAllMainApi().getAreaFence(
                GlobalParam.getInstance().getCommonParas(),
            GlobalParam.getInstance().getAccessToken(),
                AllOnlineApp.sAccount, mAlarm.getImei(), mAlarm.getId())
                .compose(RxUtils.toMain())
                .compose(RxUtils.businessTransformer())
                .subscribeWith(new BaseSubscriber<RespAreaFence>() {
                    @Override
                    public void onHttpError(ExceptionHandle.ResponeThrowable e) {
                        showErr(e.getErrCodeMessage());
                        hideLoading();
                    }

                    @Override
                    public void onNext(RespAreaFence respAreaFence) {
                        mAreaFence = respAreaFence.getData();
                        updateAreaFenceView();
                        hideLoading();
                    }
                });
        subscribeRx(d);
    }

    private void updateAreaFenceView() {
        String address = "";
        if (!TextUtils.isEmpty(mAreaFence.getShape_param().province)) {
            address += mAreaFence.getShape_param().province;
        }

        if (!TextUtils.isEmpty(mAreaFence.getShape_param().city)) {
            address += mAreaFence.getShape_param().city;
        }

        if (!TextUtils.isEmpty(mAreaFence.getShape_param().district)) {
            address += mAreaFence.getShape_param().district;
        }
        alarmAreaTV.setText(getString(R.string.alarm_geoareafence_prefix) + address);
    }

    protected abstract void initFence();

    protected abstract void showInfoWindow();

    protected abstract void changeMapMode(int iMapType);

}
