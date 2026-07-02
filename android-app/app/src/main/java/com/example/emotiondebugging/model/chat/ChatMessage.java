package com.example.emotiondebugging.model.chat;

import com.google.gson.annotations.SerializedName;

public class ChatMessage {
    @SerializedName("message_id")
    public int messageId;

    @SerializedName("conversation_id")
    public int conversationId;

    @SerializedName("sender_student_id")
    public int senderStudentId;

    @SerializedName("receiver_student_id")
    public int receiverStudentId;

    @SerializedName("message_text")
    public String messageText;

    @SerializedName("is_read")
    public int isRead;

    @SerializedName("created_at")
    public String createdAt;
}