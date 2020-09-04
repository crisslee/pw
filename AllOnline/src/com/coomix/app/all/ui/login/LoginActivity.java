package com.coomix.app.all.ui.login;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import butterknife.BindView;
import butterknife.ButterKnife;
import com.coomix.app.all.AllOnlineApp;
import com.coomix.app.all.Constant;
import com.coomix.app.all.manager.DeviceManager;
import com.coomix.app.all.GlobalParam;
import com.coomix.app.all.R;
import com.coomix.app.all.ui.web.BusAdverActivity;
import com.coomix.app.all.model.bean.Random;
import com.coomix.app.all.model.bean.ThemeAll;
import com.coomix.app.all.model.bean.Token;
import com.coomix.app.all.common.Keys;
import com.coomix.app.all.log.AppConfigs;
import com.coomix.app.all.manager.ActivityStateManager;
import com.coomix.app.framework.util.Encryption;
import com.coomix.app.framework.util.NetworkUtil;
import com.coomix.app.framework.util.OSUtil;
import com.coomix.app.framework.util.PreferenceUtil;
import com.coomix.app.all.data.BaseSubscriber;
import com.coomix.app.all.data.DataEngine;
import com.coomix.app.all.data.ExceptionHandle;
import com.coomix.app.all.data.RxUtils;
import com.coomix.app.all.manager.DomainManager;
import com.coomix.app.all.manager.ThemeManager;
import com.coomix.app.all.model.response.RespAccountGroupInfo;
import com.coomix.app.all.model.response.RespBase;
import com.coomix.app.all.model.response.RespRandom;
import com.coomix.app.all.model.response.RespServiceProvider;
import com.coomix.app.all.model.response.RespThemeAll;
import com.coomix.app.all.model.response.RespToken;
import com.coomix.app.all.ui.base.BaseActivity;
import com.coomix.app.all.util.CommunityUtil;
import com.coomix.app.all.util.FileUtil;
import com.coomix.app.all.manager.SettingDataManager;
import com.coomix.app.all.util.StringUtil;
import com.coomix.app.all.util.ViewUtil;
import com.coomix.security.Security;
import com.google.zxing.activity.CaptureActivity;
import com.google.zxing.util.ZConstant;
import com.tencent.mm.opensdk.modelmsg.SendAuth;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;
import io.reactivex.disposables.Disposable;
import java.lang.ref.WeakReference;

import static com.coomix.app.framework.app.BaseApiClient.COMMUNITY_COOMIX_VERSION;

/**
 * File Description:
 *
 * @author felixqiu
 * @since 2018/8/6.
 */
public class LoginActivity extends BaseActivity implements View.OnClickListener {
    private static final String TAG = LoginActivity.class.getSimpleName();

    @BindView(R.id.register)
    TextView register;
    @BindView(R.id.title)
    TextView title;
    @BindView(R.id.statement)
    TextView statement;
    @BindView(R.id.phoneOrImei)
    EditText phoneOrImei;
    @BindView(R.id.password)
    EditText password;
    @BindView(R.id.getCaptcha)
    TextView getCaptcha;
    @BindView(R.id.login)
    TextView login;
    @BindView(R.id.forgetPwd)
    TextView forgetPwd;
    @BindView(R.id.loginByCaptcha)
    TextView loginByCaptcha;
    @BindView(R.id.imageWx)
    ImageView imageWx;
    @BindView(R.id.imageQr)
    ImageView imageQr;

    public static final String FROM_WHERE = "from";
    public static final String FROM_DAILY_REDPACKET = "daily_redpacket";

    //账号登录
    private static final int UI_PHONE = 0;
    //验证码登录
    private static final int UI_CAPTCHA = 1;
    private int uiMode = UI_PHONE;
    private String username;
    private String pwd;
    private Encryption encrypt;
    //登录类型
    private int loginType = 0;
    public static final int LOGIN_USERNAME = 0;
    public static final int LOGIN_PHONE = 1;
    public static final int LOGIN_IMEI = 2;
    public static final int LOGIN_WX = 3;

    /**
     * 微信登录相关
     */
    private IWXAPI wxApi;
    public static boolean needWxLogin = false;
    public static String sWxState = "";
    public static String sWxCode = "";

