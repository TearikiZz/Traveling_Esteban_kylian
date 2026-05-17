package com.kcorteel.travel_esteban_kylian.travelshare.model;

import androidx.room.Entity;

@Entity(tableName = "group_memberships", primaryKeys = {"groupId", "userId"})
public class GroupMembership {

    private final long groupId;
    private final long userId;

    public GroupMembership(long groupId, long userId) {
        this.groupId = groupId;
        this.userId = userId;
    }

    public long getGroupId() {
        return groupId;
    }

    public long getUserId() {
        return userId;
    }
}
