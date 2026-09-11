package com.vfstr.cse.file.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.vfstr.cse.auth.data.AuthRepository;
import com.vfstr.cse.core.network.models.FileModel;
import com.vfstr.cse.file.data.FileRepository;

import java.util.List;

import okhttp3.MultipartBody;

public class FileViewModel extends AndroidViewModel {

    private final FileRepository repository;

    public FileViewModel(@NonNull Application application) {
        super(application);
        this.repository = new FileRepository(application);
    }

    public LiveData<AuthRepository.Resource<List<FileModel>>> getFiles(
            int page, int limit, String category, String type, String search) {
        return repository.getFiles(page, limit, category, type, search);
    }

    public LiveData<AuthRepository.Resource<FileModel>> uploadFile(
            String name, String category, String description,
            List<String> targetYears, List<String> targetSections, MultipartBody.Part file) {
        return repository.uploadFile(name, category, description, targetYears, targetSections, file);
    }

    public LiveData<AuthRepository.Resource<Void>> deleteFile(String id) {
        return repository.deleteFile(id);
    }
}

