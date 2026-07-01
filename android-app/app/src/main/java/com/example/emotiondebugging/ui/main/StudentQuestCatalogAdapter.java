package com.example.emotiondebugging.ui.main;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.emotiondebugging.R;
import com.example.emotiondebugging.model.response.QuestDraftSummary;

import java.util.ArrayList;
import java.util.List;

public class StudentQuestCatalogAdapter extends RecyclerView.Adapter<StudentQuestCatalogAdapter.Holder> {
    public interface Listener { void onStart(QuestDraftSummary quest); }
    private final Listener listener;
    private final List<QuestDraftSummary> items = new ArrayList<>();

    public StudentQuestCatalogAdapter(Listener listener) { this.listener = listener; }

    public void submit(List<QuestDraftSummary> data) {
        items.clear();
        if (data != null) items.addAll(data);
        notifyDataSetChanged();
    }

    @NonNull @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new Holder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_student_quest, parent, false));
    }

    @Override public void onBindViewHolder(@NonNull Holder holder, int position) {
        QuestDraftSummary item = items.get(position);
        holder.title.setText(text(item.quest_title, "Healing quest"));
        holder.category.setText(text(item.problem_path, "Vấn đề chưa phân loại"));
        holder.description.setText(text(item.quest_description, "A guided moment for you"));
        holder.meta.setText("Level " + Math.max(1, item.quest_level));
        holder.start.setOnClickListener(v -> listener.onStart(item));
    }

    @Override public int getItemCount() { return items.size(); }

    private static String text(String value, String fallback) {
        return value == null || value.trim().isEmpty() ? fallback : value;
    }

    static class Holder extends RecyclerView.ViewHolder {
        final TextView title, category, description, meta;
        final Button start;
        Holder(View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.tvStudentQuestTitle);
            category = itemView.findViewById(R.id.tvStudentQuestCategory);
            description = itemView.findViewById(R.id.tvStudentQuestDescription);
            meta = itemView.findViewById(R.id.tvStudentQuestMeta);
            start = itemView.findViewById(R.id.btnStartStudentQuest);
        }
    }
}
