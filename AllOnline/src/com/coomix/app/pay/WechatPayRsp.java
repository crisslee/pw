package com.coomix.app.pay;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

/**
 * Created by ssl on 2017/1/21.
 */
public class WechatPayRsp implements Serializable {
    private static final long serialVersionUID = 1L;

    private String appid; //“goome_tech_coomix”微信开放平台审核通过的应用APPID
    private String partnerid; // “1900000109”, 	微信支付分配的商户号
    private String prepayid; // “WX1217752501201407033233368018”  微信返回的支付交易会话ID
    @SerializedName("package")
    private String packages;//  “Sign=WXPay”, 扩展字段
    private String noncestr; //"5K8264ILTKCH16CQ2502SI8ZNMTM67VS", 随机字符串
    private String timestamp;// “1412000000”,  // 时间戳
    private String sign;  //“C380BEC2BFD727A4B6845133519F3AD6”  签名

    public String getAppid() {
        return appid;
    }

    public void setAppid(String appid) {
        this.appid = appid;
    }

    public String getPartnerid() {
        return partnerid;
    }

    public void setPartnerid(String partnered) {
        this.partnerid = partnered;
    }

    public String getPrepayid() {
        return prepayid;
    }

    public void setPrepayid(String prepayid) {
        this.prepayid = prepayid;
    }

    public String getPackages() {
        return packages;
    }

    public void setPackages(String packages) {
        this.packages = packages;
    }

    public String getNoncestr() {
        return noncestr;
    }

    public void setNoncestr(String noncestr) {
        this.noncestr = noncestr;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getSign() {
        return sign;
    }

    public void setSign(String sign) {
        this.sign = sign;
    }
}
