package com.kcorteel.travel_esteban_kylian;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Button;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.kcorteel.travel_esteban_kylian.travelshare.adapter.PhotoMetadataAdapter;
import com.kcorteel.travel_esteban_kylian.travelshare.model.PhotoMetadata;
import com.kcorteel.travel_esteban_kylian.travelshare.repository.TravelShareRepository;

public class TravelShareActivity extends AppCompatActivity {

    private EditText searchEditText;
    private RecyclerView photoMetadataRecyclerView;
    private PhotoMetadataAdapter photoMetadataAdapter;
    private TravelShareRepository travelShareRepository;
    private TextView subtitleTextView;
    private Button createPhotoMetadataButton;
    private ImageView profileShortcutImageView;

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
        profileShortcutImageView = findViewById(R.id.ivProfileShortcut);

        photoMetadataAdapter = new PhotoMetadataAdapter(
                travelShareRepository,
                travelShareRepository.getPhotoMetadataList(),
                this::openPhotoMetadataDetails
        );

        photoMetadataRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        photoMetadataRecyclerView.setHasFixedSize(true);
        photoMetadataRecyclerView.setAdapter(photoMetadataAdapter);

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

        profileShortcutImageView.setOnClickListener(v ->
                startActivity(new Intent(this, ProfileActivity.class))
        );

        updateSubtitle();
    }

    @Override
    protected void onResume() {
        super.onResume();
        photoMetadataAdapter.submitPhotoMetadataList(travelShareRepository.getPhotoMetadataList());
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
}
