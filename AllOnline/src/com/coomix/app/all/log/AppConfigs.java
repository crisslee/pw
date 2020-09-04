package com.coomix.app.all.log;

import android.text.TextUtils;
import com.coomix.app.all.Constant;
import com.google.gson.annotations.SerializedName;
import java.io.Serializable;
import net.goome.im.chat.GMConstant;
import org.apache.http.conn.params.ConnManagerParams;
import org.apache.http.conn.params.ConnPerRouteBean;

/**
 * app配置信息（网络返回）
 */
public class AppConfigs implements Serializable {

    public static final String TAG = "AppConfigs";

    private static final long serialVersionUID = -7958241056763114609L;

    private final String REDPACKET_BEST_IMG_URL_DEF =
        "http://buspic.gpsoo.net/goome01/M00/11/6D/wKgCoViv0W2EPFfRAAAAABXSpv8852.png";
    private final String REDPACKET_WORST_IMG_URL_DEF =
        "http://buspic.gpsoo.net/goome01/M00/11/6E/wKgCoFiv0XCEOs75AAAAADhyBzA466.png";
    public static final String DEFAULT_RECORD_TIME = "30,60,300,3600,-1";
    private static final String DEFAULT_EXCEPTION_URL = "http://lite.gmiot.net/wx/equipError.html";

    //默认录音灵敏度设置
    private static final String DEFAULT_RECORD_DEVICE_VOLUME_TITLE = "细语(窗外水滴敲打玻璃，高能耗),人声(好友之间的交谈，中能耗),轰鸣(狗的犬吠，低能耗)";
    private static final String DEFAULT_RECORD_DEVICE_VOLUME_SCOPE = "30,60,80";

    /** 关闭状态，禁用 */
    public static final int CONFIG_CLOSED = 0;
    /** 打开状态，启用 */
    public static final int CONFIG_OPENED = 1;

    /**
     * 是否采用bugly升级apk
     * 服务器返回字符串，“1”为是 “0”为不是
     */
    @SerializedName("carol_bugly_upgrade_agent")
    private String isBuglyUpgradeAgent = "0";

    /** int 0=禁用，1=启用 默认0 是否启用日志上报 */
    private int log_upload;
    /** string IS_DEBUG_MODE,INFO,WARN,ERROR 默认ERROR 写入sd卡日志级别 */
    private String log_level;
    /** log_level对应的本地枚举 */
    private GoomeLogLevel logLevel;
    /**
     * int<br/>
     * 0=仅wifi，<br/>
     * 1=所有网络<br/>
     * 2=2G即可(2/3/4G+wifi)<br/>
     * 3=3G即可(3/4G+wifi)<br/>
     * 4=4G即可(4G+wifi)<br/>
     * 默认0 上报日志的网络条件
     */
    private int log_condition;
    /** int 0=禁用，1=启用 默认0 是否启用性能数据上报 */
    private int performance_onoff;
    /**
     * int<br/>
     * 0=仅wifi，<br/>
     * 1=所有网络<br/>
     * 2=2G即可(2/3/4G+wifi)<br/>
     * 3=3G即可(3/4G+wifi)<br/>
     * 4=4G即可(4G+wifi)<br/>
     * 默认0 上报日志的网络条件
     */
    private int performance_condition;

    /** int 0=禁用，1=启用 默认0 是否启用运营工具 */
    private int operation_onoff;
    /** 网络重试机制--第一次普通重试的超时--单位：秒 */
    private int network_timeout_fir = 1;
    /** 网络重试机制--第二次普通重试的超时--单位：秒 */
    private int network_timeout_sec = 2;
    /** 网络重试机制--第三次普通重试的超时--单位：秒 */
    private int network_timeout_thir = 5;
    /** 网络重试机制--上传速度，计算上传时候的数据超时时间--单位：kb/秒 */
    private int network_upload_speed = 10;
    /** 网络性能上报--达到多少条数据才上报,默认20 */
    private int net_performance_report_num = 20;
    /** int 0=禁用，1=启用 默认0 是否启用ERROR日志自动上报 */
    private int log_autoupload;
    //谷米的IM的开关。 0关闭，1打开，默认1
    private int private_msg_using_im;

