package com.csehub.app.profile.data;

import com.csehub.app.core.network.models.ApiResponse;

import java.util.Map;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.PUT;
import retrofit2.http.Part;

public interface ProfileApi {

    @GET("profile")
    Call<ApiResponse<Map<String, Object>>> getProfile();

    @PUT("profile")
    Call<ApiResponse<Map<String, Object>>> updateProfile(@Body Map<String, Object> fields);

    @Multipart
    @PUT("profile/photo")
    Call<ApiResponse<Map<String, Object>>> updateProfilePhoto(@Part MultipartBody.Part photo);
}
