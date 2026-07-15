package com.csehub.app.search.data;

import com.csehub.app.core.network.models.ApiResponse;

import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface SearchApi {

    @GET("search/student")
    Call<ApiResponse<List<Map<String, Object>>>> searchStudent(@Query("regNo") String regNo);
}
