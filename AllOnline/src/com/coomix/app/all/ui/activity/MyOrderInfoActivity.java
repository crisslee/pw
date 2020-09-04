package com.coomix.app.all.ui.activity;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.CharacterStyle;
import android.util.Log;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.bumptech.GlideApp;
import com.coomix.app.all.AllOnlineApp;
import com.coomix.app.all.Constant;
import com.coomix.app.all.R;
import com.coomix.app.all.model.bean.ActDisplay;
import com.coomix.app.all.model.bean.ActOrderInfo;
import com.coomix.app.all.model.bean.ActPay;
import com.coomix.app.all.model.bean.ActPrice;
import com.coomix.app.all.model.bean.BalanceInfo;
import com.coomix.app.all.model.bean.CommunityAct;
import com.coomix.app.all.model.bean.ImageInfo;
import com.coomix.app.all.model.bean.MyActivity;
import com.coomix.app.all.model.bean.PriceRule;
import com.coomix.app.all.service.Error;
import com.coomix.app.all.service.ServiceAdapter;
import com.coomix.app.all.service.ServiceAdapter.ServiceAdapterCallback;
import com.coomix.app.all.dialog.AskDialog;
import com.coomix.app.all.ui.base.BaseActivity;
import com.coomix.app.all.util.StringUtil;
import com.coomix.app.all.widget.MyActionbar;
import com.coomix.app.all.dialog.NoticeDialog;
import com.coomix.app.all.widget.OrderInfoPicGridView;
import com.coomix.app.framework.app.Result;
import com.coomix.app.framework.util.CommonUtil;
import com.coomix.app.all.dialog.ProgressDialogEx;
import com.coomix.app.pay.CoomixPayManager;
import com.coomix.app.pay.CoomixPayRsp;
import com.coomix.app.pay.GoomePayRsp;
import com.coomix.app.pay.ICoomixPay;
import com.coomix.app.pay.PayResultManager;
import com.coomix.app.pay.WechatPayManager;
import com.coomix.app.all.util.CommunityDateUtil;
import com.coomix.app.all.util.CommunityUtil;
import com.muzhi.camerasdk.PreviewActivity;
import com.muzhi.camerasdk.model.CameraSdkParameterInfo;
import com.muzhi.camerasdk.view.ThumbnailImageView;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by think<br/>
 * 我的订单详情<br/>
 * 这个页面用于两个页面的展示：活动信息和确认支付
 *
 * @since 2017/1/20.
 */
public class MyOrderInfoActivity extends BaseActivity implements ServiceAdapterCallback, View.OnClickListener {
    private static final String TAG = MyOrderInfoActivity.class.getSimpleName();

    public static final String ORDER_DETAIL_OR_PAYING = "order_detail_or_pay";
    public static final String ORDER_FROM_SIGNED = "order_from_signed";
    public static final String ORDER_ID = "order_id";
    //活动信息
    public static final int ORDER_DETAIL = 0;
    //确认支付
    public static final int ORDER_PAYING = 1;

    //活动详情是否点击
    private boolean actDetailClicked = false;
    //是否已添加过报名信息
    private boolean isBookInfoAdded = false;

    //页面头部和底部，根据 iMunuType 变化
    private View viewActPicture;
    private View viewTitle;
    private View viewConfirmPayHead;
    private View viewOrderDetailBottom;
    private View viewConfirmPayBottom;
    //固定底部，待支付显示
    private View viewFixedBottom;
    private View llPayLeftTime;
    //滚动底部，进行中显示
    private View viewScrollBottom;
    //支付类型，在确认支付页面展示
    private View viewPayType;

    //活动信息
    private ImageView imageActPic;
    private TextView textGeneralStatus;
    private TextView textActTitle;
    private TextView textActTime;
    private TextView textActLocation;
    private TextView textActCost;
    private TextView textGotoActDetail; //查看详情

    private ImageView imageConfirmPayPic;
    private TextView textConfirmPayTitle;
    private TextView textConfirmPayTime;
    private TextView textConfirmPayLocation;
    private TextView textConfirmPayPrice;

    //支付信息
    private View spanBeforePayInfo;
    private View payInfo;
    private View spanAfterPayInfo;
    private TextView textOrderTotalFee;
    private TextView textOrderId;
    private TextView textOrderCreateTime;
    private TextView textConfirmPayGoToPay;
    private TextView textPayLeftTime;
    private TextView textOrderDetailGotoPay;
    private TextView textPayWechat;
    //    private TextView textPayBalance;
    private TextView textPayAli;

    //报名信息
    private LinearLayout layoutOrderDetail;

    //取消报名
    private TextView textFixCancelRegister;

    //private ServerRequestController mServiceAdapter;
    private ServiceAdapter serviceAdapter;
    private int getOrderInfoTaskId;
    private int iGetPayInfoTaskId;
    private int iSendSignInfoTaskId;
    private int iActId;
    private long orderId;

    private int iMenuType = ORDER_DETAIL;
    private ActOrderInfo actOrderInfo = null;
    //支付平台---0：微信，1：支付宝
    private int iPayPlatform = CoomixPayManager.PAY_WECHAT;
    //支付平台选择微信时--0=微信App支付; 1=微信公众号Web支付; 2=微信扫码支付; 3=微信刷卡支付
    private int iPayManner = WechatPayManager.WECHAT_PAY_APP;
    private ProgressDialogEx progressDialogEx = null;
    private boolean bFromSigned = false;
    private final int REQUESTCODE_GO_TO_PAY = 100;

    private int leftTime;
    private static final int UPDATE_PAY_LEFT_TIME = 0;
    private int SECONDS_PER_MINUTE = 60;
    private int iRequestBalance = -1;
    private BalanceInfo balanceInfo;

