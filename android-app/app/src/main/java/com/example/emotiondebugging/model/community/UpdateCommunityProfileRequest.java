package com.example.emotiondebugging.model.community;

import com.google.gson.annotations.SerializedName;

public class UpdateCommunityProfileRequest {

    @SerializedName("username")
    private String username;

    @SerializedName("display_name")
    private String displayName;

    @SerializedName("avatar_url")
    private String avatarUrl;

    @SerializedName("cover_url")
    private String coverUrl;

    @SerializedName("bio")
    private String bio;

    @SerializedName("music_url")
    private String musicUrl;

    @SerializedName("music_name")
    private String musicName;

    public UpdateCommunityProfileRequest() {
    }

    public UpdateCommunityProfileRequest(
            String username,
            String displayName,
            String avatarUrl,
            String coverUrl,
            String bio
    ) {
        this.username = username;
        this.displayName = displayName;
        this.avatarUrl = avatarUrl;
        this.coverUrl = coverUrl;
        this.bio = bio;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public String getCoverUrl() {
        return coverUrl;
    }

    public void setCoverUrl(String coverUrl) {
        this.coverUrl = coverUrl;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public String getMusicUrl() {
        return musicUrl;
    }

    public void setMusicUrl(String musicUrl) {
        this.musicUrl = musicUrl;
    }

    public String getMusicName() {
        return musicName;
    }

    public void setMusicName(String musicName) {
        this.musicName = musicName;
    }
}