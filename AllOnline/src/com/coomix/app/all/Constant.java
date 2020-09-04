package com.coomix.app.all;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.content.Context;
import com.coomix.app.all.model.bean.Adver;
import java.util.ArrayList;
import java.util.List;

public class Constant {
    //这个控制很多调试信息的输出。在发布的时候请务必设为false
    public static final boolean IS_DEBUG_MODE = false;

    public static final String IS_STARTED_FROM_BOOT = "IS_STARTED_FROM_BOOT";

    /** 汽车在线社区默认citycode */
    public static final String COMMUNITY_CITYCODE = "869998";//"862108";//
    /** 运营人员登陆后,AllOnlineApp.sACCOUNT == INNER_ACCOUNT+CommunityUser.getUid()*/
    public static final String INNER_ACCOUNT = "谷米coomix运营人员account";

    /**
     * 登录手机号
     */
    public static final String PREFERENCE_USER_PHONE = "user_phone";
    /**
     * 登录用户信息
     */
    public static final String PREFERENCE_USER_INFO  = "user_info";
    /**
     * 此账户具有上报安装信息的功能
     */
    public static final String ACCOUNT_AN_ZHUANG_GONG  = "安装工";

    /**
     * 目前使用的adtype，AdTypeHome（首页banner）AdTypeWindow(首页弹窗)
     */
    public static final int AD_TYPE_LAUNCH               = 1;
    public static final int AD_TYPE_HOME                 = 2;
    public static final int AD_TYPE_TRANSFER             = 3;
    public static final int AD_TYPE_WINDOW               = 4;
    public static final int AD_TYPE_COMMUNITY_BUTTON_MIN = 5;
    public static final int AD_TYPE_COMMUNITY_BUTTON_MAX = 9;
    public static final int AD_TYPE_GAME                 = 10;
    public static final int AD_TYPE_COMMUNITY_MIN        = 100;
    public static final int AD_TYPE_COMMUNITY_MAX        = 110;

    public static final int HTTP_TIMEOUT = 15000;
    public static final int SOCKET_TIMEOUT = 15000;
    
    public static final int UPDATE_INTERVAL_GPS = 1000;
    public static final int UPDATE_INTERVAL_NETWORK = 10000;

    public static final String MULTI_PROCESS_PROPERTY_FILE = "multi_process_property_file";
    public final static String SECRET_KEY = "goome886644";
    public final static String PRIVATE_KEY = "l1pPukuVJikaU5ge";
    public final static String PRIVATE_PACKAGE = "bus.coomix.com";

    //1.8.4new.gpsoo.net
    public static final String firstUrl = "http://www.gpsoo.net/current_time.txt";
    public static final String helpFeedbackUrl = "http://www.gpsoo.net/webapp/help/index.shtml";   
    public static final String helpUrl = "http://www.gpsoo.net/webapp/help/index.shtml";   
    public static final String newIndicationUrl = "http://gps.goocar.net";
    public static final String weizhangUrl = "http://www.gpsoo.net/tool/wzcx.html";
    public static final String instructionUrl = "http://www.gpsoo.net/help/help_car_app.html";
    /**
     * 软件服务条款声明url
     */
    public static final String REDPACKET_HELP_URL = "https://bussem.gpsoo.net/coomix-redpacket-help/index.html";

    //汽车在线活动须知url
    public static final String DISCLAIMER_URL = "https://bussem.gpsoo.net/goocar-statement/index.html";
    /**
     * 免责声明 url
     */
    public static final String STATEMENT_URL = "http://lite.gmiot.net/wx/disclaimer.html";

    public static final String DOMAIN_ADDRESS_FIRST = "lite.gmiot.net";
    // ip
    public static final String[] DOMAIN_ADDRESS_FIRST_IPS     = {"120.77.46.49", "47.92.131.128"};
    public static final String DOMAIN_ADDRESS_AD = "ad.gpsoo.net";

    /**
     * 汽车在线的网络环境配置，生成环境 "carolhome.gpsoo.net" 测试换："test-carolhome.gpsoo.net";
     */
    public static final String DOMAIN_ADDRESS_COMMUNITY = "carolhome.gpsoo.net";

