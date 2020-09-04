package com.coomix.app.all.widget;

import android.content.Context;
import android.text.ClipboardManager;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

public class TextViewEx extends TextView {
    private ClipboardManager clipboard;

    public TextViewEx(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public TextViewEx(Context context) {
        super(context);
        init(context);
    }

    private void init(final Context context) {
        setClickable(true);
        clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        /* setFocusableInTouchMode(true);
        setFocusable(true);  
        setClickable(true);  
        setLongClickable(true);  
        setMovementMethod(  ArrowKeyMovementMethod.getInstance());  
        setText(getText(),BufferType.SPANNABLE ); */

        setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                copy();
                if (mListener != null) {
                    mListener.onCopy(v, getText());
                }
                //Toast.makeText(context, getResources().getString(R.string.copy_hint), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private OnCopyListener mListener;

    public void setOnCopyListener(OnCopyListener l) {
        mListener = l;
    }

    public interface OnCopyListener {
        void onCopy(View view, CharSequence text);
    }

    public void copy() {
        setText(getText(), BufferType.SPANNABLE);
        clipboard.setText(getText());
    }
}
