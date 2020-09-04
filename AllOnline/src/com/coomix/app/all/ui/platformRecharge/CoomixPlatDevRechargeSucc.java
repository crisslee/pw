package com.coomix.app.all.ui.platformRecharge;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import com.coomix.app.all.GlobalParam;
import com.coomix.app.all.R;
import com.coomix.app.all.data.BaseSubscriber;
import com.coomix.app.all.data.DataEngine;
import com.coomix.app.all.data.ExceptionHandle;
import com.coomix.app.all.data.RxUtils;
import com.coomix.app.all.model.event.RefreshPlatDevsEvent;
import com.coomix.app.all.model.response.RespRefrshRenewPayOrder;
import com.coomix.app.all.widget.MyActionbar;
import com.coomix.app.all.wxapi.CoomixPayBase;
import com.coomix.app.framework.app.Result;
import com.coomix.app.framework.util.CommonUtil;
import com.coomix.app.pay.ICoomixPay;
import com.coomix.app.pay.PayResultManager;
import com.coomix.app.all.util.CommunityUtil;
import com.tencent.mm.opensdk.constants.ConstantsAPI;
import com.tencent.mm.opensdk.modelbase.BaseReq;
import com.tencent.mm.opensdk.modelbase.BaseResp;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import java.util.List;
import org.greenrobot.eventbus.EventBus;

public class CoomixPlatDevRechargeSucc extends CoomixPayBase {
    private Context mContext;
    private Resources mRes;

    private long order_id = 0;
    private long rechargeAmount = 0;

    private String ticket;
    private String rechargeFrom;

    private RecyclerView recyclerView;
    private TextView tvStatus, tvTotalMoney;
    private View llSummary; //The rootView group that holds all other details.
    private PlatRechargeSuccAdapter mAdapter;

    private CompositeDisposable mCompositeDisposable = new CompositeDisposable();

    public CoomixPlatDevRechargeSucc(Context context, Intent intent, FinishListener listener) {
        super(context, intent, listener);
        this.mContext = context;
        mRes = mContext.getApplicationContext().getResources();
        order_id = PayResultManager.getInstance().getOrder_id();
        ticket = CommonUtil.getTicket();
        rechargeAmount = PayResultManager.getInstance().getRechargeAmount();
        rechargeFrom = PayResultManager.getInstance().getRechargeFrom();
        PayResultManager.getInstance().setRechargeFrom(null);
    }

    @Override
    public View getMainView() {
        mainView = LayoutInflater.from(mContext).inflate(R.layout.activity_plat_recharge_success, null);
        initViews();
        return mainView;
    }

    private void initViews() {
        MyActionbar actionbar = (MyActionbar) mainView.findViewById(R.id.myActionbar);
        actionbar.initActionbar(true, "", 0, 0);
        //action bar
        tvStatus = (TextView) mainView.findViewById(R.id.tvStatus);
        tvTotalMoney = (TextView) mainView.findViewById(R.id.tvTotalMoney);
        llSummary = mainView.findViewById(R.id.llSummary);
        recyclerView = (RecyclerView) mainView.findViewById(R.id.recyclerView);
        mAdapter = new PlatRechargeSuccAdapter();
        recyclerView.setLayoutManager(new LinearLayoutManager(mContext));
        recyclerView.setAdapter(mAdapter);

        tvTotalMoney.setText(
            mContext.getString(R.string.money_unit) + CommunityUtil.getDecimalStrByLong(rechargeAmount, 2));
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
                    if (PayResultManager.getInstance().getPayOrderType()
                        == ICoomixPay.ORDER_FROM.FROM_RECHARGE_PLATFORM_DEVICES) {
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
        Disposable disposable = DataEngine.getAllMainApi()
            .refreshRenewPayOrder(
                GlobalParam.getInstance().getCommonParas(),
                GlobalParam.getInstance().getAccessToken(),
                order_id)
            .compose(RxUtils.toMain())
            .onErrorResumeNext(new RxUtils.HttpResponseFunc<>())
            .subscribeWith(new BaseSubscriber<RespRefrshRenewPayOrder>() {

                @Override
                public void onNext(RespRefrshRenewPayOrder response) {
                    if (response.isSuccess()) {
                        EventBus.getDefault().post(new RefreshPlatDevsEvent());
                        try {
                            setUiData(response.getData().getRenew_infos());
                        } catch (Exception e) {
                            showToast(mContext, R.string.wx_errcode_success);
                            finish();
                        }
                    } else {
                        if (response.getErrcode() == Result.ERRCODE_ORDER_TIMEOUT_INVALID) {

                            showToast(mContext, R.string.order_timeout);
                            finish();
                        } else {
                            onError(new ExceptionHandle.ServerException(response));
                        }
                    }
                    dismissWaitDialog();
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
        mCompositeDisposable.clear();
    }

    private void goBack() {

        finish();
    }

    private void setUiData(List<RespRefrshRenewPayOrder.DataBean.RenewInfosBean> renew_infos) {
        mAdapter.setData(renew_infos);
    }

    @Override
    protected void onBackFinish() {

    }

    @Override
    protected void onBackPressed() {
        goBack();
    }

    @Override
    public void callback(int messageId, Result result) {

    }
}
