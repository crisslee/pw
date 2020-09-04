package com.coomix.app.all.ui.cardRecharge;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import com.coomix.app.all.AllOnlineApp;
import com.coomix.app.all.Constant;
import com.coomix.app.all.GlobalParam;
import com.coomix.app.all.R;
import com.coomix.app.all.data.BaseSubscriber;
import com.coomix.app.all.data.DataEngine;
import com.coomix.app.all.data.ExceptionHandle;
import com.coomix.app.all.data.RxUtils;
import com.coomix.app.all.manager.ThemeManager;
import com.coomix.app.all.model.event.RefreshPlatDevsEvent;
import com.coomix.app.all.model.response.RenewWlCard;
import com.coomix.app.all.model.response.RespRenewWlCard;
import com.coomix.app.all.model.response.RespWLCardExpire;
import com.coomix.app.all.model.response.RespWLCardRecharge;
import com.coomix.app.all.model.response.WLCardRecharge;
import com.coomix.app.all.ui.base.BaseActivity;
import com.coomix.app.all.util.AnimUtils;
import com.coomix.app.all.util.CommunityUtil;
import com.coomix.app.all.util.ViewUtil;
import com.coomix.app.all.widget.MyActionbar;
import com.coomix.app.pay.CoomixPayManager;
import com.coomix.app.pay.ICoomixPay;
import com.coomix.app.pay.PayResultManager;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshRecyclerView;
import com.jakewharton.rxbinding2.view.RxView;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

public class CardRechargeActivity extends BaseActivity {
    private View bottom_layout;
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