    public static final String DOMAIN_ADDRESS_NATIVEACTIVITY = "carolcomm.gpsoo.net";
    public static final String DOMAIN_ADDRESS_COMMUNITY_PICUP = "picup.gpsoo.net";
    public static final String DOMAIN_ADDRESS_COMMUNITY_TEST = "test-carolhome.gpsoo.net";
    public static final String DOMAIN_ADDRESS_USERINFO = "gcomm.gpsoo.net";

    public static final String SERVER_HOST_IP = "121.40.39.214";
    
    public static final int COOMIX_APP_ID = 2001;
    
    public static final int LIMITCARNUM = 200;
    public static final int POI_LEVEL = 15;
    public static final int CLUSTER_LEVEL = 15;
    public static final int MAP_MOVE_DISTANCE = 200;

    public final static String GPNS_MSG = "gnps_msg"; // 传递推送消息内容的key键
    public final static String GPNS_MSG_RECEV = "pushMsg"; // 传递推送消息内容的key键
    public final static String EXTRA_NOTIFICATION_ID = "messageID"; // 传递推送消息内容的key键

    public static final String NEW_VERSION_TAG = "app1.2";
    public static final double DEFAULT_LAT = 22.53786;
    public static final double DEFAULT_LNG=113.957369;

    public static final int CM = 1;
    public static final int UN = 2;
    public static final int CT = 3;
    public static final int OTHER = 4;

    public static final int NUMBER_OF_API_BASE = 1000;
    public static final int FM_APIID_ACCESS_TOKEN = 0x00000001 + Constant.NUMBER_OF_API_BASE;
    public static final int FM_APIID_DEVICE_INFO = 0x00000002 + Constant.NUMBER_OF_API_BASE;
    public static final int FM_APIID_TRACKING = 0x00000003 + Constant.NUMBER_OF_API_BASE;
    public static final int FM_APIID_HISTROY = 0x00000004 + Constant.NUMBER_OF_API_BASE;
    public static final int FM_APIID_MONITOR = 0x00000005 + Constant.NUMBER_OF_API_BASE;
    public static final int FM_APIID_REVERSE_GEO = 0x00000006 + Constant.NUMBER_OF_API_BASE;
    public static final int FM_APIID_ADJUST_LATLNG = 0x00000007 + Constant.NUMBER_OF_API_BASE;
    public static final int FM_APIID_QUERY_FENCE = 0x00000008 + Constant.NUMBER_OF_API_BASE;
    public static final int FM_APIID_ADD_FENCE = 0x00000009 + Constant.NUMBER_OF_API_BASE;
    public static final int FM_APIID_MODIFY_PWD = 0x0000000A + Constant.NUMBER_OF_API_BASE;
    public static final int FM_APIID_GEI_ALARMS = 0x0000000B + Constant.NUMBER_OF_API_BASE;
    public static final int FM_APIID_GEI_COMMAND = 0x0000000C + Constant.NUMBER_OF_API_BASE;
    public static final int FM_APIID_GEI_COMMAND_RESP = 0x0000000D + Constant.NUMBER_OF_API_BASE;
    public static final int FM_APIID_SET_COMMAND = 0x0000000E + Constant.NUMBER_OF_API_BASE;
    public static final int FM_APIID_SWITCH_FENCE = 0x0000000F + Constant.NUMBER_OF_API_BASE;
    public static final int FM_APIID_SET_ALARM_PHONE = 0x00000010 + Constant.NUMBER_OF_API_BASE;
    public static final int FM_APIID_SET_ALARM_READ = 0x00000011 + Constant.NUMBER_OF_API_BASE;
    public static final int FM_APIID_GET_ALARM_SETTING = 0x00000012 + Constant.NUMBER_OF_API_BASE;
    public static final int FM_APIID_SET_ALARM_SETTING = 0x00000013 + Constant.NUMBER_OF_API_BASE;

    public static final int MODIFUUSER = 0x00000014 + Constant.NUMBER_OF_API_BASE;

    public static final int QQBIND_WEB = 0x00000015 + Constant.NUMBER_OF_API_BASE;
    public static final int QQLOGIN_WEB = 0x00000016 + Constant.NUMBER_OF_API_BASE;
    public static final int experienceQQUser = 0x00000017 + Constant.NUMBER_OF_API_BASE;

