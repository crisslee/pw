package com.coomix.app.all.widget;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.FontMetricsInt;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;

import com.coomix.app.all.R;

public class BottomActionItemView extends View {
    private static final String TAG = "BottomActionItemView";

    private Drawable mActionIcon = null;
    private ColorStateList mTitleColorStateList;
    private int mActionIconWidth;
    private int mActionIconHeight;
    private String mActionTitle = "";
    private int mActionTitleWidth;
    private int mActionTitleHeight;
    private Drawable mTipBgDrawable;
    private int mTipBgDrawableWidth;
    private int mTipBgDrawableHeight;
    private int mTipTextHMargin;

    private Paint mPaint;
    private int mTitleSize;
    private int mTitleColor;
    private int mTipSize;
    private int mTipColor;
    private Rect mRect;
    private int mVSpan;

    //
    /* 消息提醒数目 */
    private int mNotifyCount;

    public BottomActionItemView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    public BottomActionItemView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BottomActionItemView(Context context) {
        this(context, null);
    }

    private void init(Context context, AttributeSet attrs) {
        setFocusable(true);
        setPadding(10, 10, 10, 10);
        TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.bottom_action_item);
        if (a != null) {
            mActionIcon = a.getDrawable(R.styleable.bottom_action_item_actionIcon);
            mTitleColorStateList = a.getColorStateList(R.styleable.bottom_action_item_actionTitleColor);
            mActionTitle = a.getString(R.styleable.bottom_action_item_actionTitle);
            mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            Resources res = getResources();
            mTitleSize = res.getDimensionPixelSize(R.dimen.bottom_action_item_title_size);
            mTipSize = res.getDimensionPixelSize(R.dimen.bottom_action_item_tip_size);
            mTipColor = res.getColor(R.color.bottom_action_item_tip_color);
            mRect = new Rect();
            mVSpan = res.getDimensionPixelSize(R.dimen.bottom_action_item_v_span);
            mTipTextHMargin = res.getDimensionPixelSize(R.dimen.bottom_action_item_tip_h_margin);
            mTipBgDrawable = res.getDrawable(R.drawable.count_tip_bg);
            mTipBgDrawableWidth = mTipBgDrawable.getIntrinsicWidth();
            mTipBgDrawableHeight = mTipBgDrawable.getIntrinsicHeight();
            mActionIconWidth = mActionIcon.getIntrinsicWidth();
            mActionIconHeight = mActionIcon.getIntrinsicHeight();
            mPaint.setTextSize(mTitleSize);
            mActionTitleWidth = Math.round(mPaint.measureText(mActionTitle));
            FontMetricsInt fm = mPaint.getFontMetricsInt();
            mActionTitleHeight = fm.bottom - fm.top;
            mNotifyCount = 0;
            mActionIcon.setCallback(this);
            mTitleColor = mTitleColorStateList.getDefaultColor();
            a.recycle();
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = mActionIconHeight + mActionTitleHeight + 2 * mVSpan;
        setMeasuredDimension(width, height);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int width = getWidth();
        int height = getHeight();
        if (width == 0 || height == 0) {
            return;
        }
        drawIcon(canvas, width, height);
        drawTitle(canvas, width, height);
        drawNotifyIfNeeded(canvas, width, height);
    }

    @Override
    protected void drawableStateChanged() {
        super.drawableStateChanged();
        mActionIcon.setState(getDrawableState());
        mTitleColor = mTitleColorStateList.getColorForState(getDrawableState(), mTitleColorStateList.getDefaultColor());
        invalidate();
    }

    // private


    private void drawIcon(Canvas canvas, int width, int height) {
        int left = width / 2 - mActionIconWidth / 2;
        int top = height / 2 - (mActionIconHeight + mActionTitleHeight) / 2;
        int right = left + mActionIconWidth;
        int bottom = top + mActionIconHeight;
        mRect.setEmpty();
        mRect.set(left, top, right, bottom);
        mActionIcon.setBounds(mRect);
        mActionIcon.draw(canvas);
    }

    private void drawTitle(Canvas canvas, int width, int height) {
        mPaint.setColor(mTitleColor);
        mPaint.setTextSize(mTitleSize);
        int startX = width / 2 - mActionTitleWidth / 2;
        int top = mRect.bottom;
        int startY = top - mPaint.getFontMetricsInt().top;
        canvas.drawText(mActionTitle, startX, startY, mPaint);
    }

    private void drawNotifyIfNeeded(Canvas canvas, int width, int height) {
        if (mNotifyCount <= 0) {
            return;
        }
        String notifyCount = formatCount();
        int top = mRect.top;
        int right = mRect.right;
        mPaint.setTextSize(mTipSize);
        int textWidth = Math.round(mPaint.measureText(notifyCount));
        FontMetricsInt fm = mPaint.getFontMetricsInt();
        int textHeight = fm.bottom - fm.top;
        mRect.setEmpty();
        mRect.left = right - Math.round(mTipBgDrawableWidth / 2);
        mRect.top = top - 1;
        int bgWidth = mTipTextHMargin + textWidth + mTipTextHMargin;
        if (bgWidth < mTipBgDrawableWidth) {
            bgWidth = mTipBgDrawableWidth;
        }
        mRect.right = mRect.left + bgWidth;
        mRect.bottom = mRect.top + mTipBgDrawableHeight;
        mTipBgDrawable.setBounds(mRect);
        mTipBgDrawable.draw(canvas);
        int startX = mRect.left + mTipTextHMargin;
        int startY = mTipBgDrawableHeight / 2 - textHeight / 2 - fm.top;
        mPaint.setColor(mTipColor);
        canvas.drawText(notifyCount, startX, startY, mPaint);
    }


    //private
    private String formatCount() {
        if (mNotifyCount >= 100) {
            return String.valueOf("99+");
        } else {
            return String.valueOf(mNotifyCount);
        }
    }

    // public
    public void setNotification(int count) {
        if (mNotifyCount == count) {
            return;
        }
        mNotifyCount = count;
        invalidate();
    }

    public void removeNotification() {
        if (mNotifyCount == 0) {
            return;
        }
        mNotifyCount = 0;
        invalidate();
    }
}
