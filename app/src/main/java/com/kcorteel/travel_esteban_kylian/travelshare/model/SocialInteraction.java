package com.kcorteel.travel_esteban_kylian.travelshare.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "social_interactions")
public class SocialInteraction {

    @PrimaryKey
    private long interactionId;
    private long userId;
    private long targetId;
    private SocialInteractionType type;

    public SocialInteraction(long interactionId, long userId, long targetId, SocialInteractionType type) {
        this.interactionId = interactionId;
        this.userId = userId;
        this.targetId = targetId;
        this.type = type;
    }

    public long getInteractionId() {
        return interactionId;
    }

    public long getUserId() {
        return userId;
    }

    public long getTargetId() {
        return targetId;
    }

    public SocialInteractionType getType() {
        return type;
    }
}
