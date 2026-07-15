package com.csehub.app.core.database.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.csehub.app.core.database.entity.EventEntity;

import java.util.List;

@Dao
public interface EventDao {

    @Query("SELECT * FROM events ORDER BY date ASC")
    LiveData<List<EventEntity>> getAllEvents();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<EventEntity> events);

    @Query("DELETE FROM events")
    void deleteAll();

    @Query("DELETE FROM events WHERE id = :id")
    void deleteById(String id);
}
