package com.csehub.app.dashboard.ui.adapter;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.csehub.app.R;
import com.csehub.app.core.network.models.Notification;
import com.csehub.app.databinding.ItemNotificationBinding;

import java.util.ArrayList;
import java.util.List;

public class DashboardNotificationAdapter extends RecyclerView.Adapter<DashboardNotificationAdapter.ViewHolder> {

    private final List<Notification> list = new ArrayList<>();
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(Notification notification);
    }

    public void setList(List<Notification> newList) {
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
        ItemNotificationBinding binding = ItemNotificationBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false
        );
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Notification item = list.get(position);
        holder.binding.titleText.setText(item.getTitle());
        holder.binding.messagePreview.setText(item.getMessage());
        holder.binding.categoryText.setText(item.getCategory());
        
        if (item.getCreatedBy() != null) {
            holder.binding.authorText.setText("By " + item.getCreatedBy().getName());
        } else {
            holder.binding.authorText.setText("System Notification");
        }

        // Setup dates (simple slice)
        if (item.getCreatedAt() != null && item.getCreatedAt().length() >= 10) {
            holder.binding.dateText.setText(item.getCreatedAt().substring(0, 10));
        }

        // Bind priority indicators
        int colorRes = R.color.priority_medium;
        if ("low".equalsIgnoreCase(item.getPriority())) {
            colorRes = R.color.priority_low;
        } else if ("high".equalsIgnoreCase(item.getPriority())) {
            colorRes = R.color.priority_high;
        } else if ("urgent".equalsIgnoreCase(item.getPriority())) {
            colorRes = R.color.priority_urgent;
        }
        
        holder.binding.priorityIndicator.setBackgroundTintList(
                ColorStateList.valueOf(ContextCompat.getColor(holder.itemView.getContext(), colorRes))
        );

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onItemClick(item);
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final ItemNotificationBinding binding;
        ViewHolder(ItemNotificationBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
