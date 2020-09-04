package com.coomix.app.all.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.GridView;

/**
 * Created by think on 2017/1/22.
 */
public class OrderInfoPicGridView extends GridView {
    public OrderInfoPicGridView(Context context) {
        super(context);
    }

    public OrderInfoPicGridView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public OrderInfoPicGridView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    /**
     * 重写该方法，达到使 GridView 自适应高度，避免显示不完全
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int expandSpec = MeasureSpec.makeMeasureSpec(Integer.MAX_VALUE >> 2, MeasureSpec.AT_MOST);
        super.onMeasure(widthMeasureSpec, expandSpec);
    }
}
