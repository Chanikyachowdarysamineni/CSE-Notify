package com.csehub.app.dashboard.ui.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.csehub.app.R;
import com.csehub.app.core.network.models.Gallery;
import com.csehub.app.core.utils.FileUtils;
import com.csehub.app.databinding.ItemGalleryPreviewBinding;

import java.util.ArrayList;
import java.util.List;

public class DashboardGalleryAdapter extends RecyclerView.Adapter<DashboardGalleryAdapter.ViewHolder> {

    private final List<Gallery> list = new ArrayList<>();
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(Gallery post);
    }

    public void setList(List<Gallery> newList) {
        list.clear();
        if (newList != null) list.addAll(newList);
        notifyDataSetChanged();
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemGalleryPreviewBinding binding = ItemGalleryPreviewBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false
        );
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Gallery item = list.get(position);
        holder.binding.categoryText.setText(item.getCategory());

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

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onItemClick(item);
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final ItemGalleryPreviewBinding binding;
        ViewHolder(ItemGalleryPreviewBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
