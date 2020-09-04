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
package com.goomeim.utils;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.Looper;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.style.ClickableSpan;
import android.view.View;
import android.widget.Toast;
import com.coomix.app.all.AllOnlineApp;
import com.coomix.app.all.GlobalParam;
import com.coomix.app.all.R;
import com.coomix.app.all.service.AllOnlineApiClient;
import com.coomix.app.framework.app.Result;
import com.coomix.app.framework.util.CommonUtil;
import com.coomix.app.all.dialog.ProgressDialogEx;
import com.coomix.app.redpacket.activity.DetailRedPacketActivity;
import com.coomix.app.redpacket.util.RedPacketConstant;
import com.coomix.app.redpacket.util.RedPacketInfo;
import com.google.gson.JsonObject;
import com.goomeim.GMAppConstant;
import com.goomeim.controller.GMImManager;
import com.goomeim.domain.GMUser;
import com.goomeim.model.GMAtMessageHelper;
import java.util.ArrayList;
import java.util.List;
import net.goome.im.GMError;
import net.goome.im.chat.GMChatManager;
import net.goome.im.chat.GMClient;
import net.goome.im.chat.GMCmdMessageBody;
import net.goome.im.chat.GMConstant.ConversationType;
import net.goome.im.chat.GMConstant.MsgBodyType;
import net.goome.im.chat.GMConstant.MsgDirection;
import net.goome.im.chat.GMConstant.MsgStatus;
import net.goome.im.chat.GMConversation;
import net.goome.im.chat.GMMessage;
import net.goome.im.chat.GMTextMessageBody;
import net.goome.im.util.HanziToPinyin;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class GMCommonUtils
{
    private static final String TAG = "CommonUtils";

    /**
     * check if network avalable
     *
     * @param context
     * @return
     */
    public static boolean isNetWorkConnected(Context context)
    {
        if (context != null)
        {
            ConnectivityManager mConnectivityManager =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo mNetworkInfo = mConnectivityManager.getActiveNetworkInfo();
            if (mNetworkInfo != null)
            {
                return mNetworkInfo.isAvailable() && mNetworkInfo.isConnected();
            }
        }

        return false;
    }

    /**
     * check if sdcard exist
     */
    public static boolean isSdcardExist() {
        if (android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED)) {
            return true;
        } else {
            return false;
        }
    }

    public static GMMessage createExpressionMessage(String toChatUsername, String expressioName, String identityCode)
    {
        GMMessage message = GMMessage.createTxtSendMessage("[" + expressioName + "]", Long.parseLong(toChatUsername));
        JSONObject jsonObject = new JSONObject();
        try
        {
            if (identityCode != null)
            {
                jsonObject.put(GMAppConstant.MESSAGE_ATTR_EXPRESSION_ID, identityCode);
            }
            jsonObject.put(GMAppConstant.MESSAGE_ATTR_IS_BIG_EXPRESSION, true);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return message;
    }

    /**
     * Get digest according message type and content
     *
     * @param message
     * @param context
     * @return
     */
    public static String getMessageDigest(GMMessage message, Context context, String nickName) {
        String digest = "";
        switch (message.getBodyType()) {
            case LOCATION:
                if (message.getDirection() == MsgDirection.RECEIVE)
                {
                    digest = getString(context, R.string.location_recv);
                } else {
                    digest = getString(context, R.string.location_prefix);
                }
                break;

            case IMAGE:
                digest = getString(context, R.string.picture);
                break;

            case VOICE:
                digest = getString(context, R.string.voice_prefix);
                break;

            case VIDEO:
                digest = getString(context, R.string.video);
                break;

            case TEXT:
                GMTextMessageBody txtBody = (GMTextMessageBody) message.getMsgBody();
                if (getMessageExtBoolean(message, GMAppConstant.MESSAGE_ATTR_IS_VOICE_CALL, false)) {
                    digest = getString(context, R.string.voice_call) + txtBody.getMessage();
                } else if (getMessageExtBoolean(message, GMAppConstant.MESSAGE_ATTR_IS_VIDEO_CALL, false)) {
                    digest = getString(context, R.string.video_call) + txtBody.getMessage();
                } else if (getMessageExtBoolean(message, GMAppConstant.MESSAGE_ATTR_IS_BIG_EXPRESSION, false)) {
                    if (!TextUtils.isEmpty(txtBody.getMessage())) {
                        digest = txtBody.getMessage();
                    } else {
                        digest = getString(context, R.string.dynamic_expression);
                    }
                } else {
                    digest = txtBody.getMessage();
                }
                break;

            case FILE:
                digest = getString(context, R.string.file);
                break;

            case CUSTOM:
                if (isRedPacketMessage(message)) {
                    digest = getRedPacketWelcomeText(context, message, true);
                }
                break;

            default:
                return getString(context, R.string.not_support_msg_type);
        }

        return (TextUtils.isEmpty(nickName) ? "" : (nickName + ":")) + digest;
    }

    static String getString(Context context, int resId) {
        return context.getResources().getString(resId);
    }

    /**
     * get top activity
     *
     * @param context
     * @return
     */
    public static String getTopActivity(Context context)
    {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<RunningTaskInfo> runningTaskInfos = manager.getRunningTasks(1);

        if (runningTaskInfos != null) {
            return runningTaskInfos.get(0).topActivity.getClassName();
        } else {
            return "";
        }
    }

    /**
     * set initial letter of according user's nickname( username if no nickname)
     *
     * @param user
     */
    public static void setUserInitialLetter(GMUser user)
    {
        final String DefaultLetter = "#";
        String letter = DefaultLetter;

        final class GetInitialLetter
        {
            String getLetter(String name)
            {
                if (TextUtils.isEmpty(name))
                {
                    return DefaultLetter;
                }
                char char0 = name.toLowerCase().charAt(0);
                if (Character.isDigit(char0))
                {
                    return DefaultLetter;
                }
                ArrayList<HanziToPinyin.Token> l = HanziToPinyin.getInstance().get(name.substring(0, 1));
                if (l != null && l.size() > 0 && l.get(0).target.length() > 0)
                {
                    HanziToPinyin.Token token = l.get(0);
                    String letter = token.target.substring(0, 1).toUpperCase();
                    char c = letter.charAt(0);
                    if (c < 'A' || c > 'Z')
                    {
                        return DefaultLetter;
                    }
                    return letter;
                }
                return DefaultLetter;
            }
        }

        if (!TextUtils.isEmpty(user.getNickname()))
        {
            letter = new GetInitialLetter().getLetter(user.getNickname());
            user.setInitialLetter(letter);
            return;
        }
        if (letter == DefaultLetter && user.getUid() != 0)
        {
            letter = new GetInitialLetter().getLetter(String.valueOf(user.getUid()));
        }
        user.setInitialLetter(letter);
    }

    /**
     * 获取admin发出出入群或退群消息 <br/>
     * 欧贻葆<br/>
     * 1、加群/退群的消息提醒，后台会发送文本形式的消息，其中msg字段为空。 <br/>
     * 2、扩展消息部分，包括了 msgtype（必选），以及 id, account, nickname（可选） 及content（可选）字段<br/>
     * 3、msgtype有三种somebody_in, somebody_out，showcontent，分别代表有人加群，有人退群，显示通知文本
     * <br/>
     * 4、加群和退群，我会在ext字段告诉你们操作者的：
     * id（酷米客账户id）、account（环信账号，目前暂时与id相等）、nickname（昵称）。
     * 你们根据msgtype以及当前登陆者的身份来输出通知消息。<br/>
     * 5、msgtype为showcontent时，ext会有content字段，用于直接输出通知消息。<br/>
     *
     * @param message msg
     * @return admin string
     */
    public static String getAdminMessage(GMMessage message)
    {
        if (message.getDirection() == MsgDirection.RECEIVE)
        {
            if (getMessageExtBoolean(message, GMAppConstant.IS_ADMIN_MESSAGE, false)) {
                return ((GMTextMessageBody) message.getMsgBody()).getText();
            } else {
                //4.10.1开始此处分支代码弃用
                if (getMessageExtBoolean(message, GMAppConstant.KEY_IN_BLACKLIST, false)) {
                    //拉入黑名单
                    return ((GMTextMessageBody) message.getMsgBody()).getText();
                }
                String type = getMessageExtString(message, GMAppConstant.MESSAGE_TYPE, "");
                if (TextUtils.isEmpty(type)) {
                    return null;
                }
                String adminMsg = null;
                String account = getStringAttriFromGMMessage(message, GMAppConstant.MESSAGE_ACOUNT);
                String nickname = getStringAttriFromGMMessage(message, GMAppConstant.MESSAGE_NICKNAME);
                String content = getStringAttriFromGMMessage(message, GMAppConstant.MESSAGE_CONTENT);
                if (GMAppConstant.MSGTYPE_SOMEBODY_IN.equals(type)) {
                    if (account != null && account.equals(String.valueOf(GMClient.getInstance().getCurrentUserId()))) {
                        if (message.getChatType() == ConversationType.CHATROOM) {
                            adminMsg = getString(AllOnlineApp.mApp, R.string.chatroom_welcome);
                        } else if (message.getChatType() == ConversationType.CLASSICROOM) {
                            adminMsg = getString(AllOnlineApp.mApp, R.string.chatroom_classic_welcome);
                        }
                    } else {
                        if (nickname == null) {
                            nickname = account;
                        }
                        if (message.getChatType() == ConversationType.CHATROOM) {
                            adminMsg = nickname + getString(AllOnlineApp.mApp.getApplicationContext(),
                                R.string.chatroom_joinin);
                        } else if (message.getChatType() == ConversationType.CLASSICROOM) {
                            adminMsg = nickname + getString(AllOnlineApp.mApp.getApplicationContext(),
                                R.string.chatroom_classic_joinin);
                        }
                    }
                } else if (GMAppConstant.MSGTYPE_SOMEBODY_OUT.equals(type)) {
                    if (nickname == null) {
                        nickname = account;
                    }
                    if (message.getChatType() == ConversationType.CHATROOM) {
                        adminMsg =
                            nickname + getString(AllOnlineApp.mApp.getApplicationContext(), R.string.chatroom_exit);
                    } else if (message.getChatType() == ConversationType.CLASSICROOM) {
                        adminMsg = nickname + getString(AllOnlineApp.mApp.getApplicationContext(),
                            R.string.chatroom_classic_exit);
                    }
                } else if (GMAppConstant.MSGTYPE_SHOWCONTENT.equals(type)) {
                    adminMsg = content;
                }
                return adminMsg;
            }
        }
        return null;
    }

    public static boolean isCMDToTextMessage(GMMessage message)
    {
        if (message != null)
        {
            boolean bRevokeMsg = getMessageExtBoolean(message, GMAppConstant.REVOKE_CMD_TO_TEXT, false);
            String rpHostUid = getMessageExtString(message, GMAppConstant.REDPACKET_KEY_HOST_UID, "");
            if (bRevokeMsg || !TextUtils.isEmpty(rpHostUid))
            {
                return true;
            }
        }
        return false;
    }

    public static boolean isCMDRedpacketMessage(GMMessage message)
    {
        if(message != null)
        {
            String rpHostUid = getMessageExtString(message, GMAppConstant.REDPACKET_KEY_HOST_UID, "");
            if(!TextUtils.isEmpty(rpHostUid))
            {
                return true;
            }
        }
        return false;
    }

    public static boolean isRevokeCMDToTextMessage(GMMessage message)
    {
        if (message != null)
        {
            if (getMessageExtBoolean(message, GMAppConstant.REVOKE_CMD_TO_TEXT, false))
            {
                return true;
            }
        }
        return false;
    }
    private static void goToRedPacketDetailActivity(Context context,RedPacketInfo redPacketInfo)
    {
        Intent intent = new Intent(context, DetailRedPacketActivity.class);
        intent.putExtra(DetailRedPacketActivity.EXTRA_REDPACKET_INFO, redPacketInfo);
        context.startActivity(intent);
    }
    public static SpannableStringBuilder matcherSearchText(final Context context, int color, String string, String keyWord, final String redPacketId) {
        SpannableStringBuilder builder = new SpannableStringBuilder(string);
        int indexOf = string.indexOf(keyWord);
        if (indexOf != -1) {
            builder.setSpan(new TextClick(context,color,redPacketId),indexOf,indexOf + keyWord.length()
                    , Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        return builder;
    }
    private static class TextClick extends ClickableSpan {
        private Context context;
        private int color;
        private String redPacketId;
        public TextClick(Context context,int color,String redPacketId){
            this.context = context;
            this.color = color;
            this.redPacketId = redPacketId;
        }
        @Override
        public void onClick(View widget) {
            //在此处理点击事件
            requestRedPacketInfo(context,redPacketId);
        }

        @Override
        public void updateDrawState(TextPaint ds) {
            ds.setColor(color);
        }
    }

    public static String getCmdRevokeMessage(Context context, GMMessage message) {
        if (message == null || message.getFrom() == 0) {
            return null;
        }

        //本地存储后的撤回消息
        if (isRevokeCMDToTextMessage(message)) {
            String userId = String.valueOf(message.getFrom());
            if (userId.equals(String.valueOf(AllOnlineApp.sToken.community_id))) {
                return context.getString(R.string.you_revoke_message);
            }

            String name = GMUserUtil.getInstance().getCacheOrAttributeNickname(userId, message);
            if (TextUtils.isEmpty(name)) {
                return context.getString(R.string.revoke_a_message);
            }
            return String.format(context.getString(R.string.xxx_revoke_message), name);
        }

        return null;
    }

    public static String getCmdVoiceMessage(Context context, boolean bOpenedFlag)
    {
        if(bOpenedFlag)
        {
            return context.getString(R.string.audio_connect_succ);
        }
        else
        {
            return context.getString(R.string.audio_close_succ);
        }
    }

    public static String getCmdRedPaketMessage(Context context, GMMessage message) {
        if (message == null || message.getFrom() <= 0) {
            return null;
        }

        //本地存储后的领取红包消息
        String rpHostUid = GMCommonUtils.getMessageExtString(message, GMAppConstant.REDPACKET_KEY_HOST_UID, "");
        String myUid = String.valueOf(AllOnlineApp.sToken.community_id);
        String msgUserId = String.valueOf(message.getFrom());
        if (!TextUtils.isEmpty(rpHostUid) && (rpHostUid.equals(myUid) || (msgUserId != null && msgUserId.equals(
            myUid))))
        {
            int status = GMCommonUtils.getMessageExtInt(message, GMAppConstant.REDPACKET_KEY_STATUS, 0);
            String text;
            if (message.getDirection() == MsgDirection.SEND)
            {
                if (msgUserId.equals(rpHostUid))
                {
                    //你领取了自己发的红包
                    text = context.getString(R.string.alloc_redpacket_mine_mine);
                }
                else
                {
                    //你领取了别人发的红包.会带上红包人的你昵称REDPACKET_KEY_HOST_NAME
                    String name = getMessageExtString(message, GMAppConstant.REDPACKET_KEY_HOST_NAME, "");
                    text = context.getString(R.string.alloc_redpacket_mine_other, name);
                }
            }
            else
            {
                //别人领取了你发的红包
                String name = getMessageExtString(message, GMAppConstant.GOOME_NICKNAME, "");
                text = context.getString(R.string.alloc_redpacket_someone, name);
            }

            if (message.getChatType() != ConversationType.CHAT && status == RedPacketInfo.RP_STATUS_LOOT_ALL
                && rpHostUid.equals(String.valueOf(AllOnlineApp.sToken.community_id)))
            {
                text += context.getString(R.string.alloc_redpacket_finished);
            }
            //return matcherSearchText(context, Color.RED,text,context.getString(R.string.redpacket),rpId);
            return text;
        }

        return null;
    }
    private static void requestRedPacketInfo(final Context context,final String redPacketId)
    {
        //先网络请求红包状态，然后根据状态跳转
        final ProgressDialogEx progressDialogEx = showWaitInfoDialog(context, context.getString(R.string.query_ing));

        Runnable myRunnable = new Runnable() {
            @Override
            public void run()
            {
                Result result = AllOnlineApp.mApiClient.getRedPacketInfoById(-1, CommonUtil.getTicket(),
                        redPacketId, 0, RedPacketConstant.REDPACKET_USER_PER_PAGE,
                        AllOnlineApiClient.MAP_TYPE_GOOGLE,
                        String.valueOf(GlobalParam.getInstance().get_lat()),
                        String.valueOf(GlobalParam.getInstance().get_lng()), 0);
                new Handler(Looper.getMainLooper()).post(new CallBackRunnale(context,result,progressDialogEx));
            }
        };
        new Thread(myRunnable).start();
    }
    private static ProgressDialogEx showWaitInfoDialog(Context context, String msg)
    {
        ProgressDialogEx progressDialogEx = new ProgressDialogEx(context);
        progressDialogEx.setAutoDismiss(false);
        progressDialogEx.setCancelOnTouchOutside(false);

        try
        {
            progressDialogEx.show(msg);
        }
        catch (Exception e)
        {

        }
        return progressDialogEx;
    }

    private static void dismissWaitDialog(ProgressDialogEx progressDialogEx)
    {
        if (progressDialogEx != null && progressDialogEx.isShowing())
        {
            progressDialogEx.dismiss();
        }
    }
    private static class CallBackRunnale implements Runnable
    {
        private Context context;
        private Result result = null;
        private ProgressDialogEx progressDialogEx;

        CallBackRunnale(final Context context,final Result response,final ProgressDialogEx progressDialogEx)
        {
            this.context = context;
            this.result = response;
            this.progressDialogEx = progressDialogEx;
        }

        @Override
        public void run()
        {
            if(result == null)
            {
                return;
            }

            if (result.statusCode == Result.ERROR_NETWORK)
            {
                dismissWaitDialog(progressDialogEx);
                Toast.makeText(context, R.string.network_error, Toast.LENGTH_SHORT).show();
            }
            else
            {
                dismissWaitDialog(progressDialogEx);
                if (result.success && result.mResult != null && result.mResult instanceof RedPacketInfo)
                {
                    RedPacketInfo info = (RedPacketInfo) result.mResult;
                    goToRedPacketDetailActivity(context,info);
                }
                else
                {
                    Toast.makeText(context, R.string.leave_group_error, Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
    public static boolean isRedPacketMessage(GMMessage message)
    {
        if (message == null)
        {
            return false;
        }

        try
        {
            //判断扩展消息中是否携带红包的ID
            String redPacketId = getMessageExtString(message, GMAppConstant.REDPACKET_KEY_ID, "");
            if (!TextUtils.isEmpty(redPacketId))
            {
                return true;
            }
        }
        catch (Exception e)
        {
            //e.printStackTrace();
        }
        return false;
    }

    public static String getRedPacketWelcomeText(Context context, GMMessage message, boolean bAddCoomixRP) {
        if (message == null) {
            return "";
        }

        String redPacketTitle = "";
        try
        {
            redPacketTitle = getMessageExtString(message, GMAppConstant.REDPACKET_KEY_TITLE, "");
        }
        catch (Exception e)
        {
            //e.printStackTrace();
        }
        if (TextUtils.isEmpty(redPacketTitle))
        {
            redPacketTitle = context.getString(R.string.redpacket_default_title);
        }

        if (bAddCoomixRP)
        {
            redPacketTitle = "[" + context.getString(R.string.coomix_redpacket) + "]" + redPacketTitle;
        }
        return redPacketTitle;
    }

    public static int getRedPacketType(GMMessage message)
    {
        if (message == null)
        {
            return -1;
        }

        return getMessageExtInt(message, GMAppConstant.REDPACKET_KEY_TYPE, RedPacketConstant.REDPACKET_TYPE_NORMAL);
    }

    private static String getStringAttriFromGMMessage(GMMessage message, String attri)
    {
        return getMessageExtString(message, attri, "");
    }

    public static void saveRevokeCMDMessage(long revokeServerMsgId, String toChatUsername, GMMessage cmdMessage) {
        if (revokeServerMsgId < 0
            || TextUtils.isEmpty(toChatUsername)
            || cmdMessage == null
            || GMClient.getInstance().chatManager() == null) {
            return;
        }

        GMConversation conversation = GMClient.getInstance()
            .chatManager()
            .getConversation(cmdMessage.getConversationId(), cmdMessage.getChatType());
        if (conversation == null) {
            return;
        }

        long localMsgId = -1;
        if (cmdMessage.getChatType() == ConversationType.CLASSICROOM)
        {
            localMsgId = GMClient.getInstance().chatManager().queryMessageIdBySvrId(cmdMessage.getConversationId(),
                revokeServerMsgId);
        }
        else
        {
            localMsgId = GMClient.getInstance().chatManager().queryMessageIdBySvrId(revokeServerMsgId);
        }
        GMMessage delMessage = conversation.getMessage(localMsgId);
        if (delMessage != null)
        {
            //把需要撤回的消息删除掉
            conversation.removeMessage(localMsgId);
            String content = "";
            if (cmdMessage.getFrom() == AllOnlineApp.sToken.community_id) {
                content = AllOnlineApp.mApp.getString(R.string.you_revoke_message);
            } else {
                content = String.format(AllOnlineApp.mApp.getString(R.string.xxx_revoke_message),
                    getMessageExtString(cmdMessage, GMAppConstant.GOOME_NICKNAME, ""));
            }

            /**把撤回提示作为新消息插入到本地数据库--把透传消息的时间作为lastmsg，在聊天页面的列表中不显示，只早外面对话列表显示*/
            GMMessage newMessage = GMMessage.createReceiveMessage(delMessage.getFrom(), MsgBodyType.TEXT);
            newMessage.setFrom(delMessage.getFrom());
            newMessage.setTo(delMessage.getTo());
            GMTextMessageBody textBody = new GMTextMessageBody(content);
            newMessage.setMsgBody(textBody);
            newMessage.setMsgId(delMessage.getMsgId());
            newMessage.setSvrMsgId(delMessage.getSvrMsgId());
            newMessage.setStatus(MsgStatus.SUCCESSED);
            newMessage.setTimestamp(delMessage.getTimestamp() / 1000);
            newMessage.setLocaltime(delMessage.getLocaltime() / 1000);
            newMessage.setChatType(cmdMessage.getChatType());
            newMessage.setRead(true);

            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty(GMAppConstant.IS_ADMIN_MESSAGE, true);
            newMessage.setExt(jsonObject.toString());

            conversation.appendMessage(newMessage);
        }
    }

    public static boolean handleCMDMessageAndHasInputing(List<GMMessage> messages, Context context) {
        boolean bInputing = false;
        for (GMMessage message : messages) {
            if (message == null || message.getMsgBody() == null) {
                continue;
            }

            GMCmdMessageBody cmdMsgBody = (GMCmdMessageBody) message.getMsgBody();
            String action = cmdMsgBody.getAction();//获取自定义action
            if (action.equals(GMAppConstant.REVOKE_FLAG)) {
                try {
                    long revokeServerMsgId = getMessageExtLong(message, GMAppConstant.REVOKE_MESSAGE_ID, -1L);
                    /*--删除消息来表示撤回,并且新插入一条消息提示已撤回--*/
                    if (revokeServerMsgId >= 0) {
                        long localMsgId = -1;
                        if (message.getChatType() == ConversationType.CLASSICROOM) {
                            localMsgId = GMClient.getInstance().chatManager().queryMessageIdBySvrId(
                                message.getConversationId(), revokeServerMsgId);
                        } else {
                            localMsgId = GMClient.getInstance().chatManager().queryMessageIdBySvrId(revokeServerMsgId);
                        }
                        GMAtMessageHelper.get().removeAtMeGroup(String.valueOf(message.getTo()),
                            String.valueOf(localMsgId));
                        saveRevokeCMDMessage(revokeServerMsgId, String.valueOf(message.getTo()), message);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else if (action.equals(GMAppConstant.REDPACKET_FLAG)) {
                //领取红包的透传消息，只有红包主人和领取红包的人才会保存此消息
                String rpHostUid = getMessageExtString(message, GMAppConstant.REDPACKET_KEY_HOST_UID, "");
                String rpId = getMessageExtString(message, GMAppConstant.REDPACKET_KEY_UNPACK_ID, "");
                String myUid = String.valueOf(AllOnlineApp.sToken.community_id);
                if (!TextUtils.isEmpty(rpHostUid) && rpHostUid.equals(myUid) && message.getFrom() != Long.parseLong(
                    myUid)) {
                    saveRedpacketCMDMessage(message, rpHostUid, rpId, message.getChatType());
                }
            } else if (action.equals(GMAppConstant.INPUTING_FLAG) && message.getChatType() == ConversationType.CHAT) {
                bInputing = true;
            } else if (message.getChatType() == ConversationType.CAROLVOICEROOM) {
                boolean bOpenedFlag = false;
                if (action.equals(GMAppConstant.k_strVoiceOpenFlag)) {
                    bOpenedFlag = true;
                    Intent intent = new Intent(GMAppConstant.k_strVoiceOpenFlag);
                    intent.putExtra(GMAppConstant.EXTRA_USER_ID, message.getTo());
                    context.sendBroadcast(intent);
                } else if (action.equals(GMAppConstant.k_strVoiceCloseFlag)) {
                    bOpenedFlag = false;
                    Intent intent = new Intent(GMAppConstant.k_strVoiceCloseFlag);
                    intent.putExtra(GMAppConstant.EXTRA_USER_ID, message.getTo());
                    context.sendBroadcast(intent);
                }
                saveVoiceCMDMessage(message, bOpenedFlag);
            }
        }
        return bInputing;
    }

    public static void saveRedpacketCMDMessage(GMMessage cmdMessage, String rpHostUid, String redPacket_id, ConversationType chatType)
    {
        if(cmdMessage == null || GMClient.getInstance().chatManager() == null) {
            return;
        }
        GMConversation conversation =
            GMClient.getInstance().chatManager().getConversation(cmdMessage.getConversationId(), chatType);
        if (conversation != null) {
            /**把撤回提示作为新消息插入到本地数据库--把透传消息的时间作为lastmsg，在聊天页面的列表中不显示，只早外面对话列表显示*/
            GMMessage newMessage = GMMessage.createReceiveMessage(cmdMessage.getFrom(), MsgBodyType.TEXT);
            newMessage.setFrom(cmdMessage.getFrom());
            newMessage.setTo(cmdMessage.getTo());
            GMTextMessageBody textBody = new GMTextMessageBody(getCmdRedPaketMessage(AllOnlineApp.mApp, cmdMessage));
            newMessage.setMsgBody(textBody);
            newMessage.setStatus(MsgStatus.SUCCESSED);
            newMessage.setTimestamp(cmdMessage.getTimestamp() / 1000);
            newMessage.setLocaltime(cmdMessage.getLocaltime() / 1000);
            newMessage.setChatType(cmdMessage.getChatType());
            newMessage.setRead(true);

            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty(GMAppConstant.IS_ADMIN_MESSAGE, true);
            jsonObject.addProperty(GMAppConstant.REDPACKET_KEY_HOST_UID, rpHostUid);
            jsonObject.addProperty(GMAppConstant.REDPACKET_KEY_UNPACK_ID, redPacket_id);
            newMessage.setExt(jsonObject.toString());

            conversation.appendMessage(newMessage);
        }
    }

    public static void saveVoiceCMDMessage(GMMessage cmdMessage, boolean bOpenedFlag)
    {
        if(cmdMessage == null || GMClient.getInstance().chatManager() == null)
        {
            return;
        }

        GMConversation conversation = GMClient.getInstance().chatManager().getConversation(cmdMessage.getConversationId(), cmdMessage.getChatType());
        if(conversation == null)
        {
            return;
        }

        /**把开启关闭语音提示作为新消息插入到本地数据库--把透传消息的时间作为lastmsg，在聊天页面的列表中不显示，只早外面对话列表显示*/
        GMMessage newMessage = GMMessage.createReceiveMessage(cmdMessage.getFrom(), MsgBodyType.TEXT);
        newMessage.setFrom(cmdMessage.getFrom());
        newMessage.setTo(cmdMessage.getTo());
        GMTextMessageBody textBody = new GMTextMessageBody(getCmdVoiceMessage(AllOnlineApp.mApp, bOpenedFlag));
        newMessage.setMsgBody(textBody);
        newMessage.setStatus(MsgStatus.SUCCESSED);
        newMessage.setTimestamp(cmdMessage.getTimestamp() / 1000);
        newMessage.setLocaltime(cmdMessage.getLocaltime() / 1000);
        newMessage.setChatType(cmdMessage.getChatType());
        newMessage.setRead(true);

        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty(GMAppConstant.IS_ADMIN_MESSAGE, true);
        newMessage.setExt(jsonObject.toString());

        conversation.appendMessage(newMessage);
    }

    public static boolean getMessageExtBoolean(GMMessage message, String key, boolean defaultValue)
    {
        if (message != null && !TextUtils.isEmpty(key))
        {
            String ext = message.getExt();
            if (!TextUtils.isEmpty(ext))
            {
                JSONObject json = getJSONObject(ext);
                if (json != null)
                {
                    return json.optBoolean(key, defaultValue);
                }
            }
        }
        return false;
    }

    public static String getMessageExtString(GMMessage message, String key, String defaultValue)
    {
        if (message != null && !TextUtils.isEmpty(key))
        {
            String ext = message.getExt();
            if (!TextUtils.isEmpty(ext))
            {
                JSONObject json = getJSONObject(ext);
                if (json != null)
                {
                    return json.optString(key, defaultValue);
                }
            }
        }
        return null;
    }

    public static int getMessageExtInt(GMMessage message, String key, int defaultValue)
    {
        if (message != null && !TextUtils.isEmpty(key))
        {
            String ext = message.getExt();
            if (!TextUtils.isEmpty(ext))
            {
                JSONObject json = getJSONObject(ext);
                if (json != null)
                {
                    return json.optInt(key, defaultValue);
                }
            }
        }
        return -1;
    }

    public static long getMessageExtLong(GMMessage message, String key, long defaultValue)
    {
        if (message != null && !TextUtils.isEmpty(key))
        {
            String ext = message.getExt();
            if (!TextUtils.isEmpty(ext))
            {
                JSONObject json = getJSONObject(ext);
                if (json != null)
                {
                    return json.optLong(key, defaultValue);
                }
            }
        }
        return -1;
    }

    public static JSONArray getMessageExtArray(GMMessage message, String key)
    {
        if (message != null && !TextUtils.isEmpty(key))
        {
            String ext = message.getExt();
            if (!TextUtils.isEmpty(ext))
            {
                JSONObject extObj = getJSONObject(ext);
                if (extObj != null)
                {
                    return extObj.optJSONArray(key);
                }
            }
        }
        return null;
    }
    public static void addMessageExtInt(GMMessage message, String key, int value)
    {
        if (message != null && !TextUtils.isEmpty(key))
        {
            String ext = message.getExt();
           // if (!TextUtils.isEmpty(ext))
            {
                JsonObject extObj = new JsonObject();//JsonParser().parse(ext).getAsJsonObject();
                if (extObj != null)
                {
                    extObj.addProperty(key, value);
                    message.setExt(extObj.toString());
                }
            }
        }
    }

    private static JSONObject getJSONObject(String content)
    {
        try
        {
            return new JSONObject(content);
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
        return null;
    }

    public static String getConversationIdByUserId(String uid, ConversationType chatType) {
        if (chatType != ConversationType.CHAT) {
            return uid;
        }
        return AllOnlineApp.sToken.community_id + "#" + uid;
    }

    public static String getUserIdByConversationId(String conId, ConversationType chatType)
    {
        if (chatType != ConversationType.CHAT)
        {
            return conId;
        }
        if (!TextUtils.isEmpty(conId) && conId.contains("#"))
        {
            return conId.split("#")[1];
        }
        return null;
    }

    public static GMConversation getSystemGMConversation()
    {
        GMClient gmClient = GMClient.getInstance();
        if (gmClient != null)
        {
            GMChatManager chatManager = gmClient.chatManager();
            if (chatManager != null)
            {
                return chatManager.getConversation(GMCommonUtils.getConversationIdByUserId(
                    GMImManager.SYSTEM_REPLY_FAV_IM_ID_STR, ConversationType.CHAT), ConversationType.CHAT);
            }
        }
        return null;
    }

    public static boolean isHyperLinkMessage(GMMessage message)
    {
        if (message != null)
        {
            String ext = message.getExt();
            if (!TextUtils.isEmpty(ext) && ext.contains(GMAppConstant.GM_EXT_HYPER_LINK_NICKNAME)
                && ext.contains(GMAppConstant.GM_EXT_HYPER_LINK_UID) && ext.contains(
                GMAppConstant.GM_EXT_HYPER_LINK_JUMP_TYPE))
            {
                return true;
            }
        }
        return false;
    }

    public static ArrayList<GMHyperLinkValue> getMessageHyperLinkValueList(GMMessage message)
    {
        ArrayList<GMHyperLinkValue> listLinkVale = new ArrayList<GMHyperLinkValue>();
        if (message != null)
        {
            try
            {
                JSONObject jsonObject = new JSONObject(message.getExt());
                JSONArray jsonArray = jsonObject.getJSONArray(GMAppConstant.GM_EXT_HYPER_LINK_LIST);
                if (jsonArray != null)
                {
                    for (int i = 0; i < jsonArray.length(); i++)
                    {
                        jsonObject = jsonArray.getJSONObject(i);
                        GMHyperLinkValue gmHyperLinkValue = new GMHyperLinkValue();
                        gmHyperLinkValue.setNickName(
                            jsonObject.optString(GMAppConstant.GM_EXT_HYPER_LINK_NICKNAME, ""));
                        gmHyperLinkValue.setUid(jsonObject.optLong(GMAppConstant.GM_EXT_HYPER_LINK_UID, 0L));
                        gmHyperLinkValue.setJumpType(jsonObject.optInt(GMAppConstant.GM_EXT_HYPER_LINK_JUMP_TYPE, 0));
                        listLinkVale.add(gmHyperLinkValue);
                    }
                }

                return listLinkVale;
            }
            catch (Exception e)
            {

            }
        }
        return listLinkVale;
    }

    public static int saveAsInBlackMessage(long toChatUsername, ConversationType chatType, String content)
    {
        if (GMClient.getInstance().chatManager() == null)
        {
            return -1;
        }

        GMConversation conversation = GMClient.getInstance().chatManager().getConversation(
            getConversationIdByUserId(String.valueOf(toChatUsername), chatType), chatType);
        if (conversation == null)
        {
            return -1;
        }

        GMMessage newMessage = GMMessage.createReceiveMessage(toChatUsername, MsgBodyType.TEXT);
        GMTextMessageBody textBody = new GMTextMessageBody(content);
        newMessage.setMsgBody(textBody);
        newMessage.setStatus(MsgStatus.SUCCESSED);
        newMessage.setChatType(chatType);
        if (chatType != ConversationType.CHAT)
        {
            newMessage.setTo(toChatUsername);
        }
        newMessage.setRead(true);

        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty(GMAppConstant.IS_ADMIN_MESSAGE, true);
        newMessage.setExt(jsonObject.toString());

        GMError error = conversation.appendMessage(newMessage);
        return error.errCode();
    }
}
