package com.csehub.app.core.network;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.csehub.app.BuildConfig;
import com.csehub.app.core.security.TokenManager;
import com.csehub.app.core.utils.Constants;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.IOException;

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
            OkHttpClient client = new OkHttpClient();
            
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

            Response response = client.newCall(request).execute();
            if (response.isSuccessful() && response.body() != null) {
                String responseBody = response.body().string();
                
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
