package com.csehub.app.event.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;

import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.csehub.app.R;
import com.csehub.app.core.base.BaseActivity;
import com.csehub.app.core.network.models.Event;
import com.csehub.app.core.utils.Constants;
import com.csehub.app.core.utils.FileUtils;
import com.csehub.app.databinding.ActivityEventDetailBinding;
import com.csehub.app.event.viewmodel.EventViewModel;

public class EventDetailActivity extends BaseActivity {

    private ActivityEventDetailBinding binding;
    private EventViewModel viewModel;
    private String eventId;
    private Event currentEvent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityEventDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        viewModel = new ViewModelProvider(this).get(EventViewModel.class);
        eventId = getIntent().getStringExtra(Constants.EXTRA_EVENT_ID);

        setupListeners();
        fetchDetails();
    }

    private void setupListeners() {
        binding.toolbar.setNavigationOnClickListener(v -> finish());
        binding.registerButton.setOnClickListener(v -> openRegistrationLink());
    }

    private void fetchDetails() {
        if (eventId == null || eventId.isEmpty()) {
            showToast("Invalid Event ID");
            finish();
            return;
        }

        viewModel.getEventById(eventId).observe(this, resource -> {
            if (resource == null) return;
            switch (resource.status) {
                case SUCCESS:
                    currentEvent = resource.data;
                    bindDetails(currentEvent);
                    break;
                case ERROR:
                    showToast(resource.message);
                    finish();
                    break;
                case LOADING:
                    break;
            }
        });
    }

    private void bindDetails(Event e) {
        binding.detailTitle.setText(e.getTitle());
        binding.detailDescription.setText(e.getDescription());
        binding.detailEventType.setText(e.getEventType());
        binding.detailVenue.setText("📍 Venue: " + e.getVenue());

        if (e.getDate() != null && e.getDate().length() >= 10) {
            binding.detailDate.setText("📅 Date: " + e.getDate().substring(0, 10) + " at " + e.getTime());
        } else {
            binding.detailDate.setText("📅 Date: " + e.getTime());
        }

        // Load banner image
        String bannerUrl = FileUtils.resolveUrl(e.getBannerImage());
        if (!bannerUrl.isEmpty()) {
            Glide.with(this)
                    .load(bannerUrl)
                    .placeholder(R.drawable.ic_gallery)
                    .error(R.drawable.ic_gallery)
                    .into(binding.detailBannerImage);
        } else {
            binding.detailBannerImage.setImageResource(R.drawable.ic_gallery);
        }

        // Handle registration button
        if (e.getRegistrationLink() != null && !e.getRegistrationLink().isEmpty()) {
            binding.registerButton.setVisibility(View.VISIBLE);
        } else {
            binding.registerButton.setVisibility(View.GONE);
        }
    }

    private void openRegistrationLink() {
        if (currentEvent == null || currentEvent.getRegistrationLink() == null) return;
        String url = currentEvent.getRegistrationLink();
        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            url = "http://" + url;
        }
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            startActivity(intent);
        } catch (Exception e) {
            showToast("Cannot open link: " + e.getMessage());
        }
    }
}
