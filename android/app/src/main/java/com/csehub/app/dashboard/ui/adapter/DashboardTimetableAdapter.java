package com.csehub.app.dashboard.ui.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.csehub.app.core.network.models.Timetable;
import com.csehub.app.databinding.ItemDashboardTimetableBinding;

import java.util.ArrayList;
import java.util.List;

public class DashboardTimetableAdapter extends RecyclerView.Adapter<DashboardTimetableAdapter.ViewHolder> {

    private final List<Timetable> list = new ArrayList<>();
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(Timetable item);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public void setList(List<Timetable> newList) {
        list.clear();
        if (newList != null) list.addAll(newList);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemDashboardTimetableBinding binding = ItemDashboardTimetableBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false
        );
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Timetable item = list.get(position);
        holder.binding.periodText.setText("Period " + item.getPeriod());
        holder.binding.timeText.setText(item.getStartTime() + " - " + item.getEndTime());
        holder.binding.subjectText.setText(item.getSubject());
        holder.binding.roomText.setText(item.getRoom() != null && !item.getRoom().isEmpty() ? "Room: " + item.getRoom() : "No Room Info");

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onItemClick(item);
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final ItemDashboardTimetableBinding binding;
        ViewHolder(ItemDashboardTimetableBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
