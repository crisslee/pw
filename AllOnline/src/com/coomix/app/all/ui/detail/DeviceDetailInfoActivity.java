package com.coomix.app.all.ui.detail;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.coomix.app.all.AllOnlineApp;
import com.coomix.app.all.Constant;
import com.coomix.app.all.GlobalParam;
import com.coomix.app.all.R;
import com.coomix.app.all.data.BaseSubscriber;
import com.coomix.app.all.data.DataEngine;
import com.coomix.app.all.data.ExceptionHandle;
import com.coomix.app.all.data.RxUtils;
import com.coomix.app.all.model.bean.DevPassInfo;
import com.coomix.app.all.model.bean.DeviceDetailInfo;
import com.coomix.app.all.model.bean.DeviceInfo;
import com.coomix.app.all.model.response.RespDevPassInfo;
import com.coomix.app.all.model.response.RespDeviceDetailInfo;
import com.coomix.app.all.model.response.RespServiceProvider;
import com.coomix.app.all.share.PopupWindowUtil;
import com.coomix.app.all.share.TextSet;
import com.coomix.app.all.ui.alarm.AlarmCategoryListActivity;
import com.coomix.app.all.ui.base.BaseActivity;
import com.coomix.app.all.ui.cardRecharge.CardSingleRechargeActivity;
import com.coomix.app.all.util.DisplayUtils;
import com.coomix.app.all.util.PermissionUtil;
import com.coomix.app.all.widget.MyActionbar;
import com.coomix.app.framework.util.CommonUtil;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.umeng.analytics.MobclickAgent;
import io.reactivex.disposables.Disposable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@SuppressLint("NewApi")
public class DeviceDetailInfoActivity extends BaseActivity implements OnClickListener {
    public static final String DEV_IMEI = "dev_imei";
    public static final String DEV_ANGLE = "dev_angle";
    public static boolean bUpdate = false;
    private DeviceDetailInfo device;
    //private String textAddress;
    private View mainView;
    private TextView textCardNum;
    private TextView textDevName, textImei, textExtra, textPhone, textLastConn, textDevType, textStartTime, textEndTime,
        textDevContact, textContactPhone, textExpiredInfo, textGpsTime;
    private String imei ="";
    private MyActionbar actionbar;
    private LinearLayout llCardRecharge, llPassInfo;
    private View divCardRecharge;
    //卡终身
    private LinearLayout llCardDueTo;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mainView = LayoutInflater.from(this).inflate(R.layout.activity_device_info, null);
        setContentView(mainView);

        int iAngle = 0;
        if (getIntent() != null) {
            if(getIntent().hasExtra(DEV_IMEI)) {
                imei = getIntent().getStringExtra(DEV_IMEI);
            }else{
                finish();
                return;
            }
            if(getIntent().hasExtra(DEV_ANGLE)){
                iAngle = getIntent().getIntExtra(DEV_ANGLE, 0);
            }
        } else {
            finish();
            return;
        }

        initView(iAngle);

        requestDevDetailInfo(imei);
        getPassInfo(imei);

