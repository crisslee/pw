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
package com.goomeim;

public class GMAppConstant
{
	public static final String MESSAGE_ATTR_IS_VOICE_CALL = "is_voice_call";
	public static final String MESSAGE_ATTR_IS_VIDEO_CALL = "is_video_call";

	public static final String MESSAGE_ATTR_IS_BIG_EXPRESSION = "em_is_big_expression";
	public static final String MESSAGE_ATTR_EXPRESSION_ID = "em_expression_id";

	//public static final String MESSAGE_ATTR_AT_MSG = "em_at_list";
	//public static final String MESSAGE_ATTR_VALUE_AT_MSG_ALL = "ALL";
	/**
	 * 拓展消息--消息中携带的用户名
	 */
	public static final String GOOME_NICKNAME = "goome.ext.nickname";
	/**
	 * 拓展消息--消息中携带的用户头像url
	 */
	public static final String GOOME_AVATAR = "goome.ext.imageurl";
	/**
	 * 拓展消息--消息中携带的用户认证类型
	 */
	public static final String GOOME_VTYPE = "goome.ext.vtype";
	/**
	 * 拓展消息--消息中携带的@的用户名字列表，jsonarray
	 */
	public static final String GOOME_AT_LIST = "goome.ext.at.list";
	/**
	 * 拓展消息--消息中携带@ALL
	 */
	public static final String GOOME_AT_ALL = "goome.ext.all";
	/**
	 * 拓展消息--@ALL的时候需要给iPhone携带必须强制显示推送--boolean
	 */
	public static final String GOOME_FORCE_NOTIFICATION = "em_force_notification";
	/**
	 * 拓展消息--@ALL的时候携带一个json做推送用
	 */
	public static final String GOOME_AT_ALL_NOTIFY= "em_apns_ext";
	/**
	 * 拓展消息--@ALL的时候携带信息：xxx@了你--json的key参照GOOME_AT_ALL_NOTIFY
	 */
	public static final String GOOME_AT_ALL_NOTIFY_MSG = "em_push_title";
	/**
	 * 拓展消息--@ALL
	 */
	public static final String GOOME_AT_ALL_MSG = "all";

//	public static final int CHATTYPE_SINGLE = 1;
//	public static final int CHATTYPE_GROUP = 2;
//	public static final int CHATTYPE_CHATROOM = 3;

	public static final String EXTRA_CHAT_TYPE = "chatType";
	public static final String EXTRA_USER_ID = "user_id";
	/** 消息免打扰+userid+groupid */
	public static final String GROUP_MSG_UNNOTIFY = "group_msg_unnotify";

    public static final String GET_CHATROOM_PUSH_SERVICE = "get_push_service";

	/***消息撤回（revoke）——需要的信息关键字**/
	public static final String REVOKE_FLAG = "REVOKE_FLAG";
	public static final String REVOKE_MESSAGE_ID = "goome.ext.revoke.msgid";
	public static final String REVOKE_CMD_TO_TEXT = "goome.ext.revoke.cmd2text";

	/**********红包信息，携带的扩展字段信息**********/
	/**发送红包---红包类型：1、普通红包 2、随机红包  3、裂变红包 4、位置红包。  @link RedPacketConstant.java 中定义的value */
	public static final String REDPACKET_KEY_TYPE = "caronline.ext.redpacket.type";
	/**发送红包---红包的id*/
	public static final String REDPACKET_KEY_ID = "caronline.ext.redpacket.id";
	/**发送红包---红包的title--祝福语*/
	public static final String REDPACKET_KEY_TITLE = "caronline.ext.redpacket.title";

	/**领取红包--发送透传消息--红包透传消息的flag*/
	public static final String REDPACKET_FLAG = "REDPACKET_FLAG";
	/**领取红包--发送透传消息红包主人的uid*/
	public static final String REDPACKET_KEY_HOST_UID = "caronline.ext.redpacket.hostuid";
	/**领取红包--发送透传消息红包主人的昵称*/
	public static final String REDPACKET_KEY_HOST_NAME = "caronline.ext.redpacket.hostname";
	/**领取红包--发送透传消息红包是否被领取完*/
	public static final String REDPACKET_KEY_STATUS = "caronline.ext.redpacket.status";
	/**领取红包--发送透传消息红包ID*/
	public static final String REDPACKET_KEY_UNPACK_ID = "caronline.ext.redpacket.unpackid";

	/***list的key***/
	public static final String GM_EXT_HYPER_LINK_LIST = "goome.ext.hyperlink.list";
	/***超链接昵称***/
	public static final String GM_EXT_HYPER_LINK_NICKNAME = "goome.ext.hyperlink.nickname";
	/****超链接uid******/
	public static final String GM_EXT_HYPER_LINK_UID = "goome.ext.hyperlink.uid";
	/*超链接跳转类型 0 跳转私聊，1跳转个人主页*/
	public static final String GM_EXT_HYPER_LINK_JUMP_TYPE = "goome.ext.hyperlink.jump.type";
	public static final int GM_EXT_JUMP_TO_CHAT = 0;
	public static final int GM_EXT_JUMP_TO_PERSONNAL_PAGE = 1;

	/**发送透传消息--给小酷的flag*/
	public static final String ALPHAKU_FLAG = "ALPHAKU_FLAG";

	/**************用户通知展示--例如加退群、拉黑********/
	public static final String IS_ADMIN_MESSAGE="is_admin";
	public static final String IS_CHATROOM_NOTIFY="is_chatroom_notify";

	/**正在输入--发送透传消息--flag*/
	public static final String INPUTING_FLAG = "INPUTING_FLAG";

	/**
	 * 接受open voice 的cmd 消息
	 */
	public static final String CAROL_VOICE_OPEN_FLAG = "CAROL_VOICE_OPEN_FLAG";

	/**************4.10.1版本开始弃用********START******/
	public static final String KEY_IN_BLACKLIST = "goome.ext.in.blacklist";
	public static final String MESSAGE_TYPE="msgtype";
	public static final String MESSAGE_ACOUNT="account";
	public static final String MESSAGE_NICKNAME="nickname";
	public static final String MESSAGE_CONTENT="content";
	public static final String MSGTYPE_SOMEBODY_IN = "somebody_in";
	public static final String MSGTYPE_SOMEBODY_OUT = "somebody_out";
	public static final String MSGTYPE_SHOWCONTENT = "showcontent";
	/**************4.10.1版本开始弃用********END******/

	public static final String GOOME_VOICE_LISTENED = "goome.ext.listened";

	public static final String k_strVoiceOpenFlag = "CAROL_VOICE_OPEN_FLAG";
	public static final String k_strVoiceCloseFlag = "CAROL_VOICE_CLOSE_FLAG";
}
