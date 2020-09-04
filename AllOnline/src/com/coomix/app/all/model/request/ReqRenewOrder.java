package com.coomix.app.all.model.request;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by ly on 2017/12/19 11:30.
 */
public class ReqRenewOrder {
    private long amount = 0;
    private int pay_plat = 0;
    private int pay_manner = 0;
    private List<DevRenewBean> renew_info = new LinkedList<>();

    public boolean isValid() {
        if (renew_info.size() == 0) {
            return false;
        } else {
            return true;
        }
    }

    public List<DevRenewBean> getRenewItemsInfo() {
        return renew_info;
    }

    public long getAmount() {
        return amount;
    }

    public void setAmount(long amount) {
        this.amount = amount;
    }

    public int getPay_plat() {
        return pay_plat;
    }

    public void setPay_plat(int pay_plat) {
        this.pay_plat = pay_plat;
    }

    public int getPay_manner() {
        return pay_manner;
    }

    public void setPay_manner(int pay_manner) {
        this.pay_manner = pay_manner;
    }

    public static class DevRenewBean {
        //{uid:1234,imei:"1234",fee_type:0,fee_amount:50},
        private long uid;
        private String imei;
        private int fee_type;
        private long fee_amount;

        public String getImei() {
            return imei;
        }

        public void setImei(String imei) {
            this.imei = imei;
        }

        public int getFee_type() {
            return fee_type;
        }

        public void setFee_type(int fee_type) {
            this.fee_type = fee_type;
        }

        public long getFee_amount() {
            return fee_amount;
        }

        public void setFee_amount(long fee_amount) {
            this.fee_amount = fee_amount;
        }

        public long getUid() {
            return uid;
        }

        public void setUid(long uid) {
            this.uid = uid;
        }
    }
}
