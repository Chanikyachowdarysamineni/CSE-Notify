package com.csehub.app.notification.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.csehub.app.core.base.BaseFragment;
import com.csehub.app.core.network.models.Notification;
import com.csehub.app.core.utils.Constants;
import com.csehub.app.databinding.FragmentNotificationListBinding;
import com.csehub.app.notification.ui.adapter.NotificationAdapter;
import com.csehub.app.notification.viewmodel.NotificationViewModel;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.ArrayList;
import java.util.List;

public class NotificationListFragment extends BaseFragment {

    private FragmentNotificationListBinding binding;
    private NotificationViewModel viewModel;
    private NotificationAdapter adapter;
    private final List<Notification> allNotifications = new ArrayList<>();
    private boolean showingMyNotifications = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentNotificationListBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(NotificationViewModel.class);

        setupRecyclerView();
        setupFAB();
        setupTabs();
        setupSearchView();
        fetchNotifications();

        binding.swipeRefresh.setOnRefreshListener(this::fetchNotifications);
    }

    private void setupRecyclerView() {
        adapter = new NotificationAdapter();
        adapter.setCurrentUserId(tokenManager.getUserId());
        
        adapter.setOnItemClickListener(notif -> {
            Intent intent = new Intent(requireActivity(), NotificationDetailActivity.class);
            intent.putExtra(Constants.EXTRA_NOTIFICATION_ID, notif.getId());
            startActivity(intent);
        });

        adapter.setOnItemActionClickListener(new NotificationAdapter.OnItemActionClickListener() {
            @Override
            public void onEdit(Notification notification) {
                Intent intent = new Intent(requireActivity(), CreateNotificationActivity.class);
                intent.putExtra(Constants.EXTRA_NOTIFICATION_ID, notification.getId());
                startActivity(intent);
            }

            @Override
            public void onDelete(Notification notification) {
                new MaterialAlertDialogBuilder(requireContext())
                    .setTitle("Delete Notification")
                    .setMessage("Are you sure you want to delete this notification?")
                    .setPositiveButton("Delete", (dialog, which) -> {
                        deleteNotification(notification.getId());
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
            }
        });

        binding.recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerView.setAdapter(adapter);
    }

    private void setupFAB() {
        // Show FAB only if user role can publish (faculty or admin)
        if (canCreate()) {
            binding.fabAdd.setVisibility(View.VISIBLE);
            binding.fabAdd.setOnClickListener(v -> {
                startActivity(new Intent(requireActivity(), CreateNotificationActivity.class));
            });
        } else {
            binding.fabAdd.setVisibility(View.GONE);
        }
    }

    private void setupTabs() {
        if (canCreate()) {
            binding.tabLayout.setVisibility(View.VISIBLE);
            binding.tabLayout.addTab(binding.tabLayout.newTab().setText("All Notifications"));
            binding.tabLayout.addTab(binding.tabLayout.newTab().setText("My Notifications"));

            binding.tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
                @Override
                public void onTabSelected(TabLayout.Tab tab) {
                    showingMyNotifications = (tab.getPosition() == 1);
                    applyFilters();
                }
                @Override public void onTabUnselected(TabLayout.Tab tab) {}
                @Override public void onTabReselected(TabLayout.Tab tab) {}
            });
        } else {
            binding.tabLayout.setVisibility(View.GONE);
        }
    }

    private void setupSearchView() {
        binding.searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                applyFilters();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                applyFilters();
                return true;
            }
        });
    }

    private void fetchNotifications() {
        binding.swipeRefresh.setRefreshing(true);
        viewModel.getNotifications(1, 100, null, null, null, false)
                .observe(getViewLifecycleOwner(), resource -> {
                    if (resource == null) return;
                    switch (resource.status) {
                        case SUCCESS:
                            binding.swipeRefresh.setRefreshing(false);
                            allNotifications.clear();
                            if (resource.data != null) {
                                allNotifications.addAll(resource.data);
                            }
                            applyFilters();
                            break;
                        case ERROR:
                            binding.swipeRefresh.setRefreshing(false);
                            showErrorSnackbar(resource.message);
                            // Fallback to Room Database offline cache
                            loadOfflineNotifications();
                            break;
                        case LOADING:
                            break;
                    }
                });
    }

    private void loadOfflineNotifications() {
        viewModel.getOfflineNotifications().observe(getViewLifecycleOwner(), entities -> {
            if (entities != null && entities.size() > 0) {
                List<Notification> list = new ArrayList<>();
                for (com.csehub.app.core.database.entity.NotificationEntity ent : entities) {
                    Notification n = new Notification();
                    // Inject properties
                    // We need a helper mapping or simple setters. Let's make sure we map them.
                    list.add(mapEntityToModel(ent));
                }
                adapter.submitList(list);
                showSnackbar("Viewing offline cache data");
            }
        });
    }

    private Notification mapEntityToModel(com.csehub.app.core.database.entity.NotificationEntity ent) {
        // Minimal deserialization mapping
        return new Notification() {
            @Override
            public String getId() { return ent.getId(); }
            @Override
            public String getTitle() { return ent.getTitle(); }
            @Override
            public String getMessage() { return ent.getMessage(); }
            @Override
            public String getCategory() { return ent.getCategory(); }
            @Override
            public String getPriority() { return ent.getPriority(); }
            @Override
            public String getAttachment() { return ent.getAttachment(); }
            @Override
            public String getAttachmentName() { return ent.getAttachmentName(); }
            @Override
            public String getLink() { return ent.getLink(); }
            @Override
            public boolean isRead() { return ent.isRead(); }
            @Override
            public Creator getCreatedBy() {
                return new Creator() {
                    @Override
                    public String getName() { return ent.getCreatedByName(); }
                    @Override
                    public String getRole() { return ent.getCreatedByRole(); }
                };
            }
        };
    }

    private void applyFilters() {
        String query = binding.searchView.getQuery() != null ? binding.searchView.getQuery().toString().toLowerCase() : "";
        List<Notification> filtered = new ArrayList<>();
        
        for (Notification n : allNotifications) {
            boolean matchesSearch = query.isEmpty() || 
                n.getTitle().toLowerCase().contains(query) || 
                n.getMessage().toLowerCase().contains(query);
                
            boolean matchesTab = !showingMyNotifications || 
                (n.getCreatedBy() != null && tokenManager.getUserId().equals(n.getCreatedBy().getId()));
                
            if (matchesSearch && matchesTab) {
                filtered.add(n);
            }
        }
        
        boolean show = filtered.isEmpty();
        if (show) {
            binding.emptyStateLayout.getRoot().setVisibility(View.VISIBLE);
        } else {  binding.recyclerView.setVisibility(View.VISIBLE);
        }
        
        adapter.submitList(filtered);
    }
    
    private void deleteNotification(String id) {
        viewModel.deleteNotification(id).observe(getViewLifecycleOwner(), resource -> {
            if (resource == null) return;
            switch (resource.status) {
                case SUCCESS:
                    showToast("Notification deleted successfully");
                    fetchNotifications();
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
        // Refresh notifications to capture read count updates
        fetchNotifications();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
