package com.goomeim.widget.chatrow;

import android.content.Context;
import android.text.Spannable;
import android.view.View;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView.BufferType;

import com.coomix.app.all.R;
import com.coomix.app.redpacket.util.RedPacketConstant;
import com.coomix.app.redpacket.widget.RedPacketBaseView;
import com.coomix.app.redpacket.widget.RedPacketNormalView;
import com.goomeim.GMAppConstant;
import com.goomeim.utils.GMCommonUtils;
import com.goomeim.utils.GMSmileUtils;

import net.goome.im.chat.GMClient;
import net.goome.im.chat.GMConstant;
import net.goome.im.chat.GMMessage;
import net.goome.im.exceptions.GMException;

public class GMChatRowRedPacket extends GMChatRow
{
    protected View msgRlayout;
    private RedPacketBaseView redPacketBaseView;

    public GMChatRowRedPacket(Context context, GMMessage message, int position, BaseAdapter adapter)
    {
        super(context, message, position, adapter);
    }

	@Override
	protected View onInflatView()
	{
		return inflater.inflate(message.getDirection() == GMConstant.MsgDirection.RECEIVE ? R.layout.ease_row_received_redpacket
				: R.layout.ease_row_sent_redpacket, this);
	}

	@Override
	protected void onFindViewById()
	{
		msgRlayout = mainView.findViewById(R.id.msgRlayout);
		LinearLayout bubbleLayout = (LinearLayout) mainView.findViewById(R.id.bubble);

        switch (GMCommonUtils.getRedPacketType(message))
        {
            case RedPacketConstant.REDPACKET_TYPE_NORMAL:
            default:
                redPacketBaseView = new RedPacketNormalView(getContext(),
                    GMCommonUtils.getMessageExtString(message, GMAppConstant.REDPACKET_KEY_ID, "0"));
                break;
        }

		redPacketBaseView.setOnLongClickListener(new OnLongClickListener()
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

        bubbleLayout.addView(redPacketBaseView);
    }

	@Override
	public void onSetUpView()
	{
		Spannable span = GMSmileUtils.getSmiledText(context, GMCommonUtils.getRedPacketWelcomeText(getContext(), message, false), itemClickListener);
		redPacketBaseView.setRedPacketId(GMCommonUtils.getMessageExtString(message, GMAppConstant.REDPACKET_KEY_ID, "0"));
		redPacketBaseView.setRedPacketTitle(span, BufferType.SPANNABLE);
		if(message.getChatType() == GMConstant.ConversationType.CHAT)
		{
			if(message.getDirection() == GMConstant.MsgDirection.RECEIVE)
			{
				redPacketBaseView.setToChatId(message.getFrom());//发出红包人的id--相当于会话ID
			}
			else
			{
				redPacketBaseView.setToChatId(message.getTo());//发出红包人的id--相当于会话ID
			}
		}
		else
		{
			redPacketBaseView.setToChatId(Long.parseLong(message.getConversationId()));//会话id
		}

		if(message.getStatus() == GMConstant.MsgStatus.SUCCESSED)
		{
			redPacketBaseView.setRedPacketSended(true);
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
        //去抢红包，已经在RedPacketBaseView中处理了
    }

}
