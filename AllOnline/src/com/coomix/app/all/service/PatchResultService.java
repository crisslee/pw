package com.coomix.app.all.service;

import android.os.Handler;
import android.os.Looper;

import com.coomix.app.all.tinker.TinkerUtil;
import com.tencent.tinker.lib.service.DefaultTinkerResultService;
import com.tencent.tinker.lib.service.PatchResult;
import com.tencent.tinker.lib.util.TinkerLog;
import com.tencent.tinker.lib.util.TinkerServiceInternals;

import java.io.File;

/**
 * optional, you can just use DefaultTinkerResultService
 * we can restart process when we are at background or screen off
 *
 * @author qiufei
 * @since 2016/11/5
 */
public class PatchResultService extends DefaultTinkerResultService {
    private static final String TAG = PatchResultService.class.getSimpleName();

    @Override
    public void onPatchResult(final PatchResult result) {
        if (result == null) {
            TinkerLog.e(TAG, "SampleResultService received null result!!!!");
            return;
        }
        TinkerLog.i(TAG, "SampleResultService receive result: %s", result.toString());

        //first, we want to kill the recover process
        TinkerServiceInternals.killTinkerPatchServiceProcess(getApplicationContext());

        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (result.isSuccess) {
                    TinkerLog.i(TAG, "patch success, please restart process");
                    // Toast.makeText(getApplicationContext(), "patch success, please restart process", Toast
                    // .LENGTH_LONG)
                    //      .show();
                } else {
                    TinkerLog.i(TAG, "patch fail, please check reason");
                    // Toast.makeText(getApplicationContext(), "patch fail, please check reason", Toast.LENGTH_LONG)
                    //      .show();
                }
            }
        });
        // is success and newPatch, it is nice to delete the raw file, and restart at once
        // for old patch, you can't delete the patch file
        if (result.isSuccess) {
            deleteRawPatchFile(new File(result.rawPatchFilePath));

            //not like TinkerResultService, I want to restart just when I am at background!
            //if you have not install tinker this moment, you can use TinkerApplicationHelper api
            if (checkIfNeedKill(result)) {
                if (TinkerUtil.isBackground(getApplicationContext())) {
                    TinkerLog.i(TAG, "it is in background, just restart process");
                    restartProcess();
                } else {
                    //we can wait process at background, such as onAppBackground
                    //or we can restart when the screen off
                    TinkerLog.i(TAG, "tinker wait screen to restart process");
                    new TinkerUtil.ScreenState(getApplicationContext(), new TinkerUtil.ScreenState.IOnScreenOff() {
                        @Override
                        public void onScreenOff() {
                            restartProcess();
                        }
                    });
                }
            } else {
                TinkerLog.i(TAG, "I have already install the newly patch version!");
            }
        }
    }

    /**
     * you can restart your process through service or broadcast
     */
    private void restartProcess() {
        TinkerLog.i(TAG, "app is background now, i can kill quietly");
        //you can send service or broadcast intent to restart your process
        android.os.Process.killProcess(android.os.Process.myPid());
    }
}