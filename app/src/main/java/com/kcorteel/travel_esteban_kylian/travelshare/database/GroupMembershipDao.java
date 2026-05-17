package com.kcorteel.travel_esteban_kylian.travelshare.database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.kcorteel.travel_esteban_kylian.travelshare.model.GroupMembership;

import java.util.List;

@Dao
public interface GroupMembershipDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<GroupMembership> memberships);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(GroupMembership membership);

    @Delete
    void delete(GroupMembership membership);

    @Query("SELECT * FROM group_memberships WHERE groupId = :groupId AND userId = :userId LIMIT 1")
    GroupMembership getByGroupIdAndUserId(long groupId, long userId);

    @Query("SELECT * FROM group_memberships WHERE groupId = :groupId")
    List<GroupMembership> getByGroupId(long groupId);

    @Query("SELECT * FROM group_memberships WHERE userId = :userId")
    List<GroupMembership> getByUserId(long userId);

    @Query("SELECT COUNT(*) FROM group_memberships WHERE groupId = :groupId")
    int countByGroupId(long groupId);
}