    private boolean isTitlebarOptionClicked = false;
    private CardRechargeAdapter mAdpter;
    private MyActionbar actionbar;
    private String distribute;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_palt_recharge);

        initView();

        getExpireWlCardDistributeNum();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (isInRechargeProcess) {
            resetState();
        }
    }

    private void resetState() {
        isInRechargeProcess = false;
        if (isTitlebarOptionClicked) {
            toggleTitlebarOption();
        }
        getRenewCardList(0, PAGE_SIZE);

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
        actionbar.initActionbar(true, R.string.card_recharge, R.string.select, 0);
        actionbar.setRightTextClickListener(view -> toggleTitlebarOption());

        bottom_layout = findViewById(R.id.bottom_layout);

        cbSelectAll = (CheckBox) findViewById(R.id.cbSelectAll);
        cbSelectAll.setOnCheckedChangeListener((buttonView, isChecked) -> {
            mAdpter.setSelectAll(isChecked);
        });


        tvTotalRMB = (TextView) findViewById(R.id.tvTotalRMB);
        setTotalRMB(0);
        btnRecharge = (Button) findViewById(R.id.btnRecharge);
        RxView.clicks(btnRecharge)
                .throttleFirst(1, TimeUnit.SECONDS)
                .subscribeOn(AndroidSchedulers.mainThread())
                .subscribe(v ->
                        getRenewDevOrder(mAdpter.getSelectedCardList())
//                        mAdpter.isSeletMode()
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
                if (!mAdpter.isSeletMode()) { //平台充值，多选模式下 不能下拉刷新,与 ios一致
                    getRenewCardList(0, PAGE_SIZE);
                } else {
                    stopRefreshingUi();
                }
            }

            @Override
            public void onPullUpToRefresh(PullToRefreshBase<RecyclerView> refreshView) {
                getRenewCardList(currentPosition, PAGE_SIZE);
            }
        });

        mRecyclerView = mRecyclerViewWrapper.getRefreshableView();
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(CardRechargeActivity.this,
                RecyclerView.VERTICAL, false));

        mAdpter = new CardRechargeAdapter();
        mAdpter.setmOnItemSelectListener(new CardRechargeAdapter.OnItemSelectListener() {
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

        mRecyclerView.setAdapter(mAdpter);
        mAdpter.notifyDataSetChanged();
        mAdpter.setmOnBtnRechargeOnclickListne(new CardRechargeAdapter.OnBtnRechargeOnclickListne() {
            @Override
            public void onClick(WLCardRecharge wlCardRecharge) {
                if (wlCardRecharge == null) {
                    return;
                }
                ArrayList<WLCardRecharge> list = new ArrayList<WLCardRecharge>();
                list.add(wlCardRecharge);
                getRenewDevOrder(list);
            }
        });

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

    long amount = 0;

    private void getRenewDevOrder(ArrayList<WLCardRecharge> listCards) {
        if (listCards == null || listCards.size() <= 0) {
            showErr(getResources().getString(R.string.pls_select_one));
            return;
        }
        showProgressDialog();
        StringBuilder stringBuilder = new StringBuilder();
        int size = listCards.size() - 1;
        amount = 0;
        for (int i = 0; i <= size; i++) {
            stringBuilder.append(listCards.get(i).getMsisdn());
            amount += listCards.get(i).getPrice();
            if (i != size) {
                stringBuilder.append(",");
            }
        }
        Disposable disposable = DataEngine.getAllMainApi()
                .renewWlcard(
                        GlobalParam.getInstance().getCommonParas(),
                        AllOnlineApp.sAccount,
                        AllOnlineApp.sTarget,
                    GlobalParam.getInstance().getAccessToken(),
                        "app",
                        stringBuilder.toString())
                .compose(RxUtils.toMain())
                .compose(RxUtils.businessTransformer())
                .subscribeWith(new BaseSubscriber<RespRenewWlCard>() {
                    @Override
                    public void onNext(RespRenewWlCard respRenewWlCard) {
                        startWechatPay(respRenewWlCard.getData(), amount);
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
            Log.d(CardRechargeActivity.class.getCanonicalName(), "Get a event: RefreshPlatDevsEvent");
        }
        resetState();
    }

    private void startWechatPay(RenewWlCard renewWlCard, long rechargeAmount) {
        isInRechargeProcess = true;
        CoomixPayManager coomixPayManager = new CoomixPayManager(this, CoomixPayManager.PAY_WECHAT);
        PayResultManager.getInstance().setPayOrderType(ICoomixPay.ORDER_FROM.FROM_RECHARGE_PLATFORM_DEVICES);
        PayResultManager.getInstance().setRechargeAmount(rechargeAmount);
        PayResultManager.getInstance().setOrder_id(renewWlCard.getOrder_id());

        //PayResultManager.getInstance().setRechargeFrom(FROM_PLAT_RECHARGE);
        coomixPayManager.pay(renewWlCard.getWx_pay());
    }


    private void getExpireWlCardDistributeNum() {
        showProgressDialog();
        Disposable disposable = DataEngine.getAllMainApi()
                .getExpireWlCardDistributeNum(
                        GlobalParam.getInstance().getCommonParas(),
                        AllOnlineApp.sAccount,
                        AllOnlineApp.sTarget,
                    GlobalParam.getInstance().getAccessToken(),
                        3,
                        "0-0")
                .compose(RxUtils.toMain())
                .compose(RxUtils.businessTransformer())
                .subscribeWith(new BaseSubscriber<RespWLCardExpire>(null) {
                    @Override
                    public void onNext(RespWLCardExpire respWLCardExpire) {
                        if (respWLCardExpire != null && respWLCardExpire.getData() != null) {
                            distribute = respWLCardExpire.getData().getDistribute();
                            getRenewCardList(0, PAGE_SIZE);
                        }
                    }

                    @Override
                    public void onHttpError(ExceptionHandle.ResponeThrowable e) {
                        dismissProgressDialog();
                    }
                });

        subscribeRx(disposable);
    }

    private void getRenewCardList(int beginpos, int pagesize) {
        showProgressDialog();
        Disposable disposable = DataEngine.getAllMainApi()
                .getExpireWlCardInfo(
                        GlobalParam.getInstance().getCommonParas(),
                        AllOnlineApp.sAccount,
                        AllOnlineApp.sTarget,
                    GlobalParam.getInstance().getAccessToken(),
                        3,
                        "0-0",
                        beginpos,
                        pagesize,
                        distribute)
                .compose(RxUtils.toMain())
                .compose(RxUtils.businessTransformer())
                .subscribeWith(new BaseSubscriber<RespWLCardRecharge>(null) {
                    @Override
                    public void onNext(RespWLCardRecharge respWLCardPrice) {
                        dismissProgressDialog();
                        stopRefreshingUi();
                        if (respWLCardPrice.getData() != null) {
                            if (beginpos == 0) {
                                mAdpter.setData(respWLCardPrice.getData().getRecords());
                                currentPosition = 0;
                            } else {
                                mAdpter.addData(respWLCardPrice.getData().getRecords());
                            }
                            currentPosition += respWLCardPrice.getData().getRecords().size();
                            showEmptyView(false);
                        }
                    }

                    @Override
                    public void onHttpError(ExceptionHandle.ResponeThrowable e) {
                        dismissProgressDialog();
                        if (beginpos == 0) {
                            showEmptyView(true);
                            currentPosition = 0;
                        }
                        //showToast(e.getErrCodeMessage());
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
