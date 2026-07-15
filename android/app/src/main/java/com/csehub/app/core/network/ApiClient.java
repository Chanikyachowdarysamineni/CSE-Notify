package com.csehub.app.core.network;

import android.content.Context;

import com.csehub.app.BuildConfig;
import com.csehub.app.core.security.TokenManager;
import com.csehub.app.core.utils.Constants;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Singleton API client using Retrofit + OkHttp.
 * Automatically injects JWT token into requests via AuthInterceptor.
 */
public class ApiClient {

    private static Retrofit retrofit;
    private static Context appContext;

    public static void init(Context context) {
        appContext = context.getApplicationContext();
    }

    public static Retrofit getInstance() {
        if (retrofit == null) {
            synchronized (ApiClient.class) {
                if (retrofit == null) {
                    retrofit = buildRetrofit();
                }
            }
        }
        return retrofit;
    }

    private static Retrofit buildRetrofit() {
        // Logging interceptor (debug only)
        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
        loggingInterceptor.setLevel(
                BuildConfig.DEBUG
                        ? HttpLoggingInterceptor.Level.BODY
                        : HttpLoggingInterceptor.Level.NONE
        );

        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(Constants.CONNECT_TIMEOUT, TimeUnit.SECONDS)
                .readTimeout(Constants.READ_TIMEOUT, TimeUnit.SECONDS)
                .writeTimeout(Constants.WRITE_TIMEOUT, TimeUnit.SECONDS)
                .addInterceptor(new NetworkConnectionInterceptor(appContext))
                .addInterceptor(new AuthErrorInterceptor(appContext))
                .authenticator(new TokenAuthenticator(appContext))
                .addInterceptor(chain -> {
                    // Auth interceptor - inject JWT token
                    Request original = chain.request();
                    Request.Builder builder = original.newBuilder();

                    if (appContext != null) {
                        TokenManager tokenManager = TokenManager.getInstance(appContext);
                        String token = tokenManager.getToken();
                        if (token != null && !token.isEmpty()) {
                            builder.header("Authorization", "Bearer " + token);
                        }
                    }

                    builder.header("Content-Type", "application/json");
                    builder.header("Accept", "application/json");

                    return chain.proceed(builder.build());
                })
                .addInterceptor(loggingInterceptor)
                .build();

        return new Retrofit.Builder()
                .baseUrl(BuildConfig.BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }

    /**
     * Create API service interface
     */
    public static <T> T createService(Class<T> serviceClass) {
        return getInstance().create(serviceClass);
    }

    /**
     * Reset client (e.g., after logout)
     */
    public static void reset() {
        retrofit = null;
    }
}
