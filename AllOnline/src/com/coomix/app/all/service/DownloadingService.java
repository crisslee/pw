package com.coomix.app.all.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.FileProvider;
import android.text.TextUtils;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.Toast;
import com.coomix.app.all.Constant;
import com.coomix.app.all.R;
import com.coomix.app.all.log.GLog;
import com.coomix.app.all.log.GoomeLog;
import com.coomix.app.all.ui.update.GoomeUpdateConstant;
import com.coomix.app.all.ui.update.GoomeUpdateInfo;
import com.coomix.app.framework.util.PreferenceUtil;
import com.coomix.app.all.util.Md5Util;
import com.coomix.app.all.util.Utils;
import com.tencent.tinker.lib.tinker.TinkerInstaller;
import com.umeng.analytics.MobclickAgent;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

public class DownloadingService extends Service {
    private static final String TAG = "DownloadingService";
    public static final String KEY_UPDATE_INFO = "updateInfo";
    public static final String KEY_FILE_PATH = "path";
    public static final String KEY_ONLY_WIFI = "onlyWifi";
    public static final String KEY_UPDATE_NOTIFY = "updateNotify";
    public static final String KEY_IS_PATCH = "patch";

    // 是否正在下载 true下载 false停止下载
    private boolean isDownloading = false;
    // 是否出错
    private boolean isError = false;
    private String errMsg = "";

    // 是否下载完成
    private boolean isFinished = false;
    // 保存文件名
    private String apkFilePath = "";
    // patch文件路径
    private String patchFilePath = "";

    /**
     * 下载进度
     */
    private int loadingProcess;
    private NotificationManager notificationMgr;
    /**
     * 上次更新进度
     */
    private int old_process = -1;
    private boolean isFirstStart = false;// 逻辑未实现
    /**
     * 是否更新通知栏
     */
    private boolean isUpdateNotify = true;
    private Notification notification;
    private WifiReceiver mWifiReceiver = new WifiReceiver();
    /**
     * 是否只允许通过WIFI下载
     */
    private boolean mOnlyWifi = true;
    private GoomeUpdateInfo mUpdateInfo = null;

    // msg type
    private static final int MSG_APK_DOWNLOAD_FAIL = -1;
    private static final int MSG_APK_VERIFY_FAIL = -2;
    private static final int MSG_UPDATE_PROGRESS = 1;
    private static final int MSG_APK_DOWNLOAD_SUCCESS = 2;
    private static final int MSG_PATCH_DOWNLOAD_SUCCESS = 3;
    private static final int MSG_PATCH_DOWNLOAD_FAIL = 4;

