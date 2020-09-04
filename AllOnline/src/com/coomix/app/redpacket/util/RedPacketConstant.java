package com.coomix.app.redpacket.util;

/**
 * Created by ssl
 *
 * @since 2017/2/14.
 */
public class RedPacketConstant {
    /**
     * 红包领取的用户展示，每页多少个数据
     */
    public static final int REDPACKET_USER_PER_PAGE = 15;

    /**
     * 普通红包--均包
     */
    public static final int REDPACKET_TYPE_NORMAL = 1;
    /**
     * 拼手气红包
     */
    public static final int REDPACKET_TYPE_RANDOM = 2;
    /**
     * 裂变红包
     */
    public static final int REDPACKET_TYPE_FISSION = 3;

    /**********位置红包(无位置红包的概念，社区红包就是位置红包)**********/
    //public static final int REDPACKET_TYPE_LOCATION = 4;

    /**
     * 公司红包
     */
    public static final int REDPACKET_TYPE_COMPANY = 5;

    /**
     * 每日红包--启动时app弹出
     */
    public static final int REDPACKET_TYPE_DAILY = 6;

    /********************************************************************************/
    /**
     * 所有红包
     */
    public static final int RP_DISPLAY_ALL = 0;
    /**
     * 群红包
     */
    public static final int RP_DISPLAY_CHAT_GROUP = 1;
    /**
     * 社区红包--帖子
     */
    public static final int RP_DISPLAY_COMMUNITY_TOPIC = 2;
    /**
     * 私信红包
     */
    public static final int RP_DISPLAY_CHAT_SINGLE = 3;
    /**
     * 社区红包--评论
     */
    public static final int RP_DISPLAY_COMMUNITY_REPLY = 4;

    /**
     * 拆开、领取红包的广播
     */
    public static final String OPEN_REDPACKET_ACTION = "com.coomix.car.app.redpacket.OPEN_REDPACKET_ACTION";
    /**
     * 发送红包的广播
     */
    public static final String REDPACKET_WILL_SEND_ACTION = "com.coomix.car.app.redpacket.REDPACKET_PAYED_ACTION";

    public static final String LONGIN_TWO_DEVICES_CONFIRM_ACTION = "com.coomix.car.app.TWO_DEVICES_CONFIRM_ACTION";

    public static final String REDPACKET_DATA = "redpacket_data";

    public static final int ERROR_REDPACKET_OUT_OF_RANGE = 10003;

    public static final String RP_LAST_PAY_PLATFORM = "rp_last_pay_platform";
}
