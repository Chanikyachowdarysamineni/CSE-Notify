package com.csehub.app.dashboard.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.csehub.app.auth.data.AuthRepository;
import com.csehub.app.core.network.models.DashboardData;
import com.csehub.app.dashboard.data.DashboardRepository;

public class DashboardViewModel extends AndroidViewModel {

    private final DashboardRepository repository;

    public DashboardViewModel(@NonNull Application application) {
        super(application);
        this.repository = new DashboardRepository(application);
    }

    public LiveData<AuthRepository.Resource<DashboardData>> getDashboardData() {
        return repository.getDashboardData();
    }
}
