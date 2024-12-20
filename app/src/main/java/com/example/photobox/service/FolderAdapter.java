package com.example.photobox.service;

import static androidx.core.content.ContextCompat.startActivity;

import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.photobox.R;
import com.example.photobox.view.ImageListActivity;
import com.example.photobox.view.TrashActivity;

import java.io.File;
import java.util.List;


public class FolderAdapter extends ArrayAdapter<String> {
    private Context context;
    private List<String> folderPaths;

    public FolderAdapter(Context context, List<String> folderPaths) {
        super(context, android.R.layout.simple_list_item_1, folderPaths);
        this.context = context;
        this.folderPaths = folderPaths;
    }

    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = View.inflate(context, R.layout.list_item_folder, null);
        }
        TextView textView = convertView.findViewById(R.id.folderName);
        ImageView folderIcon = convertView.findViewById(R.id.folderIcon);

        String folderName = new File(folderPaths.get(position)).getName();

        textView.setText(folderName);
        folderIcon.setImageResource(R.drawable.ic_folder);


        return convertView;
    }
    public void updateFolders(List<String> newFolderPaths) {
        folderPaths.clear();
        folderPaths.addAll(newFolderPaths);
        notifyDataSetChanged();
    }
}
