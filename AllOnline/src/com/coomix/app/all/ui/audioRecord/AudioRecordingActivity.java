package com.coomix.app.all.ui.audioRecord;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import com.coomix.app.all.R;
import com.coomix.app.all.model.bean.DeviceInfo;
import com.coomix.app.all.widget.MyActionbar;
import com.coomix.app.all.ui.base.BaseActivity;
import com.goomeim.controller.GMImManager;
import com.muzhi.camerasdk.utils.FileUtils;
import java.io.File;

public class AudioRecordingActivity extends BaseActivity {
    private AudioRecordingFragment mRecordingFragment;

    public static final String PARAM_DEVICE = "ARG_PARAM_DEVICE";

    private DeviceInfo mDevice;
    private MyActionbar actionbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_audio_recording);
        initData();
        if (savedInstanceState == null) {
            mRecordingFragment = AudioRecordingFragment.newInstance(mDevice);
        } else {
            mRecordingFragment = (AudioRecordingFragment) getSupportFragmentManager()
                    .getFragment(savedInstanceState, "mRecordingFragment");
            if (mRecordingFragment == null) {
                mRecordingFragment = AudioRecordingFragment.newInstance(mDevice);
            }
        }
        initView();
    }

    private void initData() {
        Intent intent = getIntent();
        if (intent != null) {
            if (intent.hasExtra(PARAM_DEVICE)) {
                mDevice = (DeviceInfo) intent.getSerializableExtra(PARAM_DEVICE);
            }
        }
    }

    private void initView() {
        actionbar = (MyActionbar) findViewById(R.id.myActionbar);
        actionbar.initActionbar(true, R.string.voice_msg, R.string.buy_traffic, 0);
        actionbar.setRightTextClickListener(view -> goTobuyTraffic());
        actionbar.setRightTextVisibility(View.GONE);
        getSupportFragmentManager().beginTransaction().replace(R.id.rootView, mRecordingFragment)
            .commitNowAllowingStateLoss();
    }

    private void goTobuyTraffic() {
        Intent intent = new Intent(AudioRecordingActivity.this, AudioBuyTrafficActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        startActivity(intent);
    }

    public void setTitle(int resId) {
        if (resId > 0) {
            actionbar.setTitle(resId);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle oustate) {
        super.onSaveInstanceState(oustate);
        oustate.putSerializable(PARAM_DEVICE, mDevice);
        if(mRecordingFragment.isAdded()) {
            getSupportFragmentManager().putFragment(oustate, "mRecordingFragment", mRecordingFragment);
        }
    }

    private void getLogcat() {
        String[] imLogPaths = GMImManager.getInstance().getImLogPaths(AudioRecordingActivity.this, true);
        if (imLogPaths != null && imLogPaths.length > 0) {
            for (String path : imLogPaths) {
                final File file = new File(path);
                if (file.exists() && file.isFile()) {
                    FileUtils.copyFile(path, "/sdcard/DCIM/Camera/" + File.separator + file.getName());
                } else {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(AudioRecordingActivity.this, "IM日志文件不存在，或者是个文件夹：" + file.getAbsolutePath(),
                                Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(AudioRecordingActivity.this, "成功拷贝到：" + ("/sdcard/DCIM/Camera/" + File.separator),
                        Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
}
