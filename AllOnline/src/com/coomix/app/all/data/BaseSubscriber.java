package com.coomix.app.all.data;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.coomix.app.all.AllOnlineApp;
import com.coomix.app.all.R;
import com.coomix.app.all.log.GoomeLog;
import com.coomix.app.all.service.CommonService;
import com.coomix.app.all.ui.base.BaseView;
import com.coomix.app.all.util.AppUtil;

import io.reactivex.subscribers.DisposableSubscriber;

import static com.coomix.app.all.data.ExceptionHandle.BusinessCode.ERR_EXIRPED_SESSION;
import static com.coomix.app.all.data.ExceptionHandle.BusinessCode.ERR_INVALID_SESSION;

/**
 * Created by ly on 2017/9/26 11:06.
 */
public abstract class BaseSubscriber<T> extends DisposableSubscriber<T>  {

    private BaseView baseView;

    public BaseSubscriber(@Nullable BaseView v) {
        baseView = v;
    }
    public BaseSubscriber() {
    }

    @Override
    public void onError(Throwable e) {
        if(baseView != null) {
            baseView.hideLoading();
        }

        // to log error
        GoomeLog.getInstance().logE(BaseSubscriber.class.getSimpleName(),e.getStackTrace().toString() ,0);

        if (e instanceof ExceptionHandle.ServerException) {
            onBusinessError((ExceptionHandle.ServerException)e);
        } else if (e instanceof ExceptionHandle.ResponeThrowable) {
            onHttpError((ExceptionHandle.ResponeThrowable) e);
        } else {
            onHttpError(new ExceptionHandle.ResponeThrowable(e, ExceptionHandle.ERROR.UNKNOWN));
        }
    }

    @Override
    public void onComplete() {
        if(baseView != null) {
            baseView.hideLoading();
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        if (!NetworkUtils.isNetworkAvailable(AllOnlineApp.mApp)) {
            if(baseView != null) {
                baseView.showErr(AllOnlineApp.mApp.getString(R.string.network_error));
            }
            onComplete();
            dispose();
        } else if(baseView != null) {
            baseView.showLoading(null);
        }
    }

    public void onBusinessError(@NonNull ExceptionHandle.ServerException e) {
        // 对特殊的错误，特殊处理；否者直接显示给用户错误代码
        switch (e.code) {
            case 4004:
                break;

            case ERR_INVALID_SESSION:
                //被抢绑，重启社区和环信
                AppUtil.showWeinxinUndindDialog(AllOnlineApp.mApp,500);
                break;

            case ERR_EXIRPED_SESSION:
                CommonService.loginCommunityInBackground(AllOnlineApp.mApp, true);
                return;// Return inorder to show nothing,

            case ExceptionHandle.BusinessCode.ERROR_TOKEN_EXPIRE:
                AppUtil.reLogin();
                break;

            default:
                break;
        }

        ExceptionHandle.ResponeThrowable ex = new ExceptionHandle.ResponeThrowable(e, e.code);
        ex.message = e.message;
        onHttpError(ex);
    }

    public abstract void onHttpError(ExceptionHandle.ResponeThrowable e);

}
