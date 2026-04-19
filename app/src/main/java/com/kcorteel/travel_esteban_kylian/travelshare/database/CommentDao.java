package com.kcorteel.travel_esteban_kylian.travelshare.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.kcorteel.travel_esteban_kylian.travelshare.model.Comment;

import java.util.List;

@Dao
public interface CommentDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<Comment> comments);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Comment comment);

    @Query("SELECT * FROM comments WHERE photoId = :photoId ORDER BY createdAt ASC")
    List<Comment> getByPhotoId(long photoId);

    @Query("SELECT COUNT(*) FROM comments WHERE photoId = :photoId")
    int countByPhotoId(long photoId);

    @Query("SELECT COALESCE(MAX(commentId), 0) FROM comments")
    long getMaxCommentId();
}
