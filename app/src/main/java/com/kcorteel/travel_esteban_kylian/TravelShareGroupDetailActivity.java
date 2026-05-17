package com.kcorteel.travel_esteban_kylian;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.kcorteel.travel_esteban_kylian.travelshare.adapter.PhotoMetadataAdapter;
import com.kcorteel.travel_esteban_kylian.travelshare.model.PhotoMetadata;
import com.kcorteel.travel_esteban_kylian.travelshare.model.TravelGroup;
import com.kcorteel.travel_esteban_kylian.travelshare.repository.TravelShareRepository;

import java.util.List;

public class TravelShareGroupDetailActivity extends AppCompatActivity {

    public static final String EXTRA_GROUP_ID = "extra_group_id";

    private TravelShareRepository repository;
    private long groupId;
    private TravelGroup group;
    private TextView titleTextView;
    private TextView metaTextView;
    private TextView emptyStateTextView;
    private Button membershipButton;
    private Button createPostButton;
    private RecyclerView postsRecyclerView;
    private PhotoMetadataAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        repository = TravelShareRepository.getInstance(this);
        repository.applyCurrentUserThemePreference();
        setContentView(R.layout.activity_travel_share_group_detail);

        groupId = getIntent().getLongExtra(EXTRA_GROUP_ID, -1L);

        titleTextView = findViewById(R.id.tvGroupDetailTitle);
        metaTextView = findViewById(R.id.tvGroupDetailMeta);
        emptyStateTextView = findViewById(R.id.tvGroupPostsEmptyState);
        membershipButton = findViewById(R.id.btnGroupMembershipAction);
        createPostButton = findViewById(R.id.btnCreatePostInGroup);
        postsRecyclerView = findViewById(R.id.rvGroupPosts);

        adapter = new PhotoMetadataAdapter(repository, repository.getPhotoMetadataForGroup(groupId), this::openPost);
        postsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        postsRecyclerView.setAdapter(adapter);

        membershipButton.setOnClickListener(v -> toggleMembership());
        createPostButton.setOnClickListener(v -> openCreatePost());
    }

    @Override
    protected void onResume() {
        super.onResume();
        bindGroup();
    }

    private void bindGroup() {
        group = repository.getGroupById(groupId);
        if (group == null) {
            Toast.makeText(this, R.string.travelshare_group_missing, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        titleTextView.setText(group.getGroupName());
        metaTextView.setText(
                repository.getGroupCreatorLabel(group)
                        + " • "
                        + getString(group.isPrivate()
                        ? R.string.travelshare_group_visibility_private
                        : R.string.travelshare_group_visibility_public)
                        + " • "
                        + getString(R.string.travelshare_group_members_format, repository.getGroupMemberCount(groupId))
        );

        boolean anonymous = repository.isCurrentUserAnonymous();
        boolean member = repository.isCurrentUserMemberOfGroup(groupId);
        boolean creator = !anonymous && repository.getCurrentUser().getUserId() == group.getCreatorId();

        if (anonymous) {
            membershipButton.setText(R.string.travelshare_group_login_required);
            membershipButton.setEnabled(false);
            createPostButton.setVisibility(View.GONE);
        } else if (creator) {
            membershipButton.setText(R.string.travelshare_group_owned_button);
            membershipButton.setEnabled(false);
            createPostButton.setVisibility(View.VISIBLE);
        } else if (member) {
            membershipButton.setText(R.string.travelshare_group_leave_button);
            membershipButton.setEnabled(true);
            createPostButton.setVisibility(View.VISIBLE);
        } else {
            membershipButton.setText(R.string.travelshare_group_join_button);
            membershipButton.setEnabled(true);
            createPostButton.setVisibility(View.GONE);
        }

        List<PhotoMetadata> posts = repository.getPhotoMetadataForGroup(groupId);
        adapter.submitPhotoMetadataList(posts);
        emptyStateTextView.setVisibility(posts.isEmpty() ? View.VISIBLE : View.GONE);
        postsRecyclerView.setVisibility(posts.isEmpty() ? View.GONE : View.VISIBLE);
    }

    private void toggleMembership() {
        boolean success;
        if (repository.isCurrentUserMemberOfGroup(groupId)) {
            success = repository.leaveGroup(groupId);
            Toast.makeText(
                    this,
                    success ? R.string.travelshare_group_left : R.string.travelshare_group_action_failed,
                    Toast.LENGTH_SHORT
            ).show();
        } else {
            success = repository.joinGroup(groupId);
            Toast.makeText(
                    this,
                    success ? R.string.travelshare_group_joined : R.string.travelshare_group_action_failed,
                    Toast.LENGTH_SHORT
            ).show();
        }

        if (success) {
            bindGroup();
        }
    }

    private void openCreatePost() {
        Intent intent = new Intent(this, CreatePhotoMetadataActivity.class);
        intent.putExtra(CreatePhotoMetadataActivity.EXTRA_PRESELECT_GROUP_ID, groupId);
        startActivity(intent);
    }

    private void openPost(PhotoMetadata photoMetadata) {
        Intent intent = new Intent(this, TravelShareDetailActivity.class);
        intent.putExtra(TravelShareDetailActivity.EXTRA_PHOTO_ID, photoMetadata.getPhotoId());
        startActivity(intent);
    }
}
