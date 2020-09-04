package com.coomix.app.all.dialog;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.Window;
import android.widget.TextView;
import com.coomix.app.all.R;

public class ProgressDialogEx implements DialogInterface {
    private boolean isAutoDismiss;
    private int duration;

    private OnCancelListener2 mCancelListener;

    private Handler mHandler;
    private ProgressDialog progressDialog;

    public interface OnCancelListener2 extends OnCancelListener {

        /**
         * call this method when the dialog is canceled by the user
         */
        void onCancel(DialogInterface dialog);

        void onAutoCancel(DialogInterface dialog);
    }

    public ProgressDialogEx(Context context) {
        init(context);
    }

    private void init(Context context) {
        progressDialog = new ProgressDialog(context);
        progressDialog.setIndeterminate(true);
        progressDialog.setCancelable(true);
        progressDialog.setCanceledOnTouchOutside(false);
        mHandler = new MyHandler(this);
    }

    public boolean isShowing() {
        return progressDialog != null && progressDialog.isShowing();
    }

    public static class MyHandler extends Handler {

        private ProgressDialogEx dialog;

        public MyHandler(ProgressDialogEx dialog) {
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
        if (isShowing()) {
            progressDialog.dismiss();
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

    public void setCancelable(boolean bCancel) {
        progressDialog.setCancelable(bCancel);
    }

    public static ProgressDialogEx show(Context context, String title, String message, boolean isAutoDismiss,
        int duration, OnCancelListener2 listener) {
        ProgressDialogEx dialog = new ProgressDialogEx(context);
        dialog.setTitle(title);
        dialog.setMessage(message);
        dialog.setAutoDismiss(isAutoDismiss);
        dialog.setOnAutoCancelListener(listener);
        if (duration <= 0) {
            dialog.setDuration(30000);
        } else {
            dialog.setDuration(duration);
        }
        dialog.show();
        return dialog;
    }

    public void show() {
        if (isAutoDismiss()) {
            mHandler.sendEmptyMessageDelayed(0, getDuration());
        }
        progressDialog.show();
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
     * @param listener the mCancelListener to set
     */
    public void setOnAutoCancelListener(OnCancelListener2 listener) {
        this.mCancelListener = listener;
        progressDialog.setOnCancelListener(listener);
    }

    OnCancelListener2 getOnCancelListener2() {
        return mCancelListener;
    }
}
