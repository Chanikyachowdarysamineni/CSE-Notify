package com.csehub.app.event.data;

import android.content.Context;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.csehub.app.auth.data.AuthRepository;
import com.csehub.app.core.database.AppDatabase;
import com.csehub.app.core.database.entity.EventEntity;
import com.csehub.app.core.network.ApiClient;
import com.csehub.app.core.network.models.ApiResponse;
import com.csehub.app.core.network.models.Event;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EventRepository {

    private final EventApi eventApi;
    private final AppDatabase db;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    public EventRepository(Context context) {
        this.eventApi = ApiClient.createService(EventApi.class);
        this.db = AppDatabase.getInstance(context);
    }

    public LiveData<List<EventEntity>> getOfflineEvents() {
        return db.eventDao().getAllEvents();
    }

    public LiveData<AuthRepository.Resource<List<Event>>> getEvents(
            int page, int limit, String eventType, String search, boolean upcoming) {
        MutableLiveData<AuthRepository.Resource<List<Event>>> result = new MutableLiveData<>();
        result.setValue(AuthRepository.Resource.loading());

        eventApi.getEvents(page, limit, eventType, search, String.valueOf(upcoming))
                .enqueue(new Callback<ApiResponse<List<Event>>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<List<Event>>> call, Response<ApiResponse<List<Event>>> response) {
                        if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                            List<Event> list = response.body().getData();
                            result.setValue(AuthRepository.Resource.success(list));

                            // Cache offline in background thread
                            executor.execute(() -> {
                                List<EventEntity> entities = new ArrayList<>();
                                for (Event e : list) {
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
                                    
                                    // Parse date string to long timestamp
                                    try {
                                        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
                                        if (e.getDate() != null) {
                                            ent.setDate(format.parse(e.getDate().substring(0, 10)).getTime());
                                        }
                                    } catch (Exception ex) { /* ignore parse error */ }

                                    entities.add(ent);
                                }
                                db.eventDao().insertAll(entities);
                            });
                        } else {
                            result.setValue(AuthRepository.Resource.error("Failed to load events"));
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<List<Event>>> call, Throwable t) {
                        result.setValue(AuthRepository.Resource.error("Failed to connect: " + t.getMessage()));
                    }
                });

        return result;
    }

    public LiveData<AuthRepository.Resource<Event>> getEventById(String id) {
        MutableLiveData<AuthRepository.Resource<Event>> result = new MutableLiveData<>();
        result.setValue(AuthRepository.Resource.loading());

        eventApi.getEventById(id).enqueue(new Callback<ApiResponse<Event>>() {
            @Override
            public void onResponse(Call<ApiResponse<Event>> call, Response<ApiResponse<Event>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    result.setValue(AuthRepository.Resource.success(response.body().getData()));
                } else {
                    result.setValue(AuthRepository.Resource.error("Event not found"));
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Event>> call, Throwable t) {
                result.setValue(AuthRepository.Resource.error("Network connection failed"));
            }
        });

        return result;
    }

    public LiveData<AuthRepository.Resource<Event>> createEvent(
            String title, String description, String eventType, String date, String time,
            String venue, List<String> targetYears, List<String> targetSections,
            String registrationLink, MultipartBody.Part bannerFile) {

        MutableLiveData<AuthRepository.Resource<Event>> result = new MutableLiveData<>();
        result.setValue(AuthRepository.Resource.loading());

        Map<String, RequestBody> map = new HashMap<>();
        map.put("title", RequestBody.create(title, MediaType.parse("text/plain")));
        map.put("description", RequestBody.create(description, MediaType.parse("text/plain")));
        map.put("eventType", RequestBody.create(eventType, MediaType.parse("text/plain")));
        map.put("date", RequestBody.create(date, MediaType.parse("text/plain")));
        map.put("time", RequestBody.create(time, MediaType.parse("text/plain")));
        map.put("venue", RequestBody.create(venue, MediaType.parse("text/plain")));
        map.put("registrationLink", RequestBody.create(registrationLink == null ? "" : registrationLink, MediaType.parse("text/plain")));

        String yearsJson = new com.google.gson.Gson().toJson(targetYears);
        String sectionsJson = new com.google.gson.Gson().toJson(targetSections);
        map.put("targetYears", RequestBody.create(yearsJson, MediaType.parse("application/json")));
        map.put("targetSections", RequestBody.create(sectionsJson, MediaType.parse("application/json")));

        eventApi.createEvent(map, bannerFile).enqueue(new Callback<ApiResponse<Event>>() {
            @Override
            public void onResponse(Call<ApiResponse<Event>> call, Response<ApiResponse<Event>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    result.setValue(AuthRepository.Resource.success(response.body().getData()));
                } else {
                    result.setValue(AuthRepository.Resource.error("Failed to create event"));
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Event>> call, Throwable t) {
                result.setValue(AuthRepository.Resource.error("Server connection timeout"));
            }
        });

        return result;
    }
    public LiveData<AuthRepository.Resource<Event>> updateEvent(
            String id, String title, String description, String eventType, String date, String time,
            String venue, List<String> targetYears, List<String> targetSections,
            String registrationLink, MultipartBody.Part bannerFile) {

        MutableLiveData<AuthRepository.Resource<Event>> result = new MutableLiveData<>();
        result.setValue(AuthRepository.Resource.loading());

        Map<String, RequestBody> map = new HashMap<>();
        map.put("title", RequestBody.create(title, MediaType.parse("text/plain")));
        map.put("description", RequestBody.create(description, MediaType.parse("text/plain")));
        map.put("eventType", RequestBody.create(eventType, MediaType.parse("text/plain")));
        map.put("date", RequestBody.create(date, MediaType.parse("text/plain")));
        map.put("time", RequestBody.create(time, MediaType.parse("text/plain")));
        map.put("venue", RequestBody.create(venue, MediaType.parse("text/plain")));
        map.put("registrationLink", RequestBody.create(registrationLink == null ? "" : registrationLink, MediaType.parse("text/plain")));

        String yearsJson = new com.google.gson.Gson().toJson(targetYears);
        String sectionsJson = new com.google.gson.Gson().toJson(targetSections);
        map.put("targetYears", RequestBody.create(yearsJson, MediaType.parse("application/json")));
        map.put("targetSections", RequestBody.create(sectionsJson, MediaType.parse("application/json")));

        eventApi.updateEvent(id, map, bannerFile).enqueue(new Callback<ApiResponse<Event>>() {
            @Override
            public void onResponse(Call<ApiResponse<Event>> call, Response<ApiResponse<Event>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    result.setValue(AuthRepository.Resource.success(response.body().getData()));
                } else {
                    result.setValue(AuthRepository.Resource.error("Failed to update event"));
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Event>> call, Throwable t) {
                result.setValue(AuthRepository.Resource.error("Server connection timeout"));
            }
        });

        return result;
    }

    public LiveData<AuthRepository.Resource<Void>> deleteEvent(String id) {
        MutableLiveData<AuthRepository.Resource<Void>> result = new MutableLiveData<>();
        result.setValue(AuthRepository.Resource.loading());

        eventApi.deleteEvent(id).enqueue(new Callback<ApiResponse<Void>>() {
            @Override
            public void onResponse(Call<ApiResponse<Void>> call, Response<ApiResponse<Void>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    result.setValue(AuthRepository.Resource.success(null));
                    executor.execute(() -> db.eventDao().deleteById(id));
                } else {
                    result.setValue(AuthRepository.Resource.error("Delete failed"));
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Void>> call, Throwable t) {
                result.setValue(AuthRepository.Resource.error("Offline"));
            }
        });
        return result;
    }
}
