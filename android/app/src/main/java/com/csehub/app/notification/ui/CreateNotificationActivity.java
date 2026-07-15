package com.csehub.app.notification.ui;

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
import com.csehub.app.databinding.ActivityCreateNotificationBinding;
import com.csehub.app.notification.viewmodel.NotificationViewModel;
import com.google.android.material.chip.Chip;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;

public class CreateNotificationActivity extends BaseActivity {

    private ActivityCreateNotificationBinding binding;
    private NotificationViewModel viewModel;
    private AcademicRepository academicRepository;
    private Uri selectedFileUri;
    private String selectedFileName = "";
    private boolean isEditMode = false;
    private String editNotificationId = null;

    // Store loaded academic years / sections for ID lookup
    private List<AcademicYear> loadedYears = new ArrayList<>();
    private List<Section> loadedSections = new ArrayList<>();

    private final ActivityResultLauncher<String> filePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    selectedFileUri = uri;
                    File file = FileUtils.getFileFromUri(this, uri);
                    if (file != null) {
                        selectedFileName = file.getName();
                        binding.attachmentNameText.setText(selectedFileName);
                    }
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCreateNotificationBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewModel = new ViewModelProvider(this).get(NotificationViewModel.class);
        academicRepository = new AcademicRepository();

        setupSpinners();
        loadDynamicChips();
        setupListeners();

        editNotificationId = getIntent().getStringExtra(Constants.EXTRA_NOTIFICATION_ID);
        if (editNotificationId != null && !editNotificationId.isEmpty()) {
            isEditMode = true;
            binding.submitButton.setText("Update Notification");
            if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle("Edit Notification");
            }
            fetchNotificationDetails();
        }
    }

    private void loadDynamicChips() {
        // Load Academic Years
        academicRepository.getAcademicYears().observe(this, response -> {
            if (response != null && response.isSuccess() && response.getData() != null) {
                loadedYears = response.getData();
                binding.yearChipGroup.removeAllViews();
                for (AcademicYear year : loadedYears) {
                    Chip chip = new Chip(this);
                    chip.setText(year.toString());
                    chip.setCheckable(true);
                    chip.setTag(year.getId());
                    chip.setChipBackgroundColorResource(com.google.android.material.R.color.design_default_color_primary);
                    binding.yearChipGroup.addView(chip);
                }
            }
        });

        // Load Sections
        academicRepository.getSections().observe(this, response -> {
            if (response != null && response.isSuccess() && response.getData() != null) {
                loadedSections = response.getData();
                binding.sectionChipGroup.removeAllViews();
                for (Section section : loadedSections) {
                    Chip chip = new Chip(this);
                    chip.setText("Sec " + section.getName());
                    chip.setCheckable(true);
                    chip.setTag(section.getId());
                    binding.sectionChipGroup.addView(chip);
                }
            }
        });
    }

    private void fetchNotificationDetails() {
        viewModel.getNotificationById(editNotificationId).observe(this, resource -> {
            if (resource == null) return;
            switch (resource.status) {
                case SUCCESS:
                    if (resource.data != null) {
                        populateForm(resource.data);
                    }
                    break;
                case ERROR:
                    showErrorSnackbar(binding.getRoot(), resource.message);
                    break;
                case LOADING:
                    break;
            }
        });
    }

    private void populateForm(com.csehub.app.core.network.models.Notification n) {
        binding.titleEditText.setText(n.getTitle());
        binding.messageEditText.setText(n.getMessage());
        binding.categorySpinner.setText(n.getCategory(), false);
        binding.prioritySpinner.setText(n.getPriority(), false);
        if (n.getLink() != null) binding.linkEditText.setText(n.getLink());
        if (n.getAttachmentName() != null && !n.getAttachmentName().isEmpty()) {
            binding.attachmentNameText.setText(n.getAttachmentName());
        }
    }

    private void setupSpinners() {
        // Categories autocomplete
        ArrayAdapter<String> catAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_dropdown_item_1line, Constants.NOTIFICATION_CATEGORIES
        );
        binding.categorySpinner.setAdapter(catAdapter);
        binding.categorySpinner.setText(Constants.NOTIFICATION_CATEGORIES[0], false);

        // Priorities autocomplete
        ArrayAdapter<String> prioAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_dropdown_item_1line, Constants.NOTIFICATION_PRIORITIES
        );
        binding.prioritySpinner.setAdapter(prioAdapter);
        binding.prioritySpinner.setText(Constants.NOTIFICATION_PRIORITIES[1], false); // Default: medium
    }

    private void setupListeners() {
        binding.attachmentButton.setOnClickListener(v -> filePickerLauncher.launch("*/*"));
        binding.submitButton.setOnClickListener(v -> submitNotification());
    }

    private void submitNotification() {
        String title = binding.titleEditText.getText().toString().trim();
        String message = binding.messageEditText.getText().toString().trim();
        String category = binding.categorySpinner.getText().toString();
        String priority = binding.prioritySpinner.getText().toString();
        String link = binding.linkEditText.getText().toString().trim();

        // Validate
        binding.titleInputLayout.setError(null);
        binding.messageInputLayout.setError(null);

        boolean isValid = true;
        if (title.isEmpty()) {
            binding.titleInputLayout.setError("Title is required");
            isValid = false;
        }
        if (message.isEmpty()) {
            binding.messageInputLayout.setError("Message is required");
            isValid = false;
        }

        if (!isValid) return;

        // Gather selected year/section ObjectIds from chip groups
        List<String> targetYears = getCheckedChipTags(binding.yearChipGroup);
        List<String> targetSections = getCheckedChipTags(binding.sectionChipGroup);
        // Empty = target all (backend interprets empty list as broadcast)

        // Build file body if picked
        MultipartBody.Part filePart = null;
        if (selectedFileUri != null) {
            File file = FileUtils.getFileFromUri(this, selectedFileUri);
            if (file != null) {
                RequestBody requestFile = RequestBody.create(file, MediaType.parse(getContentResolver().getType(selectedFileUri)));
                filePart = MultipartBody.Part.createFormData("attachment", file.getName(), requestFile);
            }
        }

        binding.submitButton.setEnabled(false);

        if (isEditMode) {
            viewModel.updateNotification(editNotificationId, title, message, category, priority, targetYears, targetSections, link, filePart)
                    .observe(this, resource -> {
                        if (resource == null) return;
                        switch (resource.status) {
                            case SUCCESS:
                                showToast("Notification updated successfully!");
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
        } else {
            viewModel.createNotification(title, message, category, priority, targetYears, targetSections, link, filePart)
                    .observe(this, resource -> {
                        if (resource == null) return;
                        switch (resource.status) {
                            case SUCCESS:
                                showToast("Notification published successfully!");
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

    /** Gather string tags (ObjectIds) from checked chips in a ChipGroup */
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
