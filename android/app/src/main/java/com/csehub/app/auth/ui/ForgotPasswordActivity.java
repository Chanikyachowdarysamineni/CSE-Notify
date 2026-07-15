package com.csehub.app.auth.ui;

import android.os.Bundle;
import android.view.View;

import androidx.lifecycle.ViewModelProvider;

import com.csehub.app.auth.viewmodel.AuthViewModel;
import com.csehub.app.core.base.BaseActivity;
import com.csehub.app.databinding.ActivityForgotPasswordBinding;

/**
 * Forgot password request flow
 */
public class ForgotPasswordActivity extends BaseActivity {

    private ActivityForgotPasswordBinding binding;
    private AuthViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityForgotPasswordBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        setupObservers();
        setupListeners();
    }

    private void setupObservers() {
        viewModel.getEmailError().observe(this, error -> binding.emailInputLayout.setError(error));
    }

    private void setupListeners() {
        binding.backButton.setOnClickListener(v -> finish());
        binding.resetButton.setOnClickListener(v -> attemptReset());
    }

    private void attemptReset() {
        String email = binding.emailEditText.getText().toString().trim();

        binding.loadingSpinner.setVisibility(View.VISIBLE);
        binding.resetButton.setEnabled(false);

        viewModel.forgotPassword(email).observe(this, resource -> {
            if (resource == null) return;
            switch (resource.status) {
                case SUCCESS:
                    binding.loadingSpinner.setVisibility(View.GONE);
                    showLongToast("If registered, instructions have been sent to your email!");
                    finish();
                    break;
                case ERROR:
                    binding.loadingSpinner.setVisibility(View.GONE);
                    binding.resetButton.setEnabled(true);
                    showErrorSnackbar(binding.getRoot(), resource.message);
                    break;
                case LOADING:
                    break;
            }
        });
    }
}
