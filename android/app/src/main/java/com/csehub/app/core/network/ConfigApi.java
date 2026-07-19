package com.csehub.app.core.network;

import com.csehub.app.core.network.models.ApiResponse;
import com.csehub.app.core.network.models.ConfigMetadata;

import retrofit2.Call;
import retrofit2.http.GET;

/**
 * Retrofit endpoints for dynamic configuration and settings metadata
 */
public interface ConfigApi {

    @GET("config/metadata")
    Call<ApiResponse<ConfigMetadata>> getMetadata();
}
