package com.example.photobox.view;

import static android.content.ContentValues.TAG;

import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuInflater;
import android.widget.GridView;
import android.widget.PopupMenu;

import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.photobox.R;
import com.example.photobox.service.FileUploadService;
import com.example.photobox.service.ImageAdapter;
import com.example.photobox.utils.SMBUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ImageListActivity extends AppCompatActivity {
    private GridView imageGridView;
    private ImageAdapter imageAdapter;
    private List<File> imageFiles;
    SwipeRefreshLayout swipeRefreshLayout;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_list);

        imageGridView = findViewById(R.id.imageGridView);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);

        String folderPath = getIntent().getStringExtra("folderPath");

        imageFiles = getImagesFromFolder(folderPath);
        swipeRefreshLayout.setOnRefreshListener(() -> {
            refreshData(folderPath);
            swipeRefreshLayout.setRefreshing(false);
        });
        imageAdapter = new ImageAdapter(this, imageFiles);
        imageGridView.setAdapter(imageAdapter);

        imageGridView.setOnItemLongClickListener((parent, view, position, id) -> {
            File selectedImage = imageFiles.get(position);

            PopupMenu popupMenu = new PopupMenu(ImageListActivity.this, view);
            MenuInflater inflater = getMenuInflater();
            inflater.inflate(R.menu.image_context_menu, popupMenu.getMenu());

            popupMenu.setOnMenuItemClickListener(menuItem -> {
                if (menuItem.getItemId() == R.id.action_upload) {
                    Log.d(TAG, "upload called in ImageListActivity for: " + folderPath);
                    try {
                        String folderName = extractFolderName(selectedImage.getName());
                        if (folderName.isEmpty()) {
                            Log.w(TAG, "Invalid file name format: " + selectedImage.getName());
                            return true;
                        }
                        if (moveToFileSystemFromErledigt(folderName, selectedImage)) {
                            new AlertDialog.Builder(this)
                                    .setTitle("Success")
                                    .setMessage("The files has been successfully moved.")
                                    .setPositiveButton("OK", null)
                                    .show();
                        } else {
                            new AlertDialog.Builder(this)
                                    .setTitle("Error")
                                    .setMessage("Failed to move the files.")
                                    .setPositiveButton("OK", null)
                                    .show();
                        }
                        return true;
                    } catch (Exception e) {
                        Log.e(TAG, "Error while moving file", e);
                    }
                        return true;
                }
                if (menuItem.getItemId() == R.id.action_delete) {
                    showDeleteConfirmationDialog(selectedImage);
                }
                    return false;
            });

           popupMenu.show();

            return true;
        });
    }
    public void refreshData(String folderPath) {
        imageFiles = getImagesFromFolder(folderPath);
        imageAdapter = new ImageAdapter(this, imageFiles);
        imageGridView.setAdapter(imageAdapter);
        imageAdapter.notifyDataSetChanged();
    }
    public void showDeleteConfirmationDialog(File selectedImage) {
        new AlertDialog.Builder(this)
                .setTitle("Confirm deletion")
                .setMessage("Are you sure, that you want to delete this file?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    if (deleteImageFile(selectedImage)) {
                        Log.d(TAG, "Image file deleted successfully: " + selectedImage.getAbsolutePath());
                    }
                    String folderName = extractFolderName(selectedImage.getName());
                    File targetFolder = new File(SMBUtils.ERLEDIGT_DIR, folderName);
                    String txtFileName = selectedImage.getName() + ".txt";
                    File targetTxtFile = new File(targetFolder, txtFileName);
                    if (targetTxtFile != null && deleteTxtFile(targetTxtFile)) {
                        Log.d(TAG, ".txt file deleted successfully: " + targetTxtFile.getAbsolutePath());
                    }
                })
                .setNegativeButton("No", null)
                .show();
    }
    public boolean deleteTxtFile(File txtFile) {
        if (txtFile.exists()) {
            if (txtFile.delete()) {
                Log.d(TAG, ".txt file deleted: " + txtFile.getAbsolutePath());
                return true;
            } else {
                Log.e(TAG, "Failed to delete .txt file: " + txtFile.getAbsolutePath());
                return false;
            }
        } else {
            Log.w(TAG, ".txt file not found: " + txtFile.getAbsolutePath());
            return false;
        }
    }
    public boolean deleteImageFile(File imageFile) {
        if (imageFile.exists()) {
            if (imageFile.delete()) {
                Log.d(TAG, "Image file deleted: " + imageFile.getAbsolutePath());
                return true;
            } else {
                Log.e(TAG, "Failed to delete image file: " + imageFile.getAbsolutePath());
                return false;
            }
        } else {
            Log.w(TAG, "Image file not found: " + imageFile.getAbsolutePath());
            return false;
        }
    }

    public boolean moveToFileSystemFromErledigt(String folderName, File selectedImage){
        try {
            File targetFolder = new File(getFilesDir(), folderName);
            if (!targetFolder.exists()) {
                if (targetFolder.mkdirs()) {
                    Log.d(TAG, "Target folder created: " + targetFolder.getAbsolutePath());
                } else {
                    Log.e(TAG, "Failed to create target folder: " + targetFolder.getAbsolutePath());
                    return false;
                }
            }

            File targetFile = new File(targetFolder, selectedImage.getName());
            if (selectedImage.renameTo(targetFile)) {
                Log.d(TAG, "File moved successfully to: " + targetFile.getAbsolutePath());
            } else {
                Log.e(TAG, "Failed to move file: " + selectedImage.getAbsolutePath());
            }
            return moveTxt(selectedImage, targetFolder);
        } catch (Exception e) {
            Log.e(TAG, "Error while moving file", e);
        }
        return false;
    }

    public boolean moveTxt(File selectedImage, File targetFolder) {
        File sourceFolder = new File(selectedImage.getParent());

        if (sourceFolder.exists() && sourceFolder.isDirectory()) {
            String txtFileName = selectedImage.getName() + ".txt";
            File txtFile = new File(sourceFolder, txtFileName);
            File targetTxtFile = new File(targetFolder, txtFileName);

            if (txtFile.exists()) {
                if (txtFile.renameTo(targetTxtFile)) {
                    Log.d(TAG, ".txt file moved successfully to: " + targetTxtFile.getAbsolutePath());
                } else {
                    Log.e(TAG, "Failed to move .txt file: " + txtFile.getAbsolutePath() + " to " + targetTxtFile.getAbsolutePath());
                    return false;
                }
            } else {
                Log.w(TAG, ".txt file not found in source folder: " + txtFile.getAbsolutePath());
            }
        } else {
            Log.e(TAG, "Source folder not found or not a directory: " + sourceFolder.getAbsolutePath());
            return false;
        }
        return true;
    }

    private List<File> getImagesFromFolder(String folderPath) {
        List<File> images = new ArrayList<>();
        File folder = new File(folderPath);

        File[] files = folder.listFiles((dir, name) -> name.toLowerCase().endsWith(".jpg"));
        if (files != null) {
            for (File file : files) {
                images.add(file);
            }
        }
        return images;
    }
    private String extractFolderName(String fileName) {
        int underscoreIndex = fileName.indexOf('_');
        return underscoreIndex > 0 ? fileName.substring(0, underscoreIndex) : "";
    }
}
