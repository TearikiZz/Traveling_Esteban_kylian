package com.kcorteel.travel_esteban_kylian.travelshare.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.kcorteel.travel_esteban_kylian.travelshare.model.AppPreferences;

@Dao
public interface AppPreferencesDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(AppPreferences preferences);

    @Query("SELECT * FROM app_preferences WHERE userId = :userId LIMIT 1")
    AppPreferences getByUserId(long userId);
}
