package com.kcorteel.travel_esteban_kylian.travelshare.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Entity(tableName = "photo_metadata")
public class PhotoMetadata {

    @PrimaryKey
    private long photoId;
    private long authorId;
    private String title;
    private String description;
    private long timestamp;
    private long locationId;
    private long mediaId;
    private List<String> tags;
    private PlaceType placeType;

    public PhotoMetadata(
            long photoId,
            long authorId,
            String title,
            String description,
            long timestamp,
            long locationId,
            long mediaId,
            List<String> tags,
            PlaceType placeType
    ) {
        this.photoId = photoId;
        this.authorId = authorId;
        this.title = title;
        this.description = description;
        this.timestamp = timestamp;
        this.locationId = locationId;
        this.mediaId = mediaId;
        this.tags = new ArrayList<>(tags);
        this.placeType = placeType;
    }

    public long getPhotoId() {
        return photoId;
    }

    public long getAuthorId() {
        return authorId;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public long getLocationId() {
        return locationId;
    }

    public long getMediaId() {
        return mediaId;
    }

    public List<String> getTags() {
        return Collections.unmodifiableList(tags);
    }

    public PlaceType getPlaceType() {
        return placeType;
    }
}
