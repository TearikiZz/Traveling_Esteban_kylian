package com.kcorteel.travel_esteban_kylian.travelshare.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.kcorteel.travel_esteban_kylian.travelshare.model.Location;

import java.util.List;

@Dao
public interface LocationDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<Location> locations);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Location location);

    @Query("SELECT * FROM locations WHERE locationId = :locationId LIMIT 1")
    Location getById(long locationId);

    @Query("SELECT COALESCE(MAX(locationId), 0) FROM locations")
    long getMaxLocationId();
}
