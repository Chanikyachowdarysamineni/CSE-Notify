package com.csehub.app.auth.ui;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.splashscreen.SplashScreen;

import com.csehub.app.core.security.TokenManager;
import com.csehub.app.dashboard.ui.MainActivity;
import com.csehub.app.databinding.ActivitySplashBinding;

/**
 * Animated splash screen checking session validation
 */
public class SplashActivity extends AppCompatActivity {

    private ActivitySplashBinding binding;
    private TokenManager tokenManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Handle splash screen transition using API 31+ helper
        SplashScreen splashScreen = SplashScreen.installSplashScreen(this);
        super.onCreate(savedInstanceState);
        
        binding = ActivitySplashBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        tokenManager = TokenManager.getInstance(this);

        // Keep splash visible for 1.5 seconds, then determine route
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (tokenManager.isLoggedIn()) {
                startActivity(new Intent(SplashActivity.this, MainActivity.class));
            } else {
                startActivity(new Intent(SplashActivity.this, LoginActivity.class));
            }
            finish();
        }, 1500);
    }
}
