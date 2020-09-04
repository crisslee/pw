package com.coomix.app.all.model.response;

import com.google.gson.annotations.SerializedName;

/**
 * Created by ly on 2017/12/20 11:11.
 */

public class RespPlatOrder extends RespBase {
    /**
     * data : {"order_id":3940342,"wx_pay":{"app_id":"wx2d0ee7b347002ad5","partner_id":"1467877402","prepay_id":"wx20171220110705092c855ea40795440502","package":"Sign=WXPay","nonce":"GQFl1iuik6tzWmCQ","timestamp":"1513739225","sign":"1908E92CF3A64AE3EEECA2E9C97B0135"}}
     */

    private DataBean data;

    public DataBean getData() {
        return data;
    }

    public void setData(DataBean data) {
        this.data = data;
    }


    public static class DataBean {
        /**
         * order_id : 3940342
         * wx_pay : {"app_id":"wx2d0ee7b347002ad5","partner_id":"1467877402","prepay_id":"wx20171220110705092c855ea40795440502","package":"Sign=WXPay","nonce":"GQFl1iuik6tzWmCQ","timestamp":"1513739225","sign":"1908E92CF3A64AE3EEECA2E9C97B0135"}
         */

        private int order_id;
        private WxPayBean wx_pay;

        public int getOrder_id() {
            return order_id;
        }

        public void setOrder_id(int order_id) {
            this.order_id = order_id;
        }

        public WxPayBean getWx_pay() {
            return wx_pay;
        }

        public void setWx_pay(WxPayBean wx_pay) {
            this.wx_pay = wx_pay;
        }

        public static class WxPayBean {
            /**
             * app_id : wx2d0ee7b347002ad5
             * partner_id : 1467877402
             * prepay_id : wx20171220110705092c855ea40795440502
             * package : Sign=WXPay
             * nonce : GQFl1iuik6tzWmCQ
             * timestamp : 1513739225
             * sign : 1908E92CF3A64AE3EEECA2E9C97B0135
             */

            @SerializedName(value = "app_id", alternate = { "appid" })
            private String app_id;
            private String partner_id;
            private String prepay_id;
            @SerializedName("package")
            private String packageX;
            private String nonce;
            private String timestamp;
            private String sign;

            public String getApp_id() {
                return app_id;
            }

            public void setApp_id(String app_id) {
                this.app_id = app_id;
            }

            public String getPartner_id() {
                return partner_id;
            }

            public void setPartner_id(String partner_id) {
                this.partner_id = partner_id;
            }

            public String getPrepay_id() {
                return prepay_id;
            }

            public void setPrepay_id(String prepay_id) {
                this.prepay_id = prepay_id;
            }

            public String getPackageX() {
                return packageX;
            }

            public void setPackageX(String packageX) {
                this.packageX = packageX;
            }

            public String getNonce() {
                return nonce;
            }

            public void setNonce(String nonce) {
                this.nonce = nonce;
            }

            public String getTimestamp() {
                return timestamp;
            }

            public void setTimestamp(String timestamp) {
                this.timestamp = timestamp;
            }

            public String getSign() {
                return sign;
            }

            public void setSign(String sign) {
                this.sign = sign;
            }
        }
    }
}
