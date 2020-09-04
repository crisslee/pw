package com.coomix.app.framework.app;

import java.io.Serializable;

public class Result implements Serializable{
    
    /**
     * 
     */
    private static final long serialVersionUID = 1839531064237748912L;
    public int statusCode;
    public String errorMessage;
    public Object mResult;
    public Object mResultTry;
    public int apiCode;
    
    public static final int OK = 1;
    public static final int FAIL = -1;
    public static final int ERROR_NETWORK = -10;
    public static final int ERROR_NETWORK_CONNECT_TIMEOUT = -11;
    public static final int ERROR_NETWORK_CONNECT_HOST = -12;
    public static final int ERROR_NETWORK_RESPONSE = -13;
    public static final int ERROR_NETWORK_REDIRECT = -14;
    public static final int ERROR_NETWORK_SOCKET_TIMEOUT = -15;
    public static final int ERROR_JSON_PARSE = -20;
    public static final int ERROR_UNKNOW = -100;
    public static final int ERROR_ACCOUNT_OR_PASSWORD_INVAID = 20001;//用户名和密码错误
    public static final int ERROR_QQ_LOGIN_ERROR = 20020;//QQ登录位吧绑定账号的错误号
    public static final int ERROR_QQ_LOGIN_BING_AGAIN = 20021;//QQ登录重复绑定
    /**
     * 网络返回固定错误--在另一台设备登录，强制退出
     */
    public static final int ERRCODE_LONGIN_TWO_DEVICES = 3016;// 3.1.5及之前版本错误码为-10115;
    /**
     * 网络返回固定错误--后台解析用户身份失败（解析ticket失败）
     */
    public static final int ERRCODE_LOGIN_TICKET_ERROR = 3003;
    /**
     * 网络返回固定错误--指定的用户不存在，可能是搜索某个用户的时候查不到，也可能是自己的账户不存在。这个错误码不需要用户重新登陆
     */
    public static final int ERRCODE_USER_NOT_EXSIT = 2002;
    /**
     * 网络返回固定错误--sessionid超时
     */
    public static final int ERRCODE_LONGIN_TIMEOUT = -10112;
    /**
     * 网络返回固定错误--版本太低，强制更新
     */
    public static final int ERRCODE_VERSION_TOO_LOWER = -10007;
    /**
     * 网络返回固定错误--表示没有权限操作（如：没有权限发帖或发私聊）
     */
    public static final int ERRCODE_NO_PERMISSION = 3018;
    /**
     * 网络返回固定错误--表示昵称已经存在（修改昵称接口）
     */
    public static final int ERRCODE_NICKNAME_EXIST = 3011;
    /**
     * 网络返回固定错误--表示昵称不合法（修改昵称接口）
     */
    public static final int ERRCODE_NICKNAME_INVALIDATE = 3015;
    /**
     * 网络返回固定错误--表示发帖太频繁，发送失败 || 上班不迟到当天已经打过卡
     */
    public static final int ERRCODE_SEND_FREQUENCY = 3005;
    /**
     * 网络返回固定错误--表示发的图片width或（和）height为0，发送失败
     */
    public static final int ERRCODE_IMAGE_ERROR = 3021;
    /**
     * 网络返回固定错误--已经报名，重复报名出错
     */
    public static final int ERRCODE_ACT_HAVE_SIGNED = 3022;
    /**
     * 网络返回固定错误--报名人数已经满了
     */
    public static final int ERRCODE_ACT_SIGNED_FULL = 3023;
    /**
     * 网络返回固定错误--截止时间已到，不能报名
     */
    public static final int ERRCODE_ACT_SIGN_STOPPED = 3024;
    /**
     * 网络返回固定错误--获取定位数据失败(1.9.3加入，获取本地帖子，定位失败-上传经纬度为0,0)
     */
    public static final int ERRCODE_GET_LOCATION_FAIL = 3038;
    /** 网络返回固定错误 -- 手机号已绑定 */
    public static final int ERRCODE_BINDPHONE_PHONE_IS_BIND_ERROR = 3039;
    /** 网络返回固定错误 -- 获取验证码错误 */
    public static final int ERRCODE_BINDPHONE_SMSCODE_ERROR = 3006;
    /**
     * 网络返回固定错误--订单超时未支付过期了
     */
    public static final int ERRCODE_ORDER_TIMEOUT_INVALID = 3037;

    /**
     * 当前账号的微信号，目前被其他用户绑定（被抢）
     */
    public static final int ERR_INVALID_SESSION = 3043;

    /** 自定义错误 */
	public static final int ERROR_HTTP_STATE = -25;
	public static final int ERROR_API_RETURN = -30;
	public static final int ERROR_API_RETURN_FORMAT = -35;
	public static final int ERROR_API_RETURN_UNKNOW_METHOD = -40;
	public static final int ERROR_API_RETURN_ILLEGAL_DOMAIN = -45;
	public static final int ERROR_API_RETURN_HTML = -50;
	public static final int ERROR_API_RETURN_NULL = -55;
	public static final int ERROR_API_ILLEGAL_UPDOWN_PARAM = -60;
	
    public String server;
	public String ip;
	public int wapType = 0;
	public int errcode = -101;
	public String debugUrl = "";
	/** 是否最后一次ip重试接口请求 */
	public boolean isLastTimeIpQuery = false;
	public String msg;
	public boolean success = false;
    public Result(){
        statusCode = FAIL;
    }
}
