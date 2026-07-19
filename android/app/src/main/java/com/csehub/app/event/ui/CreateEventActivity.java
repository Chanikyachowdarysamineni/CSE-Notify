package com.csehub.app.event.ui;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
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
import com.csehub.app.databinding.ActivityCreateEventBinding;
import com.csehub.app.event.viewmodel.EventViewModel;
import com.google.android.material.chip.Chip;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;

public class CreateEventActivity extends BaseActivity {

    private ActivityCreateEventBinding binding;
    private EventViewModel viewModel;
    private AcademicRepository academicRepository;
    private Uri selectedImageUri;
    private String selectedDate = "";
    private String selectedTime = "";
    private boolean isEditMode = false;
    private String editEventId = null;

    private final ActivityResultLauncher<String> imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    selectedImageUri = uri;
                    binding.bannerPreview.setImageURI(uri);
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCreateEventBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewModel = new ViewModelProvider(this).get(EventViewModel.class);
        academicRepository = new AcademicRepository();

        setupSpinner();
        loadDynamicChips();
        setupPickers();
        setupListeners();
        
        editEventId = getIntent().getStringExtra(Constants.EXTRA_EVENT_ID);
        if (editEventId != null && !editEventId.isEmpty()) {
            isEditMode = true;
            binding.submitButton.setText("Update Event");
            if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle("Edit Event");
            }
            fetchEventDetails();
        }
    }
    
    private void fetchEventDetails() {
        viewModel.getEventById(editEventId).observe(this, resource -> {
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
    
    private void populateForm(com.csehub.app.core.network.models.Event event) {
        binding.titleEditText.setText(event.getTitle());
        binding.descriptionEditText.setText(event.getDescription());
        binding.eventTypeSpinner.setText(event.getEventType(), false);
        binding.venueEditText.setText(event.getVenue());
        
        if (event.getDate() != null && event.getDate().length() >= 10) {
            selectedDate = event.getDate().substring(0, 10);
            binding.dateButton.setText(selectedDate);
        }
        if (event.getTime() != null) {
            selectedTime = event.getTime();
            binding.timeButton.setText(selectedTime);
        }
        if (event.getRegistrationLink() != null) {
            binding.linkEditText.setText(event.getRegistrationLink());
        }
        
        String bannerUrl = FileUtils.resolveUrl(event.getBannerImage());
        if (!bannerUrl.isEmpty()) {
            com.bumptech.glide.Glide.with(this).load(bannerUrl).into(binding.bannerPreview);
        }
    }

    private void setupSpinner() {
        List<String> eventTypes = com.csehub.app.core.network.ConfigRepository.getInstance(this).getEventTypes();
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this, android.R.layout.simple_dropdown_item_1line, eventTypes
        );
        binding.eventTypeSpinner.setAdapter(adapter);
        if (!eventTypes.isEmpty()) {
            binding.eventTypeSpinner.setText(eventTypes.get(0), false);
        }
    }

    private void setupPickers() {
        binding.dateButton.setOnClickListener(v -> showDatePicker());
        binding.timeButton.setOnClickListener(v -> showTimePicker());
    }

    private void showDatePicker() {
        Calendar cal = Calendar.getInstance();
        DatePickerDialog dialog = new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            selectedDate = String.format(Locale.US, "%d-%02d-%02d", year, month + 1, dayOfMonth);
            binding.dateButton.setText(selectedDate);
        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH));
        dialog.show();
    }

    private void showTimePicker() {
        Calendar cal = Calendar.getInstance();
        TimePickerDialog dialog = new TimePickerDialog(this, (view, hourOfDay, minute) -> {
            selectedTime = String.format(Locale.US, "%02d:%02d", hourOfDay, minute);
            binding.timeButton.setText(selectedTime);
        }, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), true);
        dialog.show();
    }

    private void setupListeners() {
        binding.bannerButton.setOnClickListener(v -> imagePickerLauncher.launch("image/*"));
        binding.submitButton.setOnClickListener(v -> submitEvent());
    }

    private void submitEvent() {
        String title = binding.titleEditText.getText().toString().trim();
        String description = binding.descriptionEditText.getText().toString().trim();
        String eventType = binding.eventTypeSpinner.getText().toString();
        String venue = binding.venueEditText.getText().toString().trim();
        String link = binding.linkEditText.getText().toString().trim();

        binding.titleInputLayout.setError(null);
        binding.descriptionInputLayout.setError(null);
        binding.venueInputLayout.setError(null);

        boolean isValid = true;
        if (title.isEmpty()) {
            binding.titleInputLayout.setError("Title is required");
            isValid = false;
        }
        if (description.isEmpty()) {
            binding.descriptionInputLayout.setError("Description is required");
            isValid = false;
        }
        if (venue.isEmpty()) {
            binding.venueInputLayout.setError("Venue is required");
            isValid = false;
        }
        if (selectedDate.isEmpty()) {
            showToast("Event Date is required");
            isValid = false;
        }
        if (selectedTime.isEmpty()) {
            showToast("Event Time is required");
            isValid = false;
        }

        if (!isValid) return;

        // Years selection from dynamic chips
        List<String> targetYears = getCheckedChipTags(binding.yearChipGroup);
        // Sections selection from dynamic chips
        List<String> targetSections = getCheckedChipTags(binding.sectionChipGroup);
        // Empty list = all students (backend handles broadcast)

        MultipartBody.Part imagePart = null;
        if (selectedImageUri != null) {
            File file = FileUtils.getFileFromUri(this, selectedImageUri);
            if (file != null) {
                RequestBody requestFile = RequestBody.create(file, MediaType.parse(getContentResolver().getType(selectedImageUri)));
                imagePart = MultipartBody.Part.createFormData("bannerImage", file.getName(), requestFile);
            }
        }

        binding.submitButton.setEnabled(false);

        if (isEditMode) {
            viewModel.updateEvent(editEventId, title, description, eventType, selectedDate, selectedTime, venue, targetYears, targetSections, link, imagePart)
                    .observe(this, resource -> {
                        if (resource == null) return;
                        switch (resource.status) {
                            case SUCCESS:
                                showToast("Event updated successfully!");
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
            viewModel.createEvent(title, description, eventType, selectedDate, selectedTime, venue, targetYears, targetSections, link, imagePart)
                    .observe(this, resource -> {
                        if (resource == null) return;
                        switch (resource.status) {
                            case SUCCESS:
                                showToast("Event published successfully!");
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
