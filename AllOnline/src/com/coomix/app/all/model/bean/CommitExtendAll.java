package com.coomix.app.all.model.bean;

import java.io.Serializable;
import java.util.ArrayList;

public class CommitExtendAll implements Serializable {
    private static final long serialVersionUID = 1L;

    private ArrayList<CommitPriceRuleCnt> price_rule_cnt; // 每种套餐选择的数量，与price_rule对应

    private ArrayList<CommitExtendItem> own_info; // 本人信息，数组格式

    private int own_price_rule_id; //本人的套餐id

    private ArrayList<ArrayList<CommitExtendItem>> companion_info;    // 同伴信息，数组的数组

    private ArrayList<Integer> companion_price_rule_id;     // 同伴的套餐id，与同伴信息顺序一致

    public ArrayList<CommitPriceRuleCnt> getPrice_rule_cnt() {
        return price_rule_cnt;
    }

    public void setPrice_rule_cnt(ArrayList<CommitPriceRuleCnt> price_rule_cnt) {
        this.price_rule_cnt = price_rule_cnt;
    }

    public ArrayList<CommitExtendItem> getOwn_info() {
        return own_info;
    }

    public void setOwn_info(ArrayList<CommitExtendItem> own_info) {
        this.own_info = own_info;
    }

    public int getOwn_price_rule_id() {
        return own_price_rule_id;
    }

    public void setOwn_price_rule_id(int own_price_rule_id) {
        this.own_price_rule_id = own_price_rule_id;
    }

    public ArrayList<ArrayList<CommitExtendItem>> getCompanion_info() {
        return companion_info;
    }

    public void setCompanion_info(ArrayList<ArrayList<CommitExtendItem>> companion_info) {
        this.companion_info = companion_info;
    }

    public ArrayList<Integer> getCompanion_price_rule_id() {
        return companion_price_rule_id;
    }

    public void setCompanion_price_rule_id(ArrayList<Integer> companion_price_rule_id) {
        this.companion_price_rule_id = companion_price_rule_id;
    }
}
