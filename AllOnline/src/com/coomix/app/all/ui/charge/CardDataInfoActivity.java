package com.coomix.app.all.ui.charge;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;
import butterknife.BindView;
import butterknife.ButterKnife;
import com.coomix.app.all.Constant;
import com.coomix.app.all.GlobalParam;
import com.coomix.app.all.R;
import com.coomix.app.all.ui.cardRecharge.CardSingleRechargeActivity;
import com.coomix.app.all.model.bean.CardDataInfo;
import com.coomix.app.all.model.bean.CardInfo;
import com.coomix.app.all.model.bean.DataItem;
import com.coomix.app.all.model.bean.DeviceDetailInfo;
import com.coomix.app.all.model.bean.ProductInfo;
import com.coomix.app.all.model.bean.RechargeDataBundle;
import com.coomix.app.all.share.PopupWindowUtil;
import com.coomix.app.all.share.TextSet;
import com.coomix.app.all.widget.MyActionbar;
import com.coomix.app.all.data.BaseSubscriber;
import com.coomix.app.all.data.DataEngine;
import com.coomix.app.all.data.ExceptionHandle;
import com.coomix.app.all.data.RxUtils;
import com.coomix.app.all.manager.ThemeManager;
import com.coomix.app.all.model.response.RespCardDataInfo;
import com.coomix.app.all.model.response.RespDataBundles;
import com.coomix.app.all.model.response.RespRechargeDataBundle;
import com.coomix.app.all.ui.base.BaseActivity;
import com.coomix.app.pay.CoomixPayManager;
import com.coomix.app.pay.ICoomixPay;
import com.coomix.app.pay.PayResultManager;
import com.coomix.app.all.util.ViewUtil;
import io.reactivex.disposables.Disposable;
import java.util.ArrayList;

/**
 * File Description:
 *
 * @author felixqiu
 * @since 2019/3/18.
 */
public class CardDataInfoActivity extends BaseActivity {
    public static final String KEY_CARD_DATA = "key_card_data";

    private CardDataInfo cardDataInfo;
    private DeviceDetailInfo device;
    private ArrayList<DataItem> bundles;

