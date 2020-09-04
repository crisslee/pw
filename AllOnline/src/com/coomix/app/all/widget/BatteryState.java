package com.coomix.app.all.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import com.coomix.app.all.R;

/**
 * Created by Administrator on 2017/11/21.
 */
public class BatteryState extends View {
    private float width;
    private float height;
    private Paint mPaint;
    private float powerQuantity = 0.16f;//0.5f;//电量

    public BatteryState(Context context) {
        super(context);
        mPaint = new Paint();
    }

    public BatteryState(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        mPaint = new Paint();
    }

    public BatteryState(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mPaint = new Paint();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        //计算控件尺寸
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        //绘制界面
        super.onDraw(canvas);
        int vw = getWidth();
        int vh = getHeight();
        Bitmap batteryBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.battery_empty);//读取图片资源
        width = batteryBitmap.getWidth();
        height = batteryBitmap.getHeight();
        if (powerQuantity > 0.15f && powerQuantity <= 1) {
            //电量少于15%显示红色
            mPaint.setColor(Color.GREEN);
        } else if (powerQuantity >= 0 && powerQuantity <= 0.15f) {
            mPaint.setColor(Color.RED);
        }
        //居中
        float bgLeft = (vw - width) / 2.0f;
        float bgTop = (vh - height) / 2.0f;
        //计算绘制电量的区域
        float left = width / 18;
        float top = height / 10;
        left += bgLeft;
        top += bgTop;
        float right = left + (width - width * 4 / 18) * powerQuantity;
        float bottom = top + height * 0.8f;

        canvas.drawRect(left, top, right, bottom, mPaint);
        canvas.drawBitmap(batteryBitmap, bgLeft, bgTop, mPaint);
    }

    public void refreshPower(float power) {
        powerQuantity = power;
        if (powerQuantity > 1.0f) {
            powerQuantity = 1.0f;
        }
        if (powerQuantity < 0) {
            powerQuantity = 0;
        }
        invalidate();
    }
}
