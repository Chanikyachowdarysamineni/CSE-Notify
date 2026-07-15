package com.csehub.app.academic.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.csehub.app.academic.data.AcademicRepository;
import com.csehub.app.core.network.models.AcademicYear;
import com.csehub.app.core.network.models.ApiResponse;
import com.csehub.app.core.network.models.Section;

import java.util.List;

public class AcademicViewModel extends AndroidViewModel {
    private final AcademicRepository repository;

    public AcademicViewModel(@NonNull Application application) {
        super(application);
        repository = new AcademicRepository();
    }

    public LiveData<ApiResponse<List<AcademicYear>>> getAcademicYears() {
        return repository.getAcademicYears();
    }

    public LiveData<ApiResponse<List<Section>>> getSections() {
        return repository.getSections();
    }
}
