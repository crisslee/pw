package com.coomix.app.all.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;
import com.coomix.app.all.AllOnlineApp;
import com.coomix.app.all.GlobalParam;
import com.coomix.app.all.R;
import com.coomix.app.framework.app.Result;
import com.coomix.app.framework.util.PreferenceUtil;
import com.coomix.app.all.data.BaseSubscriber;
import com.coomix.app.all.data.DataEngine;
import com.coomix.app.all.data.ExceptionHandle;
import com.coomix.app.all.data.RxUtils;
import com.coomix.app.all.model.event.LoginCommunityEvent;
import com.coomix.app.all.model.response.CommunityUser;
import com.coomix.app.all.model.response.RespBase;
import com.coomix.app.all.model.response.RespLoginCommunity;
import com.coomix.app.all.util.CommunityUtil;
import com.tencent.bugly.crashreport.CrashReport;
import io.reactivex.Flowable;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import java.util.concurrent.TimeUnit;
import org.greenrobot.eventbus.EventBus;
import org.json.JSONObject;
import org.reactivestreams.Publisher;

public class CommonService extends Service {
    public static final String CUSTOMER_ID = "customerId";
    public static final String CUSTOMER_AVATAR = "customerPic";
    public static final String ACTION = "ACTION";
    public static final int ACTION_UPLOAD_LOCATION = 4000;
    public static final int ACTION_LOGIN_COMMUNITY = 4100;
    public static final int ACTION_STOP = 5000;

    private static final String PATA_SHOW_TOAST = "PATA_SHOW_TOAST";

    public static final String PARA_INTERVAL = "PARA_INTERVAL";
    public static final String PARA_URL = "PARA_URL";

    private CompositeDisposable mCompositeDisposable = new CompositeDisposable();

    public static void startUploadLocation(Context context, long interval, String url) {
        Intent intent = new Intent(context, CommonService.class);
        intent.putExtra(ACTION, ACTION_UPLOAD_LOCATION);
        intent.putExtra(PARA_INTERVAL, interval);
        intent.putExtra(PARA_URL, url);
        context.startService(intent);
    }

