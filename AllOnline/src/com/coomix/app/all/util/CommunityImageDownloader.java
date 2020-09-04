package com.coomix.app.all.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.HandlerThread;
import com.coomix.app.all.model.bean.CommunityShare;

/**
 * Created by Administrator on 2016/11/18.
 */
public class CommunityImageDownloader {
    private HandlerThread handlerThread;
    private Handler workHandler;
    private Bitmap shareBitmap = null;
    private ShareImageDownloadListener listener;

    private void initSonThread() {
        handlerThread = new HandlerThread("work");
        handlerThread.start();
        workHandler = new Handler(handlerThread.getLooper());
    }

    public void downloadSharePicture(final Context context, final CommunityShare share) {
        if (share == null) {
            return;
        }
        downloadPicture(context, share.getPic());
    }

    public void downloadPicture(final Context context, final String picUrl) {
        if (CommunityUtil.isEmptyTrimStringOrNull(picUrl)) {
            return;
        }
        if (handlerThread == null) {
            initSonThread();
        }
        workHandler.post(new Runnable() {
            @Override
            public void run() {
                shareBitmap = CommunityPictureUtil.downloadPicByUrl(context, picUrl);
                if (listener != null) {
                    listener.onDownloadFinished(shareBitmap);
                }
            }
        });
    }

    public void release() {
        if (workHandler != null) {
            workHandler.removeCallbacksAndMessages(null);
            workHandler = null;
        }
        if (handlerThread != null) {
            handlerThread.quit();
            handlerThread = null;
        }
        if (shareBitmap != null) {
            shareBitmap.recycle();
            shareBitmap = null;
        }
        if (listener != null) {
            listener = null;
        }
    }

    public Bitmap getDownloadedBitmap() {
        return shareBitmap;
    }

    public void setDownloadListener(ShareImageDownloadListener l) {
        this.listener = l;
    }

    public interface ShareImageDownloadListener {
        public void onDownloadFinished(Bitmap bitmap);
    }
}
