package com.coomix.app.all.ui.audioRecord;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.coomix.app.all.GlobalParam;
import com.coomix.app.all.R;
import com.coomix.app.all.data.BaseSubscriber;
import com.coomix.app.all.data.DataEngine;
import com.coomix.app.all.data.ExceptionHandle;
import com.coomix.app.all.data.RxUtils;
import com.coomix.app.all.log.GoomeLog;
import com.coomix.app.all.model.bean.DeviceInfo;
import com.coomix.app.all.model.response.RespOpenAudioRecord;
import com.coomix.app.all.ui.base.BaseFragment;
import com.goomeim.GMAppConstant;
import com.goomeim.controller.GMMessageAppListener;
import io.reactivex.Flowable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import java.util.List;
import java.util.concurrent.TimeUnit;
import net.goome.im.chat.GMMessage;

/**
 *
 */
public class AudioProgressingFragment extends BaseFragment {
    private OnFragmentInteractionListener mListener;

    public static final int STAGE_OPEN_PROGRESSING = 0;
    public static final int STAGE_OPEN_SUCC = 1;
    public static final int STAGE_OPEN_FAILED = 2;

    public static final int STAGE_CLOSE_PROGRESSING = 10;
    public static final int STAGE_CLOSE_SUCC = 11;
    public static final int STAGE_CLOSE_FAILED = 12;

    private static final String ARG_PARAM_STAGE = "STAGE";
    public static final String ARG_PARAM_DEVICE = "ARG_PARAM_DEVICE";

    private static final int FADE_TIME = 500;
    private DeviceInfo mDevice;

    private int mStage = STAGE_OPEN_PROGRESSING;

    private TextView tvSending, tvResultFailed, tvResultSucc;
    private ImageView img;
    private Button btnRtry;
    private Disposable mDisposibleTimer;

    public AudioProgressingFragment() {
        // Required empty public constructor
    }

