package com.coomix.app.all.wxapi;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.coomix.app.all.AllOnlineApp;
import com.coomix.app.all.R;
import com.coomix.app.all.model.bean.ActOrderInfo;
import com.coomix.app.all.widget.MyActionbar;
import com.coomix.app.framework.app.Result;
import com.coomix.app.pay.OrderStatusRsp;
import com.coomix.app.pay.PayResultManager;
import com.coomix.app.all.util.CommunityUtil;
import com.tencent.mm.opensdk.constants.ConstantsAPI;
import com.tencent.mm.opensdk.modelbase.BaseReq;
import com.tencent.mm.opensdk.modelbase.BaseResp;

/**
 * Created by ssl on 2017/2/18.
 */

public class CoomixPayActivity extends CoomixPayBase {
    private ImageView imageStatus = null;
    private TextView textStatus = null;
    private TextView textCost = null;
    private TextView textNote = null;
    private int iTimeCount = 3;
    private int iTaskId = -1;
    private MyActionbar actionbar;

    @SuppressWarnings("HandlerLeak")
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    if (iTimeCount >= 0) {
                        textNote.setText(mContext.getString(R.string.jump_decrease_time, iTimeCount));
                        iTimeCount--;
                        if (iTimeCount < 0) {
                            finishAndGoToActivity();
                            return;
                        }
                        mHandler.sendEmptyMessageDelayed(0, 1000);
                    }
                    break;
                case 1:
                    callback(iTaskId, (Result) msg.obj);
                    break;
            }
            super.handleMessage(msg);
        }
    };

    public CoomixPayActivity(Context context, Intent intent, FinishListener finishListaner) {
        super(context, intent, finishListaner);
    }

    private void initViews() {
        imageStatus = (ImageView) mainView.findViewById(R.id.imageViewStatus);
        textStatus = (TextView) mainView.findViewById(R.id.textViewStatus);
        textCost = (TextView) mainView.findViewById(R.id.textViewCost);
        textNote = (TextView) mainView.findViewById(R.id.textViewToast);

        actionbar = (MyActionbar) mainView.findViewById(R.id.myActionbar);
        actionbar.initActionbar(true, "", 0, 0);
        actionbar.setLeftImageClickListener(view -> finishAndGoToActivity());
    }

    @Override
    public View getMainView() {
        mainView = LayoutInflater.from(mContext).inflate(R.layout.activity_pay_status, null);
        initViews();
        return mainView;
    }

    private void setUiData(int status) {
        switch (status) {
            /**已收到用户提交的订单请求*/
            case OrderStatusRsp.ORDER_COMMITED_REQ:
                break;

            /**未支付*/
            case OrderStatusRsp.ORDER_NOT_PAY:
                showToast(mContext, R.string.order_not_pay);
                break;

            /**已支付*/
            case OrderStatusRsp.ORDER_ALREADY_PAYED:
                /**已发货*/
            case OrderStatusRsp.ORDER_ALREADY_SHIPPED:
                /**完成*/
            case OrderStatusRsp.ORDER_FINISHED:
                actionbar.setTitle(R.string.payed_success);
                imageStatus.setImageResource(R.drawable.pay_success);
                textStatus.setText(R.string.payed_success);
                Activity activity = PayResultManager.getInstance().getWillFinishActivity();
                if (activity != null) {
                    //结束掉确认支付界面
                    activity.setResult(Activity.RESULT_OK);
                    activity.finish();
                    PayResultManager.getInstance().setWillFinishActivity(null);
                }
                ActOrderInfo actorderInfo = PayResultManager.getInstance().getActOrderInfo();
                if (actorderInfo != null && actorderInfo.getPay() != null) {
                    if (actorderInfo.getPay().getTotal_fee() > 0) {
                        textCost.setText(mContext.getString(R.string.order_fee) + CommunityUtil
                            .getMoneyStrByIntDecimal(mContext, actorderInfo.getPay().getTotal_fee(), 2));
                    }
                }
                mHandler.sendEmptyMessage(0);
                return;

            /**取消*/
            case OrderStatusRsp.ORDER_CANCELED:
                showToast(mContext, R.string.order_cancelled);
                break;

            /**超时未支付失效*/
            case OrderStatusRsp.ORDER_TIMEOUT_INVALID:
                showToast(mContext, R.string.order_timeout);
                break;
            default:
                break;
        }
        finish();
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
                    //活动支付---需要向我们的服务器请求一次就行确认订单支付状态，如果服务器不返回则以微信结果为准
                    //result = R.string.wx_errcode_success;
                    showWaitInfoDialog(mContext, mContext.getString(R.string.check_ing));
                    requestCoomixOrderStatus();
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

    private void finishAndGoToActivity() {
        //if (PayResultManager.getInstance().isbFromSigned())
        //{
        //    //从原生活动的报名->支付，需要跳转到我的活动中
        //    mContext.startActivity(new Intent(mContext, MyActivityActivity.class));
        //}
        //else
        //{
        //    //从“我的”页面中“活动内容”中过来，跳转到”我的活动“ 中的该活动的订单详情---直接结束该页面即可
        //    //刷新我的活动列表
        //    MyActivityActivity.shouldRefresh = true;
        //}
        finish();
    }

    private void redpacketPayedOk() {
        Activity activity = PayResultManager.getInstance().getWillFinishActivity();
        if (activity != null) {
            //结束掉之前的界面
            activity.setResult(Activity.RESULT_OK);
            activity.finish();
            PayResultManager.getInstance().setWillFinishActivity(null);
        }
        PayResultManager.getInstance().setRedPacketPayedOk(true);
        finish();
    }

    private void requestCoomixOrderStatus() {
        //iTaskId = serviceAdapter.getRefreshPayOrderStatus(this.hashCode(), PayResultManager.getInstance().getActId(),
        //                PayResultManager.getInstance().getActOrderInfo().getOrder_id());

        Runnable myRunnable = new Runnable() {
            @Override
            public void run() {
                Result result = AllOnlineApp.mApiClient.getRefreshPayOrderStatus(this.hashCode(),
                    PayResultManager.getInstance().getActId(),
                    PayResultManager.getInstance().getActOrderInfo().getOrder_id());
                Message msg = new Message();
                msg.obj = result;
                msg.what = 1;
                mHandler.sendMessage(msg);
            }
        };
        new Thread(myRunnable).start();
    }

    @Override
    public void callback(int messageId, Result result) {
        if (messageId == iTaskId) {
            dismissWaitDialog();
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

    @Override
    protected void finish() {
        super.finish();
    }

    @Override
    public void release() {
        super.release();
        if (mHandler != null) {
            mHandler.removeCallbacksAndMessages(null);
            mHandler = null;
        }
    }

    @Override
    protected void onBackFinish() {
        Activity activity = PayResultManager.getInstance().getWillFinishActivity();
        if (activity != null) {
            //结束掉确认支付界面
            activity.setResult(Activity.RESULT_OK);
            activity.finish();
            PayResultManager.getInstance().setWillFinishActivity(null);
        }
    }

    @Override
    protected void onBackPressed() {
    }
}
