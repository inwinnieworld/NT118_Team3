package com.example.emotiondebugging.model.community;

import com.google.gson.annotations.SerializedName;

public class CommunityProfile {

    @SerializedName("profile_id")
    private int profileId;

    @SerializedName("student_id")
    private int studentId;

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

    @SerializedName("major")
    private String major;

    @SerializedName("faculty")
    private String faculty;

    @SerializedName("year_of_study")
    private Integer yearOfStudy;

    @SerializedName("follower_count")
    private int followerCount;

    @SerializedName("following_count")
    private int followingCount;

    @SerializedName("post_count")
    private int postCount;

    @SerializedName("is_me")
    private boolean isMe;

    @SerializedName("followed_by_me")
    private boolean followedByMe;

    @SerializedName("music_url")
    private String musicUrl;

    @SerializedName("music_name")
    private String musicName;

    public CommunityProfile() {
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

    public int getProfileId() {
        return profileId;
    }

    public void setProfileId(int profileId) {
        this.profileId = profileId;
    }

    public int getStudentId() {
        return studentId;
    }

    public void setStudentId(int studentId) {
        this.studentId = studentId;
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

    public String getMajor() {
        return major;
    }

    public void setMajor(String major) {
        this.major = major;
    }

    public String getFaculty() {
        return faculty;
    }

    public void setFaculty(String faculty) {
        this.faculty = faculty;
    }

    public Integer getYearOfStudy() {
        return yearOfStudy;
    }

    public void setYearOfStudy(Integer yearOfStudy) {
        this.yearOfStudy = yearOfStudy;
    }

    public int getFollowerCount() {
        return followerCount;
    }

    public void setFollowerCount(int followerCount) {
        this.followerCount = followerCount;
    }

    public int getFollowingCount() {
        return followingCount;
    }

    public void setFollowingCount(int followingCount) {
        this.followingCount = followingCount;
    }

    public int getPostCount() {
        return postCount;
    }

    public void setPostCount(int postCount) {
        this.postCount = postCount;
    }

    public boolean isMe() {
        return isMe;
    }

    public void setMe(boolean me) {
        isMe = me;
    }

    public boolean isFollowedByMe() {
        return followedByMe;
    }

    public void setFollowedByMe(boolean followedByMe) {
        this.followedByMe = followedByMe;
    }

    public String getAvatarText() {
        if (displayName != null && !displayName.trim().isEmpty()) {
            return displayName.trim().substring(0, 1).toUpperCase();
        }

        if (username != null && !username.trim().isEmpty()) {
            return username.trim().substring(0, 1).toUpperCase();
        }

        return "?";
    }

    public String getFormattedUsername() {
        if (username == null || username.trim().isEmpty()) {
            return "@unknown";
        }

        if (username.startsWith("@")) {
            return username;
        }

        return "@" + username;
    }

    public String getStudentInfoText() {
        StringBuilder builder = new StringBuilder();

        if (faculty != null && !faculty.trim().isEmpty()) {
            builder.append(faculty.trim());
        }

        if (yearOfStudy != null) {
            if (builder.length() > 0) {
                builder.append(" · ");
            }
            builder.append("Năm ").append(yearOfStudy);
        }

        if (builder.length() == 0 && major != null && !major.trim().isEmpty()) {
            builder.append(major.trim());
        }

        return builder.toString();
    }
}