    // forget the password
    public static final int FORGET_BIND_PHONE_SMS = 0x00000018 + Constant.NUMBER_OF_API_BASE;
    public static final int FORGETPW_BIND = 0x00000019 + Constant.NUMBER_OF_API_BASE;
    public static final int FORGETPW_VALIDATE = 0x00000020 + Constant.NUMBER_OF_API_BASE;
    public static final int FORGETPW_RESET = 0x00000021 + Constant.NUMBER_OF_API_BASE;

    // get the information based on rect
    public static final int FM_APIID_RECTSEARCH = 0x00000022 + Constant.NUMBER_OF_API_BASE;

    public static final int UNQQBIND_WEB = 0x00000033 + Constant.FORGETPW_RESET;

    public static final int FM_APIID_PROVINCE_LIST = 0x00000041 + Constant.NUMBER_OF_API_BASE;
    public static final int FM_APIID_CITY_LIST = 0x00000042 + Constant.NUMBER_OF_API_BASE;
    public static final int FM_APIID_GET_OVERSPEED = 0x00000043 + Constant.NUMBER_OF_API_BASE;
    public static final int FM_APIID_SET_OVERSPEED = 0x00000044 + Constant.NUMBER_OF_API_BASE;
    public static final int FM_APIID_GET_AREAFENCE = 0x00000045 + Constant.NUMBER_OF_API_BASE;
    public static final int FM_APIID_SET_AREAFENCE = 0x00000046 + Constant.NUMBER_OF_API_BASE;
    public static final int FM_APIID_SWITCH_AREAFENCE = 0x00000047 + Constant.NUMBER_OF_API_BASE;
    public static final int FM_APIID_QUERY_DEVICESETTING = 0x00000048 + Constant.NUMBER_OF_API_BASE;
    public static final int FM_APIID_GET_AUTH_PAGES = 0x00000049 + Constant.NUMBER_OF_API_BASE;
    public static final int FM_APIID_UPLOAD_LOCATION = 0x00000100 + Constant.NUMBER_OF_API_BASE;

    public static final int FM_APIID_GET_AD = 0x00000200 + Constant.NUMBER_OF_API_BASE;
    public static final int FM_APIID_DOWNLOAD_IMG = 0x00000201 + Constant.NUMBER_OF_API_BASE;
    public static final int FM_APIID_UPLOAD_AD_CLICK = 0x00000202 + Constant.NUMBER_OF_API_BASE;

    public static final int FM_APIID_BLACKLIST = 0x00000300 + Constant.NUMBER_OF_API_BASE;
    
    /** 搜索车辆列表 */
    public static final int FM_APIID_DEV_SEARCH_SEARCH = 0x00000341 + Constant.NUMBER_OF_API_BASE;

    /** 刷新所有车辆列表 */
    public static final int FM_APIID_FILTER_DEVINFO = 0x00000332 + Constant.NUMBER_OF_API_BASE;

    /** 刷新单个车 */
    public static final int FM_APIID_DEV_SINGLE = 0x00000333 + Constant.NUMBER_OF_API_BASE;

    /** 刷新子账号 */
    public static final int FM_APIID_DEV_SUB = 0x00000353 + Constant.NUMBER_OF_API_BASE;

    /** 版本升级 */
    public static final int FM_APIID_LATEST_VERSION = 0x00000400 + Constant.NUMBER_OF_API_BASE;
    public static final int FM_APIID_FIRST_HOST = 0x00000500 + Constant.NUMBER_OF_API_BASE;

    public static final int FM_APIID_PICTURE_UPLOAD = 0x00000501 + Constant.NUMBER_OF_API_BASE;
    public static final int FM_APIID_LOG_UPLOAD = 0x00000502 + Constant.NUMBER_OF_API_BASE;
    public static final int FM_APIID_LOG_GETCONFIG = 0x00000503 + Constant.NUMBER_OF_API_BASE;

    public static final int FM_APIID_UPLOAD_PERFORMANCE = 0x00000504 + Constant.NUMBER_OF_API_BASE;

