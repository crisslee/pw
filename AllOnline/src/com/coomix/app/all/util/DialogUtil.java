package com.coomix.app.all.util;

import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.coomix.app.all.R;

/**
 * dialog工具类,默认只显示确认按钮，可通过设置setClickListener来显示取消按钮
 *
 * @author 刘生健
 * @since 2015-7-22 下午04:57:56
 */
public class DialogUtil {

    private Dialog mDialog;
    private TextView titleTv;
    private TextView messageTv;
    private TextView btn1;
    private TextView btn2;

    /**
     * 创建dialog(btn2按钮不可见)
     *
     * @param msg dialog显示的信息
     * @see {@link #DialogUtil(Context, String, boolean)}
     */
    public DialogUtil(Context context, String msg) {
        createDialog(context, msg, false);
    }

    /**
     * 创建dialog
     *
     * @param msg dialog显示的信息
     * @param btn2Visible btn2按钮是否可见
     * @see {@link #DialogUtil(Context, String)}
     */
    public DialogUtil(Context context, String msg, boolean btn2Visible) {
        createDialog(context, msg, btn2Visible);
    }

    /**
     * 创建dialog
     *
     * @param msg dialog显示的信息
     * @param btn2Visible btn2按钮是否可见
     */
    private void createDialog(Context context, String msg, boolean btn2Visible) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View v = inflater.inflate(R.layout.dialog_dialogutil, null);
        LinearLayout layout = (LinearLayout) v.findViewById(R.id.dialogutil_layout);
        titleTv = (TextView) v.findViewById(R.id.dialogutil_title);
        messageTv = (TextView) v.findViewById(R.id.dialogutil_message);
        btn1 = (TextView) v.findViewById(R.id.dialogutil_btn1);
        btn2 = (TextView) v.findViewById(R.id.dialogutil_btn2);

        messageTv.setText(msg);
        if (btn2Visible) {
            btn2.setVisibility(View.VISIBLE);
            btn2.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    mDialog.dismiss();
                }
            });
        }

        mDialog = new Dialog(context, R.style.add_dialog);// 创建自定义样式dialog
        mDialog.setCanceledOnTouchOutside(false);

        btn1.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mDialog.dismiss();
            }
        });
        mDialog.setContentView(layout, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT));
    }

    public void setSystemAlert() {
        mDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
    }

    public DialogUtil setTitle(String title) {
        if (titleTv != null) {
            titleTv.setText(title);
        }
        return this;
    }

    public DialogUtil setMessage(String msg) {
        if (messageTv != null) {
            messageTv.setText(msg);
        }
        return this;
    }

    public DialogUtil setBtn1(String text1) {
        if (btn1 != null) {
            btn1.setText(text1);
        }
        return this;
    }

    public DialogUtil setBtn2(String text2) {
        if (btn2 != null) {
            btn2.setText(text2);
        }
        return this;
    }

    /**
     * 设置btn事件
     *
     * @param btn1Listener 传入null，默认为dialog.dismiss();
     * @param btn2Listener 传入null，默认为btn2按钮不可见;
     */
    public void setClickListener(OnClickListener btn1Listener, OnClickListener btn2Listener) {
        if (btn1Listener != null) {
            btn1.setOnClickListener(btn1Listener);
        } else {
            btn1.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    mDialog.dismiss();
                }
            });
        }
        if (btn2Listener != null) {
            btn2.setVisibility(View.VISIBLE);
            btn2.setOnClickListener(btn2Listener);
        } else {
            btn2.setVisibility(View.GONE);
        }
    }

    /**
     * 显示dialog
     *
     * @return true：dialog能正常显示（dialog == null 或 dialog is showing,返回false）
     */
    public boolean show() {
        if (mDialog != null && !mDialog.isShowing()) {
            mDialog.show();
            return true;
        }
        return false;
    }

    /**
     * 隐藏dialog
     *
     * @return true：dialog能正常隐藏（dialog == null 或 dialog is not showing,返回false）
     */
    public boolean dismiss() {
        if (mDialog != null && mDialog.isShowing()) {
            mDialog.dismiss();
            return true;
        }
        return false;
    }

    public boolean isShowing() {
        return mDialog != null && mDialog.isShowing();
    }

    /**
     * 用于标识是否同一类Dialog
     */
    private int tagId;

    /**
     * 用于标识是否同一类Dialog
     */
    public DialogUtil setTagId(int tagId) {
        this.tagId = tagId;
        return this;
    }

    /**
     * 用于标识是否同一类Dialog
     */
    public int getTagId() {
        return tagId;
    }
}
