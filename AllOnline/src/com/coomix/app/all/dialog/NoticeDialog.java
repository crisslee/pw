package com.coomix.app.all.dialog;

import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import com.coomix.app.all.R;

/**
 * Created by think
 * 取消活动报名的dialog
 *
 * @since 2017/1/22.
 */
public class NoticeDialog extends Dialog {
    private TextView ok;
    private ImageView bg;

    public NoticeDialog(Context context) {
        this(context, R.style.notice_dialog);
    }

    public NoticeDialog(Context context, int theme) {
        super(context, theme);
        initViews();
    }

    private void initViews() {
        View main = LayoutInflater.from(getContext()).inflate(R.layout.dialog_notice, null);
        setContentView(main);
        ok = (TextView) main.findViewById(R.id.noti_confirm);
        bg = (ImageView) main.findViewById(R.id.cancel_register_bg);
        bg.bringToFront();
    }

    public NoticeDialog setOkListener(View.OnClickListener listener) {
        ok.setOnClickListener(listener);
        return this;
    }
}
