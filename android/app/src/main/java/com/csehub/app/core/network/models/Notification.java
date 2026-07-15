package com.csehub.app.core.network.models;

import com.google.gson.annotations.SerializedName;

public class Notification {

    @SerializedName("_id")
    private String id;

    @SerializedName("title")
    private String title;

    @SerializedName("message")
    private String message;

    @SerializedName("category")
    private String category;

    @SerializedName("priority")
    private String priority;

    @SerializedName("attachment")
    private String attachment;

    @SerializedName("attachmentName")
    private String attachmentName;

    @SerializedName("link")
    private String link;

    @SerializedName("createdBy")
    private Creator createdBy;

    @SerializedName("createdAt")
    private String createdAt;

    @SerializedName("isRead")
    private boolean isRead;

    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getMessage() { return message; }
    public String getCategory() { return category; }
    public String getPriority() { return priority; }
    public String getAttachment() { return attachment; }
    public String getAttachmentName() { return attachmentName; }
    public String getLink() { return link; }
    public Creator getCreatedBy() { return createdBy; }
    public String getCreatedAt() { return createdAt; }
    public boolean isRead() { return isRead; }
    public void setRead(boolean read) { isRead = read; }

    public static class Creator {
        @SerializedName("_id")
        private String id;

        @SerializedName("name")
        private String name;

        @SerializedName("role")
        private String role;

        public String getId() { return id; }
        public String getName() { return name; }
        public String getRole() { return role; }
    }
}
