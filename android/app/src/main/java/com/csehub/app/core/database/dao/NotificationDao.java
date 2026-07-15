package com.csehub.app.core.database.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.csehub.app.core.database.entity.NotificationEntity;

import java.util.List;

@Dao
public interface NotificationDao {

    @Query("SELECT * FROM notifications ORDER BY createdAt DESC")
    LiveData<List<NotificationEntity>> getAllNotifications();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<NotificationEntity> notifications);

    @Query("DELETE FROM notifications")
    void deleteAll();

    @Query("DELETE FROM notifications WHERE id = :id")
    void deleteById(String id);

    @Query("UPDATE notifications SET isRead = 1 WHERE id = :id")
    void markAsRead(String id);
}
