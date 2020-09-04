package com.coomix.app.all.util;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.Window;
import android.widget.TextView;
import com.coomix.app.all.R;

public class CommunityProgressDialogEx implements DialogInterface {
    private boolean isAutoDismiss;
    private int duration;

    private OnCancelListener2 mCancelListener;

    private Handler mHandler;
    private ProgressDialog progressDialog;
    private boolean bKeyBackPress = false;

    public interface OnCancelListener2 extends OnCancelListener {

        /**
         * call this method when the dialog is canceled by the user
         */
        void onCancel(DialogInterface dialog);

        void onAutoCancel(DialogInterface dialog);
    }

    public CommunityProgressDialogEx(Context context) {
        init(context);
    }

    private void init(Context context) {
        progressDialog = new ProgressDialog(context);
        progressDialog.setIndeterminate(true);
        progressDialog.setCancelable(true);
        progressDialog.setCanceledOnTouchOutside(false);
        mHandler = new MyHandler(this);
        progressDialog.setOnKeyListener(new OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                // TODO Auto-generated method stub
                if (keyCode == KeyEvent.KEYCODE_BACK) {
                    bKeyBackPress = true;
                } else {
                    bKeyBackPress = false;
                }
                return false;
            }
        });
    }

    public boolean isKeyBackPressed() {
        return bKeyBackPress;
    }

    public void setOnDismissListener(OnDismissListener dismissListener) {
        progressDialog.setOnDismissListener(dismissListener);
    }

    public boolean isShowing() {
        return progressDialog != null && progressDialog.isShowing();
    }

    public static class MyHandler extends Handler {

        private CommunityProgressDialogEx dialog;

        public MyHandler(CommunityProgressDialogEx dialog) {
            this.dialog = dialog;
        }

        @Override
        public void handleMessage(Message msg) {
            if (dialog.isShowing()) {
                try {
                    dialog.dismiss();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (dialog.getOnCancelListener2() != null) {
                    dialog.getOnCancelListener2().onAutoCancel(dialog);
                }
            }
        }
    }

    public void setTitle(String title) {
        progressDialog.setTitle(title);
    }

    public void dismiss() {
        try {
            if (progressDialog != null && progressDialog.isShowing()) {
                progressDialog.dismiss();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void cancel() {
        progressDialog.cancel();
    }

    public void setMessage(String message) {
        progressDialog.setMessage(message);
    }

    public void setCancelOnTouchOutside(boolean param) {
        progressDialog.setCanceledOnTouchOutside(param);
    }

    public static CommunityProgressDialogEx show(Context context, String title, String message, boolean isAutoDismiss,
        int duration, OnCancelListener2 listener) {
        CommunityProgressDialogEx dialog = new CommunityProgressDialogEx(context);
        dialog.setTitle(title);
        dialog.setMessage(message);
        dialog.setAutoDismiss(isAutoDismiss);
        dialog.setOnAutoCancelListener(listener);
        dialog.setDuration(duration);
        dialog.show(message);
        return dialog;
    }

    public void show(String msg) {
        if (isAutoDismiss()) {
            mHandler.sendEmptyMessageDelayed(0, getDuration());
        }
        try {
            progressDialog.show();
            Window window = progressDialog.getWindow();
            window.setContentView(R.layout.dialog_progress);
            try {
                if (!TextUtils.isEmpty(msg)) {
                    ((TextView) window.findViewById(R.id.message)).setText(msg);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * @return the isAutoDismiss
     */
    public boolean isAutoDismiss() {
        return isAutoDismiss;
    }

    /**
     * @param isAutoDismiss the isAutoDismiss to set
     */
    public void setAutoDismiss(boolean isAutoDismiss) {
        this.isAutoDismiss = isAutoDismiss;
    }

    /**
     * @return the duration
     */
    public int getDuration() {
        return duration;
    }

    /**
     * @param duration the duration to set
     */
    public void setDuration(int duration) {
        this.duration = duration;
    }

    /**
     * the mCancelListener to set
     */
    public void setOnAutoCancelListener(OnCancelListener2 listener) {
        this.mCancelListener = listener;
        if (listener != null) {
            progressDialog.setOnCancelListener(listener);
        }
    }

    OnCancelListener2 getOnCancelListener2() {
        return mCancelListener;
    }
}