        HashMap<String, String> map = new HashMap<String, String>();
        map.put("ev_function", "车辆信息设置");
        MobclickAgent.onEvent(DeviceDetailInfoActivity.this, "ev_function", map);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (bUpdate) {
            bUpdate = false;
            requestDevDetailInfo(imei);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    private void requestDevDetailInfo(String imei) {
        if (TextUtils.isEmpty(imei)) {
            finish();
            return;
        }
        showProgressDialog(getString(R.string.please_wait));
        Disposable disposable = DataEngine.getAllMainApi().getDevDetailInfo(
                GlobalParam.getInstance().getCommonParas(),
                AllOnlineApp.sAccount,
                GlobalParam.getInstance().getAccessToken(),
                imei)
                .compose(RxUtils.toMain())
                .compose(RxUtils.businessTransformer())
                .subscribeWith(new BaseSubscriber<RespDeviceDetailInfo>() {
                    @Override
                    public void onHttpError(ExceptionHandle.ResponeThrowable e) {
                        showErr(e.getErrCodeMessage());
                        hideLoading();
                    }

                    @Override
                    public void onNext(RespDeviceDetailInfo respDeviceDetailInfo) {
                        hideLoading();
                        if(respDeviceDetailInfo != null && respDeviceDetailInfo.getData() != null) {
                            device = respDeviceDetailInfo.getData();
                            setData();
                        }
                    }
                });
        subscribeRx(disposable);
    }

    private void getPassInfo(String imei) {
        if (TextUtils.isEmpty(imei)) {
            return;
        }
        String token = AllOnlineApp.sToken.access_token;
        Disposable d = DataEngine.getAllMainApi().getPassInfo(imei, token, GlobalParam.getInstance().getCommonParas())
            .compose(RxUtils.businessTransformer())
            .compose(RxUtils.toMain())
            .subscribeWith(new BaseSubscriber<RespDevPassInfo>() {
                @Override
                public void onHttpError(ExceptionHandle.ResponeThrowable e) {

                }

                @Override
                public void onNext(RespDevPassInfo respDevPassInfo) {
                    if (respDevPassInfo == null) {
                        return;
                    }
                    DevPassInfo info = respDevPassInfo.getData();
                    DeviceDetailInfoActivity.this.mainView.post(() -> {
                        processPassInfo(info);
                    });
                }
            });
        subscribeRx(d);
    }

    private void processPassInfo(DevPassInfo pass) {
        String info = pass.info;
        //info = "{\"TEMP\":{\"temperature\":25, \"signal\":20}}";
        String key = pass.key;
        if (TextUtils.isEmpty(info) || TextUtils.isEmpty(key)) {
            return;
        }
        Map<String, Map<String, String>> infos = new Gson().fromJson(info,
            new TypeToken<Map<String, Map<String, String>>>() {
            }.getType());
        Map<String, ArrayList<String>> keys = new Gson().fromJson(key, new TypeToken<Map<String, ArrayList<String>>>() {
        }.getType());

        for (Map<String, String> map : infos.values()) {
            for (Map.Entry<String, String> entry : map.entrySet()) {
                if (keys.containsKey(entry.getKey())) {
                    ArrayList<String> titles = keys.get(entry.getKey());
                    if (titles.size() > 1) {
                        addPassInfo(titles.get(0), entry.getValue() + titles.get(1));
                    } else {
                        addPassInfo(titles.get(0), entry.getValue());
                    }
                }
            }
        }
    }

    private void addPassInfo(String title, String text) {
        if (llPassInfo.getChildCount() > 0) {
            View line = new View(this);
            line.setBackgroundColor(Color.parseColor("#cdced2"));
            int h = DisplayUtils.dp2px(this, 0.5f);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, h);
            lp.leftMargin = DisplayUtils.dp2px(this, 10f);
            line.setLayoutParams(lp);
            llPassInfo.addView(line);
        }
        LinearLayout parent = new LinearLayout(this);
        int padding = DisplayUtils.dp2px(this, 10f);
        LinearLayout.LayoutParams lpParent = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT);
        parent.setLayoutParams(lpParent);
        parent.setOrientation(LinearLayout.HORIZONTAL);
        parent.setPadding(padding, padding, padding, padding);
        parent.setBackgroundColor(getResources().getColor(R.color.white_bg));

