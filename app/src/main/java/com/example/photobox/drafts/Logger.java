//package com.example.photobox.log;
//
//import android.content.Context;
//import android.util.Log;
//
//import java.io.FileOutputStream;
//import java.io.IOException;
//import java.io.OutputStreamWriter;
//import java.text.SimpleDateFormat;
//import java.util.Date;
//import java.util.Locale;
//
//public class Logger {
//    private static final String LOG_TAG = "Applogger";
//
//    private static final String FILE_NAME = "app_logs.txt";
//    private static Logger instance;
//    private final Context context;
//
//    public Logger(Context context) {
//        this.context = context;
//    }
//
//    public static synchronized Logger getInstance(Context context){
//        if(instance == null){
//            instance = new Logger(context);
//        }
//        return instance;
//    }
//    public void write(String message){
//        String timeStamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
//        String logMessage = timeStamp + " - " + message + "\n";
//        try {
//            FileOutputStream fos = context.openFileOutput(FILE_NAME, Context.MODE_APPEND);
//            OutputStreamWriter writer = new OutputStreamWriter(fos);
//            writer.write(logMessage);
//            writer.close();
//        } catch (IOException e) {
//            Log.e(LOG_TAG, "Error writing in log file", e);
//        }
//    }
//}
