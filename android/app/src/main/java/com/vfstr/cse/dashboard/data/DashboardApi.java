package com.vfstr.cse.dashboard.data;

import com.vfstr.cse.core.network.models.ApiResponse;
import com.vfstr.cse.core.network.models.DashboardData;

import retrofit2.Call;
import retrofit2.http.GET;

public interface DashboardApi {

    @GET("dashboard")
    Call<ApiResponse<DashboardData>> getDashboardData();
}

