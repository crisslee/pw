package com.coomix.app.all.ui.activity;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.coomix.app.all.AllOnlineApp;
import com.coomix.app.all.Constant;
import com.coomix.app.all.R;
import com.coomix.app.all.model.bean.ActOrderInfo;
import com.coomix.app.all.model.bean.ActPrice;
import com.coomix.app.all.model.bean.ActRegisterBean;
import com.coomix.app.all.model.bean.CommunityActDetail;
import com.coomix.app.all.model.bean.CommunitySignedInfo;
import com.coomix.app.all.log.GoomeLog;
import com.coomix.app.all.service.ServiceAdapter;
import com.coomix.app.all.service.ServiceAdapter.ServiceAdapterCallback;
import com.coomix.app.all.ui.web.DisclaimerActivity;
import com.coomix.app.all.util.StringUtil;
import com.coomix.app.all.widget.MyActionbar;
import com.coomix.app.all.dialog.RegisterInfoDialog;
import com.coomix.app.all.dialog.RegisterInfoDialog.OnRegisterCancelListener;
import com.coomix.app.framework.app.Result;
import com.coomix.app.framework.util.CommonUtil;
import com.coomix.app.framework.util.PreferenceUtil;
import com.coomix.app.all.ui.base.BaseActivity;
import com.coomix.app.redpacket.util.RedPacketConstant;
import com.coomix.app.all.util.CommunityUtil;
import com.coomix.app.all.util.ConfigUtil;
import net.goome.im.GMError;
import net.goome.im.GMValueCallBack;
import net.goome.im.chat.GMChatRoom;
import net.goome.im.chat.GMChatRoomManager;
import net.goome.im.chat.GMClient;

/**
 * ActRegisterActivity.java
 *
 * @author shishenglong
 * @since 2016年8月19日 上午11:39:56
 */
