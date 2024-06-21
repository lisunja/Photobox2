package com.example.photobox2;

import android.os.Build;

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

    //    public boolean readFromDirectory(){
//        String filePath = "\\\\172.16.0.54\\Daten\\file.txt";
//        File file = new File(filePath);
//        return file.exists() && file.canRead();
//    }
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
    public void uploadFileToSmbServer(String localFolderPath, String remoteFolderPath) {
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
                    uploadRecursion(share, localPath, remoteFolderPath);
                }

                share.close();
                session.close();
                connection.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        thread.start();
    }

    private void uploadRecursion(DiskShare share, Path localPath, String remotePath) throws IOException {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (Files.isDirectory(localPath)) {

                if (!share.folderExists(remotePath)) {
                    share.mkdir(remotePath);
                }

                try (DirectoryStream<Path> stream = Files.newDirectoryStream(localPath)) {
                    for (Path entry : stream) {
                        String childRemotePath = remotePath + "\\" + entry.getFileName().toString();
                        uploadRecursion(share, entry, childRemotePath);
                    }
                }
            } else {
                try (InputStream is = Files.newInputStream(localPath);
                     OutputStream os = share.openFile(
                             remotePath,
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
                }
            }
        }
    }

}