    private final int DEFAULT_CHAT_MESSAGE_NUM = 50;
    /**
     * int<br/>
     * 0=仅wifi，<br/>
     * 1=所有网络<br/>
     * 2=2G即可(2/3/4G+wifi)<br/>
     * 3=3G即可(3/4G+wifi)<br/>
     * 4=4G即可(4G+wifi)<br/>
     * 默认0 ERROR日志自动上报的网络条件
     */
    private int log_autoupload_condition;

    /**
     * 推送开关
     */
    private int push_onoff;
    /**
     * Http请求头中的Connection属性设置：0：关闭持久连接，1：保持持久连接Keep-Alive----默认0关闭Close
     */
    private int http_keepalive_onoff;
    /**
     * Http请求中，HttpClient一个Host允许的最大连接数。默认系统的ConnPerRouteBean.DEFAULT_MAX_CONNECTIONS_PER_ROUTE:2
     */
    private int android_max_connections_per_host;
    /**
     * Http请求中HttpClient允许的最大连接总数。默认系统的ConnManagerParams.DEFAULT_MAX_TOTAL_CONNECTIONS:20
     */
    private int android_max_total_connections;

    /**
     * 聊天消息每次加载的条数--默认50
     */
    private int chat_loading_number;
    /**
     * 活动报名的免责声明URL
     */
    private String activity_duty_statement_url;
    /**
     * 软件服务条款声明URL
     */
    private String coomix_service_terms_url;
    /**
     * 是否用服务器返回的时间作为基准去加密、请求.  0:关闭，用本地时间； 1:打开，用服务器时间。  默认1
     */
    private int use_server_time_onoff;

    //最小提现金额，单位(分)
    private int balance_min_withdraw_amount;

    //提现处理日期
    private String balance_withdraw_deal_with_days;

    /***群红包开关, 0：关闭； 1：打开(默认)*/
    private int group_redpacket_onoff = 1;

    /*****红包介绍****/
    private String redpacket_pack_help_url = "https://bussem.gpsoo.net/coomix-redpacket-help/car-online.html";

    /*****红包消息携带的文本内容--一般用于低版本提示版本太低需要升级****/
    private String you_get_redpacket_message;
    /*****红包手气最佳--图片****/
    private String redpacket_best_imgurl;
    /*****红包手气最佳--文字****/
    private String redpacket_best_tip;
    /*****红包手气最差--图片****/
    private String redpacket_worst_imgurl;
    /*****红包手气最差--文字****/
    private String redpacket_worst_tip;
    /*****发红包是否可以地图选位置--0：关闭（默认）， 1：打开****/
    private int redpacket_select_map_onoff;

    /***附近帖子的动态配置开关---0：关闭， 1：打开*/
    private int community_get_auto_nearby;

    //同城社区两个tab的名字
    private String community_home_first_title;
    private String community_home_second_title;
    /*****社区上传位置的时间间隔--默认200s****/
    private int community_upload_gps_interval_time;
    /*****社区上传位置的两次距离间隔--默认100米****/
    private int community_upload_gps_interval_distance;
    /***发位置红包，附近可选的名字搜索范围poi，默认200m**/
    private int redpacket_poi_range;

