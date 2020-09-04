package com.coomix.app.all.model.bean;

import java.io.Serializable;

public class EmUser implements Serializable {
    private static final long serialVersionUID = -1743756722790446357L;
    private String hxuser;
    private String hxpwd;
    private long order_id;
    private String chatroom_id;

    public String getHxuser() {
        return hxuser;
    }

    public void setHxuser(String hxuser) {
        this.hxuser = hxuser;
    }

    public String getHxpwd() {
        return hxpwd;
    }

    public void setHxpwd(String hxpwd) {
        this.hxpwd = hxpwd;
    }

    public long getOrder_id() {
        return order_id;
    }

    public void setOrder_id(long order_id) {
        this.order_id = order_id;
    }

    public String getChatroom_id() {
        return chatroom_id;
    }

    public void setChatroom_id(String chatroom_id) {
        this.chatroom_id = chatroom_id;
    }
}
