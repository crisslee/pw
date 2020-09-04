package com.coomix.app.all.ui.update;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import com.coomix.app.all.AllOnlineApp;
import com.coomix.app.all.log.GLog;
import com.coomix.app.all.log.GoomeLog;
import com.coomix.app.all.service.AllOnlineApiClient;
import com.coomix.app.all.service.DownloadingService;
import com.coomix.app.all.service.ServiceAdapter;
import com.coomix.app.framework.app.Result;
import com.coomix.app.framework.util.PreferenceUtil;
import com.coomix.app.all.util.BitmapUtils;
import com.coomix.app.all.util.Utils;
import java.io.File;

/**
 * Created by Administrator on 2016/4/19.
 */
public class GoomeUpdateAgent {
    private static final String TAG = GoomeUpdateAgent.class.getSimpleName();
    private static GoomeUpdateListener mUpdateListener = null;
    private static ServiceAdapter mServiceAdapter;

    private static Handler mHandler = new Handler(Looper.getMainLooper());

    public static void setUpdateListener(GoomeUpdateListener updateListener) {
        mUpdateListener = updateListener;
    }

    public static void update(final Context context) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                AllOnlineApiClient client = new AllOnlineApiClient(context);
                Result result = client.getLatestVersion();
                if (result.statusCode == Result.OK && mUpdateListener != null && result.mResult != null) {
                    GoomeUpdateInfo updateInfo = (GoomeUpdateInfo) result.mResult;
                    AllOnlineApp.gUpdateInfo = updateInfo;
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            mUpdateListener.onUpdateReturned(updateInfo.update ? GoomeUpdateStatus.Yes
                                : GoomeUpdateStatus.No, updateInfo);
                        }
                    });
                } else {
                    //检查更新失败，"+result.errorMessage
                    GoomeLog.getInstance().logE("CheckUpdate", "检查更新失败，" + result.errorMessage, 0);
                }
            }
        }).start();
    }

    public static void showUpdateDialog(Context context, GoomeUpdateInfo updateInfo) {
        int ignore = PreferenceUtil.getInt(GoomeUpdateConstant.KEY_lIST_PREFERENCE_IGNORE_VERSION_CODE, -1);
        if (context != null && updateInfo != null && updateInfo.update) {
            if (Integer.parseInt(updateInfo.verCode) != ignore) {
                Intent intent = new Intent(context, GoomeUpdateDialogActivity.class);
                intent.putExtra("updateInfo", updateInfo);
                context.startActivity(intent);
            }
        }
    }

    public static void startDownload(Context context, GoomeUpdateInfo updateInfo, boolean onlyWifi) {
        if (updateInfo != null && BitmapUtils.getAvailableSDcard2(context)) {
            String path = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath()
                + File.separator + context.getApplicationInfo().packageName + "_" + updateInfo.verCode + "_"
                + updateInfo.getNewMd5() + ".apk";

            File apk = new File(path);
            GLog.d(TAG, ">>> startDownload apk path=" + path);
            GLog.d(TAG, "apk exists=" + apk.exists() + ", updateinfo.vercode=" + updateInfo.verCode);
            Intent intent = new Intent(context, DownloadingService.class);
            Bundle mBundle = new Bundle();
            mBundle.putParcelable(DownloadingService.KEY_UPDATE_INFO, updateInfo);
            mBundle.putString(DownloadingService.KEY_FILE_PATH, path);
            mBundle.putBoolean(DownloadingService.KEY_ONLY_WIFI, onlyWifi);
            intent.putExtras(mBundle);
            context.startService(intent);
        }
    }

    public static void startPatchDownload(Context context, GoomeUpdateInfo updateInfo) {
        GLog.i(TAG, "start download patch");
        // 下载路径
        String patchPath = null;
        if (updateInfo != null && BitmapUtils.getAvailableSDcard2(context, false)) {
            patchPath = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath()
                + File.separator + context.getApplicationInfo().packageName + "_"
                + Utils.getInstance().getAppVersionCode() + "_patch_" + updateInfo.patchCode + ".apk";
            GLog.i(TAG, "patch path = " + patchPath + "\n" + updateInfo.toString());
        }

        if (patchPath != null) {
            File patchFile = new File(patchPath);
            GLog.i(TAG, ">>> startDownload patch path=" + patchPath);
            GLog.i(TAG, "startDownload:\npatchFile.exist=" + patchFile.exists() + "\nupdateinfo.vercode="
                + updateInfo.verCode + "\nupdateinfo.patchCode=" + updateInfo.patchCode);

            Intent intent = new Intent(context, DownloadingService.class);
            Bundle mBundle = new Bundle();
            mBundle.putParcelable(DownloadingService.KEY_UPDATE_INFO, updateInfo);
            mBundle.putString(DownloadingService.KEY_FILE_PATH, patchPath);
            mBundle.putBoolean(DownloadingService.KEY_ONLY_WIFI, false);
            mBundle.putBoolean(DownloadingService.KEY_UPDATE_NOTIFY, false);
            mBundle.putBoolean(DownloadingService.KEY_IS_PATCH, true);
            intent.putExtras(mBundle);
            context.startService(intent);
        }
    }
}