    //红包贴开关显示与否，0：关闭，1：显示
    private int community_show_nearby_redpacket_onoff;
    /***投诉网页url***/
    private String community_complain_someone_url;
    /****每日红包开关，0：关闭， 1： 打开，默认1开****/
    private int daily_redpacket_onoff;
    /***每日红包展示次数--就是关闭了daily_redpacket_close_num次后不再弹出***/
    private int daily_redpacket_close_num;
    //推广广告开关
    private int carol_popup_onoff;
    //是否保存默认头像，默认：0关闭，其他打开
    private int save_default_head_image_onoff;
    //抽奖活动规则说明
    private String community_url_lottery;
    //网络检测开关
    private int net_diagnose_onoff;
    //每日红包点击后是否跳转到同城，默认0：不跳转
    private int daily_redpacket_click_goto_community;
    //社区红包默认范围
    private int redpacket_default_alloc_range;
    //所有登录均使用新页面
    private int login_page_new_style;
    //个人主页积分兑换的展示开关，默认1，1打开，0关闭
    private int personal_exchange_onoff;
    //在抽奖页面是否显示充值
    private int show_recharge_in_lottery;
    //在抽奖也是否显示结束时间
    private int show_lottery_endtime;
    //登录免责声明
    private String login_duty_statement_url;
    //001支持微信支付, 010支持支付宝，100支持零钱. 默认111=7
    private int pay_platform_support;
    //两次发起定位的时间间隔。默认10000(10s)及每10s定位一次.单位毫秒
    private long android_location_interval;
    //GM IM日志上传级别
    private int im_log_level;
    //定位精度。0:Battery_Saving, 1:Device_Sensors, 2:Hight_Accuracy;默认2
    private int android_location_accuracy;

    //IM的数据库上传，默认关闭0
    private int goome_im_db_upload;

    //群聊是否使用im，0关闭，1打开，默认0
    private int carol_group_msg_using_gmim;

    private String carol_voice_tips_cn;

    //充值开关配置。0关闭，1打开
    private int recharge_show_onoff;
    //免责声明url
    @SerializedName("account_clause_url")
    private String clauseUrl;
    //录音持续时间
    private String record_time;
    //姿态异常url
    private String angle_exception_url;
    //录音灵敏度设置
    private String record_device_volume_title;
    private String record_device_volume_scope;
    /** w系列耗电模式参数 */
    private int high_timer_interval = 10;
    private int middle_timer_interval = 300;
    private int lower_timer_interval = 1800;
    private int temp_timer_interval = 3;
    /**小程序路径*/
    private String mini_program_offical_path = "pages/officalAcc/officalAcc";

    public AppConfigs() {
        setLog_upload(0);
        setLog_level("ERROR");
        setLogLevel(GoomeLogLevel.error);
        setLog_upload(0);
        setPerformance_onoff(0);
        setPerformance_condition(0);
        setOperation_onoff(0);
        setNetwork_timeout_fir(1);
        setNetwork_timeout_sec(2);
        setNetwork_timeout_thir(5);
        setNetwork_upload_speed(10);
        setNet_performance_report_num(20);
        setLog_autoupload(0);
        setLog_autoupload_condition(0);
        setPush_onoff(0);
        setGoome_im_db_upload(0);
        setHttp_keepalive_onoff(0);
        setMax_connections_per_host(ConnPerRouteBean.DEFAULT_MAX_CONNECTIONS_PER_ROUTE);
        setMax_total_connections(ConnManagerParams.DEFAULT_MAX_TOTAL_CONNECTIONS);
        setSave_default_head_image_onoff(0);
        setDaily_redpacket_onoff(1);
        setDaily_redpacket_close_num(1);
        setPay_platform_support(7);
        setIm_log_level(GMConstant.IMLogLevel.INFO.getValue());
        setPrivate_msg_using_im(1);
        setCarol_group_msg_using_gmim(0);
        setRecharge_show_onoff(0);
        setClauseUrl(Constant.STATEMENT_URL);
    }

    //重置一次值
    public void resetCacheAppConfig(AppConfigs appConfig) {
        if (appConfig != null) {
            //从缓存中读取出的数据需要重置不缓存的配置项
            appConfig.setLog_upload(0);
            appConfig.setLog_level("ERROR");
            appConfig.setLogLevel(GoomeLogLevel.error);
            appConfig.setLog_upload(0);
            appConfig.setPerformance_onoff(0);
            appConfig.setPerformance_condition(0);
            appConfig.setNet_performance_report_num(20);
            setHttp_keepalive_onoff(0);
            setMax_connections_per_host(ConnPerRouteBean.DEFAULT_MAX_CONNECTIONS_PER_ROUTE);
            setMax_total_connections(ConnManagerParams.DEFAULT_MAX_TOTAL_CONNECTIONS);
        }
    }

