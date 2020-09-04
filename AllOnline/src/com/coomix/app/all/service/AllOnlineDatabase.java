/*
 * Copyright (C) 2011 iBoxPay.com
 *
 * $Id: IBoxpayDatabase.java 269 2012-04-26 07:53:07Z huangpengfei $
 * 
 * Description: 
 *
 */

package com.coomix.app.all.service;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

import com.coomix.app.all.model.bean.Account;
import com.coomix.app.all.model.bean.WeiZhang;

import java.util.ArrayList;

public class AllOnlineDatabase
{
    private static final String DATABASE_NAME = "CarOnline_db";
    private static final String TAG = "AllOnlineDatabase";
    private static final int DATABASE_VERSION = 1;

    private static final int IS_EXISTS = 909;

    private static final int IS_DONOTUPDATE = -1;

    private static AllOnlineDatabase instance = null;

    private static DatabaseHelper mOpenHelper = null;

    /**
     * SQLiteOpenHelper
     */
    private static class DatabaseHelper extends SQLiteOpenHelper {

        // Construct
        public DatabaseHelper(Context context, String name, CursorFactory factory, int version) {
            super(context, name, factory, version);
        }

        public DatabaseHelper(Context context) {
            this(context, DATABASE_NAME, DATABASE_VERSION);
        }

        public DatabaseHelper(Context context, String name, int version) {
            this(context, name, null, version);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(Account.CREATE_TABLE);
            db.execSQL(WeiZhang.CREATE_TABLE);
        }

        @Override
        public synchronized void close() {
            super.close();
        }

        @Override
        public void onOpen(SQLiteDatabase db) {
            super.onOpen(db);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            if (newVersion > oldVersion) {
                switch (oldVersion) {
                case 1:

                default:
                    break;
                }
            }
        }
    }

    private AllOnlineDatabase(Context context) {
        mOpenHelper = new DatabaseHelper(context);
    }

    public static synchronized AllOnlineDatabase getInstance(Context context) {
        if (null == instance) {
            return new AllOnlineDatabase(context);
        }
        return instance;
    }

    public void close() {
        if (null != instance) {
            mOpenHelper.close();
            instance = null;
        }
    }

    public void clearData() {
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        db.execSQL("DELETE FROM " + Account.TABLE_NAME);
    }

    public static boolean isTableExists(SQLiteDatabase db, String tableName) {
        if (tableName == null || db == null || !db.isOpen()) {
            return false;
        }
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM sqlite_master WHERE type = ? AND name = ?", new String[] { "table", tableName });
        if (!cursor.moveToFirst()) {
            return false;
        }
        int count = cursor.getInt(0);
        cursor.close();
        return count > 0;
    }

    public static boolean isColumnExists(SQLiteDatabase db, String tblName, String columnName) {
        if (tblName == null || db == null || !db.isOpen()) {
            return false;
        }
        Cursor cursor = db.query(tblName, null, null, null, null, null, null);
        int index = cursor.getColumnIndex(columnName);
        cursor.close();
        return (index != -1);

    }

    public boolean isExists(String tablename, String colums, String where) {
        SQLiteDatabase mDb = mOpenHelper.getReadableDatabase();
        boolean result = false;
        Cursor cursor = mDb.query(tablename, new String[] { BaseColumns._ID }, colums + "=? ", new String[] { where }, null, null, null);
        if (cursor != null && cursor.getCount() > 0) {
            result = true;
        }
        cursor.close();
        return result;
    }

    public long insertWeiZhang(WeiZhang model) {

        SQLiteDatabase mDb = mOpenHelper.getWritableDatabase();
        if (isExists(WeiZhang.TABLE_NAME, WeiZhang.FIELD_chepai, model.chepai_number)) {
            return updateWeiZhangTable(model);
        } else {
        }

        ContentValues initialValues = new ContentValues();
        initialValues.put(WeiZhang.FIELD_sname, model.short_name);
        initialValues.put(WeiZhang.FIELD_chepai, model.chepai_number);
        initialValues.put(WeiZhang.FIELD_chejia, model.chejia_number);
        initialValues.put(WeiZhang.FIELD_engine, model.engine_number);

        return mDb.insert(WeiZhang.TABLE_NAME, null, initialValues);

    }

