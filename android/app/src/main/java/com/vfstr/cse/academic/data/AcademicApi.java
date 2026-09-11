package com.vfstr.cse.academic.data;

import com.vfstr.cse.core.network.models.AcademicYear;
import com.vfstr.cse.core.network.models.ApiResponse;
import com.vfstr.cse.core.network.models.Section;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;

public interface AcademicApi {
    @GET("academic-years")
    Call<ApiResponse<List<AcademicYear>>> getAcademicYears();

    @GET("sections")
    Call<ApiResponse<List<Section>>> getSections();
}

