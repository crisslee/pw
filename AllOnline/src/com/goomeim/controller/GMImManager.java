package com.goomeim.controller;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.content.Context;
import android.content.pm.PackageManager;
import android.util.Log;
import com.coomix.app.all.AllOnlineApp;
import com.coomix.app.all.Constant;
import com.coomix.app.all.R;
import com.coomix.app.framework.util.OSUtil;
import com.coomix.app.all.util.CommunityUtil;
import com.google.gson.JsonObject;
import com.goomeim.GMAppConstant;
import com.goomeim.domain.GMEmojicon;
import com.goomeim.model.GMAtMessageHelper;
import com.goomeim.model.GMNotifier;
import com.goomeim.utils.GMCommonUtils;
import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import net.goome.im.GMChatRoomChangeListener;
import net.goome.im.GMError;
import net.goome.im.GMMessageListener;
import net.goome.im.GMValueCallBack;
import net.goome.im.chat.GMChatManager;
import net.goome.im.chat.GMChatRoom;
import net.goome.im.chat.GMChatroomMemberInfo;
import net.goome.im.chat.GMClient;
import net.goome.im.chat.GMConstant.ChatroomMode;
import net.goome.im.chat.GMConstant.ConversationType;
import net.goome.im.chat.GMConstant.LeaveChatroomReason;
import net.goome.im.chat.GMConstant.MsgBodyType;
import net.goome.im.chat.GMConstant.MsgDownloadStatus;
import net.goome.im.chat.GMConstant.MsgStatus;
import net.goome.im.chat.GMContactManager;
import net.goome.im.chat.GMConversation;
import net.goome.im.chat.GMFileMessageBody;
import net.goome.im.chat.GMMessage;
import net.goome.im.chat.GMOptions;
import net.goome.im.chat.GMPageResult;
import net.goome.im.chat.GMTextMessageBody;

public final class GMImManager {
    private static final String TAG = GMImManager.class.getSimpleName();
    public static final String SYSTEM_REPLY_FAV_IM_ID_STR = "10000";

    /**
     * the global EaseUI instance
     */
    private static GMImManager instance = null;

    private GMSettingsProvider settingsProvider;

    /**
     * application context
     */
    private Context appContext = null;

    /**
     * init flag: test if the sdk has been inited before, we don't need to init
     * again
     */
    private boolean sdkInited = false;

    /**
     * the notifier
     */
    private GMNotifier notifier = null;

    /**
     * save foreground Activity which registered eventlistener
     */
    private List<Activity> activityList = new ArrayList<Activity>();
    private ChatroomListener chatroomListener = new ChatroomListener();
    private List<GMChatroomAppListener> listRoomListeners;
    private ArrayList<GMMessageAppListener> listListeners = new ArrayList<GMMessageAppListener>();

    public void pushActivity(Activity activity) {
        if (!activityList.contains(activity)) {
            activityList.add(0, activity);
        }
    }

    public void popActivity(Activity activity) {
        activityList.remove(activity);
    }

    private GMImManager() {

    }

    /**
     * get instance of EaseUI
     */
    public synchronized static GMImManager getInstance() {
        if (instance == null) {
            instance = new GMImManager();
        }
        return instance;
    }

    /**
     * this function will initialize the SDK and easeUI kit
     *
     * @param context context
     * @param options use default if options is null
     * @return boolean
     */
    public synchronized boolean init(Context context, GMOptions options, String clientVersion) {
        Log.i(TAG, "GMIM init");
        if (sdkInited) {
            return true;
        }
        appContext = context.getApplicationContext();

        int pid = android.os.Process.myPid();
        String processAppName = getAppName(pid);

        Log.i(TAG, "process app name : " + processAppName);

        // if there is application has remote service, application:onCreate()
        // maybe called twice
        // this check is to make sure SDK will initialized only once
        // return if process name is not application's name since the package
        // name is the default process name
        if (processAppName == null || !processAppName.equalsIgnoreCase(
            appContext.getPackageName())) {
            Log.i(TAG, "enter the service process!");
            return false;
        }
        if (options == null) {
            Log.i(TAG, "init gm client, null options");
            GMClient.getInstance().init(context, initChatOptions());
            GMClient.getInstance().setDeviceId(OSUtil.getUdid(context));
        } else {
            Log.i(TAG, "init gm client");
            GMClient.getInstance().init(context, options);
            GMClient.getInstance().setDeviceId(OSUtil.getUdid(context));
        }

        GMClient.getInstance().getChatConfig().setClientVersion(clientVersion);

        AllOnlineApp.registerConn();

        initNotifier();
        registerMessageListener();
        registerGMIMChatroomChangeListener();

        if (settingsProvider == null) {
            settingsProvider = new DefaultSettingsProvider();
        }

        sdkInited = true;
        return true;
    }

