package com.kcorteel.travel_esteban_kylian.travelshare.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.kcorteel.travel_esteban_kylian.travelshare.model.PhotoMetadata;

import java.util.List;

@Dao
public interface PhotoMetadataDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<PhotoMetadata> photoMetadataList);

    @Query("SELECT * FROM photo_metadata ORDER BY timestamp DESC")
    List<PhotoMetadata> getAll();

    @Query("SELECT * FROM photo_metadata WHERE photoId = :photoId LIMIT 1")
    PhotoMetadata getById(long photoId);

    @Query("SELECT COUNT(*) FROM photo_metadata")
    int countAll();
}
