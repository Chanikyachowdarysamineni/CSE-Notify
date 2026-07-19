package com.csehub.app.core.fcm;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.csehub.app.CSEHubApp;
import com.csehub.app.R;
import com.csehub.app.core.security.TokenManager;
import com.csehub.app.dashboard.ui.MainActivity;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Firebase Cloud Messaging Service
 *
 * DESIGN: The backend sends DATA-ONLY FCM messages (no "notification" block).
 * This means onMessageReceived() is ALWAYS called regardless of whether the
 * app is in the foreground, background, or completely terminated.
 *
 * onMessageReceived() → extract title/body from data → show system notification
 */
public class CSEHubFCMService extends FirebaseMessagingService {

    private static final String TAG = "CSEHubFCM";

    // Atomic counter so each notification gets a unique ID (prevents overwriting)
    private static final AtomicInteger notificationCounter = new AtomicInteger(0);

    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);
        Log.d(TAG, "New FCM token generated");

        // Store token locally immediately
        TokenManager tokenManager = TokenManager.getInstance(getApplicationContext());
        tokenManager.saveFCMToken(token);

        // Register with backend if the user is already logged in
        if (tokenManager.isLoggedIn()) {
            uploadFCMToken(token);
        }
        // If not logged in, the token will be sent on next login via LoginActivity
    }

    /**
     * Upload a new FCM token to the backend.
     * Called when a token refreshes while the user is already authenticated.
     */
    private void uploadFCMToken(String token) {
        try {
            com.csehub.app.auth.data.AuthApi authApi =
                    com.csehub.app.core.network.ApiClient.createService(
                            com.csehub.app.auth.data.AuthApi.class);
            com.csehub.app.auth.data.model.RefreshTokenRequest req =
                    new com.csehub.app.auth.data.model.RefreshTokenRequest(token);

            authApi.refreshFCMToken(req).enqueue(
                    new retrofit2.Callback<com.csehub.app.core.network.models.ApiResponse<Void>>() {
                        @Override
                        public void onResponse(
                                @NonNull retrofit2.Call<com.csehub.app.core.network.models.ApiResponse<Void>> call,
                                @NonNull retrofit2.Response<com.csehub.app.core.network.models.ApiResponse<Void>> response) {
                            if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                                Log.d(TAG, "FCM token successfully synced to backend");
                            } else {
                                Log.w(TAG, "FCM token sync failed: HTTP " + response.code());
                            }
                        }

                        @Override
                        public void onFailure(
                                @NonNull retrofit2.Call<com.csehub.app.core.network.models.ApiResponse<Void>> call,
                                @NonNull Throwable t) {
                            Log.w(TAG, "FCM token sync network error: " + t.getMessage());
                            // Token is saved locally; will be synced on next successful network operation
                        }
                    });
        } catch (Exception e) {
            Log.e(TAG, "Error uploading FCM token", e);
        }
    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        // Extract title and body — we use DATA-ONLY messages, so everything is in data{}
        Map<String, String> data = remoteMessage.getData();

        String title = "";
        String body  = "";

        // Primary: read from data payload (our backend always sends data-only)
        if (data.containsKey("title")) title = data.get("title");
        if (data.containsKey("body"))  body  = data.get("body");

        // Fallback: read from notification block if somehow present (legacy support)
        if (title.isEmpty() && remoteMessage.getNotification() != null
                && remoteMessage.getNotification().getTitle() != null) {
            title = remoteMessage.getNotification().getTitle();
        }
        if (body.isEmpty() && remoteMessage.getNotification() != null
                && remoteMessage.getNotification().getBody() != null) {
            body = remoteMessage.getNotification().getBody();
        }

        // --- DUPLICATE PREVENTION ---
        // FCM network retries can sometimes deliver the same payload twice.
        String notificationId = data.containsKey("notificationId") ? data.get("notificationId") : "";
        if (!notificationId.isEmpty()) {
            android.content.SharedPreferences prefs = getSharedPreferences("fcm_cache", Context.MODE_PRIVATE);
            if (prefs.getBoolean("processed_" + notificationId, false)) {
                Log.w(TAG, "Duplicate FCM payload detected for ID: " + notificationId + " — discarding.");
                return;
            }
            // Save as processed
            prefs.edit().putBoolean("processed_" + notificationId, true).apply();
        }

        // Show notification even if only title is present
        if (!title.isEmpty()) {
            showSystemNotification(title, body.isEmpty() ? " " : body, data);
        } else {
            Log.w(TAG, "Received FCM message with no title — ignoring");
        }
    }

    /**
     * Build and display a system (Android tray) notification.
     *
     * @param title The notification title
     * @param body  The notification body text
     * @param data  Extra data map for deep linking
     */
    private void showSystemNotification(String title, String body, Map<String, String> data) {
        // Build the deep-link intent
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

        // Pass deep-link extras so MainActivity can navigate to the right screen
        if (data.containsKey("notificationId")) {
            intent.putExtra("notification_id", data.get("notificationId"));
            intent.putExtra("navigate_to", "notification_detail");
        } else if (data.containsKey("eventId")) {
            intent.putExtra("event_id", data.get("eventId"));
            intent.putExtra("navigate_to", "event_detail");
        } else {
            intent.putExtra("navigate_to", "notifications");
        }

        // Unique request code prevents PendingIntent from being reused across notifications
        int requestCode = notificationCounter.incrementAndGet();

        int flags = PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE;
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this, requestCode, intent, flags);

        // Select channel based on priority
        String priority = data.containsKey("priority") ? data.get("priority") : "medium";
        String channelId = "urgent".equalsIgnoreCase(priority)
                ? CSEHubApp.CHANNEL_URGENT
                : CSEHubApp.CHANNEL_ID;

        Uri soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, channelId)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(title)
                .setContentText(body)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(body))
                .setAutoCancel(true)
                .setSound(soundUri)
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setGroup("com.csehub.app.NOTIFICATIONS")
                .setDefaults(NotificationCompat.DEFAULT_ALL);

        // Add category badge
        if (data.containsKey("category")) {
            builder.setSubText(data.get("category"));
        }

        // Add "Mark as Read" action button if notificationId exists
        if (data.containsKey("notificationId")) {
            Intent markReadIntent = new Intent(this, NotificationActionReceiver.class);
            markReadIntent.setAction("com.csehub.app.ACTION_MARK_READ");
            markReadIntent.putExtra("notification_id", data.get("notificationId"));
            markReadIntent.putExtra("system_notification_id", requestCode);

            PendingIntent markReadPendingIntent = PendingIntent.getBroadcast(
                    this,
                    requestCode + 20000,
                    markReadIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );
            builder.addAction(R.drawable.ic_notification, "Mark as Read", markReadPendingIntent);
        }

        NotificationManager notifManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // Use a unique ID per notification to prevent overwriting previous ones
        int notifId = requestCode;
        notifManager.notify(notifId, builder.build());

        // Publish/update the group summary notification
        NotificationCompat.Builder summaryBuilder = new NotificationCompat.Builder(this, channelId)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle("CSE HUB")
                .setContentText("New department updates")
                .setGroup("com.csehub.app.NOTIFICATIONS")
                .setGroupSummary(true)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setDefaults(NotificationCompat.DEFAULT_ALL);

        Intent summaryIntent = new Intent(this, MainActivity.class);
        summaryIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        summaryIntent.putExtra("navigate_to", "notifications");
        PendingIntent summaryPendingIntent = PendingIntent.getActivity(
                this, 0, summaryIntent, flags);
        summaryBuilder.setContentIntent(summaryPendingIntent);

        notifManager.notify(999, summaryBuilder.build());

        Log.d(TAG, "Notification displayed: id=" + notifId + ", title=" + title);
    }
}
