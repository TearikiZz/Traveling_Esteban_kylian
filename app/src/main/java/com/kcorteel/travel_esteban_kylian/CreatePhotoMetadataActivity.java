package com.kcorteel.travel_esteban_kylian;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

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
    private Spinner mediaSpinner;

    private TravelShareRepository travelShareRepository;

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

        bindViews();
        setupSpinners();

        Button publishButton = findViewById(R.id.btnPublishPhotoMetadata);
        publishButton.setOnClickListener(v -> publishPhotoMetadata());
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
        mediaSpinner = findViewById(R.id.spinnerCreateMedia);
    }

    private void setupSpinners() {
        ArrayAdapter<String> placeTypeAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                getResources().getStringArray(R.array.travelshare_place_type_labels)
        );
        placeTypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        placeTypeSpinner.setAdapter(placeTypeAdapter);

        ArrayAdapter<String> mediaAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                getResources().getStringArray(R.array.travelshare_media_labels)
        );
        mediaAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mediaSpinner.setAdapter(mediaAdapter);
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
        String imageDrawableName = getSelectedMediaDrawableName(mediaSpinner.getSelectedItemPosition());

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
                imageDrawableName
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

    private String getSelectedMediaDrawableName(int selectedPosition) {
        switch (selectedPosition) {
            case 1:
                return "img_mock_kyoto";
            case 2:
                return "img_mock_rome";
            case 3:
                return "img_mock_barcelona";
            case 0:
            default:
                return "img_mock_paris";
        }
    }
}