    /*********
     * 社区相关接口
     *********/
    /** 获取未读评论与赞/消息 数目 */
    public static final int FM_APIID_userGetNotifyCnt = 0x00000600 + Constant.NUMBER_OF_API_BASE;
    /** 修改用户信息 */
    public static final int FM_APIID_userModify = 0x00000601 + Constant.NUMBER_OF_API_BASE;
    /** 获取用户信息 */
    public static final int FM_APIID_userDetailInfo = 0x00000602 + Constant.NUMBER_OF_API_BASE;
    /** 点赞/取消点赞 */
    public static final int FM_APIID_userGiveFlower = 0x00000603 + Constant.NUMBER_OF_API_BASE;
    /** 获取用户帖子列表 */
    public static final int FM_APIID_topicGetUserTopicList = 0x00000604 + Constant.NUMBER_OF_API_BASE;
    /** 获取评论和赞了列表 */
    public static final int FM_APIID_userGetCommentList = 0x00000605 + Constant.NUMBER_OF_API_BASE;
    /** 获取点赞用户列表 */
    public static final int FM_APIID_userGetPraiseUser = 0x00000606 + Constant.NUMBER_OF_API_BASE;
    /** 获取帖子列表---后台自动拼接列表，例如：推荐贴+热门帖 */
    public static final int FM_APIID_getTopicListByAuto = 0x00000607 + Constant.NUMBER_OF_API_BASE;
    /** 发帖 */
    public static final int FM_APIID_topicAddTopic = 0x00000608 + Constant.NUMBER_OF_API_BASE;
    /** 删除帖子 */
    public static final int FM_APIID_topicDeleteTopic = 0x00000609 + Constant.NUMBER_OF_API_BASE;
    /** 获取帖子详情 */
    public static final int FM_APIID_topicGetDetail = 0x00000610 + Constant.NUMBER_OF_API_BASE;
    /** 获取指定帖子的评论 */
    public static final int FM_APIID_topicGetReply = 0x00000611 + Constant.NUMBER_OF_API_BASE;
    /** 根据板块获取帖子 */
    public static final int FM_APIID_topicGetSectionTopicList = 0x00000612 + Constant.NUMBER_OF_API_BASE;
    /** 获取板块精华帖 */
    public static final int FM_APIID_topicGetClassicTopicList = 0x00000613 + Constant.NUMBER_OF_API_BASE;
    /** 获取广场帖 */
    public static final int FM_APIID_topicGetCityTopic = 0x00000614 + Constant.NUMBER_OF_API_BASE;
    /** 获取板块列表 */
    public static final int FM_APIID_sectionGetSectionList = 0x00000615 + Constant.NUMBER_OF_API_BASE;
    /** 获取板块详情 */
    public static final int FM_APIID_sectionGetDetail = 0x00000616 + Constant.NUMBER_OF_API_BASE;
    /** 图片上传 */
    public static final int FM_APIID_pictureUpload = 0x00000617 + Constant.NUMBER_OF_API_BASE;
    /** 获取活动列表 */
    public static final int FM_APIID_getActivityList = 0x00000618 + Constant.NUMBER_OF_API_BASE;
    /** 获取活动详情 */
    public static final int FM_APIID_getActivityDetail = 0x00000619 + Constant.NUMBER_OF_API_BASE;
    /** 活动报名 */
    public static final int FM_APIID_sendSignInfo = 0x00000620 + Constant.NUMBER_OF_API_BASE;
    /** 获取我的报名的活动信息 */
    public static final int FM_APIID_getMySignedInfo = 0x00000621 + Constant.NUMBER_OF_API_BASE;
    /** 获取所有广告 */
    public static final int FM_APIID_getAllAd = 0x00000622 + Constant.NUMBER_OF_API_BASE;
    /** 登陆社区 */
    public static final int FM_APIID_login = 0x00000623 + Constant.NUMBER_OF_API_BASE;
    /** 绑定gpns cid */
    public static final int FM_APIID_bindChannelId = 0x00000624 + Constant.NUMBER_OF_API_BASE;
    /** 运营账号登陆 */
    public static final int FM_APIID_loginInner = 0x00000625 + Constant.NUMBER_OF_API_BASE;
    /** 退出登录 */
    public static final int FM_APIID_logout = 0x00000626 + Constant.NUMBER_OF_API_BASE;
    /**获取报警列表*/
    public static final int FM_APIID_alarmCategoryList = 0x00000627 + Constant.NUMBER_OF_API_BASE;
    /**获取报警列表*/
    public static final int FM_APIID_alarmCategoryItemList = 0x00000628 + Constant.NUMBER_OF_API_BASE;
    /**获取随机码*/
    public static final int FM_APIID_getRandomCode = 0x00000629 + Constant.NUMBER_OF_API_BASE;
    /**获取验证码*/
    public static final int FM_APIID_getSmsCode = 0x00000630 + Constant.NUMBER_OF_API_BASE;
    /**绑定手机号*/
    public static final int FM_APIID_bindPhone = 0x00000631 + Constant.NUMBER_OF_API_BASE;
    /**更换手机号*/
    public static final int FM_APIID_modifyPhone = 0x00000632 + Constant.NUMBER_OF_API_BASE;
    /** 抓取服务列表 */
    public static final int FM_APIID_getservicelist = 0x00000633 + Constant.NUMBER_OF_API_BASE;

