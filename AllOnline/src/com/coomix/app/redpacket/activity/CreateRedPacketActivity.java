package com.coomix.app.redpacket.activity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.maps.model.LatLng;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.core.PoiItem;
import com.amap.api.services.geocoder.GeocodeResult;
import com.amap.api.services.geocoder.GeocodeSearch;
import com.amap.api.services.geocoder.RegeocodeQuery;
import com.amap.api.services.geocoder.RegeocodeResult;
import com.coomix.app.all.AllOnlineApp;
import com.coomix.app.all.Constant;
import com.coomix.app.all.GlobalParam;
import com.coomix.app.all.R;
import com.coomix.app.all.model.bean.BalanceInfo;
import com.coomix.app.all.model.bean.LocationInfo;
import com.coomix.app.all.service.Error;
import com.coomix.app.all.service.ServiceAdapter;
import com.coomix.app.all.service.ServiceAdapter.ServiceAdapterCallback;
import com.coomix.app.all.dialog.AskDialog;
import com.coomix.app.all.ui.base.BaseActivity;
import com.coomix.app.all.ui.web.BusAdverActivity;
import com.coomix.app.all.widget.MyActionbar;
import com.coomix.app.all.dialog.PopListDialog;
import com.coomix.app.all.dialog.PopNearbyListDialog;
import com.coomix.app.framework.app.Result;
import com.coomix.app.framework.util.CommonUtil;
import com.coomix.app.framework.util.PreferenceUtil;
import com.coomix.app.all.dialog.ProgressDialogEx;
import com.coomix.app.pay.CoomixPayManager;
import com.coomix.app.pay.CoomixPayRsp;
import com.coomix.app.pay.ICoomixPay;
import com.coomix.app.pay.PayResultManager;
import com.coomix.app.redpacket.util.CreateRedPacketInfo;
import com.coomix.app.redpacket.util.RedPacketConfig;
import com.coomix.app.redpacket.util.RedPacketConfigItem;
import com.coomix.app.redpacket.util.RedPacketConstant;
import com.coomix.app.redpacket.util.RedPacketExtendInfo;
import com.coomix.app.redpacket.util.RedPacketInfo;
import com.coomix.app.redpacket.util.RpLocationUtil;
import com.coomix.app.redpacket.util.UnsendRedPackets;
import com.coomix.app.all.util.AppUtil;
import com.coomix.app.all.util.CommunityUtil;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by ssl on 2017/2/13.
 */
