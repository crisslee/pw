package com.goome.gpns.contentprovider;

import com.goome.gpns.service.GPNSService;
import com.goome.gpns.utils.CommonUtil;
import com.goome.gpns.utils.LogUtil;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

public class GpnsProvider extends ContentProvider {
    private static  UriMatcher sMatcher;
    ProviderDBHelper dBlite;
    SQLiteDatabase db;

    public static Uri initProviderParams(Context context){
        LogUtil.i("initProviderParams");
        ProviderConst.AUTHORITY = CommonUtil.readValueFromProperty(context, GPNSService.CONFIG_FILE_NAME,
            "provider_authorities");
        ProviderConst.CONTENT_URI = Uri.parse("content://" + ProviderConst.AUTHORITY + "/" + ProviderConst.TIMESTAMP_TABLE);
        LogUtil.i("AUTHORITY="+ProviderConst.AUTHORITY+",CONTENT_URI="+ProviderConst.CONTENT_URI);

        sMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        sMatcher.addURI(ProviderConst.AUTHORITY, ProviderConst.TIMESTAMP_TABLE, ProviderConst.ITEM);
        sMatcher.addURI(ProviderConst.AUTHORITY, ProviderConst.TIMESTAMP_TABLE + "/#", ProviderConst.ITEM_ID);

        return ProviderConst.CONTENT_URI;
    }

    @Override
    public boolean onCreate() {
        LogUtil.i("onCreate");
        Context context = this.getContext();
        initProviderParams(context);
        this.dBlite = new ProviderDBHelper(context);
        return true;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        LogUtil.i("delete");
        db = dBlite.getWritableDatabase();
        int count = 0;
        switch (sMatcher.match(uri)) {
            case ProviderConst.ITEM:
                count = db.delete(ProviderConst.TIMESTAMP_TABLE, selection, selectionArgs);
                break;
            case ProviderConst.ITEM_ID:
                String id = uri.getPathSegments().get(1);
                count = db.delete(
                    ProviderConst.ID,
                    ProviderConst.ID
                        + "="
                        + id
                        + (!TextUtils.isEmpty(ProviderConst.ID = "?") ? "AND("
                        + selection + ')' : ""), selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI" + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }

    @Override
    public String getType(Uri uri) {
        LogUtil.i("getType");
        switch (sMatcher.match(uri)) {
            case ProviderConst.ITEM:
                return ProviderConst.CONTENT_TYPE;
            case ProviderConst.ITEM_ID:
                return ProviderConst.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalArgumentException("Unknown URI" + uri);
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        return uri;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
        String[] selectionArgs, String sortOrder) {
        db = dBlite.getWritableDatabase();
        Cursor c;
        Log.d("-------", String.valueOf(sMatcher.match(uri)));
        c = db.query(ProviderConst.TIMESTAMP_TABLE, projection, selection, selectionArgs,
            null, null, null);
        c.setNotificationUri(getContext().getContentResolver(), uri);
        return c;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
        String[] selectionArgs) {
        LogUtil.i("update");
        SQLiteDatabase db = dBlite.getWritableDatabase();
        int l = db.update(ProviderConst.TIMESTAMP_TABLE, values, null, null);
        return l;
    }

}
