package com.coomix.app.all.widget;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

public class MTouchViewPager extends ViewPager {

    public MTouchViewPager(Context context) {
        super(context);
    }

    public MTouchViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    private boolean mIsDisallowIntercept = false;

    @Override
    public void requestDisallowInterceptTouchEvent(boolean disallowIntercept) {
        // keep the info about if the innerViews do
        // requestDisallowInterceptTouchEvent
        mIsDisallowIntercept = disallowIntercept;
        super.requestDisallowInterceptTouchEvent(disallowIntercept);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        // the incorrect array size will only happen in the multi-touch
        // scenario.
        try {
            if (ev.getPointerCount() > 1 && mIsDisallowIntercept) {
                requestDisallowInterceptTouchEvent(false);
                boolean handled = super.dispatchTouchEvent(ev);
                requestDisallowInterceptTouchEvent(true);
                return handled;
            } else {
                return super.dispatchTouchEvent(ev);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}