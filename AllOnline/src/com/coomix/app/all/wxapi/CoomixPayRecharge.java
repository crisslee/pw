package com.coomix.app.all.wxapi;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.coomix.app.all.Constant;
import com.coomix.app.all.GlobalParam;
import com.coomix.app.all.R;
import com.coomix.app.all.widget.MyActionbar;
import com.coomix.app.framework.app.Result;
import com.coomix.app.framework.util.CommonUtil;
import com.coomix.app.all.data.BaseSubscriber;
import com.coomix.app.all.data.DataEngine;
import com.coomix.app.all.data.ExceptionHandle;
import com.coomix.app.all.data.RxUtils;
import com.coomix.app.all.model.response.RespRefreshOrder;
import com.coomix.app.pay.ICoomixPay;
import com.coomix.app.pay.OrderStatusRsp;
import com.coomix.app.pay.PayResultManager;
import com.coomix.app.all.util.CommunityUtil;
import com.tencent.mm.opensdk.constants.ConstantsAPI;
import com.tencent.mm.opensdk.modelbase.BaseReq;
import com.tencent.mm.opensdk.modelbase.BaseResp;

import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;

/**
 * Created by felixqiu
 *
 * @since 2017/2/18.
 */
public class CoomixPayRecharge extends CoomixPayBase {
    private Context mContext;
    private Resources mRes;

    private TextView txtFinished;
    private View rlWxid;
    private View rlAmount;
    private TextView txtWxid;
    private TextView wxid;
    private TextView tip;
    private TextView time;

    private int iRechargeStatusTaskId;
    private long order_id = 0;
    private String ticket;
    private String rechargeFrom;

    private int coundDown = 3;
    private static final int UPDATE_TIME = 0;

    private CompositeDisposable mCompositeDisposable = new CompositeDisposable();

