//package com.example.photobox.service;
//
//import android.annotation.SuppressLint;
//import android.content.Context;
//import android.graphics.Bitmap;
//import android.graphics.BitmapFactory;
//import android.graphics.Color;
//import android.util.Pair;
//import android.view.Gravity;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.BaseAdapter;
//import android.widget.GridView;
//import android.widget.ImageView;
//import android.widget.LinearLayout;
//import android.widget.PopupMenu;
//import android.widget.TextView;
//
//import com.example.photobox.R;
//import com.example.photobox.utils.SMBUtils;
//
//import java.io.File;
//import java.util.ArrayList;
//import java.util.Arrays;
//import java.util.List;
//
//
//public class ImageAdapter extends BaseAdapter {
//    public static boolean isLongPress = false;
//
//    private Context context;
//    public List<Pair<String, File>> images;
//    private List<Pair<String, File>> filteredImages;
//    public ImageAdapter(Context context){
//        this.context = context;
//        this.images = getJpgFilesFromErledigt();
//        this.filteredImages = new ArrayList<>(images);
//    }
//
//    @Override
//    public int getCount() {
//        return images.size();
//    }
//
//    @Override
//    public Object getItem(int position) {
//        return images.get(position);
//    }
//
//    @Override
//    public long getItemId(int position) {
//        return 0;
//    }
//
//    @Override
//    public View getView(int position, View convertView, ViewGroup parent) {
//            if (!images.isEmpty()) {
//                Pair<String, File> filesWithNumbers = filteredImages.get(position);
//                File firstImageFile = filesWithNumbers.second;
//                String photoName = filesWithNumbers.first;
//                TextView textView = createTextView(photoName);
//
//                Bitmap bitmap = BitmapFactory.decodeFile(firstImageFile.getAbsolutePath());
//                ImageView imageView = createImageView(bitmap);
//
//                LinearLayout linearLayout = createLinearLayout();
//                linearLayout.addView(textView);
//                linearLayout.addView(imageView);
//                return linearLayout;
//            }
//        return null;
//    }
//
//    private LinearLayout createLinearLayout() {
//        LinearLayout linearLayout = new LinearLayout(context);
//        linearLayout.setOrientation(LinearLayout.VERTICAL);
//        linearLayout.setPadding(8, 8, 8, 8);
//        return linearLayout;
//    }
//
//    private TextView createTextView(String photoName) {
//        TextView textView = new TextView(context);
//        textView.setTextSize(16);
//        textView.setTextColor(Color.BLACK);
//        textView.setGravity(Gravity.CENTER);
//        textView.setText(photoName);
//        return textView;
//    }
//
//    private ImageView createImageView(Bitmap bitmap) {
//        ImageView imageView = new ImageView(context);
///*
//        if (bitmap!= null) {
//*/
//        imageView.setImageBitmap(bitmap);
////        }
//        /*else {
//            imageView.setImageResource(R.drawable.placeholder);
//        }*/
//        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
//        imageView.setLayoutParams(new GridView.LayoutParams(350, 350));
//        imageView.setPadding(8, 8, 8, 8);
//        return imageView;
//    }
//
//    @SuppressLint("RestrictedApi")
//    public void showPopupMenu(View view, int position){
//        PopupMenu popupMenu = new PopupMenu(context, view);
//        popupMenu.getMenuInflater().inflate(R.menu.image_context_menu, popupMenu.getMenu());
//
//
//        popupMenu.show();
//    }
//    private List<Pair<String, File>> getJpgFilesFromErledigt() {
//        File erledigtDir = new File(SMBUtils.ERLEDIGT_DIR);
//        List<Pair<String, File>> result = new ArrayList<>();
//
//        if (!erledigtDir.exists() || !erledigtDir.isDirectory()) {
//            return new ArrayList<>();
//        }
//
//        File[] sampleDirs = erledigtDir.listFiles(File::isDirectory);
//        if (sampleDirs != null) {
//            for (File sampleDir : sampleDirs) {
////                String sampleNumber = sampleDir.getName();
//                File[] jpgFiles = sampleDir.listFiles((dir, name) -> name.toLowerCase().endsWith(".jpg"));
//                if (jpgFiles != null) {
//                    for (File jpgFile : jpgFiles) {
//                        String fileName = jpgFile.getName();
//                        result.add(new Pair<>(fileName, jpgFile));
//                    }
//                }
//            }
//        }
//        return result;
//    }
//    public void filter(String query){
//      if (!query.isEmpty()){
//          for (Pair<String, File> pair : images) {
//              if (pair.first.contains(query)) {
//                  filteredImages.add(pair);
//              }
//          }
//          notifyDataSetChanged();
//      }
//    }
//
//}
