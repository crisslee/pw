package com.goomeim.widget.chatrow;

import android.content.Context;
import android.graphics.Color;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.BaseAdapter;
import android.widget.TextView;
import android.widget.TextView.BufferType;

import com.coomix.app.all.R;
import com.coomix.app.framework.util.CommonUtil;
import com.coomix.app.all.util.CommunityUtil;
import com.goomeim.GMAppConstant;
import com.goomeim.utils.GMCommonUtils;
import com.goomeim.utils.GMSmileUtils;

import net.goome.im.chat.GMClient;
import net.goome.im.chat.GMConstant;
import net.goome.im.chat.GMMessage;
import net.goome.im.chat.GMTextMessageBody;
import net.goome.im.exceptions.GMException;

public class GMChatRowText extends GMChatRow
{

    private TextView contentView;
    protected View msgRlayout;
    protected TextView adminMsgView;

    public GMChatRowText(Context context, GMMessage message, int position, BaseAdapter adapter)
    {
        super(context, message, position, adapter);
    }

	@Override
	protected View onInflatView()
	{
		return inflater.inflate(message.getDirection() == GMConstant.MsgDirection.RECEIVE ? R.layout.gm_row_received_message
				: R.layout.gm_row_sent_message, this);
	}

	@Override
	protected void onFindViewById()
	{
		contentView = (TextView) mainView.findViewById(R.id.tv_chatcontent);
		msgRlayout = mainView.findViewById(R.id.msgRlayout);
		adminMsgView = (TextView) mainView.findViewById(R.id.admin_msg);

        contentView.setOnLongClickListener(new OnLongClickListener()
        {
            @Override
            public boolean onLongClick(View v)
            {
                if (itemClickListener != null)
                {
                    itemClickListener.onBubbleLongClick(message);
                }
                return true;
            }
        });
    }

	@Override
	public void onSetUpView()
	{
		String adminMsg = GMCommonUtils.getAdminMessage(message);
		if (adminMsg != null)
		{
			if (msgRlayout != null)
			{
				msgRlayout.setVisibility(View.GONE);
			}
			if (adminMsgView != null)
			{
				adminMsgView.setVisibility(View.VISIBLE);
				if(GMCommonUtils.isCMDRedpacketMessage(message))
				{
					//红包的领取提示消息
					String rpId = GMCommonUtils.getMessageExtString(message, GMAppConstant.REDPACKET_KEY_UNPACK_ID, "");
					SpannableStringBuilder cmdRPMsg = GMCommonUtils.matcherSearchText(getContext(), Color.RED,adminMsg,getContext().getString(R.string.redpacket),rpId);
					CommunityUtil.setTextDrawable(getContext(), adminMsgView, R.drawable.rp_red_icon_small, cmdRPMsg, CommonUtil.DRAWABLE_LEFT);
					//CommonUtil.setTextDrawable(getContext(), adminMsgView, R.drawable.rp_red_icon_small, adminMsg, CommonUtil.DRAWABLE_LEFT);
				}
				else
				{
					//其他提示型信息
					CommonUtil.setTextDrawable(getContext(), adminMsgView, 0, adminMsg, CommonUtil.DRAWABLE_NONE);
				}
			}
		}
		else
		{
			//透传消息
			if(GMCommonUtils.isCMDToTextMessage(message))
			{
				//4.10.1开始不再使用，再过几个版本可以删除
				String cmdMsg = "";
				if (GMCommonUtils.isRevokeCMDToTextMessage(message))
				{
					//消息撤回的显示
					cmdMsg = GMCommonUtils.getCmdRevokeMessage(getContext(), message);
					if (msgRlayout != null)
					{
						msgRlayout.setVisibility(View.GONE);
					}
					if (adminMsgView != null)
					{
						adminMsgView.setVisibility(View.VISIBLE);
						CommonUtil.setTextDrawable(getContext(), adminMsgView, 0, cmdMsg, CommonUtil.DRAWABLE_NONE);
						adminMsgView.setText(cmdMsg);
					}
				}
				else
				{
					cmdMsg = GMCommonUtils.getCmdRedPaketMessage(getContext(), message);
					if (!TextUtils.isEmpty(cmdMsg))
					{
						//领取红包的消息显示
						if (msgRlayout != null)
						{
							msgRlayout.setVisibility(View.GONE);
						}
						if (adminMsgView != null)
						{
							adminMsgView.setVisibility(View.VISIBLE);
							CommonUtil.setTextDrawable(getContext(), adminMsgView, R.drawable.rp_red_icon_small, cmdMsg, CommonUtil.DRAWABLE_LEFT);
						}
					}
				}
			}
			else
			{
				if (msgRlayout != null)
				{
					msgRlayout.setVisibility(View.VISIBLE);
				}
				if (adminMsgView != null)
				{
					adminMsgView.setVisibility(View.GONE);
				}
				// 设置内容,普通的文字消息
				GMTextMessageBody txtBody = (GMTextMessageBody) message.getMsgBody();
				//Spannable span = GMSmileUtils.getSmiledText(context, txtBody.getMessage());
				//contentView.setText(span, BufferType.SPANNABLE);

                String text = txtBody.getMessage();
                contentView.setText(text);
                Spannable span = GMSmileUtils.getSmiledText(context, message, contentView.getText(), itemClickListener);
                contentView.setText(span, BufferType.SPANNABLE);
                contentView.setMovementMethod(LinkMovementMethod.getInstance());
            }
        }
        handleTextMessage();
    }

	protected void handleTextMessage()
	{
		if (message.getDirection() == GMConstant.MsgDirection.SEND)
		{
			switch (message.getStatus())
			{
				case PENDING://未发送
					if(progressBar != null)
					{
						progressBar.setVisibility(View.VISIBLE);
					}
					if(statusView != null)
					{
						statusView.setVisibility(View.GONE);
					}
					break;

				case SUCCESSED://发送成功
					if(progressBar != null)
					{
						progressBar.setVisibility(View.GONE);
					}
					if(statusView != null)
					{
						statusView.setVisibility(View.GONE);
					}
					break;

				case FAILED://发送失败
					if(progressBar != null)
					{
						progressBar.setVisibility(View.GONE);
					}
					if(statusView != null)
					{
						statusView.setVisibility(View.VISIBLE);
					}
					break;

				case DELIVERING://发送中
					if(progressBar != null)
					{
						progressBar.setVisibility(View.VISIBLE);
					}
					if(statusView != null)
					{
						statusView.setVisibility(View.GONE);
					}
					break;

				default:
					if(progressBar != null)
					{
						progressBar.setVisibility(View.GONE);
					}
					if(statusView != null)
					{
						statusView.setVisibility(View.GONE);
					}
					break;
			}
		}
		else
		{
			if (!message.isReadAcked() && message.getChatType() == GMConstant.ConversationType.CHAT)
			{
				try
				{
					GMClient.getInstance().chatManager().ackMessageRead(message);
				}
				catch (GMException e)
				{
					e.printStackTrace();
				}
			}
		}
	}

	@Override
	protected void onUpdateView()
	{
		adapter.notifyDataSetChanged();
	}

    @Override
    protected void onBubbleClick()
    {
        // TODO Auto-generated method stub

    }

}
