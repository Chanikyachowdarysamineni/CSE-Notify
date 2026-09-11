package com.vfstr.cse.core.network;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.vfstr.cse.BuildConfig;
import com.vfstr.cse.core.security.TokenManager;
import com.vfstr.cse.core.utils.Constants;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Authenticator;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.Route;

public class TokenAuthenticator implements Authenticator {

    private static final String TAG = "TokenAuthenticator";
    private final Context context;

    /**
     * Static singleton OkHttpClient for token refresh.
     * Created once with explicit timeouts — avoids creating a new bare client
     * (with no timeouts) on every 401 response, which could hang indefinitely
     * on poor mobile networks.
     */
    private static final OkHttpClient REFRESH_CLIENT = new OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .writeTimeout(10, TimeUnit.SECONDS)
            .build();

    public TokenAuthenticator(Context context) {
        this.context = context;
    }

    @Override
    public Request authenticate(Route route, Response response) {
        // Guard 1: Don't attempt token refresh for auth endpoints.
        // This prevents an infinite loop when the user enters wrong credentials on the login screen.
        String url = response.request().url().toString();
        if (url.contains("/auth/login")
                || url.contains("/auth/forgot-password")
                || url.contains("/auth/reset-password")
                || url.contains("/auth/refresh")) {
            return null;
        }

        // Guard 2: Don't retry more than once (the previous response was already a retry)
        if (responseCount(response) >= 2) {
            return null;
        }

        TokenManager tokenManager = TokenManager.getInstance(context);
        String refreshToken = tokenManager.getRefreshToken();

        if (refreshToken == null || refreshToken.isEmpty()) {
            return null; // No refresh token — can't recover
        }

        // Synchronous token refresh
        String newToken = refreshAccessToken(refreshToken);

        if (newToken != null && !newToken.isEmpty()) {
            tokenManager.saveToken(newToken);
            return response.request().newBuilder()
                    .header("Authorization", "Bearer " + newToken)
                    .build();
        } else {
            // Refresh failed → force logout
            tokenManager.clearSession();
            LocalBroadcastManager.getInstance(context)
                    .sendBroadcast(new Intent(Constants.ACTION_AUTH_ERROR));
            return null;
        }
    }

    /** Count the number of prior responses in the redirect chain. */
    private int responseCount(Response response) {
        int count = 1;
        while ((response = response.priorResponse()) != null) {
            count++;
        }
        return count;
    }

    private String refreshAccessToken(String refreshToken) {
        try {
            JsonObject jsonBody = new JsonObject();
            jsonBody.addProperty("refreshToken", refreshToken);

            RequestBody body = RequestBody.create(
                    jsonBody.toString(),
                    MediaType.parse("application/json; charset=utf-8")
            );

            Request request = new Request.Builder()
                    .url(BuildConfig.BASE_URL + "auth/refresh")
                    .post(body)
                    .build();

            // Use the singleton client with proper timeouts
            Response response = REFRESH_CLIENT.newCall(request).execute();
            if (response.isSuccessful() && response.body() != null) {
                String responseBody = response.body().string();
                response.body().close();
                
                // Parse using Gson
                Gson gson = new Gson();
                JsonObject jsonResponse = gson.fromJson(responseBody, JsonObject.class);
                if (jsonResponse.has("data")) {
                    JsonObject data = jsonResponse.getAsJsonObject("data");
                    if (data.has("token")) {
                        String newAccessToken = data.get("token").getAsString();
                        
                        // Also save new refresh token if provided
                        if (data.has("refreshToken")) {
                            TokenManager.getInstance(context).saveRefreshToken(data.get("refreshToken").getAsString());
                        }
                        
                        return newAccessToken;
                    }
                }
            }
        } catch (IOException e) {
            Log.e(TAG, "Error refreshing token", e);
        }
        return null;
    }
}

