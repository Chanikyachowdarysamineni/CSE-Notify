package com.csehub.app.timetable.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.csehub.app.auth.data.AuthRepository;
import com.csehub.app.core.database.entity.TimetableEntity;
import com.csehub.app.core.network.models.Timetable;
import com.csehub.app.timetable.data.TimetableRepository;

import java.util.List;

import okhttp3.MultipartBody;

public class TimetableViewModel extends AndroidViewModel {

    private final TimetableRepository repository;

    public TimetableViewModel(@NonNull Application application) {
        super(application);
        this.repository = new TimetableRepository(application);
    }

    public LiveData<List<TimetableEntity>> getOfflineTimetableForDay(String academicYearId, String sectionId, String day) {
        return repository.getOfflineTimetableForDay(academicYearId, sectionId, day);
    }

    public LiveData<List<TimetableEntity>> getFacultyOfflineTimetableForDay(String facultyName, String day) {
        return repository.getFacultyOfflineTimetableForDay(facultyName, day);
    }

    public LiveData<AuthRepository.Resource<List<Timetable>>> getDailyTimetable(
            String academicYearId, String sectionId, String day) {
        return repository.getDailyTimetable(academicYearId, sectionId, day);
    }

    public LiveData<AuthRepository.Resource<Void>> importCSV(MultipartBody.Part file) {
        return repository.importCSV(file);
    }
}
