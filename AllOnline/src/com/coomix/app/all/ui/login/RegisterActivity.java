package com.coomix.app.all.ui.login;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import butterknife.BindView;
import butterknife.ButterKnife;
import com.coomix.app.all.Constant;
import com.coomix.app.all.GlobalParam;
import com.coomix.app.all.R;
import com.coomix.app.all.model.bean.Random;
import com.coomix.app.framework.util.OSUtil;
import com.coomix.app.all.data.BaseSubscriber;
import com.coomix.app.all.data.DataEngine;
import com.coomix.app.all.data.ExceptionHandle;
import com.coomix.app.all.data.RxUtils;
import com.coomix.app.all.manager.ThemeManager;
import com.coomix.app.all.model.response.RespBase;
import com.coomix.app.all.model.response.RespRandom;
import com.coomix.app.all.ui.base.BaseActivity;
import com.coomix.app.all.util.CommunityUtil;
import com.coomix.app.all.util.StringUtil;
import com.coomix.app.all.util.ViewUtil;
import com.coomix.security.Security;
import io.reactivex.disposables.Disposable;
import java.lang.ref.WeakReference;

import static com.coomix.app.framework.app.BaseApiClient.COMMUNITY_COOMIX_VERSION;

/**
 * File Description:
 *
 * @author felixqiu
 * @since 2018/8/4.
 */
public class RegisterActivity extends BaseActivity implements View.OnClickListener {

    @BindView(R.id.back)
    TextView back;
    @BindView(R.id.title)
    TextView title;
    @BindView(R.id.phone)
    EditText phone;
    @BindView(R.id.captcha)
    EditText captcha;
    @BindView(R.id.getCaptcha)
    TextView getCaptcha;
    @BindView(R.id.next)
    TextView next;

    private static final int COUNTDOWN = 0;
    private static final int COUNT_PERIOD = 1000;
    private static RCountHandler rh;

    private String phoneNum;
    private String captchaNum;

    public static final String UIMODE = "ui_mode";
    //注册界面
    public static final int REGISTER = 0;
    //忘记密码界面
    public static final int FORGETPWD = 1;
    private int uiMode = REGISTER;

    static class RCountHandler extends Handler {
        WeakReference<RegisterActivity> a;

        public RCountHandler(RegisterActivity a) {
            this.a = new WeakReference<>(a);
        }

        @Override
        public void handleMessage(Message msg) {
            RegisterActivity l = a.get();
            if (l == null) {
                return;
            }
            int time = msg.arg1;
            switch (msg.what) {
                case COUNTDOWN:
                    Message m = Message.obtain();
                    m.what = COUNTDOWN;
                    m.arg1 = time - 1;
                    rh.sendMessageDelayed(m, COUNT_PERIOD);
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
        setContentView(R.layout.activity_register);
        ButterKnife.bind(this);

        rh = new RCountHandler(this);
        initListener();
        initUi();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (rh != null) {
            rh.removeCallbacksAndMessages(null);
        }
    }

    private void initListener() {
        back.setOnClickListener(this);
        getCaptcha.setOnClickListener(this);
        next.setOnClickListener(this);
        phone.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (!TextUtils.isEmpty(phone.getText().toString())
                    && !TextUtils.isEmpty(captcha.getText().toString())) {
                    setNext(true);
                } else {
                    setNext(false);
                }
            }
        });
        captcha.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (!TextUtils.isEmpty(phone.getText().toString())
                    && !TextUtils.isEmpty(captcha.getText().toString())) {
                    setNext(true);
                } else {
                    setNext(false);
                }
            }
        });
    }

    private void setNext(boolean enable) {
        if (enable) {
            ViewUtil.setBg(next, ThemeManager.getInstance().getBGColorDrawable(this));
        } else {
            ViewUtil.setBg(next, ThemeManager.getInstance().getDefaultDrawable(this));
        }
    }

    private void initUi() {
        Intent i = getIntent();
        if (i != null && i.hasExtra(UIMODE)) {
            uiMode = i.getIntExtra(UIMODE, REGISTER);
        }
        switch (uiMode) {
            case REGISTER:
                title.setText(R.string.register_new);
                phone.setHint(R.string.register_phone_hint);
                break;
            case FORGETPWD:
                title.setText(R.string.forget_password);
                phone.setHint(R.string.bindphone_phonenum_hint);
                break;
            default:
                break;
        }
    }

    private void startCount() {
        Message m = Message.obtain(rh, COUNTDOWN);
        m.arg1 = 60;
        m.sendToTarget();
    }

    private void updateCount(int time) {
        if (time == 0) {
            rh.removeCallbacksAndMessages(null);
            getCaptcha.setEnabled(true);
            getCaptcha.setText(R.string.register_get_captcha);
        } else {
            String str = getString(R.string.bindphone_sms_code_countdown_text, time);
            getCaptcha.setText(str);
            getCaptcha.setEnabled(false);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.back:
                finish();
                break;
            case R.id.getCaptcha:
                getCaptcha();
                break;
            case R.id.next:
                toSetPwd();
                break;
            default:
                break;
        }
    }

    private void getCaptcha() {
        if (checkPhone()) {
            startCount();
            getCaptchaRandom();
        } else {
            showToast(getString(R.string.act_tel_error));
        }
    }

    private boolean checkPhone() {
        phoneNum = phone.getText().toString().trim();
        return StringUtil.isPhoneValid(phoneNum);
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
        String telsec = new Security().getSecInfo(phoneNum + "|" + random.random, encryptStr, COMMUNITY_COOMIX_VERSION);
        Disposable d = DataEngine.getAllMainApi()
            .applyCaptcha(phoneNum, telsec, GlobalParam.getInstance().getCommonParas())
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

    private void toSetPwd() {
        if (checkPhone() && checkCaptcha()) {
            Intent i = new Intent(this, SetPwdActivity.class);
            Bundle b = new Bundle();
            b.putString(SetPwdActivity.PHONE, phoneNum);
            b.putString(SetPwdActivity.CAPTCHA, captchaNum);
            b.putInt(UIMODE, uiMode);
            i.putExtras(b);
            startActivity(i);
        } else {
            showToast(getString(R.string.bindphone_toast_smscode_error));
        }
    }

    private boolean checkCaptcha() {
        captchaNum = captcha.getText().toString().trim();
        return captchaNum.length() == 6 && CommunityUtil.isNumeric(captchaNum);
    }
}
