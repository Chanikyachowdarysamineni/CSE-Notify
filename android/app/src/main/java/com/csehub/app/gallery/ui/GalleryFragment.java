package com.csehub.app.gallery.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;

import com.csehub.app.core.base.BaseFragment;
import com.csehub.app.core.network.models.Gallery;
import com.csehub.app.core.utils.Constants;
import com.csehub.app.core.utils.FileUtils;
import com.csehub.app.databinding.FragmentGalleryBinding;
import com.csehub.app.gallery.ui.adapter.GalleryAdapter;
import com.csehub.app.gallery.viewmodel.GalleryViewModel;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.ArrayList;
import java.util.List;

public class GalleryFragment extends BaseFragment {

    private FragmentGalleryBinding binding;
    private GalleryViewModel viewModel;
    private GalleryAdapter adapter;
    private final List<Gallery> allPosts = new ArrayList<>();
    private String currentCategory = "All";
    private boolean showingMyPosts = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentGalleryBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(GalleryViewModel.class);

        setupTabLayout();
        setupRoleTabLayout();
        setupRecyclerView();
        setupFAB();
        fetchPosts();

        binding.swipeRefresh.setOnRefreshListener(this::fetchPosts);
    }

    private void setupTabLayout() {
        binding.categoryTabLayout.addTab(binding.categoryTabLayout.newTab().setText("All"));
        List<String> categories = com.csehub.app.core.network.ConfigRepository.getInstance(requireContext()).getGalleryCategories();
        for (String cat : categories) {
            binding.categoryTabLayout.addTab(binding.categoryTabLayout.newTab().setText(cat));
        }

        binding.categoryTabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                currentCategory = tab.getText().toString();
                fetchPosts();
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    private void setupRoleTabLayout() {
        if (canCreate()) {
            binding.roleTabLayout.setVisibility(View.VISIBLE);
            binding.roleTabLayout.addTab(binding.roleTabLayout.newTab().setText("All Posts"));
            binding.roleTabLayout.addTab(binding.roleTabLayout.newTab().setText("My Posts"));

            binding.roleTabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
                @Override
                public void onTabSelected(TabLayout.Tab tab) {
                    showingMyPosts = (tab.getPosition() == 1);
                    applyFilters();
                }
                @Override public void onTabUnselected(TabLayout.Tab tab) {}
                @Override public void onTabReselected(TabLayout.Tab tab) {}
            });
        } else {
            binding.roleTabLayout.setVisibility(View.GONE);
        }
    }

    private void setupRecyclerView() {
        adapter = new GalleryAdapter();
        adapter.setCurrentUserId(tokenManager.getUserId());
        adapter.setOnItemClickListener(post -> {
            Intent intent = new Intent(requireActivity(), ImageViewerActivity.class);
            intent.putExtra(Constants.EXTRA_IMAGE_URL, FileUtils.resolveUrl(post.getImage()));
            startActivity(intent);
        });
        
        adapter.setOnItemActionClickListener(new GalleryAdapter.OnItemActionClickListener() {
            @Override
            public void onDelete(Gallery post) {
                new MaterialAlertDialogBuilder(requireContext())
                    .setTitle("Delete Post")
                    .setMessage("Are you sure you want to delete this post?")
                    .setPositiveButton("Delete", (dialog, which) -> {
                        deletePost(post.getId());
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
            }
        });
        
        binding.recyclerView.setLayoutManager(new GridLayoutManager(requireContext(), 2));
        binding.recyclerView.setAdapter(adapter);
    }

    private void setupFAB() {
        if (canCreate()) {
            binding.fabAdd.setVisibility(View.VISIBLE);
            binding.fabAdd.setOnClickListener(v -> {
                startActivity(new Intent(requireActivity(), GalleryUploadActivity.class));
            });
        } else {
            binding.fabAdd.setVisibility(View.GONE);
        }
    }

    private void fetchPosts() {
        binding.swipeRefresh.setRefreshing(true);
        String categoryFilter = "All".equalsIgnoreCase(currentCategory) ? null : currentCategory;

        viewModel.getGalleryPosts(1, 100, categoryFilter)
                .observe(getViewLifecycleOwner(), resource -> {
                    if (resource == null) return;
                    switch (resource.status) {
                        case SUCCESS:
                            binding.swipeRefresh.setRefreshing(false);
                            allPosts.clear();
                            if (resource.data != null) {
                                allPosts.addAll(resource.data);
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
        List<Gallery> filtered = new ArrayList<>();
        
        for (Gallery g : allPosts) {
            boolean matchesTab = !showingMyPosts || 
                (g.getPostedBy() != null && tokenManager.getUserId().equals(g.getPostedBy().getId()));
                
            if (matchesTab) {
                filtered.add(g);
            }
        }
        adapter.submitList(filtered);
    }

    private void deletePost(String id) {
        viewModel.deleteGalleryPost(id).observe(getViewLifecycleOwner(), resource -> {
            if (resource == null) return;
            switch (resource.status) {
                case SUCCESS:
                    showToast("Post deleted successfully");
                    fetchPosts();
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
        fetchPosts();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
