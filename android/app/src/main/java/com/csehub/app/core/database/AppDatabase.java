package com.csehub.app.core.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.csehub.app.core.database.dao.EventDao;
import com.csehub.app.core.database.dao.NotificationDao;
import com.csehub.app.core.database.dao.TimetableDao;
import com.csehub.app.core.database.entity.EventEntity;
import com.csehub.app.core.database.entity.NotificationEntity;
import com.csehub.app.core.database.entity.TimetableEntity;

@Database(entities = {
        NotificationEntity.class,
        EventEntity.class,
        TimetableEntity.class
}, version = 1, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    private static volatile AppDatabase instance;

    public abstract NotificationDao notificationDao();
    public abstract EventDao eventDao();
    public abstract TimetableDao timetableDao();

    public static AppDatabase getInstance(Context context) {
        if (instance == null) {
            synchronized (AppDatabase.class) {
                if (instance == null) {
                    instance = Room.databaseBuilder(
                            context.getApplicationContext(),
                            AppDatabase.class,
                            "cse_hub_database"
                    )
                    .fallbackToDestructiveMigration()
                    .build();
                }
            }
        }
        return instance;
    }
}
