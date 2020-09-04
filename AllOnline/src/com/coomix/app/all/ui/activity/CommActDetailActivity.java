package com.coomix.app.all.ui.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.AbsoluteSizeSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.coomix.app.all.AllOnlineApp;
import com.coomix.app.all.Constant;
import com.coomix.app.all.GlobalParam;
import com.coomix.app.all.R;
import com.coomix.app.all.model.bean.ActPrice;
import com.coomix.app.all.model.bean.CommunityAct;
import com.coomix.app.all.model.bean.CommunityActDetail;
import com.coomix.app.all.model.bean.CommunityShare;
import com.coomix.app.all.share.UmengShareUtils;
import com.coomix.app.all.widget.MyActionbar;
import com.coomix.app.framework.util.CommonUtil;
import com.coomix.app.framework.util.PreferenceUtil;
import com.coomix.app.all.data.BaseSubscriber;
import com.coomix.app.all.data.DataEngine;
import com.coomix.app.all.data.ExceptionHandle;
import com.coomix.app.all.data.RxUtils;
import com.coomix.app.all.model.response.RespActDetail;
import com.coomix.app.all.model.response.RespRegisterAct;
import com.coomix.app.all.ui.base.BaseActivity;
import com.coomix.app.all.ui.login.LoginActivity;
import com.coomix.app.all.util.BitmapUtils;
import com.coomix.app.all.util.CommunityImageDownloader;
import com.coomix.app.all.util.CommunityUtil;
import com.coomix.app.all.util.FooterViewUtils;
import com.coomix.app.all.manager.SettingDataManager;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

import io.reactivex.disposables.Disposable;

/**
 * @title CommActDetailActivity.java
 * @Description
 * @Author shishenglong
 * @CreateTime 2016年8月18日 上午8:46:42
 * @ModifyBy
 * @ModifyTime
 * @ModifyRemark
 */
