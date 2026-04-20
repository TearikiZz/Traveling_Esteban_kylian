package com.kcorteel.travel_esteban_kylian.travelshare.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "comments")
public class Comment {

    @PrimaryKey
    private long commentId;
    private long photoId;
    private long userId;
    private String text;
    private String audioUrl;
    private long createdAt;

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
