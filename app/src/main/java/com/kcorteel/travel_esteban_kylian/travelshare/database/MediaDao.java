package com.kcorteel.travel_esteban_kylian.travelshare.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.kcorteel.travel_esteban_kylian.travelshare.model.Media;

import java.util.List;

@Dao
public interface MediaDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<Media> mediaList);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Media media);

    @Query("SELECT * FROM media WHERE mediaId = :mediaId LIMIT 1")
    Media getById(long mediaId);

    @Query("SELECT COALESCE(MAX(mediaId), 0) FROM media")
    long getMaxMediaId();
}
