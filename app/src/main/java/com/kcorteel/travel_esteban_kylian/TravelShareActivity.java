package com.kcorteel.travel_esteban_kylian;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.kcorteel.travel_esteban_kylian.travelshare.adapter.PhotoMetadataAdapter;
import com.kcorteel.travel_esteban_kylian.travelshare.model.PhotoMetadata;
import com.kcorteel.travel_esteban_kylian.travelshare.model.PlaceType;
import com.kcorteel.travel_esteban_kylian.travelshare.repository.TravelShareRepository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class TravelShareActivity extends AppCompatActivity {

    private EditText searchEditText;
    private RecyclerView photoMetadataRecyclerView;
    private PhotoMetadataAdapter photoMetadataAdapter;
    private TravelShareRepository travelShareRepository;
    private TextView subtitleTextView;
    private Button createPhotoMetadataButton;
    private Button resetFiltersButton;
    private ImageView profileShortcutImageView;
    private Spinner placeTypeSpinner;
    private Spinner authorSpinner;
    private Spinner periodSpinner;
    private final List<String> authorOptions = new ArrayList<>();
    private PlaceType selectedPlaceType;
    private String selectedAuthor = "";
    private PhotoMetadataAdapter.PeriodFilter selectedPeriod = PhotoMetadataAdapter.PeriodFilter.ALL;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        travelShareRepository = TravelShareRepository.getInstance(this);
        travelShareRepository.applyCurrentUserThemePreference();
        setContentView(R.layout.activity_travel_share);

        searchEditText = findViewById(R.id.etSearchPhotoMetadata);
        photoMetadataRecyclerView = findViewById(R.id.rvPhotoMetadata);
        subtitleTextView = findViewById(R.id.tvTravelShareSubtitle);
        createPhotoMetadataButton = findViewById(R.id.btnCreatePhotoMetadata);
        resetFiltersButton = findViewById(R.id.btnResetFilters);
        profileShortcutImageView = findViewById(R.id.ivProfileShortcut);
        placeTypeSpinner = findViewById(R.id.spinnerFilterPlaceType);
        authorSpinner = findViewById(R.id.spinnerFilterAuthor);
        periodSpinner = findViewById(R.id.spinnerFilterPeriod);

        photoMetadataAdapter = new PhotoMetadataAdapter(
                travelShareRepository,
                travelShareRepository.getPhotoMetadataList(),
                this::openPhotoMetadataDetails
        );

        photoMetadataRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        photoMetadataRecyclerView.setHasFixedSize(true);
        photoMetadataRecyclerView.setAdapter(photoMetadataAdapter);

        setupFilterControls();

        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // No-op
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                photoMetadataAdapter.filter(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
                // No-op
            }
        });

        createPhotoMetadataButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, CreatePhotoMetadataActivity.class);
            startActivity(intent);
        });

        resetFiltersButton.setOnClickListener(v -> resetAllFilters());

        profileShortcutImageView.setOnClickListener(v ->
                startActivity(new Intent(this, ProfileActivity.class))
        );

        updateSubtitle();
    }

    @Override
    protected void onResume() {
        super.onResume();
        photoMetadataAdapter.submitPhotoMetadataList(travelShareRepository.getPhotoMetadataList());
        refreshAuthorOptions();
        applySelectedFilters();
        updateSubtitle();
    }

    private void updateSubtitle() {
        if (travelShareRepository.isCurrentUserAnonymous()) {
            subtitleTextView.setText(R.string.travelshare_screen_subtitle_anonymous);
            createPhotoMetadataButton.setVisibility(View.GONE);
            profileShortcutImageView.setVisibility(View.GONE);
            return;
        }

        subtitleTextView.setText(getString(
                R.string.travelshare_screen_subtitle_connected,
                travelShareRepository.getCurrentUser().getUsername()
        ));
        createPhotoMetadataButton.setVisibility(View.VISIBLE);
        profileShortcutImageView.setVisibility(View.VISIBLE);
        travelShareRepository.loadUserAvatarIntoImageView(
                profileShortcutImageView,
                travelShareRepository.getCurrentUser()
        );
    }

    private void openPhotoMetadataDetails(PhotoMetadata photoMetadata) {
        Intent intent = new Intent(this, TravelShareDetailActivity.class);
        intent.putExtra(TravelShareDetailActivity.EXTRA_PHOTO_ID, photoMetadata.getPhotoId());
        startActivity(intent);
    }

    private void setupFilterControls() {
        setupPlaceTypeSpinner();
        setupAuthorSpinner();
        setupPeriodSpinner();
        applySelectedFilters();
    }

    private void setupPlaceTypeSpinner() {
        List<String> placeTypeOptions = new ArrayList<>();
        placeTypeOptions.add(getString(R.string.travelshare_filter_all_types));
        Collections.addAll(placeTypeOptions, getResources().getStringArray(R.array.travelshare_place_type_labels));

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                placeTypeOptions
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        placeTypeSpinner.setAdapter(adapter);
        placeTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedPlaceType = position == 0 ? null : PlaceType.values()[position - 1];
                applySelectedFilters();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // No-op
            }
        });
    }

    private void setupAuthorSpinner() {
        refreshAuthorOptions();
        authorSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedAuthor = position == 0 ? "" : authorOptions.get(position);
                applySelectedFilters();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // No-op
            }
        });
    }

    private void setupPeriodSpinner() {
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this,
                R.array.travelshare_period_filter_labels,
                android.R.layout.simple_spinner_item
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        periodSpinner.setAdapter(adapter);
        periodSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 1:
                        selectedPeriod = PhotoMetadataAdapter.PeriodFilter.LAST_30_DAYS;
                        break;
                    case 2:
                        selectedPeriod = PhotoMetadataAdapter.PeriodFilter.LAST_6_MONTHS;
                        break;
                    case 3:
                        selectedPeriod = PhotoMetadataAdapter.PeriodFilter.OLDER;
                        break;
                    case 0:
                    default:
                        selectedPeriod = PhotoMetadataAdapter.PeriodFilter.ALL;
                        break;
                }
                applySelectedFilters();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // No-op
            }
        });
    }

    private void refreshAuthorOptions() {
        String currentSelection = selectedAuthor;
        authorOptions.clear();
        authorOptions.add(getString(R.string.travelshare_filter_all_authors));

        List<String> authorNames = new ArrayList<>();
        for (PhotoMetadata photoMetadata : travelShareRepository.getPhotoMetadataList()) {
            String authorLabel = travelShareRepository.getAuthorLabel(photoMetadata);
            if (authorLabel != null && !authorLabel.trim().isEmpty()
                    && !containsIgnoreCase(authorNames, authorLabel)) {
                authorNames.add(authorLabel.trim());
            }
        }

        Collections.sort(authorNames, String.CASE_INSENSITIVE_ORDER);
        authorOptions.addAll(authorNames);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                authorOptions
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        authorSpinner.setAdapter(adapter);

        int selectedIndex = 0;
        if (!currentSelection.isEmpty()) {
            for (int i = 1; i < authorOptions.size(); i++) {
                if (authorOptions.get(i).equalsIgnoreCase(currentSelection)) {
                    selectedIndex = i;
                    break;
                }
            }
        }
        authorSpinner.setSelection(selectedIndex, false);
        selectedAuthor = selectedIndex == 0 ? "" : authorOptions.get(selectedIndex);
    }

    private void applySelectedFilters() {
        photoMetadataAdapter.setSelectedPlaceType(selectedPlaceType);
        photoMetadataAdapter.setSelectedAuthor(selectedAuthor);
        photoMetadataAdapter.setSelectedPeriod(selectedPeriod);
        photoMetadataAdapter.filter(searchEditText.getText().toString());
    }

    private void resetAllFilters() {
        selectedPlaceType = null;
        selectedAuthor = "";
        selectedPeriod = PhotoMetadataAdapter.PeriodFilter.ALL;

        searchEditText.setText("");
        placeTypeSpinner.setSelection(0, false);
        authorSpinner.setSelection(0, false);
        periodSpinner.setSelection(0, false);

        photoMetadataAdapter.resetFilters();
    }

    private boolean containsIgnoreCase(List<String> values, String candidate) {
        String normalizedCandidate = candidate.toLowerCase(Locale.getDefault());
        for (String value : values) {
            if (value.toLowerCase(Locale.getDefault()).equals(normalizedCandidate)) {
                return true;
            }
        }
        return false;
    }
}
