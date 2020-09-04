package com.goome.gpns;

import android.content.Context;
import android.util.Log;

import com.goome.gpns.crashhandler.CrashHandler;
import com.goome.gpns.utils.LogUtil;

public class GpnsSDKInitializer {

    public static void initialize(Context context) {
        GPNSInterface.appContext = context.getApplicationContext();
        LogUtil.init();
        if (LogUtil.DEBUG) {
            // 开发环境，使用自己的异常捕获机制
            CrashHandler crashHandler = CrashHandler.getInstance();
            crashHandler.init();
        }
    }

}
