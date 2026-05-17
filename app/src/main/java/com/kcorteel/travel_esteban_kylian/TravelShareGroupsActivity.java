package com.kcorteel.travel_esteban_kylian;

import android.os.Bundle;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.kcorteel.travel_esteban_kylian.travelshare.adapter.TravelGroupAdapter;
import com.kcorteel.travel_esteban_kylian.travelshare.model.TravelGroup;
import com.kcorteel.travel_esteban_kylian.travelshare.repository.TravelShareRepository;

import android.content.Intent;
import android.view.View;

import java.util.List;

public class TravelShareGroupsActivity extends AppCompatActivity {

    private TravelShareRepository repository;
    private RecyclerView groupsRecyclerView;
    private TextView emptyStateTextView;
    private TextView subtitleTextView;
    private TravelGroupAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        repository = TravelShareRepository.getInstance(this);
        repository.applyCurrentUserThemePreference();
        setContentView(R.layout.activity_travel_share_groups);

        groupsRecyclerView = findViewById(R.id.rvTravelGroups);
        emptyStateTextView = findViewById(R.id.tvGroupsEmptyState);
        subtitleTextView = findViewById(R.id.tvGroupsSubtitle);

        adapter = new TravelGroupAdapter(repository, new TravelGroupAdapter.OnGroupClickListener() {
            @Override
            public void onOpenGroup(TravelGroup group) {
                openGroup(group);
            }

            @Override
            public void onToggleMembership(TravelGroup group) {
                toggleMembership(group);
            }
        });

        groupsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        groupsRecyclerView.setAdapter(adapter);

        findViewById(R.id.btnCreateGroup).setOnClickListener(v -> showCreateGroupDialog());
    }

    @Override
    protected void onResume() {
        super.onResume();
        bindGroups();
    }

    private void bindGroups() {
        boolean anonymous = repository.isCurrentUserAnonymous();
        subtitleTextView.setText(anonymous
                ? getString(R.string.travelshare_group_login_required)
                : getString(R.string.travelshare_groups_screen_subtitle));
        findViewById(R.id.btnCreateGroup).setVisibility(anonymous ? View.GONE : View.VISIBLE);

        List<TravelGroup> groups = repository.getVisibleGroups();
        adapter.submitGroups(groups);
        emptyStateTextView.setVisibility(groups.isEmpty() ? View.VISIBLE : View.GONE);
        groupsRecyclerView.setVisibility(groups.isEmpty() ? View.GONE : View.VISIBLE);
    }

    private void showCreateGroupDialog() {
        if (repository.isCurrentUserAnonymous()) {
            Toast.makeText(this, R.string.travelshare_group_login_required, Toast.LENGTH_SHORT).show();
            return;
        }

        LinearLayout container = new LinearLayout(this);
        container.setOrientation(LinearLayout.VERTICAL);
        int padding = (int) (20 * getResources().getDisplayMetrics().density);
        container.setPadding(padding, padding, padding, 0);

        EditText nameEditText = new EditText(this);
        nameEditText.setHint(R.string.travelshare_group_name_hint);
        container.addView(nameEditText);

        CheckBox privateCheckBox = new CheckBox(this);
        privateCheckBox.setText(R.string.travelshare_group_private_label);
        container.addView(privateCheckBox);

        new AlertDialog.Builder(this)
                .setTitle(R.string.travelshare_groups_create_button)
                .setView(container)
                .setPositiveButton(R.string.travelshare_groups_create_button, (dialog, which) -> {
                    String error = repository.createGroup(
                            nameEditText.getText().toString(),
                            privateCheckBox.isChecked()
                    );
                    if (error == null) {
                        Toast.makeText(this, R.string.travelshare_group_created, Toast.LENGTH_SHORT).show();
                        bindGroups();
                    } else {
                        Toast.makeText(this, error, Toast.LENGTH_LONG).show();
                    }
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    private void toggleMembership(TravelGroup group) {
        boolean success;
        if (repository.isCurrentUserMemberOfGroup(group.getGroupId())) {
            success = repository.leaveGroup(group.getGroupId());
            Toast.makeText(
                    this,
                    success ? R.string.travelshare_group_left : R.string.travelshare_group_action_failed,
                    Toast.LENGTH_SHORT
            ).show();
        } else {
            success = repository.joinGroup(group.getGroupId());
            Toast.makeText(
                    this,
                    success ? R.string.travelshare_group_joined : R.string.travelshare_group_action_failed,
                    Toast.LENGTH_SHORT
            ).show();
        }
        bindGroups();
    }

    private void openGroup(TravelGroup group) {
        Intent intent = new Intent(this, TravelShareGroupDetailActivity.class);
        intent.putExtra(TravelShareGroupDetailActivity.EXTRA_GROUP_ID, group.getGroupId());
        startActivity(intent);
    }
}
