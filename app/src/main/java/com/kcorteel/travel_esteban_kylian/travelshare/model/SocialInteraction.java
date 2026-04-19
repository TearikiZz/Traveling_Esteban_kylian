package com.kcorteel.travel_esteban_kylian.travelshare.model;

public class SocialInteraction {

    private final long interactionId;
    private final long userId;
    private final long targetId;
    private final SocialInteractionType type;

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
