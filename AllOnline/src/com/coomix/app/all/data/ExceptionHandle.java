package com.coomix.app.all.data;

import android.net.ParseException;
import com.coomix.app.all.Constant;
import com.coomix.app.all.model.response.RespBase;
import com.google.gson.JsonParseException;
import java.net.ConnectException;
import java.net.UnknownHostException;
import org.apache.http.conn.ConnectTimeoutException;
import org.json.JSONException;
import retrofit2.HttpException;

/**
 * Created by ly on 2017/9/18 15:09.
 */
public class ExceptionHandle {

    public static Throwable handleException(Throwable e) {
        if (Constant.IS_DEBUG_MODE) {
            e.printStackTrace();
        }
        ResponeThrowable ex;
        if (e instanceof HttpException) {
            HttpException httpException = (HttpException) e;
            ex = new ResponeThrowable(e, ERROR.HTTP_ERROR);
            switch (httpException.code()) {
                case HttpCode.SUCC:
                    ex.message = "请求成功";
                    break;
                case HttpCode.ERR_INVALID_PARA:
                    ex.message = "参数错误";
                    break;
                case HttpCode.ERR_INVALID_AUTHORIZATION:
                    ex.message = "授权失败";
                    break;
                case HttpCode.ERR_INVALID_PRIVILEGE:
                    ex.message = "权限不足";
                    break;
                case HttpCode.ERR_NON_RESOURCE:
                    ex.message = "资源不存在";
                    break;
                case HttpCode.ERR_BAD_GATEWAY:
                    ex.message = "网关错误";
                    break;
                case HttpCode.ERR_REQUEST_TIMEOUT:
                    ex.message = "网络请求超时";
                    break;
                case HttpCode.ERR_GATEWAY_TIMEOUT:
                    ex.message = "网关超时";
                    break;
                case HttpCode.ERR_UNKOWN:
                    ex.message = "服务器未知错误";
                    break;
                case HttpCode.ERR_SERVER_MAINTAINANCE:
                    ex.message = "服务器暂时无法访问或正在维护";
                    break;
                default:
                    ex.message = "网络错误";
                    break;
            }
            return ex;
        } else if (e instanceof ServerException) {

            return e;
        } else if (e instanceof JsonParseException
            || e instanceof JSONException
            || e instanceof ParseException) {
            ex = new ResponeThrowable(e, ERROR.PARSE_ERROR);
            ex.message = "解析错误";
            return ex;
        } else if (e instanceof ConnectException) {
            ex = new ResponeThrowable(e, ERROR.NETWORD_ERROR);
            ex.message = "连接失败";
            return ex;
        } else if (e instanceof javax.net.ssl.SSLHandshakeException) {
            ex = new ResponeThrowable(e, ERROR.SSL_ERROR);
            ex.message = "证书验证失败";
            return ex;
        } else if (e instanceof ConnectTimeoutException) {
            ex = new ResponeThrowable(e, ERROR.TIMEOUT_ERROR);
            ex.message = "连接超时";
            return ex;
        } else if (e instanceof java.net.SocketTimeoutException) {
            ex = new ResponeThrowable(e, ERROR.TIMEOUT_ERROR);
            ex.message = "连接超时";
            return ex;
        } else if (e instanceof UnknownHostException) {
            ex = new ResponeThrowable(e, ERROR.UNKNOWN_HOST);
            ex.message = "DNS解释失败";
            return ex;
        } else {
            ex = new ResponeThrowable(e, ERROR.UNKNOWN);
            ex.message = "未知错误";
            return ex;
        }
    }

    public static class HttpCode {
        //        200	请求成功，返回下文接口定义的JSON。
        public static final int SUCC = 200;
        //        400	坏请求，参数错误或缺少参数，返回对应的Error对象。
        public static final int ERR_INVALID_PARA = 400;
        //        401	请求需要用户授权（登录）或授权（登录）失败，返回对应的Error对象。
        public static final int ERR_INVALID_AUTHORIZATION = 401;
        //        403	权限不足（如修改他人信息），返回对应的Error对象。
        public static final int ERR_INVALID_PRIVILEGE = 403;
        //        404	资源不存在，返回对应的Error对象。
        public static final int ERR_NON_RESOURCE = 404;
        //        500	服务器未知错误， 返回对应的Error对象。
        public static final int ERR_REQUEST_TIMEOUT = 408;
        public static final int ERR_BAD_GATEWAY = 502;
        //        503	服务器暂时无法访问或正在维护。
        public static final int ERR_SERVER_MAINTAINANCE = 503;
        public static final int ERR_GATEWAY_TIMEOUT = 504;
        public static final int ERR_UNKOWN = 500;
    }

    public static class BusinessCode {
        //汽车在线相关的业务代码
        /**
         * 当前账号的微信号，目前被其他用户绑定（被抢）
         */
        public static final int ERR_INVALID_SESSION = 3043;

        /**
         * 当前社区账号ticket过期
         */
        public static final int ERR_EXIRPED_SESSION = 3016;

        public static final int ERRCODE_SUCC = 0;

        /**
         * 目标微信号已经被绑定
         */
        public static final int ERR_WECHAT_ALREADY_BINDED = 3041;
        /**
         * 微信会让登陆异常的微信，换取token失败
         */
        public static final int ERR_WECHAT_WX_CODE_INVALID = 2004;

        /**
         * 网络返回固定错误--提现时余额不足
         */
        public static final int ERRCODE_BALANCE_INSUFFICIENT = 10002;

        /**
         * 用户提交的tocken已过期
         */
        public static final int ERROR_TOKEN_EXPIRE = 10006;
        //没有关注公众号
        public static final int WECHAT_NOT_SUBSCRIBE = 30006;
        //没有绑定信息
        public static final int NO_BIND_INFO = 30009;
    }

    /**
     * 约定异常
     */
    public static class ERROR {
        /**
         * 未知错误
         */
        public static final int UNKNOWN = 1000;
        /**
         * 解析错误
         */
        public static final int PARSE_ERROR = 1001;
        /**
         * 网络错误
         */
        public static final int NETWORD_ERROR = 1002;
        /**
         * 协议出错
         */
        public static final int HTTP_ERROR = 1003;

        /**
         * 证书出错
         */
        public static final int SSL_ERROR = 1005;

        /**
         * 连接超时
         */
        public static final int TIMEOUT_ERROR = 1006;

        /**
         * DNS解析错误
         */
        public static final int UNKNOWN_HOST = 1007;
    }

    public static class ResponeThrowable extends Exception {
        public int code;
        public String message;

        public ResponeThrowable(Throwable throwable, int code) {
            super(throwable);
            this.code = code;
        }

        public int getErrCode() {
            return code;
        }

        public String getErrCodeMessage() {
            if (code == 0) {
                return message;
            } else {
                return message + "(" + code + ")";
            }
        }

        public String getErrMessage() {
            return message;
        }
    }

    public static class ServerException extends RuntimeException {
        public int code;
        public String message;

        public ServerException(int code, String message) {
            this.code = code;
            this.message = message;
        }

        public ServerException(RespBase respBase) {
            this.code = respBase.getErrcode();
            this.message = respBase.getMsg();
        }
    }
}

