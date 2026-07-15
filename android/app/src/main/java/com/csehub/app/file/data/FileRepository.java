package com.csehub.app.file.data;

import android.content.Context;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.csehub.app.auth.data.AuthRepository;
import com.csehub.app.core.network.ApiClient;
import com.csehub.app.core.network.models.ApiResponse;
import com.csehub.app.core.network.models.FileModel;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FileRepository {

    private final FileApi fileApi;

    public FileRepository(Context context) {
        this.fileApi = ApiClient.createService(FileApi.class);
    }

    public LiveData<AuthRepository.Resource<List<FileModel>>> getFiles(
            int page, int limit, String category, String type, String search) {

        MutableLiveData<AuthRepository.Resource<List<FileModel>>> result = new MutableLiveData<>();
        result.setValue(AuthRepository.Resource.loading());

        fileApi.getFiles(page, limit, category, type, search).enqueue(new Callback<ApiResponse<List<FileModel>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<FileModel>>> call, Response<ApiResponse<List<FileModel>>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    result.setValue(AuthRepository.Resource.success(response.body().getData()));
                } else {
                    result.setValue(AuthRepository.Resource.error("Failed to load documents list"));
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<FileModel>>> call, Throwable t) {
                result.setValue(AuthRepository.Resource.error("Connection failed: " + t.getMessage()));
            }
        });

        return result;
    }

    public LiveData<AuthRepository.Resource<FileModel>> uploadFile(
            String name, String category, String description,
            List<String> targetYears, List<String> targetSections, MultipartBody.Part filePart) {

        MutableLiveData<AuthRepository.Resource<FileModel>> result = new MutableLiveData<>();
        result.setValue(AuthRepository.Resource.loading());

        Map<String, RequestBody> map = new HashMap<>();
        map.put("name", RequestBody.create(name, MediaType.parse("text/plain")));
        map.put("category", RequestBody.create(category, MediaType.parse("text/plain")));
        map.put("description", RequestBody.create(description == null ? "" : description, MediaType.parse("text/plain")));

        String yearsJson = new com.google.gson.Gson().toJson(targetYears);
        String sectionsJson = new com.google.gson.Gson().toJson(targetSections);
        map.put("targetYears", RequestBody.create(yearsJson, MediaType.parse("application/json")));
        map.put("targetSections", RequestBody.create(sectionsJson, MediaType.parse("application/json")));

        fileApi.uploadFile(map, filePart).enqueue(new Callback<ApiResponse<FileModel>>() {
            @Override
            public void onResponse(Call<ApiResponse<FileModel>> call, Response<ApiResponse<FileModel>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    result.setValue(AuthRepository.Resource.success(response.body().getData()));
                } else {
                    result.setValue(AuthRepository.Resource.error("Failed to upload document file"));
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<FileModel>> call, Throwable t) {
                result.setValue(AuthRepository.Resource.error("Upload timeout"));
            }
        });

        return result;
    }

    public LiveData<AuthRepository.Resource<Void>> deleteFile(String id) {
        MutableLiveData<AuthRepository.Resource<Void>> result = new MutableLiveData<>();
        result.setValue(AuthRepository.Resource.loading());

        fileApi.deleteFile(id).enqueue(new Callback<ApiResponse<Void>>() {
            @Override
            public void onResponse(Call<ApiResponse<Void>> call, Response<ApiResponse<Void>> response) {
                if (response.isSuccessful()) {
                    result.setValue(AuthRepository.Resource.success(null));
                } else {
                    result.setValue(AuthRepository.Resource.error("Failed to delete document"));
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
