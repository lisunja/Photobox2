package com.example.photobox.utils;

import static android.content.ContentValues.TAG;

import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.photobox.R;
import com.hierynomus.msdtyp.AccessMask;
import com.hierynomus.mssmb2.SMB2CreateDisposition;
import com.hierynomus.mssmb2.SMB2ShareAccess;
import com.hierynomus.mssmb2.SMBApiException;
import com.hierynomus.smbj.SMBClient;
import com.hierynomus.smbj.auth.AuthenticationContext;
import com.hierynomus.smbj.connection.Connection;
import com.hierynomus.smbj.session.Session;
import com.hierynomus.smbj.share.DiskShare;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.EnumSet;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class SMBUtils {
//    private static final String SMB_SERVER_IP = "172.16.0.54"; //  10.0.2.2
//    private static final String SHARE_NAME = "Daten";
//    private static final String USERNAME = "fotobox.blc"; // DCBLN-TENTAMUS\
//    private static final String PASSWORD = "Chemie28";
    private String ip;
    private String shareName;
    private String username;
    private String password;

    public SMBUtils(Context context){
        SecureStorage secureStorage = new SecureStorage(context);
        try {
//            settingsDatabaseManager.open();
            username = secureStorage.getUsername();
            ip = secureStorage.getIp();
            shareName = secureStorage.getShare();
            password = secureStorage.getPassword();
//            settingsDatabaseManager.close();
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }
    private void showToast(Context context, final String message) {
        new Handler(Looper.getMainLooper()).post(() -> {
            LayoutInflater inflater = LayoutInflater.from(context);
            View layout = inflater.inflate(R.layout.toast_layout, null);

            TextView text = layout.findViewById(R.id.toast_text);
            text.setText(message);

            Toast toast = new Toast(context);
            toast.setDuration(Toast.LENGTH_LONG);
            toast.setView(layout);
            int durationInMilliseconds = 10000;
            int toastDuration = 3500;
            int repeatCount = durationInMilliseconds / toastDuration;
            for (int i = 0; i < repeatCount; i++) {
                toast.show();
            }
        });
    }
    public boolean checkConnection(Context context) {

        ExecutorService executor = Executors.newSingleThreadExecutor();
        Callable<Boolean> task = () -> {
            Connection connection = null;
            Session session = null;
            DiskShare share = null;
            try {
//                if(ip == null){
//                    ip = "172.16.0.54";
//                }
//                if(username == null){
//                    username = "fotobox.blc";
//                }
//                if(shareName == null){
//                    shareName = "Daten";
//                }
//                if(password == null){
//                    password = "M6wzyHE!";
//                }
                SMBClient client = new SMBClient();
                AuthenticationContext auth = new AuthenticationContext(username, password.toCharArray(), "");

                /*Connection*/ connection = client.connect(ip);
                /*Session*/ session = connection.authenticate(auth);
                /*DiskShare*/ share = (DiskShare) session.connectShare(shareName);
                return true;
            } catch (Exception e) {
                Log.e(TAG, "Error during SMB connection or file upload", e);
                LogUtil.writeLogToExternalStorage(context,"Error during SMB connection or file upload" + e);

                showToast(context, e.getMessage());
                return false;
            }finally {
                if (share != null) {
                    try { share.close(); } catch (IOException e) { /* Handle exception */ }
                }
                if (session != null) session.close();
                if (connection != null) connection.close();
            }
        };

        Future<Boolean> future = executor.submit(task);
        executor.shutdown();
        try {
            return future.get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            return false;
        }
    }
    public void uploadAllFilesInDirectory(Context context,String baseDirectoryPath, String remoteBaseFolderPath) throws Exception{

        Path baseDir = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            baseDir = Paths.get(baseDirectoryPath);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (Files.isDirectory(baseDir)) {
                try (DirectoryStream<Path> stream = Files.newDirectoryStream(baseDir)) {
                    for (Path entry : stream) {
                        if (Files.isDirectory(entry)) {
                            String localFolderPath = entry.toString();
                            String remoteFolderPath = remoteBaseFolderPath  + entry.getFileName().toString();
                            uploadFileToSmbServer(context,localFolderPath, remoteFolderPath, entry);
                        }
                    }
                } catch (IOException e) {
                    Log.e(TAG, "Error reading directory", e);

                }
            }

        }
    }

    public void uploadFileToSmbServer(Context context, String localFolderPath, String remoteFolderPath, Path entry) {
        Thread thread = new Thread(() -> {
            try {
                SMBClient client = new SMBClient();
                AuthenticationContext auth = new AuthenticationContext(username, password.toCharArray(), "");
                Connection connection = client.connect(ip);
                Session session = connection.authenticate(auth);
                DiskShare share = (DiskShare) session.connectShare(shareName);

                Path localPath = null;
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    localPath = Paths.get(localFolderPath);
                }

                if (!share.folderExists(remoteFolderPath)) {
                    Log.d(TAG, "Directory does not exist: " + remoteFolderPath);
                   showToast(context, "Wrong data. Set up in settings");

                }

                // Продолжение загрузки файлов
                uploadRecursion(share, localPath, remoteFolderPath);

                // Удаление локальной директории после загрузки
                deleteDirectory(entry);

                share.close();
                session.close();
                connection.close();

            } catch (SMBApiException e) {
               showToast(context, "Wrong data");
            } catch (IOException e) {
                Log.e(TAG, "I/O Error during SMB connection or file upload", e);
            } catch (Exception e) {
                Log.e(TAG, "Unexpected error during SMB connection or file upload", e);
            }
        });
        thread.start();
    }


    private void uploadRecursion(DiskShare share, Path localPath, String remotePath) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                if (Files.isDirectory(localPath)) {
                    Log.d(TAG, localPath + " is a directory.");
                    if (!share.folderExists(remotePath)) {
                        Log.d(TAG, "Creating directory: " + remotePath);
                        share.mkdir(remotePath);
                    }

                    try (DirectoryStream<Path> stream = Files.newDirectoryStream(localPath)) {
                        for (Path entry : stream) {
                            String childRemotePath = remotePath + "\\" + entry.getFileName().toString();
                            Log.d(TAG, "Recursing into directory: " + childRemotePath);
                            uploadRecursion(share, entry, childRemotePath);
                        }
                    }
                } else {
                    String remoteFilePath = remotePath.replaceAll("[<>:\"/\\|?*]", "_");
                    Log.d(TAG, localPath + " is a file.");
                    try (InputStream is = Files.newInputStream(localPath);
                         OutputStream os = share.openFile(
                                 remoteFilePath,
                                 EnumSet.of(AccessMask.GENERIC_WRITE),
                                 null,
                                 SMB2ShareAccess.ALL,
                                 SMB2CreateDisposition.FILE_CREATE,
                                 null
                         ).getOutputStream()) {

                        Log.d(TAG, "Opened streams for file: " + localPath + " to " + remotePath);
                        byte[] buffer = new byte[4096];
                        int bytesRead;
                        while ((bytesRead = is.read(buffer)) > 0) {
                            os.write(buffer, 0, bytesRead);
                        }
                        Log.d(TAG, "Finished writing file: " + remotePath);
                    } catch (IOException e) {
                        Log.e(TAG, "Error during file upload", e);
                    }
                }
            }
        } catch (IOException e) {
            Log.e(TAG, "Error during directory traversal", e);
        }
        Log.d(TAG, "Finished uploadRecursion for path: " + localPath);
    }

    private void deleteDirectory(Path path) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                Files.walk(path)
                        .sorted((a, b) -> b.compareTo(a))
                        .forEach(p -> {
                            try {
                                Files.delete(p);
                            } catch (IOException e) {
                                Log.e(TAG, "Error deleting file: " + p, e);
                            }
                        });
            }
            Log.d(TAG, "Deleted directory: " + path);
        } catch (IOException e) {
            Log.e(TAG, "Error walking through directory: " + path, e);
        }
    }

}
