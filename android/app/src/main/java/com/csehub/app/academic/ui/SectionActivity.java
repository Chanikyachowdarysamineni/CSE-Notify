package com.csehub.app.academic.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.csehub.app.R;
import com.csehub.app.academic.models.AcademicYear;
import com.csehub.app.academic.models.Section;
import com.csehub.app.academic.network.AcademicApi;
import com.csehub.app.core.network.ApiClient;
import com.csehub.app.core.network.models.ApiResponse;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SectionActivity extends AppCompatActivity {

    private AcademicApi api;
    private ProgressBar progressBar;
    private Spinner spinnerAcademicYear;
    private ExtendedFloatingActionButton fabAdd;
    private String selectedYearId = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_section);

        api = ApiClient.getInstance().create(AcademicApi.class);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        progressBar = findViewById(R.id.progressBar);
        spinnerAcademicYear = findViewById(R.id.spinnerAcademicYear);
        fabAdd = findViewById(R.id.fabAdd);

        fabAdd.setOnClickListener(v -> {
            if (selectedYearId == null) {
                Toast.makeText(this, "Select Academic Year first", Toast.LENGTH_SHORT).show();
                return;
            }
            // Show Add Section Dialog BottomSheet
            Toast.makeText(this, "Add Section UI", Toast.LENGTH_SHORT).show();
        });

        loadAcademicYears();
    }

    private void loadAcademicYears() {
        progressBar.setVisibility(View.VISIBLE);
        api.getAcademicYears("active").enqueue(new Callback<ApiResponse<List<AcademicYear>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<AcademicYear>>> call, Response<ApiResponse<List<AcademicYear>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<AcademicYear> years = response.body().getData();
                    ArrayAdapter<AcademicYear> adapter = new ArrayAdapter<>(SectionActivity.this, android.R.layout.simple_spinner_dropdown_item, years);
                    spinnerAcademicYear.setAdapter(adapter);

                    spinnerAcademicYear.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                            selectedYearId = years.get(position).getId();
                            loadSections(selectedYearId);
                        }
                        @Override
                        public void onNothingSelected(AdapterView<?> parent) {
                            selectedYearId = null;
                        }
                    });
                }
                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onFailure(Call<ApiResponse<List<AcademicYear>>> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
            }
        });
    }

    private void loadSections(String yearId) {
        progressBar.setVisibility(View.VISIBLE);
        api.getSections(yearId, null).enqueue(new Callback<ApiResponse<List<Section>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<Section>>> call, Response<ApiResponse<List<Section>>> response) {
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    List<Section> sections = response.body().getData();
                    Toast.makeText(SectionActivity.this, "Loaded " + sections.size() + " sections dynamically.", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(Call<ApiResponse<List<Section>>> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
            }
        });
    }
}
