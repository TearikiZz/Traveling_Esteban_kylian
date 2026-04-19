package com.kcorteel.travel_esteban_kylian.travelshare.database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.kcorteel.travel_esteban_kylian.travelshare.model.SocialInteraction;
import com.kcorteel.travel_esteban_kylian.travelshare.model.SocialInteractionType;

import java.util.List;

@Dao
public interface SocialInteractionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<SocialInteraction> interactions);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(SocialInteraction socialInteraction);

    @Delete
    void delete(SocialInteraction socialInteraction);

    @Query("SELECT * FROM social_interactions WHERE userId = :userId AND targetId = :targetId AND type = :type LIMIT 1")
    SocialInteraction findInteraction(long userId, long targetId, SocialInteractionType type);

    @Query("SELECT COUNT(*) FROM social_interactions WHERE targetId = :targetId AND type = :type")
    int countByTargetAndType(long targetId, SocialInteractionType type);

    @Query("SELECT COUNT(*) FROM social_interactions WHERE type = 'LIKE' AND targetId IN (SELECT photoId FROM photo_metadata WHERE authorId = :authorId)")
    int countLikesReceivedByAuthor(long authorId);

    @Query("SELECT COALESCE(MAX(interactionId), 0) FROM social_interactions")
    long getMaxInteractionId();
}
