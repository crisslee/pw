package com.coomix.app.all.model.bean;

import java.io.Serializable;

/**
 * Created by ssl
 * 支付信息
 *
 * @since 2017/1/20.
 */
public class BalanceInfo implements Serializable {
    private static final long serialVersionUID = 1L;

    private long balance;   // 零钱余额
    private long android_coin;   // 安卓酷币余额
    private long ios_coin;      // iOS酷币余额（预留）

    public long getBalance() {
        return balance;
    }

    public void setBalance(long balance) {
        this.balance = balance;
    }

    public long getAndroid_coin() {
        return android_coin;
    }

    public void setAndroid_coin(long android_coin) {
        this.android_coin = android_coin;
    }

    public long getIos_coin() {
        return ios_coin;
    }

    public void setIos_coin(long ios_coin) {
        this.ios_coin = ios_coin;
    }
}