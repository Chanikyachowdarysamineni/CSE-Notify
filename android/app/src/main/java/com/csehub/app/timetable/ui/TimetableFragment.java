package com.csehub.app.timetable.ui;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.csehub.app.R;
import com.csehub.app.core.base.BaseFragment;
import com.csehub.app.core.network.models.AcademicYear;
import com.csehub.app.core.network.models.Section;
import com.csehub.app.core.network.models.Timetable;
import com.csehub.app.core.utils.Constants;
import com.csehub.app.core.utils.FileUtils;
import com.csehub.app.databinding.FragmentTimetableBinding;
import com.csehub.app.profile.data.ProfileRepository;
import com.csehub.app.timetable.ui.adapter.TimetableDayAdapter;
import com.csehub.app.timetable.viewmodel.TimetableViewModel;
import com.google.android.material.tabs.TabLayout;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;

public class TimetableFragment extends BaseFragment {

    private FragmentTimetableBinding binding;
    private TimetableViewModel viewModel;
    private TimetableDayAdapter adapter;
    private String currentSelectedDay = "Monday";

    // Student's dynamically resolved academicYearId and sectionId
    private String studentAcademicYearId = null;
    private String studentSectionId = null;
    private boolean profileLoaded = false;

    // Launcher for CSV imports (Admin only)
    private final ActivityResultLauncher<String> csvPickerLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    uploadCSV(uri);
                }
            }
    );

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentTimetableBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(TimetableViewModel.class);

        setupTabLayout();
        setupRecyclerView();

        // For students, load their year/section from profile first
        if (isStudent()) {
            loadStudentProfileThenFetch();
        } else {
            // For faculty/admin, the backend resolves automatically – pass null
            fetchTimetable();
        }

        binding.swipeRefresh.setOnRefreshListener(() -> {
            if (isStudent() && !profileLoaded) {
                loadStudentProfileThenFetch();
            } else {
                fetchTimetable();
            }
        });
    }

    private void loadStudentProfileThenFetch() {
        binding.swipeRefresh.setRefreshing(true);
        ProfileRepository profileRepo = new ProfileRepository(requireContext());
        profileRepo.getProfile().observe(getViewLifecycleOwner(), response -> {
            if (response != null && response.status == com.csehub.app.auth.data.AuthRepository.Resource.Status.SUCCESS && response.data != null) {
                Map<String, Object> data = response.data;
                Map<String, Object> profile = (Map<String, Object>) data.get("profile");
                if (profile != null) {
                    Object ayObj = profile.get("academicYear");
                    Object secObj = profile.get("section");

                    // API returns populated objects with _id and name
                    if (ayObj instanceof Map) {
                        studentAcademicYearId = String.valueOf(((Map<String, Object>) ayObj).get("_id"));
                    } else if (ayObj != null) {
                        studentAcademicYearId = String.valueOf(ayObj);
                    }

                    if (secObj instanceof Map) {
                        studentSectionId = String.valueOf(((Map<String, Object>) secObj).get("_id"));
                    } else if (secObj != null) {
                        studentSectionId = String.valueOf(secObj);
                    }

                    profileLoaded = true;
                }
            }
            // Fetch timetable even if profile lookup failed (backend will handle)
            fetchTimetable();
        });
    }

    private void setupTabLayout() {
        for (String day : Constants.DAYS) {
            binding.dayTabLayout.addTab(binding.dayTabLayout.newTab().setText(day));
        }

        binding.dayTabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                currentSelectedDay = tab.getText().toString();
                fetchTimetable();
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    private void setupRecyclerView() {
        adapter = new TimetableDayAdapter();
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerView.setAdapter(adapter);
    }

    private void fetchTimetable() {
        binding.swipeRefresh.setRefreshing(true);

        // Pass the student's academicYearId and sectionId.
        // For faculty/admin, the backend resolves via JWT role automatically.
        String ayId = (studentAcademicYearId != null) ? studentAcademicYearId : "";
        String secId = (studentSectionId != null) ? studentSectionId : "";

        viewModel.getDailyTimetable(ayId, secId, currentSelectedDay)
                .observe(getViewLifecycleOwner(), resource -> {
                    if (resource == null) return;
                    switch (resource.status) {
                        case SUCCESS:
                            binding.swipeRefresh.setRefreshing(false);
                            if (resource.data != null && resource.data.size() > 0) {
                                adapter.setList(resource.data);
                                binding.emptyStateText.setVisibility(View.GONE);
                                binding.recyclerView.setVisibility(View.VISIBLE);
                            } else {
                                adapter.setList(new ArrayList<>());
                                binding.emptyStateText.setVisibility(View.VISIBLE);
                                binding.recyclerView.setVisibility(View.GONE);
                            }
                            break;
                        case ERROR:
                            binding.swipeRefresh.setRefreshing(false);
                            showErrorSnackbar(resource.message);
                            loadOfflineTimetable();
                            break;
                        case LOADING:
                            break;
                    }
                });
    }

    private void loadOfflineTimetable() {
        String ayId = (studentAcademicYearId != null) ? studentAcademicYearId : "";
        String secId = (studentSectionId != null) ? studentSectionId : "";

        viewModel.getOfflineTimetableForDay(ayId, secId, currentSelectedDay)
                .observe(getViewLifecycleOwner(), entities -> {
                    if (entities != null && entities.size() > 0) {
                        List<Timetable> list = new ArrayList<>();
                        for (com.csehub.app.core.database.entity.TimetableEntity ent : entities) {
                            list.add(mapEntityToModel(ent));
                        }
                        adapter.setList(list);
                        binding.emptyStateText.setVisibility(View.GONE);
                        binding.recyclerView.setVisibility(View.VISIBLE);
                        showSnackbar("Viewing offline timetable cache");
                    } else {
                        adapter.setList(new ArrayList<>());
                        binding.emptyStateText.setVisibility(View.VISIBLE);
                        binding.recyclerView.setVisibility(View.GONE);
                    }
                });
    }

    private Timetable mapEntityToModel(com.csehub.app.core.database.entity.TimetableEntity ent) {
        return new Timetable() {
            @Override
            public String getId() { return ent.getId(); }
            @Override
            public AcademicYear getAcademicYear() {
                AcademicYear ay = new AcademicYear();
                return ay;
            }
            @Override
            public Section getSection() {
                Section s = new Section();
                return s;
            }
            @Override
            public String getDay() { return ent.getDay(); }
            @Override
            public int getPeriod() { return ent.getPeriod(); }
            @Override
            public String getSubject() { return ent.getSubject(); }
            @Override
            public String getSubjectCode() { return ent.getSubjectCode(); }
            @Override
            public String getFacultyName() { return ent.getFacultyName(); }
            @Override
            public String getRoom() { return ent.getRoom(); }
            @Override
            public String getStartTime() { return ent.getStartTime(); }
            @Override
            public String getEndTime() { return ent.getEndTime(); }
            @Override
            public String getType() { return ent.getType(); }
        };
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        // Inflate CSV import action only for Admins
        if (isAdmin()) {
            menu.add(0, 101, 0, "Import CSV")
                    .setIcon(android.R.drawable.ic_menu_add)
                    .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        }
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == 101) {
            csvPickerLauncher.launch("text/csv");
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void uploadCSV(Uri uri) {
        File file = FileUtils.getFileFromUri(requireContext(), uri);
        if (file == null) return;

        RequestBody requestFile = RequestBody.create(file, MediaType.parse("text/csv"));
        MultipartBody.Part filePart = MultipartBody.Part.createFormData("file", file.getName(), requestFile);

        viewModel.importCSV(filePart).observe(getViewLifecycleOwner(), resource -> {
            if (resource == null) return;
            switch (resource.status) {
                case SUCCESS:
                    showToast("CSV imported successfully!");
                    fetchTimetable();
                    break;
                case ERROR:
                    showErrorSnackbar(resource.message);
                    break;
                case LOADING:
                    showToast("Importing CSV database...");
                    break;
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
