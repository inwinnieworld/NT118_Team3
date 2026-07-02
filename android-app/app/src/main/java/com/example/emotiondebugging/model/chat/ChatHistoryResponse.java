package com.example.emotiondebugging.model.chat;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class ChatHistoryResponse {
    @SerializedName("current_student_id")
    public int currentStudentId;

    @SerializedName("target_student_id")
    public int targetStudentId;

    @SerializedName("messages")
    public List<ChatMessage> messages;
}