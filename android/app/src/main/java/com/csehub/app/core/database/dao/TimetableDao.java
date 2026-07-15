package com.csehub.app.core.database.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.csehub.app.core.database.entity.TimetableEntity;

import java.util.List;

@Dao
public interface TimetableDao {

    @Query("SELECT * FROM timetable WHERE academicYearId = :academicYearId AND sectionId = :sectionId AND day = :day ORDER BY period ASC")
    LiveData<List<TimetableEntity>> getTimetableForDay(String academicYearId, String sectionId, String day);

    @Query("SELECT * FROM timetable WHERE academicYearId = :academicYearId AND sectionId = :sectionId ORDER BY day, period ASC")
    LiveData<List<TimetableEntity>> getWeeklyTimetable(String academicYearId, String sectionId);

    @Query("SELECT * FROM timetable WHERE facultyName = :facultyName AND day = :day ORDER BY period ASC")
    LiveData<List<TimetableEntity>> getFacultyTimetableForDay(String facultyName, String day);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<TimetableEntity> timetable);

    @Query("DELETE FROM timetable")
    void deleteAll();
}
