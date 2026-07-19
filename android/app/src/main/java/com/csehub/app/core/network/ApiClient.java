package com.csehub.app.core.network;

import android.content.Context;

import com.csehub.app.BuildConfig;
import com.csehub.app.core.security.TokenManager;
import com.csehub.app.core.utils.Constants;

import java.io.File;
import java.util.concurrent.TimeUnit;

import okhttp3.Cache;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Singleton Retrofit/OkHttp API client.
 *
 * Features:
 * - JWT token injection on every request
 * - NetworkConnectionInterceptor — throws NoConnectivityException when offline
 * - AuthErrorInterceptor — triggers broadcast on 401 so activities can redirect to login
 * - TokenAuthenticator — silently refreshes the JWT on 401 and retries the original request
 * - OkHttp response cache (10 MB) — reduces API calls and improves offline resilience
 * - Longer write timeout for file upload operations (120s)
 */
public class ApiClient {

    private static Retrofit retrofit;
    private static Context appContext;

    // 10 MB OkHttp response cache
    private static final long CACHE_SIZE_BYTES = 10 * 1024 * 1024L;

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
        // Logging interceptor (debug only — strips bodies in release for performance & security)
        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
        loggingInterceptor.setLevel(
                BuildConfig.DEBUG
                        ? HttpLoggingInterceptor.Level.BODY
                        : HttpLoggingInterceptor.Level.NONE
        );

        // OkHttp response cache
        Cache cache = null;
        if (appContext != null) {
            File cacheDir = new File(appContext.getCacheDir(), "http_cache");
            cache = new Cache(cacheDir, CACHE_SIZE_BYTES);
        }

        OkHttpClient.Builder clientBuilder = new OkHttpClient.Builder()
                .connectTimeout(Constants.CONNECT_TIMEOUT, TimeUnit.SECONDS)
                .readTimeout(Constants.READ_TIMEOUT, TimeUnit.SECONDS)
                .writeTimeout(120, TimeUnit.SECONDS)  // Extended for file uploads
                .callTimeout(180, TimeUnit.SECONDS)    // Max total call time
                .addInterceptor(new NetworkConnectionInterceptor(appContext))
                .addInterceptor(new AuthErrorInterceptor(appContext))
                .authenticator(new TokenAuthenticator(appContext))
                .addInterceptor(chain -> {
                    // Auth interceptor — inject JWT token into every request
                    Request original = chain.request();
                    Request.Builder builder = original.newBuilder();

                    if (appContext != null) {
                        TokenManager tokenManager = TokenManager.getInstance(appContext);
                        String token = tokenManager.getToken();
                        if (token != null && !token.isEmpty()) {
                            builder.header("Authorization", "Bearer " + token);
                        }
                    }

                    builder.header("Accept", "application/json");
                    // Note: do NOT set Content-Type here — multipart requests set their own
                    return chain.proceed(builder.build());
                })
                .addInterceptor(loggingInterceptor);

        if (cache != null) {
            clientBuilder.cache(cache);
        }

        return new Retrofit.Builder()
                .baseUrl(BuildConfig.BASE_URL)
                .client(clientBuilder.build())
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
     * Reset client — call after logout to clear cached auth state.
     * Cache is NOT cleared on logout (anonymous cache data is fine).
     */
    public static void reset() {
        retrofit = null;
    }
}
