package com.coomix.app.all.ui.alarm;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.text.TextUtils;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.coomix.app.all.AllOnlineApp;
import com.coomix.app.all.GlobalParam;
import com.coomix.app.all.R;
import com.coomix.app.all.data.BaseSubscriber;
import com.coomix.app.all.data.DataEngine;
import com.coomix.app.all.data.ExceptionHandle;
import com.coomix.app.all.data.RxUtils;
import com.coomix.app.all.model.response.AlarmCategoryItem;
import com.coomix.app.all.model.response.RespAlarmCategoryList;
import com.coomix.app.all.model.response.RespBindedWx;
import com.coomix.app.all.ui.base.BaseActivity;
import com.coomix.app.all.ui.login.LoginActivity;
import com.coomix.app.all.util.AlarmCategoryUtils;
import com.coomix.app.all.util.StringUtil;
import com.coomix.app.all.widget.MyActionbar;
import com.coomix.app.all.widget.dragable.ItemDragHelperCallback;
import com.coomix.app.framework.util.ExtraConstants;
import com.goome.gpns.GPNSInterface;
import io.reactivex.disposables.Disposable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by herry on 2017/1/9.
 */
public class AlarmCategoryListActivity extends BaseActivity {
    private static final String TAG = "AlarmCategoryListActivity";

    public static final String IMEI = "key_imei";
    private RecyclerView mRecyclerView;
    private View mEmptyLayout;
    private TextView followWechat;
    private AlarmCategoryListAdapter mAdapter;
    List<AlarmCategoryItem> mDataList;
    private String mImei = "";

