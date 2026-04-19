package com.kcorteel.travel_esteban_kylian;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.kcorteel.travel_esteban_kylian.travelshare.model.PhotoPost;

public class TravelShareDetailActivity extends AppCompatActivity {

    public static final String EXTRA_PHOTO_POST = "extra_photo_post";

    private ImageView postImageView;
    private TextView titleTextView;
    private TextView locationTextView;
    private TextView dateTextView;
    private TextView descriptionTextView;
    private TextView routeAdviceTextView;
    private Button likeButton;
    private Button directionsButton;

    private PhotoPost photoPost;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_travel_share_detail);

        bindViews();
        readPost();

        if (photoPost == null) {
            Toast.makeText(this, R.string.travelshare_post_missing, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        bindPost();
        setupActions();
    }

    private void bindViews() {
        postImageView = findViewById(R.id.ivDetailPhoto);
        titleTextView = findViewById(R.id.tvDetailTitle);
        locationTextView = findViewById(R.id.tvDetailLocation);
        dateTextView = findViewById(R.id.tvDetailDate);
        descriptionTextView = findViewById(R.id.tvDetailDescription);
        routeAdviceTextView = findViewById(R.id.tvDetailRouteAdvice);
        likeButton = findViewById(R.id.btnLikePost);
        directionsButton = findViewById(R.id.btnOpenDirections);
    }

    private void readPost() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            photoPost = getIntent().getSerializableExtra(EXTRA_PHOTO_POST, PhotoPost.class);
            return;
        }

        Object extra = getIntent().getSerializableExtra(EXTRA_PHOTO_POST);
        if (extra instanceof PhotoPost) {
            photoPost = (PhotoPost) extra;
        }
    }

    private void bindPost() {
        postImageView.setImageResource(photoPost.getImageResId());
        titleTextView.setText(photoPost.getTitle());
        locationTextView.setText(getString(R.string.travelshare_location_format, photoPost.getLocation()));
        dateTextView.setText(getString(R.string.travelshare_date_format, photoPost.getDate()));
        descriptionTextView.setText(photoPost.getDescription());
        routeAdviceTextView.setText(photoPost.getRouteAdvice());
        updateLikeButton();
    }

    private void setupActions() {
        likeButton.setOnClickListener(v -> {
            photoPost.setLiked(!photoPost.isLiked());
            updateLikeButton();

            int messageRes = photoPost.isLiked()
                    ? R.string.travelshare_liked_message
                    : R.string.travelshare_unliked_message;

            Toast.makeText(
                    TravelShareDetailActivity.this,
                    getString(messageRes, photoPost.getTitle()),
                    Toast.LENGTH_SHORT
            ).show();
        });

        directionsButton.setOnClickListener(v -> openDirections());
    }

    private void updateLikeButton() {
        int labelRes = photoPost.isLiked()
                ? R.string.travelshare_unlike_button
                : R.string.travelshare_like_button;
        likeButton.setText(labelRes);
    }

    private void openDirections() {
        String query = photoPost.getLatitude() + "," + photoPost.getLongitude()
                + "(" + Uri.encode(photoPost.getTitle()) + ")";
        Uri geoUri = Uri.parse("geo:" + photoPost.getLatitude() + "," + photoPost.getLongitude()
                + "?q=" + query);

        Intent mapIntent = new Intent(Intent.ACTION_VIEW, geoUri);
        mapIntent.setPackage("com.google.android.apps.maps");

        if (mapIntent.resolveActivity(getPackageManager()) != null) {
            startActivity(mapIntent);
            return;
        }

        Intent fallbackIntent = new Intent(Intent.ACTION_VIEW, geoUri);
        if (fallbackIntent.resolveActivity(getPackageManager()) != null) {
            startActivity(fallbackIntent);
        } else {
            Toast.makeText(this, R.string.travelshare_no_map_app, Toast.LENGTH_SHORT).show();
        }
    }
}
