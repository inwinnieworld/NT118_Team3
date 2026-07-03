package com.example.emotiondebugging.model.response;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class ChatConversationResponse {

    @SerializedName("conversations")
    public List<ConversationItem> conversations;

    public static class ConversationItem {

        @SerializedName("student_id")
        public int studentId;

        @SerializedName("display_name")
        public String displayName;

        @SerializedName("username")
        public String username;

        @SerializedName("avatar_url")
        public String avatarUrl;

        @SerializedName("avatar_text")
        public String avatarText;

        @SerializedName("last_message")
        public String lastMessage;

        @SerializedName("last_message_at")
        public String lastMessageAt;

        @SerializedName("unread_count")
        public int unreadCount;

        @SerializedName("follower_count")
        public int followerCount;

        @SerializedName("followed_by_me")
        public boolean followedByMe;
    }
}