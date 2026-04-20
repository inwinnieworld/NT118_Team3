package com.example.emotiondebugging.ui.staff;

import android.content.Context;
import android.content.Intent;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.emotiondebugging.R;
import com.example.emotiondebugging.model.response.TraceQuestionResponse;

import java.util.ArrayList;
import java.util.List;

public class TraceQuestionAdapter extends RecyclerView.Adapter<TraceQuestionAdapter.ViewHolder> {

    private final Context context;
    private final List<TraceQuestionGroupItem> items = new ArrayList<>();

    public TraceQuestionAdapter(Context context) {
        this.context = context;
    }

    public void submitList(List<TraceQuestionGroupItem> list) {
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
                .inflate(R.layout.item_trace_question_group, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        TraceQuestionGroupItem item = items.get(position);

        holder.tvErrorCode.setText(item.getErrorCode());
        holder.tvErrorName.setText(formatErrorName(item.getErrorName()));
        bindQuestions(holder.layoutQuestionContainer, item.getQuestions(), "");

        holder.edtSearchQuestion.setText("");
        holder.edtSearchQuestion.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                bindQuestions(holder.layoutQuestionContainer, item.getQuestions(), s.toString().trim());
            }

            @Override
            public void afterTextChanged(Editable s) { }
        });
    }

    private void bindQuestions(LinearLayout container, List<TraceQuestionResponse> questions, String keyword) {
        container.removeAllViews();

        List<TraceQuestionResponse> filtered = new ArrayList<>();
        for (TraceQuestionResponse q : questions) {
            if (keyword.isEmpty() || q.getQuestion_text().toLowerCase().contains(keyword.toLowerCase())) {
                filtered.add(q);
            }
        }

        if (filtered.isEmpty()) {
            TextView empty = new TextView(context);
            empty.setText("Chưa có câu hỏi nào.");
            empty.setTextSize(15f);
            empty.setTextColor(0xFF3A4A5A);
            container.addView(empty);
            return;
        }

        for (TraceQuestionResponse q : filtered) {
            View chipView = LayoutInflater.from(context).inflate(R.layout.item_trace_question_chip, container, false);
            TextView tvChip = chipView.findViewById(R.id.tvQuestionChip);
            tvChip.setText(q.getQuestion_text());

            tvChip.setOnClickListener(v -> {
                Intent intent = new Intent(context, TraceQuestionDetailActivity.class);
                intent.putExtra("question_id", q.getQuestion_id());
                context.startActivity(intent);
            });

            container.addView(chipView);
        }
    }

    private String formatErrorName(String raw) {
        if (raw == null) return "";
        if (raw.contains(":")) {
            return raw.substring(0, raw.indexOf(":")).toUpperCase();
        }
        return raw.toUpperCase();
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvErrorCode, tvErrorName;
        EditText edtSearchQuestion;
        LinearLayout layoutQuestionContainer;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvErrorCode = itemView.findViewById(R.id.tvErrorCode);
            tvErrorName = itemView.findViewById(R.id.tvErrorName);
            edtSearchQuestion = itemView.findViewById(R.id.edtSearchQuestion);
            layoutQuestionContainer = itemView.findViewById(R.id.layoutQuestionContainer);
        }
    }
}