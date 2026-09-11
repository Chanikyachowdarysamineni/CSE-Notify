package com.vfstr.cse.core.network;

import com.vfstr.cse.core.network.models.ApiResponse;
import com.vfstr.cse.core.network.models.ConfigMetadata;

import retrofit2.Call;
import retrofit2.http.GET;

/**
 * Retrofit endpoints for dynamic configuration and settings metadata
 */
public interface ConfigApi {

    @GET("config/metadata")
    Call<ApiResponse<ConfigMetadata>> getMetadata();
}

