package com.coomix.app.all.ui.login;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import butterknife.BindView;
import butterknife.ButterKnife;
import com.coomix.app.all.AllOnlineApp;
import com.coomix.app.all.GlobalParam;
import com.coomix.app.all.R;
import com.coomix.app.framework.util.OSUtil;
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
 * @since 2018/8/20.
 */
public class InputImeiActivity extends BaseActivity implements View.OnClickListener {
    @BindView(R.id.back)
    TextView back;
    @BindView(R.id.inputImei)
    EditText etImei;
    @BindView(R.id.bindImei)
    TextView bind;

    String imei;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_input_imei);
        ButterKnife.bind(this);

        initListener();
    }

    private void initListener() {
        back.setOnClickListener(this);
        bind.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.back:
                finish();
                break;
            case R.id.bindImei:
                bindImei();
                break;
            default:
                break;
        }
    }

    private void bindImei() {
        if (checkImei()) {
            showLoading(getString(R.string.bindphone_toast_binding));
            bind();
        }
    }

    private boolean checkImei() {
        imei = etImei.getText().toString();
        return true;
    }

    private void bind() {
        String token = AllOnlineApp.sToken.access_token;
        String account = AllOnlineApp.sAccount;
        long time = System.currentTimeMillis() / 1000;
        String sig = OSUtil.toMD5(imei + time + "Goome");
        Disposable d = DataEngine.getAllMainApi()
            .bindDevice(token, account, imei, time, sig, GlobalParam.getInstance().getCommonParas())
            .compose(RxUtils.toMain())
            .compose(RxUtils.businessTransformer())
            .subscribeWith(new BaseSubscriber<RespBase>() {
                @Override
                public void onHttpError(ExceptionHandle.ResponeThrowable e) {
                    hideLoading();
                    showToast(e.getErrCode() + "," + e.getErrMessage());
                }

                @Override
                public void onNext(RespBase respBase) {
                    hideLoading();
                    showToast(getString(R.string.bindphone_toast_bind_suc));
                    setResult(RESULT_OK);
                    finish();
                }
            });
        subscribeRx(d);
    }
}
