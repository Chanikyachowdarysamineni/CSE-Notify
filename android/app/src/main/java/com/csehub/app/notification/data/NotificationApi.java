package com.csehub.app.notification.data;

import com.csehub.app.core.network.models.ApiResponse;
import com.csehub.app.core.network.models.Notification;

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

public interface NotificationApi {

    @GET("notifications")
    Call<ApiResponse<List<Notification>>> getNotifications(
            @Query("page") int page,
            @Query("limit") int limit,
            @Query("category") String category,
            @Query("priority") String priority,
            @Query("search") String search,
            @Query("unreadOnly") String unreadOnly
    );

    @GET("notifications/{id}")
    Call<ApiResponse<Notification>> getNotificationById(@Path("id") String id);

    @Multipart
    @POST("notifications")
    Call<ApiResponse<Notification>> createNotification(
            @PartMap Map<String, RequestBody> partMap,
            @Part MultipartBody.Part file
    );

    @Multipart
    @PUT("notifications/{id}")
    Call<ApiResponse<Notification>> updateNotification(
            @Path("id") String id,
            @PartMap Map<String, RequestBody> partMap,
            @Part MultipartBody.Part file
    );

    @DELETE("notifications/{id}")
    Call<ApiResponse<Void>> deleteNotification(@Path("id") String id);

    @POST("notifications/{id}/read")
    Call<ApiResponse<Void>> markAsRead(@Path("id") String id);
}
