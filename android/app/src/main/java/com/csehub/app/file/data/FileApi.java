package com.csehub.app.file.data;

import com.csehub.app.core.network.models.ApiResponse;
import com.csehub.app.core.network.models.FileModel;

import java.util.List;
import java.util.Map;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.PartMap;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface FileApi {

    @GET("files")
    Call<ApiResponse<List<FileModel>>> getFiles(
            @Query("page") int page,
            @Query("limit") int limit,
            @Query("category") String category,
            @Query("type") String type,
            @Query("search") String search
    );

    @Multipart
    @POST("files/upload")
    Call<ApiResponse<FileModel>> uploadFile(
            @PartMap Map<String, RequestBody> partMap,
            @Part MultipartBody.Part file
    );

    @DELETE("files/{id}")
    Call<ApiResponse<Void>> deleteFile(@Path("id") String id);
}