    /** 红包-----红包新建、支付 */
    public static final int FM_APIID_CREATE_PAY_REDPACKET = 0x00000634 + Constant.NUMBER_OF_API_BASE;

    /** 红包-----发送红包 */
    public static final int FM_APIID_SEND_REDPACKET = 0x00000635 + Constant.NUMBER_OF_API_BASE;

    /** 红包-----拆红包 */
    public static final int FM_APIID_OPEN_REDPACKET = 0x00000636 + Constant.NUMBER_OF_API_BASE;

    /** 红包-----查询未发送的红包 display_type--1群红包，2社区红包，3私信红包 */
    public static final int FM_APIID_GET_UNSEND_REDPACKET = 0x00000637 + Constant.NUMBER_OF_API_BASE;

    /** 红包-----根据红包id查询红包信息 */
    public static final int FM_APIID_GET_REDPACKET_INFO_BY_ID = 0x00000638 + Constant.NUMBER_OF_API_BASE;

    /** 红包-----根据类型查询红包信息（收到、发出的红包）*/
    public static final int FM_APIID_GET_REDPACKET_INFO_BY_USER = 0x00000639 + Constant.NUMBER_OF_API_BASE;

    /** 红包-----位置红包用户位置更新 */
    public static final int FM_APIID_UPDATE_REDPACKET_LOCATION = 0x00000640 + Constant.NUMBER_OF_API_BASE;

    /** 钱包-----查询零钱余额 */
    public static final int FM_APIID_GET_POCKET_BALANCE = 0x00000641 + Constant.NUMBER_OF_API_BASE;

    /** 钱包-----零钱提现 */
    public static final int FM_APIID_WITHDRAW_BALANCE = 0x00000642 + Constant.NUMBER_OF_API_BASE;

    /** 钱包-----获取配置 */
    public static final int FM_APIID_GET_REDPACKET_CONFIG = 0x00000643 + Constant.NUMBER_OF_API_BASE;

    /** 钱包-----查询交易记录 */
    public static final int FM_APIID_GET_TRANSACTION_RECORDS = 0x00000644 + Constant.NUMBER_OF_API_BASE;

    /** 钱包-----充值 */
    public static final int FM_APIID_RECHARGE_BALANCE = 0x00000645 + Constant.NUMBER_OF_API_BASE;

    /** 钱包-----查询充值状态 */
    public static final int FM_APIID_GET_RECHARGE_STATUS = 0x00000646 + Constant.NUMBER_OF_API_BASE;

    /** 获取订单的支付状态 */
    public static final int FM_APIID_GET_ORDER_STATUS = 0x00000647 + Constant.NUMBER_OF_API_BASE;

    /**
     * 获取需要支付的相关信息--预支付
     */
    public static final int FM_APIID_REQUEST_PREPAY = 0x00000648 + Constant.NUMBER_OF_API_BASE;

