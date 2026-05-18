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
import com.kcorteel.travel_esteban_kylian.travelshare.model.PlaceType;
import com.kcorteel.travel_esteban_kylian.travelshare.repository.TravelShareRepository;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class PhotoMetadataAdapter extends RecyclerView.Adapter<PhotoMetadataAdapter.PhotoMetadataViewHolder> {

    public enum DisplayMode {
        LIST,
        GRID
    }

    public enum PeriodFilter {
        ALL,
        LAST_30_DAYS,
        LAST_6_MONTHS,
        OLDER
    }

    public interface OnItemClickListener {
        void onItemClick(PhotoMetadata photoMetadata);
    }

    private final TravelShareRepository repository;
    private final OnItemClickListener onItemClickListener;
    private final List<PhotoMetadata> allPhotoMetadata;
    private final List<PhotoMetadata> visiblePhotoMetadata;
    private final DateFormat dateFormat;
    private String currentQuery = "";
    private String currentAuthor = "";
    private PlaceType currentPlaceType;
    private PeriodFilter currentPeriodFilter = PeriodFilter.ALL;
    private DisplayMode displayMode = DisplayMode.LIST;

    public PhotoMetadataAdapter(
            TravelShareRepository repository,
            List<PhotoMetadata> photoMetadataList,
            OnItemClickListener onItemClickListener
    ) {
        this.repository = repository;
        this.onItemClickListener = onItemClickListener;
        this.allPhotoMetadata = new ArrayList<>(photoMetadataList);
        this.visiblePhotoMetadata = new ArrayList<>(photoMetadataList);
        this.dateFormat = DateFormat.getDateInstance(DateFormat.LONG, repository.getCurrentLocale());
    }

    @NonNull
    @Override
    public PhotoMetadataViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(viewType == DisplayMode.GRID.ordinal()
                        ? R.layout.item_photo_metadata_grid
                        : R.layout.item_photo_metadata, parent, false);
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

    @Override
    public int getItemViewType(int position) {
        return displayMode.ordinal();
    }

    public void filter(String query) {
        currentQuery = query == null ? "" : query.trim().toLowerCase(Locale.getDefault());
        applyFilters();
    }

    public void setSelectedPlaceType(PlaceType placeType) {
        currentPlaceType = placeType;
        applyFilters();
    }

    public void setSelectedAuthor(String author) {
        currentAuthor = author == null ? "" : author.trim().toLowerCase(Locale.getDefault());
        applyFilters();
    }

    public void setSelectedPeriod(PeriodFilter periodFilter) {
        currentPeriodFilter = periodFilter == null ? PeriodFilter.ALL : periodFilter;
        applyFilters();
    }

    public void resetFilters() {
        currentQuery = "";
        currentAuthor = "";
        currentPlaceType = null;
        currentPeriodFilter = PeriodFilter.ALL;
        applyFilters();
    }

    public void submitPhotoMetadataList(List<PhotoMetadata> photoMetadataList) {
        allPhotoMetadata.clear();
        allPhotoMetadata.addAll(photoMetadataList);
        applyFilters();
    }

    public void setDisplayMode(DisplayMode displayMode) {
        DisplayMode resolvedMode = displayMode == null ? DisplayMode.LIST : displayMode;
        if (this.displayMode == resolvedMode) {
            return;
        }
        this.displayMode = resolvedMode;
        notifyDataSetChanged();
    }

    private void applyFilters() {
        visiblePhotoMetadata.clear();

        for (PhotoMetadata photoMetadata : allPhotoMetadata) {
            if (!matchesQuery(photoMetadata)) {
                continue;
            }
            if (!matchesPlaceType(photoMetadata)) {
                continue;
            }
            if (!matchesAuthor(photoMetadata)) {
                continue;
            }
            if (!matchesPeriod(photoMetadata)) {
                continue;
            }
            visiblePhotoMetadata.add(photoMetadata);
        }

        notifyDataSetChanged();
    }

    private boolean matchesQuery(PhotoMetadata photoMetadata) {
        return currentQuery.isEmpty()
                || repository.getSearchableText(photoMetadata).contains(currentQuery);
    }

    private boolean matchesPlaceType(PhotoMetadata photoMetadata) {
        return currentPlaceType == null || photoMetadata.getPlaceType() == currentPlaceType;
    }

    private boolean matchesAuthor(PhotoMetadata photoMetadata) {
        if (currentAuthor.isEmpty()) {
            return true;
        }

        String authorLabel = repository.getAuthorLabel(photoMetadata);
        return authorLabel != null
                && authorLabel.toLowerCase(Locale.getDefault()).contains(currentAuthor);
    }

    private boolean matchesPeriod(PhotoMetadata photoMetadata) {
        if (currentPeriodFilter == PeriodFilter.ALL) {
            return true;
        }

        long ageInMillis = System.currentTimeMillis() - photoMetadata.getTimestamp();
        long thirtyDaysInMillis = TimeUnit.DAYS.toMillis(30);
        long sixMonthsInMillis = TimeUnit.DAYS.toMillis(180);

        switch (currentPeriodFilter) {
            case LAST_30_DAYS:
                return ageInMillis <= thirtyDaysInMillis;
            case LAST_6_MONTHS:
                return ageInMillis <= sixMonthsInMillis;
            case OLDER:
                return ageInMillis > sixMonthsInMillis;
            case ALL:
            default:
                return true;
        }
    }

    class PhotoMetadataViewHolder extends RecyclerView.ViewHolder {

        private final ImageView photoImageView;
        private final TextView titleTextView;
        private final TextView groupTextView;
        private final TextView locationTextView;
        private final TextView dateTextView;
        private final TextView descriptionTextView;
        private final TextView infoTextView;

        PhotoMetadataViewHolder(@NonNull View itemView) {
            super(itemView);
            photoImageView = itemView.findViewById(R.id.ivPhotoMetadataMedia);
            titleTextView = itemView.findViewById(R.id.tvPhotoMetadataTitle);
            groupTextView = itemView.findViewById(R.id.tvPhotoMetadataGroup);
            locationTextView = itemView.findViewById(R.id.tvPhotoMetadataLocation);
            dateTextView = itemView.findViewById(R.id.tvPhotoMetadataDate);
            descriptionTextView = itemView.findViewById(R.id.tvPhotoMetadataDescription);
            infoTextView = itemView.findViewById(R.id.tvPhotoMetadataInfo);
        }

        void bind(final PhotoMetadata photoMetadata) {
            repository.loadMediaIntoImageView(itemView.getContext(), photoImageView, photoMetadata);
            titleTextView.setText(photoMetadata.getTitle());
            String groupLabel = repository.getGroupLabel(photoMetadata);
            if (groupLabel.isEmpty()) {
                groupTextView.setVisibility(View.GONE);
            } else {
                groupTextView.setVisibility(View.VISIBLE);
                groupTextView.setText(itemView.getContext().getString(
                        R.string.travelshare_post_group_format,
                        groupLabel
                ));
            }
            locationTextView.setText(repository.getLocationLabel(photoMetadata));
            dateTextView.setText(dateFormat.format(new Date(photoMetadata.getTimestamp())));
            descriptionTextView.setText(photoMetadata.getDescription());
            infoTextView.setText(itemView.getContext().getString(
                    R.string.travelshare_feed_info_format,
                    repository.getPlaceTypeLabel(photoMetadata.getPlaceType()),
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
