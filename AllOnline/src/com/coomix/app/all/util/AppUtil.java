package com.coomix.app.all.util;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.WindowManager;
import com.coomix.app.all.AllOnlineApp;
import com.coomix.app.all.Constant;
import com.coomix.app.all.GlobalParam;
import com.coomix.app.all.R;
import com.coomix.app.all.data.BaseSubscriber;
import com.coomix.app.all.data.DataEngine;
import com.coomix.app.all.data.ExceptionHandle;
import com.coomix.app.all.data.RxUtils;
import com.coomix.app.all.manager.DeviceManager;
import com.coomix.app.all.manager.DomainManager;
import com.coomix.app.all.manager.SettingDataManager;
import com.coomix.app.all.model.bean.Token;
import com.coomix.app.all.model.response.RespToken;
import com.coomix.app.all.ui.login.LoginActivity;
import com.coomix.app.framework.app.Result;
import com.coomix.app.framework.util.Encryption;
import com.coomix.app.framework.util.OSUtil;
import com.umeng.analytics.MobclickAgent;
import io.reactivex.disposables.Disposable;
import java.util.HashMap;

public class AppUtil {
    private static final String TAG = "AppUtil";

    private static volatile boolean isRestartPhase = false;
    private static Dialog dialog = null;

    public static void showWeinxinUndindDialog(final Context context, int delay) {
        if (isRestartPhase) {
            return;
        } else if (System.currentTimeMillis() - AllOnlineApp.getAppStartTime() < 30 * 1000) { //30秒内忽略
            return;
        } else {
            isRestartPhase = true;
        }

        new Handler().postDelayed(new Runnable() {
            public void run() {
                if (dialog != null) {
                    if (!dialog.isShowing()) {
                        dialog.show();
                    }
                    return;
                }
                String summary = context.getString(R.string.weixin_unbind_hint);
                String ok = context.getString(R.string.community_restart_ok);
                dialog = new AlertDialog.Builder(context, R.style.AppThemeM_AlertDialog)
                    .setMessage(summary)
                    .setCancelable(false)
                    .setPositiveButton(ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            //restartApp(context);
                            restartCommunityandHuanxin(context);
                        }
                    }).create();

                if (dialog != null) {
                    //dialog.setCanceledOnTouchOutside(false);
                    dialog.getWindow().setType(WindowManager.LayoutParams.TYPE_TOAST);
                    dialog.show();
                }
            }
        }, delay);
    }

    public static synchronized void showWeinxinUndindDialog() {
        if (dialog != null) {
            if (!dialog.isShowing()) {
                dialog.show();
            }
        }
    }

    public static synchronized void hideWeinxinUndindDialog() {
        if (dialog != null) {
            if (dialog.isShowing()) {
                dialog.dismiss();
            }
        }
    }

    /**
     * 只接受一次restart指令
     */
    public static synchronized void restartApp(Context context) {

        if (isRestartPhase) {
            return;
        } else if (System.currentTimeMillis() - AllOnlineApp.getAppStartTime() < 30 * 1000) { //30秒内忽略
            return;
        } else {
            isRestartPhase = true;
            restartCommunityandHuanxin(context);
        }
    }

    private static void restartCommunityandHuanxin(final Context context) {
        HashMap<String, String> map = new HashMap<String, String>();
        map.put("ev_function", "重启社区");
        MobclickAgent.onEvent(context, "ev_function", map);
        dialog = null;
        restartCommunity(context);
    }

    private static void restartCommunity(final Context context) {
        Runnable myRunnable = new Runnable() {
            @Override
            public void run() {
                toTabActionActivity(context);
            }
        };
        new Thread(myRunnable).start();
    }

    public static void restartCommunityOnly(final Context context) {
        Runnable myRunnable = new Runnable() {
            @Override
            public void run() {
                Result resultLogin = AllOnlineApp.mApiClient.login(AllOnlineApp.sToken.access_token,
                    AllOnlineApp.channelId(context), AllOnlineApp.sAccount);
            }
        };
        new Thread(myRunnable).start();
    }

    private static void toTabActionActivity(final Context context) {
        SettingDataManager.getInstance(context).goToMainByMap(context, null);
    }

    public static void resetRestartCmd(Context context) {
        isRestartPhase = false;
    }

    public static void reLogin() {
        int loginType = AllOnlineApp.sToken.loginType;
        if (loginType == LoginActivity.LOGIN_PHONE || loginType == LoginActivity.LOGIN_WX) {
            refreshToken();
        } else if (loginType == LoginActivity.LOGIN_USERNAME) {
            loginByUsername();
        } else if (loginType == LoginActivity.LOGIN_IMEI) {
            loginByImei();
        }
    }

    private static void refreshToken() {
        Token t = AllOnlineApp.sToken;
        if (t == null || TextUtils.isEmpty(t.access_token)) {
            return;
        }
        String cid = AllOnlineApp.sChannelID;
        if (TextUtils.isEmpty(cid)) {
            cid = "0";
        }
        Disposable d = DataEngine.getAllMainApi()
            .refreshToken(t.access_token, cid, GlobalParam.getInstance().getCommonParas())
            .compose(RxUtils.toIO())
            .compose(RxUtils.businessTransformer())
            .subscribeWith(new BaseSubscriber<RespToken>() {
                @Override
                public void onNext(RespToken respToken) {
                    reLoginSucc(respToken.getData());
                }

                @Override
                public void onHttpError(ExceptionHandle.ResponeThrowable e) {

                }
            });
    }

    private static void loginByUsername() {
        long current = DomainManager.sRespDomainAdd.timestamp;
        String account = Uri.encode(AllOnlineApp.sAccount, "UTF-8");
        String pwd = new Encryption().decrypt(Constant.SECRET_KEY, AllOnlineApp.sPassword);
        String sig = OSUtil.toMD5(OSUtil.toMD5(pwd) + current);
        String cid = AllOnlineApp.sChannelID;
        if (TextUtils.isEmpty(cid)) {
            cid = "0";
        }
        Disposable d = DataEngine.getAllMainApi()
            .loginByUsername(account, current, sig, cid, GlobalParam.getInstance().getCommonParas())
            .compose(RxUtils.toMain())
            .compose(RxUtils.businessTransformer())
            .subscribeWith(new BaseSubscriber<RespToken>() {
                @Override
                public void onNext(RespToken token) {
                    reLoginSucc(token.getData());
                }

                @Override
                public void onHttpError(ExceptionHandle.ResponeThrowable e) {
                }
            });
    }

    private static void loginByImei() {
        long current = DomainManager.sRespDomainAdd.timestamp;
        String imei = AllOnlineApp.sAccount;
        String pwd = new Encryption().decrypt(Constant.SECRET_KEY, AllOnlineApp.sPassword);
        String sig = OSUtil.toMD5(OSUtil.toMD5(pwd) + current);
        String cid = AllOnlineApp.sChannelID;
        if (TextUtils.isEmpty(cid)) {
            cid = "0";
        }
        Disposable d = DataEngine.getAllMainApi()
            .loginByImei(imei, current, sig, cid, GlobalParam.getInstance().getCommonParas())
            .compose(RxUtils.toMain())
            .compose(RxUtils.businessTransformer())
            .subscribeWith(new BaseSubscriber<RespToken>() {
                @Override
                public void onNext(RespToken token) {
                    reLoginSucc(token.getData());
                }

                @Override
                public void onHttpError(ExceptionHandle.ResponeThrowable e) {
                }
            });
    }

    private static void reLoginSucc(Token token) {
        int loginType = AllOnlineApp.sToken.loginType;
        AllOnlineApp.sToken = token;
        AllOnlineApp.sToken.loginType = loginType;
        //sTarget也需要重新赋值; 防止选择了子账号，返回退出后静态变量的内存缓存问题
        AllOnlineApp.sTarget = AllOnlineApp.sAccount;
        FileUtil.saveOjbect(AllOnlineApp.mApp, AllOnlineApp.sToken, FileUtil.getTokenPath(AllOnlineApp.mApp));
        //修改或初始化当前账户
        DeviceManager.getInstance().initCurrentSubaccount();
        //getAccountList();
        //getThemeAll();
    }
}
