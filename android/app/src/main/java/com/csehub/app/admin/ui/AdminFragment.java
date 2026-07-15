package com.csehub.app.admin.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.navigation.Navigation;

import com.csehub.app.R;
import com.csehub.app.core.base.BaseFragment;
import com.csehub.app.databinding.FragmentAdminBinding;

public class AdminFragment extends BaseFragment {

    private FragmentAdminBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentAdminBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setupListeners();
    }

    private void setupListeners() {
        binding.cardManageStudents.setOnClickListener(v -> {
            Navigation.findNavController(requireView()).navigate(R.id.navigation_search_student);
        });

        binding.cardManageFaculty.setOnClickListener(v -> {
            showSnackbar("Manage Faculty Module loading...");
        });

        binding.cardManageTimetable.setOnClickListener(v -> {
            Navigation.findNavController(requireView()).navigate(R.id.navigation_timetable);
        });

        binding.cardManageNotifications.setOnClickListener(v -> {
            Navigation.findNavController(requireView()).navigate(R.id.navigation_notifications);
        });

        binding.cardManageEvents.setOnClickListener(v -> {
            Navigation.findNavController(requireView()).navigate(R.id.navigation_events);
        });

        binding.cardManageGallery.setOnClickListener(v -> {
            Navigation.findNavController(requireView()).navigate(R.id.navigation_gallery);
        });

        binding.cardManageFiles.setOnClickListener(v -> {
            Navigation.findNavController(requireView()).navigate(R.id.navigation_files);
        });

        binding.cardAnalytics.setOnClickListener(v -> showAnalyticsDialog());
    }

    private void showAnalyticsDialog() {
        if (getContext() == null) return;
        new AlertDialog.Builder(getContext(), R.style.Theme_CSEHub)
                .setTitle("Department Analytics Overview")
                .setMessage("• Registered Students: 1,240\n" +
                        "• Active Faculty Members: 48\n" +
                        "• Broadcasted Notices: 184\n" +
                        "• Educational Documents: 92\n" +
                        "• Achievements Posted: 64\n" +
                        "• Push Notification Delivery: 98.4%\n" +
                        "• API Gateway Server Uptime: 99.98%")
                .setPositiveButton("Close", (dialog, which) -> dialog.dismiss())
                .show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
