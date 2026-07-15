package com.csehub.app.settings.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.csehub.app.core.base.BaseFragment;
import com.csehub.app.databinding.FragmentHelpBinding;

public class HelpFragment extends BaseFragment {

    private FragmentHelpBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentHelpBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.contactAdminButton.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_SENDTO);
            intent.setData(Uri.parse("mailto:admin@csehub.edu"));
            intent.putExtra(Intent.EXTRA_SUBJECT, "App Support Request");
            startActivity(Intent.createChooser(intent, "Send Email"));
        });

        binding.reportBugButton.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_SENDTO);
            intent.setData(Uri.parse("mailto:admin@csehub.edu"));
            intent.putExtra(Intent.EXTRA_SUBJECT, "Bug Report");
            startActivity(Intent.createChooser(intent, "Report Bug"));
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