    /**
     * @return the {@link #log_upload}
     */
    public int getLog_upload() {
        return log_upload;
    }

    /**
     * @param log_upload the {@link #log_upload} to set
     */
    public void setLog_upload(int log_upload) {
        this.log_upload = log_upload;
    }

    /**
     * @return the {@link #log_level}
     */
    public String getLog_level() {
        return log_level;
    }

    /**
     * @param log_level the {@link #log_level} to set
     */
    public void setLog_level(String log_level) {
        this.log_level = log_level;
    }

    /**
     * @return the {@link #log_condition}
     */
    public int getLog_condition() {
        return log_condition;
    }

    /**
     * @param log_condition the {@link #log_condition} to set
     */
    public void setLog_condition(int log_condition) {
        this.log_condition = log_condition;
    }

    /**
     * @return the {@link #performance_onoff}
     */
    public int getPerformance_onoff() {
        return performance_onoff;
    }

    /**
     * @param performance_upload the {@link #performance_onoff} to set
     */
    public void setPerformance_onoff(int performance_upload) {
        this.performance_onoff = performance_upload;
    }

    /**
     * @return the {@link #performance_condition}
     */
    public int getPerformance_condition() {
        return performance_condition;
    }

    /**
     * @param performance_condition the {@link #performance_condition} to set
     */
    public void setPerformance_condition(int performance_condition) {
        this.performance_condition = performance_condition;
    }

    /**
     * @return the {@link #operation_onoff}
     */
    public int getOperation_onoff() {
        return operation_onoff;
    }

    /**
     * @param operation_onoff the {@link #operation_onoff} to set
     */
    public void setOperation_onoff(int operation_onoff) {
        this.operation_onoff = operation_onoff;
    }

    /** level对应的本地枚举 {@link GoomeLogLevel} */
    public GoomeLogLevel getLogLevel() {
        if (logLevel == null) {
            if (log_level == null || "".equals(log_level)) {
                logLevel = GoomeLogLevel.error;
            } else {
                if ("IS_DEBUG_MODE".equalsIgnoreCase(log_level)) {
                    logLevel = GoomeLogLevel.error;
                } else if ("INFO".equalsIgnoreCase(log_level)) {
                    logLevel = GoomeLogLevel.info;
                } else if ("WARM".equalsIgnoreCase(log_level)) {
                    logLevel = GoomeLogLevel.warm;
                } else if ("ERROR".equalsIgnoreCase(log_level)) {
                    logLevel = GoomeLogLevel.error;
                } else {
                    logLevel = GoomeLogLevel.error;
                }
            }
        }
        return logLevel;
    }

    /** level对应的本地枚举 {@link GoomeLogLevel} */
    public void setLogLevel(GoomeLogLevel logLevel) {
        this.logLevel = logLevel;
    }

    public int getNetwork_timeout_sec() {
        if (network_timeout_sec <= 0) {
            network_timeout_sec = 2;
        }
        return network_timeout_sec;
    }

    public void setNetwork_timeout_sec(int network_timeout_sec) {
        this.network_timeout_sec = network_timeout_sec;
    }

    public int getNetwork_timeout_fir() {
        if (network_timeout_fir <= 0) {
            network_timeout_fir = 1;
        }
        return network_timeout_fir;
    }

    public void setNetwork_timeout_fir(int network_timeout_fir) {
        this.network_timeout_fir = network_timeout_fir;
    }

    public int getGoome_im_db_upload() {

        return goome_im_db_upload;
    }

