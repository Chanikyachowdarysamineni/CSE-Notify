package com.csehub.app.core.network.models;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class DashboardData {

    @SerializedName("todayNotifications")
    private List<Notification> todayNotifications;

    @SerializedName("upcomingEvents")
    private List<Event> upcomingEvents;

    @SerializedName("todayTimetable")
    private List<Timetable> todayTimetable;

    @SerializedName("recentFiles")
    private List<FileModel> recentFiles;

    @SerializedName("galleryHighlights")
    private List<Gallery> galleryHighlights;

    @SerializedName("statistics")
    private Statistics statistics;

    @SerializedName("unreadCount")
    private int unreadCount;

    public List<Notification> getTodayNotifications() { return todayNotifications; }
    public List<Event> getUpcomingEvents() { return upcomingEvents; }
    public List<Timetable> getTodayTimetable() { return todayTimetable; }
    public List<FileModel> getRecentFiles() { return recentFiles; }
    public List<Gallery> getGalleryHighlights() { return galleryHighlights; }
    public Statistics getStatistics() { return statistics; }
    public int getUnreadCount() { return unreadCount; }

    public static class Statistics {
        @SerializedName("totalStudents")
        private int totalStudents;

        @SerializedName("totalFaculty")
        private int totalFaculty;

        @SerializedName("totalNotifications")
        private int totalNotifications;

        @SerializedName("totalEvents")
        private int totalEvents;

        @SerializedName("totalFiles")
        private int totalFiles;

        @SerializedName("totalGalleryPosts")
        private int totalGalleryPosts;

        public int getTotalStudents() { return totalStudents; }
        public int getTotalFaculty() { return totalFaculty; }
        public int getTotalNotifications() { return totalNotifications; }
        public int getTotalEvents() { return totalEvents; }
        public int getTotalFiles() { return totalFiles; }
        public int getTotalGalleryPosts() { return totalGalleryPosts; }
    }
}
