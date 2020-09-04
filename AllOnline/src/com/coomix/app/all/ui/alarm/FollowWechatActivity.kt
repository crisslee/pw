package com.coomix.app.all.ui.alarm

import android.os.Bundle
import android.text.TextUtils
import android.view.View
import com.coomix.app.all.AllOnlineApp
import com.coomix.app.all.GlobalParam
import com.coomix.app.all.R
import com.coomix.app.all.common.Keys
import com.coomix.app.all.data.BaseSubscriber
import com.coomix.app.all.data.DataEngine
import com.coomix.app.all.data.ExceptionHandle.ResponeThrowable
import com.coomix.app.all.data.RxUtils
import com.coomix.app.all.model.response.RespBase
import com.coomix.app.all.model.response.RespBindedWx
import com.coomix.app.all.ui.base.BaseActivity
import com.coomix.app.all.ui.login.LoginActivity
import com.tencent.mm.opensdk.modelbiz.WXLaunchMiniProgram
import com.tencent.mm.opensdk.modelmsg.SendAuth
import com.tencent.mm.opensdk.openapi.IWXAPI
import com.tencent.mm.opensdk.openapi.WXAPIFactory
import kotlinx.android.synthetic.main.activity_follow_wechat.afterFollow
import kotlinx.android.synthetic.main.activity_follow_wechat.toWx

const val FIRST_OPEN = "first_open"

/**
 * File Description:
 *
 * @author felixqiu
 * @since 2020-01-02.
 */
class FollowWechatActivity : BaseActivity() {

    companion object {
        var wxCode = ""
    }

    val api: IWXAPI = WXAPIFactory.createWXAPI(this, Keys.WEIXIN_APP_ID)
    val config = AllOnlineApp.getAppConfig()
    var first = true
    var towxAuth = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_follow_wechat)
        initView()
    }

    override fun onResume() {
        super.onResume()
        if (first) {
            afterFollow.visibility = View.GONE
            first = false
        } else {
            afterFollow.visibility = View.VISIBLE
        }
        if (towxAuth && !TextUtils.isEmpty(wxCode)) {
            bindWx()
            towxAuth = false
            wxCode = ""
        }
    }

    private fun initView() {
        toWx.setOnClickListener() {
            if (api.isWXAppInstalled) {
                if (!api.isWXAppSupportAPI) {
                    showToast("不支持此行为!")
                    return@setOnClickListener
                }
                val req = WXLaunchMiniProgram.Req()
                req.userName = Keys.WEIXIN_MINIPROGRAM_ID
                req.path = config.mini_program_offical_path
                req.miniprogramType = WXLaunchMiniProgram.Req.MINIPTOGRAM_TYPE_RELEASE
                if (!api.sendReq(req)) {
                    showToast(getString(R.string.toast_launch_wx_fail))
                }
            } else {
                showToast(getString(R.string.toast_wx_not_installed))
            }
        }
        afterFollow.setOnClickListener() {
            if (isWxLogin()) {
                getBind()
            } else {
                if (api.isWXAppInstalled) {
                    val req = SendAuth.Req()
                    req.scope = "snsapi_userinfo"
                    req.state = "follow" + System.currentTimeMillis()
                    if (!api.sendReq(req)) {
                        showToast(getString(R.string.toast_launch_wx_fail))
                        return@setOnClickListener
                    }
                    towxAuth = true
                } else {
                    showToast(getString(R.string.toast_wx_not_installed))
                }
            }
        }
    }

    private fun bindWx() {
        val token = GlobalParam.getInstance().accessToken
        val d = DataEngine.getAllMainApi().bindWx(wxCode, token, GlobalParam.getInstance().commonParas)
            .compose(RxUtils.businessTransformer())
            .compose(RxUtils.toMain())
            .subscribeWith(object : BaseSubscriber<RespBase>() {
                override fun onNext(t: RespBase?) {
                    showToast(getString(R.string.wx_bind_succ))
                    finish()
                }

                override fun onHttpError(e: ResponeThrowable?) {
                    showToast(e?.errCodeMessage)
                }
            })
        subscribeRx(d)
    }

    private fun getBind() {
        val token = GlobalParam.getInstance().accessToken
        var unionId = ""
        if (isWxLogin()) {
            unionId = AllOnlineApp.sToken?.account ?: ""
        }
        val d = DataEngine.getAllMainApi().getBindWx(token, unionId, GlobalParam.getInstance().commonParas)
            .compose(RxUtils.businessTransformer())
            .compose(RxUtils.toMain())
            .subscribeWith(object : BaseSubscriber<RespBindedWx>() {
                override fun onNext(t: RespBindedWx?) {
                    if (t != null && (!TextUtils.isEmpty(t.data.login_name) || !TextUtils.isEmpty(t.data.imei))) {
                        showToast(getString(R.string.wx_bind_succ))
                        finish()
                    }
                }

                override fun onHttpError(e: ResponeThrowable?) {
                    showToast(e?.errCodeMessage)
                }
            })
        subscribeRx(d)
    }

    private fun isWxLogin(): Boolean {
        return AllOnlineApp.sToken?.loginType == LoginActivity.LOGIN_WX
    }
}