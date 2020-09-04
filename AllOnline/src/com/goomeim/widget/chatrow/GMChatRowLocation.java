package com.goomeim.widget.chatrow;

import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.coomix.app.all.R;
import com.coomix.app.all.ui.im.EmLocationActivity;

import net.goome.im.chat.GMClient;
import net.goome.im.chat.GMConstant.ConversationType;
import net.goome.im.chat.GMConstant.MsgDirection;
import net.goome.im.chat.GMLocationMessageBody;
import net.goome.im.chat.GMMessage;
import net.goome.im.exceptions.GMException;
import net.goome.im.util.LatLng;

public class GMChatRowLocation extends GMChatRow
{

    private TextView locationView;
    private GMLocationMessageBody locBody;

    public GMChatRowLocation(Context context, GMMessage message, int position, BaseAdapter adapter)
    {
        super(context, message, position, adapter);
    }

    @Override
    protected View onInflatView()
    {
        return inflater.inflate(message.getDirection() == MsgDirection.RECEIVE ? R.layout.ease_row_received_location
                                                                                   : R.layout.ease_row_sent_location,
            this);
    }

    @Override
    protected void onFindViewById()
    {
        locationView = (TextView) mainView.findViewById(R.id.tv_location);
    }

    @Override
    protected void onSetUpView()
    {
        locBody = (GMLocationMessageBody) message.getMsgBody();
        locationView.setText(locBody.getAddress());

        // handle sending message
        if (message.getDirection() == MsgDirection.SEND)
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
            if (!message.isReadAcked() && message.getChatType() == ConversationType.CHAT)
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
        Intent intent = new Intent(context, EmLocationActivity.class);
        intent.putExtra("latitude", locBody.getLatitude());
        intent.putExtra("longitude", locBody.getLongitude());
        intent.putExtra("textAddress", locBody.getAddress());
        activity.startActivity(intent);
    }

    /*
     * listener for map clicked
     */
    protected class MapClickListener implements View.OnClickListener
    {
        LatLng location;
        String address;

        public MapClickListener(LatLng loc, String address)
        {
            location = loc;
            this.address = address;

        }

        @Override
        public void onClick(View v)
        {

        }
    }

}
