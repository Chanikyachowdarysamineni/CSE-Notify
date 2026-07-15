package com.csehub.app.auth.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.lifecycle.ViewModelProvider;

import com.csehub.app.auth.viewmodel.AuthViewModel;
import com.csehub.app.core.base.BaseActivity;
import com.csehub.app.dashboard.ui.MainActivity;
import com.csehub.app.databinding.ActivityLoginBinding;

/**
 * Login Activity with input validations, progress indications, and secure session establishment
 */
public class LoginActivity extends BaseActivity {

    private ActivityLoginBinding binding;
    private AuthViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        setupObservers();
        setupListeners();
    }

    private void setupObservers() {
        // Observe text input layout validations
        viewModel.getLoginIdError().observe(this, error -> binding.loginIdInputLayout.setError(error));
        viewModel.getPasswordError().observe(this, error -> binding.passwordInputLayout.setError(error));
    }

    private void setupListeners() {
        binding.loginButton.setOnClickListener(v -> attemptLogin());
        binding.forgotPasswordButton.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, ForgotPasswordActivity.class));
        });
    }

    private void attemptLogin() {
        String loginId = binding.loginIdEditText.getText().toString().trim();
        String password = binding.passwordEditText.getText().toString();
        
        // Grab FCM token from manager if loaded
        String fcmToken = tokenManager.getFCMToken();

        binding.loadingSpinner.setVisibility(View.VISIBLE);
        binding.loginButton.setEnabled(false);

        viewModel.login(loginId, password, fcmToken).observe(this, resource -> {
            if (resource == null) return;
            switch (resource.status) {
                case SUCCESS:
                    binding.loadingSpinner.setVisibility(View.GONE);
                    showToast("Welcome " + tokenManager.getUserName());
                    startActivity(new Intent(LoginActivity.this, MainActivity.class));
                    finish();
                    break;
                case ERROR:
                    binding.loadingSpinner.setVisibility(View.GONE);
                    binding.loginButton.setEnabled(true);
                    showErrorSnackbar(binding.getRoot(), resource.message);
                    break;
                case LOADING:
                    binding.loadingSpinner.setVisibility(View.VISIBLE);
                    break;
            }
        });
    }
}
