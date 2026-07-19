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
    public void setId(String id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }

    public String getAttachment() { return attachment; }
    public void setAttachment(String attachment) { this.attachment = attachment; }

    public String getAttachmentName() { return attachmentName; }
    public void setAttachmentName(String attachmentName) { this.attachmentName = attachmentName; }

    public String getLink() { return link; }
    public void setLink(String link) { this.link = link; }

    public Creator getCreatedBy() { return createdBy; }
    public void setCreatedBy(Creator createdBy) { this.createdBy = createdBy; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

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
        public void setId(String id) { this.id = id; }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public String getRole() { return role; }
        public void setRole(String role) { this.role = role; }
    }
}
