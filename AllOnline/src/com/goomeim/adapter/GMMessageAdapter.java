/**
 * Copyright (C) 2016 Hyphenate Inc. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.goomeim.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ListView;
import android.widget.Toast;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.Target;
import com.coomix.app.all.AllOnlineApp;
import com.coomix.app.all.R;
import com.goomeim.GMAppConstant;
import com.goomeim.controller.MessageListItemClickListener;
import com.goomeim.utils.GMCommonUtils;
import com.goomeim.utils.GMUserUtil;
import com.goomeim.utils.VoiceMessageUtils;
import com.goomeim.widget.chatrow.GMChatRow;
import com.goomeim.widget.chatrow.GMChatRowBigExpression;
import com.goomeim.widget.chatrow.GMChatRowImage;
import com.goomeim.widget.chatrow.GMChatRowLocation;
import com.goomeim.widget.chatrow.GMChatRowRedPacket;
import com.goomeim.widget.chatrow.GMChatRowText;
import com.goomeim.widget.chatrow.GMChatRowVoice;
import java.io.File;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import net.goome.im.chat.GMConstant;
import net.goome.im.chat.GMConstant.ConversationType;
import net.goome.im.chat.GMConstant.MsgDirection;
import net.goome.im.chat.GMConversation;
import net.goome.im.chat.GMImageMessageBody;
import net.goome.im.chat.GMMessage;
import net.goome.im.chat.GMMessageBody;
import net.goome.im.chat.GMTextMessageBody;

public class GMMessageAdapter extends BaseAdapter {
    private final static String TAG = GMMessageAdapter.class.getSimpleName();

    private Context context;

    private static final int HANDLER_MESSAGE_REFRESH_LIST = 0;
    private static final int HANDLER_MESSAGE_SELECT_LAST = 1;
    private static final int HANDLER_MESSAGE_SEEK_TO = 2;
    private static final int UPDATE_MESSAGES_DONE = 3;

    // reference to conversation object in chatsdk
    private GMConversation conversation;
    List<GMMessage> messages = null;

    private String toChatUsername;
    private ConversationType chatType = ConversationType.CHAT;

    private MessageListItemClickListener itemClickListener;

    private boolean showUserNick;
    private boolean showAvatar;
    private Drawable myBubbleBg;
    private Drawable otherBuddleBg;

    private ListView listView;
    /**
     * 用于保存选中的文字消息id
     */
    private LinkedList<Long> txtMsgIdList = new LinkedList<Long>();
    /**
     * 最大可选中的文字消息数
     */
    private static final int TXT_MSG_MAX = 100;
    /**
     * 用于保存选中的图片消息id
     */
    private LinkedList<Long> imgMsgIdList = new LinkedList<Long>();
    /**
     * 最大可选中的图片消息数
     */
    private static final int IMAGE_MSG_MAX = 9;
    /**
     * 用于保存选中的非文字/非图片 消息id
     */
    private LinkedList<Long> otherMsgIdList = new LinkedList<Long>();
    private boolean isVisiable = false;
    private boolean bSelectLast = false;

    private long iChatOwnerId = 0;

    public GMMessageAdapter(Context context, String userId, ConversationType chatType, GMConversation conversation,
        ListView listView) {
        this.context = context;
        this.listView = listView;
        this.toChatUsername = userId;
        this.chatType = chatType;
        this.conversation = conversation;
        updateMessages();
    }

    @SuppressWarnings("HandlerLeak")
    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case HANDLER_MESSAGE_REFRESH_LIST:
                    if (msg.obj != null) {
                        bSelectLast = (boolean) msg.obj;
                    }
                    updateMessages();
                    break;

                case HANDLER_MESSAGE_SELECT_LAST:
                    scrollToBottom();
                    break;

                case HANDLER_MESSAGE_SEEK_TO:
                    listView.setSelection(msg.arg1);
                    break;

                case UPDATE_MESSAGES_DONE:
                    if (msg.obj != null && msg.obj instanceof List) {
                        messages = (List<GMMessage>) msg.obj;
                    }
                    notifyDataSetChanged();
                    if (bSelectLast) {
                        scrollToBottom();
                        bSelectLast = false;
                    }
                    break;

                default:
                    break;
            }
        }
    };

    private void scrollToBottom() {
        if (listView == null) {
            return;
        }
        int iCount = listView.getCount();
        if (iCount > 20) {
            /*这种方法对于长消息可以彻底滑动到底部，但是消息总数不够一屏的时候会导致第一条消息不靠顶部(用setSelection来解决)。
              ---20只是一屏大概的数字*/
            if (listView.isStackFromBottom()) {
                listView.setStackFromBottom(false);
            }
            listView.setStackFromBottom(true);
        } else {
            //这种方法对于长消息无法彻底滑动到底部
            listView.setStackFromBottom(false);
            listView.setSelection(iCount - 1);
        }
    }

    private void updateMessages() {
        if (conversation == null) {
            return;
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                List<GMMessage> messages = conversation.getAllMessages();
                Message msg = handler.obtainMessage();
                msg.obj = messages;
                msg.what = UPDATE_MESSAGES_DONE;
                handler.sendMessage(msg);
                conversation.markAllMessagesAsRead();
            }
        }).start();
    }

    public void setConversation(GMConversation conversation) {
        this.conversation = conversation;
    }

    public void refresh() {
        if (handler.hasMessages(HANDLER_MESSAGE_REFRESH_LIST)) {
            return;
        }
        Message msg = handler.obtainMessage(HANDLER_MESSAGE_REFRESH_LIST);
        handler.sendMessage(msg);
    }

    /**
     * refresh and select the last
     */
    public void refreshSelectLast() {
        handler.removeMessages(HANDLER_MESSAGE_REFRESH_LIST);
        Message msg = handler.obtainMessage(HANDLER_MESSAGE_REFRESH_LIST);
        msg.obj = true;
        handler.sendMessage(msg);
    }

    public void selectLastDelay(int delay) {
        handler.sendEmptyMessageDelayed(HANDLER_MESSAGE_SELECT_LAST, delay);
    }

    public void selectLastNow() {
        handler.sendEmptyMessage(HANDLER_MESSAGE_SELECT_LAST);
    }

    /**
     * refresh and seek to the position
     */
    public void refreshSeekTo(int position) {
        handler.sendMessage(handler.obtainMessage(HANDLER_MESSAGE_REFRESH_LIST));
        Message msg = handler.obtainMessage(HANDLER_MESSAGE_SEEK_TO);
        msg.arg1 = position;
        handler.sendMessage(msg);
    }

    public GMMessage getItem(int position) {
        if (messages != null && position < messages.size()) {
            return messages.get(position);
        }
        return null;
    }

    public long getItemId(int position) {
        return position;
    }

    /**
     * get count of messages
     */
    public int getCount() {
        return messages == null ? 0 : messages.size();
    }

    public GMMessage getLastMessage() {
        if (messages != null && messages.size() > 0) {
            return messages.get(messages.size() - 1);
        }
        return null;
    }

    protected GMChatRow createChatRow(Context context, View convertView, GMMessage message, int position) {
        GMChatRow chatRow = null;
        if (message.getDirection() == MsgDirection.SEND) {
            switch (message.getBodyType()) {
                case TEXT:
                    if (GMCommonUtils.getMessageExtBoolean(message, GMAppConstant.MESSAGE_ATTR_IS_BIG_EXPRESSION,
                        false)) {
                        if (convertView == null || convertView.getTag(R.id.tag_chat_row_big_expression_send) == null) {
                            chatRow = new GMChatRowBigExpression(context, message, position, this);
                            convertView = chatRow;
                            convertView.setTag(R.id.tag_chat_row_big_expression_send, chatRow);
                        } else {
                            chatRow = (GMChatRowBigExpression) convertView.getTag(R.id.tag_chat_row_big_expression_send);
                        }
                    } else {
                        //普通文字信息
                        if (convertView == null || convertView.getTag(R.id.tag_chat_row_txt_send) == null) {
                            chatRow = new GMChatRowText(context, message, position, this);
                            convertView = chatRow;
                            convertView.setTag(R.id.tag_chat_row_txt_send, chatRow);
                        } else {
                            chatRow = (GMChatRowText) convertView.getTag(R.id.tag_chat_row_txt_send);
                        }
                    }
                    break;

                case CUSTOM:
                    if (GMCommonUtils.isRedPacketMessage(message)) {
                        //红包信息
                        if (convertView == null || convertView.getTag(R.id.tag_chat_row_redpacket_send) == null) {
                            chatRow = new GMChatRowRedPacket(context, message, position, this);
                            convertView = chatRow;
                            convertView.setTag(R.id.tag_chat_row_redpacket_send, chatRow);
                        } else {
                            chatRow = (GMChatRowRedPacket) convertView.getTag(R.id.tag_chat_row_redpacket_send);
                        }
                    }
                    break;

                case IMAGE:
                    if (convertView == null || convertView.getTag(R.id.tag_chat_row_image_send) == null) {
                        chatRow = new GMChatRowImage(context, message, position, this);
                        convertView = chatRow;
                        convertView.setTag(R.id.tag_chat_row_image_send, chatRow);
                    } else {
                        chatRow = (GMChatRowImage) convertView.getTag(R.id.tag_chat_row_image_send);
                    }
                    initImageLocalPath(message);
                    break;

                case LOCATION:
                    if (convertView == null || convertView.getTag(R.id.tag_chat_row_location_send) == null) {
                        chatRow = new GMChatRowLocation(context, message, position, this);
                        convertView = chatRow;
                        convertView.setTag(R.id.tag_chat_row_location_send, chatRow);
                    } else {
                        chatRow = (GMChatRowLocation) convertView.getTag(R.id.tag_chat_row_location_send);
                    }
                    break;
                case VOICE:
                    if (convertView == null || convertView.getTag(R.id.tag_chat_row_voice_send) == null) {
                        chatRow = new GMChatRowVoice(context, message, position, this);
                        convertView = chatRow;
                        convertView.setTag(R.id.tag_chat_row_voice_send, chatRow);
                    } else {
                        chatRow = (GMChatRowVoice) convertView.getTag(R.id.tag_chat_row_voice_send);
                    }
                    break;
                default:
                    //4.8.0及之前，只支持图片，文字，红包。其他类型的给予提示
                    GMMessageBody messageBody = message.getMsgBody();
                    if (messageBody == null || !(messageBody instanceof GMTextMessageBody)) {
                        GMTextMessageBody textMessageBody = new GMTextMessageBody(context.getString(R.string.not_support_msg_type));
                        message.setMsgBody(textMessageBody);
                        messages.remove(position);
                        messages.add(position, message);
                    }

                    if (convertView == null || convertView.getTag(R.id.tag_chat_row_txt_send) == null) {
                        chatRow = new GMChatRowText(context, message, position, this);
                        convertView = chatRow;
                        convertView.setTag(R.id.tag_chat_row_txt_send, chatRow);
                    } else {
                        chatRow = (GMChatRowText) convertView.getTag(R.id.tag_chat_row_txt_send);
                    }
                    break;
            }
        } else {
            switch (message.getBodyType()) {
                case TEXT:
                    if (GMCommonUtils.getMessageExtBoolean(message, GMAppConstant.MESSAGE_ATTR_IS_BIG_EXPRESSION,
                        false)) {
                        if (convertView == null
                            || convertView.getTag(R.id.tag_chat_row_big_expression_receive) == null) {
                            chatRow = new GMChatRowBigExpression(context, message, position, this);
                            convertView = chatRow;
                            convertView.setTag(R.id.tag_chat_row_big_expression_receive, chatRow);
                        } else {
                            chatRow = (GMChatRowBigExpression) convertView.getTag(R.id.tag_chat_row_big_expression_receive);
                        }
                    } else {
                        //普通文字信息
                        if (convertView == null || convertView.getTag(R.id.tag_chat_row_txt_receive) == null) {
                            chatRow = new GMChatRowText(context, message, position, this);
                            convertView = chatRow;
                            convertView.setTag(R.id.tag_chat_row_txt_receive, chatRow);
                        } else {
                            chatRow = (GMChatRowText) convertView.getTag(R.id.tag_chat_row_txt_receive);
                        }
                    }
                    break;

                case CUSTOM:
                    if (GMCommonUtils.isRedPacketMessage(message)) {
                        //红包信息
                        if (convertView == null || convertView.getTag(R.id.tag_chat_row_redpacket_receive) == null) {
                            chatRow = new GMChatRowRedPacket(context, message, position, this);
                            convertView = chatRow;
                            convertView.setTag(R.id.tag_chat_row_redpacket_receive, chatRow);
                        } else {
                            chatRow = (GMChatRowRedPacket) convertView.getTag(R.id.tag_chat_row_redpacket_receive);
                        }
                    }
                    break;

                case IMAGE:
                    if (convertView == null || convertView.getTag(R.id.tag_chat_row_image_receive) == null) {
                        chatRow = new GMChatRowImage(context, message, position, this);
                        convertView = chatRow;
                        convertView.setTag(R.id.tag_chat_row_image_receive, chatRow);
                    } else {
                        chatRow = (GMChatRowImage) convertView.getTag(R.id.tag_chat_row_image_receive);
                    }
                    initImageLocalPath(message);
                    break;

                case LOCATION:
                    if (convertView == null || convertView.getTag(R.id.tag_chat_row_location_receive) == null) {
                        chatRow = new GMChatRowLocation(context, message, position, this);
                        convertView = chatRow;
                        convertView.setTag(R.id.tag_chat_row_location_receive, chatRow);
                    } else {
                        chatRow = (GMChatRowLocation) convertView.getTag(R.id.tag_chat_row_location_receive);
                    }
                    break;

                case VOICE:
                    if (convertView == null || convertView.getTag(R.id.tag_chat_row_voice_receive) == null) {
                        chatRow = new GMChatRowVoice(context, message, position, this);
                        convertView = chatRow;
                        convertView.setTag(R.id.tag_chat_row_voice_receive, chatRow);
                    } else {
                        chatRow = (GMChatRowVoice) convertView.getTag(R.id.tag_chat_row_voice_receive);
                    }
                    break;

                default:
                    //4.8.0及之前，只支持图片，文字，红包。其他类型的给予提示
                    GMMessageBody messageBody = message.getMsgBody();
                    if (messageBody == null || !(messageBody instanceof GMTextMessageBody)) {
                        GMTextMessageBody textMessageBody = new GMTextMessageBody(context.getString(R.string.not_support_msg_type));
                        message.setMsgBody(textMessageBody);
                        messages.remove(position);
                        messages.add(position, message);
                    }

                    if (convertView == null || convertView.getTag(R.id.tag_chat_row_txt_receive) == null) {
                        chatRow = new GMChatRowText(context, message, position, this);
                        convertView = chatRow;
                        convertView.setTag(R.id.tag_chat_row_txt_receive, chatRow);
                    } else {
                        chatRow = (GMChatRowText) convertView.getTag(R.id.tag_chat_row_txt_receive);
                    }
                    break;
            }
        }

        return chatRow;
    }

    private void initImageLocalPath(GMMessage message)
    {
        final GMImageMessageBody imgBody = (GMImageMessageBody) message.getMsgBody();
        if (TextUtils.isEmpty(imgBody.getLocalPath()) || !new File(imgBody.getLocalPath()).exists())
        {
            new Thread(new Runnable()
            {
                @Override
                public void run()
                {
                    try
                    {
                        File file = Glide.with(context).load(imgBody.getRemotePath()).downloadOnly(Target.SIZE_ORIGINAL,
                            Target.SIZE_ORIGINAL).get();
                        imgBody.setLocalPath(file.getAbsolutePath());
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                }
            }).start();
        }
    }

    /**
     * 设置checkbox是否可见
     */
    public void setCheckBoxVisiable(boolean isVisiable)
    {
        this.isVisiable = isVisiable;
        if (!isVisiable)
        {
            txtMsgIdList.clear();
            imgMsgIdList.clear();
            otherMsgIdList.clear();
        }
        refresh();
    }

    @SuppressLint("NewApi")
    public View getView(final int position, View convertView, ViewGroup parent) {
        final GMMessage message = getItem(position);
        final GMChatRow row = createChatRow(context, convertView, message, position);
        if (row == null) {
            return convertView;
        }

        //聊天室语音消息，在已读列表中，但是为标记已读，添加标记
        if (message.getBodyType() == GMConstant.MsgBodyType.VOICE
            && (message.getChatType() == ConversationType.CLASSICROOM
            || message.getChatType() == ConversationType.CAROLVOICEROOM)
            && VoiceMessageUtils.isListened(message)
            && GMCommonUtils.getMessageExtInt(message, GMAppConstant.GOOME_VOICE_LISTENED, 0) == 0) {
            GMCommonUtils.addMessageExtInt(message, GMAppConstant.GOOME_VOICE_LISTENED, 1);
        }

        // refresh ui with messages
        row.setUpView(message, position, iChatOwnerId, itemClickListener);
        if (isVisiable) {
            row.setCheckBoxVisiable(isVisiable);
            row.setChecked(txtMsgIdList.contains(message.getMsgId()) || imgMsgIdList.contains(message.getMsgId())
                || otherMsgIdList.contains(message.getMsgId()));
        } else {
            row.setCheckBoxVisiable(isVisiable);
            row.setChecked(false);
        }
        row.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton arg0, boolean isChecked) {
                if (isChecked) {
                    switch (message.getBodyType()) {
                        case TEXT:
                            if (txtMsgIdList.size() >= TXT_MSG_MAX) {
                                Toast.makeText(context, R.string.em_message_txt_beyond_max, Toast.LENGTH_SHORT).show();
                                row.setOnCheckedChangeListener(null);
                                row.setChecked(false);
                                row.setOnCheckedChangeListener(this);
                            } else {
                                txtMsgIdList.add(message.getMsgId());
                            }
                            break;

                        case IMAGE:
                            if (imgMsgIdList.size() >= IMAGE_MSG_MAX) {
                                Toast.makeText(context, R.string.em_message_image_beyond_max, Toast.LENGTH_SHORT)
                                    .show();
                                row.setOnCheckedChangeListener(null);
                                row.setChecked(false);
                                row.setOnCheckedChangeListener(this);
                            } else {
                                String str = getImageURL(message);
                                if (str != null) {
                                    imgMsgIdList.add(message.getMsgId());
                                } else {
                                    Toast.makeText(context, R.string.em_message_image_checked_no_found,
                                        Toast.LENGTH_SHORT).show();
                                    row.setOnCheckedChangeListener(null);
                                    row.setChecked(false);
                                    row.setOnCheckedChangeListener(this);
                                }
                            }
                            break;

                        case LOCATION:
                        case VOICE:
                        case VIDEO:
                        case FILE:
                        default:
                            otherMsgIdList.add(message.getMsgId());
                            break;
                    }
                } else if (txtMsgIdList.contains(message.getMsgId())) {
                    txtMsgIdList.remove(message.getMsgId());
                } else if (imgMsgIdList.contains(message.getMsgId())) {
                    imgMsgIdList.remove(message.getMsgId());
                } else if (otherMsgIdList.contains(message.getMsgId())) {
                    otherMsgIdList.remove(message.getMsgId());
                }
            }
        });
        return row;
    }

    /**
     * 是否选择了图片/文字以外的消息
     */
    public boolean hasCheckedOtherMessage() {
        return otherMsgIdList != null && otherMsgIdList.size() > 0;
    }

    /**
     * 获取所有选中的文字信息
     */
    public String getCheckedMessages()
    {
        if (txtMsgIdList == null || txtMsgIdList.size() <= 0)
        {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        int count = getCount();
        int num = 0;
        int txtMsgCount = txtMsgIdList.size();
        boolean is2User = txtMsgCount > 1; // 是否包含多用户
        for (int i = 0; i < count; i++)
        {
            GMMessage message = getItem(i);
            if (message != null && txtMsgIdList.contains(message.getMsgId()))
            {
                if (is2User)
                {
                    String userId = String.valueOf(message.getFrom());
                    String username = null;
                    if (message.getDirection() != MsgDirection.RECEIVE)
                    {
                        username = AllOnlineApp.sToken.name;
                    }
                    else if (userId != null && GMUserUtil.getUserInMemoryCache(userId) != null
                        && GMUserUtil.getUserInMemoryCache(userId).getName() != null)
                    {
                        username = GMUserUtil.getUserInMemoryCache(userId).getName();
                    }
                    else
                    {
                        username = userId;
                    }
                    sb.append(username + "：" + ((GMTextMessageBody) message.getMsgBody()).getMessage() + "\n");
                }
                else
                {
                    // 不显示用户名
                    sb.append(((GMTextMessageBody) message.getMsgBody()).getMessage() + "\n");
                }
                num++;
                if (num >= txtMsgCount || num >= TXT_MSG_MAX)
                {
                    break;
                }
            }
        }
        return sb.toString();
    }

    /**
     * 获取所有选中的图片
     */
    public ArrayList<String> getCheckedImages()
    {
        if (imgMsgIdList == null || imgMsgIdList.size() <= 0)
        {
            return null;
        }
        ArrayList<String> list = new ArrayList<String>();
        int count = getCount();
        int num = 0;
        int imgMsgCount = imgMsgIdList.size();
        for (int i = 0; i < count; i++)
        {
            GMMessage message = getItem(i);
            if (message != null && imgMsgIdList.contains(message.getMsgId()))
            {
                String url = getImageURL(message);
                if (url != null)
                {
                    list.add(url);
                    num++;
                    if (num >= imgMsgCount || num >= IMAGE_MSG_MAX)
                    {
                        break;
                    }
                }
            }
        }
        return list;
    }

    /**
     * 获取图片消息中，图片的本地存储地址 （优先获取大图，大图不存在时获取缩略图）
     *
     * @param message GMMessage
     * @return 返回null表示无本地存储图片
     */
    private String getImageURL(GMMessage message)
    {
        final GMImageMessageBody imageBody = (GMImageMessageBody) message.getMsgBody();
        String url = imageBody.getLocalPath();
        // 优先取本地大图
        if (!new File(url).exists())
        {
            return null;
        }
        return url;
    }

    public String getToChatUsername()
    {
        return toChatUsername;
    }

    public void setShowUserNick(boolean showUserNick)
    {
        this.showUserNick = showUserNick;
    }

    public void setShowAvatar(boolean showAvatar)
    {
        this.showAvatar = showAvatar;
    }

    public void setMyBubbleBg(Drawable myBubbleBg)
    {
        this.myBubbleBg = myBubbleBg;
    }

    public void setOtherBuddleBg(Drawable otherBuddleBg)
    {
        this.otherBuddleBg = otherBuddleBg;
    }

    public void setItemClickListener(MessageListItemClickListener listener)
    {
        itemClickListener = listener;
    }

    public boolean isShowUserNick()
    {
        return showUserNick;
    }

    public boolean isShowAvatar()
    {
        return showAvatar;
    }

    public Drawable getMyBubbleBg()
    {
        return myBubbleBg;
    }

    public Drawable getOtherBuddleBg()
    {
        return otherBuddleBg;
    }

    public void setChatOwnerId(long iChatOwnerId)
    {
        this.iChatOwnerId = iChatOwnerId;
    }
}
