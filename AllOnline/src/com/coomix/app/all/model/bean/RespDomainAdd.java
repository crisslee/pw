package com.coomix.app.all.model.bean;

import com.coomix.app.all.model.response.RespBase;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class RespDomainAdd extends RespBase implements Serializable{

    private static final long serialVersionUID = -1949671320417921921L;

    @SerializedName("dy")
    public String domainMain;
    @SerializedName("web")
    public String domainWeb;
    @SerializedName("ad")
    public String domainAd;
    @SerializedName("gps")
    public String domainGps;
    @SerializedName("log")
    public String domainLog;
    @SerializedName("cfg")
    public String domainCfg;
    @SerializedName("gapi")
    public String domainGapi;
    public String domainCard;
    @SerializedName("https")
    public boolean httpsFlag;
    @SerializedName("bip")
    public String backupIp;
    @SerializedName("timestamp")
    public long timestamp;

}
