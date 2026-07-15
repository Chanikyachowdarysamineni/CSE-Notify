package com.csehub.app;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;

import com.csehub.app.core.network.ApiClient;
import com.google.android.material.color.DynamicColors;

/**
 * CSE HUB Application class
 * Initializes global components: notification channels, API client, dynamic colors
 */
public class CSEHubApp extends Application {

    public static final String CHANNEL_ID = "cse_hub_notifications";
    public static final String CHANNEL_NAME = "CSE HUB Notifications";
    public static final String CHANNEL_URGENT = "cse_hub_urgent";

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

            // Default notification channel
            NotificationChannel defaultChannel = new NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            defaultChannel.setDescription("General notifications from CSE HUB");
            defaultChannel.enableVibration(true);
            defaultChannel.setShowBadge(true);
            manager.createNotificationChannel(defaultChannel);

            // Urgent notification channel
            NotificationChannel urgentChannel = new NotificationChannel(
                    CHANNEL_URGENT,
                    "Urgent Notifications",
                    NotificationManager.IMPORTANCE_HIGH
            );
            urgentChannel.setDescription("Urgent and emergency notifications");
            urgentChannel.enableVibration(true);
            urgentChannel.setShowBadge(true);
            manager.createNotificationChannel(urgentChannel);
        }
    }
}
