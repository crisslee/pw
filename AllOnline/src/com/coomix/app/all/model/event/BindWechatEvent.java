package com.coomix.app.all.model.event;

import com.tencent.mm.opensdk.modelmsg.SendAuth;

/**
 * Created by ly on 2017/5/22.
 */
public class BindWechatEvent {
    public static final String BIND_WEICHAT_EVENT_STATE = "BIND_WEICHAT_EVENT_STATE";

    public int actionCode = 1; //bind weichat

    public SendAuth.Resp getResponse() {
        return response;
    }

    public void setResponse(SendAuth.Resp response) {
        this.response = response;
    }

    SendAuth.Resp response;
    public BindWechatEvent(SendAuth.Resp resp) {
        response = resp;
    }
}
