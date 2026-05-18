package com.kcorteel.travel_esteban_kylian;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.speech.RecognizerIntent;
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
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.content.ContextCompat;
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
import com.kcorteel.travel_esteban_kylian.travelshare.annotation.AnnotationSuggestion;
import com.kcorteel.travel_esteban_kylian.travelshare.annotation.GeminiTravelShareAnnotationProvider;
import com.kcorteel.travel_esteban_kylian.travelshare.model.TravelGroup;
import com.kcorteel.travel_esteban_kylian.travelshare.annotation.TravelShareAnnotationProvider;
import com.kcorteel.travel_esteban_kylian.travelshare.model.PlaceType;
import com.kcorteel.travel_esteban_kylian.travelshare.repository.TravelShareRepository;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CreatePhotoMetadataActivity extends AppCompatActivity {

    public static final String EXTRA_PRESELECT_GROUP_ID = "extra_preselect_group_id";

    private static final String TAG = "CreatePhotoMetadata";
    private static final long PLACE_SEARCH_DEBOUNCE_MS = 300L;
    private static final long AI_ANNOTATION_DEBOUNCE_MS = 900L;

    private EditText titleEditText;
    private EditText descriptionEditText;
    private EditText tagsEditText;
    private AutoCompleteTextView placeSearchAutoCompleteTextView;
    private EditText manualPlaceNameEditText;
    private EditText manualPlaceAddressEditText;
    private EditText manualPlaceCityEditText;
    private EditText manualPlaceCountryEditText;
    private EditText manualPlaceLatitudeEditText;
    private EditText manualPlaceLongitudeEditText;
    private Spinner groupSpinner;
    private Spinner placeTypeSpinner;
    private ImageView selectedImagePreview;
    private View photoPlaceholderView;
    private TextView selectedPlaceLabelTextView;
    private TextView selectedPlaceDetailsTextView;
    private TextView manualPlaceFallbackTextView;
    private TextView annotationStatusTextView;
    private TextView annotationSummaryTextView;
    private TextView annotationTagsTextView;
    private TextView voiceNoteStatusTextView;
    private ProgressBar annotationProgressBar;
    private Button applyAnnotationButton;
    private Button showManualPlaceFormButton;
    private Button selectPhotoButton;
    private Button recordVoiceNoteButton;
    private Button playVoiceNoteButton;
    private Button deleteVoiceNoteButton;
    private View manualPlaceContainer;

    private TravelShareRepository travelShareRepository;
    private TravelShareAnnotationProvider annotationProvider;
    private Uri selectedImageUri;
    private ActivityResultLauncher<String[]> openDocumentLauncher;
    private ActivityResultLauncher<String> recordAudioPermissionLauncher;
    private ActivityResultLauncher<Intent> descriptionSpeechLauncher;
    private PlacesClient placesClient;
    private ArrayAdapter<String> placeSuggestionsAdapter;
    private final List<AutocompletePrediction> currentPredictions = new ArrayList<>();
    private final Handler placeSearchHandler = new Handler(Looper.getMainLooper());
    private final Handler annotationHandler = new Handler(Looper.getMainLooper());
    private final ExecutorService annotationExecutor = Executors.newSingleThreadExecutor();
    private Runnable placeSearchRunnable;
    private Runnable annotationDebounceRunnable;
    private AutocompleteSessionToken autocompleteSessionToken;
    private boolean updatingPlaceSearchText;
    private boolean placeAutocompleteUnavailable;
    private boolean manualPlaceFormExpanded;
    private String placeAutocompleteUnavailableMessage = "";
    private String selectedPlaceName = "";
    private String selectedPlaceAddress = "";
    private String selectedPlaceCity = "";
    private String selectedPlaceCountry = "";
    private double selectedPlaceLatitude;
    private double selectedPlaceLongitude;
    private boolean hasSelectedPlace;
    private Long selectedGroupId;
    private final List<TravelGroup> availableGroups = new ArrayList<>();
    private MediaRecorder mediaRecorder;
    private MediaPlayer audioPreviewPlayer;
    private String voiceNotePath = "";
    private boolean isRecordingVoiceNote;
    private AnnotationSuggestion latestAnnotationSuggestion;
    private int latestAnnotationRequestId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        travelShareRepository = TravelShareRepository.getInstance(this);
        travelShareRepository.applyCurrentUserDisplayPreferences();
        setContentView(R.layout.activity_create_photo_metadata);
        annotationProvider = new GeminiTravelShareAnnotationProvider(this);

        if (travelShareRepository.isCurrentUserAnonymous()) {
            Toast.makeText(this, R.string.travelshare_create_requires_login, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        setupImagePicker();
        setupVoiceInputHelpers();
        setupPlaceAutocomplete();
        bindViews();
        setupGroupSpinner();
        setupPlaceTypeSpinner();
        setupPlaceSearchField();
        setupAnnotationAssistant();

        selectPhotoButton.setOnClickListener(v -> openDocumentLauncher.launch(new String[]{"image/*"}));

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
                    updatePhotoPreviewState();
                    scheduleAnnotationSuggestion();
                }
        );
    }

    private void setupPlaceAutocomplete() {
        if (TextUtils.isEmpty(BuildConfig.PLACES_API_KEY)) {
            Log.w(TAG, "Places API key is missing; autocomplete is disabled.");
            markPlaceAutocompleteUnavailable(getString(R.string.travelshare_place_manual_unavailable));
            return;
        }

        try {
            if (!Places.isInitialized()) {
                Places.initializeWithNewPlacesApiEnabled(getApplicationContext(), BuildConfig.PLACES_API_KEY);
            }
            placesClient = Places.createClient(this);
            Log.d(TAG, "Places client initialized successfully.");
        } catch (Exception exception) {
            Log.e(TAG, "Places initialization failed: " + exception.getMessage(), exception);
            markPlaceAutocompleteUnavailable(getString(R.string.travelshare_place_manual_unavailable));
        }
    }

    private void bindViews() {
        titleEditText = findViewById(R.id.etCreateTitle);
        descriptionEditText = findViewById(R.id.etCreateDescription);
        tagsEditText = findViewById(R.id.etCreateTags);
        placeSearchAutoCompleteTextView = findViewById(R.id.actvCreatePlaceSearch);
        manualPlaceNameEditText = findViewById(R.id.etManualPlaceName);
        manualPlaceAddressEditText = findViewById(R.id.etManualPlaceAddress);
        manualPlaceCityEditText = findViewById(R.id.etManualPlaceCity);
        manualPlaceCountryEditText = findViewById(R.id.etManualPlaceCountry);
        manualPlaceLatitudeEditText = findViewById(R.id.etManualPlaceLatitude);
        manualPlaceLongitudeEditText = findViewById(R.id.etManualPlaceLongitude);
        groupSpinner = findViewById(R.id.spinnerCreateGroup);
        placeTypeSpinner = findViewById(R.id.spinnerCreatePlaceType);
        selectedImagePreview = findViewById(R.id.ivSelectedPhotoPreview);
        photoPlaceholderView = findViewById(R.id.layoutPhotoPlaceholder);
        selectedPlaceLabelTextView = findViewById(R.id.tvSelectedPlaceValue);
        selectedPlaceDetailsTextView = findViewById(R.id.tvSelectedPlaceDetails);
        manualPlaceFallbackTextView = findViewById(R.id.tvManualPlaceFallback);
        annotationStatusTextView = findViewById(R.id.tvAnnotationStatus);
        annotationSummaryTextView = findViewById(R.id.tvAnnotationSummary);
        annotationTagsTextView = findViewById(R.id.tvAnnotationTags);
        voiceNoteStatusTextView = findViewById(R.id.tvVoiceNoteStatus);
        annotationProgressBar = findViewById(R.id.progressAnnotation);
        applyAnnotationButton = findViewById(R.id.btnApplyAnnotation);
        showManualPlaceFormButton = findViewById(R.id.btnShowManualPlaceForm);
        selectPhotoButton = findViewById(R.id.btnSelectPhoto);
        recordVoiceNoteButton = findViewById(R.id.btnRecordVoiceNote);
        playVoiceNoteButton = findViewById(R.id.btnPlayVoiceNote);
        deleteVoiceNoteButton = findViewById(R.id.btnDeleteVoiceNote);
        manualPlaceContainer = findViewById(R.id.layoutManualPlaceContainer);

        showManualPlaceFormButton.setOnClickListener(v -> {
            manualPlaceFormExpanded = true;
            renderManualPlaceFallbackState();
            manualPlaceNameEditText.requestFocus();
        });

        bindManualPlaceWatchers();
        renderManualPlaceFallbackState();

        renderAnnotationSuggestion(null);
        updatePhotoPreviewState();
        updateVoiceNoteUi();
    }

    private void updatePhotoPreviewState() {
        boolean hasPhoto = selectedImageUri != null;
        photoPlaceholderView.setVisibility(hasPhoto ? View.GONE : View.VISIBLE);
        selectPhotoButton.setText(hasPhoto
                ? R.string.travelshare_change_photo_button
                : R.string.travelshare_select_photo_button);
    }

    private void bindManualPlaceWatchers() {
        SimpleTextWatcher watcher = new SimpleTextWatcher(() -> {
            updateSelectedPlacePreviewFromManualInputs();
            scheduleAnnotationSuggestion();
        });
        manualPlaceNameEditText.addTextChangedListener(watcher);
        manualPlaceAddressEditText.addTextChangedListener(watcher);
        manualPlaceCityEditText.addTextChangedListener(watcher);
        manualPlaceCountryEditText.addTextChangedListener(watcher);
        manualPlaceLatitudeEditText.addTextChangedListener(watcher);
        manualPlaceLongitudeEditText.addTextChangedListener(watcher);
    }

    private void markPlaceAutocompleteUnavailable(String fallbackMessage) {
        placeAutocompleteUnavailable = true;
        placeAutocompleteUnavailableMessage = TextUtils.isEmpty(fallbackMessage)
                ? getString(R.string.travelshare_place_manual_unavailable)
                : fallbackMessage;
        if (placeSearchAutoCompleteTextView != null) {
            renderManualPlaceFallbackState();
        }
    }

    private void renderManualPlaceFallbackState() {
        if (placeSearchAutoCompleteTextView == null) {
            return;
        }

        placeSearchAutoCompleteTextView.setEnabled(!placeAutocompleteUnavailable);
        placeSearchAutoCompleteTextView.setVisibility(placeAutocompleteUnavailable ? View.GONE : View.VISIBLE);

        int fallbackVisibility = placeAutocompleteUnavailable ? View.VISIBLE : View.GONE;
        manualPlaceFallbackTextView.setVisibility(fallbackVisibility);
        manualPlaceFallbackTextView.setText(
                TextUtils.isEmpty(placeAutocompleteUnavailableMessage)
                        ? getString(R.string.travelshare_place_manual_unavailable)
                        : placeAutocompleteUnavailableMessage
        );

        showManualPlaceFormButton.setVisibility(
                placeAutocompleteUnavailable && !manualPlaceFormExpanded ? View.VISIBLE : View.GONE
        );
        manualPlaceContainer.setVisibility(
                placeAutocompleteUnavailable && manualPlaceFormExpanded ? View.VISIBLE : View.GONE
        );

        if (placeAutocompleteUnavailable) {
            updateSelectedPlacePreviewFromManualInputs();
        }
    }

    private void updateSelectedPlacePreviewFromManualInputs() {
        if (!placeAutocompleteUnavailable || hasSelectedPlace) {
            return;
        }

        PlaceDraft placeDraft = resolvePlaceDraft();
        if (placeDraft.name.isEmpty() && placeDraft.city.isEmpty() && placeDraft.country.isEmpty()) {
            selectedPlaceLabelTextView.setText(R.string.travelshare_place_not_selected);
            selectedPlaceDetailsTextView.setText(R.string.travelshare_place_details_empty);
            return;
        }

        selectedPlaceLabelTextView.setText(
                placeDraft.name.isEmpty()
                        ? getString(R.string.travelshare_place_not_selected)
                        : placeDraft.name
        );

        List<String> lines = new ArrayList<>();
        if (!placeDraft.address.isEmpty() && !placeDraft.address.equals(placeDraft.name)) {
            lines.add(placeDraft.address);
        }

        StringBuilder localityBuilder = new StringBuilder();
        if (!placeDraft.city.isEmpty()) {
            localityBuilder.append(placeDraft.city);
        }
        if (!placeDraft.country.isEmpty()) {
            if (localityBuilder.length() > 0) {
                localityBuilder.append(", ");
            }
            localityBuilder.append(placeDraft.country);
        }
        if (localityBuilder.length() > 0) {
            lines.add(localityBuilder.toString());
        }

        if (!placeDraft.areCoordinatesUsable()) {
            lines.add(getString(R.string.travelshare_create_invalid_coordinates));
        }

        selectedPlaceDetailsTextView.setText(
                lines.isEmpty()
                        ? getString(R.string.travelshare_place_details_empty)
                        : TextUtils.join("\n", lines)
        );
    }

    private void setupGroupSpinner() {
        availableGroups.clear();
        availableGroups.addAll(travelShareRepository.getGroupsForCurrentUser());

        List<String> groupLabels = new ArrayList<>();
        groupLabels.add(getString(R.string.travelshare_group_none_option));
        for (TravelGroup group : availableGroups) {
            groupLabels.add(group.getGroupName());
        }

        ArrayAdapter<String> groupAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                groupLabels
        );
        groupAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        groupSpinner.setAdapter(groupAdapter);
        groupSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedGroupId = position == 0 ? null : availableGroups.get(position - 1).getGroupId();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                selectedGroupId = null;
            }
        });

        long preselectedGroupId = getIntent().getLongExtra(EXTRA_PRESELECT_GROUP_ID, -1L);
        if (preselectedGroupId > 0L) {
            for (int i = 0; i < availableGroups.size(); i++) {
                if (availableGroups.get(i).getGroupId() == preselectedGroupId) {
                    groupSpinner.setSelection(i + 1, false);
                    selectedGroupId = preselectedGroupId;
                    break;
                }
            }
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
        placeTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                scheduleAnnotationSuggestion();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // No-op
            }
        });
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
                    scheduleAnnotationSuggestion();
                    return;
                }
                requestPlacePredictions(query);
                scheduleAnnotationSuggestion();
            }

            @Override
            public void afterTextChanged(Editable s) {
                // No-op
            }
        });
    }

    private void setupAnnotationAssistant() {
        titleEditText.addTextChangedListener(new SimpleTextWatcher(this::scheduleAnnotationSuggestion));
        descriptionEditText.addTextChangedListener(new SimpleTextWatcher(this::scheduleAnnotationSuggestion));
        applyAnnotationButton.setOnClickListener(v -> applyAnnotationSuggestion());
        descriptionEditText.setOnLongClickListener(v -> {
            startDescriptionSpeechInput();
            return true;
        });
        recordVoiceNoteButton.setOnClickListener(v -> toggleVoiceRecording());
        playVoiceNoteButton.setOnClickListener(v -> toggleVoicePlayback());
        deleteVoiceNoteButton.setOnClickListener(v -> deleteVoiceNote());

        if (!annotationProvider.isConfigured()) {
            annotationStatusTextView.setText(R.string.travelshare_annotation_missing_key);
            applyAnnotationButton.setEnabled(false);
            return;
        }

        annotationStatusTextView.setText(R.string.travelshare_annotation_subtitle);
        applyAnnotationButton.setEnabled(false);
    }

    private void setupVoiceInputHelpers() {
        recordAudioPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                isGranted -> {
                    if (isGranted) {
                        startVoiceRecording();
                    } else {
                        Toast.makeText(
                                this,
                                R.string.travelshare_voice_note_permission_required,
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                }
        );

        descriptionSpeechLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                this::handleDescriptionSpeechResult
        );
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
            markPlaceAutocompleteUnavailable(getString(R.string.travelshare_place_manual_unavailable));
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

        scheduleAnnotationSuggestion();
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

        clearPredictions();
        markPlaceAutocompleteUnavailable(getString(R.string.travelshare_place_manual_unavailable));

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

    private void scheduleAnnotationSuggestion() {
        if (!annotationProvider.isConfigured()) {
            return;
        }

        annotationHandler.removeCallbacksAndMessages(null);
        annotationDebounceRunnable = () -> {
            if (!hasAnnotationInput()) {
                latestAnnotationSuggestion = null;
                annotationProgressBar.setVisibility(View.GONE);
                annotationStatusTextView.setText(R.string.travelshare_annotation_empty_state);
                renderAnnotationSuggestion(null);
                return;
            }
            requestAnnotationSuggestion();
        };
        annotationHandler.postDelayed(annotationDebounceRunnable, AI_ANNOTATION_DEBOUNCE_MS);
    }

    private void applyAnnotationSuggestion() {
        if (latestAnnotationSuggestion == null || latestAnnotationSuggestion.isEmpty()) {
            return;
        }

        if (descriptionEditText.getText().toString().trim().isEmpty()
                && !latestAnnotationSuggestion.getSummary().isEmpty()) {
            descriptionEditText.setText(latestAnnotationSuggestion.getSummary());
        }

        List<String> mergedTags = parseTags(tagsEditText.getText().toString().trim());
        for (String tag : latestAnnotationSuggestion.getTags()) {
            if (!containsIgnoreCase(mergedTags, tag)) {
                mergedTags.add(tag);
            }
        }
        tagsEditText.setText(TextUtils.join(", ", mergedTags));
        Toast.makeText(this, R.string.travelshare_annotation_applied, Toast.LENGTH_SHORT).show();
    }

    private void requestAnnotationSuggestion() {
        final int requestId = ++latestAnnotationRequestId;
        final Uri imageUri = selectedImageUri;
        final String title = titleEditText.getText().toString();
        final String description = descriptionEditText.getText().toString();
        final PlaceDraft placeDraft = resolvePlaceDraft();
        final PlaceType placeType = PlaceType.values()[placeTypeSpinner.getSelectedItemPosition()];

        annotationProgressBar.setVisibility(View.VISIBLE);
        annotationStatusTextView.setText(R.string.travelshare_annotation_loading);

        annotationExecutor.execute(() -> {
            try {
                AnnotationSuggestion suggestion = annotationProvider.generateSuggestion(
                        imageUri,
                        title,
                        description,
                        placeDraft.name,
                        placeDraft.city,
                        placeDraft.country,
                        placeType
                );
                runOnUiThread(() -> {
                    if (requestId != latestAnnotationRequestId) {
                        return;
                    }
                    annotationProgressBar.setVisibility(View.GONE);
                    latestAnnotationSuggestion = suggestion;
                    annotationStatusTextView.setText(R.string.travelshare_annotation_subtitle);
                    renderAnnotationSuggestion(suggestion);
                });
            } catch (Exception exception) {
                Log.e(TAG, "Annotation IA error: " + exception.getMessage(), exception);
                runOnUiThread(() -> {
                    if (requestId != latestAnnotationRequestId) {
                        return;
                    }
                    annotationProgressBar.setVisibility(View.GONE);
                    latestAnnotationSuggestion = null;
                    annotationStatusTextView.setText(buildAnnotationErrorMessage(exception));
                    renderAnnotationSuggestion(null);
                });
            }
        });
    }

    private void renderAnnotationSuggestion(AnnotationSuggestion suggestion) {
        if (suggestion == null || suggestion.isEmpty()) {
            annotationSummaryTextView.setText(R.string.travelshare_annotation_empty_state);
            annotationTagsTextView.setText(R.string.travelshare_annotation_empty_state);
            applyAnnotationButton.setEnabled(false);
            return;
        }

        annotationSummaryTextView.setText(
                suggestion.getSummary().isEmpty()
                        ? getString(R.string.travelshare_annotation_empty_state)
                        : suggestion.getSummary()
        );
        annotationTagsTextView.setText(
                suggestion.getTags().isEmpty()
                        ? getString(R.string.travelshare_annotation_empty_state)
                        : TextUtils.join(", ", suggestion.getTags())
        );
        applyAnnotationButton.setEnabled(true);
    }

    private void publishPhotoMetadata() {
        String title = titleEditText.getText().toString().trim();
        String description = descriptionEditText.getText().toString().trim();
        String tagsRaw = tagsEditText.getText().toString().trim();
        PlaceDraft placeDraft = resolvePlaceDraft();

        if (title.isEmpty() || description.isEmpty()) {
            Toast.makeText(this, R.string.travelshare_create_missing_fields, Toast.LENGTH_SHORT).show();
            return;
        }

        if (hasSelectedPlace) {
            placeDraft = PlaceDraft.fromAutocomplete(
                    selectedPlaceName,
                    selectedPlaceAddress,
                    selectedPlaceCity,
                    selectedPlaceCountry,
                    selectedPlaceLatitude,
                    selectedPlaceLongitude
            );
        } else if (!placeDraft.isComplete()) {
            int messageRes = placeAutocompleteUnavailable
                    ? R.string.travelshare_place_manual_required
                    : R.string.travelshare_place_required;
            Toast.makeText(this, messageRes, Toast.LENGTH_SHORT).show();
            return;
        }

        if (!placeDraft.areCoordinatesUsable()) {
            Toast.makeText(this, R.string.travelshare_create_invalid_coordinates, Toast.LENGTH_SHORT).show();
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
                placeDraft.address,
                placeDraft.city,
                placeDraft.country,
                placeDraft.latitude,
                placeDraft.longitude,
                tags,
                placeType,
                selectedGroupId,
                normalizeVoiceNotePath(),
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

    private boolean containsIgnoreCase(List<String> values, String candidate) {
        String normalizedCandidate = candidate == null ? "" : candidate.trim().toLowerCase(Locale.getDefault());
        for (String value : values) {
            if (value != null && value.trim().toLowerCase(Locale.getDefault()).equals(normalizedCandidate)) {
                return true;
            }
        }
        return false;
    }

    private boolean hasAnnotationInput() {
        PlaceDraft placeDraft = resolvePlaceDraft();
        return selectedImageUri != null
                || !titleEditText.getText().toString().trim().isEmpty()
                || !descriptionEditText.getText().toString().trim().isEmpty()
                || !placeDraft.name.isEmpty()
                || !placeDraft.city.isEmpty()
                || !placeDraft.country.isEmpty();
    }

    private PlaceDraft resolvePlaceDraft() {
        if (hasSelectedPlace) {
            return PlaceDraft.fromAutocomplete(
                    selectedPlaceName,
                    selectedPlaceAddress,
                    selectedPlaceCity,
                    selectedPlaceCountry,
                    selectedPlaceLatitude,
                    selectedPlaceLongitude
            );
        }

        String name = manualPlaceNameEditText.getText().toString().trim();
        String address = manualPlaceAddressEditText.getText().toString().trim();
        String city = manualPlaceCityEditText.getText().toString().trim();
        String country = manualPlaceCountryEditText.getText().toString().trim();
        String latitudeRaw = manualPlaceLatitudeEditText.getText().toString().trim();
        String longitudeRaw = manualPlaceLongitudeEditText.getText().toString().trim();

        boolean hasCoordinateInput = !latitudeRaw.isEmpty() || !longitudeRaw.isEmpty();
        boolean coordinatesUsable = true;
        double latitude = Double.NaN;
        double longitude = Double.NaN;

        if (hasCoordinateInput) {
            coordinatesUsable = false;
            if (!latitudeRaw.isEmpty() && !longitudeRaw.isEmpty()) {
                try {
                    latitude = Double.parseDouble(latitudeRaw);
                    longitude = Double.parseDouble(longitudeRaw);
                    coordinatesUsable = latitude >= -90d
                            && latitude <= 90d
                            && longitude >= -180d
                            && longitude <= 180d;
                    if (!coordinatesUsable) {
                        latitude = Double.NaN;
                        longitude = Double.NaN;
                    }
                } catch (NumberFormatException exception) {
                    latitude = Double.NaN;
                    longitude = Double.NaN;
                }
            }
        }

        return new PlaceDraft(
                name,
                address.isEmpty() ? name : address,
                city,
                country,
                latitude,
                longitude,
                coordinatesUsable,
                hasCoordinateInput
        );
    }

    private String buildAnnotationErrorMessage(Exception exception) {
        String message = exception == null ? "" : exception.getMessage();
        if (TextUtils.isEmpty(message)) {
            return getString(R.string.travelshare_annotation_error);
        }

        message = message.trim();
        if (message.startsWith("Gemini API error:")) {
            message = message.substring("Gemini API error:".length()).trim();
        }

        return getString(R.string.travelshare_annotation_error_with_reason, message);
    }

    private void startDescriptionSpeechInput() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, travelShareRepository.getCurrentLanguageTag());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, getString(R.string.travelshare_create_description_hint));

        try {
            descriptionSpeechLauncher.launch(intent);
        } catch (Exception exception) {
            Toast.makeText(this, R.string.travelshare_voice_search_unavailable, Toast.LENGTH_SHORT).show();
        }
    }

    private void handleDescriptionSpeechResult(ActivityResult result) {
        if (result.getResultCode() != RESULT_OK || result.getData() == null) {
            return;
        }

        ArrayList<String> matches = result.getData().getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
        if (matches == null || matches.isEmpty()) {
            return;
        }

        String recognizedText = matches.get(0).trim();
        if (recognizedText.isEmpty()) {
            return;
        }

        String current = descriptionEditText.getText().toString().trim();
        if (current.isEmpty()) {
            descriptionEditText.setText(recognizedText);
        } else {
            descriptionEditText.setText(current + " " + recognizedText);
        }
        descriptionEditText.setSelection(descriptionEditText.getText().length());
    }

    private void toggleVoiceRecording() {
        if (isRecordingVoiceNote) {
            stopVoiceRecording();
            return;
        }

        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.RECORD_AUDIO)
                == PackageManager.PERMISSION_GRANTED) {
            startVoiceRecording();
        } else {
            recordAudioPermissionLauncher.launch(android.Manifest.permission.RECORD_AUDIO);
        }
    }

    private void startVoiceRecording() {
        stopVoicePlayback();
        deleteVoiceFileSilently();
        voiceNotePath = buildVoiceNotePath();

        try {
            mediaRecorder = new MediaRecorder();
            mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
            mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
            mediaRecorder.setOutputFile(voiceNotePath);
            mediaRecorder.prepare();
            mediaRecorder.start();
            isRecordingVoiceNote = true;
            updateVoiceNoteUi();
        } catch (Exception exception) {
            Log.e(TAG, "Voice note recording error: " + exception.getMessage(), exception);
            releaseRecorder();
            voiceNotePath = "";
            isRecordingVoiceNote = false;
            updateVoiceNoteUi();
            Toast.makeText(this, R.string.travelshare_voice_note_error, Toast.LENGTH_SHORT).show();
        }
    }

    private void stopVoiceRecording() {
        try {
            if (mediaRecorder != null) {
                mediaRecorder.stop();
            }
            Toast.makeText(this, R.string.travelshare_voice_note_saved, Toast.LENGTH_SHORT).show();
        } catch (Exception exception) {
            Log.e(TAG, "Voice note stop error: " + exception.getMessage(), exception);
            deleteVoiceFileSilently();
            voiceNotePath = "";
            Toast.makeText(this, R.string.travelshare_voice_note_error, Toast.LENGTH_SHORT).show();
        } finally {
            releaseRecorder();
            isRecordingVoiceNote = false;
            updateVoiceNoteUi();
        }
    }

    private void toggleVoicePlayback() {
        if (audioPreviewPlayer != null && audioPreviewPlayer.isPlaying()) {
            stopVoicePlayback();
            updateVoiceNoteUi();
            return;
        }

        if (normalizeVoiceNotePath() == null) {
            return;
        }

        try {
            audioPreviewPlayer = new MediaPlayer();
            audioPreviewPlayer.setDataSource(voiceNotePath);
            audioPreviewPlayer.setOnCompletionListener(mp -> {
                stopVoicePlayback();
                updateVoiceNoteUi();
            });
            audioPreviewPlayer.prepare();
            audioPreviewPlayer.start();
            updateVoiceNoteUi();
        } catch (Exception exception) {
            Log.e(TAG, "Voice note playback error: " + exception.getMessage(), exception);
            stopVoicePlayback();
            updateVoiceNoteUi();
            Toast.makeText(this, R.string.travelshare_voice_note_error, Toast.LENGTH_SHORT).show();
        }
    }

    private void stopVoicePlayback() {
        if (audioPreviewPlayer != null) {
            try {
                if (audioPreviewPlayer.isPlaying()) {
                    audioPreviewPlayer.stop();
                }
            } catch (Exception ignored) {
                // No-op
            }
            audioPreviewPlayer.release();
            audioPreviewPlayer = null;
        }
    }

    private void deleteVoiceNote() {
        stopVoicePlayback();
        if (isRecordingVoiceNote) {
            stopVoiceRecording();
        }
        deleteVoiceFileSilently();
        voiceNotePath = "";
        updateVoiceNoteUi();
        Toast.makeText(this, R.string.travelshare_voice_note_deleted, Toast.LENGTH_SHORT).show();
    }

    private void updateVoiceNoteUi() {
        boolean hasVoiceNote = normalizeVoiceNotePath() != null;
        boolean isPlaying = audioPreviewPlayer != null && audioPreviewPlayer.isPlaying();

        if (isRecordingVoiceNote) {
            voiceNoteStatusTextView.setText(R.string.travelshare_voice_note_recording);
            recordVoiceNoteButton.setText(R.string.travelshare_voice_note_stop_button);
        } else if (hasVoiceNote) {
            voiceNoteStatusTextView.setText(R.string.travelshare_voice_note_ready);
            recordVoiceNoteButton.setText(R.string.travelshare_voice_note_record_button);
        } else {
            voiceNoteStatusTextView.setText(R.string.travelshare_voice_note_empty);
            recordVoiceNoteButton.setText(R.string.travelshare_voice_note_record_button);
        }

        playVoiceNoteButton.setVisibility(hasVoiceNote ? View.VISIBLE : View.GONE);
        deleteVoiceNoteButton.setVisibility(hasVoiceNote || isRecordingVoiceNote ? View.VISIBLE : View.GONE);
        playVoiceNoteButton.setEnabled(hasVoiceNote && !isRecordingVoiceNote);
        playVoiceNoteButton.setText(isPlaying
                ? R.string.travelshare_voice_note_stop_playback_button
                : R.string.travelshare_voice_note_play_button);
        deleteVoiceNoteButton.setEnabled(hasVoiceNote || isRecordingVoiceNote);
    }

    private String buildVoiceNotePath() {
        return new java.io.File(
                getFilesDir(),
                "voice_note_" + System.currentTimeMillis() + ".m4a"
        ).getAbsolutePath();
    }

    private void deleteVoiceFileSilently() {
        String path = normalizeVoiceNotePath();
        if (path == null) {
            return;
        }
        try {
            new java.io.File(path).delete();
        } catch (Exception ignored) {
            // No-op
        }
    }

    private String normalizeVoiceNotePath() {
        if (voiceNotePath == null || voiceNotePath.trim().isEmpty()) {
            return null;
        }
        return voiceNotePath.trim();
    }

    private void releaseRecorder() {
        if (mediaRecorder != null) {
            mediaRecorder.release();
            mediaRecorder = null;
        }
    }

    private static class PlaceDraft {
        private final String name;
        private final String address;
        private final String city;
        private final String country;
        private final double latitude;
        private final double longitude;
        private final boolean coordinatesUsable;
        private final boolean hasCoordinateInput;

        private PlaceDraft(
                String name,
                String address,
                String city,
                String country,
                double latitude,
                double longitude,
                boolean coordinatesUsable,
                boolean hasCoordinateInput
        ) {
            this.name = name == null ? "" : name.trim();
            this.address = address == null ? "" : address.trim();
            this.city = city == null ? "" : city.trim();
            this.country = country == null ? "" : country.trim();
            this.latitude = latitude;
            this.longitude = longitude;
            this.coordinatesUsable = coordinatesUsable;
            this.hasCoordinateInput = hasCoordinateInput;
        }

        private static PlaceDraft fromAutocomplete(
                String name,
                String address,
                String city,
                String country,
                double latitude,
                double longitude
        ) {
            return new PlaceDraft(name, address, city, country, latitude, longitude, true, true);
        }

        private boolean isComplete() {
            return !name.isEmpty() && !city.isEmpty() && !country.isEmpty();
        }

        private boolean areCoordinatesUsable() {
            return !hasCoordinateInput || coordinatesUsable;
        }
    }

    @Override
    protected void onDestroy() {
        if (isRecordingVoiceNote) {
            try {
                stopVoiceRecording();
            } catch (Exception ignored) {
                releaseRecorder();
            }
        } else {
            releaseRecorder();
        }
        stopVoicePlayback();
        annotationHandler.removeCallbacksAndMessages(null);
        placeSearchHandler.removeCallbacksAndMessages(null);
        annotationExecutor.shutdownNow();
        super.onDestroy();
    }

    private static class SimpleTextWatcher implements TextWatcher {
        private final Runnable onChanged;

        SimpleTextWatcher(Runnable onChanged) {
            this.onChanged = onChanged;
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            // No-op
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            onChanged.run();
        }

        @Override
        public void afterTextChanged(Editable s) {
            // No-op
        }
    }
}
