package com.example.emotiondebugging.model.request;

import com.google.gson.annotations.SerializedName;

public class CreatePostRequest {
    @SerializedName("title") public String title;
    @SerializedName("content") public String content;
    @SerializedName("error_type_id") public int errorTypeId;
    @SerializedName("is_anonymous") public boolean isAnonymous;

    public CreatePostRequest(String title, String content, int errorTypeId, boolean isAnonymous) {
        this.title = title;
        this.content = content;
        this.errorTypeId = errorTypeId;
        this.isAnonymous = isAnonymous;
    }
}
