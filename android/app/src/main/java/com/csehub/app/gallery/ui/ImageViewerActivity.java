package com.csehub.app.gallery.ui;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.csehub.app.R;
import com.csehub.app.core.utils.Constants;
import com.csehub.app.databinding.ActivityImageViewerBinding;

/**
 * Full screen zoomable image viewer activity using PhotoView
 */
public class ImageViewerActivity extends AppCompatActivity {

    private ActivityImageViewerBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityImageViewerBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        String imageUrl = getIntent().getStringExtra(Constants.EXTRA_IMAGE_URL);

        if (imageUrl != null && !imageUrl.isEmpty()) {
            Glide.with(this)
                    .load(imageUrl)
                    .placeholder(R.drawable.ic_gallery)
                    .error(R.drawable.ic_gallery)
                    .into(binding.photoView);
        }

        binding.closeButton.setOnClickListener(v -> finish());
    }
}
