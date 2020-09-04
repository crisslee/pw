package com.coomix.app.all.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.os.Handler;
import android.text.Html;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.coomix.app.all.AllOnlineApp;
import com.coomix.app.all.R;

/**
 * @author shishenglong
 * @since 2016年8月19日 下午3:47:41
 */
public class RegisterInfoDialog extends Dialog implements android.view.View.OnClickListener {

    private ProgressBar progressBar;
    private ImageView imageView;
    private TextView textView;
    private LinearLayout layoutButton;
    private Button btnOK, btnCancel;
    private View viewLine;
    private OnRegisterCancelListener listener;

    /** 提交数据 */
    public final static int COMMIT = 0;
    /** 报名成功 */
    public final static int SUCCESSED = 1;
    /** 报名失败 */
    public final static int FAILED = 2;
    /** 取消报名询问 */
    public final static int ASK = 3;
    /** 等待获取数据 */
    public final static int LOADING = 4;
    int iHeight = 0;
    private int dialogType = COMMIT;

    public RegisterInfoDialog(Context context, int type) {
        this(context, R.style.add_dialog, type);
    }

    public RegisterInfoDialog(Context context, int theme, int type) {
        super(context, theme);
        this.dialogType = type;
        initViews(type);
    }

    private void initViews(int type) {
        View mainView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_register_info, null);
        setContentView(mainView);
        progressBar = (ProgressBar) mainView.findViewById(R.id.progressBar);
        imageView = (ImageView) mainView.findViewById(R.id.imageViewStatus);
        textView = (TextView) mainView.findViewById(R.id.textViewInfo);
        layoutButton = (LinearLayout) mainView.findViewById(R.id.layoutButton);
        btnCancel = (Button) mainView.findViewById(R.id.buttonCancel);
        btnOK = (Button) mainView.findViewById(R.id.buttonOK);
        viewLine = (View) mainView.findViewById(R.id.gridLine);
        btnOK.setOnClickListener(this);
        btnCancel.setOnClickListener(this);

        WindowManager.LayoutParams params = getWindow().getAttributes();
        params.width = AllOnlineApp.screenWidth - 100 * 2 * AllOnlineApp.getDensity() / 480;
        iHeight = params.height = params.width * 2 / 3;
        params.gravity = Gravity.CENTER;
        getWindow().setAttributes(params);

        setDialogType(type);
        getWindow().setBackgroundDrawable(new BitmapDrawable());
    }

    LinearLayout.LayoutParams params = null;

    public void setDialogType(int type) {
        this.dialogType = type;
        switch (type) {
            case LOADING:
            default:
                setCanceledOnTouchOutside(false);
                setCancelable(false);
                progressBar.setVisibility(View.VISIBLE);
                imageView.setVisibility(View.GONE);
                layoutButton.setVisibility(View.GONE);
                viewLine.setVisibility(View.GONE);
                textView.setText(getContext().getString(R.string.please_wait));
                params = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
                textView.setLayoutParams(params);
                break;

            case SUCCESSED:
            case FAILED:
            case COMMIT:
                setCanceledOnTouchOutside(true);
                setCancelable(true);
                progressBar.setVisibility(View.GONE);
                imageView.setVisibility(View.VISIBLE);
                layoutButton.setVisibility(View.GONE);
                viewLine.setVisibility(View.GONE);
                if (type == SUCCESSED) {
                    imageView.setImageResource(R.drawable.register_success);
                    String msg = getContext().getString(R.string.register_signed_success)
                        + "<html><br/><font color=\"#9F9F9F\">" + getContext().getString(R.string.join_success)
                        + "</html>";
                    textView.setText(Html.fromHtml(msg));
                } else if (type == COMMIT) {
                    imageView.setImageResource(R.drawable.register_commit);
                    textView.setText(getContext().getString(R.string.commiting));
                } else {
                    imageView.setImageResource(R.drawable.register_fail);
                    textView.setText(getContext().getString(R.string.topic_send_image_error));
                }
                params = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
                textView.setLayoutParams(params);
                break;

            case ASK:
                setCanceledOnTouchOutside(true);
                setCancelable(true);
                progressBar.setVisibility(View.GONE);
                imageView.setVisibility(View.GONE);
                layoutButton.setVisibility(View.VISIBLE);
                viewLine.setVisibility(View.VISIBLE);
                textView.setText(getContext().getString(R.string.ask_give_up));
                params = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT,
                    iHeight - 140 * AllOnlineApp.getDensity() / 480);
                textView.setLayoutParams(params);
                params = (LayoutParams) layoutButton.getLayoutParams();
                params.height = 140 * AllOnlineApp.getDensity() / 480;
                layoutButton.setLayoutParams(params);
                break;
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.buttonCancel:
                dismiss();
                break;

            case R.id.buttonOK:
                dismiss();
                cancleRegister();
                break;
        }
    }

    private void cancleRegister() {
        if (listener != null) {
            listener.onRegisterCancel();
        }
    }

    public void setOnRegisterCancelListener(OnRegisterCancelListener listener) {
        this.listener = listener;
    }

    public interface OnRegisterCancelListener {
        public void onRegisterCancel();
    }

    public int getDialogType() {
        return dialogType;
    }

    public void stopAutoDismiss() {
        if (handler != null) {
            handler.removeCallbacksAndMessages(null);
        }
    }

    private Handler handler = new Handler();

    public void show(long delayMillis, final Activity finishActivity) {
        handler.removeCallbacksAndMessages(null);
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                dismiss();
                if (finishActivity != null) {
                    finishActivity.finish();
                }
            }
        }, delayMillis);
    }
}
