package com.csehub.app.profile.ui;

import android.os.Bundle;
import android.text.InputFilter;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;

import androidx.lifecycle.ViewModelProvider;

import com.csehub.app.auth.data.AuthRepository;
import com.csehub.app.core.base.BaseActivity;
import com.csehub.app.databinding.ActivityEditProfileBinding;
import com.csehub.app.profile.viewmodel.ProfileViewModel;
import com.google.android.material.datepicker.MaterialDatePicker;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

public class EditProfileActivity extends BaseActivity {

    private ActivityEditProfileBinding binding;
    private ProfileViewModel viewModel;
    
    // RegEx patterns matching Backend
    private static final Pattern PAN_PATTERN = Pattern.compile("^[A-Z]{5}[0-9]{4}[A-Z]{1}$");
    private static final Pattern AADHAAR_PATTERN = Pattern.compile("^\\d{12}$");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityEditProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewModel = new ViewModelProvider(this).get(ProfileViewModel.class);
        
        binding.toolbar.setNavigationOnClickListener(v -> finish());

        // Enforce PAN uppercase typing
        binding.etPan.setFilters(new InputFilter[]{new InputFilter.AllCaps()});

        setupDatePicker();
        setupInitialValues();

        binding.btnSave.setOnClickListener(v -> saveProfileChanges());
    }

    private void setupDatePicker() {
        MaterialDatePicker<Long> datePicker = MaterialDatePicker.Builder.datePicker()
                .setTitleText("Select Date of Birth")
                .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
                .build();

        datePicker.addOnPositiveButtonClickListener(selection -> {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
            binding.etDob.setText(sdf.format(new Date(selection)));
        });

        binding.etDob.setOnClickListener(v -> {
            if (!datePicker.isAdded()) {
                datePicker.show(getSupportFragmentManager(), "DOB_PICKER");
            }
        });
    }

    private void setupInitialValues() {
        binding.progressBar.setVisibility(View.VISIBLE);
        viewModel.getProfile().observe(this, resource -> {
            if (resource == null) return;
            if (resource.status == AuthRepository.Resource.Status.SUCCESS && resource.data != null) {
                binding.progressBar.setVisibility(View.GONE);
                Map<String, Object> profile = (Map<String, Object>) resource.data.get("profile");
                if (profile != null) {
                    // Read-only logic mapping (assuming backend returns these)
                    if (profile.containsKey("name")) binding.etName.setText(String.valueOf(profile.get("name")));
                    if (profile.containsKey("regNo")) binding.etRegNo.setText(String.valueOf(profile.get("regNo")));
                    
                    // Editable fields
                    if (profile.containsKey("mobile")) binding.etMobile.setText(String.valueOf(profile.get("mobile")));
                    if (profile.containsKey("personalEmail")) binding.etPersonalEmail.setText(String.valueOf(profile.get("personalEmail")));
                    if (profile.containsKey("collegeEmail")) binding.etCollegeEmail.setText(String.valueOf(profile.get("collegeEmail")));
                    if (profile.containsKey("aadhaarNumber")) binding.etAadhaar.setText(String.valueOf(profile.get("aadhaarNumber")));
                    if (profile.containsKey("panNumber")) binding.etPan.setText(String.valueOf(profile.get("panNumber")));
                    if (profile.containsKey("githubUrl")) binding.etGithub.setText(String.valueOf(profile.get("githubUrl")));
                    if (profile.containsKey("linkedinUrl")) binding.etLinkedin.setText(String.valueOf(profile.get("linkedinUrl")));
                    
                    if (profile.containsKey("dob")) {
                        String dob = String.valueOf(profile.get("dob"));
                        if (dob.contains("T")) dob = dob.substring(0, dob.indexOf("T"));
                        binding.etDob.setText(dob);
                    }
                }
            }
        });
    }

    private void saveProfileChanges() {
        // Clear all errors first
        binding.tilPersonalEmail.setError(null);
        binding.tilCollegeEmail.setError(null);
        binding.tilMobile.setError(null);
        binding.tilAadhaar.setError(null);
        binding.tilPan.setError(null);
        binding.tilGithub.setError(null);
        binding.tilLinkedin.setError(null);

        // Fetch inputs
        String personalEmail = binding.etPersonalEmail.getText().toString().trim();
        String collegeEmail = binding.etCollegeEmail.getText().toString().trim();
        String mobile = binding.etMobile.getText().toString().trim();
        String aadhaar = binding.etAadhaar.getText().toString().trim();
        String pan = binding.etPan.getText().toString().trim().toUpperCase();
        String github = binding.etGithub.getText().toString().trim();
        String linkedin = binding.etLinkedin.getText().toString().trim();
        String dob = binding.etDob.getText().toString().trim();

        boolean isValid = true;

        if (!personalEmail.isEmpty() && !Patterns.EMAIL_ADDRESS.matcher(personalEmail).matches()) {
            binding.tilPersonalEmail.setError("Invalid email format");
            isValid = false;
        }
        if (!collegeEmail.isEmpty() && !collegeEmail.endsWith("@vignan.com")) {
            binding.tilCollegeEmail.setError("Must be a @vignan.com email");
            isValid = false;
        }
        if (!mobile.isEmpty() && mobile.length() < 10) {
            binding.tilMobile.setError("Enter 10 digit mobile");
            isValid = false;
        }
        if (!aadhaar.isEmpty() && !AADHAAR_PATTERN.matcher(aadhaar).matches()) {
            binding.tilAadhaar.setError("Aadhaar must be 12 digits");
            isValid = false;
        }
        if (!pan.isEmpty() && !PAN_PATTERN.matcher(pan).matches()) {
            binding.tilPan.setError("Invalid PAN format (e.g. ABCDE1234F)");
            isValid = false;
        }
        
        if (!isValid) return;

        Map<String, Object> fields = new HashMap<>();
        if (!mobile.isEmpty()) fields.put("mobile", mobile);
        if (!personalEmail.isEmpty()) fields.put("personalEmail", personalEmail);
        if (!collegeEmail.isEmpty()) fields.put("collegeEmail", collegeEmail);
        if (!aadhaar.isEmpty()) fields.put("aadhaarNumber", aadhaar);
        if (!pan.isEmpty()) fields.put("panNumber", pan);
        if (!github.isEmpty()) fields.put("githubUrl", github);
        if (!linkedin.isEmpty()) fields.put("linkedinUrl", linkedin);
        if (!dob.isEmpty()) fields.put("dob", dob);

        binding.btnSave.setEnabled(false);
        binding.progressBar.setVisibility(View.VISIBLE);

        viewModel.updateProfile(fields).observe(this, resource -> {
            binding.progressBar.setVisibility(View.GONE);
            binding.btnSave.setEnabled(true);
            if (resource == null) return;
            switch (resource.status) {
                case SUCCESS:
                    Toast.makeText(this, "Profile changes saved successfully!", Toast.LENGTH_SHORT).show();
                    finish();
                    break;
                case ERROR:
                    Toast.makeText(this, resource.message != null ? resource.message : "Error saving profile", Toast.LENGTH_LONG).show();
                    break;
            }
        });
    }
}
