package com.example.photobox2.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.sql.SQLDataException;

public class SettingsDatabaseManager {
    private SettingsDatabaseHelper dbHelper;
    private Context context;
    public SettingsDatabaseManager(Context context) {
        this.context = context;
    }
    public SettingsDatabaseManager open () throws SQLDataException {
    dbHelper = new SettingsDatabaseHelper(context);
    return this;
    }
    public void close(){
        dbHelper.close();
    }
//    public void insert (String username, String ip, String share, String password, String remotePath){
//        ContentValues contentValues = new ContentValues();
//        contentValues.put(SettingsDatabaseHelper.USERNAME, username);
//        contentValues.put(SettingsDatabaseHelper.IP, ip);
//        contentValues.put(SettingsDatabaseHelper.SHARE, share);
//        contentValues.put(SettingsDatabaseHelper.PASSWORD, password);
//        contentValues.put(SettingsDatabaseHelper.REMOTE_PATH, remotePath);
//        database.insert(SettingsDatabaseHelper.TABLE_NAME, null,contentValues);
//    }
//    public Cursor fetch(){
//        String [] columns = new String [] {
//                SettingsDatabaseHelper.USERNAME,
//                SettingsDatabaseHelper.IP,
//                SettingsDatabaseHelper.SHARE,
//                SettingsDatabaseHelper.PASSWORD,
//                SettingsDatabaseHelper.REMOTE_PATH,};
//        Cursor cursor = database.query(SettingsDatabaseHelper.TABLE_NAME, columns, null, null, null, null, null);
//        if (cursor!= null){
//            cursor.moveToFirst();
//        }
//        return cursor;
//    }
//    public int update(String username, String ip, String share, String password, String remotePath) {
//        ContentValues contentValues = new ContentValues();
//        contentValues.put(SettingsDatabaseHelper.IP, ip);
//        contentValues.put(SettingsDatabaseHelper.SHARE, share);
//        contentValues.put(SettingsDatabaseHelper.PASSWORD, password);
//        contentValues.put(SettingsDatabaseHelper.REMOTE_PATH, remotePath);
//        int ret = database.update(SettingsDatabaseHelper.TABLE_NAME, contentValues, SettingsDatabaseHelper.USERNAME + "=" + username, null);
//        return ret;
//    }

    public void saveSetting(String key, String value) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(key, value);
        Cursor cursor = db.query(SettingsDatabaseHelper.TABLE_NAME, null, null, null, null, null, null);
        if (cursor.getCount() == 0) {
            db.insert(SettingsDatabaseHelper.TABLE_NAME, null, values);
        } else {
            db.update(SettingsDatabaseHelper.TABLE_NAME, values, null, null);
        }
        cursor.close();
    }
    public String getSetting(String key) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String[] columns = { key };
        Cursor cursor = db.query(SettingsDatabaseHelper.TABLE_NAME, columns, null, null, null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            String value = cursor.getString(cursor.getColumnIndexOrThrow(key));
            cursor.close();
            return value;
        }

        if (cursor != null) {
            cursor.close();
        }
        return null;
    }
}
