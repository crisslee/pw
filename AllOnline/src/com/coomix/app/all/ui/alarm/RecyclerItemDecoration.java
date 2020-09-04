package com.coomix.app.all.ui.alarm;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

/**
 * Created by herry on 2016/12/8.
 */
public class RecyclerItemDecoration extends RecyclerView.ItemDecoration {
    private Paint mPaint;
    private Drawable mDivider;
    /*分割线的厚度*/
    private int mDividerThick;
    /*默认垂直方向*/
    private int mOrientation;
    /*分割线切除像素数*/
    private int mDividerClipSize;

    private boolean mDrawFooter;

    /**
     * 默认垂直方向
     *
     * @param context
     */
    public RecyclerItemDecoration(Context context, int drawableId, int dividerClipSize) {
        this(context, LinearLayoutManager.VERTICAL, drawableId, dividerClipSize);
    }


    /**
     * @param context
     * @param orientation RecyclerView列表方向
     * @param drawableId  分割线的图片ID
     */
    public RecyclerItemDecoration(Context context, int orientation, int drawableId, int dividerClipSize) {
        this(context, orientation, drawableId, dividerClipSize, true);
    }

    public RecyclerItemDecoration(Context context, int orientation, int drawableId, int dividerClipSize, boolean drawFooter) {
        mOrientation = orientation;
        mDivider = ContextCompat.getDrawable(context, drawableId);
        mDividerThick = mDivider.getIntrinsicHeight();
        mDividerClipSize = dividerClipSize;
        mDrawFooter = drawFooter;
    }

    /**
     * @param dividerThick
     * @param dividerColor
     */
    public RecyclerItemDecoration(int dividerThick, int dividerColor, int dividerClipSize) {
        this(LinearLayoutManager.VERTICAL, dividerThick, dividerColor, dividerClipSize);
    }

    /**
     * 自定义颜色分割线
     *
     * @param orientation  RecyclerView的列表方向
     * @param dividerThick 分割线的厚度
     * @param dividerColor 分割线的颜色，Color值，不是Resource ID
     */
    public RecyclerItemDecoration(int orientation, int dividerThick, int dividerColor, int dividerClipSize) {
        this(orientation, dividerThick, dividerColor, dividerClipSize, true);
    }

    public RecyclerItemDecoration(int orientation, int dividerThick, int dividerColor, int dividerClipSize, boolean drawFooter) {
        mOrientation = orientation;
        mDividerThick = dividerThick;
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setColor(dividerColor);
        mPaint.setStyle(Paint.Style.FILL);
        mDividerClipSize = dividerClipSize;
        mDrawFooter = drawFooter;
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        super.getItemOffsets(outRect, view, parent, state);
        switch (mOrientation) {
            case LinearLayoutManager.VERTICAL:
                outRect.set(0, 0, 0, mDividerThick);
                break;
            case LinearLayoutManager.HORIZONTAL:
                outRect.set(0, 0, mDividerThick, 0);
                break;
        }

    }

    @Override
    public void onDraw(Canvas c, RecyclerView parent, RecyclerView.State state) {
        super.onDraw(c, parent, state);
        switch (mOrientation) {
            case LinearLayoutManager.VERTICAL:
                drawHorizontalDivider(c, parent);
                break;
            case LinearLayoutManager.HORIZONTAL:
                drawVerticalDivider(c, parent);
                break;
            default:
                break;
        }
    }

    /*绘制水平分割线*/
    private void drawHorizontalDivider(Canvas canvas, RecyclerView parent) {
        int left = parent.getPaddingLeft();
        left += mDividerClipSize;
        int right = parent.getMeasuredWidth() - parent.getPaddingRight();
        RecyclerView.Adapter adapter = parent.getAdapter();
        if (adapter == null) {
            return;
        }
        int adapterCount = adapter.getItemCount();
        int childCount = parent.getChildCount();
        if (childCount == 0 || adapterCount == 0) {
            return;
        }
        View child = null;
        int adapterPosition = -1;
        RecyclerView.LayoutParams params = null;
        for (int i = 0; i < childCount; i++) {
            child = parent.getChildAt(i);
            adapterPosition = parent.getChildAdapterPosition(child);
            params = (RecyclerView.LayoutParams) child.getLayoutParams();
            int top = child.getBottom() + params.bottomMargin;
            int bottom = top + mDividerThick;
            //it is drawable
            if (mDivider != null) {
                if (adapterPosition == adapterCount - 1) {
                    if (mDrawFooter) {
                        left -= mDividerClipSize;
                    } else {
                        continue;
                    }
                }
                mDivider.setBounds(left, top, right, bottom);
                mDivider.draw(canvas);
            } else if (mPaint != null) {
                //it is color
                if (adapterPosition == adapterCount - 1) {
                    if (mDrawFooter) {
//                        left -= mDividerClipSize;
                    } else {
                        continue;
                    }
                }
                canvas.drawRect(left, top, right, bottom, mPaint);
            }
        }
    }

    private void drawVerticalDivider(Canvas canvas, RecyclerView parent) {
        int top = parent.getPaddingTop();
        int bottom = parent.getMeasuredHeight() - parent.getPaddingBottom();
        bottom -= mDividerClipSize;
        RecyclerView.Adapter adapter = parent.getAdapter();
        if (adapter == null) {
            return;
        }
        int adapterCount = adapter.getItemCount();
        int childCount = parent.getChildCount();
        if (childCount == 0 || adapterCount == 0) {
            return;
        }
        View child = null;
        int adapterPosition = -1;
        RecyclerView.LayoutParams params = null;
        for (int i = 0; i < childCount; i++) {
            child = parent.getChildAt(i);
            adapterPosition = parent.getChildAdapterPosition(child);
            params = (RecyclerView.LayoutParams) child.getLayoutParams();
            int left = child.getRight() + params.rightMargin;
            int right = left + mDividerThick;
            if (mDivider != null) {
                if (adapterPosition == adapterCount - 1) {
                    if (mDrawFooter) {
//                        bottom += mDividerClipSize;
                    } else {
                        continue;
                    }
                }
                mDivider.setBounds(left, top, right, bottom);
                mDivider.draw(canvas);
            } else if (mPaint != null) {
                if (adapterPosition == adapterCount - 1) {
                    if (mDrawFooter) {
                        bottom += mDividerClipSize;
                    } else {
                        continue;
                    }
                }
                canvas.drawRect(left, top, right, bottom, mPaint);
            }
        }
    }
}
