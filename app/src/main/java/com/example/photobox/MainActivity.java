package com.example.photobox;


import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.AspectRatio;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraControl;
import androidx.camera.core.CameraInfo;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;

import com.example.photobox.service.FileUploadService;
import com.example.photobox.utils.Photo;
import com.example.photobox.utils.Validation;
import com.example.photobox.view.SettingActivity;
import com.google.common.util.concurrent.ListenableFuture;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;
import com.theartofdev.edmodo.cropper.CropImageView;

import org.opencv.android.OpenCVLoader;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;


public class MainActivity extends AppCompatActivity {
    private Photo photoAction;
    private Camera camera;
    private String sampleNr = "";
    private PreviewView previewView;
    private ImageView imageView;
    private EditText sampleEditText;
    private CropImageView cropImageView;
    int cameraFacing = CameraSelector.LENS_FACING_BACK;
    private Bitmap originalPhoto;
    ImageButton photoBtn, scanBtn, checkBtn, deleteBtn;
//    private List<CameraInfo> cameras;

//    Button halfZoom, oneZoom, twoZoom;
//    private LinearLayout buttonLayout;
//    private SettingsDatabaseManager settingsDatabaseManager;

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
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
//        cameras = new ArrayList<>();
//        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(this);
//        cameraProviderFuture.addListener(() -> {
//            try {
//                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
//                cameras = cameraProvider.getAvailableCameraInfos();
//
//            } catch (ExecutionException | InterruptedException e) {
//                e.printStackTrace();
//            }
//        }, ContextCompat.getMainExecutor(this));
//        settingsDatabaseManager = new SettingsDatabaseManager(this);
//            try {
//                settingsDatabaseManager.open();
//            }
//            catch (Exception e){
//                e.printStackTrace();
//            }
        init();
    }
//    private void openCameraByIndex(int index) {
//        int aspectRatio = aspectRatio(previewView.getWidth(), previewView.getHeight());
//        ListenableFuture<ProcessCameraProvider> listenableFuture = ProcessCameraProvider.getInstance(this);
//        listenableFuture.addListener(() -> {
//            try {
//                ProcessCameraProvider cameraProvider = listenableFuture.get();
//                Preview preview = new Preview.Builder()
//                        .setTargetAspectRatio(aspectRatio)
//                        .build();
//                ImageCapture imageCapture = new ImageCapture.Builder()
//                        .setCaptureMode(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY)
//                        .setTargetRotation(getWindowManager().getDefaultDisplay().getRotation())
//                        .build();
//                CameraSelector cameraSelector;
//                if (index == 0) {
//                    cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA;
//                } else {
//                    cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA;
//                }
//                cameraProvider.unbindAll();
//                camera = cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture);
//
//                photoBtn.setOnClickListener(v -> {
//                    if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
//                        activityResultLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE);
//                    } else {
//                        if (previewView.getVisibility() == View.VISIBLE) {
//                            takePicture(imageCapture);
//                        } else {
//                            cropImageView.setVisibility(View.GONE);
//                            previewView.setVisibility(View.VISIBLE);
//                            imageView.setVisibility(View.GONE);
//                            openCameraByIndex(index);
//                        }
//                    }
//                });
//
//                preview.setSurfaceProvider(previewView.getSurfaceProvider());
//            } catch (ExecutionException | InterruptedException e) {
//                e.printStackTrace();
//            }
//        }, ContextCompat.getMainExecutor(this));
//    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.setting_action) {
            Intent intent = new Intent(this, SettingActivity.class);
            startActivity(intent);
            return true;
        }
        if (id == R.id.halfZoom) {
            setZoom(camera, 1.0f);
            return true;
        }
        if (id == R.id.oneZoom) {
            setZoom(camera, 2.0f);
            return true;
        }
        if (id == R.id.twoZoom) {
            setZoom(camera, 3.0f);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    public void init(){
        photoAction = new Photo(findViewById(android.R.id.content));
        imageView = findViewById(R.id.photoImageView);
        previewView = findViewById(R.id.cameraPreview);
        sampleEditText = findViewById(R.id.editTextSampleNumber);
        cropImageView = findViewById(R.id.cropImageView);
        photoBtn = findViewById(R.id.photoAct);
        scanBtn = findViewById(R.id.scanAct);
//        halfZoom = findViewById(R.id.halfZoom);
//        oneZoom = findViewById(R.id.oneZoom);
//        twoZoom = findViewById(R.id.twoZoom);
        checkBtn = findViewById(R.id.checkAct);
        deleteBtn = findViewById(R.id.deleteAct);
//        buttonLayout = findViewById(R.id.buttonLayout);
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            activityResultLauncher.launch(Manifest.permission.CAMERA);
        } else {
           // openCameraByIndex(2);
            startCamera(cameraFacing);
        }
        sampleEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    String result = sampleEditText.getText().toString();
                    if(Validation.validateSampleNr(result)) {
                        sampleNr = result;
                    }
                    else {
                        Validation.playErrorSound(MainActivity.this);


                    }
                    return true;
                }
                return false;
            }
        });