    // file type
    private static final int FILE_TYPE_APK = 50;
    private static final int FILE_TYPE_PATCH = 51;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        notificationMgr = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
        IntentFilter mFilter = new IntentFilter();
        mFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(mWifiReceiver, mFilter);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            if (intent.hasExtra("stop") && intent.getBooleanExtra("stop", true)) {
                isDownloading = false;
                stopSelf();
            } else if (intent.hasExtra("retry") && intent.getBooleanExtra("retry", true)) {
                retry();
            } else if (intent.hasExtra(KEY_IS_PATCH) && intent.getBooleanExtra(KEY_IS_PATCH, true)) {
                GoomeUpdateInfo updateInfo = intent.getParcelableExtra(KEY_UPDATE_INFO);
                String path = intent.getStringExtra(KEY_FILE_PATH);
                updateNewPatch(updateInfo, path);
            } else {
                if (!isDownloading) {
                    GoomeUpdateInfo updateInfo = intent.getParcelableExtra(KEY_UPDATE_INFO);
                    String path = intent.getStringExtra(KEY_FILE_PATH);
                    boolean onlyWifi = intent.getBooleanExtra(KEY_ONLY_WIFI, true);
                    boolean isUpdateNotify = intent.getBooleanExtra(KEY_UPDATE_NOTIFY, true);

                    if (updateInfo != null) {
                        updateNewVersion(updateInfo, path, onlyWifi, isUpdateNotify);
                    }
                }
            }
        }
        return Service.START_NOT_STICKY;
    }

    public synchronized void updateNewVersion(final GoomeUpdateInfo updateInfo, String path,
        boolean onlyWifi, boolean updateNotify) {
        // 正在下载 或者 app没有更新
        if (isDownloading || (updateInfo != null && !updateInfo.isUpdate())) {
            GLog.i(TAG, "isUpdate=" + updateInfo.isUpdate());
            return;
        }

        mUpdateInfo = updateInfo;
        apkFilePath = path;
        mOnlyWifi = onlyWifi;
        isUpdateNotify = updateNotify;
        isError = false;
        isDownloading = true;
        isFinished = false;
        old_process = -1;
        errMsg = "";

        PreferenceUtil.commitBoolean(GoomeUpdateConstant.KEY_lIST_PREFERENCE_DOWNLOAD_COMPLETE_NEW, false);

        new Thread() {
            public void run() {
                GLog.i(TAG, "下载新版本");
                if (updateInfo != null && !TextUtils.isEmpty(updateInfo.url)) {
                    downloadApkFile(updateInfo.url);
                } else {
                    notificationHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(DownloadingService.this, "更新地址出错了", Toast.LENGTH_SHORT)
                                .show();
                        }
                    });
                }
            }
        }.start();
    }

    // 线程中下载 patch
    public synchronized void updateNewPatch(final GoomeUpdateInfo updateInfo, final String path) {
        if (updateInfo != null) {
            mUpdateInfo = updateInfo;
        }
        patchFilePath = path;
        new Thread() {
            public void run() {
                Log.i(TAG, "start download new patch");
                if (updateInfo != null && !TextUtils.isEmpty(updateInfo.patchUrl)) {
                    downloadFile(updateInfo, updateInfo.patchUrl, path, FILE_TYPE_PATCH);
                } else {
                    Log.i(TAG, "patch url is error");
                }
            }
        }.start();
    }

    public void downloadApkFile(String url) {
        int bytesCopied = 0;

        try {
            File apkFile = new File(apkFilePath);

            if (apkFile.exists() && apkFile.length() > 0 && (mUpdateInfo == null || mUpdateInfo.newMd5 == null
                || mUpdateInfo.newMd5.equalsIgnoreCase(Md5Util.fileMD5(apkFilePath)))) {
                GLog.d(TAG, "file " + apkFile.getName() + " already exits!!");

                if (isDownloading) {
                    sendMsg(MSG_APK_DOWNLOAD_SUCCESS, 0);
                }

                return;
            }

            publishProgress(0, 1);// 先生成一个0%的进度通知，防止网络卡慢的情况下通知栏迟迟不出来的问题

            URL murl = new URL(url);
            URLConnection connection = murl.openConnection();
            int netSrcLength = connection.getContentLength();
            GLog.d(TAG, "netSrcLength=" + netSrcLength);

            // 如果上面md5校验没通过
            // 则把大于等于目标大小的文件删除，重新下载，而比较小的文件则断点续传
            if (apkFile.exists() && netSrcLength <= apkFile.length()) {
                apkFile.delete();
            }

            if (apkFile.exists() && netSrcLength > apkFile.length()) {
                GLog.d(TAG, "Downdoad from breakpoint: size=" + apkFile.length() + ", total=" + netSrcLength);
                publishProgress(0, netSrcLength);
                int progress = (int) apkFile.length();
                GLog.d(TAG, "progress=" + progress);
                URLConnection connection1 = murl.openConnection();
                connection1.setRequestProperty("RANGE", "bytes=" + progress + "-");
                bytesCopied = copyFromBreakPoint(connection1.getInputStream(), apkFile, progress, netSrcLength);
                if (bytesCopied != netSrcLength && netSrcLength != -1) {
                    GLog.d(TAG, "Download incomplete bytesCopied=" + apkFile.length() + ", length" + netSrcLength);
                }
            } else if (!apkFile.exists()) {
                GLog.d(TAG, "file=" + apkFile.getName() + " not exits!! netSrcLength=" + netSrcLength);
                URLConnection connection2 = murl.openConnection();
                publishProgress(0, netSrcLength);
                int progress = 0;
                bytesCopied = copy(connection2.getInputStream(), apkFile, progress, netSrcLength);
                if (bytesCopied != netSrcLength && netSrcLength != -1) {
                    GLog.d(TAG, "Download incomplete bytesCopied=" + bytesCopied + ", netSrcLength=" + netSrcLength);
                }
            }
        } catch (IOException e) {
            if (url.contains("qq.com")) {
                MobclickAgent.onEvent(getBaseContext(), "num_yingyongbao_download_failed");
                MobclickAgent.reportError(getBaseContext(), e);
            }
            sendMsg(MSG_APK_DOWNLOAD_FAIL, 0);
        }
    }

    public void downloadFile(GoomeUpdateInfo updateInfo, String url, String path, int fileType) {
        HttpURLConnection connGetLen = null;
        HttpURLConnection connDownload = null;
        try {
            File file = new File(path);

            URL mUrl = new URL(url);
            connGetLen = (HttpURLConnection) mUrl.openConnection();
            connGetLen.setConnectTimeout(5 * 1000);
            int length = connGetLen.getContentLength();
            connGetLen.disconnect();

            // 进行文件校验
            if (isIllegal(updateInfo, file, path, fileType, length)) {
                return;
            }

            // 下载
            long startPoint = 0;
            if (file.exists()) {
                startPoint = file.length();
            }
            connDownload = (HttpURLConnection) mUrl.openConnection();
            connDownload.setRequestProperty("RANGE", "bytes=" + startPoint + "-");
            InputStream input = connDownload.getInputStream();
            byte[] buffer = new byte[1024 * 8];
            RandomAccessFile output = new RandomAccessFile(file, "rw");
            output.seek(startPoint);
            int count;
            long downloadProgress = startPoint;
            while ((count = input.read(buffer)) != -1) {
                output.write(buffer, 0, count);
                downloadProgress += count;
            }
            input.close();
            output.close();
            connDownload.disconnect();

            // 判断下载是否完成
            String fileMd5 = Md5Util.fileMD5(path);
            if (fileType == FILE_TYPE_APK) {
                if (updateInfo.newMd5.equalsIgnoreCase(fileMd5)) {
                    sendMsg(MSG_APK_DOWNLOAD_SUCCESS, 0);
                } else {
                    Log.i(TAG, "download apkfile error, md5 mismatch");
                    sendMsg(MSG_APK_DOWNLOAD_FAIL, 0);
                }
            } else if (fileType == FILE_TYPE_PATCH) {
                if (updateInfo.patchNewMd5.equalsIgnoreCase(fileMd5)) {
                    sendMsg(MSG_PATCH_DOWNLOAD_SUCCESS, 0);
                } else {
                    Log.i(TAG, "download patchfile error, md5 mismatch");
                    sendMsg(MSG_PATCH_DOWNLOAD_FAIL, 0);
                }
            }
        } catch (IOException e) {
            Log.i(TAG, "exception when download patch");
            e.printStackTrace();
        } finally {
            if (connGetLen != null) {
                connGetLen.disconnect();
            }
            if (connDownload != null) {
                connDownload.disconnect();
            }
        }
    }

    /**
     * 校验目标文件
     *
     * @param updateInfo update info
     * @param file 目标文件
     * @param filePath 目标文件路径
     * @param fileType 目标文件类型
     * @param connLength 网络读取的目标文件长度
     * @return true-文件下载完成<p>false-文件下载未完成</p>
     */
    private boolean isIllegal(GoomeUpdateInfo updateInfo, File file, String filePath, int fileType,
        int connLength) {
        String md5FromNet = "";
        if (updateInfo != null) {
            switch (fileType) {
                case FILE_TYPE_APK:
                    md5FromNet = updateInfo.newMd5;
                    break;
                case FILE_TYPE_PATCH:
                    md5FromNet = updateInfo.patchNewMd5;
                    break;
                default:
                    break;
            }
        }
        try {
            // 文件已存在，并且 md5 合法
            if (file.exists() && file.length() > 0 && (mUpdateInfo == null || md5FromNet.equalsIgnoreCase(
                Md5Util.fileMD5(filePath)))) {
                GLog.d(TAG, "file=" + file.getName() + " already exits!!");

                if (fileType == FILE_TYPE_PATCH) {
                    sendMsg(MSG_PATCH_DOWNLOAD_SUCCESS, 0);
                } else if (fileType == FILE_TYPE_APK) {
                    sendMsg(MSG_APK_DOWNLOAD_SUCCESS, 0);
                }
                return true;
            }

            // 如果上面md5校验没通过，删除文件
            // 则把大于等于目标大小的文件删除，重新下载，而比较小的文件则断点续传
            if (file.exists() && connLength <= file.length()) {
                boolean deleteResult = file.delete();
                if (!deleteResult) {
                    GoomeLog.getInstance().logE(TAG, "MD5 check error and cannot delete it", 0);
                    return false;
                }
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
        return false;
    }

    /**
     * 更新进度
     */
    private void publishProgress(long total, float length) {
        if (length <= 0) {
            return;
        }

        loadingProcess = (int) (total * 100 / length);
        if (loadingProcess > old_process) {
            sendMsg(MSG_UPDATE_PROGRESS, loadingProcess);
            old_process = loadingProcess;
        }
    }

    private int copy(InputStream is, File file, int progress, float length) {
        int count;

        try {
            InputStream input = new BufferedInputStream(is);
            OutputStream output = new FileOutputStream(file);
            byte data[] = new byte[1024];

            while ((count = input.read(data)) != -1 && isDownloading) {
                progress += count;
                output.write(data, 0, count);
                publishProgress(progress, length);
            }
            GLog.i(TAG, "end while, progress=" + progress + ", length=" + length);

            output.flush();
            output.close();
            input.close();

            if (isDownloading && progress == length) {
                GLog.i(TAG, "net md5=" + mUpdateInfo.newMd5 + ", file md5=" + Md5Util.fileMD5(apkFilePath));
                if (mUpdateInfo == null || mUpdateInfo.newMd5 == null || mUpdateInfo.newMd5.equalsIgnoreCase(
                    Md5Util.fileMD5(apkFilePath))) {
                    sendMsg(MSG_APK_DOWNLOAD_SUCCESS, 0);
                } else {
                    deleteFile(file);
                    sendMsg(MSG_APK_VERIFY_FAIL, 0);
                    sendMsg(MSG_APK_DOWNLOAD_FAIL, 0);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            deleteFile(file);
            sendMsg(MSG_APK_DOWNLOAD_FAIL, 0);
        }

        return progress;
    }

    private int copyFromBreakPoint(InputStream input, File output, int progress, float length) {
        byte[] buffer = new byte[1024 * 8];
        int count = 0, n = 0;
        try {
            RandomAccessFile fos = new RandomAccessFile(output, "rw");
            fos.seek(output.length());

            while (isDownloading) {
                n = input.read(buffer);
                if (n <= 0) {
                    break;
                }
                fos.write(buffer, 0, n);
                progress += n;
                publishProgress(progress, length);
            }
            input.close();
            fos.close();

            if (isDownloading && progress == length) {
                if (mUpdateInfo == null || mUpdateInfo.newMd5 == null || mUpdateInfo.newMd5.equalsIgnoreCase(
                    Md5Util.fileMD5(apkFilePath))) {
                    sendMsg(MSG_APK_DOWNLOAD_SUCCESS, 0);
                } else {
                    deleteFile(output);
                    sendMsg(MSG_APK_VERIFY_FAIL, 0);
                    sendMsg(MSG_APK_DOWNLOAD_FAIL, 0);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            deleteFile(output);
            sendMsg(MSG_APK_DOWNLOAD_FAIL, 0);
        }

        return progress;
    }

    private boolean deleteFile(File file) {
        if (file != null && file.exists()) {
            return file.delete();
        }
        return false;
    }

    private void sendMsg(int what, int arg1) {
        Message msg = new Message();
        msg.what = what;
        msg.arg1 = arg1;
        uiHandler.sendMessage(msg);
    }

    // 定义一个Handler，用于处理下载线程与UI间通讯
    @SuppressWarnings("HandlerLeak")
    private Handler uiHandler = new Handler() {
        public void handleMessage(Message msg) {
            if (!Thread.currentThread().isInterrupted()) {
                switch (msg.what) {
                    case MSG_UPDATE_PROGRESS:
                        notificationHandler.sendEmptyMessage(0);
                        break;
                    case MSG_APK_DOWNLOAD_SUCCESS:
                        isDownloading = false;
                        isFinished = true;
                        PreferenceUtil.commitBoolean(GoomeUpdateConstant.KEY_lIST_PREFERENCE_DOWNLOAD_COMPLETE_NEW,
                            true);
                        if (isUpdateNotify) {
                            // wifi下自动下载，则下载完成时不会自动弹出安装提示
                            installApk();
                        }
                        break;
                    case MSG_APK_DOWNLOAD_FAIL:
                        isDownloading = false;
                        isError = true;
                        Log.i(TAG, "apk下载失败!");
                        notificationHandler.sendEmptyMessage(0);
                        GoomeLog.getInstance().logE(TAG, "apk下载失败!", 0);
                        break;
                    case MSG_APK_VERIFY_FAIL:
                        Toast.makeText(DownloadingService.this, "升级文件校验失败,请重新更新!",
                            Toast.LENGTH_LONG).show();
                        break;
                    case MSG_PATCH_DOWNLOAD_SUCCESS:
                        // patch 下载成功，安装
                        Log.i(TAG, "download patch success, try to apply");
                        TinkerInstaller.onReceiveUpgradePatch(getApplicationContext(),
                            patchFilePath);
                        GoomeLog.getInstance()
                            .logE(TAG, "download patch success, try to apply!", 0);
                        break;
                    case MSG_PATCH_DOWNLOAD_FAIL:
                        Log.i(TAG, "download patch failed");
                        GoomeLog.getInstance().logE(TAG, "download patch failed", 0);
                        break;
                    default:
                        break;
                }
            }
            super.handleMessage(msg);
        }
    };

    private void installApk() {
        try {
            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                i.setDataAndType(FileProvider.getUriForFile(getApplicationContext(), "com.coomix.app.all.file_provider",
                    new File(apkFilePath)), "application/vnd.android.package-archive");
                i.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            } else {
                i.setDataAndType(Uri.fromFile(new File(apkFilePath)), "application/vnd.android.package-archive");
            }
            startActivity(i);
        } catch (Exception e) {
            e.printStackTrace();
            int index = apkFilePath.lastIndexOf("/");
            if (index <= 0) {
                Toast.makeText(this, "安装升级包失败", Toast.LENGTH_SHORT).show();
                return;
            }
            String path = apkFilePath.substring(0, index);
            Toast.makeText(this, "安装升级包失败，请到" + path + "目录下手动进行安装", Toast.LENGTH_LONG).show();
        }
    }

    // 处理notification的 Handler
    @SuppressWarnings("HandlerLeak")
    private Handler notificationHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            // 1为出现，2为隐藏
            if (isUpdateNotify) {
                if (loadingProcess > 99) {
                    notificationMgr.cancel(0);
                    stopSelf();
                    return;
                }
                displayNotificationMessage(loadingProcess);
            } else {
                if (loadingProcess > 99) {
                    stopSelf();
                }
            }
        }
    };

    @Override
    public void onDestroy() {
        notificationMgr.cancel(0);
        if (mWifiReceiver != null) {
            unregisterReceiver(mWifiReceiver);
        }
        super.onDestroy();
    }

    private void displayNotificationMessage(int count) {
        Utils.createChannel(notificationMgr, Constant.channelIdUpdate, Constant.channelNameUpdate);
        if (notification == null) {
            Intent download = new Intent(this, DownloadingService.class);
            download.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            download.putExtra("retry", true);
            PendingIntent serviceIntent = PendingIntent.getService(this, 0, download, 0);
            notification = new Notification();

            NotificationCompat.Builder builder = new NotificationCompat.Builder(this, Constant.channelIdUpdate);
            builder.setSmallIcon(android.R.drawable.stat_sys_download);
            builder.setTicker(getResources().getString(R.string.app_name) + "更新进入后台下载！");
            builder.setWhen(System.currentTimeMillis());
            if (isFirstStart || loadingProcess > 97) {
                builder.setDefaults(Notification.DEFAULT_SOUND);
            }
            RemoteViews contentView = new RemoteViews(getPackageName(), R.layout.notification_version);
            builder.setCustomContentView(contentView);
            builder.setContentIntent(serviceIntent);
            notification = builder.build();
            notification.flags |= Notification.FLAG_ONGOING_EVENT;
        } else {
            notification.defaults = 0;
        }

        if (isError) {
            notification.contentView.setTextViewText(R.id.n_title, "下载失败 点击重试");
        } else {
            notification.contentView.setTextViewText(R.id.n_title, getResources().getString(R.string.app_name)
                + "新版本下载");
        }

        notification.contentView.setTextViewText(R.id.n_text, count + "%");
        notification.contentView.setProgressBar(R.id.n_progress, 100, count, false);
        notificationMgr.notify(0, notification);
    }

    public class WifiReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i(TAG, "intent=" + intent.getAction());
            ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo wifi = manager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
            NetworkInfo mobNetInfo = manager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);

            // wifi断开
            if (wifi != null && !wifi.isConnected()) {
                if (mOnlyWifi && isDownloading) {
                    isDownloading = false;
                }
            }

            if ((wifi != null && wifi.isConnected()) || (!mOnlyWifi && mobNetInfo != null
                && mobNetInfo.isConnected())) {
                // 断网连上后，判断是否需要开启下载
                if (!isDownloading && !isFinished && mUpdateInfo != null) {
                    updateNewVersion(mUpdateInfo, apkFilePath, mOnlyWifi, isUpdateNotify);
                }
            }
        }
    }

    public void retry() {
        ConnectivityManager manager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo wifi = manager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        NetworkInfo mobNetInfo = manager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);

        if ((wifi != null && wifi.isConnected()) || (!mOnlyWifi
            && mobNetInfo != null
            && mobNetInfo.isConnected())) {
            // 断网连上后，判断是否需要开启下载
            if (!isDownloading && !isFinished) {
                updateNewVersion(mUpdateInfo, apkFilePath, mOnlyWifi, isUpdateNotify);
            }
        }
    }
}