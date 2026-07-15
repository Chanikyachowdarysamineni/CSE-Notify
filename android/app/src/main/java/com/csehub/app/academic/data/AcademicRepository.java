package com.csehub.app.academic.data;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.csehub.app.core.network.ApiClient;
import com.csehub.app.core.network.models.AcademicYear;
import com.csehub.app.core.network.models.ApiResponse;
import com.csehub.app.core.network.models.Section;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AcademicRepository {
    private final AcademicApi academicApi;

    public AcademicRepository() {
        academicApi = ApiClient.getClient().create(AcademicApi.class);
    }

    public LiveData<ApiResponse<List<AcademicYear>>> getAcademicYears() {
        MutableLiveData<ApiResponse<List<AcademicYear>>> data = new MutableLiveData<>();
        academicApi.getAcademicYears().enqueue(new Callback<ApiResponse<List<AcademicYear>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<AcademicYear>>> call, Response<ApiResponse<List<AcademicYear>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    data.setValue(response.body());
                } else {
                    data.setValue(new ApiResponse<>(false, "Failed to load academic years", null));
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<AcademicYear>>> call, Throwable t) {
                data.setValue(new ApiResponse<>(false, t.getMessage(), null));
            }
        });
        return data;
    }

    public LiveData<ApiResponse<List<Section>>> getSections() {
        MutableLiveData<ApiResponse<List<Section>>> data = new MutableLiveData<>();
        academicApi.getSections().enqueue(new Callback<ApiResponse<List<Section>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<Section>>> call, Response<ApiResponse<List<Section>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    data.setValue(response.body());
                } else {
                    data.setValue(new ApiResponse<>(false, "Failed to load sections", null));
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<Section>>> call, Throwable t) {
                data.setValue(new ApiResponse<>(false, t.getMessage(), null));
            }
        });
        return data;
    }
}
