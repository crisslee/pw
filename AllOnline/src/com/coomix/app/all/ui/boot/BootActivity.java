package com.coomix.app.all.ui.boot;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import com.bumptech.GlideApp;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.request.transition.Transition;
import com.coomix.app.all.AllOnlineApp;
import com.coomix.app.all.Constant;
import com.coomix.app.all.GlobalParam;
import com.coomix.app.all.R;
import com.coomix.app.all.data.BaseSubscriber;
import com.coomix.app.all.data.DataEngine;
import com.coomix.app.all.data.ExceptionHandle;
import com.coomix.app.all.data.RxUtils;
import com.coomix.app.all.log.AppConfigs;
import com.coomix.app.all.log.GLog;
import com.coomix.app.all.log.GoomeLogUtil;
import com.coomix.app.all.log.LogUploadInfo;
import com.coomix.app.all.manager.DeviceManager;
import com.coomix.app.all.manager.DomainManager;
import com.coomix.app.all.manager.SettingDataManager;
import com.coomix.app.all.manager.ThemeManager;
import com.coomix.app.all.model.bean.Adver;
import com.coomix.app.all.model.bean.RespDomainAdd;
import com.coomix.app.all.model.bean.ThemeAll;
import com.coomix.app.all.model.bean.ThemeLogo;
import com.coomix.app.all.model.bean.Token;
import com.coomix.app.all.model.response.RespAccountGroupInfo;
import com.coomix.app.all.model.response.RespAdver;
import com.coomix.app.all.model.response.RespAllTypes;
import com.coomix.app.all.model.response.RespAppConfig;
import com.coomix.app.all.model.response.RespBase;
import com.coomix.app.all.model.response.RespServiceProvider;
import com.coomix.app.all.model.response.RespThemeAll;
import com.coomix.app.all.model.response.RespToken;
import com.coomix.app.all.service.CheckVersionService;
import com.coomix.app.all.ui.base.BaseActivity;
import com.coomix.app.all.ui.login.LoginActivity;
import com.coomix.app.all.util.CommunityUtil;
import com.coomix.app.all.util.FileUtil;
import com.coomix.app.all.util.GpnsUtil;
import com.coomix.app.all.util.Md5Util;
import com.coomix.app.all.util.StringUtil;
import com.coomix.app.framework.app.Result;
import com.coomix.app.framework.util.Encryption;
import com.coomix.app.framework.util.NetworkUtil;
import com.coomix.app.framework.util.OSUtil;
import com.coomix.app.framework.util.PhoneInfo;
import com.coomix.app.framework.util.PreferenceUtil;
import com.google.gson.Gson;
import io.reactivex.disposables.Disposable;
import java.io.File;
import java.lang.ref.WeakReference;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;

/**
 * 启动页
 */
public class BootActivity extends BaseActivity implements OnClickListener {
    private static final String TAG = BootActivity.class.getSimpleName();

    private Handler mHandler;
    private final int MSG_LAUNCH = 1;
    private final int MSG_SKIP = 2;
    private int iAdverSkipTime = 3;
    private ImageView imageAdver, ivBoot;
    private View layoutAdver;
    private Button skipAdverBtn;
    private boolean isAdShowing = false;
    private boolean needRelogin = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        AllOnlineApp.setAppStartTime(System.currentTimeMillis());
        PreferenceUtil.init(BootActivity.this);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        hideNavigation();

        setContentView(R.layout.activity_boot);

        if (!AllOnlineApp.getAppConfig().isBuglyUpgradeAgent()) {
            CheckVersionService.startService(getApplicationContext());
        }

        mHandler = new MyHandler(this);