    //更新剩余付款时间
    @SuppressWarnings("HandlerLeak")
    private Handler updatePayLeftTime = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case UPDATE_PAY_LEFT_TIME:
                    if (leftTime == 0) {
                        //已超时，更新页面
                        MyActivityActivity.shouldRefresh = true;
                        requestOrderInfo();
                    } else if (leftTime < SECONDS_PER_MINUTE) {
                        //小于60秒，到时间后更新页面，加3秒钟延时，因为业务系统的状态改变预留2秒延时
                        updatePayLeftTime.sendEmptyMessageDelayed(UPDATE_PAY_LEFT_TIME, (leftTime + 3) * 1000);
                        setPayLeftTime(leftTime);
                        leftTime = 0;
                    } else {
                        //大于60秒，更新控件
                        updatePayLeftTime.sendEmptyMessageDelayed(UPDATE_PAY_LEFT_TIME, SECONDS_PER_MINUTE * 1000);
                        setPayLeftTime(leftTime);
                        leftTime -= SECONDS_PER_MINUTE;
                    }
                    break;
            }
            super.handleMessage(msg);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_order_info);

        serviceAdapter = ServiceAdapter.getInstance(this);
        serviceAdapter.registerServiceCallBack(this);

        Intent intent = getIntent();
        if (intent != null) {
            iActId = intent.getIntExtra(Constant.NATIVE_ACTIVITY_ID, 0);
            orderId = intent.getLongExtra(ORDER_ID, 0);
            iMenuType = intent.getIntExtra(ORDER_DETAIL_OR_PAYING, ORDER_DETAIL);
            bFromSigned = intent.getBooleanExtra(ORDER_FROM_SIGNED, false);
        }

        initViews();

        initNetData();
    }

    public void initNetData() {
        requestOrderInfo();

        if (iMenuType == ORDER_PAYING) {
            //iRequestBalance = serviceAdapter.getPocketBalance(this.hashCode(), AllOnlineApp.sToken.ticket);
        }
    }

    private void initViews() {
        MyActionbar actionbar = (MyActionbar) findViewById(R.id.myActionbar);
        actionbar.initActionbar(true, "", 0, 0);
        actionbar.setLeftImageClickListener(view -> onBackAction());

        //可变头部和底部
        viewActPicture = findViewById(R.id.rl_activity_pic);
        viewTitle = findViewById(R.id.title_view);
        viewConfirmPayHead = findViewById(R.id.confirm_pay_head_view);
        viewOrderDetailBottom = findViewById(R.id.order_detail_bottom_view);
        viewConfirmPayBottom = findViewById(R.id.confirm_pay_bottom_view);
        textFixCancelRegister = (TextView) findViewById(R.id.cancel_register);
        viewFixedBottom = findViewById(R.id.bottom_view_fixed);
        viewScrollBottom = findViewById(R.id.bottom_view_scrollable);
        viewPayType = findViewById(R.id.pay_type);
        textPayLeftTime = (TextView) findViewById(R.id.pay_left_time);

        //活动信息
        imageActPic = (ImageView) findViewById(R.id.activity_pic);
        textGeneralStatus = (TextView) findViewById(R.id.general_status);
        textGeneralStatus.setOnClickListener(this);
        textActTitle = (TextView) findViewById(R.id.activity_title);
        textActTime = (TextView) findViewById(R.id.activity_time);
        textActLocation = (TextView) findViewById(R.id.activity_location);
        textActCost = (TextView) findViewById(R.id.activity_cost);

        textGotoActDetail = (TextView) findViewById(R.id.goto_act_detail);
        textGotoActDetail.setOnClickListener(this);

        imageConfirmPayPic = (ImageView) findViewById(R.id.confirm_head_pic);
        textConfirmPayTitle = (TextView) findViewById(R.id.confirm_head_title);
        textConfirmPayTime = (TextView) findViewById(R.id.confirm_head_time);
        textConfirmPayLocation = (TextView) findViewById(R.id.confirm_head_location);
        textConfirmPayPrice = (TextView) findViewById(R.id.confirm_head_price);

        //支付信息
        spanBeforePayInfo = findViewById(R.id.span_view2);
        payInfo = findViewById(R.id.pay_info);
        spanAfterPayInfo = findViewById(R.id.span_view3);
        textOrderTotalFee = (TextView) findViewById(R.id.order_total_fee);
        textOrderId = (TextView) findViewById(R.id.order_id);
        textOrderCreateTime = (TextView) findViewById(R.id.order_create_time);

        //报名信息
        layoutOrderDetail = (LinearLayout) findViewById(R.id.order_detail);

        //微信支付
        textPayWechat = (TextView) findViewById(R.id.pay_type_wecaht);
        if (!CommonUtil.isSupportWechatPay()) {
            textPayWechat.setVisibility(View.GONE);
            if (iPayPlatform == CoomixPayManager.PAY_WECHAT) {
                iPayPlatform = CoomixPayManager.PAY_ALI;
            }
        }
        //暂时不支持支付宝支付
        textPayAli = (TextView) findViewById(R.id.pay_type_ali);
        if (true/*!CommonUtil.isSupportAliPay()*/) {
            textPayAli.setVisibility(View.GONE);
            if (iPayPlatform == CoomixPayManager.PAY_ALI) {
                iPayPlatform = CoomixPayManager.PAY_GOOME;
            }
        }
        //余额支付--目前隐藏
        //        textPayBalance = (TextView) findViewById(R.id.pay_type_balance);
        //        textPayBalance.setVisibility(View.GONE);
        //        if(!CommonUtil.isSupportGoomePay())
        //        {
        //            textPayBalance.setVisibility(View.GONE);
        //            if(iPayPlatform == CoomixPayManager.PAY_GOOME)
        //            {
        //                iPayPlatform = -1;
        //            }
        //        }

        switch (iMenuType) {
            case ORDER_DETAIL:
                //活动信息
                viewActPicture.setVisibility(View.VISIBLE);
                viewTitle.setVisibility(View.VISIBLE);
                viewOrderDetailBottom.setVisibility(View.VISIBLE);
                viewConfirmPayHead.setVisibility(View.GONE);
                viewConfirmPayBottom.setVisibility(View.GONE);
                actionbar.setTitle(R.string.activity_order_info);
                llPayLeftTime = findViewById(R.id.ll_pay_left_time);
                textFixCancelRegister.setOnClickListener(this);
                textOrderDetailGotoPay = (TextView) findViewById(R.id.order_detail_goto_pay);
                textOrderDetailGotoPay.setOnClickListener(this);
                findViewById(R.id.textViewCancel).setOnClickListener(this);
                break;

            case ORDER_PAYING:
                //确认支付
                viewActPicture.setVisibility(View.GONE);
                viewTitle.setVisibility(View.GONE);
                viewOrderDetailBottom.setVisibility(View.GONE);
                viewConfirmPayHead.setVisibility(View.VISIBLE);
                viewConfirmPayBottom.setVisibility(View.VISIBLE);
                actionbar.setTitle(R.string.confirm_pay);
                textConfirmPayGoToPay = (TextView) findViewById(R.id.confirm_pay_goto_pay);
                textConfirmPayGoToPay.setOnClickListener(this);
                viewPayType.setVisibility(View.VISIBLE);
                textPayWechat.setOnClickListener(this);
                //                textPayBalance.setOnClickListener(this);
                textPayAli.setOnClickListener(this);
                selectPayPlatform(CoomixPayManager.PAY_WECHAT);
                break;

            default:
                break;
        }

        showContentLoading();
    }

    private void selectPayPlatform(int iPayPlatform) {
        this.iPayPlatform = iPayPlatform;
        if (iPayPlatform == CoomixPayManager.PAY_WECHAT) {
            //微信支付
            iPayManner = WechatPayManager.WECHAT_PAY_APP;
            textPayWechat.setSelected(true);
            //            textPayBalance.setSelected(false);
            textPayAli.setSelected(false);
        }
        //        else if(iPayPlatform == CoomixPayManager.PAY_ALI)
        //        {
        //            iPayManner = AliPayManager.ALI_PAY_APP;
        //            textPayWechat.setSelected(false);
        //            textPayBalance.setSelected(false);
        //            textPayAli.setSelected(true);
        //        }
        else if (iPayPlatform == CoomixPayManager.PAY_GOOME) {
            //选择零钱支付
            iPayManner = GoomePayRsp.POCKET_PAY_APP;
            textPayWechat.setSelected(false);
            //            textPayBalance.setSelected(true);
            textPayAli.setSelected(false);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void onClick(View v) {
        Intent intent = null;
        switch (v.getId()) {
            case R.id.goto_act_detail:
            case R.id.general_status:
                //查看详情
                actDetailClicked = true;
                intent = new Intent(this, CommActDetailActivity.class);
                intent.putExtra(Constant.NATIVE_ACTIVITY_ID, iActId);
                startActivity(intent);
                break;

            case R.id.confirm_pay_goto_pay:
                //直接获取预支付信息进行支付
                //                if(iPayPlatform == CoomixPayManager.PAY_GOOME)
                //                {
                //                    long totalFee = 0;
                //                    if(actOrderInfo != null && actOrderInfo.getPay() != null)
                //                    {
                //                        totalFee = actOrderInfo.getPay().getTotal_fee();
                //                    }
                //                    if(balanceInfo != null && balanceInfo.getBalance() >= 0 && balanceInfo.getBalance() < totalFee)
                //                    {
                //                        Toast.makeText(MyOrderInfoActivity.this, R.string.pocket_balance_poor_change, Toast.LENGTH_SHORT).show();
                //                        return;
                //                    }
                //                }
                requestRepayInfo();
                break;

            case R.id.cancel_register:
            case R.id.textViewCancel:
                //取消报名
                final AskDialog dialog = new AskDialog(this);
                dialog.show();
                dialog.setShowMsg(R.string.confirm_cancel_register).show();
                dialog.setOnYesClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //取消报名
                        //刷新我的活动列表
                        MyActivityActivity.shouldRefresh = true;
                        showWaitInfoDialog(getString(R.string.please_wait));
                        iSendSignInfoTaskId = serviceAdapter.sendSignInfo(this.hashCode(), AllOnlineApp.sToken.ticket,
                            ActRegisterExtendActivity.REG_CANCEL, iActId, orderId, null, null, null, null, null, 0, 0);
                        dialog.dismiss();
                    }
                });
                break;

            case R.id.order_detail_goto_pay:
                //报了名的活动详情，去支付界面
                intent = new Intent(this, MyOrderInfoActivity.class);
                intent.putExtra(Constant.NATIVE_ACTIVITY_ID, iActId);
                intent.putExtra(ORDER_DETAIL_OR_PAYING, ORDER_PAYING);
                intent.putExtra(ORDER_ID, orderId);
                intent.putExtra(MyOrderInfoActivity.ORDER_FROM_SIGNED, false);
                startActivityForResult(intent, REQUESTCODE_GO_TO_PAY);
                break;

            case R.id.pay_type_wecaht:
                //选择微信支付
                selectPayPlatform(CoomixPayManager.PAY_WECHAT);
                break;

            case R.id.pay_type_ali:
                //选择支付宝支付
                selectPayPlatform(CoomixPayManager.PAY_ALI);
                break;

            //            case R.id.pay_type_balance:
            //                //选择零钱支付
            //                long totalFee = 0;
            //                if(actOrderInfo != null && actOrderInfo.getPay() != null)
            //                {
            //                    totalFee = actOrderInfo.getPay().getTotal_fee();
            //                }
            //                if(balanceInfo != null && balanceInfo.getBalance() >= 0 && balanceInfo.getBalance() < totalFee)
            //                {
            //                    Toast.makeText(MyOrderInfoActivity.this, R.string.pocket_balance_poor_change, Toast.LENGTH_SHORT).show();
            //                }
            //                selectPayPlatform(CoomixPayManager.PAY_GOOME);
            //                break;

            default:
                break;
        }
    }

    private void onBackAction() {
        if (actDetailClicked) {
            //刷新我的活动列表
            MyActivityActivity.shouldRefresh = true;
        }
        if (iMenuType != ORDER_PAYING || actOrderInfo == null || actOrderInfo.getPay() == null || (actOrderInfo.getPay()
            .getPay_status() != ActPay.PAY_PREPAY && actOrderInfo.getPay().getPay_status() != ActPay.PAY_NEW)) {
            finish();
            return;
        }
        final AskDialog askDialog = new AskDialog(this);
        askDialog.show();
        if (bFromSigned) {
            askDialog.setShowMsg(R.string.not_pay_ask_after_sign);
        } else {
            askDialog.setShowMsg(R.string.ask_giveup_pay);
        }
        askDialog.setNoText(R.string.back);
        askDialog.setYesText(R.string.pay_continue);
        askDialog.setYesTextColor(R.color.color_main);
        askDialog.setNoTextColor(R.color.gray_1);
        askDialog.setOnYesClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                askDialog.dismiss();
            }
        });
        askDialog.setOnNoClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                askDialog.dismiss();
                finish();
            }
        });
    }

    private void requestRepayInfo() {
        //每次支付的时候都需要请求预支付的信息--因为可能订单过期
        if (actOrderInfo == null || actOrderInfo.getPay() == null || (actOrderInfo.getPay().getPay_status()
            != ActPay.PAY_PREPAY && actOrderInfo.getPay().getPay_status() != ActPay.PAY_NEW)) {
            return;
        }

        if (iPayPlatform == CoomixPayManager.PAY_GOOME) {
            showWaitInfoDialog(getString(R.string.wait_for_goome_pay));
        } else {
            showWaitInfoDialog(getString(R.string.wait_jump_to_wx));
        }

        iGetPayInfoTaskId =
            serviceAdapter.prepay(this.hashCode(), iActId, actOrderInfo.getPay().getOrder_id(), iPayPlatform,
                iPayManner);
    }

    @Override
    protected void onDestroy() {
        //        if (mServiceAdapter != null)
        //        {
        //            mServiceAdapter.unregisterServiceCallBack(this);
        //        }
        if (serviceAdapter != null) {
            serviceAdapter.unregisterServiceCallBack(this);
        }
        if (updatePayLeftTime != null) {
            updatePayLeftTime.removeCallbacksAndMessages(null);
            updatePayLeftTime = null;
        }
        super.onDestroy();
    }

    private void requestOrderInfo() {
        getOrderInfoTaskId =
            serviceAdapter.getOrderInfo(this.hashCode(), AllOnlineApp.sToken.ticket, iActId, orderId, null);
    }

    @Override
    public void callback(int messageId, Result result) {
        try {
            if (result != null) {
                if (result.errcode == Result.ERROR_NETWORK) {
                    dismissWaitDialog();
                    showContentError(R.drawable.net_error, getResources().getString(R.string.network_error));
                } else if (getOrderInfoTaskId == messageId && result.apiCode == Constant.FM_APIID_GET_ORDER_INFO) {
                    if (result.mResult != null && result.success) {
                        parseOrderInfo((ActOrderInfo) result.mResult);
                    }
                } else if (iGetPayInfoTaskId == messageId && result.apiCode == Constant.FM_APIID_REQUEST_PREPAY) {
                    dismissWaitDialog();
                    if (result.success && result.mResult != null && result.mResult instanceof CoomixPayRsp) {
                        CoomixPayRsp coomixPayRsp = (CoomixPayRsp) result.mResult;
                        if (coomixPayRsp.getError() != null && (coomixPayRsp.getError().getCode()
                            == Error.ERRCODE_REDPACKET_PARAM_INVALID
                            || coomixPayRsp.getError().getCode() == Error.ERRCODE_BALANCE_INSUFFICIENT)) {
                            if (coomixPayRsp.getError().getCode() == Error.ERRCODE_REDPACKET_PARAM_INVALID) {
                                //支付参数出错了
                                showToast(coomixPayRsp.getError().getMsg());
                            } else {
                                //余额不足
                                showToast(R.string.pocket_balance_poor_change);
                            }
                        } else {
                            if (iPayPlatform == CoomixPayManager.PAY_GOOME) {
                                //零钱支付，直接提示支付成功，并结束当前界面
                                showToast(R.string.payed_success);
                                goomePayFinish();
                            } else {
                                //其他支付（微信，支付宝等）调起第三方App
                                parsePrepayInfo((CoomixPayRsp) result.mResult);
                            }
                        }
                        return;
                    } else if (!result.success) {
                        switch (result.errcode) {
                            case Result.ERRCODE_ORDER_TIMEOUT_INVALID:
                                //已经过期了
                                showToast(R.string.order_timeout);
                                return;
                        }
                    }
                    showToast(R.string.pay_fail);
                } else if (iSendSignInfoTaskId == messageId && result.apiCode == Constant.FM_APIID_sendSignInfo) {
                    dismissWaitDialog();
                    //取消报名成功
                    if (result.success) {
                        ActPrice actPrice = actOrderInfo.getPrice();
                        if (actPrice != null && actPrice.getType() == ActPrice.PRICE_FASTEN
                            && actPrice.getFixed_price() > 0 && actOrderInfo.getGeneral_status() == MyActivity.PAYED) {
                            ActPay pay = actOrderInfo.getPay();
                            //微信支付，弹窗
                            if (pay != null && pay.getPay_platform() != ICoomixPay.PAY_GOOME) {
                                //showCancelPayedActNotice();
                                showToast(R.string.cancel_register_notice);
                                finish();
                            }
                            //零钱支付，弹toast
                            else if (pay != null) {
                                showToast(R.string.cancel_register_notice_balance);
                                finish();
                            }
                            return;
                        }
                        showToast(R.string.register_cancel_success);
                        showContentLoading();
                        requestOrderInfo();
                    } else {
                        showToast(R.string.act_canceled_fail);
                    }
                } else if (iRequestBalance == messageId) {
                    //请求的零钱余额
                    if (result.success && result.mResult != null && result.mResult instanceof BalanceInfo) {
                        balanceInfo = (BalanceInfo) result.mResult;
                        //                        textPayBalance.setText(getString(R.string.pay_type_pocket_balance, CommunityUtil.getDecimalStrByInt(balanceInfo.getBalance(), 2)));
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void goomePayFinish() {
        MyActivityActivity.shouldRefresh = true;
        setResult(RESULT_OK);
        if (bFromSigned) {
            startActivity(new Intent(MyOrderInfoActivity.this, MyActivityActivity.class));
        }
        finish();
    }

    private void showCancelPayedActNotice() {
        //付费活动的取消，已经支付过的情况下需要弹出窗口
        final NoticeDialog notice = new NoticeDialog(MyOrderInfoActivity.this);
        notice.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                setResult(RESULT_OK);
                finish();
            }
        });
        notice.setOkListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                notice.dismiss();
            }
        });
        notice.show();
    }

    private void showToast(int strId) {
        Toast.makeText(this, strId, Toast.LENGTH_SHORT).show();
    }

    //设置剩余付款时间
    private void setPayLeftTime(int seconds) {
        String time = CommunityDateUtil.formatSec2UnitString(seconds, getResources().getString(R.string.hour),
            getResources().getString(R.string.minute));
        int hIndex = time.indexOf("小时");
        int mIndex = time.indexOf("分钟");
        SpannableString str = new SpannableString(time);
        CharacterStyle sizeHour = new AbsoluteSizeSpan(getResources().getDimensionPixelSize(R.dimen.text_m));
        CharacterStyle sizeMinute = new AbsoluteSizeSpan(getResources().getDimensionPixelSize(R.dimen.text_m));
        if (hIndex > 0 && mIndex < 0) {
            //仅有xx小时
            str.setSpan(sizeHour, 0, hIndex, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        } else if (hIndex < 0 && mIndex > 0) {
            //仅有xx分钟
            str.setSpan(sizeMinute, 0, mIndex, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        } else if (hIndex > 0 && mIndex > 0) {
            //xx小时xx分钟
            str.setSpan(sizeHour, 0, hIndex, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            str.setSpan(sizeMinute, hIndex + 2, mIndex, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        textPayLeftTime.setText(str + getString(R.string.out_of_date_auto_cancel));
    }

    private void parseOrderInfo(ActOrderInfo orderInfo) {
        //活动信息,可变头部
        this.actOrderInfo = orderInfo;
        if (orderInfo == null) {
            return;
        }
        switch (iMenuType) {
            case ORDER_DETAIL:
                // 支付状态
                if (orderInfo.getPay() != null) {
                    int genStatus = orderInfo.getGeneral_status();
                    long orderId = orderInfo.getPay().getOrder_id();
                    switch (genStatus) {
                        case MyActivity.PAY_WAITTING:
                            //待支付
                            viewFixedBottom.setVisibility(View.VISIBLE);
                            //活动已结束/已截止，不显示取消报名
                            if (orderInfo.getStatus() == CommunityAct.ACT_STOPPED
                                || orderInfo.getStatus() == CommunityAct.ACT_FINISHED) {
                                textFixCancelRegister.setVisibility(View.GONE);
                            }
                            viewScrollBottom.setVisibility(View.GONE);
                            textGeneralStatus.setText(R.string.activity_not_paid);
                            if (orderId > 0) {
                                llPayLeftTime.setVisibility(View.VISIBLE);
                                textOrderDetailGotoPay.setVisibility(View.VISIBLE);
                                //剩余支付时间
                                leftTime = orderInfo.getPay().getPay_left_time();
                                if (leftTime > 0) {
                                    setPayLeftTime(leftTime);
                                    updatePayLeftTime.sendEmptyMessage(UPDATE_PAY_LEFT_TIME);
                                }
                            }
                            break;
                        case MyActivity.PAYED:
                            //进行中
                            viewFixedBottom.setVisibility(View.GONE);
                            //活动已结束/已截止，不显示取消报名
                            if (orderInfo.getStatus() == CommunityAct.ACT_STOPPED
                                || orderInfo.getStatus() == CommunityAct.ACT_FINISHED) {
                                viewScrollBottom.setVisibility(View.GONE);
                            } else {
                                viewScrollBottom.setVisibility(View.VISIBLE);
                            }
                            textGeneralStatus.setText(R.string.activity_on_going);
                            break;
                        case MyActivity.ENDED:
                            //已结束
                            viewFixedBottom.setVisibility(View.GONE);
                            viewScrollBottom.setVisibility(View.GONE);
                            textGeneralStatus.setText(R.string.activity_finished);
                            break;
                        case MyActivity.CANCELED:
                            //已取消
                            viewFixedBottom.setVisibility(View.GONE);
                            viewScrollBottom.setVisibility(View.GONE);
                            textGeneralStatus.setText(R.string.activity_cancelled);
                            break;
                    }
                }
                //图片
                int height = getResources().getDimensionPixelSize(R.dimen.space_24x);
                GlideApp.with(this).load(orderInfo.getPic()).placeholder(R.drawable.image_default).error(
                    R.drawable.image_default_error).override(AllOnlineApp.screenWidth, height).centerCrop().into(
                    imageActPic);
                //标题
                textActTitle.setText(orderInfo.getTitle());

                //支付信息
                if (orderInfo.getPay() != null) {
                    long orderId = orderInfo.getPay().getOrder_id();
                    int type = orderInfo.getPrice().getType();
                    if (orderId > 0 && (type == ActPrice.PRICE_FASTEN || type == ActPrice.PRICE_PACKAGE)) {
                        spanBeforePayInfo.setVisibility(View.VISIBLE);
                        payInfo.setVisibility(View.VISIBLE);
                        spanAfterPayInfo.setVisibility(View.VISIBLE);
                        textOrderTotalFee.setText(
                            CommunityUtil.getDecimalStrByInt(orderInfo.getPay().getTotal_fee(), 2) + getResources()
                                .getString(R.string.money_yuan));
                        textOrderId.setText(String.valueOf(orderId));
                        textOrderCreateTime.setText(
                            CommunityDateUtil.formatTime(orderInfo.getPay().getPay_order_create_time() * 1000,
                                "yyyy.MM.dd HH:mm"));
                    }
                }
                break;

            case ORDER_PAYING:
                spanBeforePayInfo.setVisibility(View.VISIBLE);
                //图片
                int w = getResources().getDimensionPixelSize(R.dimen.space_112dp);
                int h = getResources().getDimensionPixelSize(R.dimen.space_14x);
                GlideApp.with(this).load(orderInfo.getPic()).placeholder(R.drawable.image_default).error(
                    R.drawable.image_default_error).override(w, h).centerCrop().into(imageConfirmPayPic);
                textConfirmPayTitle.setText(orderInfo.getTitle());
                textConfirmPayTime.setText(
                    CommunityDateUtil.formatActTime(this, orderInfo.getBegtime(), orderInfo.getEndtime(),
                        orderInfo.getBegin_date_desc(), orderInfo.getEnd_date_desc(), true));
                textConfirmPayLocation.setText(orderInfo.getLocation());
                //价格
                if (orderInfo.getPay() != null) {
                    String rmbPrice = CommunityUtil.getMoneyStrByIntDecimal(this, orderInfo.getPay().getTotal_fee(), 2);
                    textConfirmPayGoToPay.setText(String.format(getString(R.string.go_to_pay2), rmbPrice));
                    int peopleCount;
                    if (orderInfo.getCompanion_info() != null) {
                        peopleCount = orderInfo.getCompanion_info().size() + 1;
                    } else {
                        peopleCount = 1;
                    }
                    rmbPrice += "  共" + peopleCount + "人";
                    SpannableString str = new SpannableString(rmbPrice);
                    int start = rmbPrice.indexOf(getResources().getString(R.string.money_unit));
                    int end = rmbPrice.indexOf(".");
                    CharacterStyle size = new AbsoluteSizeSpan(getResources().getDimensionPixelSize(R.dimen.text_xl));
                    if (start >= 0 && end > 0 && end > start) {
                        str.setSpan(size, start + 1, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    }
                    textConfirmPayPrice.setText(str);
                }
                break;
        }
        textActTime.setText(CommunityDateUtil.formatActTime(this, orderInfo.getBegtime(), orderInfo.getEndtime(),
            orderInfo.getBegin_date_desc(), orderInfo.getEnd_date_desc(), true));
        textActLocation.setText(orderInfo.getLocation());
        textActCost.setText(orderInfo.getCost());

        //报名信息
        if (!isBookInfoAdded) {
            addPriceRuleInfo(orderInfo.getPrice(), orderInfo.getOwn_price_rule_id());
            addBookInfo(orderInfo.getDisplay());
            //addBookInfo(orderInfo.getCommit_items());
            //addCompanionInfo(orderInfo);
            isBookInfoAdded = true;
        }

        showContent();
    }

    //添加套餐信息
    private void addPriceRuleInfo(ActPrice price, int priceRuleId) {
        if (price != null) {
            String priceRuleTitle = price.getPrice_rule_title();
            if (StringUtil.isTrimEmpty(priceRuleTitle)) {
                priceRuleTitle = getString(R.string.act_package);
            }
            String priceRuleName = "";
            int priceMount = 0;
            ArrayList<PriceRule> rules = price.getPrice_rule();
            for (PriceRule rule : rules) {
                if (rule.getId() == priceRuleId) {
                    priceRuleName = rule.getName();
                    priceMount = rule.getPrice();
                    break;
                }
            }
            addTextView(null, String.format(getString(R.string.order_info_package_price), priceRuleTitle, priceRuleName,
                CommunityUtil.getDecimalStrByInt(priceMount, 2)));
        }
    }

    private void addCompanionInfo(ActOrderInfo info) {
        //        List<ActCommitItem> commitItems = info.getCommit_items();
        //        ArrayList<ArrayList<CommitExtendItem>> companionInfo = info.getCompanion_info();
        //
        //        List<ActCommitItem> companionList = new ArrayList<>();
        //        if (companionInfo != null)
        //        {
        //            for (int i = 0; i < companionInfo.size(); i++)
        //            {
        //                companionList.clear();
        //                ActCommitItem commitItem;
        //                ArrayList<CommitExtendItem> extendItems = companionInfo.get(i);
        //                for (int j = 0; j < extendItems.size(); j++)
        //                {
        //                    CommitExtendItem extendItem = extendItems.get(j);
        //                    for (ActCommitItem item : commitItems)
        //                    {
        //                        if (extendItem.getId() == item.getId())
        //                        {
        //                            commitItem = item;
        //                            commitItem.setValue(extendItem.getValue());
        //                            companionList.add(commitItem);
        //                            break;
        //                        }
        //                    }
        //                }
        //                addSpanView();
        //                addCompanionTitle(String.format(getString(R.string.companion_info_title), i + 2));
        //                addPriceRuleInfo(info.getPrice(), info.getCompanion_price_rule_id()[i]);
        //                addBookInfo(companionList);
        //            }
        //        }
    }

    //添加分割线
    private void addSpanView() {
        View spanView = new View(this);
        spanView.setBackgroundColor(getResources().getColor(R.color.color_seperator));

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
            getResources().getDimensionPixelSize(R.dimen.collect_seperator_thin));
        params.setMargins(0, getResources().getDimensionPixelSize(R.dimen.space_2x), 0, 0);

        spanView.setLayoutParams(params);

        layoutOrderDetail.addView(spanView);
    }

    //添加同伴信息头部，eg:酷粉2
    private void addCompanionTitle(String text) {
        TextView textView = new TextView(MyOrderInfoActivity.this);
        textView.setText(text);
        textView.setTextColor(getResources().getColor(R.color.color_text_h));
        textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimensionPixelSize(R.dimen.text));

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT);
        params.setMargins(0, getResources().getDimensionPixelSize(R.dimen.space_2x), 0, 0);

        textView.setLayoutParams(params);

        layoutOrderDetail.addView(textView);
    }
    //
    //    private void addBookInfo(List<ActCommitItem> list)
    //    {
    //        if (list != null && list.size() > 0)
    //        {
    //            for (ActCommitItem item : list)
    //            {
    //                //确认支付，只显示必填信息
    //                if (iMenuType == ORDER_PAYING && item.getRequired() == 1)
    //                {
    //                    drawUiByType(item);
    //                }
    //                //活动信息，显示填写的全部信息
    //                if (iMenuType == ORDER_DETAIL && item.isValueCommitted())
    //                {
    //                    drawUiByType(item);
    //                }
    //            }
    //        }
    //    }

    private void addBookInfo(List<ActDisplay> list) {
        if (list != null && list.size() > 0) {
            for (ActDisplay item : list) {
                //确认支付，只显示必填信息
                //if (iMenuType == ORDER_PAYING && item.getRequired() == 1)
                {
                    drawUiByType(item);
                }
                //                //活动信息，显示填写的全部信息
                //                if (iMenuType == ORDER_DETAIL && item.isValueCommitted())
                //                {
                //                    drawUiByType(item);
                //                }
            }
        }
    }

    //    private void drawUiByType(ActCommitItem item)
    //    {
    //        Log.i(TAG, "draw ui by type");
    //        String str = item.getName() + "：";
    //        switch (item.getType())
    //        {
    //            case ActCommitItem.INPUT_TEXT_NORMAL:
    //            case ActCommitItem.INPUT_NUMBERS:
    //                str += item.getValue();
    //                //一般的输入内容
    //                addTextView(item, str);
    //                break;
    //
    //            case ActCommitItem.RADIO_CONTENT:
    //                try
    //                {
    //                    List<Integer> list = (ArrayList<Integer>) item.getValue();
    //                    Log.i(TAG, "i=" + list.get(0));
    //                    str += item.getOptions().get((int) list.get(0)).getName();
    //                }
    //                catch (Exception e)
    //                {
    //                    e.printStackTrace();
    //                }
    //                //类似性别的单项选择器
    //                addTextView(item, str);
    //                break;
    //
    //            case ActCommitItem.POP_SELECT_CONTENT:
    //                str += item.getPopSelectValue();
    //                //类似弹窗选择省市区多级信息，或弹出一级选择
    //                addTextView(item, str);
    //                break;
    //
    //            case ActCommitItem.MAP_SELECT_LOCATION:
    //                str += item.getLocationValue();
    //                //从地图选取位置信息
    //                addTextView(item, str);
    //                break;
    //
    //            case ActCommitItem.UPLOAD_IMAGE:
    //                //上传图片
    //                str += getString(R.string.upload_image_count, item.getImagesListValue().size());
    //                //str += "已上传" + item.getImagesListValue().size() + "张";
    //                addTextView(item, str);
    //                if (iMenuType == ORDER_DETAIL)
    //                {
    //                    addImageGrid(item, true);
    //                }
    //                else if (iMenuType == ORDER_PAYING)
    //                {
    //                    addImageGrid(item, false);
    //                }
    //                break;
    //
    //            default:
    //                break;
    //        }
    //    }

    private void drawUiByType(ActDisplay item) {
        Log.i(TAG, "draw ui by type");
        String str = item.getName() + "：";
        switch (item.getType()) {
            //case ActCommitItem.INPUT_TEXT_NORMAL:
            //case ActCommitItem.INPUT_NUMBERS:
            case 1:
            case 2:
            case 3:
                str += item.getValue();
                //一般的输入内容
                addTextView(item, str);
                break;

            //            case ActCommitItem.RADIO_CONTENT:
            //                try
            //                {
            //                    List<Integer> list = (ArrayList<Integer>) item.getValue();
            //                    Log.i(TAG, "i=" + list.get(0));
            //                    str += item.getOptions().get((int) list.get(0)).getName();
            //                }
            //                catch (Exception e)
            //                {
            //                    e.printStackTrace();
            //                }
            //                //类似性别的单项选择器
            //                addTextView(item, str);
            //                break;
            //
            //            case ActCommitItem.POP_SELECT_CONTENT:
            //                str += item.getPopSelectValue();
            //                //类似弹窗选择省市区多级信息，或弹出一级选择
            //                addTextView(item, str);
            //                break;
            //
            //            case ActCommitItem.MAP_SELECT_LOCATION:
            //                str += item.getLocationValue();
            //                //从地图选取位置信息
            //                addTextView(item, str);
            //                break;
            //
            //            case ActCommitItem.UPLOAD_IMAGE:
            case 9:
                //上传图片
                str += getString(R.string.upload_image_count, item.getImagesListValue().size());
                //str += "已上传" + item.getImagesListValue().size() + "张";
                addTextView(item, str);
                //if (iMenuType == ORDER_DETAIL)
            {
                addImageGrid(item, true);
            }
            //                else if (iMenuType == ORDER_PAYING)
            //                {
            //                    addImageGrid(item, false);
            //                }
            break;

            default:
                break;
        }
    }

    private void addTextView(ActDisplay item, String text) {
        TextView textView = new TextView(MyOrderInfoActivity.this);
        textView.setText(text);
        textView.setTextColor(getResources().getColor(R.color.color_text_l));
        textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimensionPixelSize(R.dimen.text));

        LinearLayout.LayoutParams params =
            new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.setMargins(0, getResources().getDimensionPixelSize(R.dimen.space_2x), 0, 0);

        textView.setLayoutParams(params);

        layoutOrderDetail.addView(textView);
    }

    private void addImageGrid(ActDisplay item, boolean clickable) {
        OrderInfoPicGridView gridView = new OrderInfoPicGridView(this);
        gridView.setNumColumns(5);
        LinearLayout.LayoutParams params =
            new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        params.setMargins(0, getResources().getDimensionPixelSize(R.dimen.space_8dp), 0, 0);
        gridView.setLayoutParams(params);
        gridView.setSelector(new ColorDrawable(Color.TRANSPARENT));
        OrderImageAdapter adapter = new OrderImageAdapter(this, item.getImagesListValue(), clickable);
        gridView.setAdapter(adapter);
        layoutOrderDetail.addView(gridView);
    }

    private class OrderImageAdapter extends BaseAdapter {
        private Activity mContext;
        private List<ImageInfo> list;
        private boolean clickable;

        public OrderImageAdapter(Activity context, List<ImageInfo> imageList, boolean clickable) {
            this.mContext = context;
            this.list = imageList;
            this.clickable = clickable;
        }

        @Override
        public int getCount() {
            return list.size();
        }

        @Override
        public ImageInfo getItem(int position) {
            return list.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            ViewHolder viewHolder = null;
            if (convertView == null) {
                viewHolder = new ViewHolder();
                convertView = LayoutInflater.from(mContext).inflate(R.layout.item_gridview_addtopic, null);
                viewHolder.thumbnailImageView = (ThumbnailImageView) convertView.findViewById(
                    R.id.item_gridview_addtopic_iv);
                convertView.setTag(R.layout.item_gridview_addtopic, viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag(R.layout.item_gridview_addtopic);
            }

            ImageInfo info = null;
            if (position < list.size()) {
                info = list.get(position);
            }
            if (!TextUtils.isEmpty(info.getDomain()) || !TextUtils.isEmpty(info.getImg_path())) {
                String url = (info.getDomain() == null ? "" : info.getDomain()) + (info.getImg_path() == null ? "" :
                    info.getImg_path());
                viewHolder.thumbnailImageView.setImageType(url);
                int padding = getResources().getDimensionPixelOffset(R.dimen.space_4dp);
                viewHolder.thumbnailImageView.setPadding(padding, padding, padding, padding);
                int mItemSize = getResources().getDimensionPixelSize(R.dimen.space_10x);
                GlideApp.with(mContext).load(url).placeholder(R.drawable.image_default).error(
                    R.drawable.image_default_error).override(mItemSize, mItemSize).centerCrop().into(
                    viewHolder.thumbnailImageView.getImageView());
            }

            if (clickable) {
                viewHolder.thumbnailImageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (position < 0 || position > list.size() - 1) {
                            return;
                        } else {
                            Intent intent = new Intent(mContext, PreviewActivity.class);
                            Bundle bundle = new Bundle();
                            CameraSdkParameterInfo info = new CameraSdkParameterInfo();
                            info.setPosition(position);
                            ArrayList<String> image_list = new ArrayList<String>();
                            List<ImageInfo> listImages = list;
                            for (int i = 0; i < listImages.size(); i++) {
                                if (listImages.get(i) != null) {
                                    if (listImages.get(i).isNet()) {
                                        image_list.add(listImages.get(i).getDomain() + listImages.get(i).getImg_path());
                                    } else {
                                        image_list.add(listImages.get(i).getSource_image());
                                    }
                                }
                            }
                            if (image_list.size() > 0) {
                                info.setImage_list(image_list);
                                bundle.putSerializable(CameraSdkParameterInfo.EXTRA_PARAMETER, info);
                                intent.putExtras(bundle);
                                intent.putExtra(PreviewActivity.INTENT_TOP_TYPE, PreviewActivity.TOP_HIDE);
                                mContext.startActivityForResult(intent, CameraSdkParameterInfo.TAKE_PICTURE_PREVIEW);
                                mContext.overridePendingTransition(R.anim.zoom_enter, 0);
                            }
                        }
                    }
                });
            }
            return convertView;
        }

        private class ViewHolder {
            ThumbnailImageView thumbnailImageView;
        }
    }

    private void showWaitInfoDialog(String msg) {
        progressDialogEx = new ProgressDialogEx(this);
        progressDialogEx.setAutoDismiss(false);
        progressDialogEx.setCancelOnTouchOutside(false);

        try {
            progressDialogEx.show(msg);
        } catch (Exception e) {

        }
    }

    private void dismissWaitDialog() {
        if (progressDialogEx != null && progressDialogEx.isShowing()) {
            progressDialogEx.dismiss();
        }
    }

    private void parsePrepayInfo(CoomixPayRsp coomixPayRsp) {
        if (coomixPayRsp != null) {
            ActPay actPay = coomixPayRsp.getPay();
            if (actPay == null) {
                Toast.makeText(this, "pay 信息为null", Toast.LENGTH_SHORT).show();
                return;
            }
            switch (actPay.getPay_status()) {
                case ActPay.PAY_NEW:
                case ActPay.PAY_PREPAY:
                    CoomixPayManager coomixPayManager = new CoomixPayManager(this, iPayPlatform);
                    PayResultManager.getInstance().copyOrderInfo(iActId, actOrderInfo);
                    PayResultManager.getInstance().setWillFinishActivity(this);
                    PayResultManager.getInstance().setbFromSigned(bFromSigned);
                    PayResultManager.getInstance().setPayOrderType(ICoomixPay.ORDER_FROM.FROM_ACTIVITY_ORDER);
                    coomixPayManager.pay(coomixPayRsp);
                    break;

                case ActPay.PAY_PAID:
                case ActPay.PAY_COMPLETE:
                case ActPay.PAY_SHIPPED:
                    showToast(R.string.order_have_payed);
                    finish();
                    break;

                case ActPay.PAY_CANCLED:
                    showToast(R.string.order_cancelled);
                    finish();
                    break;

                case ActPay.PAY_TIMEOUT_INVALID:
                    showToast(R.string.order_timeout);
                    finish();
                    break;

                case ActPay.UNKNOWN:
                default:
                    showToast(R.string.order_unknown);
                    finish();
                    break;
            }
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            onBackAction();
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUESTCODE_GO_TO_PAY) {
                requestOrderInfo();
            }
        }
    }

    private void showContent() {

    }

    private void showContentError(int net_error, String string) {

    }

    private void showContentLoading() {

    }
}
