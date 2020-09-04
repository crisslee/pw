package com.coomix.app.pay;

import com.coomix.app.all.model.response.RespPlatOrder;

/**
 * Created by ssl on 2017/1/20.
 */
public interface ICoomixPay {
    //public static final String COOMIX_SIGNATURE = "bc955d49075b12b9ae7e997ce6ca2";
    //public static final String COOMIX_SIGNATURE_DEBUG = "F232CB19A3358D91562F1E7B5A3FD8BD";

    /***
     * 微信支付
     **/
    public static final int PAY_WECHAT = 1;
    /***
     * 支付宝支付
     **/
    public static final int PAY_ALI = 2;
    /***
     * 谷米零钱支付
     **/
    public static final int PAY_GOOME = 3;

    public enum ORDER_FROM {
        /****
         * 原生活动的报名支付
         *****/
        FROM_UNKNOW,

        /****
         * 原生活动的报名支付
         *****/
        FROM_ACTIVITY_ORDER,

        /****
         * 红包的支付
         *****/
        FROM_REDPACKET,

        /**
         * 零钱充值
         */
        FROM_RECHARGE_BALANCE,
        /**
         * 平台设备充值
         */
        FROM_RECHARGE_PLATFORM_DEVICES,

        /**
         * 物联卡充值
         */
        FROM_RECHARGE_WLCARD,

        /**
         * 流量充值
         */
        FROM_RECHARGE_DATA_BUNDLE,

        /**
         * 游戏钻石充值
         */
        FROM_GAME_H5
    }

    public void pay(CoomixPayRsp coomixPayRsp);

    public void pay(RespPlatOrder.DataBean.WxPayBean resp);

    public void registerApp(String appId);

    public void release();
}
