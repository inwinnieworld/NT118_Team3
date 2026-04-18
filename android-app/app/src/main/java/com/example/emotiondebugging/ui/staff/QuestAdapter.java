package com.example.emotiondebugging.ui.staff;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.emotiondebugging.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class QuestAdapter extends RecyclerView.Adapter<QuestAdapter.QuestViewHolder> implements Filterable {

    public interface OnQuestActionListener {
        void onEdit(QuestItem item);
        void onDelete(QuestItem item);
    }

    private final List<QuestItem> originalList;
    private final List<QuestItem> filteredList;
    private final OnQuestActionListener listener;

    public QuestAdapter(List<QuestItem> questList, OnQuestActionListener listener) {
        this.originalList = new ArrayList<>(questList);
        this.filteredList = new ArrayList<>(questList);
        this.listener = listener;
    }

    @NonNull
    @Override
    public QuestViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_quest, parent, false);
        return new QuestViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull QuestViewHolder holder, int position) {
        QuestItem item = filteredList.get(position);
        holder.tvQuestName.setText(item.getQuestName());

        holder.btnEdit.setOnClickListener(v -> {
            if (listener != null) {
                listener.onEdit(item);
            }
        });

        holder.btnDelete.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDelete(item);
            }
        });
    }

    @Override
    public int getItemCount() {
        return filteredList.size();
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                String keyword = constraint == null
                        ? ""
                        : constraint.toString().trim().toLowerCase(Locale.ROOT);

                List<QuestItem> resultList = new ArrayList<>();

                if (keyword.isEmpty()) {
                    resultList.addAll(originalList);
                } else {
                    for (QuestItem item : originalList) {
                        if (item.getQuestName().toLowerCase(Locale.ROOT).contains(keyword)) {
                            resultList.add(item);
                        }
                    }
                }

                FilterResults results = new FilterResults();
                results.values = resultList;
                return results;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                filteredList.clear();
                if (results.values != null) {
                    filteredList.addAll((List<QuestItem>) results.values);
                }
                notifyDataSetChanged();
            }
        };
    }

    static class QuestViewHolder extends RecyclerView.ViewHolder {
        TextView tvQuestName;
        LinearLayout btnEdit;
        LinearLayout btnDelete;

        public QuestViewHolder(@NonNull View itemView) {
            super(itemView);
            tvQuestName = itemView.findViewById(R.id.tvQuestName);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}