    public static final String PHONE_NUM = "phone";
    public static final String PASSWORD_NUM = "password";

    private static final int COUNTDOWN = 0;
    private static final int COUNT_PERIOD = 1000;
    private static CountHandler ch;

    static class CountHandler extends Handler {
        WeakReference<LoginActivity> a;

        public CountHandler(LoginActivity a) {
            this.a = new WeakReference<>(a);
        }

        @Override
        public void handleMessage(Message msg) {
            LoginActivity l = a.get();
            if (l == null) {
                return;
            }
            int time = msg.arg1;
            switch (msg.what) {
                case COUNTDOWN:
                    Message m = Message.obtain();
                    m.what = COUNTDOWN;
                    m.arg1 = time - 1;
                    ch.sendMessageDelayed(m, COUNT_PERIOD);
                    l.updateCount(time);
                    break;
                default:
                    break;
            }
        }
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);

        ch = new CountHandler(this);
        encrypt = new Encryption();
        initView();
        initListener();
        updateUi();
        initData();
        initWx();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (needWxLogin) {
            needWxLogin = false;
            hideLoading();
            doWxLogin();
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        initData();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (ch != null) {
            ch.removeCallbacksAndMessages(null);
        }
    }

    private void initView() {
        String s = statement.getText().toString();
        SpannableString ss = new SpannableString(s);
        ss.setSpan(new ClickableSpan() {
            @Override
            public void onClick(View widget) {
                toStatement();
            }

            @Override
            public void updateDrawState(TextPaint ds) {
                ds.setColor(Color.parseColor("#2575AC"));
            }
        }, 8, s.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        statement.setMovementMethod(LinkMovementMethod.getInstance());
        statement.setText(ss);

        setLogin(false);
        login.setTextColor(ThemeManager.getInstance().getThemeAll().getThemeColor().getColorNavibarText());
    }

    private void toStatement() {
        AppConfigs configs = AllOnlineApp.getAppConfig();
        String url = configs.getClauseUrl();
        CommunityUtil.switch2WebViewActivity(this, url, false, BusAdverActivity.INTENT_FROM_LOGIN);
    }

    private void initListener() {
        register.setOnClickListener(this);
        getCaptcha.setOnClickListener(this);
        login.setOnClickListener(this);
        forgetPwd.setOnClickListener(this);
        loginByCaptcha.setOnClickListener(this);
        imageWx.setOnClickListener(this);
        imageQr.setOnClickListener(this);
        phoneOrImei.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (!TextUtils.isEmpty(phoneOrImei.getText().toString())
                    && !TextUtils.isEmpty(password.getText().toString())) {
                    setLogin(true);
                } else {
                    setLogin(false);
                }
            }
        });
        password.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (!TextUtils.isEmpty(phoneOrImei.getText().toString())
                    && !TextUtils.isEmpty(password.getText().toString())) {
                    setLogin(true);
                } else {
                    setLogin(false);
                }
            }
        });
    }

    private void setLogin(boolean enable) {
        if (enable) {
            ViewUtil.setBg(login, ThemeManager.getInstance().getBGColorDrawable(this));
        } else {
            ViewUtil.setBg(login, ThemeManager.getInstance().getDefaultDrawable(this));
        }
    }

    private void updateUi() {
        switch (uiMode) {
            //账号登录
            case UI_PHONE:
                title.setText(R.string.login_by_account_pwd);
                getCaptcha.setVisibility(View.GONE);
                phoneOrImei.setHint(R.string.login_account_hint);
                password.setHint(R.string.login_pwd_hint);
                loginByCaptcha.setText(R.string.login_by_captcha);
                password.setInputType(EditorInfo.TYPE_CLASS_TEXT | EditorInfo.TYPE_TEXT_VARIATION_PASSWORD);
                password.setText("");
                break;
            //验证码登录
            case UI_CAPTCHA:
                title.setText(R.string.login_by_captcha_title);
                getCaptcha.setVisibility(View.VISIBLE);
                phoneOrImei.setHint(R.string.login_phone_hint);
                password.setHint(R.string.login_captcha_hint);
                loginByCaptcha.setText(R.string.login_by_account);
                password.setInputType(EditorInfo.TYPE_CLASS_NUMBER);
                password.setText("");
                break;
            default:
                break;
        }
    }

    private void initWx() {
        wxApi = WXAPIFactory.createWXAPI(this, Keys.WEIXIN_APP_ID, true);
        wxApi.registerApp(Keys.WEIXIN_APP_ID);
    }

    private void initData() {
        Intent i = getIntent();
        if (i == null) {
            return;
        }
        Bundle b = i.getExtras();
        if (b == null) {
            return;
        }
        String phoneNum = b.getString(PHONE_NUM);
        String captchaNum = b.getString(PASSWORD_NUM);
        if (!TextUtils.isEmpty(phoneNum) && !TextUtils.isEmpty(captchaNum)) {
            uiMode = UI_PHONE;
            updateUi();
            phoneOrImei.setText(phoneNum);
            password.setText(captchaNum);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.register:
                gotoRegister();
                break;
            case R.id.getCaptcha:
                getCaptcha();
                break;
            case R.id.login:
                loginAction();
                break;
            case R.id.forgetPwd:
                forgetPassword();
                break;
            case R.id.loginByCaptcha:
                loginByCaptcha();
                break;
            case R.id.imageWx:
                showLoading(null);
                toWx();
                break;
            case R.id.imageQr:
                toScan();
                break;
            default:
                break;
        }
    }

    private void gotoRegister() {
        Intent r = new Intent(this, RegisterActivity.class);
        r.putExtra(RegisterActivity.UIMODE, RegisterActivity.REGISTER);
        startActivity(r);
    }

    private void getCaptcha() {
        if (checkPhone()) {
            startCount();
            getCaptchaRandom();
        } else {
            showToast(getString(R.string.act_tel_error));
        }
    }

    private void startCount() {
        Message m = Message.obtain(ch, COUNTDOWN);
        m.arg1 = 60;
        m.sendToTarget();
    }

    private void updateCount(int time) {
        if (time == 0) {
            ch.removeCallbacksAndMessages(null);
            getCaptcha.setEnabled(true);
            getCaptcha.setText(R.string.register_get_captcha);
        } else {
            String str = getString(R.string.bindphone_sms_code_countdown_text, time);
            getCaptcha.setText(str);
            getCaptcha.setEnabled(false);
        }
    }

    private boolean checkPhone() {
        username = phoneOrImei.getText().toString().trim();
        return StringUtil.isPhoneValid(username);
    }

    private void getCaptchaRandom() {
        Disposable d = DataEngine.getAllMainApi()
                .getCaptchaRandom(GlobalParam.getInstance().getCommonParas())
                .compose(RxUtils.toIO())
                .compose(RxUtils.businessTransformer())
                .subscribeWith(new BaseSubscriber<RespRandom>() {
                    @Override
                    public void onNext(RespRandom respRandom) {
                        applyCaptcha(respRandom.getData());
                    }

                    @Override
                    public void onHttpError(ExceptionHandle.ResponeThrowable e) {
                        showToast(getString(R.string.bindphone_toast_get_sms_code_fail));
                    }
                });
        subscribeRx(d);
    }

    private void applyCaptcha(Random random) {
        long t = System.currentTimeMillis() / 1000;
        String encryptStr = OSUtil.toMD5(t + Constant.PRIVATE_KEY + Constant.PRIVATE_PACKAGE);
        String telsec = new Security().getSecInfo(username + "|" + random.random, encryptStr, COMMUNITY_COOMIX_VERSION);
        Disposable d = DataEngine.getAllMainApi()
            .applyCaptcha(username, telsec, GlobalParam.getInstance().getCommonParas())
            .compose(RxUtils.toMain())
            .compose(RxUtils.businessTransformer())
            .subscribeWith(new BaseSubscriber<RespBase>() {
                @Override
                public void onNext(RespBase respBase) {
                }

                @Override
                public void onHttpError(ExceptionHandle.ResponeThrowable e) {
                    showToast(getString(R.string.bindphone_toast_get_sms_code_fail));
                }
            });
        subscribeRx(d);
    }

    private void loginAction() {
        if (checkInput()) {
            if (NetworkUtil.isNetworkConnected(this)) {
                showLoading(getString(R.string.logining));
                if (uiMode == UI_PHONE) {
                    loginByUsername();
                } else {
                    loginByPhone();
                }
            } else {
                showToast(getString(R.string.network_error));
            }
        }
    }

    private boolean checkInput() {
        username = phoneOrImei.getText().toString().trim();
        pwd = password.getText().toString();
        if (StringUtil.isTrimEmpty(username)) {
            showToast(getString(R.string.account_not_null));
            return false;
        }
        if (StringUtil.isTrimEmpty(pwd)) {
            showToast(getString(R.string.password_not_null));
            return false;
        }
        if (!StringUtil.isTrimEmpty(pwd) && pwd.length() < 2) {
            showToast(getString(R.string.pwd_low));
            return false;
        }
        return true;
    }

    private void loginByUsername() {
        loginType = LOGIN_USERNAME;
        long current = DomainManager.sRespDomainAdd.timestamp;
        String account = Uri.encode(username, "UTF-8");
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
                    loginSucc(token);
                }

                @Override
                public void onHttpError(ExceptionHandle.ResponeThrowable e) {
                    hideLoading();
                    showErr(e.getErrCode() + "," + e.getErrMessage());
                    e.printStackTrace();
                }
            });
        subscribeRx(d);
    }

    private void loginByImei() {
        loginType = LOGIN_IMEI;
        long current = DomainManager.sRespDomainAdd.timestamp;
        String sig = OSUtil.toMD5(OSUtil.toMD5(pwd) + current);
        String cid = AllOnlineApp.sChannelID;
        if (TextUtils.isEmpty(cid)) {
            cid = "0";
        }
        Disposable d = DataEngine.getAllMainApi()
            .loginByImei(username, current, sig, cid, GlobalParam.getInstance().getCommonParas())
            .compose(RxUtils.toMain())
            .compose(RxUtils.businessTransformer())
            .subscribeWith(new BaseSubscriber<RespToken>() {
                @Override
                public void onNext(RespToken token) {
                    loginSucc(token);
                }

                @Override
                public void onHttpError(ExceptionHandle.ResponeThrowable e) {
                    hideLoading();
                    setImei();
                    showErr(e.getErrCode() + "," + e.getErrMessage());
                    e.printStackTrace();
                }
            });
        subscribeRx(d);
    }

    private void setImei() {
        phoneOrImei.setText(username);
    }

    private void loginByPhone() {
        loginType = LOGIN_PHONE;
        if (!checkPhone()) {
            showToast(getString(R.string.act_tel_error));
            return;
        }
        String cid = AllOnlineApp.sChannelID;
        if (TextUtils.isEmpty(cid)) {
            cid = "0";
        }
        Disposable d = DataEngine.getAllMainApi()
            .loginByPhone(username, pwd, cid, GlobalParam.getInstance().getCommonParas())
            .compose(RxUtils.toMain())
            .compose(RxUtils.businessTransformer())
            .subscribeWith(new BaseSubscriber<RespToken>() {
                @Override
                public void onNext(RespToken token) {
                    loginSucc(token);
                }

                @Override
                public void onHttpError(ExceptionHandle.ResponeThrowable e) {
                    hideLoading();
                    showErr(e.getErrCode() + "," + e.getErrMessage());
                    e.printStackTrace();
                }
            });
        subscribeRx(d);
    }

    private void loginSucc(RespToken token) {
        hideLoading();
        saveToken(token.getData());
        AllOnlineApp.loginGMIm(AllOnlineApp.mApp);
        getThemeAll();
        getSpInfo();
        gotoMain();
    }

    private void saveToken(Token token) {
        AllOnlineApp.sAccount = token.account;
        if (loginType == LOGIN_USERNAME || loginType == LOGIN_IMEI) {
            AllOnlineApp.sPassword = encrypt.encrypt(Constant.SECRET_KEY, pwd);
        } else {
            AllOnlineApp.sPassword = "";
        }
        // 登录时sTarget与sAccount相同
        AllOnlineApp.sTarget = AllOnlineApp.sAccount;
        AllOnlineApp.sToken = token;
        AllOnlineApp.sToken.loginType = loginType;
        //登录成功之后获取一下账户列表
        getAccountList();
        //修改或初始化当前账户
        DeviceManager.getInstance().initCurrentSubaccount();
        // 保存最后一次用户登录的账号名称---
        PreferenceUtil.commitString(Constant.LOGIN_ACCOUNT, AllOnlineApp.sAccount);
        PreferenceUtil.commitString(Constant.LOGIN_PWD, AllOnlineApp.sPassword);
        FileUtil.saveOjbect(AllOnlineApp.mApp, token, FileUtil.getTokenPath(AllOnlineApp.mApp));
    }

    private void gotoMain() {
        SettingDataManager.getInstance(this).goToMainByMap(this, null);
        finish();
    }

    private void forgetPassword() {
        Intent i = new Intent(this, RegisterActivity.class);
        i.putExtra(RegisterActivity.UIMODE, RegisterActivity.FORGETPWD);
        startActivity(i);
    }

    private void loginByCaptcha() {
        if (uiMode == UI_PHONE) {
            uiMode = UI_CAPTCHA;
        } else {
            uiMode = UI_PHONE;
        }
        updateUi();
    }

    private void toWx() {
        if (wxApi == null) {
            initWx();
        }
        if (wxApi.isWXAppInstalled()) {
            // send oauth request
            SendAuth.Req req = new SendAuth.Req();
            req.scope = "snsapi_userinfo";
            sWxState = "login" + System.currentTimeMillis();
            req.state = sWxState;
            boolean b = wxApi.sendReq(req);
            if (!b) {
                hideLoading();
                showToast(getString(R.string.toast_launch_wx_fail));
            }
        } else {
            hideLoading();
            showToast(getString(R.string.toast_wx_not_installed));
        }
    }

    private void doWxLogin() {
        if (!TextUtils.isEmpty(sWxCode)) {
            showLoading(getString(R.string.logining));
            loginByWx();
            sWxCode = null;
        }
    }

    private void loginByWx() {
        loginType = LOGIN_WX;
        String cid = AllOnlineApp.sChannelID;
        if (TextUtils.isEmpty(cid)) {
            cid = "0";
        }
        Disposable d = DataEngine.getAllMainApi()
            .loginByWx(sWxCode, cid, GlobalParam.getInstance().getCommonParas())
            .compose(RxUtils.toMain())
            .compose(RxUtils.businessTransformer())
            .subscribeWith(new BaseSubscriber<RespToken>() {
                @Override
                public void onHttpError(ExceptionHandle.ResponeThrowable e) {
                    hideLoading();
                    showToast(e.getErrCode() + "," + e.getErrMessage());
                    e.printStackTrace();
                }

                @Override
                public void onNext(RespToken respToken) {
                    loginSucc(respToken);
                }
            });
        subscribeRx(d);
    }

    private void toScan() {
        Intent scan = new Intent(this, CaptureActivity.class);
        startActivityForResult(scan, ZConstant.REQ_QR_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ZConstant.REQ_QR_CODE && resultCode == RESULT_OK) {
            Bundle b = data.getExtras();
            if (b == null) {
                return;
            }
            String imei = b.getString(ZConstant.INTENT_EXTRA_KEY_QR_SCAN);
            username = imei;
            if (username != null && username.length() > 6) {
                pwd = username.substring(username.length() - 6);
            } else {
                pwd = "";
            }
            if (Constant.IS_DEBUG_MODE) {
                Log.d(TAG, "扫描结果：" + imei + ", pwd=" + pwd);
            }
            loginByImei();
        }
    }

    private void getAccountList() {
        Disposable d = DataEngine.getAllMainApi()
            .getAccountList(GlobalParam.getInstance().getCommonParas(), AllOnlineApp.sAccount, AllOnlineApp.sTarget,
                AllOnlineApp.sToken.access_token)
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

    private long lastBack = 0;

    @Override
    public void onBackPressed() {
        long curr = System.currentTimeMillis();
        if (curr - lastBack < 1000) {
            ActivityStateManager.finishAll();
            System.exit(0);
        } else {
            showToast(getString(R.string.exit_app));
            lastBack = System.currentTimeMillis();
        }
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
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
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
}
