package com.csehub.app.search.data;

import android.content.Context;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.csehub.app.auth.data.AuthRepository;
import com.csehub.app.core.network.ApiClient;
import com.csehub.app.core.network.models.ApiResponse;

import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SearchRepository {

    private final SearchApi searchApi;

    public SearchRepository(Context context) {
        this.searchApi = ApiClient.createService(SearchApi.class);
    }

    public LiveData<AuthRepository.Resource<List<Map<String, Object>>>> searchStudent(String regNo) {
        MutableLiveData<AuthRepository.Resource<List<Map<String, Object>>>> result = new MutableLiveData<>();
        result.setValue(AuthRepository.Resource.loading());

        searchApi.searchStudent(regNo).enqueue(new Callback<ApiResponse<List<Map<String, Object>>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<Map<String, Object>>>> call, Response<ApiResponse<List<Map<String, Object>>>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    result.setValue(AuthRepository.Resource.success(response.body().getData()));
                } else {
                    String msg = "No students found";
                    if (response.body() != null) {
                        msg = response.body().getMessage();
                    }
                    result.setValue(AuthRepository.Resource.error(msg));
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<Map<String, Object>>>> call, Throwable t) {
                result.setValue(AuthRepository.Resource.error("Failed to connect: " + t.getMessage()));
            }
        });

        return result;
    }
}
