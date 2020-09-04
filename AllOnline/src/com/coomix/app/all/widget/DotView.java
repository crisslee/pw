package com.coomix.app.all.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.util.AttributeSet;
import android.view.View;

import com.coomix.app.all.R;

public class DotView extends View {

    private int shapeColor;
    private Paint paint;

    public DotView(Context context) {
        this(context, null);
    }

    public DotView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DotView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setupAttributes(attrs);
        setupPaint();
    }

    /**
     * 获取属性
     * 
     * @param attrs
     */
    private void setupAttributes(AttributeSet attrs) {
        // 获取属性
        TypedArray typedArray = getContext().getTheme().obtainStyledAttributes(attrs, R.styleable.DotView, 0, 0);
        try {
            shapeColor = typedArray.getColor(R.styleable.DotView_shapeColor, Color.BLACK);
        } finally {
            typedArray.recycle();
        }
    }

    private void setupPaint() {
        paint = new Paint();
        paint.setStyle(Style.FILL);
        paint.setColor(shapeColor);
    }

    /**
     * 获取形状颜色
     * 
     * @return color
     */
    public int getShapeColor() {
        return shapeColor;
    }

    /**
     * 设置形状颜色
     * 
     * @param shapeColor
     */
    public void setShapeColor(int shapeColor) {
        this.shapeColor = shapeColor;
        invalidate();
        requestLayout();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        paint.setColor(shapeColor);
        int rw = getMeasuredWidth();
        int rh = getMeasuredHeight();
        int pl = getPaddingLeft();
        int pr = getPaddingRight();
        int pt = getPaddingTop();
        int pb = getPaddingBottom();
        float radius = (rw - pl - pr) < (rh - pt - pb) ? (rw - pl - pr) / 2.0f : (rh - pt - pb) / 2.0f;
        canvas.drawCircle(pl + (rw - pl - pr) / 2.0f, pt + (rh - pt - pb) / 2.0f, radius, paint);
    }
}
