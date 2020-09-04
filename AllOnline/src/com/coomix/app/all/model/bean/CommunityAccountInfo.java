package com.coomix.app.all.model.bean;

/**
 * Created by herry on 2016/12/20.
 */
public class CommunityAccountInfo {
    private String account;
    private String nickName;
    private String gender;
    private String signature;
    private String iconUrl;

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }

    public String getIconUrl() {
        return iconUrl;
    }

    public void setIconUrl(String iconUrl) {
        this.iconUrl = iconUrl;
    }

    @Override
    public String toString() {
        return "CommunityAccountInfo{" +
            "account='" + account + '\'' +
            ", nickName='" + nickName + '\'' +
            ", gender='" + gender + '\'' +
            ", signature='" + signature + '\'' +
            ", iconUrl='" + iconUrl + '\'' +
            '}';
    }
}
