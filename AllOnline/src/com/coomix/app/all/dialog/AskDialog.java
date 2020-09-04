package com.coomix.app.all.dialog;

import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import com.coomix.app.all.R;

/**
 * @author shishenglong
 * @since 2016年10月15日 下午7:08:44
 */
public class AskDialog extends Dialog {
    private TextView textTitle = null;
    private TextView textMsg = null;
    private ImageView imageView = null;
    private TextView textYes = null;
    private TextView textNo = null;
    private ImageView imageClose;
    private TextView textBottom;

    public AskDialog(Context context) {
        this(context, R.style.delete_dialog);
    }

    public AskDialog(Context context, int theme) {
        super(context, theme);

        initViews();
    }

    private void initViews() {
        View mainView = LayoutInflater.from(getContext()).inflate(R.layout.dialog, null);
        setContentView(mainView);

        textTitle = (TextView) mainView.findViewById(R.id.dialog_title);
        imageView = (ImageView) mainView.findViewById(R.id.dialog_log);
        textMsg = (TextView) mainView.findViewById(R.id.dialog_text);
        textYes = (TextView) mainView.findViewById(R.id.dialog_sure);
        textNo = (TextView) mainView.findViewById(R.id.dialog_cancel);
        imageClose = (ImageView) mainView.findViewById(R.id.imageRightClose);
        textBottom = (TextView) mainView.findViewById(R.id.textBottom);

        setIconVisibility(View.GONE);

        textNo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
    }

    public AskDialog setShowTitle(String msg) {
        textTitle.setText(msg);
        return this;
    }

    public AskDialog setShowTitle(int strId) {
        textTitle.setText(strId);
        return this;
    }

    public AskDialog setMsgGravity(int gravity) {
        textMsg.setGravity(gravity);
        return this;
    }

    public AskDialog setShowMsg(String msg) {
        textMsg.setText(msg);
        return this;
    }

    public AskDialog setShowMsg(int strId) {
        textMsg.setText(strId);
        return this;
    }

    public AskDialog setYesText(String msg) {
        textYes.setText(msg);
        return this;
    }

    public AskDialog setYesText(int strId) {
        textYes.setText(strId);
        return this;
    }

    public AskDialog setNoText(String msg) {
        textNo.setText(msg);
        return this;
    }

    public AskDialog setNoText(int strId) {
        textNo.setText(strId);
        return this;
    }

    public AskDialog setShowIcon(Drawable drawable) {
        imageView.setVisibility(View.VISIBLE);
        imageView.setImageDrawable(drawable);
        return this;
    }

    public AskDialog setShowIcon(int imageId) {
        imageView.setVisibility(View.VISIBLE);
        imageView.setImageResource(imageId);
        return this;
    }

    public AskDialog setTitleVisibility(int visibility) {
        textTitle.setVisibility(visibility);
        return this;
    }

    public AskDialog setIconVisibility(int visibility) {
        if (visibility == View.GONE) {
            textMsg.setGravity(Gravity.CENTER);
        } else {
            textMsg.setGravity(Gravity.LEFT);
        }
        imageView.setVisibility(visibility);
        return this;
    }

    public AskDialog setOnYesClickListener(android.view.View.OnClickListener listener) {
        textYes.setOnClickListener(listener);
        return this;
    }

    public AskDialog setOnNoClickListener(android.view.View.OnClickListener listener) {
        textNo.setOnClickListener(listener);
        return this;
    }

    public AskDialog showBottomText(int textId, int imageCloseID) {
        imageClose.setVisibility(View.VISIBLE);
        textBottom.setVisibility(View.VISIBLE);
        if (textId > 0) {
            textBottom.setText(textId);
        }
        if (imageCloseID > 0) {
            imageClose.setImageResource(imageCloseID);
        }
        imageClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        textBottom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        return this;
    }

    public void setBottomTextClickListener(View.OnClickListener listener) {
        if (textBottom == null) {
            return;
        }
        textBottom.setOnClickListener(listener);
    }

    public void setYesTextColor(int colorId) {
        textYes.setTextColor(getContext().getResources().getColor(colorId));
    }

    public void setNoTextColor(int colorId) {
        textNo.setTextColor(getContext().getResources().getColor(colorId));
    }
}
