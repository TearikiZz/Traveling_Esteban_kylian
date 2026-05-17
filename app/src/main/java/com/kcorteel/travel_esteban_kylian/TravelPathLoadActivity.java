package com.kcorteel.travel_esteban_kylian;

import android.Manifest;
import android.content.ContentUris;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class TravelPathLoadActivity extends AppCompatActivity {

    private static final int STORAGE_PERMISSION_REQUEST_CODE = 3001;

    private ListView savedTravelListView;
    private TextView emptySavedTravelTextView;
    private final List<SavedTravelEntry> savedEntries = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_travelpath_load);

        savedTravelListView = findViewById(R.id.savedTravelListView);
        emptySavedTravelTextView = findViewById(R.id.emptySavedTravelTextView);

        savedTravelListView.setOnItemClickListener((parent, view, position, id) -> openSavedTravelSummary(position));

        if (needsReadStoragePermission()
                && ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    STORAGE_PERMISSION_REQUEST_CODE
            );
        } else {
            loadSavedTravels();
        }
    }

    private boolean needsReadStoragePermission() {
        return Build.VERSION.SDK_INT <= Build.VERSION_CODES.S_V2;
    }

    private void loadSavedTravels() {
        savedEntries.clear();
        loadFromMediaStoreDownloads();
        loadFromLegacyDownloadsFolder();
        refreshListUi();
    }

    private void loadFromMediaStoreDownloads() {
        String[] projection = new String[]{
                MediaStore.Downloads._ID,
                MediaStore.Downloads.DISPLAY_NAME,
                MediaStore.MediaColumns.RELATIVE_PATH
        };

        try (Cursor cursor = getContentResolver().query(
                MediaStore.Downloads.EXTERNAL_CONTENT_URI,
                projection,
                null,
                null,
                MediaStore.Downloads.DATE_ADDED + " DESC"
        )) {
            if (cursor == null) {
                return;
            }

            int idColumn = cursor.getColumnIndexOrThrow(MediaStore.Downloads._ID);
            int nameColumn = cursor.getColumnIndexOrThrow(MediaStore.Downloads.DISPLAY_NAME);
            int relativePathColumn = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.RELATIVE_PATH);

            while (cursor.moveToNext()) {
                long id = cursor.getLong(idColumn);
                String name = cursor.getString(nameColumn);
                String relativePath = cursor.getString(relativePathColumn);
                if (name == null || !name.endsWith(".txt")) {
                    continue;
                }
                if (relativePath == null || !relativePath.contains("travel_paths")) {
                    continue;
                }

                Uri uri = ContentUris.withAppendedId(MediaStore.Downloads.EXTERNAL_CONTENT_URI, id);
                savedEntries.add(new SavedTravelEntry(name, uri, null));
            }
        }
    }

    private void loadFromLegacyDownloadsFolder() {
        File downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        File exportDir = new File(downloadsDir, "travel_paths");
        if (!exportDir.exists() || !exportDir.isDirectory()) {
            return;
        }

        File[] files = exportDir.listFiles((dir, name) -> name != null && name.endsWith(".txt"));
        if (files == null) {
            return;
        }

        for (File file : files) {
            boolean alreadyListed = false;
            for (SavedTravelEntry entry : savedEntries) {
                if (entry.displayName.equals(file.getName())) {
                    alreadyListed = true;
                    break;
                }
            }
            if (!alreadyListed) {
                savedEntries.add(new SavedTravelEntry(file.getName(), null, file.getAbsolutePath()));
            }
        }
    }

    private void refreshListUi() {
        if (savedEntries.isEmpty()) {
            savedTravelListView.setVisibility(View.GONE);
            emptySavedTravelTextView.setVisibility(View.VISIBLE);
            return;
        }

        savedTravelListView.setVisibility(View.VISIBLE);
        emptySavedTravelTextView.setVisibility(View.GONE);

        List<String> fileNames = new ArrayList<>();
        for (SavedTravelEntry entry : savedEntries) {
            fileNames.add(entry.displayName);
        }
        savedTravelListView.setAdapter(new SavedTravelAdapter(
                this,
                fileNames,
                this::deleteFile
        ));
    }

    private void openSavedTravelSummary(int position) {
        if (position < 0 || position >= savedEntries.size()) {
            return;
        }

        SavedTravelEntry selected = savedEntries.get(position);
        String content = readSavedTravelContent(selected);
        if (content == null || content.trim().isEmpty()) {
            Toast.makeText(this, R.string.travelpath_load_error, Toast.LENGTH_LONG).show();
            return;
        }

        Intent intent = new Intent(this, TravelPathSummaryActivity.class);
        intent.putExtra("loaded_summary_text", content);
        intent.putExtra("loaded_source_name", selected.displayName);
        startActivity(intent);
    }

    private String readSavedTravelContent(SavedTravelEntry entry) {
        try {
            byte[] bytes;
            if (entry.contentUri != null) {
                try (InputStream inputStream = getContentResolver().openInputStream(entry.contentUri)) {
                    if (inputStream == null) {
                        return null;
                    }
                    bytes = readAllBytes(inputStream);
                }
            } else if (entry.absolutePath != null) {
                try (InputStream inputStream = Files.newInputStream(Paths.get(entry.absolutePath))) {
                    bytes = readAllBytes(inputStream);
                }
            } else {
                return null;
            }
            return new String(bytes, StandardCharsets.UTF_8);
        } catch (IOException e) {
            return null;
        }
    }

    private byte[] readAllBytes(InputStream inputStream) throws IOException {
        byte[] buffer = new byte[4096];
        int read;
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        while ((read = inputStream.read(buffer)) != -1) {
            outputStream.write(buffer, 0, read);
        }
        return outputStream.toByteArray();
    }

    private void deleteFile(int position) {
        if (position < 0 || position >= savedEntries.size()) {
            return;
        }

        SavedTravelEntry entry = savedEntries.get(position);
        boolean deleted = false;

        try {
            if (entry.contentUri != null) {
                deleted = getContentResolver().delete(entry.contentUri, null, null) > 0;
            } else if (entry.absolutePath != null) {
                File file = new File(entry.absolutePath);
                deleted = file.delete();
            }

            if (deleted) {
                savedEntries.remove(position);
                Toast.makeText(this, R.string.file_deleted_success, Toast.LENGTH_SHORT).show();
                refreshListUi();
            } else {
                Toast.makeText(this, R.string.file_delete_failed, Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Toast.makeText(this, R.string.file_delete_error, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode != STORAGE_PERMISSION_REQUEST_CODE) {
            return;
        }

        boolean granted = false;
        for (int result : grantResults) {
            if (result == PackageManager.PERMISSION_GRANTED) {
                granted = true;
                break;
            }
        }

        if (granted) {
            loadSavedTravels();
        } else {
            Toast.makeText(this, R.string.travelpath_storage_permission_denied, Toast.LENGTH_LONG).show();
            refreshListUi();
        }
    }

    private static class SavedTravelEntry {
        final String displayName;
        final Uri contentUri;
        final String absolutePath;

        SavedTravelEntry(String displayName, Uri contentUri, String absolutePath) {
            this.displayName = displayName;
            this.contentUri = contentUri;
            this.absolutePath = absolutePath;
        }
    }
}
