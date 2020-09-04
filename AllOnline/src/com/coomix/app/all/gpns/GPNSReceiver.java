package com.coomix.app.all.gpns;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import com.coomix.app.all.AllOnlineApp;
import com.coomix.app.all.Constant;
import com.coomix.app.all.GlobalParam;
import com.coomix.app.all.data.BaseSubscriber;
import com.coomix.app.all.data.DataEngine;
import com.coomix.app.all.data.ExceptionHandle;
import com.coomix.app.all.data.RxUtils;
import com.coomix.app.all.manager.DomainManager;
import com.coomix.app.all.model.bean.Token;
import com.coomix.app.all.model.response.RespToken;
import com.coomix.app.all.util.StringUtil;
import com.goome.gpns.GPNSInterface;
import com.goome.gpns.noti.NotifyManager;
import com.goome.gpns.service.ChannelIdInterface;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * 客户端自定义广播 接收推送消息广播
 *
 * @author goome
 */
public class GPNSReceiver extends BroadcastReceiver {
    private static final String TAG = GPNSReceiver.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        Bundle bundle = intent.getExtras();
        String action = intent.getAction();

        if (GPNSInterface.ACTION_CHANNEL_ID.equals(action)) {
            if (bundle == null) {
                return;
            }
            String channelId = bundle.getString(GPNSInterface.CHANNEL_ID);
            if(!TextUtils.isEmpty(channelId)){
                AllOnlineApp.sChannelID = channelId;
            }else{
                AllOnlineApp.sChannelID = String.valueOf(ChannelIdInterface.getChannelIdFromFile());
            }

            SharedPreferences sharedPrefs = context.getSharedPreferences(Constant.PREF_UNIQUE_GPNS_REGID,
                    Context.MODE_PRIVATE);
            Editor editor = sharedPrefs.edit();
            editor.putString(Constant.PREF_UNIQUE_GPNS_REGID, channelId);
            editor.commit();

            // 已经登录再调一次接口上传通道id
            String token = AllOnlineApp.sToken.access_token;
            if (!TextUtils.isEmpty(token)) {
                if (Constant.IS_DEBUG_MODE) {
                    Log.d(TAG, "GpnsReceiver refresh token----------");
                }
                new Thread(bindRun).start();
            }
        } else if (GPNSInterface.ACTION_NOTIFICATION_OPENED.equals(action)) {
            if (bundle == null) {
                return;
            }
            String rawMsg = bundle.getString(GPNSInterface.RAW_PUSH_MSG);
            String content = bundle.getString(GPNSInterface.CONTENT);
            Intent targetIntent = new Intent(context, FilterActivity.class);
            targetIntent.putExtra(Constant.GPNS_MSG, rawMsg);
            targetIntent.putExtra("same_msg", content);
            targetIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(targetIntent);
        } else if (GPNSInterface.ACTION_MESSAGE_RECEIVED.equals(action)) {
            //GPNSIntentService.run(context, intent);
            sendNoti(context, intent);
        }
    }

    private void sendNoti(Context context, Intent intent) {
        String rawMsg = intent.getStringExtra(GPNSInterface.RAW_PUSH_MSG);
        if (StringUtil.isTrimEmpty(rawMsg)) {
            return;
        }
        try {
            JSONObject object = new JSONObject(rawMsg);
            object = object.optJSONObject("extras");
            if (object == null) {
                return;
            }
            boolean exist = object.has("source");
            NotifyManager.showNotify(context, rawMsg, NotifyManager.MSG_TYPE_REMOTE, exist);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    Runnable bindRun = new Runnable() {
        @Override
        public void run() {
            if (DomainManager.sRespDomainAdd.timestamp == 0
                    || StringUtil.isTrimEmpty(DomainManager.sRespDomainAdd.domainMain)) {
                return;
            }
            CompositeDisposable cd = new CompositeDisposable();
            refreshToken(cd);
        }
    };

    private void refreshToken(CompositeDisposable cd) {
        Token t = AllOnlineApp.sToken;
        if (t == null || TextUtils.isEmpty(t.access_token)) {
            return;
        }
        String cid = AllOnlineApp.sChannelID;
        if (TextUtils.isEmpty(cid)) {
            cid = "0";
        }
        Disposable d = DataEngine.getAllMainApi()
            .refreshToken(t.access_token, cid, GlobalParam.getInstance().getCommonParas())
            .compose(RxUtils.toIO())
            .compose(RxUtils.businessTransformer())
            .subscribeWith(new BaseSubscriber<RespToken>() {
                @Override
                public void onNext(RespToken respToken) {
                    clean(cd);
                }

                @Override
                public void onHttpError(ExceptionHandle.ResponeThrowable e) {
                    clean(cd);
                    e.printStackTrace();
                }
            });
        cd.add(d);
    }

    private void clean(CompositeDisposable cd) {
        cd.dispose();
    }
}
