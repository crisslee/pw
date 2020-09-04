package com.coomix.app.redpacket.activity;

import android.os.Bundle;
import android.widget.Toast;
import com.coomix.app.all.R;
import com.coomix.app.all.service.ServiceAdapter;
import com.coomix.app.all.service.ServiceAdapter.ServiceAdapterCallback;
import com.coomix.app.all.ui.base.BaseActivity;
import com.coomix.app.all.util.ViewUtil;
import com.coomix.app.all.widget.MyActionbar;
import com.coomix.app.framework.app.Result;
import com.coomix.app.framework.util.CommonUtil;
import com.coomix.app.redpacket.util.RedPacketConstant;
import com.coomix.app.redpacket.util.RedPacketDetailAdapter;
import com.coomix.app.redpacket.util.RedPacketInfo;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;

/**
 * Created by ssl on 2017/2/13.
 */
public class DetailRedPacketActivity extends BaseActivity implements ServiceAdapterCallback {
    public static final String EXTRA_REDPACKET_INFO = "extra_redpacket_info";
    private PullToRefreshListView mPtrListView;
    private RedPacketInfo redPacketInfo = null;
    //private ServerRequestController mServiceAdapter = null;
    private ServiceAdapter serviceAdapter;
    private RedPacketDetailAdapter redPacketDetailAdapter = null;
    private int iRequestMoreData = -1;

    private int firstAllocNum = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_detail_redpacket);

        if (getIntent() != null) {
            if (getIntent().hasExtra(EXTRA_REDPACKET_INFO)) {
                redPacketInfo = (RedPacketInfo) getIntent().getSerializableExtra(EXTRA_REDPACKET_INFO);
            }
        }

        initViews();

        serviceAdapter = ServiceAdapter.getInstance(this);
        serviceAdapter.registerServiceCallBack(this);
    }

    private void initViews() {
        MyActionbar actionbar = (MyActionbar) findViewById(R.id.myActionbar);
        actionbar.initActionbar(true, R.string.redpacket_detail, 0, 0);
        actionbar.setBackgroundColor(getResources().getColor(R.color.redpacket_red));

        mPtrListView = (PullToRefreshListView) findViewById(R.id.listViewContentAlloc);

        mPtrListView.setOnRefreshListener(pullTorefreshlistener);
        if (redPacketInfo != null && redPacketInfo.getPacket_num() > RedPacketConstant.REDPACKET_USER_PER_PAGE) {
            if (redPacketInfo.getAlloc_infos().size() >= redPacketInfo.getAlloc_num()) {
                mPtrListView.setMode(PullToRefreshBase.Mode.DISABLED);
            } else {
                if (firstAllocNum == 0) {
                    firstAllocNum = redPacketInfo.getAlloc_num();
                }
                mPtrListView.setMode(PullToRefreshBase.Mode.PULL_FROM_END);
            }
        } else {
            mPtrListView.setMode(PullToRefreshBase.Mode.DISABLED);
        }

        ViewUtil.setPtrStateText(mPtrListView);

        redPacketDetailAdapter = new RedPacketDetailAdapter(this, redPacketInfo);
        mPtrListView.setAdapter(redPacketDetailAdapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mPtrListView.getRefreshableView().setHeaderDividersEnabled(false);
        mPtrListView.getRefreshableView().setFooterDividersEnabled(false);
    }

    private PullToRefreshBase.OnRefreshListener2 pullTorefreshlistener = new PullToRefreshBase.OnRefreshListener2() {
        @Override
        public void onPullDownToRefresh(PullToRefreshBase refreshView) {

        }

        @Override
        public void onPullUpToRefresh(PullToRefreshBase refreshView) {
            if (redPacketInfo == null) {
                return;
            }
            double pointer = 0;
            if (redPacketInfo.getReadpos() != null) {
                pointer = redPacketInfo.getReadpos().getPointer();
            }
            iRequestMoreData = serviceAdapter.getRedPacketInfoById(this.hashCode(), CommonUtil.getTicket(),
                redPacketInfo.getRedpacket_id(), pointer, RedPacketConstant.REDPACKET_USER_PER_PAGE, null, null,
                null, firstAllocNum);
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (serviceAdapter != null) {
            serviceAdapter.unregisterServiceCallBack(this);
        }
        //        if (mServiceAdapter != null)
        //        {
        //            mServiceAdapter.unregisterServiceCallBack(this);
        //            mServiceAdapter = null;
        //        }
        redPacketInfo = null;
        if (redPacketDetailAdapter != null) {
            redPacketDetailAdapter.stopAutoPlay();
            redPacketDetailAdapter = null;
        }
    }

    @Override
    public void finish() {
        super.finish();
    }

    @Override
    public void callback(int messageId, Result result) {
        if (result == null) {
            return;
        }
        if (result.statusCode == Result.ERROR_NETWORK) {
            Toast.makeText(this, R.string.network_error, Toast.LENGTH_SHORT).show();
        } else if (iRequestMoreData == messageId) {
            mPtrListView.onRefreshComplete();
            if (result.success && result.mResult != null && result.mResult instanceof RedPacketInfo) {
                RedPacketInfo tempRP = (RedPacketInfo) result.mResult;
                parseMoreData(tempRP);
            } else {
                Toast.makeText(DetailRedPacketActivity.this, R.string.leave_group_error, Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void parseMoreData(RedPacketInfo tempRP) {
        if (tempRP != null) {
            if (redPacketInfo != null) {
                redPacketInfo.setReadpos(tempRP.getReadpos());
                if (tempRP.getAlloc_infos() != null && tempRP.getAlloc_infos().size() > 0) {
                    redPacketInfo.getAlloc_infos().addAll(tempRP.getAlloc_infos());
                } else {
                    //没有更多内容
                    Toast.makeText(DetailRedPacketActivity.this, R.string.no_more_data, Toast.LENGTH_SHORT).show();
                    mPtrListView.setMode(PullToRefreshBase.Mode.DISABLED);
                }
            } else {
                redPacketInfo = tempRP;
            }
            if (redPacketInfo.getAlloc_num() <= RedPacketConstant.REDPACKET_USER_PER_PAGE) {
                mPtrListView.setMode(PullToRefreshBase.Mode.DISABLED);
            }
            redPacketDetailAdapter.notifyDataSetChanged();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (redPacketDetailAdapter != null) {
            redPacketDetailAdapter.startAutoPlay();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (redPacketDetailAdapter != null) {
            redPacketDetailAdapter.stopAutoPlay();
        }
    }
}
