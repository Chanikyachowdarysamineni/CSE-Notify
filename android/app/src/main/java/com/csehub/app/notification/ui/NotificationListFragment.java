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
import com.csehub.app.core.database.entity.NotificationEntity;
import com.csehub.app.core.network.models.Notification;
import com.csehub.app.core.utils.Constants;
import com.csehub.app.databinding.FragmentNotificationListBinding;
import com.csehub.app.notification.ui.adapter.NotificationAdapter;
import com.csehub.app.notification.viewmodel.NotificationViewModel;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.List;

/**
 * NotificationListFragment
 *
 * Fixes applied:
 * 1. UI toggle bug — emptyState and recyclerView are now correctly mutually exclusive.
 * 2. LiveData observer leak — observations are set up ONCE in onViewCreated(); refresh
 *    is triggered by calling viewModel.refresh() rather than re-subscribing each time.
 */
public class NotificationListFragment extends BaseFragment {

    private FragmentNotificationListBinding binding;
    private NotificationViewModel viewModel;
    private NotificationAdapter adapter;

    // Master list loaded from server; applyFilters() derives the display list from this
    private final List<Notification> allNotifications = new ArrayList<>();
    private boolean showingMyNotifications = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
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
        setupObservers();   // ← Set up ONCE; do NOT re-call on every refresh
        fetchNotifications();

        binding.swipeRefresh.setOnRefreshListener(this::fetchNotifications);
    }

    // -------------------------------------------------------------------------
    // Setup helpers
    // -------------------------------------------------------------------------

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
                        .setPositiveButton("Delete", (dialog, which) ->
                                deleteNotification(notification.getId()))
                        .setNegativeButton("Cancel", null)
                        .show();
            }
        });

        binding.recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerView.setAdapter(adapter);
    }

    private void setupFAB() {
        if (canCreate()) {
            binding.fabAdd.setVisibility(View.VISIBLE);
            binding.fabAdd.setOnClickListener(v ->
                    startActivity(new Intent(requireActivity(), CreateNotificationActivity.class)));
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

    /**
     * Set up LiveData observers ONCE.
     * Observations are tied to getViewLifecycleOwner() so they are automatically
     * removed when the fragment's view is destroyed — no manual leak management needed.
     */
    private void setupObservers() {
        // Main notifications stream
        viewModel.getNotificationsLiveData().observe(getViewLifecycleOwner(), resource -> {
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
                    loadOfflineNotifications();
                    break;
                case LOADING:
                    // swipeRefresh handles the visual indicator
                    break;
            }
        });

        // Offline (Room) fallback
        viewModel.getOfflineNotifications().observe(getViewLifecycleOwner(), entities -> {
            if (entities != null && !entities.isEmpty() && allNotifications.isEmpty()) {
                List<Notification> list = new ArrayList<>();
                for (NotificationEntity ent : entities) {
                    list.add(mapEntityToModel(ent));
                }
                adapter.submitList(list);
                showSnackbar("Viewing cached offline data");
            }
        });
    }

    // -------------------------------------------------------------------------
    // Data operations
    // -------------------------------------------------------------------------

    private void fetchNotifications() {
        binding.swipeRefresh.setRefreshing(true);
        // Trigger a fetch; the result will flow through the observer in setupObservers()
        viewModel.fetchNotifications(1, 100, null, null, null, false);
    }

    private void loadOfflineNotifications() {
        // Observer already set up in setupObservers(); nothing to do here
        // The offline observer will fire automatically when Room data changes
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

    // -------------------------------------------------------------------------
    // Filter logic
    // -------------------------------------------------------------------------

    private void applyFilters() {
        String query = binding.searchView.getQuery() != null
                ? binding.searchView.getQuery().toString().toLowerCase().trim()
                : "";

        List<Notification> filtered = new ArrayList<>();

        for (Notification n : allNotifications) {
            boolean matchesSearch = query.isEmpty()
                    || (n.getTitle() != null && n.getTitle().toLowerCase().contains(query))
                    || (n.getMessage() != null && n.getMessage().toLowerCase().contains(query));

            boolean matchesTab = !showingMyNotifications
                    || (n.getCreatedBy() != null
                    && tokenManager.getUserId().equals(n.getCreatedBy().getId()));

            if (matchesSearch && matchesTab) {
                filtered.add(n);
            }
        }

        // FIX: properly toggle visibility — they must be mutually exclusive
        boolean isEmpty = filtered.isEmpty();
        binding.emptyStateLayout.getRoot().setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        binding.recyclerView.setVisibility(isEmpty ? View.GONE : View.VISIBLE);

        adapter.submitList(filtered);
    }

    // -------------------------------------------------------------------------
    // Mapping helper
    // -------------------------------------------------------------------------

    private Notification mapEntityToModel(NotificationEntity ent) {
        Notification n = new Notification();
        n.setId(ent.getId());
        n.setTitle(ent.getTitle());
        n.setMessage(ent.getMessage());
        n.setCategory(ent.getCategory());
        n.setPriority(ent.getPriority());
        n.setAttachment(ent.getAttachment());
        n.setAttachmentName(ent.getAttachmentName());
        n.setLink(ent.getLink());
        n.setRead(ent.isRead());

        if (ent.getCreatedByName() != null || ent.getCreatedByRole() != null) {
            Notification.Creator creator = new Notification.Creator();
            creator.setName(ent.getCreatedByName());
            creator.setRole(ent.getCreatedByRole());
            n.setCreatedBy(creator);
        }
        return n;
    }

    // -------------------------------------------------------------------------
    // Lifecycle
    // -------------------------------------------------------------------------

    @Override
    public void onResume() {
        super.onResume();
        // Refresh to capture read-status updates when returning from detail screen
        fetchNotifications();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
