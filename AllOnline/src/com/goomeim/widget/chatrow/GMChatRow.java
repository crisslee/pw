package com.goomeim.widget.chatrow;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.coomix.app.all.R;
import com.goomeim.adapter.GMMessageAdapter;
import com.goomeim.controller.MessageListItemClickListener;
import com.goomeim.utils.GMCommonUtils;
import com.goomeim.utils.GMDateUtil;
import com.goomeim.utils.GMUserUtil;

import net.goome.im.chat.GMClient;
import net.goome.im.chat.GMConstant;
import net.goome.im.chat.GMMessage;

public abstract class GMChatRow extends LinearLayout
{
    protected static final String TAG = GMChatRow.class.getSimpleName();

    protected LayoutInflater inflater;
    protected View mainView;
    protected Context context;
    protected BaseAdapter adapter;
    protected GMMessage message;
    protected int position;

    private CheckBox checkBox;

    protected TextView timeStampView;
    protected ImageView userAvatarView;
    protected ImageView userAvatarManagerHintView;
    protected View bubbleLayout;
    protected TextView usernickView;

    protected TextView percentageView;
    protected ProgressBar progressBar;
    protected ImageView statusView;
    protected Activity activity;

    protected TextView ackedView;
    protected TextView deliveredView;
    private long iChatOwnerId = 0;

    protected MessageListItemClickListener itemClickListener;

    public GMChatRow(Context context, GMMessage message, int position, BaseAdapter adapter)
    {
        super(context);
        this.context = context;
        this.activity = (Activity) context;
        this.message = message;
        this.position = position;
        this.adapter = adapter;
        inflater = LayoutInflater.from(context);

        initView();
    }

    private void initView()
    {
        mainView = onInflatView();
        checkBox = (CheckBox) mainView.findViewById(R.id.checkBox);
        timeStampView = (TextView) mainView.findViewById(R.id.timestamp);
        userAvatarView = (ImageView) mainView.findViewById(R.id.iv_userhead);
        userAvatarManagerHintView = (ImageView) mainView.findViewById(R.id.iv_userhead_groupmanager_hint);
        bubbleLayout = mainView.findViewById(R.id.bubble);
        usernickView = (TextView) mainView.findViewById(R.id.tv_userid);

        progressBar = (ProgressBar) mainView.findViewById(R.id.progress_bar);
        statusView = (ImageView) mainView.findViewById(R.id.msg_status);
        ackedView = (TextView) mainView.findViewById(R.id.tv_ack);
        deliveredView = (TextView) mainView.findViewById(R.id.tv_delivered);

        onFindViewById();
    }

    /**
     * set property according message and postion
     *
     * @param message
     * @param position
     */
    public void setUpView(GMMessage message, int position, long iChatOwnerId, MessageListItemClickListener itemClickListener)
    {
        this.message = message;
        this.position = position;
        this.itemClickListener = itemClickListener;

        setUpBaseView(iChatOwnerId);
        onSetUpView();
        setClickListener();
    }

