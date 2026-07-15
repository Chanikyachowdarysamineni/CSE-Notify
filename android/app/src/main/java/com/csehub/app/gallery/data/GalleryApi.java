package com.csehub.app.gallery.data;

import com.csehub.app.core.network.models.ApiResponse;
import com.csehub.app.core.network.models.Gallery;

import java.util.List;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface GalleryApi {

    @GET("gallery")
    Call<ApiResponse<List<Gallery>>> getGalleryPosts(
            @Query("page") int page,
            @Query("limit") int limit,
            @Query("category") String category
    );

    @Multipart
    @POST("gallery")
    Call<ApiResponse<Gallery>> createGalleryPost(
            @Part("caption") RequestBody caption,
            @Part("category") RequestBody category,
            @Part MultipartBody.Part image
    );

    @DELETE("gallery/{id}")
    Call<ApiResponse<Void>> deleteGalleryPost(@Path("id") String id);
}
