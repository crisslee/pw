package com.coomix.app.all.ui.update;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.coomix.app.all.Constant;
import com.coomix.app.all.R;
import com.coomix.app.framework.util.NetworkUtil;
import com.coomix.app.framework.util.PreferenceUtil;
import com.coomix.app.all.ui.base.BaseActivity;

/**
 * Created by Administrator on 2016/4/19.
 */
public class GoomeUpdateDialogActivity extends BaseActivity {
    private GoomeUpdateInfo mUpdateInfo;

    private TextView mVersionTv, mContentTv;
    private CheckBox mIgnoreCb;
    private Button mUpdateBtn, mRemindBtn;
    private FrameLayout layout;
    private boolean mIgnore = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.goome_update_dialog_activity);
        PreferenceUtil.init(this);
        Intent intent = getIntent();
        if (intent != null) {
            mUpdateInfo = intent.getParcelableExtra("updateInfo");
        }
        initView();
    }

    private void initView() {
        layout = (FrameLayout) findViewById(R.id.update_layout);
        mVersionTv = (TextView) findViewById(R.id.version_tv);
        mContentTv = (TextView) findViewById(R.id.content_tv);
        mIgnoreCb = (CheckBox) findViewById(R.id.ignore_cb);
        mUpdateBtn = (Button) findViewById(R.id.update_btn);
        mRemindBtn = (Button) findViewById(R.id.remind_btn);

        mIgnoreCb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mIgnore = isChecked;
            }
        });

        mUpdateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showUpdate();
            }
        });

        mRemindBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mIgnore = true;
                finish();
            }
        });
        if (mUpdateInfo != null) {
            mVersionTv.setText(mUpdateInfo.verName);
            mContentTv.setText(mUpdateInfo.desc);
        }
    }

    @Override
    protected void onDestroy() {
        if (mIgnore) {
            int i = 0;
            try {
                i = Integer.parseInt(mUpdateInfo.verCode);
            } catch (Exception e) {
                if (Constant.IS_DEBUG_MODE) {
                    e.printStackTrace();
                }
            }
            PreferenceUtil.commitInt(GoomeUpdateConstant.KEY_lIST_PREFERENCE_IGNORE_VERSION_CODE, i);
        }
        super.onDestroy();
    }

    private void showUpdate() {
        int type = NetworkUtil.checkNetwork(getApplicationContext());
        if (type == NetworkUtil.wifi) {
            GoomeUpdateAgent.startDownload(GoomeUpdateDialogActivity.this, mUpdateInfo, true);
            finish();
        } else if (type == NetworkUtil.mobile) {
            layout.setVisibility(View.INVISIBLE);
            showDialog();
        } else if (type == 0) {
            Toast.makeText(this, "当前网络不可用，请稍候重试", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    @Override
    public void finish() {
        super.finish();
        //关闭窗体动画显示
        this.overridePendingTransition(R.anim.goome_update_fade_out, 0);
    }

    /**
     * 移动网络状态下，确认流量更新
     */
    private void showDialog() {
        final Dialog dialog = new AlertDialog.Builder(GoomeUpdateDialogActivity.this).create();
        dialog.show();
        dialog.setCanceledOnTouchOutside(false);
        dialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_BACK) {
                    dialog.dismiss();
                    finish();
                    return true;
                } else {
                    return false;
                }
            }
        });
        Window window = dialog.getWindow();
        window.setContentView(R.layout.dialog_mobile);
        // 为确认按钮添加事件,执行退出应用操作
        window.findViewById(R.id.dialog_sure).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                GoomeUpdateAgent.startDownload(GoomeUpdateDialogActivity.this, mUpdateInfo, false);
                dialog.dismiss();
                finish();
            }
        });

        // 关闭alert对话框架
        window.findViewById(R.id.dialog_cancel).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                dialog.dismiss();
                finish();
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}