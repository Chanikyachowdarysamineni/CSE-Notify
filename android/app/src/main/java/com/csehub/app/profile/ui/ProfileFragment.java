package com.csehub.app.profile.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.csehub.app.R;
import com.csehub.app.core.base.BaseFragment;
import com.csehub.app.auth.data.AuthRepository;
import com.csehub.app.auth.ui.LoginActivity;
import com.csehub.app.core.utils.FileUtils;
import com.csehub.app.core.security.TokenManager;
import com.csehub.app.databinding.FragmentProfileBinding;
import com.csehub.app.profile.viewmodel.ProfileViewModel;

import java.io.File;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;

public class ProfileFragment extends BaseFragment {

    private FragmentProfileBinding binding;
    private ProfileViewModel viewModel;

    // Photo picker launcher
    private final ActivityResultLauncher<String> photoPickerLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    uploadPhoto(uri);
                }
            }
    );

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentProfileBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(ProfileViewModel.class);

        setupListeners();
        fetchProfile();
    }

    private void setupListeners() {
        binding.editButton.setOnClickListener(v -> {
            startActivity(new Intent(requireActivity(), EditProfileActivity.class));
        });

        binding.settingsButton.setOnClickListener(v -> {
            androidx.navigation.Navigation.findNavController(v).navigate(R.id.navigation_settings);
        });

        binding.logoutButton.setOnClickListener(v -> {
            new AuthRepository(requireContext()).logout(() -> {
                Intent intent = new Intent(requireActivity(), LoginActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                requireActivity().finish();
            });
        });

        // Click on photo to update it
        binding.profilePhoto.setOnClickListener(v -> photoPickerLauncher.launch("image/*"));
    }

    private void fetchProfile() {
        viewModel.getProfile().observe(getViewLifecycleOwner(), resource -> {
            if (resource == null) return;
            switch (resource.status) {
                case SUCCESS:
                    bindProfileData(resource.data);
                    break;
                case ERROR:
                    showErrorSnackbar(resource.message);
                    break;
                case LOADING:
                    break;
            }
        });
    }

    private void bindProfileData(Map<String, Object> data) {
        if (data == null) return;

        // Parse user details
        Map<String, Object> user = (Map<String, Object>) data.get("user");
        Map<String, Object> profile = (Map<String, Object>) data.get("profile");

        if (user != null) {
            String name = String.valueOf(user.get("name"));
            String role = String.valueOf(user.get("role"));
            binding.profileName.setText(name);
            binding.profileRole.setText(role.toUpperCase());
        }

        if (profile != null) {
            // Check student vs faculty unique IDs
            String uniqueId = profile.containsKey("regNo")
                    ? String.valueOf(profile.get("regNo"))
                    : (profile.containsKey("employeeId") ? String.valueOf(profile.get("employeeId")) : "N/A");
            binding.detailId.setText(uniqueId);

            String mobile = profile.containsKey("mobile") ? String.valueOf(profile.get("mobile")) : "N/A";
            binding.detailMobile.setText(mobile);

            String collegeEmail = profile.containsKey("collegeEmail") ? String.valueOf(profile.get("collegeEmail")) : "N/A";
            binding.detailCollegeEmail.setText(collegeEmail);

            if (profile.containsKey("section")) {
                binding.sectionContainer.setVisibility(View.VISIBLE);
                Object sectionObj = profile.get("section");
                if (sectionObj instanceof Map) {
                    binding.detailSection.setText(String.valueOf(((Map<String, Object>) sectionObj).get("name")));
                } else {
                    binding.detailSection.setText(String.valueOf(sectionObj));
                }
            } else {
                binding.sectionContainer.setVisibility(View.GONE);
            }

            if (profile.containsKey("dob")) {
                binding.dobContainer.setVisibility(View.VISIBLE);
                String dob = String.valueOf(profile.get("dob"));
                if (dob.contains("T")) dob = dob.substring(0, dob.indexOf("T"));
                binding.detailDob.setText(dob);
            } else {
                binding.dobContainer.setVisibility(View.GONE);
            }

            if (profile.containsKey("dayScholarHosteller")) {
                binding.scholarContainer.setVisibility(View.VISIBLE);
                binding.detailScholar.setText(String.valueOf(profile.get("dayScholarHosteller")));
            } else {
                binding.scholarContainer.setVisibility(View.GONE);
            }

            // Bind student CGPA details
            TokenManager tokenManager = TokenManager.getInstance(requireContext());
            if (tokenManager.isStudent() && profile.containsKey("cgpa")) {
                binding.cgpaContainer.setVisibility(View.VISIBLE);
                binding.detailCgpa.setText(String.valueOf(profile.get("cgpa")));
            } else {
                binding.cgpaContainer.setVisibility(View.GONE);
            }

            // Bind profile photo
            String photoField = profile.containsKey("profilePhoto") ? "profilePhoto" : "photo";
            String photoPath = String.valueOf(profile.get(photoField));
            String photoUrl = FileUtils.resolveUrl(photoPath);

            if (!photoUrl.isEmpty() && !"null".equals(photoPath)) {
                Glide.with(this)
                        .load(photoUrl)
                        .placeholder(R.drawable.ic_profile)
                        .error(R.drawable.ic_profile)
                        .into(binding.profilePhoto);
            } else {
                binding.profilePhoto.setImageResource(R.drawable.ic_profile);
            }
        }
    }

    private void uploadPhoto(Uri uri) {
        File file = FileUtils.getFileFromUri(requireContext(), uri);
        if (file == null) return;

        RequestBody requestFile = RequestBody.create(file, MediaType.parse(requireContext().getContentResolver().getType(uri)));
        MultipartBody.Part body = MultipartBody.Part.createFormData("photo", file.getName(), requestFile);

        viewModel.updateProfilePhoto(body).observe(getViewLifecycleOwner(), resource -> {
            if (resource == null) return;
            switch (resource.status) {
                case SUCCESS:
                    showToast("Profile photo updated successfully!");
                    fetchProfile();
                    break;
                case ERROR:
                    showErrorSnackbar(resource.message);
                    break;
                case LOADING:
                    break;
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        fetchProfile();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