    /**
     * 获取我的活动列表
     */
    public static final int FM_APIID_GET_MY_ACTIVITY_LIST = 0x00000649 + Constant.NUMBER_OF_API_BASE;

    /**
     * 获取我对某活动的报名及订单信息
     */
    public static final int FM_APIID_GET_ORDER_INFO = 0x00000650 + Constant.NUMBER_OF_API_BASE;

    /**
     * 原生的活动报名
     */
    public static final int FM_APIID_SEND_SIGN_INFO = 0x00000651 + Constant.NUMBER_OF_API_BASE;
    /**
     * 查看报名信息
     */
    public static final int FM_APIID_GET_MY_SIGNED_INFO = 0x00000652 + Constant.NUMBER_OF_API_BASE;
    /**
     * 获取每日红包
     */
    public static final int FM_APIID_GET_DAILY_RP = 0x00000653 + Constant.NUMBER_OF_API_BASE;

    /**
     * 绘制原生活动总图
     */
    public static final int FM_APIID_PAINTING = 0x00000654 + Constant.NUMBER_OF_API_BASE;

    /**
     * 原生活动我周边的"gps安装师傅"等
     */
    public static final int FM_APIID_RECTPAINT = 0x00000655 + Constant.NUMBER_OF_API_BASE;

    /**
     * 活动推广
     */
    public static final int FM_APIID_PUSHADV = 0x00000656 + Constant.NUMBER_OF_API_BASE;

    /**获取报警总数*/
    public static final int FM_APIID_AllAlarmCount = 0x00000657 + Constant.NUMBER_OF_API_BASE;

    public static final int DIALOG_AUTO_DISMISS = 30000;

    public static final int MAP_TYPE_BAIDU = 0;
    public static final int MAP_TYPE_AMAP = 1;
    public static final int MAP_TYPE_TENCENT = 2;
    public static final int MAP_TYPE_GOOGLE = 3;

    public static final int MAP_NORMAL = 0; // 普通模式
    public static final int MAP_SATELLITE = 1;// 卫星模式

    public static final String MAP_BAIDU = "BAIDU";
    public static final String MAP_GOOGLE = "GOOGLE";
    public static final String MAP_AMAP = "AMAP";
    public static final String MAP_TENCENT = "AMAP";//腾讯和高德地图坐标系一致

    public static final String PREFERENCE_CHOOSE_AUDIO = "choose_audio";
    public static final String PREFERENCE_CHOOSE_RING = "choose_ring";
    public static final String PREFERENCE_CHOOSE_SOUND = "choose_sound";
    
    public static final String PREFERENCE_CAR_COOKIE = "car_cookie";
    public static final String PREFERENCE_SINGLE_UDID= "single_udid";
    
    public static final String PREFERENCE_APP_CONFIG = "app_config";
    /**
     * 每日红包--领取后存储时间
     */
    public final static String PREFERENCE_DAILY_RP_OPENED_TIME = "daily_rp_opened_time";
    /**
     * 每日红包--红包弹窗的显示次数
     */
    public final static String PREFERENCE_DAILY_RP_SHOW_COUNT = "daily_rp_show_count";

    /** 当前写的日志文件 */
    public static final String PREFERENCE_LOG_FILE = "log_file";
    public static final String PREFERENCE_DOMAIN_IP = "car_domain_ip";
    public static final String PREFERENCE_DOMAINS = "preference_car_domains";

    public static final String PREFERENCE_USER_JSON = "preference_user_json";

    /**
     * 网络接口访问--计数
     */
    public final static String PREFERENCE_HTTP_SEQ = "network_http_seq";

    /**
     * 社区模块
     */
    public final static String USER_DATA      = "userData";

    public static final String DATA_TEXT = "data_text";

    /**
     * 原生活动的id携带参数
     **/
    public final static String NATIVE_ACTIVITY_ID = "activityId";
    /**
     * 页面进入携带参数
     **/
    public final static String WHERE_FROM_WELCOME = "activityFromWelCome";

    // 0,通知音 1,铃声 2,自定义
    public static final String PREFERENCE_NOTIFICATION_TYPE = "notification_type";

    // 环信的未读消息数目
    public static final String NOTIFICATION_UNREAD_HYPHENATE = "unread_num_hyphenate";

