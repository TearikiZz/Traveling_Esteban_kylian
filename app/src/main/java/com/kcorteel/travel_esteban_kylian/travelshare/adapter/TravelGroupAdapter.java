package com.kcorteel.travel_esteban_kylian.travelshare.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.kcorteel.travel_esteban_kylian.R;
import com.kcorteel.travel_esteban_kylian.travelshare.model.TravelGroup;
import com.kcorteel.travel_esteban_kylian.travelshare.repository.TravelShareRepository;

import java.util.ArrayList;
import java.util.List;

public class TravelGroupAdapter extends RecyclerView.Adapter<TravelGroupAdapter.TravelGroupViewHolder> {

    public interface OnGroupClickListener {
        void onOpenGroup(TravelGroup group);

        void onToggleMembership(TravelGroup group);
    }

    private final TravelShareRepository repository;
    private final OnGroupClickListener listener;
    private final List<TravelGroup> groups = new ArrayList<>();

    public TravelGroupAdapter(TravelShareRepository repository, OnGroupClickListener listener) {
        this.repository = repository;
        this.listener = listener;
    }

    @NonNull
    @Override
    public TravelGroupViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_travel_group, parent, false);
        return new TravelGroupViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TravelGroupViewHolder holder, int position) {
        holder.bind(groups.get(position));
    }

    @Override
    public int getItemCount() {
        return groups.size();
    }

    public void submitGroups(List<TravelGroup> travelGroups) {
        groups.clear();
        groups.addAll(travelGroups);
        notifyDataSetChanged();
    }

    class TravelGroupViewHolder extends RecyclerView.ViewHolder {

        private final TextView groupNameTextView;
        private final TextView visibilityTextView;
        private final TextView creatorTextView;
        private final TextView membersTextView;
        private final Button openGroupButton;
        private final Button membershipButton;

        TravelGroupViewHolder(@NonNull View itemView) {
            super(itemView);
            groupNameTextView = itemView.findViewById(R.id.tvGroupName);
            visibilityTextView = itemView.findViewById(R.id.tvGroupVisibility);
            creatorTextView = itemView.findViewById(R.id.tvGroupCreator);
            membersTextView = itemView.findViewById(R.id.tvGroupMembers);
            openGroupButton = itemView.findViewById(R.id.btnOpenGroup);
            membershipButton = itemView.findViewById(R.id.btnGroupMembership);
        }

        void bind(TravelGroup group) {
            groupNameTextView.setText(group.getGroupName());
            visibilityTextView.setText(group.isPrivate()
                    ? R.string.travelshare_group_visibility_private
                    : R.string.travelshare_group_visibility_public);
            creatorTextView.setText(itemView.getContext().getString(
                    R.string.travelshare_group_creator_format,
                    repository.getGroupCreatorLabel(group)
            ));
            membersTextView.setText(itemView.getContext().getString(
                    R.string.travelshare_group_members_format,
                    repository.getGroupMemberCount(group.getGroupId())
            ));

            openGroupButton.setOnClickListener(v -> listener.onOpenGroup(group));

            if (repository.isCurrentUserAnonymous()) {
                membershipButton.setText(R.string.travelshare_group_login_required);
                membershipButton.setEnabled(false);
            } else if (group.getCreatorId() == repository.getCurrentUser().getUserId()) {
                membershipButton.setText(R.string.travelshare_group_owned_button);
                membershipButton.setEnabled(false);
            } else {
                membershipButton.setEnabled(true);
                membershipButton.setText(repository.isCurrentUserMemberOfGroup(group.getGroupId())
                        ? R.string.travelshare_group_leave_button
                        : R.string.travelshare_group_join_button);
                membershipButton.setOnClickListener(v -> listener.onToggleMembership(group));
            }
        }
    }
}
