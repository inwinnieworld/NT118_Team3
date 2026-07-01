package com.example.emotiondebugging.ui.admin;

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

public class AdminQuestReviewAdapter extends RecyclerView.Adapter<AdminQuestReviewAdapter.Holder> {
    public interface Listener {
        void onPreview(QuestDraftSummary quest);
        void onApprove(QuestDraftSummary quest);
        void onReject(QuestDraftSummary quest);
        void onVisibility(QuestDraftSummary quest, boolean active);
    }

    private final Listener listener;
    private final List<QuestDraftSummary> items = new ArrayList<>();

    public AdminQuestReviewAdapter(Listener listener) { this.listener = listener; }

    public void submit(List<QuestDraftSummary> data) {
        items.clear();
        if (data != null) items.addAll(data);
        notifyDataSetChanged();
    }

    @NonNull @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_admin_quest_review, parent, false);
        return new Holder(view);
    }

    @Override public void onBindViewHolder(@NonNull Holder holder, int position) {
        QuestDraftSummary item = items.get(position);
        holder.title.setText(text(item.quest_title, "Untitled quest"));
        holder.meta.setText(text(item.problem_path, "Chưa chọn vấn đề") + "  |  Level " + Math.max(1, item.quest_level)
                + "  |  " + status(item));
        holder.goal.setText(text(item.quest_description, "No description"));
        holder.preview.setOnClickListener(v -> listener.onPreview(item));
        boolean pending = "pending_review".equals(item.approval_status);
        boolean approved = "approved".equals(item.approval_status);
        holder.approve.setVisibility(pending ? View.VISIBLE : View.GONE);
        holder.reject.setVisibility(pending || approved ? View.VISIBLE : View.GONE);
        holder.approve.setOnClickListener(v -> listener.onApprove(item));
        if (pending) {
            holder.reject.setText("Reject");
            holder.reject.setOnClickListener(v -> listener.onReject(item));
        } else if (approved) {
            holder.reject.setText(item.is_active ? "Hide" : "Restore");
            holder.reject.setOnClickListener(v -> listener.onVisibility(item, !item.is_active));
        }
    }

    @Override public int getItemCount() { return items.size(); }

    private static String text(String value, String fallback) {
        return value == null || value.trim().isEmpty() ? fallback : value;
    }

    private static String duration(Integer seconds) {
        if (seconds == null || seconds <= 0) return "Flexible duration";
        int minutes = Math.max(1, (int) Math.ceil(seconds / 60.0));
        return minutes + " min";
    }

    private static String status(QuestDraftSummary item) {
        if ("approved".equals(item.approval_status) && !item.is_active) return "Hidden";
        if ("pending_review".equals(item.approval_status)) return "Pending";
        if ("approved".equals(item.approval_status)) return "Visible";
        return text(item.approval_status, "Draft").replace('_', ' ');
    }

    static class Holder extends RecyclerView.ViewHolder {
        final TextView title, meta, goal;
        final Button preview, approve, reject;
        Holder(View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.tvReviewQuestTitle);
            meta = itemView.findViewById(R.id.tvReviewQuestMeta);
            goal = itemView.findViewById(R.id.tvReviewQuestGoal);
            preview = itemView.findViewById(R.id.btnReviewPreview);
            approve = itemView.findViewById(R.id.btnReviewApprove);
            reject = itemView.findViewById(R.id.btnReviewReject);
        }
    }
}
