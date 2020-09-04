package com.coomix.app.all.ui.login;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import butterknife.BindView;
import butterknife.ButterKnife;
import com.coomix.app.all.Constant;
import com.coomix.app.all.GlobalParam;
import com.coomix.app.all.R;
import com.coomix.app.all.data.BaseSubscriber;
import com.coomix.app.all.data.DataEngine;
import com.coomix.app.all.data.ExceptionHandle;
import com.coomix.app.all.data.RxUtils;
import com.coomix.app.all.model.response.RespBase;
import com.coomix.app.all.ui.base.BaseActivity;
import io.reactivex.disposables.Disposable;

/**
 * File Description:
 *
 * @author felixqiu
 * @since 2018/8/14.
 */
public class SetPwdActivity extends BaseActivity implements View.OnClickListener {
    private static final String TAG = SetPwdActivity.class.getSimpleName();

    public static final String PHONE = "phone";
    public static final String CAPTCHA = "captcha";

    @BindView(R.id.back)
    ImageView close;
    @BindView(R.id.title)
    TextView title;
    @BindView(R.id.password)
    EditText etPwd;
    @BindView(R.id.confirmPwd)
    EditText etConfirmPwd;
    @BindView(R.id.deviceNick)
    EditText etDevice;
    @BindView(R.id.register)
    TextView tvRegister;

    private String phone;
    private String captcha;
    private int uiMode = RegisterActivity.REGISTER;

    private String password;
    private String confirmPwd;
    private String deviceNick;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_password);
        ButterKnife.bind(this);

        initListener();
        initData();
        initUi();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private void initListener() {
        close.setOnClickListener(this);
        tvRegister.setOnClickListener(this);
    }

    private void initData() {
        Intent i = getIntent();
        if (i != null && i.getExtras() != null) {
            Bundle b = i.getExtras();
            phone = b.getString(PHONE);
            captcha = b.getString(CAPTCHA);
            uiMode = b.getInt(RegisterActivity.UIMODE);
        }
        if (TextUtils.isEmpty(phone) || TextUtils.isEmpty(captcha)) {
            finish();
        }
    }

    private void initUi() {
        switch (uiMode) {
            case RegisterActivity.REGISTER:
                title.setText(R.string.register_set_pwd);
                etPwd.setHint(R.string.register_set_pwd_hint);
                etDevice.setVisibility(View.VISIBLE);
                tvRegister.setText(R.string.register);
                break;
            case RegisterActivity.FORGETPWD:
                title.setText(R.string.reset_pwd);
                etPwd.setHint(R.string.new_pwd);
                etDevice.setVisibility(View.GONE);
                tvRegister.setText(R.string.baidu_navi_sure);
                break;
            default:
                break;
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.back:
                finish();
                break;
            case R.id.register:
                checkInfo();
                break;
            default:
                break;
        }
    }

    private void checkInfo() {
        password = etPwd.getText().toString();
        confirmPwd = etConfirmPwd.getText().toString();
        deviceNick = etDevice.getText().toString();
        if (password.length() < 6) {
            showToast(getString(R.string.pwd_tooshort));
            return;
        } else if (password.length() > 20) {
            showToast(getString(R.string.pwd_toolong));
            return;
        }
        if (!confirmPwd.equals(password)) {
            showToast(getString(R.string.pwd_different));
            return;
        }
        if (uiMode == RegisterActivity.REGISTER) {
            register();
        } else {
            modifyPwd();
        }
    }

    private void register() {
        Disposable d = DataEngine.getAllMainApi()
            .appRegister(phone, captcha, password, deviceNick, GlobalParam.getInstance().getCommonParas())
            .compose(RxUtils.toMain())
            .compose(RxUtils.businessTransformer())
            .subscribeWith(new BaseSubscriber<RespBase>() {
                @Override
                public void onHttpError(ExceptionHandle.ResponeThrowable e) {
                    showToast(e.getErrCode() + "," + e.getErrMessage());
                }

                @Override
                public void onNext(RespBase respBase) {
                    if (respBase.isSuccess()) {
                        toLogin();
                    } else {
                        showToast(respBase.getErrcode() + "," + respBase.getMsg());
                    }
                }
            });
        subscribeRx(d);
    }

    private void toLogin() {
        Intent i = new Intent(this, LoginActivity.class);
        Bundle b = new Bundle();
        b.putString(LoginActivity.PHONE_NUM, phone);
        b.putString(LoginActivity.PASSWORD_NUM, password);
        i.putExtras(b);
        startActivity(i);
    }

    private void modifyPwd() {
        Disposable d = DataEngine.getAllMainApi()
            .modifyPwd(phone, captcha, password, GlobalParam.getInstance().getCommonParas())
            .compose(RxUtils.toMain())
            .compose(RxUtils.businessTransformer())
            .subscribeWith(new BaseSubscriber<RespBase>() {
                @Override
                public void onHttpError(ExceptionHandle.ResponeThrowable e) {
                    if (Constant.IS_DEBUG_MODE) {
                        Log.d(TAG, "onHttpError");
                    }
                    showToast(e.getErrCode() + "," + e.getErrMessage());
                }

                @Override
                public void onNext(RespBase respBase) {
                    if (Constant.IS_DEBUG_MODE) {
                        Log.i(TAG, "onNext, base=" + respBase);
                    }
                    if (respBase.isSuccess()) {
                        toLogin();
                    } else {
                        showToast(respBase.getErrcode() + "," + respBase.getMsg());
                    }
                }
            });
        subscribeRx(d);
    }
}