    public int updateWeiZhangTable(WeiZhang model) {
        SQLiteDatabase mDb = mOpenHelper.getWritableDatabase();
        if (isExists(WeiZhang.TABLE_NAME, WeiZhang.FIELD_chepai, model.chepai_number)) {
            String where = WeiZhang.FIELD_chepai + " =? ";
            ContentValues args = new ContentValues();
            args.put(WeiZhang.FIELD_sname, model.short_name);
            args.put(WeiZhang.FIELD_chepai, model.chepai_number);
            args.put(WeiZhang.FIELD_chejia, model.chejia_number);
            args.put(WeiZhang.FIELD_engine, model.engine_number);
            return mDb.update(WeiZhang.TABLE_NAME, args, where, new String[] { model.chepai_number });
        } else {
            return IS_DONOTUPDATE;
        }
    }

    // account
    public long insertAccount(Account model) {
        SQLiteDatabase mDb = mOpenHelper.getWritableDatabase();
        if (isExists(Account.TABLE_NAME, Account.FIELD_ACCOUNT, model.account)) {
            return updateAccountTable(model);
        }
        ContentValues initialValues = new ContentValues();
        initialValues.put(Account.FIELD_ACCOUNT, model.account);
        initialValues.put(Account.FIELD_PASSWORD, model.password);
        initialValues.put(Account.FIELD_TIME, model.time);

        return mDb.insert(Account.TABLE_NAME, null, initialValues);
    }

    public int updateAccountTable(Account model) {
        SQLiteDatabase mDb = mOpenHelper.getWritableDatabase();
        if (isExists(Account.TABLE_NAME, Account.FIELD_ACCOUNT, model.account)) {
            String where = Account.FIELD_ACCOUNT + " =? ";
            ContentValues args = new ContentValues();
            args.put(Account.FIELD_ACCOUNT, model.account);
            args.put(Account.FIELD_PASSWORD, model.password);
            args.put(Account.FIELD_TIME, model.time);
            return mDb.update(Account.TABLE_NAME, args, where, new String[] { model.account });
        } else {
            return IS_DONOTUPDATE;
        }
    }

    public Cursor getAccount(String user) {
        SQLiteDatabase mDb = mOpenHelper.getWritableDatabase();
        String where = Account.FIELD_ACCOUNT + " =? ";
        return mDb.query(Account.TABLE_NAME, Account.TABLE_COLUMNS, where, new String[] { user }, null, null, null);
    }

    public ArrayList<WeiZhang> getWeiZhangListory() {
        try {

            SQLiteDatabase mDb = mOpenHelper.getReadableDatabase();

            // 判断是否存在表名
            if (isTableExists(mDb, WeiZhang.TABLE_NAME)) {
                // 存在表
            } else {
                SQLiteDatabase mNDb = mOpenHelper.getWritableDatabase();
                mNDb.execSQL(WeiZhang.CREATE_TABLE);
            }

            Cursor cursor = mDb.query(WeiZhang.TABLE_NAME, WeiZhang.TABLE_COLUMNS, null, null, null, null, null, null);

            ArrayList<WeiZhang> listAccounts = new ArrayList<WeiZhang>();
            if (cursor != null && cursor.getCount() > 0) {
                cursor.moveToFirst();
                for (int i = 0; i < cursor.getCount(); ++i) {
                    listAccounts.add(WeiZhang.parseCursor(cursor));
                    cursor.moveToNext();
                }
            }
            if (cursor != null) {
                cursor.close();
            }
            return listAccounts;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;

    }

    public ArrayList<Account> getAllAccounts() {
        SQLiteDatabase mDb = mOpenHelper.getReadableDatabase();
        //Cursor cursor = mDb.query(Account.TABLE_NAME, Account.TABLE_COLUMNS, null, null, null, null, Account.FIELD_TIME + " DESC", "5");
        Cursor cursor = mDb.query(Account.TABLE_NAME, Account.TABLE_COLUMNS, null, null, null, null, Account.FIELD_TIME + " DESC", null);
        ArrayList<Account> listAccounts = new ArrayList<Account>();
        if (cursor != null && cursor.getCount() > 0) {
            cursor.moveToFirst();
            for (int i = 0; i < cursor.getCount(); ++i) {
                listAccounts.add(Account.parseCursor(cursor));
                cursor.moveToNext();
            }
        }
        if (cursor != null) {
            cursor.close();
        }
        return listAccounts;
    }

    public int deleteAccount(String user) {
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        String where = Account.FIELD_ACCOUNT + " =? ";
        return db.delete(Account.TABLE_NAME, where, new String[] { user });
    }

    public int deleteAllLoginAccount() {
        SQLiteDatabase mDb = mOpenHelper.getWritableDatabase();
        return mDb.delete(Account.TABLE_NAME, null, null);
    }

}
