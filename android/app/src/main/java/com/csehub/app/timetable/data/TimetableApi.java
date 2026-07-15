package com.csehub.app.timetable.data;

import com.csehub.app.core.network.models.ApiResponse;
import com.csehub.app.core.network.models.Timetable;

import java.util.List;
import java.util.Map;

import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Query;

public interface TimetableApi {

    @GET("timetable/daily")
    Call<ApiResponse<List<Timetable>>> getDailyTimetable(
            @Query("academicYear") String academicYear,
            @Query("section") String section,
            @Query("day") String day
    );

    @GET("timetable/weekly")
    Call<ApiResponse<Map<String, List<Timetable>>>> getWeeklyTimetable(
            @Query("academicYear") String academicYear,
            @Query("section") String section
    );

    @Multipart
    @POST("timetable/import-csv")
    Call<ApiResponse<Void>> importCSV(@Part MultipartBody.Part file);
}
