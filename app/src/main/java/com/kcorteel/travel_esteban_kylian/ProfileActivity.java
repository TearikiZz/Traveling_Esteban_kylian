package com.kcorteel.travel_esteban_kylian;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

import com.kcorteel.travel_esteban_kylian.auth.AuthManager;
import com.kcorteel.travel_esteban_kylian.travelshare.model.AppPreferences;
import com.kcorteel.travel_esteban_kylian.travelshare.model.AppTheme;
import com.kcorteel.travel_esteban_kylian.travelshare.model.User;
import com.kcorteel.travel_esteban_kylian.travelshare.repository.TravelShareRepository;

public class ProfileActivity extends AppCompatActivity {

    private ImageView avatarImageView;
    private EditText usernameEditText;
    private EditText emailEditText;
    private TextView modeTextView;
    private TextView userIdTextView;
    private TextView publicationsCountTextView;
    private TextView commentsCountTextView;
    private TextView likesCountTextView;
    private Spinner themeSpinner;
    private Spinner languageSpinner;
    private SwitchCompat notificationsSwitch;

    private TravelShareRepository travelShareRepository;
    private AuthManager authManager;
    private Uri selectedAvatarUri;
    private ActivityResultLauncher<String[]> openDocumentLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        travelShareRepository = TravelShareRepository.getInstance(this);
        travelShareRepository.applyCurrentUserThemePreference();
        setContentView(R.layout.activity_profile);

        authManager = new AuthManager(this);
        if (travelShareRepository.isCurrentUserAnonymous()) {
            Toast.makeText(this, R.string.travelshare_create_requires_login, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        bindViews();
        setupSpinners();
        setupImagePicker();
        setupActions();
        bindProfile();
    }

    private void bindViews() {
        avatarImageView = findViewById(R.id.ivProfileAvatar);
        usernameEditText = findViewById(R.id.etProfileUsername);
        emailEditText = findViewById(R.id.etProfileEmail);
        modeTextView = findViewById(R.id.tvProfileMode);
        userIdTextView = findViewById(R.id.tvProfileUserId);
        publicationsCountTextView = findViewById(R.id.tvProfilePublicationsCount);
        commentsCountTextView = findViewById(R.id.tvProfileCommentsCount);
        likesCountTextView = findViewById(R.id.tvProfileLikesCount);
        themeSpinner = findViewById(R.id.spinnerProfileTheme);
        languageSpinner = findViewById(R.id.spinnerProfileLanguage);
        notificationsSwitch = findViewById(R.id.switchProfileNotifications);
    }

    private void setupSpinners() {
        ArrayAdapter<String> themeAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                getResources().getStringArray(R.array.profile_theme_labels)
        );
        themeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        themeSpinner.setAdapter(themeAdapter);

        ArrayAdapter<String> languageAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                getResources().getStringArray(R.array.profile_language_labels)
        );
        languageAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        languageSpinner.setAdapter(languageAdapter);
    }

    private void setupImagePicker() {
        openDocumentLauncher = registerForActivityResult(
                new ActivityResultContracts.OpenDocument(),
                uri -> {
                    if (uri == null) {
                        return;
                    }

                    selectedAvatarUri = uri;
                    getContentResolver().takePersistableUriPermission(
                            uri,
                            Intent.FLAG_GRANT_READ_URI_PERMISSION
                    );
                    avatarImageView.setImageURI(uri);
                    avatarImageView.setImageTintList(null);
                }
        );
    }

    private void setupActions() {
        Button changeAvatarButton = findViewById(R.id.btnChangeAvatar);
        Button saveProfileButton = findViewById(R.id.btnSaveProfile);
        Button logoutButton = findViewById(R.id.btnProfileLogout);

        changeAvatarButton.setOnClickListener(v -> openDocumentLauncher.launch(new String[]{"image/*"}));
        saveProfileButton.setOnClickListener(v -> saveProfile());
        logoutButton.setOnClickListener(v -> logout());
    }

    private void bindProfile() {
        User currentUser = travelShareRepository.getCurrentUser();
        AppPreferences preferences = travelShareRepository.getCurrentUserPreferences();
        TravelShareRepository.ProfileStats stats = travelShareRepository.getCurrentUserProfileStats();

        if (currentUser == null) {
            finish();
            return;
        }

        selectedAvatarUri = currentUser.getAvatarUri() == null || currentUser.getAvatarUri().trim().isEmpty()
                ? null
                : Uri.parse(currentUser.getAvatarUri());

        travelShareRepository.loadUserAvatarIntoImageView(avatarImageView, currentUser);
        usernameEditText.setText(currentUser.getUsername());
        emailEditText.setText(currentUser.getEmail());
        modeTextView.setText(getString(R.string.profile_mode_format, getString(R.string.profile_mode_connected)));
        userIdTextView.setText(getString(R.string.profile_user_id_format, currentUser.getUserId()));

        publicationsCountTextView.setText(String.valueOf(stats.getPublicationsCount()));
        commentsCountTextView.setText(String.valueOf(stats.getCommentsCount()));
        likesCountTextView.setText(String.valueOf(stats.getLikesReceivedCount()));

        themeSpinner.setSelection(mapThemeToSelection(preferences.getTheme()));
        languageSpinner.setSelection(mapLanguageToSelection(preferences.getLanguage()));
        notificationsSwitch.setChecked(preferences.isNotificationsEnabled());
    }

    private void saveProfile() {
        String errorMessage = travelShareRepository.updateCurrentUserProfile(
                usernameEditText.getText().toString(),
                emailEditText.getText().toString(),
                selectedAvatarUri == null ? null : selectedAvatarUri.toString()
        );

        if (errorMessage != null) {
            Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show();
            return;
        }

        travelShareRepository.updateCurrentUserPreferences(
                mapSelectionToTheme(themeSpinner.getSelectedItemPosition()),
                mapSelectionToLanguage(languageSpinner.getSelectedItemPosition()),
                notificationsSwitch.isChecked()
        );
        travelShareRepository.applyCurrentUserThemePreference();
        bindProfile();
        Toast.makeText(this, R.string.profile_saved_message, Toast.LENGTH_SHORT).show();
    }

    private void logout() {
        authManager.logout();
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    private int mapThemeToSelection(AppTheme theme) {
        switch (theme) {
            case LIGHT:
                return 1;
            case DARK:
                return 2;
            case SYSTEM:
            default:
                return 0;
        }
    }

    private AppTheme mapSelectionToTheme(int position) {
        switch (position) {
            case 1:
                return AppTheme.LIGHT;
            case 2:
                return AppTheme.DARK;
            case 0:
            default:
                return AppTheme.SYSTEM;
        }
    }

    private int mapLanguageToSelection(String language) {
        if ("en".equalsIgnoreCase(language)) {
            return 1;
        }
        if ("es".equalsIgnoreCase(language)) {
            return 2;
        }
        return 0;
    }

    private String mapSelectionToLanguage(int position) {
        switch (position) {
            case 1:
                return "en";
            case 2:
                return "es";
            case 0:
            default:
                return "fr";
        }
    }
}