    protected GMOptions initChatOptions() {
        Log.i(TAG, "init GMIM Options");

        GMOptions options = new GMOptions();
        // change to need confirm contact invitation
        options.setAutoAcceptFriendInvitation(false);
        // set if need read ack
        options.setRequireReadAck(false);
        // set if need delivery ack
        options.setEnableDeliveryAck(false);
        // options.setMipushConfig("2882303761517144232", "5731714425232");
        // options.setHuaweiPushAppId("10014796");
        options.setEnableConsoleLog(Constant.IS_DEBUG_MODE);
        //options.setLogLevel(BusOnlineApp.getAppConfig().getIm_log_level());
        options.setLogLevel(AllOnlineApp.getAppConfig().getIm_log_level());
        return options;
    }

    void initNotifier() {
        if (notifier == null) {
            if (appContext == null) {
                appContext = AllOnlineApp.mApp.getApplicationContext();
            }
            notifier = new GMNotifier();
            notifier.init(appContext);
        }
    }

    private void registerMessageListener() {
        GMChatManager chatManager = GMClient.getInstance().chatManager();
        if (chatManager == null) {
            return;
        }
        chatManager.addMessageListener(new GMMessageListener() {
            @Override
            public void onMessageReceived(List<GMMessage> messages) {
                if (isOnlySystemMsg(messages)) {
                    //用于消息通知的，是系统账户推送的
                    return;
                }
                GMAtMessageHelper.get().parseMessages(messages);
                processVoiceMessage(messages);
                if(CommunityUtil.isBackground(appContext)) {
                    //处于后台，且打开了私聊的通知，则消息通知栏展示
                    getNotifier().onNewMsg(messages);
                } else {
                    for (GMMessageAppListener listener : listListeners) {
                        if (listener != null) {
                            listener.onMessageReceived(messages);
                        }
                    }
                }
            }

            @Override
            public void onMessageRead(List<GMMessage> messages) {
                for (GMMessageAppListener listener : listListeners) {
                    if (listener != null) {
                        listener.onMessageRead(messages);
                    }
                }
            }

            @Override
            public void onMessageDelivered(List<GMMessage> messages) {
                for (GMMessageAppListener listener : listListeners) {
                    if (listener != null) {
                        listener.onMessageDelivered(messages);
                    }
                }
            }

            @Override
            public void onMessageChanged(GMMessage message, GMError error) {
                List<GMMessage> messages = new ArrayList<GMMessage>();
                messages.add(message);
                if (isOnlySystemMsg(messages)) {
                    //用于消息通知的，是系统账户推送的
                    return;
                }
                for (GMMessageAppListener listener : listListeners) {
                    if (listener != null) {
                        listener.onMessageChanged(message, error);
                    }
                }
            }

            @Override
            public void onCmdMessageReceived(List<GMMessage> messages) {
                boolean bInputing = GMCommonUtils.handleCMDMessageAndHasInputing(messages, appContext);
                if (!CommunityUtil.isBackground(appContext)) {
                    for (GMMessageAppListener listener : listListeners) {
                        if (listener != null) {
                            listener.onCmdMessageReceived(messages, bInputing);
                        }
                    }
                }
            }
        });
    }

