package com.csehub.app.profile.ui;

import android.os.Bundle;
import android.view.View;

import androidx.lifecycle.ViewModelProvider;

import com.csehub.app.auth.data.AuthRepository;
import com.csehub.app.core.base.BaseActivity;
import com.csehub.app.databinding.ActivityEditProfileBinding;
import com.csehub.app.profile.viewmodel.ProfileViewModel;

import java.util.HashMap;
import java.util.Map;

public class EditProfileActivity extends BaseActivity {

    private ActivityEditProfileBinding binding;
    private ProfileViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityEditProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewModel = new ViewModelProvider(this).get(ProfileViewModel.class);

        setupInitialValues();
        binding.saveButton.setOnClickListener(v -> saveProfileChanges());
    }

    private void setupInitialValues() {
        viewModel.getProfile().observe(this, resource -> {
            if (resource == null) return;
            if (resource.status == AuthRepository.Resource.Status.SUCCESS && resource.data != null) {
                Map<String, Object> profile = (Map<String, Object>) resource.data.get("profile");
                if (profile != null) {
                    if (profile.containsKey("mobile")) {
                        binding.mobileEditText.setText(String.valueOf(profile.get("mobile")));
                    }
                    if (profile.containsKey("personalEmail")) {
                        binding.personalEmailEditText.setText(String.valueOf(profile.get("personalEmail")));
                    }
                    if (profile.containsKey("collegeEmail")) {
                        binding.collegeEmailEditText.setText(String.valueOf(profile.get("collegeEmail")));
                    }
                    if (profile.containsKey("dob")) {
                        String dob = String.valueOf(profile.get("dob"));
                        if (dob.contains("T")) dob = dob.substring(0, dob.indexOf("T"));
                        binding.dobEditText.setText(dob);
                    }
                    if (profile.containsKey("dayScholarHosteller")) {
                        binding.scholarEditText.setText(String.valueOf(profile.get("dayScholarHosteller")));
                    }
                }
            }
        });
    }

    private void saveProfileChanges() {
        String mobile = binding.mobileEditText.getText().toString().trim();
        String personalEmail = binding.personalEmailEditText.getText().toString().trim();
        String collegeEmail = binding.collegeEmailEditText.getText().toString().trim();
        String dob = binding.dobEditText.getText().toString().trim();
        String scholar = binding.scholarEditText.getText().toString().trim();

        binding.mobileInputLayout.setError(null);
        binding.personalEmailInputLayout.setError(null);
        binding.collegeEmailInputLayout.setError(null);

        boolean isValid = true;
        if (mobile.isEmpty() || mobile.length() < 10) {
            binding.mobileInputLayout.setError("Valid mobile number is required");
            isValid = false;
        }
        if (personalEmail.isEmpty()) {
            binding.personalEmailInputLayout.setError("Personal email is required");
            isValid = false;
        }

        if (!isValid) return;

        Map<String, Object> fields = new HashMap<>();
        fields.put("mobile", mobile);
        fields.put("personalEmail", personalEmail);
        fields.put("collegeEmail", collegeEmail);

        if (!dob.isEmpty()) fields.put("dob", dob);
        if (!scholar.isEmpty()) fields.put("dayScholarHosteller", scholar);

        binding.saveButton.setEnabled(false);

        viewModel.updateProfile(fields).observe(this, resource -> {
            if (resource == null) return;
            switch (resource.status) {
                case SUCCESS:
                    showToast("Profile changes saved successfully!");
                    finish();
                    break;
                case ERROR:
                    binding.saveButton.setEnabled(true);
                    showErrorSnackbar(binding.getRoot(), resource.message);
                    break;
                case LOADING:
                    break;
            }
        });
    }
}