    public static void loginCommunityInBackground(Context context) {
        Intent intent = new Intent(context, CommonService.class);
        intent.putExtra(ACTION, ACTION_LOGIN_COMMUNITY);
        context.startService(intent);
    }
    public static void loginCommunityInBackground(Context context, boolean showToastMsg) {
        Intent intent = new Intent(context, CommonService.class);
        intent.putExtra(ACTION, ACTION_LOGIN_COMMUNITY);
        intent.putExtra(PATA_SHOW_TOAST, true);
        context.startService(intent);
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            int action = intent.getIntExtra(ACTION, 0);
            switch (action) {
                case 0:
                    break;
                case ACTION_UPLOAD_LOCATION:
                    long interval = intent.getLongExtra(PARA_INTERVAL, 0);
                    String url = intent.getStringExtra(PARA_URL);
                    if (interval > 0 && !TextUtils.isEmpty(url)) {
                        uploadLocation(interval, url);
                    }

                    break;
                case ACTION_LOGIN_COMMUNITY:
                    boolean ifShowToast = intent.getBooleanExtra(PATA_SHOW_TOAST, false);

                    loginCommunity(ifShowToast);
                    break;
                case ACTION_STOP:
                    break;

            }

        }
        return super.onStartCommand(intent, flags, startId);
    }

    @SuppressWarnings("CheckResult")
    private void loginCommunity(boolean ifShowToast) {
        DataEngine.getCommunityApi().loginCommunity(GlobalParam.getInstance().getCommonParas(),
            GlobalParam.getInstance().getAccessToken(), AllOnlineApp.channelId(this), AllOnlineApp.sAccount)
            .compose(RxUtils.toIO())
            .compose(RxUtils.businessTransformer())
            .map(new Function<RespLoginCommunity, CommunityUser>() {
                @Override
                public CommunityUser apply(RespLoginCommunity respLoginCommunity) throws Exception {
                    CommunityUser user = respLoginCommunity.getData().getUserinfo();
                    user.setTicket(respLoginCommunity.getData().getTicket());
                    user.setHxAccount(respLoginCommunity.getData().getHxAccount());
                    user.setHxPwd(respLoginCommunity.getData().getHxPwd());
                    user.setAccount(AllOnlineApp.sAccount);
                    CommunityDbHelper.getInstance(AllOnlineApp.mApp).saveAccountInfo(user);

                    String customerId = respLoginCommunity.getData().getCustomerId();
                    if (customerId != null) {
                        PreferenceUtil.commitString(CUSTOMER_ID, customerId);
                    }
                    String customerAvatar = respLoginCommunity.getData().getCustomerPic();
                    if (customerAvatar != null) {
                        PreferenceUtil.commitString(CUSTOMER_AVATAR, customerAvatar);
                    }

                    return user;
                }
            })
            .subscribeWith(new BaseSubscriber<CommunityUser>() {
                @Override
                public void onHttpError(ExceptionHandle.ResponeThrowable e) {
                    EventBus.getDefault().post(new LoginCommunityEvent(false));
                    CrashReport.postCatchedException(e);
                }

                @Override
                public void onNext(CommunityUser communityUser) {
                    if (ifShowToast) {
                        Toast.makeText(getApplicationContext(), R.string.loggin_community_hint, Toast.LENGTH_SHORT)
                            .show();
                    }
                    String ticket = communityUser.getTicket();
                    if (!CommunityUtil.isEmptyTrimStringOrNull(ticket)) {
                        // 綁定gpns id
                        bindChannel();
                        EventBus.getDefault().post(new LoginCommunityEvent(true));
                    }
                    String uid = communityUser.getUid();
                    //上传用户信息
                    if (!TextUtils.isEmpty(ticket) && !TextUtils.isEmpty(uid)) {
                        Result startCallback = AllOnlineApp.mApiClient.senUserInfo(ticket, uid, "record_login", "0");
                    }
                }
            });
    }

    @SuppressWarnings("CheckResult")
    private void setUserInfo(String ticket, String uid, String acctype, String recordhis) {
        DataEngine.getCommunityApi().senUserInfo(GlobalParam.getInstance().getCommonParas(), ticket, uid, acctype,
            recordhis)
            .compose(RxUtils.toIO())
            .compose(RxUtils.businessTransformer())
            .subscribeWith(new BaseSubscriber<RespBase>() {
                @Override
                public void onHttpError(ExceptionHandle.ResponeThrowable e) {
                    CrashReport.postCatchedException(e);
                }

                @Override
                public void onNext(RespBase respBase) {
                }
            });
    }

    @SuppressWarnings("CheckResult")
    private void bindChannel() {
        DataEngine.getCommunityApi().bindChannelId(GlobalParam.getInstance().getCommonParas(),
            GlobalParam.getInstance().getAccessToken(), GlobalParam.getInstance().getChannelId())
            .compose(RxUtils.toIO())
            .compose(RxUtils.businessTransformer())
            .subscribeWith(new BaseSubscriber<RespBase>() {
                @Override
                public void onHttpError(ExceptionHandle.ResponeThrowable e) {
                    CrashReport.postCatchedException(e);
                }

                @Override
                public void onNext(RespBase respBase) {

                }
            });
    }

    private void uploadLocation(long interval, final String url) {
        Disposable disposable = Flowable.interval(interval, TimeUnit.SECONDS).onBackpressureDrop()
            .compose(RxUtils.toIO())
            .flatMap(new Function<Long, Publisher<JSONObject>>() {
                @Override
                public Publisher<JSONObject> apply(@NonNull Long aLong) throws Exception {
                    return DataEngine.getCommunityApi()
                        .uploadLocation(url, GlobalParam.getInstance().getLocTicketParas());
                }
            })
            .subscribe(new Consumer<JSONObject>() {
                @Override
                public void accept(JSONObject jsonObject) throws Exception {
                    Log.d(CommonService.class.getName(), jsonObject.toString());
                }
            }, new Consumer<Throwable>() {
                @Override
                public void accept(Throwable throwable) throws Exception {
                    Log.d(CommonService.class.getName(), throwable.getMessage());
                }
            });

        mCompositeDisposable.add(disposable);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mCompositeDisposable.clear();
    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
