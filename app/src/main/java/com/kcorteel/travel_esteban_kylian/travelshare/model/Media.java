package com.kcorteel.travel_esteban_kylian.travelshare.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "media")
public class Media {

    @PrimaryKey
    private long mediaId;
    private long ownerId;
    private String url;
    private MediaType type;
    private String thumbnailUrl;

    public Media(long mediaId, long ownerId, String url, MediaType type, String thumbnailUrl) {
        this.mediaId = mediaId;
        this.ownerId = ownerId;
        this.url = url;
        this.type = type;
        this.thumbnailUrl = thumbnailUrl;
    }

    public long getMediaId() {
        return mediaId;
    }

    public long getOwnerId() {
        return ownerId;
    }

    public String getUrl() {
        return url;
    }

    public MediaType getType() {
        return type;
    }

    public String getThumbnailUrl() {
        return thumbnailUrl;
    }
}
