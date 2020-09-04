package com.coomix.app.all.ui.advance;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;
import com.coomix.app.all.AllOnlineApp;
import com.coomix.app.all.Constant;
import com.coomix.app.all.GlobalParam;
import com.coomix.app.all.R;
import com.coomix.app.all.model.bean.AreaFence;
import com.coomix.app.all.model.bean.DevMode;
import com.coomix.app.all.model.bean.DeviceInfo;
import com.coomix.app.all.model.bean.DeviceSetting;
import com.coomix.app.all.model.bean.Fence;
import com.coomix.app.all.model.bean.Overspeed;
import com.coomix.app.all.widget.MyActionbar;
import com.coomix.app.framework.util.CommonUtil;
import com.coomix.app.framework.util.TimeUtil;
import com.coomix.app.all.data.BaseSubscriber;
import com.coomix.app.all.data.DataEngine;
import com.coomix.app.all.data.ExceptionHandle;
import com.coomix.app.all.data.RxUtils;
import com.coomix.app.all.model.response.RespBase;
import com.coomix.app.all.model.response.RespDeviceSetting;
import com.coomix.app.all.model.response.RespMode;
import com.coomix.app.all.ui.base.BaseActivity;
import com.coomix.app.all.ui.devTime.DevTimeActivity;
import com.coomix.app.all.manager.SettingDataManager;
import io.reactivex.disposables.Disposable;

@SuppressLint("NewApi")
public class DeviceSettingActivity extends BaseActivity {
    public static final int REQUEST_CODE_AREA_SELECT = 10;
    public static final String AREA_FENCE = "areaFence";
    public static final String IS_FROM_ITEM = "isFromItem";
    public static final String NEW_FENCE = "newFence";

    private View fenceSettingView, lineFence;
    private ToggleButton fenceBtn, overspeedBtn, outAreaBtn;
    private LinearLayout overspeedLayout;
    private RelativeLayout fenceLayout, outAreaLayout, devTimeLayout;
    private TextView subTv_overspeed, subTv_outArea, textFenceSet, titleMode, nextTime;
    public static DeviceInfo device;
    private Fence fence;

    private final int defaultMaxSpeed = 120;

