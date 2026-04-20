package com.example.emotiondebugging.ui.staff;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.emotiondebugging.R;
import com.example.emotiondebugging.model.response.QuestRankingReportResponse;

import java.util.ArrayList;
import java.util.List;

public class QuestRankingAdapter extends RecyclerView.Adapter<QuestRankingAdapter.ViewHolder> {

    private final List<QuestRankingReportResponse> items = new ArrayList<>();

    public void submitList(List<QuestRankingReportResponse> list) {
        items.clear();
        if (list != null) {
            items.addAll(list);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_quest_ranking, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        QuestRankingReportResponse item = items.get(position);
        holder.tvQuestId.setText(String.valueOf(item.getQuest_id()));
        holder.tvQuestTitle.setText(item.getQuest_title());
        holder.tvValue.setText(String.valueOf(item.getTotal_assigned()));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvQuestId, tvQuestTitle, tvValue;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvQuestId = itemView.findViewById(R.id.tvQuestId);
            tvQuestTitle = itemView.findViewById(R.id.tvQuestTitle);
            tvValue = itemView.findViewById(R.id.tvValue);
        }
    }
}