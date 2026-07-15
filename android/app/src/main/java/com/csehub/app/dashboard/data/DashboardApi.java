package com.csehub.app.dashboard.data;

import com.csehub.app.core.network.models.ApiResponse;
import com.csehub.app.core.network.models.DashboardData;

import retrofit2.Call;
import retrofit2.http.GET;

public interface DashboardApi {

    @GET("dashboard")
    Call<ApiResponse<DashboardData>> getDashboardData();
}
