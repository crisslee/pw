package com.coomix.app.all.model.bean;

import java.io.Serializable;

public class Token implements Serializable {
    private static final long serialVersionUID = 2956933980897314392L;

    /**
     * 用户类型：
     * 公交公司=2， 公交线路=3， 经销商=4， 有汽修厂功能帐户=5 租赁=6， 物流=7， 用户=8, 4s店=9  部门=10
     */
    public static final int BUS_COMPANY = 2; //公交公司(经销商备注   只能看不能改)
    public static final int BUS_LINE = 3; //公交线路(经销商备注  只能看不能改)
    public static final int DISTRIBUTOR = 4; //经销商(经销商备注    能看能修改)
    public static final int REPAIR_FUNCTION = 5; //有汽修厂功能帐户(经销商备注   只能看不能改)
    public static final int LEASE = 6; //租赁(设备备注    能看能修改)
    public static final int LOGISTICS = 7; //物流(设备备注    能看能修改)
    public static final int NORMAL = 8; //用户(设备备注   能看能修改)
    public static final int SHOP4S = 9; //4s店(经销商备注 只能看不能改)
    public static final int DEPARTMENT = 10; //部门(经销商备注     能看能修改)

    public String access_token;
    public String ticket;
    public long expires_in;
    public String name;
    //后端新返回的账户  用于获取数据   QQ当前的时候
    public String account;
    //登录类型：0账号登录，1验证码登录，2imei登录，3微信登录
    public int loginType;
    public int usertype;
    public String verify_phone;
    public long community_id;

    @Override
    public String toString() {
        return "Token{" +
            "access_token='" + access_token + '\'' +
            ", ticket='" + ticket + '\'' +
            ", expires_in=" + expires_in +
            ", name='" + name + '\'' +
            ", account='" + account + '\'' +
            ", loginType='" + loginType + '\'' +
            ", usertype=" + usertype +
            ", verify_phone='" + verify_phone + '\'' +
            ", community_id=" + community_id +
            '}';
    }
}
