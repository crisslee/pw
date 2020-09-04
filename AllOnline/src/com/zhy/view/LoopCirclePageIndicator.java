package com.zhy.view;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.LinearLayout;

import com.coomix.app.all.R;
import com.coomix.app.all.util.ResizeUtil;
import com.viewpagerindicator.PageIndicator;

import static android.graphics.Paint.ANTI_ALIAS_FLAG;

public class LoopCirclePageIndicator extends LinearLayout implements PageIndicator
{

	private int mViewsCount;
	private int mWidth, mHeight;
	private int unSelectedTextColor, selectedTextColor;
	private int mIndicatorColor;
	private float mTranslationX;
	private Paint mPaint;

	// private ViewPager mViewPager;
	// public void setViewPager(ViewPager viewpager) {
	// mViewPager = viewpager;
	// }

	public LoopCirclePageIndicator(Context context)
	{
		this(context, null);
	}

	public LoopCirclePageIndicator(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		initParams(context);
		mPaintStroke.setStyle(Style.STROKE);
		mPaintStroke.setColor(Color.WHITE);
		mPaintStroke.setStrokeWidth(3);
		mPaintFill.setStyle(Style.FILL);
		mPaintFill.setColor(Color.WHITE);
		// mPaintStroke.setColor(a.getColor(R.styleable.CirclePageIndicator_strokeColor,
		// defaultStrokeColor));
		// mPaintStroke.setStrokeWidth(a.getDimension(R.styleable.CirclePageIndicator_strokeWidth,
		// defaultStrokeWidth));
		// mPaintFill.setStyle(Style.FILL);
		// mPaintFill.setColor(a.getColor(R.styleable.CirclePageIndicator_fillColor,
		// defaultFillColor));
		// mRadius = a.getDimension(R.styleable.CirclePageIndicator_radius,
		// defaultRadius);
	}

	private void initParams(Context context)
	{
		Resources res = context.getResources();
		// textSize = res.getDimension(R.dimen.textsize);

		mPaint = new Paint();

		unSelectedTextColor = res.getColor(R.color.color_text_l);
		selectedTextColor = res.getColor(R.color.color_text_h);
		mIndicatorColor = res.getColor(R.color.color_main);
		mPaint.setColor(mIndicatorColor);
		mPaint.setStrokeWidth(3 * ResizeUtil.getResizeFactor());
	}

	public void initParams(int viewsCount, int width, int height)
	{
		mViewsCount = viewsCount;
		mWidth = width;
		mHeight = height;
	}

	public void setViewsCount(int viewsCount)
	{
		mViewsCount = viewsCount;
		invalidate();
	}

	private final Paint mPaintStroke = new Paint(ANTI_ALIAS_FLAG);
	private final Paint mPaintFill = new Paint(ANTI_ALIAS_FLAG);
	private int interval = 20;

	@Override
	protected void onDraw(Canvas canvas)
	{
		// TODO Auto-generated method stub
		super.onDraw(canvas);
		float cx = 0, cy = 0;
		float radius = 3 * ResizeUtil.getResizeFactor();
		for (int i = 0; i < mViewsCount; i++)
		{
			canvas.drawCircle(cx, cy, radius, mPaintFill);
			cx += interval;
		}
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh)
	{
		super.onSizeChanged(w, h, oldw, oldh);
		// if (mTabCount != 0) {
		// mTabWidth = w / mTabCount;
		// } else {
		// }
	}

	public void setIndicatorColor(int indicatorColor)
	{
		this.mIndicatorColor = indicatorColor;
	}

	@Override
	protected void dispatchDraw(Canvas canvas)
	{
		super.dispatchDraw(canvas);
		canvas.save();
		canvas.translate(mTranslationX, getHeight() - 2);
		// canvas.drawLine(0 + selectedLinePadding, 0, mTabWidth -
		// selectedLinePadding, 0, mPaint);
		canvas.restore();
	}

	public void scroll(int position, float offset)
	{
		/**
		 * <pre>
		 *  0-1:position=0 ;1-0:postion=0;
		 * </pre>
		 */
		mTranslationX = getWidth() / mViewsCount * (position + offset);
		invalidate();
	}

	private int selectedPosition = 0;

	@Override
	public boolean dispatchTouchEvent(MotionEvent ev)
	{
		return super.dispatchTouchEvent(ev);
	}

	@Override
	public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void onPageSelected(int position)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void onPageScrollStateChanged(int state)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void setViewPager(ViewPager view)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void setViewPager(ViewPager view, int initialPosition)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void setCurrentItem(int item)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void setOnPageChangeListener(OnPageChangeListener listener)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void notifyDataSetChanged()
	{
		// TODO Auto-generated method stub

	}

}
