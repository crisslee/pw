package com.goomeim.widget.chatrow;

import android.content.Context;
import android.view.View;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.GlideApp;
import com.coomix.app.all.R;
import com.goomeim.GMAppConstant;
import com.goomeim.controller.GMImManager;
import com.goomeim.domain.GMEmojicon;
import com.goomeim.utils.GMCommonUtils;

import net.goome.im.chat.GMConstant;
import net.goome.im.chat.GMMessage;

/**
 * big emoji icons
 */
public class GMChatRowBigExpression extends GMChatRowText
{

    private ImageView imageView;

	public GMChatRowBigExpression(Context context, GMMessage message, int position, BaseAdapter adapter)
	{
		super(context, message, position, adapter);
	}

	@Override
	protected View onInflatView()
	{
		return inflater.inflate(message.getDirection() == GMConstant.MsgDirection.RECEIVE ? R.layout.ease_row_received_bigexpression
				: R.layout.ease_row_sent_bigexpression, this);
	}

	@Override
	protected void onFindViewById()
	{
		percentageView = (TextView) mainView.findViewById(R.id.percentage);
		imageView = (ImageView) mainView.findViewById(R.id.image);
	}

    @Override
    public void onSetUpView()
    {
        String emojiconId = GMCommonUtils.getMessageExtString(message, GMAppConstant.MESSAGE_ATTR_EXPRESSION_ID, null);
        GMEmojicon emojicon = null;
        if (GMImManager.getInstance().getEmojiconInfoProvider() != null)
        {
            emojicon = GMImManager.getInstance().getEmojiconInfoProvider().getEmojiconInfo(emojiconId);
        }
        if (emojicon != null)
        {
            if (emojicon.getBigIcon() != 0)
            {
                GlideApp.with(activity).load(emojicon.getBigIcon()).placeholder(R.drawable.ease_default_expression)
                    .into(imageView);
            }
            else if (emojicon.getBigIconPath() != null)
            {
                GlideApp.with(activity).load(emojicon.getBigIconPath()).placeholder(R.drawable.ease_default_expression)
                    .into(imageView);
            }
            else
            {
                imageView.setImageResource(R.drawable.ease_default_expression);
            }
        }

        handleTextMessage();
    }
}
