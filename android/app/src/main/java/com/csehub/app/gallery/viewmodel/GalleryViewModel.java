package com.csehub.app.gallery.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.csehub.app.auth.data.AuthRepository;
import com.csehub.app.core.network.models.Gallery;
import com.csehub.app.gallery.data.GalleryRepository;

import java.util.List;

import okhttp3.MultipartBody;

public class GalleryViewModel extends AndroidViewModel {

    private final GalleryRepository repository;

    public GalleryViewModel(@NonNull Application application) {
        super(application);
        this.repository = new GalleryRepository(application);
    }

    public LiveData<AuthRepository.Resource<List<Gallery>>> getGalleryPosts(
            int page, int limit, String category) {
        return repository.getGalleryPosts(page, limit, category);
    }

    public LiveData<AuthRepository.Resource<Gallery>> createGalleryPost(
            String caption, String category, MultipartBody.Part file) {
        return repository.createGalleryPost(caption, category, file);
    }

    public LiveData<AuthRepository.Resource<Void>> deleteGalleryPost(String id) {
        return repository.deleteGalleryPost(id);
    }
}
