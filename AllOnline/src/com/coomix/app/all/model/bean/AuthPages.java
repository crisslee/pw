package com.coomix.app.all.model.bean;

/**
 * 用于权限控制
 * 对应接口getAuthPages <br/>
 * 1001001设备信息 1001002设备销售转移 1001003用户管理 1001004消息管理 1001005报警信息
 * 1001006监控设备分组 1001007围栏设置 1002爱车安高级管理 1003子账号管理 1004下发指令 1005批量指令下发,
 * 1001008 平台续费
 */
public class AuthPages {
    /**
     * 1001001设备信息 1001002设备销售转移 1001003用户管理 1001004消息管理 1001005报警信息
     * 1001006监控设备分组 1001007围栏设置 1002爱车安高级管理 1003子账号管理 1004下发指令 1005批量指令下发
     */
    private String pages;

    private String accountid;

    @Override
    public String toString() {
        return "AuthPages [pages=" + pages + ", accountid=" + accountid + "]";
    }

    public void setPages(String pages) {
        this.pages = pages;
    }

    public String getPages() {
        return pages;
    }

    public void setAccountid(String accountid) {
        this.accountid = accountid;
    }

    public String getAccountid() {
        return accountid;
    }

    public boolean hasSendCommandAuth() {
        if(getPages() == null) {
            return false;
        }
        for(String page : getPages().split(",")) {
            if("1004".equals(page)) {
                // 含有1004下发指令权限
                return true;
            }
        }
        return false;
    }

    public boolean haPlatrechargeAuth() {
        if(getPages() == null) {
            return false;
        }
        for(String page : getPages().split(",")) {
            if("1001008".equals(page)) {
                return true;
            }
        }
        return false;
    }
}