    private int mRecyclerViewBottomMargin;

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.activity_alarm_category_list_layout);

        initViews();

        if (getIntent() != null && getIntent().hasExtra(IMEI)) {
            mImei = getIntent().getStringExtra(IMEI);
        }

        registerAlarmReceiver();
    }

    @Override
    protected void onResume() {
        super.onResume();

        getAlarmCategoryList();
        getBindWx();
    }

    @Override
    protected void onDestroy() {
        unregisterAlarmReceiver();
        super.onDestroy();
    }

    private void initViews() {
        MyActionbar actionbar = (MyActionbar) findViewById(R.id.myActionbar);
        actionbar.initActionbar(true, R.string.alarm_category_title, 0, R.drawable.alarm_category_title_setting);
        actionbar.setRightImageClickListener(view -> onOptionClick());

        followWechat = findViewById(R.id.followWechat);
        followWechat.setOnClickListener(v -> {
            startActivity(new Intent(AlarmCategoryListActivity.this, FollowWechatActivity.class));
        });

        mRecyclerViewBottomMargin = getResources().getDimensionPixelSize(
                R.dimen.alarm_category_recyclerview_bottom_margin);
        mEmptyLayout = findViewById(R.id.empty_layout);
        mEmptyLayout.setVisibility(View.GONE);
        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerview);
        LinearLayoutManager manager = new LinearLayoutManager(this);
        Resources res = getResources();
        mRecyclerView.addItemDecoration(
            new RecyclerItemDecoration(
                res.getDimensionPixelSize(R.dimen.collect_seperator_thin),
                res.getColor(R.color.line_show_thf),
                res.getDimensionPixelSize(R.dimen.alarm_category_short_length)));
        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                fixRecyclerViewMargin();
            }

            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (newState == RecyclerView.SCROLL_STATE_DRAGGING) {
                    fixRecyclerViewMarginWithDragging();
                }
            }
        });
        mRecyclerView.setLayoutManager(manager);
    }

    private void getAlarmCategoryList() {
        showLoading(getString(R.string.please_wait));
        Disposable d = DataEngine.getAllMainApi()
            .getAlarmCategoryList(GlobalParam.getInstance().getCommonParas(), AllOnlineApp.sAccount,
                AllOnlineApp.sToken.access_token, 0, mImei == null ? "" : mImei)
            .compose(RxUtils.toMain())
            .compose(RxUtils.businessTransformer())
            .subscribeWith(new BaseSubscriber<RespAlarmCategoryList>() {
                @Override
                public void onNext(RespAlarmCategoryList respAlarmCategoryList) {
                    hideLoading();
                    mDataList = respAlarmCategoryList.getData();
                    fillData();
                }

                @Override
                public void onHttpError(ExceptionHandle.ResponeThrowable e) {
                    hideLoading();
                    showToast(e.getErrMessage());
                }
            });
        subscribeRx(d);
    }

    private void getBindWx() {
        String token = GlobalParam.getInstance().getAccessToken();
        String unionId = "";
        if (AllOnlineApp.sToken != null && AllOnlineApp.sToken.loginType == LoginActivity.LOGIN_WX) {
            unionId = AllOnlineApp.sToken.account;
        }
        Disposable d = DataEngine.getAllMainApi().getBindWx(token, unionId, GlobalParam.getInstance().getCommonParas())
            .compose(RxUtils.businessTransformer())
            .compose(RxUtils.toMain())
            .subscribeWith(new BaseSubscriber<RespBindedWx>() {
                @Override
                public void onHttpError(ExceptionHandle.ResponeThrowable e) {
                    if (e.getErrCode() == ExceptionHandle.BusinessCode.NO_BIND_INFO
                        || e.getErrCode() == ExceptionHandle.BusinessCode.WECHAT_NOT_SUBSCRIBE) {
                        followWechat.setVisibility(View.VISIBLE);
                    }
                }

                @Override
                public void onNext(RespBindedWx resp) {
                    if (resp != null && (!TextUtils.isEmpty(resp.getData().getLogin_name())
                        || !TextUtils.isEmpty(resp.getData().getImei()))) {
                        followWechat.setVisibility(View.GONE);
                    }
                }
            });
        subscribeRx(d);
    }

    protected void fillData() {
        ItemDragHelperCallback callback = new ItemDragHelperCallback() {
            @Override
            public boolean isLongPressDragEnabled() {
                return true;
            }
        };
        ItemTouchHelper helper = new ItemTouchHelper(callback);
        helper.attachToRecyclerView(mRecyclerView);
        fixDataset();

        if (mDataList == null || mDataList.size() <= 0) {
            mEmptyLayout.setVisibility(View.VISIBLE);
            mRecyclerView.setVisibility(View.GONE);
        } else {
            mEmptyLayout.setVisibility(View.GONE);
            mRecyclerView.setVisibility(View.VISIBLE);
            mAdapter = new AlarmCategoryListAdapter(this, mImei, mDataList);
            mRecyclerView.setAdapter(mAdapter);
        }
    }

    private void registerAlarmReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(GPNSInterface.ACTION_MESSAGE_RECEIVED);
        registerReceiver(mAlarmReceiver, filter);
    }

    private void unregisterAlarmReceiver() {
        unregisterReceiver(mAlarmReceiver);
    }

    private BroadcastReceiver mAlarmReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent == null) {
                return;
            }
            String action = intent.getAction();
            if (StringUtil.isTrimEmpty(action)) {
                return;
            }

            if (TextUtils.equals(action, GPNSInterface.ACTION_MESSAGE_RECEIVED)) {
                getAlarmCategoryList();
            }
        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ExtraConstants.REQ_CODE_ALARM_ITEM_LIST) {
            if (resultCode != RESULT_OK || data == null) {
                return;
            }

            int position = data.getIntExtra(ExtraConstants.EXTRA_ALARM_CATEGORY_POSITION, -1);
            int size = (mDataList == null ? 0 : mDataList.size());
            if (position <= -1 || position >= size) {
                return;
            }
            int delCount = data.getIntExtra(ExtraConstants.EXTRA_ALARM_DELETION_COUNT, 0);
            if (delCount <= 0) {
                return;
            }
            AlarmCategoryItem item = mDataList.get(position);
            if (item == null) {
                return;
            }
            if (item.isAlias()) {
                item.setAlarm_num_alias(item.getAlarm_num_alias() - delCount);
            } else {
                item.setAlarm_num(item.getAlarm_num() - delCount);
            }
            mAdapter.saveReadAlarmCount(item);
            mAdapter.notifyItemChanged(position);
        }
    }

    protected void onOptionClick() {
        startActivity(new Intent(this, AlarmSettingActivity.class));
    }

    private void fixRecyclerViewMargin() {
        int lastVisiblePosition = ((LinearLayoutManager) mRecyclerView.getLayoutManager()).findLastCompletelyVisibleItemPosition();
        int adapaterCount = mRecyclerView.getAdapter().getItemCount();
        if (lastVisiblePosition + 1 == adapaterCount) {
            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) mRecyclerView.getLayoutParams();
            params.bottomMargin = mRecyclerViewBottomMargin;
            mRecyclerView.setLayoutParams(params);
        } else {
            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) mRecyclerView.getLayoutParams();
            params.bottomMargin = 0;
            mRecyclerView.setLayoutParams(params);
        }
    }

    private void fixRecyclerViewMarginWithDragging() {
        if (mRecyclerView == null || mRecyclerView.getAdapter() == null) {
            return;
        }
        int lastVisiblePosition = ((LinearLayoutManager) mRecyclerView.getLayoutManager()).findLastCompletelyVisibleItemPosition();
        int adapaterCount = mRecyclerView.getAdapter().getItemCount();
        if (lastVisiblePosition + 1 == adapaterCount) {
            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) mRecyclerView.getLayoutParams();
            if (params.bottomMargin == 0) {
                params.bottomMargin = mRecyclerViewBottomMargin;
                mRecyclerView.setLayoutParams(params);
            }
        }
    }

    private void fixDataset() {
        List<AlarmCategoryItem> copy = new ArrayList<AlarmCategoryItem>();
        AlarmCategoryItem other = new AlarmCategoryItem();
        other.setAlias(true);
        other.setAlarm_type_alias(getString(R.string.alarm_type_other_title));
        other.setAlarm_num_alias(0);
        other.setSend_time(0L);
        other.setAlarm_type_id(-1);
        boolean isOtherExist = false;
        for (AlarmCategoryItem item : mDataList) {
            if (AlarmCategoryUtils.isNormalCategory(item.getAlarm_type_id())) {
                copy.add(item);
                continue;
            }
            isOtherExist = true;
            //add
            other.setUser_name(item.getUser_name());
            other.setAlarm_num_alias(other.getAlarm_num_alias() + item.getAlarm_num());
            if (item.getSend_time() > other.getSend_time()) {
                other.setSend_time(item.getSend_time());
            }
        }

        if (isOtherExist) {
            copy.add(other);
            mDataList = copy;
        }
    }
}
