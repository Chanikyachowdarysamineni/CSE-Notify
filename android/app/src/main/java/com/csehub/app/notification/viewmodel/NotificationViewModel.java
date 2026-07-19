package com.csehub.app.notification.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;

import com.csehub.app.auth.data.AuthRepository;
import com.csehub.app.core.database.entity.NotificationEntity;
import com.csehub.app.core.network.models.Notification;
import com.csehub.app.notification.data.NotificationRepository;

import java.util.List;

import okhttp3.MultipartBody;

/**
 * NotificationViewModel
 *
 * Key design change: getNotificationsLiveData() exposes a single persistent LiveData that
 * the Fragment observes ONCE. Data is refreshed by calling fetchNotifications(), which
 * updates the internal MediatorLiveData. This eliminates the observer accumulation bug.
 */
public class NotificationViewModel extends AndroidViewModel {

    private final NotificationRepository repository;

    // Persistent LiveData that the Fragment subscribes to once
    private final MediatorLiveData<AuthRepository.Resource<List<Notification>>> notificationsLiveData =
            new MediatorLiveData<>();

    // Track the currently active source so we can remove it before adding a new one
    private LiveData<AuthRepository.Resource<List<Notification>>> currentSource = null;

    // Fetch parameters (updated on each fetchNotifications() call)
    private int   fetchPage      = 1;
    private int   fetchLimit     = 100;
    private String fetchCategory = null;
    private String fetchPriority = null;
    private String fetchSearch   = null;
    private boolean fetchUnreadOnly = false;

    public NotificationViewModel(@NonNull Application application) {
        super(application);
        this.repository = new NotificationRepository(application);
    }

    // -------------------------------------------------------------------------
    // Persistent notifications stream (observe once, refresh via fetchNotifications)
    // -------------------------------------------------------------------------

    /** Returns the single persistent LiveData the Fragment should observe exactly once. */
    public LiveData<AuthRepository.Resource<List<Notification>>> getNotificationsLiveData() {
        return notificationsLiveData;
    }

    /**
     * Triggers a fresh network fetch. Results are delivered through getNotificationsLiveData().
     * Safe to call multiple times — removes the previous source before adding the new one.
     */
    public void fetchNotifications(int page, int limit,
                                   String category, String priority,
                                   String search, boolean unreadOnly) {
        this.fetchPage       = page;
        this.fetchLimit      = limit;
        this.fetchCategory   = category;
        this.fetchPriority   = priority;
        this.fetchSearch     = search;
        this.fetchUnreadOnly = unreadOnly;

        // Emit loading state immediately
        notificationsLiveData.setValue(AuthRepository.Resource.loading());

        // Remove old source (prevents observer accumulation)
        if (currentSource != null) {
            notificationsLiveData.removeSource(currentSource);
        }

        // Create new source and wire it to the mediator
        currentSource = repository.getNotifications(page, limit, category, priority, search, unreadOnly);
        notificationsLiveData.addSource(currentSource, value ->
                notificationsLiveData.setValue(value));
    }

    // -------------------------------------------------------------------------
    // Offline (Room) data
    // -------------------------------------------------------------------------

    public LiveData<List<NotificationEntity>> getOfflineNotifications() {
        return repository.getOfflineNotifications();
    }

    // -------------------------------------------------------------------------
    // Detail / CRUD operations
    // -------------------------------------------------------------------------

    public LiveData<AuthRepository.Resource<Notification>> getNotificationById(String id) {
        return repository.getNotificationById(id);
    }

    public LiveData<AuthRepository.Resource<Notification>> createNotification(
            String title, String message, String category, String priority,
            List<String> targetYears, List<String> targetSections,
            String link, MultipartBody.Part file) {
        return repository.createNotification(
                title, message, category, priority, targetYears, targetSections, link, file);
    }

    public LiveData<AuthRepository.Resource<Void>> markAsRead(String id) {
        return repository.markAsRead(id);
    }

    public LiveData<AuthRepository.Resource<Void>> deleteNotification(String id) {
        return repository.deleteNotification(id);
    }

    public LiveData<AuthRepository.Resource<Notification>> updateNotification(
            String id, String title, String message, String category, String priority,
            List<String> targetYears, List<String> targetSections,
            String link, MultipartBody.Part file) {
        return repository.updateNotification(
                id, title, message, category, priority, targetYears, targetSections, link, file);
    }
}
