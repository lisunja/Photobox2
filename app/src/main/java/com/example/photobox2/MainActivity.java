package com.example.photobox2;


import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
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
import androidx.camera.core.ImageProxy;
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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;


public class MainActivity extends AppCompatActivity {
    private final int factor = 5;
    private String sample = "";
    private PreviewView previewView;
    private ImageView imageView;
    private EditText sampleEditText;
    private CropImageView cropImageView;
    int cameraFacing = CameraSelector.LENS_FACING_BACK;
    private Bitmap photo;
    private Bitmap photoToSave;
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
        Intent serviceIntent = new Intent(this, FileUploadService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent);
        }
        imageView = findViewById(R.id.photoImageView);
        previewView = findViewById(R.id.cameraPreview);
        sampleEditText = findViewById(R.id.editTextSampleNumber);
        cropImageView = findViewById(R.id.cropImageView);
        photoBtn = findViewById(R.id.photoAct);
        scanBtn = findViewById(R.id.scanAct);

        checkBtn = findViewById(R.id.checkAct);
        deleteBtn = findViewById(R.id.deleteAct);

        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            activityResultLauncher.launch(Manifest.permission.CAMERA);
        } else {
            startCamera(cameraFacing);
        }
        sampleEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    String result = sampleEditText.getText().toString();
                    if(validateSample(result)) {
                        sample = result;
                    }
                    else {
                        playErrorSound(MainActivity.this);


                    }
                    return true;
                }
                return false;
            }
        });
        scanBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                scanCode();
            }
        });
        deleteBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
               // String lastPhoto = getLastImagePathFromTextFile();
                if (cropImageView.getVisibility()==View.VISIBLE)
                //deleteImg(lastPhoto, false);
                imageView.setVisibility(View.GONE);
                cropImageView.setVisibility(View.GONE);
                previewView.setVisibility(View.VISIBLE);
                startCamera(cameraFacing);
            }
        });

        checkBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sample = sampleEditText.getText().toString();

//                FileUploadService fileUploadService = new FileUploadService();
//                fileUploadService.setSample(sample);

                createDirectory(MainActivity.this, sample);
//                    Thread thread = new Thread(() -> {
                File imageFile = saveBitmapToDirectory(MainActivity.this, photoToSave, sample);
//                if (imageFile != null) {
//                    saveImagePathToFile(imageFile);
//                }

                    saveCropCoordinates(imageFile.getAbsolutePath());
