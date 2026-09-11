package com.vfstr.cse.core.fcm;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;

import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

/**
 * Helper class to manage POST_NOTIFICATIONS permission on Android 13+ (Tiramisu).
 * Provides a rationale dialog before requesting to improve opt-in rates.
 */
public class NotificationPermissionHelper {

    public static final int NOTIFICATION_PERMISSION_CODE = 1001;

    /**
     * Checks and requests notification permissions with rationale.
     * @param activity The requesting activity
     */
    public static void checkAndRequestPermission(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(activity, Manifest.permission.POST_NOTIFICATIONS) 
                    != PackageManager.PERMISSION_GRANTED) {
                
                // Should we show an explanation?
                if (ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.POST_NOTIFICATIONS)) {
                    showRationaleDialog(activity);
                } else {
                    // No explanation needed, we can request the permission.
                    requestPermission(activity);
                }
            }
        }
    }

    private static void requestPermission(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(activity,
                    new String[]{Manifest.permission.POST_NOTIFICATIONS},
                    NOTIFICATION_PERMISSION_CODE);
        }
    }

    private static void showRationaleDialog(Activity activity) {
        new AlertDialog.Builder(activity)
                .setTitle("Stay Updated")
                .setMessage("CSE HUB needs notification permissions to send you important academic updates, timetable changes, and department announcements.\n\nPlease allow notifications in the next prompt.")
                .setPositiveButton("Continue", (dialog, which) -> {
                    requestPermission(activity);
                })
                .setNegativeButton("Not Now", (dialog, which) -> dialog.dismiss())
                .setCancelable(false)
                .show();
    }

    /**
     * Shows a dialog guiding the user to app settings if they permanently denied the permission.
     */
    public static void showSettingsDialog(Activity activity) {
        new AlertDialog.Builder(activity)
                .setTitle("Notifications Disabled")
                .setMessage("You have disabled notifications for CSE HUB. To receive important academic updates, please enable them in Settings.")
                .setPositiveButton("Go to Settings", (dialog, which) -> {
                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    Uri uri = Uri.fromParts("package", activity.getPackageName(), null);
                    intent.setData(uri);
                    activity.startActivity(intent);
                })
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .show();
    }
}

