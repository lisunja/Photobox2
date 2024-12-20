package com.example.photobox.view;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.SearchView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.photobox.R;
import com.example.photobox.service.FolderAdapter;
import com.example.photobox.service.ImageAdapter;
import com.example.photobox.utils.SMBUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class TrashActivity extends AppCompatActivity {
    private ListView folderListView;
    private FolderAdapter folderAdapter;
    private SearchView searchView;
    private List<String> folderPaths = new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_trash);
        folderListView = findViewById(R.id.folderListView);
        searchView = findViewById(R.id.searchView);
        folderPaths = getFolders();
        folderAdapter = new FolderAdapter(this, folderPaths);
        folderListView.setAdapter(folderAdapter);

        folderListView.setOnItemClickListener((parent, view, position, id) -> {
            String folderPath = folderPaths.get(position);


            Intent intent = new Intent(TrashActivity.this, ImageListActivity.class);
            intent.putExtra("folderPath", folderPath);
            startActivity(intent);
        });
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                filterFolders(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterFolders(newText);
                return true;
            }
        });

    }
    private void filterFolders(String query) {
        if (query == null || query.isEmpty()) {
//            folderPaths = getFolders();
            folderAdapter.updateFolders(folderPaths);
            return;
        }

        List<String> filteredFolders = new ArrayList<>();
        for (String folderPath : folderPaths) {
            File folder = new File(folderPath);
            String folderName = folder.getName();
            if (folderName.contains(query)) {
                filteredFolders.add(folderPath);
            }
        }

        folderAdapter.updateFolders(filteredFolders);
    }


    private List<String> getFolders(){
        List<String> folders = new ArrayList<>();
        File directory = new File(SMBUtils.ERLEDIGT_DIR);
        File[] files = directory.listFiles(File::isDirectory);
        if (files != null) {
            for (File file : files) {
                folders.add(file.getAbsolutePath());
            }
        }
        return folders;
    }
//    private List<File> getImagesFromFolder(String folderPath) {
//        List<File> images = new ArrayList<>();
//        File folder = new File(folderPath);
//
//        File[] files = folder.listFiles((dir, name) -> name.toLowerCase().endsWith(".jpg"));
//        if (files != null) {
//            for (File file : files) {
//                images.add(file);
//            }
//        }
//        return images;
//    }
}
//        @SuppressLint({"MissingInflatedId", "LocalSuppress"})
//        GridView gridView = (GridView)findViewById(R.id.gridView);

//        ImageAdapter adapter = new ImageAdapter(this);
//        gridView.setAdapter(adapter);
//
//        gridView.setOnItemClickListener((parent, view, position, id) -> {
//            if (!ImageAdapter.isLongPress) {
//                Intent intent = new Intent(getApplicationContext(), FullImageActivity.class);
//                intent.putExtra("id", position);
//                startActivity(intent);
//            }
//            ImageAdapter.isLongPress = false;
//        });
//
//        gridView.setOnItemLongClickListener((parent, view, position, id) -> {
//            adapter.showPopupMenu(view, position);
//            ImageAdapter.isLongPress = true;
//            return true;
//        });
//        SearchView searchView = findViewById(R.id.searchView);
//        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
//            @Override
//            public boolean onQueryTextSubmit(String query) {
//                adapter.filter(query);
//                return true;
//            }
//
//            @Override
//            public boolean onQueryTextChange(String newText) {
//                return false;
//            }
//        });