    private Handler mh = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case UPDATE_TIME:
                    if (coundDown >= 0) {
                        time.setText(mContext.getString(R.string.jump_decrease_time, coundDown));
                        coundDown--;
                        if (coundDown < 0) {
                            goBack();
                            return;
                        }
                        mh.sendEmptyMessageDelayed(UPDATE_TIME, 1000);
                    }
                    break;
            }
            super.handleMessage(msg);
        }
    };

    public CoomixPayRecharge(Context context, Intent intent, FinishListener listener) {
        super(context, intent, listener);
        this.mContext = context;
        mRes = mContext.getApplicationContext().getResources();
        order_id = PayResultManager.getInstance().getOrder_id();
        ticket = CommonUtil.getTicket();
        rechargeFrom = PayResultManager.getInstance().getRechargeFrom();
        PayResultManager.getInstance().setRechargeFrom(null);
    }

    @Override
    public View getMainView() {
        mainView = LayoutInflater.from(mContext).inflate(R.layout.activity_withdraw_deposit_success, null);
        initViews();
        return mainView;
    }

    private void initViews() {
        MyActionbar actionbar = (MyActionbar) mainView.findViewById(R.id.myActionbar);
        actionbar.initActionbar(true, "", 0, 0);

        ((ImageView) mainView.findViewById(R.id.image)).setImageResource(R.drawable.withdraw_success);

        txtFinished = (TextView) mainView.findViewById(R.id.finished);
        txtFinished.setText(mRes.getString(R.string.recharge_success));
        rlWxid = mainView.findViewById(R.id.rl_wxid);
        rlAmount = mainView.findViewById(R.id.rl_amount);
        rlAmount.setVisibility(View.GONE);
        txtWxid = (TextView) mainView.findViewById(R.id.wechat_id_text);
        txtWxid.setText(mRes.getString(R.string.recharge_money));
        wxid = (TextView) mainView.findViewById(R.id.wechat_id);
        tip = (TextView) mainView.findViewById(R.id.tip);
        tip.setVisibility(View.GONE);
        time = (TextView) mainView.findViewById(R.id.time);
    }

    @Override
    protected void onNewIntent(Intent intent) {

    }

    @Override
    public void onReq(BaseReq baseReq) {

    }

    @Override
    public void onResp(BaseResp baseResp) {
        if (baseResp.getType() == ConstantsAPI.COMMAND_PAY_BY_WX) {
            int result = 0;
            switch (baseResp.errCode) {
                case BaseResp.ErrCode.ERR_OK:
                    if (PayResultManager.getInstance().getPayOrderType() == ICoomixPay.ORDER_FROM.FROM_RECHARGE_BALANCE) {
                        //零钱充值---需要向我们的服务器请求一次就行确认订单支付状态，如果服务器不返回则以微信结果为准
                        showWaitInfoDialog(mContext, mContext.getString(R.string.check_ing));
                        requestRechargeStatus();
                    }
                    return;

                case BaseResp.ErrCode.ERR_USER_CANCEL:
                    result = R.string.wx_errcode_cancel;
                    break;

                case BaseResp.ErrCode.ERR_AUTH_DENIED:
                    result = R.string.wx_errcode_deny;
                    break;

                case BaseResp.ErrCode.ERR_UNSUPPORT:
                    result = R.string.wx_errcode_unsupported;
                    break;

                default:
                    //result = R.string.wx_errcode_unknown;
                    break;
            }
            if (result > 0) {
                Toast.makeText(mContext, result, Toast.LENGTH_SHORT).show();
            }
            finish();
        }
    }

    private void requestRechargeStatus() {
        //查询充值状态
//        iRechargeStatusTaskId = serviceAdapter.getRechargeStatus(this.hashCode(), ticket, order_id);
        Disposable disposable = DataEngine.getCommunityApi()
                .refreshOrder(GlobalParam.getInstance().getCommonParas(), order_id)
                .compose(RxUtils.toMain())
                .onErrorResumeNext(new RxUtils.HttpResponseFunc<>())
                .subscribeWith(new BaseSubscriber<RespRefreshOrder>(null) {

                    @Override
                    public void onNext(RespRefreshOrder response) {
                        if (response.isSuccess()) {
                            try {
                                setUiData(response.getData().getOrder_status());

                            } catch (Exception e) {
                                showToast(mContext, R.string.wx_errcode_success);
                                finish();
                            }
                        } else {
                            if (response.getErrcode() == Result.ERRCODE_ORDER_TIMEOUT_INVALID) {
                                dismissWaitDialog();
                                showToast(mContext, R.string.order_timeout);
                                finish();
                            } else {
                                onError(new ExceptionHandle.ServerException(response));
                            }
                        }
                    }

                    @Override
                    public void onHttpError(ExceptionHandle.ResponeThrowable e) {
                        dismissWaitDialog();
                        Toast.makeText(mContext, e.getErrCodeMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
        mCompositeDisposable.add(disposable);

    }

    @Override
    public void release() {
        super.release();
        if (mh != null) {
            mh.removeCallbacksAndMessages(null);
            mh = null;
        }
        mCompositeDisposable.clear();
    }

    private void goBack() {
        finish();
    }

    @Override
    public void callback(int messageId, Result result) {
        if (messageId == iRechargeStatusTaskId) {
            dismissWaitDialog();
            if (result.apiCode == Constant.FM_APIID_GET_RECHARGE_STATUS) {
                if (result.mResult != null && result.success && result.mResult instanceof OrderStatusRsp) {
                    parseOrderPayStatus((OrderStatusRsp) result.mResult);
                    return;
                } else if (!result.success) {
                    switch (result.statusCode) {
                        case Result.ERRCODE_ORDER_TIMEOUT_INVALID:
                            //已经过期了
                            showToast(mContext, R.string.order_timeout);
                            finish();
                            return;
                    }
                }
            }

            //其他，当做支付成功。因为只有微信回调的状态是OK的才会向我们后台发起请求确认
            showToast(mContext, R.string.wx_errcode_success);
            setUiData(OrderStatusRsp.ORDER_ALREADY_PAYED);
        }
    }

    private void parseOrderPayStatus(OrderStatusRsp orderStatusRsp) {
        if (orderStatusRsp != null) {
            setUiData(orderStatusRsp.getOrder_status());
        } else {
            showToast(mContext, R.string.wx_errcode_success);
            finish();
        }
    }

    private void setUiData(int status) {
        switch (status) {
            /**
             * 已收到用户提交的订单请求
             */
            case OrderStatusRsp.ORDER_COMMITED_REQ:
                break;

            /**
             * 未支付
             */
            case OrderStatusRsp.ORDER_NOT_PAY:
                showToast(mContext, R.string.order_not_pay);
                break;

            /**
             * 已支付
             */
            case OrderStatusRsp.ORDER_ALREADY_PAYED:
                /**
                 * 已发货
                 */
            case OrderStatusRsp.ORDER_ALREADY_SHIPPED:
                /**
                 * 完成
                 */
            case OrderStatusRsp.ORDER_FINISHED:
                Activity activity = PayResultManager.getInstance().getWillFinishActivity();
                if (activity != null) {
                    activity.finish();
                    PayResultManager.getInstance().setWillFinishActivity(null);
                }
                long rechargeAmount = PayResultManager.getInstance().getRechargeAmount();
                wxid.setText(
                        mContext.getString(R.string.money_unit) + CommunityUtil.getDecimalStrByLong(rechargeAmount, 2));
                mh.sendEmptyMessage(UPDATE_TIME);
                return;

            /**
             * 取消
             */
            case OrderStatusRsp.ORDER_CANCELED:
                showToast(mContext, R.string.order_cancelled);
                break;

            /**
             * 超时未支付失效
             */
            case OrderStatusRsp.ORDER_TIMEOUT_INVALID:
                showToast(mContext, R.string.order_timeout);
                break;
        }
        finish();
    }

    @Override
    protected void onBackFinish() {

    }

    @Override
    protected void onBackPressed() {
        goBack();
    }
}
