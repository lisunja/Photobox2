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
import java.util.Comparator;
import java.util.Date;
import java.util.EnumSet;
import java.util.Objects;
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

    public SMBUtils(Context context) {
        SecureStorage secureStorage = new SecureStorage(context);
        try {
//            settingsDatabaseManager.open();
            username = secureStorage.getUsername();

            ip = secureStorage.getIp();
            shareName = secureStorage.getShare();
            password = secureStorage.getPassword();
//            settingsDatabaseManager.close();
        } catch (Exception e) {
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

                SMBClient client = new SMBClient();
                AuthenticationContext auth = new AuthenticationContext(username, password.toCharArray(), "");

                /*Connection*/
                connection = client.connect(ip);
                /*Session*/
                session = connection.authenticate(auth);
                /*DiskShare*/
                share = (DiskShare) session.connectShare(shareName);
                return true;
            } catch (Exception e) {
                LogUtil.writeLogToExternalStorage("Error during SMB connection or file upload" + e);
                Log.e(TAG, "Error during SMB connection or file upload", e);

                showToast(context, e.getMessage());
                return false;
            } finally {
                if (share != null) {
                    try {
                        share.close();
                    } catch (IOException e) { /* Handle exception */ }
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

    public void uploadAllFilesInDirectory(Context context, String baseDirectoryPath, String remoteBaseFolderPath/*, String photoName*/) throws Exception {
    Log.d(TAG, "Starting uploadAllFilesInDirectory");
         Path baseDir = getPath(baseDirectoryPath);

         if (baseDir == null || !Files.isDirectory(baseDir)) {
             Log.d(TAG, "Invalid base directory path: " + baseDirectoryPath);
             return;
         }

         try (DirectoryStream<Path> stream = Files.newDirectoryStream(baseDir)) {
             for (Path entry : stream) {
                 /*Log.d(TAG, "A path for entry: " + entry.toString());
                 Log.d(TAG, "photoname: " + photoName);*/

                 if (entry.toString().contains("erledigt") ) {
                     Log.d(TAG, "Skipping 'erledigt' folder: " + entry);
                     deleteOldFiles(entry.toString());
                     continue;
                 }

                 if (Files.isDirectory(entry)) {
                     String remoteFolderPath = remoteBaseFolderPath + entry.getFileName().toString();
                     processDirectory(context, entry, remoteFolderPath);
                 }
             }
         } catch (IOException e) {
             Log.e(TAG, "Error reading directory" + e);
         }
     }
    private String extractFolderName(String photoName) {
        int underscoreIndex = photoName.indexOf('_');
        return underscoreIndex > 0 ? photoName.substring(0, underscoreIndex) : "";
    }
    private Path getPath(String directoryPath) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            return Paths.get(directoryPath);
        } else {
            Log.d(TAG, "Unsupported Android version for Path API.");
            return null;
        }
    }

    private void processDirectory(Context context, Path localDir, String remoteFolderPath) {
        try {
            SMBClient client = new SMBClient();
            try (Connection connection = client.connect(ip);
                 Session session = connection.authenticate(new AuthenticationContext(username, password.toCharArray(), ""));
                 DiskShare share = (DiskShare) session.connectShare(shareName)) {

                if (!share.folderExists(remoteFolderPath)) {
                    share.mkdir(remoteFolderPath);
                }
                Log.d(TAG, "starting upload file recursively to remotefolderpath: " + remoteFolderPath);
                uploadFilesRecursively(share, localDir, remoteFolderPath, context);

                moveDirectoryToErledigt(localDir);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error during SMB connection or file upload" + e);
        }
    }

    private void uploadFilesRecursively(DiskShare share, Path localPath, String remotePath, Context context) throws IOException {
        if (Files.isDirectory(localPath)) {
            Log.d(TAG, "Processing directory: " + localPath);

            try (DirectoryStream<Path> stream = Files.newDirectoryStream(localPath)) {
                for (Path entry : stream) {
                    String childRemotePath = remotePath + "/" + entry.getFileName().toString();
                    uploadFilesRecursively(share, entry, childRemotePath, context);
                }
            }
        } else {
            uploadFile(share, localPath, remotePath);
        }
    }
    private void uploadFile(DiskShare share, Path localFilePath, String remoteFilePath) {
        try (InputStream is = Files.newInputStream(localFilePath);
             OutputStream os = share.openFile(
                     remoteFilePath,
                     EnumSet.of(AccessMask.GENERIC_WRITE),
                     null,
                     SMB2ShareAccess.ALL,
                     SMB2CreateDisposition.FILE_CREATE,
                     null
             ).getOutputStream()) {

            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = is.read(buffer)) > 0) {
                os.write(buffer, 0, bytesRead);
            }
            Log.d(TAG, "Uploaded file: " + localFilePath);
        } catch (IOException e) {
            Log.e(TAG,"Error uploading file: " + localFilePath + e);
        }
    }
    private void moveDirectoryToErledigt(Path localDir) throws IOException {
        Path erledigtDir = Paths.get(ERLEDIGT_DIR);
        if (!Files.exists(erledigtDir)) {
            Files.createDirectories(erledigtDir);
        }

        Files.walk(localDir)
                .sorted(Comparator.reverseOrder())
                .forEach(p -> {
                    try {
                        if (Files.isDirectory(p)) {
                            Files.delete(p);
                        } else {
                            Path targetPath = erledigtDir.resolve(localDir.getFileName()).resolve(p.getFileName());
                            Files.createDirectories(targetPath.getParent());
                            Files.move(p, targetPath, StandardCopyOption.REPLACE_EXISTING);
                        }
                    } catch (IOException e) {
                        Log.d(TAG,"Error moving file: " + p + e);
                    }
                });
        Log.d(TAG, "Moved directory to 'erledigt': " + localDir);
    }
    public static void deleteOldFiles(String directoryPath) {
        Log.d(TAG, "Processing directory: " + directoryPath);
        File directory = new File(directoryPath);

        if (!directory.exists() || !directory.isDirectory()) return;

        for (File file : Objects.requireNonNull(directory.listFiles())) {
            if (file.isDirectory()) {
                deleteOldFiles(file.getAbsolutePath());
            } else if (file.isFile() && isFileOld(file)) {
                if (file.delete()) {
                    Log.d(TAG, "Deleted file: " + file.getAbsolutePath());
                } else {
                    Log.d(TAG, "Failed to delete file: " + file.getAbsolutePath());
                }
            }
        }

        if (directory.listFiles() != null && directory.listFiles().length == 0) {
            if (directory.delete()) {
                Log.d(TAG, "Deleted empty folder: " + directory.getAbsolutePath());
            } else {
                Log.d(TAG, "Failed to delete folder: " + directory.getAbsolutePath());
            }
        }
    }



    private static boolean isFileOld(File file) {
        try {
            String fileName = file.getName();
            int firstUnderscore = fileName.indexOf("_");
            int secondUnderscore = fileName.indexOf("_", firstUnderscore + 1);
            if (firstUnderscore == -1 || secondUnderscore == -1) return false;

            String dateString = fileName.substring(firstUnderscore + 1, secondUnderscore);
            Date fileDate = FILE_DATE_FORMAT.parse(dateString);
            long ageInDays = TimeUnit.MILLISECONDS.toDays(new Date().getTime() - fileDate.getTime());
            return ageInDays >= 14;
        } catch (Exception e) {
            Log.d(TAG, "Error parsing file date: " + file.getName());
            return false;
        }
    }

}