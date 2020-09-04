/************************************************************
 *  * Hyphenate CONFIDENTIAL 
 * __________________ 
 * Copyright (C) 2016 Hyphenate Inc. All rights reserved. 
 *  
 * NOTICE: All information contained herein is, and remains 
 * the property of Hyphenate Inc.
 * Dissemination of this information or reproduction of this material 
 * is strictly forbidden unless prior written permission is obtained
 * from Hyphenate Inc.
 */
package com.goomeim.model;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Vibrator;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;
import android.util.Log;
import com.coomix.app.all.AllOnlineApp;
import com.coomix.app.all.Constant;
import com.coomix.app.all.R;
import com.coomix.app.all.service.NotificationActivityManager;
import com.coomix.app.framework.util.PreferenceUtil;
import com.coomix.app.all.util.CommunityUtil;
import com.coomix.app.all.util.ConfigUtil;
import com.coomix.app.all.util.Utils;
import com.goomeim.GMAppConstant;
import com.goomeim.controller.GMImManager;
import com.goomeim.utils.GMCommonUtils;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import net.goome.im.chat.GMClient;
import net.goome.im.chat.GMConstant.ConversationType;
import net.goome.im.chat.GMMessage;
import org.json.JSONObject;

/**
 * new message notifier class
 * 
 * this class is subject to be inherited and implement the relative APIs
 */
public class GMNotifier {
    private final static String TAG = "notify";
    Ringtone ringtone = null;

    // start notification id
    protected static int notifyID           = 0525;
    protected static int foregroundNotifyID = 0555;

    protected NotificationManager notificationManager = null;

    protected HashSet<Long> fromUsers       = new HashSet<Long>();
    protected int             notificationNum = 0;

    protected Context appContext;
    protected String packageName;
    protected String[]     msgs;
    protected long         lastNotifiyTime;
    protected AudioManager audioManager;
    protected Vibrator vibrator;

    protected EaseNotificationInfoProvider notificationInfoProvider;

    public GMNotifier()
    {
    }

    /**
     * this function can be override
     *
     * @param context
     * @return
     */
    public GMNotifier init(Context context)
    {
        if(context == null)
        {
            context = AllOnlineApp.mApp;
        }
        appContext = context;
        notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        packageName = appContext.getApplicationInfo().packageName;
        msgs = context.getResources().getStringArray(R.array.message_notifys);

        audioManager = (AudioManager) appContext.getSystemService(Context.AUDIO_SERVICE);
        vibrator = (Vibrator) appContext.getSystemService(Context.VIBRATOR_SERVICE);

        return this;
    }

    /**
     * this function can be override
     */
    public void reset()
    {
        resetNotificationCount();
        cancelNotificaton();
    }

    void resetNotificationCount()
    {
        notificationNum = 0;
        PreferenceUtil.commitInt(Constant.NOTIFICATION_UNREAD_HYPHENATE, 0);
        fromUsers.clear();
    }

    void cancelNotificaton()
    {
        if (notificationManager != null)
        {
            notificationManager.cancel(notifyID);
        }
    }

    /**
     * handle the new message this function can be override
     *
     * @param message
     */
    public synchronized void onNewMsg(GMMessage message)
    {
        if (!isNotify(message))
        {
            return;
        }
        List<GMMessage> messages = new ArrayList<GMMessage>();
        messages.add(message);
        GMImManager.GMSettingsProvider settingsProvider = GMImManager.getInstance().getSettingsProvider();
        if (!settingsProvider.isMsgNotifyAllowed(message))
        {
            return;
        }

        // check if app running background
        if (CommunityUtil.isBackground(appContext))
        {
            Log.d(TAG, "app is running in backgroud");
            sendNotification(message, false);
        }
        else
        {
            sendNotification(message, true);
        }

        vibrateAndPlayTone(message);
    }

    public synchronized void onNewMsg(List<GMMessage> messages)
    {
        if (messages == null || messages.size() <= 0)
        {
            return;
        }
        int size = messages.size();
        int i = 0;
        do
        {
            if (!isNotify(messages.get(i)))
            {
                // 移除所有的静默消息和免打扰消息
                messages.remove(i);
                size--;
            }
            else
            {
                i++;
            }
        } while (i < size);
        if (messages.size() <= 0)
        {
            return;
        }

        // check if app running background
        if (CommunityUtil.isBackground(appContext))
        {
            Log.d(TAG, "app is running in backgroud");
            sendNotification(messages, false);
        }
        else
        {
            sendNotification(messages, true);
        }
        GMMessage lastMessage = messages.get(messages.size() - 1);
        vibrateAndPlayTone(lastMessage);
    }

