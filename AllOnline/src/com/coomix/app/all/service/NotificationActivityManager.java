package com.coomix.app.all.service;

import android.content.Context;
import android.content.Intent;

/**
 * Created by ssl
 *
 * @since 2017/08/22.
 */
public class NotificationActivityManager {
    private static final String TAG = NotificationActivityManager.class.getSimpleName();
    public static final String DESTINATION = "dest";
    public static final int TO_MY_MESSAGE = 1;
    public static final int TO_COMMENT_LIST = 2;
    public static final int TO_GROUP_CHAT = 3;
    public static final int TO_NEARBY_TOPIC = 4;

    public static final String GM_MESSAGE_NAME = "gm_message_name";
    public static final String GM_MESSAGE_CHATID = "gm_message_chatid";//私聊时候是对方的uid，群聊时候是群id
    public static final String GM_MESSAGE_IMG = "gm_message_img";

    public Intent getDirectionActivityIntent(Context context, int iDirection, String userName, String groupOrUserId,
        String userAvatorUrl) {
        Intent intent = null;
        // TODO: 2017/9/7 通知部分稍后处理
        // if (MainActivity.isAlive)
        // {
        //     switch (iDirection)
        //     {
        //         case TO_MY_MESSAGE:
        //             if(AllOnlineApp.getAppConfig().getPrivate_msg_using_im() == 1)
        //             {
        //                 User user = new User();
        //                 user.setUid(groupOrUserId);
        //                 user.setImg(userAvatorUrl);
        //                 user.setName(userName);
        //                 intent = new Intent(context, GMChatActivity.class);
        //                 intent.putExtra(Constant.USER_DATA, user);
        //             }
        //             else
        //             {
        //                 intent = new Intent(context, MyMessageActivity.class);
        //             }
        //             break;
        //
        //         case TO_COMMENT_LIST:
        //             intent = new Intent(context, CommentListActivity.class);
        //             intent.putExtra(CommentListActivity.Intent_ISNEWCOMMENT, true);
        //             break;
        //
        //         case TO_GROUP_CHAT:
        //             if(ConfigUtil.isUseGMIMGroup())
        //             {
        //                 intent = new Intent(context, GMChatActivity.class);
        //                 intent.putExtra(GMAppConstant.EXTRA_CHAT_TYPE, GMConstant.ConversationType.CHATROOM);
        //                 intent.putExtra(GMAppConstant.EXTRA_USER_ID, groupOrUserId);
        //             }
        //             else
        //             {
        //                 intent = new Intent(context, EmChatActivity.class);
        //                 intent.putExtra(EaseConstant.EXTRA_USER_ID, groupOrUserId);
        //             }
        //             break;
        //
        //         case TO_NEARBY_TOPIC:
        //             //跳到主界面然后在主界面处理
        //             intent = new Intent(context, MainActivity.class);
        //             intent.putExtra(DESTINATION, iDirection);
        //             break;
        //
        //     }
        //
        //     if(intent != null)
        //     {
        //         intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        //     }
        // }
        // else
        // {
        //     intent = context.getPackageManager().getLaunchIntentForPackage("com.coomix.app.bus");
        //     intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
        //     intent.putExtra(DESTINATION, iDirection);
        //     intent.putExtra(GM_MESSAGE_CHATID, groupOrUserId);
        //     intent.putExtra(GM_MESSAGE_IMG, userAvatorUrl);
        //     intent.putExtra(GM_MESSAGE_NAME, userName);
        // }

        return intent;
    }
}
