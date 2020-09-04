package com.goomeim.controller;

import net.goome.im.chat.GMChatRoom;
import net.goome.im.chat.GMChatroomMemberInfo;
import net.goome.im.chat.GMConstant.LeaveChatroomReason;

/**
 * File Description:
 *
 * @author felixqiu
 * @since 2017/8/15.
 */

public interface GMChatroomAppListener
{
//    void onMemberJoined(long chatroomId, GMChatroomMemberInfo user);
//
//    void onMemberExited(long chatroomId, GMChatroomMemberInfo user);
//
//    void onRemovedFromChatRoom(long chatroomId, GMChatroomMemberInfo user, LeaveChatroomReason reason);
//
//    void onInvited(GMChatRoom room, GMChatroomMemberInfo inviter, String message);
//
//    void onBecomeOwner(GMChatRoom room, GMChatroomMemberInfo operator);
//
//    void onAddedToAdmin(GMChatRoom room, GMChatroomMemberInfo operator);
//
//    void onRemovedFromAdmin(GMChatRoom room, GMChatroomMemberInfo operator);
//
//    void onAddedToBlackList(GMChatRoom room, GMChatroomMemberInfo user);
//
//    void onRemovedFromBlackList(GMChatRoom room, GMChatroomMemberInfo user);
//
//    void onAddedToMuteList(GMChatRoom room, GMChatroomMemberInfo user, int muteSeconds);
//
//    void onRemovedFromMuteList(GMChatRoom room, GMChatroomMemberInfo user);
    void onChatRoomChaged();
}
