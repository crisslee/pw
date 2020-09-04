package com.coomix.app.all.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import com.coomix.app.all.R;

public class SideBar extends View {

    private Context mContext;

    private OnTouchingLetterChangedListener onTouchingLetterChangedListener;

    private String[] b = { "A", "B", "D", "G", "H", "J", "L", "Q", "S", "W", "X", "Y" };
    private int choose = -1;
    private Paint paint = new Paint();
    private int TEXT_SIZE = 23;

    public SideBar(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public SideBar(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SideBar(Context context) {
        super(context);
    }

    /**
     * 重写这个方法
     */
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        // 获取焦点改变背景颜色.
        int height = getHeight();// 获取对应高度
        int width = getWidth(); // 获取对应宽度
        int singleHeight = height / b.length;// 获取每一个字母的高度

        for (int i = 0; i < b.length; i++) {
            paint.setColor(getResources().getColor(R.color.color_text_h));
            paint.setAntiAlias(true);
            paint.setTextSize(TEXT_SIZE);

            // x坐标等于中间-字符串宽度的一半.
            float xPos = width / 2 - paint.measureText(b[i]) / 2;
            float yPos = singleHeight * i + singleHeight;
            canvas.drawText(b[i], xPos, yPos, paint);
            paint.reset();// 重置画笔
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        final int action = event.getAction();
        final float y = event.getY();// 点击y坐标
        final int oldChoose = choose;
        final OnTouchingLetterChangedListener listener = onTouchingLetterChangedListener;
        int singleHeight = getHeight() / b.length;
        final int c = ((int) ((y - singleHeight / 2) / getHeight() * b.length)) < 0 ? 0
            : ((int) ((y - singleHeight / 2) / getHeight() * b.length));// 点击y坐标所占总高度的比例*b数组的长度就等于点击b中的个数.

        switch (action) {
            case MotionEvent.ACTION_UP:
                choose = -1;
                invalidate();
                break;
            default:
                if (oldChoose != c) {
                    if (c >= 0 && c < b.length) {
                        if (listener != null) {
                            listener.onTouchingLetterChanged(b[c]);
                        }
                        choose = c;
                        invalidate();
                    }
                }

                break;
        }
        return true;
    }

    /**
     * 设置显示数组与字体大小
     */
    public void setSideBarText(String[] textArray, int textSize) {
        this.b = textArray;
        this.TEXT_SIZE = textSize;
        this.invalidate();
    }

    /**
     * 向外公开的方法
     */
    public void setOnTouchingLetterChangedListener(OnTouchingLetterChangedListener onTouchingLetterChangedListener) {
        this.onTouchingLetterChangedListener = onTouchingLetterChangedListener;
    }

    /**
     * 接口
     *
     * @author coder
     */
    public interface OnTouchingLetterChangedListener {
        public void onTouchingLetterChanged(String s);
    }
}