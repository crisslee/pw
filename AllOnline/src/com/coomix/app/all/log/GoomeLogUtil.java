package com.coomix.app.all.log;

import com.coomix.app.all.AllOnlineApp;
import com.coomix.app.framework.util.NetworkUtil;
import com.coomix.app.framework.util.PreferenceUtil;

/**
 * 日志（打印/上传）辅助类
 *
 * Created by goome on 2016/11/9.
 */
public class GoomeLogUtil {
    private static final String PREFERENCE_HAS_UPLOAD_FAILED_ERROR_LOG = "preference_has_upload_failed_error_log";

    private GoomeLogUtil() {

    }

    /**
     * 设置日志是否含有提交失败的错误日志
     */
    public static void setUploadFailedErrorLog(boolean UploadErrorLogFailed) {
        PreferenceUtil.commitBoolean(PREFERENCE_HAS_UPLOAD_FAILED_ERROR_LOG, UploadErrorLogFailed);
    }

    /**
     * 是否含有提交失败的错误日志
     */
    public static boolean hasUploadFailedErrorLog() {
        return PreferenceUtil.getBoolean(PREFERENCE_HAS_UPLOAD_FAILED_ERROR_LOG, false);
    }

    /**
     * 根据后台返回的配置信息，判断是否需要上传日志
     *
     * @return 是否需要上传日志
     */
    public static boolean isNeedUploadLog() {
        return isUpload(false);
    }

    /**
     * 根据后台返回的配置信息，判断是否需要自动上传带errorCode的日志
     */
    public static boolean isNeedAutoUploadErrorLog() {
        return isUpload(true);
    }

    /**
     * @param isErrorAutoUpload 是否是错误自动上传
     */
    private static boolean isUpload(boolean isErrorAutoUpload) {
        AppConfigs config = AllOnlineApp.getAppConfig();
        if (isErrorAutoUpload) {
            // 根据配置确认是否需要上传(错误自动上传)
            return config.getLog_autoupload() == 1 && compareNetWorkType(config.getLog_autoupload_condition());
        } else {
            // 根据配置确认是否需要上传(普通上传)
            return config.getLog_upload() == 1 && compareNetWorkType(config.getLog_condition());
        }
    }

    /**
     * @param condition int<br/>
     * 0=仅wifi，<br/>
     * 1=所有网络<br/>
     * 2=2G即可(2/3/4G+wifi)<br/>
     * 3=3G即可(3/4G+wifi)<br/>
     * 4=4G即可(4G+wifi)<br/>
     * 默认0 上报日志的网络条件
     */
    public static boolean compareNetWorkType(int condition) {
        boolean upload = false;
        /* 0=未知网络 1=wifi 2=2G 3=3G 4=4G */
        int networkType = NetworkUtil.getInstance().getCurrNetType();
        switch (condition) {
            // log_condition 日志上传的网络类型
            case 1:
                // 所有网络都上传
                upload = true;
                break;
            case 2:
                // 2G/3G/4G/wifi都上传
                if (networkType == 1 || networkType == 2 || networkType == 3 ||
                    networkType == 4) {
                    upload = true;
                }
                break;
            case 3:
                // 3G/4G/wifi都上传
                if (networkType == 1 || networkType == 3 || networkType == 4) {
                    upload = true;
                }
                break;
            case 4:
                // 4G/wifi都上传
                if (networkType == 1 || networkType == 4) {
                    upload = true;
                }
                break;
            case 0:
                // 仅wifi上传
            default:
                if (networkType == 1) {
                    // 网络类型为wifi
                    upload = true;
                }
                break;
        }
        return upload;
    }
}
