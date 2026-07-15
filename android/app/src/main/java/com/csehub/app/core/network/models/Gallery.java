package com.csehub.app.core.network.models;

import com.google.gson.annotations.SerializedName;

public class Gallery {

    @SerializedName("_id")
    private String id;

    @SerializedName("image")
    private String image;

    @SerializedName("caption")
    private String caption;

    @SerializedName("category")
    private String category;

    @SerializedName("postedBy")
    private Creator postedBy;

    @SerializedName("createdAt")
    private String createdAt;

    public String getId() { return id; }
    public String getImage() { return image; }
    public String getCaption() { return caption; }
    public String getCategory() { return category; }
    public Creator getPostedBy() { return postedBy; }
    public String getCreatedAt() { return createdAt; }

    public static class Creator {
        @SerializedName("_id")
        private String id;
        @SerializedName("name")
        private String name;

        public String getId() { return id; }
        public String getName() { return name; }
    }
}
