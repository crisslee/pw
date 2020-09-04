package com.coomix.app.all.log;

import android.content.Context;
import android.os.Build;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;
import com.coomix.app.all.AllOnlineApp;
import com.coomix.app.all.Constant;
import com.coomix.app.all.GlobalParam;
import com.coomix.app.framework.util.NetworkUtil;
import com.coomix.app.framework.util.PreferenceUtil;
import com.coomix.app.all.data.BaseSubscriber;
import com.coomix.app.all.data.DataEngine;
import com.coomix.app.all.data.ExceptionHandle;
import com.coomix.app.all.data.RxUtils;
import com.coomix.app.all.model.response.RespBase;
import com.coomix.app.all.util.Md5Util;
import com.tencent.bugly.crashreport.CrashReport;
import java.io.File;
import java.io.FileWriter;
import java.text.SimpleDateFormat;

import io.reactivex.disposables.Disposable;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;

/**
 * 日志记录类 所有log方法返回boolean表示是否已存储
 */
public class GoomeLog {
    private Context context;
    private boolean isDebug;
    private GoomeLogLevel logLevel;

    private static GoomeLog instance = new GoomeLog();

    private GoomeLog() {
    }

    public static GoomeLog getInstance() {
        return instance;
    }

    public void init(Context ctx) {
        context = ctx.getApplicationContext();
        PreferenceUtil.init(context);
        new Thread(new Runnable() {
            @Override
            public void run() {
                initDiskCacheDir(context);
            }
        }).start();
    }

    public void setLogLevel(GoomeLogLevel logLevel) {
        this.logLevel = logLevel;
    }

    public GoomeLogLevel getLogLevel() {
        if (logLevel == null) {
            return GoomeLogLevel.error;
        }
        return logLevel;
    }

    /** 设置是否打开debug模式，debug模式会打印日志内容到logCat, 默认关闭 */
    public void setDebug(boolean isDebug) {
        this.isDebug = isDebug;
    }

    public boolean logD(final String tag, final String msg) {
        if (isDebug) {
            Log.i(tag, msg);
        }
        GoomeLogLevel level = getLogLevel();
        if (level == GoomeLogLevel.debug) {
            final long time = System.currentTimeMillis();
            saveToFileAsync("D", time, tag, msg, 0);
            return true;
        }
        return false;
    }

    public boolean logI(final String tag, final String msg) {
        if (isDebug) {
            Log.i(tag, msg);
        }
        GoomeLogLevel level = getLogLevel();
        if (level == GoomeLogLevel.debug || level == GoomeLogLevel.info) {
            final long time = System.currentTimeMillis();
            saveToFileAsync("I", time, tag, msg, 0);
            return true;
        }
        return false;
    }

    public boolean logW(final String tag, final String msg) {
        if (isDebug) {
            Log.w(tag, msg);
        }
        GoomeLogLevel level = getLogLevel();
        if (level == GoomeLogLevel.debug || level == GoomeLogLevel.info || level == GoomeLogLevel.warm) {
            final long time = System.currentTimeMillis();
            saveToFileAsync("W", time, tag, " L " + msg, 0);
            return true;
        }
        return false;
    }

    /**
     * 比其他日志打印多了errorCode， errorCode != 0时会立即将日志文件上传到服务器
     *
     * @param errorCode 当!=0时，表示需要立即上传日志文件至网络
     */
    public boolean logE(final String tag, final String msg, final int errorCode) {
        if (isDebug) {
            Log.e(tag, errorCode + msg);
        }
        GoomeLogLevel level = getLogLevel();
        if (level == GoomeLogLevel.debug || level == GoomeLogLevel.info
            || level == GoomeLogLevel.warm || level == GoomeLogLevel.error) {
            final long time = System.currentTimeMillis();
            if (errorCode != 0) {
                saveToFileAsync("E", time, tag, "errorCode: " + errorCode + "   msg: " + msg, errorCode);
            } else {
                saveToFileAsync("E", time, tag, msg, errorCode);
            }
            return true;
        }
        return false;
    }

    private SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
    /** 每个文件的限制 */
    private long MAX_FILE_SIZE = 4 * 1024 * 1024;
    /** 最多允许log文件数 */
    private int MAX_FILE_NUM = 1;
    /** 存储目录 */
    private String DIR_LOG = "goomelog";
    /** 存储文件名 */
    private String FILE_LOG = "errorlog";

