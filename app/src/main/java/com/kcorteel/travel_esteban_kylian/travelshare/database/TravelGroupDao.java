package com.kcorteel.travel_esteban_kylian.travelshare.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.kcorteel.travel_esteban_kylian.travelshare.model.TravelGroup;

import java.util.List;

@Dao
public interface TravelGroupDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<TravelGroup> groups);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(TravelGroup group);

    @Query("SELECT * FROM travel_groups ORDER BY groupName COLLATE NOCASE ASC")
    List<TravelGroup> getAll();

    @Query("SELECT * FROM travel_groups WHERE groupId = :groupId LIMIT 1")
    TravelGroup getById(long groupId);

    @Query("SELECT COALESCE(MAX(groupId), 0) FROM travel_groups")
    long getMaxGroupId();
}
