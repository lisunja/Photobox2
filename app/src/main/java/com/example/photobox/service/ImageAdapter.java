package com.example.photobox.service;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.PopupMenu;

import com.example.photobox.R;
import com.example.photobox.utils.SMBUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class ImageAdapter extends BaseAdapter {
    public static boolean isLongPress = false;

    private Context context;
    public List<File> images;

    @Override
    public int getCount() {
        return images.size();
    }

    @Override
    public Object getItem(int position) {
        return images.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ImageView imageView = new ImageView(context);
            if (!images.isEmpty()) {
                File firstImageFile = images.get(position);
                Bitmap bitmap = BitmapFactory.decodeFile(firstImageFile.getAbsolutePath());
                imageView.setImageBitmap(bitmap);
                imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                imageView.setLayoutParams(new GridView.LayoutParams(350, 350));
                imageView.setPadding(8, 8, 8, 8);
                return imageView;
            }
        return null;
    }

    @SuppressLint("RestrictedApi")
    public void showPopupMenu(View view, int position){
        PopupMenu popupMenu = new PopupMenu(context, view);
        popupMenu.getMenuInflater().inflate(R.menu.image_context_menu, popupMenu.getMenu());


        popupMenu.show();
    }
    private List<File> getJpgFilesFromErledigt() {
        File erledigtDir = new File(SMBUtils.ERLEDIGT_DIR);
        if (!erledigtDir.exists() || !erledigtDir.isDirectory()) {
            return new ArrayList<>();
        }

        File[] jpgFiles = erledigtDir.listFiles((dir, name) -> name.toLowerCase().endsWith(".jpg"));
        if (jpgFiles != null) {
            return Arrays.asList(jpgFiles);
        } else {
            return new ArrayList<>();
        }
    }
    public ImageAdapter(Context context){
        this.context = context;
        this.images = getJpgFilesFromErledigt();
    }
}
