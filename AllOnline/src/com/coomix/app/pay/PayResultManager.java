package com.coomix.app.pay;

import android.app.Activity;
import com.coomix.app.all.model.bean.ActOrderInfo;

/**
 * Created by ssl on 2017/1/22.
 */
public class PayResultManager {
    private static PayResultManager instance = null;
    private int iActId = 0;
    private ActOrderInfo actOrderInfo = null;
    private Activity willFinishActivity = null;
    private boolean bFromSigned = false;
    private ICoomixPay.ORDER_FROM order_from = ICoomixPay.ORDER_FROM.FROM_ACTIVITY_ORDER;
    private boolean bRedPacketPayedOk = false;

    private long rechargeAmount = 0;
    private long order_id = 0;
    private String rechargeFrom = null;

    private PayResultManager() {

    }

    public synchronized static PayResultManager getInstance() {
        if (instance == null) {
            instance = new PayResultManager();
        }
        return instance;
    }

    public void copyOrderInfo(int iActId, ActOrderInfo actOrderInfo) {
        this.iActId = iActId;
        this.actOrderInfo = actOrderInfo;
    }

    public ActOrderInfo getActOrderInfo() {
        return actOrderInfo;
    }

    public int getActId() {
        return iActId;
    }

    public void setWillFinishActivity(Activity activity) {
        this.willFinishActivity = activity;
    }

    public Activity getWillFinishActivity() {
        return willFinishActivity;
    }

    /**
     * 设置是否从原生活动报名->支付
     */
    public void setbFromSigned(boolean bFromSigned) {
        this.bFromSigned = bFromSigned;
    }

    /**
     * 是否从原生活动报名->支付
     */
    public boolean isbFromSigned() {
        return bFromSigned;
    }

    public void setPayOrderType(ICoomixPay.ORDER_FROM order_from) {
        this.order_from = order_from;
    }

    public ICoomixPay.ORDER_FROM getPayOrderType() {
        return order_from;
    }

    public void setRedPacketPayedOk(boolean bOk) {
        this.bRedPacketPayedOk = bOk;
    }

    public boolean isRedPacketPayedOk() {
        return bRedPacketPayedOk;
    }

    public long getRechargeAmount() {
        return rechargeAmount;
    }

    public void setRechargeAmount(long rechargeAmount) {
        this.rechargeAmount = rechargeAmount;
    }

    public long getOrder_id() {
        return order_id;
    }

    public void setOrder_id(long order_id) {
        this.order_id = order_id;
    }

    public String getRechargeFrom() {
        return rechargeFrom;
    }

    public void setRechargeFrom(String rechargeFrom) {
        this.rechargeFrom = rechargeFrom;
    }

    /*_________________________支付结果回调______________START____________*/
    private OnPayResultCallback callback;

    public interface OnPayResultCallback {
        public void onPayResult(boolean bOk, String msg, int code);
    }

    public void setOnPayResultCallback(OnPayResultCallback callback) {
        this.callback = callback;
    }

    public OnPayResultCallback getOnPayResultCallback() {
        return callback;
    }
    /*_________________________支付结果回调______________END____________*/
}
