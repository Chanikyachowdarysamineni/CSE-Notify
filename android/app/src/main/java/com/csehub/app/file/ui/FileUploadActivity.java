package com.csehub.app.file.ui;

import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.lifecycle.ViewModelProvider;

import com.csehub.app.academic.data.AcademicRepository;
import com.csehub.app.core.base.BaseActivity;
import com.csehub.app.core.network.models.AcademicYear;
import com.csehub.app.core.network.models.Section;
import com.csehub.app.core.utils.Constants;
import com.csehub.app.core.utils.FileUtils;
import com.csehub.app.databinding.ActivityFileUploadBinding;
import com.csehub.app.file.viewmodel.FileViewModel;
import com.google.android.material.chip.Chip;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;

public class FileUploadActivity extends BaseActivity {

    private ActivityFileUploadBinding binding;
    private FileViewModel viewModel;
    private AcademicRepository academicRepository;
    private Uri selectedFileUri;
    private String selectedFileName = "";

    private final ActivityResultLauncher<String> filePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    selectedFileUri = uri;
                    File file = FileUtils.getFileFromUri(this, uri);
                    if (file != null) {
                        selectedFileName = file.getName();
                        binding.selectedFileNameText.setText(selectedFileName);
                        if (binding.nameEditText.getText().toString().isEmpty()) {
                            binding.nameEditText.setText(selectedFileName);
                        }
                    }
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityFileUploadBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewModel = new ViewModelProvider(this).get(FileViewModel.class);
        academicRepository = new AcademicRepository();

        setupSpinner();
        loadDynamicChips();
        setupListeners();
    }

    private void loadDynamicChips() {
        academicRepository.getAcademicYears().observe(this, response -> {
            if (response != null && response.isSuccess() && response.getData() != null) {
                binding.yearChipGroup.removeAllViews();
                for (AcademicYear year : response.getData()) {
                    Chip chip = new Chip(this);
                    chip.setText(year.toString());
                    chip.setCheckable(true);
                    chip.setTag(year.getId());
                    binding.yearChipGroup.addView(chip);
                }
            }
        });

        academicRepository.getSections().observe(this, response -> {
            if (response != null && response.isSuccess() && response.getData() != null) {
                binding.sectionChipGroup.removeAllViews();
                for (Section section : response.getData()) {
                    Chip chip = new Chip(this);
                    chip.setText("Sec " + section.getName());
                    chip.setCheckable(true);
                    chip.setTag(section.getId());
                    binding.sectionChipGroup.addView(chip);
                }
            }
        });
    }

    private void setupSpinner() {
        List<String> fileCategories = com.csehub.app.core.network.ConfigRepository.getInstance(this).getFileCategories();
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this, android.R.layout.simple_dropdown_item_1line, fileCategories
        );
        binding.categorySpinner.setAdapter(adapter);
        if (!fileCategories.isEmpty()) {
            binding.categorySpinner.setText(fileCategories.get(0), false);
        }
    }

    private void setupListeners() {
        binding.pickButton.setOnClickListener(v -> filePickerLauncher.launch("*/*"));
        binding.submitButton.setOnClickListener(v -> submitUpload());
    }

    private void submitUpload() {
        String displayName = binding.nameEditText.getText().toString().trim();
        String category = binding.categorySpinner.getText().toString();
        String description = binding.descriptionEditText.getText().toString().trim();

        binding.nameInputLayout.setError(null);

        boolean isValid = true;
        if (displayName.isEmpty()) {
            binding.nameInputLayout.setError("Display Name is required");
            isValid = false;
        }
        if (selectedFileUri == null) {
            showToast("Please choose a file to upload");
            isValid = false;
        }

        if (!isValid) return;

        File file = FileUtils.getFileFromUri(this, selectedFileUri);
        if (file == null) return;

        RequestBody requestFile = RequestBody.create(file, MediaType.parse(getContentResolver().getType(selectedFileUri)));
        MultipartBody.Part filePart = MultipartBody.Part.createFormData("file", file.getName(), requestFile);

        // Gather selected year/section ObjectIds from dynamic chips; empty = broadcast to all
        List<String> targetYears = getCheckedChipTags(binding.yearChipGroup);
        List<String> targetSections = getCheckedChipTags(binding.sectionChipGroup);

        binding.submitButton.setEnabled(false);

        viewModel.uploadFile(displayName, category, description, targetYears, targetSections, filePart)
                .observe(this, resource -> {
                    if (resource == null) return;
                    switch (resource.status) {
                        case SUCCESS:
                            showToast("File uploaded successfully!");
                            finish();
                            break;
                        case ERROR:
                            binding.submitButton.setEnabled(true);
                            showErrorSnackbar(binding.getRoot(), resource.message);
                            break;
                        case LOADING:
                            showToast("Uploading document...");
                            break;
                    }
                });
    }

    private List<String> getCheckedChipTags(com.google.android.material.chip.ChipGroup group) {
        List<String> result = new ArrayList<>();
        for (int i = 0; i < group.getChildCount(); i++) {
            View child = group.getChildAt(i);
            if (child instanceof Chip) {
                Chip chip = (Chip) child;
                if (chip.isChecked() && chip.getTag() != null) {
                    result.add(chip.getTag().toString());
                }
            }
        }
        return result;
    }
}
