package com.csehub.app.notification.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.csehub.app.auth.data.AuthRepository;
import com.csehub.app.core.database.entity.NotificationEntity;
import com.csehub.app.core.network.models.Notification;
import com.csehub.app.notification.data.NotificationRepository;

import java.util.List;

import okhttp3.MultipartBody;

public class NotificationViewModel extends AndroidViewModel {

    private final NotificationRepository repository;

    public NotificationViewModel(@NonNull Application application) {
        super(application);
        this.repository = new NotificationRepository(application);
    }

    public LiveData<List<NotificationEntity>> getOfflineNotifications() {
        return repository.getOfflineNotifications();
    }

    public LiveData<AuthRepository.Resource<List<Notification>>> getNotifications(
            int page, int limit, String category, String priority, String search, boolean unreadOnly) {
        return repository.getNotifications(page, limit, category, priority, search, unreadOnly);
    }

    public LiveData<AuthRepository.Resource<Notification>> getNotificationById(String id) {
        return repository.getNotificationById(id);
    }

    public LiveData<AuthRepository.Resource<Notification>> createNotification(
            String title, String message, String category, String priority,
            List<String> targetYears, List<String> targetSections, String link, MultipartBody.Part file) {
        return repository.createNotification(title, message, category, priority, targetYears, targetSections, link, file);
    }

    public LiveData<AuthRepository.Resource<Void>> markAsRead(String id) {
        return repository.markAsRead(id);
    }
    public LiveData<AuthRepository.Resource<Void>> deleteNotification(String id) {
        return repository.deleteNotification(id);
    }

    public LiveData<AuthRepository.Resource<Notification>> updateNotification(
            String id, String title, String message, String category, String priority,
            List<String> targetYears, List<String> targetSections, String link, MultipartBody.Part file) {
        return repository.updateNotification(id, title, message, category, priority, targetYears, targetSections, link, file);
    }
}
