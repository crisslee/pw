package com.coomix.app.all.ui.mine;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;
import com.coomix.app.all.AllOnlineApp;
import com.coomix.app.all.GlobalParam;
import com.coomix.app.all.R;
import com.coomix.app.all.util.StringUtil;
import com.coomix.app.all.widget.MyActionbar;
import com.coomix.app.all.widget.TextViewEx;
import com.coomix.app.all.data.BaseSubscriber;
import com.coomix.app.all.data.DataEngine;
import com.coomix.app.all.data.ExceptionHandle;
import com.coomix.app.all.data.RxUtils;
import com.coomix.app.all.model.response.RespServiceProvider;
import com.coomix.app.all.ui.base.BaseActivity;
import com.coomix.app.all.util.FileUtil;
import com.coomix.app.all.util.PermissionUtil;
import com.umeng.analytics.MobclickAgent;
import io.reactivex.disposables.Disposable;
import java.util.HashMap;

/**
 * 服务商信息
 *
 * @author goome
 */
public class ProviderInfoActivity extends BaseActivity {
    private TextView spName, spContact, spPhone;
    private TextViewEx spAddr;
    private View mAddressLayout;
    private View mLinearLayoutPhone;
    private RespServiceProvider.ServiceProvider spInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        HashMap<String, String> map = new HashMap<String, String>();
        map.put("ev_function", "服务商信息");
        MobclickAgent.onEvent(ProviderInfoActivity.this, "ev_function", map);

        setContentView(R.layout.activity_customer);
        findViewById();
        initData();
    }

    private void findViewById() {
        MyActionbar actionbar = (MyActionbar) findViewById(R.id.mineActionbar);
        actionbar.initActionbar(true, R.string.servemerchant_info, 0, 0);

        spName = (TextView) findViewById(R.id.spName);
        spContact = (TextView) findViewById(R.id.spContact);
        spPhone = (TextView) findViewById(R.id.spPhone);

        // 拨打电话
        mLinearLayoutPhone = findViewById(R.id.ll_phoneCall);
        mLinearLayoutPhone.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                requestCall();
            }
        });

        // 赋值地址
        mAddressLayout = findViewById(R.id.address_layout);
        spAddr = (TextViewEx) findViewById(R.id.spAddr);
        mAddressLayout.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!TextUtils.isEmpty(spAddr.getText())) {
                    spAddr.copy();
                    showToast(getString(R.string.address_copy_hint));
                }
            }
        });
    }

    @SuppressWarnings("CheckResult")
    private void requestCall() {
        rxPermissions.requestEach(Manifest.permission.CALL_PHONE)
            .subscribe(permission -> {
                if (permission.granted) {
                    goCall();
                } else if (permission.shouldShowRequestPermissionRationale) {
                    showSettingDlg(getString(R.string.per_call_hint), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            PermissionUtil.goIntentSetting(ProviderInfoActivity.this);
                        }
                    });
                } else {

                }
            });
    }

    private void goCall() {
        try {
            if (spInfo != null && !StringUtil.isTrimEmpty(spInfo.sp_phone)) {
                showToast(getString(R.string.calling));
                Intent intent = new Intent(Intent.ACTION_CALL);
                intent.setData(Uri.parse("tel:" + spInfo.sp_phone));
                startActivity(intent);
            }
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }

    private void updateData() {
        if (spInfo == null) {
            return;
        }
        if (!StringUtil.isTrimEmpty(spInfo.sp_name)) {
            spName.setText(spInfo.sp_name);
        }
        if (!StringUtil.isTrimEmpty(spInfo.sp_contact)) {
            spContact.setText(spInfo.sp_contact);
        }
        if (!StringUtil.isTrimEmpty(spInfo.sp_addr)) {
            spAddr.setText(spInfo.sp_addr);
        }
        if (!StringUtil.isTrimEmpty(spInfo.sp_phone)) {
            spPhone.setText(spInfo.sp_phone);
        }
    }

    private void initData() {
        showLoading(getString(R.string.query_ing));
        getSpInfo();
    }

    private void getSpInfo() {
        String token = AllOnlineApp.sToken.access_token;
        String account = AllOnlineApp.sAccount;
        String target = AllOnlineApp.sTarget;
        long time = System.currentTimeMillis() / 1000;
        Disposable d = DataEngine.getAllMainApi()
            .getServiceProvider(token, account, target, time, GlobalParam.getInstance().getCommonParas())
            .compose(RxUtils.toMain())
            .compose(RxUtils.businessTransformer())
            .subscribeWith(new BaseSubscriber<RespServiceProvider>() {
                @Override
                public void onHttpError(ExceptionHandle.ResponeThrowable e) {
                    hideLoading();
                    showToast(getString(R.string.request_fail));
                }

                @Override
                public void onNext(RespServiceProvider respServiceProvider) {
                    hideLoading();
                    spInfo = respServiceProvider.getData();
                    AllOnlineApp.spInfo = spInfo;
                    FileUtil.saveOjbect(AllOnlineApp.mApp, spInfo, FileUtil.getSpPath(AllOnlineApp.mApp));
                    updateData();
                }
            });
        subscribeRx(d);
    }

}
