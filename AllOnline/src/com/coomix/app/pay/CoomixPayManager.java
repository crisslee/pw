package com.coomix.app.pay;

import android.content.Context;
import com.coomix.app.all.model.response.RespPlatOrder;

/**
 * Created by ssl on 2017/1/20.
 */

public class CoomixPayManager implements ICoomixPay {
    private ICoomixPay coomixPay = null;
    private int iPayPlatform = 0;
    private int iPayManner = 0;

    public CoomixPayManager(Context context, int iPayPlatform) {
        this.iPayPlatform = iPayPlatform;
        if (iPayPlatform == PAY_WECHAT) {
            iPayManner = WechatPayManager.WECHAT_PAY_APP;
            coomixPay = new WechatPayManager(context);
        } else if (iPayPlatform == PAY_GOOME) {
            //直接发送支付请求即可，无需到此处
            iPayManner = GoomePayRsp.POCKET_PAY_APP;
            //coomixPay = new GoomePayManager(context);
        }
    }

    public int getPayPlatform() {
        return iPayPlatform;
    }

    public int getPayManner() {
        return iPayManner;
    }

    @Override
    public void registerApp(String appId) {
        coomixPay.registerApp(appId);
    }

    @Override
    public void pay(CoomixPayRsp coomixPayRsp) {
        if (coomixPay != null) {
            coomixPay.pay(coomixPayRsp);
        }
    }

    @Override
    public void pay(RespPlatOrder.DataBean.WxPayBean respPlatOrder) {
        if (coomixPay != null) {
            coomixPay.pay(respPlatOrder);
        }
    }

    @Override
    public void release() {
        if (coomixPay != null) {
            coomixPay.release();
        }
    }
}
