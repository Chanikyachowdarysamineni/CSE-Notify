package com.csehub.app.auth.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModelProvider;

import com.csehub.app.auth.viewmodel.AuthViewModel;
import com.csehub.app.core.base.BaseActivity;
import com.csehub.app.dashboard.ui.MainActivity;
import com.csehub.app.databinding.ActivityLoginBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.messaging.FirebaseMessaging;

/**
 * Login Activity
 *
 * Key fix: FCM token is fetched asynchronously via FirebaseMessaging.getInstance().getToken()
 * BEFORE submitting the login request, so the backend always receives a valid FCM token
 * on first login (previously, the cached token could be empty if Firebase hadn't resolved yet).
 */
public class LoginActivity extends BaseActivity {

    private ActivityLoginBinding binding;
    private AuthViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Fetch dynamic categories config from backend asynchronously
        com.csehub.app.core.network.ConfigRepository.getInstance(this).fetchAndCacheMetadata(null);

        // Session routing (replaces SplashActivity)
        if (tokenManager != null && tokenManager.isLoggedIn()) {
            startActivity(new Intent(this, MainActivity.class));
            finish();
            return;
        }

        // Prevent screenshots on login screen
        getWindow().setFlags(
                android.view.WindowManager.LayoutParams.FLAG_SECURE,
                android.view.WindowManager.LayoutParams.FLAG_SECURE);

        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        setupObservers();
        setupListeners();

        // Pre-fetch FCM token in the background while user types credentials.
        // This ensures the token is in TokenManager before attemptLogin() runs.
        prefetchFCMToken();
    }

    private void prefetchFCMToken() {
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        String token = task.getResult();
                        tokenManager.saveFCMToken(token);
                    }
                    // Failures are silently ignored — attemptLogin() will fall back
                    // to the cached token (which may be empty on first install)
                });
    }

    private void setupObservers() {
        viewModel.getLoginIdError().observe(this,
                error -> binding.loginIdInputLayout.setError(error));
        viewModel.getPasswordError().observe(this,
                error -> binding.passwordInputLayout.setError(error));
    }

    private void setupListeners() {
        binding.loginButton.setOnClickListener(v -> attemptLogin());
        binding.forgotPasswordButton.setOnClickListener(v ->
                startActivity(new Intent(LoginActivity.this, ForgotPasswordActivity.class)));
        binding.privacyPolicyLink.setOnClickListener(v ->
                startActivity(new Intent(LoginActivity.this, PrivacyPolicyActivity.class)));
        binding.termsConditionsLink.setOnClickListener(v ->
                startActivity(new Intent(LoginActivity.this, TermsConditionsActivity.class)));
    }

    private void attemptLogin() {
        final String loginId  = binding.loginIdEditText.getText().toString().trim();
        final String password = binding.passwordEditText.getText().toString();

        if (loginId.isEmpty()) {
            binding.loginIdInputLayout.setError("Please enter your login ID");
            return;
        }
        if (password.isEmpty()) {
            binding.passwordInputLayout.setError("Please enter your password");
            return;
        }

        // Clear previous errors
        binding.loginIdInputLayout.setError(null);
        binding.passwordInputLayout.setError(null);

        setLoading(true);

        // Fetch the latest FCM token from Firebase and then login.
        // This guarantees we always send a fresh token, even if prefetch hadn't completed.
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(task -> {
                    String fcmToken;
                    if (task.isSuccessful() && task.getResult() != null) {
                        fcmToken = task.getResult();
                        tokenManager.saveFCMToken(fcmToken);
                    } else {
                        // Fallback to cached token
                        fcmToken = tokenManager.getFCMToken();
                    }
                    performLogin(loginId, password, fcmToken);
                });
    }

    private void performLogin(String loginId, String password, String fcmToken) {
        viewModel.login(loginId, password, fcmToken).observe(this, resource -> {
            if (resource == null) return;
            switch (resource.status) {
                case SUCCESS:
                    setLoading(false);
                    showToast("Welcome " + tokenManager.getUserName() + "!");
                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                    break;
                case ERROR:
                    setLoading(false);
                    showErrorSnackbar(binding.getRoot(), resource.message);
                    break;
                case LOADING:
                    setLoading(true);
                    break;
            }
        });
    }

    private void setLoading(boolean loading) {
        binding.loadingSpinner.setVisibility(loading ? View.VISIBLE : View.GONE);
        binding.loginButton.setEnabled(!loading);
        binding.loginIdEditText.setEnabled(!loading);
        binding.passwordEditText.setEnabled(!loading);
    }
}
