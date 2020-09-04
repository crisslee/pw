package com.coomix.app.pay;

import java.io.Serializable;

/**
 * Created by ssl on 2017/1/21.
 */
public class OrderStatusRsp implements Serializable {
    private static final long serialVersionUID = 1L;
    /** 已收到用户提交的订单请求 */
    public static final int ORDER_COMMITED_REQ = 0;
    /** 未支付 */
    public static final int ORDER_NOT_PAY = 2;
    /** 已支付 */
    public static final int ORDER_ALREADY_PAYED = 3;
    /** 已发货 */
    public static final int ORDER_ALREADY_SHIPPED = 4;
    /** 完成 */
    public static final int ORDER_FINISHED = 5;
    /** 取消 */
    public static final int ORDER_CANCELED = 6;
    /** 超时未支付失效 */
    public static final int ORDER_TIMEOUT_INVALID = 7;

    private long order_id;
    /***0=已收到用户提交的订单请求;  2=未支付; 3=已支付; 4=已发货; 5=完成; 6=取消; 7=超时未支付失效。其他值为“未知状态”（此处的状态枚举值与订单系统中的定义一致）*/
    private int order_status;

    public long getOrder_id() {
        return order_id;
    }

    public void setOrder_id(long order_id) {
        this.order_id = order_id;
    }

    public int getOrder_status() {
        return order_status;
    }

    public void setOrder_status(int order_status) {
        this.order_status = order_status;
    }
}