    public void setGoome_im_db_upload(int goome_im_db_upload) {
        this.goome_im_db_upload = goome_im_db_upload;
    }

    public int getNetwork_timeout_thir() {
        if (network_timeout_thir <= 0) {
            network_timeout_thir = 5;
        }
        return network_timeout_thir;
    }

    public void setNetwork_timeout_thir(int network_timeout_thir) {
        this.network_timeout_thir = network_timeout_thir;
    }

    public int getNetwork_upload_speed() {
        if (network_upload_speed <= 0) {
            network_upload_speed = 10;
        }
        return network_upload_speed;
    }

    public void setNetwork_upload_speed(int network_upload_speed) {
        this.network_upload_speed = network_upload_speed;
    }

    public int getNet_performance_report_num() {
        return net_performance_report_num;
    }

    public void setNet_performance_report_num(int net_performance_report_num) {
        this.net_performance_report_num = net_performance_report_num;
    }

    /**
     * @return the {@link #log_autoupload}
     */
    public int getLog_autoupload() {
        return log_autoupload;
    }

    /**
     * @param log_autoupload the {@link #log_autoupload} to set
     */
    public void setLog_autoupload(int log_autoupload) {
        this.log_autoupload = log_autoupload;
    }

    /**
     * @return the {@link #log_autoupload_condition}
     */
    public int getLog_autoupload_condition() {
        return log_autoupload_condition;
    }

    /**
     * @param log_autoupload_condition the {@link #log_autoupload_condition} to set
     */
    public void setLog_autoupload_condition(int log_autoupload_condition) {
        this.log_autoupload_condition = log_autoupload_condition;
    }

    public int getPush_onoff() {
        return push_onoff;
    }

    public void setPush_onoff(int push_onoff) {
        this.push_onoff = push_onoff;
    }

    public int getHttp_keepalive_onoff() {
        return http_keepalive_onoff;
    }

    public void setHttp_keepalive_onoff(int http_keepalive_onoff) {
        this.http_keepalive_onoff = http_keepalive_onoff;
    }

    public int getMax_connections_per_host() {
        if (android_max_connections_per_host <= 0) {
            android_max_connections_per_host = ConnPerRouteBean.DEFAULT_MAX_CONNECTIONS_PER_ROUTE;
        }
        return android_max_connections_per_host;
    }

    public void setMax_connections_per_host(int max_connections_per_host) {
        this.android_max_connections_per_host = max_connections_per_host;
    }

    public int getMax_total_connections() {
        if (android_max_total_connections <= 0) {
            android_max_total_connections = ConnManagerParams.DEFAULT_MAX_TOTAL_CONNECTIONS;
        }
        return android_max_total_connections;
    }

    public void setMax_total_connections(int max_total_connections) {
        this.android_max_total_connections = max_total_connections;
    }

    public int getAndroid_max_connections_per_host() {
        if (android_max_connections_per_host <= 0) {
            android_max_connections_per_host = ConnPerRouteBean.DEFAULT_MAX_CONNECTIONS_PER_ROUTE;
        }
        return android_max_connections_per_host;
    }

    public void setAndroid_max_connections_per_host(int max_connections_per_host) {
        this.android_max_connections_per_host = max_connections_per_host;
    }

    public int getAndroid_max_total_connections() {
        if (android_max_total_connections <= 0) {
            android_max_total_connections = ConnManagerParams.DEFAULT_MAX_TOTAL_CONNECTIONS;
        }
        return android_max_total_connections;
    }

    public void setAndroid_max_total_connections(int max_total_connections) {
        this.android_max_total_connections = max_total_connections;
    }

    public void setChat_loading_number(int chat_loading_number) {
        this.chat_loading_number = chat_loading_number;
    }

    public int getChat_loading_number() {
        if (chat_loading_number <= 0) {
            chat_loading_number = DEFAULT_CHAT_MESSAGE_NUM;
        }
        return chat_loading_number;
    }

    public String getActivity_duty_statement_url() {
        return activity_duty_statement_url;
    }

