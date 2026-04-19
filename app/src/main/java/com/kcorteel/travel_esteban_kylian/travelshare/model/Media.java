package com.kcorteel.travel_esteban_kylian.travelshare.model;

public class Media {

    private final long mediaId;
    private final long ownerId;
    private final String url;
    private final MediaType type;
    private final String thumbnailUrl;

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
