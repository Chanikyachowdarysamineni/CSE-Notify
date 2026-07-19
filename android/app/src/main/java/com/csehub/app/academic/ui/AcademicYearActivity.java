package com.csehub.app.academic.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.csehub.app.R;
import com.csehub.app.academic.models.AcademicYear;
import com.csehub.app.academic.network.AcademicApi;
import com.csehub.app.core.network.ApiClient;
import com.csehub.app.core.network.models.ApiResponse;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AcademicYearActivity extends AppCompatActivity {

    private ProgressBar progressBar;
    private TextView tvEmpty;
    private ExtendedFloatingActionButton fabAdd;
    private AcademicApi api;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_academic_year);

        api = ApiClient.getInstance().create(AcademicApi.class);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        progressBar = findViewById(R.id.progressBar);
        tvEmpty = findViewById(R.id.tvEmpty);
        fabAdd = findViewById(R.id.fabAdd);

        fabAdd.setOnClickListener(v -> showAddEditDialog(null));

        loadAcademicYears();
    }

    private void loadAcademicYears() {
        progressBar.setVisibility(View.VISIBLE);
        tvEmpty.setVisibility(View.GONE);

        api.getAcademicYears(null).enqueue(new Callback<ApiResponse<List<AcademicYear>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<AcademicYear>>> call, Response<ApiResponse<List<AcademicYear>>> response) {
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    List<AcademicYear> list = response.body().getData();
                    if (list.isEmpty()) {
                        tvEmpty.setVisibility(View.VISIBLE);
                    } else {
                        // TODO: Set up RecyclerView adapter with 'list'
                        // adapter.setItems(list);
                        Toast.makeText(AcademicYearActivity.this, "Loaded " + list.size() + " years", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(AcademicYearActivity.this, "Failed to load", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<AcademicYear>>> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(AcademicYearActivity.this, "Network error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showAddEditDialog(AcademicYear existing) {
        // Implementation for Add/Edit bottom sheet
        new AlertDialog.Builder(this)
                .setTitle(existing == null ? "Add Academic Year" : "Edit Academic Year")
                .setMessage("Forms are handled dynamically via Material BottomSheets.")
                .setPositiveButton("Save", (dialog, which) -> {
                    // Call api.createAcademicYear or api.updateAcademicYear
                    loadAcademicYears();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
