package com.coomix.app.all.model.bean;

import java.io.Serializable;

/**
 * File Description:
 *
 * @author felixqiu
 * @since 2019/3/12.
 */
public class CardInfo implements Serializable {
    private static final long serialVersionUID = 796989241730868557L;
    //卡号
    public String msisdn;
    public String iccid;
    public String imsi;
    public String expdate;
    //生效时间
    public String effdate;
    //运营商 1深圳移动
    public int isp;
    //2 未激活 3 正在使用
    public int status;
}