public class CreateRedPacketActivity extends BaseActivity
        implements View.OnClickListener, ServiceAdapterCallback,
                   AllOnlineApp.OnLocationChangeListener
{
    //public static final String EXTRA_TYPE_RP = "extra_type_rp";
    public static final String EXTRA_TYPE_DISPLAY = "extra_type_display";
    public static final String EXTRA_PEOPLE_COUNT = "extra_people_count";
    public static final String EXTRA_SPECIFIED_ID = "extra_specified_id";
    private LinearLayout layoutMoney;
    private TextView textTopInfo;
    private TextView textRPTypeFlag;
    private TextView textMoneyTitle;
    private EditText editMoney;
    private EditText editNum;
    private EditText editHello;
    private TextView textMoneyUnit;
    private TextView textRPCurType;
    private TextView textRPTypeChange;
    private TextView textRPNumTitle;
    private TextView textRPNumUnit;
    private TextView textTotalPeople;
    private TextView textMoneyTotal;
    private TextView textCommit;
    private TextView textWechatPay;
    private TextView textPocketPay;
    private TextView textExplain;
    //private ServerRequestController serverController;
    private ServiceAdapter serviceAdapter;
    private int iRandomNormal = RedPacketConstant.REDPACKET_TYPE_RANDOM;
    private int iCurDisplayType = RedPacketConstant.RP_DISPLAY_CHAT_GROUP;
    private int iRequestRPConfig = -1;
    private int iRequestCreateRP = -1;
    private int iRequestBalance = -1;
    private int iGetUnsendRedPacket = -1;
    private int iPeoPleCount = 0;
    //private RedPacketConfig rpConfigItem = null;
    private RedPacketConfigItem rpConfigItem = new RedPacketConfigItem();
    private long iInputMoney = 0;  //乘以了100，以分为单位
    private int iInputNum = 0;
    private boolean bCommitOk = false;
    private int iPayPlatform = CoomixPayManager.PAY_WECHAT;
    private CoomixPayManager coomixPayManager = null;
    private String toSpecifiedId;
    private ProgressDialogEx progressDialogEx = null;
    private final long GOOME_BALANCE_DEF = -101; //还没向后台请求过零钱的余额默认值
    private long iGoomeBalance = GOOME_BALANCE_DEF; //谷米零钱的余额
    private String sCreateRedPacketId = "";
    private static int iExplainIndex = -1;
    private RedPacketConfig redPacketConfigs;
    private boolean bFirstIn = true;
    private final int REQUEST_MAP_CODE = 10;
    //发红包帖
    private LinearLayout llRpRange;
    private LinearLayout llRpLocName;
    private TextView textRpRange;
    private TextView textRpLocName;
    //0表示不限范围
    private int currRange = 0;
    private int defRange = AllOnlineApp.getAppConfig().getRedpacket_default_alloc_range();
    private boolean parseLocName = false;
    private int[] ranges;
    private String currLocName;
    private View topBarView = null;
    private LatLng redpacketLatLng = null;
    private int iClickCount = 0;
    private boolean bSelectedNameOk = false;
    private PopNearbyListDialog popDialog = null;
    private final int WEIXIN_UNBIND_MSG = 10000;

    private Handler mHandler = new Handler()
    {
        @Override
        public void handleMessage(Message msg)
        {
            if (msg.what == 0)
            {
                iClickCount = 0;
            }
            super.handleMessage(msg);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        ++iExplainIndex;

        setContentView(R.layout.activity_create_redpacket);

        AllOnlineApp.registerLocationChangeListener(this.hashCode(), this);

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        AllOnlineApp.getInstantce().setSelectLocationInfo(null);

        getData();

        initViews();

        setUiData();

        serviceAdapter = ServiceAdapter.getInstance(this);
        serviceAdapter.registerServiceCallBack(this);

//        serverController = ServerRequestController.getInstance(this);
//        serverController.registerServiceCallBack(this);
//        iRequestRPConfig = serverController.getRedPacketConfig(this.hashCode(), CommonUtil.getTicket(),
//                iCurDisplayType);
        //requestBalance();
        initNetData();
    }

    public void initNetData() {
        iRequestRPConfig = serviceAdapter.getRedPacketConfig(this.hashCode(), CommonUtil.getTicket(), iCurDisplayType);
        requestBalance();
    }
    private void requestBalance()
    {
        iRequestBalance = serviceAdapter.getPocketBalance(this.hashCode(), CommonUtil.getTicket());
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
//        if (serverController != null)
//        {
//            serverController.unregisterServiceCallBack(this);
//        }
        if (serviceAdapter != null) {
            serviceAdapter.unregisterServiceCallBack(this);
        }
        if (mHandler != null)
        {
            mHandler.removeCallbacksAndMessages(null);
        }
        AllOnlineApp.unregisterLocationChangeListener(this.hashCode());
    }

    private void getData()
    {
        if (getIntent() != null)
        {
            if (getIntent().hasExtra(EXTRA_TYPE_DISPLAY))
            {
                iCurDisplayType = getIntent().getIntExtra(EXTRA_TYPE_DISPLAY, RedPacketConstant.RP_DISPLAY_CHAT_GROUP);
            }
            if (getIntent().hasExtra(EXTRA_PEOPLE_COUNT))
            {
                iPeoPleCount = getIntent().getIntExtra(EXTRA_PEOPLE_COUNT, 0);
            }
            if (getIntent().hasExtra(EXTRA_SPECIFIED_ID))
            {
                toSpecifiedId = getIntent().getStringExtra(EXTRA_SPECIFIED_ID);
            }
        }
    }

    private void initViews()
    {
        MyActionbar actionbar = (MyActionbar) findViewById(R.id.myActionbar);
        actionbar.initActionbar(true, R.string.pack_redpacket, 0, R.drawable.redpacket_help);
        actionbar.setLeftImageVisibility(View.GONE);
        actionbar.setBackgroundColor(getResources().getColor(R.color.redpacket_red));

        //左侧返回
        actionbar.setLeftText(R.string.back);
        actionbar.setLeftTextCLickListener(view -> finish());

        //右侧介绍
        actionbar.setRightImageClickListener(view -> {
            bFirstIn = true;
            CommunityUtil.switch2WebViewActivity(CreateRedPacketActivity.this,
                    AllOnlineApp.getAppConfig().getRedpacket_pack_help_url(), false,
                    BusAdverActivity.INTENT_FROM_REDPACKET);
        });

        //layoutMainAdd = (LinearLayout) findViewById(R.id.layoutMainAdd);
        layoutMoney = (LinearLayout) findViewById(R.id.layoutMoneyInput);
        textTopInfo = (TextView) findViewById(R.id.textViewToast);
        textRPTypeFlag = (TextView) findViewById(R.id.textViewRpTypeFlag);
        textMoneyTitle = (TextView) findViewById(R.id.textViewMoneyNum);
        editMoney = (EditText) findViewById(R.id.editTextMoney);
        editNum = (EditText) findViewById(R.id.editTextRPNum);
        textMoneyUnit = (TextView) findViewById(R.id.textViewMoneyUnit);
        textRPCurType = (TextView) findViewById(R.id.textViewRPType);
        textRPTypeChange = (TextView) findViewById(R.id.textViewRPTypeChange);
        textRPNumTitle = (TextView) findViewById(R.id.textViewRPNum);
        textRPNumUnit = (TextView) findViewById(R.id.textViewRPNumUnit);
        textTotalPeople = (TextView) findViewById(R.id.textViewPeopleCount);
        editHello = (EditText) findViewById(R.id.editTextHello);
        textMoneyTotal = (TextView) findViewById(R.id.textViewMoneyTotal);
        textMoneyTotal.setTypeface((Typeface.createFromAsset(getAssets(), "goome.ttf")));
        textCommit = (TextView) findViewById(R.id.textViewRPCommit);
        textWechatPay = (TextView) findViewById(R.id.textViewWechatPay);
        textPocketPay = (TextView) findViewById(R.id.textViewPocketPay);
        textExplain = (TextView) findViewById(R.id.textViewExplain);

        //红包范围
        llRpRange = (LinearLayout) findViewById(R.id.ll_redpacket_range);
        llRpLocName = (LinearLayout) findViewById(R.id.ll_redpacket_range_name);
        textRpRange = (TextView) findViewById(R.id.redpacket_range);
        textRpLocName = (TextView) findViewById(R.id.redpacket_loc_name);
        textRpLocName.setOnClickListener(this);
        if (isCommunityRp())
        {
            textRpRange.setOnClickListener(this);
            llRpRange.setVisibility(View.VISIBLE);
            llRpLocName.setVisibility(View.GONE);
            setDefaultRpRange();
            if (isRpTopic())
            {
                textCommit.setText(R.string.send_redpacket_topic);
            }
            else if (isRpReply())
            {
                textCommit.setText(R.string.send_redpacket_reply);
            }
        }
        else
        {
            llRpRange.setVisibility(View.GONE);
        }

        iPayPlatform = PreferenceUtil.getInt(RedPacketConstant.RP_LAST_PAY_PLATFORM, CoomixPayManager.PAY_WECHAT);
        setPayPlatform();

        textRPTypeChange.setOnClickListener(this);
        textCommit.setOnClickListener(this);
        textWechatPay.setOnClickListener(this);
        textPocketPay.setOnClickListener(this);
        editMoney.addTextChangedListener(textWatcher);
        editNum.addTextChangedListener(textWatcher);

        setRedPacketExplain();

        currLocName = getString(R.string.redpacket_def_loc_name);
    }

    private boolean isCommunityRp()
    {
        return iCurDisplayType == RedPacketConstant.RP_DISPLAY_COMMUNITY_TOPIC
               || iCurDisplayType == RedPacketConstant.RP_DISPLAY_COMMUNITY_REPLY;
    }

    private boolean isRpTopic()
    {
        return iCurDisplayType == RedPacketConstant.RP_DISPLAY_COMMUNITY_TOPIC;
    }

    private boolean isRpReply()
    {
        return iCurDisplayType == RedPacketConstant.RP_DISPLAY_COMMUNITY_REPLY;
    }

    private void setRedPacketExplain()
    {
        int[] explainStrs = {R.string.redpacket_explain1, R.string.redpacket_explain2, R.string.redpacket_explain3};
        if (iExplainIndex < 0 || iExplainIndex > explainStrs.length - 1)
        {
            iExplainIndex = 0;
        }
        if (iExplainIndex == explainStrs.length - 1)
        {
            if (rpConfigItem != null)
            {
                textExplain.setText(getString(R.string.redpacket_explain3,
                        CommunityUtil.getDecimalStrByInt(rpConfigItem.getMin_amount(), 2),
                        CommunityUtil.getDecimalStrByInt(rpConfigItem.getMax_single(), 2)));
            }
        }
        else
        {
            textExplain.setText(explainStrs[iExplainIndex]);
        }
    }

    private TextWatcher textWatcher = new TextWatcher()
    {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after)
        {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count)
        {

        }

        @Override
        public void afterTextChanged(Editable s)
        {
            if (editMoney.hasFocus() && s != null)
            {
                String text = s.toString();
                if (text.contains(".") && text.substring(text.indexOf(".") + 1, text.length()).length() > 2)
                {
                    editMoney.setText(text.substring(0, text.indexOf(".") + 3));
                    editMoney.setSelection(editMoney.getText().length());
                    return;
                }
            }
            setMoneyNumStatus(editMoney.getText().toString(), editNum.getText().toString());
        }
    };

    private void setUiData()
    {
        resetNormalRandom();
        if (iCurDisplayType == RedPacketConstant.RP_DISPLAY_CHAT_GROUP)
        {
            //群红包
            if (iPeoPleCount > 0)
            {
                textTotalPeople.setVisibility(View.VISIBLE);
                textTotalPeople.setText(getString(R.string.group_people_count, iPeoPleCount));
            }
            else
            {
                textTotalPeople.setVisibility(View.GONE);
            }
        }
        else if (iCurDisplayType == RedPacketConstant.RP_DISPLAY_CHAT_SINGLE)
        {
//            //私聊红包
//            textTotalPeople.setVisibility(View.GONE);
//            iRandomNormal = RedPacketConstant.REDPACKET_TYPE_NORMAL;
//            editNum.setText("1");
//            findViewById(R.id.layoutRPChangeType).setVisibility(View.GONE);
//            findViewById(R.id.layoutRPNum).setVisibility(View.GONE);
        }
        else
        {
            textTotalPeople.setVisibility(View.GONE);
        }

        if (!TextUtils.isEmpty(rpConfigItem.getHello_words()))
        {
            editHello.setHint(rpConfigItem.getHello_words());
        }
        else
        {
            editHello.setHint(R.string.redpacket_default_title);
        }
    }

    private void resetNormalRandom()
    {
        if (iRandomNormal == RedPacketConstant.REDPACKET_TYPE_NORMAL)
        {
            //均包
            textRPTypeFlag.setVisibility(View.GONE);
            //textMoneyTitle.setText(R.string.money_single);
            if (iCurDisplayType == RedPacketConstant.RP_DISPLAY_CHAT_SINGLE)
            {
                textMoneyTitle.setText(R.string.money_chat_single);
            }
            else
            {

                iInputNum = 1;
                textMoneyTitle.setText(R.string.money_single);
            }
            textRPCurType.setText(getString(R.string.redpacket_type, getString(R.string.redpacket_normal)));
            textRPTypeChange.setText(getString(R.string.change_to, getString(R.string.redpacket_random)));
            if (iInputMoney > 0)
            {
                if (iInputNum <= 0)
                {
                    editMoney.setText(CommunityUtil.getDecimalStrByInt(iInputMoney, 2));
                }
                else
                {
                    editMoney.setText(CommunityUtil.getDecimalStrByInt(iInputMoney / iInputNum, 2));
                }
                editMoney.setSelection(editMoney.length());
            }
        }
        else if (iRandomNormal == RedPacketConstant.REDPACKET_TYPE_RANDOM)
        {
            //拼手气
            textRPTypeFlag.setVisibility(View.VISIBLE);
            textRPTypeFlag.setText(R.string.grab);
            textMoneyTitle.setText(R.string.money_total);
            textRPCurType.setText(getString(R.string.redpacket_type, getString(R.string.redpacket_random)));
            textRPTypeChange.setText(getString(R.string.change_to, getString(R.string.redpacket_normal)));
            if (iInputMoney > 0)
            {
                if (iInputNum <= 0)
                {
                    editMoney.setText(CommunityUtil.getDecimalStrByInt(iInputMoney, 2));
                }
                else
                {
                    editMoney.setText(CommunityUtil.getDecimalStrByInt(iInputMoney * iInputNum, 2));
                }
                editMoney.setSelection(editMoney.length());
            }
        }

        String money = "";
        String num = "";
        if (editMoney.getText() != null)
        {
            money = editMoney.getText().toString();
        }
        if (editNum.getText() != null)
        {
            num = editNum.getText().toString();
        }

        setMoneyNumStatus(money, num);
    }

    private void setMoneyNumStatus(String money, String num)
    {
        if (rpConfigItem == null)
        {
            rpConfigItem = new RedPacketConfigItem();
        }

        if (!TextUtils.isEmpty(money))
        {
            iInputMoney = getTextMoney(money);
        }
        else
        {
            iInputMoney = 0;
        }

        if (!TextUtils.isEmpty(num))
        {
            iInputNum = Integer.parseInt(num);
        }
        else
        {
            iInputNum = 0;
        }

        String errorMsg = "";
        boolean bInvalidMoney = false;
        boolean bInvalidNum = false;
        long iInputTotalMoney = 0;

        if (iRandomNormal == RedPacketConstant.REDPACKET_TYPE_NORMAL)
        {
            //均包
            if (iInputNum >= 0)
            {
                iInputTotalMoney = iInputMoney * iInputNum;
            }
            if (!TextUtils.isEmpty(num) && iInputNum <= 0)
            {
                //红包个数无效
                bInvalidNum = true;
                errorMsg = getString(R.string.pls_input_rp_num);
            }
            else if (iInputNum > rpConfigItem.getMax_num())
            {
                //红包个数超了
                bInvalidNum = true;
                errorMsg = getString(R.string.max_rp_send, rpConfigItem.getMax_num());
            }
            else if (iInputMoney > rpConfigItem.getMax_single())
            {
                //单个红包超额
                bInvalidMoney = true;
                errorMsg = getString(R.string.max_rp_single_money, rpConfigItem.getMax_single() / 100);
            }
            else if (iInputMoney > 0 && iInputMoney < rpConfigItem.getMin_amount())
            {
                //单个红包额度不够
                bInvalidMoney = true;
                errorMsg = getString(R.string.min_rp_single_money,
                        CommunityUtil.getDecimalStrByInt(rpConfigItem.getMin_amount(), 2));
            }
            else if (iInputTotalMoney > 0 && iInputTotalMoney > rpConfigItem.getMax_amount())
            {
                //红包总额超额
                bInvalidMoney = true;
                errorMsg = getString(R.string.max_rp_total_money, rpConfigItem.getMax_amount() / 100);
            }
            else if (iInputNum <= 0 && iInputMoney > 0 && iInputMoney > rpConfigItem.getMax_amount())
            {
                //红包总额超额
                bInvalidMoney = true;
                errorMsg = getString(R.string.max_rp_total_money, rpConfigItem.getMax_amount() / 100);
            }
        }
        else if (iRandomNormal == RedPacketConstant.REDPACKET_TYPE_RANDOM)
        {
            //拼手气
            iInputTotalMoney = iInputMoney;
            if (!TextUtils.isEmpty(num) && iInputNum <= 0)
            {
                //红包个数无效
                bInvalidNum = true;
                errorMsg = getString(R.string.pls_input_rp_num);
            }
            else if (iInputNum > rpConfigItem.getMax_num())
            {
                //红包个数超了
                bInvalidNum = true;
                errorMsg = getString(R.string.max_rp_send, rpConfigItem.getMax_num());
            }
            else if (iInputTotalMoney > 0 && iInputTotalMoney > rpConfigItem.getMax_amount())
            {
                //红包总额超额
                bInvalidMoney = true;
                errorMsg = getString(R.string.max_rp_total_money, rpConfigItem.getMax_amount() / 100);
            }
            else if (iInputTotalMoney > 0 && iInputNum > 0)
            {
                if (iInputTotalMoney > rpConfigItem.getMax_single() * iInputNum)
                {
                    //单个红包超额
                    bInvalidMoney = true;
                    errorMsg = getString(R.string.max_rp_single_money, rpConfigItem.getMax_single() / 100);
                }
                else if (iInputTotalMoney < rpConfigItem.getMin_amount() * iInputNum)
                {
                    //单个红包额度不够
                    bInvalidMoney = true;
                    errorMsg = getString(R.string.min_rp_single_money,
                            CommunityUtil.getDecimalStrByInt(rpConfigItem.getMin_amount(), 2));
                }
            }
        }

        textMoneyTotal.setText(CommunityUtil.getDecimalStrByInt(iInputTotalMoney, 2));

        showDynamicInfo(bInvalidNum, bInvalidMoney, errorMsg);

        setCommitStatus(bInvalidNum, bInvalidMoney);
    }

    private long getTextMoney(String money)
    {
        if (TextUtils.isEmpty(money))
        {
            return 0;
        }

        if (money.startsWith("."))
        {
            money = "0" + money;
        }

        if (money.contains(".") && !money.endsWith("."))
        {
            //避免浮点转换整型变小的错误
            money = money + "001";
            float fMoney = Float.parseFloat(money) * 100;
            return (long) fMoney;
        }
        else
        {
            if (money.endsWith("."))
            {
                money = money.substring(0, money.length() - 1);
            }
            return Long.parseLong(money) * 100;
        }
    }

    private void setCommitStatus(boolean bInvalidNum, boolean bInvalidMoney)
    {
        bCommitOk = false;
        if (iPayPlatform == CoomixPayManager.PAY_GOOME)
        {
            //零钱支付需要再判断零钱的额度够不够
            long payMoney = getTextMoney(textMoneyTotal.getText().toString());
            if (payMoney <= iGoomeBalance && iInputMoney > 0 && iInputNum > 0 && !bInvalidNum && !bInvalidMoney)
            {
                bCommitOk = true;
                textCommit.setBackgroundResource(R.drawable.rounded_corners_redpacket_commit_bg1);
            }
            else
            {
                bCommitOk = false;
                textCommit.setBackgroundResource(R.drawable.rounded_corners_redpacket_commit_bg2);
            }
        }
        else if (iPayPlatform == CoomixPayManager.PAY_WECHAT)
        {
            if (iInputMoney > 0 && iInputNum > 0 && !bInvalidNum && !bInvalidMoney)
            {
                bCommitOk = true;
                textCommit.setBackgroundResource(R.drawable.rounded_corners_redpacket_commit_bg1);
            }
            else
            {
                bCommitOk = false;
                textCommit.setBackgroundResource(R.drawable.rounded_corners_redpacket_commit_bg2);
            }
        }
        else
        {
            bCommitOk = false;
            textCommit.setBackgroundResource(R.drawable.rounded_corners_redpacket_commit_bg2);
        }
    }

    private void showDynamicInfo(boolean bInvalidNum, boolean bInvalidMoney, String errorMsg)
    {
        if (bInvalidNum)
        {
            textTopInfo.setVisibility(View.VISIBLE);
            textTopInfo.setText(errorMsg);
            //设置菜单文字颜色
            textRPNumTitle.setTextColor(getResources().getColor(R.color.redpacket_red));
            editNum.setTextColor(getResources().getColor(R.color.redpacket_red));
            textRPNumUnit.setTextColor(getResources().getColor(R.color.redpacket_red));

            textMoneyTitle.setTextColor(getResources().getColor(R.color.black));
            editMoney.setTextColor(getResources().getColor(R.color.black));
            textMoneyUnit.setTextColor(getResources().getColor(R.color.black));
        }
        else if (bInvalidMoney)
        {
            textTopInfo.setVisibility(View.VISIBLE);
            textTopInfo.setText(errorMsg);
            //设置菜单文字颜色
            textRPNumTitle.setTextColor(getResources().getColor(R.color.black));
            editNum.setTextColor(getResources().getColor(R.color.black));
            textRPNumUnit.setTextColor(getResources().getColor(R.color.black));

            textMoneyTitle.setTextColor(getResources().getColor(R.color.redpacket_red));
            editMoney.setTextColor(getResources().getColor(R.color.redpacket_red));
            textMoneyUnit.setTextColor(getResources().getColor(R.color.redpacket_red));
        }
        else
        {
            textTopInfo.setVisibility(View.INVISIBLE);
            //设置菜单文字颜色
            textRPNumTitle.setTextColor(getResources().getColor(R.color.black));
            editNum.setTextColor(getResources().getColor(R.color.black));
            textRPNumUnit.setTextColor(getResources().getColor(R.color.black));

            textMoneyTitle.setTextColor(getResources().getColor(R.color.black));
            editMoney.setTextColor(getResources().getColor(R.color.black));
            textMoneyUnit.setTextColor(getResources().getColor(R.color.black));
        }
    }

    private void setPayPlatform()
    {
        if (iPayPlatform == CoomixPayManager.PAY_WECHAT)
        {
            textWechatPay.setSelected(true);
            textPocketPay.setSelected(false);
            PreferenceUtil.commitInt(RedPacketConstant.RP_LAST_PAY_PLATFORM, CoomixPayManager.PAY_WECHAT);
        }
        else if (iPayPlatform == CoomixPayManager.PAY_GOOME)
        {
            textPocketPay.setSelected(true);
            textWechatPay.setSelected(false);
            PreferenceUtil.commitInt(RedPacketConstant.RP_LAST_PAY_PLATFORM, CoomixPayManager.PAY_GOOME);
        }
        setMoneyNumStatus(editMoney.getText().toString(), editNum.getText().toString());
    }

    private CreateRedPacketInfo getCreateData()
    {
        CreateRedPacketInfo createRedPacketInfo = new CreateRedPacketInfo();
        createRedPacketInfo.setDisplay_type(iCurDisplayType);
        createRedPacketInfo.setPacket_type(iRandomNormal);
        createRedPacketInfo.setAmount(getTextMoney(textMoneyTotal.getText().toString()));
        createRedPacketInfo.setPay_platform(iPayPlatform);
        coomixPayManager = new CoomixPayManager(this, iPayPlatform);
        createRedPacketInfo.setPay_manner(coomixPayManager.getPayManner());
        createRedPacketInfo.setPacket_num(iInputNum);
        if (!TextUtils.isEmpty(editHello.getText()))
        {
            createRedPacketInfo.setHello_words(editHello.getText().toString());
        }
        else
        {
            createRedPacketInfo.setHello_words(editHello.getHint().toString());
        }
        createRedPacketInfo.setAlloc_range(currRange);

        RedPacketExtendInfo extendInfo = new RedPacketExtendInfo();

        extendInfo.setCitycode(Constant.COMMUNITY_CITYCODE);

        if (iCurDisplayType == RedPacketConstant.RP_DISPLAY_CHAT_GROUP)
        {
            if(TextUtils.isEmpty(toSpecifiedId))
            {
                toSpecifiedId = "0";
            }
            extendInfo.setGroupid(toSpecifiedId);
        }
        else if (iCurDisplayType == RedPacketConstant.RP_DISPLAY_CHAT_SINGLE)
        {
            if(TextUtils.isEmpty(toSpecifiedId))
            {
                toSpecifiedId = "0";
            }
            extendInfo.setToid(toSpecifiedId); //发给某用户的用户id
        }
        else if (isCommunityRp())
        {
            if(!TextUtils.isEmpty(toSpecifiedId)) {
                extendInfo.setSectionId(toSpecifiedId); //板块id
            }
        }
        extendInfo.setCitycode(Constant.COMMUNITY_CITYCODE);
        extendInfo.setLoc_name(currLocName);
        //运营环境用地图选点，以及用户矫正但钱位置。没有就用定位到的当前位置
        if (redpacketLatLng != null)
        {
            extendInfo.setLat(String.valueOf(redpacketLatLng.latitude));
            extendInfo.setLng(String.valueOf(redpacketLatLng.longitude));
        }
        else
        {
            extendInfo.setLat(String.valueOf(GlobalParam.getInstance().get_lat()));
            extendInfo.setLng(String.valueOf(GlobalParam.getInstance().get_lng()));
        }

        createRedPacketInfo.setExtend_item(extendInfo);

        return createRedPacketInfo;
    }

    @Override
    public void onClick(View v)
    {
        switch (v.getId())
        {
            case R.id.textViewRPTypeChange:
                //切换红包类型
                if (iRandomNormal == RedPacketConstant.REDPACKET_TYPE_RANDOM)
                {
                    iRandomNormal = RedPacketConstant.REDPACKET_TYPE_NORMAL;
                }
                else if (iRandomNormal == RedPacketConstant.REDPACKET_TYPE_NORMAL)
                {
                    iRandomNormal = RedPacketConstant.REDPACKET_TYPE_RANDOM;
                }
                setConfigByType(iRandomNormal);
                resetNormalRandom();
                Animation animation = AnimationUtils.loadAnimation(this, R.anim.push_left_in);
                layoutMoney.setAnimation(animation);
                animation.start();
                break;

            case R.id.textViewWechatPay:
                //微信支付
                iPayPlatform = CoomixPayManager.PAY_WECHAT;
                setPayPlatform();
                break;

            case R.id.textViewPocketPay:
                //零钱支付
                if (iGoomeBalance != GOOME_BALANCE_DEF)
                {
                    textPocketPay.setText(getString(R.string.pay_type_pocket_balance,
                            CommunityUtil.getDecimalStrByInt(iGoomeBalance, 2)));
                }
                else
                {
                    requestBalance();
                }
                iPayPlatform = CoomixPayManager.PAY_GOOME;
                setPayPlatform();
                break;

            case R.id.textViewRPCommit:
                //提交，塞钱进红包
                //提交，塞钱进红包
                if(/*iCurDisplayType == RedPacketConstant.RP_DISPLAY_CHAT_SINGLE || */iCurDisplayType == RedPacketConstant.RP_DISPLAY_CHAT_GROUP)
                {
                    //聊天的红包直接发送出去
                    sendRedpacket();
                    return;
                }
                if (currRange == 0 || (currRange > 0 && CommunityUtil.isGPSOpened(this)))
                {
                    if (isCurrLocValid())
                    {
                        sendRedpacket();
                    }
                    else
                    {
                        showChangeToNormalRpDialog();
                    }
                }
                else
                {
                    showAskGpsDialog();
                }
                break;

            case R.id.redpacket_range:
                //选择红包范围
                if (CommunityUtil.isGPSOpened(this))
                {
                    showRange();
                }
                else
                {
                    showAskGpsDialog();
                }
                break;

//            case R.id.packRedPacketActionBar:
//                //运营选择发红包的地图位置
//                if (AllOnlineApp.getAppConfig().getRedpacket_select_map_onoff() == 0)
//                {
//                    //运营才放开此功能
//                    return;
//                }
//                ++iClickCount;
//                if (iClickCount >= 3)
//                {
//                    iClickCount = 0;
//                    bFirstIn = true;
//                    Intent intent = new Intent(CreateRedPacketActivity.this, TransitSearchPoiActivity.class);
//                    intent.putExtra(TransitSearchPoiActivity.SEARCH_TYPE, TransitSearchPoiActivity.SELECT_LOC);
//                    startActivityForResult(intent, REQUEST_MAP_CODE);
//                }
//                if (mHandler.hasMessages(0))
//                {
//                    mHandler.removeMessages(0);
//                }
//                mHandler.sendEmptyMessageDelayed(0, 3000);
//                break;

            case R.id.redpacket_loc_name:
                //选择我的位置的名称
                showNearbyLocName();
                break;
        }
    }

    private void sendRedpacket()
    {
        if (bCommitOk)
        {
            long money = getTextMoney(textMoneyTotal.getText().toString());
            if (iPayPlatform == CoomixPayManager.PAY_GOOME && iGoomeBalance != GOOME_BALANCE_DEF
                && iGoomeBalance < money)
            {
                Toast.makeText(CreateRedPacketActivity.this, R.string.pocket_balance_poor, Toast.LENGTH_SHORT).show();
                return;
            }
            sendCreateRedPacket();
        }
        else
        {
            if (iPayPlatform == CoomixPayManager.PAY_GOOME)
            {
                if (GOOME_BALANCE_DEF == iGoomeBalance)
                {
                    Toast.makeText(CreateRedPacketActivity.this, R.string.balance_unknown, Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private boolean isCurrLocValid()
    {
        AMapLocation currLocation = AllOnlineApp.getCurrentLocation();
        return !(GlobalParam.getInstance().get_lat() == 0 && GlobalParam.getInstance().get_lng() == 0);
    }

    private void showChangeToNormalRpDialog()
    {
        final AskDialog askChange = new AskDialog(this);
        askChange.setShowMsg(R.string.redpacket_location_weak);
        askChange.setNoText(R.string.cancel);
        askChange.setYesText(R.string.redpacket_location_weak_yes);
        askChange.setOnYesClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                currRange = 0;
                textRpRange.setText(getString(R.string.redpacket_range_none));
                askChange.dismiss();
                sendRedpacket();
            }
        });
        askChange.show();
    }

    private ArrayList<String> getRangeList()
    {
        if (ranges == null)
        {
            ranges = rpConfigItem.getAlloc_ranges();
        }
        ArrayList<String> list = new ArrayList<String>();
        for (int range : ranges)
        {
            list.add(RpLocationUtil.getRangeString(this, range));
        }
        return list;
    }

    //显示范围选择
    private void showRange()
    {
        final ArrayList<String> list = getRangeList();
        final PopListDialog popListDialog = new PopListDialog(CreateRedPacketActivity.this, list);
        popListDialog.show();
        popListDialog.setTextTitle(getString(R.string.redpacket_select_range));
        popListDialog.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                popListDialog.dismiss();
                currRange = ranges[position];
                setLocNameByCurrRange(list.get(position));
            }
        });
    }

    private void showAskGpsDialog()
    {
        final AskDialog askGps = new AskDialog(this);
        askGps.setShowMsg(R.string.redpacket_range_gps_tip);
        askGps.setNoText(R.string.redpacket_range_gps_no);
        askGps.setYesText(R.string.redpacket_range_gps_yes);
        askGps.setOnYesClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivityForResult(intent, 0);
                askGps.dismiss();
            }
        });
        askGps.show();
    }

    @Override
    public void finish()
    {
        super.finish();
        if (iCurDisplayType == RedPacketConstant.RP_DISPLAY_CHAT_GROUP || iCurDisplayType == RedPacketConstant.RP_DISPLAY_CHAT_SINGLE)
        {
            overridePendingTransition(0, R.anim.push_bottom_out);
        }
    }

    private void showWaitInfoDialog()
    {
        progressDialogEx = new ProgressDialogEx(this);
        progressDialogEx.setAutoDismiss(false);
        progressDialogEx.setCancelOnTouchOutside(false);

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

    private void sendCreateRedPacket()
    {
        showWaitInfoDialog();
        iRequestCreateRP = serviceAdapter.createRedPacket(this.hashCode(), CommonUtil.getTicket(), getCreateData());
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        if (!bFirstIn || PayResultManager.getInstance().isRedPacketPayedOk())
        {
            //微信支付红包成功，或者第二次进入（微信支付后没有返回），去查询未发送的红包
            requestUnsendRedPacket();
            PayResultManager.getInstance().setRedPacketPayedOk(false);
        }
        bFirstIn = false;
    }

    private void requestUnsendRedPacket()
    {
        showWaitInfoDialog();
        iGetUnsendRedPacket = serviceAdapter.getUnsendRedPacketsByType(this.hashCode(), CommonUtil.getTicket(),
                iCurDisplayType);
    }

    private void setConfigByType(int type)
    {
        if (redPacketConfigs != null)
        {
            rpConfigItem = redPacketConfigs.getRedPacketConfigItemByType(type);
        }
        else
        {
            rpConfigItem = new RedPacketConfigItem();
        }

        if (rpConfigItem == null)
        {
            rpConfigItem = new RedPacketConfigItem();
        }

        setRedPacketExplain();

        String hello = rpConfigItem.getHello_words();

        if (!TextUtils.isEmpty(hello))
        {
            editHello.setHint(hello);
        }

        ranges = rpConfigItem.getAlloc_ranges();
    }

    @Override
    public void callback(int messageId, Result result)
    {
        if (result.statusCode == Result.ERROR_NETWORK)
        {
            dismissWaitDialog();
            Toast.makeText(this, R.string.network_error, Toast.LENGTH_SHORT).show();
        }
        else if (messageId == iRequestRPConfig)
        {
            if (result != null && result.success)
            {
                redPacketConfigs = (RedPacketConfig) result.mResult;
                setConfigByType(iRandomNormal);
            }
            else if (result.statusCode == Result.ERRCODE_LONGIN_TWO_DEVICES)
            {
                restartCommunityOnly(CreateRedPacketActivity.this);
                return;
            }else if(result.statusCode == Result.ERR_INVALID_SESSION) {
                if(handler != null) {
                    handler.sendEmptyMessage(WEIXIN_UNBIND_MSG);
                }
                return;
            }
            else
            {
                Toast.makeText(CreateRedPacketActivity.this, R.string.leave_group_error, Toast.LENGTH_SHORT).show();
            }
        }
        else if (iRequestCreateRP == messageId
                 && result.apiCode == Constant.FM_APIID_CREATE_PAY_REDPACKET)
        {
            dismissWaitDialog();
            if (result.success &&  result.mResult != null &&  result.mResult instanceof CoomixPayRsp)
            {
                CoomixPayRsp coomixPayRsp = (CoomixPayRsp)  result.mResult;
                if (coomixPayRsp.getError() != null
                    && coomixPayRsp.getError().getCode() == Error.ERRCODE_REDPACKET_PARAM_INVALID)
                {
                    Toast.makeText(CreateRedPacketActivity.this, coomixPayRsp.getError().getMsg(), Toast.LENGTH_SHORT)
                         .show();
                }
                else
                {
                    sCreateRedPacketId = coomixPayRsp.getRedpacket_id();
                    if (iPayPlatform == CoomixPayManager.PAY_GOOME)
                    {
                        //零钱支付成功，去查询未发送红包/零钱支付在红包创建的时候就自动支付了
                        requestUnsendRedPacket();
                    }
                    else
                    {
                        //其他支付方式
                        PayResultManager.getInstance().setPayOrderType(ICoomixPay.ORDER_FROM.FROM_REDPACKET);
                        coomixPayManager.pay(coomixPayRsp);
                    }
                }
            }
            else
            {
                Toast.makeText(CreateRedPacketActivity.this, R.string.pay_error, Toast.LENGTH_SHORT).show();
            }
        }
        else if (iRequestBalance == messageId)
        {
            if (result.mResult != null && result.success && result.mResult instanceof BalanceInfo)
            {
                BalanceInfo balanceInfo = (BalanceInfo) result.mResult;
                iGoomeBalance = balanceInfo.getBalance();
                textPocketPay.setText(getString(R.string.pay_type_pocket_balance,
                        CommunityUtil.getDecimalStrByInt(iGoomeBalance, 2)));

            }
            else if (result.statusCode == Result.ERRCODE_LONGIN_TWO_DEVICES || result.statusCode == Result.ERR_INVALID_SESSION)
            {
                return;
            }
            else
            {
                Toast.makeText(CreateRedPacketActivity.this, R.string.leave_group_error, Toast.LENGTH_SHORT).show();
            }
        }
        else if (iGetUnsendRedPacket == messageId)
        {
            dismissWaitDialog();
            if (result.success && result.mResult != null && result.mResult instanceof UnsendRedPackets)
            {
                //获取全部未发送的红包，如果是刚新建的红包则可以发出去抢了
                UnsendRedPackets unsendRedPackets = (UnsendRedPackets) result.mResult;
                ArrayList<RedPacketInfo> redPacketInfos = unsendRedPackets.getRedpackets();
                if (redPacketInfos != null)
                {
                    for (RedPacketInfo redPacketInfo : redPacketInfos)
                    {
                        if (redPacketInfo != null && redPacketInfo.getRedpacket_id() != null && redPacketInfo
                                .getRedpacket_id().equals(sCreateRedPacketId))
                        {
                            sendBroadCastSendRedPacket(redPacketInfo);
                            break;
                        }
                    }
                }
            }
            else
            {
                Toast.makeText(CreateRedPacketActivity.this, R.string.leave_group_error, Toast.LENGTH_SHORT).show();
            }
        }
    }
    private Handler handler = new Handler(){
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case WEIXIN_UNBIND_MSG:
                {
                    AppUtil.showWeinxinUndindDialog(AllOnlineApp.mApp.getApplicationContext(),1);
                    break;
                }
                default:
                    break;
            }
        }
    };
    private void restartCommunityOnly(final Context context){
        Runnable myRunnable = new Runnable() {
            @Override
            public void run()
            {
                Result resultLogin = AllOnlineApp.mApiClient.login(AllOnlineApp.sToken.access_token,
                        AllOnlineApp.channelId(context), AllOnlineApp.sAccount);
                if(resultLogin != null && resultLogin.success){
                    if (isWechatBinded()) {
                        iRequestRPConfig = serviceAdapter.getRedPacketConfig(this.hashCode(), CommonUtil.getTicket(), iCurDisplayType);
                    }
                }
            }
        };
        new Thread(myRunnable).start();
    }
    private boolean isWechatBinded() {
//        CommunityUser user = AllOnlineApp.getCommunityUser();
//        if (!user.isWechatBinded()) {
//            return false;
//        } else {
//            return true;
//        }
        return true;
    }
    private void setDefaultRpRange()
    {
        if (!CommunityUtil.isGPSOpened(this))
        {
            defRange = 0;
        }
        currRange = defRange;

        setLocNameByCurrRange(RpLocationUtil.getRangeString(this, currRange));
    }

    private void setLocNameByCurrRange(String rpRange)
    {
        if (currRange == 0)
        {
            textRpRange.setText(getString(R.string.redpacket_range_none));
            llRpLocName.setVisibility(View.GONE);
            return;
        }
        else
        {
            //textRpRange.setText(getString(R.string.redpacket_range_limit, list.get(position)));
            textRpRange.setText(rpRange);
            llRpLocName.setVisibility(View.VISIBLE);
        }

        if (redpacketLatLng == null || (redpacketLatLng.latitude == 0 && redpacketLatLng.longitude == 0))
        {
            //解析当前地址，运营选择了的或者用户所在的地点
            if (AllOnlineApp.getAppConfig().getRedpacket_select_map_onoff() == 0 || !bSelectedNameOk)
            {
                AMapLocation currLocation = AllOnlineApp.getCurrentLocation();
                redpacketLatLng = new LatLng(currLocation.getLatitude(), currLocation.getLongitude());
                //startLocation();
            }
            textRpLocName.setHint(R.string.loading);

            if (redpacketLatLng.latitude == 0 && redpacketLatLng.longitude == 0)
            {
                parseLocName = true;
            }
            else
            {
                searchNearbyLocationList(redpacketLatLng.latitude, redpacketLatLng.longitude);
            }
        }
    }
    public AMapLocationClient mLocationClient = null;
    private AMapLocationClientOption mLocationClientOption;
    private LocationListener mLocationListener;
    private void startLocation()
    {
        mLocationClient = new AMapLocationClient(getApplicationContext());
        if(mLocationClient != null) {
            mLocationListener = new LocationListener();
            if(mLocationListener != null){
                mLocationClient.setLocationListener(mLocationListener);
            }
            mLocationClient.setLocationOption(getLocationClientOption());
            mLocationClient.startLocation();
        }
    }
    private class LocationListener implements AMapLocationListener
    {

        @SuppressLint("SimpleDateFormat")
        @Override
        public void onLocationChanged(AMapLocation location)
        {
            if (location != null)
            {
                AllOnlineApp.setCurrentLocation(location);
                redpacketLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                if (redpacketLatLng.latitude == 0 && redpacketLatLng.longitude == 0)
                {
                    parseLocName = true;
                }
                else
                {
                    searchNearbyLocationList(redpacketLatLng.latitude, redpacketLatLng.longitude);
                }
            }
        }
    }
    /**
     * 获取特定的LocationClientOption
     */
    private AMapLocationClientOption getLocationClientOption()
    {
        mLocationClientOption = new AMapLocationClientOption();
        mLocationClientOption.setGpsFirst(true);
        mLocationClientOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
        mLocationClientOption.setNeedAddress(true);
        return mLocationClientOption;
    }
    /**
     * 获取到了未发送的红包就是刚新建的红包，然后在EaseChatFragment.java中的广播监听发送出去
     */
    private void sendBroadCastSendRedPacket(RedPacketInfo redPacketInfo)
    {
        Intent intent = new Intent(RedPacketConstant.REDPACKET_WILL_SEND_ACTION);
        redPacketInfo.setToChatId(toSpecifiedId);
        intent.putExtra(RedPacketConstant.REDPACKET_DATA, redPacketInfo);
        sendBroadcast(intent);
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_MAP_CODE && resultCode == RESULT_OK)
        {
            LocationInfo selectedLocInfo = AllOnlineApp.getInstantce().getSelectLocationInfo();
            if (selectedLocInfo != null)
            {
                currLocName = selectedLocInfo.getName();
                if (!TextUtils.isEmpty(currLocName) && !currLocName.equals(getString(R.string.my_location)))
                {
                    bSelectedNameOk = true;
                }
                else
                {
                    bSelectedNameOk = false;
                }

                textRpLocName.setText(currLocName);
                redpacketLatLng = new LatLng(selectedLocInfo.getLatitude(), selectedLocInfo.getLongitude());
                //解析这些list，保证在选择附近的地名时候已经有数据
                searchNearbyLocationList(redpacketLatLng.latitude, redpacketLatLng.longitude);
                //Toast.makeText(CreateRedPacketActivity.this, "已选择位置:" + currLocName, Toast.LENGTH_SHORT).show();
            }
            else
            {
                Toast.makeText(CreateRedPacketActivity.this, "选择位置失败,请重新选择", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private List<PoiItem> localLocationList = null;

    private void searchNearbyLocationList(double latitude, double longitude)
    {
        GeocodeSearch mGeocoderSearch = new GeocodeSearch(this);
        LatLonPoint latLonPoint = new LatLonPoint(latitude, longitude);
        RegeocodeQuery query = new RegeocodeQuery(latLonPoint, AllOnlineApp.getAppConfig().getRedpacket_poi_range(),
                GeocodeSearch.AMAP);
        mGeocoderSearch.setOnGeocodeSearchListener(new GeocodeSearch.OnGeocodeSearchListener()
        {
            @Override
            public void onRegeocodeSearched(RegeocodeResult result, int rCode)
            {
                if (rCode == 0/*AMapException.CODE_AMAP_SUCCESS*/)
                {
                    if (result != null && result.getRegeocodeAddress() != null
                        && result.getRegeocodeAddress().getFormatAddress() != null)
                    {
                        //mLocationClient.stopLocation();
                        localLocationList = result.getRegeocodeAddress().getPois();
                        if (!bSelectedNameOk && localLocationList != null && localLocationList.size() > 0)
                        {
                            currLocName = localLocationList.get(0).getTitle();
                            textRpLocName.setText(currLocName);
                        }
                    }
                }
            }

            @Override
            public void onGeocodeSearched(GeocodeResult geocodeResult, int i)
            {

            }
        });
        // 设置异步逆地理编码请求
        mGeocoderSearch.getFromLocationAsyn(query);
    }

    private void showNearbyLocName()
    {
        if (localLocationList == null || localLocationList.size() <= 0)
        {
            return;
        }
        if (popDialog == null)
        {
            popDialog = new PopNearbyListDialog(CreateRedPacketActivity.this, localLocationList);
            popDialog.setOnItemClickListener(new AdapterView.OnItemClickListener()
            {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id)
                {
                    if (localLocationList == null || position < 0 || position > localLocationList.size() - 1)
                    {
                        return;
                    }
                    popDialog.dismiss();
                    popDialog.setSelect(position);
                    currLocName = localLocationList.get(position).getTitle();
                    LatLonPoint latLonPoint = localLocationList.get(position).getLatLonPoint();
                    if (latLonPoint != null)
                    {
                        redpacketLatLng = new LatLng(latLonPoint.getLatitude(), latLonPoint.getLongitude());
                    }
                    textRpLocName.setText(currLocName);
                }
            });
        }
        popDialog.show();
    }

    @Override
    public void onLocationChange(AMapLocation location)
    {
        if (parseLocName)
        {
            parseLocName = false;
            searchNearbyLocationList(location.getLatitude(), location.getLongitude());
        }
    }

}
