package com.coomix.app.all.ui.detail;

import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.coomix.app.all.AllOnlineApp;
import com.coomix.app.all.GlobalParam;
import com.coomix.app.all.R;
import com.coomix.app.all.model.bean.DeviceDetailInfo;
import com.coomix.app.all.widget.ClearEditView;
import com.coomix.app.all.widget.MyActionbar;
import com.coomix.app.framework.util.CommonUtil;
import com.coomix.app.all.data.BaseSubscriber;
import com.coomix.app.all.data.DataEngine;
import com.coomix.app.all.data.ExceptionHandle;
import com.coomix.app.all.data.RxUtils;
import com.coomix.app.all.manager.ThemeManager;
import com.coomix.app.all.model.response.RespBase;
import com.coomix.app.all.ui.base.BaseActivity;

import io.reactivex.disposables.Disposable;

public class DeviceInfoEditActivity extends BaseActivity {
    private DeviceDetailInfo device;
    private ClearEditView editText;
    private MyActionbar actionbar;

    public static final String DATA_DEVICE = "devicec";
    public static final String DATA_TYPE = "type";
    public static final int DEV_NAME = 0;//设备名称
    public static final int DEV_CAR_NUM = 1;//车牌号
    public static final int DEV_PHONE_NUM = 2;  //设备电话号码、物联卡号
    public static final int DEV_CONTACT_PHONE = 3; //联系人
    public static final int DEV_CONTACT_NAME = 4; //联系人电话
    public static final int DEV_EXTRA_INFO = 5; //备注
    private int iType = DEV_NAME;

