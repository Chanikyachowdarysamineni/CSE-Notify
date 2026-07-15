package com.csehub.app.dashboard.data;

import android.content.Context;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.csehub.app.auth.data.AuthRepository;
import com.csehub.app.core.database.AppDatabase;
import com.csehub.app.core.database.entity.EventEntity;
import com.csehub.app.core.database.entity.NotificationEntity;
import com.csehub.app.core.database.entity.TimetableEntity;
import com.csehub.app.core.network.ApiClient;
import com.csehub.app.core.network.ApiClient;
import com.csehub.app.core.network.models.AcademicYear;
import com.csehub.app.core.network.models.ApiResponse;
import com.csehub.app.core.network.models.DashboardData;
import com.csehub.app.core.network.models.Event;
import com.csehub.app.core.network.models.Notification;
import com.csehub.app.core.network.models.Section;
import com.csehub.app.core.network.models.Timetable;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DashboardRepository {

    private final DashboardApi dashboardApi;
    private final AppDatabase db;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    public DashboardRepository(Context context) {
        this.dashboardApi = ApiClient.createService(DashboardApi.class);
        this.db = AppDatabase.getInstance(context);
    }

    public LiveData<AuthRepository.Resource<DashboardData>> getDashboardData() {
        MutableLiveData<AuthRepository.Resource<DashboardData>> data = new MutableLiveData<>();
        data.setValue(AuthRepository.Resource.loading());

        dashboardApi.getDashboardData().enqueue(new Callback<ApiResponse<DashboardData>>() {
            @Override
            public void onResponse(Call<ApiResponse<DashboardData>> call, Response<ApiResponse<DashboardData>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    DashboardData dashboardData = response.body().getData();
                    data.setValue(AuthRepository.Resource.success(dashboardData));

                    // Cache details offline in Room using Executor
                    executor.execute(() -> cacheDashboardData(dashboardData));
                } else {
                    data.setValue(AuthRepository.Resource.error("Failed to retrieve dashboard details"));
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<DashboardData>> call, Throwable t) {
                data.setValue(AuthRepository.Resource.error("No internet connection. Loading offline..."));
            }
        });

        return data;
    }

    private void cacheDashboardData(DashboardData dashboardData) {
        if (dashboardData == null) return;

        // Cache Notifications
        if (dashboardData.getTodayNotifications() != null) {
            List<NotificationEntity> entities = new ArrayList<>();
            for (Notification n : dashboardData.getTodayNotifications()) {
                NotificationEntity ent = new NotificationEntity();
                ent.setId(n.getId());
                ent.setTitle(n.getTitle());
                ent.setMessage(n.getMessage());
                ent.setCategory(n.getCategory());
                ent.setPriority(n.getPriority());
                ent.setAttachment(n.getAttachment());
                ent.setAttachmentName(n.getAttachmentName());
                ent.setLink(n.getLink());
                if (n.getCreatedBy() != null) {
                    ent.setCreatedByName(n.getCreatedBy().getName());
                    ent.setCreatedByRole(n.getCreatedBy().getRole());
                }
                ent.setRead(n.isRead());
                entities.add(ent);
            }
            db.notificationDao().insertAll(entities);
        }

        // Cache Events
        if (dashboardData.getUpcomingEvents() != null) {
            List<EventEntity> entities = new ArrayList<>();
            for (Event e : dashboardData.getUpcomingEvents()) {
                EventEntity ent = new EventEntity();
                ent.setId(e.getId());
                ent.setTitle(e.getTitle());
                ent.setDescription(e.getDescription());
                ent.setEventType(e.getEventType());
                ent.setVenue(e.getVenue());
                ent.setTime(e.getTime());
                ent.setBannerImage(e.getBannerImage());
                ent.setRegistrationLink(e.getRegistrationLink());
                if (e.getCreatedBy() != null) {
                    ent.setCreatedByName(e.getCreatedBy().getName());
                }
                entities.add(ent);
            }
            db.eventDao().insertAll(entities);
        }

        // Cache Today's Timetable
        if (dashboardData.getTodayTimetable() != null) {
            List<TimetableEntity> entities = new ArrayList<>();
            for (Timetable t : dashboardData.getTodayTimetable()) {
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
        }
    }
}
