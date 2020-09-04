package com.goome.gpns.contentprovider;

import android.content.Context;
import android.net.Uri;

public class ProviderConst {
    public static final String DBNAME = "gpnssdk";
    public static final String TIMESTAMP_TABLE = "timestamp";
    public static final int VERSION = 6;
    public static final int ITEM = 1;
    public static final int ITEM_ID = 2;
    public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.goome.gpns";
    public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.goome.gpns";
    public static String ID = "id";
    public static String LAST_NOTIFY = "lastnotify"; // 保存最近一次C库正常运行的时间戳
    public static String AUDIO_URI = "audioUri"; // 保存通知音uri
    public static String AUTHORITY = null;
    static Uri CONTENT_URI = null;

    public static Uri getContentUri(Context context) {
        if (null == CONTENT_URI) {
            CONTENT_URI = GpnsProvider.initProviderParams(context);
        }
        return CONTENT_URI;
    }
}
