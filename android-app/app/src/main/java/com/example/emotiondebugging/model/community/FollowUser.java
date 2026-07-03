package com.example.emotiondebugging.model.community;

import com.google.gson.annotations.SerializedName;

public class FollowUser {
    @SerializedName("student_id")
    public int studentId;

    @SerializedName("username")
    public String username;

    @SerializedName("display_name")
    public String displayName;

    @SerializedName("avatar_url")
    public String avatarUrl;

    @SerializedName("bio")
    public String bio;

    @SerializedName("followed_by_me")
    public int followedByMe;
}