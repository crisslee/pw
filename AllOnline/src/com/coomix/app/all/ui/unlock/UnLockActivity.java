package com.coomix.app.all.ui.unlock;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.mapsdkplatform.comjni.engine.AppEngine;
import com.coomix.app.all.GlobalParam;
import com.coomix.app.all.R;
import com.coomix.app.all.data.BaseSubscriber;
import com.coomix.app.all.data.DataEngine;
import com.coomix.app.all.data.ExceptionHandle;
import com.coomix.app.all.data.RxUtils;
import com.coomix.app.all.model.response.LockInfo;
import com.coomix.app.all.model.response.RespBase;
import com.coomix.app.all.model.response.RespLockList;
import com.coomix.app.all.ui.base.BaseActivity;
import com.coomix.app.all.ui.cardRecharge.CardRechargeActivity;
import com.coomix.app.all.util.AnimUtils;
import com.coomix.app.all.util.ViewUtil;
import com.coomix.app.all.widget.MyActionbar;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshRecyclerView;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.disposables.Disposable;

/**
 * 开锁审批
 * wangchuntao
 * 2020.02.25
 */
public class UnLockActivity extends BaseActivity {


    @BindView(R.id.mineActionbar)
    MyActionbar mActionbar;
    @BindView(R.id.content_layout)
    View mContentLayout;
    @BindView(R.id.recycler_view_wrap)
    PullToRefreshRecyclerView mRecyclerViewWrapper;
    @BindView(R.id.bottom_layout)
    View mBottomLayout;
    @BindView(R.id.cbSelectAll)
    CheckBox mCheckBox;
    @BindView(R.id.btnLock)
    Button mLockBtn;
    @BindView(R.id.btnUnLock)
    Button mUnLockBtn;
    @BindView(R.id.empty_layout)
    View mEmptyView;
    @BindView(R.id.empty)
    TextView mEmptyTxt;