    private void processVoiceMessage(List<GMMessage> messages)
    {
        for (final GMMessage msg : messages)
        {
            if (msg.getBodyType() == MsgBodyType.VOICE)
            {
                GMFileMessageBody body = (GMFileMessageBody) msg.getMsgBody();
                if (body != null && (body.getDownloadStatus() == MsgDownloadStatus.PENDING
                        || body.getDownloadStatus() == MsgDownloadStatus.FAILED))
                {
                    new Thread(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            GMChatManager mgr = GMClient.getInstance().chatManager();
                            if (mgr != null)
                            {
                                mgr.downloadAttachment(msg);
                            }
                        }
                    }).start();
                }
            }
        }
    }

    private void registerGMIMChatroomChangeListener()
    {
        GMClient.getInstance().chatroomManager().addChatRoomChangeListener(chatroomListener);
    }

    private class ChatroomListener implements GMChatRoomChangeListener
    {
        @Override
        public void onMemberJoined(final long roomId, final List<GMChatroomMemberInfo> memberInfos)
        {
            GMChatRoom room = GMClient.getInstance().chatroomManager().getChatroomInfoFromDB(roomId);

            if (room != null)
            {
                saveSomebodyInMsg(roomId, room, memberInfos);
            }
            else
            {
                GMClient.getInstance().chatroomManager().getChatroomSpecificationFromServerWithId(roomId,
                    new GMValueCallBack<GMChatRoom>()
                    {
                        @Override
                        public void onSuccess(GMChatRoom room)
                        {
                            if (room != null)
                            {
                                saveSomebodyInMsg(roomId, room, memberInfos);
                            }
                        }

                        @Override
                        public void onError(GMError gmError)
                        {
                        }
                    });
            }
            for(GMChatroomAppListener listener : listRoomListeners)
            {
                if (listener != null)
                {
                    listener.onChatRoomChaged();
                }
            }
        }

        private void saveSomebodyInMsg(long roomId, GMChatRoom room, List<GMChatroomMemberInfo> memberInfos)
        {
            if (room == null || memberInfos == null || memberInfos.size() <= 0)
            {
                return;
            }
            GMChatroomMemberInfo user = memberInfos.get(0);
            ConversationType type = getConversationTypeByRoomType(room.getModeType());
            GMConversation conversation = GMClient.getInstance().chatManager().getConversation(String.valueOf(roomId),
                type, true);
            if (conversation != null)
            {
                long myUid = GMClient.getInstance().getCurrentUserId();
                String content = "";
                if(user.getUid() == myUid)
                {
                    //用户自己加入了聊天室或群聊
                    if (conversation.getType() == ConversationType.CLASSICROOM)
                    {
                        //聊天室
                        content = appContext.getString(R.string.chatroom_classic_welcome);
                    }
                    else if (conversation.getType() == ConversationType.CHATROOM)
                    {
                        //群聊
                        content = appContext.getString(R.string.chatroom_welcome);
                    }
                    else if (conversation.getType() == ConversationType.CAROLVOICEROOM)
                    {
                        //录音设备
                        //content = appContext.getString(R.string.chatroom_welcome);
                        return;
                    }
                }
                else
                {
                    String userName = "\"" + user.getNickname() + "\"";
                    if(memberInfos.size() > 1)
                    {
                        //多人，批量处理
                        userName += appContext.getString(R.string.multiple_members, memberInfos.size());
                    }
                    //其他用户加入
                    if (conversation.getType() == ConversationType.CLASSICROOM)
                    {
                        //聊天室
                        content = userName + appContext.getString(R.string.chatroom_classic_joinin);
                    }
                    else if (conversation.getType() == ConversationType.CHATROOM)
                    {
                        //群聊
                        content = userName + appContext.getString(R.string.chatroom_joinin);
                    }
                    else if (conversation.getType() == ConversationType.CAROLVOICEROOM)
                    {
                        //录音设备
                        //content = appContext.getString(R.string.chatroom_welcome);
                        return;
                    }
                }

                GMMessage msg = GMMessage.createReceiveMessage(myUid, MsgBodyType.TEXT);
                GMTextMessageBody body = new GMTextMessageBody(content);
                msg.setMsgBody(body);
                msg.setChatType(type);
                msg.setFrom(myUid);
                msg.setTo(roomId);
                msg.setRead(true);
                msg.setStatus(MsgStatus.SUCCESSED);

                JsonObject extObj = new JsonObject();
                extObj.addProperty(GMAppConstant.IS_ADMIN_MESSAGE, true);
                msg.setExt(extObj.toString());

                conversation.appendMessage(msg);
            }
        }

        @Override
        public void onMemberExited(final long roomId, final List<GMChatroomMemberInfo> memberInfos)
        {
            GMChatRoom room = GMClient.getInstance().chatroomManager().getChatroomInfoFromDB(roomId);
            if (room != null)
            {
                saveSomebodyOutMsg(roomId, room, memberInfos);
            }
            else
            {
                GMClient.getInstance().chatroomManager().getChatroomSpecificationFromServerWithId(roomId,
                    new GMValueCallBack<GMChatRoom>()
                    {
                        @Override
                        public void onSuccess(GMChatRoom room)
                        {
                            saveSomebodyOutMsg(roomId, room, memberInfos);
                        }

                            @Override
                            public void onError(GMError gmError)
                            {
                            }
                        });
            }
            for(GMChatroomAppListener listener : listRoomListeners)
            {
                if (listener != null)
                {
                    listener.onChatRoomChaged();
                }
            }
        }

        private void saveSomebodyOutMsg(long roomId, GMChatRoom room, List<GMChatroomMemberInfo> memberInfos)
        {
            if (room == null || memberInfos == null || memberInfos.size() <= 0)
            {
                return;
            }
            GMChatroomMemberInfo user = memberInfos.get(0);
            ConversationType type = getConversationTypeByRoomType(room.getModeType());
            GMConversation conversation = GMClient.getInstance().chatManager().getConversation(String.valueOf(roomId),
                type, true);
            long currUid = GMClient.getInstance().getCurrentUserId();
            if (conversation != null && currUid == room.getOwner())
            {
                String userName = "\"" + user.getNickname() + "\"";
                String content = "";
                if(memberInfos.size() > 1)
                {
                    //多人，批量处理
                    userName += appContext.getString(R.string.multiple_members, memberInfos.size());
                }
                //其他用户加入
                if (conversation.getType() == ConversationType.CLASSICROOM)
                {
                    //聊天室
                    content = userName + appContext.getString(R.string.chatroom_classic_exit);
                }
                else if (conversation.getType() == ConversationType.CHATROOM)
                {
                    //群聊
                    content = userName + appContext.getString(R.string.chatroom_exit);
                }

                GMMessage msg = GMMessage.createReceiveMessage(currUid, MsgBodyType.TEXT);
                GMTextMessageBody body = new GMTextMessageBody(content);
                msg.setMsgBody(body);
                msg.setChatType(type);
                msg.setFrom(currUid);
                msg.setTo(roomId);
                msg.setRead(true);
                msg.setStatus(MsgStatus.SUCCESSED);

                JsonObject extObj = new JsonObject();
                extObj.addProperty(GMAppConstant.IS_ADMIN_MESSAGE, true);
                extObj.addProperty(GMAppConstant.IS_CHATROOM_NOTIFY, true);
                msg.setExt(extObj.toString());

                conversation.appendMessage(msg);
            }
        }

        @Override
        public void onRemovedFromChatRoom(final long roomId, final List<GMChatroomMemberInfo> memberInfos,
                                          final LeaveChatroomReason reason)
        {
            GMChatRoom room = GMClient.getInstance().chatroomManager().getChatroomInfoFromDB(roomId);
            if (room != null)
            {
                saveSomebodyRemovedMsg(roomId, room, memberInfos, reason);
            }
            else
            {
                GMClient.getInstance().chatroomManager().getChatroomSpecificationFromServerWithId(roomId,
                    new GMValueCallBack<GMChatRoom>()
                    {
                        @Override
                        public void onSuccess(GMChatRoom room)
                        {
                            saveSomebodyRemovedMsg(roomId, room, memberInfos, reason);
                        }

                        @Override
                        public void onError(GMError gmError)
                        {
                        }
                    });
            }
            for(GMChatroomAppListener listener : listRoomListeners)
            {
                if (listener != null)
                {
                    listener.onChatRoomChaged();
                }
            }
        }

        private void saveSomebodyRemovedMsg(long roomId, GMChatRoom room, List<GMChatroomMemberInfo> memberInfos,
                                            LeaveChatroomReason reason)
        {
            if (room == null || memberInfos == null || memberInfos.size() <= 0)
            {
                return;
            }
            GMChatroomMemberInfo user = memberInfos.get(0);
            ConversationType type = getConversationTypeByRoomType(room.getModeType());
            GMConversation conversation = GMClient.getInstance().chatManager().getConversation(String.valueOf(roomId),
                type, true);
            long currUid = GMClient.getInstance().getCurrentUserId();
            if (conversation != null)
            {
                String kicked = "";
                String destroyed = "";
                if (conversation.getType() == ConversationType.CHATROOM)
                {
                    kicked = appContext.getString(R.string.chatroom_be_kicked_you);
                    destroyed = appContext.getString(R.string.chatroom_destroyed);
                }
                else if (conversation.getType() == ConversationType.CLASSICROOM)
                {
                    kicked = appContext.getString(R.string.chatroom_classic_be_kicked_you);
                    destroyed = appContext.getString(R.string.chatroom_classic_destroyed);
                }
                else if (conversation.getType() == ConversationType.CAROLVOICEROOM)
                {
                    //录音设备
                    //content = appContext.getString(R.string.chatroom_welcome);
                    return;
                }
                String content = "";
                if (reason == LeaveChatroomReason.KICKED)
                {
                    //群主显示 xxx被移出群聊
                    if (currUid == room.getOwner())
                    {
                        String userName = user.getNickname();
                        if(memberInfos.size() > 0)
                        {
                            userName += appContext.getString(R.string.multiple_members, memberInfos.size());
                        }
                        if (conversation.getType() == ConversationType.CHATROOM)
                        {
                            content = userName + appContext.getString(R.string.chatroom_be_kicked);
                        }
                        else if (conversation.getType() == ConversationType.CLASSICROOM)
                        {
                            content = userName + appContext.getString(R.string.chatroom_classic_be_kicked);
                        }
                    }
                    else if (user.getUid() == currUid)
                    {
                        //被踢的人是自己，显示 你被移出群聊
                        content = kicked;
                    }
                    else
                    {
                        //别人被踢不提示
                        return;
                    }
                }
                else if (reason == LeaveChatroomReason.DESTROYED)
                {
                    content = destroyed;
                }

                GMMessage msg = GMMessage.createReceiveMessage(currUid, MsgBodyType.TEXT);
                GMTextMessageBody body = new GMTextMessageBody(content);
                msg.setMsgBody(body);
                msg.setChatType(type);
                msg.setFrom(currUid);
                msg.setTo(roomId);
                msg.setStatus(MsgStatus.SUCCESSED);
                JsonObject extObj = new JsonObject();
                extObj.addProperty(GMAppConstant.IS_ADMIN_MESSAGE, true);
                extObj.addProperty(GMAppConstant.IS_CHATROOM_NOTIFY, true);
                msg.setExt(extObj.toString());
                msg.setRead(true);
                conversation.appendMessage(msg);
            }
        }

        @Override
        public void onInvited(GMChatRoom room, GMChatroomMemberInfo inviter, String message)
        {
            for(GMChatroomAppListener listener : listRoomListeners)
            {
                if (listener != null)
                {
                    listener.onChatRoomChaged();
                }
            }
        }

        @Override
        public void onBecomeOwner(GMChatRoom room, GMChatroomMemberInfo memberInfo)
        {
            for(GMChatroomAppListener listener : listRoomListeners)
            {
                if (listener != null)
                {
                    listener.onChatRoomChaged();
                }
            }
        }

        @Override
        public void onAddedToAdmin(GMChatRoom room, GMChatroomMemberInfo memberInfo)
        {
            for(GMChatroomAppListener listener : listRoomListeners)
            {
                if (listener != null)
                {
                    listener.onChatRoomChaged();
                }
            }
        }

        @Override
        public void onRemovedFromAdmin(GMChatRoom room, GMChatroomMemberInfo memberInfo)
        {
            for(GMChatroomAppListener listener : listRoomListeners)
            {
                if (listener != null)
                {
                    listener.onChatRoomChaged();
                }
            }
        }

        @Override
        public void onAddedToBlackList(GMChatRoom room, GMChatroomMemberInfo memberInfo)
        {
            for(GMChatroomAppListener listener : listRoomListeners)
            {
                if (listener != null)
                {
                    listener.onChatRoomChaged();
                }
            }
        }

        @Override
        public void onRemovedFromBlackList(GMChatRoom room, GMChatroomMemberInfo memberInfo)
        {
            for(GMChatroomAppListener listener : listRoomListeners)
            {
                if (listener != null)
                {
                    listener.onChatRoomChaged();
                }
            }
        }

        @Override
        public void onAddedToMuteList(GMChatRoom room, GMChatroomMemberInfo memberInfo, int seconds)
        {
            for(GMChatroomAppListener listener : listRoomListeners)
            {
                if (listener != null)
                {
                    listener.onChatRoomChaged();
                }
            }
        }

        @Override
        public void onRemovedFromMuteList(GMChatRoom room, GMChatroomMemberInfo memberInfo)
        {
            for(GMChatroomAppListener listener : listRoomListeners)
            {
                if (listener != null)
                {
                    listener.onChatRoomChaged();
                }
            }
        }
    }

    private ConversationType getConversationTypeByRoomType(ChatroomMode mode)
    {
        if (mode == ChatroomMode.NORMAL)
        {
            return ConversationType.CHATROOM;
        }
        else if (mode == ChatroomMode.CLASSIC)
        {
            return ConversationType.CLASSICROOM;
        }
        else if( mode==ChatroomMode.CAROLVOICE)
        {
            return ConversationType.CAROLVOICEROOM;
        }
        else
        {
            return ConversationType.CHAT;
        }
    }

    private boolean isOnlySystemMsg(List<GMMessage> messages) {
        if (messages == null || AllOnlineApp.getAppConfig().getPrivate_msg_using_im() == 0) {
            return false;
        }
        boolean bOnlySysMsg = true;
        for (GMMessage message : messages) {
            if (message != null) {
                String conId = message.getConversationId();
                if (conId != null && conId.equals(
                    GMCommonUtils.getConversationIdByUserId(SYSTEM_REPLY_FAV_IM_ID_STR, ConversationType.CHAT)))
                {
                    messages.remove(message);
                    //louis: 这种全局函数更新方式要改变，应该用消息通知的方式
                } else {
                    //还有普通消息，则需要继续处理
                    bOnlySysMsg = false;
                }
            }
        }
        return bOnlySysMsg;
    }

    public void addChatroomListener(GMChatroomAppListener listener)
    {
        if(listRoomListeners == null)
        {
            listRoomListeners = new ArrayList<GMChatroomAppListener>();
        }
        if(!listRoomListeners.contains(listener))
        {
            listRoomListeners.add(listener);
        }
    }

    public void removeChatroomListener(GMChatroomAppListener listener)
    {
        if(listRoomListeners != null && listener != null)
        {
            listRoomListeners.remove(listener);
        }
    }

    public void addGMMessageListener(GMMessageAppListener listener) {
        //this.listener = listener;
        if (listListeners == null) {
            listListeners = new ArrayList<GMMessageAppListener>();
        }
        if (!listListeners.contains(listener)) {
            listListeners.add(listener);
        }
    }

    public void removeGMMessageListener(GMMessageAppListener listener) {
        if (listListeners != null && listener != null) {
            listListeners.remove(listener);
        }
    }

    public GMNotifier getNotifier() {
        if (notifier == null) {
            initNotifier();
        }
        return notifier;
    }

    public boolean hasForegroundActivies() {
        return activityList.size() != 0;
    }

    public void setSettingsProvider(GMSettingsProvider settingsProvider) {
        this.settingsProvider = settingsProvider;
    }

    public GMSettingsProvider getSettingsProvider() {
        return settingsProvider;
    }

    /**
     * check the application process name if process name is not qualified, then
     * we think it is a service process and we will not init SDK
     */
    private String getAppName(int pID) {
        String processName = null;
        ActivityManager am = (ActivityManager) appContext.getSystemService(Context.ACTIVITY_SERVICE);
        List<RunningAppProcessInfo> l = am.getRunningAppProcesses();
        if (l == null) {
            return null;
        }
        Iterator<RunningAppProcessInfo> i = l.iterator();
        if (i == null) {
            return null;
        }
        PackageManager pm = appContext.getPackageManager();
        while (i.hasNext()) {
            ActivityManager.RunningAppProcessInfo info = (ActivityManager.RunningAppProcessInfo) (i.next());
            try {
                if (info.pid == pID) {
                    CharSequence c = pm.getApplicationLabel(
                        pm.getApplicationInfo(info.processName, PackageManager.GET_META_DATA));
                    // Log.d("Process", "Id: "+ info.pid +" ProcessName: "+
                    // info.processName +" Label: "+c.toString());
                    // processName = c.toString();
                    processName = info.processName;
                    return processName;
                }
            } catch (Exception e) {
                // Log.d("Process", "Error>> :"+ e.toString());
            }
        }
        return processName;
    }

    /**
     * Emojicon provider
     */
    public interface GMEmojiconInfoProvider {
        /**
         * return GMEmojicon for input emojiconIdentityCode
         */
        GMEmojicon getEmojiconInfo(String emojiconIdentityCode);

        /**
         * get Emojicon map, key is the text of emoji, value is the resource id
         * or local path of emoji icon(can't be URL on internet)
         */
        Map<String, Object> getTextEmojiconMapping();
    }

    private GMEmojiconInfoProvider emojiconInfoProvider;

    /**
     * Emojicon provider
     */
    public GMEmojiconInfoProvider getEmojiconInfoProvider() {
        return emojiconInfoProvider;
    }

    /**
     * set Emojicon provider
     */
    public void setEmojiconInfoProvider(GMEmojiconInfoProvider emojiconInfoProvider) {
        this.emojiconInfoProvider = emojiconInfoProvider;
    }

    /**
     * new message options provider
     */
    public interface GMSettingsProvider {
        boolean isMsgNotifyAllowed(GMMessage message);

        boolean isMsgSoundAllowed(GMMessage message);

        boolean isMsgVibrateAllowed(GMMessage message);

        boolean isSpeakerOpened();
    }

    /**
     * default settings provider
     */
    protected class DefaultSettingsProvider implements GMSettingsProvider {
        @Override
        public boolean isMsgNotifyAllowed(GMMessage message) {
            return true;
        }

        @Override
        public boolean isMsgSoundAllowed(GMMessage message) {
            return true;
        }

        @Override
        public boolean isMsgVibrateAllowed(GMMessage message) {
            return true;
        }

        @Override
        public boolean isSpeakerOpened() {
            return isSpeakerOpened;
        }
    }

    boolean isSpeakerOpened = true;

    public void setSpeakerOpened(boolean isSpeakerOpened) {
        this.isSpeakerOpened = isSpeakerOpened;
    }

    public Context getContext() {
        return appContext;
    }

    public String[] getImLogPaths(Context context, boolean... bCopyDBParams) {
        File filesDir = context.getFilesDir();
        if (filesDir == null) {
            return null;
        }

        final String IM_FILE_PATH = filesDir.getAbsolutePath();
        final String IM_MSG_DB_PATH = IM_FILE_PATH + File.separator + "MessageDB";//聊天信息DB文件，直接是文件
        final String IM_CONTACTS_DB_PACTH = IM_FILE_PATH + File.separator + "IMClientDB";//联系人DB文件，直接是文件
        final String IM_LOG_DIR_PATH = IM_FILE_PATH + File.separator + "imlog"; //日志文件夹
        boolean bCopyDB = false;
        if (bCopyDBParams != null && bCopyDBParams.length > 0) {
            bCopyDB = bCopyDBParams[0];
        }

        File file = new File(IM_LOG_DIR_PATH);
        if (file != null && file.exists()) {
            File[] files = file.listFiles();
            if (files != null) {
                int iAdd;
                String[] paths;
                if (bCopyDB || AllOnlineApp.getAppConfig().getGoome_im_db_upload() == 1) {
                    //需要上传数据库文件
                    iAdd = 2;
                    paths = new String[files.length + iAdd];
                    paths[0] = IM_MSG_DB_PATH;
                    paths[1] = IM_CONTACTS_DB_PACTH;
                } else {
                    iAdd = 0;
                    paths = new String[files.length];
                }

                for (int i = 0; i < files.length; i++) {
                    paths[i + iAdd] = files[i].getAbsolutePath();
                }
                return paths;
            }
        }
        return null;
    }

    private final int MAX_CUROR = 100;
    private int iCount = 0;

    public void requestGmImDistublist(int imCursor) {
        GMContactManager contactManager = GMClient.getInstance().contactManager();
        if (contactManager != null) {
            contactManager.getDonotDisturbListFromServer(imCursor, MAX_CUROR,
                new GMValueCallBack<GMPageResult<Long>>() {
                    @Override
                    public void onSuccess(GMPageResult<Long> longGMPageResult) {
                        if (longGMPageResult != null && longGMPageResult.getData() != null) {
                            if (longGMPageResult.getData().size() == MAX_CUROR) {
                                //每次获取最大数量，如果返回的数目是最大数目，则可能还没获取完，继续获取
                                iCount += longGMPageResult.getData().size();
                                requestGmImDistublist(iCount);
                            }
                        }
                    }

                    @Override
                    public void onError(GMError gmError) {

                    }
                });
        }
    }

    private Activity activity;

    public void setChatActivity(Activity activity) {
        this.activity = activity;
    }

    public void finishChatActivity()
    {
        if(activity != null)
        {
            activity.finish();
            activity = null;
        }
    }
}
