package com.coomix.app.all.model.bean;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

/**
 * File Description:
 *
 * @author felixqiu
 * @since 2019/3/18.
 */
public class DataItem implements Serializable {
    private static final long serialVersionUID = 2305063272611996634L;
    private long id;
    //价格
    private int amount;
    //流量
    @SerializedName("bundle_size")
    private int size;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }
}
