package com.example.emotiondebugging.model.response;

import com.google.gson.annotations.SerializedName;

public class BaseResponse {
    @SerializedName("message") public String message;
    @SerializedName("avatar_url") public String avatarUrl; // dùng cho upload avatar
}
