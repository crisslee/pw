package com.coomix.app.all.ui.mine;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;
import com.coomix.app.all.AllOnlineApp;
import com.coomix.app.all.GlobalParam;
import com.coomix.app.all.R;
import com.coomix.app.all.widget.MyActionbar;
import com.coomix.app.all.manager.ActivityStateManager;
import com.coomix.app.framework.util.CommonUtil;
import com.coomix.app.framework.util.ExtraConstants;
import com.coomix.app.all.data.BaseSubscriber;
import com.coomix.app.all.data.DataEngine;
import com.coomix.app.all.data.ExceptionHandle;
import com.coomix.app.all.data.RxUtils;
import com.coomix.app.all.manager.ThemeManager;
import com.coomix.app.all.model.response.RespBase;
import com.coomix.app.all.ui.base.BaseActivity;
import com.coomix.app.all.ui.login.LoginActivity;
import com.coomix.app.all.util.ExperienceUserUtil;
import com.coomix.app.all.util.FileUtil;
import com.coomix.app.all.util.ViewUtil;
import com.google.gson.JsonObject;
import com.umeng.analytics.MobclickAgent;
import io.reactivex.disposables.Disposable;
import java.util.HashMap;

/**
 * 爱车安的“账户信息”界面
 *
 */
public class AccountInfoActivity extends BaseActivity {
    private static final String TAG = "AccountInfoActivity";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.community_activity_account_setting_layout);
        initViews();
    }

    protected void initViews() {
        MyActionbar actionbar = (MyActionbar) findViewById(R.id.mineActionbar);
        actionbar.initActionbar(true, R.string.account_setting_title, 0, 0);

        TextView textAccount = (TextView)findViewById(R.id.textViewUserAccount);

        if (AllOnlineApp.sToken.loginType == LoginActivity.LOGIN_WX) {
            textAccount.setText(AllOnlineApp.sToken.name);
        } else {
            textAccount.setText(AllOnlineApp.sAccount);
        }

        if (AllOnlineApp.sToken.loginType != LoginActivity.LOGIN_WX) {
            //名称
            findViewById(R.id.lineAccount).setVisibility(View.VISIBLE);
            findViewById(R.id.layoutName).setVisibility(View.VISIBLE);
            TextView textName = (TextView)findViewById(R.id.textViewName);
            if (!TextUtils.isEmpty(AllOnlineApp.sToken.name)) {
                textName.setText(AllOnlineApp.sToken.name);
            }
            else{
                textName.setText("");
            }

            //修改密码
            findViewById(R.id.lineName).setVisibility(View.VISIBLE);
            findViewById(R.id.textViewChangePWD).setVisibility(View.VISIBLE);
            findViewById(R.id.textViewChangePWD).setOnClickListener(view -> changePwd());
        }

        TextView textViewLogout = (TextView)findViewById(R.id.textViewLogout);
        textViewLogout.setOnClickListener(view -> switchAccount());
        ViewUtil.setBg(textViewLogout, ThemeManager.getInstance().getBGColorDrawable(this));
        textViewLogout.setTextColor(ThemeManager.getInstance().getThemeAll().getThemeColor().getColorNavibarText());
    }

    /**
     * 对应账户信息页的 注销登录 按钮
     */
    private void switchAccount() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.putExtra(ExtraConstants.EXTRA_FROM_BOOT, false);
        startActivity(intent);

        HashMap<String, String> map = new HashMap<String, String>();
        map.put("ev_function", "切换账户");
        MobclickAgent.onEvent(this, "ev_function", map);
        logout();
        logoutCommunity();
        AllOnlineApp.logoutGMIM();
        ExperienceUserUtil.clearUserJsonFromSp(this);
        AllOnlineApp.clearLoginInfo();
        FileUtil.deleteSpFile(this);
        AllOnlineApp.spInfo = null;
        ActivityStateManager.finishAll();
    }

    private void changePwd() {
        HashMap<String, String> mapModifyPw = new HashMap<String, String>();
        mapModifyPw.put("ev_function", "修改密码");
        MobclickAgent.onEvent(this, "ev_function", mapModifyPw);

        startActivity(new Intent(this, ModifyPwdActivity.class));
    }

    private void logout() {
        String token = AllOnlineApp.sToken.access_token;
        long current = System.currentTimeMillis() / 1000;
        String account = AllOnlineApp.sAccount;
        String cid = AllOnlineApp.sChannelID;
        Disposable d = DataEngine.getAllMainApi()
            .signOut(token, account, current, cid, GlobalParam.getInstance().getCommonParas())
            .compose(RxUtils.toIO())
            .compose(RxUtils.businessTransformer())
            .subscribeWith(new BaseSubscriber<RespBase>() {
                @Override
                public void onHttpError(ExceptionHandle.ResponeThrowable e) {
                }

                @Override
                public void onNext(RespBase respBase) {
                }
            });
        subscribeRx(d);
    }

    private void logoutCommunity() {
        String ticket = AllOnlineApp.sToken.ticket;
        String cid = AllOnlineApp.sChannelID;
        Disposable d = DataEngine.getCommunityApi()
            .logoutCommunity(CommonUtil.encodeTicket(ticket), cid, GlobalParam.getInstance().getCommonParas())
            .compose(RxUtils.toIO())
            .compose(RxUtils.businessTransformer())
            .subscribeWith(new BaseSubscriber<JsonObject>() {
                @Override
                public void onHttpError(ExceptionHandle.ResponeThrowable e) {
                }

                @Override
                public void onNext(JsonObject respBase) {
                }
            });
        subscribeRx(d);
    }
}
