package com.csehub.app.event.ui;

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
import com.csehub.app.core.network.models.Event;
import com.csehub.app.core.utils.Constants;
import com.csehub.app.databinding.FragmentEventListBinding;
import com.csehub.app.event.ui.adapter.EventAdapter;
import com.csehub.app.event.viewmodel.EventViewModel;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.ArrayList;
import java.util.List;

public class EventListFragment extends BaseFragment {

    private FragmentEventListBinding binding;
    private EventViewModel viewModel;
    private EventAdapter adapter;
    private final List<Event> allEvents = new ArrayList<>();
    private boolean showingMyEvents = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentEventListBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(EventViewModel.class);

        setupRecyclerView();
        setupFAB();
        setupTabs();
        setupSearchView();
        fetchEvents();

        binding.swipeRefresh.setOnRefreshListener(this::fetchEvents);
    }

    private void setupRecyclerView() {
        adapter = new EventAdapter();
        adapter.setCurrentUserId(tokenManager.getUserId());
        
        adapter.setOnItemClickListener(event -> {
            Intent intent = new Intent(requireActivity(), EventDetailActivity.class);
            intent.putExtra(Constants.EXTRA_EVENT_ID, event.getId());
            startActivity(intent);
        });

        adapter.setOnItemActionClickListener(new EventAdapter.OnItemActionClickListener() {
            @Override
            public void onEdit(Event event) {
                Intent intent = new Intent(requireActivity(), CreateEventActivity.class);
                intent.putExtra(Constants.EXTRA_EVENT_ID, event.getId());
                startActivity(intent);
            }

            @Override
            public void onDelete(Event event) {
                new MaterialAlertDialogBuilder(requireContext())
                    .setTitle("Delete Event")
                    .setMessage("Are you sure you want to delete this event?")
                    .setPositiveButton("Delete", (dialog, which) -> {
                        deleteEvent(event.getId());
                    })
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
            binding.fabAdd.setOnClickListener(v -> {
                startActivity(new Intent(requireActivity(), CreateEventActivity.class));
            });
        } else {
            binding.fabAdd.setVisibility(View.GONE);
        }
    }

    private void setupTabs() {
        if (canCreate()) {
            binding.tabLayout.setVisibility(View.VISIBLE);
            binding.tabLayout.addTab(binding.tabLayout.newTab().setText("All Events"));
            binding.tabLayout.addTab(binding.tabLayout.newTab().setText("My Events"));

            binding.tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
                @Override
                public void onTabSelected(TabLayout.Tab tab) {
                    showingMyEvents = (tab.getPosition() == 1);
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

    private void fetchEvents() {
        binding.swipeRefresh.setRefreshing(true);
        viewModel.getEvents(1, 100, null, null, false)
                .observe(getViewLifecycleOwner(), resource -> {
                    if (resource == null) return;
                    switch (resource.status) {
                        case SUCCESS:
                            binding.swipeRefresh.setRefreshing(false);
                            allEvents.clear();
                            if (resource.data != null) {
                                allEvents.addAll(resource.data);
                            }
                            applyFilters();
                            break;
                        case ERROR:
                            binding.swipeRefresh.setRefreshing(false);
                            showErrorSnackbar(resource.message);
                            loadOfflineEvents();
                            break;
                        case LOADING:
                            break;
                    }
                });
    }

    private void loadOfflineEvents() {
        viewModel.getOfflineEvents().observe(getViewLifecycleOwner(), entities -> {
            if (entities != null && entities.size() > 0) {
                List<Event> list = new ArrayList<>();
                for (com.csehub.app.core.database.entity.EventEntity ent : entities) {
                    list.add(mapEntityToModel(ent));
                }
                adapter.submitList(list);
                showSnackbar("Viewing offline cached events");
            }
        });
    }

    private Event mapEntityToModel(com.csehub.app.core.database.entity.EventEntity ent) {
        return new Event() {
            @Override
            public String getId() { return ent.getId(); }
            @Override
            public String getTitle() { return ent.getTitle(); }
            @Override
            public String getDescription() { return ent.getDescription(); }
            @Override
            public String getEventType() { return ent.getEventType(); }
            @Override
            public String getVenue() { return ent.getVenue(); }
            @Override
            public String getTime() { return ent.getTime(); }
            @Override
            public String getBannerImage() { return ent.getBannerImage(); }
            @Override
            public String getRegistrationLink() { return ent.getRegistrationLink(); }
            @Override
            public Creator getCreatedBy() {
                return new Creator() {
                    @Override
                    public String getName() { return ent.getCreatedByName(); }
                };
            }
        };
    }

    private void applyFilters() {
        String query = binding.searchView.getQuery() != null ? binding.searchView.getQuery().toString().toLowerCase() : "";
        List<Event> filtered = new ArrayList<>();
        
        for (Event e : allEvents) {
            boolean matchesSearch = query.isEmpty() || 
                e.getTitle().toLowerCase().contains(query) || 
                e.getDescription().toLowerCase().contains(query);
                
            boolean matchesTab = !showingMyEvents || 
                (e.getCreatedBy() != null && tokenManager.getUserId().equals(e.getCreatedBy().getId()));
                
            if (matchesSearch && matchesTab) {
                filtered.add(e);
            }
        }
        adapter.submitList(filtered);
    }
    
    private void deleteEvent(String id) {
        viewModel.deleteEvent(id).observe(getViewLifecycleOwner(), resource -> {
            if (resource == null) return;
            switch (resource.status) {
                case SUCCESS:
                    showToast("Event deleted successfully");
                    fetchEvents();
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
        fetchEvents();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
