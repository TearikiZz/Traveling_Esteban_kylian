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
    private final boolean isDelivered;
    private final long createdAt;

    public Notification(
            long notifId,
            long targetUserId,
            long relatedPhotoId,
            String message,
            NotificationTriggerType triggerType,
            boolean isRead,
            boolean isDelivered,
            long createdAt
    ) {
        this.notifId = notifId;
        this.targetUserId = targetUserId;
        this.relatedPhotoId = relatedPhotoId;
        this.message = message;
        this.triggerType = triggerType;
        this.isRead = isRead;
        this.isDelivered = isDelivered;
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

    public boolean isDelivered() {
        return isDelivered;
    }

    public long getCreatedAt() {
        return createdAt;
    }
}
