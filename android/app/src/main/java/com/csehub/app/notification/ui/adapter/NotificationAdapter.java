package com.csehub.app.notification.ui.adapter;

import android.content.res.ColorStateList;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.csehub.app.R;
import com.csehub.app.core.network.models.Notification;
import com.csehub.app.databinding.ItemNotificationBinding;

public class NotificationAdapter extends ListAdapter<Notification, NotificationAdapter.ViewHolder> {

    private OnItemClickListener listener;
    private OnItemActionClickListener actionListener;
    private String currentUserId;

    public interface OnItemClickListener {
        void onItemClick(Notification notification);
    }

    public interface OnItemActionClickListener {
        void onEdit(Notification notification);
        void onDelete(Notification notification);
    }

    public void setCurrentUserId(String userId) {
        this.currentUserId = userId;
    }

    public void setOnItemActionClickListener(OnItemActionClickListener listener) {
        this.actionListener = listener;
    }

    public NotificationAdapter() {
        super(DIFF_CALLBACK);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    private static final DiffUtil.ItemCallback<Notification> DIFF_CALLBACK = new DiffUtil.ItemCallback<Notification>() {
        @Override
        public boolean areItemsTheSame(@NonNull Notification oldItem, @NonNull Notification newItem) {
            return oldItem.getId().equals(newItem.getId());
        }

        @Override
        public boolean areContentsTheSame(@NonNull Notification oldItem, @NonNull Notification newItem) {
            return oldItem.getTitle().equals(newItem.getTitle())
                    && oldItem.getMessage().equals(newItem.getMessage())
                    && oldItem.isRead() == newItem.isRead()
                    && oldItem.getPriority().equals(newItem.getPriority());
        }
    };

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemNotificationBinding binding = ItemNotificationBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false
        );
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Notification item = getItem(position);
        holder.binding.titleText.setText(item.getTitle());
        holder.binding.messagePreview.setText(item.getMessage());
        holder.binding.categoryText.setText(item.getCategory());

        if (item.getCreatedBy() != null) {
            holder.binding.authorText.setText("By " + item.getCreatedBy().getName());
        } else {
            holder.binding.authorText.setText("System Notification");
        }

        if (item.getCreatedAt() != null && item.getCreatedAt().length() >= 10) {
            holder.binding.dateText.setText(item.getCreatedAt().substring(0, 10));
        }

        // Stylize read/unread status
        if (item.isRead()) {
            holder.binding.titleText.setTypeface(null, Typeface.NORMAL);
            holder.binding.titleText.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.on_surface_variant));
            holder.itemView.setAlpha(0.85f);
        } else {
            holder.binding.titleText.setTypeface(null, Typeface.BOLD);
            holder.binding.titleText.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.on_surface));
            holder.itemView.setAlpha(1.0f);
        }

        // Setup priority
        int colorRes = R.color.priority_medium;
        if ("low".equalsIgnoreCase(item.getPriority())) {
            colorRes = R.color.priority_low;
        } else if ("high".equalsIgnoreCase(item.getPriority())) {
            colorRes = R.color.priority_high;
        } else if ("urgent".equalsIgnoreCase(item.getPriority())) {
            colorRes = R.color.priority_urgent;
        }

        holder.binding.priorityStripe.setBackgroundTintList(
                ColorStateList.valueOf(ContextCompat.getColor(holder.itemView.getContext(), colorRes))
        );

        // Show edit/delete buttons if owner
        boolean isOwner = currentUserId != null && item.getCreatedBy() != null 
                && currentUserId.equals(item.getCreatedBy().getId());
                
        if (isOwner) {
            holder.binding.actionButtonsContainer.setVisibility(android.view.View.VISIBLE);
        } else {
            holder.binding.actionButtonsContainer.setVisibility(android.view.View.GONE);
        }

        holder.binding.editButton.setOnClickListener(v -> {
            if (actionListener != null) actionListener.onEdit(item);
        });

        holder.binding.deleteButton.setOnClickListener(v -> {
            if (actionListener != null) actionListener.onDelete(item);
        });

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onItemClick(item);
        });
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public final ItemNotificationBinding binding;
        public ViewHolder(ItemNotificationBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
