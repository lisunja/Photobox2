package com.example.photobox2.service;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.example.photobox2.R;
import com.example.photobox2.utils.SMBUtils;

public class FileUploadService extends Service {
    private static final String REMOTE_FOLDER_PATH = "IT\\30_LIMS_2\\50_Sample_Registration\\Bilacon\\Fotobox\\";

    private static final String CHANNEL_ID = "FileUploadServiceChannel";
    private Handler handler;
    //for action in new thread
    private Runnable periodicTask;
    //30 sec
    private static final long CHECK_INTERVAL = 30 * 1000;
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
        String localFilePath = getFilesDir().getPath();
        SMBUtils smbUtils = new SMBUtils();
        if (smbUtils.checkConnection()) {
            smbUtils.uploadAllFilesInDirectory (localFilePath, REMOTE_FOLDER_PATH);
        }
    }
}
