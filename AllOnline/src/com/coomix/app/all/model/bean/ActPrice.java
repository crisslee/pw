package com.coomix.app.all.model.bean;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by ssl on 2017/1/20.
 */
public class ActPrice implements Serializable {
    private static final long serialVersionUID = 1L;
    /** 线下支付，不走支付系统 **/
    public static final int PRICE_OFFLINE = 0;
    /** 固定价格支付 **/
    public static final int PRICE_FASTEN = 1;
    /** 按套餐决定价格支付 **/
    public static final int PRICE_PACKAGE = 2;
    /** 免费活动 **/
    public static final int PRICE_FREE = 3;

    private int type; // 0=线下支付，不走支付系统；1=固定价格；2=按套餐决定价格(4.0.0 APP不支持)；3=AA
    private int fixed_price;  // 单位（分），仅type=1时有效
    private ArrayList<PriceRule> price_rule; // 套餐及价格对应关系，仅type=2有效
    private int total_max_cnt;   // 所有套餐的报名总数限制, -1表示无限制
    private String price_rule_title; //套餐的名字

    private transient int price_index = -1;

    public int getPrice_index() {
        return price_index;
    }

    public void setPrice_index(int price_index) {
        this.price_index = price_index;
    }

    public int getFixed_price() {
        return fixed_price;
    }

    public void setFixed_price(int fixed_price) {
        this.fixed_price = fixed_price;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public ArrayList<PriceRule> getPrice_rule() {
        return price_rule;
    }

    public void setPrice_rule(ArrayList<PriceRule> price_rule) {
        this.price_rule = price_rule;
    }

    public int getTotal_max_cnt() {
        return total_max_cnt;
    }

    public void setTotal_max_cnt(int total_max_cnt) {
        this.total_max_cnt = total_max_cnt;
    }

    public String getPrice_rule_title() {
        return price_rule_title;
    }

    public void setPrice_rule_title(String price_rule_title) {
        this.price_rule_title = price_rule_title;
    }
}
