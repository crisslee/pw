package com.coomix.app.all.ui.alarm;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.coomix.app.all.AllOnlineApp;
import com.coomix.app.all.GlobalParam;
import com.coomix.app.all.R;
import com.coomix.app.all.widget.MyActionbar;
import com.coomix.app.framework.util.ExtraConstants;
import com.coomix.app.all.data.BaseSubscriber;
import com.coomix.app.all.data.DataEngine;
import com.coomix.app.all.data.ExceptionHandle;
import com.coomix.app.all.data.RxUtils;
import com.coomix.app.all.model.response.AlarmCategoryItem;
import com.coomix.app.all.model.response.RespAlarmDetailList;
import com.coomix.app.all.model.response.RespBase;
import com.coomix.app.all.ui.base.BaseActivity;
import com.coomix.app.all.util.ViewUtil;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.Mode;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener2;
import com.handmark.pulltorefresh.library.PullToRefreshRecyclerView;

import java.util.ArrayList;

import io.reactivex.disposables.Disposable;

/**
 * Created by herry on 2017/1/12.
 */

public class AlarmDetailListActivity extends BaseActivity
        implements OnRefreshListener2<RecyclerView>, AlarmDetailListAdapter.OnItemSelectListener {
    public static final String PAGE_DIR_PRE = "pre";
    public static final String PAGE_DIR_NEXT = "next";
    private static final int PAGE_SIZE = 15;
    private int mDelCount;

    private int mAlarmCategoryPosition;
    private String mImei;
    private AlarmCategoryItem mAlarmCategoryItem;

    private AlarmDetailListAdapter mAdapter;
    private boolean mInEditMode;
    private boolean mHasDelSelected;
    private boolean isAllSelected;

    private PullToRefreshRecyclerView mRecyclerViewWrapper;
    private RecyclerView mRecyclerView;
    private TextView mDeleteBtn;
    private View mDeleteLayout;
    private View mContentLayout;
    private TextView mSelectorView;
    private MyActionbar actionbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_alarm_category_item_list_layout);

        Intent intent = getIntent();
        mAlarmCategoryPosition = intent.getIntExtra(ExtraConstants.EXTRA_ALARM_CATEGORY_POSITION, 0);
        mImei = intent.getStringExtra(ExtraConstants.EXTRA_IMEI);
        if (intent.hasExtra(ExtraConstants.EXTRA_ALARM_CATEGORY_ITEM)) {
            mAlarmCategoryItem = (AlarmCategoryItem) intent.getSerializableExtra(ExtraConstants.EXTRA_ALARM_CATEGORY_ITEM);
        } else {
            finish();
            return;
        }

        initViews();

        getAlarmDetailList(PAGE_DIR_NEXT, 0);
    }

    private void initViews() {
        mInEditMode = false;
        mHasDelSelected = false;
        isAllSelected = false;
        mDelCount = 0;

        actionbar = (MyActionbar) findViewById(R.id.myActionbar);
        actionbar.initActionbar(true, mAlarmCategoryItem.isAlias() ? mAlarmCategoryItem.getAlarm_type_alias() : mAlarmCategoryItem.getAlarm_type(),0, 0);

        mSelectorView = (TextView) findViewById(R.id.all_item_selector);
        mDeleteBtn = (TextView) findViewById(R.id.delete_btn);

        mDeleteLayout = findViewById(R.id.delete_layout);
        mContentLayout = findViewById(R.id.content_layout);
        mRecyclerViewWrapper = (PullToRefreshRecyclerView) findViewById(R.id.recycler_view_wrap);
        setBothMode();
        ViewUtil.setPtrStateText(mRecyclerViewWrapper);
        mRecyclerViewWrapper.setScrollingWhileRefreshingEnabled(true);
        mRecyclerViewWrapper.setOnRefreshListener(this);
        mRecyclerView = mRecyclerViewWrapper.getRefreshableView();
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this, RecyclerView.VERTICAL, false));
        setOptionText();
        setDelBtnTextColor();

        mSelectorView.setOnClickListener(view -> {
            isAllSelected = !isAllSelected;
            if (isAllSelected) {
                mSelectorView.setText(getResources().getText(R.string.all_not_selected));
            } else {
                mSelectorView.setText(getResources().getText(R.string.all_selected));
            }
            if (mAdapter != null) {
                mAdapter.setAllSelected(isAllSelected);
            }
        });

        mDeleteBtn.setOnClickListener(view -> {
            deleteSection();
        });

        actionbar.setRightTextClickListener(view -> {
            if (mAdapter == null || mAdapter.getItemCount() <= 0) {
                return;
            }
            mInEditMode = !mInEditMode;
            setOptionText();
            mAdapter.setInEditMode(mInEditMode);
        });
    }

    @Override
    public void finish() {
        if (mDelCount > 0) {
            Intent data = new Intent();
            data.putExtra(ExtraConstants.EXTRA_ALARM_CATEGORY_POSITION, mAlarmCategoryPosition);
            data.putExtra(ExtraConstants.EXTRA_ALARM_DELETION_COUNT, mDelCount);
            setResult(RESULT_OK, data);
        }
        super.finish();
    }

    @Override
    public void onHasItemSelected(boolean selected) {
        mHasDelSelected = selected;
        setDelBtnTextColor();
    }

    @Override
    public void onEmpty() {
        setOptionText();
    }

    @Override
    public void onPullDownToRefresh(PullToRefreshBase<RecyclerView> refreshView) {
        // load newer page
        getAlarmDetailList(PAGE_DIR_NEXT, mAdapter.getNextAlarmTime());
    }

    @Override
    public void onPullUpToRefresh(PullToRefreshBase<RecyclerView> refreshView) {
        // load next page
        getAlarmDetailList(PAGE_DIR_PRE, mAdapter.getPrevAlarmTime());
    }

    private void deleteSection() {
        if (mHasDelSelected) {
            showDelDialog();
        }
    }

    private void getAlarmDetailList(final String pageDir, long timestamp) {
        showLoading(getString(R.string.please_wait));
        Disposable d = DataEngine.getAllMainApi()
                .getAlarmDetailList(GlobalParam.getInstance().getCommonParas(), AllOnlineApp.sAccount,
                        AllOnlineApp.sToken.access_token, timestamp, mImei == null ? "" : mImei, getAlarmType(), pageDir, PAGE_SIZE)
                .compose(RxUtils.toMain())
                .compose(RxUtils.businessTransformer())
                .subscribeWith(new BaseSubscriber<RespAlarmDetailList>() {
                    @Override
                    public void onNext(RespAlarmDetailList respAlarmDetailList) {
                        hideLoading();
                        if (mRecyclerViewWrapper.isRefreshing()) {
                            mRecyclerViewWrapper.onRefreshComplete();
                        }
                        if (mAdapter == null) {
                            mAdapter = new AlarmDetailListAdapter(AlarmDetailListActivity.this, AlarmDetailListActivity.this, fixQueryAlarmType(), respAlarmDetailList.getData());
                            mRecyclerView.setAdapter(mAdapter);
                        } else {
                            if (pageDir.equals(PAGE_DIR_NEXT)) {
                                mAdapter.appendPrevPageData(respAlarmDetailList.getData());
                            } else {
                                mAdapter.appendNextPageData(respAlarmDetailList.getData());
                            }
                        }
                    }

                    @Override
                    public void onHttpError(ExceptionHandle.ResponeThrowable e) {
                        hideLoading();
                        showToast(e.getErrMessage());
                        if (mRecyclerViewWrapper.isRefreshing()) {
                            mRecyclerViewWrapper.onRefreshComplete();
                        }
                    }
                });
        subscribeRx(d);
    }

    /***删除，相当于标记为已读**/
    private void setAlarmRead(ArrayList<String> listIds, long timestamp, boolean except) {
        showLoading(getString(R.string.please_wait));
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0, size = listIds.size(); i < size; i++) {
            stringBuilder.append(listIds.get(i));
            if (i != size - 1) {
                stringBuilder.append(",");
            }
        }
        Disposable d = DataEngine.getAllMainApi()
                .setAlarmRead(GlobalParam.getInstance().getCommonParas(), AllOnlineApp.sAccount,
                        AllOnlineApp.sToken.access_token, timestamp, mImei == null ? "" : mImei, getAlarmType(), stringBuilder.toString(), except)
                .compose(RxUtils.toMain())
                .compose(RxUtils.businessTransformer())
                .subscribeWith(new BaseSubscriber<RespBase>() {
                    @Override
                    public void onNext(RespBase resp) {
                        hideLoading();
                        if (mAdapter != null) {
                            mDelCount += mAdapter.getDelSlectedCount();
                            mAdapter.onDelSuccess();
                        }
                        mInEditMode = !mInEditMode;
                        mHasDelSelected = false;
                        setOptionText();
                        setDelBtnTextColor();
                        showToast(getString(R.string.delete_success));
                    }

                    @Override
                    public void onHttpError(ExceptionHandle.ResponeThrowable e) {
                        showToast(e.getErrMessage());
                    }
                });
        subscribeRx(d);
    }

    private void setOptionText() {
        if (mInEditMode) {
            actionbar.setRightText(R.string.alarm_cancel_btn_text);
            mDeleteLayout.setVisibility(View.VISIBLE);
        } else {
            actionbar.setRightText(R.string.alarm_delete_btn_text);
            mDeleteLayout.setVisibility(View.GONE);
        }
    }

    private void setDelBtnTextColor() {
        if (mHasDelSelected) {
            mDeleteBtn.setTextColor(getResources().getColor(R.color.btn_enable_text_color));
        } else {
            mDeleteBtn.setTextColor(getResources().getColor(R.color.btn_disable_text_color));
        }
    }

    private void showDelDialog() {
        final Dialog dialog = new Dialog(this, R.style.Dialog);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_alarm_ensure_read, null);
        //获得dialog的window窗口
        Window window = dialog.getWindow();
        //设置dialog在屏幕底部
        window.setGravity(Gravity.BOTTOM);
        window.getDecorView().setPadding(0, 0, 0, 0);
        //获得window窗口的属性
        android.view.WindowManager.LayoutParams lp = window.getAttributes();
        //设置窗口宽度为充满全屏
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        //设置窗口高度为包裹内容
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        //将设置好的属性set回去
        window.setAttributes(lp);
        //将自定义布局加载到dialog上
        dialog.setContentView(dialogView);
        TextView ensureTitle = (TextView) dialogView.findViewById(R.id.message1);
        final ArrayList<String> ids = mAdapter.getDelSelection();
        final ArrayList<String> idsKeep = mAdapter.getKeepSelection(ids);

        int count = 0;
        if (isAllSelected) {
            int total = 0;
            if (mAlarmCategoryItem.isAlias()) {
                total = mAlarmCategoryItem.getAlarm_num_alias();
            } else {
                total = mAlarmCategoryItem.getAlarm_num();
            }

            if (ids.size() == mAdapter.getItemCount()) {
                count = total;
            } else {
                count = total - idsKeep.size();
            }
        } else {
            count = ids.size();
        }
        ensureTitle.setText(String.format(getResources().getString(R.string.ensure_read_dialog), count));

        TextView ensureBtn = (TextView) dialogView.findViewById(R.id.ensureBtn);
        ensureBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (dialog != null) {
                    dialog.dismiss();
                }
                Long timeStamp = 0L;
                if (isAllSelected) {
                    if (mAdapter.getmDataList() != null && mAdapter.getmDataList().size() > 0) {
                        timeStamp = mAdapter.getmDataList().get(0).getAlarm_time();
                    }
                    if (ids.size() == mAdapter.getItemCount()) {
                        //全选删除，不用传id，except为true
                        ids.clear();
                        setAlarmRead(ids, timeStamp, true);
                    } else {
                        //全选后某些不选，删除，传不删除的id，except为true
                        setAlarmRead(idsKeep, timeStamp, true);
                    }
                } else {
                    setAlarmRead(ids, 0L, false);
                }
            }
        });

        TextView cancleBtn = (TextView) dialogView.findViewById(R.id.cancleBtn);
        cancleBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });
        dialog.show();
    }

    protected void setBothMode() {
        mRecyclerViewWrapper.setMode(Mode.BOTH);
    }

    private int fixQueryAlarmType() {
        if (mAlarmCategoryItem.isAlias()) {
            return -1;
        } else {
            return mAlarmCategoryItem.getAlarm_type_id();
        }
    }

    private String getAlarmType() {
        if (mAlarmCategoryItem.isAlias()) {
            return "";
        } else {
            return String.valueOf(mAlarmCategoryItem.getAlarm_type_id());
        }
    }
}
