package com.goome.gpns.contentprovider;

import com.goome.gpns.utils.LogUtil;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class ProviderDBHelper extends SQLiteOpenHelper {

    public ProviderDBHelper(Context context) {
        super(context, ProviderConst.DBNAME, null, ProviderConst.VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String sqlStr = "create table " + ProviderConst.TIMESTAMP_TABLE + "(" + ProviderConst.ID
                + " integer primary key autoincrement not null," + ProviderConst.LAST_NOTIFY + " text,"
                + ProviderConst.AUDIO_URI + " text);";
        LogUtil.e(sqlStr);
        db.execSQL(sqlStr);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        LogUtil.e("oldVersion=" + oldVersion + ",newVersion=" + newVersion);
        db.execSQL("DROP TABLE IF EXISTS " + ProviderConst.TIMESTAMP_TABLE);
        onCreate(db);
    }

    public boolean update(String key, String value) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(key, value);
        long rowId = db.update(ProviderConst.TIMESTAMP_TABLE, values, null, null);
        boolean result = false;
        String msg = "";
        if (rowId != -1) {
            msg = "更新" + key;
            result = true;
        } else {
            msg = key + "更新失败";
            result = false;
        }
        // db.close();
        LogUtil.d(msg);
        return result;
    }

    public boolean insert(String key, String value) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(key, value);
        long rowId = db.insert(ProviderConst.TIMESTAMP_TABLE, null, values);
        boolean result = false;
        String msg = "";
        if (-1 != rowId) {
            msg = key + "|插入成功的行数：" + rowId;
            result = true;
        } else {
            msg = key + "插入失败";// ("+value +")"+DateUtil.formatTime(value)
            result = false;
        }
        LogUtil.d(msg);
        return result;
    }

    public static SQLiteDatabase open(Context context) {
        ProviderDBHelper dblite = new ProviderDBHelper(context);
        SQLiteDatabase database = dblite.getWritableDatabase();
        return database;
    }

}
