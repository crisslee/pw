package com.coomix.app.all.ui.platformRecharge;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import com.coomix.app.all.Constant;
import com.coomix.app.all.GlobalParam;
import com.coomix.app.all.R;
import com.coomix.app.all.data.BaseSubscriber;
import com.coomix.app.all.data.DataEngine;
import com.coomix.app.all.data.ExceptionHandle;
import com.coomix.app.all.data.RxUtils;
import com.coomix.app.all.manager.ThemeManager;
import com.coomix.app.all.model.event.RefreshPlatDevsEvent;
import com.coomix.app.all.model.request.ReqRenewOrder;
import com.coomix.app.all.model.response.RespPlatDevList;
import com.coomix.app.all.model.response.RespPlatOrder;
import com.coomix.app.all.ui.base.BaseActivity;
import com.coomix.app.all.util.AnimUtils;
import com.coomix.app.all.util.CommunityUtil;
import com.coomix.app.all.util.ViewUtil;
import com.coomix.app.all.widget.MyActionbar;
import com.coomix.app.pay.CoomixPayManager;
import com.coomix.app.pay.ICoomixPay;
import com.coomix.app.pay.PayResultManager;
import com.coomix.app.pay.WechatPayManager;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshRecyclerView;
import com.jakewharton.rxbinding2.view.RxView;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import java.util.concurrent.TimeUnit;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

public class PlatRechargeActivity extends BaseActivity {
    private View  bottom_layout;
    private View mEmptyView, mContentLayout;
    private TextView mEmptyViewText;
    private PullToRefreshRecyclerView mRecyclerViewWrapper;
    private RecyclerView mRecyclerView;
    private CheckBox cbSelectAll;
    private TextView tvTotalRMB;
    private Button btnRecharge;

    private static final int PAGE_SIZE = 20;
    private int currentPosition = 0;
    private boolean isInRechargeProcess = false;

    private int payPlatform = CoomixPayManager.PAY_WECHAT;
    private int payManner = WechatPayManager.WECHAT_PAY_APP;
    public static final String FROM_PLAT_RECHARGE = "FROM_PLAT_RECHARGE";

