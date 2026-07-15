package com.csehub.app.core.fcm;

import android.app.NotificationChannel;
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

/**
 * Service to handle incoming Firebase Cloud Messages
 */
public class CSEHubFCMService extends FirebaseMessagingService {

    private static final String TAG = "CSEHubFCM";

    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);
        Log.d(TAG, "Refreshed token: " + token);

        // Store token locally
        TokenManager tokenManager = TokenManager.getInstance(getApplicationContext());
        tokenManager.saveFCMToken(token);

        // Upload the token directly if the user is logged in.
        if (tokenManager.isLoggedIn()) {
            com.csehub.app.auth.data.AuthApi authApi = com.csehub.app.core.network.ApiClient.createService(com.csehub.app.auth.data.AuthApi.class);
            authApi.refreshFCMToken(new com.csehub.app.auth.data.model.RefreshTokenRequest(token)).enqueue(new retrofit2.Callback<com.csehub.app.core.network.models.ApiResponse<Void>>() {
                @Override
                public void onResponse(@NonNull retrofit2.Call<com.csehub.app.core.network.models.ApiResponse<Void>> call, @NonNull retrofit2.Response<com.csehub.app.core.network.models.ApiResponse<Void>> response) {
                    if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                        Log.d(TAG, "Successfully synced new FCM token to backend");
                    } else {
                        Log.e(TAG, "Failed to sync FCM token to backend: " + response.code());
                    }
                }

                @Override
                public void onFailure(@NonNull retrofit2.Call<com.csehub.app.core.network.models.ApiResponse<Void>> call, @NonNull Throwable t) {
                    Log.e(TAG, "Error syncing FCM token", t);
                }
            });
        }
    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        Log.d(TAG, "From: " + remoteMessage.getFrom());

        // Check if message contains notification payload
        String title = "";
        String body = "";
        if (remoteMessage.getNotification() != null) {
            title = remoteMessage.getNotification().getTitle();
            body = remoteMessage.getNotification().getBody();
        }

        // Check if message contains data payload
        Map<String, String> data = remoteMessage.getData();
        if (data.size() > 0) {
            if (title.isEmpty() && data.containsKey("title")) {
                title = data.get("title");
            }
            if (body.isEmpty() && data.containsKey("body")) {
                body = data.get("body");
            }
        }

        if (!title.isEmpty() && !body.isEmpty()) {
            sendNotification(title, body, data);
        }
    }

    private void sendNotification(String title, String messageBody, Map<String, String> data) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        
        // Pass payload variables to navigate to detail screens if needed
        if (data.containsKey("notificationId")) {
            intent.putExtra("notification_id", data.get("notificationId"));
            intent.putExtra("navigate_to", "notification_detail");
        } else if (data.containsKey("eventId")) {
            intent.putExtra("event_id", data.get("eventId"));
            intent.putExtra("navigate_to", "event_detail");
        }

        PendingIntent pendingIntent = PendingIntent.getActivity(
                this, 0, intent,
                PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE
        );

        String channelId = CSEHubApp.CHANNEL_ID;
        if (data.containsKey("priority") && "urgent".equalsIgnoreCase(data.get("priority"))) {
            channelId = CSEHubApp.CHANNEL_URGENT;
        }

        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(this, channelId)
                        .setSmallIcon(R.drawable.ic_notification)
                        .setContentTitle(title)
                        .setContentText(messageBody)
                        .setAutoCancel(true)
                        .setSound(defaultSoundUri)
                        .setContentIntent(pendingIntent)
                        .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // Generate a unique ID for the notification to prevent overwriting
        int notificationId = (int) System.currentTimeMillis();
        notificationManager.notify(notificationId, notificationBuilder.build());
    }
}
