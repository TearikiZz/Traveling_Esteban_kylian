package com.kcorteel.travel_esteban_kylian.travelshare.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.kcorteel.travel_esteban_kylian.travelshare.model.Notification;

import java.util.List;

@Dao
public interface NotificationDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Notification notification);

    @Query("SELECT * FROM notifications WHERE targetUserId = :userId ORDER BY createdAt DESC")
    List<Notification> getByTargetUserId(long userId);

    @Query("SELECT * FROM notifications WHERE targetUserId = :userId AND isDelivered = 0 ORDER BY createdAt DESC")
    List<Notification> getUndeliveredByTargetUserId(long userId);

    @Query("SELECT COUNT(*) FROM notifications WHERE targetUserId = :userId AND isRead = 0")
    int countUnreadByTargetUserId(long userId);

    @Query("UPDATE notifications SET isRead = 1 WHERE targetUserId = :userId AND isRead = 0")
    void markAllAsRead(long userId);

    @Query("UPDATE notifications SET isDelivered = 1 WHERE notifId = :notificationId")
    void markAsDelivered(long notificationId);

    @Query("SELECT COALESCE(MAX(notifId), 0) FROM notifications")
    long getMaxNotificationId();

    @Query("DELETE FROM notifications WHERE relatedPhotoId = :photoId")
    void deleteByRelatedPhotoId(long photoId);
}
