package com.example.photobox.service;

import static android.content.ContentValues.TAG;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.example.photobox.log.LogUtil;
import com.example.photobox.utils.SMBUtils;
import com.example.photobox.utils.SecureStorage;
import com.example.photobox.R;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class FileUploadService extends Service {
    //private static final String REMOTE_FOLDER_PATH = "IT\\30_LIMS_2\\50_Sample_Registration\\Bilacon\\Fotobox\\";

    private static final String CHANNEL_ID = "FileUploadServiceChannel";
    private Handler handler;
//    private Handler directoryHandler;
//    private Runnable directoryPeriodicTask;
    //for action in new thread
    private Runnable periodicTask;
    //30 sec
    private static final long CHECK_INTERVAL = 30 * 1000;
    private static final long DELETE_INTERVAL = 14 * 24 * 60 * 60 * 1000;
    private Context context;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    @SuppressLint("ForegroundServiceType")
    @Override
    public void onCreate() {
        super.onCreate();
        context = this;
        createNotificationChannel();
        Notification notification = createNotification();
        startForeground(1, notification);

        handler = new Handler(Looper.getMainLooper());
//        directoryHandler = new Handler(Looper.getMainLooper());
        periodicTask = new Runnable() {
            @Override
            public void run() {
                try {
                    uploadFile();
                }catch (Exception e){
                    Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
                }

                handler.postDelayed(this, CHECK_INTERVAL);
            }
        };

//        directoryPeriodicTask = new Runnable() {
//            @Override
//            public void run() {
//                try {
//                    deleteErledigtDirectory();
//                }catch (Exception e){
//                    Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
//                }
//
//                handler.postDelayed(this, DELETE_INTERVAL);
//            }
//        };

        handler.post(periodicTask);
//        directoryHandler.post(directoryPeriodicTask);
    }
//    @SuppressLint("ScheduleExactAlarm")
//    public void scheduleDirectoryDeletion(Context context) {
//        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
//        Intent intent = new Intent(context, DirectoryDeletionReceiver.class);
//        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
//        long triggerTime = System.currentTimeMillis() + 30 * 1000;
//
////    long triggerTime = System.currentTimeMillis() + TimeUnit.DAYS.toMillis(14);
//        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent);
//    }

//    private void deleteErledigtDirectory() {
//        Path erledigtPath = Paths.get(SMBUtils.ERLEDIGT_DIR);
//        try {
//            if (Files.exists(erledigtPath)) {
//                Files.walk(erledigtPath)
//                        .sorted(Comparator.reverseOrder())
//                        .forEach(p -> {
//                            try {
//                                Files.delete(p);
//                            } catch (IOException e) {
//                                Log.e("DirectoryDeletion", "Error deleting file: " + p, e);
//                            }
//                        });
//                Log.d("DirectoryDeletion", "Directory deleted: " + erledigtPath);
//            }
//        } catch (IOException e) {
//            Log.e("DirectoryDeletion", "Error cleaning directory", e);
//        }
//    }


    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "File Upload Service Channel",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(serviceChannel);
        }
    }
    //shows service notification
    private Notification createNotification() {
        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("File Upload Service")
                .setContentText("Service is running...")
                .setSmallIcon(R.drawable.ic_notification)
                .build();
    }
    //upload files into server
    private void uploadFile() {
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.submit(() -> {
            try {
                String localFilePath = getFilesDir().getPath();
                SecureStorage secureStorage = new SecureStorage(context);
                String remotePath = secureStorage.getRemotePath();
                SMBUtils smbUtils = new SMBUtils(context);
                if (smbUtils.checkConnection(context)) {
                        smbUtils.uploadAllFilesInDirectory(context,localFilePath, remotePath);
                } else {
                    LogUtil.writeLogToExternalStorage("No connection to the server");
                    throw new Exception("No connection to the server");
                }
            } catch (Exception e) {
                LogUtil.writeLogToExternalStorage("Upload failed for " + getFilesDir().getPath() + " " + e);
                Log.e("FileUploadService", "Upload failed", e);
                // Handle exception appropriately
            }
        });
    }

    @Override
    public void onDestroy() {
        handler.removeCallbacks(periodicTask);
//        directoryHandler.removeCallbacks(directoryPeriodicTask);
        super.onDestroy();
    }

}
