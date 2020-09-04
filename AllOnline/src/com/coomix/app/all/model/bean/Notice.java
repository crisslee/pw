package com.coomix.app.all.model.bean;

import java.io.Serializable;

/**
 * File Description:
 *
 * @author felixqiu
 * @since 2019-10-18.
 */
public class Notice implements Serializable {

    private static final long serialVersionUID = -5635272328441344707L;
    //0 平台到期 1 物联卡到期 2 平台/物联卡 3 URL
    public int id;
    public String lang;
    public String content;
    public String platform;
    public int level_eid;
    public int publisher;
    public String begin_time;
    public String end_time;
    public String create_time;
    public String url;
}
