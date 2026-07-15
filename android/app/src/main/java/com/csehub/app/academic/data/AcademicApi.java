package com.csehub.app.academic.data;

import com.csehub.app.core.network.models.AcademicYear;
import com.csehub.app.core.network.models.ApiResponse;
import com.csehub.app.core.network.models.Section;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;

public interface AcademicApi {
    @GET("academic-years")
    Call<ApiResponse<List<AcademicYear>>> getAcademicYears();

    @GET("sections")
    Call<ApiResponse<List<Section>>> getSections();
}
