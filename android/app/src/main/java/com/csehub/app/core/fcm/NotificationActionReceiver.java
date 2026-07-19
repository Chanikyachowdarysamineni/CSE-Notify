package com.csehub.app.core.fcm;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.csehub.app.auth.data.AuthRepository;
import com.csehub.app.core.database.AppDatabase;
import com.csehub.app.core.network.ApiClient;
import com.csehub.app.notification.data.NotificationApi;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * BroadcastReceiver to handle notification actions (like "Mark as Read")
 * directly from the system notification panel without opening the app.
 */
public class NotificationActionReceiver extends BroadcastReceiver {
    private static final String TAG = "NotificationActionRec";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null) return;
        
        String action = intent.getAction();
        String notificationId = intent.getStringExtra("notification_id");
        int systemNotificationId = intent.getIntExtra("system_notification_id", -1);

        if ("com.csehub.app.ACTION_MARK_READ".equals(action) && notificationId != null) {
            Log.d(TAG, "Marking notification " + notificationId + " as read from notification action");

            // 1. Instantly dismiss the notification from the tray
            NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            if (manager != null && systemNotificationId != -1) {
                manager.cancel(systemNotificationId);
            }

            // 2. Update local database in background thread
            AppDatabase db = AppDatabase.getInstance(context);
            new Thread(() -> {
                db.notificationDao().markAsRead(notificationId);
                Log.d(TAG, "Notification marked as read in local Room cache");
            }).start();

            // 3. Make API call to sync read status with backend
            try {
                // Initialize ApiClient if needed
                ApiClient.init(context);
                NotificationApi api = ApiClient.createService(NotificationApi.class);
                api.markAsRead(notificationId).enqueue(new Callback<com.csehub.app.core.network.models.ApiResponse<Void>>() {
                    @Override
                    public void onResponse(Call<com.csehub.app.core.network.models.ApiResponse<Void>> call,
                                           Response<com.csehub.app.core.network.models.ApiResponse<Void>> response) {
                        if (response.isSuccessful()) {
                            Log.d(TAG, "Successfully synced read status with server");
                        } else {
                            Log.w(TAG, "Failed to sync read status with server: Code " + response.code());
                        }
                    }

                    @Override
                    public void onFailure(Call<com.csehub.app.core.network.models.ApiResponse<Void>> call, Throwable t) {
                        Log.w(TAG, "Network failure syncing read status with server: " + t.getMessage());
                    }
                });
            } catch (Exception e) {
                Log.e(TAG, "Error initiating API call for read sync", e);
            }
        }
    }
}
