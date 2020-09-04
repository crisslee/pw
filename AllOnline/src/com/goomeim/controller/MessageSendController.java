package com.goomeim.controller;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.Gravity;
import android.widget.Toast;
import com.coomix.app.all.AllOnlineApp;
import com.coomix.app.all.Constant;
import com.coomix.app.all.R;
import com.coomix.app.all.dialog.ProgressDialogEx;
import com.google.gson.JsonObject;
import com.goomeim.GMAppConstant;
import com.goomeim.model.GMAtMessageHelper;
import com.goomeim.utils.GMCommonUtils;
import java.io.File;
import java.util.List;
import net.goome.im.GMCallBack;
import net.goome.im.GMError;
import net.goome.im.chat.GMChatRoom;
import net.goome.im.chat.GMClient;
import net.goome.im.chat.GMConstant;
import net.goome.im.chat.GMMessage;

/**
 * Created by think on 2017/6/13.
 */

public class MessageSendController
{
    private Context mContext = null;
    private GMConstant.ConversationType chatType;
    private long toChatUserId;
    private ProgressDialogEx progressDialogEx;
    private OnMessageRefreshListener refreshListener;
    private Handler mHandler = new Handler(Looper.getMainLooper());

    public MessageSendController(Context context, long toChatUserId, GMConstant.ConversationType chatType)
    {
        this.mContext = context;
        this.toChatUserId = toChatUserId;
        this.chatType = chatType;
    }
    
    public void setToChatUserId(long toChatUserId)
    {
        this.toChatUserId = toChatUserId;
    }

    public void setChatType(GMConstant.ConversationType chatType)
    {
        this.chatType = chatType;
    }
    
    public void setMessageRefreshListener(OnMessageRefreshListener refreshListener)
    {
        this.refreshListener = refreshListener;
    }

    public void sendTextMessage(String content)
    {
        if(!GMClient.getInstance().isInited() || content == null || TextUtils.isEmpty(content.trim()))
        {
            Toast.makeText(mContext, R.string.null_message, Toast.LENGTH_SHORT).show();
            return;
        }
        if (GMAtMessageHelper.get().containsAtUsername(content))
        {
            sendAtMessage(content);
        }
        else
        {
            GMMessage message = GMMessage.createTxtSendMessage(content, toChatUserId);
            sendMessage(message, null);
        }
    }

    /**
     * send @ message, only support group chat message
     */
    public void sendAtMessage(String content)
    {
        if (!GMClient.getInstance().isInited())
        {
            return;
        }
        GMMessage message = GMMessage.createTxtSendMessage(content, toChatUserId);
        if (TextUtils.isEmpty(content))
        {
            return;
        }
        GMChatRoom room = GMClient.getInstance().chatroomManager().getChatroomInfoFromDB(toChatUserId);
        long curUser = GMClient.getInstance().getCurrentUserId();
        JsonObject jsonObject = new JsonObject();
        if ((Constant.IS_DEBUG_MODE || (curUser != -1 && room != null && room.getOwner() != -1
            && curUser == room.getOwner())) && GMAtMessageHelper.get().containsAtAll(content, mContext))
        {
            jsonObject.addProperty(GMAppConstant.GOOME_AT_ALL, GMAppConstant.GOOME_AT_ALL_MSG);
            //for iPhone.iphone强制1推送显示@all的信息
            jsonObject.addProperty(GMAppConstant.GOOME_FORCE_NOTIFICATION, true);
            try
            {
                jsonObject.addProperty(GMAppConstant.GOOME_AT_ALL_NOTIFY_MSG, String
                    .format(mContext.getString(R.string.push_group_at_all), AllOnlineApp.sToken.name)); //xxx@了你
            }
            catch (Exception e)
            {

            }
            // jsonObject.addProperty(GMAppConstant.GOOME_AT_ALL_NOTIFY, jsonObject);
        }
        else if (content.contains("@"))
        {
            List<Long> atList = GMAtMessageHelper.get().getAtMessageUserIds(content);
            if (atList != null)
            {
                jsonObject.add(GMAppConstant.GOOME_AT_LIST, GMAtMessageHelper.get().atListToJsonArray(atList));
            }
        }
        sendMessage(message, jsonObject);
    }

