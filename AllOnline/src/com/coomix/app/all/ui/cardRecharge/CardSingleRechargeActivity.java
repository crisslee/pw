package com.coomix.app.all.ui.cardRecharge;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;
import com.coomix.app.all.AllOnlineApp;
import com.coomix.app.all.Constant;
import com.coomix.app.all.GlobalParam;
import com.coomix.app.all.R;
import com.coomix.app.all.model.bean.DeviceDetailInfo;
import com.coomix.app.all.widget.MyActionbar;
import com.coomix.app.all.data.BaseSubscriber;
import com.coomix.app.all.data.DataEngine;
import com.coomix.app.all.data.ExceptionHandle;
import com.coomix.app.all.data.RxUtils;
import com.coomix.app.all.model.response.RenewWlCard;
import com.coomix.app.all.model.response.RespRenewWlCard;
import com.coomix.app.all.model.response.RespWLCardInfo;
import com.coomix.app.all.model.response.WLCardInfo;
import com.coomix.app.all.ui.base.BaseActivity;
import com.coomix.app.pay.CoomixPayManager;
import com.coomix.app.pay.ICoomixPay;
import com.coomix.app.pay.PayResultManager;
import com.coomix.app.all.util.CommunityUtil;
import io.reactivex.disposables.Disposable;
import java.util.ArrayList;

public class CardSingleRechargeActivity extends BaseActivity {

    private TextView textDevName, textCardTime, textCardPrice, textRecharge, textReson;
    private DeviceDetailInfo deviceInfo;
    private WLCardInfo WLCardInfo;

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);

        deviceInfo = (DeviceDetailInfo) getIntent().getSerializableExtra(Constant.KEY_DEVICE);

        setContentView(R.layout.activity_single_card_recharge);

        initViews();
    }

    @Override
    protected void onResume() {
        super.onResume();
        initData();
    }

    private void initViews() {
        MyActionbar myActionbar = (MyActionbar) findViewById(R.id.mineActionbar);
        myActionbar.initActionbar(true, R.string.card_recharge, 0, 0);

        textDevName = (TextView) findViewById(R.id.textViewDevName);
        textCardTime = (TextView) findViewById(R.id.textViewCardTime);
        textCardPrice = (TextView) findViewById(R.id.textViewCardPrice);
        textReson = (TextView) findViewById(R.id.textViewReason);
        textRecharge = (TextView) findViewById(R.id.textViewRecharge);
        textRecharge.setOnClickListener(view -> toRecharge());
    }

    private void initData() {
        if (deviceInfo != null) {
            showProgressDialog();
            textDevName.setText(getString(R.string.dev_name, deviceInfo.getName()));
            Disposable d = DataEngine.getAllMainApi().getWlCardPayInfo(GlobalParam.getInstance().getCommonParas(),
                AllOnlineApp.sAccount, AllOnlineApp.sTarget, AllOnlineApp.sToken.access_token, deviceInfo.getPhone())
                .compose(RxUtils.toMain())
                .compose(RxUtils.businessTransformer())
                .subscribeWith(new BaseSubscriber<RespWLCardInfo>() {
                    @Override
                    public void onHttpError(ExceptionHandle.ResponeThrowable e) {
                        dismissProgressDialog();
                        showToast(e.getErrCode() + "," + e.getErrMessage());
                    }

                    @Override
                    public void onNext(RespWLCardInfo respCardInfo) {
                        dismissProgressDialog();
                        if (respCardInfo != null && respCardInfo.getData() != null) {
                            ArrayList<WLCardInfo> list = respCardInfo.getData().card_info;
                            if (list != null && list.size() > 0) {
                                WLCardInfo = list.get(0);
                                textCardTime.setText(getString(R.string.card_time, WLCardInfo.getC_out_time()));
                                textCardPrice.setText(getString(R.string.card_price,
                                    CommunityUtil.getDecimalStrByInt(WLCardInfo.getPrice(), 2)));
                                setCardRechargeStatus();
                            }
                        }
                    }
                });
            subscribeRx(d);
        }
    }

    private void setCardRechargeStatus() {
        if (WLCardInfo == null) {
            return;
        }
        if (!TextUtils.isEmpty(WLCardInfo.getPay_msg())) {
            //不能充值
            textRecharge.setVisibility(View.GONE);
            textReson.setText(WLCardInfo.getPay_msg());
        } else {
            if (WLCardInfo.getCombo_id() != 0 && (WLCardInfo.getCard_status() == 3 || WLCardInfo.getCard_status() == 4)) {
                //卡状态,0:未知,1:测试期,2:静默期,3:正常使用,4:停机,5:销户,6:预销户,7:单向停机,8:休眠,9:过户,99:号码不存在 
                textRecharge.setVisibility(View.VISIBLE);
                textReson.setText("");
            } else {
                //不能充值
                textRecharge.setVisibility(View.GONE);
                textReson.setText("不能充值");
            }
        }
    }

    private void toRecharge() {
        if (deviceInfo == null || WLCardInfo == null) {

            return;
        }
        showProgressDialog();
        Disposable d = DataEngine.getAllMainApi().renewWlcard(GlobalParam.getInstance().getCommonParas(),
            AllOnlineApp.sAccount, AllOnlineApp.sTarget, AllOnlineApp.sToken.access_token, "app",
            WLCardInfo.getMsisdn())
            .compose(RxUtils.toMain())
            .compose(RxUtils.businessTransformer())
            .subscribeWith(new BaseSubscriber<RespRenewWlCard>() {
                @Override
                public void onHttpError(ExceptionHandle.ResponeThrowable e) {
                    dismissProgressDialog();
                    showToast(e.getErrCode() + "," + e.getErrMessage());
                }

                @Override
                public void onNext(RespRenewWlCard respRenewWlCard) {
                    dismissProgressDialog();
                    if (respRenewWlCard != null && respRenewWlCard.getData() != null) {
                        //CoomixPayManager coomixPayManager = new CoomixPayManager(CardSingleRechargeActivity.this, CoomixPayManager.PAY_WECHAT);
                        //PayResultManager.getInstance().setPayOrderType(ICoomixPay.ORDER_FROM.FROM_RECHARGE_PLATFORM_DEVICES);
                        //PayResultManager.getInstance().setRechargeAmount(rechargeAmount);
                        //PayResultManager.getInstance().setOrder_id(respRenewWlCard.getData().getOrder_id());
                        //PayResultManager.getInstance().setRechargeFrom(FROM_PLAT_RECHARGE);
                        //coomixPayManager.pay(respRenewWlCard.getData().getWx_pay());
                        startWechatPay(respRenewWlCard.getData(), WLCardInfo.getPrice());
                    }
                }
            });
        subscribeRx(d);
    }

    private void startWechatPay(RenewWlCard renewWlCard, long rechargeAmount) {
        CoomixPayManager coomixPayManager = new CoomixPayManager(this, ICoomixPay.PAY_WECHAT);
        PayResultManager.getInstance().setPayOrderType(ICoomixPay.ORDER_FROM.FROM_RECHARGE_PLATFORM_DEVICES);
        PayResultManager.getInstance().setRechargeAmount(rechargeAmount);
        PayResultManager.getInstance().setOrder_id(renewWlCard.getOrder_id());

        //PayResultManager.getInstance().setRechargeFrom(FROM_PLAT_RECHARGE);
        coomixPayManager.pay(renewWlCard.getWx_pay());
    }
}