    private void setUpBaseView(long iChatOwnerId)
    {
        // set nickname, avatar and background of bubble
        TextView timeStampView = (TextView) findViewById(R.id.timestamp);
        if (timeStampView != null)
        {
            if (position == 0)
            {
                timeStampView.setText(GMDateUtil.formatMsgTimeMillis(message.getTimestamp()));
                timeStampView.setVisibility(View.VISIBLE);
            }
            else
            {
                //录音设备，每隔一分钟显示一次时间戳
                if(message.getChatType() == GMConstant.ConversationType.CAROLVOICEROOM)
                {
                    GMMessage prevMessage = (GMMessage) adapter.getItem(position - 1);
                    if (prevMessage != null && isInsideOneMinute(message.getTimestamp(), prevMessage.getTimestamp())) {
                        timeStampView.setVisibility(View.GONE);
                    } else {
                        timeStampView.setText(GMDateUtil.formatMsgTimeMillis(message.getTimestamp()));
                        timeStampView.setVisibility(View.VISIBLE);
                    }
                }
                else
                {
                    // show time stamp if interval with last message is > 30 seconds
                    // 30秒内，DateUtils.isCloseEnough(message.getMsgTime(), prevMessage.getMsgTime())
                    // 改为2min内 isCloseEnough(message.getMsgTime(), prevMessage.getMsgTime())
                    GMMessage prevMessage = (GMMessage) adapter.getItem(position - 1);
                    if (prevMessage != null && isCloseEnough(message.getTimestamp(), prevMessage.getTimestamp())) {
                        timeStampView.setVisibility(View.GONE);
                    } else {
                        timeStampView.setText(GMDateUtil.formatMsgTimeMillis(message.getTimestamp()));
                        timeStampView.setVisibility(View.VISIBLE);
                    }
                }
            }
        }

        //设置群主标识
        if(message != null && (message.getChatType() == GMConstant.ConversationType.CHATROOM
                || message.getChatType() == GMConstant.ConversationType.CLASSICROOM))
        {
            //只有群聊，聊天室才会有群主标识
            if(iChatOwnerId == message.getFrom())
            {
                userAvatarManagerHintView.setVisibility(View.VISIBLE);
            }
            else
            {
                userAvatarManagerHintView.setVisibility(View.GONE);
            }
        }
        else
        {
            userAvatarManagerHintView.setVisibility(View.GONE);
        }
        // set nickname and avatar
        GMUserUtil.setUserInfo(context, usernickView, userAvatarView,
            getContext().getResources().getDimensionPixelOffset(R.dimen.space_7x), message);

        if (deliveredView != null)
        {
            if (message.isDeliverAcked())
            {
                deliveredView.setVisibility(View.VISIBLE);
            }
            else
            {
                deliveredView.setVisibility(View.INVISIBLE);
            }
        }

        if (ackedView != null)
        {
            if (message.isReadAcked())
            {
                if (deliveredView != null)
                {
                    deliveredView.setVisibility(View.INVISIBLE);
                }
                ackedView.setVisibility(View.VISIBLE);
            }
            else
            {
                ackedView.setVisibility(View.INVISIBLE);
            }
        }

        if (adapter instanceof GMMessageAdapter)
        {
            if (((GMMessageAdapter) adapter).isShowAvatar())
            {
                userAvatarView.setVisibility(View.VISIBLE);
            }
            else
            {
                userAvatarView.setVisibility(View.GONE);
            }

            if (usernickView != null)
            {
                if (((GMMessageAdapter) adapter).isShowUserNick())
                {
                    usernickView.setVisibility(View.VISIBLE);
                }
                else
                {
                    usernickView.setVisibility(View.GONE);
                }
            }
            if (message.getDirection() == GMConstant.MsgDirection.SEND)
            {
                if (((GMMessageAdapter) adapter).getMyBubbleBg() != null)
                {
                    bubbleLayout.setBackgroundDrawable(((GMMessageAdapter) adapter).getMyBubbleBg());
                }
            }
            else if (message.getDirection() == GMConstant.MsgDirection.RECEIVE)
            {
                if (((GMMessageAdapter) adapter).getOtherBuddleBg() != null)
                {
                    bubbleLayout.setBackgroundDrawable(((GMMessageAdapter) adapter).getOtherBuddleBg());
                }
            }
        }
    }

    /**
     * 判断是否在2min以内
     *
     * @param var0
     * @param var2
     * @return
     */
    public boolean isCloseEnough(long var0, long var2)
    {
        long var4 = var0 - var2;
        if (var4 < 0L)
        {
            var4 = -var4;
        }

        return var4 < 2 * 60 * 1000L;
    }

    /**
     * 判断是否在1min以内
     *
     * @param var0
     * @param var2
     * @return
     */
    public boolean isInsideOneMinute(long var0, long var2)
    {
        long l1 = var0 / (60 * 1000L);
        long l2 = var2 / (60 * 1000L);

        return l1 == l2;
    }

    /**
     * set callback for sending message
     */
    //    protected void setMessageSendCallback()
    //    {
    //        if (messageSendCallback == null)
    //        {
    //            messageSendCallback = new GMCallBack()
    //            {
    //                @Override
    //                public void onSuccess()
    //                {
    //                    updateView();
    //                }
    //
    //                @Override
    //                public void onProgress(final int progress, String status)
    //                {
    //                    activity.runOnUiThread(new Runnable()
    //                    {
    //                        @Override
    //                        public void run()
    //                        {
    //                            if (percentageView != null)
    //                            {
    //                                percentageView.setText(progress + "%");
    //                            }
    //                        }
    //                    });
    //                }
    //
    //                @Override
    //                public void onError(int code)
    //                {
    //                    updateView();
    //                }
    //            };
    //        }
    //        message.setStatusCallback(messageSendCallback);
    //    }
    public boolean isChecked()
    {
        return checkBox != null && checkBox.isChecked();
    }

    public void setCheckBoxVisiable(boolean isVisiable)
    {
        if (GMCommonUtils.isRedPacketMessage(message))
        {
            //红包不能转发
            return;
        }
        if (checkBox != null)
        {
            checkBox.setVisibility(isVisiable ? View.VISIBLE : View.GONE);
        }
    }

