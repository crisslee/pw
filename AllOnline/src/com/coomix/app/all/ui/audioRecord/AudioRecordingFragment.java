package com.coomix.app.all.ui.audioRecord;

import android.animation.ValueAnimator;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import com.coomix.app.all.AllOnlineApp;
import com.coomix.app.all.Constant;
import com.coomix.app.all.GlobalParam;
import com.coomix.app.all.R;
import com.coomix.app.all.data.BaseSubscriber;
import com.coomix.app.all.data.DataEngine;
import com.coomix.app.all.data.ExceptionHandle;
import com.coomix.app.all.data.RxUtils;
import com.coomix.app.all.log.AppConfigs;
import com.coomix.app.all.log.GoomeLog;
import com.coomix.app.all.manager.ThemeManager;
import com.coomix.app.all.model.bean.CardDataInfo;
import com.coomix.app.all.model.bean.DeviceDetailInfo;
import com.coomix.app.all.model.bean.DeviceInfo;
import com.coomix.app.all.model.response.RespBase;
import com.coomix.app.all.model.response.RespCardDataInfo;
import com.coomix.app.all.model.response.RespDeviceDetailInfo;
import com.coomix.app.all.model.response.RespOpenAudioRecord;
import com.coomix.app.all.share.PopupWindowUtil;
import com.coomix.app.all.share.TextSet;
import com.coomix.app.all.ui.base.BaseFragment;
import com.coomix.app.all.ui.charge.CardDataInfoActivity;
import com.coomix.app.all.util.CommunityDateUtil;
import com.coomix.app.all.util.ViewUtil;
import com.goomeim.GMAppConstant;
import com.goomeim.utils.GMCommonUtils;
import io.reactivex.Flowable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import net.goome.im.chat.GMChatManager;
import net.goome.im.chat.GMClient;
import net.goome.im.chat.GMConstant.ConversationType;
import net.goome.im.chat.GMConversation;

public class AudioRecordingFragment extends BaseFragment {
    public static final String PARAM_DEVICE = "ARG_PARAM_DEVICE";
    private DeviceInfo mDevice;
    private DeviceDetailInfo deviceDetail;
    private CardDataInfo cardData;
    private TextView btnStopRecord, shortBalance;
    private boolean isRecordOpened;
    private int status;
    private Disposable mDisposibleTimer;
    private String[] dotText = { ".  ", ".. ", "..." };
    private ValueAnimator valueAnimator;
    private long groupId;

    public static final int STATUS_CLOSE = 0;
    public static final int STATUS_OPENING = 2;
    public static final int STATUS_OPEN_FAILED = 4;
    public static final int STATUS_RECORDING = 1;
    public static final int STATUS_CLOSING = 3;
    public static final int STATUS_CLOSE_FAILED = 5;

