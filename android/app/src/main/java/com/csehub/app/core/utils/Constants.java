package com.csehub.app.core.utils;

/**
 * Application-wide constants
 */
public final class Constants {

    private Constants() {} // Prevent instantiation

    // API
    public static final int CONNECT_TIMEOUT = 30; // seconds
    public static final int READ_TIMEOUT = 30;
    public static final int WRITE_TIMEOUT = 30;

    // Pagination
    public static final int DEFAULT_PAGE_SIZE = 20;

    // Preferences
    public static final String PREF_NAME = "cse_hub_prefs";
    public static final String KEY_TOKEN = "jwt_token";
    public static final String KEY_USER_ID = "user_id";
    public static final String KEY_USER_EMAIL = "user_email";
    public static final String KEY_USER_NAME = "user_name";
    public static final String KEY_USER_ROLE = "user_role";
    public static final String KEY_IS_LOGGED_IN = "is_logged_in";
    public static final String KEY_FCM_TOKEN = "fcm_token";
    public static final String KEY_REFRESH_TOKEN = "refresh_token";
    public static final String KEY_DARK_MODE = "dark_mode";
    public static final String KEY_PROFILE_PHOTO = "profile_photo";

    // Broadcast Actions
    public static final String ACTION_AUTH_ERROR = "com.csehub.app.ACTION_AUTH_ERROR";

    // Roles
    public static final String ROLE_ADMIN = "admin";
    public static final String ROLE_FACULTY = "faculty";
    public static final String ROLE_STUDENT = "student";

    // Intent extras
    public static final String EXTRA_NOTIFICATION_ID = "notification_id";
    public static final String EXTRA_EVENT_ID = "event_id";
    public static final String EXTRA_FILE_ID = "file_id";
    public static final String EXTRA_IMAGE_URL = "image_url";
    public static final String EXTRA_GALLERY_POST = "gallery_post";

    // Request codes
    public static final int REQUEST_PICK_IMAGE = 1001;
    public static final int REQUEST_PICK_FILE = 1002;
    public static final int REQUEST_CAMERA = 1003;
    public static final int REQUEST_PICK_CSV = 1004;

    // Notification categories
    public static final String[] NOTIFICATION_CATEGORIES = {
        "General", "Academic", "Exam", "Placement", "Event",
        "Workshop", "Holiday", "Sports", "Cultural", "Technical",
        "Birthday", "Timetable", "Emergency"
    };

    // Notification priorities
    public static final String[] NOTIFICATION_PRIORITIES = {
        "low", "medium", "high", "urgent"
    };

    // Event types
    public static final String[] EVENT_TYPES = {
        "Workshop", "Seminar", "Hackathon", "Cultural", "Sports",
        "Technical", "Placement", "Guest Lecture", "Competition",
        "Exhibition", "Other"
    };

    // Gallery categories
    public static final String[] GALLERY_CATEGORIES = {
        "Achievements", "Workshops", "Placements", "Campus Life", "Events", "Sports"
    };

    // File categories
    public static final String[] FILE_CATEGORIES = {
        "Notes", "Assignments", "Previous Papers", "Lab Manuals",
        "Syllabus", "Circulars", "Forms", "Others"
    };

    // Years and Sections
    public static final String[] YEARS = {"I", "II", "III", "IV"};
    public static final String[] SECTIONS = {"A", "B", "C", "D"};
    public static final String[] DAYS = {
        "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"
    };
}
