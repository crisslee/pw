package com.coomix.app.all.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ListView;

/**
 * 重写listview，设置最大高度
 * mMaxHeight暂定500，以后可以根据需要设置
 *
 * @author goome
 */
public class HeightListView extends ListView {

    /** listview最大高度 */
    //    private int mMaxHeight;
    //
    //    public int getListViewHeight() {
    //            return mMaxHeight;
    //    }
    //
    //    public void setListViewHeight(int mMaxHeight) {
    //            this.mMaxHeight = mMaxHeight;
    //    }
    public HeightListView(Context context) {
        super(context);
        // TODO Auto-generated constructor stub
    }

    public HeightListView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public HeightListView(Context context, AttributeSet attrs,
        int defStyle) {
        super(context, attrs, defStyle);
    }

    int mMaxHeight = 500;

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        if (heightMode == MeasureSpec.EXACTLY) {
            heightSize = heightSize <= mMaxHeight ? heightSize : (int) mMaxHeight;
        }
        if (heightMode == MeasureSpec.UNSPECIFIED) {
            heightSize = heightSize <= mMaxHeight ? heightSize : (int) mMaxHeight;
        }
        if (heightMode == MeasureSpec.AT_MOST) {
            heightSize = heightSize <= mMaxHeight ? heightSize : (int) mMaxHeight;
        }
        int maxHeightMeasureSpec = MeasureSpec.makeMeasureSpec(heightSize,
            heightMode);
        super.onMeasure(widthMeasureSpec, maxHeightMeasureSpec);
    }
}
