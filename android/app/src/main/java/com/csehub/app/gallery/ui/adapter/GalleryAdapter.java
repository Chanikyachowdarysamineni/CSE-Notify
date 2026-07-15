package com.csehub.app.gallery.ui.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.csehub.app.R;
import com.csehub.app.core.network.models.Gallery;
import com.csehub.app.core.utils.FileUtils;
import com.csehub.app.databinding.ItemGalleryGridBinding;

public class GalleryAdapter extends ListAdapter<Gallery, GalleryAdapter.ViewHolder> {

    private OnItemClickListener listener;
    private OnItemActionClickListener actionListener;
    private String currentUserId;

    public interface OnItemClickListener {
        void onItemClick(Gallery post);
    }

    public interface OnItemActionClickListener {
        void onDelete(Gallery post);
    }

    public void setCurrentUserId(String userId) {
        this.currentUserId = userId;
    }

    public void setOnItemActionClickListener(OnItemActionClickListener listener) {
        this.actionListener = listener;
    }

    public GalleryAdapter() {
        super(DIFF_CALLBACK);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    private static final DiffUtil.ItemCallback<Gallery> DIFF_CALLBACK = new DiffUtil.ItemCallback<Gallery>() {
        @Override
        public boolean areItemsTheSame(@NonNull Gallery oldItem, @NonNull Gallery newItem) {
            return oldItem.getId().equals(newItem.getId());
        }

        @Override
        public boolean areContentsTheSame(@NonNull Gallery oldItem, @NonNull Gallery newItem) {
            return oldItem.getImage().equals(newItem.getImage())
                    && oldItem.getCaption().equals(newItem.getCaption());
        }
    };

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemGalleryGridBinding binding = ItemGalleryGridBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false
        );
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Gallery item = getItem(position);
        holder.binding.captionText.setText(item.getCaption());

        String imageUrl = FileUtils.resolveUrl(item.getImage());
        if (!imageUrl.isEmpty()) {
            Glide.with(holder.itemView.getContext())
                    .load(imageUrl)
                    .placeholder(R.drawable.ic_gallery)
                    .error(R.drawable.ic_gallery)
                    .into(holder.binding.galleryImage);
        } else {
            holder.binding.galleryImage.setImageResource(R.drawable.ic_gallery);
        }

        // Delete button logic
        boolean isOwner = currentUserId != null && item.getPostedBy() != null
                && currentUserId.equals(item.getPostedBy().getId());

        if (isOwner) {
            holder.binding.deleteButton.setVisibility(android.view.View.VISIBLE);
        } else {
            holder.binding.deleteButton.setVisibility(android.view.View.GONE);
        }

        holder.binding.deleteButton.setOnClickListener(v -> {
            if (actionListener != null) actionListener.onDelete(item);
        });

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onItemClick(item);
        });
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public final ItemGalleryGridBinding binding;
        public ViewHolder(ItemGalleryGridBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