public class ActRegisterActivity extends BaseActivity implements OnClickListener, OnRegisterCancelListener,
    ServiceAdapterCallback {

    public static final String ACT_DETAIL_INFO = "act_detailinfo";
    private EditText editName, editTel, editQQWX, editAddr;
    private TextView textCommit;
    private ImageView imageCheck;
    private boolean bNameInputed = false;
    private boolean bTelInputed = false;
    // private boolean bQQInputed = false;
    private boolean bChecked = true;
    private RegisterInfoDialog dialogInfo = null;
    //private ServerRequestController mServiceAdaper = null;
    private ServiceAdapter serviceAdapter;
    public static final int REG_CANCEL = 0;// 取消报名
    public static final int REG_SIGN = 1;// 活动报名
    public static final int REG_EDIT = 2;// 编辑报名
    private CommunityActDetail commActDetailInfo = null;
    private int iRequestId = -1;
    private int registerStatus = REG_SIGN;
    private CommunitySignedInfo singnedInfo = null;
    public static boolean bResume = false;
    /** 姓名限制在20个字符以内 */
    private final int MAX_NAME_CHAR_LENGTH = 20;
    private ActRegisterBean mActRegisterBean = new ActRegisterBean();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_act_register);

        if (getIntent() != null) {
            commActDetailInfo = (CommunityActDetail) getIntent().getSerializableExtra(ACT_DETAIL_INFO);
        }

        initViews();

        // mServiceAdaper = ServerRequestController.getInstance(this);
        // mServiceAdaper.registerServiceCallBack(this);
        serviceAdapter = ServiceAdapter.getInstance(this);
        serviceAdapter.registerServiceCallBack(this);

        loadUiData();

        if (!TextUtils.isEmpty(editName.getText().toString())) {
            editName.setSelection(editName.getText().toString().length());
        }
        initData();
    }

    public void initData() {
        if (commActDetailInfo != null && commActDetailInfo.getSigned() == 1) {
            // 获取用户已经报名的信息
            sendOrRequestData(false, 0, commActDetailInfo.getId(), null, null, null, null);
        }
    }

    private void updateUiData() {
        editName.setText(mActRegisterBean.getName());
        editTel.setText(mActRegisterBean.getPhone());
        editQQWX.setText(mActRegisterBean.getWechat());
        editAddr.setText(mActRegisterBean.getAddr());
    }

    private void loadUiData() {
        Object obj = com.coomix.app.framework.util.PreferenceUtil.getObj(ActRegisterBean.TAG);
        if (obj != null) {
            mActRegisterBean = (ActRegisterBean) obj;
            updateUiData();
        }
    }

    private void saveUiData() {
        mActRegisterBean.setName(editName.getText().toString());
        mActRegisterBean.setPhone(editTel.getText().toString());
        mActRegisterBean.setWechat(editQQWX.getText().toString());
        mActRegisterBean.setAddr(editAddr.getText().toString());
        com.coomix.app.framework.util.PreferenceUtil.commitObj(ActRegisterBean.TAG, mActRegisterBean);
    }

    @Override
    protected void onDestroy() {
        //if (mServiceAdaper != null)
        //{
        //  mServiceAdaper.unregisterServiceCallBack(this);
        //}
        if (serviceAdapter != null) {
            serviceAdapter.unregisterServiceCallBack(this);
        }
        super.onDestroy();
    }

    private void initViews() {
        editName = (EditText) findViewById(R.id.editTextName);
        editTel = (EditText) findViewById(R.id.editTextTel);
        editQQWX = (EditText) findViewById(R.id.editTextQQ);
        editAddr = (EditText) findViewById(R.id.editTextAddr);
        textCommit = (TextView) findViewById(R.id.textViewCommit);
        imageCheck = (ImageView) findViewById(R.id.checkImageAgreement);

        editName.addTextChangedListener(textChangeListener);
        editTel.addTextChangedListener(textChangeListener);
        editQQWX.addTextChangedListener(textChangeListener);
        editAddr.addTextChangedListener(textChangeListener);

        textCommit.setOnClickListener(this);
        findViewById(R.id.textViewLiability).setOnClickListener(this);
        imageCheck.setOnClickListener(this);
        bChecked = true;
        imageCheck.setSelected(true);

        textCommit.setBackgroundColor(getResources().getColor(R.color.register_gray));

        MyActionbar actionbar = (MyActionbar) findViewById(R.id.mineActionbar);
        actionbar.initActionbar(true, R.string.activity_register, 0, 0);

        dialogInfo = new RegisterInfoDialog(this, RegisterInfoDialog.LOADING);
        dialogInfo.setOnRegisterCancelListener(this);

        if (commActDetailInfo != null && commActDetailInfo.getSigned() == 1) {
            registerStatus = REG_CANCEL;
            bChecked = true;
            imageCheck.setSelected(true);
            textCommit.setBackgroundColor(getResources().getColor(R.color.register_blue));
            textCommit.setText(R.string.register_cancel);
        } else {
            if (CommonUtil.isLogin()) {
                String tel = PreferenceUtil.getString(Constant.PREFERENCE_USER_PHONE, "");
                if (CommunityUtil.isMobileNO(tel)) {
                    editTel.setText(tel);
                    bTelInputed = true;
                }
            }
        }
    }

    private TextWatcher textChangeListener = new TextWatcher() {

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            //if (editName.isFocused())
            {
                if (TextUtils.isEmpty(editName.getText().toString())) {
                    bNameInputed = false;
                } else {
                    bNameInputed = true;
                }
            }
            //else if (editTel.isFocused())
            {
                if (TextUtils.isEmpty(editTel.getText().toString())) {
                    bTelInputed = false;
                } else {
                    bTelInputed = true;
                }
            }
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            checkCommitStatus();
        }
    };

    private void checkCommitStatus() {
        if (bChecked && bNameInputed && bTelInputed) {
            textCommit.setText(R.string.commit);
            if (isValidInput(false)) {
                textCommit.setBackgroundColor(getResources().getColor(R.color.register_blue));
            } else {
                textCommit.setBackgroundColor(getResources().getColor(R.color.register_gray));
            }

            if (registerStatus == REG_CANCEL || registerStatus == REG_EDIT) {
                registerStatus = REG_EDIT;
                if (singnedInfo == null) {
                    return;
                }
                if (!editName.getText().toString().equals(singnedInfo.getName())) {
                    return;
                }
                if (!editTel.getText().toString().equals(singnedInfo.getTel())) {
                    return;
                }
                if (!editQQWX.getText().toString().equals(singnedInfo.getQqorwx())) {
                    return;
                }
                if (!editAddr.getText().toString().equals(singnedInfo.getAddr())) {
                    return;
                }
                registerStatus = REG_CANCEL;
                textCommit.setBackgroundColor(getResources().getColor(R.color.register_blue));
                textCommit.setText(R.string.register_cancel);
            }
        } else {
            textCommit.setBackgroundColor(getResources().getColor(R.color.register_gray));
            textCommit.setText(R.string.commit);
        }
    }

    private void commitOrCancel() {
        if (!isValidInput(true)) {
            return;
        }

        if (dialogInfo == null) {
            dialogInfo = new RegisterInfoDialog(this, RegisterInfoDialog.COMMIT);
        }
        if (registerStatus == REG_CANCEL) {
            // 取消报名需要弹窗供用户确认
            dialogInfo.setDialogType(RegisterInfoDialog.ASK);
            dialogInfo.setOnRegisterCancelListener(this);
            dialogInfo.show();
            return;
        } else {
            dialogInfo.setDialogType(RegisterInfoDialog.COMMIT);
            dialogInfo.show();
        }

        if (commActDetailInfo != null) {
            String name = editName.getText().toString().trim();
            String tel = editTel.getText().toString().trim();
            String qqorwx = editQQWX.getText().toString().trim();
            String addr = editAddr.getText().toString().trim();
            sendOrRequestData(true, registerStatus, commActDetailInfo.getId(), name, tel, qqorwx, addr);
        }
    }

    private boolean isValidInput(boolean showToast) {
        String name = editName.getText().toString().trim();
        String tel = editTel.getText().toString().trim();
        String qqorwx = editQQWX.getText().toString().trim();
        String addr = editAddr.getText().toString().trim();

        if (!CommunityUtil.isName(name)) {
            if (showToast) {
                Toast.makeText(this, R.string.act_name_error, Toast.LENGTH_SHORT).show();
            }
            return false;
        } else if (!CommunityUtil.isLengthOk(name, MAX_NAME_CHAR_LENGTH)) {
            if (showToast) {
                Toast.makeText(this, "姓名太长了，请重新输入", Toast.LENGTH_SHORT).show();
            }
            return false;
        }

        if (!CommunityUtil.isMobileNO(tel)) {
            if (showToast) {
                Toast.makeText(this, R.string.act_tel_error, Toast.LENGTH_SHORT).show();
            }
            return false;
        }

        if (!TextUtils.isEmpty(qqorwx) && !CommunityUtil.isQQOrWX(qqorwx)) {
            if (showToast) {
                Toast.makeText(this, R.string.act_wx_error, Toast.LENGTH_SHORT).show();
            }
            return false;
        }

        if (TextUtils.isEmpty(addr)) {
            if (showToast) {
                Toast.makeText(this, R.string.act_addr_error, Toast.LENGTH_SHORT).show();
            }
            return false;
        }

        if (!bChecked) {
            if (showToast) {
                Toast.makeText(this, R.string.act_disclaimer_unselect, Toast.LENGTH_SHORT).show();
            }
            return false;
        }
        return true;
    }

    private String getUserTicket() {
        if (!CommonUtil.isLogin()) {
            Toast.makeText(this, "您尚未登录", Toast.LENGTH_SHORT).show();
            CommonUtil.goToLogin(ActRegisterActivity.this);
            return null;
        }
        if (!StringUtil.isTrimEmpty(CommonUtil.getTicket())) {
            return CommonUtil.getTicket();
        } else {
            Toast.makeText(this, "您尚未登录", Toast.LENGTH_SHORT).show();
            CommonUtil.goToLogin(ActRegisterActivity.this);
        }
        return null;
    }

    private void sendOrRequestData(boolean bSend, int type, int aid, String name, String tel, String qqorwx,
        String addr) {
        String ticket = getUserTicket();
        if (TextUtils.isEmpty(ticket)) {
            Toast.makeText(this, "您尚未登录", Toast.LENGTH_SHORT).show();
            return;
        }
        if (bSend) {
            // 发送数据
            iRequestId = serviceAdapter.sendSignInfo(this.hashCode(), ticket, type, aid, -1, name, tel, qqorwx,
                addr, null, 0, 0);
        } else {
            // 请求数据
            try {
                dialogInfo.setDialogType(RegisterInfoDialog.LOADING);
                dialogInfo.show();
            } catch (Exception e) {
                e.printStackTrace();
            }
            iRequestId = serviceAdapter.getMySignedInfo(this.hashCode(), ticket, aid);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.textViewCommit:
                commitOrCancel();
                break;

            case R.id.textViewLiability:
                // 显示免责声明
                Intent intent = new Intent(ActRegisterActivity.this, DisclaimerActivity.class);
                startActivity(intent);
                break;

            case R.id.checkImageAgreement:
                bChecked = !bChecked;
                if (bChecked) {
                    imageCheck.setSelected(true);
                } else {
                    imageCheck.setSelected(false);
                }
                checkCommitStatus();
                break;
        }
    }

    @Override
    public void onRegisterCancel() {
        // 取消报名
        if (commActDetailInfo != null) {
            sendOrRequestData(true, registerStatus, commActDetailInfo.getId(), editName.getText().toString().trim(),
                editTel.getText().toString().trim(), editQQWX.getText().toString().trim(),
                editAddr.getText().toString().trim());
        }
    }

    @Override
    public void callback(int messageId, Result result) {
        if (result == null) {
            dialogInfo.dismiss();
            return;
        }
        try {
            if (result.errcode == Result.ERROR_NETWORK) {
                dialogInfo.dismiss();
                Toast.makeText(this, R.string.network_error, Toast.LENGTH_SHORT).show();
            } else if (iRequestId == messageId && result.apiCode == Constant.FM_APIID_sendSignInfo) {
                Log.i("ActRegisterActivity", "1111111111111111111111111111");
                CommActDetailActivity.bReRequestNewData = true;
                if (result.success) {
                    if (result.mResult != null && result.mResult instanceof ActOrderInfo) {
                        Log.i("ActRegisterActivity", "22222222222222222222222222222");
                        ActOrderInfo hdUser = (ActOrderInfo) result.mResult;
                        parseReturnSigned(hdUser);
                    } else {
                        Log.i("ActRegisterActivity", "333333333333333333333333333333333");
                        dialogInfo.dismiss();
                        parseReturnSigned(null);
                        //Toast.makeText(ActRegisterActivity.this, "报名成功，但返回数据有误", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    switch (result.errcode) {
                        case Result.ERRCODE_ACT_HAVE_SIGNED:
                            dialogInfo.dismiss();
                            Toast.makeText(ActRegisterActivity.this, "您已经报过名了", Toast.LENGTH_SHORT).show();
                            break;
                        case Result.ERRCODE_ACT_SIGN_STOPPED:
                            dialogInfo.dismiss();
                            Toast.makeText(ActRegisterActivity.this, "报名已截止", Toast.LENGTH_SHORT).show();
                            break;
                        case Result.ERRCODE_ACT_SIGNED_FULL:
                            dialogInfo.dismiss();
                            Toast.makeText(ActRegisterActivity.this, "活动已满员", Toast.LENGTH_SHORT).show();
                            finish();
                            break;
                        default:
                            dialogInfo.dismiss();
                            String str = "报名失败，";
                            if (registerStatus == REG_EDIT) {
                                str = "编辑失败，";
                            } else if (registerStatus == REG_CANCEL) {
                                str = "取消失败，";
                            }
                            Toast.makeText(ActRegisterActivity.this, str + result.msg, Toast.LENGTH_SHORT).show();
                            break;
                    }
                }
            } else if (iRequestId == messageId && result.apiCode == Constant.FM_APIID_getMySignedInfo) {
                dialogInfo.dismiss();
                if (result.mResult == null) {
                    Toast.makeText(ActRegisterActivity.this, "获取已报名信息失败", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (result.mResult instanceof CommunitySignedInfo) {
                    singnedInfo = (CommunitySignedInfo) result.mResult;
                    parseMySignedInfo(singnedInfo);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            if (dialogInfo != null && dialogInfo.isShowing()) {
                dialogInfo.dismiss();
            }
        }
    }

    private void parseReturnSigned(ActOrderInfo actOrderInfo) {
        if (actOrderInfo != null) {
            if (registerStatus == REG_CANCEL) {
                // registerStatus = REG_SIGN;
                dialogInfo.dismiss();
                showToast(getString(R.string.register_cancel_success));
                finish();
            } else if (registerStatus == REG_EDIT) {
                dialogInfo.dismiss();
                showToast(getString(R.string.register_edit_success));
                finish();
            } else {
                dialogInfo.dismiss();
                if (ConfigUtil.isUseGMIMGroup()) {
                    //goome群聊
                    if (GMClient.getInstance().isInited() && !TextUtils.isEmpty(actOrderInfo.getChatroom_id())) {
                        final long chatRoomId = Long.parseLong(actOrderInfo.getChatroom_id());
                        if (chatRoomId > 0) {
                            GMClient.getInstance().chatroomManager().joinChatRoom(chatRoomId,
                                AllOnlineApp.getCurrentLocation().getLongitude(),
                                AllOnlineApp.getCurrentLocation().getLatitude(),
                                AllOnlineApp.sToken.name, "",
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
                            GoomeLog.getInstance()
                                .logE("ActRegisterActivity", "Signed successed. Chat room id: " + chatRoomId, 0);
                        }
                    } else {
                        GoomeLog.getInstance()
                            .logE("ActRegisterActivity",
                                "Signed successed. GmClient not init, chat room id: " + actOrderInfo.getChatroom_id(),
                                0);
                    }
                } else {
                    showToast(getString(R.string.register_success));
                }

                // // 报名成功，获取到环信用户密码，延时3s后自动调用登录接口
                // if (MainActivity.mActivity != null)
                // {
                //  MainActivity.mActivity.setEmUser(hdUser, 3000);
                // }
                // else if (BusOnlineApp.getUser() != null)
                // {
                //  BusOnlineApp.getUser().setEmUser(hdUser);
                //  PreferenceUtil.commitString(Constant.PREFERENCE_USER_INFO, new Gson().toJson(BusOnlineApp.getUser(), User.class));
                // }
                if (actOrderInfo.getOrder_id() > 0) {
                    goToOrderActivity(actOrderInfo.getOrder_id());
                }
            }
        } else {
            showToast(R.string.register_fail);
        }
    }

    //报名加群成功后就初始化群的基本信息
    private void initChatroomInfo(final long roomId) {
        final GMChatRoomManager chatroomMgr = GMClient.getInstance().chatroomManager();
        if (chatroomMgr == null) {
            return;
        }
        GMChatRoom room = chatroomMgr.getChatroomInfoFromDB(roomId);
        if (room == null) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    chatroomMgr.getChatroomSpecificationFromServerWithId(roomId, new GMValueCallBack<GMChatRoom>() {
                        @Override
                        public void onSuccess(GMChatRoom room) {
                            chatroomMgr.getPushServiceEnableFromServer(roomId, new GMValueCallBack<Boolean>() {
                                @Override
                                public void onSuccess(Boolean aBoolean) {
                                }

                                @Override
                                public void onError(GMError gmError) {
                                }
                            });
                        }

                        @Override
                        public void onError(GMError gmError) {
                        }
                    });
                }
            }).start();
        }
    }

    private void goToOrderActivity(long iOrderId) {
        if (commActDetailInfo != null) {
            ActPrice actPrice = commActDetailInfo.getPrice();
            if (actPrice != null) {
                if (actPrice.getType() == ActPrice.PRICE_FASTEN) {
                    //固定价格
                    if (actPrice.getFixed_price() > 0) {
                        //去支付
                        Intent intent = new Intent(ActRegisterActivity.this, MyOrderInfoActivity.class);
                        //intent.setFlags(Intent.FLAG_ACTIVITY_FORWARD_RESULT);
                        intent.putExtra(Constant.NATIVE_ACTIVITY_ID, commActDetailInfo.getId());
                        intent.putExtra(MyOrderInfoActivity.ORDER_DETAIL_OR_PAYING, MyOrderInfoActivity.ORDER_PAYING);
                        intent.putExtra(MyOrderInfoActivity.ORDER_ID, iOrderId);
                        intent.putExtra(MyOrderInfoActivity.ORDER_FROM_SIGNED, true);
                        startActivity(intent);
                        finish();
                        return;
                    }
                }
            }

            //线下支付或免费，不需要支付，直接去报了名的活动详情
            Intent intent = new Intent(ActRegisterActivity.this, MyOrderInfoActivity.class);
            intent.putExtra(Constant.NATIVE_ACTIVITY_ID, commActDetailInfo.getId());
            intent.putExtra(MyOrderInfoActivity.ORDER_DETAIL_OR_PAYING, MyOrderInfoActivity.ORDER_DETAIL);
            intent.putExtra(MyOrderInfoActivity.ORDER_ID, iOrderId);
            startActivity(intent);
            finish();
        }
    }

    private void showToast(int msgId) {
        Toast.makeText(this, msgId, Toast.LENGTH_SHORT).show();
    }

    private void parseMySignedInfo(CommunitySignedInfo singnedInfo) {
        if (singnedInfo != null) {
            if (!TextUtils.isEmpty(singnedInfo.getName())) {
                editName.setText(singnedInfo.getName());
                editName.setSelection(singnedInfo.getName().length());
                bNameInputed = true;
            }
            if (!TextUtils.isEmpty(singnedInfo.getTel())) {
                editTel.setText(singnedInfo.getTel());
                editTel.setSelection(singnedInfo.getTel().length());
                bTelInputed = true;
            }
            if (!TextUtils.isEmpty(singnedInfo.getQqorwx())) {
                editQQWX.setText(singnedInfo.getQqorwx());
                editQQWX.setSelection(singnedInfo.getQqorwx().length());
                // bQQInputed = true;
            }
            if (!TextUtils.isEmpty(singnedInfo.getAddr())) {
                editAddr.setText(singnedInfo.getAddr());
                editAddr.setSelection(singnedInfo.getAddr().length());
                // bQQInputed = true;
            }
            bChecked = true;
            registerStatus = REG_CANCEL;
            textCommit.setBackgroundColor(getResources().getColor(R.color.register_blue));
            textCommit.setText(R.string.register_cancel);
        }
    }

    @Override
    public void finish() {
        // 在CommActDetailActivity.class中判断
        ActRegisterActivity.this.setResult(Activity.RESULT_OK);
        super.finish();
    }

    @Override
    protected void onStart() {
        super.onStart();
        bResume = true;
        IntentFilter filter = new IntentFilter(RedPacketConstant.LONGIN_TWO_DEVICES_CONFIRM_ACTION);
        registerReceiver(broadcastReceiver, filter);
    }

    @Override
    protected void onStop() {
        super.onStop();
        saveUiData();
        bResume = false;
        unregisterReceiver(broadcastReceiver);
    }

    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (RedPacketConstant.LONGIN_TWO_DEVICES_CONFIRM_ACTION.equals(intent.getAction())) {
                finish();
            }
        }
    };
}
