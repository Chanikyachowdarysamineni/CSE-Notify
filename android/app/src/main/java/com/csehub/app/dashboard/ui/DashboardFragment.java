package com.csehub.app.dashboard.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.csehub.app.R;
import com.csehub.app.core.base.BaseFragment;
import com.csehub.app.core.network.models.DashboardData;
import com.csehub.app.dashboard.ui.adapter.DashboardEventAdapter;
import com.csehub.app.dashboard.ui.adapter.DashboardGalleryAdapter;
import com.csehub.app.dashboard.ui.adapter.DashboardNotificationAdapter;
import com.csehub.app.dashboard.ui.adapter.DashboardTimetableAdapter;
import com.csehub.app.file.ui.adapter.FileAdapter;
import com.csehub.app.dashboard.viewmodel.DashboardViewModel;
import com.csehub.app.databinding.FragmentDashboardBinding;

/**
 * Dashboard Fragment managing welcome states, pulling data from ViewModel,
 * and dispatching list bindings to horizontal and vertical layouts
 */
public class DashboardFragment extends BaseFragment {

    private FragmentDashboardBinding binding;
    private DashboardViewModel viewModel;

    private DashboardTimetableAdapter timetableAdapter;
    private DashboardNotificationAdapter notificationAdapter;
    private DashboardEventAdapter eventAdapter;
    private DashboardGalleryAdapter galleryAdapter;
    private FileAdapter fileAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentDashboardBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(DashboardViewModel.class);

        setupAdapters();
        setupViewLayout();
        fetchData();

        binding.swipeRefresh.setOnRefreshListener(this::fetchData);
    }

    private void setupAdapters() {
        timetableAdapter = new DashboardTimetableAdapter();
        binding.timetableRecyclerView.setAdapter(timetableAdapter);

        notificationAdapter = new DashboardNotificationAdapter();
        notificationAdapter.setOnItemClickListener(notif -> {
            // Navigate to notifications screen
            Navigation.findNavController(requireView()).navigate(R.id.navigation_notifications);
        });
        binding.notificationsRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.notificationsRecyclerView.setAdapter(notificationAdapter);

        eventAdapter = new DashboardEventAdapter();
        eventAdapter.setOnItemClickListener(event -> {
            Navigation.findNavController(requireView()).navigate(R.id.navigation_events);
        });
        binding.eventsRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.eventsRecyclerView.setAdapter(eventAdapter);

        galleryAdapter = new DashboardGalleryAdapter();
        galleryAdapter.setOnItemClickListener(post -> {
            Navigation.findNavController(requireView()).navigate(R.id.navigation_gallery);
        });
        binding.galleryRecyclerView.setAdapter(galleryAdapter);

        fileAdapter = new FileAdapter();
        fileAdapter.setOnItemClickListener(new FileAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(com.csehub.app.core.network.models.FileModel file) {
                Navigation.findNavController(requireView()).navigate(R.id.navigation_files);
            }
            @Override
            public void onDownloadClick(com.csehub.app.core.network.models.FileModel file) {
                // Ignore for dashboard, just navigate
                Navigation.findNavController(requireView()).navigate(R.id.navigation_files);
            }
        });
        binding.filesRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.filesRecyclerView.setAdapter(fileAdapter);

        // Make quick stat cards clickable to navigate to their respective sections
        binding.cardNotifications.setOnClickListener(v -> 
            Navigation.findNavController(requireView()).navigate(R.id.navigation_notifications));
            
        binding.cardClasses.setOnClickListener(v -> 
            Navigation.findNavController(requireView()).navigate(R.id.navigation_timetable));
            
        binding.cardEvents.setOnClickListener(v -> 
            Navigation.findNavController(requireView()).navigate(R.id.navigation_events));
            
        binding.cardFiles.setOnClickListener(v -> 
            Navigation.findNavController(requireView()).navigate(R.id.navigation_files));

        // Let's add click listeners to the timetable widget
        binding.timetableWidget.setOnClickListener(v -> {
            Navigation.findNavController(requireView()).navigate(R.id.navigation_timetable);
        });
        
        timetableAdapter.setOnItemClickListener(item -> {
            Navigation.findNavController(requireView()).navigate(R.id.navigation_timetable);
        });

        // Wire up "View All" buttons
        binding.btnViewAllNotifications.setOnClickListener(v -> Navigation.findNavController(requireView()).navigate(R.id.navigation_notifications));
        binding.btnViewAllTimetable.setOnClickListener(v -> Navigation.findNavController(requireView()).navigate(R.id.navigation_timetable));
        binding.btnViewAllFiles.setOnClickListener(v -> Navigation.findNavController(requireView()).navigate(R.id.navigation_files));
        binding.btnViewAllGallery.setOnClickListener(v -> Navigation.findNavController(requireView()).navigate(R.id.navigation_gallery));
        binding.btnViewAllEvents.setOnClickListener(v -> Navigation.findNavController(requireView()).navigate(R.id.navigation_events));
    }

    private void setupViewLayout() {
        // Welcoming text customization
        binding.welcomeName.setText(tokenManager.getUserName());
        
        // Hide welcomeRegNo if we are combining it into welcomeYearSection
        binding.welcomeRegNo.setVisibility(View.GONE);

        if (tokenManager.isStudent()) {
            // Mocking Year and Section for UI spec matching until dynamic profile endpoints are cached
            String regNo = tokenManager.getUserId();
            if (regNo == null || regNo.isEmpty()) regNo = "Reg No";
            binding.welcomeYearSection.setText(regNo + " • 3rd Year • Section C");
        } else {
            binding.welcomeYearSection.setText("Role: " + tokenManager.getUserRole().toUpperCase());
        }
    }

    private void fetchData() {
        binding.swipeRefresh.setRefreshing(true);
        viewModel.getDashboardData().observe(getViewLifecycleOwner(), resource -> {
            if (resource == null) return;
            switch (resource.status) {
                case SUCCESS:
                    binding.swipeRefresh.setRefreshing(false);
                    bindData(resource.data);
                    break;
                case ERROR:
                    binding.swipeRefresh.setRefreshing(false);
                    showErrorSnackbar(resource.message);
                    break;
                case LOADING:
                    break;
            }
        });
    }

    private void bindData(DashboardData data) {
        if (data == null) return;

        // Bind timetable
        if (data.getTodayTimetable() != null && data.getTodayTimetable().size() > 0) {
            timetableAdapter.setList(data.getTodayTimetable());
            binding.timetableRecyclerView.setVisibility(View.VISIBLE);
        } else {
            binding.timetableRecyclerView.setVisibility(View.GONE);
        }

        // Bind notifications
        notificationAdapter.setList(data.getTodayNotifications());

        // Bind events
        eventAdapter.setList(data.getUpcomingEvents());

        // Bind gallery highlights
        galleryAdapter.setList(data.getGalleryHighlights());

        // Bind recent files
        if (data.getRecentFiles() != null) {
            fileAdapter.submitList(data.getRecentFiles());
        }

        // Bind stats
        binding.statNotifications.setText(String.valueOf(data.getUnreadCount()));
        
        int classesCount = (data.getTodayTimetable() != null) ? data.getTodayTimetable().size() : 0;
        binding.statClasses.setText(String.valueOf(classesCount));
        
        int eventsCount = (data.getUpcomingEvents() != null) ? data.getUpcomingEvents().size() : 0;
        binding.statEvents.setText(String.valueOf(eventsCount));

        int filesCount = (data.getRecentFiles() != null) ? data.getRecentFiles().size() : 0;
        binding.statFiles.setText(String.valueOf(filesCount));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
