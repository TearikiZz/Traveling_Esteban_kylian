package com.kcorteel.travel_esteban_kylian.travelshare.model;

public class TravelGroup {

    private final long groupId;
    private final String groupName;
    private final long creatorId;
    private final boolean isPrivate;

    public TravelGroup(long groupId, String groupName, long creatorId, boolean isPrivate) {
        this.groupId = groupId;
        this.groupName = groupName;
        this.creatorId = creatorId;
        this.isPrivate = isPrivate;
    }

    public long getGroupId() {
        return groupId;
    }

    public String getGroupName() {
        return groupName;
    }

    public long getCreatorId() {
        return creatorId;
    }

    public boolean isPrivate() {
        return isPrivate;
    }
}
