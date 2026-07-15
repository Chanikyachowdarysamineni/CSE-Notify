package com.csehub.app.search.ui.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.csehub.app.R;
import com.csehub.app.core.utils.FileUtils;
import com.csehub.app.databinding.ItemStudentSearchBinding;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SearchStudentAdapter extends RecyclerView.Adapter<SearchStudentAdapter.ViewHolder> {

    private final List<Map<String, Object>> list = new ArrayList<>();

    public void setList(List<Map<String, Object>> newList) {
        list.clear();
        if (newList != null) list.addAll(newList);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemStudentSearchBinding binding = ItemStudentSearchBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false
        );
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Map<String, Object> item = list.get(position);
        holder.binding.nameText.setText(String.valueOf(item.get("name")));
        holder.binding.regNoText.setText("Reg No: " + String.valueOf(item.get("regNo")));
        String yearStr = "";
        Object yearObj = item.get("academicYear");
        if (yearObj instanceof Map) {
            yearStr = String.valueOf(((Map<String, Object>) yearObj).get("name"));
        }

        String sectionStr = "";
        Object secObj = item.get("section");
        if (secObj instanceof Map) {
            sectionStr = String.valueOf(((Map<String, Object>) secObj).get("name"));
        }

        holder.binding.classText.setText(yearStr + " | Sec " + sectionStr);
        holder.binding.emailText.setText(String.valueOf(item.get("collegeEmail")));
        holder.binding.cgpaText.setText(String.valueOf(item.get("cgpa")));

        // Avatar photo binding
        if (item.containsKey("profilePhoto")) {
            String path = String.valueOf(item.get("profilePhoto"));
            String url = FileUtils.resolveUrl(path);
            if (!url.isEmpty() && !"null".equals(path)) {
                Glide.with(holder.itemView.getContext())
                        .load(url)
                        .placeholder(R.drawable.ic_profile)
                        .error(R.drawable.ic_profile)
                        .into(holder.binding.photoImageView);
            } else {
                holder.binding.photoImageView.setImageResource(R.drawable.ic_profile);
            }
        } else {
            holder.binding.photoImageView.setImageResource(R.drawable.ic_profile);
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final ItemStudentSearchBinding binding;
        ViewHolder(ItemStudentSearchBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
