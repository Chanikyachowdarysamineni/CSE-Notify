package com.csehub.app.core.base;

import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.csehub.app.core.security.TokenManager;
import com.google.android.material.snackbar.Snackbar;

/**
 * Base Fragment with common functionality
 */
public abstract class BaseFragment extends Fragment {

    protected TokenManager tokenManager;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        tokenManager = TokenManager.getInstance(requireContext());
    }

    protected void showLoading(ProgressBar progressBar) {
        if (progressBar != null) progressBar.setVisibility(View.VISIBLE);
    }

    protected void hideLoading(ProgressBar progressBar) {
        if (progressBar != null) progressBar.setVisibility(View.GONE);
    }

    protected void showToast(String message) {
        if (getContext() != null) {
            Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
        }
    }

    protected void showSnackbar(String message) {
        View view = getView();
        if (view != null) {
            Snackbar.make(view, message, Snackbar.LENGTH_SHORT).show();
        }
    }

    protected void showErrorSnackbar(String message) {
        View view = getView();
        if (view != null) {
            Snackbar.make(view, message, Snackbar.LENGTH_LONG)
                    .setBackgroundTint(getResources().getColor(
                            com.google.android.material.R.color.design_default_color_error,
                            requireContext().getTheme()))
                    .show();
        }
    }

    protected boolean isAdmin() {
        return tokenManager != null && tokenManager.isAdmin();
    }

    protected boolean isFaculty() {
        return tokenManager != null && tokenManager.isFaculty();
    }

    protected boolean isStudent() {
        return tokenManager != null && tokenManager.isStudent();
    }

    protected boolean canCreate() {
        return tokenManager != null && tokenManager.canCreate();
    }
}