    public void setActivity_duty_statement_url(String activity_duty_statement_url) {
        this.activity_duty_statement_url = activity_duty_statement_url;
    }

    public int getUse_server_time_onoff() {
        return use_server_time_onoff;
    }

    public void setUse_server_time_onoff(int use_server_time_onoff) {
        this.use_server_time_onoff = use_server_time_onoff;
    }

    public int getBalance_min_withdraw_amount() {
        return balance_min_withdraw_amount;
    }

    public void setBalance_min_withdraw_amount(int balance_min_withdraw_amount) {
        this.balance_min_withdraw_amount = balance_min_withdraw_amount;
    }

    public int getGroup_redpacket_onoff() {
        return group_redpacket_onoff;
    }

    public void setGroup_redpacket_onoff(int group_redpacket_onoff) {
        this.group_redpacket_onoff = group_redpacket_onoff;
    }

    public String getBalance_withdraw_deal_with_days() {
        return balance_withdraw_deal_with_days;
    }

    public void setBalance_withdraw_deal_with_days(String balance_withdraw_deal_with_days) {
        this.balance_withdraw_deal_with_days = balance_withdraw_deal_with_days;
    }

    public String getRedpacket_pack_help_url() {
        return redpacket_pack_help_url;
    }

    public void setRedpacket_pack_help_url(String redpacket_pack_help_url) {
        this.redpacket_pack_help_url = redpacket_pack_help_url;
    }

    public String getYou_get_redpacket_message() {
        return you_get_redpacket_message;
    }

    public void setYou_get_redpacket_message(String you_get_redpacket_message) {
        this.you_get_redpacket_message = you_get_redpacket_message;
    }

    public String getRedpacket_best_imgurl() {
        return redpacket_best_imgurl;
    }

    public void setRedpacket_best_imgurl(String redpacket_best_imgurl) {
        this.redpacket_best_imgurl = redpacket_best_imgurl;
    }

    public String getRedpacket_best_tip() {
        return redpacket_best_tip;
    }

    public void setRedpacket_best_tip(String redpacket_best_tip) {
        this.redpacket_best_tip = redpacket_best_tip;
    }

    public String getRedpacket_worst_imgurl() {
        if (TextUtils.isEmpty(redpacket_worst_imgurl)) {
            redpacket_worst_imgurl = REDPACKET_BEST_IMG_URL_DEF;
        }
        return redpacket_worst_imgurl;
    }

    public void setRedpacket_worst_imgurl(String redpacket_worst_imgurl) {
        this.redpacket_worst_imgurl = redpacket_worst_imgurl;
    }

    public String getRedpacket_worst_tip() {
        if (TextUtils.isEmpty(redpacket_worst_tip)) {
            redpacket_worst_tip = REDPACKET_WORST_IMG_URL_DEF;
        }
        return redpacket_worst_tip;
    }

    public void setRedpacket_worst_tip(String redpacket_worst_tip) {
        this.redpacket_worst_tip = redpacket_worst_tip;
    }

    public int getRedpacket_select_map_onoff() {
        return redpacket_select_map_onoff;
    }

    public void setRedpacket_select_map_onoff(int redpacket_select_map_onoff) {
        this.redpacket_select_map_onoff = redpacket_select_map_onoff;
    }

    public int getCommunity_get_auto_nearby() {
        return community_get_auto_nearby;
    }

    public void setCommunity_get_auto_nearby(int community_get_auto_nearby) {
        this.community_get_auto_nearby = community_get_auto_nearby;
    }

    public String getCommunity_home_first_title() {
        return community_home_first_title;
    }

    public void setCommunity_home_first_title(String community_home_first_title) {
        this.community_home_first_title = community_home_first_title;
    }

    public String getCommunity_home_second_title() {
        return community_home_second_title;
    }

    public void setCommunity_home_second_title(String community_home_second_title) {
        this.community_home_second_title = community_home_second_title;
    }