//        halfZoom.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//               setZoom(camera, 1.0f);
//
//            }
//        });
//        oneZoom.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                setZoom(camera, 2.0f);
//            }
//        });
//        twoZoom.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                setZoom(camera, 3.0f);
//            }
//        });
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
//                buttonLayout.setVisibility(View.VISIBLE);
                startCamera(cameraFacing);
            }
        });

        checkBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sampleNr = sampleEditText.getText().toString();
                if (Validation.validateSampleNr(sampleNr)) { // TODO
                    createDirectory(MainActivity.this, sampleNr);
                    //                    Thread thread = new Thread(() -> {
                    File imageFile = saveBitmapToDirectory(MainActivity.this, originalPhoto, sampleNr);
                    photoAction.setSampleNr(sampleNr);
                    photoAction.saveCropCoordinates(imageFile.getAbsolutePath(), MainActivity.this);
                    //                    });
                    //                    thread.start();
//                    buttonLayout.setVisibility(View.VISIBLE);
                    cropImageView.setVisibility(View.GONE);
                    imageView.setVisibility(View.GONE);
                    previewView.setVisibility(View.VISIBLE);
                    startCamera(cameraFacing);
                }
                else {
                    Validation.playErrorSound(MainActivity.this);
                }
            }
        });
    }
    private float getMinZoom(Camera camera) {
        CameraInfo cameraInfo = camera.getCameraInfo();
        return cameraInfo.getZoomState().getValue().getMinZoomRatio();
    }

    private float getMaxZoom(Camera camera) {
        CameraInfo cameraInfo = camera.getCameraInfo();
        return cameraInfo.getZoomState().getValue().getMaxZoomRatio();
    }

    private void setZoom(Camera camera, float zoomRatio) {
        float minZoom = getMinZoom(camera);
        float maxZoom = getMaxZoom(camera);
        CameraControl cameraControl = camera.getCameraControl();
        cameraControl.setZoomRatio(zoomRatio);
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
        // TODO java.time verwenden statt java.util.Date
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.getDefault());
        String currentTime = sdf.format(new Date());
        String fileName = sampleNr + "_" + currentTime + ".jpg";
        File directory = new File(context.getFilesDir(), directoryName);
        File file = new File(directory, fileName);
        try {
            OutputStream os = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, os);
            os.flush();
            os.close();
            return file;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }


    public void showCropImageView(Bitmap reducedPhoto) {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int width = displayMetrics.widthPixels;
        int height = displayMetrics.heightPixels;
        ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) cropImageView.getLayoutParams();
        params.width = height;
        params.height = width;
        cropImageView.setLayoutParams(params);
        cropImageView.setVisibility(View.VISIBLE);
        cropImageView.setAutoZoomEnabled(false);
        cropImageView.setMaxZoom(1);
        previewView.setVisibility(View.GONE);

        try {

            Rect objectLocation = ObjectDetection.detectObject(reducedPhoto);

//            if (Validation.validateCoordinates(objectLocation)) { // TODO wirklich notwendig?
            Log.d("MainActivity", "Object location: " + objectLocation);
            cropImageView.setImageBitmap(reducedPhoto);
            cropImageView.setScaleType(CropImageView.ScaleType.FIT_CENTER);
            cropImageView.setCropRect(objectLocation);
            sampleEditText.bringToFront();
//            } else {
//                cropImageView.setScaleType(CropImageView.ScaleType.FIT_CENTER);
//                cropImageView.setImageBitmap(photo);
//            }
        } catch (Exception e) {
            e.printStackTrace();
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
        // TODO in den Einstellungen festlegen, ob Überprüfung der Probennummer erfolgt
        if(result.getContents()!=null){
            sampleNr = result.getContents();
            if(Validation.validateSampleNr(sampleNr)){
                sampleEditText.setText(sampleNr);
            }
            else {
                Validation.playErrorSound(MainActivity.this);
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
                camera = cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture);
                photoBtn.setOnClickListener(new View.OnClickListener(){

                    @Override
                    public void onClick(View v) {
                        if (ContextCompat.checkSelfPermission(MainActivity.this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                            activityResultLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE);
                        } else {
                            if (previewView.getVisibility() == View.VISIBLE) {
                                takePicture(imageCapture);
//                                buttonLayout.setVisibility(View.GONE);
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
                originalPhoto = image.toBitmap();
                image.close();

                runOnUiThread(() -> {
                    Bitmap reducedPhoto = photoAction.reducePhoto(originalPhoto);
                    showCropImageView(reducedPhoto);

                });

            }
            @Override
            public void onError(@NonNull ImageCaptureException exception) {
                super.onError(exception);
                Log.e("CameraX", "Capture error: " + exception.getMessage());
            }
        });
    }

}