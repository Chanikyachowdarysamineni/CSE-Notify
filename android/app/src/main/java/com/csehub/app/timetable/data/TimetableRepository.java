package com.csehub.app.timetable.data;

import android.content.Context;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.csehub.app.auth.data.AuthRepository;
import com.csehub.app.core.database.AppDatabase;
import com.csehub.app.core.database.entity.TimetableEntity;
import com.csehub.app.core.network.ApiClient;
import com.csehub.app.core.network.models.AcademicYear;
import com.csehub.app.core.network.models.ApiResponse;
import com.csehub.app.core.network.models.Section;
import com.csehub.app.core.network.models.Timetable;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TimetableRepository {

    private final TimetableApi timetableApi;
    private final AppDatabase db;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    public TimetableRepository(Context context) {
        this.timetableApi = ApiClient.createService(TimetableApi.class);
        this.db = AppDatabase.getInstance(context);
    }

    public LiveData<List<TimetableEntity>> getOfflineTimetableForDay(
            String academicYearId, String sectionId, String day) {
        return db.timetableDao().getTimetableForDay(academicYearId, sectionId, day);
    }

    public LiveData<List<TimetableEntity>> getFacultyOfflineTimetableForDay(String facultyName, String day) {
        return db.timetableDao().getFacultyTimetableForDay(facultyName, day);
    }

    public LiveData<AuthRepository.Resource<List<Timetable>>> getDailyTimetable(
            String academicYearId, String sectionId, String day) {

        MutableLiveData<AuthRepository.Resource<List<Timetable>>> result = new MutableLiveData<>();
        result.setValue(AuthRepository.Resource.loading());

        timetableApi.getDailyTimetable(academicYearId, sectionId, day)
                .enqueue(new Callback<ApiResponse<List<Timetable>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<Timetable>>> call,
                                   Response<ApiResponse<List<Timetable>>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    List<Timetable> list = response.body().getData();
                    result.setValue(AuthRepository.Resource.success(list));

                    // Cache in background thread
                    executor.execute(() -> {
                        List<TimetableEntity> entities = new ArrayList<>();
                        for (Timetable t : list) {
                            TimetableEntity ent = new TimetableEntity();
                            ent.setId(t.getId());

                            AcademicYear ay = t.getAcademicYear();
                            if (ay != null) {
                                ent.setAcademicYearId(ay.getId());
                                ent.setAcademicYearName(ay.getName());
                            }

                            Section sec = t.getSection();
                            if (sec != null) {
                                ent.setSectionId(sec.getId());
                                ent.setSection(sec.getName());
                            }

                            ent.setDay(t.getDay());
                            ent.setPeriod(t.getPeriod());
                            ent.setSubject(t.getSubject());
                            ent.setSubjectCode(t.getSubjectCode());
                            ent.setFacultyName(t.getFacultyName());
                            ent.setRoom(t.getRoom());
                            ent.setStartTime(t.getStartTime());
                            ent.setEndTime(t.getEndTime());
                            ent.setType(t.getType());
                            entities.add(ent);
                        }
                        db.timetableDao().insertAll(entities);
                    });
                } else {
                    result.setValue(AuthRepository.Resource.error("Failed to load timetable"));
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<Timetable>>> call, Throwable t) {
                result.setValue(AuthRepository.Resource.error("Failed to connect: " + t.getMessage()));
            }
        });

        return result;
    }

    public LiveData<AuthRepository.Resource<Void>> importCSV(MultipartBody.Part filePart) {
        MutableLiveData<AuthRepository.Resource<Void>> result = new MutableLiveData<>();
        result.setValue(AuthRepository.Resource.loading());

        timetableApi.importCSV(filePart).enqueue(new Callback<ApiResponse<Void>>() {
            @Override
            public void onResponse(Call<ApiResponse<Void>> call, Response<ApiResponse<Void>> response) {
                if (response.isSuccessful()) {
                    result.setValue(AuthRepository.Resource.success(null));
                } else {
                    result.setValue(AuthRepository.Resource.error("Failed to import CSV timetable details"));
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Void>> call, Throwable t) {
                result.setValue(AuthRepository.Resource.error("Connection failed"));
            }
        });

        return result;
    }
}
