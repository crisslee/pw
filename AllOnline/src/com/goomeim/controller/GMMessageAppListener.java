package com.goomeim.controller;

import net.goome.im.chat.GMMessage;

import java.util.List;

public interface GMMessageAppListener
{
	/** 是否已处理消息 */
	public boolean onMessageReceived(List<GMMessage> messages);

	/** 是否已处理消息 */
	public boolean onMessageRead(List<GMMessage> messages);

	/** 是否已处理消息 */
	public boolean onMessageDelivered(List<GMMessage> messages);

	/** 是否已处理消息 */
	public boolean onMessageChanged(GMMessage message, Object change);

	/** 是否已处理消息 */
	public boolean onCmdMessageReceived(List<GMMessage> messages, boolean bInputing);
}