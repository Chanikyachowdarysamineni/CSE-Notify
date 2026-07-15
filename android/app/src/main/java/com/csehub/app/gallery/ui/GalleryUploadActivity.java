package com.csehub.app.gallery.ui;

import android.net.Uri;
import android.os.Bundle;
import android.widget.ArrayAdapter;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.lifecycle.ViewModelProvider;

import com.csehub.app.core.base.BaseActivity;
import com.csehub.app.core.utils.Constants;
import com.csehub.app.core.utils.FileUtils;
import com.csehub.app.databinding.ActivityGalleryUploadBinding;
import com.csehub.app.gallery.viewmodel.GalleryViewModel;

import java.io.File;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;

public class GalleryUploadActivity extends BaseActivity {

    private ActivityGalleryUploadBinding binding;
    private GalleryViewModel viewModel;
    private Uri selectedImageUri;

    private final ActivityResultLauncher<String> imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    selectedImageUri = uri;
                    binding.imagePreview.setImageURI(uri);
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityGalleryUploadBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewModel = new ViewModelProvider(this).get(GalleryViewModel.class);

        setupSpinner();
        setupListeners();
    }

    private void setupSpinner() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this, android.R.layout.simple_dropdown_item_1line, Constants.GALLERY_CATEGORIES
        );
        binding.categorySpinner.setAdapter(adapter);
        binding.categorySpinner.setText(Constants.GALLERY_CATEGORIES[0], false);
    }

    private void setupListeners() {
        binding.selectImageButton.setOnClickListener(v -> imagePickerLauncher.launch("image/*"));
        binding.submitButton.setOnClickListener(v -> submitPost());
    }

    private void submitPost() {
        String caption = binding.captionEditText.getText().toString().trim();
        String category = binding.categorySpinner.getText().toString();

        binding.captionInputLayout.setError(null);

        boolean isValid = true;
        if (caption.isEmpty()) {
            binding.captionInputLayout.setError("Caption is required");
            isValid = false;
        }
        if (selectedImageUri == null) {
            showToast("Please choose an image to post");
            isValid = false;
        }

        if (!isValid) return;

        File file = FileUtils.getFileFromUri(this, selectedImageUri);
        if (file == null) return;

        RequestBody requestFile = RequestBody.create(file, MediaType.parse(getContentResolver().getType(selectedImageUri)));
        MultipartBody.Part imagePart = MultipartBody.Part.createFormData("image", file.getName(), requestFile);

        binding.submitButton.setEnabled(false);

        viewModel.createGalleryPost(caption, category, imagePart)
                .observe(this, resource -> {
                    if (resource == null) return;
                    switch (resource.status) {
                        case SUCCESS:
                            showToast("Photo posted to gallery!");
                            finish();
                            break;
                        case ERROR:
                            binding.submitButton.setEnabled(true);
                            showErrorSnackbar(binding.getRoot(), resource.message);
                            break;
                        case LOADING:
                            break;
                    }
                });
    }
}
