package com.coomix.app.all.model.bean;

import java.io.Serializable;

/**
 * Created by ssl
 * 支付信息
 *
 * @since 2017/1/20.
 */
public class ActPay implements Serializable {
    private static final long serialVersionUID = 1L;

    /******  未知  初始状态******/
    public static final int UNKNOWN = 0;
    /******  收到用户提交的订单请求后 新建订单-待支付 ******/
    public static final int PAY_NEW = 1;
    /******  未支付  ******/
    public static final int PAY_PREPAY = 2;
    /******  已支付  ******/
    public static final int PAY_PAID = 3;
    /******  已发货  ******/
    public static final int PAY_SHIPPED = 4;
    /******  完成  ******/
    public static final int PAY_COMPLETE = 5;
    /******  取消  ******/
    public static final int PAY_CANCLED = 6;
    /******  超时未支付失效  ******/
    public static final int PAY_TIMEOUT_INVALID = 7;

    //订单编号
    private long order_id;
    //当前用户对本次活动的支付状态，0=未知；1=待支付；2=已支付，进行中；3=已结束；4=已取消
    private int pay_status;
    //订单创建时间，UTC时间
    private long pay_order_create_time;
    //付款剩余时间（秒）
    private int pay_left_time;
    // 待支付的总金额
    private int total_fee;
    //支付方式，取值在ICoomixPay接口中定义
    private int pay_platform;
    private int pay_manner;

    public long getOrder_id() {
        return order_id;
    }

    public int getPay_status() {
        return pay_status;
    }

    public void setPay_status(int pay_status) {
        this.pay_status = pay_status;
    }

    public long getPay_order_create_time() {
        return pay_order_create_time;
    }

    public int getPay_left_time() {
        return pay_left_time;
    }

    public int getTotal_fee() {
        return total_fee;
    }

    public void setTotal_fee(int total_fee) {
        this.total_fee = total_fee;
    }

    public int getPay_platform() {
        return pay_platform;
    }

    public void setPay_platform(int pay_platform) {
        this.pay_platform = pay_platform;
    }
}