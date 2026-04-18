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
import com.example.emotiondebugging.model.response.QuestResponse;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class QuestAdapter extends RecyclerView.Adapter<QuestAdapter.QuestViewHolder> implements Filterable {

    public interface OnQuestActionListener {
        void onEdit(QuestResponse item);
        void onDelete(QuestResponse item);
    }

    private final List<QuestResponse> originalList = new ArrayList<>();
    private final List<QuestResponse> filteredList = new ArrayList<>();
    private final OnQuestActionListener listener;

    public QuestAdapter(OnQuestActionListener listener) {
        this.listener = listener;
    }

    public void submitList(List<QuestResponse> list) {
        originalList.clear();
        filteredList.clear();

        if (list != null) {
            originalList.addAll(list);
            filteredList.addAll(list);
        }

        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public QuestViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_quest, parent, false);
        return new QuestViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull QuestViewHolder holder, int position) {
        QuestResponse item = filteredList.get(position);
        holder.tvQuestName.setText(item.getQuest_title());

        holder.btnEdit.setOnClickListener(v -> {
            if (listener != null) listener.onEdit(item);
        });

        holder.btnDelete.setOnClickListener(v -> {
            if (listener != null) listener.onDelete(item);
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
                String keyword = constraint == null ? "" : constraint.toString().trim().toLowerCase(Locale.ROOT);
                List<QuestResponse> result = new ArrayList<>();

                if (keyword.isEmpty()) {
                    result.addAll(originalList);
                } else {
                    for (QuestResponse item : originalList) {
                        String title = item.getQuest_title() == null ? "" : item.getQuest_title().toLowerCase(Locale.ROOT);
                        String errorName = item.getError_name() == null ? "" : item.getError_name().toLowerCase(Locale.ROOT);

                        if (title.contains(keyword) || errorName.contains(keyword)) {
                            result.add(item);
                        }
                    }
                }

                FilterResults filterResults = new FilterResults();
                filterResults.values = result;
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                filteredList.clear();
                if (results.values != null) {
                    filteredList.addAll((List<QuestResponse>) results.values);
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