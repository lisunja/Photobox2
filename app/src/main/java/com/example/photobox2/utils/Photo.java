package com.example.photobox2.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.View;
import android.widget.Toast;

import com.example.photobox2.R;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public final class Photo {
    private String sampleNr;
    private CropImageView cropImageView;
    //reduce factor
    private final int factor = 5;
    public Photo(View view){
        cropImageView = view.findViewById(R.id.cropImageView);
    }
    public void setSampleNr(String sampleNr) {
        this.sampleNr = sampleNr;
    }
    public Bitmap reducePhoto(Bitmap bitmap){
        int width = bitmap.getWidth()/factor;
        int height = bitmap.getHeight()/factor;
        return Bitmap.createScaledBitmap(bitmap, width, height, true);
    }
    public void saveCropCoordinates(String lastPhotoPath, Context context) {
        File photoFile = new File(lastPhotoPath);

        File coordinatesFile = new File(context.getFilesDir() + "/" + sampleNr , photoFile.getName() + ".txt");

        if (photoFile.exists() && cropImageView != null) {
            float[] cropPoints = cropImageView.getCropPoints();
            if (cropPoints != null && cropPoints.length == 8) {
                float x1 = cropPoints[0];
                float y1 = cropPoints[1];
                float x2 = cropPoints[2];
                float y2 = cropPoints[3];
                float x3 = cropPoints[4];
                float y3 = cropPoints[5];
                float x4 = cropPoints[6];
                float y4 = cropPoints[7];

                float top = Math.max(Math.max(y1, y2), Math.max(y3, y4));
                float bottom = Math.min(Math.min(y1, y2), Math.min(y3, y4));
                float left = Math.min(Math.min(x1, x2), Math.min(x3, x4));
                float right = Math.max(Math.max(x1, x2), Math.max(x3, x4));

                try (FileOutputStream fos = new FileOutputStream(coordinatesFile, true)) {
                    fos.write(("cropLeft=" + (int)left*factor + "\n").getBytes());
                    fos.write(("cropTop=" + (int)bottom*factor + "\n").getBytes());
                    fos.write(("cropRight=" + (int)right*factor + "\n").getBytes());
                    fos.write(("cropBottom=" + (int)top*factor + "\n").getBytes());
                    Toast.makeText(context, "Coordinates saved", Toast.LENGTH_SHORT).show();
                } catch (IOException e) {
                    e.printStackTrace();
                    Toast.makeText(context, "Failed to save coordinates", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(context, "Invalid crop points", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(context, "Photo file does not exist or cropImageView is null", Toast.LENGTH_SHORT).show();
        }
    }

}
