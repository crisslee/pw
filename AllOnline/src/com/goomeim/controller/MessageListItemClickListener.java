package com.goomeim.controller;

import net.goome.im.chat.GMMessage;

/**
 * Created by think
 *
 * @since 2017/6/12.
 */

public interface MessageListItemClickListener
{
    void onResendClick(GMMessage message);

    /**
     * there is default handling when bubble is clicked, if you want handle
     * it, return true another way is you implement in onBubbleClick() of
     * chat row
     */
    boolean onBubbleClick(GMMessage message);

    void onBubbleLongClick(GMMessage message);

    void onUserAvatarClick(String username);

    void onUserAvatarLongClick(GMMessage message, long uid);

    void onTextAutoLinkClick(GMMessage message, String data, int type);
}
