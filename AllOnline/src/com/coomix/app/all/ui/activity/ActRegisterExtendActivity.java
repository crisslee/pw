package com.coomix.app.all.ui.activity;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.AbsoluteSizeSpan;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.coomix.app.all.AllOnlineApp;
import com.coomix.app.all.Constant;
import com.coomix.app.all.R;
import com.coomix.app.all.model.bean.ActCommitItem;
import com.coomix.app.all.model.bean.ActPrice;
import com.coomix.app.all.model.bean.CommitExtendAll;
import com.coomix.app.all.model.bean.CommitExtendItem;
import com.coomix.app.all.model.bean.CommitPriceRuleCnt;
import com.coomix.app.all.model.bean.CommunityActDetail;
import com.coomix.app.all.model.bean.EmUser;
import com.coomix.app.all.model.bean.ImageInfo;
import com.coomix.app.all.model.bean.MyLocationInfo;
import com.coomix.app.all.model.bean.PriceRule;
import com.coomix.app.all.log.GoomeLog;
import com.coomix.app.all.service.AllOnlineApiClient;
import com.coomix.app.all.service.ServiceAdapter;
import com.coomix.app.all.service.ServiceAdapter.ServiceAdapterCallback;
import com.coomix.app.all.ui.web.DisclaimerActivity;
import com.coomix.app.all.widget.ActRegisterView;
import com.coomix.app.all.widget.MyActionbar;
import com.coomix.app.framework.app.Result;
import com.coomix.app.framework.util.CommonUtil;
import com.coomix.app.all.dialog.ProgressDialogEx;
import com.coomix.app.all.ui.base.BaseActivity;
import com.coomix.app.redpacket.util.RedPacketConstant;
import com.coomix.app.all.util.CommunityUtil;
import com.coomix.app.all.util.ConfigUtil;
import com.google.gson.Gson;

import net.goome.im.GMError;
import net.goome.im.GMValueCallBack;
import net.goome.im.chat.GMChatRoom;
import net.goome.im.chat.GMChatRoomManager;
import net.goome.im.chat.GMClient;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * @title ActRegisterExtendActivity.java
 * @Description
 * @Author shishenglong
 * @CreateTime
 * @ModifyBy
 * @ModifyTime
 * @ModifyRemark
 */
