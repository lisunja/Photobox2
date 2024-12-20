//package com.example.photobox2.database;
//
//import android.content.ContentValues;
//import android.content.Context;
//import android.content.SharedPreferences;
//import android.database.Cursor;
//import android.database.sqlite.SQLiteDatabase;
//
//import androidx.security.crypto.EncryptedSharedPreferences;
//import androidx.security.crypto.MasterKey;
//
//import java.sql.SQLDataException;
//
//public class SettingsDatabaseManager {
//    private SettingsDatabaseHelper dbHelper;
//    private Context context;
//    public SettingsDatabaseManager(Context context) {
//        this.context = context;
//    }
//    public SettingsDatabaseManager open () throws SQLDataException {
//    dbHelper = new SettingsDatabaseHelper(context);
//    return this;
//    }
//    public void close(){
//        dbHelper.close();
//    }
//    public void saveSetting(String key, String value) {
//        SQLiteDatabase db = dbHelper.getWritableDatabase();
//        ContentValues values = new ContentValues();
//        values.put(key, value);
//        Cursor cursor = db.query(SettingsDatabaseHelper.TABLE_NAME, null, null, null, null, null, null);
//        if (cursor.getCount() == 0) {
//            db.insert(SettingsDatabaseHelper.TABLE_NAME, null, values);
//        } else {
//            db.update(SettingsDatabaseHelper.TABLE_NAME, values, null, null);
//        }
//        cursor.close();
//    }
//    public String getSetting(String key) {
//        SQLiteDatabase db = dbHelper.getReadableDatabase();
//        String[] columns = { key };
//        Cursor cursor = db.query(SettingsDatabaseHelper.TABLE_NAME, columns, null, null, null, null, null);
//
//        if (cursor != null && cursor.moveToFirst()) {
//            String value = cursor.getString(cursor.getColumnIndexOrThrow(key));
//            cursor.close();
//            return value;
//        }
//
//        if (cursor != null) {
//            cursor.close();
//        }
//        return null;
//    }
//
//}
