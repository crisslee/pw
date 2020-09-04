package com.coomix.app.all.ui.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import com.coomix.app.all.Constant;
import com.coomix.app.all.GlobalParam;
import com.coomix.app.all.R;
import com.coomix.app.all.model.bean.ActCategory;
import com.coomix.app.all.model.bean.CommunityAct;
import com.coomix.app.all.model.bean.CommunityActs;
import com.coomix.app.all.model.bean.CommunityReadpos;
import com.coomix.app.all.model.bean.CommunityShare;
import com.coomix.app.all.share.UmengShareUtils;
import com.coomix.app.all.widget.HScrollAndDropDownView;
import com.coomix.app.all.widget.MyActionbar;
import com.coomix.app.framework.util.CommonUtil;
import com.coomix.app.framework.util.PreferenceUtil;
import com.coomix.app.all.data.BaseSubscriber;
import com.coomix.app.all.data.DataEngine;
import com.coomix.app.all.data.ExceptionHandle;
import com.coomix.app.all.data.RxUtils;
import com.coomix.app.all.model.response.RespActCategory;
import com.coomix.app.all.model.response.RespActList;
import com.coomix.app.all.ui.base.BaseActivity;
import com.coomix.app.all.ui.login.LoginActivity;
import com.coomix.app.all.util.BitmapUtils;
import com.coomix.app.all.util.CommunityImageDownloader;
import com.coomix.app.all.util.FooterViewUtils;
import com.coomix.app.all.util.PullToRefreshUtils;
import com.coomix.app.all.manager.SettingDataManager;
import com.coomix.app.all.util.ViewUtil;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.Mode;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener2;
import com.handmark.pulltorefresh.library.PullToRefreshListView;

import java.util.ArrayList;

import io.reactivex.disposables.Disposable;

/**
 * @title CommActListActivity.java
 * @Description 活动 的列表界面
 * @Author shishenglong
 * @CreateTime 2016年8月16日 上午9:58:25
 * @ModifyBy
 * @ModifyTime
 * @ModifyRemark
 */
public class CommActListActivity extends BaseActivity implements GestureDetector.OnGestureListener
{

    public static final String            CATEGORY_ID          = "cat_id";

    private PullToRefreshListView mPtrListView = null;
    private CommActListAdapter eventAdapter = null;
    private ImageView imageViewShare;
    private final int REFRESH = 0, MORE = 1;
    private int curListType = REFRESH;
    private CommunityActs commActListInfo;
    private ArrayList<CommunityAct> listActs = new ArrayList<CommunityAct>();
    private int iRequestId = -1;
    private double last_pointer = 0;
    private String last_id = "0";
    private View noMoreFooterView;
    private final int MAX_PUR_PAGE = 15;
    private final int REQ_ACT_IN_PROGRESS = 1;
    private final int REQ_ACT_FINISHED = 2;
    private int iActRequestType = REQ_ACT_IN_PROGRESS;// 1:未结束； 2：已结束活动

    private HScrollAndDropDownView hsddView;
    private ActCategory curActCategory = null;
    private int iCategoryId = -1;
    private GestureDetector gestureDetector = new GestureDetector(this);
    private long lastTime = 0;


    private int selectPosition = 0;
    private boolean isFromBoot = false;

