package com.example.photobox.view;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.widget.GridView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.photobox.R;
import com.example.photobox.service.ImageAdapter;

public class TrashActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_trash);
        @SuppressLint({"MissingInflatedId", "LocalSuppress"})
        GridView gridView = (GridView)findViewById(R.id.gridView);
        ImageAdapter adapter = new ImageAdapter(this);
        gridView.setAdapter(adapter);
        gridView.setOnItemClickListener((parent, view, position, id) -> {
            if (!ImageAdapter.isLongPress) {
                Intent intent = new Intent(getApplicationContext(), FullImageActivity.class);
                intent.putExtra("id", position);
                startActivity(intent);
            }
            ImageAdapter.isLongPress = false;
        });

        gridView.setOnItemLongClickListener((parent, view, position, id) -> {
            adapter.showPopupMenu(view, position);
            ImageAdapter.isLongPress = true;
            return true;
        });

    }
}
