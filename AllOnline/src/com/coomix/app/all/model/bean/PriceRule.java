package com.coomix.app.all.model.bean;

import java.io.Serializable;

/**
 * @author shishenglong
 * @since 2016年10月15日 下午3:51:43
 */
public class PriceRule implements Serializable {

    private static final long serialVersionUID = 1L;

    private int id;          // 套餐ID
    private String name; // 套餐名
    private int price;       // 选择该套餐的价格，单位（分）
    private int max_cnt;      // 数量限制，最大值

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public int getMax_cnt() {
        return max_cnt;
    }

    public void setMax_cnt(int max_cnt) {
        this.max_cnt = max_cnt;
    }
}
