package com.coomix.app.all.ui.audioRecord;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import com.coomix.app.all.AllOnlineApp;
import com.coomix.app.all.R;
import com.coomix.app.all.model.bean.DeviceInfo;
import com.coomix.app.all.log.AppConfigs;
import com.coomix.app.all.ui.base.BaseFragment;
import com.goomeim.GMAppConstant;

public class AudioStartFragment extends BaseFragment {
    public static final String PARAM_DEVICE = "ARG_PARAM_DEVICE";
    private DeviceInfo mDevice;

    private OnFragmentInteractionListener mListener;
    private Button btnRecord;
    private TextView mealTv;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mDevice = (DeviceInfo) getArguments().getSerializable(PARAM_DEVICE);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_audio_start, container, false);
        btnRecord = (Button) v.findViewById(R.id.btnStartRecord);
        btnRecord.setOnClickListener(v1 -> {
            onStartRecordingClicked();
        });
        mealTv = (TextView) v.findViewById(R.id.textView5);
        AppConfigs config = AllOnlineApp.getAppConfig();

        if (!TextUtils.isEmpty(config.getCarol_voice_tips_cn())) {
            mealTv.setText(config.getCarol_voice_tips_cn());
        }
        return v;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        registerReceiver();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        unRegisterReceiver();
        mListener = null;
    }

    public AudioStartFragment() {
        // Required empty public constructor
    }

    public static AudioStartFragment newInstance() {
        AudioStartFragment fragment = new AudioStartFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    private void onStartRecordingClicked() {
        goToConnectFragment();
    }

    private void goToConnectFragment() {
        mListener.showConnectingFragment(AudioProgressingFragment.STAGE_OPEN_PROGRESSING);
    }

    public void setDevice(DeviceInfo device) {
        mDevice = device;
        Bundle args = getArguments();
        args.putSerializable(PARAM_DEVICE, device);
    }

    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null && intent.getAction() != null) {
                String action = intent.getAction();
                if (action.equals(GMAppConstant.k_strVoiceOpenFlag)) {
                    if (intent.hasExtra(GMAppConstant.EXTRA_USER_ID)) {
                        long userId = intent.getLongExtra(GMAppConstant.EXTRA_USER_ID, 0);
                        if (mDevice != null && userId == mDevice.getVoice_gid()) {
                            mListener.showRecordingFragment();
                        }
                    }
                } else if (action.equals(GMAppConstant.k_strVoiceCloseFlag)) {
                    if (intent.hasExtra(GMAppConstant.EXTRA_USER_ID)) {
                        long userId = intent.getLongExtra(GMAppConstant.EXTRA_USER_ID, 0);
                        if (mDevice != null && userId == mDevice.getVoice_gid()) {
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
}