public class ActRegisterExtendActivity extends BaseActivity
        implements OnClickListener, ServiceAdapterCallback
{
    public static final int REG_CANCEL = 0;// 取消报名
    public static final int REG_SIGN = 1;// 活动报名
    public static final int REG_EDIT = 2;// 编辑报名
    public static final int ACT_REQUEST_CODE = 101;
    private TextView textCosts;
    private TextView textCommit;
    private TextView textAdd;
    private ImageView imageCheck;
    private LinearLayout layoutMain;
    //private ServerRequestController mServiceAdaper = null;
    private ServiceAdapter serviceAdapter;
    private CommunityActDetail originalDetailInfo = null;
    private HashMap<Integer, ArrayList<ImageInfo>> mapImagesList;
    private final int IMAGE_UPLOAD_FAIL = -1;
    private final int COMMIT_FAIL = -2;
    private ProgressDialogEx progressDialogEx = null;
    private int iSendSignedInfoRequestId = -1;
    public static boolean bResume = false;
    private ArrayList<CommunityActDetail> listCommActData = null;
    private ScrollView scrollView;
    private int iAddViewIndex = 0;
    public static Map<Integer, Integer> mapItemPackages = null;
    private Handler mHandler = new Handler()
    {
        @Override
        public void handleMessage(Message msg)
        {
            switch(msg.what)
            {
                case 0:
                    sendSigndInfo((String) msg.obj);
                    break;
            }
            super.handleMessage(msg);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_act_register_extend);

        if (getIntent() != null)
        {
            originalDetailInfo = (CommunityActDetail) getIntent().getSerializableExtra(ActRegisterActivity.ACT_DETAIL_INFO);
        }
        if (originalDetailInfo == null || originalDetailInfo.getCommit_items() == null)
        {
            finish();
            String fileMethodLine = "";
            fileMethodLine += "File: " + Thread.currentThread().getStackTrace()[2].getFileName();//文件名
            fileMethodLine += ",Method: " + Thread.currentThread().getStackTrace()[2].getMethodName();//函数名
            fileMethodLine += ",Line: " + Thread.currentThread().getStackTrace()[2].getLineNumber();//行号
            GoomeLog.getInstance().logE(fileMethodLine, "CommunityActDetail or CommitItems is null! Can't draw UI.", 0);
            return;
        }

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

        initData();

        initViews();

//        mServiceAdaper = ServerRequestController.getInstance(this);
//        mServiceAdaper.registerServiceCallBack(this);
        serviceAdapter = ServiceAdapter.getInstance(this);
        serviceAdapter.registerServiceCallBack(this);
    }

    private void initData()
    {
        listCommActData = new ArrayList<CommunityActDetail>();
        if(originalDetailInfo.getPrice() != null && originalDetailInfo.getPrice().getType() == ActPrice.PRICE_PACKAGE
                && originalDetailInfo.getPrice().getPrice_rule() != null)
        {
            int iMax = 0;
            mapItemPackages = new HashMap<Integer, Integer>();
            int i = 0;
            while (i < originalDetailInfo.getPrice().getPrice_rule().size())
            {
                int iPackageMaxCnt = originalDetailInfo.getPrice().getPrice_rule().get(i).getMax_cnt();
                if(iPackageMaxCnt <= 0)
                {
                    //该套餐不限制人数
                    iMax = Integer.MAX_VALUE / 2;
                }
                else
                {
                    iMax += iPackageMaxCnt;
                }
                mapItemPackages.put(i, 0);
                i++;
            }

            //取总人数限制和探槽综合限制的最小值
            int iTotalMaxCnt = originalDetailInfo.getPrice().getTotal_max_cnt();
            if(iTotalMaxCnt <= 0 && iMax > 0)
            {
                //总人数不限，但是套餐限制了，那么以套餐限制的为准
                originalDetailInfo.getPrice().setTotal_max_cnt(iMax);
            }
            else if(iTotalMaxCnt > 0 && iTotalMaxCnt > iMax)
            {
                //总人数限制了，但是总人数的限制比套餐限制的多，那么也以套餐限制的为准
                originalDetailInfo.getPrice().setTotal_max_cnt(iMax);
            }
        }
    }

    private void sendSigndInfo(String data)
    {
        iSendSignedInfoRequestId = serviceAdapter.sendSignInfo(this.hashCode(), CommonUtil.getTicket(),
                ActRegisterExtendActivity.REG_SIGN, REG_SIGN, originalDetailInfo.getId(), null, null, null, null, data,0,0);
    }

    private void showWaitInfoDialog()
    {
        progressDialogEx = new ProgressDialogEx(this);
        progressDialogEx.setAutoDismiss(false);
        progressDialogEx.setCancelOnTouchOutside(false);
        progressDialogEx.setCancelable(false);

        try
        {
            progressDialogEx.show(getString(R.string.please_wait));
        }
        catch (Exception e)
        {

        }
    }

    private void dismissWaitDialog()
    {
        if (progressDialogEx != null && progressDialogEx.isShowing())
        {
            progressDialogEx.dismiss();
        }
    }

    @Override
    protected void onDestroy()
    {
        // TODO Auto-generated method stub
//        if (mServiceAdaper != null)
//        {
//            mServiceAdaper.unregisterServiceCallBack(this);
//        }
        if (serviceAdapter != null) {
            serviceAdapter.unregisterServiceCallBack(this);
        }
        if(mapItemPackages != null)
        {
            mapItemPackages.clear();
            mapItemPackages = null;
        }
        super.onDestroy();
    }

    private void initViews()
    {
        MyActionbar actionbar = (MyActionbar) findViewById(R.id.mineActionbar);
        actionbar.initActionbar(true, R.string.activity_register, 0, 0);

        textCommit = (TextView) findViewById(R.id.textViewCommit);
        textCosts = (TextView) findViewById(R.id.textViewCostAll);
        imageCheck = (ImageView) findViewById(R.id.checkImageAgreement);
        scrollView = (ScrollView)findViewById(R.id.scrollViewMain);

        textCommit.setOnClickListener(this);
        imageCheck.setOnClickListener(this);
        findViewById(R.id.textViewLiability).setOnClickListener(this);
        imageCheck.setSelected(true);

        textAdd = (TextView) findViewById(R.id.textViewAdd);
        textAdd.setOnClickListener(this);

        layoutMain = (LinearLayout) findViewById(R.id.layoutMain);

        addInputItem();

        setTextByUser();
    }

    private void setTextByUser()
    {
        int price = 0;
        ActPrice actPrice = originalDetailInfo.getPrice();
        if(actPrice != null)
        {
            if (actPrice.getType() == ActPrice.PRICE_FASTEN)
            {
                //固定价格
                for(CommunityActDetail actDetail : listCommActData)
                {
                    if(actDetail.getPrice() != null)
                    {
                        price += actDetail.getPrice().getFixed_price();
                    }
                }
                if (price > 0)
                {
                    String data = CommunityUtil.getMoneyStrByIntDecimal(this, price, 2);
                    SpannableStringBuilder spsbStyl = new SpannableStringBuilder(data);
                    int endLengh = data.length();
                    if (data.contains("."))
                    {
                        endLengh = data.indexOf(".");
                    }
                    spsbStyl.setSpan(new AbsoluteSizeSpan(getResources().getDimensionPixelSize(R.dimen.text_num)), 1, endLengh, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    textCosts.setText(spsbStyl);
                }
                else
                {
                    textCosts.setText(R.string.free);
                }
            }
            else if (actPrice.getType() == ActPrice.PRICE_OFFLINE)
            {
                textCosts.setText(R.string.pay_offline);
            }
            else if (actPrice.getType() == ActPrice.PRICE_FREE)
            {
                textCosts.setText(R.string.free);
            }
            else if (actPrice.getType() == ActPrice.PRICE_PACKAGE)
            {
                //套餐价格
                for(CommunityActDetail actDetail : listCommActData)
                {
                    if(actDetail.getPrice() != null)
                    {
                        ArrayList<PriceRule> priceRules = actDetail.getPrice().getPrice_rule();
                        int value = actDetail.getPrice().getPrice_index();
                        if(value >= 0 && value < priceRules.size())
                        {
                            price += priceRules.get(value).getPrice();
                        }
                    }
                }
                if (price > 0)
                {
                    String data = CommunityUtil.getMoneyStrByIntDecimal(this, price, 2);
                    SpannableStringBuilder spsbStyl = new SpannableStringBuilder(data);
                    int endLengh = data.length();
                    if (data.contains("."))
                    {
                        endLengh = data.indexOf(".");
                    }
                    spsbStyl.setSpan(new AbsoluteSizeSpan(getResources().getDimensionPixelSize(R.dimen.text_num)), 1, endLengh, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    textCosts.setText(spsbStyl);
                }
                else
                {
                    String data = CommunityUtil.getPackagePriceMin(this, actPrice.getPrice_rule());
                    SpannableStringBuilder spsbStyl = new SpannableStringBuilder(data);
                    int endLengh = data.length();
                    if (data.contains("."))
                    {
                        endLengh = data.indexOf(".");
                    }
                    spsbStyl.setSpan(new AbsoluteSizeSpan(getResources().getDimensionPixelSize(R.dimen.text_num)), 1, endLengh, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    textCosts.setText(spsbStyl);
                }
            }
        }
        else
        {
            textCosts.setText("");
        }

        if(listCommActData.size() > 1)
        {
            textCommit.setText(getString(R.string.commit_users, listCommActData.size()));
        }
        else
        {
            textCommit.setText(R.string.commit);
        }
    }

    private ActRegisterView getCurrentActView()
    {
        for (int i = 0; i < layoutMain.getChildCount(); i++)
        {
            View view = layoutMain.getChildAt(i);
            if (view != null && view instanceof ActRegisterView && ((ActRegisterView) view).isJumpToOtherActivity())
            {
                return (ActRegisterView) view;
            }
        }
        return null;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        ActRegisterView actRegisterView = getCurrentActView();
        if(actRegisterView != null)
        {
            actRegisterView.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void callback(int messageId, Result result)
    {
        // TODO Auto-generated method stub
        try
        {
            if (result.errcode == Result.ERROR_NETWORK)
            {
                dismissWaitDialog();
                Toast.makeText(this, R.string.network_error, Toast.LENGTH_SHORT).show();
            }
            else if (iSendSignedInfoRequestId == messageId && result.apiCode == Constant.FM_APIID_SEND_SIGN_INFO)
            {
                CommActDetailActivity.bReRequestNewData = true;
                dismissWaitDialog();
                if (result.success)
                {
                    if (result.mResult != null && result.mResult instanceof EmUser)
                    {
                        EmUser hdUser = (EmUser) result.mResult;
                        parseReturnSigned(hdUser);
                    }
                }
                else
                {
                    switch (result.errcode)
                    {
                        case Result.ERRCODE_ACT_HAVE_SIGNED:
                            showToast(getString(R.string.act_have_signed));
                            break;

                        case Result.ERRCODE_ACT_SIGN_STOPPED:
                            showToast(getString(R.string.act_signed_end_time));
                            break;

                        case Result.ERRCODE_ACT_SIGNED_FULL:
                            showToast(getString(R.string.act_have_fulled));
                            break;

                        default:
                            showToast(getString(R.string.act_signed_fail));
                            break;
                    }
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    private void parseReturnSigned(EmUser hdUser)
    {
        if (hdUser != null)
        {
            if(ConfigUtil.isUseGMIMGroup())
            {
                //goome群聊
                if (GMClient.getInstance().isInited() && !TextUtils.isEmpty(hdUser.getChatroom_id())) {
                    final long chatRoomId = Long.parseLong(hdUser.getChatroom_id());
                    if (chatRoomId > 0) {
                        GMClient.getInstance().chatroomManager().joinChatRoom(chatRoomId,
                                AllOnlineApp.getCurrentLocation().getLongitude(),
                                AllOnlineApp.getCurrentLocation().getLatitude(),
                                AllOnlineApp.sToken.name,
                                "",
                                new GMValueCallBack<GMChatRoom>() {
                                    @Override
                                    public void onSuccess(GMChatRoom room) {
                                        initChatroomInfo(chatRoomId);
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                showToast(getString(R.string.register_success));
                                            }
                                        });
                                    }

                                    @Override
                                    public void onError(GMError gmError) {
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                showToast(getString(R.string.register_success_join_fail));
                                            }
                                        });
                                    }
                                });
                    } else {
                        GoomeLog.getInstance().logE("ActRegisterExtendActivity", "Signed successed. Chat room id: " + chatRoomId, 0);
                    }
                } else {
                    GoomeLog.getInstance().logE("ActRegisterExtendActivity", "Signed successed. GmClient not init, chat room id: " + hdUser.getChatroom_id(), 0);
                }
            }
            else
            {
                showToast(getString(R.string.register_success));
            }
            if(hdUser.getOrder_id() > 0)
            {
                goToOrderActivity(hdUser.getOrder_id());
            }

            // 报名成功，获取到环信用户密码，延时3s后自动调用登录接口
//            if (MainActivity.mActivity != null)
//            {
//                MainActivity.mActivity.setEmUser(hdUser, 3000);
//            }
//            else if (AllOnlineApp.getCommunityUser() != null)
//            {
//                //AllOnlineApp.getCommunityUser().setEmUser(hdUser);
//                AllOnlineApp.getCommunityUser().setHxAccount(hdUser.getHxuser());
//                AllOnlineApp.getCommunityUser().setHxPwd(hdUser.getHxpwd());
//                PreferenceUtil.commitString(Constant.PREFERENCE_USER_INFO, new Gson().toJson(AllOnlineApp.getCommunityUser(), CommunityUser.class));
//            }
        }
        else
        {
            showToast(R.string.register_fail);
        }
    }
    //报名加群成功后就初始化群的基本信息
    private void initChatroomInfo(final long roomId)
    {
        final GMChatRoomManager chatroomMgr = GMClient.getInstance().chatroomManager();
        if (chatroomMgr == null)
        {
            return;
        }
        GMChatRoom room = chatroomMgr.getChatroomInfoFromDB(roomId);
        if (room == null)
        {
            new Thread(new Runnable()
            {
                @Override
                public void run()
                {
                    chatroomMgr.getChatroomSpecificationFromServerWithId(roomId, new GMValueCallBack<GMChatRoom>()
                    {
                        @Override
                        public void onSuccess(GMChatRoom room)
                        {
                            chatroomMgr.getPushServiceEnableFromServer(roomId, new GMValueCallBack<Boolean>()
                            {
                                @Override
                                public void onSuccess(Boolean aBoolean)
                                {
                                }

                                @Override
                                public void onError(GMError gmError)
                                {
                                }
                            });
                        }

                        @Override
                        public void onError(GMError gmError)
                        {
                        }
                    });
                }
            }).start();
        }
    }
    private void goToOrderActivity(long iOrderId)
    {
        if (originalDetailInfo != null)
        {
            ActPrice actPrice = originalDetailInfo.getPrice();
            if (actPrice != null)
            {
                if ((actPrice.getType() == ActPrice.PRICE_FASTEN && actPrice.getFixed_price() > 0) || actPrice.getType() == ActPrice.PRICE_PACKAGE)
                {
                    //固定价格且大于0、套餐价格，去支付
                    Intent intent = new Intent(ActRegisterExtendActivity.this, MyOrderInfoActivity.class);
                    //intent.setFlags(Intent.FLAG_ACTIVITY_FORWARD_RESULT);
                    intent.putExtra(Constant.NATIVE_ACTIVITY_ID, originalDetailInfo.getId());
                    intent.putExtra(MyOrderInfoActivity.ORDER_DETAIL_OR_PAYING, MyOrderInfoActivity.ORDER_PAYING);
                    intent.putExtra(MyOrderInfoActivity.ORDER_ID, iOrderId);
                    intent.putExtra(MyOrderInfoActivity.ORDER_FROM_SIGNED, true);
                    startActivity(intent);
                    finish();
                    return;
                }
            }

            //线下支付或免费，不需要支付，直接去报了名的活动详情
            Intent intent = new Intent(ActRegisterExtendActivity.this, MyOrderInfoActivity.class);
            intent.putExtra(Constant.NATIVE_ACTIVITY_ID, originalDetailInfo.getId());
            intent.putExtra(MyOrderInfoActivity.ORDER_DETAIL_OR_PAYING, MyOrderInfoActivity.ORDER_DETAIL);
            intent.putExtra(MyOrderInfoActivity.ORDER_ID, iOrderId);
            startActivity(intent);
            finish();
        }
    }

    @Override
    public void finish()
    {
        // TODO Auto-generated method stub
        // 在CommActDetailActivity.class中判断
        ActRegisterExtendActivity.this.setResult(Activity.RESULT_OK);
        super.finish();
    }

    @Override
    protected void onStart()
    {
        // TODO Auto-generated method stub
        super.onStart();
        bResume = true;
        IntentFilter filter = new IntentFilter(RedPacketConstant.LONGIN_TWO_DEVICES_CONFIRM_ACTION);
        registerReceiver(broadcastReceiver, filter);
    }

    @Override
    protected void onStop()
    {
        // TODO Auto-generated method stub
        super.onStop();
        bResume = false;
        unregisterReceiver(broadcastReceiver);
    }

    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver()
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            // TODO Auto-generated method stub
            if (RedPacketConstant.LONGIN_TWO_DEVICES_CONFIRM_ACTION.equals(intent.getAction()))
            {
                finish();
            }
        }
    };

    private String getInputAllData()
    {
        /*这个接口！！！上传报名信息，请参照接口文档吧...*/
        CommitExtendAll commitExtendAll = new CommitExtendAll();
        //他人的总报名信息
        ArrayList<ArrayList<CommitExtendItem>> listOthersInfo = new ArrayList<ArrayList<CommitExtendItem>>();
        //他人的套餐信息
        ArrayList<Integer> listPriceIds = new ArrayList<Integer>();
        //套餐中每个分类的计数
        ArrayList<CommitPriceRuleCnt> listPrices = new ArrayList<CommitPriceRuleCnt>();
        if (originalDetailInfo.getPrice() != null && originalDetailInfo.getPrice().getType() == ActPrice.PRICE_PACKAGE && originalDetailInfo.getPrice().getPrice_rule() != null)
        {
            for (PriceRule priceRule : originalDetailInfo.getPrice().getPrice_rule())
            {
                //套餐形式
                CommitPriceRuleCnt priceRuleCnt = new CommitPriceRuleCnt();
                priceRuleCnt.setId(priceRule.getId());
                priceRuleCnt.setCnt(0);
                listPrices.add(priceRuleCnt);
            }
        }

        for (int i = 0; i < listCommActData.size(); i++)
        {
            if(mapImagesList != null)
            {
                mapImagesList.clear();
            }
            CommunityActDetail commActDetailInfo = listCommActData.get(i);
            int iPriceRuleId = 0;
            if (commActDetailInfo.getPrice() != null && commActDetailInfo.getPrice().getType() == ActPrice.PRICE_PACKAGE)
            {
                //拼接所有的套餐数据
                int valueIndex = commActDetailInfo.getPrice().getPrice_index();
                if(valueIndex >= 0 && valueIndex < commActDetailInfo.getPrice().getPrice_rule().size())
                {
                    //统计各个套餐类型的选择数量
                    listPrices.get(valueIndex).setCnt(listPrices.get(valueIndex).getCnt() + 1);
                    iPriceRuleId = commActDetailInfo.getPrice().getPrice_rule().get(valueIndex).getId();
                }
            }
            //非套餐模式也必须传套餐的id
            if (i == 0)
            {
                //本人的套餐信息
                commitExtendAll.setOwn_price_rule_id(iPriceRuleId);
            }
            else
            {
                //他人的单人的套餐信息
                listPriceIds.add(iPriceRuleId);
            }
            /************************以上是套餐相关的信息**********************************/

            if (i == 0)
            {
                ArrayList<CommitExtendItem> listHostUserInfo = new ArrayList<CommitExtendItem>();
                if (getItemData(commActDetailInfo, listHostUserInfo))
                {
                    //本人的报名信息
                    if(mapImagesList != null && mapImagesList.size() > 0)
                    {
                        //本人上传图片
                        final int iStatus = uploadImageToServer(commActDetailInfo, listHostUserInfo);
                        runOnUiThread(new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                switch (iStatus)
                                {
                                    case IMAGE_UPLOAD_FAIL:
                                        showToast(getString(R.string.image_upload_fail));
                                        break;

                                    case COMMIT_FAIL:
                                        showToast(getString(R.string.act_signed_fail));
                                        break;
                                }
                            }
                        });
                        if(iStatus != 0)
                        {
                            //图片上传失败
                            return null;
                        }
                    }
                    commitExtendAll.setOwn_info(listHostUserInfo);
                }
            }
            else
            {
                //他人的单个报名信息
                ArrayList<CommitExtendItem> listothersInfo = new ArrayList<CommitExtendItem>();
                if (getItemData(commActDetailInfo, listothersInfo))
                {
                    if(mapImagesList != null && mapImagesList.size() > 0)
                    {
                        //他人上传图片
                        final int iStatus = uploadImageToServer(commActDetailInfo, listothersInfo);
                        runOnUiThread(new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                switch (iStatus)
                                {
                                    case IMAGE_UPLOAD_FAIL:
                                        showToast(getString(R.string.image_upload_fail));
                                        break;

                                    case COMMIT_FAIL:
                                        showToast(getString(R.string.act_signed_fail));
                                        break;
                                }
                            }
                        });
                        if (iStatus != 0)
                        {
                            //图片上传失败
                            return null;
                        }
                    }
                    listOthersInfo.add(listothersInfo);
                }
            }
        }

        commitExtendAll.setCompanion_info(listOthersInfo);
        commitExtendAll.setCompanion_price_rule_id(listPriceIds);
        commitExtendAll.setPrice_rule_cnt(listPrices);

        return new Gson().toJson(commitExtendAll);
    }

    private boolean isInputAllOk()
    {
        if(listCommActData == null || listCommActData.isEmpty())
        {
            return false;
        }
        for (int i =0; i < listCommActData.size(); i++)
        {
            if(!isItemInputOk(listCommActData.get(i), i))
            {
                return false;
            }
        }
        return true;
    }

    /**
     *检查有效性，然后进行消息提示
     * */
    private boolean isItemInputOk(CommunityActDetail commActDetailInfo, int position)
    {
        if (commActDetailInfo == null || commActDetailInfo.getCommit_items() == null || !imageCheck.isSelected())
        {
            if (!imageCheck.isSelected())
            {
                showToast(getString(R.string.act_disclaimer_unselect));
            }
            return false;
        }

        String userName = "";
        if(position < layoutMain.getChildCount())
        {
            View view = layoutMain.getChildAt(position);
            if(view != null && view instanceof ActRegisterView)
            {
                userName = ((ActRegisterView)view).getUserName();
            }
        }

        final ActPrice actPrice = commActDetailInfo.getPrice();
        if(actPrice != null && actPrice.getType() == ActPrice.PRICE_PACKAGE && actPrice.getPrice_rule() != null)
        {
            //套餐需要判断是否已经选择
            if(actPrice.getPrice_index() < 0 || actPrice.getPrice_index() > actPrice.getPrice_rule().size() - 1)
            {
                String title = actPrice.getPrice_rule_title();
                if(TextUtils.isEmpty(title))
                {
                    title = getString(R.string.act_package);
                }
                showToast(getString(R.string.act_package_tips, userName, title));
                return false;
            }
        }

        for (int i = 0; i < commActDetailInfo.getCommit_items().size(); i++)
        {
            ActCommitItem actCommitItem = commActDetailInfo.getCommit_items().get(i);
            if (actCommitItem != null && actCommitItem.getView() != null)
            {
                Object objectValue = actCommitItem.getValue();
                switch (actCommitItem.getType())
                {
                    case ActCommitItem.INPUT_NUMBERS:
                    case ActCommitItem.INPUT_TEXT_NORMAL:
                        if (actCommitItem.getRequired() == 1)
                        {
                            if (objectValue == null || TextUtils.isEmpty((String)objectValue) || !CommonUtil.isMatched(actCommitItem.getRegex(), (String) objectValue))
                            {
                                //必填项目，必须进行检查，并且只有提交数据的时候无效数据才提醒
                                showToast(actCommitItem, userName);
                                return false;
                            }
                        }
                        else
                        {
                            //非必填选项，如果填了则进行有效性检查
                            if (objectValue != null && !TextUtils.isEmpty((String)objectValue) && !CommonUtil.isMatched(actCommitItem.getRegex(), (String) objectValue))
                            {
                                showToast(actCommitItem, userName);
                                return false;
                            }
                        }
                        break;

                    case ActCommitItem.RADIO_CONTENT:
                        if (objectValue != null)
                        {
                            int value = -1;
                            if (objectValue instanceof Integer)
                            {
                                value = (Integer) objectValue;
                            }

                            if (value < 0 && value > actCommitItem.getOptions().size() - 1)
                            {
                                if (actCommitItem.getRequired() == 1)
                                {
                                    showToast(actCommitItem, userName);
                                    return false;
                                }
                            }
                        }
                        else if (actCommitItem.getRequired() == 1)
                        {
                            showToast(actCommitItem, userName);
                            return false;
                        }
                        break;

                    case ActCommitItem.POP_SELECT_CONTENT:
                    case ActCommitItem.MAP_SELECT_LOCATION:
                        if (actCommitItem.getRequired() == 1)
                        {
                            if (objectValue == null)
                            {
                                showToast(actCommitItem, userName);
                                return false;
                            }
                            if (objectValue instanceof ArrayList && ((ArrayList<Integer>) objectValue).size() <= 0)
                            {
                                //一般是弹窗选择
                                showToast(actCommitItem, userName);
                                return false;
                            }
                            if (objectValue instanceof MyLocationInfo)
                            {
                                //位置信息
                                MyLocationInfo locInfo = (MyLocationInfo) objectValue;
                                if (TextUtils.isEmpty(locInfo.getName()) || locInfo.getLat() <= 0 || locInfo.getLng() <= 0)
                                {
                                    showToast(actCommitItem, userName);
                                    return false;
                                }
                            }
                        }
                        break;

                    case ActCommitItem.UPLOAD_IMAGE:
                        GridView gridView = (GridView) actCommitItem.getView();
                        ImageAddActAdapter imageAddAdapter = (ImageAddActAdapter) gridView.getAdapter();
                        if (imageAddAdapter != null)
                        {
                            if (actCommitItem.getRequired() == 1 && imageAddAdapter.getImageList().size() <= 0)
                            {
                                showToast(actCommitItem, userName);
                                return false;
                            }
                        }
                        else if (actCommitItem.getRequired() == 1)
                        {
                            showToast(actCommitItem, userName);
                            return false;
                        }
                        break;
                }
            }
            else
            {
                showToast(actCommitItem, userName);
                return false;
            }
        }

        return true;
    }

    /**
     *获取每个人的输入信息
     * */
    private boolean getItemData(CommunityActDetail commActDetailInfo, ArrayList<CommitExtendItem> listCommits)
    {
        if (commActDetailInfo == null || commActDetailInfo.getCommit_items() == null)
        {
            return false;
        }

        if (listCommits == null)
        {
            listCommits = new ArrayList<CommitExtendItem>();
        }
        else
        {
            listCommits.clear();
        }
        if (mapImagesList == null)
        {
            mapImagesList = new HashMap<Integer, ArrayList<ImageInfo>>();
        }
        else
        {
            mapImagesList.clear();
        }

        for (int i = 0; i < commActDetailInfo.getCommit_items().size(); i++)
        {
            ActCommitItem actCommitItem = commActDetailInfo.getCommit_items().get(i);
            if (actCommitItem != null && actCommitItem.getView() != null)
            {
                Object objectValue = actCommitItem.getValue();
                if (actCommitItem.getType() == ActCommitItem.UPLOAD_IMAGE)
                {
                    //图片需要上传后再用URL拼接
                    GridView gridView = (GridView) actCommitItem.getView();
                    ImageAddActAdapter imageAddAdapter = (ImageAddActAdapter) gridView.getAdapter();
                    mapImagesList.put(actCommitItem.getId(), imageAddAdapter.getImageList());
                }
                else
                {
                    //其他数据直接拼接
                    CommitExtendItem commititem = new CommitExtendItem();
                    commititem.setId(actCommitItem.getId());
                    commititem.setValue(objectValue);
                    commititem.setType(actCommitItem.getType());
                    listCommits.add(commititem);
                }
            }
        }

        return true;
    }

    private void showToast(final ActCommitItem actCommitItem, final String userName)
    {
        if (actCommitItem != null)
        {
            showToast(getString(R.string.invalid_input, userName) + actCommitItem.getName());
        }
    }

    private void showToast(int strId)
    {
        Toast.makeText(this, strId, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onClick(View v)
    {
        // TODO Auto-generated method stub
        switch (v.getId())
        {
            case R.id.textViewCommit:
                commitData();
                break;

            case R.id.textViewAdd:
                addInputItem();
                break;

            case R.id.textViewLiability:
                // 显示免责声明
                Intent intent = new Intent(ActRegisterExtendActivity.this, DisclaimerActivity.class);
                startActivity(intent);
                break;

            case R.id.checkImageAgreement:
                if (imageCheck.isSelected())
                {
                    imageCheck.setSelected(false);
                }
                else
                {
                    imageCheck.setSelected(true);
                }
                break;
        }
    }

    private void commitData()
    {
        if(!CommonUtil.checkAndGoToLogin(this))
        {
            return;
        }

        if(!isInputAllOk())
        {
            return;
        }

        showWaitInfoDialog();

        new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                String data = getInputAllData();
                if(!TextUtils.isEmpty(data))
                {
                    Message msg = mHandler.obtainMessage();
                    msg.obj = data;
                    msg.what = 0;
                    mHandler.sendMessage(msg);
                }
                else
                {
                    runOnUiThread(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            dismissWaitDialog();
                        }
                    });
                }
                Log.i("Test", "=====data: " + data);
            }
        }).start();
    }

    private int uploadImageToServer(CommunityActDetail commActDetailInfo, ArrayList<CommitExtendItem> listCommits)
    {
        final AllOnlineApiClient client = new AllOnlineApiClient(this);
        if (mapImagesList != null && !mapImagesList.isEmpty() && commActDetailInfo.getCommit_items() != null)
        {
            //上传本地图片，可能存在多组（对应读个GridView）需要上传，一组有多张
            for (ActCommitItem mainItem : commActDetailInfo.getCommit_items())
            {
                ArrayList<ImageInfo> listImages = mapImagesList.get(mainItem.getId());
                ArrayList<String> listImageUrls = new ArrayList<String>();
                if (listImages != null && listImages.size() > 0)
                {
                    //存在图片，则上传后进行数据拼接
                    for (int i = 0; i < listImages.size(); i++)
                    {
                        ImageInfo imageInfo = listImages.get(i);
                        if (imageInfo != null)
                        {
                            if (!imageInfo.isNet())
                            {
                                if (uploadPic(client, imageInfo) != 0)
                                {
                                    //上传图片失败
                                    return IMAGE_UPLOAD_FAIL;
                                }
                            }
                            if (imageInfo.isNet())
                            {
                                listImageUrls.add((imageInfo.getDomain() == null ? "" : imageInfo.getDomain()) + (imageInfo.getImg_path() == null ? "" : imageInfo.getImg_path()));
                            }
                        }
                    }
                    //把上传后的网络图片拼接到要提交的数据里面
                    CommitExtendItem extendItem = new CommitExtendItem();
                    extendItem.setId(mainItem.getId());
                    extendItem.setValue(listImageUrls);
                    extendItem.setType(ActCommitItem.UPLOAD_IMAGE);
                    listCommits.add(extendItem);
                }
            }
        }
        return 0;
    }

    private int uploadPic(AllOnlineApiClient client, ImageInfo imageInfo)
    {
        Result result = client.pictureUpload(CommonUtil.getTicket(), imageInfo.getSource_image(), CommonUtil.getFileMimeType(imageInfo.getSource_image()));
        if (result != null && result.success)
        {
            ImageInfo resultImage = (ImageInfo) result.mResult;
            if (resultImage != null)
            {
                //String picUrl = resultImage.getDomain() + resultImage.getImg_path();
                imageInfo.setImg_path(resultImage.getImg_path());
                imageInfo.setDomain(resultImage.getDomain());
                imageInfo.setNet(true);
                try
                {
                    new File(imageInfo.getSource_image()).delete();
                }
                catch (Exception e)
                {
                }
            }
        }
        else
        {
            return IMAGE_UPLOAD_FAIL;
        }
        return 0;
    }

    private void addInputItem()
    {
        if(originalDetailInfo == null)
        {
            return;
        }
        ActPrice actPrice = originalDetailInfo.getPrice();
        if(actPrice == null)
        {
            return;
        }

        if(actPrice.getTotal_max_cnt() > 0 && listCommActData.size() >= actPrice.getTotal_max_cnt())
        {
            //多人报名人数已经达到上限
            showToast(R.string.act_sign_max);
            return;
        }
        CommunityActDetail detailData = new CommunityActDetail();
        if(detailData.getCommit_items() == null)
        {
            detailData.setCommit_items(new ArrayList<ActCommitItem>());
        }
        else
        {
            detailData.getCommit_items().clear();
        }

        if(actPrice != null && actPrice.getType() == ActPrice.PRICE_PACKAGE)
        {
            ActPrice newActPrice = new ActPrice();
            newActPrice.setType(actPrice.getType());
            newActPrice.setPrice_index(-1);
            newActPrice.setTotal_max_cnt(actPrice.getTotal_max_cnt());
            newActPrice.setFixed_price(actPrice.getFixed_price());
            newActPrice.setPrice_rule(actPrice.getPrice_rule());
            detailData.setPrice(newActPrice);
        }
        else
        {
            detailData.setPrice(actPrice);
        }
        detailData.setTitle(originalDetailInfo.getTitle());

        for(int i = 0; i < originalDetailInfo.getCommit_items().size(); i++)
        {
            ActCommitItem actCommitItem = originalDetailInfo.getCommit_items().get(i);
            if(actCommitItem != null && (actCommitItem.getIs_common() == 1 || listCommActData.size() == 0))
            {
                ActCommitItem tempItem = new ActCommitItem();
                tempItem.setIs_common(actCommitItem.getIs_common());
                tempItem.setId(actCommitItem.getId());
                tempItem.setName(actCommitItem.getName());
                tempItem.setType(actCommitItem.getType());
                tempItem.setDefault_option(actCommitItem.getDefault_option());
                tempItem.setOptions(actCommitItem.getOptions());
                tempItem.setHint(actCommitItem.getHint());
                tempItem.setLevel_num(actCommitItem.getLevel_num());
                tempItem.setValue(actCommitItem.getValue());
                tempItem.setRequired(actCommitItem.getRequired());
                tempItem.setRegex(actCommitItem.getRegex());
                detailData.getCommit_items().add(tempItem);
            }
        }

        listCommActData.add(detailData);

        int position = listCommActData.size() - 1;

        ActRegisterView actRegisterView = new ActRegisterView(this, detailData, position);
        actRegisterView.setViewTitle(iAddViewIndex);
        iAddViewIndex++;
        actRegisterView.setOnViewDeleteListener(onViewDeleteListener);
        layoutMain.addView(actRegisterView);
        setTextByUser();

        if(position > 0)
        {
            mHandler.postDelayed(new Runnable()
            {
                @Override
                public void run()
                {
                    scrollView.fullScroll(ScrollView.FOCUS_DOWN);
                }
            }, 0);
        }
        resetAddBtnVisibility();
    }

    private ActRegisterView.OnViewOrDataChangeListener onViewDeleteListener = new ActRegisterView.OnViewOrDataChangeListener()
    {
        @Override
        public void onViewDeleted(int position)
        {
            removeInputItem(position);
        }

        @Override
        public void onPackageChange()
        {
            setTextByUser();
        }
    };

    private void removeInputItem(int position)
    {
        if(position > 0 && position < layoutMain.getChildCount())
        {
            View removeView = layoutMain.getChildAt(position);
            if(originalDetailInfo.getPrice().getType() == ActPrice.PRICE_PACKAGE &&
                    removeView != null && removeView instanceof ActRegisterView)
            {
                //当前View选中的套餐
                int iPriceIndex = ((ActRegisterView)removeView).getPriceIndex();
                if(iPriceIndex >= 0 && mapItemPackages.containsKey(iPriceIndex))
                {
                    int iCount = mapItemPackages.get(iPriceIndex);
                    if(iCount > 0)
                    {
                        mapItemPackages.put(iPriceIndex, iCount - 1);
                    }
                }
            }
            layoutMain.removeViewAt(position);
            listCommActData.remove(position);
            setTextByUser();
            for(int i = 0; i < layoutMain.getChildCount(); i++)
            {
                View view = layoutMain.getChildAt(i);
                if(view != null && view instanceof ActRegisterView)
                {
                    ((ActRegisterView)view).resetCurViewIndex(i);
                }
            }
            resetAddBtnVisibility();
        }
    }

    private void resetAddBtnVisibility()
    {
        if(originalDetailInfo.getPrice().getTotal_max_cnt() <= layoutMain.getChildCount())
        {
            textAdd.setVisibility(View.GONE);
        }
        else
        {
            textAdd.setVisibility(View.VISIBLE);
        }
    }
}
