package com.coomix.app.all.model.response;

import java.io.Serializable;
import java.util.ArrayList;

public class RenewWlCard implements Serializable {
    /**
     *
     */
    private static final long serialVersionUID = 1L;

    private int order_id;
    private RespPlatOrder.DataBean.WxPayBean wx_pay;
    private ArrayList<RenewWlCardDetail> detail;

    public int getOrder_id() {
        return order_id;
    }

    public RespPlatOrder.DataBean.WxPayBean getWx_pay() {
        return wx_pay;
    }

    public ArrayList<RenewWlCardDetail> getDetail() {
        return detail;
    }
}