    public void sendBigExpressionMessage(String name, String identityCode)
    {
        if(!GMClient.getInstance().isInited())
        {
            return;
        }
        GMMessage message = GMCommonUtils.createExpressionMessage(String.valueOf(toChatUserId), name, identityCode);
        sendMessage(message, null);
    }

    public void sendVoiceMessage(String filePath, int length)
    {
        if(!GMClient.getInstance().isInited())
        {
            return;
        }
        GMMessage message = GMMessage.createVoiceSendMessage(filePath, length, toChatUserId);
        sendMessage(message, null);
    }

    public void sendImageMessage(String imagePath)
    {
        if(!GMClient.getInstance().isInited())
        {
            return;
        }
        GMMessage message = GMMessage.createImageSendMessage(imagePath, toChatUserId);
        sendMessage(message, null);
    }

    /***********发送红包信息***********/
    public void sendRedPacketMessage(int redPacketType, String redpacket_id, String title)
    {
        if(!GMClient.getInstance().isInited())
        {
            return;
        }
        // send redpacket message
        String getRPMsg = AllOnlineApp.getAppConfig().getYou_get_redpacket_message();
        if(TextUtils.isEmpty(getRPMsg))
        {
            getRPMsg = mContext.getString(R.string.redpacket_version_too_low);
        }
        GMMessage message = GMMessage.createCustomSendMessage(getRPMsg, toChatUserId);
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty(GMAppConstant.REDPACKET_KEY_TYPE, redPacketType);
        jsonObject.addProperty(GMAppConstant.REDPACKET_KEY_ID, redpacket_id);
        jsonObject.addProperty(GMAppConstant.REDPACKET_KEY_TITLE, title);
        sendMessage(message, jsonObject);
    }

    public void sendLocationMessage(double latitude, double longitude, String locationAddress)
    {
        if(!GMClient.getInstance().isInited())
        {
            return;
        }
        GMMessage message = GMMessage.createLocationSendMessage(longitude, latitude, locationAddress, toChatUserId);
        sendMessage(message, null);
    }

    public void sendVideoMessage(String videoPath, String thumbPath, int videoLength)
    {
        if(!GMClient.getInstance().isInited())
        {
            return;
        }
        GMMessage message = GMMessage.createVideoSendMessage(videoPath, thumbPath, videoLength, toChatUserId);
        sendMessage(message, null);
    }

    public void sendFileMessage(String filePath)
    {
        if(!GMClient.getInstance().isInited())
        {
            return;
        }
        GMMessage message = GMMessage.createFileSendMessage(filePath, toChatUserId);
        sendMessage(message, null);
    }

    private long lastErrorTime = 0;
    private final Byte[] lock = new Byte[0];
    public void sendMessage(GMMessage message, JsonObject jsonObject)
    {
        if (message == null || !GMClient.getInstance().isInited())
        {
            return;
        }
        message.setChatType(chatType);
        addJsonUserInfos(message, jsonObject);
        message.setStatusCallback(new GMCallBack()
        {
            @Override
            public void onSuccess()
            {

            }

            @Override
            public void onError(GMError gmError)
            {
                if(gmError != null && gmError.errCode() == GMError.ERROR_USER_IN_BLACKLIST)
                {
                    synchronized (lock)
                    {
                        if(System.currentTimeMillis() - lastErrorTime > 600)
                        {
                            lastErrorTime = System.currentTimeMillis();
                            GMCommonUtils.saveAsInBlackMessage(toChatUserId, chatType, mContext.getString(R.string.chat_in_blacklist));
                            if (refreshListener != null)
                            {
                                refreshListener.onRefreshAndSelectLast();
                            }
                        }
                    }
                }
            }

            @Override
            public void onProgress(int i, String s)
            {

            }
        });
        // send message
        GMClient.getInstance().chatManager().sendMessage(message);
        // refresh ui
        if (refreshListener != null)
        {
            refreshListener.onRefreshAndSelectLast();
        }
        GMAtMessageHelper.get().cleanToAtUserList();
    }

