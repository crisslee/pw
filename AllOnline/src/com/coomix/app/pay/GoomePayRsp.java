package com.coomix.app.pay;

import java.io.Serializable;

/***
 * 酷米客零钱支付
 * **/
public class GoomePayRsp implements Serializable {
    //酷米客零钱 -- App支付
    public static final int POCKET_PAY_APP = 5;

    private static final long serialVersionUID = 1L;
    private long money_left;  //零钱余额

    public long getMoney_left() {
        return money_left;
    }

    public void setMoney_left(long money_left) {
        this.money_left = money_left;
    }
}
