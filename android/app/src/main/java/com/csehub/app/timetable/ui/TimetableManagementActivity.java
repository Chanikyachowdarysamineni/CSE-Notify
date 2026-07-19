package com.csehub.app.timetable.ui;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.csehub.app.R;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;

public class TimetableManagementActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timetable_management);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        ExtendedFloatingActionButton fabImportCsv = findViewById(R.id.fabImportCsv);
        fabImportCsv.setOnClickListener(v -> {
            Toast.makeText(this, "CSV Import UI (Launches File Picker)", Toast.LENGTH_SHORT).show();
            // TODO: Launch Intent.ACTION_GET_CONTENT, read file, send via Retrofit Multipart API
        });
    }
}
