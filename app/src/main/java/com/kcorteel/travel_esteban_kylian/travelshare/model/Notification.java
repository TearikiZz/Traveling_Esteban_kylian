package com.kcorteel.travel_esteban_kylian.travelshare.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "notifications")
public class Notification {

    @PrimaryKey
    private final long notifId;
    private final long targetUserId;
    private final long relatedPhotoId;
    private final String message;
    private final NotificationTriggerType triggerType;
    private final boolean isRead;
    private final long createdAt;

    public Notification(
            long notifId,
            long targetUserId,
            long relatedPhotoId,
            String message,
            NotificationTriggerType triggerType,
            boolean isRead,
            long createdAt
    ) {
        this.notifId = notifId;
        this.targetUserId = targetUserId;
        this.relatedPhotoId = relatedPhotoId;
        this.message = message;
        this.triggerType = triggerType;
        this.isRead = isRead;
        this.createdAt = createdAt;
    }

    public long getNotifId() {
        return notifId;
    }

    public long getTargetUserId() {
        return targetUserId;
    }

    public long getRelatedPhotoId() {
        return relatedPhotoId;
    }

    public String getMessage() {
        return message;
    }

    public NotificationTriggerType getTriggerType() {
        return triggerType;
    }

    public boolean isRead() {
        return isRead;
    }

    public long getCreatedAt() {
        return createdAt;
    }
}
