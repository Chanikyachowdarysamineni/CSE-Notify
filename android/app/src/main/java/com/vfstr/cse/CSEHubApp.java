package com.vfstr.cse;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.graphics.Color;
import android.os.Build;

import com.vfstr.cse.core.network.ApiClient;
import com.google.android.material.color.DynamicColors;

/**
 * CSE HUB Application class
 *
 * Initializes global components:
 * - Notification channels (HIGH importance for heads-up display)
 * - Retrofit API client
 * - Material You dynamic colors
 */
import androidx.work.Constraints;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;
import java.util.concurrent.TimeUnit;
import com.vfstr.cse.core.fcm.FcmTokenSyncWorker;
import com.vfstr.cse.core.security.TokenManager;

public class CSEHubApp extends Application {

    // Default high importance
    public static final String CHANNEL_GENERAL = "cse_general";
    public static final String CHANNEL_ACADEMIC = "cse_academic";
    public static final String CHANNEL_EVENTS = "cse_events";
    public static final String CHANNEL_PLACEMENT = "cse_placement";

    // Default normal importance
    public static final String CHANNEL_TIMETABLE = "cse_timetable";

    // Max importance
    public static final String CHANNEL_URGENT = "cse_urgent";

    // Min importance
    public static final String CHANNEL_BIRTHDAY = "cse_birthday";

    // Legacy fallback
    public static final String CHANNEL_ID = "cse_hub_notifications";

    @Override
    public void onCreate() {
        super.onCreate();

        // Apply Material You dynamic colors
        DynamicColors.applyToActivitiesIfAvailable(this);

        // Initialize API client
        ApiClient.init(this);

        // Global token manager initialization
        TokenManager.getInstance(this);

        createNotificationChannels();
        setupFcmTokenSync();
    }

    private void setupFcmTokenSync() {
        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build();

        PeriodicWorkRequest syncWork = new PeriodicWorkRequest.Builder(
                FcmTokenSyncWorker.class, 3, TimeUnit.DAYS)
                .setConstraints(constraints)
                .build();

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
                "fcm_token_sync",
                ExistingPeriodicWorkPolicy.KEEP,
                syncWork
        );
    }

    private void createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager manager = getSystemService(NotificationManager.class);

            // 1. General Announcements (HIGH)
            NotificationChannel generalChannel = new NotificationChannel(
                    CHANNEL_GENERAL, "General Announcements", NotificationManager.IMPORTANCE_HIGH);
            generalChannel.setDescription("General department updates");
            generalChannel.enableLights(true);
            generalChannel.setLightColor(Color.parseColor("#1565C0"));
            generalChannel.setShowBadge(true);
            manager.createNotificationChannel(generalChannel);

            // 2. Academic Updates (HIGH)
            NotificationChannel academicChannel = new NotificationChannel(
                    CHANNEL_ACADEMIC, "Academic Updates", NotificationManager.IMPORTANCE_HIGH);
            academicChannel.setDescription("Exams, assignments, and results");
            academicChannel.enableLights(true);
            academicChannel.setLightColor(Color.parseColor("#1565C0"));
            academicChannel.setShowBadge(true);
            manager.createNotificationChannel(academicChannel);

            // 3. Events & Workshops (HIGH)
            NotificationChannel eventsChannel = new NotificationChannel(
                    CHANNEL_EVENTS, "Events & Workshops", NotificationManager.IMPORTANCE_HIGH);
            eventsChannel.setDescription("Seminars, hackathons, and events");
            eventsChannel.enableLights(true);
            eventsChannel.setLightColor(Color.parseColor("#FF9800"));
            eventsChannel.setShowBadge(true);
            manager.createNotificationChannel(eventsChannel);

            // 4. Placements (HIGH)
            NotificationChannel placementChannel = new NotificationChannel(
                    CHANNEL_PLACEMENT, "Placements & Internships", NotificationManager.IMPORTANCE_HIGH);
            placementChannel.setDescription("Campus recruitment and internship drives");
            placementChannel.enableLights(true);
            placementChannel.setLightColor(Color.parseColor("#4CAF50"));
            placementChannel.setShowBadge(true);
            manager.createNotificationChannel(placementChannel);

            // 5. Timetable (DEFAULT)
            NotificationChannel timetableChannel = new NotificationChannel(
                    CHANNEL_TIMETABLE, "Timetable Reminders", NotificationManager.IMPORTANCE_DEFAULT);
            timetableChannel.setDescription("Daily schedule and class changes");
            timetableChannel.setShowBadge(true);
            manager.createNotificationChannel(timetableChannel);

            // 6. Urgent/Emergency (MAX)
            NotificationChannel urgentChannel = new NotificationChannel(
                    CHANNEL_URGENT, "Emergency Alerts", NotificationManager.IMPORTANCE_HIGH); // Use HIGH, MAX is deprecated in constructor but behaves as HIGH
            urgentChannel.setDescription("Urgent and emergency notifications");
            urgentChannel.enableVibration(true);
            urgentChannel.setVibrationPattern(new long[]{0, 500, 200, 500});
            urgentChannel.enableLights(true);
            urgentChannel.setLightColor(Color.RED);
            urgentChannel.setShowBadge(true);
            manager.createNotificationChannel(urgentChannel);

            // 7. Birthday Wishes (MIN)
            NotificationChannel birthdayChannel = new NotificationChannel(
                    CHANNEL_BIRTHDAY, "Birthday Wishes", NotificationManager.IMPORTANCE_MIN);
            birthdayChannel.setDescription("Automated birthday greetings");
            birthdayChannel.setShowBadge(false);
            manager.createNotificationChannel(birthdayChannel);

            // 8. Legacy Channel (fallback)
            NotificationChannel legacyChannel = new NotificationChannel(
                    CHANNEL_ID, "CSE HUB Notifications", NotificationManager.IMPORTANCE_HIGH);
            manager.createNotificationChannel(legacyChannel);
        }
    }
}

