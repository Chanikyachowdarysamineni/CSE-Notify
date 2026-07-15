package com.csehub.app.gallery.data;

import android.content.Context;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.csehub.app.auth.data.AuthRepository;
import com.csehub.app.core.network.ApiClient;
import com.csehub.app.core.network.models.ApiResponse;
import com.csehub.app.core.network.models.Gallery;

import java.util.List;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GalleryRepository {

    private final GalleryApi galleryApi;

    public GalleryRepository(Context context) {
        this.galleryApi = ApiClient.createService(GalleryApi.class);
    }

    public LiveData<AuthRepository.Resource<List<Gallery>>> getGalleryPosts(
            int page, int limit, String category) {
        
        MutableLiveData<AuthRepository.Resource<List<Gallery>>> result = new MutableLiveData<>();
        result.setValue(AuthRepository.Resource.loading());

        galleryApi.getGalleryPosts(page, limit, category).enqueue(new Callback<ApiResponse<List<Gallery>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<Gallery>>> call, Response<ApiResponse<List<Gallery>>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    result.setValue(AuthRepository.Resource.success(response.body().getData()));
                } else {
                    result.setValue(AuthRepository.Resource.error("Failed to load gallery posts"));
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<Gallery>>> call, Throwable t) {
                result.setValue(AuthRepository.Resource.error("Connection failed: " + t.getMessage()));
            }
        });

        return result;
    }

    public LiveData<AuthRepository.Resource<Gallery>> createGalleryPost(
            String caption, String category, MultipartBody.Part imagePart) {

        MutableLiveData<AuthRepository.Resource<Gallery>> result = new MutableLiveData<>();
        result.setValue(AuthRepository.Resource.loading());

        RequestBody capPart = RequestBody.create(caption, MediaType.parse("text/plain"));
        RequestBody catPart = RequestBody.create(category, MediaType.parse("text/plain"));

        galleryApi.createGalleryPost(capPart, catPart, imagePart).enqueue(new Callback<ApiResponse<Gallery>>() {
            @Override
            public void onResponse(Call<ApiResponse<Gallery>> call, Response<ApiResponse<Gallery>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    result.setValue(AuthRepository.Resource.success(response.body().getData()));
                } else {
                    result.setValue(AuthRepository.Resource.error("Failed to post image to gallery"));
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Gallery>> call, Throwable t) {
                result.setValue(AuthRepository.Resource.error("Server connection timeout"));
            }
        });

        return result;
    }

    public LiveData<AuthRepository.Resource<Void>> deleteGalleryPost(String id) {
        MutableLiveData<AuthRepository.Resource<Void>> result = new MutableLiveData<>();
        result.setValue(AuthRepository.Resource.loading());

        galleryApi.deleteGalleryPost(id).enqueue(new Callback<ApiResponse<Void>>() {
            @Override
            public void onResponse(Call<ApiResponse<Void>> call, Response<ApiResponse<Void>> response) {
                if (response.isSuccessful()) {
                    result.setValue(AuthRepository.Resource.success(null));
                } else {
                    result.setValue(AuthRepository.Resource.error("Failed to delete gallery post"));
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Void>> call, Throwable t) {
                result.setValue(AuthRepository.Resource.error("Server connection timeout"));
            }
        });

        return result;
    }
}
