package com.csehub.app.event.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.csehub.app.auth.data.AuthRepository;
import com.csehub.app.core.database.entity.EventEntity;
import com.csehub.app.core.network.models.Event;
import com.csehub.app.event.data.EventRepository;

import java.util.List;

import okhttp3.MultipartBody;

public class EventViewModel extends AndroidViewModel {

    private final EventRepository repository;

    public EventViewModel(@NonNull Application application) {
        super(application);
        this.repository = new EventRepository(application);
    }

    public LiveData<List<EventEntity>> getOfflineEvents() {
        return repository.getOfflineEvents();
    }

    public LiveData<AuthRepository.Resource<List<Event>>> getEvents(
            int page, int limit, String eventType, String search, boolean upcoming) {
        return repository.getEvents(page, limit, eventType, search, upcoming);
    }

    public LiveData<AuthRepository.Resource<Event>> getEventById(String id) {
        return repository.getEventById(id);
    }

    public LiveData<AuthRepository.Resource<Event>> createEvent(
            String title, String description, String eventType, String date, String time,
            String venue, List<String> targetYears, List<String> targetSections,
            String registrationLink, MultipartBody.Part bannerFile) {
        return repository.createEvent(title, description, eventType, date, time, venue, targetYears, targetSections, registrationLink, bannerFile);
    }
    public LiveData<AuthRepository.Resource<Void>> deleteEvent(String id) {
        return repository.deleteEvent(id);
    }

    public LiveData<AuthRepository.Resource<Event>> updateEvent(
            String id, String title, String description, String eventType, String date, String time,
            String venue, List<String> targetYears, List<String> targetSections,
            String registrationLink, MultipartBody.Part bannerFile) {
        return repository.updateEvent(id, title, description, eventType, date, time, venue, targetYears, targetSections, registrationLink, bannerFile);
    }
}
