package com.zhy.view;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.coomix.app.all.R;
import com.coomix.app.all.util.ResizeUtil;

public class SimpleViewPagerIndicator extends LinearLayout
{

	private float textSize;
	private int unSelectedTextColor, selectedTextColor;
	private int dividerColor;
	private int mIndicatorColor;
	private Paint mBottomLinePaint;
	private Paint mPaint;
	private float mTranslationX;
	private float selectedLinePadding;
	private int mTabCount;
	private int mTabWidth;

	private String[] titleStringArray;
	private TextView[] titleViews;

	public SimpleViewPagerIndicator(Context context)
	{
		this(context, null);
		initParams(context);
	}

	public SimpleViewPagerIndicator(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		initParams(context);
	}

	public void setParams(float textSize, int selectedTextColor, int unSelectedTextColor, float sidePadding)
	{
		this.textSize = textSize;
		this.selectedTextColor = selectedTextColor;
		this.unSelectedTextColor = unSelectedTextColor;
		selectedLinePadding = sidePadding;
	}

	private void initParams(Context context)
	{
		Resources res = context.getResources();
		// textSize = res.getDimension(R.dimen.textsize);
		mBottomLinePaint = new Paint();
		mPaint = new Paint();

		textSize = 45;
		unSelectedTextColor = res.getColor(R.color.color_text_l);
		selectedTextColor = res.getColor(R.color.color_text_h);
		dividerColor = res.getColor(R.color.color_bg);
		mIndicatorColor = res.getColor(R.color.color_main);
		mBottomLinePaint.setColor(res.getColor(R.color.community_main_tab_bottom));
		mBottomLinePaint.setStrokeWidth(1 * ResizeUtil.getResizeFactor());
		mPaint.setColor(mIndicatorColor);
		mPaint.setStrokeWidth(3 * ResizeUtil.getResizeFactor());

		selectedLinePadding = (int) (15 * ResizeUtil.getResizeFactor());
	}

	private ViewPager mViewPager;

	public void setViewPager(ViewPager viewpager)
	{
		mViewPager = viewpager;
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh)
	{
		super.onSizeChanged(w, h, oldw, oldh);
		if (mTabCount != 0)
		{
			mTabWidth = w / mTabCount;
		}
		else
		{
		}
	}

	public void setTitles(String[] titles)
	{
		// initParams(getContext());

		titleViews = new TextView[titles.length];
		titleStringArray = titles;
		mTabCount = titles.length;
		generateTitleView();
	}

	public TextView getTitleTextView(int position)
	{
		if(position >= 0 && titleViews.length > 0 && position < titleViews.length)
		{
			return titleViews[position];
		}
		return null;
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
		canvas.drawLine(0 + selectedLinePadding, getHeight() - 1, 3 * mTabWidth - selectedLinePadding, getHeight() - 1, mBottomLinePaint);
		canvas.translate(mTranslationX, getHeight() - 2);
		canvas.drawLine(0 + selectedLinePadding, 0, mTabWidth - selectedLinePadding, 0, mPaint);
		canvas.restore();
	}

	@Override
	protected void onDraw(Canvas canvas)
	{
		// TODO Auto-generated method stub
		super.onDraw(canvas);
	}

	public void scroll(int position, float offset)
	{
		/**
		 * <pre>
		 *  0-1:position=0 ;1-0:postion=0;
		 * </pre>
		 */
		mTranslationX = getWidth() / mTabCount * (position + offset);
		invalidate();
	}

	private int selectedPosition = 0;

	public void changeTitleSelectedState(int position)
	{
		selectedPosition = position;
		for (int i = 0; i < titleViews.length; i++)
		{
			if (i == selectedPosition)
			{
				titleViews[i].setTextColor(selectedTextColor);
			}
			else
			{
				titleViews[i].setTextColor(unSelectedTextColor);
			}
		}
	}

	@Override
	public boolean dispatchTouchEvent(MotionEvent ev)
	{
		return super.dispatchTouchEvent(ev);
	}

	private void generateTitleView()
	{
		if (getChildCount() > 0)
			this.removeAllViews();
		int count = titleStringArray.length;

		setWeightSum(count);
		for (int i = 0; i < count; i++)
		{

			/*if (i != 0)
			{
				View divider = new View(getContext());
				LinearLayout.LayoutParams dividerLp = new LinearLayout.LayoutParams(
						(int) (2*//* * ResizeUtil.resizeFactor *//*), LayoutParams.MATCH_PARENT);
				final int MARGIN = (int) (10 * ResizeUtil.getResizeFactor());
				dividerLp.setMargins(0, MARGIN, 0, MARGIN);
				divider.setLayoutParams(dividerLp);
				divider.setBackgroundColor(dividerColor);
				addView(divider);
			}*/

			DrawableCenterTextView tv = new DrawableCenterTextView(getContext());
			LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, LayoutParams.MATCH_PARENT);
			lp.weight = 1;
			tv.setGravity(Gravity.CENTER);
			if (i == selectedPosition)
			{
				tv.setTextColor(selectedTextColor);
			}
			else
			{
				tv.setTextColor(unSelectedTextColor);
			}
			tv.setText(titleStringArray[i]);
			tv.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize);
			tv.setLayoutParams(lp);
			final int position = i;
			tv.setOnClickListener(new OnClickListener()
			{
				@Override
				public void onClick(View v)
				{
					mViewPager.setCurrentItem(position);
				}
			});
			addView(tv);

			titleViews[i] = tv;

		}
	}

	public void setTitleImage(int index, int res)
	{
		if (index >= 0 && index < titleViews.length)
		{
			((DrawableCenterTextView) titleViews[index]).setImageInView(DrawableCenterTextView.RIGHT, res);
		}
	}
}
