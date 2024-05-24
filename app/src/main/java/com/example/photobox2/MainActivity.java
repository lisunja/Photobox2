package com.example.photobox2;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;

import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.AspectRatio;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;

import com.google.common.util.concurrent.ListenableFuture;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;
import com.theartofdev.edmodo.cropper.CropImageView;


import org.opencv.android.OpenCVLoader;
import org.opencv.core.Rect;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;


public class MainActivity extends AppCompatActivity {
    private final String IMAGES_FILE = "images.txt";
    private PreviewView previewView;
    private ImageView imageView;
    private EditText sampleEditText;
    private CropImageView cropImageView;
    int cameraFacing = CameraSelector.LENS_FACING_BACK;
    ImageButton photoBtn, scanBtn, checkBtn, deleteBtn;

    private final ActivityResultLauncher<String> activityResultLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), new ActivityResultCallback<Boolean>() {
        @Override
        public void onActivityResult(Boolean result) {
            if (result) {
                startCamera(cameraFacing);
            }
        }
    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        OpenCVLoader.initDebug();

        imageView = findViewById(R.id.photoImageView);
        previewView = findViewById(R.id.cameraPreview);
        sampleEditText = findViewById(R.id.editTextSampleNumber);
        cropImageView = findViewById(R.id.cropImageView);
        photoBtn = findViewById(R.id.photoAct);
        scanBtn = findViewById(R.id.scanAct);

        checkBtn = findViewById(R.id.checkAct);
        deleteBtn = findViewById(R.id.deleteAct);

        if (ContextCompat.checkSelfPermission(MainActivity.this, android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            activityResultLauncher.launch(Manifest.permission.CAMERA);
        } else {
            startCamera(cameraFacing);
        }
        scanBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                scanCode();
            }
        });
        deleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String lastPhoto = getLastImagePathFromTextFile();
                if (cropImageView.getVisibility()==View.VISIBLE)
                deleteImg(lastPhoto, false);
                imageView.setVisibility(View.GONE);
                cropImageView.setVisibility(View.GONE);
                previewView.setVisibility(View.VISIBLE);
                startCamera(cameraFacing);
            }
        });

        checkBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                savePhoto();
                saveCropCoordinates();
                cropImageView.setVisibility(View.GONE);
                imageView.setVisibility(View.GONE);
                previewView.setVisibility(View.VISIBLE);
                startCamera(cameraFacing);
            }
        });
    }

    public void savePhoto(){

    }

    public void showCropImageView() {
        imageView.setVisibility(View.GONE);
        previewView.setVisibility(View.GONE);
        cropImageView.setVisibility(View.VISIBLE);
        String lastPhotoPath = getLastImagePathFromTextFile();
        try {
            Bitmap bitmap = BitmapFactory.decodeFile(lastPhotoPath);
            Bitmap rotatedBitmap = rotateBitmap(bitmap, 90);
            ObjectDetection objectDetector = new ObjectDetection();
            org.opencv.core.Rect objectLocation  = objectDetector.detectObject(rotatedBitmap);

            if (isValidCoordinates(objectLocation)) {
                Log.d("MainActivity", "Object location: " + objectLocation.toString());

                cropImageView.setVisibility(View.VISIBLE);
                cropImageView.setImageUriAsync(Uri.fromFile(new File(lastPhotoPath)));

                int left = Math.max(0, objectLocation.x);
                int top = Math.max(0, objectLocation.y);
                int right = Math.min(rotatedBitmap.getWidth(), objectLocation.x + objectLocation.width);
                int bottom = Math.min(rotatedBitmap.getHeight(), objectLocation.y + objectLocation.height);

                android.graphics.Rect cropRect = new android.graphics.Rect(left, top, right, bottom);

                Log.d("MainActivity", "Crop rectangle: " + cropRect.toString());

                cropImageView.setCropRect(cropRect);

            } else {
                cropImageView.setVisibility(View.VISIBLE);
                cropImageView.setImageUriAsync(Uri.fromFile(new File(lastPhotoPath)));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
    private boolean isValidCoordinates(Rect coordinates) {
        return coordinates != null && coordinates.width > 0 && coordinates.height > 0;
    }
    public android.graphics.Rect convertToAndroidRect(org.opencv.core.Rect opencvRect) {
        return new android.graphics.Rect(opencvRect.x, opencvRect.y, opencvRect.x + opencvRect.width, opencvRect.y + opencvRect.height);
    }

    public void saveCropCoordinates() {
        String lastPhotoPath = getLastImagePathFromTextFile();
        File photoFile = new File(lastPhotoPath);

        File coordinatesFile = new File(getFilesDir(), photoFile.getName() + ".txt");

        if (photoFile.exists() && cropImageView != null) {
            float[] cropPoints = cropImageView.getCropPoints();
            if (cropPoints != null && cropPoints.length == 8) {
                float left = Math.min(Math.min(cropPoints[0], cropPoints[2]), Math.min(cropPoints[4], cropPoints[6]));
                float top = Math.min(Math.min(cropPoints[1], cropPoints[3]), Math.min(cropPoints[5], cropPoints[7]));
                float right = Math.max(Math.max(cropPoints[0], cropPoints[2]), Math.max(cropPoints[4], cropPoints[6]));
                float bottom = Math.max(Math.max(cropPoints[1], cropPoints[3]), Math.max(cropPoints[5], cropPoints[7]));
                try (FileOutputStream fos = new FileOutputStream(coordinatesFile, true)) {
                    fos.write(("Left: " + left + "\n").getBytes());
                    fos.write(("Top: " + top + "\n").getBytes());
                    fos.write(("Right: " + right + "\n").getBytes());
                    fos.write(("Bottom: " + bottom + "\n").getBytes());
                    Toast.makeText(this, "Coordinates saved", Toast.LENGTH_SHORT).show();
                } catch (IOException e) {
                    e.printStackTrace();
                    Toast.makeText(this, "Failed to save coordinates", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "Invalid crop points", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Photo file does not exist or cropImageView is null", Toast.LENGTH_SHORT).show();
        }
    }
    public void scanCode(){
        ScanOptions options = new ScanOptions();
        options.setPrompt("Volume up to flash on");
        options.setBeepEnabled(true);
        options.setOrientationLocked(true);
        options.setCaptureActivity(CaptureActivity.class);
        barLauncher.launch(options);
    }
    ActivityResultLauncher<ScanOptions> barLauncher = registerForActivityResult(new ScanContract(), result ->{
        if(result.getContents()!=null){
            sampleEditText.setText(result.getContents());
        }
    });
    public int aspectRatio (int width, int height){
        double previewRatio = (double)Math.max(width, height)/Math.min(width,height);
        if (Math.abs(previewRatio - 4.0 / 3.0) <= Math.abs(previewRatio - 16.0 / 9.0)){
            return AspectRatio.RATIO_4_3;
        }
        return AspectRatio.RATIO_16_9;
    }
    public void startCamera (int cameraFacing){
        int aspectRatio = aspectRatio (previewView.getWidth(), previewView.getHeight());
        ListenableFuture<ProcessCameraProvider> listenableFuture = ProcessCameraProvider.getInstance(this);
        listenableFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = (ProcessCameraProvider) listenableFuture.get();
                Preview preview = new Preview.Builder().setTargetAspectRatio(aspectRatio).build();
                ImageCapture imageCapture = new ImageCapture.Builder().setCaptureMode(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY)
                        .setTargetRotation(getWindowManager().getDefaultDisplay().getRotation()).build();
                CameraSelector cameraSelector = new CameraSelector.Builder()
                        .requireLensFacing(cameraFacing).build();
                cameraProvider.unbindAll();
                Camera camera = cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture);
                photoBtn.setOnClickListener(new View.OnClickListener(){

                    @Override
                    public void onClick(View v) {
                        if (ContextCompat.checkSelfPermission(MainActivity.this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                            activityResultLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE);
                        } else {
                            if (previewView.getVisibility() == View.VISIBLE) {
                                takePicture(imageCapture, false);
                            } else {
                                cropImageView.setVisibility(View.GONE);
                                previewView.setVisibility(View.VISIBLE);
                                imageView.setVisibility(View.GONE);
                                startCamera(cameraFacing);
                            }
                        }
                    }
                });

                preview.setSurfaceProvider(previewView.getSurfaceProvider());
            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }
        }, ContextCompat.getMainExecutor(this));
    }
    public void takePicture(ImageCapture imageCapture, boolean isQr) {
        final File file = new File(getFilesDir(), System.currentTimeMillis() + "jpg");
        ImageCapture.OutputFileOptions outputFileOptions = new ImageCapture.OutputFileOptions.Builder(file).build();


        imageCapture.takePicture(outputFileOptions, Executors.newCachedThreadPool(), new ImageCapture.OnImageSavedCallback() {
            @Override
            public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MainActivity.this, "Image saved at: " + file.getPath(), Toast.LENGTH_SHORT).show();
                        if (!isQr) {
                            try {
                                FileOutputStream fos = MainActivity.this.openFileOutput(IMAGES_FILE, Context.MODE_APPEND);
                                try {
                                    fos.write((file.getPath() + "\n").getBytes());
                                    fos.close();
                                } catch (IOException e) {
                                    throw new RuntimeException(e);
                                }
                                loadAndDisplayImage(file.getPath());
                                showCropImageView();
                            } catch (FileNotFoundException e) {
                                throw new RuntimeException(e);
                            }
                        }
                    }
                });
                startCamera(cameraFacing);
            }

            @Override
            public void onError(@NonNull ImageCaptureException exception) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MainActivity.this, "Failed to save: " + exception.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
                startCamera(cameraFacing);
            }
        });
    }
    public void loadAndDisplayImage(String imagePath) {
        File imgFile = new File(imagePath);
        if (imgFile.exists()) {
            Bitmap bitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
            Bitmap rotatedBitmap = rotateBitmap(bitmap, 90);
            previewView.setVisibility(View.GONE);
            imageView.setVisibility(View.VISIBLE);
            imageView.setImageBitmap(rotatedBitmap);
        }
    }
    public Bitmap rotateBitmap(Bitmap bitmap, int orientation) {
        Matrix matrix = new Matrix();
        matrix.postRotate(orientation);
        Bitmap rotatedBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        imageView.setImageBitmap(rotatedBitmap);
        return rotatedBitmap;
    }
    public void deleteImg(String photoPath, boolean isScan) {
        if(!isScan) {
            try {
                List<String> lines = new ArrayList<>();

                FileInputStream fis = openFileInput(IMAGES_FILE);
                InputStreamReader isr = new InputStreamReader(fis);
                BufferedReader br = new BufferedReader(isr);
                String line;
                while ((line = br.readLine()) != null) {
                    if (!line.equals(photoPath)) {
                        lines.add(line);
                    }
                }
                br.close();
                if (lines.size() != 0) {
                    FileOutputStream fos = openFileOutput(IMAGES_FILE, Context.MODE_PRIVATE);
                    PrintWriter pw = new PrintWriter(fos);
                    for (String newLine : lines) {
                        pw.println(newLine);
                    }
                    pw.close();
                    File fileToDelete = new File(photoPath);
                    fileToDelete.delete();
                    Toast.makeText(MainActivity.this, "Photo was deleted", Toast.LENGTH_SHORT).show();
                }
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
    public String getLastImagePathFromTextFile() {
        String lastImagePath = null;
        try {
            FileInputStream fis = openFileInput(IMAGES_FILE);
            InputStreamReader isr = new InputStreamReader(fis);
            BufferedReader br = new BufferedReader(isr);
            String line;
            while ((line = br.readLine()) != null) {
                lastImagePath = line;
            }
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return lastImagePath;
    }

}