    public static AudioProgressingFragment newInstance(int stage) {
        AudioProgressingFragment fragment = new AudioProgressingFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_PARAM_STAGE, stage);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mStage = getArguments().getInt(ARG_PARAM_STAGE);
            mDevice = (DeviceInfo) getArguments().getSerializable(ARG_PARAM_DEVICE);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
        Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_audio_progressing, container, false);
        tvResultFailed = (TextView) v.findViewById(R.id.tvResultFailed);
        tvResultSucc = (TextView) v.findViewById(R.id.tvResultSucc);
        tvSending = (TextView) v.findViewById(R.id.tvSending);
        btnRtry = (Button) v.findViewById(R.id.btnRtry);
        img = (ImageView) v.findViewById(R.id.img);
        btnRtry.setOnClickListener(v1 -> {
            refrshToStage(mStage - 2);
        });
        return v;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        registerReceiver();
    }

    private void sendCloseCmd() {
        RxUtils.dispose(mDisposibleTimer);

        Disposable disposable = DataEngine.getAllMainApi().closeRecord(
            GlobalParam.getInstance().getCommonParas(),
            GlobalParam.getInstance().getAccessToken(),
            mDevice.getImei()
        )
            .compose(RxUtils.businessTransformer())
            .compose(RxUtils.toMain())
            .subscribeWith(new BaseSubscriber<RespOpenAudioRecord>() {
                @Override
                public void onNext(RespOpenAudioRecord respOpenAudioRecord) {
                    switch (respOpenAudioRecord.getData().getData().getStatus()) {
                        case 0://device is closed
                            refrshToStage(STAGE_CLOSE_SUCC);
                            break;
                        case 1://device is open,waitting to close.
                            //延时30s
                            mDisposibleTimer = Flowable.just(0)
                                .delay(30, TimeUnit.SECONDS)
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(integer -> {
                                    refrshToStage(STAGE_CLOSE_FAILED); //failed
                                });
                            subscribeRx(mDisposibleTimer);
                            break;
                        //                            default:
                        //                                refrshToStage(mStage + 2);
                        //                                break;
                    }
                }

                @Override
                public void onHttpError(ExceptionHandle.ResponeThrowable e) {
                    mListener.showConnectingFragment(STAGE_OPEN_FAILED);

                    Toast.makeText(getContext(), e.getErrCodeMessage(), Toast.LENGTH_SHORT).show();
                    GoomeLog.getInstance().logE("AudioProgressingFragment", e.getErrCodeMessage(), 0);
                }
            });
        subscribeRx(disposable);
    }

    private void sendRecordCmd() {
        RxUtils.dispose(mDisposibleTimer);

        Disposable disposable = DataEngine.getAllMainApi().openRecord(GlobalParam.getInstance().getAccessToken(),
            mDevice.getImei(), -1, GlobalParam.getInstance().getCommonParas())
            .compose(RxUtils.businessTransformer())
            .compose(RxUtils.toMain())
            .subscribeWith(new BaseSubscriber<RespOpenAudioRecord>() {
                @Override
                public void onNext(RespOpenAudioRecord respOpenAudioRecord) {
                    switch (respOpenAudioRecord.getData().getData().getStatus()) {
                        case 0:
                            //延时20s
                            mDisposibleTimer = Flowable.just(0)
                                .delay(30, TimeUnit.SECONDS)
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(integer -> {
                                    refrshToStage(mStage + 2); //failed
                                });
                            subscribeRx(mDisposibleTimer);
                            break;
                        case 1: // recording
                            refrshToStage(mStage + 1);
                            break;
                        default:
                            refrshToStage(mStage + 2);
                            break;
                    }
                }

                @Override
                public void onHttpError(ExceptionHandle.ResponeThrowable e) {
                    mListener.showConnectingFragment(STAGE_OPEN_FAILED);
                    Toast.makeText(getContext(), e.getErrCodeMessage(), Toast.LENGTH_SHORT).show();
                    GoomeLog.getInstance().logE("AudioProgressingFragment", e.getErrCodeMessage(), 0);
                }
            });
        subscribeRx(disposable);
    }

    public void setDevStage(DeviceInfo device, int stage) {
        mDevice = device;
        mStage = stage;
        Bundle args = getArguments();
        args.putInt(ARG_PARAM_STAGE, stage);
        args.putSerializable(ARG_PARAM_DEVICE, mDevice);
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
                            refrshToStage(STAGE_OPEN_SUCC);
                        }
                    }
                } else if (action.equals(GMAppConstant.k_strVoiceCloseFlag)) {
                    if (intent.hasExtra(GMAppConstant.EXTRA_USER_ID)) {
                        long userId = intent.getLongExtra(GMAppConstant.EXTRA_USER_ID, 0);
                        if (userId == mDevice.getVoice_gid()) {
                            RxUtils.dispose(mDisposibleTimer);
                            refrshToStage(STAGE_CLOSE_SUCC);
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

    private void refrshToStage(int stage) {
        Log.d("louis:", "refresh to stage");
        mStage = stage;
        //int commonStage = mStage % 10;
        switch (mStage/*commonStage*/) {

            case STAGE_OPEN_SUCC:
            case STAGE_CLOSE_SUCC:
                tvResultFailed.setVisibility(View.GONE);
                tvResultSucc.setVisibility(View.VISIBLE);
                if (mStage == STAGE_OPEN_SUCC) {
                    tvResultSucc.setText(getString(R.string.audio_connect_succ));
                } else {
                    tvResultSucc.setText(getString(R.string.audio_close_succ));
                }
                tvSending.setVisibility(View.INVISIBLE);
                Glide.with(AudioProgressingFragment.this)
                    .load(R.drawable.audio_sent)
                    .transition(new DrawableTransitionOptions().crossFade(FADE_TIME))
                    .into(img);
                btnRtry.setVisibility(View.INVISIBLE);
                subscribeRx(Flowable.just(mStage)
                    .delay(2, TimeUnit.SECONDS)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(integer -> {
                        if (integer == STAGE_OPEN_SUCC) {
                            mListener.showRecordingFragment();
                        } else if (integer == STAGE_CLOSE_SUCC) {
                            mListener.showStartRecordFragment();
                        }
                    }));
                break;
            case STAGE_OPEN_FAILED:
            case STAGE_CLOSE_FAILED:
                tvResultFailed.setVisibility(View.VISIBLE);
                tvResultSucc.setVisibility(View.GONE);
                tvSending.setVisibility(View.INVISIBLE);
                Glide.with(AudioProgressingFragment.this)
                    .load(R.drawable.audio_failed)
                    .transition(new DrawableTransitionOptions().crossFade(FADE_TIME))
                    .into(img);
                btnRtry.setVisibility(View.VISIBLE);
                break;
            default: //progressing
                tvResultFailed.setVisibility(View.GONE);
                tvResultSucc.setVisibility(View.GONE);
                tvSending.setVisibility(View.VISIBLE);
                Glide.with(AudioProgressingFragment.this)
                    .load(R.drawable.audio_loading)
                    .transition(new DrawableTransitionOptions().crossFade(FADE_TIME))
                    .into(img);
                btnRtry.setVisibility(View.INVISIBLE);
                if (mStage == STAGE_OPEN_PROGRESSING) {
                    sendRecordCmd();
                } else if (mStage == STAGE_CLOSE_PROGRESSING) {
                    sendCloseCmd();
                }
                break;
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                + " must implement OnFragmentInteractionListener");
        }
        //GMImManager.getInstance().addGMMessageListener(mGmMessageAppListener);

    }

    @Override
    public void onResume() {
        super.onResume();
        refrshToStage(mStage);
    }

    @Override
    public void onPause() {
        super.onPause();
        RxUtils.dispose(mDisposibleTimer);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        unsubscribeRx();
        unRegisterReceiver();
        mListener = null;
        //GMImManager.getInstance().removeGMMessageListener(mGmMessageAppListener);

    }

    private GMMessageAppListener mGmMessageAppListener = new GMMessageAppListener() {
        @Override
        public boolean onMessageReceived(List<GMMessage> messages) {
            return false;
        }

        @Override
        public boolean onMessageRead(List<GMMessage> messages) {
            return false;
        }

        @Override
        public boolean onMessageDelivered(List<GMMessage> messages) {
            return false;
        }

        @Override
        public boolean onMessageChanged(GMMessage message, Object change) {
            return false;
        }

        @Override
        public boolean onCmdMessageReceived(List<GMMessage> messages, boolean bInputing) {
            for (GMMessage message : messages) {
                if (message == null || message.getMsgBody() == null) {
                    continue;
                }

                //                GMCmdMessageBody cmdMsgBody = (GMCmdMessageBody) message.getMsgBody();
                //                String action = cmdMsgBody.getAction();//获取自定义action
                //                if (action.equals(GMAppConstant.CAROL_VOICE_OPEN_FLAG)) {
                //                    getActivity().runOnUiThread(new Runnable() {
                //                        @Override
                //                        public void run() {
                //                            refrshToStage(STAGE_OPEN_SUCC);
                //                        }
                //                    });
                //
                //                }
            }
            return false;
        }
    };
}
