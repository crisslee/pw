package com.coomix.app.all.ui.mine;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import com.bumptech.GlideApp;
import com.coomix.app.all.AllOnlineApp;
import com.coomix.app.all.Constant;
import com.coomix.app.all.GlobalParam;
import com.coomix.app.all.R;
import com.coomix.app.all.data.BaseSubscriber;
import com.coomix.app.all.data.DataEngine;
import com.coomix.app.all.data.ExceptionHandle;
import com.coomix.app.all.data.RxUtils;
import com.coomix.app.all.manager.ThemeManager;
import com.coomix.app.all.model.response.RespBase;
import com.coomix.app.all.ui.activity.MyActivityActivity;
import com.coomix.app.all.ui.base.BaseActivity;
import com.coomix.app.all.ui.cardRecharge.CardRechargeActivity;
import com.coomix.app.all.ui.login.LoginActivity;
import com.coomix.app.all.ui.platformRecharge.PlatRechargeActivity;
import com.coomix.app.all.ui.setting.SettingActivity;
import com.coomix.app.all.ui.unlock.UnLockActivity;
import com.coomix.app.all.widget.MyActionbar;
import com.coomix.app.framework.util.OSUtil;
import com.google.zxing.activity.CaptureActivity;
import com.google.zxing.util.ZConstant;
import io.reactivex.disposables.Disposable;

/**
 * “我的”界面
 * 首先需要获取报警数量和评论数量
 *
 * @author herry
 */
