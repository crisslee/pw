package com.coomix.app.all.widget;

import android.content.Context;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.EditText;

public class ClearEditView extends EditText {
    private Drawable mButtonRight;

    private Drawable mButtonLeft;

    private Rect mBoundsRight;

    private Rect mBoundsLeft;

    // prevent duplicated action
    private int flag = 0;

    private EditText _intance;

    public ClearEditView(Context context) {
        super(context);

        _intance = this;
    }

    public ClearEditView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        _intance = this;
        this.addTextChangedListener(iboxTextWatcher);
    }

    public ClearEditView(Context context, AttributeSet attrs) {
        super(context, attrs);

        _intance = this;
        this.addTextChangedListener(iboxTextWatcher);
    }


    private TextWatcher iboxTextWatcher = new TextWatcher() {

        @Override
        public void onTextChanged(CharSequence s, int start, int before,
                                  int count) {
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count,
                                      int after) {
        }

        @Override
        public void afterTextChanged(Editable s) {
            showClearDrawable(s.toString(), _intance.isFocused());
        }
    };

    @Override
    public void setCompoundDrawables(Drawable left, Drawable top,
                                     Drawable right, Drawable bottom) {
        if (right != null) {
            mButtonRight = right;
        }

        if (left != null) {
            mButtonLeft = left;
        }
        if (flag == 0) {
            right = null;
        }
        super.setCompoundDrawables(left, top, right, bottom);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        try {
            if (this.isEnabled() && mButtonRight != null) {
                mBoundsRight = mButtonRight.getBounds();
                final int x = (int) event.getX();
                final int y = (int) event.getY();
                if (x >= (this.getWidth() - this.getPaddingRight() - mBoundsRight.width())
                        && x <= (this.getWidth() - this.getPaddingRight())
                        && y >= this.getPaddingTop()
                        && y <= (this.getHeight() - this.getPaddingBottom())) {
                    this.setText("");
                    if (_intance != null) {
                        _intance.setText("");
                    }
                    event.setAction(MotionEvent.ACTION_CANCEL);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return super.onTouchEvent(event);
    }

    //当光标聚焦到文本框时，不显示Hint
    @Override
    protected void onFocusChanged(boolean focused, int direction,
                                  Rect previouslyFocusedRect) {
        String str = this.getText().toString().trim();
        showClearDrawable(str, focused);
//        String hint = this.getHint().toString();
//        if (focused) {
//            this.setTag(hint);
//            this.setHint("");
//        } else {
//            hint = this.getTag().toString();
//            this.setHint(hint);
//        }
        super.onFocusChanged(focused, direction, previouslyFocusedRect);
    }

    private void showClearDrawable(String text, boolean focused) {
        try {
            if (text.length() > 0 && focused && _intance.isEnabled()) {
                if (flag != 1) {
                    flag = 1;
                    setCompoundDrawables(mButtonLeft, null, mButtonRight, null);
                }
            } else {
                if (flag != 0) {
                    flag = 0;
                    setCompoundDrawables(mButtonLeft, null, null, null);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void finalize() throws Throwable {
        mButtonRight = null;
        mButtonLeft = null;
        mBoundsRight = null;
        mBoundsLeft = null;

        super.finalize();
    }


    interface clearOnclick {

    }

}
