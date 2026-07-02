package com.example.emotiondebugging.model.response;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class CommunityPostResponse {

    @SerializedName("posts")
    public List<PostItem> posts;

    @SerializedName("total")
    public int total;

    @SerializedName("page")
    public int page;

    @SerializedName("totalPages")
    public int totalPages;

    public static class PostItem {

        @SerializedName("post_id")
        public int postId;

        @SerializedName("student_id")
        public int studentId;

        @SerializedName("title")
        public String title;

        @SerializedName("content")
        public String content;
        @SerializedName("image_url")
        public String imageUrl;

        // 0 = công khai, 1 = ẩn danh
        @SerializedName("is_anonymous")
        public int isAnonymous;

        @SerializedName("created_at")
        public String createdAt;

        /*
         * Phần cũ liên quan error type.
         * Tạm giữ lại để không lỗi các API/code cũ.
         * Adapter mới cũng có fallback: nếu chưa có hashtags thì dùng errorName như hashtag tạm.
         */
        @SerializedName("error_type_id")
        public int errorTypeId;

        @SerializedName("error_name")
        public String errorName;

        @SerializedName("author_name")
        public String authorName;

        @SerializedName("author_avatar")
        public String authorAvatar;

        @SerializedName("upvote_count")
        public int upvoteCount;

        @SerializedName("downvote_count")
        public int downvoteCount;

        @SerializedName("comment_count")
        public int commentCount;

        @SerializedName("view_count")
        public int viewCount;

        // 0 = chưa lưu, 1 = đã lưu
        @SerializedName("is_saved")
        public int isSaved;

        @SerializedName("repost_count")
        public int repostCount;

        @SerializedName("is_reposted")
        public int isReposted;

        @SerializedName(value = "hashtags", alternate = {"topics"})
        public List<String> hashtags;
    }
}