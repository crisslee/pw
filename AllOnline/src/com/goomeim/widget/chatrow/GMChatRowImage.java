package com.goomeim.widget.chatrow;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.view.View;
import android.widget.BaseAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.bumptech.GlideApp;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.coomix.app.all.AllOnlineApp;
import com.coomix.app.all.R;
import com.coomix.app.all.util.ViewUtil;
import com.goomeim.widget.ChatRoundRectImageView;
import net.goome.im.chat.GMClient;
import net.goome.im.chat.GMConstant;
import net.goome.im.chat.GMImageMessageBody;
import net.goome.im.chat.GMMessage;

public class GMChatRowImage extends GMChatRowFile {
    protected ChatRoundRectImageView imageView;
    private GMImageMessageBody imgBody;

    public GMChatRowImage(Context context, GMMessage message, int position, BaseAdapter adapter) {
        super(context, message, position, adapter);
    }

    @Override
    protected View onInflatView() {
        return inflater.inflate(message.getDirection() == GMConstant.MsgDirection.RECEIVE ?
            R.layout.gm_row_received_picture : R.layout.gm_row_sent_picture, this);
    }

    @Override
    protected void onFindViewById() {
        percentageView = (TextView) mainView.findViewById(R.id.percentage);
        imageView = (ChatRoundRectImageView) mainView.findViewById(R.id.image);
        imageView.setBack(message.getDirection() == GMConstant.MsgDirection.RECEIVE ? R.drawable.ease_chatfrom_bg_normal
            : R.drawable.ease_chatto_bg_normal);
    }

    @Override
    protected void onSetUpView() {
        imgBody = (GMImageMessageBody) message.getMsgBody();
        // received messages
        int[] wh;
        if (imgBody != null) {
            wh = ViewUtil.getSingleImageWH(getContext(), (int) imgBody.getWidth(), (int) imgBody.getHeight(),
                AllOnlineApp.screenWidth * 5 / 9);
            imageView.setSrcWidthHeight(imgBody.getWidth(), imgBody.getHeight());
            imageView.setLayoutParams(new RelativeLayout.LayoutParams(wh[0], wh[1]));
        } else {
            return;
        }

        imageView.setImageResource(R.drawable.image_default);

        String imagePath = null;
        if (message.getDirection() == GMConstant.MsgDirection.SEND) {
            //发送方本地会有图片文件存储
            imagePath = imgBody.getLocalPath();
            if (TextUtils.isEmpty(imagePath)) {
                imagePath = imgBody.getRemotePath();
            }
        } else {
            imagePath = imgBody.getRemotePath();
        }

        GlideApp.with(getContext()).load(imagePath)
            .placeholder(R.drawable.image_default).centerCrop()
            .error(R.drawable.image_default_error).centerCrop()
            .override(wh[0], wh[1])
            .into(new SimpleTarget<Drawable>() {
                @Override
                public void onResourceReady(Drawable resource, Transition<? super Drawable> transition) {
                    imageView.setImageDrawable(resource);
                }
            });

        if (message.getDirection() == GMConstant.MsgDirection.SEND) {
            handleSendMessage();
        }
    }

    @Override
    protected void onUpdateView() {
        super.onUpdateView();
    }

    @Override
    protected void onBubbleClick() {
        if (message != null && message.getDirection() == GMConstant.MsgDirection.RECEIVE && !message.isReadAcked()
            && message.getChatType() == GMConstant.ConversationType.CHAT) {
            try {
                GMClient.getInstance().chatManager().ackMessageRead(message);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
