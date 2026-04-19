package com.kcorteel.travel_esteban_kylian;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.kcorteel.travel_esteban_kylian.travelshare.adapter.CommentAdapter;
import com.kcorteel.travel_esteban_kylian.travelshare.model.Location;
import com.kcorteel.travel_esteban_kylian.travelshare.model.PhotoMetadata;
import com.kcorteel.travel_esteban_kylian.travelshare.repository.TravelShareRepository;

import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;

public class TravelShareDetailActivity extends AppCompatActivity {

    public static final String EXTRA_PHOTO_ID = "extra_photo_id";

    private ImageView mediaImageView;
    private TextView titleTextView;
    private TextView authorTextView;
    private TextView locationTextView;
    private TextView dateTextView;
    private TextView descriptionTextView;
    private TextView tagsTextView;
    private TextView routeAdviceTextView;
    private TextView commentsCountTextView;
    private Button likeButton;
    private Button reportButton;
    private Button directionsButton;
    private Button addCommentButton;
    private EditText commentEditText;
    private RecyclerView commentsRecyclerView;

    private TravelShareRepository travelShareRepository;
    private CommentAdapter commentAdapter;
    private PhotoMetadata photoMetadata;
    private long photoId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_travel_share_detail);

        travelShareRepository = TravelShareRepository.getInstance(this);
        photoId = getIntent().getLongExtra(EXTRA_PHOTO_ID, -1L);
        photoMetadata = travelShareRepository.getPhotoMetadataById(photoId);

        if (photoMetadata == null) {
            Toast.makeText(this, R.string.travelshare_post_missing, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        bindViews();
        setupCommentsRecyclerView();
        bindPhotoMetadata();
        setupActions();
    }

    @Override
    protected void onResume() {
        super.onResume();
        photoMetadata = travelShareRepository.getPhotoMetadataById(photoId);
        if (photoMetadata != null) {
            bindPhotoMetadata();
        }
    }

    private void bindViews() {
        mediaImageView = findViewById(R.id.ivDetailPhotoMetadataMedia);
        titleTextView = findViewById(R.id.tvDetailPhotoMetadataTitle);
        authorTextView = findViewById(R.id.tvDetailPhotoMetadataAuthor);
        locationTextView = findViewById(R.id.tvDetailPhotoMetadataLocation);
        dateTextView = findViewById(R.id.tvDetailPhotoMetadataDate);
        descriptionTextView = findViewById(R.id.tvDetailPhotoMetadataDescription);
        tagsTextView = findViewById(R.id.tvDetailPhotoMetadataTags);
        routeAdviceTextView = findViewById(R.id.tvDetailPhotoMetadataRouteAdvice);
        commentsCountTextView = findViewById(R.id.tvCommentsCount);
        likeButton = findViewById(R.id.btnLikePhotoMetadata);
        reportButton = findViewById(R.id.btnReportPhotoMetadata);
        directionsButton = findViewById(R.id.btnOpenDirections);
        addCommentButton = findViewById(R.id.btnAddComment);
        commentEditText = findViewById(R.id.etAddComment);
        commentsRecyclerView = findViewById(R.id.rvComments);
    }

    private void setupCommentsRecyclerView() {
        commentAdapter = new CommentAdapter(travelShareRepository);
        commentsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        commentsRecyclerView.setNestedScrollingEnabled(false);
        commentsRecyclerView.setAdapter(commentAdapter);
    }

    private void bindPhotoMetadata() {
        if (photoMetadata == null) {
            return;
        }

        mediaImageView.setImageResource(travelShareRepository.resolveMediaResourceId(this, photoMetadata));
        titleTextView.setText(photoMetadata.getTitle());
        authorTextView.setText(getString(
                R.string.travelshare_author_format,
                travelShareRepository.getAuthorLabel(photoMetadata)
        ));
        locationTextView.setText(getString(
                R.string.travelshare_location_format,
                travelShareRepository.getLocationLabel(photoMetadata)
        ));
        dateTextView.setText(getString(
                R.string.travelshare_date_format,
                DateFormat.getDateInstance(DateFormat.LONG, Locale.FRANCE)
                        .format(new Date(photoMetadata.getTimestamp()))
        ));
        descriptionTextView.setText(photoMetadata.getDescription());
        tagsTextView.setText(getString(
                R.string.travelshare_tags_format,
                TextUtils.join(", ", photoMetadata.getTags())
        ));
        routeAdviceTextView.setText(travelShareRepository.getRouteAdvice(photoMetadata));

        int commentsCount = travelShareRepository.getCommentsForPhoto(photoId).size();
        commentsCountTextView.setText(getString(R.string.travelshare_comments_count_format, commentsCount));
        commentAdapter.submitComments(travelShareRepository.getCommentsForPhoto(photoId));

        updateLikeButton();
        updateReportButton();
        updateCommentInputState();
    }

    private void setupActions() {
        likeButton.setOnClickListener(v -> {
            boolean liked = travelShareRepository.toggleLike(photoId);
            updateLikeButton();

            int messageRes = liked
                    ? R.string.travelshare_liked_message
                    : R.string.travelshare_unliked_message;

            Toast.makeText(
                    TravelShareDetailActivity.this,
                    getString(messageRes, photoMetadata.getTitle()),
                    Toast.LENGTH_SHORT
            ).show();
        });

        reportButton.setOnClickListener(v -> {
            boolean reported = travelShareRepository.reportPhoto(photoId);
            updateReportButton();

            int messageRes = reported
                    ? R.string.travelshare_report_success
                    : R.string.travelshare_report_already_done;

            Toast.makeText(this, messageRes, Toast.LENGTH_SHORT).show();
        });

        directionsButton.setOnClickListener(v -> openDirections());

        addCommentButton.setOnClickListener(v -> addComment());
    }

    private void updateLikeButton() {
        boolean liked = travelShareRepository.isPhotoLikedByCurrentUser(photoId);
        int likeCount = travelShareRepository.getLikeCount(photoId);
        int labelRes = liked
                ? R.string.travelshare_unlike_button
                : R.string.travelshare_like_button;

        likeButton.setText(getString(labelRes) + " (" + likeCount + ")");
    }

    private void updateReportButton() {
        boolean reported = travelShareRepository.isPhotoReportedByCurrentUser(photoId);
        reportButton.setText(reported
                ? R.string.travelshare_reported_button
                : R.string.travelshare_report_button);
    }

    private void addComment() {
        if (travelShareRepository.isCurrentUserAnonymous()) {
            Toast.makeText(this, R.string.auth_comment_requires_login, Toast.LENGTH_SHORT).show();
            return;
        }

        String commentText = commentEditText.getText().toString().trim();
        if (commentText.isEmpty()) {
            Toast.makeText(this, R.string.travelshare_comment_empty_error, Toast.LENGTH_SHORT).show();
            return;
        }

        travelShareRepository.addComment(photoId, commentText);
        commentEditText.setText("");
        bindPhotoMetadata();
        Toast.makeText(this, R.string.travelshare_comment_added, Toast.LENGTH_SHORT).show();
    }

    private void updateCommentInputState() {
        boolean anonymous = travelShareRepository.isCurrentUserAnonymous();
        commentEditText.setEnabled(!anonymous);
        addCommentButton.setEnabled(!anonymous);
        commentEditText.setHint(anonymous
                ? getString(R.string.auth_comment_requires_login)
                : getString(R.string.travelshare_comment_hint));
    }

    private void openDirections() {
        Location location = travelShareRepository.getLocationById(photoMetadata.getLocationId());
        if (location == null) {
            Toast.makeText(this, R.string.travelshare_no_map_app, Toast.LENGTH_SHORT).show();
            return;
        }

        String query = location.getLatitude() + "," + location.getLongitude()
                + "(" + Uri.encode(photoMetadata.getTitle()) + ")";
        Uri geoUri = Uri.parse("geo:" + location.getLatitude() + "," + location.getLongitude()
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
