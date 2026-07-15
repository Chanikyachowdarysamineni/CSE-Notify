package com.csehub.app.profile.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.csehub.app.auth.data.AuthRepository;
import com.csehub.app.profile.data.ProfileRepository;

import java.util.Map;

import okhttp3.MultipartBody;

public class ProfileViewModel extends AndroidViewModel {

    private final ProfileRepository repository;

    public ProfileViewModel(@NonNull Application application) {
        super(application);
        this.repository = new ProfileRepository(application);
    }

    public LiveData<AuthRepository.Resource<Map<String, Object>>> getProfile() {
        return repository.getProfile();
    }

    public LiveData<AuthRepository.Resource<Map<String, Object>>> updateProfile(Map<String, Object> fields) {
        return repository.updateProfile(fields);
    }

    public LiveData<AuthRepository.Resource<Map<String, Object>>> updateProfilePhoto(MultipartBody.Part photo) {
        return repository.updateProfilePhoto(photo);
    }
}
