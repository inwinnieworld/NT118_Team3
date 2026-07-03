package com.example.emotiondebugging.model.community;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class NotificationListResponse {
    @SerializedName("notifications")
    public List<NotificationItem> notifications;

    @SerializedName("total")
    public int total;
}