    public int getRedpacket_poi_range() {
        if (redpacket_poi_range <= 0) {
            redpacket_poi_range = 200;
        }
        return redpacket_poi_range;
    }

    public void setRedpacket_poi_range(int redpacket_poi_range) {
        this.redpacket_poi_range = redpacket_poi_range;
    }

    public int getCommunity_show_nearby_redpacket_onoff() {
        return community_show_nearby_redpacket_onoff;
    }

    public void setCommunity_show_nearby_redpacket_onoff(int community_show_nearby_redpacket_onoff) {
        this.community_show_nearby_redpacket_onoff = community_show_nearby_redpacket_onoff;
    }

    public int getDaily_redpacket_onoff() {
        return daily_redpacket_onoff;
    }

    public void setDaily_redpacket_onoff(int daily_redpacket_onoff) {
        this.daily_redpacket_onoff = daily_redpacket_onoff;
    }

    public int getDaily_redpacket_close_num() {
        return daily_redpacket_close_num;
    }

    public void setDaily_redpacket_close_num(int daily_redpacket_close_num) {
        this.daily_redpacket_close_num = daily_redpacket_close_num;
    }

    public int getCarol_popup_onoff() {
        return carol_popup_onoff;
    }

    public void setCarol_popup_onoff(int carol_popup_onoff) {
        this.carol_popup_onoff = carol_popup_onoff;
    }

    public int getSave_default_head_image_onoff() {
        return save_default_head_image_onoff;
    }

    public void setSave_default_head_image_onoff(int save_default_head_image_onoff) {
        this.save_default_head_image_onoff = save_default_head_image_onoff;
    }

    public String getCommunity_url_lottery() {
        return community_url_lottery;
    }

    public void setCommunity_url_lottery(String community_url_lottery) {
        this.community_url_lottery = community_url_lottery;
    }

    public int getNet_diagnose_onoff() {
        return net_diagnose_onoff;
    }

    public void setNet_diagnose_onoff(int net_diagnose_onoff) {
        this.net_diagnose_onoff = net_diagnose_onoff;
    }

    public int getDaily_redpacket_click_goto_community() {
        return daily_redpacket_click_goto_community;
    }

    public void setDaily_redpacket_click_goto_community(int daily_redpacket_click_goto_community) {
        this.daily_redpacket_click_goto_community = daily_redpacket_click_goto_community;
    }

    public int getRedpacket_default_alloc_range() {
        return redpacket_default_alloc_range;
    }

    public void setRedpacket_default_alloc_range(int redpacket_default_alloc_range) {
        this.redpacket_default_alloc_range = redpacket_default_alloc_range;
    }

    public int getLogin_page_new_style() {
        return login_page_new_style;
    }

    public void setLogin_page_new_style(int login_page_new_style) {
        this.login_page_new_style = login_page_new_style;
    }

    public int getPersonal_exchange_onoff() {
        return personal_exchange_onoff;
    }

    public void setPersonal_exchange_onoff(int personal_exchange_onoff) {
        this.personal_exchange_onoff = personal_exchange_onoff;
    }

    public int getShow_recharge_in_lottery() {
        return show_recharge_in_lottery;
    }

    public void setShow_recharge_in_lottery(int show_recharge_in_lottery) {
        this.show_recharge_in_lottery = show_recharge_in_lottery;
    }

    public int getShow_lottery_endtime() {
        return show_lottery_endtime;
    }

    public void setShow_lottery_endtime(int show_lottery_endtime) {
        this.show_lottery_endtime = show_lottery_endtime;
    }

    public String getLogin_duty_statement_url() {
        return login_duty_statement_url;
    }

    public void setLogin_duty_statement_url(String login_duty_statement_url) {
        this.login_duty_statement_url = login_duty_statement_url;
    }

    public int getPay_platform_support() {
        return pay_platform_support;
    }

    public void setPay_platform_support(int pay_platform_support) {
        this.pay_platform_support = pay_platform_support;
    }

