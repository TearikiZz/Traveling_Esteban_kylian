package com.kcorteel.travel_esteban_kylian;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.common.api.ApiException;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.AddressComponent;
import com.google.android.libraries.places.api.model.AddressComponents;
import com.google.android.libraries.places.api.model.AutocompletePrediction;
import com.google.android.libraries.places.api.model.AutocompleteSessionToken;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.FetchPlaceRequest;
import com.google.android.libraries.places.api.net.FetchPlaceResponse;
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest;
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsResponse;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.kcorteel.travel_esteban_kylian.travelshare.model.PlaceType;
import com.kcorteel.travel_esteban_kylian.travelshare.repository.TravelShareRepository;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class CreatePhotoMetadataActivity extends AppCompatActivity {

    private static final String TAG = "CreatePhotoMetadata";
    private static final long PLACE_SEARCH_DEBOUNCE_MS = 300L;

    private EditText titleEditText;
    private EditText descriptionEditText;
    private EditText tagsEditText;
    private AutoCompleteTextView placeSearchAutoCompleteTextView;
    private Spinner placeTypeSpinner;
    private ImageView selectedImagePreview;
    private TextView selectedPlaceLabelTextView;
    private TextView selectedPlaceDetailsTextView;

    private TravelShareRepository travelShareRepository;
    private Uri selectedImageUri;
    private ActivityResultLauncher<String[]> openDocumentLauncher;
    private PlacesClient placesClient;
    private ArrayAdapter<String> placeSuggestionsAdapter;
    private final List<AutocompletePrediction> currentPredictions = new ArrayList<>();
    private final Handler placeSearchHandler = new Handler(Looper.getMainLooper());
    private Runnable placeSearchRunnable;
    private AutocompleteSessionToken autocompleteSessionToken;
    private boolean updatingPlaceSearchText;
    private String selectedPlaceName = "";
    private String selectedPlaceAddress = "";
    private String selectedPlaceCity = "";
    private String selectedPlaceCountry = "";
    private double selectedPlaceLatitude;
    private double selectedPlaceLongitude;
    private boolean hasSelectedPlace;

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
        setupPlaceAutocomplete();
        bindViews();
        setupPlaceTypeSpinner();
        setupPlaceSearchField();

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

    private void setupPlaceAutocomplete() {
        if (TextUtils.isEmpty(BuildConfig.PLACES_API_KEY)) {
            Log.w(TAG, "Places API key is missing; autocomplete is disabled.");
            return;
        }

        if (!Places.isInitialized()) {
            Places.initializeWithNewPlacesApiEnabled(getApplicationContext(), BuildConfig.PLACES_API_KEY);
        }
        placesClient = Places.createClient(this);
        Log.d(TAG, "Places client initialized successfully.");
    }

    private void bindViews() {
        titleEditText = findViewById(R.id.etCreateTitle);
        descriptionEditText = findViewById(R.id.etCreateDescription);
        tagsEditText = findViewById(R.id.etCreateTags);
        placeSearchAutoCompleteTextView = findViewById(R.id.actvCreatePlaceSearch);
        placeTypeSpinner = findViewById(R.id.spinnerCreatePlaceType);
        selectedImagePreview = findViewById(R.id.ivSelectedPhotoPreview);
        selectedPlaceLabelTextView = findViewById(R.id.tvSelectedPlaceValue);
        selectedPlaceDetailsTextView = findViewById(R.id.tvSelectedPlaceDetails);

        boolean placesConfigured = !TextUtils.isEmpty(BuildConfig.PLACES_API_KEY);
        placeSearchAutoCompleteTextView.setEnabled(placesConfigured);
        if (!placesConfigured) {
            selectedPlaceLabelTextView.setText(R.string.travelshare_place_api_missing);
            selectedPlaceDetailsTextView.setText(R.string.travelshare_place_api_missing);
            placeSearchAutoCompleteTextView.setVisibility(View.GONE);
        }
    }

    private void setupPlaceTypeSpinner() {
        ArrayAdapter<String> placeTypeAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                getResources().getStringArray(R.array.travelshare_place_type_labels)
        );
        placeTypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        placeTypeSpinner.setAdapter(placeTypeAdapter);
    }

    private void setupPlaceSearchField() {
        placeSuggestionsAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                new ArrayList<>()
        );
        placeSearchAutoCompleteTextView.setThreshold(2);
        placeSearchAutoCompleteTextView.setAdapter(placeSuggestionsAdapter);
        placeSearchAutoCompleteTextView.setOnItemClickListener(this::onPlaceSuggestionSelected);
        placeSearchAutoCompleteTextView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // No-op
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (updatingPlaceSearchText) {
                    return;
                }

                clearSelectedPlaceData();
                String query = s == null ? "" : s.toString().trim();
                if (query.length() < 2) {
                    clearPredictions();
                    return;
                }
                requestPlacePredictions(query);
            }

            @Override
            public void afterTextChanged(Editable s) {
                // No-op
            }
        });
    }

    private void onPlaceSuggestionSelected(AdapterView<?> parent, View view, int position, long id) {
        if (position < 0 || position >= currentPredictions.size()) {
            return;
        }

        AutocompletePrediction prediction = currentPredictions.get(position);
        fetchSelectedPlace(
                prediction.getPlaceId(),
                prediction.getFullText(null).toString()
        );
    }

    private void requestPlacePredictions(String query) {
        if (placesClient == null || TextUtils.isEmpty(BuildConfig.PLACES_API_KEY)) {
            Log.w(TAG, "Ignoring autocomplete request because Places client is unavailable.");
            return;
        }

        placeSearchHandler.removeCallbacksAndMessages(null);
        placeSearchRunnable = () -> {
            if (autocompleteSessionToken == null) {
                autocompleteSessionToken = AutocompleteSessionToken.newInstance();
            }

            FindAutocompletePredictionsRequest request =
                    FindAutocompletePredictionsRequest.builder()
                            .setQuery(query)
                            .setSessionToken(autocompleteSessionToken)
                            .build();

            Log.d(TAG, "Requesting autocomplete predictions for query='" + query + "'.");
            placesClient.findAutocompletePredictions(request)
                    .addOnSuccessListener(this::applyPredictionResults)
                    .addOnFailureListener(exception -> showPlacesError(
                            "findAutocompletePredictions",
                            null,
                            exception
                    ));
        };
        placeSearchHandler.postDelayed(placeSearchRunnable, PLACE_SEARCH_DEBOUNCE_MS);
    }

    private void applyPredictionResults(FindAutocompletePredictionsResponse response) {
        currentPredictions.clear();
        currentPredictions.addAll(response.getAutocompletePredictions());

        List<String> suggestionLabels = new ArrayList<>();
        for (AutocompletePrediction prediction : currentPredictions) {
            suggestionLabels.add(prediction.getFullText(null).toString());
        }

        Log.d(TAG, "Autocomplete returned " + suggestionLabels.size() + " suggestion(s).");

        placeSuggestionsAdapter.clear();
        placeSuggestionsAdapter.addAll(suggestionLabels);
        placeSuggestionsAdapter.notifyDataSetChanged();

        if (!suggestionLabels.isEmpty()) {
            placeSearchAutoCompleteTextView.showDropDown();
        } else {
            placeSearchAutoCompleteTextView.dismissDropDown();
        }
    }

    private void fetchSelectedPlace(String placeId, String displayLabel) {
        if (placesClient == null || TextUtils.isEmpty(placeId)) {
            Toast.makeText(this, R.string.travelshare_place_autocomplete_error, Toast.LENGTH_SHORT).show();
            return;
        }

        List<Place.Field> fields = Arrays.asList(
                Place.Field.DISPLAY_NAME,
                Place.Field.FORMATTED_ADDRESS,
                Place.Field.LOCATION,
                Place.Field.ADDRESS_COMPONENTS
        );

        FetchPlaceRequest request = FetchPlaceRequest.builder(placeId, fields)
                .setSessionToken(autocompleteSessionToken)
                .build();

        Log.d(TAG, "Fetching place details for placeId=" + placeId + " label='" + displayLabel + "'.");
        placesClient.fetchPlace(request)
                .addOnSuccessListener(response -> applySelectedPlace(response, displayLabel))
                .addOnFailureListener(exception -> showPlacesError(
                        "fetchPlace",
                        null,
                        exception
                ));
    }

    private void applySelectedPlace(FetchPlaceResponse response, String displayLabel) {
        Place place = response.getPlace();
        String placeName = safeValue(place.getDisplayName());
        String formattedAddress = safeValue(place.getFormattedAddress());
        String city = extractAddressComponent(place.getAddressComponents(), "locality", "postal_town", "administrative_area_level_2");
        String country = extractAddressComponent(place.getAddressComponents(), "country");
        double latitude = place.getLocation() == null ? Double.NaN : place.getLocation().latitude;
        double longitude = place.getLocation() == null ? Double.NaN : place.getLocation().longitude;

        updatingPlaceSearchText = true;
        placeSearchAutoCompleteTextView.setText(displayLabel, false);
        updatingPlaceSearchText = false;

        selectedPlaceName = placeName.isEmpty() ? displayLabel : placeName;
        selectedPlaceAddress = formattedAddress;
        selectedPlaceCity = city;
        selectedPlaceCountry = country;
        selectedPlaceLatitude = latitude;
        selectedPlaceLongitude = longitude;
        hasSelectedPlace = !TextUtils.isEmpty(selectedPlaceAddress)
                && !TextUtils.isEmpty(selectedPlaceCountry)
                && !Double.isNaN(selectedPlaceLatitude)
                && !Double.isNaN(selectedPlaceLongitude);

        selectedPlaceLabelTextView.setText(selectedPlaceName);
        selectedPlaceDetailsTextView.setText(buildSelectedPlaceDetails());
        autocompleteSessionToken = null;
        clearPredictions();

        Log.d(TAG, "Selected place resolved: name='" + selectedPlaceName
                + "', city='" + selectedPlaceCity
                + "', country='" + selectedPlaceCountry
                + "', lat=" + selectedPlaceLatitude
                + ", lng=" + selectedPlaceLongitude
                + ", valid=" + hasSelectedPlace + ".");

        if (!hasSelectedPlace) {
            Toast.makeText(this, R.string.travelshare_place_fetch_incomplete, Toast.LENGTH_LONG).show();
        }
    }

    private String extractAddressComponent(AddressComponents addressComponents, String... expectedTypes) {
        if (addressComponents == null) {
            return "";
        }

        List<String> expected = Arrays.asList(expectedTypes);
        for (AddressComponent component : addressComponents.asList()) {
            for (String type : component.getTypes()) {
                if (expected.contains(type)) {
                    return safeValue(component.getName());
                }
            }
        }
        return "";
    }

    private String safeValue(CharSequence value) {
        return value == null ? "" : value.toString().trim();
    }

    private void clearSelectedPlaceData() {
        selectedPlaceName = "";
        selectedPlaceAddress = "";
        selectedPlaceCity = "";
        selectedPlaceCountry = "";
        selectedPlaceLatitude = Double.NaN;
        selectedPlaceLongitude = Double.NaN;
        hasSelectedPlace = false;
        selectedPlaceLabelTextView.setText(R.string.travelshare_place_not_selected);
        selectedPlaceDetailsTextView.setText(R.string.travelshare_place_details_empty);
    }

    private void clearPredictions() {
        currentPredictions.clear();
        placeSuggestionsAdapter.clear();
        placeSuggestionsAdapter.notifyDataSetChanged();
    }

    private String buildSelectedPlaceDetails() {
        List<String> lines = new ArrayList<>();

        if (!TextUtils.isEmpty(selectedPlaceAddress)) {
            lines.add(selectedPlaceAddress);
        }

        StringBuilder localityBuilder = new StringBuilder();
        if (!TextUtils.isEmpty(selectedPlaceCity)) {
            localityBuilder.append(selectedPlaceCity);
        }
        if (!TextUtils.isEmpty(selectedPlaceCountry)) {
            if (localityBuilder.length() > 0) {
                localityBuilder.append(", ");
            }
            localityBuilder.append(selectedPlaceCountry);
        }
        if (localityBuilder.length() > 0) {
            lines.add(localityBuilder.toString());
        }

        return lines.isEmpty()
                ? getString(R.string.travelshare_place_details_empty)
                : TextUtils.join("\n", lines);
    }

    private void showPlacesError(String step, Status status, Exception exception) {
        String statusMessage = status == null ? null : status.getStatusMessage();
        String fallbackMessage = exception == null ? null : exception.getMessage();

        if (exception instanceof ApiException) {
            ApiException apiException = (ApiException) exception;
            Log.e(
                    TAG,
                    "Places error during " + step
                            + " (statusCode=" + apiException.getStatusCode()
                            + ", message=" + apiException.getMessage() + ")",
                    apiException
            );
        } else if (exception != null) {
            Log.e(TAG, "Places error during " + step + ": " + exception.getMessage(), exception);
        } else {
            Log.e(TAG, "Places error during " + step + " without exception details.");
        }

        Toast.makeText(
                this,
                TextUtils.isEmpty(statusMessage)
                        ? (TextUtils.isEmpty(fallbackMessage)
                        ? getString(R.string.travelshare_place_autocomplete_error)
                        : fallbackMessage)
                        : statusMessage,
                Toast.LENGTH_LONG
        ).show();
    }

    private void publishPhotoMetadata() {
        String title = titleEditText.getText().toString().trim();
        String description = descriptionEditText.getText().toString().trim();
        String tagsRaw = tagsEditText.getText().toString().trim();

        if (title.isEmpty() || description.isEmpty()) {
            Toast.makeText(this, R.string.travelshare_create_missing_fields, Toast.LENGTH_SHORT).show();
            return;
        }

        if (!hasSelectedPlace) {
            Toast.makeText(this, R.string.travelshare_place_required, Toast.LENGTH_SHORT).show();
            return;
        }

        if (selectedImageUri == null) {
            Toast.makeText(this, R.string.travelshare_create_missing_photo, Toast.LENGTH_SHORT).show();
            return;
        }

        List<String> tags = parseTags(tagsRaw);
        PlaceType placeType = PlaceType.values()[placeTypeSpinner.getSelectedItemPosition()];

        if (travelShareRepository.createPhotoMetadata(
                title,
                description,
                selectedPlaceAddress,
                TextUtils.isEmpty(selectedPlaceCity) ? selectedPlaceName : selectedPlaceCity,
                selectedPlaceCountry,
                selectedPlaceLatitude,
                selectedPlaceLongitude,
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
