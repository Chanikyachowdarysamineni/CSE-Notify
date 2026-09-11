package com.vfstr.cse.auth.ui;

import android.os.Bundle;
import com.vfstr.cse.core.base.BaseActivity;
import com.vfstr.cse.databinding.ActivityTermsConditionsBinding;

/**
 * Screen displaying the CSE HUB Terms and Conditions details
 */
public class TermsConditionsActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityTermsConditionsBinding binding = ActivityTermsConditionsBinding.inflate(getLayoutInflater());
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

