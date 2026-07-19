package com.csehub.app.auth.ui;

import android.os.Bundle;
import com.csehub.app.core.base.BaseActivity;
import com.csehub.app.databinding.ActivityPrivacyPolicyBinding;

/**
 * Screen displaying the CSE HUB Privacy Policy details
 */
public class PrivacyPolicyActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityPrivacyPolicyBinding binding = ActivityPrivacyPolicyBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Setup Toolbar back navigation
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        binding.toolbar.setNavigationOnClickListener(v -> finish());
    }
}
