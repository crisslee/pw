package com.goome.gpns.crashhandler;

import android.os.Process;
import com.goome.gpns.utils.FileOperationUtil;
import com.goome.gpns.utils.LogUtil;
import java.lang.Thread.UncaughtExceptionHandler;

/**
 * 开发模式下，捕获异常并写到错误日志
 *
 * @author Administrator
 */
public class CrashHandler implements UncaughtExceptionHandler {

    private static CrashHandler instance = null;

    // 系统默认的异常处理（默认情况下，系统会终止当前的异常程序）
    private UncaughtExceptionHandler mDefaultCrashHandler;

    // 构造方法私有，防止外部构造多个实例，即采用单例模式
    private CrashHandler() {
    }

    private synchronized static void syncInit() {
        if (instance == null) {
            instance = new CrashHandler();
        }
    }

    public static CrashHandler getInstance() {
        if (instance == null) {
            syncInit();
        }
        return instance;
    }

    // 这里主要完成初始化工作
    public synchronized void init() {
        if (mDefaultCrashHandler == null) {
            // 获取系统默认的异常处理器
            mDefaultCrashHandler = Thread.getDefaultUncaughtExceptionHandler();
            // 将当前实例设为系统默认的异常处理器
            Thread.setDefaultUncaughtExceptionHandler(this);
        }
    }

    /**
     * 这个是最关键的函数，当程序中有未被捕获的异常，系统将会自动调用#uncaughtException方法
     * thread为出现未捕获异常的线程，ex为未捕获的异常，有了这个ex，就可以得到异常信息。
     */
    @Override
    public void uncaughtException(Thread thread, Throwable throwable) {
        try {
            // 打印出当前调用栈信息
            LogUtil.printException2Console(throwable);

            StackTraceElement[] traces = throwable.getStackTrace();
            String exceptionMsg = "Exception in thread \"" + thread.getName() + "\":" + throwable.toString() + "\n";
            traces = throwable.getStackTrace();
            if (traces != null) {
                for (int i = 0; i < traces.length; i++) {
                    exceptionMsg += "at " + traces[i].toString() + "\n";
                }
            }
            FileOperationUtil.saveErrMsgToFile(exceptionMsg);

            // 这里也可以通过网络上传异常信息到服务器，便于开发人员分析日志从而解决bug

            // 如果系统提供了默认的异常处理器，则交给系统去结束我们的程序，否则就由我们自己结束自己
            if (mDefaultCrashHandler != null) {
                mDefaultCrashHandler.uncaughtException(thread, throwable);
            } else {
                Process.killProcess(Process.myPid());
            }
        } catch (Exception e) {
            FileOperationUtil.saveErrMsgToFile("uncaughtException() occur an exception");
            LogUtil.printException2Console(e);
        }
    }
}
