package com.csehub.app.timetable.ui.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.csehub.app.core.network.models.Timetable;
import com.csehub.app.databinding.ItemTimetableSlotBinding;

import java.util.ArrayList;
import java.util.List;

public class TimetableDayAdapter extends RecyclerView.Adapter<TimetableDayAdapter.ViewHolder> {

    private final List<Timetable> list = new ArrayList<>();

    public void setList(List<Timetable> newList) {
        list.clear();
        if (newList != null) list.addAll(newList);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemTimetableSlotBinding binding = ItemTimetableSlotBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false
        );
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Timetable item = list.get(position);
        holder.binding.periodText.setText("P" + item.getPeriod());
        holder.binding.timeText.setText(item.getStartTime() + "\n" + item.getEndTime());
        holder.binding.subjectText.setText(item.getSubject());
        holder.binding.subjectCodeText.setText(item.getSubjectCode() != null ? item.getSubjectCode() : "");
        holder.binding.facultyText.setText("👤 " + (item.getFacultyName() != null ? item.getFacultyName() : "No Faculty"));
        holder.binding.roomText.setText("🚪 Room " + (item.getRoom() != null ? item.getRoom() : "N/A"));
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final ItemTimetableSlotBinding binding;
        ViewHolder(ItemTimetableSlotBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
