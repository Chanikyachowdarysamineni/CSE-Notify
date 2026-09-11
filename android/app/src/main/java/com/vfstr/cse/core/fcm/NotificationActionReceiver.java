package com.vfstr.cse.core.fcm;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.work.Data;
import androidx.work.ExistingWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

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

        if ("com.vfstr.cse.ACTION_MARK_READ".equals(action) && notificationId != null) {
            Log.d(TAG, "Marking notification " + notificationId + " as read from notification action");

            // 1. Instantly dismiss the notification from the tray
            NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            if (manager != null && systemNotificationId != -1) {
                manager.cancel(systemNotificationId);
            }

            // 2. Queue WorkManager job for reliable DB + Network sync
            Data inputData = new Data.Builder()
                    .putString(NotificationReadWorker.KEY_NOTIFICATION_ID, notificationId)
                    .build();

            OneTimeWorkRequest readWork = new OneTimeWorkRequest.Builder(NotificationReadWorker.class)
                    .setInputData(inputData)
                    .build();

            WorkManager.getInstance(context).enqueueUniqueWork(
                    "mark_read_" + notificationId,
                    ExistingWorkPolicy.REPLACE,
                    readWork
            );

            // 3. Notify UI to update the badge immediately
            Intent updateIntent = new Intent("ACTION_UNREAD_COUNT_UPDATE");
            LocalBroadcastManager.getInstance(context).sendBroadcast(updateIntent);
        }
    }
}

