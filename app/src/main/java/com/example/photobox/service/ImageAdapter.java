package com.example.photobox.service;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.photobox.R;

import java.io.File;
import java.util.List;

public class ImageAdapter extends BaseAdapter{
    private Context context;
    private List<File> imageFiles;
    public ImageAdapter(Context context, List<File> imageFiles) {
        this.context = context;
        this.imageFiles = imageFiles;
    }

    @Override
    public int getCount() {
        return imageFiles.size();
    }

    @Override
    public Object getItem(int position) {
        return imageFiles.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.images_activity, parent, false);

            holder = new ViewHolder();
            holder.imageView = convertView.findViewById(R.id.imageView);
            holder.textView = convertView.findViewById(R.id.imageTitle);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.imageView.setImageBitmap(null);
        holder.textView.setText("");

        if (position < imageFiles.size()) {
            File imageFile = imageFiles.get(position);
            Bitmap bitmap = BitmapFactory.decodeFile(imageFile.getAbsolutePath());
            holder.imageView.setImageBitmap(bitmap);
            holder.textView.setText(imageFile.getName());
        }

        return convertView;
    }

    private static class ViewHolder {
        ImageView imageView;
        TextView textView;
    }
}
