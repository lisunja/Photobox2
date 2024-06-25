package com.example.photobox2;


import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
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
    private final String IMAGES_FILE = "images.txt";
    private String sample = "";
    private PreviewView previewView;
    private ImageView imageView;
    private EditText sampleEditText;
    private CropImageView cropImageView;
    int cameraFacing = CameraSelector.LENS_FACING_BACK;
    private Bitmap photo;
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

                File imageFile = saveBitmapToDirectory(MainActivity.this, photo, sample);
//                if (imageFile != null) {
//                    saveImagePathToFile(imageFile);
//                }
                saveCropCoordinates(imageFile.getAbsolutePath());
                cropImageView.setVisibility(View.GONE);
                imageView.setVisibility(View.GONE);
                previewView.setVisibility(View.VISIBLE);
                startCamera(cameraFacing);
            }
        });
//        sampleEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
//            @Override
//            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
//                if (actionId == EditorInfo.IME_ACTION_DONE) {
//                    String filePath = getFilesDir() + "/" + sample;
//                    if (!filePath.isEmpty()) {
//                        uploadFileToServer(filePath);
//                    }
//                    return true;
//                }
//                return false;
//            }
//        });
    }
//    private void uploadFileToServer(String sampleFiePath) {
//        Intent intent = new Intent(this, FileUploadService.class);
//        intent.putExtra("filePath", sampleFiePath);
//
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            startForegroundService(intent);
//        } else {
//            startService(intent);
//        }
//    }
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
//    private void saveImagePathToFile(File imageFile) {
//        File file = new File(getFilesDir(), IMAGES_FILE);
//        try (FileWriter writer = new FileWriter(file, true)) {
//            writer.append(imageFile.getAbsolutePath()).append("\n");
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
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
            sample = result.getContents();
            sampleEditText.setText(sample);
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
                    long startTime = SystemClock.elapsedRealtime();
                    Bitmap reducedPhoto = reducePhoto(bitmap);
                    photo = rotateBitmap(reducedPhoto);
                    //photo = restoreOriginalSize(rotatedBitmap, bitmap.getWidth(), bitmap.getHeight());
                    long endTime1 = SystemClock.elapsedRealtime();
                    long elapsedTime = endTime1 - startTime;
                    long startTime2 = SystemClock.elapsedRealtime();
                    showCropImageView();
                    long endTime = SystemClock.elapsedRealtime();
                    long elapsedTime2 = endTime - startTime2;
                });

            }
            @Override
            public void onError(@NonNull ImageCaptureException exception) {
                super.onError(exception);
                Log.e("CameraX", "Capture error: " + exception.getMessage());
            }
        });
    }
//    public Bitmap restoreOriginalSize(Bitmap bitmap, int originalWidth, int originalHeight) {
//        Bitmap originalSizeBitmap = Bitmap.createScaledBitmap(bitmap, originalWidth, originalHeight*2, true);
//        return originalSizeBitmap;
//    }
    public Bitmap reducePhoto(Bitmap bitmap){
        int width = bitmap.getWidth()/5;
        int height = bitmap.getHeight()/5;
        Bitmap resizedBitmap = Bitmap.createScaledBitmap(bitmap, width, height, true);
        return resizedBitmap;
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
        Bitmap rotatedBitmap = Bitmap.createBitmap(rotatedPixels, height, width, Bitmap.Config.ARGB_8888);


        return rotatedBitmap;
    }

//    public Bitmap rotateBitmap2(Bitmap bitmap) {
//        int width = bitmap.getWidth();
//        int height = bitmap.getHeight();
//        Bitmap rotatedBitmap = Bitmap.createBitmap(height, width, Bitmap.Config.ARGB_8888);
//        for (int x = 0; x < width; x++) {
//            for (int y = 0; y < height; y++) {
//                rotatedBitmap.setPixel(y, x, bitmap.getPixel(x, y));
//            }
//        }
//        return rotatedBitmap;
//    }

//    public void deleteImg(String photoPath, boolean isScan) {
//        if(!isScan) {
//            try {
//                List<String> lines = new ArrayList<>();
//
//                FileInputStream fis = openFileInput(IMAGES_FILE);
//                InputStreamReader isr = new InputStreamReader(fis);
//                BufferedReader br = new BufferedReader(isr);
//                String line;
//                while ((line = br.readLine()) != null) {
//                    if (!line.equals(photoPath)) {
//                        lines.add(line);
//                    }
//                }
//                br.close();
//                if (lines.size() != 0) {
//                    FileOutputStream fos = openFileOutput(IMAGES_FILE, Context.MODE_PRIVATE);
//                    PrintWriter pw = new PrintWriter(fos);
//                    for (String newLine : lines) {
//                        pw.println(newLine);
//                    }
//                    pw.close();
//                    File fileToDelete = new File(photoPath);
//                    fileToDelete.delete();
//                    Toast.makeText(MainActivity.this, "Photo was deleted", Toast.LENGTH_SHORT).show();
//                }
//            } catch (FileNotFoundException e) {
//                throw new RuntimeException(e);
//            } catch (IOException e) {
//                throw new RuntimeException(e);
//            }
//        }
//    }
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