    @BindView(R.id.topbar)
    MyActionbar topbar;
    @BindView(R.id.cardNo)
    TextView cardNo;
    @BindView(R.id.cardIsp)
    TextView cardIsp;
    @BindView(R.id.cardIccid)
    TextView cardIccid;
    @BindView(R.id.cardExp)
    TextView cardExp;
    @BindView(R.id.dataTotal)
    TextView dataTotal;
    @BindView(R.id.dataLeft)
    TextView dataLeft;
    @BindView(R.id.dataUsed)
    TextView dataUsed;
    @BindView(R.id.cardCharge)
    TextView cardCharge;
    @BindView(R.id.dataCharge)
    TextView dataCharge;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_card_data_info);
        ButterKnife.bind(this);
        initIntent();
        getDataBundles();
        initView();
    }

    @Override
    protected void onResume() {
        super.onResume();
        queryCardDataInfo();
    }

    private void initIntent() {
        Intent i = getIntent();
        if (i == null) {
            return;
        }
        if (i.hasExtra(KEY_CARD_DATA)) {
            cardDataInfo = (CardDataInfo) i.getSerializableExtra(KEY_CARD_DATA);
        }
        if (i.hasExtra(Constant.KEY_DEVICE)) {
            device = (DeviceDetailInfo) i.getSerializableExtra(Constant.KEY_DEVICE);
        }
    }

    private void initView() {
        topbar.setTitle(R.string.card_data_info_title);
        cardCharge.setOnClickListener(v -> toCardCharge());
        ViewUtil.setBg(cardCharge, ThemeManager.getInstance().getBGColorDrawable(this));
        dataCharge.setOnClickListener(v -> toDataCharge());
        ViewUtil.setBg(dataCharge, ThemeManager.getInstance().getBGColorDrawable(this));
        updateUi();
    }

    private void updateUi() {
        if (cardDataInfo == null || cardDataInfo.card == null || cardDataInfo.product == null) {
            return;
        }
        CardInfo ci = cardDataInfo.card;
        cardNo.setText(ci.msisdn);
        cardIccid.setText(ci.iccid);
        cardExp.setText(ci.expdate);
        ProductInfo pi = cardDataInfo.product;
        dataTotal.setText(String.valueOf(pi.total_value) + " MB");
        dataLeft.setText(String.valueOf(pi.left_value) + " MB");
        dataUsed.setText(String.valueOf(pi.cumulate_value) + " MB");
    }

    private void toCardCharge() {
        if (device == null) {
            return;
        }
        Intent intent = new Intent(this, CardSingleRechargeActivity.class);
        intent.putExtra(Constant.KEY_DEVICE, device);
        startActivity(intent);
    }

    private void toDataCharge() {
        if (bundles == null || bundles.size() == 0) {
            showToast("未获取到流量套餐信息");
        }
        ArrayList<TextSet> listSet = new ArrayList<TextSet>();
        for (int i = 0; i < bundles.size(); i++) {
            final DataItem item = bundles.get(i);
            String title = item.getSize() + " M，" + item.getAmount() / 100.00 + " 元";
            TextSet t1 = new TextSet(title, true, new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //复制
                    toRealCharge(item);
                }
            });
            listSet.add(t1);
        }
        PopupWindowUtil.showPopWindow(this, getWindow().getDecorView(), R.string.data_bundles_title, listSet, true);
    }

    private void toRealCharge(DataItem item) {
        Disposable d = DataEngine.getCardApi().buyDataBundle(cardDataInfo.card.msisdn, item.getId(),
            "lite", GlobalParam.getInstance().getCommonParas())
            .compose(RxUtils.businessTransformer())
            .compose(RxUtils.toMain())
            .subscribeWith(new BaseSubscriber<RespRechargeDataBundle>() {
                @Override
                public void onHttpError(ExceptionHandle.ResponeThrowable e) {

                }

                @Override
                public void onNext(RespRechargeDataBundle respRechargeDataBundle) {
                    if (respRechargeDataBundle != null && respRechargeDataBundle.getData() != null) {
                        startWechatPay(respRechargeDataBundle.getData(), item);
                    }
                }
            });
        subscribeRx(d);
    }

    private void startWechatPay(RechargeDataBundle data, DataItem item) {
        CoomixPayManager coomixPayManager = new CoomixPayManager(this, ICoomixPay.PAY_WECHAT);
        PayResultManager.getInstance().setPayOrderType(ICoomixPay.ORDER_FROM.FROM_RECHARGE_DATA_BUNDLE);
        PayResultManager.getInstance().setRechargeAmount(item.getAmount());
        PayResultManager.getInstance().setOrder_id(data.getOrder_id());
        coomixPayManager.pay(data.getWx_pay());
    }

    private void getDataBundles() {
        if (cardDataInfo == null || cardDataInfo.card == null || TextUtils.isEmpty(cardDataInfo.card.msisdn)) {
            return;
        }
        Disposable d = DataEngine.getCardApi().queryBundles(cardDataInfo.card.msisdn,
            GlobalParam.getInstance().getCommonParas())
            .compose(RxUtils.businessTransformer())
            .compose(RxUtils.toMain())
            .subscribeWith(new BaseSubscriber<RespDataBundles>() {
                @Override
                public void onHttpError(ExceptionHandle.ResponeThrowable e) {

                }

                @Override
                public void onNext(RespDataBundles respDataBundles) {
                    if (respDataBundles != null && respDataBundles.getData() != null) {
                        bundles = respDataBundles.getData().getBundles();
                    }
                }
            });
        subscribeRx(d);
    }

    private void queryCardDataInfo() {
        if (device == null || TextUtils.isEmpty(device.getPhone())) {
            return;
        }
        Disposable d = DataEngine.getCardApi().queryCardData(device.getPhone(),
            GlobalParam.getInstance().getCommonParas())
            .compose(RxUtils.toMain())
            .compose(RxUtils.businessTransformer())
            .subscribeWith(new BaseSubscriber<RespCardDataInfo>() {
                @Override
                public void onHttpError(ExceptionHandle.ResponeThrowable e) {

                }

                @Override
                public void onNext(RespCardDataInfo respCardDataInfo) {
                    if (respCardDataInfo != null && respCardDataInfo.getData() != null) {
                        cardDataInfo = respCardDataInfo.getData();
                        updateUi();
                    }
                }
            });
        subscribeRx(d);
    }
}
