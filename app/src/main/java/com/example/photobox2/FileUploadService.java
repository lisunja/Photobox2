package com.example.photobox2;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import java.io.File;

public class FileUploadService extends Service {
    private static final String REMOTE_FOLDER_PATH = "IT\\30_LIMS_2\\50_Sample_Registration\\"; //  10.0.2.2

    private static final String CHANNEL_ID = "FileUploadServiceChannel";
    private Handler handler;
    private Runnable periodicTask;
    private static final int NOTIFICATION_ID = 1;

//    public void setSample(String sample) {
//        this.sample = sample;
//    }

//    private String sample = "12345";
    private static final long CHECK_INTERVAL = 30 * 1000;
    //private static final long DELETE_DELAY = 24 * 60 * 60 * 1000;
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    @SuppressLint("ForegroundServiceType")
    @Override
    public void onCreate() {
        super.onCreate();

        createNotificationChannel();
        Notification notification = createNotification();
        startForeground(1, notification);

        handler = new Handler(Looper.getMainLooper());
        periodicTask = new Runnable() {
            @Override
            public void run() {
                uploadFile();
                handler.postDelayed(this, CHECK_INTERVAL);
            }
        };
        handler.post(periodicTask);

    }
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
    private Notification createNotification() {
        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("File Upload Service")
                .setContentText("Service is running...")
                .setSmallIcon(R.drawable.ic_notification)
                .build();
    }
    private Notification resultNotification() {
        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("File Upload Service")
                .setContentText("All photos were uploaded")
                .setSmallIcon(R.drawable.ic_notification)
                .build();
    }
    public void showNotification(){
        Notification notification = resultNotification();
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(NOTIFICATION_ID, notification);
    }
//    private void checkAndUploadFiles() {
////        String directoryPath = getFilesDir().getPath() + "/" + sample;
////        File directory = new File(directoryPath);
////
////
////        if (directory.exists() && directory.isDirectory()) {
////            File[] files = directory.listFiles();
////            if (files != null) {
////                for (File file : files) {
////                    if (file.isFile()) {
//
//                        uploadFile(/*file*/);
//
////                        scheduleFileDeletion(/*file*/);
////                    }
////                }
////            }
////        }
//    }

    private void uploadFile(/*File file*/) {
        String localFilePath = getFilesDir().getPath(); /*file.getAbsolutePath();*/
        SMBUtils smbUtils = new SMBUtils();
        if (smbUtils.checkConnection()) {

            smbUtils.uploadAllFilesInDirectory (localFilePath, REMOTE_FOLDER_PATH);
        }
    }

//    private void scheduleFileDeletion(File file) {
//        handler.postDelayed(() -> {
//            if (file.exists()) {
//                file.delete();
//            }
//        }, DELETE_DELAY);
//    }
}
