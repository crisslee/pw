package com.coomix.app.all.wxapi;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.Toast;
import com.coomix.app.all.R;
import com.coomix.app.framework.app.Result;
import com.coomix.app.pay.PayResultManager;
import com.tencent.mm.opensdk.constants.ConstantsAPI;
import com.tencent.mm.opensdk.modelbase.BaseReq;
import com.tencent.mm.opensdk.modelbase.BaseResp;

/**
 * Created by think on 2017/2/18.
 */

public class CoomixPayRedPacket extends CoomixPayBase
{
    public CoomixPayRedPacket(Context context, Intent intent, FinishListener finishListaner)
    {
        super(context, intent, finishListaner);
    }

    @Override
    public View getMainView()
    {
        return null;
    }

    @Override
    protected void onNewIntent(Intent intent)
    {

    }

    @Override
    public void onReq(BaseReq baseReq)
    {

    }

    @Override
    public void onResp(BaseResp baseResp)
    {
        if (baseResp.getType() == ConstantsAPI.COMMAND_PAY_BY_WX)
        {
            int result = 0;
            switch (baseResp.errCode)
            {
                case BaseResp.ErrCode.ERR_OK:
                    //红包支付--发送广播通知
                    redpacketPayedOk();
                    return;

                case BaseResp.ErrCode.ERR_USER_CANCEL:
                    result = R.string.wx_errcode_cancel;
                    break;

                case BaseResp.ErrCode.ERR_AUTH_DENIED:
                    result = R.string.wx_errcode_deny;
                    break;

                case BaseResp.ErrCode.ERR_UNSUPPORT:
                    result = R.string.wx_errcode_unsupported;
                    break;

                default:
                    //result = R.string.wx_errcode_unknown;
                    break;
            }
            if (result > 0)
            {
                Toast.makeText(mContext, result, Toast.LENGTH_SHORT).show();
            }
            finish();
        }
    }

    private void redpacketPayedOk()
    {
        onBackFinish();
        PayResultManager.getInstance().setRedPacketPayedOk(true);
        finish();
    }

    @Override
    protected void finish()
    {
        super.finish();
    }

    @Override
    public void release()
    {
        super.release();

    }

    @Override
    protected void onBackFinish()
    {
        Activity activity = PayResultManager.getInstance().getWillFinishActivity();
        if (activity != null)
        {
            //结束掉确认支付界面
            activity.setResult(Activity.RESULT_OK);
            activity.finish();
            PayResultManager.getInstance().setWillFinishActivity(null);
        }
    }

//    @Override
//    public void callback(Response response)
//    {
//
//    }

    @Override
    protected void onBackPressed()
    {
    }

    @Override
    public void callback(int messageId, Result result) {

    }
}
