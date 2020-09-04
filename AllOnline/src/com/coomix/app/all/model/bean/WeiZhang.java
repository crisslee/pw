package com.coomix.app.all.model.bean;

import android.database.Cursor;
import android.provider.BaseColumns;

public class WeiZhang {
    public static final String TABLE_NAME = "WeiZhangTable";
    public static final String FIELD_ID = BaseColumns._ID;
    public static final String FIELD_sname = "sname";
    public static final String FIELD_chepai = "chepai";
    public static final String FIELD_chejia = "chejia";
    public static final String FIELD_engine = "engine";
    public static final String[] TABLE_COLUMNS = new String[] { FIELD_ID, FIELD_sname, FIELD_chepai, FIELD_chejia,
            FIELD_engine };
    // CREATE TABLE IF NOT EXISTS
    public static final String CREATE_TABLE = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " (" + FIELD_ID
            + " integer primary key autoincrement, " + FIELD_sname + " text, " + FIELD_chepai + " text, " + FIELD_chejia
            + " text, " + FIELD_engine + " text ) ";
    public String chepai_number;// 车牌号
    public String chejia_number;// 车架号
    public String engine_number;// 发动机号
    public String short_name;// 发动机号

    // 创建

    public WeiZhang() {
        super();
    }

    public WeiZhang(String sName, String cheiP, String cheJ, String enN) {
        this.short_name = sName;
        this.chepai_number = cheiP;
        this.chejia_number = cheJ;
        this.engine_number = enN;
    }

    public static WeiZhang parseCursor(Cursor cursor) {
        if (null == cursor || 0 == cursor.getCount()) {
            return null;
        }
        WeiZhang weiZhang = new WeiZhang();
        weiZhang.short_name = cursor.getString(cursor.getColumnIndex(FIELD_sname));

        weiZhang.chepai_number = cursor.getString(cursor.getColumnIndex(FIELD_chepai));
        weiZhang.chejia_number = cursor.getString(cursor.getColumnIndex(FIELD_chejia));
        weiZhang.engine_number = cursor.getString(cursor.getColumnIndex(FIELD_engine));
        return weiZhang;
    }

}