        initView();
        initData();
    }

    @Override
    protected void onDestroy() {
        try {
            mHandler.removeCallbacksAndMessages(null);
            super.onDestroy();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void hideNavigation() {
        View decorView = getWindow().getDecorView();
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
            decorView.setSystemUiVisibility(View.GONE);
        } else {
            int option = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
            decorView.setSystemUiVisibility(option);
        }
    }

    private void initView() {
        ivBoot = (ImageView) findViewById(R.id.iv_boot);
        setBootBg();
        layoutAdver = findViewById(R.id.ad_rl);
        imageAdver = (ImageView) findViewById(R.id.ad_iv);
        skipAdverBtn = (Button) findViewById(R.id.skip_btn);

        imageAdver.setOnClickListener(this);
        skipAdverBtn.setOnClickListener(this);
    }

    private void setBootBg() {
        ThemeLogo logo = ThemeManager.getInstance().getThemeAll().getLogo();
        if (!TextUtils.isEmpty(logo.url)) {
            GlideApp.with(AllOnlineApp.mApp)
                .load(logo.url)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(ivBoot);
        }
    }

    public void initData() {
        readFirstHost();
        getAllAD();
        getAllDevTypes();
        reLogin();
        initSpInfo();
        getAppConfig();
        // 延迟x秒不再等待,正常启动
        mHandler.sendEmptyMessageDelayed(MSG_LAUNCH, 4000);
    }

    private void jumpToAd() {
        if (Constant.mAdvers != null) {
            for (Adver adver : Constant.mAdvers) {
                if (adver.type == Adver.AD_TYPE_LAUNCH) {
                    if (!StringUtil.isTrimEmpty(adver.jumpurl)
                            && DomainManager.sRespDomainAdd != null
                            && !StringUtil.isTrimEmpty(DomainManager.sRespDomainAdd.domainAd)) {
                        // 广告点击统计
                        reportAdver(adver.id, Adver.REPORT_CLICK);
                        //跳转到原生广告
                        CommunityUtil.onAdvertisementClick(this, adver, true);
                        finish();
                    }
                }
            }
        }
    }

    private static class MyHandler extends Handler {
        private WeakReference<BootActivity> mActivityRef;

        public MyHandler(BootActivity activity) {
            mActivityRef = new WeakReference<BootActivity>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            BootActivity activity = mActivityRef.get();
            if (activity != null) {
                if (msg.what == activity.MSG_LAUNCH) {
                    if (!activity.isAdShowing) {
                        // 没有显示广告, 则跳往下个页面
                        activity.launch();
                    }
                } else if (msg.what == activity.MSG_SKIP) {
                    if (--activity.iAdverSkipTime > 0) {
                        activity.setSkipText();
                        sendEmptyMessageDelayed(activity.MSG_SKIP, 1000);
                    } else {
                        activity.setSkipText();
                        activity.launch();
                    }
                }
            }
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ad_iv:
                jumpToAd();
                break;
            case R.id.skip_btn:
                launch();
                break;
            default:
                break;
        }
    }

    private void readFirstHost() {
        Disposable d = DataEngine.getFirstHostApi().getFirstHost()
            .compose(RxUtils.toMain())
            .compose(RxUtils.businessTransformer())
            .subscribeWith(new BaseSubscriber<RespDomainAdd>() {
                @Override
                public void onNext(RespDomainAdd domainAdd) {
                    if (domainAdd != null) {
                        //赋值给全局变量，并且存储到本地作为下次app启动的初始数据
                        DomainManager.sRespDomainAdd = domainAdd;
                        GpnsUtil.initGpns(BootActivity.this, DomainManager.getInstance().getInitGpnsHost());
                        PreferenceUtil.commitString(Constant.PREFERENCE_DOMAINS, new Gson().toJson(domainAdd));
                    }
                }

                @Override
                public void onHttpError(ExceptionHandle.ResponeThrowable e) {
                    //如果请求出错，也需要用默认数据初始化Gpns
                    GpnsUtil.initGpns(BootActivity.this, DomainManager.getInstance().getInitGpnsHost());
                }
            });
        subscribeRx(d);
    }

    private void getAllAD() {
        Disposable d = DataEngine.getAdverApi().getAllAd(GlobalParam.getInstance().getCommonParas(),
            AllOnlineApp.sAccount, AllOnlineApp.screenWidth, AllOnlineApp.screenHeight,
            AllOnlineApp.getCurrentLocation().getLatitude(), AllOnlineApp.getCurrentLocation().getLongitude(),
            Constant.COMMUNITY_CITYCODE)
            .compose(RxUtils.toMain())
            .compose(RxUtils.businessTransformer())
            .subscribeWith(new BaseSubscriber<RespAdver>() {
                @Override
                public void onNext(RespAdver respAdver) {
                    //展示主页广告
                    if (respAdver != null) {
                        Constant.mAdvers = respAdver.getData();
                        for (Adver adver : Constant.mAdvers) {
                            if (adver != null && adver.type == Adver.AD_TYPE_LAUNCH && !TextUtils.isEmpty(
                                adver.picurl)) {
                                isAdShowing = showAd(adver);
                                //移除跳转下一个界面的消息
                                mHandler.removeMessages(MSG_LAUNCH);
                                return;
                            }
                        }
                    }
                }

                @Override
                public void onHttpError(ExceptionHandle.ResponeThrowable e) {

                }
            });
        subscribeRx(d);
    }

    private void getAllDevTypes() {
        String token = GlobalParam.getInstance().getAccessToken();
        Disposable d = DataEngine.getAllMainApi()
            .getAllTypes(token, 1, 0, 1000, GlobalParam.getInstance().getCommonParas())
            .compose(RxUtils.businessTransformer())
            .compose(RxUtils.toIO())
            .subscribeWith(new BaseSubscriber<RespAllTypes>() {
                @Override
                public void onHttpError(ExceptionHandle.ResponeThrowable e) {
                    GLog.e(TAG, e.getErrCodeMessage());
                }

                @Override
                public void onNext(RespAllTypes respAllTypes) {
                    if (respAllTypes == null) {
                        return;
                    }
                    DeviceManager.getInstance().addDevTypes(respAllTypes.getData().getTypes());
                }
            });
        subscribeRx(d);
    }

    private void reportAdver(int adverId, int iType) {
        if (adverId <= 0) {
            return;
        }
        PhoneInfo phoneInfo = new PhoneInfo(this);
        String phoneNumber = phoneInfo.getNativePhoneNumber();
        if (phoneNumber == null) {
            phoneNumber = "";
        }
        Disposable d = DataEngine.getAdverApi().clickAdverReport(GlobalParam.getInstance().getCommonParas(),
            phoneNumber, Constant.COMMUNITY_CITYCODE, iType, adverId)
            .compose(RxUtils.toMain())
            .compose(RxUtils.businessTransformer())
            .subscribeWith(new BaseSubscriber<RespBase>() {
                @Override
                public void onNext(RespBase respBase) {

                }

                @Override
                public void onHttpError(ExceptionHandle.ResponeThrowable e) {

                }
            });
        subscribeRx(d);
    }

    private void refreshToken() {
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
        subscribeRx(d);
    }

    private void loginByUsername() {
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
                    if (e.code == Result.ERROR_ACCOUNT_OR_PASSWORD_INVAID) {
                        showAccOrPwdError();
                    }
                }
            });
        subscribeRx(d);
    }

    private void loginByImei() {
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
                    if (e.code == Result.ERROR_ACCOUNT_OR_PASSWORD_INVAID) {
                        showAccOrPwdError();
                    }
                }
            });
        subscribeRx(d);
    }

    private void reLoginSucc(Token token) {
        int loginType = AllOnlineApp.sToken.loginType;
        AllOnlineApp.sToken = token;
        AllOnlineApp.sToken.loginType = loginType;
        //sTarget也需要重新赋值; 防止选择了子账号，返回退出后静态变量的内存缓存问题
        AllOnlineApp.sTarget = AllOnlineApp.sAccount;
        FileUtil.saveOjbect(AllOnlineApp.mApp, AllOnlineApp.sToken, FileUtil.getTokenPath(AllOnlineApp.mApp));
        //修改或初始化当前账户
        DeviceManager.getInstance().initCurrentSubaccount();
        getAccountList();
        getThemeAll();
    }

    private void reLogin() {
        int loginType = AllOnlineApp.sToken.loginType;
        if (loginType == LoginActivity.LOGIN_PHONE || loginType == LoginActivity.LOGIN_WX) {
            refreshToken();
        } else if (loginType == LoginActivity.LOGIN_USERNAME) {
            loginByUsername();
        } else if (loginType == LoginActivity.LOGIN_IMEI) {
            loginByImei();
        }
    }

    private void getAccountList() {
        Disposable d = DataEngine.getAllMainApi().getAccountList(GlobalParam.getInstance().getCommonParas(),
            AllOnlineApp.sAccount, AllOnlineApp.sTarget, AllOnlineApp.sToken.access_token)
            .compose(RxUtils.toIO())
            .compose(RxUtils.businessTransformer())
            .subscribeWith(new BaseSubscriber<RespAccountGroupInfo>() {
                @Override
                public void onNext(RespAccountGroupInfo respAccountGroupInfo) {
                    if (respAccountGroupInfo.getData() != null) {
                        AllOnlineApp.customerId = respAccountGroupInfo.getData().getCustomer_id();
                        AllOnlineApp.bMainHasChild = respAccountGroupInfo.getData().getChildren().size() > 0;
                        DeviceManager dm = DeviceManager.getInstance();
                        dm.addGroupInMap(respAccountGroupInfo.getData().getGroup());
                        dm.addSubaccountsList(respAccountGroupInfo.getData().getChildren(), "0");
                        dm.getCurrentSubaccount().haschild = respAccountGroupInfo.getData().getChildren().size() > 0;
                    }
                }

                @Override
                public void onHttpError(ExceptionHandle.ResponeThrowable e) {
                    if (Constant.IS_DEBUG_MODE) {
                        e.printStackTrace();
                    }
                }
            });
        subscribeRx(d);
    }

    private void setSkipText() {
        skipAdverBtn.setText(getString(R.string.skip, iAdverSkipTime));
    }

    private void initSpInfo() {
        String token = AllOnlineApp.sToken.access_token;
        RespServiceProvider.ServiceProvider sp = AllOnlineApp.spInfo;
        if (!TextUtils.isEmpty(token) && (sp == null || TextUtils.isEmpty(sp.sp_name))) {
            getSpInfo();
        }
    }

    private void getSpInfo() {
        String token = AllOnlineApp.sToken.access_token;
        String account = AllOnlineApp.sAccount;
        String target = AllOnlineApp.sTarget;
        long time = System.currentTimeMillis() / 1000;
        Disposable d = DataEngine.getAllMainApi()
            .getServiceProvider(token, account, target, time, GlobalParam.getInstance().getCommonParas())
            .compose(RxUtils.toMain())
            .compose(RxUtils.businessTransformer())
            .subscribeWith(new BaseSubscriber<RespServiceProvider>() {
                @Override
                public void onHttpError(ExceptionHandle.ResponeThrowable e) {
                    e.printStackTrace();
                }

                @Override
                public void onNext(RespServiceProvider respServiceProvider) {
                    AllOnlineApp.spInfo = respServiceProvider.getData();
                    FileUtil.saveOjbect(AllOnlineApp.mApp, AllOnlineApp.spInfo, FileUtil.getSpPath(AllOnlineApp.mApp));
                }
            });
        subscribeRx(d);
    }

    private boolean showAd(final Adver adver) {
        if (adver == null) {
            return false;
        }
        GlideApp.with(this).load(adver.picurl).centerCrop()
            .override(AllOnlineApp.screenWidth, AllOnlineApp.screenHeight)
            //.skipMemoryCache(true)
            .into(new SimpleTarget<Drawable>() {
                @Override
                public void onResourceReady(Drawable resource, Transition<? super Drawable> transition) {
                    if (resource != null) {
                        layoutAdver.setVisibility(View.VISIBLE);
                        imageAdver.setImageDrawable(resource);
                        iAdverSkipTime = 3;
                        setSkipText();
                        mHandler.sendEmptyMessageDelayed(MSG_SKIP, 1000);
                        reportAdver(adver.id, Adver.REPORT_SHOW);
                    }
                }

                @Override
                public void onLoadFailed(@Nullable Drawable errorDrawable) {
                    super.onLoadFailed(errorDrawable);
                    //广告失败，直接跳转
                    launch();
                }
            });
        return true;
    }

    private void launch() {
        if (needRelogin) {
            return;
        }
        if (AllOnlineApp.sToken != null && !TextUtils.isEmpty(AllOnlineApp.sToken.access_token)) {
            SettingDataManager.getInstance(this).goToMainByMap(this, null);
        } else {
            startActivity(new Intent(this, LoginActivity.class));
        }
        finish();
    }

    private void getAppConfig() {
        Disposable d = DataEngine.getConfigApi().getAppConfig(GlobalParam.getInstance().getCommonParas(),
            Build.MODEL, Build.DISPLAY, Build.VERSION.SDK_INT, Build.VERSION.RELEASE, AllOnlineApp.sChannelID,
            NetworkUtil.isWifiExtend(this) ? "wifi" : "mobile")
            .compose(RxUtils.toMain())
            .compose(RxUtils.businessTransformer())
            .subscribeWith(new BaseSubscriber<RespAppConfig>() {
                @Override
                public void onNext(RespAppConfig respAppConfig) {
                    AppConfigs config = respAppConfig.getData();
                    if (config != null) {
                        //PreferenceUtil.commitObj(Constant.PREFERENCE_APP_CONFIG, config);
                        AllOnlineApp.setAppConfig(config);
                    }
                    uploadLog();
                }

                @Override
                public void onHttpError(ExceptionHandle.ResponeThrowable e) {

                }
            });
        subscribeRx(d);
    }

    private void getThemeAll() {
        if (AllOnlineApp.sToken == null || TextUtils.isEmpty(AllOnlineApp.sToken.account)
            || TextUtils.isEmpty(AllOnlineApp.sToken.access_token)) {
            return;
        }
        String account = AllOnlineApp.sToken.account;
        String token = AllOnlineApp.sToken.access_token;
        Disposable d = DataEngine.getAllMainApi()
            .getThemeInfo(account, token, GlobalParam.getInstance().getCommonParas())
            .compose(RxUtils.toIO())
            .compose(RxUtils.businessTransformer())
            .subscribeWith(new BaseSubscriber<RespThemeAll>() {
                @Override
                public void onHttpError(ExceptionHandle.ResponeThrowable e) {
                }

                @Override
                public void onNext(RespThemeAll respThemeAll) {
                    final ThemeAll themeAll = respThemeAll.getData();
                    downloadLogo();
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            ThemeManager.getInstance().setThemeAll(themeAll);
                        }
                    });
                    FileUtil.saveOjbect(AllOnlineApp.mApp, themeAll, FileUtil.getThemePath(AllOnlineApp.mApp));
                }
            });
        subscribeRx(d);
    }

    private void downloadLogo() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                ThemeLogo logo = ThemeManager.getInstance().getThemeAll().getLogo();
                if (!TextUtils.isEmpty(logo.url)) {
                    GlideApp.with(AllOnlineApp.mApp)
                        .load(logo.url)
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .downloadOnly(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL);
                }
            }
        }).start();
    }

    /**
     * 本地日志上传至网络端(仅执行一次)
     */
    private void uploadLog() {
        if (GoomeLogUtil.hasUploadFailedErrorLog() || GoomeLogUtil.isNeedUploadLog()) {
            //上传日志
            final LogUploadInfo info = new LogUploadInfo(this);
            if (info.getLocal_path() != null && info.getLocal_path().length > 0) {
                for (String path : info.getLocal_path()) {
                    final File file = new File(path);
                    if (file.exists()) {
                        try {
                            RequestBody requestBody = new MultipartBody.Builder().setType(MultipartBody.FORM)
                                .addFormDataPart("md5", Md5Util.fileMD5(path))
                                .addFormDataPart("size", String.valueOf(file.length()))
                                .addFormDataPart("content", file.getName(),
                                    RequestBody.create(MediaType.parse("text/*"), file))
                                .build();

                            Disposable d = DataEngine.getGoomeLogApi()
                                .uploadLogFile(GlobalParam.getInstance().getCommonParas(), info.getProduction(),
                                    info.getDevname(), info.getOsver(),
                                    info.getOsextra(), info.getExtra(),
                                    NetworkUtil.isWifiExtend(this) ? "wifi" : "mobile", requestBody)
                                .compose(RxUtils.toIO())
                                .compose(RxUtils.businessTransformer())
                                .subscribeWith(new BaseSubscriber<RespBase>() {
                                    @Override
                                    public void onNext(RespBase respBase) {
                                        GoomeLogUtil.setUploadFailedErrorLog(false);
                                        try {
                                            file.delete();
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                    }

                                    @Override
                                    public void onHttpError(ExceptionHandle.ResponeThrowable e) {
                                        GoomeLogUtil.setUploadFailedErrorLog(true);
                                    }
                                });
                            //subscribeRx(d);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
    }

    private void showAccOrPwdError() {
        needRelogin = true;
        AllOnlineApp.clearLoginInfo();
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.account_or_pwd_error);
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                startActivity(new Intent(BootActivity.this, LoginActivity.class));
                finish();
            }
        });
        builder.setCancelable(false);
        builder.show();
    }
}
