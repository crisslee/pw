package com.coomix.app.all.model.bean;

import android.database.Cursor;
import android.provider.BaseColumns;

import com.coomix.app.all.AllOnlineApp;

public class Account {

    public String account;

    public String password;

    public String time;

    public static final String TABLE_NAME = "AccountsTable";

    @Override
    public String toString() {
        return "Account [account=" + account + ", password=" + password + ", time=" + time + "]";
    }

    public static final String FIELD_ID = BaseColumns._ID;

    public static final String FIELD_ACCOUNT = "account";

    public static final String FIELD_PASSWORD = "password";

    public static final String FIELD_TIME = "time";

    public static final String[] TABLE_COLUMNS = new String[] { FIELD_ID, FIELD_ACCOUNT, FIELD_PASSWORD, FIELD_TIME };

    public static final String CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + " (" + FIELD_ID + " integer primary key autoincrement, " + FIELD_ACCOUNT + " text not null, " + FIELD_PASSWORD + " text not null, " + FIELD_TIME
            + " date not null ) ";

    // 创建

    public static Account parseCursor(Cursor cursor) {
        Account account = new Account();
        if (null == cursor || 0 == cursor.getCount()) {
            account.account = AllOnlineApp.sAccount;
            return account;
        }
        account.account = cursor.getString(cursor.getColumnIndex(FIELD_ACCOUNT));
        account.password = cursor.getString(cursor.getColumnIndex(FIELD_PASSWORD));
        account.time = cursor.getString(cursor.getColumnIndex(FIELD_TIME));
        return account;
    }
}
