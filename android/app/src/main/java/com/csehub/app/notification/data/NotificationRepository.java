package com.csehub.app.notification.data;

import android.content.Context;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.csehub.app.auth.data.AuthRepository;
import com.csehub.app.core.database.AppDatabase;
import com.csehub.app.core.database.entity.NotificationEntity;
import com.csehub.app.core.network.ApiClient;
import com.csehub.app.core.network.models.ApiResponse;
import com.csehub.app.core.network.models.Notification;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NotificationRepository {

    private final NotificationApi notificationApi;
    private final AppDatabase db;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    public NotificationRepository(Context context) {
        this.notificationApi = ApiClient.createService(NotificationApi.class);
        this.db = AppDatabase.getInstance(context);
    }

    public LiveData<List<NotificationEntity>> getOfflineNotifications() {
        return db.notificationDao().getAllNotifications();
    }

    public LiveData<AuthRepository.Resource<List<Notification>>> getNotifications(
            int page, int limit, String category, String priority, String search, boolean unreadOnly) {
        MutableLiveData<AuthRepository.Resource<List<Notification>>> result = new MutableLiveData<>();
        result.setValue(AuthRepository.Resource.loading());

        notificationApi.getNotifications(page, limit, category, priority, search, String.valueOf(unreadOnly))
                .enqueue(new Callback<ApiResponse<List<Notification>>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<List<Notification>>> call, Response<ApiResponse<List<Notification>>> response) {
                        if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                            List<Notification> list = response.body().getData();
                            result.setValue(AuthRepository.Resource.success(list));
                            
                            // Cache in background thread
                            executor.execute(() -> {
                                List<NotificationEntity> entities = new ArrayList<>();
                                for (Notification n : list) {
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
                            });
                        } else {
                            result.setValue(AuthRepository.Resource.error("Failed to load notifications"));
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<List<Notification>>> call, Throwable t) {
                        result.setValue(AuthRepository.Resource.error("Failed to connect: " + t.getMessage()));
                    }
                });

        return result;
    }

    public LiveData<AuthRepository.Resource<Notification>> getNotificationById(String id) {
        MutableLiveData<AuthRepository.Resource<Notification>> result = new MutableLiveData<>();
        result.setValue(AuthRepository.Resource.loading());

        notificationApi.getNotificationById(id).enqueue(new Callback<ApiResponse<Notification>>() {
            @Override
            public void onResponse(Call<ApiResponse<Notification>> call, Response<ApiResponse<Notification>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    result.setValue(AuthRepository.Resource.success(response.body().getData()));
                } else {
                    result.setValue(AuthRepository.Resource.error("Notification details not found"));
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Notification>> call, Throwable t) {
                result.setValue(AuthRepository.Resource.error("Network connection failed"));
            }
        });

        return result;
    }

    public LiveData<AuthRepository.Resource<Notification>> createNotification(
            String title, String message, String category, String priority,
            List<String> targetYears, List<String> targetSections, String link, MultipartBody.Part attachmentFile) {

        MutableLiveData<AuthRepository.Resource<Notification>> result = new MutableLiveData<>();
        result.setValue(AuthRepository.Resource.loading());

        // Prepare request body parts
        Map<String, RequestBody> map = new HashMap<>();
        map.put("title", RequestBody.create(title, MediaType.parse("text/plain")));
        map.put("message", RequestBody.create(message, MediaType.parse("text/plain")));
        map.put("category", RequestBody.create(category, MediaType.parse("text/plain")));
        map.put("priority", RequestBody.create(priority, MediaType.parse("text/plain")));
        map.put("link", RequestBody.create(link == null ? "" : link, MediaType.parse("text/plain")));

        // Map target lists
        String yearsJson = new com.google.gson.Gson().toJson(targetYears);
        String sectionsJson = new com.google.gson.Gson().toJson(targetSections);
        map.put("targetYears", RequestBody.create(yearsJson, MediaType.parse("application/json")));
        map.put("targetSections", RequestBody.create(sectionsJson, MediaType.parse("application/json")));

        notificationApi.createNotification(map, attachmentFile).enqueue(new Callback<ApiResponse<Notification>>() {
            @Override
            public void onResponse(Call<ApiResponse<Notification>> call, Response<ApiResponse<Notification>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    result.setValue(AuthRepository.Resource.success(response.body().getData()));
                } else {
                    result.setValue(AuthRepository.Resource.error("Failed to publish notification"));
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Notification>> call, Throwable t) {
                result.setValue(AuthRepository.Resource.error("Server connection timeout"));
            }
        });

        return result;
    }

    public LiveData<AuthRepository.Resource<Void>> markAsRead(String id) {
        MutableLiveData<AuthRepository.Resource<Void>> result = new MutableLiveData<>();

        notificationApi.markAsRead(id).enqueue(new Callback<ApiResponse<Void>>() {
            @Override
            public void onResponse(Call<ApiResponse<Void>> call, Response<ApiResponse<Void>> response) {
                if (response.isSuccessful()) {
                    result.setValue(AuthRepository.Resource.success(null));
                    executor.execute(() -> db.notificationDao().markAsRead(id));
                } else {
                    result.setValue(AuthRepository.Resource.error("Mark read failed"));
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Void>> call, Throwable t) {
                result.setValue(AuthRepository.Resource.error("Offline"));
            }
        });

        return result;
    }
    public LiveData<AuthRepository.Resource<Void>> deleteNotification(String id) {
        MutableLiveData<AuthRepository.Resource<Void>> result = new MutableLiveData<>();
        result.setValue(AuthRepository.Resource.loading());

        notificationApi.deleteNotification(id).enqueue(new Callback<ApiResponse<Void>>() {
            @Override
            public void onResponse(Call<ApiResponse<Void>> call, Response<ApiResponse<Void>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    result.setValue(AuthRepository.Resource.success(null));
                    executor.execute(() -> db.notificationDao().deleteById(id));
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
    public LiveData<AuthRepository.Resource<Notification>> updateNotification(
            String id, String title, String message, String category, String priority,
            List<String> targetYears, List<String> targetSections, String link, MultipartBody.Part attachmentFile) {

        MutableLiveData<AuthRepository.Resource<Notification>> result = new MutableLiveData<>();
        result.setValue(AuthRepository.Resource.loading());

        Map<String, RequestBody> map = new HashMap<>();
        map.put("title", RequestBody.create(title, MediaType.parse("text/plain")));
        map.put("message", RequestBody.create(message, MediaType.parse("text/plain")));
        map.put("category", RequestBody.create(category, MediaType.parse("text/plain")));
        map.put("priority", RequestBody.create(priority, MediaType.parse("text/plain")));
        map.put("link", RequestBody.create(link == null ? "" : link, MediaType.parse("text/plain")));

        String yearsJson = new com.google.gson.Gson().toJson(targetYears);
        String sectionsJson = new com.google.gson.Gson().toJson(targetSections);
        map.put("targetYears", RequestBody.create(yearsJson, MediaType.parse("application/json")));
        map.put("targetSections", RequestBody.create(sectionsJson, MediaType.parse("application/json")));

        notificationApi.updateNotification(id, map, attachmentFile).enqueue(new Callback<ApiResponse<Notification>>() {
            @Override
            public void onResponse(Call<ApiResponse<Notification>> call, Response<ApiResponse<Notification>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    result.setValue(AuthRepository.Resource.success(response.body().getData()));
                } else {
                    result.setValue(AuthRepository.Resource.error("Failed to update notification"));
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Notification>> call, Throwable t) {
                result.setValue(AuthRepository.Resource.error("Server connection timeout"));
            }
        });

        return result;
    }
}
