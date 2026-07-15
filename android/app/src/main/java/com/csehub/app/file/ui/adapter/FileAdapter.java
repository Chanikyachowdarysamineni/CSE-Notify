package com.csehub.app.file.ui.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.csehub.app.R;
import com.csehub.app.core.network.models.FileModel;
import com.csehub.app.core.utils.FileUtils;
import com.csehub.app.databinding.ItemFileBinding;

public class FileAdapter extends ListAdapter<FileModel, FileAdapter.ViewHolder> {

    private OnItemClickListener listener;
    private OnItemActionClickListener actionListener;
    private String currentUserId;

    public interface OnItemClickListener {
        void onItemClick(FileModel file);
        void onDownloadClick(FileModel file);
    }

    public interface OnItemActionClickListener {
        void onDelete(FileModel file);
    }

    public void setCurrentUserId(String userId) {
        this.currentUserId = userId;
    }

    public void setOnItemActionClickListener(OnItemActionClickListener listener) {
        this.actionListener = listener;
    }

    public FileAdapter() {
        super(DIFF_CALLBACK);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    private static final DiffUtil.ItemCallback<FileModel> DIFF_CALLBACK = new DiffUtil.ItemCallback<FileModel>() {
        @Override
        public boolean areItemsTheSame(@NonNull FileModel oldItem, @NonNull FileModel newItem) {
            return oldItem.getId().equals(newItem.getId());
        }

        @Override
        public boolean areContentsTheSame(@NonNull FileModel oldItem, @NonNull FileModel newItem) {
            return oldItem.getName().equals(newItem.getName())
                    && oldItem.getSize() == newItem.getSize()
                    && oldItem.getDownloadCount() == newItem.getDownloadCount();
        }
    };

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemFileBinding binding = ItemFileBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false
        );
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        FileModel item = getItem(position);
        holder.binding.fileNameText.setText(item.getName());
        holder.binding.fileCategoryBadge.setText(item.getCategory());

        String meta = FileUtils.formatFileSize(item.getSize());
        if (item.getCreatedAt() != null && item.getCreatedAt().length() >= 10) {
            meta += " | " + item.getCreatedAt().substring(0, 10);
        }
        holder.binding.fileMetaText.setText(meta);

        // Bind custom icon based on file extension/type
        int iconRes = R.drawable.ic_file;
        String type = item.getType() != null ? item.getType().toLowerCase() : "";
        switch (type) {
            case "pdf":
                // In a real project, we could set custom tint or icons.
                break;
            case "doc":
            case "docx":
                break;
            case "xls":
            case "xlsx":
            case "csv":
                break;
            case "ppt":
            case "pptx":
                break;
            case "zip":
            case "rar":
                break;
            case "apk":
                break;
        }
        holder.binding.fileIcon.setImageResource(iconRes);

        // Delete button logic
        boolean isOwner = currentUserId != null && item.getUploadedBy() != null
                && currentUserId.equals(item.getUploadedBy().getId());

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

        holder.binding.downloadButton.setOnClickListener(v -> {
            if (listener != null) listener.onDownloadClick(item);
        });
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public final ItemFileBinding binding;
        public ViewHolder(ItemFileBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
