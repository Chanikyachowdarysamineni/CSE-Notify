package com.csehub.app.event.ui.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.csehub.app.R;
import com.csehub.app.core.network.models.Event;
import com.csehub.app.core.utils.FileUtils;
import com.csehub.app.databinding.ItemEventBinding;

public class EventAdapter extends ListAdapter<Event, EventAdapter.ViewHolder> {

    private OnItemClickListener listener;
    private OnItemActionClickListener actionListener;
    private String currentUserId;

    public interface OnItemClickListener {
        void onItemClick(Event event);
    }

    public interface OnItemActionClickListener {
        void onEdit(Event event);
        void onDelete(Event event);
    }

    public void setCurrentUserId(String userId) {
        this.currentUserId = userId;
    }

    public void setOnItemActionClickListener(OnItemActionClickListener listener) {
        this.actionListener = listener;
    }

    public EventAdapter() {
        super(DIFF_CALLBACK);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    private static final DiffUtil.ItemCallback<Event> DIFF_CALLBACK = new DiffUtil.ItemCallback<Event>() {
        @Override
        public boolean areItemsTheSame(@NonNull Event oldItem, @NonNull Event newItem) {
            return oldItem.getId().equals(newItem.getId());
        }

        @Override
        public boolean areContentsTheSame(@NonNull Event oldItem, @NonNull Event newItem) {
            return oldItem.getTitle().equals(newItem.getTitle())
                    && oldItem.getDescription().equals(newItem.getDescription())
                    && oldItem.getVenue().equals(newItem.getVenue())
                    && oldItem.getTime().equals(newItem.getTime());
        }
    };

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemEventBinding binding = ItemEventBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false
        );
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Event item = getItem(position);
        holder.binding.eventTitle.setText(item.getTitle());
        holder.binding.eventTypeBadge.setText(item.getEventType());
        holder.binding.eventVenue.setText("📍 " + item.getVenue());

        if (item.getDate() != null && item.getDate().length() >= 10) {
            holder.binding.eventDate.setText("📅 " + item.getDate().substring(0, 10) + " at " + item.getTime());
        } else {
            holder.binding.eventDate.setText("📅 " + item.getTime());
        }

        String bannerUrl = FileUtils.resolveUrl(item.getBannerImage());
        if (!bannerUrl.isEmpty()) {
            Glide.with(holder.itemView.getContext())
                    .load(bannerUrl)
                    .placeholder(R.drawable.ic_gallery)
                    .error(R.drawable.ic_gallery)
                    .into(holder.binding.bannerImage);
        } else {
            holder.binding.bannerImage.setImageResource(R.drawable.ic_gallery);
        }

        // Action Buttons Logic
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
        public final ItemEventBinding binding;
        public ViewHolder(ItemEventBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
