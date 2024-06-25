package com.example.photobox2;

import static android.content.ContentValues.TAG;

import android.os.Build;
import android.util.Log;

import com.hierynomus.msdtyp.AccessMask;
import com.hierynomus.mssmb2.SMB2CreateDisposition;
import com.hierynomus.mssmb2.SMB2ShareAccess;
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

public class SMBUtils {
    private static final String SMB_SERVER_IP = "172.16.0.54"; //  10.0.2.2
    private static final String SHARE_NAME = "Daten";
    private static final String USERNAME = "Yelyzaveta.Bespalova"; // DCBLN-TENTAMUS\
    private static final String PASSWORD = "89d866178530384810I";

    public boolean checkConnection(){
        Thread thread = new Thread(() -> {
            SMBClient client = new SMBClient();
            AuthenticationContext auth = new AuthenticationContext(USERNAME, PASSWORD.toCharArray(), "");
            try {
                Connection connection = client.connect(SMB_SERVER_IP);
                Session session = connection.authenticate(auth);
                DiskShare share = (DiskShare) session.connectShare(SHARE_NAME);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        thread.start();
        return true;
    }
    public void uploadAllFilesInDirectory(String baseDirectoryPath, String remoteBaseFolderPath) {
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
                            uploadFileToSmbServer(localFolderPath, remoteFolderPath, entry);
                            //deleteDirectory(entry);
                        }
                    }
                } catch (IOException e) {
                    Log.e(TAG, "Error reading directory", e);
                }
            }
        }
    }

    public void uploadFileToSmbServer(String localFolderPath, String remoteFolderPath, Path entry) {
        Thread thread = new Thread(() -> {
            try {
                SMBClient client = new SMBClient();
                AuthenticationContext auth = new AuthenticationContext(USERNAME, PASSWORD.toCharArray(), "");
                Connection connection = client.connect(SMB_SERVER_IP);
                Session session = connection.authenticate(auth);
                DiskShare share = (DiskShare) session.connectShare(SHARE_NAME);

                Path localPath;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    localPath = Paths.get(localFolderPath);

                    if (!share.folderExists(remoteFolderPath)) {
                        Log.d(TAG, "Creating directory: " + remoteFolderPath);
                        share.mkdir(remoteFolderPath);
                    } else {
                        Log.d(TAG, "Directory already exists: " + remoteFolderPath);
                    }

                    uploadRecursion(share, localPath, remoteFolderPath);
                }
                deleteDirectory(entry);
                share.close();
                session.close();
                connection.close();
            } catch (IOException e) {
                Log.e(TAG, "Error during SMB connection or file upload", e);
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
