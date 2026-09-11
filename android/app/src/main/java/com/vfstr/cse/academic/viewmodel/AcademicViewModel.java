package com.vfstr.cse.academic.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.vfstr.cse.academic.data.AcademicRepository;
import com.vfstr.cse.core.network.models.AcademicYear;
import com.vfstr.cse.core.network.models.ApiResponse;
import com.vfstr.cse.core.network.models.Section;

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

