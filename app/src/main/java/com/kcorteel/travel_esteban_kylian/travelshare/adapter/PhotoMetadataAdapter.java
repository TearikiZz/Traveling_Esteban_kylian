package com.kcorteel.travel_esteban_kylian.travelshare.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.kcorteel.travel_esteban_kylian.R;
import com.kcorteel.travel_esteban_kylian.travelshare.model.PhotoMetadata;
import com.kcorteel.travel_esteban_kylian.travelshare.repository.TravelShareRepository;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class PhotoMetadataAdapter extends RecyclerView.Adapter<PhotoMetadataAdapter.PhotoMetadataViewHolder> {

    public interface OnItemClickListener {
        void onItemClick(PhotoMetadata photoMetadata);
    }

    private final TravelShareRepository repository;
    private final OnItemClickListener onItemClickListener;
    private final List<PhotoMetadata> allPhotoMetadata;
    private final List<PhotoMetadata> visiblePhotoMetadata;
    private final DateFormat dateFormat;

    public PhotoMetadataAdapter(
            TravelShareRepository repository,
            List<PhotoMetadata> photoMetadataList,
            OnItemClickListener onItemClickListener
    ) {
        this.repository = repository;
        this.onItemClickListener = onItemClickListener;
        this.allPhotoMetadata = new ArrayList<>(photoMetadataList);
        this.visiblePhotoMetadata = new ArrayList<>(photoMetadataList);
        this.dateFormat = DateFormat.getDateInstance(DateFormat.LONG, Locale.FRANCE);
    }

    @NonNull
    @Override
    public PhotoMetadataViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_photo_metadata, parent, false);
        return new PhotoMetadataViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PhotoMetadataViewHolder holder, int position) {
        holder.bind(visiblePhotoMetadata.get(position));
    }

    @Override
    public int getItemCount() {
        return visiblePhotoMetadata.size();
    }

    public void filter(String query) {
        String normalizedQuery = query == null ? "" : query.trim().toLowerCase(Locale.getDefault());
        visiblePhotoMetadata.clear();

        if (normalizedQuery.isEmpty()) {
            visiblePhotoMetadata.addAll(allPhotoMetadata);
        } else {
            for (PhotoMetadata photoMetadata : allPhotoMetadata) {
                if (repository.getSearchableText(photoMetadata).contains(normalizedQuery)) {
                    visiblePhotoMetadata.add(photoMetadata);
                }
            }
        }

        notifyDataSetChanged();
    }

    class PhotoMetadataViewHolder extends RecyclerView.ViewHolder {

        private final ImageView photoImageView;
        private final TextView titleTextView;
        private final TextView locationTextView;
        private final TextView dateTextView;
        private final TextView descriptionTextView;
        private final TextView infoTextView;

        PhotoMetadataViewHolder(@NonNull View itemView) {
            super(itemView);
            photoImageView = itemView.findViewById(R.id.ivPhotoMetadataMedia);
            titleTextView = itemView.findViewById(R.id.tvPhotoMetadataTitle);
            locationTextView = itemView.findViewById(R.id.tvPhotoMetadataLocation);
            dateTextView = itemView.findViewById(R.id.tvPhotoMetadataDate);
            descriptionTextView = itemView.findViewById(R.id.tvPhotoMetadataDescription);
            infoTextView = itemView.findViewById(R.id.tvPhotoMetadataInfo);
        }

        void bind(final PhotoMetadata photoMetadata) {
            photoImageView.setImageResource(repository.resolveMediaResourceId(itemView.getContext(), photoMetadata));
            titleTextView.setText(photoMetadata.getTitle());
            locationTextView.setText(repository.getLocationLabel(photoMetadata));
            dateTextView.setText(dateFormat.format(new Date(photoMetadata.getTimestamp())));
            descriptionTextView.setText(photoMetadata.getDescription());
            infoTextView.setText(itemView.getContext().getString(
                    R.string.travelshare_feed_info_format,
                    photoMetadata.getPlaceType().name(),
                    repository.getLikeCount(photoMetadata.getPhotoId())
            ));

            itemView.setOnClickListener(v -> {
                if (onItemClickListener != null && getBindingAdapterPosition() != RecyclerView.NO_POSITION) {
                    onItemClickListener.onItemClick(photoMetadata);
                }
            });
        }
    }
}
