package com.coomix.app.all.model.response;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class RenewWlCardDetail implements Serializable {
    /**
     *
     */
    private static final long serialVersionUID = 1L;

    private int combo_id;
    private String batch;
    @SerializedName("package")
    private String package_content;
    private int num;
    private int price;
    private long total_price;

    public int getCombo_id() {
        return combo_id;
    }

    public String getBatch() {
        return batch;
    }

    public String getPackage_content() {
        return package_content;
    }

    public int getNum() {
        return num;
    }

    public int getPrice() {
        return price;
    }

    public long getTotal_price() {
        return total_price;
    }
}
