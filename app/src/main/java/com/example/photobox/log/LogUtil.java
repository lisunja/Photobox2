package com.example.photobox.log;

import android.content.Context;
import android.os.Build;
import android.os.Environment;
import android.util.Log;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class LogUtil {
    private static final String TAG = "LogUtil";
    private static final String LOG_FILE_NAME = "app_logs.txt";

    public static void writeLogToExternalStorage(String message) {
        File publicDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS);
        if(!publicDir.exists()){
            publicDir.mkdirs();
        }

        File logFile = new File(publicDir, LOG_FILE_NAME);

        try (FileWriter fileWriter = new FileWriter(logFile, true);
             BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
             PrintWriter printWriter = new PrintWriter(bufferedWriter)) {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                printWriter.println(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) + " " + message);
            }

        } catch (IOException e) {
            Log.e(TAG, "Failed to write log to external file", e);
        }
    }
}
