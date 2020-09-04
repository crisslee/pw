/**
 * Copyright (C) 2016 Hyphenate Inc. All rights reserved.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.goomeim.widget.chatrow;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.Toast;

import com.coomix.app.all.R;
import com.goomeim.controller.GMImManager;

import net.goome.im.chat.GMClient;
import net.goome.im.chat.GMConstant;
import net.goome.im.chat.GMMessage;
import net.goome.im.chat.GMVoiceMessageBody;

import java.io.File;

/**
 * 语音row播放点击事件监听
 *
 */
public class GMChatRowVoicePlayClickListener implements View.OnClickListener
{
    private static final String TAG = "VoicePlayClickListener";
    GMMessage message;
    GMVoiceMessageBody voiceBody;
    ImageView voiceIconView;

    private AnimationDrawable voiceAnimation = null;
    MediaPlayer mediaPlayer = null;
    ImageView iv_read_status;
    Activity activity;
    private GMConstant.ConversationType chatType;
    private BaseAdapter adapter;

    public static boolean isPlaying = false;
    public static GMChatRowVoicePlayClickListener currentPlayListener = null;
    public static String playMsgId;

    public GMChatRowVoicePlayClickListener(GMMessage message, ImageView v, ImageView iv_read_status,
                                           BaseAdapter adapter, Activity context)
    {
        this.message = message;
        voiceBody = (GMVoiceMessageBody) message.getMsgBody();
        this.iv_read_status = iv_read_status;
        this.adapter = adapter;
        voiceIconView = v;
        this.activity = context;
        this.chatType = message.getChatType();
    }

    public void stopPlayVoice()
    {
        voiceAnimation.stop();
        if (message.getDirection() == GMConstant.MsgDirection.RECEIVE)
        {
            voiceIconView.setImageResource(R.drawable.ease_chatfrom_voice_playing);
        }
        else
        {
            voiceIconView.setImageResource(R.drawable.ease_chatto_voice_playing);
        }
        // stop play voice
        if (mediaPlayer != null)
        {
            mediaPlayer.stop();
            mediaPlayer.release();
        }
        isPlaying = false;
        playMsgId = null;
        adapter.notifyDataSetChanged();
    }

    public void playVoice(String filePath)
    {
        if (!(new File(filePath).exists()))
        {
            Toast.makeText(activity, "该语音已失效", Toast.LENGTH_SHORT).show();
            return;
        }
        playMsgId = String.valueOf(message.getMsgId());
        AudioManager audioManager = (AudioManager) activity.getSystemService(Context.AUDIO_SERVICE);

        mediaPlayer = new MediaPlayer();
        if (GMImManager.getInstance().getSettingsProvider().isSpeakerOpened())
        {
            audioManager.setMode(AudioManager.MODE_NORMAL);
            audioManager.setSpeakerphoneOn(true);
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_RING);
        }
        else
        {
            audioManager.setSpeakerphoneOn(false);// 关闭扬声器
            // 把声音设定成Earpiece（听筒）出来，设定为正在通话中
            audioManager.setMode(AudioManager.MODE_IN_CALL);
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_VOICE_CALL);
        }
        try
        {
            mediaPlayer.setDataSource(filePath);
            mediaPlayer.prepare();
            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener()
            {

                @Override
                public void onCompletion(MediaPlayer mp)
                {
                    // TODO Auto-generated method stub
                    mediaPlayer.release();
                    mediaPlayer = null;
                    stopPlayVoice(); // stop animation
                }

            });
            isPlaying = true;
            currentPlayListener = this;
            mediaPlayer.start();
            showAnimation();

            // 如果是接收的消息
            if (message.getDirection() == GMConstant.MsgDirection.RECEIVE)
            {
                if (!message.isReadAcked() && chatType == GMConstant.ConversationType.CHAT)
                {
                    // 告知对方已读这条消息
                    GMClient.getInstance().chatManager().ackMessageRead(message);
                }
                //				if (!message.isListened() && iv_read_status != null && iv_read_status.getVisibility()
                // == View.VISIBLE)
                //				{
                //					// 隐藏自己未播放这条语音消息的标志
                //					iv_read_status.setVisibility(View.INVISIBLE);
                //					message.setListened(true);
                //					GMClient.getInstance().chatManager().setMessageListened(message);
                //				}

            }

        }
        catch (Exception e)
        {
            System.out.println();
        }
    }

    // show the voice playing animation
    private void showAnimation()
    {
        // play voice, and start animation
        if (message.getDirection() == GMConstant.MsgDirection.RECEIVE)
        {
            voiceIconView.setImageResource(R.drawable.voice_from_icon);
        }
        else
        {
            voiceIconView.setImageResource(R.drawable.voice_to_icon);
        }
        voiceAnimation = (AnimationDrawable) voiceIconView.getDrawable();
        voiceAnimation.start();
    }

    @Override
    public void onClick(View v)
    {
        String st = activity.getResources().getString(R.string.Is_download_voice_click_later);
        if (isPlaying)
        {
            if (playMsgId != null && playMsgId.equals(message.getMsgId()))
            {
                currentPlayListener.stopPlayVoice();
                return;
            }
            currentPlayListener.stopPlayVoice();
        }

        if (message.getDirection() == GMConstant.MsgDirection.SEND)
        {
            // for sent msg, we will try to play the voice file directly
            playVoice(voiceBody.getLocalPath());
        }
        else
        {
            if (message.getStatus() == GMConstant.MsgStatus.SUCCESSED)
            {
                File file = new File(voiceBody.getLocalPath());
                if (file.exists() && file.isFile())
                {
                    playVoice(voiceBody.getLocalPath());
                }
                else
                {
                    Log.e(TAG, "file not exist");
                }
            }
            else if (message.getStatus() == GMConstant.MsgStatus.DELIVERING)
            {
                Toast.makeText(activity, st, Toast.LENGTH_SHORT).show();
            }
            else if (message.getStatus() == GMConstant.MsgStatus.FAILED)
            {
                Toast.makeText(activity, st, Toast.LENGTH_SHORT).show();
                new AsyncTask<Void, Void, Void>()
                {
                    @Override
                    protected Void doInBackground(Void... params)
                    {
                        //GMClient.getInstance().chatManager().downloadAttachment(message);
                        return null;
                    }

                    @Override
                    protected void onPostExecute(Void result)
                    {
                        super.onPostExecute(result);
                        adapter.notifyDataSetChanged();
                    }
                }.execute();
            }
        }
    }
}
