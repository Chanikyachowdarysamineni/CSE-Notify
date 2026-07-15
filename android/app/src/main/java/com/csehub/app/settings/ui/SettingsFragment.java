package com.csehub.app.settings.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.navigation.Navigation;

import com.csehub.app.R;
import com.csehub.app.auth.data.AuthRepository;
import com.csehub.app.auth.ui.LoginActivity;
import com.csehub.app.core.base.BaseFragment;
import com.csehub.app.databinding.FragmentSettingsBinding;

public class SettingsFragment extends BaseFragment {

    private FragmentSettingsBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentSettingsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.themeSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            showToast("Theme changed (Coming soon)");
        });

        binding.notificationSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            showToast("Notification preferences updated");
        });

        binding.aboutContainer.setOnClickListener(v -> {
            Navigation.findNavController(v).navigate(R.id.navigation_about);
        });

        binding.helpContainer.setOnClickListener(v -> {
            Navigation.findNavController(v).navigate(R.id.navigation_help);
        });

        binding.logoutButton.setOnClickListener(v -> {
            new AuthRepository(requireContext()).logout(() -> {
                Intent intent = new Intent(requireActivity(), LoginActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                requireActivity().finish();
            });
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
