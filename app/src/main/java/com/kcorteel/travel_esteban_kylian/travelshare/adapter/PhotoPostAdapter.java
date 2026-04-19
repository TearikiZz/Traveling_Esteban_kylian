package com.kcorteel.travel_esteban_kylian.travelshare.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.kcorteel.travel_esteban_kylian.R;
import com.kcorteel.travel_esteban_kylian.travelshare.model.PhotoPost;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class PhotoPostAdapter extends RecyclerView.Adapter<PhotoPostAdapter.PhotoPostViewHolder> {

    public interface OnItemClickListener {
        void onItemClick(PhotoPost photoPost);
    }

    private final List<PhotoPost> allPosts;
    private final List<PhotoPost> visiblePosts;
    private final OnItemClickListener onItemClickListener;

    public PhotoPostAdapter(List<PhotoPost> posts, OnItemClickListener onItemClickListener) {
        this.allPosts = new ArrayList<>(posts);
        this.visiblePosts = new ArrayList<>(posts);
        this.onItemClickListener = onItemClickListener;
    }

    @NonNull
    @Override
    public PhotoPostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_photo_post, parent, false);
        return new PhotoPostViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PhotoPostViewHolder holder, int position) {
        PhotoPost photoPost = visiblePosts.get(position);
        holder.bind(photoPost, onItemClickListener);
    }

    @Override
    public int getItemCount() {
        return visiblePosts.size();
    }

    public void filter(String query) {
        String normalizedQuery = query == null ? "" : query.trim().toLowerCase(Locale.getDefault());
        visiblePosts.clear();

        if (normalizedQuery.isEmpty()) {
            visiblePosts.addAll(allPosts);
        } else {
            for (PhotoPost photoPost : allPosts) {
                if (matches(photoPost, normalizedQuery)) {
                    visiblePosts.add(photoPost);
                }
            }
        }

        notifyDataSetChanged();
    }

    private boolean matches(PhotoPost photoPost, String query) {
        return photoPost.getTitle().toLowerCase(Locale.getDefault()).contains(query)
                || photoPost.getLocation().toLowerCase(Locale.getDefault()).contains(query)
                || photoPost.getDate().toLowerCase(Locale.getDefault()).contains(query)
                || photoPost.getDescription().toLowerCase(Locale.getDefault()).contains(query)
                || photoPost.getRouteAdvice().toLowerCase(Locale.getDefault()).contains(query);
    }

    static class PhotoPostViewHolder extends RecyclerView.ViewHolder {

        private final ImageView photoImageView;
        private final TextView titleTextView;
        private final TextView locationTextView;
        private final TextView dateTextView;
        private final TextView descriptionTextView;

        PhotoPostViewHolder(@NonNull View itemView) {
            super(itemView);
            photoImageView = itemView.findViewById(R.id.ivPostPhoto);
            titleTextView = itemView.findViewById(R.id.tvPostTitle);
            locationTextView = itemView.findViewById(R.id.tvPostLocation);
            dateTextView = itemView.findViewById(R.id.tvPostDate);
            descriptionTextView = itemView.findViewById(R.id.tvPostDescription);
        }

        void bind(final PhotoPost photoPost, final OnItemClickListener onItemClickListener) {
            photoImageView.setImageResource(photoPost.getImageResId());
            titleTextView.setText(photoPost.getTitle());
            locationTextView.setText(photoPost.getLocation());
            dateTextView.setText(photoPost.getDate());
            descriptionTextView.setText(photoPost.getDescription());

            itemView.setOnClickListener(v -> {
                if (onItemClickListener != null && getBindingAdapterPosition() != RecyclerView.NO_POSITION) {
                    onItemClickListener.onItemClick(photoPost);
                }
            });
        }
    }
}