    private TextView textExtra;

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);

        setContentView(R.layout.activity_device_info_update);

        device = (DeviceDetailInfo) getIntent().getSerializableExtra(DATA_DEVICE);
        iType = getIntent().getIntExtra(DATA_TYPE, DEV_NAME);
        if (device == null) {
            finish();
            return;
        }

        initView();
    }

    private void initView() {
        actionbar = (MyActionbar) findViewById(R.id.myActionbar);
        actionbar.initActionbar(true, R.string.bottom_action_item_title_mine, R.string.save_tetx, 0);

        editText = (ClearEditView) findViewById(R.id.editTextView);
        textExtra = (TextView) findViewById(R.id.textViewExtra);
        textExtra.setVisibility(View.GONE);
        editText.setVisibility(View.VISIBLE);

        actionbar.setRightTextClickListener(view -> modifyUserInfo());

        if (iType == DEV_EXTRA_INFO) {
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, getResources().getDimensionPixelOffset(R.dimen.edit_max_height));
            params.setMargins(getResources().getDimensionPixelOffset(R.dimen.space_2x), getResources().getDimensionPixelOffset(R.dimen.space_3x), getResources().getDimensionPixelOffset(R.dimen.space_2x), 0);
            editText.setLayoutParams(params);
            editText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(150)});
            editText.setGravity(Gravity.TOP);
            editText.setSingleLine(false);
            editText.setHorizontallyScrolling(false);
        } else {
            editText.setSingleLine(true);
        }

        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (isValidContent()) {
                    actionbar.setRightTextColor(ThemeManager.getInstance().getThemeAll().getThemeColor().getColorNavibarText());
                } else {
                    actionbar.setRightTextColor(ThemeManager.getInstance().getThemeAll().getThemeColor().getColorUntouchableBtn());
                }
            }
        });

        String textFron = getString(R.string.setting);
        int iResId = 0;
        switch (iType) {
            case DEV_NAME:
                //设备名称
                iResId = R.string.device_name;
                editText.setInputType(InputType.TYPE_CLASS_TEXT);
                editText.setText(device.getName());
                break;

            case DEV_CAR_NUM:
                //车牌号
                iResId = R.string.device_number;
                editText.setInputType(InputType.TYPE_CLASS_TEXT);
                editText.setText(device.getNumber());
                break;

            case DEV_EXTRA_INFO:
                iResId = R.string.device_extra;
                editText.setInputType(InputType.TYPE_CLASS_TEXT);
                editText.setText(device.getRemark());
                break;

            case DEV_PHONE_NUM:
                //设备电话号码、物联卡号
                if (device.getIs_iot_card() == 1) {
                    iResId = R.string.phone_card_num_iot;
                } else {
                    iResId = R.string.phone_card_num_tel;
                }
                editText.setInputType(InputType.TYPE_CLASS_NUMBER);
                editText.setText(device.getPhone());
                break;

            case DEV_CONTACT_NAME:
                //联系人
                iResId = R.string.device_contact;
                editText.setInputType(InputType.TYPE_CLASS_TEXT);
                editText.setText(device.getOwner());
                break;

            case DEV_CONTACT_PHONE:
                //联系人电话
                iResId = R.string.contact_num;
                editText.setInputType(InputType.TYPE_CLASS_NUMBER);
                editText.setText(device.getTel());
                break;

            default:
                return;
        }

        if (editText.getText().length() > 0) {
            editText.setSelection(editText.getText().length());
        }
        actionbar.setTitle(textFron + getString(iResId));
    }

    private boolean isValidContent() {
        String content = editText.getText().toString();
        switch (iType) {
            case DEV_NAME:
                //设备名称
                break;

            case DEV_CAR_NUM:
                //车牌号
                break;

            case DEV_EXTRA_INFO:
                //备注
                break;

            case DEV_PHONE_NUM:
                //设备电话号码、物联卡号
                if (!TextUtils.isEmpty(content) && !(CommonUtil.isTelNumber(content) || CommonUtil.isCoupletCard(content))) {
                    return false;
                }
                break;

            case DEV_CONTACT_PHONE:
                //联系人电话
                if (!TextUtils.isEmpty(content) && !CommonUtil.isTelNumber(content)) {
                    return false;
                }
                break;

            case DEV_CONTACT_NAME:
                //联系人
                break;

            default:
                return false;
        }

        return true;
    }

    private void modifyUserInfo() {
        if (!isValidContent()) {
            String title = actionbar.getTitle();
            if (title != null && title.length() > 2) {
                title = title.substring(2);
            }
            Toast.makeText(this, getString(R.string.pls_input_correct) + title, Toast.LENGTH_SHORT).show();
            return;
        }

        String content = editText.getText().toString();
        switch (iType) {
            case DEV_NAME:
                //设备名称
                if (device.getName().equals(content)) {
                    finish();
                    return;
                }
                device.setName(content);
                break;

            case DEV_CAR_NUM:
                //车牌号
                if (device.getNumber().equals(content)) {
                    finish();
                    return;
                }
                device.setNumber(content);
                break;

            case DEV_EXTRA_INFO:
                if (device.getRemark().equals(content)) {
                    finish();
                    return;
                }
                device.setRemark(content);
                break;

            case DEV_PHONE_NUM:
                //设备电话号码、物联卡号
                if (device.getPhone().equals(content)) {
                    finish();
                    return;
                }
                device.setPhone(content);
                break;

            case DEV_CONTACT_PHONE:
                //联系人电话
                if (device.getTel().equals(content)) {
                    finish();
                    return;
                }
                device.setTel(content);
                break;

            case DEV_CONTACT_NAME:
                //联系人
                if (device.getOwner().equals(content)) {
                    finish();
                    return;
                }
                device.setOwner(content);
                break;

            default:
                return;
        }

        //进行更改设备信息
        modifyDeviceDetailInfo();
    }

    private void modifyDeviceDetailInfo() {
        showProgressDialog(getString(R.string.please_wait));
        Disposable disposable = DataEngine.getAllMainApi().modifyDevDetailInfo(
                GlobalParam.getInstance().getCommonParas(),
                AllOnlineApp.sAccount,
                GlobalParam.getInstance().getAccessToken(),
                device.getImei(),
                device.getName(),
                device.getPhone(),
                device.getNumber(),
                device.getOwner(),
                device.getTel(),
                device.getRemark())
                .compose(RxUtils.toMain())
                .compose(RxUtils.businessTransformer())
                .subscribeWith(new BaseSubscriber<RespBase>() {
                    @Override
                    public void onHttpError(ExceptionHandle.ResponeThrowable e) {
                        showErr(e.getErrCodeMessage());
                        hideLoading();
                        showToast(getString(R.string.update_fail));
                    }

                    @Override
                    public void onNext(RespBase respBase) {
                        hideLoading();
                        showToast(getString(R.string.update_ok));
                        DeviceDetailInfoActivity.bUpdate = true;
                        finish();
                    }
                });
        subscribeRx(disposable);
    }

}