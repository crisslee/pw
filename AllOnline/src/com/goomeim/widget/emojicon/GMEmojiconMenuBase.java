package com.goomeim.widget.emojicon;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;

import com.goomeim.domain.GMEmojicon;

public class GMEmojiconMenuBase extends LinearLayout
{
	protected EaseEmojiconMenuListener listener;

	public GMEmojiconMenuBase(Context context)
	{
		super(context);
	}

	@SuppressLint("NewApi")
	public GMEmojiconMenuBase(Context context, AttributeSet attrs, int defStyle)
	{
		super(context, attrs, defStyle);
	}

	public GMEmojiconMenuBase(Context context, AttributeSet attrs)
	{
		super(context, attrs);
	}

	/**
	 * set emojicon menu listener
	 * 
	 * @param listener
	 */
	public void setEmojiconMenuListener(EaseEmojiconMenuListener listener)
	{
		this.listener = listener;
	}

	public interface EaseEmojiconMenuListener
	{
		/**
		 * on emojicon clicked
		 * 
		 * @param emojicon
		 */
		void onExpressionClicked(GMEmojicon emojicon);

		/**
		 * on delete image clicked
		 */
		void onDeleteImageClicked();
	}
}