        TextView tvTitle = new TextView(this);
        tvTitle.setText(title);
        LinearLayout.LayoutParams lpTitle = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT);
        lpTitle.weight = 1;
        tvTitle.setLayoutParams(lpTitle);
        tvTitle.setTextColor(getResources().getColor(R.color.text_black));
        tvTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);

        TextView tvInfo = new TextView(this);
        tvInfo.setText(text);
        LinearLayout.LayoutParams lpInfo = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT);
        tvInfo.setLayoutParams(lpInfo);
        tvInfo.setMaxLines(1);
        tvInfo.setTextColor(getResources().getColor(R.color.black2_90));
        tvInfo.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);

        parent.addView(tvTitle);
        parent.addView(tvInfo);
        llPassInfo.addView(parent);
        llPassInfo.invalidate();
    }

    private void initView(int iAngle) {
        actionbar = (MyActionbar) findViewById(R.id.myActionbar);
        actionbar.initActionbar(true, "", 0, 0);

        textDevName = (TextView) findViewById(R.id.textViewDevName);
        textImei = (TextView) findViewById(R.id.textViewImei);
        textExtra = (TextView) findViewById(R.id.textViewExtra);
        textPhone = (TextView) findViewById(R.id.textViewCardNum);
        textLastConn = (TextView) findViewById(R.id.textViewLastConn);
        textDevType = (TextView) findViewById(R.id.textViewDevType);
        textStartTime = (TextView) findViewById(R.id.textViewOpenDate);
        textEndTime = (TextView) findViewById(R.id.textViewDueTo);
        llCardDueTo = (LinearLayout) findViewById(R.id.layoutCardDueTo);
        textDevContact = (TextView) findViewById(R.id.textViewDevContact);
        textContactPhone = (TextView) findViewById(R.id.textViewContactNum);
        textExpiredInfo = (TextView) findViewById(R.id.textViewExpired);
        textGpsTime = (TextView) findViewById(R.id.textViewLastGPS);

        findViewById(R.id.layoutDevName).setOnClickListener(this);
        findViewById(R.id.layoutImei).setOnClickListener(this);
        findViewById(R.id.layoutExtra).setOnClickListener(this);
        findViewById(R.id.layoutCardNum).setOnClickListener(this);
        findViewById(R.id.layoutTotalDistance).setOnClickListener(this);
        findViewById(R.id.layoutWarning).setOnClickListener(this);
        findViewById(R.id.layoutDevContact).setOnClickListener(this);
        findViewById(R.id.layoutContactNum).setOnClickListener(this);
        llCardRecharge = (LinearLayout) findViewById(R.id.layoutCardRecharge);
        divCardRecharge = findViewById(R.id.lastLine);
        llCardRecharge.setOnClickListener(this);

        textCardNum = (TextView) findViewById(R.id.textViewCardNumTitle);

        if(iAngle > 0){
            findViewById(R.id.lineAngle).setVisibility(View.VISIBLE);
            findViewById(R.id.layoutAngle).setVisibility(View.VISIBLE);
            //((TextView)findViewById(R.id.textViewAngle)).setText(String.valueOf(iAngle - 1));
            ((TextView)findViewById(R.id.textViewAngle)).setText(getString(R.string.device_angle_du, iAngle - 1));
        }else{
            findViewById(R.id.lineAngle).setVisibility(View.GONE);
            findViewById(R.id.layoutAngle).setVisibility(View.GONE);
        }

        llPassInfo = findViewById(R.id.passInfo);
    }

    private void showRecharge(boolean show) {
        if (show) {
            llCardRecharge.setVisibility(View.VISIBLE);
            divCardRecharge.setVisibility(View.VISIBLE);
        } else {
            llCardRecharge.setVisibility(View.GONE);
            divCardRecharge.setVisibility(View.GONE);
        }
    }

    private void setData() {
        if (device == null) {
            return;
        }

        textImei.setText(device.getImei());
        textExtra.setText(device.getRemark());

        if (device.getIs_iot_card() == 1) {
            textCardNum.setText(R.string.phone_card_num_iot);
        } else {
            textCardNum.setText(R.string.phone_card_num_tel);
        }

        if (device.isIs_card_lifelong()) {
            llCardDueTo.setVisibility(View.VISIBLE);
            showRecharge(false);
        } else {
            llCardDueTo.setVisibility(View.GONE);
            if (device.getIs_iot_card() == 1 && device.goome_card == 1 && !TextUtils.isEmpty(device.getPhone())) {
                //显示物联卡充值
                showRecharge(true);
            } else {
                //隐藏物联卡充值
                showRecharge(false);
            }
        }

        if (device.getState() == DeviceInfo.STATE_EXPIRE) {
            String sp_contact = "";
            String sp_phone = "";
            RespServiceProvider.ServiceProvider sp = AllOnlineApp.spInfo;
            if (!TextUtils.isEmpty(sp.sp_contact)) {
                sp_contact = sp.sp_contact;
            }
            if (!TextUtils.isEmpty(sp.sp_phone)) {
                sp_phone = sp.sp_phone;
            }
            textExpiredInfo.setVisibility(View.VISIBLE);
            textExpiredInfo.setText(getString(R.string.device_is_expire_detail) + sp_contact + ":" + sp_phone);
        } else {
            textExpiredInfo.setVisibility(View.GONE);
        }

        if (TextUtils.isEmpty(device.getName())) {
            textDevName.setText(this.getString(R.string.car_name_empty));
            actionbar.setTitle(this.getString(R.string.car_info_text));
        } else {
            textDevName.setText(device.getName());
            actionbar.setTitle(device.getName());
        }

        if (TextUtils.isEmpty(device.getPhone())) {
            textPhone.setText(R.string.not_detect);
        } else {
            textPhone.setText(device.getPhone());
        }

        textLastConn.setText(CommonUtil.getTimeString(device.getHeart_time() * 1000));
        textGpsTime.setText(CommonUtil.getTimeString(device.getGps_time() * 1000));
        if (!TextUtils.isEmpty(device.getClient_product_type())) {
            textDevType.setText(device.getClient_product_type());
        } else {
            textDevType.setText(device.getDev_type());
        }

        // 需要根据后台数据 判断显示，， 如果是2030-12-31年的就显示”终身“
        if (device.getIs_enable() == 0) {
            //设备未上线，到期时间显示为空
            textStartTime.setText(R.string.not_enable);
            textEndTime.setText("");
        } else {
            //开通时间
            if (device.getIn_time() > 0) {
                textStartTime.setText(CommonUtil.getDateString(device.getIn_time() * 1000));
            } else {
                textStartTime.setText(getString(R.string.not_enable));
            }

            //到期时间
            if (device.getOut_time() >= 1924876800) {
                textEndTime.setText(DeviceDetailInfoActivity.this.getString(R.string.show_end_date));
            } else if (device.getOut_time() > 0) {
                textEndTime.setText(CommonUtil.getDateString(device.getOut_time() * 1000));
            } else {
                textEndTime.setText("");
            }
        }

        // 联系人相关
        textDevContact.setText(device.getOwner());
        textContactPhone.setText(device.getTel());
    }

    @Override
    public void onClick(View v) {
        if (device == null) {
            return;
        }
        Intent intent = null;
        switch (v.getId()) {
            case R.id.layoutDevName:
                intent = new Intent(DeviceDetailInfoActivity.this, DeviceInfoEditActivity.class);
                intent.putExtra(DeviceInfoEditActivity.DATA_DEVICE, device);
                intent.putExtra(DeviceInfoEditActivity.DATA_TYPE, DeviceInfoEditActivity.DEV_NAME);
                startActivity(intent);
                break;

            case R.id.layoutImei:
                //复制
                copy(textImei.getText().toString(), ((TextView) findViewById(R.id.textViewImeiTitle)).getText()
                    .toString());
                break;

            case R.id.layoutExtra:
                //备注
                intent = new Intent(DeviceDetailInfoActivity.this, DeviceInfoEditActivity.class);
                intent.putExtra(DeviceInfoEditActivity.DATA_DEVICE, device);
                intent.putExtra(DeviceInfoEditActivity.DATA_TYPE, DeviceInfoEditActivity.DEV_EXTRA_INFO);
                startActivity(intent);
                break;

            case R.id.layoutCardNum:
                if (device.goome_card == 1) {
                    copy(device.getPhone(), ((TextView) findViewById(R.id.textViewCardNumTitle)).getText().toString());
                } else {
                    if (TextUtils.isEmpty(device.getPhone())) {
                        //为空。只能编辑
                        intent = new Intent(DeviceDetailInfoActivity.this, DeviceInfoEditActivity.class);
                        intent.putExtra(DeviceInfoEditActivity.DATA_DEVICE, device);
                        intent.putExtra(DeviceInfoEditActivity.DATA_TYPE, DeviceInfoEditActivity.DEV_PHONE_NUM);
                        startActivity(intent);
                        return;
                    }

                    //可以复制、编辑，打电话(device.is_iot_card != 1 非物联卡,才成立)
                    showTelephoneWindow(DeviceInfoEditActivity.DEV_PHONE_NUM, device.getPhone(),
                        device.getIs_iot_card() != 1);
                }
                break;

            case R.id.layoutTotalDistance:
                showDisList();
                break;

            case R.id.layoutWarning:
                if (device == null) {
                    return;
                }
                //设备的告警
                intent = new Intent(DeviceDetailInfoActivity.this, AlarmCategoryListActivity.class);
                intent.putExtra(AlarmCategoryListActivity.IMEI, device.getImei());
                startActivity(intent);
                break;

            case R.id.layoutCardRecharge:
                if (device == null) {
                    return;
                }
                //物联卡充值
                intent = new Intent(DeviceDetailInfoActivity.this, CardSingleRechargeActivity.class);
                intent.putExtra(Constant.KEY_DEVICE, device);
                startActivity(intent);
                break;

            //case R.id.layoutIllegal:
            //    //违章
            //    HashMap<String, String> map = new HashMap<String, String>();
            //    map.put("ev_function", "违章查询");
            //    MobclickAgent.onEvent(DeviceDetailInfoActivity.this, "ev_function", map);
            //
            //    intent = new Intent(DeviceDetailInfoActivity.this, WeiZhangSearchActivity.class);
            //    //device.state.textAddress = addressTv.getText().toString();
            //    intent.putExtra("weizhang_device", device);
            //    startActivity(intent);
            //    break;

            case R.id.layoutDevContact:
                //联系人
                intent = new Intent(DeviceDetailInfoActivity.this, DeviceInfoEditActivity.class);
                intent.putExtra(DeviceInfoEditActivity.DATA_DEVICE, device);
                intent.putExtra(DeviceInfoEditActivity.DATA_TYPE, DeviceInfoEditActivity.DEV_CONTACT_NAME);
                startActivity(intent);
                break;

            case R.id.layoutContactNum:
                //联系人电话号码
                if (device != null && !TextUtils.isEmpty(device.getTel())) {
                    showTelephoneWindow(DeviceInfoEditActivity.DEV_CONTACT_PHONE, device.getTel(), true);
                } else {
                    intent = new Intent(DeviceDetailInfoActivity.this, DeviceInfoEditActivity.class);
                    intent.putExtra(DeviceInfoEditActivity.DATA_DEVICE, device);
                    intent.putExtra(DeviceInfoEditActivity.DATA_TYPE, DeviceInfoEditActivity.DEV_CONTACT_PHONE);
                    startActivity(intent);
                }
                break;

            default:
                break;
        }
    }

    private void copy(String data, String title) {
        if (!TextUtils.isEmpty(data)) {
            ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            // 将文本内容放到系统剪贴板里。
            cm.setText(data);
            if (title == null) {
                title = "";
            }
            Toast.makeText(this, title + getResources().getString(R.string.imei_copy_hint),
                Toast.LENGTH_SHORT).show();
        }
    }

    private void showTelephoneWindow(int iType, final String telNum, boolean bCanCall) {
        ArrayList<TextSet> listSet = new ArrayList<TextSet>();
        TextSet textSet0 = new TextSet(R.string.copy, true, new OnClickListener() {
            @Override
            public void onClick(View view) {
                //复制
                copy(telNum, null);
            }
        });

        TextSet textSet2 = new TextSet(R.string.edit, true, new OnClickListener() {
            @Override
            public void onClick(View view) {
                //编辑
                Intent intent = new Intent(DeviceDetailInfoActivity.this, DeviceInfoEditActivity.class);
                intent.putExtra(DeviceInfoEditActivity.DATA_DEVICE, device);
                intent.putExtra(DeviceInfoEditActivity.DATA_TYPE, iType);
                startActivity(intent);
            }
        });

        listSet.add(textSet0);
        if (bCanCall) {
            TextSet textSet1 = new TextSet(R.string.tel_call, true, new OnClickListener() {
                @Override
                public void onClick(View view) {
                    //通话
                    requestCall(telNum);
                }
            });
            listSet.add(textSet1);
        }
        listSet.add(textSet2);

        PopupWindowUtil.showPopWindow(this, mainView, 0, listSet, true);
    }

    @SuppressWarnings("CheckResult")
    private void requestCall(String num) {
        rxPermissions.requestEach(Manifest.permission.CALL_PHONE)
            .subscribe(permission -> {
                if (permission.granted) {
                    call(num);
                } else if (permission.shouldShowRequestPermissionRationale) {
                    showSettingDlg(getString(R.string.per_call_hint), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            PermissionUtil.goIntentSetting(DeviceDetailInfoActivity.this);
                        }
                    });
                } else {

                }
            });
    }

    private void showDisList() {
        final String items[] = getResources().getStringArray(R.array.statistics_time);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("里程统计");
        builder.setIcon(R.drawable.ic_launcher);
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                gotoStatistics(items[which]);
            }
        });
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.create().show();
    }

    private void gotoStatistics(String time) {
        Intent i = new Intent(this, DistanceStatisticsActivity.class);
        i.putExtra("device_name", device != null ? device.getName() : "");
        i.putExtra(DEV_IMEI, imei);
        i.putExtra("time", time);
        startActivity(i);
    }

    private void call(String number) {
        if (TextUtils.isEmpty(number)) {
            return;
        }
        Toast.makeText(DeviceDetailInfoActivity.this, R.string.calling, Toast.LENGTH_SHORT).show();
        try {
            Intent intent = new Intent(Intent.ACTION_CALL);
            intent.setData(Uri.parse("tel:" + number));
            startActivity(intent);
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }
}
