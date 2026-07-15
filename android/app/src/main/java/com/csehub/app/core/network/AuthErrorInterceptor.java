package com.csehub.app.core.network;

import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class AuthErrorInterceptor implements Interceptor {

    private final Context context;
    public static final String ACTION_UNAUTHORIZED = "com.csehub.app.UNAUTHORIZED";
    public static final String ACTION_SERVER_ERROR = "com.csehub.app.SERVER_ERROR";

    public AuthErrorInterceptor(Context context) {
        this.context = context;
    }

    @NonNull
    @Override
    public Response intercept(@NonNull Chain chain) throws IOException {
        Request request = chain.request();
        Response response = chain.proceed(request);

        if (response.code() == 401) {
            // Token expired or unauthorized
            Intent intent = new Intent(ACTION_UNAUTHORIZED);
            LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
        } else if (response.code() >= 500) {
            // Server error
            Intent intent = new Intent(ACTION_SERVER_ERROR);
            LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
        }

        return response;
    }
}
