package com.kcorteel.travel_esteban_kylian.travelshare.model;

public class Notification {

    private final long notifId;
    private final long targetUserId;
    private final long relatedPhotoId;
    private final String message;
    private final NotificationTriggerType triggerType;
    private final boolean isRead;

    public Notification(
            long notifId,
            long targetUserId,
            long relatedPhotoId,
            String message,
            NotificationTriggerType triggerType,
            boolean isRead
    ) {
        this.notifId = notifId;
        this.targetUserId = targetUserId;
        this.relatedPhotoId = relatedPhotoId;
        this.message = message;
        this.triggerType = triggerType;
        this.isRead = isRead;
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
}
