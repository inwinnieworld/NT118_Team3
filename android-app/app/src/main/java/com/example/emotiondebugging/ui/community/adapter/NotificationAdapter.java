package com.example.emotiondebugging.ui.community.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.emotiondebugging.R;
import com.example.emotiondebugging.model.community.NotificationItem;

import java.util.ArrayList;
import java.util.List;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder> {

    public interface OnNotificationClickListener {
        void onNotificationClick(NotificationItem item);
        void onReviewRequest(NotificationItem item);
    }

    private List<NotificationItem> items = new ArrayList<>();
    private final OnNotificationClickListener listener;

    public NotificationAdapter(OnNotificationClickListener listener) {
        this.listener = listener;
    }

    public void setItems(List<NotificationItem> newItems) {
        this.items = newItems != null ? newItems : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public NotificationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_notification, parent, false);
        return new NotificationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NotificationViewHolder holder, int position) {
        holder.bind(items.get(position));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    class NotificationViewHolder extends RecyclerView.ViewHolder {

        View dotUnread;
        TextView tvTitle;
        TextView tvTime;
        TextView tvBody;
        Button btnReviewRequest;

        NotificationViewHolder(@NonNull View itemView) {
            super(itemView);
            dotUnread = itemView.findViewById(R.id.dot_unread);
            tvTitle = itemView.findViewById(R.id.tv_title);
            tvTime = itemView.findViewById(R.id.tv_time);
            tvBody = itemView.findViewById(R.id.tv_body);
            btnReviewRequest = itemView.findViewById(R.id.btn_review_request);
        }

        void bind(NotificationItem item) {
            if (item == null) return;

            tvTitle.setText(item.title != null ? item.title : "");
            tvBody.setText(item.body != null ? item.body : "");
            tvTime.setText(formatTime(item.createdAt));

            dotUnread.setVisibility(item.isRead == 0 ? View.VISIBLE : View.INVISIBLE);

            // Chỉ thông báo bài bị ẩn mới cho phép gửi yêu cầu xem xét.
            boolean canReview = "post_hidden".equals(item.type)
                    && item.relatedPostId != null
                    && item.relatedCommentId == null;
            btnReviewRequest.setVisibility(canReview ? View.VISIBLE : View.GONE);
            btnReviewRequest.setOnClickListener(v -> {
                if (listener != null) listener.onReviewRequest(item);
            });

            itemView.setOnClickListener(v -> {
                if (listener != null) listener.onNotificationClick(item);
            });
        }

        private String formatTime(String createdAt) {
            if (createdAt == null || createdAt.trim().isEmpty()) return "";
            try {
                java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat(
                        "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", java.util.Locale.getDefault());
                sdf.setTimeZone(java.util.TimeZone.getTimeZone("UTC"));
                java.util.Date date = sdf.parse(createdAt);
                if (date == null) return "";
                long diff = System.currentTimeMillis() - date.getTime();
                long minutes = diff / (1000 * 60);
                long hours = diff / (1000 * 60 * 60);
                if (minutes < 1) return "now";
                if (minutes < 60) return minutes + " m";
                if (hours < 24) return hours + " h";
                return (hours / 24) + " d";
            } catch (Exception e) {
                return "";
            }
        }
    }
}
