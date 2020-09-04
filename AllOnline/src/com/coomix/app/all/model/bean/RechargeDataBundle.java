package com.coomix.app.all.model.bean;

import com.coomix.app.all.model.response.RespPlatOrder;
import java.io.Serializable;

/**
 * File Description:
 *
 * @author felixqiu
 * @since 2019/3/19.
 */
public class RechargeDataBundle implements Serializable {
    private static final long serialVersionUID = 7336015762984067392L;
    private int pay_order_id;
    private RespPlatOrder.DataBean.WxPayBean wechat_rsp;

    public int getOrder_id() {
        return pay_order_id;
    }

    public void setOrder_id(int order_id) {
        this.pay_order_id = order_id;
    }

    public RespPlatOrder.DataBean.WxPayBean getWx_pay() {
        return wechat_rsp;
    }

    public void setWx_pay(RespPlatOrder.DataBean.WxPayBean wx_pay) {
        this.wechat_rsp = wx_pay;
    }
}
