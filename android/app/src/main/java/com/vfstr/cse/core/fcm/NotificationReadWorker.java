package com.vfstr.cse.core.fcm;

import android.content.Context;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.vfstr.cse.core.database.AppDatabase;
import com.vfstr.cse.core.network.ApiClient;
import com.vfstr.cse.core.network.models.ApiResponse;
import com.vfstr.cse.notification.data.NotificationApi;
import retrofit2.Response;

/**
 * WorkManager worker to mark a notification as read in the local Room DB
 * and sync with the backend.
 */
public class NotificationReadWorker extends Worker {

    private static final String TAG = "NotifReadWorker";
    public static final String KEY_NOTIFICATION_ID = "notification_id";

    public NotificationReadWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        String notificationId = getInputData().getString(KEY_NOTIFICATION_ID);
        if (notificationId == null || notificationId.isEmpty()) {
            return Result.failure();
        }

        try {
            // 1. Update local database
            AppDatabase db = AppDatabase.getInstance(getApplicationContext());
            db.notificationDao().markAsRead(notificationId);
            Log.d(TAG, "Notification marked as read in local Room cache");

            // 2. Make API call to sync read status with backend
            ApiClient.init(getApplicationContext());
            NotificationApi api = ApiClient.createService(NotificationApi.class);
            Response<ApiResponse<Void>> response = api.markAsRead(notificationId).execute();

            if (response.isSuccessful()) {
                Log.d(TAG, "Successfully synced read status with server");
                return Result.success();
            } else {
                Log.w(TAG, "Failed to sync read status with server: Code " + response.code());
                return Result.retry();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error processing read sync", e);
            return Result.retry();
        }
    }
}