    /**
     * send it to notification bar This can be override by subclass to provide
     * customer implementation
     *
     * @param messages
     * @param isForeground
     */
    protected void sendNotification(List<GMMessage> messages, boolean isForeground)
    {
        if(messages == null || messages.size() <= 0)
        {
            return;
        }
        for (GMMessage message : messages)
        {
            if (!isForeground)
            {
                notificationNum++;
                fromUsers.add(message.getFrom());
            }
        }
        sendNotification(messages.get(messages.size() - 1), isForeground, false);
    }

    protected void sendNotification(GMMessage message, boolean isForeground)
    {
        sendNotification(message, isForeground, true);
    }

    /**
     * send it to notification bar This can be override by subclass to provide
     * customer implementation
     *
     * @param message
     */
    NotificationActivityManager notiActManager = new NotificationActivityManager();
    protected void sendNotification(GMMessage message, boolean isForeground, boolean numIncrease)
    {
        if(message == null)
        {
            return;
        }
        try
        {
            Utils.createChannel(notificationManager, Constant.channelIdMsg, Constant.channelNameMsg);
            String nickName = GMCommonUtils.getMessageExtString(message, GMAppConstant.GOOME_NICKNAME, "");
            if(TextUtils.isEmpty(nickName))
            {
                nickName = String.valueOf(message.getFrom());
            }

            String notifyText = GMCommonUtils.getMessageDigest(message,  appContext, nickName);
            PackageManager packageManager = appContext.getPackageManager();

            // notification title
            String contentTitle = (String) packageManager.getApplicationLabel(appContext.getApplicationInfo());
            if (notificationInfoProvider != null)
            {
                String customNotifyText = notificationInfoProvider.getDisplayedText(message);
                String customCotentTitle = notificationInfoProvider.getTitle(message);
                if (customNotifyText != null)
                {
                    notifyText = customNotifyText;
                }

                if (customCotentTitle != null)
                {
                    contentTitle = customCotentTitle;
                }
            }

            int drawableId = R.drawable.ic_notify;
//            if (android.os.Build.VERSION.SDK_INT >= 21)
//            {
//                // 5.0以上版本要求仅支持alpha通道，白色
//                drawableId = R.drawable.ic_notify_sdk21;
//            }
            // create and send notificaiton
            Bitmap bitmap = BitmapFactory.decodeResource(appContext.getResources(), R.drawable.ic_launcher);
            NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(appContext, Constant.channelIdMsg)
                .setSmallIcon(drawableId)
                .setLargeIcon(bitmap)
                .setWhen(System.currentTimeMillis())
                .setAutoCancel(true);

            int iDir = NotificationActivityManager.TO_MY_MESSAGE;
            String groupOrUserId = String.valueOf(message.getFrom());
            if(message.getChatType() != ConversationType.CHAT)
            {
                iDir = NotificationActivityManager.TO_GROUP_CHAT;
                groupOrUserId = String.valueOf(message.getTo());
            }
            Intent notiIntent = notiActManager.getDirectionActivityIntent(appContext, iDir,
                    nickName, groupOrUserId, GMCommonUtils.getMessageExtString(message, GMAppConstant.GOOME_AVATAR, ""));

            PendingIntent pendingIntent = PendingIntent.getActivity(appContext, (int) System.currentTimeMillis(), notiIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT);

            if (numIncrease)
            {
                // prepare latest event info section
                if (!isForeground)
                {
                    notificationNum++;
                    fromUsers.add(message.getFrom());
                }
            }

            int fromUsersNum = fromUsers.size();
            if (fromUsersNum <= 0 || notificationNum <= 0)
            {
                return;
            }

            if(message.getChatType() != ConversationType.CHAT)
            {
                String pushMsg = null;
                boolean bAtMsg = false;
                if(GMAtMessageHelper.get().isAtAllMessage(message))
                {
                    bAtMsg = true;
                    try
                    {
                        JSONObject jsonObject = new JSONObject(message.getExt());
                        if(jsonObject.has(GMAppConstant.GOOME_AT_ALL_NOTIFY_MSG))
                        {
                            pushMsg = jsonObject.getString(GMAppConstant.GOOME_AT_ALL_NOTIFY_MSG);
                        }
                    }
                    catch (Exception e)
                    {

                    }
                }
                else if(GMAtMessageHelper.get().isAtMeMessage(message))
                {
                    bAtMsg = true;
                    try
                    {
                        String name = GMCommonUtils.getMessageExtString(message, GMAppConstant.GOOME_NICKNAME, "");
                        if(!TextUtils.isEmpty(name))
                        {
                            pushMsg = String.format(appContext.getString(R.string.push_group_at_all), name);
                        }
                    }
                    catch (Exception e)
                    {

                    }
                }

                if(bAtMsg)
                {
                    if(TextUtils.isEmpty(pushMsg))
                    {
                        notifyText = appContext.getString(R.string.at_me) + notifyText;
                    }
                    else
                    {
                        notifyText = pushMsg;
                    }
                }
            }

            PreferenceUtil.commitInt(Constant.NOTIFICATION_UNREAD_HYPHENATE, notificationNum);
            int totalUnreadNum = notificationNum;

            mBuilder.setContentTitle(contentTitle);
            mBuilder.setTicker(notifyText);
            mBuilder.setContentText(notifyText);
            mBuilder.setContentIntent(pendingIntent);
            Notification notification = mBuilder.build();

            if (isForeground)
            {
                notificationManager.notify(foregroundNotifyID, notification);
                notificationManager.cancel(foregroundNotifyID);
            }
            else
            {
                //BadgeUtil.sendBadgeNotification(notification, notifyID, appContext, notificationNum, totalUnreadNum);
            }

        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    /**
     * 判断哪些消息需要通知栏提醒
     */
    private boolean isNotify(GMMessage message) {
        try {
            if (message != null) {
                if (GMAtMessageHelper.get().isAtAllMessage(message)) {
                    return true;
                }
                if ((!GMCommonUtils.getMessageExtBoolean(message, "em_ignore_notification", false))) {
                    if (message.getChatType() == ConversationType.CHAT) {
                        //私聊
                        return false;
                    } else if (message.getChatType() == ConversationType.CHATROOM && (PreferenceUtil.getBoolean(
                        GMAppConstant.GROUP_MSG_UNNOTIFY + GMClient.getInstance().getCurrentUserId()
                            + message.getTo(), false) || !ConfigUtil.isUseGMIMGroup())) {
                        //群聊
                        return false;
                    } else if (message.getChatType() == ConversationType.CLASSICROOM) {
                        //聊天室不推送，不显示通知栏
                        return false;
                    }
                    return true;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * vibrate and play tone
     */
    public void vibrateAndPlayTone(GMMessage message)
    {
        if (message == null || !isNotify(message))
        {
            return;
        }

        if (System.currentTimeMillis() - lastNotifiyTime < 1000)
        {
            // received new messages within 2 seconds, skip play ringtone
            return;
        }

        try
        {
            lastNotifiyTime = System.currentTimeMillis();

            // check if in silent mode
            if (audioManager.getRingerMode() == AudioManager.RINGER_MODE_SILENT)
            {
                Log.e(TAG, "in slient mode now");
                return;
            }
            GMImManager.GMSettingsProvider settingsProvider = GMImManager.getInstance().getSettingsProvider();
            if (settingsProvider.isMsgVibrateAllowed(message))
            {
                long[] pattern = new long[]{0, 180, 80, 120};
                vibrator.vibrate(pattern, -1);
            }

            if (settingsProvider.isMsgSoundAllowed(message))
            {
                if (ringtone == null)
                {
                    Uri notificationUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

                    ringtone = RingtoneManager.getRingtone(appContext, notificationUri);
                    if (ringtone == null)
                    {
                        Log.d(TAG, "cant find ringtone at:" + notificationUri.getPath());
                        return;
                    }
                }

                if (!ringtone.isPlaying())
                {
                    String vendor = Build.MANUFACTURER;

                    ringtone.play();
                    // for samsung S3, we meet a bug that the phone will
                    // continue ringtone without stop
                    // so add below special handler to stop it after 3s if
                    // needed
                    if (vendor != null && vendor.toLowerCase().contains("samsung"))
                    {
                        Thread ctlThread = new Thread()
                        {
                            public void run()
                            {
                                try
                                {
                                    Thread.sleep(3000);
                                    if (ringtone.isPlaying())
                                    {
                                        ringtone.stop();
                                    }
                                }
                                catch (Exception e)
                                {
                                }
                            }
                        };
                        ctlThread.run();
                    }
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    /**
     * set notification info Provider
     *
     * @param provider
     */
    public void setNotificationInfoProvider(EaseNotificationInfoProvider provider)
    {
        notificationInfoProvider = provider;
    }

    public interface EaseNotificationInfoProvider
    {
        /**
         * set the notification content, such as
         * "you received a new image from xxx"
         *
         * @param message
         * @return null-will use the default text
         */
        String getDisplayedText(GMMessage message);

        /**
         * set the notification content: such as
         * "you received 5 message from 2 contacts"
         *
         * @param message
         * @param fromUsersNum- number of message sender
         * @param messageNum    -number of messages
         * @return null-will use the default text
         */
        String getLatestText(GMMessage message, int fromUsersNum, int messageNum);

        /**
         * 设置notification标题
         *
         * @param message
         * @return null- will use the default text
         */
        String getTitle(GMMessage message);

        /**
         * set the small icon
         *
         * @param message
         * @return 0- will use the default icon
         */
        int getSmallIcon(GMMessage message);

        /**
         * set the intent when notification is pressed
         *
         * @param message
         * @return null- will use the default icon
         */
        Intent getLaunchIntent(GMMessage message);
    }
}
