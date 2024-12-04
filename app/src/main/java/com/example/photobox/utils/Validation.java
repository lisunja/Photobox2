package com.example.photobox.utils;

import android.app.AlertDialog;
import android.content.Context;
import android.media.MediaPlayer;
import android.os.Build;
import android.widget.Toast;

import com.example.photobox.R;
import com.example.photobox.log.LogUtil;

import org.opencv.core.Rect;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;


public class Validation {
    public static void playErrorSound(Context context) {
        MediaPlayer mediaPlayer = MediaPlayer.create(context, R.raw.beep);
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                mediaPlayer.release();
            }
        });
        mediaPlayer.start();
        new AlertDialog.Builder(context)
                .setTitle("Invalid Sample Number")
                .setMessage("Sample number must be an 8-digit number that starts with the current year, previous year or next year")
                .setPositiveButton("OK", null)
                .show();
    }

    public static boolean validateSampleNr(String sample, Context context) {
        SecureStorage secureStorage = new SecureStorage(context);
        String year = secureStorage.getYear();
        if(year != null) {
            int currentYear = Integer.parseInt(year);
            String prevYear = String.valueOf(currentYear - 1);
            String nextYear = String.valueOf(currentYear + 1);
            return sample.matches("\\d{8}") && sample.matches("(" + prevYear + "|" + year + "|" + nextYear + ")\\d{6}"); // TODO sicherstellen, dass Probennummer mit aktuellem Jahreskürzel beginnt +-1

        }
        return false;
    }

//    public static boolean validateSampleNr(String sample, Context context) {
//        int currentYear = 0;
//        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
//            currentYear = LocalDate.now().getYear();
//        }
//        String year = String.valueOf(currentYear).substring(2);
//        if(year != null) {
//            int prefixCurrentYear = Integer.parseInt(year);
//            String prevYear = String.valueOf(prefixCurrentYear - 1);
//            String nextYear = String.valueOf(prefixCurrentYear + 1);
//            return sample.matches("\\d{8}") && sample.matches("(" + prevYear + "|" + year + "|" + nextYear + ")\\d{6}"); // TODO sicherstellen, dass Probennummer mit aktuellem Jahreskürzel beginnt +-1
//
//        }
//        return false;
//    }

//    peiodOfTime - indicator, that shows, when this methode was called (before uploading to the server or after)
    public synchronized static void showFiles(Path localPath, int peiodOfTime){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (Files.isDirectory(localPath)) {
                try (Stream<Path> paths = Files.list(localPath)) {
                    paths.forEach(path -> {
                        if (Files.isRegularFile(path)) {
                            switch (peiodOfTime){
                                case 1:
                                    LogUtil.writeLogToExternalStorage("Found file before uploading to server: " + path.toString());
                                break;
                                case 2:
                                    LogUtil.writeLogToExternalStorage("Found file after uploading to server(before deleting the directory with files in phone): " + path.toString());
                                case 3:
                                    LogUtil.writeLogToExternalStorage("Found file after deleting the directory with files): " + path.toString());
                                break;
                            }
                        }
                    });
                } catch (IOException e) {
                    LogUtil.writeLogToExternalStorage("Error listing files in directory: " + localPath.toString() + ", " + e.getMessage());
                }
            } else {
                LogUtil.writeLogToExternalStorage(localPath.toString() + " is not a directory.");
            }
        }
    }
    public static boolean validateCoordinates(Rect coordinates) {
        return coordinates != null && coordinates.width > 0 && coordinates.height > 0;
    }
}
