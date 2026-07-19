package com.csehub.app;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.graphics.Color;
import android.os.Build;

import com.csehub.app.core.network.ApiClient;
import com.google.android.material.color.DynamicColors;

/**
 * CSE HUB Application class
 *
 * Initializes global components:
 * - Notification channels (HIGH importance for heads-up display)
 * - Retrofit API client
 * - Material You dynamic colors
 */
public class CSEHubApp extends Application {

    public static final String CHANNEL_ID      = "cse_hub_notifications";
    public static final String CHANNEL_NAME    = "CSE HUB Notifications";
    public static final String CHANNEL_URGENT  = "cse_hub_urgent";

    @Override
    public void onCreate() {
        super.onCreate();

        // Apply Material You dynamic colors
        DynamicColors.applyToActivitiesIfAvailable(this);

        // Initialize API client
        ApiClient.init(this);

        // Create notification channels
        createNotificationChannels();
    }

    private void createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager manager = getSystemService(NotificationManager.class);

            // ---- Default channel (IMPORTANCE_HIGH for heads-up display) ----
            NotificationChannel defaultChannel = new NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH  // Required for heads-up (popup) notifications
            );
            defaultChannel.setDescription("General notifications from CSE HUB");
            defaultChannel.enableVibration(true);
            defaultChannel.setVibrationPattern(new long[]{0, 250, 250, 250});
            defaultChannel.enableLights(true);
            defaultChannel.setLightColor(Color.parseColor("#1565C0")); // Brand blue
            defaultChannel.setShowBadge(true);
            manager.createNotificationChannel(defaultChannel);

            // ---- Urgent channel (IMPORTANCE_HIGH with loud settings) ----
            NotificationChannel urgentChannel = new NotificationChannel(
                    CHANNEL_URGENT,
                    "Urgent Notifications",
                    NotificationManager.IMPORTANCE_HIGH
            );
            urgentChannel.setDescription("Urgent and emergency notifications from CSE Department");
            urgentChannel.enableVibration(true);
            urgentChannel.setVibrationPattern(new long[]{0, 500, 200, 500});
            urgentChannel.enableLights(true);
            urgentChannel.setLightColor(Color.RED);
            urgentChannel.setShowBadge(true);
            manager.createNotificationChannel(urgentChannel);
        }
    }
}
