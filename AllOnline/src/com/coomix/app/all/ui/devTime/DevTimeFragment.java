package com.coomix.app.all.ui.devTime;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import butterknife.ButterKnife;
import com.coomix.app.all.GlobalParam;
import com.coomix.app.all.R;
import com.coomix.app.all.ui.advance.DeviceSettingActivity;
import com.coomix.app.all.model.bean.DevMode;
import com.coomix.app.all.data.BaseSubscriber;
import com.coomix.app.all.data.DataEngine;
import com.coomix.app.all.data.ExceptionHandle;
import com.coomix.app.all.data.RxUtils;
import com.coomix.app.all.model.response.RespBase;
import com.coomix.app.all.ui.base.BaseFragment;
import io.reactivex.disposables.Disposable;

/**
 * File Description:
 *
 * @author felixqiu
 * @since 2019/1/8.
 */
public abstract class DevTimeFragment extends BaseFragment {

    protected final int MIN_INTERVAL = 5;
    private View rootView;
    protected DevMode mode;
    private Toast mToast;
    protected int currMode;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
        @Nullable Bundle savedInstanceState) {
        if (rootView != null) {
            ViewGroup p = (ViewGroup) rootView.getParent();
            if (p != null) {
                p.removeView(rootView);
            }
        } else {
            rootView = inflater.inflate(getLayoutId(), container, false);
            ButterKnife.bind(this, rootView);
            initView();
            processContent();
        }
        return rootView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    protected void setMode(int resId) {
        if (!checkTime()) {
            return;
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setMessage(getString(R.string.set_dev_mode_hint, getString(resId)));
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                getActivity().finish();
            }
        });
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                setDeviceMode();
            }
        });
        builder.show();
    }

    protected void showInvalidDlg() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setMessage(getString(R.string.plz_input_valid_time));
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                getActivity().finish();
            }
        });
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.show();
    }

    protected void setDeviceMode() {
        String token = GlobalParam.getInstance().getAccessToken();
        String content = makeContent();
        Disposable d = DataEngine.getAllMainApi()
            .setDevMode(token, mode.imei, content, currMode, GlobalParam.getInstance().getCommonParas())
            .compose(RxUtils.toMain())
            .compose(RxUtils.businessTransformer())
            .subscribeWith(new BaseSubscriber<RespBase>() {
                @Override
                public void onHttpError(ExceptionHandle.ResponeThrowable e) {
                    showToast(getString(R.string.set_dev_mode_error));
                }

                @Override
                public void onNext(RespBase respBase) {
                    DeviceSettingActivity.needRefresh = true;
                    getActivity().finish();
                }
            });
        subscribeRx(d);
    }

    protected abstract int getLayoutId();

    protected abstract void initView();

    protected abstract void processContent();

    protected abstract String makeContent();

    protected boolean checkTime() {
        return true;
    }

    protected void showToast(String msg) {
        if (!TextUtils.isEmpty(msg)) {
            if (mToast == null) {
                mToast = Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT);
            } else {
                mToast.setText(msg);
            }
            mToast.show();
        }
    }
}
