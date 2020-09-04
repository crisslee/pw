package com.coomix.app.all.model.event;

/**
 * Created by ly on 2018/1/4 17:45.
 */
public class LoginCommunityEvent {
    private boolean loginSucc = false;

    public LoginCommunityEvent(boolean loginSucc) {
        this.loginSucc = loginSucc;
    }

    public boolean isLoginSucc() {
        return loginSucc;
    }

    public void setLoginSucc(boolean loginSucc) {
        this.loginSucc = loginSucc;
    }
}
