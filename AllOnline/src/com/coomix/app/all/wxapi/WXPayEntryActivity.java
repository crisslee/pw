package com.coomix.app.all.wxapi;

import android.content.Intent;
import android.os.Bundle;
import com.coomix.app.all.ui.base.BaseActivity;
import com.coomix.app.all.ui.platformRecharge.CoomixPlatDevRechargeSucc;
import com.coomix.app.pay.PayResultManager;
import com.tencent.mm.opensdk.modelbase.BaseReq;
import com.tencent.mm.opensdk.modelbase.BaseResp;
import com.tencent.mm.opensdk.openapi.IWXAPIEventHandler;

/**
 * Created by ssl on 2017/1/23.
 * 微信支付回调
 */
public class WXPayEntryActivity extends BaseActivity implements IWXAPIEventHandler, CoomixPayBase.FinishListener {

    private CoomixPayBase coomixView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        switch (PayResultManager.getInstance().getPayOrderType()) {
            case FROM_ACTIVITY_ORDER:
                coomixView = new CoomixPayActivity(WXPayEntryActivity.this, getIntent(), this);
                break;

            case FROM_REDPACKET:
                coomixView = new CoomixPayRedPacket(WXPayEntryActivity.this, getIntent(), this);
                break;

            case FROM_RECHARGE_BALANCE:
                coomixView = new CoomixPayRecharge(WXPayEntryActivity.this, getIntent(), this);
                break;
            case FROM_RECHARGE_PLATFORM_DEVICES:
                coomixView = new CoomixPlatDevRechargeSucc(WXPayEntryActivity.this, getIntent(), this);
                break;

            default:
                finish();
                return;
        }

        coomixView.initApi(getIntent(), this);

        if (coomixView.getMainView() != null) {
            setContentView(coomixView.getMainView());
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (coomixView != null) {
            coomixView.release();
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        setIntent(intent);

        if (coomixView != null) {
            coomixView.onNewIntent(intent);
        }
    }

    @Override
    public void onReq(BaseReq baseReq) {
        if (coomixView != null) {
            coomixView.onReq(baseReq);
        }
    }

    @Override
    public void onResp(BaseResp baseResp) {
        if (coomixView != null) {
            coomixView.onResp(baseResp);
        }
    }

    @Override
    public void finish() {
        super.finish();
        if (coomixView != null) {
            coomixView.onBackFinish();
        }
    }

    @Override
    public void toFinish() {
        finish();
    }

    @Override
    public void onBackPressed() {
        if (coomixView != null) {
            coomixView.onBackPressed();
        }
        finish();
    }
}
