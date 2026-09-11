package com.vfstr.cse.core.fcm;

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

import com.vfstr.cse.CSEHubApp;
import com.vfstr.cse.R;
import com.vfstr.cse.core.security.TokenManager;
import com.vfstr.cse.dashboard.ui.MainActivity;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import android.graphics.Color;
import android.graphics.BitmapFactory;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import com.vfstr.cse.core.database.AppDatabase;
import com.vfstr.cse.core.database.entity.NotificationEntity;
import com.vfstr.cse.core.network.ApiClient;
import com.vfstr.cse.auth.data.AuthApi;
import com.vfstr.cse.auth.data.model.RefreshTokenRequest;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import com.vfstr.cse.core.network.models.ApiResponse;

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
            AuthApi authApi = ApiClient.createService(AuthApi.class);
            
            String deviceId = android.provider.Settings.Secure.getString(getContentResolver(), android.provider.Settings.Secure.ANDROID_ID);
            String deviceModel = android.os.Build.MANUFACTURER + " " + android.os.Build.MODEL;
            String appVersion = "1.0.0";
            try {
                android.content.pm.PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
                appVersion = pInfo.versionName;
            } catch (Exception e) { /* ignored */ }

            RefreshTokenRequest req = new RefreshTokenRequest(token, deviceId, deviceModel, appVersion);

            authApi.refreshFCMToken(req).enqueue(new Callback<ApiResponse<Void>>() {
                @Override
                public void onResponse(@NonNull Call<ApiResponse<Void>> call, @NonNull Response<ApiResponse<Void>> response) {
                    if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                        Log.d(TAG, "FCM token successfully synced to backend");
                    } else {
                        Log.w(TAG, "FCM token sync failed: HTTP " + response.code());
                    }
                }

                @Override
                public void onFailure(@NonNull Call<ApiResponse<Void>> call, @NonNull Throwable t) {
                    Log.w(TAG, "FCM token sync network error: " + t.getMessage());
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

        // --- DUPLICATE PREVENTION & CACHE PRUNING ---
        String notificationId = data.containsKey("notificationId") ? data.get("notificationId") : "";
        if (!notificationId.isEmpty()) {
            android.content.SharedPreferences prefs = getSharedPreferences("fcm_cache", Context.MODE_PRIVATE);
            long now = System.currentTimeMillis();
            
            if (prefs.getBoolean("processed_" + notificationId, false)) {
                Log.w(TAG, "Duplicate FCM payload detected for ID: " + notificationId + " — discarding.");
                return;
            }
            
            // Periodically prune old entries (e.g., > 3 days) to prevent SharedPreferences unbounded growth
            if (Math.random() < 0.05) { // 5% chance on receive
                Map<String, ?> allEntries = prefs.getAll();
                android.content.SharedPreferences.Editor editor = prefs.edit();
                for (Map.Entry<String, ?> entry : allEntries.entrySet()) {
                    if (entry.getKey().startsWith("time_")) {
                        long time = (Long) entry.getValue();
                        if (now - time > 3 * 24 * 60 * 60 * 1000L) {
                            String id = entry.getKey().replace("time_", "");
                            editor.remove("processed_" + id);
                            editor.remove("time_" + id);
                        }
                    }
                }
                editor.apply();
            }

            // Save as processed
            prefs.edit()
                 .putBoolean("processed_" + notificationId, true)
                 .putLong("time_" + notificationId, now)
                 .apply();
                 
            // --- OFFLINE SYNC: Save to Room DB ---
            saveToDatabase(notificationId, title, body, data);
        }

        // Show notification even if only title is present
        if (!title.isEmpty()) {
            showSystemNotification(title, body.isEmpty() ? " " : body, data);
        } else {
            Log.w(TAG, "Received FCM message with no title — ignoring");
        }
    }

    private void saveToDatabase(String notificationId, String title, String message, Map<String, String> data) {
        new Thread(() -> {
            try {
                NotificationEntity entity = new NotificationEntity();
                entity.setId(notificationId);
                entity.setTitle(title);
                entity.setMessage(message);
                entity.setCategory(data.containsKey("category") ? data.get("category") : "General");
                entity.setPriority(data.containsKey("priority") ? data.get("priority") : "medium");
                entity.setRead(false);
                entity.setCreatedAt(System.currentTimeMillis());
                
                AppDatabase.getInstance(getApplicationContext()).notificationDao().insertSingle(entity);
                
                // Notify UI that a new notification arrived
                Intent updateIntent = new Intent("ACTION_UNREAD_COUNT_UPDATE");
                LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(updateIntent);
            } catch (Exception e) {
                Log.e(TAG, "Error saving to local DB", e);
            }
        }).start();
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

        // Select channel based on category and priority
        String priority = data.containsKey("priority") ? data.get("priority") : "medium";
        String category = data.containsKey("category") ? data.get("category") : "";
        
        String channelId = CSEHubApp.CHANNEL_GENERAL;
        if ("urgent".equalsIgnoreCase(priority)) {
            channelId = CSEHubApp.CHANNEL_URGENT;
        } else if (category.toLowerCase().contains("academic") || category.toLowerCase().contains("exam")) {
            channelId = CSEHubApp.CHANNEL_ACADEMIC;
        } else if (category.toLowerCase().contains("event") || category.toLowerCase().contains("workshop")) {
            channelId = CSEHubApp.CHANNEL_EVENTS;
        } else if (category.toLowerCase().contains("placement") || category.toLowerCase().contains("internship")) {
            channelId = CSEHubApp.CHANNEL_PLACEMENT;
        } else if (category.toLowerCase().contains("timetable")) {
            channelId = CSEHubApp.CHANNEL_TIMETABLE;
        } else if (category.toLowerCase().contains("birthday")) {
            channelId = CSEHubApp.CHANNEL_BIRTHDAY;
        }

        Uri soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, channelId)
                .setSmallIcon(R.drawable.ic_notification)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.ic_logo))
                .setColor(Color.parseColor("#1565C0"))
                .setContentTitle(title)
                .setContentText(body)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(body))
                .setAutoCancel(true)
                .setSound(soundUri)
                .setContentIntent(pendingIntent)
                .setGroup("com.vfstr.cse.NOTIFICATIONS")
                .setDefaults(NotificationCompat.DEFAULT_ALL);
                
        // Set priority mapping
        if ("urgent".equalsIgnoreCase(priority)) {
            builder.setPriority(NotificationCompat.PRIORITY_MAX);
            builder.setFullScreenIntent(pendingIntent, true);
        } else if ("high".equalsIgnoreCase(priority)) {
            builder.setPriority(NotificationCompat.PRIORITY_HIGH);
        } else if ("low".equalsIgnoreCase(priority)) {
            builder.setPriority(NotificationCompat.PRIORITY_LOW);
        } else {
            builder.setPriority(NotificationCompat.PRIORITY_DEFAULT);
        }

        // Add category badge
        if (data.containsKey("category")) {
            builder.setSubText(data.get("category"));
        }

        // Add "Mark as Read" action button if notificationId exists
        if (data.containsKey("notificationId")) {
            Intent markReadIntent = new Intent(this, NotificationActionReceiver.class);
            markReadIntent.setAction("com.vfstr.cse.ACTION_MARK_READ");
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
                .setGroup("com.vfstr.cse.NOTIFICATIONS")
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

