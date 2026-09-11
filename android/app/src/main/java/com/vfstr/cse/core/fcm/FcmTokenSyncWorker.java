package com.vfstr.cse.core.fcm;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.vfstr.cse.auth.data.AuthApi;
import com.vfstr.cse.auth.data.model.RefreshTokenRequest;
import com.vfstr.cse.core.network.ApiClient;
import com.vfstr.cse.core.network.models.ApiResponse;
import com.vfstr.cse.core.security.TokenManager;
import com.google.firebase.messaging.FirebaseMessaging;

import retrofit2.Response;

/**
 * WorkManager worker to periodically sync FCM token with the backend.
 * Ensures the token remains valid even if the user hasn't opened the app recently.
 */
public class FcmTokenSyncWorker extends Worker {

    private static final String TAG = "FcmTokenSyncWorker";

    public FcmTokenSyncWorker(
            @NonNull Context context,
            @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull
    @Override
    public Result doWork() {
        Log.d(TAG, "Running periodic FCM token sync...");
        TokenManager tokenManager = TokenManager.getInstance(getApplicationContext());

        if (!tokenManager.isLoggedIn()) {
            Log.d(TAG, "User not logged in, skipping FCM token sync");
            return Result.success();
        }

        try {
            // Block until Firebase gives us the token (Worker runs on background thread)
            String currentToken = com.google.android.gms.tasks.Tasks.await(
                    FirebaseMessaging.getInstance().getToken()
            );

            if (currentToken == null || currentToken.isEmpty()) {
                Log.w(TAG, "Failed to get token from Firebase");
                return Result.retry();
            }

            tokenManager.saveFCMToken(currentToken);
            return uploadTokenSync(currentToken);

        } catch (Exception e) {
            Log.e(TAG, "Error in FCM token sync worker", e);
            return Result.retry();
        }
    }

    private Result uploadTokenSync(String token) {
        try {
            AuthApi authApi = ApiClient.createService(AuthApi.class);
            Context context = getApplicationContext();

            String deviceId = android.provider.Settings.Secure.getString(context.getContentResolver(), android.provider.Settings.Secure.ANDROID_ID);
            String deviceModel = android.os.Build.MANUFACTURER + " " + android.os.Build.MODEL;
            String appVersion = "1.0.0";
            try {
                android.content.pm.PackageInfo pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
                appVersion = pInfo.versionName;
            } catch (Exception e) { /* ignored */ }

            RefreshTokenRequest req = new RefreshTokenRequest(token, deviceId, deviceModel, appVersion);

            // Execute synchronously since we are in a Worker thread
            Response<ApiResponse<Void>> response = authApi.refreshFCMToken(req).execute();

            if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                Log.d(TAG, "FCM token successfully synced to backend via WorkManager");
                return Result.success();
            } else {
                Log.w(TAG, "FCM token sync failed via WorkManager: HTTP " + response.code());
                return Result.retry();
            }
        } catch (Exception e) {
            Log.e(TAG, "Network error in FCM token sync worker", e);
            return Result.retry();
        }
    }
}

