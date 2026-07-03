package com.example.emotiondebugging.model.community;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class TopicListResponse {
    @SerializedName("topics")
    public List<TopicItem> topics;
}
