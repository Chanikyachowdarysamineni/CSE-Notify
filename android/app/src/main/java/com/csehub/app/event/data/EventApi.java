package com.csehub.app.event.data;

import com.csehub.app.core.network.models.ApiResponse;
import com.csehub.app.core.network.models.Event;

import java.util.List;
import java.util.Map;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;
import retrofit2.http.PartMap;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface EventApi {

    @GET("events")
    Call<ApiResponse<List<Event>>> getEvents(
            @Query("page") int page,
            @Query("limit") int limit,
            @Query("eventType") String eventType,
            @Query("search") String search,
            @Query("upcoming") String upcoming
    );

    @GET("events/{id}")
    Call<ApiResponse<Event>> getEventById(@Path("id") String id);

    @Multipart
    @POST("events")
    Call<ApiResponse<Event>> createEvent(
            @PartMap Map<String, RequestBody> partMap,
            @Part MultipartBody.Part bannerImage
    );

    @Multipart
    @PUT("events/{id}")
    Call<ApiResponse<Event>> updateEvent(
            @Path("id") String id,
            @PartMap Map<String, RequestBody> partMap,
            @Part MultipartBody.Part bannerImage
    );

    @DELETE("events/{id}")
    Call<ApiResponse<Void>> deleteEvent(@Path("id") String id);
}
