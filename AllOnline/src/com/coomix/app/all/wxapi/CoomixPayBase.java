package com.coomix.app.all.wxapi;

import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.Toast;
import com.coomix.app.all.common.Keys;
import com.coomix.app.all.service.ServiceAdapter;
import com.coomix.app.all.service.ServiceAdapter.ServiceAdapterCallback;
import com.coomix.app.all.dialog.ProgressDialogEx;
import com.tencent.mm.opensdk.modelbase.BaseReq;
import com.tencent.mm.opensdk.modelbase.BaseResp;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.IWXAPIEventHandler;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;

/**
 * Created by ssl on 2017/2/18.
 */
public abstract class CoomixPayBase implements ServiceAdapterCallback {
    protected ProgressDialogEx progressDialogEx;
    protected Context mContext;
    private IWXAPI api;
    protected ServiceAdapter serviceAdapter;
    protected View mainView;
    private FinishListener finishListener;

    public CoomixPayBase(Context context, Intent intent, FinishListener finishListener) {
        this.mContext = context;

        this.finishListener = finishListener;

        initServerRequest();
    }

    public void initApi(Intent intent, IWXAPIEventHandler listener) {
        api = WXAPIFactory.createWXAPI(mContext, Keys.WEIXIN_APP_ID, false);
        api.registerApp(Keys.WEIXIN_APP_ID);
        api.handleIntent(intent, listener);
    }

    public void showWaitInfoDialog(Context context, String text) {
        progressDialogEx = new ProgressDialogEx(context);
        progressDialogEx.setAutoDismiss(false);
        progressDialogEx.setCancelOnTouchOutside(false);

        try {
            progressDialogEx.show(text);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void dismissWaitDialog() {
        if (progressDialogEx != null && progressDialogEx.isShowing()) {
            progressDialogEx.dismiss();
        }
    }

    public void release() {
        if (serviceAdapter != null) {
            serviceAdapter.unregisterServiceCallBack(this);
        }
        if (finishListener != null) {
            finishListener = null;
        }
    }

    public void showToast(Context context, int strId) {
        Toast.makeText(context, strId, Toast.LENGTH_SHORT).show();
    }

    public void showToast(Context context, String str) {
        Toast.makeText(context, str, Toast.LENGTH_SHORT).show();
    }

    public interface FinishListener {
        public void toFinish();
    }

    protected void finish() {
        if (finishListener != null) {
            finishListener.toFinish();
        }
    }

    protected void initServerRequest() {
        if (serviceAdapter == null) {
            serviceAdapter = ServiceAdapter.getInstance(mContext);
            serviceAdapter.registerServiceCallBack(this);
        }
    }

    public abstract View getMainView();

    protected abstract void onNewIntent(Intent intent);

    public abstract void onReq(BaseReq baseReq);

    public abstract void onResp(BaseResp baseResp);

    protected abstract void onBackFinish();

    protected abstract void onBackPressed();
}