    // 告警消息提醒--持续震动
    public static final String ALARM_LONG_SHAKE = "long_shake";

    /**
     * 原生活动的跳转URL起始字段
     */
    public static final String KEY_ACTIVITY_DETAIL = "activitydetail:";
    /**
     * 原生活动的跳转URL起始字段
     */
    public static final String KEY_ACTIVITY = "activity";
    /**
     * 原生活动的跳转URL起始字段。加冒号，携带分类数据
     */
    public static final String KEY_ACTIVITY_CATEGORY = "activity:";
    /**
     * H5广告跳转URL起始字段
     */
    public static final String KEY_HTTP     = "http:";
    /**
     * H5广告跳转URL起始字段
     */
    public static final String KEY_HTTPS    = "https:";
    /**
     * 淘宝商城的跳转URL起始字段
     */
    public static final String KEY_TAOBAO = "taobao:";

    /**
     * 下拉，重新请求数据
     */
    public static final int REFRESH = 0;
    /**
     * 上拉，请求更多数据
     */
    public static final int MORE    = 1;
    
    public static int HISTORY_PLAY_SPEED = 500;
    public final static int SET_FENCE = 100;

    // 监控页面定位自己刷新间隔 原60秒太慢, 现在改为1秒
    public final static int MY_LOCATION_INTERVAL = 5 * 1000;

    public static ArrayList<Adver> mAdvers;

    // 默认百度地图10公里范围
    public static final double LatSpan = 0.11125;
    public static final double LngSpan = 0.11125;
    // 默认深圳市经纬度范围
    public static final double ShenzhenLat = 22.544132;
    public static final double ShenzhenLng = 113.964028;
    public static double toprightlat = ShenzhenLat + Constant.LatSpan / 2.0;
    public static double toprightlng = ShenzhenLng + Constant.LngSpan / 2.0;
    public static double bottomleftlat = ShenzhenLat - Constant.LatSpan / 2.0;
    public static double bottomleftlng = ShenzhenLng - Constant.LngSpan / 2.0;

    //登录账户
    public static final String LOGIN_ACCOUNT = "account";
    //登录密码（加密的）
    public static final String LOGIN_PWD = "password";

    public static final String PREF_UNIQUE_REGID = "REG_ID";
    public static final String PREF_UNIQUE_GPNS_REGID = "GPNS_REG_ID";

    public static final String KEY_DEVICE = "device_data";

    //通知渠道
    public static final String channelIdService = "coomix.all.service";
    public static final String channelNameService = "服务通知";

    public static final String channelId = "coomix.all.gpns.high";
    public static final String channelName = "报警通知";

    public static final String channelIdUpdate = "coomix.all.update.high";
    public static final String channelNameUpdate = "升级通知";

    public static final String channelIdMsg = "coomix.all.message.high";
    public static final String channelNameMsg = "消息通知";

    public static boolean isAppOnForground(Context mc) {
        ActivityManager activityManager = (ActivityManager) mc.getApplicationContext()
                .getSystemService(Context.ACTIVITY_SERVICE);

        String packageName = mc.getApplicationContext().getPackageName();

        List<RunningAppProcessInfo> appRrocess = activityManager.getRunningAppProcesses();
        if (appRrocess == null) {
            return false;
        }

        for (RunningAppProcessInfo runApp : appRrocess) {
            if (runApp.processName.equals(packageName)
                    && runApp.importance == RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                return true;
            }
        }
        return false;
    }

    public static int getTimeZoon() {
        // 本地时区与GMT格林威治标准时间的偏移量(北京是东八区 相差的毫秒数是28800000)
        // 1、取得本地时间：
        java.util.Calendar cal = java.util.Calendar.getInstance();
        // 2、取得时间偏移量：
        int zoneOffset = cal.get(java.util.Calendar.ZONE_OFFSET);
        // 3、取得夏令时差：
        int dstOffset = cal.get(java.util.Calendar.DST_OFFSET);
        // 本地时区与GMT格林威治标准时间的偏移量
        int timeZoneDif = (zoneOffset + dstOffset) / 1000;
        return timeZoneDif;
    }
}
