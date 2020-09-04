package com.coomix.app.all.model.bean;

import java.io.Serializable;

/**
 * Created by think
 * 我的活动列表中的活动信息
 *
 * @since 2017/1/19.
 */
public class MyActivity implements Serializable {
    private static final long serialVersionUID = 3756363674647362L;

    /******  待支付  ******/
    public static final int PAY_WAITTING = 0;
    /******  已支付，进行中  ******/
    public static final int PAYED = 1;
    /******  已结束  ******/
    public static final int ENDED = 2;
    /******  已取消  ******/
    public static final int CANCELED = 3;

    private int id;
    private String title;
    private long begtime;
    private long endtime;
    private long deadline;
    private String cost;
    //价格
    private ActPrice price;
    private String location;
    // 宣传图
    private String pic;
    // 0:已结束,1:已截止,2:已满员,3:可报名
    private int status;
    //跳转类型，0-原生，1-H5
    private int jump_type;
    // 跳转到H5的url
    private String jump_url;

    private int general_status;

    //支付相关信息
    private ActPay pay;

    public int getId() {
        return id;
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

    public String getCost() {
        return cost;
    }

    public ActPrice getPrice() {
        return price;
    }

    public String getLocation() {
        return location;
    }

    public String getPic() {
        return pic;
    }

    public ActPay getPay() {
        return pay;
    }

    public static long getSerialVersionUID() {
        return serialVersionUID;
    }

    public int getStatus() {
        return status;
    }

    public int getGeneral_status() {
        return general_status;
    }
}
