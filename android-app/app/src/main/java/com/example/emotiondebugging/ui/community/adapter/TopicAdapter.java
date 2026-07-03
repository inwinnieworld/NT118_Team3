package com.example.emotiondebugging.ui.community.adapter;

import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.emotiondebugging.R;
import com.example.emotiondebugging.model.community.TopicItem;

import java.util.ArrayList;
import java.util.List;

public class TopicAdapter extends RecyclerView.Adapter<TopicAdapter.TopicViewHolder> {

    public interface OnTopicClickListener {
        void onTopicClick(TopicItem topic);
    }

    private List<TopicItem> topics = new ArrayList<>();
    private final OnTopicClickListener listener;

    public TopicAdapter(OnTopicClickListener listener) {
        this.listener = listener;
    }

    public void setTopics(List<TopicItem> newTopics) {
        this.topics = newTopics != null ? newTopics : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public TopicViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_topic, parent, false);
        return new TopicViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TopicViewHolder holder, int position) {
        holder.bind(topics.get(position));
    }

    @Override
    public int getItemCount() {
        return topics.size();
    }

    class TopicViewHolder extends RecyclerView.ViewHolder {

        View colorDot;
        TextView tvName;
        TextView tvDesc;

        TopicViewHolder(@NonNull View itemView) {
            super(itemView);
            colorDot = itemView.findViewById(R.id.color_dot);
            tvName = itemView.findViewById(R.id.tv_topic_name);
            tvDesc = itemView.findViewById(R.id.tv_topic_desc);
        }

        void bind(TopicItem topic) {
            if (topic == null) return;

            tvName.setText(topic.topicName != null ? topic.topicName : "");
            tvDesc.setText(topic.topicDescription != null ? topic.topicDescription : "");

            try {
                if (topic.colorHex != null && !topic.colorHex.trim().isEmpty()) {
                    GradientDrawable bg = new GradientDrawable();
                    bg.setShape(GradientDrawable.OVAL);
                    bg.setColor(android.graphics.Color.parseColor(topic.colorHex));
                    colorDot.setBackground(bg);
                }
            } catch (Exception ignored) {
            }

            itemView.setOnClickListener(v -> {
                if (listener != null) listener.onTopicClick(topic);
            });
        }
    }
}
