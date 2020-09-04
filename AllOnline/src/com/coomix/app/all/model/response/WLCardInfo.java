package com.coomix.app.all.model.response;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class WLCardInfo implements Serializable {
    /**
     *
     */
    private static final long serialVersionUID = 1L;

    private String msisdn;
    private int combo_id;
    private String batch;
    @SerializedName("package")
    private String package_content;
    private int card_status;
    private int price;  //单位分
    private String c_out_time;
    private String pay_msg;

    public String getMsisdn() {
        return msisdn;
    }

    public int getCombo_id() {
        return combo_id;
    }

    public String getBatch() {
        return batch;
    }

    public String getPackage_content() {
        return package_content;
    }

    public int getCard_status() {
        return card_status;
    }

    public int getPrice() {
        return price;
    }

    public String getC_out_time() {
        return c_out_time;
    }

    public String getPay_msg() {
        return pay_msg;
    }
}
