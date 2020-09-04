package com.coomix.app.all.util;

import android.content.Context;
import com.goome.gpns.utils.FileUtils;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * This util observes the cache and file dirs of this app, both on internal and external storage card.
 * Calculate the total size of cache, and delete them all.
 * Created by ly on 2017/7/11.
 */
public class CacheCleaner {
    private List<String> observePathList = new ArrayList<>();
    private static CacheCleaner mCacheCleaner;
    private Context mContext;

    private CacheCleaner() {
    }

    private CacheCleaner(Context context) {
        mContext = context;
    }

    private synchronized CacheCleaner initSetting() {
        observePathList = new ArrayList<>(4);
        observePathList.add(mContext.getCacheDir().getAbsolutePath());
        //observePathList.add(mContext.getFilesDir().getAbsolutePath());
        File fc = mContext.getExternalCacheDir();
        if (fc != null && fc.exists()) {
            observePathList.add(fc.getAbsolutePath());
        }

        //不清除数据库
        //清除地图缓存 new
        File ff = mContext.getExternalFilesDir(null);
        if (ff != null && ff.exists()) {
            observePathList.add(ff.getAbsolutePath() + "/BaiduMapSDKNew/cache");
        }
        return this;
    }

    /**
     * @param context should be app context
     */
    public static CacheCleaner with(Context context) {
        if (mCacheCleaner == null) {
            synchronized (CacheCleaner.class) {
                if (mCacheCleaner == null) {
                    mCacheCleaner = new CacheCleaner(context);
                }
            }
        }
        return mCacheCleaner;
    }

    //in bytes
    public long getFileSize() {
        initSetting();
        long size = 0;
        for (String path : observePathList) {
            size += FileUtils.getFolderSize(path);
        }

        return size;
    }

    public void cleanCache() {
        for (String path : observePathList) {
            FileUtils.deleteFile(path);
        }
    }
}
