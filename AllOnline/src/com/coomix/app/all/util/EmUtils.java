package com.coomix.app.all.util;

import com.coomix.app.framework.util.PreferenceUtil;
import com.goomeim.GMAppConstant;
import java.util.List;
import net.goome.im.chat.GMClient;
import net.goome.im.chat.GMConstant.ConversationType;
import net.goome.im.chat.GMConversation;

public class EmUtils {
    /**
     * 获取未读群消息，
     *
     * @param list 聊天会话列表
     * @return int[2]  {未设置免打扰的未读消息, 总的未读消息}
     */
    public static int[] loadGMIMGroupUnreadMsgsCount(List<GMConversation> list) {
        int[] resultInt = { 0, 0 };
        if (list == null || list.size() <= 0) {
            return resultInt;
        }
        int size = list.size();
        int tmpCount;
        for (int i = 0; i < size; i++) {
            GMConversation conversation = list.get(i);
            if (conversation != null && (conversation.getType() == ConversationType.CHATROOM)) {
                tmpCount = conversation.getUnreadMsgCount();
                if (!PreferenceUtil.getBoolean(GMAppConstant.GROUP_MSG_UNNOTIFY +
                    GMClient.getInstance().getCurrentUserId() + conversation.conversationId(), false)) {
                    // 群组没有设置免打扰
                    resultInt[0] += tmpCount;
                }
                resultInt[1] += tmpCount;
                if (resultInt[0] > 99) {
                    break;
                }
            }
        }
        return resultInt;
    }
}