    private Overspeed mOverspeed;
    private AreaFence mAreaFence;
    private DevMode mode;
    public static boolean needRefresh;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_device_command);

        if (getIntent() != null && getIntent().hasExtra(Constant.KEY_DEVICE)) {
            device = (DeviceInfo) getIntent().getSerializableExtra(Constant.KEY_DEVICE);
        }
        if (device == null) {
            finish();
            return;
        }

        initViews();

        queryDeviceSetting();
        queryDevMode();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (needRefresh) {
            needRefresh = false;
            queryDevMode();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    private void initViews() {
        MyActionbar actionbar = (MyActionbar) findViewById(R.id.myActionbar);
        actionbar.initActionbar(true, R.string.senior, 0, 0);

        lineFence = findViewById(R.id.lineFence);
        fenceSettingView = findViewById(R.id.fenceSettingView);
        textFenceSet = (TextView) findViewById(R.id.fenceSetting);
        fenceBtn = (ToggleButton) findViewById(R.id.toggle);
        fenceLayout = (RelativeLayout) findViewById(R.id.fence_layout);

        findViewById(R.id.textViewCommand).setOnClickListener(view -> {
            if (device == null) {
                return;
            }
            Intent intent = new Intent(this, CommandListWebActivity.class);
            intent.putExtra(Constant.KEY_DEVICE, device);
            startActivity(intent);
        });

        if (fenceBtn.isChecked()) {
            fenceSettingView.setVisibility(View.VISIBLE);
            lineFence.setVisibility(View.VISIBLE);
        } else {
            fenceSettingView.setVisibility(View.GONE);
            lineFence.setVisibility(View.GONE);
        }

        // 超速报警
        subTv_overspeed = (TextView) findViewById(R.id.subtv_overspeed);
        overspeedLayout = (LinearLayout) findViewById(R.id.ll_overspeed);
        overspeedBtn = (ToggleButton) findViewById(R.id.toggleOverspeed);
        overspeedBtn.setOnClickListener(view1 -> {
            if (overspeedBtn.isChecked()) {
                inputOverspeedDialog(LayoutInflater.from(DeviceSettingActivity.this), false);
            } else {
                if (mOverspeed == null) {
                    return;
                }
                setOverspeed(0, mOverspeed.speed);
            }
        });
        overspeedLayout.setOnClickListener(view1 -> {
            inputOverspeedDialog(LayoutInflater.from(DeviceSettingActivity.this), true);
        });

        // 出区域报警
        subTv_outArea = (TextView) findViewById(R.id.subtv_out_area);
        outAreaLayout = (RelativeLayout) findViewById(R.id.ll_out_area);
        outAreaBtn = (ToggleButton) findViewById(R.id.toggleOutArea);

        if (CommonUtil.isCN()) {
            outAreaLayout.setVisibility(View.VISIBLE);
        } else {
            outAreaLayout.setVisibility(View.GONE);
        }

        outAreaBtn.setOnClickListener(view1 -> {
            if (outAreaBtn.isChecked()) {
                Intent intent = new Intent();
                intent.setClass(DeviceSettingActivity.this, AreaSelectActivity.class);
                intent.putExtra(Constant.KEY_DEVICE, device);
                intent.putExtra(IS_FROM_ITEM, false);
                intent.putExtra(AREA_FENCE, mAreaFence);
                startActivityForResult(intent, REQUEST_CODE_AREA_SELECT);
            } else {
                if (mAreaFence != null) {
                    setAreaFence(mAreaFence.id, 0);
                }
            }
        });
        outAreaLayout.setOnClickListener(view1 -> {
            Intent intent = new Intent();
            intent.setClass(DeviceSettingActivity.this, AreaSelectActivity.class);
            intent.putExtra(Constant.KEY_DEVICE, device);
            intent.putExtra(IS_FROM_ITEM, true);
            intent.putExtra(AREA_FENCE, mAreaFence);
            startActivityForResult(intent, REQUEST_CODE_AREA_SELECT);
        });

        //电子围栏
        textFenceSet.setOnClickListener(view1 -> goToSetFenceAcrivity());
        fenceBtn.setOnClickListener(view1 -> {
            if (fenceBtn.isChecked()) {
                switchFence(Fence.SWITCH_ON);
            } else {
                switchFence(Fence.SWITCH_OFF);
            }
        });
        fenceLayout.setOnClickListener(view1 -> {
            if (fenceBtn.isChecked()) {
                switchFence(Fence.SWITCH_OFF);
            } else {
                switchFence(Fence.SWITCH_ON);
            }
        });

        //设备上传时间
        devTimeLayout = (RelativeLayout) findViewById(R.id.devTime);
        titleMode = (TextView) findViewById(R.id.titleMode);
        nextTime = (TextView) findViewById(R.id.nextTime);
        devTimeLayout.setOnClickListener(v -> {
            Intent i = new Intent(this, DevTimeActivity.class);
            if (mode == null) {
                return;
            }
            Bundle b = new Bundle();
            b.putSerializable(DevTimeActivity.DEV_MODE, mode);
            i.putExtras(b);
            startActivity(i);
        });
    }

    private void setOverspeed(final int overFlag, final int speed) {
        showProgressDialog(getString(R.string.please_wait));
        Disposable disposable = DataEngine.getAllMainApi()
                .setOverspeed(
                        GlobalParam.getInstance().getCommonParas(),
                        AllOnlineApp.sAccount,
                        GlobalParam.getInstance().getAccessToken(),
                        device.getImei(),
                        overFlag, speed)
                .compose(RxUtils.toMain())
                .compose(RxUtils.businessTransformer())
                .subscribeWith(new BaseSubscriber<RespBase>() {
                    @Override
                    public void onNext(RespBase respBase) {
                        hideLoading();
                        if (overFlag == 1) {
                            overspeedBtn.setChecked(true);
                            subTv_overspeed.setVisibility(View.VISIBLE);
                            subTv_overspeed.setText(speed + "km/h");

                        } else {
                            overspeedBtn.setChecked(false);
                            subTv_overspeed.setVisibility(View.INVISIBLE);
                        }
                        if(mOverspeed == null){
                            mOverspeed = new Overspeed();
                        }
                        mOverspeed.overspeed_flag = overFlag;
                        mOverspeed.speed = speed;
                    }

                    @Override
                    public void onHttpError(ExceptionHandle.ResponeThrowable e) {
                        showToast(e.getErrCodeMessage());
                        hideLoading();
                    }
                });
        subscribeRx(disposable);
    }

    private void setAreaFence(String id, final int flag) {
        showProgressDialog(getString(R.string.please_wait));
        Disposable disposable = DataEngine.getAllMainApi()
                .switchAreaFence(
                        GlobalParam.getInstance().getCommonParas(),
                        AllOnlineApp.sAccount,
                        GlobalParam.getInstance().getAccessToken(),
                        device.getImei(),
                        id, flag)
                .compose(RxUtils.toMain())
                .compose(RxUtils.businessTransformer())
                .subscribeWith(new BaseSubscriber<RespBase>() {
                    @Override
                    public void onNext(RespBase respBase) {
                        hideLoading();
                        if (flag == 0) {
                            //关闭成功。该界面只有关闭功能。打开需要在另外的界面选择省市信息
                            outAreaBtn.setChecked(false);
                            mAreaFence.validate_flag = 0;
                            subTv_outArea.setVisibility(View.GONE);
                        }
                    }

                    @Override
                    public void onHttpError(ExceptionHandle.ResponeThrowable e) {
                        showToast(e.getErrCodeMessage());
                        hideLoading();
                    }
                });
        subscribeRx(disposable);
    }

    private void inputOverspeedDialog(LayoutInflater inflater, final boolean isFromItem) {
        View layout = inflater.inflate(R.layout.dialog_overspeed, null);
        final EditText maxSpeedEt = (EditText) layout.findViewById(R.id.et_max_speed);
        String speed;
        if (mOverspeed == null) {
            speed = String.valueOf(defaultMaxSpeed);
        } else {
            speed = String.valueOf(mOverspeed.speed);
        }
        maxSpeedEt.setText(speed);
        maxSpeedEt.setSelection(speed.length());

        final AlertDialog dlgsDate = new AlertDialog.Builder(DeviceSettingActivity.this).setInverseBackgroundForced(true)
                .setTitle(R.string.overspeed_setting).setView(layout)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String maxSpeedString = maxSpeedEt.getText().toString().trim();
                        if (TextUtils.isEmpty(maxSpeedString)) {
                            showToast(getString(R.string.cmd_check_input));
                            if (!isFromItem) {
                                overspeedBtn.setChecked(false);
                            }
                        } else {
                            int maxSpeed = Integer.valueOf(maxSpeedString);
                            if (maxSpeed < 20) {
                                showToast(getString(R.string.cmd_check_low));
                                return;
                            }
                            setOverspeed(1, maxSpeed);
                        }
                    }

                }).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (!isFromItem) {
                            overspeedBtn.setChecked(false);
                        }
                    }
                }).create();
        dlgsDate.show();
    }

    private void queryDeviceSetting() {
        showProgressDialog(getString(R.string.please_wait));
        Disposable disposable = DataEngine.getAllMainApi()
                .queryDeviceSetting(
                        GlobalParam.getInstance().getCommonParas(),
                        AllOnlineApp.sAccount,
                        GlobalParam.getInstance().getAccessToken(),
                        device.getImei())
                .compose(RxUtils.toMain())
                .compose(RxUtils.businessTransformer())
                .subscribeWith(new BaseSubscriber<RespDeviceSetting>() {
                    @Override
                    public void onNext(RespDeviceSetting respDeviceSetting) {
                        hideLoading();
                        DeviceSetting deviceSetting = respDeviceSetting.getData();
                        if (deviceSetting != null) {
                            mAreaFence = deviceSetting.getArea();
                            if (mAreaFence != null && mAreaFence.validate_flag == 1) {
                                outAreaBtn.setChecked(true);
                                subTv_outArea.setVisibility(View.VISIBLE);
                                if (!TextUtils.isEmpty(mAreaFence.getShape_param().city)) {
                                    subTv_outArea.setText(mAreaFence.getShape_param().city);
                                } else if (!TextUtils.isEmpty(mAreaFence.getShape_param().province)) {
                                    subTv_outArea.setText(mAreaFence.getShape_param().province);
                                } else {
                                    subTv_outArea.setText("");
                                }
                            } else {
                                subTv_outArea.setVisibility(View.GONE);
                                outAreaBtn.setChecked(false);
                            }

                            mOverspeed = deviceSetting.getOverspeed();
                            if (mOverspeed != null && mOverspeed.overspeed_flag == 1) {
                                overspeedBtn.setChecked(true);

                                subTv_overspeed.setVisibility(View.VISIBLE);
                                subTv_overspeed.setText(mOverspeed.speed + "km/h");
                            } else {
                                overspeedBtn.setChecked(false);
                                subTv_overspeed.setVisibility(View.GONE);
                            }

                            fence = deviceSetting.getEfence();
                            if (fence != null) {
                                if (fence.getValidate_flag() == Fence.SWITCH_ON) {
                                    // 打开电子围栏
                                    fenceBtn.setChecked(true);
                                    fenceSettingView.setVisibility(View.VISIBLE);
                                    lineFence.setVisibility(View.VISIBLE);
                                } else if (fence.getValidate_flag() == Fence.SWITCH_OFF) {
                                    // 关闭电子围栏
                                    fenceBtn.setChecked(false);
                                    fenceSettingView.setVisibility(View.GONE);
                                    lineFence.setVisibility(View.GONE);
                                }
                            }
                        } else {
                            mAreaFence = null;
                            outAreaBtn.setChecked(false);
                            mOverspeed = null;
                            overspeedBtn.setChecked(false);
                            fence = null;
                            fenceBtn.setChecked(false);
                        }
                    }

                    @Override
                    public void onHttpError(ExceptionHandle.ResponeThrowable e) {
                        showToast(e.getErrCodeMessage());
                        hideLoading();
                    }
                });
        subscribeRx(disposable);
    }

    private void goToSetFenceAcrivity() {
        if(device == null){
            return;
        }
        int iMapType = SettingDataManager.getInstance(DeviceSettingActivity.this).getMapTypeInt();
//        if (iMapType == Constant.MAP_TYPE_GOOGLE) {
//            Intent intent = new Intent(DeviceSettingActivity.this, GFenceActivity.class);
//            intent.putExtra(SetFenceParentActivity.FENCE_DATA, fence);
//            intent.putExtra(Constant.KEY_DEVICE, device);
//            startActivityForResult(intent, Constant.SET_FENCE);
//        } else
        if (iMapType == Constant.MAP_TYPE_AMAP) {
            Intent intent = new Intent(DeviceSettingActivity.this, AMapFenceActivity.class);
            intent.putExtra(SetFenceParentActivity.FENCE_DATA, fence);
            intent.putExtra(Constant.KEY_DEVICE, device);
            startActivityForResult(intent, Constant.SET_FENCE);
        } else if (iMapType == Constant.MAP_TYPE_TENCENT) {
            Intent intent = new Intent(DeviceSettingActivity.this, TFenceActivity.class);
            intent.putExtra(SetFenceParentActivity.FENCE_DATA, fence);
            intent.putExtra(Constant.KEY_DEVICE, device);
            startActivityForResult(intent, Constant.SET_FENCE);
        } else {
            Intent intent = new Intent(DeviceSettingActivity.this, BFenceActivity.class);
            intent.putExtra(SetFenceParentActivity.FENCE_DATA, fence);
            intent.putExtra(Constant.KEY_DEVICE, device);
            startActivityForResult(intent, Constant.SET_FENCE);
        }
    }

    private void switchFence(final int switchType) {
        if(fence == null){
            return;
        }
        showProgressDialog(getString(R.string.please_wait));
        Disposable disposable = DataEngine.getAllMainApi()
                .switchFence(GlobalParam.getInstance().getCommonParas(),
                        AllOnlineApp.sAccount,
                        GlobalParam.getInstance().getAccessToken(),
                        device.getImei(), TextUtils.isEmpty(fence.getId()) ? "0" : fence.getId(), switchType,
                        Fence.ALARM_TYPE_NORMAL)
                .compose(RxUtils.toMain())
                .compose(RxUtils.businessTransformer())
                .subscribeWith(new BaseSubscriber<RespBase>() {
                    @Override
                    public void onNext(RespBase respBase) {
                        hideLoading();
                        if (switchType == Fence.SWITCH_OFF) {
                            // 关闭
                            fenceBtn.setChecked(false);
                            fenceSettingView.setVisibility(View.GONE);
                            lineFence.setVisibility(View.GONE);
                            if (fence != null) {
                                fence.setValidate_flag(Fence.SWITCH_OFF);
                            }
                            Toast.makeText(DeviceSettingActivity.this, DeviceSettingActivity.this.getString(R.string.switch_fence_off_toast),
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            fenceBtn.setChecked(true);
                            fenceSettingView.setVisibility(View.VISIBLE);
                            lineFence.setVisibility(View.VISIBLE);
                            if (fence != null) {
                                fence.setValidate_flag(Fence.SWITCH_ON);
                            }
                            Toast.makeText(DeviceSettingActivity.this, DeviceSettingActivity.this.getString(R.string.switch_fence_on_toast),
                                    Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onHttpError(ExceptionHandle.ResponeThrowable e) {
                        showToast(e.getErrCodeMessage());
                        hideLoading();
                    }
                });
        subscribeRx(disposable);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_AREA_SELECT && data != null) {
            if (resultCode == RESULT_OK) {
                AreaFence areaFence = (AreaFence) data.getSerializableExtra(AREA_FENCE);
                if (areaFence != null && !TextUtils.isEmpty(areaFence.id)) {
                    mAreaFence = areaFence;

                    subTv_outArea.setVisibility(View.VISIBLE);
                    if (!TextUtils.isEmpty(mAreaFence.getShape_param().city)) {
                        subTv_outArea.setText(mAreaFence.getShape_param().city);
                    } else if (!TextUtils.isEmpty(mAreaFence.getShape_param().province)) {
                        subTv_outArea.setText(mAreaFence.getShape_param().province);
                    } else {
                        subTv_outArea.setText("");
                    }
                }
                outAreaBtn.setChecked(true);
            } else {
                boolean isFromItem = data.getBooleanExtra(IS_FROM_ITEM, true);
                if (!isFromItem) {
                    outAreaBtn.setChecked(false);
                }
            }
        } else if (requestCode == Constant.SET_FENCE && resultCode == Activity.RESULT_OK && data != null) {
            Fence newFence = (Fence) data.getSerializableExtra(NEW_FENCE);
            if (newFence != null) {
                fence = newFence;
            }
        }
    }

    public void queryDevMode() {
        String token = GlobalParam.getInstance().getAccessToken();
        Disposable d = DataEngine.getAllMainApi()
            .getDevMode(token, device.getImei(), GlobalParam.getInstance().getCommonParas())
            .compose(RxUtils.toMain())
            .compose(RxUtils.businessTransformer())
            .subscribeWith(new BaseSubscriber<RespMode>() {
                @Override
                public void onHttpError(ExceptionHandle.ResponeThrowable e) {
                    devTimeLayout.setVisibility(View.GONE);
                }

                @Override
                public void onNext(RespMode respMode) {
                    if (respMode != null && respMode.getData() != null) {
                        mode = respMode.getData();
                        processMode(respMode.getData());
                    }
                }
            });
        subscribeRx(d);
    }

    private void processMode(DevMode mode) {
        switch (mode.mode) {
            case DevMode.MODE_ALARM:
                titleMode.setText(R.string.title_mode_alarm);
                break;
            case DevMode.MODE_TRACK:
                titleMode.setText(R.string.title_mode_track);
                break;
            case DevMode.MODE_WEEK:
                titleMode.setText(R.string.title_mode_week);
                break;
            case DevMode.MODE_SCHEDULE:
                if (mode.sub_mode == DevMode.SUB_MODE_LOOP) {
                    titleMode.setText(R.string.title_mode_loop);
                } else if (mode.sub_mode == DevMode.SUB_MODE_CUSTOM) {
                    titleMode.setText(R.string.title_mode_custom);
                }
                break;
            default:
                break;
        }
        nextTime.setText(getString(R.string.next_online_time,
            TimeUtil.long2MinuteDate(mode.next_online_utc * 1000)));
    }
}