    /***********发送撤回提醒的信息***********/
    private void sendRevokeCMDMessage(final long revokeServerMsgId)
    {
        if(!GMClient.getInstance().isInited())
        {
            return;
        }
        //createSendMessage(toChatUserId, GMConstant.MsgBodyType.CMD);
        final GMMessage cmdMsg = GMMessage.createCmdSendMessage(GMAppConstant.REVOKE_FLAG, toChatUserId);
        // 如果是群聊，设置chattype，默认是单聊
        cmdMsg.setChatType(chatType);
        // 通过扩展字段添加要撤回消息的id
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty(GMAppConstant.REVOKE_MESSAGE_ID, revokeServerMsgId);

        addJsonUserInfos(cmdMsg, jsonObject);

        showWaitInfo(mContext.getString(R.string.wait_revoke_message));

        cmdMsg.setStatusCallback(new GMCallBack()
        {
            @Override
            public void onSuccess()
            {
                GMCommonUtils.saveRevokeCMDMessage(revokeServerMsgId, String.valueOf(toChatUserId), cmdMsg);
                mHandler.post(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        dismissWait();
                    }
                });
                if(refreshListener != null)
                {
                    refreshListener.onRefresh();
                }
            }

            @Override
            public void onError(GMError gmError)
            {
                mHandler.post(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        dismissWait();
                        Toast.makeText(mContext, R.string.revoke_message_fail, Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onProgress(int i, String s)
            {

            }
        });

        GMClient.getInstance().chatManager().sendMessage(cmdMsg);
    }

    /***********发送领取了红包的提醒信息***********/
    public void sendRedpacketCMDMessage(final String redpacketHostUid, final String redpacketHostName, final String redpacket_id, int status)
    {
        if(!GMClient.getInstance().isInited())
        {
            return;
        }
        final GMMessage cmdMsg = GMMessage.createCmdSendMessage(GMAppConstant.REDPACKET_FLAG, toChatUserId);
        // 如果是群聊，设置chattype，默认是单聊
        cmdMsg.setChatType(chatType);
        // 通过扩展字段添加要撤回消息的id
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty(GMAppConstant.REDPACKET_KEY_HOST_UID, redpacketHostUid);
        jsonObject.addProperty(GMAppConstant.REDPACKET_KEY_HOST_NAME, redpacketHostName);
        jsonObject.addProperty(GMAppConstant.REDPACKET_KEY_UNPACK_ID, redpacket_id);
        jsonObject.addProperty(GMAppConstant.REDPACKET_KEY_STATUS, status);

        addJsonUserInfos(cmdMsg, jsonObject);

        if(redpacketHostUid != null && redpacketHostUid.equals(String.valueOf(AllOnlineApp.sToken.community_id)))
        {
            //如果是自己领取自己的红包就不发消息了，直接存储到本地---因为环信的并发透传消息有问题
            GMCommonUtils.saveRedpacketCMDMessage(cmdMsg, redpacketHostUid, redpacket_id,chatType);
            if(refreshListener != null)
            {
                refreshListener.onRefreshAndSelectLast();
            }
            return;
        }

        cmdMsg.setStatusCallback(new GMCallBack()
        {
            @Override
            public void onSuccess()
            {
                GMCommonUtils.saveRedpacketCMDMessage(cmdMsg, redpacketHostUid, redpacket_id, chatType);
            }

            @Override
            public void onError(GMError gmError)
            {

            }

            @Override
            public void onProgress(int i, String s)
            {

            }
        });

        GMClient.getInstance().chatManager().sendMessage(cmdMsg);
    }

    /***发给小酷的透传消息ALPHAKU_FLAG***/
    public void sendAlphaCMDMessage()
    {
        if(!GMClient.getInstance().isInited())
        {
            return;
        }
        final GMMessage cmdMsg = GMMessage.createCmdSendMessage(GMAppConstant.ALPHAKU_FLAG, toChatUserId);
        // 如果是群聊，设置chattype，默认是单聊
        cmdMsg.setChatType(chatType);

        addJsonUserInfos(cmdMsg, null);

        GMClient.getInstance().chatManager().sendMessage(cmdMsg);
    }

    public void resendMessage(GMMessage message)
    {
        if(!GMClient.getInstance().isInited())
        {
            return;
        }
        message.setStatus(GMConstant.MsgStatus.PENDING);
        GMClient.getInstance().chatManager().resendMessage(message);
        if(refreshListener != null)
        {
            refreshListener.onRefresh();
        }
    }

    public void addJsonUserInfos(GMMessage message, JsonObject jsonObject)
    {
        if(message == null)
        {
            return;
        }
        //发送消息的时候带上扩展消息--用户的昵称和头像
        if(jsonObject == null)
        {
            jsonObject = new JsonObject();
        }
        jsonObject.addProperty(GMAppConstant.GOOME_AVATAR, "");
        jsonObject.addProperty(GMAppConstant.GOOME_NICKNAME, AllOnlineApp.sToken.name);
        jsonObject.addProperty(GMAppConstant.GOOME_VTYPE, 0);
        message.setExt(jsonObject.toString());
    }

    /**
     * send image
     *
     * @param selectedImage
     */
    public void sendPicByUri(Uri selectedImage)
    {
        String[] filePathColumn = { MediaStore.Images.Media.DATA };
        Cursor cursor = mContext.getContentResolver().query(selectedImage, filePathColumn, null, null, null);
        if (cursor != null)
        {
            cursor.moveToFirst();
            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            String picturePath = cursor.getString(columnIndex);
            cursor.close();
            cursor = null;

            if (picturePath == null || picturePath.equals("null"))
            {
                Toast toast = Toast.makeText(mContext, R.string.cant_find_pictures, Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.CENTER, 0, 0);
                toast.show();
                return;
            }
            sendImageMessage(picturePath);
        }
        else
        {
            File file = new File(selectedImage.getPath());
            if (!file.exists())
            {
                Toast toast = Toast.makeText(mContext, R.string.cant_find_pictures, Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.CENTER, 0, 0);
                toast.show();
                return;

            }
            sendImageMessage(file.getAbsolutePath());
        }

    }

    /**
     * send file
     *
     * @param uri
     */
    public void sendFileByUri(Uri uri)
    {
        String filePath = null;
        if ("content".equalsIgnoreCase(uri.getScheme()))
        {
            String[] filePathColumn = { MediaStore.Images.Media.DATA };
            Cursor cursor = null;

            try
            {
                cursor = mContext.getContentResolver().query(uri, filePathColumn, null, null, null);
                int column_index = cursor.getColumnIndexOrThrow("_data");
                if (cursor.moveToFirst())
                {
                    filePath = cursor.getString(column_index);
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
        else if ("file".equalsIgnoreCase(uri.getScheme()))
        {
            filePath = uri.getPath();
        }
        File file = new File(filePath);
        if (file == null || !file.exists())
        {
            Toast.makeText(mContext, R.string.File_does_not_exist, Toast.LENGTH_SHORT).show();
            return;
        }
        // limit the size < 10M
        if (file.length() > 10 * 1024 * 1024)
        {
            Toast.makeText(mContext, R.string.The_file_is_not_greater_than_10_m, Toast.LENGTH_SHORT).show();
            return;
        }
        sendFileMessage(filePath);
    }

    public void revokeMessage(GMMessage message)
    {
        if(!GMClient.getInstance().isInited())
        {
            return;
        }
        if(message == null)
        {
            Toast.makeText(mContext, R.string.revoke_message_fail, Toast.LENGTH_SHORT).show();
            return;
        }
        sendRevokeCMDMessage(message.getSvrMsgId());
    }

    public void sendInputingCMDMessage()
    {
        if(!GMClient.getInstance().isInited())
        {
            return;
        }
        final GMMessage cmdMsg = GMMessage.createCmdSendMessage(GMAppConstant.INPUTING_FLAG, toChatUserId);
        cmdMsg.setChatType(chatType);

        addJsonUserInfos(cmdMsg, null);

        GMClient.getInstance().chatManager().sendMessage(cmdMsg);
    }

    public void showWaitInfo(String msg)
    {
        if(progressDialogEx == null)
        {
            progressDialogEx = new ProgressDialogEx(mContext);
            progressDialogEx.setAutoDismiss(true);
            progressDialogEx.setDuration(30000);
        }

        try
        {
            progressDialogEx.show(msg);
        }
        catch (Exception e)
        {

        }
    }

    public void dismissWait()
    {
        if(progressDialogEx != null && progressDialogEx.isShowing())
        {
            progressDialogEx.dismiss();
        }
    }
    
    public interface OnMessageRefreshListener
    {
        public void onRefresh();
        public void onRefreshAndSelectLast();
    }
}
