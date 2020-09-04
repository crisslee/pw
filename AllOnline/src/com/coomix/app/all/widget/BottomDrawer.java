package com.coomix.app.all.widget;

import android.animation.ValueAnimator;
import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ViewConfiguration;
import android.widget.RelativeLayout;

/**
 * File Description:
 *
 * @author felixqiu
 * @since 2019-12-18.
 */
public class BottomDrawer extends RelativeLayout {
    int slop;
    float initY;
    float lastY;
    float keep, max;
    private static final int TIME = 200;

    public BottomDrawer(Context context) {
        super(context);
        init();
    }

    public BottomDrawer(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public BottomDrawer(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        slop = ViewConfiguration.get(getContext()).getScaledTouchSlop();

    }

    public void setKeep(float keep) {
        this.keep = keep;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        float y = ev.getY();
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                initY = y;
                lastY = y;
                max = getHeight() - keep;
                break;
            case MotionEvent.ACTION_MOVE:
                float dy = Math.abs(y - lastY);
                if (dy > slop) {
                    return true;
                }
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                break;
        }
        return super.onInterceptTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float y = event.getY();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                initY = y;
                lastY = y;
                break;
            case MotionEvent.ACTION_MOVE:
                float dy = y - lastY;
                float ty = getTranslationY();
                float nty = ty + dy;
                if (nty < 0) {
                    nty = 0;
                } else if (nty > max) {
                    nty = max;
                }
                setTranslationY(nty);
                return true;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                float uty = getTranslationY();
                if (uty != 0 && uty != max) {
                    if (uty > max / 2) {
                        ValueAnimator close = ValueAnimator.ofFloat(uty, max).setDuration(TIME);
                        close.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                            @Override
                            public void onAnimationUpdate(ValueAnimator animation) {
                                float curr = (float) animation.getAnimatedValue();
                                setTranslationY(curr);
                            }
                        });
                        close.start();
                    } else {
                        ValueAnimator open = ValueAnimator.ofFloat(uty, 0).setDuration(TIME);
                        open.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                            @Override
                            public void onAnimationUpdate(ValueAnimator animation) {
                                float curr = (float) animation.getAnimatedValue();
                                setTranslationY(curr);
                            }
                        });
                        open.start();
                    }
                }
                break;
        }
        return super.onTouchEvent(event);
    }
}
