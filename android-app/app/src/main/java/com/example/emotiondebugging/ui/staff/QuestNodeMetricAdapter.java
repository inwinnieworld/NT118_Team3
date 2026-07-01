package com.example.emotiondebugging.ui.staff;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.emotiondebugging.R;
import com.example.emotiondebugging.model.response.QuestNodeMetricResponse;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class QuestNodeMetricAdapter extends RecyclerView.Adapter<QuestNodeMetricAdapter.Holder> {
    private final List<QuestNodeMetricResponse> items = new ArrayList<>();

    public void submitList(List<QuestNodeMetricResponse> rows) {
        items.clear();
        if (rows != null) items.addAll(rows);
        notifyDataSetChanged();
    }

    @NonNull @Override public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new Holder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_quest_node_metric, parent, false));
    }

    @Override public void onBindViewHolder(@NonNull Holder holder, int position) {
        QuestNodeMetricResponse item = items.get(position);
        holder.title.setText(item.getQuestTitle() + " • " + item.getNodeName());
        holder.subtitle.setText(item.getEngineSubtype() + " • " + item.getClientConfigId());
        holder.stats.setText(String.format(Locale.getDefault(),
                "Started %d  |  Completed %d  |  Drop-off %.1f%%  |  Avg %.1fs  |  Errors %d",
                item.getStartedRuns(), item.getCompletedRuns(), item.getDropOffRate(),
                item.getAverageDurationSeconds(), item.getErrorCount()));
    }

    @Override public int getItemCount() { return items.size(); }

    static class Holder extends RecyclerView.ViewHolder {
        final TextView title;
        final TextView subtitle;
        final TextView stats;
        Holder(@NonNull View view) {
            super(view);
            title = view.findViewById(R.id.tvNodeMetricTitle);
            subtitle = view.findViewById(R.id.tvNodeMetricSubtitle);
            stats = view.findViewById(R.id.tvNodeMetricStats);
        }
    }
}
