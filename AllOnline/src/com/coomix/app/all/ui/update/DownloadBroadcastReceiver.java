package com.coomix.app.all.ui.update;

import android.app.ActivityManager;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import com.coomix.app.all.service.CheckVersionService;
import com.coomix.app.all.util.StringUtil;
import com.coomix.app.framework.util.PreferenceUtil;
import com.coomix.app.all.util.BitmapUtils;
import java.io.File;

/**
 * Created by Administrator on 2016/4/20.
 */
public class DownloadBroadcastReceiver extends BroadcastReceiver {
    private static long reptId = 0;

    @Override
    public void onReceive(Context context, Intent intent) {
        try {
            PreferenceUtil.init(context);
            long downloadId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
            long updateDownloadId = PreferenceUtil.getLong(GoomeUpdateConstant.KEY_lIST_PREFERENCE_APK_DOWNLOAD_ID, 0);

            if (reptId != downloadId && downloadId == updateDownloadId && BitmapUtils.getAvailableSDcard2(context)) {
                reptId = downloadId;

                String path;
                path = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath()
                    + File.separator + GoomeUpdateConstant.DOWNLOAD_APK_FILE_NAME;

                File apk = new File(path);
                Uri uri = Uri.parse(Uri.fromFile(apk).toString());
                if (null != apk && apk.exists()) {
                    PreferenceUtil.commitBoolean(GoomeUpdateConstant.KEY_lIST_PREFERENCE_DOWNLOAD_COMPLETE, true);

                    if (isRunningForeground(context)) {
                        //安装
                        Intent installIntent = new Intent(Intent.ACTION_VIEW);
                        installIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        installIntent.setDataAndType(uri, "application/vnd.android.package-archive");
                        context.startActivity(installIntent);

                        CheckVersionService.cancleNotify();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean isRunningForeground(Context context) {
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        ComponentName cn = am.getRunningTasks(1).get(0).topActivity;
        String currentPackageName = cn.getPackageName();
        if (!StringUtil.isTrimEmpty(currentPackageName) && currentPackageName.equals(context.getPackageName())) {
            return true;
        }
        return false;
    }
}
