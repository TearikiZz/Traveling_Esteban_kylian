package com.kcorteel.travel_esteban_kylian;

import android.Manifest;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.kcorteel.travel_esteban_kylian.travelshare.model.Location;
import com.kcorteel.travel_esteban_kylian.travelshare.model.PhotoMetadata;
import com.kcorteel.travel_esteban_kylian.travelshare.repository.TravelShareRepository;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TravelPathSummaryActivity extends AppCompatActivity {

    private static final int STORAGE_PERMISSION_REQUEST_CODE = 2001;

    private TextView summaryTextView;
    private LinearLayout pathCardsContainer;
    private Button saveTravelPathButton;
    private List<PhotoMetadata> currentSelectedPath;
    private TravelShareRepository repository;
    private String summary;
    private String pendingSaveBaseName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_travelpath_summary);

        // Initialisation des vues
        summaryTextView = findViewById(R.id.summaryTextView);
        pathCardsContainer = findViewById(R.id.pathCardsContainer);
        saveTravelPathButton = findViewById(R.id.saveTravelPathButton);

        String loadedSummaryText = getIntent().getStringExtra("loaded_summary_text");
        String loadedSourceName = getIntent().getStringExtra("loaded_source_name");
        if (loadedSummaryText != null && !loadedSummaryText.trim().isEmpty()) {
            repository = TravelShareRepository.getInstance(this);
            currentSelectedPath = new ArrayList<>();
            summary = loadedSummaryText;
            summaryTextView.setText(loadedSourceName == null
                    ? loadedSummaryText
                    : ("Fichier chargé : " + loadedSourceName + "\n\n" + loadedSummaryText));
            pathCardsContainer.setVisibility(View.GONE);
            saveTravelPathButton.setVisibility(View.GONE);
            return;
        }

        // Récupération des données des écrans précédents
        Bundle extras = getIntent().getExtras();
        String budget = extras != null ? extras.getString("budget") : "";
        String duration = extras != null ? extras.getString("duration") : "";
        boolean culture = extras != null && extras.getBoolean("culture");
        boolean leisure = extras != null && extras.getBoolean("leisure");
        boolean food = extras != null && extras.getBoolean("food");
        String effort = extras != null ? extras.getString("effort") : "";
        String pathType = extras != null ? extras.getString("pathType") : "";

        repository = TravelShareRepository.getInstance(this);
        currentSelectedPath = repository.getGreedyTravelPath(
                budget, duration, culture, leisure, food, effort, pathType
        );

        double totalPrice = repository.computeTotalActivitiesPrice(currentSelectedPath);
        double totalDurationHours = repository.computeTotalActivitiesDurationHours(currentSelectedPath);
        double totalTravelHours = repository.computeTotalTravelDurationHours(currentSelectedPath);

        StringBuilder travelLegsBuilder = new StringBuilder();
        if (currentSelectedPath.size() < 2) {
            travelLegsBuilder.append("Temps de trajet entre POI : aucun trajet (1 seul POI)");
        } else {
            travelLegsBuilder.append("Temps de trajet entre POI :").append('\n');
            for (int i = 0; i < currentSelectedPath.size() - 1; i++) {
                PhotoMetadata fromPoi = currentSelectedPath.get(i);
                PhotoMetadata toPoi = currentSelectedPath.get(i + 1);
                double legHours = repository.estimateTravelDurationHoursBetween(fromPoi, toPoi);
                travelLegsBuilder.append("- ")
                        .append(fromPoi.getTitle())
                        .append(" -> ")
                        .append(toPoi.getTitle())
                        .append(" : ")
                        .append(formatDurationHoursToMinutes(legHours))
                        .append('\n');
            }
        }

        StringBuilder hoursBuilder = new StringBuilder();
        if (!currentSelectedPath.isEmpty()) {
            hoursBuilder.append("Horaires des POI :").append('\n');
            for (PhotoMetadata poi : currentSelectedPath) {
                hoursBuilder.append("- ")
                        .append(poi.getTitle())
                        .append(" : ")
                        .append(poi.getOpeningTime())
                        .append(" - ")
                        .append(poi.getClosingTime())
                        .append('\n');
            }
        }

        summary = String.format(
                Locale.getDefault(),
                "Parcours sélectionné : %d activité(s)\nPrix total : %.2f €\nTemps total des activités : %.2f h\nTemps total de trajet : %.2f h\n\n%s\n\n%s",
                currentSelectedPath.size(),
                totalPrice,
                totalDurationHours,
                totalTravelHours,
                travelLegsBuilder.toString().trim(),
                hoursBuilder.toString().trim()
        );
        summaryTextView.setText(summary);

        renderPathCards(repository, currentSelectedPath);

        saveTravelPathButton.setOnClickListener(v -> exportTravelPathToTextFile());
    }

    private void renderPathCards(TravelShareRepository repository, List<PhotoMetadata> pois) {
        pathCardsContainer.removeAllViews();
        if (pois == null || pois.isEmpty()) {
            pathCardsContainer.setVisibility(View.GONE);
            return;
        }
        pathCardsContainer.setVisibility(View.VISIBLE);

        LayoutInflater inflater = LayoutInflater.from(this);

        for (int i = 0; i < pois.size(); i++) {
            PhotoMetadata poi = pois.get(i);

            View poiCard = inflater.inflate(R.layout.item_travelpath_poi_card, pathCardsContainer, false);
            TextView poiTitle = poiCard.findViewById(R.id.poiTitle);
            TextView poiHours = poiCard.findViewById(R.id.poiHours);
            ImageView poiImage = poiCard.findViewById(R.id.poiImage);
            ImageButton poiMapButton = poiCard.findViewById(R.id.poiMapButton);

            Location location = repository.getLocationById(poi.getLocationId());
            String locationLabel = location == null ? "" : " - " + location.getCity();
            poiTitle.setText("POI " + (i + 1) + " : " + poi.getTitle() + locationLabel);
            poiHours.setText(String.format(
                    Locale.getDefault(),
                    "Horaires : %s - %s    |    Prix : %.2f €",
                    poi.getOpeningTime(),
                    poi.getClosingTime(),
                    poi.getPrice()
            ));
            repository.loadMediaIntoImageView(this, poiImage, poi);

            if (location != null) {
                poiMapButton.setVisibility(View.VISIBLE);
                poiMapButton.setOnClickListener(v -> openDefaultMapApp(location));
            } else {
                poiMapButton.setVisibility(View.GONE);
            }

            pathCardsContainer.addView(poiCard);

            if (i < pois.size() - 1) {
                PhotoMetadata nextPoi = pois.get(i + 1);
                double legHours = repository.estimateTravelDurationHoursBetween(poi, nextPoi);

                View travelCard = inflater.inflate(R.layout.item_travelpath_travel_card, pathCardsContainer, false);
                TextView travelLegText = travelCard.findViewById(R.id.travelLegText);
                travelLegText.setText("Trajet POI " + (i + 1) + " -> POI " + (i + 2) + " : "
                        + formatDurationHoursToMinutes(legHours));

                pathCardsContainer.addView(travelCard);
            }
        }
    }

    private void openDefaultMapApp(Location location) {
        if (location == null) {
            Toast.makeText(this, R.string.travelshare_no_map_app, Toast.LENGTH_SHORT).show();
            return;
        }

        String query = location.getLatitude() + "," + location.getLongitude();
        Uri geoUri = Uri.parse("geo:0,0?q=" + query);
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, geoUri);

        if (mapIntent.resolveActivity(getPackageManager()) != null) {
            startActivity(mapIntent);
        } else {
            Toast.makeText(this, R.string.travelshare_no_map_app, Toast.LENGTH_SHORT).show();
        }
    }

    private String formatDurationHoursToMinutes(double hours) {
        int minutes = (int) Math.round(hours * 60d);
        if (minutes < 60) {
            return minutes + " min";
        }
        int h = minutes / 60;
        int m = minutes % 60;
        if (m == 0) {
            return h + " h";
        }
        return h + " h " + m + " min";
    }

    private void exportTravelPathToTextFile() {
        showSaveNameDialog();
    }

    private void showSaveNameDialog() {
        EditText input = new EditText(this);
        input.setHint(R.string.travelpath_save_name_hint);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        input.setTextColor(0xFF000000);
        input.setBackgroundResource(R.drawable.bg_save_name_input);
        int horizontalPadding = (int) (14 * getResources().getDisplayMetrics().density);
        int verticalPadding = (int) (10 * getResources().getDisplayMetrics().density);
        input.setPadding(horizontalPadding, verticalPadding, horizontalPadding, verticalPadding);

        new AlertDialog.Builder(this)
                .setTitle(R.string.travelpath_save_name_title)
                .setView(input)
                .setPositiveButton(R.string.travelpath_save_name_confirm, (dialog, which) -> {
                    String rawName = input.getText() == null ? "" : input.getText().toString().trim();
                    if (rawName.isEmpty()) {
                        Toast.makeText(this, R.string.travelpath_save_name_required, Toast.LENGTH_LONG).show();
                        return;
                    }
                    exportTravelPathToTextFileWithName(rawName);
                })
                .setNegativeButton(R.string.travelpath_save_name_cancel, null)
                .show();
    }

    private void exportTravelPathToTextFileWithName(String rawName) {
        String safeBaseName = sanitizeFileName(rawName);
        if (safeBaseName.isEmpty()) {
            Toast.makeText(this, R.string.travelpath_save_name_required, Toast.LENGTH_LONG).show();
            return;
        }
        pendingSaveBaseName = safeBaseName;

        writeTravelPathFile(safeBaseName);
    }

    private String sanitizeFileName(String input) {
        return input.replaceAll("[\\\\/:*?\"<>|]", "_")
                .replaceAll("\\s+", "_")
                .replaceAll("_+", "_")
                .replaceAll("^_+|_+$", "");
    }

    private void writeTravelPathFile(String baseName) {
        String fileName = baseName + ".txt";

        StringBuilder content = new StringBuilder();
        content.append("Travel Path Export").append('\n');
        content.append("Date: ").append(new Date()).append('\n').append('\n');
        content.append(summary).append('\n').append('\n');
        content.append("POIs:").append('\n');

        for (int i = 0; i < currentSelectedPath.size(); i++) {
            PhotoMetadata poi = currentSelectedPath.get(i);
            Location location = repository.getLocationById(poi.getLocationId());

            content.append(i + 1).append(". ").append(poi.getTitle()).append('\n');
            content.append("   Activity: ").append(poi.getActivityType().name()).append('\n');
            content.append("   Price: ").append(String.format(Locale.getDefault(), "%.2f", poi.getPrice())).append(" €").append('\n');
            content.append("   Duration: ")
                    .append(String.format(Locale.getDefault(), "%.2f", repository.estimateActivityDurationHours(poi)))
                    .append(" h").append('\n');
            content.append("   Opening hours: ")
                    .append(poi.getOpeningTime())
                    .append(" - ")
                    .append(poi.getClosingTime())
                    .append('\n');

            if (location != null) {
                content.append("   Location: ")
                        .append(location.getAddress())
                        .append(", ")
                        .append(location.getCity())
                        .append(", ")
                        .append(location.getCountry())
                        .append('\n');
            }
            content.append('\n');
        }

        byte[] data = content.toString().getBytes(StandardCharsets.UTF_8);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            saveToDownloadsWithMediaStore(fileName, data);
            return;
        }

        saveToDownloadsLegacy(fileName, data);
    }

    private void saveToDownloadsWithMediaStore(String fileName, byte[] data) {
        ContentValues values = new ContentValues();
        values.put(MediaStore.Downloads.DISPLAY_NAME, fileName);
        values.put(MediaStore.Downloads.MIME_TYPE, "text/plain");
        values.put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS + "/travel_paths");

        Uri uri = getContentResolver().insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values);
        if (uri == null) {
            Toast.makeText(this, R.string.travelpath_save_error, Toast.LENGTH_LONG).show();
            return;
        }

        try (OutputStream outputStream = getContentResolver().openOutputStream(uri)) {
            if (outputStream == null) {
                Toast.makeText(this, R.string.travelpath_save_error, Toast.LENGTH_LONG).show();
                pendingSaveBaseName = null;
                return;
            }
            outputStream.write(data);
            Toast.makeText(this, getString(R.string.travelpath_save_success, "Downloads/travel_paths/" + fileName), Toast.LENGTH_LONG).show();
            pendingSaveBaseName = null;
        } catch (IOException e) {
            Toast.makeText(this, R.string.travelpath_save_error, Toast.LENGTH_LONG).show();
            pendingSaveBaseName = null;
        }
    }

    private void saveToDownloadsLegacy(String fileName, byte[] data) {
        File downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        File exportDir = new File(downloadsDir, "travel_paths");
        if (!exportDir.exists() && !exportDir.mkdirs()) {
            Toast.makeText(this, R.string.travelpath_save_error, Toast.LENGTH_LONG).show();
            return;
        }

        File file = new File(exportDir, fileName);
        try (FileOutputStream outputStream = new FileOutputStream(file)) {
            outputStream.write(data);
            Toast.makeText(this, getString(R.string.travelpath_save_success, file.getAbsolutePath()), Toast.LENGTH_LONG).show();
            pendingSaveBaseName = null;
        } catch (IOException e) {
            Toast.makeText(this, R.string.travelpath_save_error, Toast.LENGTH_LONG).show();
            pendingSaveBaseName = null;
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
            if (pendingSaveBaseName == null || pendingSaveBaseName.trim().isEmpty()) {
                String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
                pendingSaveBaseName = "travel_path_" + timestamp;
            }
            writeTravelPathFile(pendingSaveBaseName);
        } else {
            Toast.makeText(this, R.string.travelpath_storage_permission_denied, Toast.LENGTH_LONG).show();
        }
    }
}