//                    });
//                    thread.start();
                    cropImageView.setVisibility(View.GONE);
                    imageView.setVisibility(View.GONE);
                    previewView.setVisibility(View.VISIBLE);
                    startCamera(cameraFacing);

            }
        });


    }


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

    public static boolean validateSample(String sample) {
            return sample.matches("\\d{8}");
    }
    private void createDirectory(Context context, String directoryName){
        File directory = new File(context.getFilesDir(), directoryName);
        if (!directory.exists()) {
            boolean created = directory.mkdirs();
            if (created) {
                System.out.println("Directory created " + directory.getAbsolutePath());
            } else {
                System.err.println("Error" + directory.getAbsolutePath());
            }
        } else {
            System.out.println("Directory has already exists" + directory.getAbsolutePath());
        }
    }

    public File saveBitmapToDirectory(Context context, Bitmap bitmap, String directoryName) {

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        String currentTime = sdf.format(new Date());
        String fileName = sample + ":" + currentTime + ".jpg";
        File directory = new File(context.getFilesDir(), directoryName);
        File file = new File(directory, fileName);
        try {

            OutputStream os = new FileOutputStream(file);

            bitmap.compress(Bitmap.CompressFormat.PNG, 100, os);
            os.flush();
            os.close();
            return file;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }


    public void showCropImageView() {

        cropImageView.setVisibility(View.VISIBLE);
        cropImageView.setAutoZoomEnabled(false);
        cropImageView.setMaxZoom(1);
        previewView.setVisibility(View.GONE);
        try {

            ObjectDetection objectDetector = new ObjectDetection();

            org.opencv.core.Rect objectLocation = objectDetector.detectObject(photo);

            if (isValidCoordinates(objectLocation)) {
                Log.d("MainActivity", "Object location: " + objectLocation);

                cropImageView.setImageBitmap(photo);

                int left = Math.max(0, objectLocation.x - 15);
                int top = Math.max(0, objectLocation.y - 15);
                int right = Math.min(photo.getWidth(), objectLocation.x + objectLocation.width + 15);
                int bottom = Math.min(photo.getHeight(), objectLocation.y + objectLocation.height + 15);

                android.graphics.Rect cropRect = new android.graphics.Rect(left, top, right, bottom);
                cropImageView.setScaleType(CropImageView.ScaleType.FIT_CENTER);
                cropImageView.setCropRect(cropRect);
            } else {
                cropImageView.setScaleType(CropImageView.ScaleType.FIT_CENTER);
                cropImageView.setImageBitmap(photo);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
    private boolean isValidCoordinates(Rect coordinates) {
        return coordinates != null && coordinates.width > 0 && coordinates.height > 0;
    }
    public void saveCropCoordinates(String lastPhotoPath) {
        //String lastPhotoPath = getLastImagePathFromTextFile();
        File photoFile = new File(lastPhotoPath);

        File coordinatesFile = new File(getFilesDir() + "/" + sample , photoFile.getName() + ".txt");

        if (photoFile.exists() && cropImageView != null) {
            float[] cropPoints = cropImageView.getCropPoints();
            if (cropPoints != null && cropPoints.length == 8) {
                float bottom = Math.min(Math.min(cropPoints[0], cropPoints[2]), Math.min(cropPoints[4], cropPoints[6]));
                float left = Math.min(Math.min(cropPoints[1], cropPoints[3]), Math.min(cropPoints[5], cropPoints[7]));
                float  top= Math.max(Math.max(cropPoints[0], cropPoints[2]), Math.max(cropPoints[4], cropPoints[6]));
                float right= Math.max(Math.max(cropPoints[1], cropPoints[3]), Math.max(cropPoints[5], cropPoints[7]));

                try (FileOutputStream fos = new FileOutputStream(coordinatesFile, true)) {
                    fos.write(("cropLeft=" + (int)left*factor + "\n").getBytes());
                    fos.write(("cropTop=" + (int)bottom*factor + "\n").getBytes());
                    fos.write(("cropRight=" + (int)right*factor + "\n").getBytes());
                    fos.write(("cropBottom=" + (int)top*factor + "\n").getBytes());
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
            sample = result.getContents();
            if(validateSample(sample)){
                sampleEditText.setText(sample);
            }
            else {
                playErrorSound(MainActivity.this);
            }
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
                                takePicture(imageCapture);
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
    public void takePicture(ImageCapture imageCapture) {
        imageCapture.takePicture(Executors.newCachedThreadPool(), new ImageCapture.OnImageCapturedCallback() {
            @Override
            public void onCaptureSuccess(@NonNull ImageProxy image) {
                super.onCaptureSuccess(image);
                Log.d("CameraX", "Capture success");
                Bitmap bitmap = image.toBitmap();
                image.close();

                runOnUiThread(() -> {
//                    long startTime = SystemClock.elapsedRealtime();
                    Bitmap reducedPhoto = reducePhoto(bitmap);
                    photo = rotateBitmap(reducedPhoto);
                    Thread thread = new Thread(() -> {
                    photoToSave = bitmap;
                    });
                    thread.start();

                    //photo = restoreOriginalSize(rotatedBitmap, bitmap.getWidth(), bitmap.getHeight());
//                    long endTime1 = SystemClock.elapsedRealtime();
//                    long elapsedTime = endTime1 - startTime;
//                    long startTime2 = SystemClock.elapsedRealtime();
                    showCropImageView();
//                    long endTime = SystemClock.elapsedRealtime();
//                    long elapsedTime2 = endTime - startTime2;
                });

            }
            @Override
            public void onError(@NonNull ImageCaptureException exception) {
                super.onError(exception);
                Log.e("CameraX", "Capture error: " + exception.getMessage());
            }
        });
    }

    public Bitmap reducePhoto(Bitmap bitmap){
        int width = bitmap.getWidth()/factor;
        int height = bitmap.getHeight()/factor;
        return Bitmap.createScaledBitmap(bitmap, width, height, true);
    }
    public Bitmap rotateBitmap(Bitmap bitmap) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int[] pixels = new int[width * height];
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height);

        int[] rotatedPixels = new int[width * height];
            for (int i = 0; i < height; ++i) {
                for (int j = 0; j < width; ++j) {
                    rotatedPixels[j * height + height - 1 - i] = pixels[i * width + j];
                }
            }


        return Bitmap.createBitmap(rotatedPixels, height, width, Bitmap.Config.ARGB_8888);
    }



}