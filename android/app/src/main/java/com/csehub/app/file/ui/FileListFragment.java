package com.csehub.app.file.ui;

import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.csehub.app.core.base.BaseFragment;
import com.csehub.app.core.network.models.FileModel;
import com.csehub.app.core.utils.Constants;
import com.csehub.app.core.utils.FileUtils;
import com.csehub.app.databinding.FragmentFileListBinding;
import com.csehub.app.file.ui.adapter.FileAdapter;
import com.csehub.app.file.viewmodel.FileViewModel;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.ArrayList;
import java.util.List;

public class FileListFragment extends BaseFragment {

    private FragmentFileListBinding binding;
    private FileViewModel viewModel;
    private FileAdapter adapter;
    private final List<FileModel> allFiles = new ArrayList<>();
    private boolean showingMyFiles = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentFileListBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(FileViewModel.class);

        setupRecyclerView();
        setupFAB();
        setupTabs();
        setupSearchView();
        fetchFiles();

        binding.swipeRefresh.setOnRefreshListener(this::fetchFiles);
    }

    private void setupRecyclerView() {
        adapter = new FileAdapter();
        adapter.setCurrentUserId(tokenManager.getUserId());
        
        adapter.setOnItemClickListener(new FileAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(FileModel file) {
                openFile(file);
            }

            @Override
            public void onDownloadClick(FileModel file) {
                downloadFile(file);
            }
        });
        
        adapter.setOnItemActionClickListener(new FileAdapter.OnItemActionClickListener() {
            @Override
            public void onDelete(FileModel file) {
                new MaterialAlertDialogBuilder(requireContext())
                    .setTitle("Delete File")
                    .setMessage("Are you sure you want to delete this file?")
                    .setPositiveButton("Delete", (dialog, which) -> {
                        deleteFile(file.getId());
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
                startActivity(new Intent(requireActivity(), FileUploadActivity.class));
            });
        } else {
            binding.fabAdd.setVisibility(View.GONE);
        }
    }

    private void setupTabs() {
        if (canCreate()) {
            binding.tabLayout.setVisibility(View.VISIBLE);
            binding.tabLayout.addTab(binding.tabLayout.newTab().setText("All Files"));
            binding.tabLayout.addTab(binding.tabLayout.newTab().setText("My Files"));

            binding.tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
                @Override
                public void onTabSelected(TabLayout.Tab tab) {
                    showingMyFiles = (tab.getPosition() == 1);
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

    private void fetchFiles() {
        binding.swipeRefresh.setRefreshing(true);
        viewModel.getFiles(1, 100, null, null, null)
                .observe(getViewLifecycleOwner(), resource -> {
                    if (resource == null) return;
                    switch (resource.status) {
                        case SUCCESS:
                            binding.swipeRefresh.setRefreshing(false);
                            allFiles.clear();
                            if (resource.data != null) {
                                allFiles.addAll(resource.data);
                            }
                            applyFilters();
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

    private void applyFilters() {
        String query = binding.searchView.getQuery() != null ? binding.searchView.getQuery().toString().toLowerCase() : "";
        List<FileModel> filtered = new ArrayList<>();
        
        for (FileModel f : allFiles) {
            boolean matchesSearch = query.isEmpty() || 
                f.getName().toLowerCase().contains(query) || 
                f.getCategory().toLowerCase().contains(query);
                
            boolean matchesTab = !showingMyFiles || 
                (f.getUploadedBy() != null && tokenManager.getUserId().equals(f.getUploadedBy().getId()));
                
            if (matchesSearch && matchesTab) {
                filtered.add(f);
            }
        }
        adapter.submitList(filtered);
    }
    
    private void deleteFile(String id) {
        viewModel.deleteFile(id).observe(getViewLifecycleOwner(), resource -> {
            if (resource == null) return;
            switch (resource.status) {
                case SUCCESS:
                    showToast("File deleted successfully");
                    fetchFiles();
                    break;
                case ERROR:
                    showErrorSnackbar(resource.message);
                    break;
                case LOADING:
                    break;
            }
        });
    }

    private void downloadFile(FileModel file) {
        String absoluteUrl = FileUtils.resolveUrl(file.getPath());
        String fileName = file.getOriginalName() != null ? file.getOriginalName() : file.getName();

        try {
            DownloadManager.Request request = new DownloadManager.Request(Uri.parse(absoluteUrl));
            request.setTitle(fileName);
            request.setDescription("Downloading file from CSE HUB");
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
            request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName);
            request.addRequestHeader("Authorization", "Bearer " + tokenManager.getToken());

            DownloadManager manager = (DownloadManager) requireActivity().getSystemService(Context.DOWNLOAD_SERVICE);
            if (manager != null) {
                manager.enqueue(request);
                showToast("Download started: " + fileName);
            }
        } catch (Exception e) {
            showToast("Download failed: " + e.getMessage());
        }
    }

    private void openFile(FileModel file) {
        String url = FileUtils.resolveUrl(file.getPath());
        if ("pdf".equalsIgnoreCase(file.getType())) {
            // Can launch a custom PDF previewer or route to default PDF reader via intent
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            try {
                startActivity(intent);
            } catch (Exception e) {
                // Fallback to custom browser
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                startActivity(browserIntent);
            }
        } else {
            // General browser fallback or trigger download
            downloadFile(file);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        fetchFiles();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
