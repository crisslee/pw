package com.coomix.app.all.log;

/**
 * Created by goome on 2016/11/7.
 */
public class GoomeLogErrorCode {
    // 网络相关，定义范围预留[10000~10199] ERROR_CODE_NETWORK开头
    /**
     * 通用网络错误
     */
    public static final int ERROR_CODE_NETWORK = 10000;
    /**
     * 网络模块出现NullPointException
     */
    public static final int ERROR_CODE_NETWORK_NULL = 10001;
    /**
     * 网络模块出现JsonException
     */
    public static final int ERROR_CODE_NETWORK_JSON = 10002;
    /**
     * 网络上传参数加密错误
     */
    public static final int ERROR_CODE_OTHER_ENCODE_ERROR = 10003;

    // 图片相关，定义范围预留[10100~10299] ERROR_CODE_PIC开头
    /**
     * 上传图片异常（网络异常以外）
     */
    public static final int ERROR_CODE_PIC_UPLOAD_ERROR = 10100;

    // 帖子相关，定义范围预留[10200~10399] ERROR_CODE_TOPIC开头
    /**
     * 发帖异常（网络异常以外）
     */
    public static final int ERROR_CODE_TOPIC_ADD_ERROR = 10200;
    /**
     * 启动，帖子队列重置
     */
    public static final int ERROR_CODE_TOPIC_SENDING_RESET = 10201;
    /**
     * 发帖发送广播异常（网络异常以外）
     */
    public static final int ERROR_CODE_TOPIC_ADD_ERROR_SEND_BROADCAST = 10201;
    /**
     * 回复异常（网络异常以外）
     */
    public static final int ERROR_CODE_TOPIC_REPLY_ERROR = 10202;

    // 板块相关，定义范围预留[10300~10499] ERROR_CODE_SECTION开头

    // 聊天相关，定义范围预留[10400~10599] ERROR_CODE_CHAT开头

    // 公交相关，定义范围预留[10500~10699] ERROR_CODE_BUS开头
    public static final int ERROR_CODE_BUS_CITY = 10500;

    // 地图相关，定义范围预留[10700~10899] ERROR_CODE_MAP开头

    // 其它，定义范围预留[10900~10999] ERROR_CODE_OTHER开头
    /**
     * 实例化/初始化错误
     */
    public static final int ERROR_CODE_OTHER_INIT_ERROR = 10201;
}