    private CommunityImageDownloader myImageDownloader = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_comm_act_main);

        initViews();

        myImageDownloader = new CommunityImageDownloader();

        if (getIntent() != null) {
            iCategoryId = getIntent().getIntExtra(CATEGORY_ID, -1);
            isFromBoot = getIntent().getBooleanExtra(Constant.IS_STARTED_FROM_BOOT, false);
        }
        // requestNewData();
        getActivityCategoryList();
    }

    private void getActivityCategoryList() {
        Disposable disposable = DataEngine.getCommunityApi()
                .getCategorys(GlobalParam.getInstance().getCommonParas(), Constant.COMMUNITY_CITYCODE)
                .compose(RxUtils.toMain())
                .compose(RxUtils.businessTransformer())
                .subscribeWith(new BaseSubscriber<RespActCategory>(null) {

                    @Override
                    public void onNext(RespActCategory respActCategory) {
                        parseCategoryData(respActCategory);
                    }

                    @Override
                    public void onHttpError(ExceptionHandle.ResponeThrowable e) {
                        showErr(e.getErrCodeMessage());
                    }
                });

        subscribeRx(disposable);

    }


    @Override
    protected void onDestroy() {

        if (myImageDownloader != null) {
            myImageDownloader.release();
            myImageDownloader = null;
        }
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
        mPtrListView.getRefreshableView().setHeaderDividersEnabled(false);
        mPtrListView.getRefreshableView().setFooterDividersEnabled(false);
    }

    private void initViews() {
        MyActionbar actionbar = (MyActionbar) findViewById(R.id.myActionbar);
        actionbar.initActionbar(true, R.string.activity, 0, R.drawable.actionbar_share);
        actionbar.setRightImageClickListener(view -> shareActList());

        mPtrListView = (PullToRefreshListView) findViewById(R.id.actListListView);
        handleListView();
        eventAdapter = new CommActListAdapter(getBaseContext(), listActs);
        mPtrListView.setAdapter(eventAdapter);
        footerViewUtils = new FooterViewUtils(this, mPtrListView.getRefreshableView());

        hsddView = (HScrollAndDropDownView) findViewById(R.id.hscrollAndDropDownLayout);
        hsddView.bringToFront();
        hsddView.setMyItemClickListener(onMyItemClickListener);

        mPtrListView.getRefreshableView().setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                //return false;
                return gestureDetector.onTouchEvent(event);
            }
        });

    }

    private void handleListView() {
        mPtrListView.setMode(Mode.BOTH);
        ViewUtil.setPtrStateText(mPtrListView);
        mPtrListView.setOnRefreshListener(new OnRefreshListener2<ListView>() {

            @Override
            public void onPullDownToRefresh(PullToRefreshBase<ListView> refreshView) {
                requestNewData();
            }

            @Override
            public void onPullUpToRefresh(PullToRefreshBase<ListView> refreshView) {
                requestMoreData();
            }
        });

        mPtrListView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // TODO Auto-generated method stub
                position -= 1;
                if (position >= 0 && position < listActs.size() && listActs.get(position) != null) {
                    Intent intent = new Intent(CommActListActivity.this, CommActDetailActivity.class);
                    intent.putExtra(Constant.NATIVE_ACTIVITY_ID, listActs.get(position).getId());
                    startActivity(intent);
                }
            }
        });
    }

    private void requestNewData() {
        curListType = REFRESH;
        resetParams();
        requestData(iActRequestType, MAX_PUR_PAGE);
    }

    private void requestMoreData() {
        curListType = MORE;
        requestData(iActRequestType, MAX_PUR_PAGE);
    }

    private View getNoMoreDataFootView() {
        if (noMoreFooterView == null) {
            noMoreFooterView = FooterViewUtils.getNoMoreDataFooterView(this);
        }
        return noMoreFooterView;
    }

    private void removeNoMoreDataFootView() {
        if (noMoreFooterView != null) {
            mPtrListView.getRefreshableView().removeFooterView(noMoreFooterView);
            noMoreFooterView = null;
        }
    }

    private void shareActList() {
        boolean shareSuccess = false;
        if (commActListInfo != null) {
            final CommunityShare share = commActListInfo.getShare();
            if (share != null) {
                Bitmap shareBitmap = myImageDownloader.getDownloadedBitmap();
                if (shareBitmap == null) {
                    shareBitmap = BitmapUtils.takeScreenShot(CommActListActivity.this);
                }
                shareSuccess = UmengShareUtils.toShare(CommActListActivity.this, share, shareBitmap);
            } else {
                Toast.makeText(this, R.string.share_failed, Toast.LENGTH_SHORT).show();
            }
        }
        if (!shareSuccess) {
            // 分享失败
            Toast.makeText(this, R.string.share_failed, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        /*if (mController != null && mController.getConfig() != null)
        {
			*//** 使用SSO授权必须添加如下代码 *//*
            UMSsoHandler ssoHandler = mController.getConfig().getSsoHandler(requestCode);
			if (ssoHandler != null)
			{
				ssoHandler.authorizeCallBack(requestCode, resultCode, data);
			}
		}*/
        super.onActivityResult(requestCode, resultCode, data);
    }


    protected FooterViewUtils footerViewUtils;

    private void showError(String msg) {
        dismissProgressDialog();
        mPtrListView.onRefreshComplete();
        if (eventAdapter == null || eventAdapter.getCount() == 0) {
            // 列表，无内容，且网络访问出错时
            footerViewUtils.showEmptyView(R.drawable.icon_hint_no_net, R.string.hint_no_net, 10000);
        } else {
            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
        }
    }
    

    private void parseCategoryData(RespActCategory actCategories) {
        if (actCategories != null) {
            ArrayList<ActCategory> listData = actCategories.getData().getCategories();

            if (listData != null && listData.size() > 0) {
                int select = -1;
                for (int i = 0; i < listData.size(); i++) {
                    if (listData.get(i) != null && listData.get(i).getId() == iCategoryId) {
                        select = i;
                        curActCategory = listData.get(i);
                        requestNewData();
                        break;
                    }
                }

                if (select < 0) {
                    //没有指定的分类信息，则获取第一个分类
                    select = 0;
                    curActCategory = listData.get(0);
                    requestNewData();
                }
                hsddView.setCategoryData(listData, select);
            } else {
                handleActsCountChanged();
            }
//			else
//			{
//				requestNewData();
//			}
        }
    }

    private void parseData(CommunityActs actList) {
        dismissProgressDialog();
        commActListInfo = actList;
        if (actList == null || !(actList instanceof CommunityActs))
            return;
        CommunityReadpos readPos = actList.getReadpos();
        if (readPos != null) {
            last_pointer = readPos.getPointer();
            last_id = readPos.getId();
        }
        if (actList.getShare() != null) {
            imageViewShare.setVisibility(View.VISIBLE);
        } else {
            imageViewShare.setVisibility(View.GONE);
        }
        if (actList.getShare() != null && myImageDownloader.getDownloadedBitmap() == null) {
            myImageDownloader.downloadSharePicture(this, actList.getShare());
        }

        ArrayList<CommunityAct> newActList = actList.getActivityList();
        if (newActList == null) {
            newActList = new ArrayList<CommunityAct>();
        }

        if (curListType == REFRESH) {
            this.listActs.clear();
            this.listActs.addAll(newActList);
            setViewsValue(0);
            removeNoMoreDataFootView();
            handleActsCountChanged();
            mPtrListView.onRefreshComplete();
            if (mPtrListView.getRefreshableView() != null) {
                mPtrListView.getRefreshableView().setSelection(0);
            }
        } else if (curListType == MORE) {
            this.listActs.addAll(newActList);
            boolean bAddFootView = false;
            if (newActList.size() <= 0) {
                bAddFootView = true;
            }
            mPtrListView.onRefreshComplete();
            if (bAddFootView) {
                PullToRefreshUtils.disablePullUp(mPtrListView);
                mPtrListView.getRefreshableView().addFooterView(getNoMoreDataFootView());
            } else if (newActList.size() > 0) {
                setViewsValue(selectPosition);
            }
        }

    }

    public void handleActsCountChanged() {
        try {
            if (listActs.size() <= 0) {
                footerViewUtils.showEmptyView(R.drawable.hint_noconetent, R.string.hint_no_content);
            } else {
                footerViewUtils.dismiss();
                if (noMoreFooterView == null) {
                    mPtrListView.setMode(Mode.BOTH);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            // LogUtil.e(":" + e.getMessage());
        }
    }


    public void resetParams() {
        curListType = REFRESH;
        iActRequestType = REQ_ACT_IN_PROGRESS;// 进行中的活动
        last_pointer = 0;
        last_id = "0";
    }

    public void requestData(int requestType, int MAX) {

        if (curActCategory != null) {
            int catId = curActCategory.getId();
            showLoading(getString(R.string.please_wait));
            Disposable disposable = DataEngine.getCommunityApi()
                    .getActivityList(
                            GlobalParam.getInstance().getCommonParas(),
                            REQ_ACT_IN_PROGRESS,
                            Constant.COMMUNITY_CITYCODE,
                            catId,
                            last_pointer,
                            last_id,
                            MAX_PUR_PAGE)
                    .compose(RxUtils.toMain())
                    .compose(RxUtils.businessTransformer())
                    .subscribeWith(new BaseSubscriber<RespActList>(null) {

                        @Override
                        public void onNext(RespActList respActList) {
                            hideLoading();
                            parseData(respActList.getData());
                        }

                        @Override
                        public void onHttpError(ExceptionHandle.ResponeThrowable e) {
                            hideLoading();
                            showError(e.getErrCodeMessage());
                        }
                    });

            subscribeRx(disposable);
        }

    }

    private void setViewsValue(int position) {
        if (eventAdapter == null) {
            eventAdapter = new CommActListAdapter(getBaseContext(), listActs);
            mPtrListView.setAdapter(eventAdapter);
        } else {
            eventAdapter.notifyDataSetChanged();
            // mPtrListView.getRefreshableView().setSelection(position);
            selectPosition = 0;
        }
    }


    @Override
    public void finish() {
        boolean isFromBoot = PreferenceUtil.getBoolean(Constant.IS_STARTED_FROM_BOOT, false);
        if (isFromBoot) {
            if (!CommonUtil.isLogin()) {
                //去登陆
                startActivity(new Intent(this, LoginActivity.class));
            }else{
                SettingDataManager.getInstance(this).goToMainByMap(this, null);
            }
        }
        super.finish();
    }

    private HScrollAndDropDownView.OnMyItemClickListener onMyItemClickListener = new HScrollAndDropDownView.OnMyItemClickListener() {
        @Override
        public void onClick(ActCategory actCategory) {
            if (actCategory != null) {
                //请求对应的分类的活动列表
//				if(!TextUtils.isEmpty(actCategory.getTitle()))
//				{
//					textTitle.setText(actCategory.getTitle());
//				}
//				else
//				{
//					textTitle.setText(R.string.activity);
//				}
                showProgressDialog(getString(R.string.loading));

                curActCategory = actCategory;
                requestNewData();
            }
        }
    };

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && hsddView.dismissGridView()) {
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onDown(MotionEvent e) {
        return false;
    }

    @Override
    public void onShowPress(MotionEvent e) {

    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        return false;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        return false;
    }

    @Override
    public void onLongPress(MotionEvent e) {

    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        //左滑动
        if (e1 != null && e2 != null && (e1.getX() - e2.getX() > 400)) {
            if (System.currentTimeMillis() - lastTime > 500) {
                lastTime = System.currentTimeMillis();
                hsddView.changeItem(true);
            }
            return true;
        }
        //右滑动
        else if (e1 != null && e2 != null && (e1.getX() - e2.getX() < -400)) {
            if (System.currentTimeMillis() - lastTime > 500) {
                lastTime = System.currentTimeMillis();
                hsddView.changeItem(false);
            }
            return true;
        }
        return false;
    }
}
