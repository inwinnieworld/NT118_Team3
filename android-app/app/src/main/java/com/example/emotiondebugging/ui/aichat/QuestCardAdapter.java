package com.example.emotiondebugging.ui.aichat;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.emotiondebugging.R;
import com.example.emotiondebugging.model.response.AiChatModels.Quest;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Adapter cho RecyclerView ngang hiển thị Top quests dưới bong bóng AI.
 * Giai đoạn này Quest Engine chưa build — khi backend trả mảng rỗng, ChatAdapter sẽ
 * hiện placeholder thay vì RecyclerView này. Click thẻ → callback kèm quest_id (TODO: Intent).
 */
public class QuestCardAdapter extends RecyclerView.Adapter<QuestCardAdapter.QuestViewHolder> {

    public interface OnQuestClickListener {
        void onQuestClick(Quest quest);
    }

    private final List<Quest> quests = new ArrayList<>();
    private final OnQuestClickListener listener;

    public QuestCardAdapter(List<Quest> data, OnQuestClickListener listener) {
        this.listener = listener;
        if (data != null) quests.addAll(data);
    }

    @NonNull
    @Override
    public QuestViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_quest_card, parent, false);
        return new QuestViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull QuestViewHolder holder, int position) {
        holder.bind(quests.get(position));
    }

    @Override
    public int getItemCount() {
        return quests.size();
    }

    class QuestViewHolder extends RecyclerView.ViewHolder {
        final TextView tvQuestTitle, tvQuestRating;

        QuestViewHolder(@NonNull View itemView) {
            super(itemView);
            tvQuestTitle = itemView.findViewById(R.id.tvQuestTitle);
            tvQuestRating = itemView.findViewById(R.id.tvQuestRating);
        }

        void bind(Quest quest) {
            tvQuestTitle.setText(quest.isCompleted ? "✓ " + quest.title : quest.title);
            tvQuestRating.setText(quest.isCompleted
                    ? String.format(Locale.getDefault(), "★ %.1f  •  Đã hoàn thành", quest.rating)
                    : String.format(Locale.getDefault(), "★ %.1f", quest.rating));
            itemView.setOnClickListener(v -> {
                if (listener != null) listener.onQuestClick(quest);
            });
        }
    }
}