public class MineActivity extends BaseActivity {
    private static final int REQ_SCAN_BIND = 10000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_mine_layout);

        initViews();
    }

    private void initViews() {
        MyActionbar actionbar = (MyActionbar) findViewById(R.id.myActionbar);
        actionbar.initActionbar(true, R.string.bottom_action_item_title_mine, 0, 0);

        TextView textUser = (TextView) findViewById(R.id.textViewUserName);
        if (AllOnlineApp.sToken.loginType == LoginActivity.LOGIN_WX) {
            textUser.setText(AllOnlineApp.sToken.name);
        } else {
            textUser.setText(AllOnlineApp.sAccount);
        }

        findViewById(R.id.layoutUserInfo).setOnClickListener(view -> startActivity(AccountInfoActivity.class));
        findViewById(R.id.layoutService).setOnClickListener(view -> startActivity(ProviderInfoActivity.class));
        findViewById(R.id.layoutMyActivity).setOnClickListener(view -> startActivity(MyActivityActivity.class));
        findViewById(R.id.layoutRecharge).setOnClickListener(view -> startActivity(PlatRechargeActivity.class));
        findViewById(R.id.layoutCardRecharge).setOnClickListener(view -> startActivity(CardRechargeActivity.class));
        findViewById(R.id.layoutUnLock).setOnClickListener(view -> startActivity(UnLockActivity.class));
        findViewById(R.id.layoutSetting).setOnClickListener(view -> startActivity(SettingActivity.class));
        findViewById(R.id.layoutScanCode).setOnClickListener(view -> {
            Intent i = new Intent(this, CaptureActivity.class);
            i.putExtra(CaptureActivity.SHOW_MANUAL, true);
            startActivityForResult(i, REQ_SCAN_BIND);
        });

        ImageView imageUser = (ImageView) findViewById(R.id.imageViewUser);
        ImageView imageService = (ImageView) findViewById(R.id.imageViewService);
        ImageView imageActivity = (ImageView) findViewById(R.id.imageViewActivity);
        ImageView imageRecharge = (ImageView) findViewById(R.id.imageViewRecharge);
        ImageView imageCardRecharge = (ImageView) findViewById(R.id.imageViewCardRecharge);
        ImageView imageUnLock = findViewById(R.id.imageViewUnLock);
        ImageView imageScanCode = (ImageView) findViewById(R.id.imageViewScanCode);
        ImageView imageSetting = (ImageView) findViewById(R.id.imageViewSetting);

        GlideApp.with(this)
            .load(ThemeManager.getInstance().getThemeAll().getThemeColor().getAccountInfo())
            .placeholder(R.drawable.mine_item_user_icon)
            .error(R.drawable.mine_item_user_icon)
            .into(imageUser);
        GlideApp.with(this)
            .load(ThemeManager.getInstance().getThemeAll().getThemeColor().getSpInfo())
            .placeholder(R.drawable.mine_item_sp_icon)
            .error(R.drawable.mine_item_sp_icon)
            .into(imageService);
        GlideApp.with(this)
            .load(ThemeManager.getInstance().getThemeAll().getThemeColor().getActivity())
            .placeholder(R.drawable.mine_item_activity_icon)
            .error(R.drawable.mine_item_activity_icon)
            .into(imageActivity);
        GlideApp.with(this)
            .load(ThemeManager.getInstance().getThemeAll().getThemeColor().getCharge())
            .placeholder(R.drawable.mine_platform_recharge)
            .error(R.drawable.mine_platform_recharge)
            .into(imageRecharge);
        GlideApp.with(this)
            .load(ThemeManager.getInstance().getThemeAll().getThemeColor().getCard_charge())
            .placeholder(R.drawable.mine_card_recharge)
            .error(R.drawable.mine_card_recharge)
            .into(imageCardRecharge);
        GlideApp.with(this)
                .load(ThemeManager.getInstance().getThemeAll().getThemeColor().getUn_lock())
                .placeholder(R.drawable.mine_card_recharge)
                .error(R.drawable.mine_card_recharge)
                .into(imageUnLock);
        GlideApp.with(this)
            .load(ThemeManager.getInstance().getThemeAll().getThemeColor().getBarcode())
            .placeholder(R.drawable.mine_item_scan_icon)
            .error(R.drawable.mine_item_scan_icon)
            .into(imageScanCode);
        GlideApp.with(this)
            .load(ThemeManager.getInstance().getThemeAll().getThemeColor().getSetting())
            .placeholder(R.drawable.mine_item_setting_icon)
            .error(R.drawable.mine_item_setting_icon)
            .into(imageSetting);

        if(AllOnlineApp.sAccount != null && AllOnlineApp.sAccount.equals(Constant.ACCOUNT_AN_ZHUANG_GONG)){
            //隐藏其余选项，只保留账户的
            findViewById(R.id.layoutService).setVisibility(View.GONE);
            findViewById(R.id.layoutMyActivity).setVisibility(View.GONE);
            findViewById(R.id.layoutRecharge).setVisibility(View.GONE);
            findViewById(R.id.layoutCardRecharge).setVisibility(View.GONE);
            findViewById(R.id.layoutSetting).setVisibility(View.GONE);
            findViewById(R.id.layoutScanCode).setVisibility(View.GONE);
            findViewById(R.id.imageViewLine1).setVisibility(View.GONE);
            findViewById(R.id.imageViewLine2).setVisibility(View.GONE);
            findViewById(R.id.imageViewLine3).setVisibility(View.GONE);
            findViewById(R.id.imageViewLine4).setVisibility(View.GONE);
        }
    }

    private void startActivity(Class<?> cls) {
        startActivity(new Intent(this, cls));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && data != null) {
            switch (requestCode) {
                //绑定设备
                case REQ_SCAN_BIND:
                    Bundle b = data.getExtras();
                    if (b == null) {
                        return;
                    }
                    if (b.containsKey(ZConstant.INTENT_EXTRA_KEY_QR_SCAN)) {
                        String imei = b.getString(ZConstant.INTENT_EXTRA_KEY_QR_SCAN);
                        bindDevice(imei);
                    }
                    break;
                default:
                    break;
            }
        }
    }

    private void bindDevice(String imei) {
        String token = AllOnlineApp.sToken.access_token;
        String account = AllOnlineApp.sAccount;
        long time = System.currentTimeMillis() / 1000;
        String sig = OSUtil.toMD5(imei + time + "Goome");
        Disposable d = DataEngine.getAllMainApi()
                .bindDevice(token, account, imei, time, sig, GlobalParam.getInstance().getCommonParas())
                .compose(RxUtils.toMain())
                .compose(RxUtils.businessTransformer())
                .subscribeWith(new BaseSubscriber<RespBase>() {
                    @Override
                    public void onHttpError(ExceptionHandle.ResponeThrowable e) {
                        showToast(e.getErrCode() + "," + e.getErrMessage());
                    }

                    @Override
                    public void onNext(RespBase respBase) {
                        showToast(getString(R.string.bindphone_toast_bind_suc));
                    }
                });
        subscribeRx(d);
    }
}
