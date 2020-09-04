package com.goomeim.widget.chatrow;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Build;
import android.widget.BaseAdapter;
import android.widget.Toast;
import com.coomix.app.all.AllOnlineApp;
import com.coomix.app.all.R;
import com.goomeim.GMAppConstant;
import com.goomeim.controller.GMImManager;
import com.goomeim.controller.VoicePlayCompletedListener;
import com.goomeim.controller.VoicePlayStopListener;
import com.goomeim.utils.GMCommonUtils;
import com.goomeim.utils.VoiceMessageUtils;
import java.io.File;
import net.goome.im.chat.GMChatManager;
import net.goome.im.chat.GMClient;
import net.goome.im.chat.GMConstant.ConversationType;
import net.goome.im.chat.GMConstant.MsgDirection;
import net.goome.im.chat.GMConstant.MsgDownloadStatus;
import net.goome.im.chat.GMConversation;
import net.goome.im.chat.GMMessage;
import net.goome.im.chat.GMVoiceMessageBody;

/**
 * File Description:
 *
 * @author felixqiu
 * @since 2018/1/31.
 */

public class GMVoicePlayManager {
    private static final GMVoicePlayManager INSTANCE = new GMVoicePlayManager();

    private static final long INVALID_MSGID = -1L;

    public static boolean isPlaying = false;
    public static long playMsgId = INVALID_MSGID;
    private MediaPlayer mediaPlayer = null;
    private Context appContext;
    private VoicePlayCompletedListener playListener;
    private int currPosition = -1;
    private VoicePlayStopListener stopListener;
    private BaseAdapter adapter;
    private AudioManager audioManager = null;

    private GMVoicePlayManager() {
        appContext = AllOnlineApp.mApp;
        audioManager = (AudioManager) appContext.getSystemService(Context.AUDIO_SERVICE);
    }

    public static GMVoicePlayManager getInstance() {
        return INSTANCE;
    }

    public void playVoice(int position, final GMMessage message, VoicePlayStopListener listener) {
        try {
            if (isPlaying) {
                stopVoice();
            }
            stopListener = listener;
            currPosition = position;
            playMsgId = message.getMsgId();
            isPlaying = true;
            GMVoiceMessageBody body = (GMVoiceMessageBody) message.getMsgBody();
            String path = body.getLocalPath();
            if (message.getDirection() == MsgDirection.RECEIVE) {
                String downloading = appContext.getString(R.string.Is_download_voice_click_later);
                if (body.getDownloadStatus() == MsgDownloadStatus.DOWNLOADING) {
                    Toast.makeText(appContext, downloading, Toast.LENGTH_SHORT).show();
                    return;
                } else if (body.getDownloadStatus() == MsgDownloadStatus.FAILED
                    || body.getDownloadStatus() == MsgDownloadStatus.PENDING) {
                    Toast.makeText(appContext, downloading, Toast.LENGTH_SHORT).show();
                    new AsyncTask<Void, Void, Void>() {
                        @Override
                        protected Void doInBackground(Void... params) {
                            GMClient.getInstance().chatManager().downloadAttachment(message);
                            return null;
                        }

                        @Override
                        protected void onPostExecute(Void result) {
                            super.onPostExecute(result);
                            if (adapter != null) {
                                adapter.notifyDataSetChanged();
                            }
                        }
                    }.execute();
                    return;
                }
            }
            playVoice(path);
            //标记已读
            markVoiceMsgListened(message);
        } catch (Exception e) {
            currPosition = -1;
            playMsgId = INVALID_MSGID;
            isPlaying = false;
            e.printStackTrace();
        }
    }

    private void playVoice(String path) {
        File voiceFile = new File(path);
        if (!voiceFile.exists()) {
            String fileInvalid = appContext.getString(R.string.voice_file_not_exist);
            Toast.makeText(appContext, fileInvalid, Toast.LENGTH_SHORT).show();
            return;
        }
        setVoiceChannel();
        try {
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setDataSource(path);
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_VOICE_CALL);
            mediaPlayer.prepareAsync();
            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    mediaPlayer.release();
                    mediaPlayer = null;
                    isPlaying = false;
                    playMsgId = INVALID_MSGID;
                    if (stopListener != null) {
                        stopListener.onStop();
                        stopListener = null;
                    }
                    if (playListener != null) {
                        playListener.onVoicePlayCompleted(currPosition);
                    }
                }
            });
            mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    mediaPlayer.start();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setVoiceChannel() {
        if (isWiredHeadsetOn()) {
            changeToHeadset();
        } else if (GMImManager.getInstance().getSettingsProvider().isSpeakerOpened()) {
            changeToSpeaker();
        } else {
            changeToReceiver();
        }
    }

    public void stopVoice() {
        if (stopListener != null) {
            stopListener.onStop();
            stopListener = null;
        }
        currPosition = -1;
        isPlaying = false;
        playMsgId = INVALID_MSGID;
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    public void setVoicePlayCompletedListener(VoicePlayCompletedListener listener) {
        playListener = listener;
    }

    public void setVoicePlayStopListener(VoicePlayStopListener listener) {
        stopListener = listener;
    }

    public void setAdapter(BaseAdapter adapter) {
        this.adapter = adapter;
    }

    private void markVoiceMsgListened(final GMMessage message) {
        if (GMCommonUtils.getMessageExtInt(message, GMAppConstant.GOOME_VOICE_LISTENED, 0) == 1) {
            return;
        }
        //设置语音已读
        GMCommonUtils.addMessageExtInt(message, GMAppConstant.GOOME_VOICE_LISTENED, 1);
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
        //聊天室的语音消息已读时加入已读列表
        if ((message.getChatType() == ConversationType.CLASSICROOM
            || message.getChatType() == ConversationType.CAROLVOICEROOM)
            && !VoiceMessageUtils.isListened(message)) {
            VoiceMessageUtils.saveListenedMessage(message);
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                GMChatManager chatMgr = GMClient.getInstance().chatManager();
                if (chatMgr != null) {
                    GMConversation conversation = chatMgr.getConversation(message.getConversationId(),
                        message.getChatType(), false);
                    if (conversation != null) {
                        conversation.updateMessage(message);
                    }
                }
            }
        }).start();
    }

    public void updatePlayMode(int mode) {
        if (mode == AudioManager.MODE_NORMAL) {
            changeToSpeaker();
        } else {
            changeToReceiver();
        }
    }

    /**
     * 切换到扬声器
     */
    private void changeToSpeaker() {
        audioManager.setMode(AudioManager.MODE_NORMAL);
        audioManager.setSpeakerphoneOn(true);
    }

    /**
     * 切换到耳机模式
     */
    public void changeToHeadset() {
        audioManager.setSpeakerphoneOn(false);
    }

    /**
     * 切换到听筒
     */
    public void changeToReceiver() {
        audioManager.setSpeakerphoneOn(false);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            audioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);
        } else {
            audioManager.setMode(AudioManager.MODE_IN_CALL);
        }
    }

    public boolean isWiredHeadsetOn() {
        return audioManager.isWiredHeadsetOn();
    }
}
