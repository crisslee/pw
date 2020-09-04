package com.coomix.app.all.ui.wallet;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.coomix.app.all.AllOnlineApp;
import com.coomix.app.all.GlobalParam;
import com.coomix.app.all.R;
import com.coomix.app.all.common.Keys;
import com.coomix.app.all.data.BaseSubscriber;
import com.coomix.app.all.data.DataEngine;
import com.coomix.app.all.data.ExceptionHandle;
import com.coomix.app.all.data.RxUtils;
import com.coomix.app.all.manager.ActivityStateManager;
import com.coomix.app.all.model.event.BindWechatEvent;
import com.coomix.app.all.model.event.SwitchUserEvent;
import com.coomix.app.all.model.response.RespBindWechat;
import com.coomix.app.all.service.ServiceAdapter;
import com.coomix.app.all.ui.base.BaseActivity;
import com.coomix.app.all.ui.login.LoginActivity;
import com.coomix.app.all.util.ExperienceUserUtil;
import com.coomix.app.framework.app.Result;
import com.tencent.mm.opensdk.modelmsg.SendAuth;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;
import com.umeng.analytics.MobclickAgent;
import io.reactivex.disposables.Disposable;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import static com.tencent.mm.opensdk.modelbase.BaseResp.ErrCode.ERR_OK;

public class BindWechatActivity extends BaseActivity
    implements View.OnClickListener, ServiceAdapter.ServiceAdapterCallback {
    private static final String TAG = "BindWechatActivity";

    private ImageView imgBack;

    //为了去除控件的深度，将button改为textview 下同
    // private Button btnBindWechat;
    private TextView btnBindWechat;

    private IWXAPI api;

    private ServiceAdapter serviceAdapter;
    private int iWithdrawTaskId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bind_wechat);

        imgBack = (ImageView) findViewById(R.id.imageViewBack);
        btnBindWechat = (TextView) findViewById(R.id.btnBindWechat);

        imgBack.setOnClickListener(this);
        btnBindWechat.setOnClickListener(this);

        serviceAdapter = ServiceAdapter.getInstance(this);
        serviceAdapter.registerServiceCallBack(this);
    }

    @Override
    protected void onDestroy() {
        if (serviceAdapter != null) {
            serviceAdapter.unregisterServiceCallBack(this);
        }
        super.onDestroy();
    }

    private void initWeixin() {
        api = WXAPIFactory.createWXAPI(this, Keys.WEIXIN_APP_ID, true);
        api.registerApp(Keys.WEIXIN_APP_ID);
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.imageViewBack:
                onBackPressed();
                break;

            case R.id.btnBindWechat:
                onBindWeChatClick();
                break;
        }
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

    private void onBindWeChatClick() {
        if (api == null) {
            initWeixin();
        }
        if (api.isWXAppInstalled()) {
            // send oauth request
            SendAuth.Req req = new SendAuth.Req();
            req.scope = "snsapi_userinfo";
            req.state = BindWechatEvent.BIND_WEICHAT_EVENT_STATE;

            boolean b = api.sendReq(req);
            if (!b) {
                Toast.makeText(BindWechatActivity.this, R.string.toast_launch_wx_fail, Toast.LENGTH_SHORT).show();
            } else {
                showProgressDialog("努力加载中...");
            }
            MobclickAgent.onEvent(BindWechatActivity.this, "num_bindwechat_click");
        } else {
            Toast.makeText(BindWechatActivity.this, R.string.toast_wx_not_installed, Toast.LENGTH_SHORT).show();
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onWeixinAuth(BindWechatEvent event) {
        SendAuth.Resp resp = event.getResponse();
        if (resp == null) {
            dismissProgressDialog();
            return;
        } else {
            if (BindWechatEvent.BIND_WEICHAT_EVENT_STATE.equals(resp.state) && resp.errCode == ERR_OK) { //succ
                bindWechat(resp.code, false, null);
            } else {
                if (resp.errCode == -2) {
                    Toast.makeText(BindWechatActivity.this, R.string.toast_wx_auth_cancel, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(BindWechatActivity.this, R.string.toast_wx_auth_failed, Toast.LENGTH_SHORT).show();
                }
                dismissProgressDialog();
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onSwitchUser(SwitchUserEvent event) {
        if (event.isSucc()) {
            switchUserSucc();
        } else {
            Toast.makeText(this, event.getErrCode() + event.getMsg(), Toast.LENGTH_LONG).show();
            switchUserFail();
        }
    }

    private void bindWechat(final String wxcode, boolean cover, String openId) {
        Disposable disposable = DataEngine.getCommunityApi()
            .bindWeChat(
                GlobalParam.getInstance().getCommonParas(),
                wxcode,
                cover,
                GlobalParam.getInstance().getAccessToken(),
                GlobalParam.getInstance().get_cid(),
                openId)
            .compose(RxUtils.toMain())
            .onErrorResumeNext(new RxUtils.HttpResponseFunc<>())
            .subscribeWith(new BaseSubscriber<RespBindWechat>(BindWechatActivity.this) {

                @Override
                public void onNext(RespBindWechat respBindWechat) {
                    if (respBindWechat.isSuccess()) {
                        onWechatBindSucc(respBindWechat);
                    } else if (respBindWechat.getErrcode() == ExceptionHandle.BusinessCode.ERR_WECHAT_ALREADY_BINDED) {
                        String openId = respBindWechat.getData().getUserinfo().getWxid();
                        onWechatBindedAlready(wxcode, openId);
                    } else if (respBindWechat.getErrcode() == ExceptionHandle.BusinessCode.ERR_WECHAT_WX_CODE_INVALID) {
                        showErr(getString(R.string.ERR_BINDWECHAT_INVALID_CODE));
                    } else {
                        onError(new ExceptionHandle.ServerException(respBindWechat));
                    }
                }

                @Override
                public void onHttpError(ExceptionHandle.ResponeThrowable e) {
                    showErr(e.getErrCodeMessage());
                    MobclickAgent.reportError(BindWechatActivity.this,
                        "bindwechat:" + e.getErrCodeMessage()); //report error
                }
            });

        subscribeRx(disposable);
    }

    private void onWechatBindSucc(RespBindWechat respBindWechat) {

        MobclickAgent.onEvent(BindWechatActivity.this, "num_bindwechat_succ");
        if (AllOnlineApp.sToken != null) {
            iWithdrawTaskId = serviceAdapter.switchCommunityUserAndHX(this.hashCode(), AllOnlineApp.sToken.access_token,
                AllOnlineApp.channelId(this), AllOnlineApp.sAccount);
        } else {
            iWithdrawTaskId = -1;
        }

        ExperienceUserUtil.clearUserJsonFromSp(this);

        if (iWithdrawTaskId < 0) {
            switchUserFail();
        } else { //可能不成功

        }
    }

    private void switchUserSucc() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.wx_bind_succ);
        builder.setCancelable(true);

        builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                finish();
            }
        });
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });
        builder.show();
    }

    private void switchUserFail() {
        ExperienceUserUtil.clearUserJsonFromSp(this);
        Intent intent = new Intent(this, LoginActivity.class);
        intent.putExtra("fromBoot", false);
        startActivity(intent);
        AllOnlineApp.sAccount = "";
        ActivityStateManager.finishAll();
    }

    private void onWechatBindedAlready(final String wxcode, final String openId) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("微信绑定");
        builder.setMessage("该微信已经被绑定，您是否确定绑定该微信？");
        builder.setCancelable(false);

        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                bindWechat(wxcode, true, openId);
            }
        });
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });
        builder.show();
    }

    @Override
    public void callback(int messageId, Result result) {

    }
}