    private boolean isTitlebarOptionClicked = false;
    private UnLockAdapter mAdapter;
    private RecyclerView mRecyclerView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_un_lock);
        ButterKnife.bind(this);
        initView();
        getLockList();
    }


    private void initView(){

        mActionbar.initActionbar(true , R.string.un_lock_title , R.string.select , 0);
        mActionbar.setRightTextClickListener(View -> toggleTitlebarOption());
        mCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            mAdapter.setSelectAll(isChecked);
        });

        mLockBtn.setOnClickListener(View -> actApproveLock(mAdapter.getInfos()));
        mUnLockBtn.setOnClickListener(View -> actRejectLock(mAdapter.getInfos()));

        mRecyclerViewWrapper.setMode(PullToRefreshBase.Mode.PULL_FROM_START);
        ViewUtil.setPtrStateText(mRecyclerViewWrapper);
        mRecyclerViewWrapper.setScrollingWhileRefreshingEnabled(true);
        mRecyclerViewWrapper.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener<RecyclerView>() {
            @Override
            public void onRefresh(PullToRefreshBase<RecyclerView> refreshView) {
                getLockList();
            }
        });

        mAdapter = new UnLockAdapter(this);
        mRecyclerView = mRecyclerViewWrapper.getRefreshableView();
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(UnLockActivity.this,
                RecyclerView.VERTICAL, false));
        mRecyclerView.setAdapter(mAdapter);
    }




    private void toggleTitlebarOption() {
        isTitlebarOptionClicked = !isTitlebarOptionClicked;
        if (!isTitlebarOptionClicked) {
            mRecyclerViewWrapper.setMode(PullToRefreshBase.Mode.PULL_FROM_START);
            mActionbar.setRightText(R.string.select);
            mBottomLayout.setAnimation(AnimUtils.moveToViewBottom());
            mBottomLayout.setVisibility(View.GONE);
        } else {
            mRecyclerViewWrapper.setMode(PullToRefreshBase.Mode.DISABLED);
            mActionbar.setRightText(R.string.cancel);
            Animation animation = AnimUtils.moveToViewLocation();
            animation.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {

                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    mBottomLayout.setVisibility(View.VISIBLE);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
            mBottomLayout.setAnimation(animation);
        }
        mAdapter.setSelectMode(isTitlebarOptionClicked);
    }



    public void selectAll(boolean select){
        if(select && mCheckBox.isChecked()){
            return;
        }

        if(!select && !mCheckBox.isChecked()){
            return;
        }

        mCheckBox.setOnCheckedChangeListener(null);
        mCheckBox.setChecked(select);
        mCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            mAdapter.setSelectAll(isChecked);
        });
    }


    private void showEmptyView(boolean show){
        mEmptyTxt.setText(R.string.lock_list_empty);
        if(show){
            mEmptyView.setVisibility(View.VISIBLE);
            mContentLayout.setVisibility(View.GONE);
            mActionbar.setRightText("");
        }else {
            mEmptyView.setVisibility(View.GONE);
            mContentLayout.setVisibility(View.VISIBLE);
            mActionbar.setRightText(R.string.select);
        }
    }



    private void getLockList(){
        showProgressDialog();
        Disposable disposable = DataEngine.getAllMainApi().getQueryLockList(GlobalParam.getInstance().getAccessToken())
                .compose(RxUtils.toMain())
                .compose(RxUtils.businessTransformer())
                .subscribeWith(new BaseSubscriber<RespLockList>() {
                    @Override
                    public void onNext(RespLockList respLockList) {

                        if(respLockList != null){
                            List<LockInfo> lockInfos = respLockList.getLockInfoList();
                            mAdapter.refreshData(lockInfos);
                        }

                        dismissProgressDialog();
                        if(mRecyclerViewWrapper.isRefreshing()){
                            mRecyclerViewWrapper.onRefreshComplete();
                        }


                        if(mAdapter.isEmpty()){
                            showEmptyView(true);
                        }
                    }

                    @Override
                    public void onHttpError(ExceptionHandle.ResponeThrowable e) {
                        dismissProgressDialog();
                        if(mRecyclerViewWrapper.isRefreshing()){
                            mRecyclerViewWrapper.onRefreshComplete();
                        }
                        showToast(e.message);

                        if(mAdapter.isEmpty()){
                            showEmptyView(true);
                        }
                    }
                });
        subscribeRx(disposable);
    }


    public void actApproveLock(String infos){
        if(TextUtils.isEmpty(infos)){
            showToast(getString(R.string.pls_select_common));
            return;
        }
        Disposable disposable = DataEngine.getAllMainApi().actApproveLock(infos , GlobalParam.getInstance().getAccessToken())
                .compose(RxUtils.toMain())
                .compose(RxUtils.businessTransformer())
                .subscribeWith(new BaseSubscriber<RespBase>() {
                    @Override
                    public void onNext(RespBase respBase) {
                        if(respBase != null && respBase.isSuccess()){
                            showToast(getString(R.string.lock_approve_success));
                            getLockList();
                        }
                    }

                    @Override
                    public void onHttpError(ExceptionHandle.ResponeThrowable e) {
                        showToast(e.message);
                    }
                });
        subscribeRx(disposable);
    }


    public void actRejectLock(String infos){
        if(TextUtils.isEmpty(infos)){
            showToast(getString(R.string.pls_select_common));
            return;
        }
        Disposable disposable = DataEngine.getAllMainApi().actRejectLock(infos,GlobalParam.getInstance().getAccessToken())
                .compose(RxUtils.toMain())
                .compose(RxUtils.businessTransformer())
                .subscribeWith(new BaseSubscriber<RespBase>() {
                    @Override
                    public void onNext(RespBase respBase) {
                        if(respBase != null && respBase.isSuccess()){
                            showToast(getString(R.string.lock_reject_success));
                            getLockList();
                        }
                    }

                    @Override
                    public void onHttpError(ExceptionHandle.ResponeThrowable e) {
                        showToast(e.message);
                    }
                });
        subscribeRx(disposable);
    }
}
