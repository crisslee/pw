package com.coomix.app.all.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.GridView;

public class CommunityGridView extends GridView {
    public CommunityGridView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CommunityGridView(Context context) {
        super(context);
    }

    public CommunityGridView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int expandSpec = MeasureSpec.makeMeasureSpec(Integer.MAX_VALUE >> 2, MeasureSpec.AT_MOST);
        // LogUtil.e("heightMeasureSpec="+heightMeasureSpec+",expandSpecHei="+expandSpec);
        super.onMeasure(widthMeasureSpec, expandSpec);
    }

    /*
     * @Override public boolean onTouchEvent(MotionEvent ev) { // TODO
     * Auto-generated method stub return false; }
     */

    private float mTouchX;
    private float mTouchY;
    private OnClickListener mClickBlankPosListener;
    private boolean isMoved;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (mClickBlankPosListener != null) {
            if (!isEnabled()) {
                return isClickable() || isLongClickable();
            }
            int action = event.getActionMasked();
            float x = event.getX();
            float y = event.getY();
            final int motionPosition = pointToPosition((int) x, (int) y);
            if (motionPosition == INVALID_POSITION) {
                switch (action) {
                    case MotionEvent.ACTION_DOWN:
                        mTouchX = x;
                        mTouchY = y;
                        isMoved = false;
                        break;
                    case MotionEvent.ACTION_MOVE:
                        if (Math.abs(mTouchX - x) > 10 || Math.abs(mTouchY - y) > 10) {
                            isMoved = true;
                        }
                        break;
                    case MotionEvent.ACTION_UP:
                        mTouchX = 0;
                        mTouchY = 0;
                        if (isMoved) {
                            isMoved = false;
                        } else {
                            mClickBlankPosListener.onClick(this);
                            return true;
                        }
                        break;
                }
            }
        }
        return super.onTouchEvent(event);
    }

    /**
     * 设置GridView的空白区域的点击事件
     */
    public void setOnClickBlankPositionListener(OnClickListener listener) {
        mClickBlankPosListener = listener;
    }
}
