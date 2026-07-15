package com.csehub.app.core.network.models;

import com.google.gson.annotations.SerializedName;

public class Event {

    @SerializedName("_id")
    private String id;

    @SerializedName("title")
    private String title;

    @SerializedName("description")
    private String description;

    @SerializedName("eventType")
    private String eventType;

    @SerializedName("date")
    private String date;

    @SerializedName("time")
    private String time;

    @SerializedName("venue")
    private String venue;

    @SerializedName("bannerImage")
    private String bannerImage;

    @SerializedName("registrationLink")
    private String registrationLink;

    @SerializedName("createdBy")
    private Creator createdBy;

    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getEventType() { return eventType; }
    public String getDate() { return date; }
    public String getTime() { return time; }
    public String getVenue() { return venue; }
    public String getBannerImage() { return bannerImage; }
    public String getRegistrationLink() { return registrationLink; }
    public Creator getCreatedBy() { return createdBy; }

    public static class Creator {
        @SerializedName("_id")
        private String id;
        
        @SerializedName("name")
        private String name;

        public String getId() { return id; }
        public String getName() { return name; }
    }
}
