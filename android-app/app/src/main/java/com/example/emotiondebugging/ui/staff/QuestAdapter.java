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
import com.example.emotiondebugging.model.response.QuestDraftSummary;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class QuestAdapter extends RecyclerView.Adapter<QuestAdapter.Holder> implements Filterable {
    public interface Listener {
        void onOpen(QuestDraftSummary item);
        void onDelete(QuestDraftSummary item);
    }

    private final List<QuestDraftSummary> original = new ArrayList<>();
    private final List<QuestDraftSummary> filtered = new ArrayList<>();
    private final Listener listener;

    public QuestAdapter(Listener listener) { this.listener = listener; }

    public void submitList(List<QuestDraftSummary> list) {
        original.clear();
        filtered.clear();
        if (list != null) {
            original.addAll(list);
            filtered.addAll(list);
        }
        notifyDataSetChanged();
    }

    @NonNull @Override public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new Holder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_quest, parent, false));
    }

    @Override public void onBindViewHolder(@NonNull Holder holder, int position) {
        QuestDraftSummary item = filtered.get(position);
        String status = item.approval_status == null ? "draft" : item.approval_status;
        holder.title.setText((item.quest_title == null ? "Untitled quest" : item.quest_title)
                + "\n" + status.replace('_', ' ').toUpperCase(Locale.ROOT));

        boolean editable = "draft".equals(status) || "rejected".equals(status);
        holder.openLabel.setText(editable ? "Edit" : "View");
        holder.open.setOnClickListener(v -> listener.onOpen(item));
        holder.delete.setVisibility("draft".equals(status) ? View.VISIBLE : View.GONE);
        holder.delete.setOnClickListener(v -> listener.onDelete(item));
    }

    @Override public int getItemCount() { return filtered.size(); }

    @Override public Filter getFilter() {
        return new Filter() {
            @Override protected FilterResults performFiltering(CharSequence value) {
                String query = value == null ? "" : value.toString().trim().toLowerCase(Locale.ROOT);
                List<QuestDraftSummary> result = new ArrayList<>();
                for (QuestDraftSummary item : original) {
                    String title = item.quest_title == null ? "" : item.quest_title.toLowerCase(Locale.ROOT);
                    String category = item.error_name == null ? "" : item.error_name.toLowerCase(Locale.ROOT);
                    String status = item.approval_status == null ? "" : item.approval_status.toLowerCase(Locale.ROOT);
                    if (query.isEmpty() || title.contains(query) || category.contains(query) || status.contains(query)) result.add(item);
                }
                FilterResults results = new FilterResults();
                results.values = result;
                return results;
            }

            @SuppressWarnings("unchecked")
            @Override protected void publishResults(CharSequence value, FilterResults results) {
                filtered.clear();
                if (results.values != null) filtered.addAll((List<QuestDraftSummary>) results.values);
                notifyDataSetChanged();
            }
        };
    }

    static class Holder extends RecyclerView.ViewHolder {
        final TextView title, openLabel;
        final LinearLayout open, delete;
        Holder(View view) {
            super(view);
            title = view.findViewById(R.id.tvQuestName);
            open = view.findViewById(R.id.btnEdit);
            delete = view.findViewById(R.id.btnDelete);
            openLabel = view.findViewById(R.id.tvQuestOpenAction);
        }
    }
}
