package com.csehub.app.academic.network;

import com.csehub.app.academic.models.AcademicYear;
import com.csehub.app.academic.models.Section;
import com.csehub.app.core.network.models.ApiResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface AcademicApi {

    // --- Academic Years ---
    @GET("academic-year")
    Call<ApiResponse<List<AcademicYear>>> getAcademicYears(@Query("status") String status);

    @POST("academic-year")
    Call<ApiResponse<AcademicYear>> createAcademicYear(@Body AcademicYear year);

    @PUT("academic-year/{id}")
    Call<ApiResponse<AcademicYear>> updateAcademicYear(@Path("id") String id, @Body AcademicYear year);

    @DELETE("academic-year/{id}")
    Call<ApiResponse<Void>> deleteAcademicYear(@Path("id") String id);

    // --- Sections ---
    @GET("sections")
    Call<ApiResponse<List<Section>>> getSections(@Query("academicYear") String yearId, @Query("status") String status);

    @POST("sections")
    Call<ApiResponse<Section>> createSection(@Body Section section);

    @PUT("sections/{id}")
    Call<ApiResponse<Section>> updateSection(@Path("id") String id, @Body Section section);

    @DELETE("sections/{id}")
    Call<ApiResponse<Void>> deleteSection(@Path("id") String id);
}
