package com.example.emotiondebugging.model.request;

import com.google.gson.annotations.SerializedName;

public class CreatePostRequest {
    @SerializedName("title") public String title;
    @SerializedName("content") public String content;
    @SerializedName("topic_id") public int topicId;
    @SerializedName("is_anonymous") public boolean isAnonymous;

    public CreatePostRequest(String title, String content, int topicId, boolean isAnonymous) {
        this.title = title;
        this.content = content;
        this.topicId = topicId;
        this.isAnonymous = isAnonymous;
    }
}
