package com.csehub.app.search.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.csehub.app.auth.data.AuthRepository;
import com.csehub.app.search.data.SearchRepository;

import java.util.List;
import java.util.Map;

public class SearchViewModel extends AndroidViewModel {

    private final SearchRepository repository;

    public SearchViewModel(@NonNull Application application) {
        super(application);
        this.repository = new SearchRepository(application);
    }

    public LiveData<AuthRepository.Resource<List<Map<String, Object>>>> searchStudent(String regNo) {
        return repository.searchStudent(regNo);
    }
}
