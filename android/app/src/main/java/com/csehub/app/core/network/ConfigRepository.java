package com.csehub.app.core.network;

import android.content.Context;
import android.content.SharedPreferences;

import com.csehub.app.core.network.models.ApiResponse;
import com.csehub.app.core.network.models.ConfigMetadata;
import com.csehub.app.core.utils.Constants;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Repository for dynamic category metadata, handling sync from server
 * and offline caching using SharedPreferences.
 */
public class ConfigRepository {

    private static ConfigRepository instance;
    private final ConfigApi configApi;
    private final SharedPreferences prefs;
    private final Gson gson;

    private static final String PREFS_NAME = "cse_hub_config_cache";
    private static final String KEY_NOTIF_CATEGORIES = "notif_categories";
    private static final String KEY_EVENT_TYPES = "event_types";
    private static final String KEY_GALLERY_CATEGORIES = "gallery_categories";
    private static final String KEY_FILE_CATEGORIES = "file_categories";

    private ConfigRepository(Context context) {
        this.configApi = ApiClient.createService(ConfigApi.class);
        this.prefs = context.getApplicationContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        this.gson = new Gson();
    }

    public static synchronized ConfigRepository getInstance(Context context) {
        if (instance == null) {
            instance = new ConfigRepository(context);
        }
        return instance;
    }

    /**
     * Fetch metadata from server and update local cache
     */
    public void fetchAndCacheMetadata(Runnable onComplete) {
        configApi.getMetadata().enqueue(new Callback<ApiResponse<ConfigMetadata>>() {
            @Override
            public void onResponse(Call<ApiResponse<ConfigMetadata>> call, Response<ApiResponse<ConfigMetadata>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    ConfigMetadata meta = response.body().getData();
                    if (meta != null) {
                        cacheMetadata(meta);
                    }
                }
                if (onComplete != null) onComplete.run();
            }

            @Override
            public void onFailure(Call<ApiResponse<ConfigMetadata>> call, Throwable t) {
                // Network failed — fall back to cached variables silently
                if (onComplete != null) onComplete.run();
            }
        });
    }

    private void cacheMetadata(ConfigMetadata meta) {
        SharedPreferences.Editor editor = prefs.edit();
        if (meta.getNotificationCategories() != null) {
            editor.putString(KEY_NOTIF_CATEGORIES, gson.toJson(meta.getNotificationCategories()));
        }
        if (meta.getEventTypes() != null) {
            editor.putString(KEY_EVENT_TYPES, gson.toJson(meta.getEventTypes()));
        }
        if (meta.getGalleryCategories() != null) {
            editor.putString(KEY_GALLERY_CATEGORIES, gson.toJson(meta.getGalleryCategories()));
        }
        if (meta.getFileCategories() != null) {
            editor.putString(KEY_FILE_CATEGORIES, gson.toJson(meta.getFileCategories()));
        }
        editor.apply();
    }

    public List<String> getNotificationCategories() {
        String json = prefs.getString(KEY_NOTIF_CATEGORIES, null);
        if (json != null) {
            Type type = new TypeToken<List<String>>(){}.getType();
            return gson.fromJson(json, type);
        }
        return Arrays.asList(Constants.NOTIFICATION_CATEGORIES); // Offline/initial launch fallback
    }

    public List<String> getEventTypes() {
        String json = prefs.getString(KEY_EVENT_TYPES, null);
        if (json != null) {
            Type type = new TypeToken<List<String>>(){}.getType();
            return gson.fromJson(json, type);
        }
        return Arrays.asList(Constants.EVENT_TYPES);
    }

    public List<String> getGalleryCategories() {
        String json = prefs.getString(KEY_GALLERY_CATEGORIES, null);
        if (json != null) {
            Type type = new TypeToken<List<String>>(){}.getType();
            return gson.fromJson(json, type);
        }
        return Arrays.asList(Constants.GALLERY_CATEGORIES);
    }

    public List<String> getFileCategories() {
        String json = prefs.getString(KEY_FILE_CATEGORIES, null);
        if (json != null) {
            Type type = new TypeToken<List<String>>(){}.getType();
            return gson.fromJson(json, type);
        }
        return Arrays.asList(Constants.FILE_CATEGORIES);
    }
}
