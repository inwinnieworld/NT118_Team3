package com.example.emotiondebugging.model.community;

import com.google.gson.annotations.SerializedName;

public class TopicItem {
    @SerializedName("topic_id")
    public int topicId;

    @SerializedName("topic_name")
    public String topicName;

    @SerializedName("topic_description")
    public String topicDescription;

    @SerializedName("color_hex")
    public String colorHex;

    @SerializedName("icon_url")
    public String iconUrl;
}
