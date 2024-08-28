package com.example.photobox.utils;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class LogUtil {
    private static final String TAG = "LogUtil";
    private static final String LOG_FILE_NAME = "app_logs.txt";

    public static void writeLogToExternalStorage(Context context, String message) {
        File publicDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS);
        if(!publicDir.exists()){
            publicDir.mkdirs();
        }

        File logFile = new File(publicDir, LOG_FILE_NAME);

        try (FileWriter fileWriter = new FileWriter(logFile, true);
             BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
             PrintWriter printWriter = new PrintWriter(bufferedWriter)) {

             printWriter.println(message);

        } catch (IOException e) {
            Log.e(TAG, "Failed to write log to external file", e);
        }
    }
}
