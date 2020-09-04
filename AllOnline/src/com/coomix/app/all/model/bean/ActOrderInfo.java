package com.coomix.app.all.model.bean;

import com.google.gson.annotations.Expose;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by think
 * 活动订单信息
 *
 * @since 2017/1/21.
 */
public class ActOrderInfo implements Serializable {
    private static final long serialVersionUID = 1L;
    //----活动信息summary----
    @Expose
    private long order_id;
    @Expose
    private String chatroom_id;
    private String title;
    private long begtime;
    private long endtime;
    private long deadline;
    private String location;
    private String pic;
    private int applynum;
    private int maxnum;
    private String cost;
    //价格
    private ActPrice price;
    private int jump_type;
    private String jump_url;
    private int status;
    private Share share;
    //综合状态
    private int general_status;

    //套餐信息
    //本人套餐
    private int own_price_rule_id;
    //同伴套餐
    private int[] companion_price_rule_id;

    //----报名信息----（本人信息）
    private ArrayList<ActCommitItem> commit_items;
    private ArrayList<ActDisplay> display;
    //----同伴信息----
    private ArrayList<ArrayList<CommitExtendItem>> companion_info;

    //----订单信息----
    private ActPay pay;

    private String begin_date_desc; //起始时间描述
    private String end_date_desc;//结束时间描述

    public long getOrder_id() {
        return order_id;
    }

    public String getChatroom_id() {
        return chatroom_id;
    }

    public void setChatroom_id(String chatroom_id) {
        this.chatroom_id = chatroom_id;
    }

    public String getTitle() {
        return title;
    }

    public long getBegtime() {
        return begtime;
    }

    public long getEndtime() {
        return endtime;
    }

    public String getLocation() {
        return location;
    }

    public String getPic() {
        return pic;
    }

    public String getCost() {
        return cost;
    }

    public ActPrice getPrice() {
        return price;
    }

    public int getStatus() {
        return status;
    }

    public int getGeneral_status() {
        return general_status;
    }

    public int getOwn_price_rule_id() {
        return own_price_rule_id;
    }

    public int[] getCompanion_price_rule_id() {
        return companion_price_rule_id;
    }

    public List<ActCommitItem> getCommit_items() {
        return commit_items;
    }

    public ArrayList<ActDisplay> getDisplay() {
        return display;
    }

    public ArrayList<ArrayList<CommitExtendItem>> getCompanion_info() {
        return companion_info;
    }

    public ActPay getPay() {
        return pay;
    }

    public String getBegin_date_desc() {
        return begin_date_desc;
    }

    public void setBegin_date_desc(String begin_date_desc) {
        this.begin_date_desc = begin_date_desc;
    }

    public String getEnd_date_desc() {
        return end_date_desc;
    }

    public void setEnd_date_desc(String end_date_desc) {
        this.end_date_desc = end_date_desc;
    }
}
