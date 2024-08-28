package com.example.photobox.utils;

import android.content.Context;
import android.media.MediaPlayer;
import android.widget.Toast;

import com.example.photobox.R;

import org.opencv.core.Rect;

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
        Toast.makeText(context, "Sample number must be an 8-digit number ", Toast.LENGTH_LONG).show();

    }

    public static boolean validateSampleNr(String sample) {
        return sample.matches("\\d{8}"); // TODO sicherstellen, dass Probennummer mit aktuellem Jahreskürzel beginnt +-1
    }
    public static boolean validateCoordinates(Rect coordinates) {
        return coordinates != null && coordinates.width > 0 && coordinates.height > 0;
    }
}
