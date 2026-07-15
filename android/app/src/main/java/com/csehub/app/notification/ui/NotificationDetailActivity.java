package com.csehub.app.notification.ui;

import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;

import androidx.lifecycle.ViewModelProvider;

import com.csehub.app.core.base.BaseActivity;
import com.csehub.app.core.network.models.Notification;
import com.csehub.app.core.utils.Constants;
import com.csehub.app.core.utils.FileUtils;
import com.csehub.app.databinding.ActivityNotificationDetailBinding;
import com.csehub.app.notification.viewmodel.NotificationViewModel;

public class NotificationDetailActivity extends BaseActivity {

    private ActivityNotificationDetailBinding binding;
    private NotificationViewModel viewModel;
    private String notificationId;
    private Notification currentNotification;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityNotificationDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        viewModel = new ViewModelProvider(this).get(NotificationViewModel.class);
        notificationId = getIntent().getStringExtra(Constants.EXTRA_NOTIFICATION_ID);

        setupListeners();
        fetchDetails();
    }

    private void setupListeners() {
        binding.toolbar.setNavigationOnClickListener(v -> finish());
        binding.downloadButton.setOnClickListener(v -> downloadAttachment());
        binding.attachmentCard.setOnClickListener(v -> downloadAttachment());
        binding.linkButton.setOnClickListener(v -> openLink());
    }

    private void fetchDetails() {
        if (notificationId == null || notificationId.isEmpty()) {
            showToast("Invalid Notification ID");
            finish();
            return;
        }

        viewModel.getNotificationById(notificationId).observe(this, resource -> {
            if (resource == null) return;
            switch (resource.status) {
                case SUCCESS:
                    currentNotification = resource.data;
                    bindDetails(currentNotification);
                    // Mark as read once opened
                    markAsRead();
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

    private void bindDetails(Notification n) {
        binding.detailTitle.setText(n.getTitle());
        binding.detailMessage.setText(n.getMessage());
        binding.detailCategory.setText(n.getCategory());

        if (n.getCreatedBy() != null) {
            binding.detailAuthor.setText("By " + n.getCreatedBy().getName() + " (" + n.getCreatedBy().getRole().toUpperCase() + ")");
        } else {
            binding.detailAuthor.setText("System Notification");
        }

        if (n.getCreatedAt() != null && n.getCreatedAt().length() >= 10) {
            binding.detailDate.setText(n.getCreatedAt().substring(0, 10));
        }

        // Check for attachments
        if (n.getAttachment() != null && !n.getAttachment().isEmpty()) {
            binding.attachmentCard.setVisibility(View.VISIBLE);
            binding.attachmentNameText.setText(n.getAttachmentName() != null ? n.getAttachmentName() : "Attachment");
        } else {
            binding.attachmentCard.setVisibility(View.GONE);
        }

        // Check for link redirect
        if (n.getLink() != null && !n.getLink().isEmpty()) {
            binding.linkButton.setVisibility(View.VISIBLE);
        } else {
            binding.linkButton.setVisibility(View.GONE);
        }
    }

    private void markAsRead() {
        if (currentNotification == null || currentNotification.isRead()) return;
        viewModel.markAsRead(notificationId).observe(this, resource -> {
            // Silently mark read
        });
    }

    private void downloadAttachment() {
        if (currentNotification == null || currentNotification.getAttachment() == null) return;

        String absoluteUrl = FileUtils.resolveUrl(currentNotification.getAttachment());
        String fileName = currentNotification.getAttachmentName() != null ? currentNotification.getAttachmentName() : "attachment";

        try {
            DownloadManager.Request request = new DownloadManager.Request(Uri.parse(absoluteUrl));
            request.setTitle(fileName);
            request.setDescription("Downloading attachment from CSE HUB");
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
            request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName);
            request.addRequestHeader("Authorization", "Bearer " + tokenManager.getToken());

            DownloadManager manager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
            if (manager != null) {
                manager.enqueue(request);
                showToast("Download started...");
            }
        } catch (Exception e) {
            showToast("Download failed: " + e.getMessage());
        }
    }

    private void openLink() {
        if (currentNotification == null || currentNotification.getLink() == null) return;
        String url = currentNotification.getLink();
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
