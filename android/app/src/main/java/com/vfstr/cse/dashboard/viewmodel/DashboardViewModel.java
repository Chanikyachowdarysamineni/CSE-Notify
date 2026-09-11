package com.vfstr.cse.dashboard.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.vfstr.cse.auth.data.AuthRepository;
import com.vfstr.cse.core.network.models.DashboardData;
import com.vfstr.cse.dashboard.data.DashboardRepository;

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

