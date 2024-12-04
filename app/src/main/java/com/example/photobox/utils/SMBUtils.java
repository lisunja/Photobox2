package com.example.photobox.utils;

import static android.content.ContentValues.TAG;

import android.content.Context;
import android.nfc.Tag;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.photobox.R;
import com.example.photobox.log.LogUtil;
import com.hierynomus.msdtyp.AccessMask;
import com.hierynomus.mssmb2.SMB2CreateDisposition;
import com.hierynomus.mssmb2.SMB2ShareAccess;
import com.hierynomus.mssmb2.SMBApiException;
import com.hierynomus.smbj.SMBClient;
import com.hierynomus.smbj.auth.AuthenticationContext;
import com.hierynomus.smbj.connection.Connection;
import com.hierynomus.smbj.session.Session;
import com.hierynomus.smbj.share.DiskShare;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.EnumSet;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

public class SMBUtils {
//    private static final String SMB_SERVER_IP = "172.16.0.54"; //  10.0.2.2
//    private static final String SHARE_NAME = "Daten";
//    private static final String USERNAME = "fotobox.blc"; // DCBLN-TENTAMUS\
//    private static final String PASSWORD = "Chemie28";
    private String ip;
    private String shareName;
    private String username;
    private String password;
    public static final String ERLEDIGT_DIR = "/data/data/com.example.photobox/files/erledigt";

    private static final SimpleDateFormat FILE_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");

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
                LogUtil.writeLogToExternalStorage("Error during SMB connection or file upload" + e);
                Log.e(TAG, "Error during SMB connection or file upload", e);

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
                            Log.d(TAG,"Localpath: "+ localFolderPath);
                            if (localFolderPath.contains("erledigt")) {
                                deleteOldFiles(localFolderPath);
                                return;
                            }
                            String remoteFolderPath = remoteBaseFolderPath  + entry.getFileName().toString();
                            uploadFileToSmbServer(context,localFolderPath, remoteFolderPath, entry);
                        }
                    }
                } catch (IOException e) {
                    LogUtil.writeLogToExternalStorage("Error reading directory" + e);
                    Log.e(TAG, "Error reading directory", e);

                }
            }

        }
    }
    public static void deleteOldFiles(String directoryPath) {
        File directory = new File(directoryPath);
        if (directory.exists() && directory.isDirectory()) {
            File[] files = directory.listFiles();

            if (files != null) {
                for (File file : files) {
                    if (file.isFile()) {
                        String fileName = file.getName();
                        try {
                            int firstUnderscore = fileName.indexOf("_");
                            int secondUnderscore = fileName.indexOf("_", firstUnderscore + 1);

                            if (firstUnderscore == -1 || secondUnderscore == -1) {
                                Log.d(TAG,"Skipping file with invalid format: " + fileName);
                                continue;
                            }

                            String dateString = fileName.substring(firstUnderscore + 1, secondUnderscore);


                            Date fileDate = FILE_DATE_FORMAT.parse(dateString);
                            long fileAgeInMillis = new Date().getTime() - fileDate.getTime();
                            long ageInDays = TimeUnit.MILLISECONDS.toDays(fileAgeInMillis);
                            Log.d(TAG,"ageInDays: " + ageInDays);

                            if (ageInDays >= 14) {
                                if (file.delete()) {
                                    Log.d(TAG,"Deleted file: " + file.getName());
                                } else {
                                    Log.d(TAG,"Failed to delete file: " + file.getName());
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            System.out.println("Error parsing date for file: " + fileName);
                        }
                    }
                }
            }
        }
    }
    public synchronized void uploadFileToSmbServer(Context context, String localFolderPath, String remoteFolderPath, Path entry) {
        Thread thread = new Thread(() -> {
            try {
                SMBClient client = new SMBClient();
                AuthenticationContext auth = new AuthenticationContext(username, password.toCharArray(), "");
                Connection connection = client.connect(ip);
                Session session = connection.authenticate(auth);
                DiskShare share = (DiskShare) session.connectShare(shareName);

                Path localPath = null;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    localPath = Paths.get(localFolderPath);
                }

                if (!share.folderExists(remoteFolderPath)) {
                    Log.d(TAG, "Directory does not exist: " + remoteFolderPath);
                   showToast(context, "Wrong data. Set up in settings");
                }


                uploadRecursion(share, localPath, remoteFolderPath, context);
                Validation.showFiles(localPath, 2);
                deleteDirectory(entry);
                Validation.showFiles(localPath, 3);
                share.close();
                session.close();
                connection.close();
                LogUtil.writeLogToExternalStorage("Photo was moved successfully" );

            } catch (SMBApiException e) {
                LogUtil.writeLogToExternalStorage("Wrong data" + e);
                //                showToast(context, "Wrong data");
            } catch (IOException e) {
                LogUtil.writeLogToExternalStorage("I/O Error during SMB connection or file upload" + e);
                Log.e(TAG, "I/O Error during SMB connection or file upload", e);
            } catch (Exception e) {
                LogUtil.writeLogToExternalStorage("Unexpected error during SMB connection or file upload" + e);
                Log.e(TAG, "Unexpected error during SMB connection or file upload", e);
            }
        });
        thread.start();
    }


    private synchronized void uploadRecursion(DiskShare share, Path localPath, String remotePath, Context context)  {
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
                            LogUtil.writeLogToExternalStorage("Recursing into directory: " + childRemotePath);
                            uploadRecursion(share, entry, childRemotePath, context);

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
                        LogUtil.writeLogToExternalStorage("Finished writing file: " + remotePath);
                    } catch (IOException e) {
                        Log.e(TAG, "Error during file upload", e);
                        LogUtil.writeLogToExternalStorage("Error during file upload" + e + remotePath);
                    }
                }
            }
        } catch (IOException e) {
            Log.e(TAG, "Error during directory traversal", e);
        }
        Log.d(TAG, "Finished uploadRecursion for path: " + localPath);
        LogUtil.writeLogToExternalStorage("Finished uploadRecursion for path: " + localPath);
    }

    private void deleteDirectory(Path path) {
        try {
            Path erledigtDir = Paths.get(ERLEDIGT_DIR);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                if (!Files.exists(erledigtDir)) {
                    Files.createDirectories(erledigtDir);
                }
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                Files.walk(path)
                        .sorted((a, b) -> b.compareTo(a))
                        .forEach(p -> {
                            try {
                                if (Files.isDirectory(p) && p.equals(erledigtDir)) {
                                    Log.d(TAG, "Skipped directory: " + p);
                                    return;
                                }
                                if (Files.isDirectory(p)) {
                                    Files.delete(p);
                                    Log.d(TAG, "Deleted directory: " + p);
                                } else {
                                    String fileName = p.getFileName().toString();
                                    String sampleNumber = fileName.split("_")[0];
                                    Path sampleDir = erledigtDir.resolve(sampleNumber);
                                    if (!Files.exists(sampleDir)) {
                                        Files.createDirectories(sampleDir);
                                    }
                                    Path targetPath = sampleDir.resolve(p.getFileName());
                                    Files.move(p, targetPath, StandardCopyOption.REPLACE_EXISTING);

                                    Log.d(TAG, "Moved file to folder: " + sampleDir + " -> " + targetPath);
                                }
                            } catch (IOException e) {
                                Log.e(TAG, "Error moving file: " + p, e);
                            }
                        });
            }

            Log.d(TAG, "Moved all files from directory: " + path);
        } catch (IOException e) {
            Log.e(TAG, "Error handling directory: " + path, e);
        }
    }

}