    public long getAndroid_location_interval() {
        if (android_location_interval <= 0) {
            android_location_interval = 10000;
        }
        return android_location_interval;
    }

    public void setAndroid_location_interval(long android_location_interval) {
        this.android_location_interval = android_location_interval;
    }

    public int getAndroid_location_accuracy() {
        return android_location_accuracy;
    }

    public void setAndroid_location_accuracy(int android_location_accuracy) {
        this.android_location_accuracy = android_location_accuracy;
    }

    public int getIm_log_level() {
        return im_log_level;
    }

    public void setIm_log_level(int im_log_level) {
        this.im_log_level = im_log_level;
    }

    public int getPrivate_msg_using_im() {
        return private_msg_using_im;
    }

    public void setPrivate_msg_using_im(int private_msg_using_im) {
        this.private_msg_using_im = private_msg_using_im;
    }

    public int getCarol_group_msg_using_gmim() {
        return carol_group_msg_using_gmim;
    }

    public void setCarol_group_msg_using_gmim(int carol_group_msg_using_gmim) {
        this.carol_group_msg_using_gmim = carol_group_msg_using_gmim;
    }

    public String getCarol_voice_tips_cn() {
        return carol_voice_tips_cn;
    }

    public void setCarol_voice_tips_cn(String carol_voice_tips_cn) {
        this.carol_voice_tips_cn = carol_voice_tips_cn;
    }

    public boolean isBuglyUpgradeAgent() {
        return "1".equals(isBuglyUpgradeAgent);
    }

    public void setBuglyUpgradeAgent(String buglyUpgradeAgent) {
        isBuglyUpgradeAgent = buglyUpgradeAgent;
    }

    public int getRecharge_show_onoff() {
        return recharge_show_onoff;
    }

    public void setRecharge_show_onoff(int recharge_show_onoff) {
        this.recharge_show_onoff = recharge_show_onoff;
    }

    public String getClauseUrl() {
        if (TextUtils.isEmpty(clauseUrl)) {
            clauseUrl = Constant.STATEMENT_URL;
        }
        return clauseUrl;
    }

    public void setClauseUrl(String clauseUrl) {
        this.clauseUrl = clauseUrl;
    }

    public String getRecord_time() {
        if (TextUtils.isEmpty(record_time)) {
            return DEFAULT_RECORD_TIME;
        }
        return record_time;
    }

    public void setRecord_time(String record_time) {
        this.record_time = record_time;
    }

    public String getAngle_exception_url() {
        if (TextUtils.isEmpty(angle_exception_url)) {
            return DEFAULT_EXCEPTION_URL;
        }
        return angle_exception_url;
    }

    public void setAngle_exception_url(String angle_exception_url) {
        this.angle_exception_url = angle_exception_url;
    }

    public String getRecord_device_volume_title() {
        if(TextUtils.isEmpty(record_device_volume_title)) {
            return DEFAULT_RECORD_DEVICE_VOLUME_TITLE;
        }
        return record_device_volume_title;
    }

    public void setRecord_device_volume_title(String record_device_volume_title) {
        this.record_device_volume_title = record_device_volume_title;
    }

    public String getRecord_device_volume_scope() {
        if(TextUtils.isEmpty(record_device_volume_scope)) {
            return DEFAULT_RECORD_DEVICE_VOLUME_SCOPE;
        }
        return record_device_volume_scope;
    }

    public void setRecord_device_volume_scope(String record_device_volume_scope) {
        this.record_device_volume_scope = record_device_volume_scope;
    }

    public int getHighTimerInterval() {
        return high_timer_interval;
    }

    public int getMiddleTimerInterval() {
        return middle_timer_interval;
    }

    public int getLowerTimerInterval() {
        return lower_timer_interval;
    }

    public int getTempTimerInterval() {
        return temp_timer_interval;
    }

    public String getMini_program_offical_path() {
        return mini_program_offical_path;
    }
}
