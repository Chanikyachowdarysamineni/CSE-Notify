package com.csehub.app.auth.ui;

import android.os.Bundle;
import android.view.View;

import androidx.lifecycle.ViewModelProvider;

import com.csehub.app.auth.viewmodel.AuthViewModel;
import com.csehub.app.core.base.BaseActivity;
import com.csehub.app.databinding.ActivityChangePasswordBinding;

/**
 * Change password workflow
 */
public class ChangePasswordActivity extends BaseActivity {

    private ActivityChangePasswordBinding binding;
    private AuthViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChangePasswordBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        binding.saveButton.setOnClickListener(v -> attemptChange());
    }

    private void attemptChange() {
        String currentPassword = binding.currentPasswordEditText.getText().toString();
        String newPassword = binding.newPasswordEditText.getText().toString();
        String confirmPassword = binding.confirmPasswordEditText.getText().toString();

        binding.currentPasswordInputLayout.setError(null);
        binding.newPasswordInputLayout.setError(null);
        binding.confirmPasswordInputLayout.setError(null);

        boolean isValid = true;
        if (currentPassword.isEmpty()) {
            binding.currentPasswordInputLayout.setError("Required field");
            isValid = false;
        }
        if (newPassword.length() < 6) {
            binding.newPasswordInputLayout.setError("Must be at least 6 characters");
            isValid = false;
        }
        if (!newPassword.equals(confirmPassword)) {
            binding.confirmPasswordInputLayout.setError("Passwords do not match");
            isValid = false;
        }

        if (!isValid) return;

        binding.loadingSpinner.setVisibility(View.VISIBLE);
        binding.saveButton.setEnabled(false);

        viewModel.changePassword(currentPassword, newPassword).observe(this, resource -> {
            if (resource == null) return;
            switch (resource.status) {
                case SUCCESS:
                    binding.loadingSpinner.setVisibility(View.GONE);
                    showToast("Password updated successfully!");
                    finish();
                    break;
                case ERROR:
                    binding.loadingSpinner.setVisibility(View.GONE);
                    binding.saveButton.setEnabled(true);
                    showErrorSnackbar(binding.getRoot(), resource.message);
                    break;
                case LOADING:
                    break;
            }
        });
    }
}