public class CommActDetailActivity extends BaseActivity implements OnClickListener {
    private ListView mPtrListView = null;
    private FooterViewUtils footerViewUtils;
    private TextView textSignStatus = null;
    private TextView textCost = null;
    private int iActID = 0;
    private CommActDetailAdapter actDetailAdapter = null; // 活动详情部分的adapter
    private CommunityActDetail actDetailInfo = null;
    private CommunityImageDownloader myImageDownloader = null;
    private View rootView = null;
    private final int ACTIVITY_ACT_REGISTER_REQUEST_CODE = 10;
    public static boolean bReRequestNewData = false;
    private LinearLayout layoutBottomNormal,layoutBottomPre;
    private TextView textRmb, textPeopleCount, textDeadline, textDay, textHour, textMin, textSec, textCommit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getIntent() != null) {
            iActID = getIntent().getIntExtra(Constant.NATIVE_ACTIVITY_ID, 0);
        }

        rootView = LayoutInflater.from(this).inflate(R.layout.activity_comm_act_detail, null);
        setContentView(rootView);

        initViews();

        myImageDownloader = new CommunityImageDownloader();

        requestNetData();
    }

    @Override
    protected void onDestroy() {
        try {
            if (actDetailAdapter != null) {
                actDetailAdapter.destyoy();
            }
            if (myImageDownloader != null) {
                myImageDownloader.release();
                myImageDownloader = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        bReRequestNewData = false;
        if(mHandler != null){
            mHandler.removeCallbacksAndMessages(null);
        }
        super.onDestroy();
    }

    @Override
    public void finish() {
        //跳转到
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

    @Override
    protected void onResume() {
        super.onResume();
        mPtrListView.setHeaderDividersEnabled(false);
        mPtrListView.setFooterDividersEnabled(false);

        if (bReRequestNewData) {
            requestNetData();
            bReRequestNewData = false;
        }
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        refreshViewCOntent();
    }

    private void refreshViewCOntent() {
        String manafacturer = Build.MANUFACTURER;
        /**
         * 目前测试在Vivo手机上发现:进入活动详情不滚动UI，然后进入别的activity后再返回，
         * 该界面用WebView显示的文字会出现模糊的现象.
         * 可能是Vivo手机Rom处理了android浏览器内核导致（其他手机暂时未发现此问题），所以再显示的时候新建adapter刷新
         */
        if (!TextUtils.isEmpty(manafacturer) && manafacturer.toLowerCase().contains("vivo")) {
            if (actDetailAdapter != null && actDetailInfo != null) {
                actDetailAdapter = new CommActDetailAdapter(this, actDetailInfo, rootView);
                mPtrListView.setAdapter(actDetailAdapter);
            }
        }
    }

    private void initViews() {
        MyActionbar actionbar = (MyActionbar) findViewById(R.id.myActionbar);
        actionbar.initActionbar(true, R.string.activity_detail, 0, R.drawable.actionbar_share);
        actionbar.setRightImageClickListener(view -> shareCommActDetail());

        mPtrListView = (ListView) findViewById(R.id.listInnerscrollview);
        textSignStatus = (TextView) findViewById(R.id.textViewSign);
        textSignStatus.setOnClickListener(this);
        textCost = (TextView) findViewById(R.id.textViewCostAll);

        // mPtrListView.setMode(Mode.DISABLED);
        // ViewUtil.setPtrStateText(mPtrListView);
        actDetailAdapter = new CommActDetailAdapter(this, null, rootView);
        mPtrListView.setAdapter(actDetailAdapter);
        footerViewUtils = new FooterViewUtils(this, mPtrListView);

        //预报名相关
        layoutBottomNormal = (LinearLayout)findViewById(R.id.layoutBottomNormalSign);
        layoutBottomPre = (LinearLayout)findViewById(R.id.layoutBottomPreSign);
        textRmb = (TextView)findViewById(R.id.textViewRMB);
        textPeopleCount = (TextView)findViewById(R.id.textViewPeopleCount);
        textDeadline = (TextView)findViewById(R.id.textViewDeadline);
        textDay = (TextView)findViewById(R.id.textViewTimeDay);
        textHour = (TextView)findViewById(R.id.textViewTimeHour);
        textMin = (TextView)findViewById(R.id.textViewTimeMin);
        textSec = (TextView)findViewById(R.id.textViewTimeSec);
        textCommit = (TextView)findViewById(R.id.textViewPreOrSign);
        textCommit.setOnClickListener(view -> signOrGet());
    }

    //预定或立即抢
    private void signOrGet()
    {
        if (actDetailInfo != null)
        {
            //没预约还没开抢，去预约,填写报名信息一致...没预约开抢了，直接去报名
            Intent intent = new Intent(this, ActRegister2Activity.class);
            intent.putExtra(ActRegister2Activity.SIGN_TYPE, ActRegister2Activity.REG_SIGN_PRE);
            intent.putExtra(ActRegisterActivity.ACT_DETAIL_INFO, actDetailInfo);
            startActivity(intent);
        }
    }

    private void sendPreapplyNow(){
        showLoading(getString(R.string.please_wait));
        Disposable disposable= DataEngine.getCommunityApi()
                .registerAct(
                        GlobalParam.getInstance().getCommonParas(),
                        ActRegister2Activity.REG_SIGN,
                        actDetailInfo.getId(),
                        -1,
                        new HashMap<String, String>())
                .compose(RxUtils.toMain())
                .compose(RxUtils.businessTransformer())
                .subscribeWith(new BaseSubscriber<RespRegisterAct>(CommActDetailActivity.this) {
                    @Override
                    public void onNext(RespRegisterAct response) {
                        hideLoading();
                        showToast(getString(R.string.register_success));
                        requestNetData();
                        if (response != null && response.getData() != null && response.getData().getOrder_id() > 0) {
                            goToOrderActivity(response.getData().getOrder_id());
                        }
                    }

                    @Override
                    public void onHttpError(ExceptionHandle.ResponeThrowable e) {
                        hideLoading();
                        showErr(e.getErrCodeMessage());
                    }
                });
        subscribeRx(disposable);
    }

    private void goToOrderActivity(long iOrderId) {
        if (actDetailInfo != null) {
            ActPrice actPrice = actDetailInfo.getPrice();
            if (actPrice != null) {
                if (actPrice.getType() == ActPrice.PRICE_FASTEN) {
                    //固定价格
                    if (actPrice.getFixed_price() > 0) {
                        //去支付
                        Intent intent = new Intent(this, MyOrderInfoActivity.class);
                        intent.putExtra(Constant.NATIVE_ACTIVITY_ID, actDetailInfo.getId());
                        intent.putExtra(MyOrderInfoActivity.ORDER_DETAIL_OR_PAYING, MyOrderInfoActivity.ORDER_PAYING);
                        intent.putExtra(MyOrderInfoActivity.ORDER_ID, iOrderId);
                        intent.putExtra(MyOrderInfoActivity.ORDER_FROM_SIGNED, true);
                        startActivity(intent);
                        return;
                    }
                }
            }

            //线下支付或免费，不需要支付，直接去报了名的活动详情
            Intent intent = new Intent(this, MyOrderInfoActivity.class);
            intent.putExtra(Constant.NATIVE_ACTIVITY_ID, actDetailInfo.getId());
            intent.putExtra(MyOrderInfoActivity.ORDER_DETAIL_OR_PAYING, MyOrderInfoActivity.ORDER_DETAIL);
            intent.putExtra(MyOrderInfoActivity.ORDER_ID, iOrderId);
            startActivity(intent);
        }
    }

    private void requestNetData() {
        showLoading(getString(R.string.please_wait));
        Disposable disposable = DataEngine.getCommunityApi().getActivityDetail(
                GlobalParam.getInstance().getCommonParas(),
                AllOnlineApp.sToken.ticket, iActID)
                .compose(RxUtils.toMain())
                .compose(RxUtils.businessTransformer())
                .subscribeWith(new BaseSubscriber<RespActDetail>() {
                    @Override
                    public void onHttpError(ExceptionHandle.ResponeThrowable e) {
                        showErr(e.getErrCodeMessage());
                        hideLoading();
                        footerViewUtils.showEmptyView(R.drawable.hint_noconetent, R.string.hint_no_content, onClickListener, 10000);
                    }

                    @Override
                    public void onNext(RespActDetail respActDetail) {
                        hideLoading();
                        if (footerViewUtils != null && (10000 == footerViewUtils.getTag() || actDetailInfo == null)) {
                            footerViewUtils.dismiss();
                        }
                        if (respActDetail.getData() != null) {
                            parseData(respActDetail.getData());
                        }
                    }
                });
        subscribeRx(disposable);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.textViewSign:
                // 报名
                if (!CommonUtil.isLogin()) {
                    CommonUtil.goToLogin(CommActDetailActivity.this);
                    return;
                }
                if (actDetailInfo != null) {
                    if (actDetailInfo.getSigned() == 1) {
                        //已经报名就跳转到我的活动
                        bReRequestNewData = true;
                        Intent intent = new Intent(CommActDetailActivity.this, MyActivityActivity.class);
                        startActivity(intent);
                    }
                    else if(actDetailInfo.getPreapply() == 1){
                        //已经预报名
                        sendPreapplyNow();
                    }else {
                        if (actDetailInfo.getStatus() == CommunityAct.ACT_SIGNING) {
                            //去报名
                            Intent intent = null;
                            //if(actDetailInfo.getCommit_items() != null && actDetailInfo.getCommit_items().size() > 0)
                            if (actDetailInfo.getDisplay() != null && actDetailInfo.getDisplay().size() > 0) {
                                //去拓展信息的报名
//								intent = new Intent(CommActDetailActivity.this, ActRegisterExtendActivity.class);
                                intent = new Intent(CommActDetailActivity.this, ActRegister2Activity.class);
                            } else {
                                //输入固定信息的报名
                                intent = new Intent(CommActDetailActivity.this, ActRegisterActivity.class);
                            }
                            intent.putExtra(ActRegisterActivity.ACT_DETAIL_INFO, actDetailInfo);
                            startActivity(intent);
                        }
                    }
                }
                break;

            default:
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ACTIVITY_ACT_REGISTER_REQUEST_CODE) {
            requestNetData();
        }
    }

    private void parseData(CommunityActDetail actDetailInfo) {
        this.actDetailInfo = actDetailInfo;
        if (actDetailInfo == null) {
            footerViewUtils.showEmptyView(R.drawable.hint_noconetent, R.string.hint_no_content, onClickListener);
            return;
        } else {
            footerViewUtils.dismiss();
        }

        if (actDetailInfo.getShare() != null && myImageDownloader.getDownloadedBitmap() == null) {
            myImageDownloader.downloadSharePicture(this, actDetailInfo.getShare());
        }

        refreshAdapter(actDetailInfo);

        int strId = R.string.register_error;
        int bgColorId = R.color.register_gray;
        int signed = actDetailInfo.getSigned();// 0：未报名；1：已经报名
        int status = actDetailInfo.getStatus();
        boolean bSet = true;
        if (status == CommunityAct.ACT_FINISHED || actDetailInfo.getStatus() == CommunityAct.ACT_CANCELED) {
            strId = R.string.register_finished;
        } else if (signed == 1) {
            strId = R.string.registered_edit;
            bgColorId = R.color.register_blue;
        } else {
            if (status == CommunityAct.ACT_FULL) {
                strId = R.string.register_full;
            } else if (status == CommunityAct.ACT_STOPPED) {
                strId = R.string.register_stop;
            } else if (status == CommunityAct.ACT_SIGNING) {
                strId = R.string.register_now;
                bgColorId = R.color.register_blue;
            } else if (status == CommunityAct.ACT_WAITING) {
                //strId = R.string.register_wait;
                //bgColorId = R.color.register_blue;
                if (actDetailInfo.getOpentime() - System.currentTimeMillis() / 1000 > 0) {
                    //预报名
                    bSet = false;
                    if(actDetailInfo.getPreapply() == 1){
                        //已经预约
                        textCommit.setVisibility(View.GONE);
                    }else{
                        textCommit.setVisibility(View.VISIBLE);
                    }
                    layoutBottomNormal.setVisibility(View.GONE);
                    layoutBottomPre.setVisibility(View.VISIBLE);
                    textPeopleCount.setText(getString(R.string.pre_sign_people, actDetailInfo.getPreapplynum()));
                    textDeadline.setText(getOpenTime(actDetailInfo.getOpentime()));
                    mHandler.sendEmptyMessage(0);
                }else{
                    strId = R.string.register_now;
                    bgColorId = R.color.register_blue;
                    actDetailInfo.setStatus( CommunityAct.ACT_SIGNING);
                }
            }
        }

        if(bSet) {
            layoutBottomNormal.setVisibility(View.VISIBLE);
            layoutBottomPre.setVisibility(View.GONE);
            textSignStatus.setBackgroundColor(getResources().getColor(bgColorId));
            textSignStatus.setPadding(15, 5, 15, 5);
            textSignStatus.setText(strId);
        }

        ActPrice actPrice = actDetailInfo.getPrice();
        if (actPrice != null) {
            if (actPrice.getType() == ActPrice.PRICE_FASTEN) {
                int price = actPrice.getFixed_price();
                if (price > 0) {
                    String data = CommunityUtil.getMoneyStrByIntDecimal(this, price, 2);
                    SpannableStringBuilder spsbStyl = new SpannableStringBuilder(data);
                    int endLengh = data.length();
                    if (data.contains(".")) {
                        endLengh = data.indexOf(".");
                    }
                    spsbStyl.setSpan(new AbsoluteSizeSpan(getResources().getDimensionPixelSize(R.dimen.text_num)), 1, endLengh, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    textCost.setText(spsbStyl);
                    textRmb.setText(spsbStyl);
                } else {
                    textCost.setText(R.string.free);
                    textRmb.setText(R.string.free);
                }
            } else if (actPrice.getType() == ActPrice.PRICE_PACKAGE) {
                String data = CommunityUtil.getPackagePriceMin(this, actPrice.getPrice_rule());
                SpannableStringBuilder spsbStyl = new SpannableStringBuilder(data);
                int endLengh = data.length();
                if (data.contains(".")) {
                    endLengh = data.indexOf(".");
                }
                spsbStyl.setSpan(new AbsoluteSizeSpan(getResources().getDimensionPixelSize(R.dimen.text_num)), 1, endLengh, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                textCost.setText(spsbStyl);
                textRmb.setText(spsbStyl);
            } else if (actPrice.getType() == ActPrice.PRICE_OFFLINE) {
                textCost.setText(R.string.pay_offline);
                textRmb.setText(R.string.pay_offline);
            } else if (actPrice.getType() == ActPrice.PRICE_FREE) {
                textCost.setText(R.string.free);
                textRmb.setText(R.string.free);
            } else {
                textCost.setText("");
                textRmb.setText("");
            }
        } else {
            textCost.setText("");
            textRmb.setText("");
        }
    }

    private Handler mHandler = new Handler(Looper.getMainLooper()){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case 0:
                    //活动报名结束倒计时
                    long time = actDetailInfo.getOpentime() - System.currentTimeMillis() / 1000;
                    if(time < 0){
                        //倒计时结束，刷新活动
                        requestNetData();
                        return;
                    }
                    textDay.setText(String.valueOf(getDay(time)));
                    textHour.setText(String.valueOf(getHour(time)));
                    textMin.setText(String.valueOf(getMin(time)));
                    textSec.setText(String.valueOf(getSec(time)));
                    mHandler.sendEmptyMessageDelayed(0, 1000);
                    break;

                    default:
                        break;
            }
            super.handleMessage(msg);
        }
    };

    private String getDay(long time){
        time = time / (24 *  60 * 60);
        return time > 9 ? String.valueOf(time) : "0"+String.valueOf(time);
    }

    private String getHour(long time){
        time = time % (24 *  60 * 60);
        time =  time /( 60 * 60);
        return time > 9 ? String.valueOf(time) : "0"+String.valueOf(time);
    }

    private String getMin(long time){
        time = time % (60 * 60);
        time = time / 60;
        return time > 9 ? String.valueOf(time) : "0"+String.valueOf(time);
    }

    private String getSec(long time){
        time = time % 60;
        return time > 9 ? String.valueOf(time) : "0"+String.valueOf(time);
    }

    private String getOpenTime(long time) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        int iCurYear = calendar.get(Calendar.YEAR);
        int iCurMonth = calendar.get(Calendar.MONTH);
        int iCurDay = calendar.get(Calendar.DAY_OF_MONTH);

        calendar.setTimeInMillis(time * 1000);
        if (calendar.get(Calendar.YEAR) == iCurYear && calendar.get(Calendar.MONTH) == iCurMonth && calendar.get(Calendar.DAY_OF_MONTH) == iCurDay) {
            //今天
            return getString(R.string.start_pre, getString(R.string.today) + new SimpleDateFormat("HH:mm").format(new Date(time * 1000)));
        } else {
            calendar.setTimeInMillis(System.currentTimeMillis());
            calendar.set(Calendar.DAY_OF_MONTH, calendar.get(Calendar.DAY_OF_MONTH) + 1);
            iCurYear = calendar.get(Calendar.YEAR);
            iCurMonth = calendar.get(Calendar.MONTH);
            iCurDay = calendar.get(Calendar.DAY_OF_MONTH);

            calendar.setTimeInMillis(time * 1000);
            if (calendar.get(Calendar.YEAR) == iCurYear && calendar.get(Calendar.MONTH) == iCurMonth && calendar.get(Calendar.DAY_OF_MONTH) == iCurDay) {
                //明天
                return getString(R.string.start_pre, getString(R.string.tomorrow) + new SimpleDateFormat("HH:mm").format(new Date(time * 1000)));
            } else {
                return getString(R.string.start_pre, new SimpleDateFormat("yyyy/MM/dd HH:mm").format(new Date(time * 1000)));
            }
        }
    }

    private void refreshAdapter(CommunityActDetail actDetailInfo) {
        if (actDetailAdapter == null) {
            actDetailAdapter = new CommActDetailAdapter(this, actDetailInfo, rootView);
            mPtrListView.setAdapter(actDetailAdapter);
        } else {
            actDetailAdapter.setData(actDetailInfo);
            actDetailAdapter.notifyDataSetChanged();
        }
    }

    OnClickListener onClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            requestNetData();
        }
    };

    // private View getNoMoreDataFootView() {
    // if (noMoreFooterView == null) {
    // noMoreFooterView = FooterViewUtils.getNoMoreDataFooterView(this);
    // }
    // return noMoreFooterView;
    // }

    private void shareCommActDetail() {
        boolean shareSuccess = false;
        if (actDetailInfo != null) {
            final CommunityShare share = actDetailInfo.getShare();
            if (share != null) {
                Bitmap shareBitmap = myImageDownloader.getDownloadedBitmap();
                if (shareBitmap == null || shareBitmap.isRecycled()) {
                    shareBitmap = BitmapUtils.takeScreenShot(CommActDetailActivity.this);
                }
                shareSuccess = UmengShareUtils.toShare(CommActDetailActivity.this, share, shareBitmap);
            }
        }
        if (!shareSuccess) {
            // 分享失败
            Toast.makeText(this, R.string.share_failed, Toast.LENGTH_SHORT).show();
        }
    }

}