    public void setChecked(boolean isChecked)
    {
        if (checkBox != null)
        {
            checkBox.setChecked(isChecked);
        }
    }

    public void setOnCheckedChangeListener(OnCheckedChangeListener listener)
    {
        if (checkBox != null)
        {
            checkBox.setOnCheckedChangeListener(listener);
        }
    }

    /**
     * set callback for receiving message
     */
    //    protected void setMessageReceiveCallback()
    //    {
    //        if (messageReceiveCallback == null)
    //        {
    //            messageReceiveCallback = new GMCallBack()
    //            {
    //                @Override
    //                public void onSuccess()
    //                {
    //                    updateView();
    //                }
    //
    //                @Override
    //                public void onProgress(final int progress, String status)
    //                {
    //                    activity.runOnUiThread(new Runnable()
    //                    {
    //                        public void run()
    //                        {
    //                            if (percentageView != null)
    //                            {
    //                                percentageView.setText(progress + "%");
    //                            }
    //                        }
    //                    });
    //                }
    //
    //                @Override
    //                public void onError(int code)
    //                {
    //                    updateView();
    //                }
    //            };
    //        }
    //        message.setStatusCallback(messageReceiveCallback);
    //    }
    private void setClickListener()
    {
        if (bubbleLayout != null)
        {
            bubbleLayout.setOnClickListener(new OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    if (itemClickListener != null)
                    {
                        if (!itemClickListener.onBubbleClick(message))
                        {
                            // if listener return false, we call default
                            // handling
                            onBubbleClick();
                        }
                    }
                }
            });

            bubbleLayout.setOnLongClickListener(new OnLongClickListener()
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

        if (statusView != null)
        {
            statusView.setOnClickListener(new OnClickListener()
            {

                @Override
                public void onClick(View v)
                {
                    if (itemClickListener != null)
                    {
                        itemClickListener.onResendClick(message);
                    }
                }
            });
        }

        if (userAvatarView != null)
        {
            userAvatarView.setOnClickListener(new OnClickListener()
            {

                @Override
                public void onClick(View v)
                {
                    if (itemClickListener != null)
                    {
                        if (message.getDirection() == GMConstant.MsgDirection.SEND)
                        {
                            itemClickListener.onUserAvatarClick(
                                String.valueOf(GMClient.getInstance().getCurrentUserId()));
                        }
                        else
                        {
                            itemClickListener.onUserAvatarClick(String.valueOf(message.getFrom()));
                        }
                    }
                }
            });
            userAvatarView.setOnLongClickListener(new OnLongClickListener()
            {
                @Override
                public boolean onLongClick(View v)
                {
                    if (itemClickListener != null)
                    {
                        if (message.getDirection() == GMConstant.MsgDirection.SEND)
                        {
                            return false;
                        }
                        else
                        {
                            itemClickListener.onUserAvatarLongClick(message, message.getFrom());
                        }
                        return true;
                    }
                    return false;
                }
            });
        }
    }

    protected void updateView()
    {
        activity.runOnUiThread(new Runnable()
        {
            public void run()
            {
                if (message.getStatus() == GMConstant.MsgStatus.FAILED)
                {
                    Toast.makeText(activity, activity.getString(R.string.send_fail), Toast.LENGTH_SHORT).show();
                    //                    if (message.getError() == EMError.MESSAGE_INCLUDE_ILLEGAL_CONTENT)
                    //                    {
                    //                        Toast.makeText(activity, activity.getString(R.string.send_fail) +
                    // activity.getString(R.string.error_send_invalid_content), Toast.LENGTH_SHORT).show();
                    //                    }
                    //                    else if (message.getError() == EMError.GROUP_NOT_JOINED)
                    //                    {
                    //                        Toast.makeText(activity, activity.getString(R.string.send_fail) +
                    // activity.getString(R.string.error_send_not_in_the_group), Toast.LENGTH_SHORT).show();
                    //                    }
                    //                    else
                    //                    {
                    //                        Toast.makeText(activity, activity.getString(R.string.send_fail) + activity.getString(R.string.connect_failuer_toast), Toast.LENGTH_SHORT).show();
                    //                    }
                }

                onUpdateView();
            }
        });

    }

    protected abstract View onInflatView();

    /**
     * find rootView by id
     */
    protected abstract void onFindViewById();

    /**
     * refresh list rootView when message status change
     */
    protected abstract void onUpdateView();

    /**
     * setup rootView
     */
    protected abstract void onSetUpView();

    /**
     * on bubble clicked
     */
    protected abstract void onBubbleClick();

}
