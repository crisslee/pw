package com.coomix.app.pay;

import com.coomix.app.all.model.bean.ActPay;
import com.coomix.app.all.service.Error;
import java.io.Serializable;

/**
 * Created by ssl on 2017/1/21.
 */

public class CoomixPayRsp implements Serializable {
    private static final long serialVersionUID = 1L;
    private String redpacket_id;//红包支付才有此项，红包id
    private long order_id; //订单支付才有此项，订单id
    private WechatPayRsp wechat_rsp; //微信支付
    private AliPayRsp alipay_rsp;  //ali支付
    private GoomePayRsp goome_rsp; //零钱支付
    private ActPay pay;
    private Error error;

    public long getOrder_id() {
        return order_id;
    }

    public void setOrder_id(long order_id) {
        this.order_id = order_id;
    }

    public WechatPayRsp getWechat_rsp() {
        return wechat_rsp;
    }

    public void setWechat_rsp(WechatPayRsp wechat_rsp) {
        this.wechat_rsp = wechat_rsp;
    }

    public AliPayRsp getAlipay_rsp() {
        return alipay_rsp;
    }

    public void setAlipay_rsp(AliPayRsp alipay_rsp) {
        this.alipay_rsp = alipay_rsp;
    }

    public ActPay getPay() {
        return pay;
    }

    public void setPay(ActPay pay) {
        this.pay = pay;
    }

    public String getRedpacket_id() {
        return redpacket_id;
    }

    public void setRedpacket_id(String redpacket_id) {
        this.redpacket_id = redpacket_id;
    }

    public GoomePayRsp getGoome_rsp() {
        return goome_rsp;
    }

    public void setGoome_rsp(GoomePayRsp goome_rsp) {
        this.goome_rsp = goome_rsp;
    }

    public Error getError() {
        return error;
    }

    public void setError(Error error) {
        this.error = error;
    }
}
