package com.csehub.app.core.database.dao;

import android.database.Cursor;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.SharedSQLiteStatement;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import com.csehub.app.core.database.entity.TimetableEntity;
import java.lang.Class;
import java.lang.Exception;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import javax.annotation.processing.Generated;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class TimetableDao_Impl implements TimetableDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<TimetableEntity> __insertionAdapterOfTimetableEntity;

  private final SharedSQLiteStatement __preparedStmtOfDeleteAll;

  public TimetableDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfTimetableEntity = new EntityInsertionAdapter<TimetableEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `timetable` (`id`,`year`,`section`,`day`,`period`,`subject`,`subjectCode`,`facultyName`,`room`,`startTime`,`endTime`,`type`) VALUES (?,?,?,?,?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          final TimetableEntity entity) {
        if (entity.getId() == null) {
          statement.bindNull(1);
        } else {
          statement.bindString(1, entity.getId());
        }
        if (entity.getYear() == null) {
          statement.bindNull(2);
        } else {
          statement.bindString(2, entity.getYear());
        }
        if (entity.getSection() == null) {
          statement.bindNull(3);
        } else {
          statement.bindString(3, entity.getSection());
        }
        if (entity.getDay() == null) {
          statement.bindNull(4);
        } else {
          statement.bindString(4, entity.getDay());
        }
        statement.bindLong(5, entity.getPeriod());
        if (entity.getSubject() == null) {
          statement.bindNull(6);
        } else {
          statement.bindString(6, entity.getSubject());
        }
        if (entity.getSubjectCode() == null) {
          statement.bindNull(7);
        } else {
          statement.bindString(7, entity.getSubjectCode());
        }
        if (entity.getFacultyName() == null) {
          statement.bindNull(8);
        } else {
          statement.bindString(8, entity.getFacultyName());
        }
        if (entity.getRoom() == null) {
          statement.bindNull(9);
        } else {
          statement.bindString(9, entity.getRoom());
        }
        if (entity.getStartTime() == null) {
          statement.bindNull(10);
        } else {
          statement.bindString(10, entity.getStartTime());
        }
        if (entity.getEndTime() == null) {
          statement.bindNull(11);
        } else {
          statement.bindString(11, entity.getEndTime());
        }
        if (entity.getType() == null) {
          statement.bindNull(12);
        } else {
          statement.bindString(12, entity.getType());
        }
      }
    };
    this.__preparedStmtOfDeleteAll = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM timetable";
        return _query;
      }
    };
  }

  @Override
  public void insertAll(final List<TimetableEntity> timetable) {
    __db.assertNotSuspendingTransaction();
    __db.beginTransaction();
    try {
      __insertionAdapterOfTimetableEntity.insert(timetable);
      __db.setTransactionSuccessful();
    } finally {
      __db.endTransaction();
    }
  }

  @Override
  public void deleteAll() {
    __db.assertNotSuspendingTransaction();
    final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteAll.acquire();
    try {
      __db.beginTransaction();
      try {
        _stmt.executeUpdateDelete();
        __db.setTransactionSuccessful();
      } finally {
        __db.endTransaction();
      }
    } finally {
      __preparedStmtOfDeleteAll.release(_stmt);
    }
  }

  @Override
  public LiveData<List<TimetableEntity>> getTimetableForDay(final String year, final String section,
      final String day) {
    final String _sql = "SELECT * FROM timetable WHERE year = ? AND section = ? AND day = ? ORDER BY period ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 3);
    int _argIndex = 1;
    if (year == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, year);
    }
    _argIndex = 2;
    if (section == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, section);
    }
    _argIndex = 3;
    if (day == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, day);
    }
    return __db.getInvalidationTracker().createLiveData(new String[] {"timetable"}, false, new Callable<List<TimetableEntity>>() {
      @Override
      @Nullable
      public List<TimetableEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfYear = CursorUtil.getColumnIndexOrThrow(_cursor, "year");
          final int _cursorIndexOfSection = CursorUtil.getColumnIndexOrThrow(_cursor, "section");
          final int _cursorIndexOfDay = CursorUtil.getColumnIndexOrThrow(_cursor, "day");
          final int _cursorIndexOfPeriod = CursorUtil.getColumnIndexOrThrow(_cursor, "period");
          final int _cursorIndexOfSubject = CursorUtil.getColumnIndexOrThrow(_cursor, "subject");
          final int _cursorIndexOfSubjectCode = CursorUtil.getColumnIndexOrThrow(_cursor, "subjectCode");
          final int _cursorIndexOfFacultyName = CursorUtil.getColumnIndexOrThrow(_cursor, "facultyName");
          final int _cursorIndexOfRoom = CursorUtil.getColumnIndexOrThrow(_cursor, "room");
          final int _cursorIndexOfStartTime = CursorUtil.getColumnIndexOrThrow(_cursor, "startTime");
          final int _cursorIndexOfEndTime = CursorUtil.getColumnIndexOrThrow(_cursor, "endTime");
          final int _cursorIndexOfType = CursorUtil.getColumnIndexOrThrow(_cursor, "type");
          final List<TimetableEntity> _result = new ArrayList<TimetableEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final TimetableEntity _item;
            _item = new TimetableEntity();
            final String _tmpId;
            if (_cursor.isNull(_cursorIndexOfId)) {
              _tmpId = null;
            } else {
              _tmpId = _cursor.getString(_cursorIndexOfId);
            }
            _item.setId(_tmpId);
            final String _tmpYear;
            if (_cursor.isNull(_cursorIndexOfYear)) {
              _tmpYear = null;
            } else {
              _tmpYear = _cursor.getString(_cursorIndexOfYear);
            }
            _item.setYear(_tmpYear);
            final String _tmpSection;
            if (_cursor.isNull(_cursorIndexOfSection)) {
              _tmpSection = null;
            } else {
              _tmpSection = _cursor.getString(_cursorIndexOfSection);
            }
            _item.setSection(_tmpSection);
            final String _tmpDay;
            if (_cursor.isNull(_cursorIndexOfDay)) {
              _tmpDay = null;
            } else {
              _tmpDay = _cursor.getString(_cursorIndexOfDay);
            }
            _item.setDay(_tmpDay);
            final int _tmpPeriod;
            _tmpPeriod = _cursor.getInt(_cursorIndexOfPeriod);
            _item.setPeriod(_tmpPeriod);
            final String _tmpSubject;
            if (_cursor.isNull(_cursorIndexOfSubject)) {
              _tmpSubject = null;
            } else {
              _tmpSubject = _cursor.getString(_cursorIndexOfSubject);
            }
            _item.setSubject(_tmpSubject);
            final String _tmpSubjectCode;
            if (_cursor.isNull(_cursorIndexOfSubjectCode)) {
              _tmpSubjectCode = null;
            } else {
              _tmpSubjectCode = _cursor.getString(_cursorIndexOfSubjectCode);
            }
            _item.setSubjectCode(_tmpSubjectCode);
            final String _tmpFacultyName;
            if (_cursor.isNull(_cursorIndexOfFacultyName)) {
              _tmpFacultyName = null;
            } else {
              _tmpFacultyName = _cursor.getString(_cursorIndexOfFacultyName);
            }
            _item.setFacultyName(_tmpFacultyName);
            final String _tmpRoom;
            if (_cursor.isNull(_cursorIndexOfRoom)) {
              _tmpRoom = null;
            } else {
              _tmpRoom = _cursor.getString(_cursorIndexOfRoom);
            }
            _item.setRoom(_tmpRoom);
            final String _tmpStartTime;
            if (_cursor.isNull(_cursorIndexOfStartTime)) {
              _tmpStartTime = null;
            } else {
              _tmpStartTime = _cursor.getString(_cursorIndexOfStartTime);
            }
            _item.setStartTime(_tmpStartTime);
            final String _tmpEndTime;
            if (_cursor.isNull(_cursorIndexOfEndTime)) {
              _tmpEndTime = null;
            } else {
              _tmpEndTime = _cursor.getString(_cursorIndexOfEndTime);
            }
            _item.setEndTime(_tmpEndTime);
            final String _tmpType;
            if (_cursor.isNull(_cursorIndexOfType)) {
              _tmpType = null;
            } else {
              _tmpType = _cursor.getString(_cursorIndexOfType);
            }
            _item.setType(_tmpType);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public LiveData<List<TimetableEntity>> getWeeklyTimetable(final String year,
      final String section) {
    final String _sql = "SELECT * FROM timetable WHERE year = ? AND section = ? ORDER BY day, period ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 2);
    int _argIndex = 1;
    if (year == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, year);
    }
    _argIndex = 2;
    if (section == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, section);
    }
    return __db.getInvalidationTracker().createLiveData(new String[] {"timetable"}, false, new Callable<List<TimetableEntity>>() {
      @Override
      @Nullable
      public List<TimetableEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfYear = CursorUtil.getColumnIndexOrThrow(_cursor, "year");
          final int _cursorIndexOfSection = CursorUtil.getColumnIndexOrThrow(_cursor, "section");
          final int _cursorIndexOfDay = CursorUtil.getColumnIndexOrThrow(_cursor, "day");
          final int _cursorIndexOfPeriod = CursorUtil.getColumnIndexOrThrow(_cursor, "period");
          final int _cursorIndexOfSubject = CursorUtil.getColumnIndexOrThrow(_cursor, "subject");
          final int _cursorIndexOfSubjectCode = CursorUtil.getColumnIndexOrThrow(_cursor, "subjectCode");
          final int _cursorIndexOfFacultyName = CursorUtil.getColumnIndexOrThrow(_cursor, "facultyName");
          final int _cursorIndexOfRoom = CursorUtil.getColumnIndexOrThrow(_cursor, "room");
          final int _cursorIndexOfStartTime = CursorUtil.getColumnIndexOrThrow(_cursor, "startTime");
          final int _cursorIndexOfEndTime = CursorUtil.getColumnIndexOrThrow(_cursor, "endTime");
          final int _cursorIndexOfType = CursorUtil.getColumnIndexOrThrow(_cursor, "type");
          final List<TimetableEntity> _result = new ArrayList<TimetableEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final TimetableEntity _item;
            _item = new TimetableEntity();
            final String _tmpId;
            if (_cursor.isNull(_cursorIndexOfId)) {
              _tmpId = null;
            } else {
              _tmpId = _cursor.getString(_cursorIndexOfId);
            }
            _item.setId(_tmpId);
            final String _tmpYear;
            if (_cursor.isNull(_cursorIndexOfYear)) {
              _tmpYear = null;
            } else {
              _tmpYear = _cursor.getString(_cursorIndexOfYear);
            }
            _item.setYear(_tmpYear);
            final String _tmpSection;
            if (_cursor.isNull(_cursorIndexOfSection)) {
              _tmpSection = null;
            } else {
              _tmpSection = _cursor.getString(_cursorIndexOfSection);
            }
            _item.setSection(_tmpSection);
            final String _tmpDay;
            if (_cursor.isNull(_cursorIndexOfDay)) {
              _tmpDay = null;
            } else {
              _tmpDay = _cursor.getString(_cursorIndexOfDay);
            }
            _item.setDay(_tmpDay);
            final int _tmpPeriod;
            _tmpPeriod = _cursor.getInt(_cursorIndexOfPeriod);
            _item.setPeriod(_tmpPeriod);
            final String _tmpSubject;
            if (_cursor.isNull(_cursorIndexOfSubject)) {
              _tmpSubject = null;
            } else {
              _tmpSubject = _cursor.getString(_cursorIndexOfSubject);
            }
            _item.setSubject(_tmpSubject);
            final String _tmpSubjectCode;
            if (_cursor.isNull(_cursorIndexOfSubjectCode)) {
              _tmpSubjectCode = null;
            } else {
              _tmpSubjectCode = _cursor.getString(_cursorIndexOfSubjectCode);
            }
            _item.setSubjectCode(_tmpSubjectCode);
            final String _tmpFacultyName;
            if (_cursor.isNull(_cursorIndexOfFacultyName)) {
              _tmpFacultyName = null;
            } else {
              _tmpFacultyName = _cursor.getString(_cursorIndexOfFacultyName);
            }
            _item.setFacultyName(_tmpFacultyName);
            final String _tmpRoom;
            if (_cursor.isNull(_cursorIndexOfRoom)) {
              _tmpRoom = null;
            } else {
              _tmpRoom = _cursor.getString(_cursorIndexOfRoom);
            }
            _item.setRoom(_tmpRoom);
            final String _tmpStartTime;
            if (_cursor.isNull(_cursorIndexOfStartTime)) {
              _tmpStartTime = null;
            } else {
              _tmpStartTime = _cursor.getString(_cursorIndexOfStartTime);
            }
            _item.setStartTime(_tmpStartTime);
            final String _tmpEndTime;
            if (_cursor.isNull(_cursorIndexOfEndTime)) {
              _tmpEndTime = null;
            } else {
              _tmpEndTime = _cursor.getString(_cursorIndexOfEndTime);
            }
            _item.setEndTime(_tmpEndTime);
            final String _tmpType;
            if (_cursor.isNull(_cursorIndexOfType)) {
              _tmpType = null;
            } else {
              _tmpType = _cursor.getString(_cursorIndexOfType);
            }
            _item.setType(_tmpType);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public LiveData<List<TimetableEntity>> getFacultyTimetableForDay(final String facultyName,
      final String day) {
    final String _sql = "SELECT * FROM timetable WHERE facultyName = ? AND day = ? ORDER BY period ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 2);
    int _argIndex = 1;
    if (facultyName == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, facultyName);
    }
    _argIndex = 2;
    if (day == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, day);
    }
    return __db.getInvalidationTracker().createLiveData(new String[] {"timetable"}, false, new Callable<List<TimetableEntity>>() {
      @Override
      @Nullable
      public List<TimetableEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfYear = CursorUtil.getColumnIndexOrThrow(_cursor, "year");
          final int _cursorIndexOfSection = CursorUtil.getColumnIndexOrThrow(_cursor, "section");
          final int _cursorIndexOfDay = CursorUtil.getColumnIndexOrThrow(_cursor, "day");
          final int _cursorIndexOfPeriod = CursorUtil.getColumnIndexOrThrow(_cursor, "period");
          final int _cursorIndexOfSubject = CursorUtil.getColumnIndexOrThrow(_cursor, "subject");
          final int _cursorIndexOfSubjectCode = CursorUtil.getColumnIndexOrThrow(_cursor, "subjectCode");
          final int _cursorIndexOfFacultyName = CursorUtil.getColumnIndexOrThrow(_cursor, "facultyName");
          final int _cursorIndexOfRoom = CursorUtil.getColumnIndexOrThrow(_cursor, "room");
          final int _cursorIndexOfStartTime = CursorUtil.getColumnIndexOrThrow(_cursor, "startTime");
          final int _cursorIndexOfEndTime = CursorUtil.getColumnIndexOrThrow(_cursor, "endTime");
          final int _cursorIndexOfType = CursorUtil.getColumnIndexOrThrow(_cursor, "type");
          final List<TimetableEntity> _result = new ArrayList<TimetableEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final TimetableEntity _item;
            _item = new TimetableEntity();
            final String _tmpId;
            if (_cursor.isNull(_cursorIndexOfId)) {
              _tmpId = null;
            } else {
              _tmpId = _cursor.getString(_cursorIndexOfId);
            }
            _item.setId(_tmpId);
            final String _tmpYear;
            if (_cursor.isNull(_cursorIndexOfYear)) {
              _tmpYear = null;
            } else {
              _tmpYear = _cursor.getString(_cursorIndexOfYear);
            }
            _item.setYear(_tmpYear);
            final String _tmpSection;
            if (_cursor.isNull(_cursorIndexOfSection)) {
              _tmpSection = null;
            } else {
              _tmpSection = _cursor.getString(_cursorIndexOfSection);
            }
            _item.setSection(_tmpSection);
            final String _tmpDay;
            if (_cursor.isNull(_cursorIndexOfDay)) {
              _tmpDay = null;
            } else {
              _tmpDay = _cursor.getString(_cursorIndexOfDay);
            }
            _item.setDay(_tmpDay);
            final int _tmpPeriod;
            _tmpPeriod = _cursor.getInt(_cursorIndexOfPeriod);
            _item.setPeriod(_tmpPeriod);
            final String _tmpSubject;
            if (_cursor.isNull(_cursorIndexOfSubject)) {
              _tmpSubject = null;
            } else {
              _tmpSubject = _cursor.getString(_cursorIndexOfSubject);
            }
            _item.setSubject(_tmpSubject);
            final String _tmpSubjectCode;
            if (_cursor.isNull(_cursorIndexOfSubjectCode)) {
              _tmpSubjectCode = null;
            } else {
              _tmpSubjectCode = _cursor.getString(_cursorIndexOfSubjectCode);
            }
            _item.setSubjectCode(_tmpSubjectCode);
            final String _tmpFacultyName;
            if (_cursor.isNull(_cursorIndexOfFacultyName)) {
              _tmpFacultyName = null;
            } else {
              _tmpFacultyName = _cursor.getString(_cursorIndexOfFacultyName);
            }
            _item.setFacultyName(_tmpFacultyName);
            final String _tmpRoom;
            if (_cursor.isNull(_cursorIndexOfRoom)) {
              _tmpRoom = null;
            } else {
              _tmpRoom = _cursor.getString(_cursorIndexOfRoom);
            }
            _item.setRoom(_tmpRoom);
            final String _tmpStartTime;
            if (_cursor.isNull(_cursorIndexOfStartTime)) {
              _tmpStartTime = null;
            } else {
              _tmpStartTime = _cursor.getString(_cursorIndexOfStartTime);
            }
            _item.setStartTime(_tmpStartTime);
            final String _tmpEndTime;
            if (_cursor.isNull(_cursorIndexOfEndTime)) {
              _tmpEndTime = null;
            } else {
              _tmpEndTime = _cursor.getString(_cursorIndexOfEndTime);
            }
            _item.setEndTime(_tmpEndTime);
            final String _tmpType;
            if (_cursor.isNull(_cursorIndexOfType)) {
              _tmpType = null;
            } else {
              _tmpType = _cursor.getString(_cursorIndexOfType);
            }
            _item.setType(_tmpType);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
