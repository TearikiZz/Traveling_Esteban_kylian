package com.kcorteel.travel_esteban_kylian.travelshare.model;

public class Comment {

    private final long commentId;
    private final long photoId;
    private final long userId;
    private final String text;
    private final String audioUrl;
    private final long createdAt;

    public Comment(long commentId, long photoId, long userId, String text, String audioUrl, long createdAt) {
        this.commentId = commentId;
        this.photoId = photoId;
        this.userId = userId;
        this.text = text;
        this.audioUrl = audioUrl;
        this.createdAt = createdAt;
    }

    public long getCommentId() {
        return commentId;
    }

    public long getPhotoId() {
        return photoId;
    }

    public long getUserId() {
        return userId;
    }

    public String getText() {
        return text;
    }

    public String getAudioUrl() {
        return audioUrl;
    }

    public long getCreatedAt() {
        return createdAt;
    }
}
