//package com.example.photobox2.database;
//
//import android.content.Context;
//import android.database.sqlite.SQLiteDatabase;
//import android.database.sqlite.SQLiteOpenHelper;
//
//import androidx.annotation.Nullable;
//
//public class SettingsDatabaseHelper extends SQLiteOpenHelper {
//    public static final String DATABASE_NAME = "settings.db";
//    public static final int DATABASE_VERSION = 1;
//    public static final String TABLE_NAME = "settings";
//    public static final String USERNAME = "username";
//    public static final String IP = "ip";
//    public static final String SHARE = "share";
//    public static final String PASSWORD = "password";
//    public static final String REMOTE_PATH = "remote_path";
//    private static final String CREATE_DB_QUERY =
//            "CREATE TABLE " + TABLE_NAME + " (" +
//                    USERNAME + " TEXT PRIMARY KEY, " +
//                    IP + " TEXT, " +
//                    SHARE + " TEXT, " +
//                    PASSWORD + " TEXT, " +
//                    REMOTE_PATH + " TEXT);";
//    private static final String INSERT_DEFAULT_VALUES_QUERY =
//            "INSERT INTO " + TABLE_NAME + " (" +
//                    USERNAME + ", " +
//                    IP + ", " +
//                    SHARE + ", " +
//                    PASSWORD + ", " +
//                    REMOTE_PATH + ") VALUES ('Yelyzaveta.Bespalova', '172.16.0.54', 'Daten', '89d866178530384810I', 'IT\\30_LIMS_2\\50_Sample_Registration\\Bilacon\\Fotobox\\');";
//
//
//    public SettingsDatabaseHelper(@Nullable Context context) {
//        super(context, DATABASE_NAME, null, DATABASE_VERSION);
//    }
//
//    @Override
//    public void onCreate(SQLiteDatabase db) {
//        db.execSQL(CREATE_DB_QUERY);
//        db.execSQL(INSERT_DEFAULT_VALUES_QUERY);
//    }
//
//    @Override
//    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
//        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
//        onCreate(db);
//    }
//}
