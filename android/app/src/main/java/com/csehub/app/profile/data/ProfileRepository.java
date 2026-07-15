package com.csehub.app.profile.data;

import android.content.Context;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.csehub.app.auth.data.AuthRepository;
import com.csehub.app.core.network.ApiClient;
import com.csehub.app.core.network.models.ApiResponse;

import java.util.Map;

import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileRepository {

    private final ProfileApi profileApi;

    public ProfileRepository(Context context) {
        this.profileApi = ApiClient.createService(ProfileApi.class);
    }

    public LiveData<AuthRepository.Resource<Map<String, Object>>> getProfile() {
        MutableLiveData<AuthRepository.Resource<Map<String, Object>>> result = new MutableLiveData<>();
        result.setValue(AuthRepository.Resource.loading());

        profileApi.getProfile().enqueue(new Callback<ApiResponse<Map<String, Object>>>() {
            @Override
            public void onResponse(Call<ApiResponse<Map<String, Object>>> call, Response<ApiResponse<Map<String, Object>>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    result.setValue(AuthRepository.Resource.success(response.body().getData()));
                } else {
                    result.setValue(AuthRepository.Resource.error("Failed to load profile details"));
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Map<String, Object>>> call, Throwable t) {
                result.setValue(AuthRepository.Resource.error("Connection failed"));
            }
        });

        return result;
    }

    public LiveData<AuthRepository.Resource<Map<String, Object>>> updateProfile(Map<String, Object> fields) {
        MutableLiveData<AuthRepository.Resource<Map<String, Object>>> result = new MutableLiveData<>();
        result.setValue(AuthRepository.Resource.loading());

        profileApi.updateProfile(fields).enqueue(new Callback<ApiResponse<Map<String, Object>>>() {
            @Override
            public void onResponse(Call<ApiResponse<Map<String, Object>>> call, Response<ApiResponse<Map<String, Object>>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    result.setValue(AuthRepository.Resource.success(response.body().getData()));
                } else {
                    result.setValue(AuthRepository.Resource.error("Failed to update profile changes"));
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Map<String, Object>>> call, Throwable t) {
                result.setValue(AuthRepository.Resource.error("Network request timeout"));
            }
        });

        return result;
    }

    public LiveData<AuthRepository.Resource<Map<String, Object>>> updateProfilePhoto(MultipartBody.Part photo) {
        MutableLiveData<AuthRepository.Resource<Map<String, Object>>> result = new MutableLiveData<>();
        result.setValue(AuthRepository.Resource.loading());

        profileApi.updateProfilePhoto(photo).enqueue(new Callback<ApiResponse<Map<String, Object>>>() {
            @Override
            public void onResponse(Call<ApiResponse<Map<String, Object>>> call, Response<ApiResponse<Map<String, Object>>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    result.setValue(AuthRepository.Resource.success(response.body().getData()));
                } else {
                    result.setValue(AuthRepository.Resource.error("Failed to update profile photo"));
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Map<String, Object>>> call, Throwable t) {
                result.setValue(AuthRepository.Resource.error("Server connection timeout"));
            }
        });

        return result;
    }
}
