package com.example.emotiondebugging.model.community;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class FollowListResponse {
    @SerializedName("users")
    public List<FollowUser> users;
}