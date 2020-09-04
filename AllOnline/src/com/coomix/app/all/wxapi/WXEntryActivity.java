package com.coomix.app.all.wxapi;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;
import com.coomix.app.all.Constant;
import com.coomix.app.all.R;
import com.coomix.app.all.log.GLog;
import com.coomix.app.all.ui.alarm.FollowWechatActivity;
import com.coomix.app.all.ui.login.LoginActivity;
import com.tencent.mm.opensdk.modelbase.BaseReq;
import com.tencent.mm.opensdk.modelbase.BaseResp;
import com.tencent.mm.opensdk.modelmsg.SendAuth;
import com.umeng.socialize.weixin.view.WXCallbackActivity;

public class WXEntryActivity extends WXCallbackActivity {
    private static final String TAG = WXEntryActivity.class.getSimpleName();

    @Override
    public void onResp(BaseResp resp) {
        Log.i("felix", "type=" + resp.getType());
        SendAuth.Resp r = (SendAuth.Resp) resp;
        int error = r.errCode;
        String code = r.code;
        Log.d(TAG, "微信登录返回onResp error=" + error + ", code=" + code);
        super.onResp(resp);
    }

    @Override
    public void onReq(BaseReq req) {
        if (Constant.IS_DEBUG_MODE) {
            Log.d(TAG, "微信登录返回onReq: " + req);
        }
        super.onReq(req);
    }

    @Override
    protected void handleIntent(Intent intent) {
        if (Constant.IS_DEBUG_MODE) {
            Log.d(TAG, "微信登录返回handleIntent: " + intent);
        }
        // 使用了umeng分享，登录执行handleIntent，不执行onResp
        try {
            Bundle b = intent.getExtras();
            if (b == null) {
                return;
            }
            String state = b.getString("_wxapi_sendauth_resp_state");
            int errCode = b.getInt("_wxapi_baseresp_errcode");
            String token = b.getString("_wxapi_sendauth_resp_token");
            GLog.i(TAG, "微信授权返回errcode=" + errCode + ", state=" + state + ", token=" + token);
            if (LoginActivity.sWxState.equals(state)) {
                if (errCode == 0) {
                    // 即为所需的code
                    LoginActivity.sWxCode = token;
                    LoginActivity.needWxLogin = true;
                } else {
                    Toast.makeText(this, R.string.toast_wechat_login_failed, Toast.LENGTH_SHORT).show();
                    LoginActivity.sWxCode = null;
                }
            } else if (!TextUtils.isEmpty(state) && state.contains("follow")) {
                if (errCode == 0) {
                    FollowWechatActivity.Companion.setWxCode(token);
                } else {
                    Toast.makeText(this, R.string.toast_wechat_login_failed, Toast.LENGTH_SHORT).show();
                    LoginActivity.sWxCode = null;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        //super.handleIntent(intent);
        finish();
    }
}