    private boolean isTitlebarOptionClicked = false;
    private PlatRechargeAdapter mAdpter;
    private MyActionbar actionbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_palt_recharge);

        initView();

        getRenewDevList(0, PAGE_SIZE);
        showProgressDialog();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(isInRechargeProcess) {
            resetState();
        }

    }
    private void resetState() {
        isInRechargeProcess = false;
        if(isTitlebarOptionClicked) {
            toggleTitlebarOption();
        }
        getRenewDevList(0, PAGE_SIZE);

    }

    private void setTotalRMB(long rmb) {
        String amount = String.format(getString(R.string.amount_yuan),
                CommunityUtil.getDecimalStrByLong(rmb, 2));
        tvTotalRMB.setText(amount);
    }

    private void setRechargeNum(int num) {
        String strNum = num == 0 ? "" : "(" + num + ")";
        btnRecharge.setText(getString(R.string.btn_renew) + strNum);
    }

    private void initView() {
        actionbar = (MyActionbar) findViewById(R.id.mineActionbar);
        actionbar.initActionbar(true, R.string.renewal, R.string.select, 0);
        actionbar.setRightTextClickListener(view -> toggleTitlebarOption());

        bottom_layout = findViewById(R.id.bottom_layout);

        cbSelectAll = (CheckBox) findViewById(R.id.cbSelectAll);
        cbSelectAll.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                mAdpter.selectAll();
            } else {
                mAdpter.cancelSelectall();
            }
        });


        tvTotalRMB = (TextView) findViewById(R.id.tvTotalRMB);
        setTotalRMB(0);
        btnRecharge = (Button) findViewById(R.id.btnRecharge);
        RxView.clicks(btnRecharge)
                .throttleFirst(1, TimeUnit.SECONDS)
                .subscribeOn(AndroidSchedulers.mainThread())
                .subscribe(v ->
                    getRenewDevOrder(mAdpter.getReqRenewDevOrders())
                );


        mContentLayout = findViewById(R.id.content_layout);

        mEmptyView = findViewById(R.id.empty_layout);
        mEmptyViewText = (TextView) findViewById(R.id.empty);
        mEmptyViewText.setText(R.string.no_device);

        mRecyclerViewWrapper = (PullToRefreshRecyclerView) findViewById(R.id.recycler_view_wrap);
        mRecyclerViewWrapper.setMode(PullToRefreshBase.Mode.BOTH);
        ViewUtil.setPtrStateText(mRecyclerViewWrapper);

        mRecyclerViewWrapper.setScrollingWhileRefreshingEnabled(true);
        mRecyclerViewWrapper.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener2<RecyclerView>() {
            @Override
            public void onPullDownToRefresh(PullToRefreshBase<RecyclerView> refreshView) {
                if(!mAdpter.ismSeletMode()) { //平台充值，多选模式下 不能下拉刷新,与 ios一致
                    getRenewDevList(0, PAGE_SIZE);
                } else {
                    stopRefreshingUi();
                }
            }

            @Override
            public void onPullUpToRefresh(PullToRefreshBase<RecyclerView> refreshView) {
                getRenewDevList(currentPosition, PAGE_SIZE);
            }
        });

        mRecyclerView = mRecyclerViewWrapper.getRefreshableView();
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(PlatRechargeActivity.this,
                RecyclerView.VERTICAL, false));

        mAdpter = new PlatRechargeAdapter();
        mAdpter.setmOnItemSelectListener(new PlatRechargeAdapter.OnItemSelectListener() {
            @Override
            public void onItemsSelected(long totalMoney, int totalNum) {
                setTotalRMB(totalMoney);
                setRechargeNum(totalNum);
            }

            @Override
            public void showEmpty(boolean show) {
                showEmptyView(show);
            }
        });
        mAdpter.setmOnBtnRechargeOnclickListne(reqRenewOrder -> getRenewDevOrder(reqRenewOrder));
        mRecyclerView.setAdapter(mAdpter);
        mAdpter.notifyDataSetChanged();

        btnRecharge.setBackgroundColor(ThemeManager.getInstance().getThemeAll().getThemeColor().getColorWhole());
        btnRecharge.setTextColor(ThemeManager.getInstance().getThemeAll().getThemeColor().getColorNavibarText());
    }

    private void toggleTitlebarOption() {

        isTitlebarOptionClicked = !isTitlebarOptionClicked;
        if (!isTitlebarOptionClicked) {
            actionbar.setRightText(R.string.select);
            bottom_layout.setAnimation(AnimUtils.moveToViewBottom());
            bottom_layout.setVisibility(View.GONE);
        } else {
            actionbar.setRightText(R.string.cancel);
            Animation animation = AnimUtils.moveToViewLocation();
            animation.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {

                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    bottom_layout.setVisibility(View.VISIBLE);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
            bottom_layout.setAnimation(animation);
        }
        mAdpter.setSelectMode(isTitlebarOptionClicked);
    }

    private void showEmptyView(boolean show) {
        if (!show) {
            mEmptyView.setVisibility(View.GONE);
            mContentLayout.setVisibility(View.VISIBLE);
        } else {
            mEmptyView.setVisibility(View.VISIBLE);
            mContentLayout.setVisibility(View.GONE);
        }
    }

    private void getRenewDevOrder(ReqRenewOrder reqRenewOrder) {
        if (!reqRenewOrder.isValid()) {
            showErr(getResources().getString(R.string.pls_select_one));
            return;
        }
        showProgressDialog();
        Disposable disposable = DataEngine.getAllMainApi()
                .getRenewOrder(
                        GlobalParam.getInstance().getCommonParas(),
                        GlobalParam.getInstance().getAccessToken(),
                        reqRenewOrder)
                .compose(RxUtils.toMain())
                .compose(RxUtils.businessTransformer())
                .subscribeWith(new BaseSubscriber<RespPlatOrder>() {
                    @Override
                    public void onNext(RespPlatOrder respPlatOrder) {
                        startWechatPay(respPlatOrder, reqRenewOrder.getAmount());
                        dismissProgressDialog();
                    }

                    @Override
                    public void onHttpError(ExceptionHandle.ResponeThrowable e) {
                        showToast(e.getErrCodeMessage());
                        dismissProgressDialog();
                    }
                });
        subscribeRx(disposable);
    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

    @Subscribe
    public void refresh(RefreshPlatDevsEvent event) {
        if (Constant.IS_DEBUG_MODE) {
            Log.d(PlatRechargeActivity.class.getCanonicalName(), "Get a event: RefreshPlatDevsEvent");
        }
        resetState();
    }

    private void startWechatPay(RespPlatOrder respPlatOrder, long rechargeAmount) {
        isInRechargeProcess = true;
        CoomixPayManager coomixPayManager = new CoomixPayManager(this, payPlatform);
        PayResultManager.getInstance().setPayOrderType(ICoomixPay.ORDER_FROM.FROM_RECHARGE_PLATFORM_DEVICES);
        PayResultManager.getInstance().setRechargeAmount(rechargeAmount);
        PayResultManager.getInstance().setOrder_id(respPlatOrder.getData().getOrder_id());

        PayResultManager.getInstance().setRechargeFrom(FROM_PLAT_RECHARGE);
        coomixPayManager.pay(respPlatOrder.getData().getWx_pay());
    }

    private void getRenewDevList(int beginpos, int pagesize) {
        Disposable disposable = DataEngine.getAllMainApi()
            .getRenewDevList(GlobalParam.getInstance().getCommonParas(), GlobalParam.getInstance().getAccessToken(),
                beginpos, pagesize)
            .compose(RxUtils.toMain())
            .compose(RxUtils.businessTransformer())
            .subscribeWith(new BaseSubscriber<RespPlatDevList>(null) {
                @Override
                public void onNext(RespPlatDevList respPlatRecharge) {
                    stopRefreshingUi();
                    if (respPlatRecharge.getData() != null) {
                        if (beginpos == 0) {
                            mAdpter.setData(respPlatRecharge.getData());
                            currentPosition = 0;
                        } else {
                            mAdpter.addData(respPlatRecharge.getData());
                        }
                        currentPosition += respPlatRecharge.getData().size();
                        showEmptyView(false);
                    }
                }

                @Override
                public void onHttpError(ExceptionHandle.ResponeThrowable e) {
                    if (beginpos == 0) {
                        showEmptyView(true);
                        currentPosition = 0;
                    }
                    showToast(e.getErrCodeMessage());
                    stopRefreshingUi();
                }
            });

        subscribeRx(disposable);
    }


    private void stopRefreshingUi() {
        if (mRecyclerViewWrapper.isRefreshing()) {
            mRecyclerViewWrapper.onRefreshComplete();
        }
        dismissProgressDialog();
    }
}
