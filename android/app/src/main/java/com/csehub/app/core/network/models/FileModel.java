package com.csehub.app.core.network.models;

import com.google.gson.annotations.SerializedName;

public class FileModel {

    @SerializedName("_id")
    private String id;

    @SerializedName("name")
    private String name;

    @SerializedName("originalName")
    private String originalName;

    @SerializedName("type")
    private String type;

    @SerializedName("mimeType")
    private String mimeType;

    @SerializedName("size")
    private long size;

    @SerializedName("path")
    private String path;

    @SerializedName("category")
    private String category;

    @SerializedName("description")
    private String description;

    @SerializedName("downloadCount")
    private int downloadCount;

    @SerializedName("uploadedBy")
    private Creator uploadedBy;

    @SerializedName("createdAt")
    private String createdAt;

    public String getId() { return id; }
    public String getName() { return name; }
    public String getOriginalName() { return originalName; }
    public String getType() { return type; }
    public String getMimeType() { return mimeType; }
    public long getSize() { return size; }
    public String getPath() { return path; }
    public String getCategory() { return category; }
    public String getDescription() { return description; }
    public int getDownloadCount() { return downloadCount; }
    public Creator getUploadedBy() { return uploadedBy; }
    public String getCreatedAt() { return createdAt; }

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
