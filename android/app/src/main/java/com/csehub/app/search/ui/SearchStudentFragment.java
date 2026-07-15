package com.csehub.app.search.ui;

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
import com.csehub.app.databinding.FragmentSearchStudentBinding;
import com.csehub.app.search.ui.adapter.SearchStudentAdapter;
import com.csehub.app.search.viewmodel.SearchViewModel;

import java.util.ArrayList;

public class SearchStudentFragment extends BaseFragment {

    private FragmentSearchStudentBinding binding;
    private SearchViewModel viewModel;
    private SearchStudentAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentSearchStudentBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(SearchViewModel.class);

        setupRecyclerView();
        setupSearchView();
    }

    private void setupRecyclerView() {
        adapter = new SearchStudentAdapter();
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerView.setAdapter(adapter);
    }

    private void setupSearchView() {
        binding.searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                performSearch(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (newText.length() >= 3) {
                    performSearch(newText);
                } else if (newText.isEmpty()) {
                    adapter.setList(new ArrayList<>());
                }
                return true;
            }
        });
    }

    private void performSearch(String regNo) {
        if (regNo == null || regNo.trim().isEmpty()) return;

        viewModel.searchStudent(regNo.trim()).observe(getViewLifecycleOwner(), resource -> {
            if (resource == null) return;
            switch (resource.status) {
                case SUCCESS:
                    adapter.setList(resource.data);
                    break;
                case ERROR:
                    adapter.setList(new ArrayList<>());
                    showErrorSnackbar(resource.message);
                    break;
                case LOADING:
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
