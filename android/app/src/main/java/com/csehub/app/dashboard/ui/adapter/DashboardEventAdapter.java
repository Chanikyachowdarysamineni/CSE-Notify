package com.csehub.app.dashboard.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.csehub.app.R;
import com.csehub.app.core.network.models.Event;
import com.csehub.app.core.utils.FileUtils;
import com.csehub.app.databinding.ItemEventBinding;

import java.util.ArrayList;
import java.util.List;

public class DashboardEventAdapter extends RecyclerView.Adapter<DashboardEventAdapter.ViewHolder> {

    private final List<Event> list = new ArrayList<>();
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(Event event);
    }

    public void setList(List<Event> newList) {
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
        ItemEventBinding binding = ItemEventBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false
        );
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Event item = list.get(position);
        holder.binding.eventTitle.setText(item.getTitle());
        holder.binding.eventTypeBadge.setText(item.getEventType());
        holder.binding.eventVenue.setText("📍 " + item.getVenue());

        if (item.getDate() != null && item.getDate().length() >= 10) {
            holder.binding.eventDate.setText("📅 " + item.getDate().substring(0, 10) + " at " + item.getTime());
        } else {
            holder.binding.eventDate.setText("📅 " + item.getTime());
        }

        // Load banner image using resolved absolute url
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

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onItemClick(item);
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final ItemEventBinding binding;
        ViewHolder(ItemEventBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