    private static final int CARD_EXP = 5;//5days
    private static final int DATA_EXP = 1; //1M
    private int currRecordLen = -1;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mDevice = (DeviceInfo) getArguments().getSerializable(PARAM_DEVICE);
            groupId = mDevice != null ? mDevice.getVoice_gid() : 0;
            status = mDevice == null ? 0 : mDevice.getVoice_status();
        }
        getDeviceDetail();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_audio_recording, container, false);
        btnStopRecord = (TextView) v.findViewById(R.id.btnStopRecord);
        shortBalance = (TextView) v.findViewById(R.id.shortBalance);

        if (mDevice.getVoice_status() == 1) {
            isRecordOpened = true;
        } else if (mDevice.getVoice_status() == 0) {
            isRecordOpened = false;
        }
        shortBalance.setOnClickListener(v1 -> gotoCharge());
        btnStopRecord.setTextColor(ThemeManager.getInstance().getThemeAll().getThemeColor().getColorNavibarText());
        ViewUtil.setBg(btnStopRecord, ThemeManager.getInstance().getBGColorDrawable(getContext()));
        btnStopRecord.setOnClickListener(v1 -> {
            showOpenRecordHint();
        });
        updateUi();
        return v;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initFragment();
        registerReceiver();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        stopAnim();
        unRegisterReceiver();
    }

    private void initFragment() {
        GMChatManager chatManager = GMClient.getInstance().chatManager();
        if (chatManager == null) {
            return;
        }
        GMConversation conversation = chatManager.getConversation(GMCommonUtils.getConversationIdByUserId(
            String.valueOf(groupId), ConversationType.CAROLVOICEROOM), ConversationType.CAROLVOICEROOM,
            false);
        if (conversation != null && conversation.getAllMessages() != null && conversation.getAllMessages().size() > 0) {
            addChatFragment(groupId);
        } else {
            addStartFragment();
        }
    }

    public AudioRecordingFragment() {
        // Required empty public constructor
    }

    private void addChatFragment(long groupId) {
        AudioChatroomFragment fragment = AudioChatroomFragment.newInstance(groupId,
            ConversationType.CAROLVOICEROOM);
        getChildFragmentManager().beginTransaction().replace(R.id.layoutContent, fragment)
            .commitNowAllowingStateLoss();
    }

    private void addStartFragment() {
        AudioStartFragment start = AudioStartFragment.newInstance();
        getChildFragmentManager().beginTransaction().replace(R.id.layoutContent, start)
            .commitNowAllowingStateLoss();
    }

    public void setDevice(DeviceInfo device) {
        mDevice = device;
        Bundle args = getArguments();
        args.putSerializable(PARAM_DEVICE, device);
    }

    public void setRecordOpened(boolean recordOpened) {
        isRecordOpened = recordOpened;
    }

    private void startAnim() {
        if (valueAnimator == null) {
            valueAnimator = ValueAnimator.ofInt(0, 3).setDuration(1500);
            valueAnimator.setRepeatCount(ValueAnimator.INFINITE);
            valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    int i = (int) animation.getAnimatedValue();
                    String str = "";
                    if (status == STATUS_OPENING) {
                        str = getContext().getString(R.string.is_opening_recording);
                    } else if (status == STATUS_CLOSING) {
                        str = getContext().getString(R.string.is_closing_recording);
                    }
                    btnStopRecord.setText(str + dotText[i % dotText.length]);
                }
            });
        }
        if (valueAnimator.isRunning()) {
            return;
        }
        valueAnimator.start();
    }

    private void stopAnim() {
        if (valueAnimator != null) {
            valueAnimator.cancel();
        }
    }

    public static AudioRecordingFragment newInstance(DeviceInfo device) {
        AudioRecordingFragment fragment = new AudioRecordingFragment();
        Bundle args = new Bundle();
        args.putSerializable(PARAM_DEVICE, device);
        fragment.setArguments(args);
        return fragment;
    }

    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null && intent.getAction() != null) {
                String action = intent.getAction();
                if (action.equals(GMAppConstant.k_strVoiceOpenFlag)) {
                    if (intent.hasExtra(GMAppConstant.EXTRA_USER_ID)) {
                        long userId = intent.getLongExtra(GMAppConstant.EXTRA_USER_ID, 0);
                        if (userId == mDevice.getVoice_gid()) {
                            RxUtils.dispose(mDisposibleTimer);
                            status = STATUS_RECORDING;
                            updateUi();
                            isRecordOpened = true;
                            addChatFragment(groupId);
                        }
                    }
                } else if (action.equals(GMAppConstant.k_strVoiceCloseFlag)) {
                    if (intent.hasExtra(GMAppConstant.EXTRA_USER_ID)) {
                        long userId = intent.getLongExtra(GMAppConstant.EXTRA_USER_ID, 0);
                        if (userId == mDevice.getVoice_gid()) {
                            RxUtils.dispose(mDisposibleTimer);
                            status = STATUS_CLOSE;
                            updateUi();
                            isRecordOpened = false;
                        }
                    }
                }
            }
        }
    };

    private void registerReceiver() {
        if (broadcastReceiver != null) {
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(GMAppConstant.k_strVoiceOpenFlag);
            intentFilter.addAction(GMAppConstant.k_strVoiceCloseFlag);
            getActivity().registerReceiver(broadcastReceiver, intentFilter);
        }
    }

    private void unRegisterReceiver() {
        if (broadcastReceiver != null) {
            getActivity().unregisterReceiver(broadcastReceiver);
        }
    }

    private void updateUi() {
        Activity a = getActivity();
        if (a instanceof AudioRecordingActivity) {
            if (status == STATUS_RECORDING) {
                ((AudioRecordingActivity) a).setTitle(R.string.is_recording);
            } else {
                ((AudioRecordingActivity) a).setTitle(R.string.voice_msg);
            }
        }
        switch (status) {
            //关闭
            case STATUS_CLOSE:
                stopAnim();
                btnStopRecord.setText(R.string.start_recording);
                break;
            //开启
            case STATUS_RECORDING:
                stopAnim();
                btnStopRecord.setText(R.string.stop_recording);
                break;
            //正在开启
            case STATUS_OPENING:
                startAnim();
                btnStopRecord.setText(R.string.is_opening_recording);
                break;
            //开启失败
            case STATUS_OPEN_FAILED:
                stopAnim();
                showDlg();
                break;
            //正在关闭
            case STATUS_CLOSING:
                startAnim();
                btnStopRecord.setText(R.string.is_closing_recording);
                break;
            //关闭失败
            case STATUS_CLOSE_FAILED:
                stopAnim();
                showDlg();
                break;
            default:
                break;
        }
    }

    private void showDlg() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        String msg = "";
        String posBtn = "";
        if (status == STATUS_OPEN_FAILED) {
            msg = getString(R.string.open_recording_failed);
            posBtn = getString(R.string.open_again);
        } else if (status == STATUS_CLOSE_FAILED) {
            msg = getString(R.string.stop_recording_failed);
            posBtn = getString(R.string.close_again);
        }
        builder.setMessage(msg);
        builder.setCancelable(false);
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                if (status == STATUS_OPEN_FAILED) {
                    status = STATUS_CLOSE;
                    updateUi();
                } else if (status == STATUS_CLOSE_FAILED) {
                    status = STATUS_RECORDING;
                    updateUi();
                }
            }
        });
        builder.setPositiveButton(posBtn, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                if (status == STATUS_OPEN_FAILED) {
                    openRecording(currRecordLen);
                } else if (status == STATUS_CLOSE_FAILED) {
                    closeRecording();
                }
            }
        });
        builder.show();
    }

    private void showOpenRecordHint() {
        if (status == STATUS_CLOSE || status == STATUS_OPEN_FAILED) {
            AppConfigs config = AllOnlineApp.getAppConfig();
            if (TextUtils.isEmpty(config.getRecord_time())) {
                return;
            }
            String[] times = config.getRecord_time().split(",");
            ArrayList<TextSet> timeMenu = new ArrayList<>(5);
            for (int i = 0; i < times.length; i++) {
                final int time;
                try {
                    time = Integer.parseInt(times[i]);
                } catch (Exception e) {
                    e.printStackTrace();
                    return;
                }
                String title = getTimeTitle(times[i]);
                if (TextUtils.isEmpty(title)) {
                    continue;
                }
                TextSet set = new TextSet(title, true, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        currRecordLen = time;
                        if (time == -1) {
                            showVolumeScopeMenu();
                        }
                        openRecording(time);
                    }
                });
                timeMenu.add(set);
            }
            PopupWindowUtil.showPopWindow(getContext(), getView(), 0, timeMenu, true);
        } else if (status == STATUS_RECORDING || status == STATUS_CLOSE_FAILED) {
            closeRecording();
        }
    }

    private void showVolumeScopeMenu() {
        AppConfigs config = AllOnlineApp.getAppConfig();
        if (TextUtils.isEmpty(config.getRecord_device_volume_title()) || TextUtils.isEmpty(
            config.getRecord_device_volume_scope())) {
            return;
        }
        String[] titles = config.getRecord_device_volume_title().split(",");
        String[] scopes = config.getRecord_device_volume_scope().split(",");
        ArrayList<TextSet> list = new ArrayList<>(titles.length);
        for (int i = 0; i < titles.length; i++) {
            if (i > scopes.length - 1) {
                break;
            }
            final String volume = scopes[i];
            TextSet menu = new TextSet(titles[i], true, new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    setRecordVolume(volume);
                }
            });
            list.add(menu);
        }
        PopupWindowUtil.showPopWindow(getContext(), getView(), R.string.record_volume_title,
            getString(R.string.cancel_set), list, true);
    }

    private void setRecordVolume(String volume) {
        String imei = mDevice.getImei();
        String token = GlobalParam.getInstance().getAccessToken();
        Disposable d = DataEngine.getAllMainApi()
            .setRecordVolume(imei, volume, token, GlobalParam.getInstance().getCommonParas())
            .compose(RxUtils.businessTransformer())
            .compose(RxUtils.toMain())
            .subscribeWith(new BaseSubscriber<RespBase>() {
                @Override
                public void onHttpError(ExceptionHandle.ResponeThrowable e) {

                }

                @Override
                public void onNext(RespBase respBase) {

                }
            });
    }

    private String getTimeTitle(String time) {
        try {
            int t = Integer.parseInt(time);
            if (t == -1) {
                return "持续录音";
            }
            int day = 0;
            int hour = 0;
            int minute = 0;
            int second = 0;
            if (t >= 86400) {
                day = t / 86400;
                t %= 86400;
            }
            if (t >= 3600) {
                hour = t / 3600;
                t %= 3600;
            }
            if (t >= 60) {
                minute = t / 60;
                t %= 60;
            }
            second = t;
            StringBuilder b = new StringBuilder();
            if (day > 0) {
                b.append(day).append("天");
            }
            if (hour > 0) {
                b.append(hour).append("小时");
            }
            if (minute > 0) {
                b.append(minute).append("分钟");
            }
            if (second > 0) {
                b.append(second).append("秒");
            }
            return b.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    private void openRecording(int time) {
        RxUtils.dispose(mDisposibleTimer);
        Disposable disposable = DataEngine.getAllMainApi().openRecord(GlobalParam.getInstance().getAccessToken(),
            mDevice.getImei(), time, GlobalParam.getInstance().getCommonParas())
            .compose(RxUtils.businessTransformer())
            .compose(RxUtils.toMain())
            .subscribeWith(new BaseSubscriber<RespOpenAudioRecord>() {
                @Override
                public void onNext(RespOpenAudioRecord respOpenAudioRecord) {
                    switch (respOpenAudioRecord.getData().getData().getStatus()) {
                        case 0:
                            status = STATUS_OPENING;
                            updateUi();
                            //延时20s
                            mDisposibleTimer = Flowable.just(STATUS_OPEN_FAILED)
                                .delay(30, TimeUnit.SECONDS)
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(integer -> {
                                    //开启失败
                                    status = integer;
                                    updateUi();
                                });
                            subscribeRx(mDisposibleTimer);
                            break;
                        case 1: // recording
                            status = STATUS_RECORDING;
                            updateUi();
                            break;
                        default:
                            break;
                    }
                }

                @Override
                public void onHttpError(ExceptionHandle.ResponeThrowable e) {
                    Toast.makeText(getContext(), e.getErrCodeMessage(), Toast.LENGTH_SHORT).show();
                    GoomeLog.getInstance().logE("AudioProgressingFragment", e.getErrCodeMessage(), 0);
                }
            });
        subscribeRx(disposable);
    }

    private void closeRecording() {
        RxUtils.dispose(mDisposibleTimer);
        Disposable disposable = DataEngine.getAllMainApi().closeRecord(GlobalParam.getInstance().getCommonParas(),
            GlobalParam.getInstance().getAccessToken(), mDevice.getImei())
            .compose(RxUtils.businessTransformer())
            .compose(RxUtils.toMain())
            .subscribeWith(new BaseSubscriber<RespOpenAudioRecord>() {
                @Override
                public void onNext(RespOpenAudioRecord respOpenAudioRecord) {
                    switch (respOpenAudioRecord.getData().getData().getStatus()) {
                        case 0://device is closed
                            status = STATUS_CLOSE;
                            updateUi();
                            break;
                        case 1://device is open,waitting to close.
                            status = STATUS_CLOSING;
                            updateUi();
                            //延时30s
                            mDisposibleTimer = Flowable.just(STATUS_CLOSE_FAILED)
                                .delay(30, TimeUnit.SECONDS)
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(integer -> {
                                    //failed
                                    status = integer;
                                    updateUi();
                                });
                            subscribeRx(mDisposibleTimer);
                            break;
                    }
                }

                @Override
                public void onHttpError(ExceptionHandle.ResponeThrowable e) {
                    Toast.makeText(getContext(), e.getErrCodeMessage(), Toast.LENGTH_SHORT).show();
                    GoomeLog.getInstance().logE("AudioProgressingFragment", e.getErrCodeMessage(), 0);
                }
            });
        subscribeRx(disposable);
    }

    private void getDeviceDetail() {
        if (mDevice == null || TextUtils.isEmpty(mDevice.getImei())) {
            return;
        }
        Disposable d = DataEngine.getAllMainApi().getDevDetailInfo(GlobalParam.getInstance().getCommonParas(),
            AllOnlineApp.sAccount, GlobalParam.getInstance().getAccessToken(), mDevice.getImei())
            .compose(RxUtils.toMain())
            .compose(RxUtils.businessTransformer())
            .subscribeWith(new BaseSubscriber<RespDeviceDetailInfo>() {
                @Override
                public void onHttpError(ExceptionHandle.ResponeThrowable e) {

                }

                @Override
                public void onNext(RespDeviceDetailInfo respDeviceDetailInfo) {
                    if (respDeviceDetailInfo != null && respDeviceDetailInfo.getData() != null) {
                        deviceDetail = respDeviceDetailInfo.getData();
                        queryCardDataInfo();
                    }
                }
            });
        subscribeRx(d);
    }

    private void gotoCharge() {
        if (deviceDetail == null || cardData == null) {
            return;
        }
        Intent i = new Intent(getContext(), CardDataInfoActivity.class);
        i.putExtra(CardDataInfoActivity.KEY_CARD_DATA, cardData);
        i.putExtra(Constant.KEY_DEVICE, deviceDetail);
        startActivity(i);
    }

    private void queryCardDataInfo() {
        if (deviceDetail == null || TextUtils.isEmpty(deviceDetail.getPhone())) {
            return;
        }
        Disposable d = DataEngine.getCardApi().queryCardData(deviceDetail.getPhone(),
            GlobalParam.getInstance().getCommonParas())
            .compose(RxUtils.toMain())
            .compose(RxUtils.businessTransformer())
            .subscribeWith(new BaseSubscriber<RespCardDataInfo>() {
                @Override
                public void onHttpError(ExceptionHandle.ResponeThrowable e) {

                }

                @Override
                public void onNext(RespCardDataInfo respCardDataInfo) {
                    if (respCardDataInfo != null && respCardDataInfo.getData() != null) {
                        cardData = respCardDataInfo.getData();
                        setNotify();
                    }
                }
            });
        subscribeRx(d);
    }

    private void setNotify() {
        boolean set = false;
        if(cardData!=null && cardData.card!=null && !TextUtils.isEmpty(cardData.card.expdate)) {
            String expdate = cardData.card.expdate;
            DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
            try {
                Date exp = df.parse(expdate);
                Date now = new Date();
                if (exp.getTime() < now.getTime()) {
                    shortBalance.setText(R.string.card_already_exp);
                    shortBalance.setVisibility(View.VISIBLE);
                    set = true;
                } else {
                    int diffDays = CommunityDateUtil.getOffectDay(exp.getTime(), now.getTime());
                    if (diffDays <= CARD_EXP) {
                        shortBalance.setText(R.string.card_about_to_exp);
                        shortBalance.setVisibility(View.VISIBLE);
                        set = true;
                    } else {
                        shortBalance.setVisibility(View.GONE);
                    }
                }
            }catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (!set && cardData != null && cardData.product != null) {
            float left = cardData.product.left_value;
            if (left <= 0) {
                shortBalance.setText(R.string.data_already_exp);
                shortBalance.setVisibility(View.VISIBLE);
            } else if (left <= DATA_EXP) {
                shortBalance.setText(R.string.data_about_to_exp);
                shortBalance.setVisibility(View.VISIBLE);
            } else {
                shortBalance.setVisibility(View.GONE);
            }
        }
    }
}
