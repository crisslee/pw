package com.coomix.app.pay;

import android.content.Context;
import android.widget.Toast;
import com.coomix.app.all.R;
import com.coomix.app.all.common.Keys;
import com.coomix.app.all.model.response.RespPlatOrder;
import com.tencent.mm.opensdk.modelpay.PayReq;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;

/**
 * Created by ssl on 2017/1/20.
 */
public class WechatPayManager implements ICoomixPay {
    private IWXAPI api = null;
    private Context mContext = null;

    //微信App支付
    public static final int WECHAT_PAY_APP = 1;
    //微信公众号web支付
    public static final int WECHAT_PAY_WEB = 2;
    //微信扫码支付
    public static final int WECHAT_PAY_SCAN_QRCODE = 3;
    //微信刷卡支付
    public static final int WECHAT_PAY_CARD = 4;

    public WechatPayManager(Context context) {
        this.mContext = context;
    }

    public void registerApp(String appId) {
        api = WXAPIFactory.createWXAPI(mContext, Keys.WEIXIN_APP_ID, true);
        api.registerApp(Keys.WEIXIN_APP_ID);
    }

    @Override
    public void pay(RespPlatOrder.DataBean.WxPayBean bean) {
        try {
            if (api == null) {
                registerApp(bean.getApp_id());
            }
            if (!api.isWXAppInstalled()) {
                Toast.makeText(mContext, R.string.toast_wx_not_installed, Toast.LENGTH_SHORT).show();
                return;
            }

            PayReq payRequest = new PayReq();
            payRequest.appId = bean.getApp_id();
            payRequest.partnerId = bean.getPartner_id();
            payRequest.prepayId = bean.getPrepay_id();
            payRequest.packageValue = bean.getPackageX();//"Sign=WXPay";
            payRequest.nonceStr = bean.getNonce();
            payRequest.timeStamp = bean.getTimestamp();
            payRequest.sign = bean.getSign();
            boolean b = api.sendReq(payRequest);
            if (!b) {
                Toast.makeText(mContext, R.string.toast_launch_wx_fail, Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Toast.makeText(mContext, R.string.pay_recharge_wrong_params, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void pay(CoomixPayRsp coomixPayRsp) {
        if (coomixPayRsp == null) {
            return;
        }

        WechatPayRsp wechatPayRsp = coomixPayRsp.getWechat_rsp();
        if (wechatPayRsp == null) {
            return;
        }
        if (api == null) {
            registerApp(wechatPayRsp.getAppid());
        }
        if (!api.isWXAppInstalled()) {
            Toast.makeText(mContext, R.string.toast_wx_not_installed, Toast.LENGTH_SHORT).show();
            return;
        }

        PayReq payRequest = new PayReq();
        payRequest.appId = wechatPayRsp.getAppid();
        payRequest.partnerId = wechatPayRsp.getPartnerid();
        payRequest.prepayId = wechatPayRsp.getPrepayid();
        payRequest.packageValue = wechatPayRsp.getPackages();//"Sign=WXPay";
        payRequest.nonceStr = wechatPayRsp.getNoncestr();
        payRequest.timeStamp = wechatPayRsp.getTimestamp();
        payRequest.sign = wechatPayRsp.getSign();
        boolean b = api.sendReq(payRequest);
        if (!b) {
            Toast.makeText(mContext, R.string.toast_launch_wx_fail, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void release() {
        if (api != null) {
            api.unregisterApp();
        }
    }
}
