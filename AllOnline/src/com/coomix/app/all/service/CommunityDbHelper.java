package com.coomix.app.all.service;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import android.util.Log;
import com.coomix.app.all.BuildConfig;
import com.coomix.app.all.util.StringUtil;
import com.coomix.app.all.model.response.CommunityUser;
import com.coomix.app.all.model.response.Picture;
import com.google.gson.Gson;
import java.util.ArrayList;
import org.json.JSONArray;

/**
 * Created by herry on 2016/12/20.
 */

public class CommunityDbHelper extends SQLiteOpenHelper {
    private static final String TAG = "CommunityDbHelper";
    private static final String DB_NAME = "db_community_account_info";
    private static final int DB_VERSION = 4;
    private static final String TABLE_ACCOUNT = "t_account_info";

    private static CommunityDbHelper mInstance = null;
    private SQLiteDatabase mDatabase;

    //double check
    public static CommunityDbHelper getInstance(Context context) {
        if (mInstance == null) {
            synchronized (CommunityDbHelper.class) {
                if (mInstance == null) {
                    mInstance = new CommunityDbHelper(context);
                }
            }
        }
        return mInstance;
    }

    private CommunityDbHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
        mDatabase = getWritableDatabase();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        StringBuilder sql = new StringBuilder().append("CREATE TABLE ")
            .append(TABLE_ACCOUNT)
            .append("(")
            .append(AccountInfoColumns._ID)
            .append(" INTEGER PRIMARY KEY AUTOINCREMENT,")
            .append(AccountInfoColumns.ACCOUNT)
            .append(" TEXT NOT NULL,")
            .append(AccountInfoColumns.ICON_URL)
            .append(" TEXT,")
            .append(AccountInfoColumns.NICK_NAME)
            .append(" TEXT,")
            .append(AccountInfoColumns.GENDER)
            .append(" INTEGER,")
            .append(AccountInfoColumns.SIGNATURE)
            .append(" TEXT,")
            .append(AccountInfoColumns.UID)
            .append(" TEXT,")
            .append(AccountInfoColumns.TICKET)
            .append(" TEXT,")
            .append(AccountInfoColumns.SCORE)
            .append(" INTEGER,")
            .append(AccountInfoColumns.GRADE)
            .append(" INTEGER,")
            .append(AccountInfoColumns.TEL)
            .append(" TEXT,")
            .append(AccountInfoColumns.CITY_CODE)
            .append(" TEXT,")
            .append(AccountInfoColumns.SID)
            .append(" TEXT,")
            .append(AccountInfoColumns.WXID)
            .append(" TEXT,")
            .append(AccountInfoColumns.IS_OPERATION_SPECIALIST)
            .append(" INTEGER,")
            .append(AccountInfoColumns.BACKGROUD)
            .append(" TEXT,")
            .append(AccountInfoColumns.STATICS)
            .append(" TEXT")
            .append(")");
        db.execSQL(sql.toString());
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < newVersion) {
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_ACCOUNT);
            onCreate(db);
        }
    }

    private static class AccountInfoColumns implements BaseColumns {
        private static final String ACCOUNT = "account";
        private static final String ICON_URL = "icon_url";
        private static final String NICK_NAME = "nick_name";
        private static final String GENDER = "gender";
        private static final String SIGNATURE = "signature";
        private static final String UID = "uid";
        private static final String TICKET = "ticket";
        private static final String SCORE = "score";
        private static final String GRADE = "grade";
        // ==GENDER private static final String SEX = "sex";
        private static final String TEL = "tel";
        private static final String CITY_CODE = "city_code";
        private static final String SID = "sid";
        private static final String BACKGROUD = "backgroud";
        private static final String STATICS = "statics";
        private static final String WXID = "WXID";
        private static final String IS_OPERATION_SPECIALIST = "IS_OPERATION_SPECIALIST";

    }

    /*判断该用户是否拥有社区账号信息*/
    public boolean isAccountRegisted(String account) {
        StringBuilder selection = new StringBuilder().append(AccountInfoColumns.ACCOUNT).append("=?");
        Cursor c = mDatabase.query(TABLE_ACCOUNT, new String[]{AccountInfoColumns._ID}, selection.toString(), new String[]{account}, null, null, null);
        if (c == null) {
            return false;
        }
        boolean exist = c.moveToFirst();
        c.close();
        return exist;
    }

    /*获取社区账户信息*/
    public CommunityUser getAccountInfo(String account) {
        if (StringUtil.isTrimEmpty(account)) {
            return null;
        }
        StringBuilder selection = new StringBuilder().append(AccountInfoColumns.ACCOUNT).append("=?");
        Cursor c = mDatabase.query(TABLE_ACCOUNT, null, selection.toString(), new String[]{account}, null, null, null);
        if (c == null) {
            return null;
        }
        boolean exist = c.moveToFirst();
        if (!exist) {
            c.close();
            return null;
        }
        CommunityUser ret = new CommunityUser();
        ret.setAccount(account);
        ret.setImg(c.getString(c.getColumnIndex(AccountInfoColumns.ICON_URL)));
        ret.setName(c.getString(c.getColumnIndex(AccountInfoColumns.NICK_NAME)));
        ret.setSex(c.getInt(c.getColumnIndex(AccountInfoColumns.GENDER)));
        ret.setLabel(c.getString(c.getColumnIndex(AccountInfoColumns.SIGNATURE)));
        ret.setUid(c.getString(c.getColumnIndex(AccountInfoColumns.UID)));
        ret.setTicket(c.getString(c.getColumnIndex(AccountInfoColumns.TICKET)));
        ret.setScore(c.getInt(c.getColumnIndex(AccountInfoColumns.SCORE)));
        ret.setGrade(c.getInt(c.getColumnIndex(AccountInfoColumns.GRADE)));
        ret.setTel(c.getString(c.getColumnIndex(AccountInfoColumns.TEL)));
        ret.setCitycode(c.getString(c.getColumnIndex(AccountInfoColumns.CITY_CODE)));
        ret.setSid(c.getString(c.getColumnIndex(AccountInfoColumns.SID)));
        ret.setWxid(c.getString(c.getColumnIndex(AccountInfoColumns.WXID)));
        ret.setOperationSpecialist(c.getInt(c.getColumnIndex(AccountInfoColumns.IS_OPERATION_SPECIALIST)));
        Gson gson = new Gson();
        String background = c.getString(c.getColumnIndex(AccountInfoColumns.BACKGROUD));
        if(background != null) {
            try {
                JSONArray ja = new JSONArray(background);
                ArrayList<Picture> list = new ArrayList<Picture>();
                for(int i = 0; i < ja.length(); i++) {
                    list.add(gson.fromJson(ja.getString(i), Picture.class));
                }
                ret.setBackground(list);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        c.close();
        return ret;
    }

    /*保存服务器下发的社区账号信息*/
    public void saveAccountInfo(CommunityUser accountInfo) {
        if (accountInfo == null) {
            return;
        }
        deleteAccountInfo(accountInfo.getAccount());
        ContentValues values = new ContentValues();
        values.put(AccountInfoColumns.ACCOUNT, accountInfo.getAccount());
        values.put(AccountInfoColumns.NICK_NAME, accountInfo.getName());
        values.put(AccountInfoColumns.GENDER, accountInfo.getSex());
        values.put(AccountInfoColumns.SIGNATURE, accountInfo.getLabel());
        values.put(AccountInfoColumns.ICON_URL, accountInfo.getImg());
        values.put(AccountInfoColumns.UID, accountInfo.getUid());
        values.put(AccountInfoColumns.TICKET, accountInfo.getTicket());
        values.put(AccountInfoColumns.SCORE, accountInfo.getScore());
        values.put(AccountInfoColumns.GRADE, accountInfo.getGrade());
        values.put(AccountInfoColumns.TEL, accountInfo.getTel());
        values.put(AccountInfoColumns.CITY_CODE, accountInfo.getCitycode());
        values.put(AccountInfoColumns.SID, accountInfo.getSid());
        values.put(AccountInfoColumns.WXID, accountInfo.getWxid());
        values.put(AccountInfoColumns.IS_OPERATION_SPECIALIST, accountInfo.isOperationSpecialist()? 1 : 0);

        Gson gson = new Gson();
        values.put(AccountInfoColumns.BACKGROUD, gson.toJson(accountInfo.getBackground()).toString());
        values.put(AccountInfoColumns.STATICS, "");
        long id = mDatabase.insert(TABLE_ACCOUNT, AccountInfoColumns.ACCOUNT, values);
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "save community account, id : " + id);
        }
    }

    /*删除社区账号信息*/
    public void deleteAccountInfo(String account) {
        StringBuilder where = new StringBuilder().append(AccountInfoColumns.ACCOUNT).append("=?");
        int count = mDatabase.delete(TABLE_ACCOUNT, where.toString(), new String[]{account});
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "delete community account info, count : " + count);
        }
    }

    /*修改昵称*/
    public void updateNickName(String account, String newNickName) {
        ContentValues values = new ContentValues();
        values.put(AccountInfoColumns.NICK_NAME, newNickName);
        updateItemInfo(account, values);
    }

    public void updateGender(String account, int newGender) {
        ContentValues values = new ContentValues();
        values.put(AccountInfoColumns.GENDER, newGender);
        updateItemInfo(account, values);
    }

    public void updateSignature(String account, String newSignature) {
        ContentValues values = new ContentValues();
        values.put(AccountInfoColumns.SIGNATURE, newSignature);
        updateItemInfo(account, values);
    }

    public void updateIconUrl(String account, String newIconUrl) {
        ContentValues values = new ContentValues();
        values.put(AccountInfoColumns.ICON_URL, newIconUrl);
        updateItemInfo(account, values);
    }

    public void updateTicket(String account, String newTicket) {
        ContentValues values = new ContentValues();
        values.put(AccountInfoColumns.TICKET, newTicket);
        updateItemInfo(account, values);
    }

    public void updateBackgroud(String account, String newBackgroud) {
        ContentValues values = new ContentValues();
        values.put(AccountInfoColumns.BACKGROUD, newBackgroud);
        updateItemInfo(account, values);
    }

    public void updateImg(String account, String newImg) {
        ContentValues values = new ContentValues();
        values.put(AccountInfoColumns.ICON_URL, newImg);
        updateItemInfo(account, values);
    }

    private void updateItemInfo(String account, ContentValues values) {
        StringBuilder where = new StringBuilder().append(AccountInfoColumns.ACCOUNT).append("=?");
        int count = mDatabase.update(TABLE_ACCOUNT, values, where.toString(), new String[]{account});
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "update item info, count : " + count);
        }
    }

    public void updateTel(String account, String newTel) {
        ContentValues values = new ContentValues();
        values.put(AccountInfoColumns.TEL, newTel);
        updateItemInfo(account, values);
    }
}
