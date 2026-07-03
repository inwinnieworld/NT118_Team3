package com.example.emotiondebugging.model.community;

import com.google.gson.annotations.SerializedName;

public class NotificationItem {

    @SerializedName("notification_id")
    public int notificationId;

    @SerializedName("type")
    public String type;

    @SerializedName("title")
    public String title;

    @SerializedName("body")
    public String body;

    @SerializedName("related_post_id")
    public Integer relatedPostId;

    @SerializedName("related_comment_id")
    public Integer relatedCommentId;

    @SerializedName("is_read")
    public int isRead;

    @SerializedName("created_at")
    public String createdAt;
}
