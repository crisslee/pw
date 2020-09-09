package com.coomix.app.all.ui.base;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;
import com.coomix.app.all.Constant;
import com.coomix.app.all.R;
import com.coomix.app.all.manager.ActivityStateManager;
import com.coomix.app.all.manager.ThemeManager;
import com.coomix.app.all.util.AppUtil;
import com.coomix.app.all.util.StatusBarUtil;
import com.tbruyelle.rxpermissions2.RxPermissions;
import com.umeng.analytics.MobclickAgent;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import java.util.Calendar;

/**
 * 新的基类
 * mCall 是网络请求的指针
 * Created by ly on 2017/5/18.
 */
public abstract class BaseActivity extends AppCompatActivity implements BaseView {

    protected CompositeDisposable mCompositeDisposable;
    private long lastClickTime = 0;
    private static final long MIN_CLICK_SPAN = 1000;
    protected volatile ProgressDialog progressDialog;
    private Toast mToast;
    protected RxPermissions rxPermissions;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        rxPermissions = new RxPermissions(this);
        ActivityStateManager.onCreate(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        ActivityStateManager.onResume(this);
        MobclickAgent.onResume(this);
        AppUtil.showWeinxinUndindDialog();
    }

    @Override
    protected void onPause() {
        super.onPause();
        hideToast();
        MobclickAgent.onPause(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        ActivityStateManager.onStop(this);
        AppUtil.hideWeinxinUndindDialog();
    }

    @Override
    protected void onDestroy() {
        ActivityStateManager.onDestroy(this);
        try {
            unsubscribeRx();
        } catch (Exception e) {
            if (Constant.IS_DEBUG_MODE) {
                e.printStackTrace();
            }
        }
        super.onDestroy();
    }

    @Override
    public void setContentView(int layoutResID) {
        super.setContentView(layoutResID);
        StatusBarUtil.setColor(this, ThemeManager.getInstance().getThemeAll().getThemeColor().getColorWhole());
    }

    @Override
    public void setContentView(View view) {
        super.setContentView(view);
        if(this instanceof MainActivityParent) {
            StatusBarUtil.setTransparent(this);
            StatusBarUtil.setTextDark(this, true);
        } else {
            StatusBarUtil.setColor(this, ThemeManager.getInstance().getThemeAll().getThemeColor().getColorWhole());
        }
    }

    public void subscribeRx(Disposable disposable) {
        if (mCompositeDisposable == null) {
            mCompositeDisposable = new CompositeDisposable();
        }
        mCompositeDisposable.add(disposable);
    }

    protected void unsubscribeRx() {
        if (mCompositeDisposable != null && mCompositeDisposable.size() > 0) {
            mCompositeDisposable.clear();
        }
    }

    protected boolean ifClickTooFast() {
        long currentTime = Calendar.getInstance().getTimeInMillis();
        boolean tooFast = (currentTime - lastClickTime < MIN_CLICK_SPAN);
        lastClickTime = currentTime;
        return tooFast;
    }

    public void showProgressDialog() {
        showProgressDialog(getString(R.string.sys_loading));
    }

    public synchronized void showProgressDialog(CharSequence message) {
        runOnUiThread(() -> {
            if (progressDialog == null) {
                progressDialog = new ProgressDialog(BaseActivity.this);
                progressDialog.setCancelable(true);// 设置是否可以通过点击Back键取消
                progressDialog.setCanceledOnTouchOutside(false);// 设置在点击Dialog外是否取消Dialog进度条
            }
            progressDialog.setMessage(message);
            if (!progressDialog.isShowing()) {
                progressDialog.show();
            }
        });
    }

    public synchronized void dismissProgressDialog() {
        runOnUiThread(() -> {
            if (progressDialog != null && progressDialog.isShowing()) {
                progressDialog.dismiss();
            }
        });
    }

    @Override
    public void hideLoading() {
        dismissProgressDialog();
    }

    @Override
    public void showLoading(String msg) {
        if (TextUtils.isEmpty(msg)) {
            showProgressDialog();
        } else {
            showProgressDialog(msg);
        }
    }

    @Override
    public void showErr(String msg) {
        showToast(msg);
    }

    public void showToast(String msg) {
        runOnUiThread(() -> {
            if (!TextUtils.isEmpty(msg)) {
                if (mToast == null) {
                    mToast = Toast.makeText(BaseActivity.this, msg, Toast.LENGTH_SHORT);
                } else {
                    mToast.setText(msg);
                }
                mToast.show();
            }
        });
    }

    public void showToast(int resId, int duration) {
        if (mToast == null) {
            mToast = Toast.makeText(BaseActivity.this, resId, duration);
        } else {
            mToast.setText(resId);
        }
        mToast.show();
    }

    private void hideToast() {
        if (mToast != null) {
            mToast.cancel();
        }
    }

    protected void showSettingDlg(String msg, DialogInterface.OnClickListener okListener) {
        showSettingDlg(msg, okListener, null);
    }

    protected void showSettingDlg(String msg, DialogInterface.OnClickListener okListener,
        DialogInterface.OnClickListener cancelListener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(msg);
        builder.setPositiveButton("去设置", okListener).setNegativeButton("取消", cancelListener);
        builder.setCancelable(false);
        builder.create().show();
    }
}