    /**
     * @param errcode 当!=0时，表示需要立即上传日志文件至网络
     */
    private void saveToFileAsync(final String errorLevel, final long time, final String tag, final String msg,
        final int errcode) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                saveToFile(errorLevel, time, tag, msg);
                if (errcode != 0 && GoomeLogUtil.isNeedAutoUploadErrorLog()) {
                    // 特殊错误（有页面提示），需要自动立即上传， 且文件配置该网络环境下允许上传日志
                    LogUploadInfo info = new LogUploadInfo(AllOnlineApp.mApp);
                    info.setErrorCode(errcode);
                    if (NetworkUtil.getInstance().isNetWorkConnected()) {
                        uploadLog(info);
                    }
                }
            }
        }).start();
    }

    private void uploadLog(final LogUploadInfo info) {
        //立即上传当前写入的日志
        if (!TextUtils.isEmpty(currentFilePath)) {
            final File file = new File(currentFilePath);
            if (file.exists()) {
                try {
                    RequestBody requestBody = new MultipartBody.Builder().setType(MultipartBody.FORM)
                            .addFormDataPart("md5", Md5Util.fileMD5(currentFilePath))
                            .addFormDataPart("size", String.valueOf(file.length()))
                            .addFormDataPart("content", file.getName(), RequestBody.create(MediaType.parse("text/*"), file))
                            .build();

                    Disposable d = DataEngine.getGoomeLogApi()
                            .uploadLogFile(GlobalParam.getInstance().getCommonParas(), info.getProduction(), info.getDevname(), info.getOsver(),
                                    info.getOsextra(), info.getExtra(), NetworkUtil.isWifiExtend(AllOnlineApp.mApp) ? "wifi" : "mobile", requestBody)
                            .compose(RxUtils.toIO())
                            .compose(RxUtils.businessTransformer())
                            .subscribeWith(new BaseSubscriber<RespBase>() {
                                @Override
                                public void onNext(RespBase respBase) {
                                    GoomeLogUtil.setUploadFailedErrorLog(false);
                                    try {
                                        file.delete();
                                    } catch (Exception e) {
                                    }
                                }

                                @Override
                                public void onHttpError(ExceptionHandle.ResponeThrowable e) {
                                    GoomeLogUtil.setUploadFailedErrorLog(true);
                                }
                            });

                } catch (Exception e) {
                }
            }
        }
    }

    /**
     * 将日志写入文件中
     */
    private synchronized void saveToFile(String errorLevel, long time, String tag, String msg) {
        // 日志写入文件中
        StringBuilder sbLog = new StringBuilder("\r\n");
        sbLog.append(errorLevel);
        sbLog.append("    ");
        sbLog.append(format.format(time));
        sbLog.append("    ");
        sbLog.append(tag);
        sbLog.append("    ");
        sbLog.append(msg);
        sbLog.append("\r\n");
        String logDirPath = getLogDir();
        if (logDirPath == null) {
            CrashReport.postCatchedException(new Exception("保存日志失败，文件路径为null"));
            return;
        }
        int logFileIndex = PreferenceUtil.getInt(Constant.PREFERENCE_LOG_FILE, 0);
        int i = 0;
        do {
            if (save(logDirPath, sbLog.toString(), true, logFileIndex, i)) {
                return;
            }
            if (i == MAX_FILE_NUM - 1) {
                // 循环到最后一次，仍然没有写入，则覆盖最后一个日志（保存时间最长的一个）
                save(logDirPath, sbLog.toString(), false, logFileIndex, i);
                return;
            }
            i++;
        }
        while (i < MAX_FILE_NUM);
    }

    public void deleteFile(int logFileIndex) {
        File file = new File(getLogDir(), FILE_LOG + (logFileIndex + 1) + ".txt");
        // file.deleteOnExit();
        // 不删除文件，以覆盖写入的方式清空wenj
        writeToFile(file, "", false);
    }

    public String[] getLogPaths() {
        String[] paths = new String[MAX_FILE_NUM];
        for (int i = 0; i < MAX_FILE_NUM; i++) {
            paths[i] = getLogDir() + File.separator + FILE_LOG + (i + 1) + ".txt";
        }
        return paths;
    }

    public String getLogGZipPath(String path) {
        if (path == null) {
            return null;
        }
        File file = new File(path);
        try {
            if (file.isFile()) {
                if (file.length() == 0) {
                    return null;
                }
                return GzipUtils.fileToGzip(file);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 处理存储逻辑
     *
     * @param logDirPath 保存的文件夹目录
     * @param log 需要写入的日志内容
     * @param append 是否增加在原有日志后面
     * @param logFileIndex 上一次写入成功的目录index
     * @param i 进入到第几次循环
     * @return 是否已经进行了写日志操作
     */
    private boolean save(String logDirPath, String log, boolean append, int logFileIndex, int i) {
        int file_log_num = (logFileIndex + i) / MAX_FILE_NUM;
        File file = new File(logDirPath, FILE_LOG + (file_log_num + 1) + ".txt");
        if (append && isFileFull(file, log.length())) {
            // 在原文件后增加日志 并且 原文件已经写满
            // 则不写入日志
            return false;
        } else {
            writeToFile(file, log, append);
            if (i != 0) {
                PreferenceUtil.commitInt(Constant.PREFERENCE_LOG_FILE, file_log_num);
            }
            return true;
        }
    }

    /** 请使用getLocDir() */
    @Deprecated
    private String logDirPath;

    public String getLogDir() {
        if (logDirPath == null) {
            initDiskCacheDir(context);
        }
        return logDirPath;
    }

    private boolean initDiskCacheDir(Context context) {
        File logDir = getDiskCacheDir(context, DIR_LOG);
        if (logDir == null) {
            return false;
        } else {
            logDirPath = logDir.getAbsolutePath();
            return true;
        }
    }

    private boolean isFileFull(File file, int logSize) {
        if (file.exists() && file.isFile() && file.length() + logSize > MAX_FILE_SIZE) {
            // file存在，并且是一个文件，并且file的大小+log大小超过单个日志文件最大值MAX_FILE_SIZE
            // 文件已经满了
            return true;
        }
        return false;
    }

    private String currentFilePath = null;
    private synchronized void writeToFile(File file, String log, boolean append) {
        try {
            if (file == null) {
                return;
            }
            File parentFile = file.getParentFile();
            boolean isFileExists = true;
            if (!parentFile.exists()) {
                isFileExists = parentFile.mkdirs();
            }
            if (!isFileExists) {
                throw (new Exception("无法创建目录" + file.getAbsolutePath()));
            }
            if (!file.isFile()) {
                isFileExists = file.createNewFile();
            }
            if (!isFileExists) {
                throw (new Exception("无法创建文件" + file.getAbsolutePath()));
            }
            currentFilePath = file.getAbsolutePath();
            FileWriter writer = new FileWriter(file, append);
            writer.write(log);
            writer.flush();
            writer.close();
        } catch (Exception e) {
            CrashReport.postCatchedException(e);
        }
    }

    private static File getDiskCacheDir(Context context, String uniqueName) {
        // Check if media is mounted or storage is built-in, if so, try and use
        // external cache dir
        // otherwise use internal cache dir
        try {
            File cacheDir = null;
            if (cacheDir == null) {
                cacheDir = context.getFilesDir();
            }

            if (cacheDir != null) {
                File result = new File(cacheDir, uniqueName);
                if (!result.mkdirs() && (!result.exists() || !result.isDirectory())) {
                    return null;
                }
                return result;
            }
        } catch (Exception e) {
            CrashReport.postCatchedException(e);
        }
        return null;
    }

    public static boolean isExternalStorageRemovable() {
        if (hasGingerbread()) {
            return Environment.isExternalStorageRemovable();
        }
        return true;
    }

    private static File getExternalCacheDir(Context context) {
        if (hasFroyo()) {
            return context.getExternalCacheDir();
        }
        final String cacheDir = "/Android/data/" + context.getPackageName() + "/cache/";
        return new File(Environment.getExternalStorageDirectory().getPath() + cacheDir);
    }

    private static boolean hasGingerbread() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD;
    }

    private static boolean hasFroyo() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO;
    }

    public static String getFileMethodLine() {
        String str = "";
        str += "File: " + Thread.currentThread().getStackTrace()[2].getFileName();//文件名
        str += ",Method: " + Thread.currentThread().getStackTrace()[2].getMethodName();//函数名
        str += ",Line: " + Thread.currentThread().getStackTrace()[2].getLineNumber();//行号
        return str;
    }
}