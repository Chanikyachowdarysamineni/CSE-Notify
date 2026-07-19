package com.csehub.app.core.network.models;

import com.google.gson.annotations.SerializedName;
import java.util.List;

/**
 * Data model representing the dynamic metadata configuration from backend
 */
public class ConfigMetadata {

    @SerializedName("notificationCategories")
    private List<String> notificationCategories;

    @SerializedName("eventTypes")
    private List<String> eventTypes;

    @SerializedName("galleryCategories")
    private List<String> galleryCategories;

    @SerializedName("fileCategories")
    private List<String> fileCategories;

    public List<String> getNotificationCategories() {
        return notificationCategories;
    }

    public void setNotificationCategories(List<String> notificationCategories) {
        this.notificationCategories = notificationCategories;
    }

    public List<String> getEventTypes() {
        return eventTypes;
    }

    public void setEventTypes(List<String> eventTypes) {
        this.eventTypes = eventTypes;
    }

    public List<String> getGalleryCategories() {
        return galleryCategories;
    }

    public void setGalleryCategories(List<String> galleryCategories) {
        this.galleryCategories = galleryCategories;
    }

    public List<String> getFileCategories() {
        return fileCategories;
    }

    public void setFileCategories(List<String> fileCategories) {
        this.fileCategories = fileCategories;
    }
}
