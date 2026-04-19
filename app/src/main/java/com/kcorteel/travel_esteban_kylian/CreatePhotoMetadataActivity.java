package com.kcorteel.travel_esteban_kylian;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import com.kcorteel.travel_esteban_kylian.travelshare.model.PlaceType;
import com.kcorteel.travel_esteban_kylian.travelshare.repository.TravelShareRepository;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CreatePhotoMetadataActivity extends AppCompatActivity {

    private EditText titleEditText;
    private EditText descriptionEditText;
    private EditText addressEditText;
    private EditText cityEditText;
    private EditText countryEditText;
    private EditText latitudeEditText;
    private EditText longitudeEditText;
    private EditText tagsEditText;
    private Spinner placeTypeSpinner;
    private ImageView selectedImagePreview;

    private TravelShareRepository travelShareRepository;
    private Uri selectedImageUri;
    private ActivityResultLauncher<String[]> openDocumentLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_photo_metadata);

        travelShareRepository = TravelShareRepository.getInstance(this);

        if (travelShareRepository.isCurrentUserAnonymous()) {
            Toast.makeText(this, R.string.travelshare_create_requires_login, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        setupImagePicker();
        bindViews();
        setupPlaceTypeSpinner();

        Button selectImageButton = findViewById(R.id.btnSelectPhoto);
        selectImageButton.setOnClickListener(v -> openDocumentLauncher.launch(new String[]{"image/*"}));

        Button publishButton = findViewById(R.id.btnPublishPhotoMetadata);
        publishButton.setOnClickListener(v -> publishPhotoMetadata());
    }

    private void setupImagePicker() {
        openDocumentLauncher = registerForActivityResult(
                new ActivityResultContracts.OpenDocument(),
                uri -> {
                    if (uri == null) {
                        return;
                    }

                    selectedImageUri = uri;
                    final int flags = Intent.FLAG_GRANT_READ_URI_PERMISSION;
                    getContentResolver().takePersistableUriPermission(uri, flags);
                    if (selectedImagePreview != null) {
                        selectedImagePreview.setImageURI(uri);
                    }
                }
        );
    }

    private void bindViews() {
        titleEditText = findViewById(R.id.etCreateTitle);
        descriptionEditText = findViewById(R.id.etCreateDescription);
        addressEditText = findViewById(R.id.etCreateAddress);
        cityEditText = findViewById(R.id.etCreateCity);
        countryEditText = findViewById(R.id.etCreateCountry);
        latitudeEditText = findViewById(R.id.etCreateLatitude);
        longitudeEditText = findViewById(R.id.etCreateLongitude);
        tagsEditText = findViewById(R.id.etCreateTags);
        placeTypeSpinner = findViewById(R.id.spinnerCreatePlaceType);
        selectedImagePreview = findViewById(R.id.ivSelectedPhotoPreview);
    }

    private void setupPlaceTypeSpinner() {
        android.widget.ArrayAdapter<String> placeTypeAdapter = new android.widget.ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                getResources().getStringArray(R.array.travelshare_place_type_labels)
        );
        placeTypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        placeTypeSpinner.setAdapter(placeTypeAdapter);
    }

    private void publishPhotoMetadata() {
        String title = titleEditText.getText().toString().trim();
        String description = descriptionEditText.getText().toString().trim();
        String address = addressEditText.getText().toString().trim();
        String city = cityEditText.getText().toString().trim();
        String country = countryEditText.getText().toString().trim();
        String latitudeValue = latitudeEditText.getText().toString().trim();
        String longitudeValue = longitudeEditText.getText().toString().trim();
        String tagsRaw = tagsEditText.getText().toString().trim();

        if (title.isEmpty() || description.isEmpty() || address.isEmpty() || city.isEmpty()
                || country.isEmpty() || latitudeValue.isEmpty() || longitudeValue.isEmpty()) {
            Toast.makeText(this, R.string.travelshare_create_missing_fields, Toast.LENGTH_SHORT).show();
            return;
        }

        if (selectedImageUri == null) {
            Toast.makeText(this, R.string.travelshare_create_missing_photo, Toast.LENGTH_SHORT).show();
            return;
        }

        double latitude;
        double longitude;
        try {
            latitude = Double.parseDouble(latitudeValue);
            longitude = Double.parseDouble(longitudeValue);
        } catch (NumberFormatException exception) {
            Toast.makeText(this, R.string.travelshare_create_invalid_coordinates, Toast.LENGTH_SHORT).show();
            return;
        }

        List<String> tags = parseTags(tagsRaw);
        PlaceType placeType = PlaceType.values()[placeTypeSpinner.getSelectedItemPosition()];

        if (travelShareRepository.createPhotoMetadata(
                title,
                description,
                address,
                city,
                country,
                latitude,
                longitude,
                tags,
                placeType,
                selectedImageUri.toString()
        ) == null) {
            Toast.makeText(this, R.string.travelshare_create_requires_login, Toast.LENGTH_SHORT).show();
            return;
        }

        Toast.makeText(this, R.string.travelshare_create_success, Toast.LENGTH_SHORT).show();
        finish();
    }

    private List<String> parseTags(String tagsRaw) {
        if (tagsRaw.isEmpty()) {
            return new ArrayList<>();
        }

        List<String> tags = new ArrayList<>();
        for (String part : Arrays.asList(tagsRaw.split(","))) {
            String tag = part.trim();
            if (!tag.isEmpty()) {
                tags.add(tag);
            }
        }
        return tags;
    }
}
