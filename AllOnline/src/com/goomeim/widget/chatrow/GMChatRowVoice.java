package com.goomeim.widget.chatrow;

import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.view.View;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.coomix.app.all.AllOnlineApp;
import com.coomix.app.all.R;
import com.goomeim.GMAppConstant;
import com.goomeim.controller.VoicePlayStopListener;
import com.goomeim.utils.GMCommonUtils;
import net.goome.im.chat.GMConstant.MsgDirection;
import net.goome.im.chat.GMConstant.MsgDownloadStatus;
import net.goome.im.chat.GMMessage;
import net.goome.im.chat.GMVoiceMessageBody;

public class GMChatRowVoice extends GMChatRowFile {
    private ImageView ivVoice;
    private TextView tvVoiceLength;
    private ImageView ivReadStatus;
    private final int VOICE_LENGTH_PER_SECOND = 7; //px
    private AnimationDrawable voiceAnimation = null;

    public GMChatRowVoice(Context context, GMMessage message, int position, BaseAdapter adapter) {
        super(context, message, position, adapter);
    }

    @Override
    protected View onInflatView() {
        return inflater.inflate(message.getDirection() == MsgDirection.RECEIVE ? R.layout.ease_row_received_voice
            : R.layout.ease_row_sent_voice, this);
    }

    @Override
    protected void onFindViewById() {
        ivVoice = ((ImageView) mainView.findViewById(R.id.iv_voice));
        tvVoiceLength = (TextView) mainView.findViewById(R.id.tv_length);
        ivReadStatus = (ImageView) mainView.findViewById(R.id.iv_unread_voice);
    }

    @Override
    protected void onSetUpView() {
        GMVoiceMessageBody voiceBody = (GMVoiceMessageBody) message.getMsgBody();
        int len = voiceBody.getDuration();
        if (len > 0) {
            tvVoiceLength.setText(voiceBody.getDuration() + "\"");
            tvVoiceLength.setVisibility(View.VISIBLE);
            int baseLength = getResources().getDimensionPixelOffset(R.dimen.chat_voice_length_base);
            baseLength += VOICE_LENGTH_PER_SECOND * (len - 1);
            if (baseLength > AllOnlineApp.screenWidth / 2) {
                baseLength = AllOnlineApp.screenWidth / 2;
            }
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(baseLength,
                RelativeLayout.LayoutParams.WRAP_CONTENT);
            params.addRule(RelativeLayout.BELOW, R.id.tv_userid);
            params.addRule(RelativeLayout.CENTER_VERTICAL, RelativeLayout.TRUE);
            if (message.getDirection() == MsgDirection.RECEIVE) {
                params.leftMargin = getResources().getDimensionPixelOffset(R.dimen.margin_chat_activity);
                params.addRule(RelativeLayout.RIGHT_OF, R.id.iv_userhead);
            } else {
                params.rightMargin = getResources().getDimensionPixelOffset(R.dimen.margin_chat_activity);
                params.addRule(RelativeLayout.LEFT_OF, R.id.iv_userhead);
            }
            bubbleLayout.setLayoutParams(params);
        } else {
            tvVoiceLength.setVisibility(View.INVISIBLE);
        }
        if (isPlayingCurrMsg()) {
            updateListener();
            showAnimation();
        } else {
            stopAnimation();
        }

        if (message.getDirection() == MsgDirection.RECEIVE) {
            int listened = GMCommonUtils.getMessageExtInt(message, GMAppConstant.GOOME_VOICE_LISTENED, 0);
            if (listened == 1) {
                // hide the unread icon
                ivReadStatus.setVisibility(View.INVISIBLE);
            } else {
                ivReadStatus.setVisibility(View.VISIBLE);
            }
            if (voiceBody.getDownloadStatus() == MsgDownloadStatus.DOWNLOADING) {
                progressBar.setVisibility(View.VISIBLE);
                //setMessageReceiveCallback();
            } else {
                progressBar.setVisibility(View.INVISIBLE);
            }
            return;
        }

        // until here, handle sending voice message
        handleSendMessage();
    }

    @Override
    protected void onUpdateView() {
        super.onUpdateView();
    }

    private boolean isPlayingCurrMsg() {
        return GMVoicePlayManager.isPlaying && GMVoicePlayManager.playMsgId == message.getMsgId();
    }

    @Override
    protected void onBubbleClick() {
        //播放动画
        if (voiceAnimation != null && voiceAnimation.isRunning() && isPlayingCurrMsg()) {
            stopAnimation();
            GMVoicePlayManager.getInstance().stopVoice();
        } else {
            showAnimation();
            GMVoicePlayManager.getInstance().playVoice(position, message, stopListener);
        }
    }

    // show the voice playing animation
    private void showAnimation() {
        // play voice, and start animation
        if (message.getDirection() == MsgDirection.RECEIVE) {
            ivVoice.setImageResource(R.drawable.voice_from_icon);
        } else {
            ivVoice.setImageResource(R.drawable.voice_to_icon);
        }
        voiceAnimation = (AnimationDrawable) ivVoice.getDrawable();
        voiceAnimation.start();
    }

    private void stopAnimation() {
        if (voiceAnimation != null) {
            voiceAnimation.stop();
        }
        if (message.getDirection() == MsgDirection.RECEIVE) {
            ivVoice.setImageResource(R.drawable.ease_chatfrom_voice_playing_f3);
        } else {
            ivVoice.setImageResource(R.drawable.ease_chatto_voice_playing);
        }
    }

    private VoicePlayStopListener stopListener = new VoicePlayStopListener() {
        @Override
        public void onStop() {
            stopAnimation();
        }
    };

    private void updateListener() {
        GMVoicePlayManager.getInstance().setVoicePlayStopListener(stopListener);
    }
}