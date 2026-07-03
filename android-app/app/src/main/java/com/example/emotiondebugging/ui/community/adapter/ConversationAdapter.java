package com.example.emotiondebugging.ui.community.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.emotiondebugging.R;
import com.example.emotiondebugging.model.response.ChatConversationResponse;
import com.example.emotiondebugging.utils.AvatarHelper;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class ConversationAdapter extends RecyclerView.Adapter<ConversationAdapter.ConversationViewHolder> {

    public interface OnConversationClickListener {
        void onClick(ChatConversationResponse.ConversationItem item);
    }

    private final List<ChatConversationResponse.ConversationItem> items = new ArrayList<>();
    private final OnConversationClickListener listener;

    public ConversationAdapter(OnConversationClickListener listener) {
        this.listener = listener;
    }

    public void setItems(List<ChatConversationResponse.ConversationItem> newItems) {
        items.clear();

        if (newItems != null) {
            items.addAll(newItems);
        }

        android.util.Log.d("MSG_LIST", "Adapter item size = " + items.size());
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ConversationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_conversation, parent, false);
        return new ConversationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ConversationViewHolder holder, int position) {
        holder.bind(items.get(position));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    class ConversationViewHolder extends RecyclerView.ViewHolder {

        ImageView ivAvatar;
        TextView tvName;
        TextView tvUsername;
        TextView tvLastMessage;
        TextView tvUnread;

        ConversationViewHolder(@NonNull View itemView) {
            super(itemView);

            ivAvatar = itemView.findViewById(R.id.iv_avatar);
            tvName = itemView.findViewById(R.id.tv_name);

            // Nếu layout mới không còn tv_username thì dòng này sẽ trả null, không sao.
            tvUsername = itemView.findViewById(R.id.tv_username);

            tvLastMessage = itemView.findViewById(R.id.tv_last_message);
            tvUnread = itemView.findViewById(R.id.tv_unread);
        }

        void bind(ChatConversationResponse.ConversationItem item) {
            if (item == null) return;

            String name = getDisplayName(item);

            if (tvName != null) {
                tvName.setText(name);
            }

            // Giao diện Threads chỉ cần tên + last message, không cần dòng username riêng.
            if (tvUsername != null) {
                tvUsername.setVisibility(View.GONE);
            }

            if (ivAvatar != null) {
                AvatarHelper.loadAvatar(ivAvatar, item.avatarUrl, name);
            }

            if (tvLastMessage != null) {
                String lastMessage = item.lastMessage != null && !item.lastMessage.trim().isEmpty()
                        ? item.lastMessage.trim()
                        : "Chưa có tin nhắn";

                String timeText = formatRelativeTime(item.lastMessageAt);

                if (!timeText.isEmpty()) {
                    tvLastMessage.setText(lastMessage + " · " + timeText);
                } else {
                    tvLastMessage.setText(lastMessage);
                }
            }

            if (tvUnread != null) {
                if (item.unreadCount > 0) {
                    tvUnread.setVisibility(View.VISIBLE);
                    tvUnread.setText(String.valueOf(item.unreadCount));
                } else {
                    tvUnread.setVisibility(View.GONE);
                }
            }

            itemView.setOnClickListener(v -> {
                android.util.Log.d(
                        "CHAT_OPEN",
                        "Clicked conversation: studentId = " + item.studentId
                                + ", displayName = " + item.displayName
                                + ", username = " + item.username
                                + ", lastMessage = " + item.lastMessage
                );

                if (listener != null) {
                    listener.onClick(item);
                }
            });
        }

        private String getDisplayName(ChatConversationResponse.ConversationItem item) {
            if (item.displayName != null && !item.displayName.trim().isEmpty()) {
                return item.displayName.trim();
            }

            if (item.username != null && !item.username.trim().isEmpty()) {
                return item.username.trim();
            }

            if (item.avatarText != null && !item.avatarText.trim().isEmpty()) {
                return item.avatarText.trim();
            }

            return "Người dùng";
        }

        private String formatRelativeTime(String time) {
            if (time == null || time.trim().isEmpty()) {
                return "";
            }

            try {
                Date date = parseDate(time);
                if (date == null) return "";

                long diff = System.currentTimeMillis() - date.getTime();
                if (diff < 0) diff = 0;

                long minutes = diff / (1000 * 60);
                long hours = diff / (1000 * 60 * 60);
                long days = hours / 24;
                long weeks = days / 7;

                if (minutes < 1) return "now";
                if (minutes < 60) return minutes + "m";
                if (hours < 24) return hours + "h";
                if (days < 7) return days + "d";

                return weeks + "w";
            } catch (Exception e) {
                android.util.Log.e("MSG_TIME", "formatRelativeTime failed: " + time, e);
                return "";
            }
        }

        private Date parseDate(String time) {
            String[] patterns = new String[]{
                    "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
                    "yyyy-MM-dd'T'HH:mm:ss'Z'",
                    "yyyy-MM-dd HH:mm:ss",
                    "yyyy-MM-dd'T'HH:mm:ss.SSSXXX",
                    "yyyy-MM-dd'T'HH:mm:ssXXX"
            };

            for (String pattern : patterns) {
                try {
                    SimpleDateFormat sdf = new SimpleDateFormat(pattern, Locale.getDefault());

                    if (pattern.endsWith("'Z'")) {
                        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
                    }

                    return sdf.parse(time);
                } catch (Exception ignored) {
                }
            }

            return null;
        }
    }
}